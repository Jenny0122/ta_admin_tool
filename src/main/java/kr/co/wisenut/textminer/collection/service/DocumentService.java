package kr.co.wisenut.textminer.collection.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.FilenameUtils;
import org.bson.Document;
import org.bson.codecs.configuration.CodecConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import kr.co.wisenut.exception.TMResourceUploadingException;
import kr.co.wisenut.exception.TMUploadException;
import kr.co.wisenut.textminer.collection.mapper.CollectionMapper;
import kr.co.wisenut.textminer.collection.mapper.DocumentMapper;
import kr.co.wisenut.textminer.collection.vo.CollectionVo;
import kr.co.wisenut.textminer.common.TextMinerConstants;
import kr.co.wisenut.textminer.common.mapper.ImportProgressMapper;
import kr.co.wisenut.textminer.common.resource.ExportProgress;
import kr.co.wisenut.textminer.common.resource.FileType;
import kr.co.wisenut.textminer.common.resource.ImportErrorHandle;
import kr.co.wisenut.textminer.common.resource.StagingFileInfo;
import kr.co.wisenut.textminer.common.resource.StorageResourceType;
import kr.co.wisenut.textminer.common.service.StorageService;
import kr.co.wisenut.textminer.common.vo.ImportProgressVo;
import kr.co.wisenut.textminer.history.mapper.ActionHistoryMapper;
import kr.co.wisenut.textminer.history.vo.ActionHistoryVo;
import kr.co.wisenut.textminer.user.vo.TmUser;
import kr.co.wisenut.util.AesCryptoUtil;
import kr.co.wisenut.util.PageUtil;

@Service
public class DocumentService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
    final static public AtomicReference<Map<Long, ExportProgress>> EXPORT_PROGRESS = new AtomicReference<>(new HashMap<>());
    
    // final static public AtomicReference<Map<Long, ExportProgress>> EXPORT_PROGRESS = new AtomicReference<>(new HashMap<>());
    final static public AtomicReference<Map<Long, List<ImportProgressVo>>> IMPORT_PROGRESS = new AtomicReference<>(new HashMap<>());
	
	@Autowired
	private CollectionMapper collectionMapper;

	@Autowired
	private DocumentMapper documentMapper;
	
	@Autowired
	private ImportProgressMapper importProgressMapper;
	
	@Autowired
	private ActionHistoryMapper actionHistoryMapper;

	@Autowired
	private StorageService storageService;
	
	@Value("${database.name}")
	private String databaseName;
	
	@Value("${smp.api.url}")
	private String smpApiUrl;
	
	// 암호화 처리 시 사용할 JsonParser
	com.google.gson.JsonParser gsonParser = new com.google.gson.JsonParser();
	
	public List<String> logTextList;
	
	// 문서 리스트 조회
	public Map<String, Object> getDocumentList(Map<String, Object> paramMap) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();

		try {
			// 컬렉션 상세조회
			CollectionVo collectionVo = new CollectionVo();
			collectionVo.setCollectionId(Integer.parseInt(paramMap.get("collectionId").toString()));
			collectionVo.setCollectionOwner(paramMap.get("collectionOwner").toString());
			collectionVo.setRole(paramMap.get("role").toString());
			collectionVo = collectionMapper.getCollectionDetail(collectionVo);
			
			// JSON_TABLE을 위한 필드정보 추가, 없으면 데이터 조회불가
			if (collectionVo.getDocumentField() != null && !collectionVo.getDocumentField().equals(" ")) {
				paramMap.put("documentField", collectionVo.getDocumentField());						
	
				// 조회결과 리스트
				paramMap.put("queryType", "LIST");
				List<Map<String, Object>> resultList = documentMapper.getDocumentList(paramMap);
				
				resultMap.put("dataTable", convertHtmlTagForDocumentList( resultList
																		, paramMap.get("pageRow").toString()
																		, Integer.parseInt(paramMap.get("collectionId").toString())
																		, Arrays.asList(collectionVo.getFieldInfo().split(","))));
				
				// 문서 전체 건수
				paramMap.put("queryType", "CNT");
				int totalCnt = documentMapper.getDocumentListCount(paramMap);
				resultMap.put("totalCount", totalCnt);
				
				// 페이징
				resultMap.put("pageNav", PageUtil.createPageNav(totalCnt, paramMap));
			} else {
				resultMap.put("dataTable", convertHtmlTagForDocumentList( null
																		, paramMap.get("pageRow").toString()
																		, Integer.parseInt(paramMap.get("collectionId").toString())
																		, Arrays.asList(collectionVo.getFieldInfo().split(","))));
			}
		} catch (NullPointerException e) {
			logger.error("조회 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("조회 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	// 문서 상세 조회
	public Map<String, Object> getDocumentDetail(Map<String, Object> paramMap) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();

		try {
			// 컬렉션 상세조회
			CollectionVo collectionVo = new CollectionVo();
			collectionVo.setCollectionId(Integer.parseInt(paramMap.get("collectionId").toString()));
			collectionVo.setCollectionOwner(paramMap.get("collectionOwner").toString());
			collectionVo.setRole(paramMap.get("role").toString());
			collectionVo = collectionMapper.getCollectionDetail(collectionVo);
			
			// JSON_TABLE을 위한 필드정보 추가
			paramMap.put("documentField", collectionVo.getDocumentField());						

			// 조회결과 리스트
			Map<String, Object> resultMapInq = documentMapper.getDocumentDetail(paramMap);
			resultMap.put("documentForm", convertHtmlTagForDocumentForm( resultMapInq
																	   , Integer.parseInt(paramMap.get("collectionId").toString())
																	   , Arrays.asList(collectionVo.getFieldInfo().split(","))));
			
		} catch (NullPointerException e) {
			logger.error("조회 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("조회 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	// 문서 수정
	public Map<String, Object> updateDocument(Map<String, Object> paramMap) {
			
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			
			int result = documentMapper.updateDocument(paramMap);
			
			if (result > 0) {
				resultMap.put("collectionId", paramMap.get("collectionId"));
				resultMap.put("result", "S");
				resultMap.put("resultMsg", "문서 수정 작업이 완료되었습니다.");
			} else {
				resultMap.put("collectionId", paramMap.get("collectionId"));
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "문서 수정 권한이 없습니다.");
			}
			
		} catch (NullPointerException e) {
			logger.error("수정 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("수정 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	@Async("threadPoolTaskExecutor")
    public ListenableFuture<List<ImportProgressVo>> asyncImportAllFile(TmUser user, 
    																 long collectionId,
                                                                     ImportErrorHandle errorHandle,
                                                                     List<String> fieldNames,
                                                                     List<Boolean> fieldChecks, @NotNull List<Path> stagedFiles) {
		
		// 파일을 업로드하기 위한 컬렉션 정보 조회
		CollectionVo collectionVo = new CollectionVo();
		collectionVo.setCollectionId(Integer.parseInt(String.valueOf(collectionId)));
		collectionVo.setCollectionOwner(user.getUsername());
		collectionVo.setRole(user.getAuthorities().toString());
        collectionVo = collectionMapper.getCollectionDetail(collectionVo);
        
        logger.debug("{}\ttm-collection document-import prepare. errorHandle={}, stagedFiles={}, {}",
                Thread.currentThread(), errorHandle, stagedFiles, collectionVo);

        // ImportProgress 설정
        final List<ImportProgressVo> progresses = stagedFiles.stream()
											                 .map(path -> {
											                	 // 마지막 수정날짜 저장포맷
											                	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
											                	 
											                	return new ImportProgressVo( TextMinerConstants.PROGRESS_TYPE_COLLECTION
											                							   , Integer.parseInt(String.valueOf(collectionId))
											                							   , path.getFileName().toString()
											                							   , sdf.format(new Date(path.toFile().lastModified()))
											                							   , path.toFile().length()
											                							   , 0
											                							   , 0
											                							   , errorHandle.toString()
											                							   , null);
											                 })
											                 .collect(Collectors.toList());
        // ImportProgress 에러로그를 담기위한 List<String> 초기화
        logTextList = new ArrayList<String>();
        
        // 동시제어 설정
        IMPORT_PROGRESS.getAndUpdate(longListMap -> {
        												if (longListMap.containsKey(collectionId)) {
											                // TODO(wisnt65): 동시 접근 제어 테스트 필요 - collection 당 하나의 task만 수행되게끔
											                throw new TMResourceUploadingException(CollectionVo.class, collectionId);
											            }
											
											            longListMap.put(collectionId, progresses);
											            return longListMap;
											        });

        // [0] 해당 컬렉션의 최초 업로드일 경우 - fieldInfo, originalFieldInfo, documentField 업데이트 선행
        if ((collectionVo.getFieldInfo() == null || collectionVo.getFieldInfo().trim().equals(""))&& collectionVo.getDocumentCount() == 0) {
            // if valid param, zip field names, field checked --> set original field info
            List<String> originalFieldInfo = new ArrayList<>();

            // checked field list
            List<String> fieldInfo = new ArrayList<>();
            StringBuffer documentField = new StringBuffer();
            
            for (int idx = 0; idx < fieldNames.size(); idx++) {
                final String fieldName = fieldNames.get(idx);
                final Boolean isChecked = fieldChecks.get(idx);

                // 모든 필드 추가
                originalFieldInfo.add(fieldName);

                // 실제로 사용할 필드만 추가(oracle, mariadb 양식이 다름)
                if (isChecked) {
                    fieldInfo.add(fieldName);
                    
                	if (fieldName.equals("docid")) {
                		documentField.append(", " + fieldName + " VARCHAR(100) PATH '$." + fieldName + "'" );
                		continue;
                	}
                	
                    if (databaseName.equals("mariadb")) {
                    		documentField.append(", " + fieldName + " TEXT PATH '$." + fieldName + "'" );
                    } else {
                    		documentField.append(", " + fieldName + " CLOB PATH '$." + fieldName + "'" );
                    }
                }
            }
            
            collectionVo.setModUser(user.getUsername());
    		collectionVo.setRole(user.getAuthorities().toString());
            collectionVo.setFieldInfo(String.join(",", fieldInfo));
            collectionVo.setOriginalFieldInfo(String.join(",", originalFieldInfo));
            collectionVo.setDocumentField(documentField.substring(1));
            
            collectionMapper.updateCollection(collectionVo);
            
            logger.info("{}\ttm-collection fieldInfo updated. {}", Thread.currentThread(), collectionVo);
        }

        // SCD 파싱 작업을 위한 원본 파읠의 전체 필드 리스트 가져오기.
        final List<String> originalFields = Arrays.asList(collectionVo.getOriginalFieldInfo().split(","));

        // foreach remaining
        logger.info("{}\ttm-collection document-import start. errorHandle={}, stagedFiles={}, {}.",
                Thread.currentThread(), errorHandle, stagedFiles, collectionVo);

        int insertedCount = 0;

        for (int idx = 0; idx < stagedFiles.size(); idx++) {
            long start = System.currentTimeMillis();

            Path path = stagedFiles.get(idx);
            final ImportProgressVo progress = progresses.get(idx);

            logger.info("{}\tfile parsing and import start. {}'", Thread.currentThread(), path.toUri());

            // 진행상태 저장
            progress.setProgress(TextMinerConstants.PROGRESS_STATUS_IN_PROGRESS);
            
            // 파일 내용 보관을 위한 List객체
            List<Document> documents = null;
            
            try {
	            // before parse, validate encoding, malformed, ... --> throw unsupported file exception
	            StagingFileInfo.validate(StorageResourceType.COLLECTION, path.toFile());
	
	            // todo: Verify that the file is already uploaded
	
	            switch (FilenameUtils.getExtension(path.getFileName().toString()).toUpperCase()) {
	                case "JSON":
	                    documents = importJSON(collectionId, errorHandle, Arrays.asList(collectionVo.getFieldInfo().split(",")), path, progress);
	                    break;
	                case "CSV":
	                    documents = importCSV(collectionId, errorHandle, Arrays.asList(collectionVo.getFieldInfo().split(",")), path, progress);
	                    break;
	                default:
	                    throw new TMUploadException("지원하지 않는 파일 포맷 " + path.getFileName(), StorageResourceType.COLLECTION, collectionId);
	            }
	
	            insertDocuments(documents, Integer.parseInt(String.valueOf(collectionId)));
	            
	            if (progress.getValidCount() == 0) {
	                throw new TMUploadException("File does not have any valid documents.", StorageResourceType.COLLECTION, collectionId);
	            } else if (progress.getTotalCount() == progress.getValidCount()) {
	                progress.setProgress(TextMinerConstants.PROGRESS_STATUS_SUCCESS);
	                
                    // 이력저장
    				ActionHistoryVo actionHistoryVo = new ActionHistoryVo();

    		        actionHistoryVo.setActionUser(user.getUsername());
    		        actionHistoryVo.setResourceId(String.valueOf(collectionVo.getCollectionId()));
    		        actionHistoryVo.setResourceType(TextMinerConstants.PROGRESS_TYPE_COLLECTION);
    		        actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_DOCUMENT_UPLOAD);
    		        actionHistoryVo.setActionMsg(user.getUsername() + " 컬렉션 문서 업로드 ( 대상 컬렉션 : " + collectionVo.getCollectionName() + " )");
    		        actionHistoryVo.setUserIp(user.getUserIp());
    		        
    		        actionHistoryMapper.insertActionHistory(actionHistoryVo);
	            } else {
	                progress.setProgress(TextMinerConstants.PROGRESS_STATUS_PARTIAL_SUCCESS);
	                
                    // 이력저장
    				ActionHistoryVo actionHistoryVo = new ActionHistoryVo();

    		        actionHistoryVo.setActionUser(user.getUsername());
    		        actionHistoryVo.setResourceId(String.valueOf(collectionVo.getCollectionId()));
    		        actionHistoryVo.setResourceType(TextMinerConstants.PROGRESS_TYPE_COLLECTION);
    		        actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_DOCUMENT_UPLOAD);
    		        actionHistoryVo.setActionMsg(user.getUsername() + " 컬렉션 문서 업로드 ( 대상 컬렉션 : " + collectionVo.getCollectionName() + " )");
    		        actionHistoryVo.setUserIp(user.getUserIp());
    		        
    		        actionHistoryMapper.insertActionHistory(actionHistoryVo);
	            }
	            
            } catch (RuntimeException | IOException e) { // fixme(wisnt65) : catch InvalidDocumentFileException, throw IOException --> handle in async exception handler
                logger.error("{}\tfile parsing and import failed. {}, cause={}", Thread.currentThread(), path.toUri(), e.getMessage());
                logger.error("Failed to import documents. " + e.getMessage() + (e.getCause() != null ? "\n" + e.getCause().getMessage() : ""));
                logTextList.add("Failed to import documents. " + e.getMessage() + (e.getCause() != null ? "\n" + e.getCause().getMessage() : ""));
                progress.setValidCount(0);
                progress.setProgress(TextMinerConstants.PROGRESS_STATUS_FAILURE);
            } finally {
                // todo(wisnt65) log 가 많아지면 16MB 넘어감 --> mongo Document 제한 사항 초과. https://docs.mongodb.com/manual/reference/limits/
                // failure log save.
                // 정책 정의 필요. minimize log / threshold log / ...
            	// 현재 RDB로 작업중이라 필요없음.
            	progress.setLogText(String.join("\\|", logTextList));
            	importProgressMapper.insertImportProgress(progress);
            	logTextList = null;
            	
                // Remove file whether successful or unsuccessful.
                try {
                    Files.deleteIfExists(path);
                } catch (IOException | SecurityException e) {
                    logger.warn("{}\timported file deletion failed. file={}, cause={}", Thread.currentThread(), path.toUri(), e.getMessage());
                }
            }
            
            logger.info("{}\tfile parsing and import complete. {}ms, {}", Thread.currentThread(), (System.currentTimeMillis() - start), path.toUri());
        } // end for-each

        logger.info("{}\ttm-collection document-import complete. {} documents imported from {} files.", Thread.currentThread(), insertedCount, stagedFiles.size());

        // todo(wisnt65) defect async task exception handle 에서 확인 필요
        final List<ImportProgressVo> resultSet = IMPORT_PROGRESS.get().remove(collectionId);

        return new AsyncResult<List<ImportProgressVo>>(resultSet);
    }
	
    /**
     * 1. *.json file open
     * 1. parse JSON ARRAY or JSON OBJECT lines
     * 1. document list
     *
     * @param collectionId
     * @param errorHandle
     * @param fields
     * @param filePath
     * @param progress
     * @return
     */
    protected List<Document> importJSON(long collectionId, @NotNull ImportErrorHandle errorHandle, @NotEmpty final List<String> fields,
                                        final Path filePath, ImportProgressVo progress) {


        // valid document list to insert into tm-collection
        List<Document> validDocuments = new ArrayList<>();

        // number of document that readable
        int numTotalDoc = 0;

        // number of document that importable
        int numValidDoc = 0;

        JsonFactory jsonFactory = new JsonFactory();

        try (BufferedReader bufferedReader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8);
             JsonParser jsonParser = jsonFactory.createParser(bufferedReader)) {

            logger.info("{}\tdocument parsing start. fileName={}", Thread.currentThread(), filePath.getFileName());

            ObjectMapper jsonMapper = new ObjectMapper();
            JsonToken token = jsonParser.nextToken();

            // if JSON_ARRAY document file , skip start_array token '['
            if (JsonToken.START_ARRAY.equals(token)) {
                token = jsonParser.nextToken();
            }

            // for-each remaining
            while (true) {
                try {
                    if (token == null || !JsonToken.START_OBJECT.equals(token)) {
                        // end or malformed json documents file
                        break;
                    }

                    numTotalDoc += 1; // is number of valid JSON_TOKEN ?
                    progress.setTotalCount(numTotalDoc);
                    Document document = jsonMapper.readValue(jsonParser, Document.class);

                    // fieldInfo 순서대로 org.bson.Document extends LinkedHashMap 새로 생성
                    Document filtered = new Document();
                    for (String field : fields) {
                        // 해당 필드가 없을 경우 -- NullPointerException?
                        if (!document.containsKey(field)) {
                            logger.trace("{}", document);
                            throw new NullPointerException(String.format("Missing field '%s'.", field));
                        }

                        filtered.append(field, document.get(field));
                    }

                    validDocuments.add(filtered);
                    numValidDoc += 1;

                    progress.setValidCount(numValidDoc);
                } catch (JsonMappingException | NullPointerException documentParsingException) {
                    logger.trace("{} {}", documentParsingException.getMessage(), jsonParser.getCurrentLocation());

                    logTextList.add(String.format("%s %s", documentParsingException.getMessage(), jsonParser.getCurrentLocation()));

                    // STOP 모드면 throw 로 중지. resource auto-close 되는지??
                    if (ImportErrorHandle.STOP.equals(errorHandle)) {
                        throw new TMUploadException(String.format("STOP import JSON document '%s'.", filePath.getFileName()),
                                StorageResourceType.COLLECTION, collectionId, documentParsingException);
                    }
                } catch (JsonParseException jsonParseException) {
                    // Error caused non-well-formed json file --> do not import
                    logger.error("Malformed JSON document file '{}'. {}", filePath.getFileName(), jsonParseException.getMessage());
                    throw new TMUploadException("Malformed json document file.", StorageResourceType.COLLECTION, collectionId, jsonParseException);
                }

                token = jsonParser.nextToken();
            } // end while

        } catch (IOException | SecurityException resourceException) {
            // in case of not import.
            logger.error("{}\t{}, {}", Thread.currentThread(), filePath.toUri(), resourceException.getMessage());
            throw new TMUploadException(String.format("Failed to open file '%s'.", filePath.getFileName()),
                    StorageResourceType.COLLECTION, collectionId, resourceException);
        }
        // end file read

        logger.info("{}\tdocument-parsing complete. Number of documents {valid '{}' of readable '{}'} in '{}'.",
                Thread.currentThread(), validDocuments.size(), numTotalDoc, filePath.getFileName());

        return validDocuments;
        // TODO: 결과는 리턴? db? atomic field ?
    }

    /**
     * 1. *.csv file open
     * 1. schema with header, parse lines
     * 1. document list
     *
     * @param collectionId
     * @param errorHandle
     * @param fields
     * @param filePath
     * @param progress
     * @return
     */
    protected List<Document> importCSV(long collectionId, @NotNull ImportErrorHandle errorHandle, @NotEmpty final List<String> fields,
                                       final Path filePath, ImportProgressVo progress) {

        // valid document list to insert into tm-collection
        List<Document> validDocuments = new ArrayList<>();

        // number of document that readable
        int numTotalDoc = 0;

        // number of document that importable
        int numValidDoc = 0;

        CsvSchema csvSchema = CsvSchema.emptySchema().withHeader();
        CsvMapper csvMapper = CsvMapper.builder()
                .enable(CsvParser.Feature.WRAP_AS_ARRAY)
                .enable(CsvParser.Feature.INSERT_NULLS_FOR_MISSING_COLUMNS)
                .build();
        ObjectReader csvReader = csvMapper.readerFor(Document.class).with(csvSchema);

        try (BufferedReader bufferedReader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8);
             MappingIterator<Document> mappingIterator = csvReader.readValues(bufferedReader)) {

            logger.info("{}\tdocument parsing start. fileName={}", Thread.currentThread(), filePath.getFileName());

            final JsonParser parser = mappingIterator.getParser();
            logger.debug("{}", mappingIterator.getParserSchema());

            // for-each remaining
            while (mappingIterator.hasNext()) {
                try {
                    numTotalDoc += 1;
                    Document document = mappingIterator.nextValue();

                    // fieldInfo 순서대로 org.bson.Document extends LinkedHashMap 새로 생성
                    Document filtered = new Document();
                    for (String field : fields) {
                        // 해당 필드가 없을 경우 -- NullPointerException?
                        if (!document.containsKey(field)) {
                            logger.trace("{}", document);
                            throw new NullPointerException(String.format("Missing field '%s'.", field));
                        }

                        filtered.append(field, document.get(field));
                    }

                    validDocuments.add(filtered);
                    numValidDoc += 1;

                    progress.setTotalCount(numTotalDoc);
                    progress.setValidCount(numValidDoc);
                } catch (JsonMappingException | NullPointerException documentParsingException) {
                    logger.trace("{} {}", documentParsingException.getMessage(), parser.getCurrentLocation());

                    logTextList.add(String.format("%s %s", documentParsingException.getMessage(), parser.getCurrentLocation()));

                    // STOP 모드면 throw 로 중지. resource auto-close 되는지??
                    if (ImportErrorHandle.STOP.equals(errorHandle)) {
                        throw new TMUploadException(String.format("STOP import CSV document '%s'.", filePath.getFileName()),
                                StorageResourceType.COLLECTION, collectionId, documentParsingException);
                    }
                } catch (JsonParseException jsonParseException) {
                    // Error caused non-well-formed json file --> do not import
                    logger.error("Malformed CSV document file '{}'. {}", filePath.getFileName(), jsonParseException.getMessage());
                    throw new TMUploadException("Malformed CSV document file.", StorageResourceType.COLLECTION, collectionId, jsonParseException);
                }

                //token = jsonParser.nextToken();
            } // end while

        } catch (IOException | SecurityException resourceException) {
            // in case of not import.
            logger.error("{}\t{}, {}", Thread.currentThread(), filePath.toUri(), resourceException.getMessage());
            throw new TMUploadException(String.format("Failed to open file '%s'.", filePath.getFileName()),
                    StorageResourceType.COLLECTION, collectionId, resourceException);
        }
        // end file read
        logger.info("{}\tdocument-parsing complete. Number of documents {valid '{}' of readable '{}'} in '{}'.",
                Thread.currentThread(), validDocuments.size(), numTotalDoc, filePath.getFileName());

        return validDocuments;
    }
    
    public void insertDocuments(List<Document> documents, int collectionId) {
    	
    	Map<String, Object> paramMap = null;
    	
    	for (int i = 0; i < documents.size(); i++) {
    		paramMap = new HashMap<String, Object>();
    		paramMap.put("collectionId", collectionId);		// 컬렉션Id
//    		paramMap.put("documentContent", documents.get(i).toJson());		// 문서 내용
    		
    		// content 암호화하기...
    		JsonObject documentContent = (JsonObject) gsonParser.parse(documents.get(i).toJson());
    		String content = documentContent.get("content").getAsString();
    		documentContent.addProperty("content", AesCryptoUtil.encryption(content));
    		
    		paramMap.put("documentContent", documentContent.toString());		// 문서 내용
    		
    		documentMapper.insertDocument(paramMap);
    	}
    }

	// 다운로드
	@Async("threadPoolTaskExecutor")
    public ListenableFuture<ExportProgress<CollectionVo>> asyncExportDocuments(TmUser user, long collectionId, FileType exportFormat, String username, String dateRange) {
		
		//컬렉션 정보 조회
		CollectionVo collectionVo = new CollectionVo();
		collectionVo.setCollectionId(Integer.parseInt(String.valueOf(collectionId)));
		collectionVo.setCollectionOwner(user.getUsername());
		collectionVo.setRole(user.getAuthorities().toString());
		
		// 미분류데이터(collectionId = 0)는 컬렉션 정보를 하드코딩하여 설정한다.
		if (collectionId > 0) {
			collectionVo = collectionMapper.getCollectionDetail(collectionVo);
		} else {

			Map<String, Object> paramMap = new HashMap<String, Object>();
    		
			String [] date = dateRange.split(" - ");
    		paramMap.put("startDate", date[0].replaceAll("\\.", ""));
    		paramMap.put("endDate", date[1].replaceAll("\\.", ""));
			
    		collectionVo.setDocumentCount(documentMapper.getNotClsCallIdList(paramMap).size());
			collectionVo.setFieldInfo("docid,label,content");
			collectionVo.setCollectionType(TextMinerConstants.COLLECTION_TYPE_NAME_CLASSIFICATION);
			collectionVo.setCollectionJob(TextMinerConstants.COLLECTION_JOB_TRAINING);
		}
        collectionVo.setResourceType(TextMinerConstants.PROGRESS_TYPE_COLLECTION);
		
        ExportProgress<CollectionVo> exportProgress = ExportProgress.<CollectionVo>builder().requestedTime(LocalDateTime.now())
                .fileType(exportFormat)
                .username(username)
                .resourceInfo(collectionVo)
                .build();

        exportProgress.setInProgress();
        EXPORT_PROGRESS.get().put(collectionId, exportProgress);
        
        logger.debug("{}\ttm-collection document-export start. {}", Thread.currentThread(), collectionVo);

        // 경로, 파일 생성 - 파일명= 컬렉션명 + 타임스탬프
        Path exportFilePath = storageService.exportPath(StorageResourceType.COLLECTION, collectionId, collectionVo.getCollectionName() == null ? "미분류데이터" : collectionVo.getCollectionName(), exportFormat);
        
        // write to file
        switch (exportFormat) {
            case CSV:
                exportCsv(collectionVo, exportFilePath, dateRange);
                break;
            default:
                logger.warn("IllegalExportFormat={}. export as defaultFormat=json", exportFormat);
            case JSON:
                exportJson(collectionVo, exportFilePath, dateRange);
                break;
        }

        // export complete
        collectionVo.setProgress(TextMinerConstants.PROGRESS_STATUS_SUCCESS);
        exportProgress.setComplete(exportFilePath);

        // 보안이슈로 해당 Vo Class 초기화
        collectionVo = new CollectionVo();
        
        logger.info("{}\ttm-collection document-export complete. {}", Thread.currentThread(), exportProgress);

        return new AsyncResult(exportProgress);
    }
	

    /**
     * write collection's documents to a file in json format
     *
     * @param collection     collection to export.
     * @param exportFilePath <b>Not JsonArray. a document on a line.</b>
     */
    private void exportJson(@NotNull CollectionVo collectionVo, @NotNull Path exportFilePath, String dateRange) {
        int stepForLogging = collectionVo.getDocumentCount() < 10000 ? 100 : (int) (((collectionVo.getDocumentCount() / 10) / 1000) * 1000);
        
        final ExportProgress exportProgress = EXPORT_PROGRESS.get().get((long) collectionVo.getCollectionId());
        
        // open stream
        try {
        	Map<String, Object> paramMap = new HashMap<String, Object>();
        	paramMap.put("pageSize", 1);
        	paramMap.put("collectionId", collectionVo.getCollectionId());
        	paramMap.put("documentField", collectionVo.getDocumentField());

        	List<Map<String, Object>> documentList = null;
        	if (collectionVo.getCollectionId() == 0) {
        		String [] date = dateRange.split(" - ");
        		paramMap.put("startDate", date[0].replaceAll("\\.", ""));
        		paramMap.put("endDate", date[1].replaceAll("\\.", ""));
        		documentList = getNoneClassifiedDataList(paramMap);
        	} else {
        		documentList = documentMapper.getDocumentList(paramMap);
        	}

        	BufferedWriter bufferedWriter = Files.newBufferedWriter(exportFilePath, StandardCharsets.UTF_8);	  

        	long writeCount = 0;
        	Gson gson = new Gson();
        	
        	JsonObject tempJson = null;
        	
        	for (int i = 0; i < documentList.size(); i++) {
        		try {
        			// 암호화된 내용 복호화하여 다운받기
        			tempJson = (JsonObject)gsonParser.parse(gson.toJson(documentList.get(i)));
        			String content = tempJson.get("content").getAsString();
        			tempJson.addProperty("content", AesCryptoUtil.decryption(content));
        			
        			bufferedWriter.append(tempJson.toString()).append('\n'); // without indent.
        			writeCount += 1;
        		} catch (CodecConfigurationException e) {
        			logger.debug("{}\twrite document failed. _id={}, cause={}", Thread.currentThread(), i, e.getMessage());
        		}
        		
        		// update export progress
                if (writeCount % 1000 == 0) {
                    exportProgress.setWriteCount(writeCount);
                }

                if ((writeCount % stepForLogging) == 0) {
                    logger.debug("{}\twriting documents to {}... {}/{}", Thread.currentThread(), exportFilePath.toUri(), writeCount, collectionVo.getDocumentCount());
                }
        	}
        	
        	bufferedWriter.flush();
            exportProgress.setWriteCount(writeCount);

        	// end file write
        	if (writeCount != collectionVo.getDocumentCount()) {
        		logger.debug("{}\t{} documents failed.", Thread.currentThread(), collectionVo.getDocumentCount() - writeCount);
        	}
        	logger.info("{}\tjson export complete. document.count={}, {}", Thread.currentThread(), writeCount, exportFilePath.toUri());
    	  
        } catch (Exception e) {
            logger.error("{}", e.toString());
            logger.trace("{}", e);
            exportProgress.setFailed();
        }
    }

    /**
     * write collection's documents to a file in csv format
     *
     * @param collection     collection to export.
     * @param exportFilePath <b>with header, double quotation comma separated values. a document on a line.</b>
     */
    private void exportCsv(@NotNull CollectionVo collectionVo, @NotNull Path exportFilePath, String dateRange) {
        int stepForLogging = collectionVo.getDocumentCount() < 10000 ? 100 : (int) (((collectionVo.getDocumentCount() / 10) / 1000) * 1000);

        final ExportProgress exportProgress = EXPORT_PROGRESS.get().get((long) collectionVo.getCollectionId());
        
        // collection.fieldInfo --> csv schema
        CsvSchema.Builder csvSchemaBuilder = CsvSchema.builder();
        for (String field : Arrays.asList(collectionVo.getFieldInfo().split(","))) {
            csvSchemaBuilder.addColumn(field, CsvSchema.ColumnType.STRING);
        }

        CsvMapper mapper = new CsvMapper();
        mapper.configure(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS, true)
                .configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);

        ObjectWriter objectWriter = mapper.writerFor(Document.class)
                						  .with(csvSchemaBuilder.build().withHeader());

        try {
        	Map<String, Object> paramMap = new HashMap<String, Object>();
        	paramMap.put("pageSize", 1);
        	paramMap.put("collectionId", collectionVo.getCollectionId());
        	paramMap.put("documentField", collectionVo.getDocumentField());

        	List<Map<String, Object>> documentList = null;
        	if (collectionVo.getCollectionId() == 0) {
        		String [] date = dateRange.split(" - ");
        		paramMap.put("startDate", date[0].replaceAll("\\.", ""));
        		paramMap.put("endDate", date[1].replaceAll("\\.", ""));
        		documentList = getNoneClassifiedDataList(paramMap);
        	} else {
        		documentList = documentMapper.getDocumentList(paramMap);
        	}
        	
//        	logger.info("@@ documentList : " + documentList.get(0));
        	
        	BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exportFilePath.toString()), StandardCharsets.UTF_8));
        	bufferedWriter.write("\uFEFF");        	// CSV 한글 꺠지는 현상 해결을 위한 처리
            SequenceWriter sequenceWriter = objectWriter.writeValues(bufferedWriter);
        	
            int writeCount = 0;
            Document doc = null;
            
            logger.info("@@ document size : " + documentList.size());
            
            for (int i = 0; i < documentList.size(); i++) {
                try {
                	// 암호화된 내용 복호화하여 다운받기
                	String content = documentList.get(i).get("content").toString();
                	documentList.get(i).put("content", AesCryptoUtil.decryption(content));
                	
                	doc = new Document();
                	doc.putAll(documentList.get(i));
//                	
	            	sequenceWriter.write(doc);
	                writeCount += 1;
                } catch (IOException e) {
                    logger.debug("{}\twrite document failed. _id={}, cause={}", Thread.currentThread(), i, e.getMessage());
                }
                
                // update export progress
                if (writeCount % 1000 == 0) {
                    //EXPORT_PROGRESS.get().put(collection.getId(), "in progress " + writeCount + "/" + collection.getCountDocuments());
                    exportProgress.setWriteCount(writeCount);
                }
                
                if ((writeCount % stepForLogging) == 0) {
                    logger.debug("{}\twriting documents to {}... {}/{}", Thread.currentThread(), exportFilePath.toUri(), writeCount, collectionVo.getDocumentCount());
                }
            }
            
            sequenceWriter.flush();
            exportProgress.setWriteCount(writeCount);

            // end file write
            if (writeCount != collectionVo.getDocumentCount()) {
                logger.debug("{}\t{} documents failed.", Thread.currentThread(), collectionVo.getDocumentCount() - writeCount);
            }
            logger.info("{}\tcsv export complete. document.count={}, {}", Thread.currentThread(), writeCount, exportFilePath.toUri());

        } catch (Exception e) {
            logger.error("{}", e.toString());
            logger.trace("{}", e);
            exportProgress.setFailed();
        }
    }
    
	// 미분류 데이터 건수 가져오기
	public int getNoneClassifiedDataCnt(Map<String, Object> paramMap) {
		return documentMapper.getNotClsCallIdList(paramMap).size();
	}
    
    // 미분류 데이터 텍스트 데이터 가져오기
    public List<Map<String, Object>> getNoneClassifiedDataList(Map<String, Object> paramMap) {
    	
    	List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
    	List<String> notClsCallIdList = documentMapper.getNotClsCallIdList(paramMap);
    	Map<String, Object> tempMap = null;
    	
    	for (int i = 0; i < notClsCallIdList.size(); i++) {
    		
    		tempMap = new HashMap<String, Object>();
    		
    		// docid, label 설정
    		tempMap.put("docid", notClsCallIdList.get(i));
    		tempMap.put("label", TextMinerConstants.COMMON_BLANK);
    		
    		// content 설정
    		// content는 smp의 녹취 텍스트를 가져와서 생성한다.
    		// API Call을 위한 RestTemplate 객체 선언
    		URI uri = UriComponentsBuilder
					 .fromUriString(smpApiUrl + notClsCallIdList.get(i))
					 .build()
					 .toUri();
    		
    		HttpHeaders headers = new HttpHeaders();
    		headers.setContentType(MediaType.APPLICATION_JSON);
    		HttpEntity<?> entity = new HttpEntity<>(headers);

    		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
			factory.setConnectTimeout(10000);
			factory.setReadTimeout(10000);		
			
    		RestTemplate restTemplate = new RestTemplate(factory);;
    		ResponseEntity<String> responseEntity;
    		
    		// 통신 중 에러가 발생하는 코드는 제외한다.
    		try {
    			responseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
    		} catch (Exception e) {
    			logger.error("@@ callInfo2 Error : {} ", e.getMessage());
    			continue;
    		}
    		
    		// 미분류 데이터 셋 만들기
    		if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
    			JsonObject result = (JsonObject) gsonParser.parse(responseEntity.getBody());
	    		JsonArray conversationList = result.get("conversationList").getAsJsonArray();
	    		JsonObject tempObj = null;
	    		StringBuffer content = new StringBuffer();
	    		
	    		for (int j = 0; j < conversationList.size(); j++) {
	    			tempObj = conversationList.get(j).getAsJsonObject();
	    			
	    			if (j > 0) {
	    				content.append(" ");
	    			}
	    			content.append(tempObj.get("sttText").getAsString());
	    		}
	    		tempMap.put("content", content.toString());
	    		
	    		resultList.add(tempMap);
    		}
    	}
    	
    	return resultList;
    }
    
	// 조회결과 HTML로 변환
	public String convertHtmlTagForDocumentList(List<Map<String, Object>> resultList, String pageRow, int collectionId, List<String> fieldInfo) {
		StringBuffer convertHtml = new StringBuffer();
		
		// colgroup 설정시작
		convertHtml.append("<colgroup>\n");
		convertHtml.append("\t<col width=\"2%;\">\n");
		convertHtml.append("\t<col width=\"3%;\">\n");
		convertHtml.append("</colgroup>\n\n");
		// colgroup 설정종료

		// thead 설정시작
		convertHtml.append("<thead>\n");
		if (resultList == null || resultList.isEmpty()) {
			convertHtml.append("\t\t<th>No.</th>");
			convertHtml.append("\t\t<th>Content</th>");
		} else {
			for (int i = 0; i < fieldInfo.size(); i++) {
				convertHtml.append("\t<th>" + fieldInfo.get(i) +"</th>\n");
			}
		}
		convertHtml.append("</thead>\n\n");
		// thead 설정종료

		// tbody 설정시작
		convertHtml.append("<tbody>\n");
		if (resultList == null || resultList.isEmpty()) {
			convertHtml.append("\t<tr>\n");
			convertHtml.append("\t\t<td colspan=\"2\">등록된 문서가 없습니다.</td>");
			convertHtml.append("\t</tr>\n");
		} else {
			for (int i = 0; i < resultList.size(); i++) {
				convertHtml.append("\t<tr>\n");
				for (int j = 0; j < fieldInfo.size(); j++) {
					if (fieldInfo.get(j).equals("docid")) {
						convertHtml.append("\t\t<td><a href= \"#\"onclick=\"showPopup('document', '" + collectionId + "', '" + resultList.get(i).get(fieldInfo.get(j)) + "', '" + pageRow + "')\">" + resultList.get(i).get(fieldInfo.get(j)) + "</a></td>");
					} else {
						if (resultList.get(i).get(fieldInfo.get(j)) != null && AesCryptoUtil.decryption(resultList.get(i).get(fieldInfo.get(j)).toString()).length() > 100) {
							convertHtml.append("\t\t<td>" + AesCryptoUtil.decryption(resultList.get(i).get(fieldInfo.get(j)).toString()).substring(0,100) + "...</td>");
						} else {
							convertHtml.append("\t\t<td>" + (resultList.get(i).get(fieldInfo.get(j))==null?"":AesCryptoUtil.decryption(resultList.get(i).get(fieldInfo.get(j)).toString())) + "</td>");
						}
					}
				}
				convertHtml.append("\t</tr>\n");
			}
		}
		convertHtml.append("<tbody>\n");
		// tbody 설정종료
		
		return convertHtml.toString();
	}
	
	// 조회결과 HTML로 변환
	public String convertHtmlTagForDocumentForm(Map<String, Object> resultMap, int collectionId, List<String> fieldInfo) {
		StringBuffer convertHtml = new StringBuffer();
		
		convertHtml.append("<input type=\"hidden\" name=\"collectionId\" value=\"" + collectionId + "\">");
		
		for (int i = 0 ; i < fieldInfo.size(); i++) {
			convertHtml.append("<div class=\"mt20\">");
			convertHtml.append("\t<label for=\"name\">" + fieldInfo.get(i) + "</label>");
			
			if (fieldInfo.get(i).equals("docid")) {
				convertHtml.append("<input type=\"hidden\" name=\"" + fieldInfo.get(i) + "\" value=\"" + resultMap.get(fieldInfo.get(i)) + "\">");
				convertHtml.append("<input type=\"text\" class=\"w536\" name=\"" + fieldInfo.get(i) + "\" value=\"" + resultMap.get(fieldInfo.get(i)) + "\" disabled>");
			} else {
				if (resultMap.get(fieldInfo.get(i)) != null && AesCryptoUtil.decryption(resultMap.get(fieldInfo.get(i)).toString()).length() > 100) {
					convertHtml.append("<textarea rows=\"15\" class=\"w536\" name=\"" + fieldInfo.get(i) + "\">" + AesCryptoUtil.decryption(resultMap.get(fieldInfo.get(i)).toString()) + "</textarea>");
				} else {
					convertHtml.append("<input type=\"text\" class=\"w536\" name=\"" + fieldInfo.get(i) + "\" value=\"" + AesCryptoUtil.decryption(resultMap.get(fieldInfo.get(i)).toString()) + "\">");
				}
			}
			
			convertHtml.append("<div>");
		}
		
		return convertHtml.toString();
	}
}
