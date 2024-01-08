package kr.co.wisenut.textminer.dictionary.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import kr.co.wisenut.exception.UnsupportedFileException;
import kr.co.wisenut.textminer.common.TextMinerConstants;
import kr.co.wisenut.textminer.common.mapper.ImportProgressMapper;
import kr.co.wisenut.textminer.common.resource.ExportProgress;
import kr.co.wisenut.textminer.common.resource.FileType;
import kr.co.wisenut.textminer.common.resource.ImportErrorHandle;
import kr.co.wisenut.textminer.common.resource.ProgressState;
import kr.co.wisenut.textminer.common.resource.StagingFileInfo;
import kr.co.wisenut.textminer.common.resource.StorageResourceType;
import kr.co.wisenut.textminer.common.service.StorageService;
import kr.co.wisenut.textminer.common.vo.ImportProgressVo;
import kr.co.wisenut.textminer.dictionary.mapper.DictionaryMapper;
import kr.co.wisenut.textminer.dictionary.mapper.EntryMapper;
import kr.co.wisenut.textminer.dictionary.vo.DictionaryVo;
import kr.co.wisenut.textminer.dictionary.vo.EntryVo;
import kr.co.wisenut.textminer.dictionary.vo.SynonymVo;
import kr.co.wisenut.textminer.history.service.ActionHistoryService;
import kr.co.wisenut.textminer.history.vo.ActionHistoryVo;
import kr.co.wisenut.textminer.user.vo.TmUser;
import kr.co.wisenut.util.DictionaryFileParser;
import kr.co.wisenut.util.PageUtil;

@Service
public class EntryService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private DictionaryMapper dictionaryMapper;
	
	@Autowired
	private EntryMapper entryMapper;
	
	@Autowired
	private ImportProgressMapper importProgressService;
	
	@Autowired
	private ActionHistoryService actionHistoryService;

	@Autowired
	private StorageService storageService;
	
	public List<String> logTextList;
	
	
	// 엔트리 리스트 조회
	public Map<String, Object> getEntryList(Map<String, Object> paramMap) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			// 사전 상세조회
			DictionaryVo dictionaryVo = new DictionaryVo();
			dictionaryVo.setDictionaryId(Integer.parseInt(paramMap.get("dictionaryId").toString()));
			dictionaryVo.setDictionaryOwner(paramMap.get("dictionaryOwner").toString());
			dictionaryVo.setRole(paramMap.get("role").toString());
			dictionaryVo = dictionaryMapper.getDictionaryDetail(dictionaryVo);
			
			// 엔트리 전체 건수
			int totalCount = entryMapper.getEntryTotalCount(paramMap);
			resultMap.put("totalCount", totalCount);
			
			// 조회결과 리스트
			List<EntryVo> resultList = entryMapper.getEntryList(paramMap);
			resultMap.put("dataTable", convertHtmlTagForEntryList( resultList
																 , Integer.parseInt(paramMap.get("dictionaryId").toString())
																 , dictionaryVo.getDictionaryType()
																 , Integer.parseInt(paramMap.get("pageRow").toString())));
			
			// 페이징
			resultMap.put("pageNav", PageUtil.createPageNav(totalCount, paramMap));
			
		} catch (NullPointerException e) {
			logger.error("조회 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("조회 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	// 엔트리 등록
	public Map<String, Object> chkDuplicateEntry(int dictionaryId, String entry){
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		int result = 0;

		try {
			EntryVo entryVo = new EntryVo();
			entryVo.setDictionaryId(dictionaryId);
			entryVo.setEntryContent(entry);
			
			// 동일 사전 내에 중복 엔트리 확인
			result = entryMapper.checkDuplicatedEntry(entryVo);
			if (result > 0) {
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "해당 사전에 이미 등록된 엔트리가 존재합니다.");
				return resultMap;
			}
			
			// 수용어 및 불용어 사전 등록 시, 중복 엔트리 체크
			DictionaryVo dictionaryVo = new DictionaryVo();
			dictionaryVo.setDictionaryId(dictionaryId);
			dictionaryVo.setRole("[ADMIN]");
			dictionaryVo = dictionaryMapper.getDictionaryDetail(dictionaryVo);
			String dicName = null;
			
			switch(dictionaryVo.getDictionaryType()) {
				case TextMinerConstants.DICTIONARY_TYPE_WHITE:
					entryVo.setDictionaryType(TextMinerConstants.DICTIONARY_TYPE_BLACK);
					dicName = entryMapper.checkDuplicatedEntryOthers(entryVo);
					break;
				case TextMinerConstants.DICTIONARY_TYPE_BLACK:
					entryVo.setDictionaryType(TextMinerConstants.DICTIONARY_TYPE_WHITE);
					dicName = entryMapper.checkDuplicatedEntryOthers(entryVo);
					break;
			}
			
			if (dicName != null) {
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "다른 사전에 이미 등록된 엔트리가 존재합니다.\n등록사전 : " + dicName);
				return resultMap;
			}
			
			// 등록하려는 엔트리가 동의어에 등록된 단어인지 체크
			SynonymVo dupSynonymVo = new SynonymVo();
			dupSynonymVo.setSynonymContent(entryVo.getEntryContent());
			
			if (entryMapper.checkDuplicatedSynonym(dupSynonymVo) > 0) {
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "다른 사전에 이미 등록된 엔트리가 존재합니다.\n등록사전 : " + dicName);
				return resultMap;
			}

			resultMap.put("result", "S");
			resultMap.put("resultMsg", "등록가능");
			
		} catch (NullPointerException e) {
			logger.error("엔트리 중복체크 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("엔트리 중복체크 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	// 엔트리 등록
	public Map<String, Object> insertEntry(int dictionaryId, String entry){
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		int result = 0;

		try {
			EntryVo entryVo = new EntryVo();
			entryVo.setDictionaryId(dictionaryId);
			entryVo.setEntryContent(entry);
			
			result = entryMapper.insertEntry(entryVo);
			
			if (result > 0) {
				resultMap.put("result", "S");
				resultMap.put("resultMsg", "엔트리 추가 작업이 완료되었습니다.");
			} else {
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "엔트리 추가 작업을 실패하였습니다.");
			}
		} catch (NullPointerException e) {
			logger.error("엔트리 등록 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("엔트리 등록 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	// 엔트리 삭제
	public Map<String, Object> deleteEntry(int dictionaryId, String... id){
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		String [] ids = id;
		
		// 엔트리 및 동의어 삭제
		int result = 0;
		StringBuffer entries = new StringBuffer();	// 삭제된 엔트리
		
		try {
			EntryVo entryVo = null;
			SynonymVo synonymVo = null;
			
			for (int i = 0; i < ids.length; i++) {
				// 엔트리 삭제
				entryVo = new EntryVo();
				entryVo.setDictionaryId(dictionaryId);
				entryVo.setEntryId(Integer.parseInt(id[i]));
				
				result += entryMapper.deleteEntry(entryVo);
				
				// 동의어 삭제
				synonymVo = new SynonymVo();
				synonymVo.setDictionaryId(dictionaryId);
				synonymVo.setEntryId(entryVo.getEntryId());
	
				result += entryMapper.deleteSynonym(synonymVo);
			}
			
			if (result > 0) {
				resultMap.put("result", "S");
				resultMap.put("resultMsg", "엔트리 삭제 작업이 완료되었습니다.");
			} else {
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "엔트리 삭제 작업을 실패하였습니다.");
			}
		} catch (NullPointerException e) {
			logger.error("엔트리 삭제 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("엔트리 삭제 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	// 동의어 변경
	public Map<String, Object> modifySynonym(Map<String, Object> paramMap) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		String [] synonyms = paramMap.get("synonyms").toString().split(";");

		int result = 0;
		SynonymVo synonymVo = null;
		StringBuffer failedSynonyms = new StringBuffer();			// 등록 실패 동의어
		List<String> checkedSynonyms = new ArrayList<String>();		// 등록 가능 동의어
		
		// 동의어 Validation check
		for (int i = 0 ; i < synonyms.length; i++) {
//			유효성검사 주석처리
//			if (!Pattern.matches("^[ㄱ-ㅎ가-힣0-9a-zA-Z]+$", synonyms[i].trim())) {
//				if (!synonyms[i].equals("")) {
//					failedSynonyms.append(synonyms[i].trim() + ",");
//				}
//			} else {
//				checkedSynonyms.add(synonyms[i].trim());
//			}
			
			checkedSynonyms.add(synonyms[i].trim());
		}
		
		try {
			// 기존 동의어 삭제
			synonymVo = new SynonymVo();
			synonymVo.setDictionaryId(Integer.parseInt(paramMap.get("dictionaryId").toString()));
			synonymVo.setEntryId(Integer.parseInt(paramMap.get("entryId").toString()));
			entryMapper.deleteSynonym(synonymVo);
			
			// 신규 동의어 등록
			for (int i = 0; i < checkedSynonyms.size(); i++) {
				synonymVo.setSynonymContent(checkedSynonyms.get(i));
				result += entryMapper.insertSynonym(synonymVo);
			}
			
			if (result > 0) {
				resultMap.put("result", "S");
				
				if (!failedSynonyms.toString().equals("")) {
					resultMap.put("resultMsg", "동의어 변경 작업이 완료되었습니다.\n등록 실패 동의어 : " + failedSynonyms.toString());
				} else {
					resultMap.put("resultMsg", "동의어 변경 작업이 완료되었습니다.");
				}
			} else {
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "동의어 변경 작업을 실패하였습니다.");
			}
		} catch (NullPointerException e) {
			resultMap.put("result", "F");
			resultMap.put("resultMsg", "동의어 변경 작업을 실패하였습니다.");
			logger.error("동의어 변경 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			resultMap.put("result", "F");
			resultMap.put("resultMsg", "동의어 변경 작업을 실패하였습니다.");
			logger.error("동의어 변경 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	// 파일 업로드 처리
	public LinkedHashMap<String, Map> importAllFile(DictionaryVo dictionary, ImportErrorHandle errorHandle, List<Path> stagedFiles, TmUser user) {

        logger.debug("{}\ttm-dictionary entry-import prepare. errorHandle={}, stagedFiles={}, {}",
                Thread.currentThread(), errorHandle, stagedFiles, dictionary);

        logger.info("{}\ttm-dictionary entry-import start. errorHandle={}, stagedFiles={}, {}.",
                Thread.currentThread(), errorHandle, stagedFiles, dictionary);

        LinkedHashMap<String, Map> totalResult = new LinkedHashMap<>();
        for (Path stagedFile : stagedFiles) {
            long start = System.currentTimeMillis();
            logger.info("{}\tfile parsing and import start. {}'", Thread.currentThread(), stagedFile.toUri());

            Map<String, Long> importResult = importEntries(dictionary, errorHandle, stagedFile, user);
            totalResult.put(stagedFile.getFileName().toString(), importResult);

            // Remove file whether successful or unsuccessful.
            try {
                Files.deleteIfExists(stagedFile);
            } catch (IOException | SecurityException e) {
                logger.warn("{}\timported file deletion failed. file={}, cause={}", Thread.currentThread(), stagedFile.toUri(), e.getMessage());
            }

            logger.info("{}\tfile parsing and import complete. {}ms, {}", Thread.currentThread(), (System.currentTimeMillis() - start), stagedFile.toUri());
        }

        logger.info("{}\ttm-dictionary entry-import complete. {}", Thread.currentThread(), totalResult);

        return totalResult;
    }
	
	/**
     * 사전 파일 업로드
     * @param dictionary
     * @param errorHandle
     * @param stagedFile
     * @return
     */
    public LinkedHashMap<String, Long> importEntries(DictionaryVo dictionary, ImportErrorHandle errorHandle, Path stagedFile, TmUser user) {
        LinkedHashMap<String, Long> resultInfo = new LinkedHashMap<>();

        long succesCount = 0l;
        long failCount = 0l;
        long dupCount = 0l;
        
        List<String> dupList = new ArrayList<String>();
        List<String> otherDupList = new ArrayList<String>();
        List<String> dupEntrySynonymList = new ArrayList<String>();
        List<String> dupSynonymList = new ArrayList<String>();

    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        
    	// 업로드 결과를 저장하기위한 ImportProgressVo 객체 생성
        ImportProgressVo progress = new ImportProgressVo( TextMinerConstants.PROGRESS_TYPE_DICTIONARY
													    , dictionary.getDictionaryId()
													    , stagedFile.getFileName().toString()
													    , sdf.format(new Date(stagedFile.toFile().lastModified()))
													    , stagedFile.toFile().length()
													    , 0
													    , 0
													    , errorHandle.toString()
													    , null);
        
        progress.setProgress(TextMinerConstants.PROGRESS_STATUS_IN_PROGRESS);

        // ImportProgress 에러로그를 담기위한 List<String> 초기화
        logTextList = new ArrayList<String>();
        
        logger.info("{}\tentry parsing start. fileName={}", Thread.currentThread(), stagedFile.getFileName());

        try {
            // before parse, validate encoding, malformed, ... --> throw unsupported file exception
            StagingFileInfo.validate(StorageResourceType.DICTIONARY, stagedFile.toFile());

            List<String> errorEntries = null;
            
            if (progress.getLogText() != null) {
            	errorEntries = Arrays.asList(progress.getLogText().split("\\|"));
            	failCount = errorEntries.size();
            } 
            
            List<EntryVo> entries = DictionaryFileParser
				                    .path(stagedFile)
//            						.findAll(dictionary.getDictionaryType().hasCategory(), errorEntries, errorHandle);
				                    .findAll(false, errorEntries, errorHandle);

            if (entries.size() > 0) {  // 엔트리 파싱 성공

            	// 수용어, 불용어 사전 업로드 시 다른 사전에서 중복된 단어가 나오는 경우를 체크하기위한 변수
            	String dicName = null;
            	
            	// 파싱된 데이터 저장처리
            	for (int i = 0; i < entries.size(); i++) {

            		EntryVo entry = entries.get(i);
            		entry.setDictionaryId(dictionary.getDictionaryId());
            		entry.setDictionarySharedYn(dictionary.getDictionarySharedYn());
            		entry.setDictionaryType(dictionary.getDictionaryType());
            		entry.setEntryContent(entry.getEntryContent().trim());
            	
            		// String Matcher는 한 행이 entry로 저장되도록 적용
            		if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_GROUP_PATTERN)) {
            			entry.setEntryContent(entry.getEntryContent() + "|" + entry.getSynonyms().replace(";", ","));
            		}
            		
            		// 중복여부 확인 후 엔트리 및 동의어 등록
            		if (entryMapper.checkDuplicatedEntry(entry) > 0) {
            			dupCount++;
            			dupList.add(entry.getEntryContent());
//            		다른 사전 중복검사 (사용안함, 주석처리)
//            		} else if (entryMapper.checkDuplicatedEntryInOthers(entry) > 0) {
//            			failCount++;
            		} else {
            			
            			if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_WHITE)
            			 || dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_BLACK)) {
	            			switch (dictionary.getDictionaryType()) {
		            			case TextMinerConstants.DICTIONARY_TYPE_WHITE:
		            				entry.setDictionaryType(TextMinerConstants.DICTIONARY_TYPE_BLACK);
		            				dicName = entryMapper.checkDuplicatedEntryOthers(entry);
		            				break;
		            			case TextMinerConstants.DICTIONARY_TYPE_BLACK:
		            				entry.setDictionaryType(TextMinerConstants.DICTIONARY_TYPE_WHITE);
		            				dicName = entryMapper.checkDuplicatedEntryOthers(entry);
		            				break;
	            			}
	            			
	            			if (dicName != null) {
	            				dupCount++;
	                			otherDupList.add(entry.getEntryContent());
	            				continue;
	            			} else {
	            				// 등록하려는 엔트리가 동의어에 등록된 단어인지 체크
	            				SynonymVo dupSynonymVo = new SynonymVo();
	            				dupSynonymVo.setSynonymContent(entry.getEntryContent());
	            				
	            				if (entryMapper.checkDuplicatedSynonym(dupSynonymVo) > 0) {
		            				dupCount++;
		                			dupEntrySynonymList.add(entry.getEntryContent());
		            				continue;
	            				}
	            			}
	            		}
            			
            			// 등록 후 ID 가져오기
            			entryMapper.insertEntry(entry);
            			int entryId = entryMapper.getMaxEntryId(entry);

            			// 동의어 등록(수용어)
            			if (dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_WHITE)) {
	            			if (entry.getSynonyms() != null) {
	            				
	            				String [] synonyms = entry.getSynonyms().split(";");
	            				
	            				for (int j = 0; j < synonyms.length; j++) {
	            					SynonymVo synonym = new SynonymVo();
	
	            					synonym.setEntryId(entryId);
	            					synonym.setDictionaryId(dictionary.getDictionaryId());
	            					synonym.setSynonymContent(synonyms[j].trim());
	            					
	            					// 중복검사 후 저장
	            					if (!synonym.getSynonymContent().equals("") && synonym.getSynonymContent() != null) {
		            					if (entryMapper.checkDuplicatedSynonym(synonym) == 0) {
		            						entryMapper.insertSynonym(synonym);
		            					} else {
		            						dupSynonymList.add(synonym.getSynonymContent());
		            					}
	            					}
	            				}
	            			}
            			}

            			succesCount++;
            		}
            	}
            }

            progress.setTotalCount(Long.valueOf(succesCount + failCount).intValue());
            progress.setValidCount(Long.valueOf(succesCount).intValue());
            
            if (succesCount == 0l) {
            	if (dupCount > 0l) {
            		logTextList.add("insert failed. -> 업로드 파일 내에 데이터가 이미 등록되어있음.");
            		logTextList.add("동일 사전 내 중복 데이터 -> " + dupList.toString());
            		logTextList.add("다른 사전 내 중복 데이터 -> " + otherDupList.toString());
            		logTextList.add("엔트리 - 동의어 중복 데이터 -> " + dupEntrySynonymList.toString());
            		logTextList.add("동의어 중복 데이터 -> " + dupSynonymList.toString());
            	} else {
            		logTextList.add("insert failed.");
            	}
            	progress.setProgress(TextMinerConstants.PROGRESS_STATUS_FAILURE);
            } else if (failCount == 0l) {
            	progress.setProgress(TextMinerConstants.PROGRESS_STATUS_SUCCESS);
            	
                // 이력저장
				ActionHistoryVo actionHistoryVo = new ActionHistoryVo();

		        actionHistoryVo.setActionUser(user.getUsername());
		        actionHistoryVo.setResourceId(String.valueOf(dictionary.getDictionaryId()));
		        actionHistoryVo.setResourceType(TextMinerConstants.PROGRESS_TYPE_DICTIONARY);
		        actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_ENTRY_UPLOAD);
		        actionHistoryVo.setActionMsg(user.getUsername() + " 사전 엔트리 업로드 ( 대상 사전 : " + dictionary.getDictionaryName() + ", 등록 건수 : " + (succesCount - failCount) + "/" + succesCount + "건 )");
		        actionHistoryVo.setUserIp(user.getUserIp());
		        
		        actionHistoryService.insertActionHistory(actionHistoryVo);
            } else {
                logTextList.add("Duplicated Word deleted. (" + failCount + ")");
        		logTextList.add("동일 사전 내 중복 데이터 -> " + dupList.toString());
        		logTextList.add("다른 사전 내 중복 데이터 -> " + otherDupList.toString());
        		logTextList.add("엔트리 - 동의어 중복 데이터 -> " + dupEntrySynonymList.toString());
        		logTextList.add("동의어 중복 데이터 -> " + dupSynonymList.toString());
            	progress.setProgress(TextMinerConstants.PROGRESS_STATUS_PARTIAL_SUCCESS);

                // 이력저장
				ActionHistoryVo actionHistoryVo = new ActionHistoryVo();

		        actionHistoryVo.setActionUser(user.getUsername());
		        actionHistoryVo.setResourceId(String.valueOf(dictionary.getDictionaryId()));
		        actionHistoryVo.setResourceType(TextMinerConstants.PROGRESS_TYPE_DICTIONARY);
		        actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_ENTRY_UPLOAD);
		        actionHistoryVo.setActionMsg(user.getUsername() + " 사전 엔트리 부분 업로드 ( 대상 사전 : " + dictionary.getDictionaryName() + ", 등록 건수 : " + (succesCount - failCount) + "/" + succesCount + "건 )");
		        actionHistoryVo.setUserIp(user.getUserIp());
		        
		        actionHistoryService.insertActionHistory(actionHistoryVo);
            }

        } catch (UnsupportedFileException | IOException e) {
            logger.error("{} file parsing and import failed. {}, cause={}", Thread.currentThread(), stagedFile.toUri(), e.getMessage());
            logTextList.add("Failed to import entries. " + e.getMessage());
            progress.setProgress(TextMinerConstants.PROGRESS_STATUS_FAILURE);
        }

        progress.setLogText(String.join("\\|", logTextList));
        importProgressService.insertImportProgress(progress);  // 오류엔트리 저장

        resultInfo.put("success", succesCount);
        resultInfo.put("failure", failCount);

        logger.info("{}\tentry-parsing and insert complete. Number of documents {valid '{}' of readable '{}'} in '{}'.",
                Thread.currentThread(), succesCount, succesCount+failCount, stagedFile.getFileName());


        return resultInfo;
    }
    
    /**
     * Synchronously export data as file. 동기식으로 엔트리 익스포트.
     *
     * @param dictionaryId
     * @param exportFormat
     * @param username
     * @return
     */
    public ExportProgress<DictionaryVo> exportEntries(TmUser user, DictionaryVo dictionaryVo, FileType exportFormat, String username) {

        ExportProgress<DictionaryVo> exportProgress = ExportProgress.<DictionaryVo>builder().requestedTime(LocalDateTime.now())
                .resourceInfo(dictionaryVo)
                .fileType(exportFormat)
                .username(username)
                .build();
        exportProgress.setInProgress();

        logger.debug("{}\ttm-dictionary entry-export start. {}", Thread.currentThread(), dictionaryVo);

        // 경로, 파일 생성 - 파일명= 컬렉션명 + 타임스탬프
        Path exportFilePath = storageService.exportPath(StorageResourceType.DICTIONARY, (long) dictionaryVo.getDictionaryId(), dictionaryVo.getDictionaryName(), exportFormat);

        // write to file.
        switch (exportFormat) {
            case CSV:
                exportCsv(dictionaryVo, exportFilePath);
                break;
            default:
                logger.warn("tm-dictionary can not export as {}. use default format(=txt).", exportFormat);
            case TEXT:
                exportText(dictionaryVo, exportFilePath);
                break;
        }

        exportProgress.setComplete(exportFilePath);

        logger.info("{}\ttm-dictionary entry-export complete. {}", Thread.currentThread(), exportProgress);

        // return path of file.
        return exportProgress;
    }

    /**
     * 해당 사전의 entry 를 csv 파일로 저장
     *
     * @param dictionary
     * @param exportFilePath
     */
    private void exportCsv(@NotNull DictionaryVo dictionary, @NotNull Path exportFilePath) {
        CsvSchema.Builder csvSchemaBuilder = CsvSchema.builder();

        csvSchemaBuilder.addColumn("entry", CsvSchema.ColumnType.STRING);
        if(dictionary.getDictionaryType().equals(TextMinerConstants.DICTIONARY_TYPE_WHITE)) {
        	csvSchemaBuilder.addColumn("synonyms", CsvSchema.ColumnType.STRING);
        }

        CsvMapper mapper = new CsvMapper();
        mapper.configure(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS, true)
                .configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);

        ObjectWriter objectWriter = mapper.writerFor(HashMap.class)
                .with(csvSchemaBuilder.build().withHeader());

        // open stream : auto closable resources
        try {

        	BufferedWriter bufferedWriter = Files.newBufferedWriter(exportFilePath, StandardCharsets.UTF_8);
            SequenceWriter sequenceWriter = objectWriter.writeValues(bufferedWriter);
            
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("dictionaryId", dictionary.getDictionaryId());
            paramMap.put("pageSize", 1);
            List<EntryVo> entryList = entryMapper.getEntryList(paramMap);
        	
            int writeCount = 0;

            // foreach remaining - write entry
            for (int i = 0; i < entryList.size(); i++) {
            	try {
            		EntryVo entry = entryList.get(i);
            		HashMap<String, String> resultMap = new HashMap<String, String>();
            		
            		if (entry.getSynonyms().equals("") || entry.getSynonyms() == null) {
            			resultMap.put("entry", entry.getEntryContent());
            		} else {
            			resultMap.put("entry", entry.getEntryContent());
            			resultMap.put("synonyms", entry.getSynonyms());
            		}
            		
            		sequenceWriter.write(resultMap);
                    writeCount += 1;
            	} catch (IOException e) {
                    logger.debug("{}\twrite entry failed. entry={}, cause={}", Thread.currentThread(), entryList.get(i), e.getMessage());
                }
            }
            
            sequenceWriter.flush();

            // end file write
            logger.info("{}\tcsv export complete. entry.count={}, {}", Thread.currentThread(), writeCount, exportFilePath.toUri());

        } catch (Exception e) {
            logger.error("{}\ttm-dictionary entry-export failed. dictionary={} cause={}", Thread.currentThread(), dictionary, e.toString());
            logger.trace("{}", e);
        }
    }

    /**
     * 해당 사전의 entry 를 txt 파일로 저장
     *
     * @param dictionary
     * @param exportFilePath
     */
    private void exportText(@NotNull DictionaryVo dictionary, @NotNull Path exportFilePath) {
        // open stream : auto closable resources
        try {
        	BufferedWriter bufferedWriter = Files.newBufferedWriter(exportFilePath, StandardCharsets.UTF_8);

        	Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("dictionaryId", dictionary.getDictionaryId());
            paramMap.put("pageSize", 1);
            List<EntryVo> entryList = entryMapper.getEntryList(paramMap);
        	
            int writeCount = 0;
            
            // foreach remaining - write entry
            for (int i = 0; i < entryList.size(); i++) {
            	try {
            		EntryVo entry = entryList.get(i);
            		if (entry.getSynonyms().equals("") || entry.getSynonyms() == null) {
            			bufferedWriter.append(entry.getEntryContent() + "\n");
            		} else {
            			bufferedWriter.append(entry.getEntryContent() + "|" + entry.getSynonyms() + "\n");
            		}
                    writeCount += 1;
            	} catch (IOException e) {
                    logger.debug("{}\twrite entry failed. entry={}, cause={}", Thread.currentThread(), entryList.get(i), e.getMessage());
                }
            }
            
            bufferedWriter.flush();

            // end file write
            logger.info("{}\ttxt export complete. entry.count={}, {}", Thread.currentThread(), writeCount, exportFilePath.toUri());

        } catch (Exception e) {
            logger.error("{}\ttm-dictionary entry-export failed. dictionary={} cause={}", Thread.currentThread(), dictionary, e.toString());
            logger.trace("{}", e);
        }
    }
    
    // 조회결과 HTML로 변환
 	public String convertHtmlTagForEntryList(List<EntryVo> resultList, int documentId, String dictionaryType, int pageRow) {
 		StringBuffer convertHtml = new StringBuffer();
 		
 		// colgroup 시작
 		convertHtml.append("<colgroup>\n");
 		convertHtml.append("\t<col width=\"6%\">");
 		convertHtml.append("\t<col width=\"6%\">");

 		// 수용어 사전의 경우, 동의어 항목 추가
 		if (dictionaryType.equals(TextMinerConstants.DICTIONARY_TYPE_WHITE)) {
	 		convertHtml.append("\t<col width=\"10%\">");
	 		convertHtml.append("\t<col width=\"*\">");
 		} else {
 			convertHtml.append("\t<col width=\"*\">");
 		}
 		convertHtml.append("</colgroup>\\n");
 		// colgroup 종료
 		
 		// thead 시작
 		convertHtml.append("<thead>\n");
 		convertHtml.append("\t<th>선택</th>");
 		convertHtml.append("\t<th>No.</th>");
 		convertHtml.append("\t<th>단어</th>");
 		if (dictionaryType.equals(TextMinerConstants.DICTIONARY_TYPE_WHITE)) convertHtml.append("\t<th>동의어</th>");
 		convertHtml.append("</thead>\\n");
 		// thead 종료

 		// tbody 시작
 		convertHtml.append("<tbody>\n");

 		if (resultList == null || resultList.isEmpty()) {
			convertHtml.append("\t<tr>\n");
			convertHtml.append("\t\t<td colspan=\"");
			
			if (dictionaryType.equals(TextMinerConstants.DICTIONARY_TYPE_WHITE)) {
				convertHtml.append(4);
			} else {
				convertHtml.append(3);
			}

			convertHtml.append("\">등록된 엔트리가 없습니다.</td>\n");
			convertHtml.append("\t<tr>\n");
 		} else {
 			for (int i = 0; i < resultList.size(); i++) {
 				convertHtml.append("\t<tr>\n");
 				convertHtml.append("\t\t<td><input type=\"checkbox\" name=\"entry_choice\" value=\"" + resultList.get(i).getEntryId() + "\"</td>\n");
 				convertHtml.append("\t\t<td class=\"offset\">" + ((i + 1) + (pageRow * 10) - 10) + "</td>\n");
 				convertHtml.append("\t\t<td class=\"entry\">" + resultList.get(i).getEntryContent() +"</td>\n");
 				
 				if (dictionaryType.equals(TextMinerConstants.DICTIONARY_TYPE_WHITE)) {
 					convertHtml.append("\t\t<td style=\"text-align:left;\">\n");
 					convertHtml.append("\t\t\t<input type=\"text\" id=\"synonyms_" + resultList.get(i).getEntryId() + "\" style=\"width:95%;\" value=\"" + resultList.get(i).getSynonyms() + "\" disabled>\n");
 					convertHtml.append("\t\t\t<button type=\"button\" id=\"modifyBtn_" + resultList.get(i).getEntryId() + "\" onclick=\"enableTextBox(" + resultList.get(i).getEntryId() + ")\"><i class=\"fas fa-edit ml10 mr10\"></i></button>\n");
 					convertHtml.append("\t\t\t<button type=\"button\" id=\"saveBtn_" + resultList.get(i).getEntryId() + "\" class=\"btn_green w84\" style=\"display:none;\" onclick=\"saveSynonym(" + resultList.get(i).getEntryId() + ")\">저장</button>\n");
 					convertHtml.append("\t\t\t<button type=\"button\" id=\"cancelBtn_" + resultList.get(i).getEntryId() + "\" class=\"btn_red w84\" style=\"display:none;\" onclick=\"disableTextBox(" + resultList.get(i).getEntryId() + ")\">취소</button>\n");
 					convertHtml.append("\t\t</td>\n");
 				}
 				
 				convertHtml.append("\t<tr>\n");
 			}
 		}
 		
 		convertHtml.append("</tbody>\n");
 		// tbody 종료
 		
 		return convertHtml.toString();
 	}
}
