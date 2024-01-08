package kr.co.wisenut.textminer.dictionary.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.co.wisenut.textminer.common.TextMinerConstants;
import kr.co.wisenut.textminer.common.mapper.ImportProgressMapper;
import kr.co.wisenut.textminer.common.vo.ImportProgressVo;
import kr.co.wisenut.textminer.dictionary.mapper.DictionaryMapper;
import kr.co.wisenut.textminer.dictionary.mapper.EntryMapper;
import kr.co.wisenut.textminer.dictionary.vo.DictionaryVo;
import kr.co.wisenut.textminer.dictionary.vo.EntryVo;
import kr.co.wisenut.textminer.dictionary.vo.SynonymVo;
import kr.co.wisenut.util.PageUtil;

@Service
public class DictionaryService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private DictionaryMapper dictionaryMapper;
	
	@Autowired
	private EntryMapper entryMapper;
	
	@Autowired
	private ImportProgressMapper importProgressMapper;
	
	// 컬렉션 리스트 조회
	public Map<String, Object> getDictionaryList(Map<String, Object> paramMap) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();

		try {
			
			// 전체 건수
			double totalCount = dictionaryMapper.getDictionaryTotalCount(paramMap);
			resultMap.put("totalCount", totalCount);					
			// 조회결과 리스트
			List<DictionaryVo> resultList = dictionaryMapper.getDictionaryList(paramMap);
			resultMap.put("dataTable", convertHtmlTagForDictionaryList(resultList, Integer.parseInt(paramMap.get("pageRow").toString()), paramMap.get("contextPath").toString()));
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
	
	// 컬렉션 상세조회
	public ImportProgressVo getDictionaryDetailForImportProgress(DictionaryVo dictionaryVo) {
		
		DictionaryVo dictionary = new DictionaryVo();
		ImportProgressVo result = new ImportProgressVo();
		
		try {
			dictionary = dictionaryMapper.getDictionaryDetail(dictionaryVo);
			
			result.setResourceType(TextMinerConstants.PROGRESS_TYPE_DICTIONARY);
			result.setResourceId(dictionaryVo.getDictionaryId());
			
		} catch (NullPointerException e) {
			logger.error("조회 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("조회 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return result;
	}
	
	// 컬렉션 리스트 조회
	public List<DictionaryVo> getDictionaryNames(Map<String, Object> paramMap) {
		
		List<DictionaryVo> resultList = null;
		
		try {
			resultList = dictionaryMapper.getDictionaryNames(paramMap);
			
		} catch (NullPointerException e) {
			logger.error("조회 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("조회 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultList;
	}
	
	// 컬렉션 상세조회
	public DictionaryVo getDictionaryDetail(DictionaryVo dictionaryVo) {
		
		DictionaryVo result = new DictionaryVo();
		
		try {
			result = dictionaryMapper.getDictionaryDetail(dictionaryVo);
		} catch (NullPointerException e) {
			logger.error("조회 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("조회 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return result;
	}
	
	// 사전 등록
	public Map<String, Object> insertDictionary(DictionaryVo dictionaryVo) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			/*// 공통사전 기능을 사용하지 않으므로 해당 로직 주석처리 
			if (dictionaryVo.getDictionarySharedYn().equals("Y") 
				&& dictionaryMapper.getSharedDictionaryId(dictionaryVo) > 0) {
				resultMap.put("dictionaryName", dictionaryVo.getDictionaryName());
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "공통 사전은 1개만 등록 가능합니다.");
			} else {
				int result = dictionaryMapper.insertDictionary(dictionaryVo);
				
				if (result > 0) {
					resultMap.put("dictionaryId", dictionaryVo.getDictionaryId());
					resultMap.put("dictionaryName", dictionaryVo.getDictionaryName());
					resultMap.put("result", "S");
					resultMap.put("resultMsg", "사전 등록 작업이 완료되었습니다.");
				} else {
					resultMap.put("dictionaryName", dictionaryVo.getDictionaryName());
					resultMap.put("result", "F");
					resultMap.put("resultMsg", "사전 등록 작업을 실패하였습니다.");
				}
			}
			*/
			
			int result = dictionaryMapper.insertDictionary(dictionaryVo);
			
			if (result > 0) {
				resultMap.put("dictionaryId", dictionaryVo.getDictionaryId());
				resultMap.put("dictionaryName", dictionaryVo.getDictionaryName());
				resultMap.put("result", "S");
				resultMap.put("resultMsg", "사전 등록 작업이 완료되었습니다.");
			} else {
				resultMap.put("dictionaryName", dictionaryVo.getDictionaryName());
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "사전 등록 작업을 실패하였습니다.");
			}
		} catch (NullPointerException e) {
			logger.error("등록 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("등록 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	// 컬렉션 수정
	public Map<String, Object> updateDictionary(DictionaryVo dictionaryVo) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			/* // 공통사전 기능을 사용하지 않으므로 해당 로직 주석처리 
			if (dictionaryVo.getDictionarySharedYn().equals("Y") 
				&& dictionaryMapper.getSharedDictionaryId(dictionaryVo) != dictionaryVo.getDictionaryId()) {
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "공통 사전은 1개만 등록 가능합니다.");
			} else {
				int result = dictionaryMapper.updateDictionary(dictionaryVo);
				
				if (result > 0) {
					resultMap.put("result", "S");
					resultMap.put("resultMsg", "사전 수정 작업이 완료되었습니다.");
				} else {
					resultMap.put("result", "F");
					resultMap.put("resultMsg", "사전 수정 권한이 없습니다.");
				}
			}
			*/
			
			int result = dictionaryMapper.updateDictionary(dictionaryVo);
			
			if (result > 0) {
				resultMap.put("result", "S");
				resultMap.put("resultMsg", "사전 수정 작업이 완료되었습니다.");
			} else {
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "사전 수정 권한이 없습니다.");
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
	
	// 사전 삭제
	public Map<String, Object> deleteDictionary(DictionaryVo dictionaryVo) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			
			int result = dictionaryMapper.deleteDictionary(dictionaryVo);
			
			if (result > 0) {
				// 사전 업로드 이력 삭제
				ImportProgressVo importProgressVo = new ImportProgressVo();
				importProgressVo.setResourceId(dictionaryVo.getDictionaryId());
				importProgressVo.setResourceType(TextMinerConstants.PROGRESS_TYPE_DICTIONARY);
				importProgressMapper.deleteImportProgress(importProgressVo);
				
				// 동의어 삭제
				SynonymVo synonymVo = new SynonymVo();
				synonymVo.setDictionaryId(dictionaryVo.getDictionaryId());
				entryMapper.deleteSynonym(synonymVo);
				
				// 엔트리 삭제
				EntryVo entryVo = new EntryVo();
				entryVo.setDictionaryId(dictionaryVo.getDictionaryId());
				entryMapper.deleteEntry(entryVo);
				
				// 결과 리턴
				resultMap.put("result", "S");
				resultMap.put("resultMsg", "사전 삭제 작업이 완료되었습니다.");
			} else {
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "사전 삭제 권한이 없습니다.");
			}
			
		} catch (NullPointerException e) {
			logger.error("삭제 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("삭제 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	public String convertHtmlTagForDictionaryList(List<DictionaryVo> resultList, int pageRow, String contextPath) {
		StringBuffer convertHtml = new StringBuffer();
		
		// colgroup 설정시작
		convertHtml.append("<colgroup>\n");
		convertHtml.append("\t<col width=\"6%;\">\n");
		convertHtml.append("\t<col width=\"6%;\">\n");
		convertHtml.append("\t<col width=\"9%;\">\n");
		convertHtml.append("\t<col width=\"7%;\">\n");
//		convertHtml.append("\t<col width=\"5%;\">\n");
		convertHtml.append("\t<col width=\"*;\">\n");
		convertHtml.append("\t<col width=\"7%;\">\n");
		convertHtml.append("\t<col width=\"7%;\">\n");
		convertHtml.append("</colgroup>\n\n");
		// colgroup 설정종료

		// thead 설정시작
		convertHtml.append("<thead>\n");
		convertHtml.append("\t<th>선택</th>\n");
		convertHtml.append("\t<th>NO.</th>\n");
		convertHtml.append("\t<th>사전 명</th>\n");
		convertHtml.append("\t<th>사전 종류</th>\n");
//		convertHtml.append("\t<th>공통사전</th>\n");
		convertHtml.append("\t<th>설명</th>\n");
		convertHtml.append("\t<th>엔트리 수</th>\n");
		convertHtml.append("\t<th>생성날짜</th>\n");
		convertHtml.append("</thead>\n\n");
		// thead 설정종료

		// tbody 설정시작
		convertHtml.append("<tbody>\n");
		
		if (resultList == null || resultList.isEmpty()) {
			convertHtml.append("\t<tr>\n");
			convertHtml.append("\t\t<td colspan=\"8\">생성된 사전이 없습니다. 사전을 생성해주세요.</td>");
			convertHtml.append("\t</tr>\n");
		} else {
			for (int i = 0; i < resultList.size(); i++) {
				convertHtml.append("\t<tr id=\"dictionaryList\" class=\"dictionary\">\n");
				
				// 선택
				convertHtml.append("\t\t<td>");
				convertHtml.append("\t\t\t<input type=\"radio\" name=\"dict_choice\" value=\"");
				convertHtml.append(resultList.get(i).getDictionaryId());
				convertHtml.append("\" data-name=\"" + resultList.get(i).getDictionaryName());
				convertHtml.append("\" data-count=\"" + resultList.get(i).getDictionaryEntryCnt() + "\">\n");
				convertHtml.append("\t\t</td>\n");
				
				// No.
				convertHtml.append("\t\t<td>");
				convertHtml.append((i + 1) + (pageRow * 10) - 10);
				convertHtml.append("</td>\n");
				
				// 사전 명
				convertHtml.append("\t\t<td class=\"field-desc\" title=\"'클릭시 엔트리조회'\">");
				convertHtml.append("<a href=\"" + contextPath + "/dictionary/" + resultList.get(i).getDictionaryId() + "/entries\">");
				convertHtml.append(resultList.get(i).getDictionaryName() + "</a>");
				convertHtml.append("</td>\n");
				
				// 사전 종류
				convertHtml.append("\t\t<td>");
				convertHtml.append(resultList.get(i).getLabel());
				convertHtml.append("</td>\n");
				
				// 공통사전
//				convertHtml.append("\t\t<td>");
//				convertHtml.append(resultList.get(i).getDictionarySharedYn());
//				convertHtml.append("</td>\n");
				
				// 설명
				convertHtml.append("\t\t<td>");
				convertHtml.append(resultList.get(i).getDictionaryDesc());
				convertHtml.append("</td>\n");
				
				// 엔트리 수
				convertHtml.append("\t\t<td>");
				convertHtml.append(resultList.get(i).getDictionaryEntryCnt());
				convertHtml.append("</td>\n");
				
				// 생성날짜
				convertHtml.append("\t\t<td>");
				convertHtml.append(resultList.get(i).getCreDt());
				convertHtml.append("</td>\n");
				
				convertHtml.append("\t</tr>");
			}
		}
		
		convertHtml.append("<tbody>\n");
		// tbody 설정종료
		
		return convertHtml.toString();
	}
}
