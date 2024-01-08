package kr.co.wisenut.textminer.collection.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.co.wisenut.textminer.collection.mapper.CollectionMapper;
import kr.co.wisenut.textminer.collection.mapper.DocumentMapper;
import kr.co.wisenut.textminer.collection.vo.CollectionVo;
import kr.co.wisenut.textminer.common.TextMinerConstants;
import kr.co.wisenut.textminer.common.mapper.ImportProgressMapper;
import kr.co.wisenut.textminer.common.vo.ImportProgressVo;
import kr.co.wisenut.util.PageUtil;

@Service
public class CollectionService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private CollectionMapper collectionMapper;
	
	@Autowired
	private ImportProgressMapper importProgressMapper;
	
	@Autowired
	private DocumentMapper documentMapper;
	
	// 컬렉션 리스트 조회
	public Map<String, Object> getCollectionList(Map<String, Object> paramMap) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();

		try {
			
			// 전체 건수
			double totalCount = collectionMapper.getCollectionTotalCount(paramMap);
			resultMap.put("totalCount", totalCount);					
			// 조회결과 리스트
			List<CollectionVo> resultList = collectionMapper.getCollectionList(paramMap);
			resultMap.put("dataTable", convertHtmlTagForCollectionList(resultList, Integer.parseInt(paramMap.get("pageRow").toString()), paramMap.get("contextPath").toString()));
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
	
	// 컬렉션 리스트 조회
	public List<CollectionVo> getCollectionNames(Map<String, Object> paramMap) {
		
		List<CollectionVo> resultList = null;
		
		try {
			resultList = collectionMapper.getCollectionNames(paramMap);
			
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
	public CollectionVo getCollectionDetail(CollectionVo collectionVo) {
		
		CollectionVo result = new CollectionVo();
		
		try {
			result = collectionMapper.getCollectionDetail(collectionVo);
		} catch (NullPointerException e) {
			logger.error("조회 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("조회 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return result;
	}
	
	// 컬렉션 상세조회
	public ImportProgressVo getCollectionDetailForImportProgress(CollectionVo collectionVo) {
		
		CollectionVo collection = new CollectionVo();
		ImportProgressVo result = new ImportProgressVo();
		
		try {
			collection = collectionMapper.getCollectionDetail(collectionVo);
			
			result.setResourceType(TextMinerConstants.PROGRESS_TYPE_COLLECTION);
			result.setResourceId(collectionVo.getCollectionId());
			
			
		} catch (NullPointerException e) {
			logger.error("조회 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("조회 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return result;
	}
	
	// 컬렉션 등록
	public Map<String, Object> insertCollection(CollectionVo collectionVo) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		int result = 0;
		
		try {
			
			if (collectionMapper.chkDuplicatedCollectionName(collectionVo) == 0) {
				result = collectionMapper.insertCollection(collectionVo);
				
				if (result > 0) {
					resultMap.put("collectionId", collectionVo.getCollectionId());
					resultMap.put("collectionName", collectionVo.getCollectionName());
					resultMap.put("result", "S");
					resultMap.put("resultMsg", "컬렉션 등록 작업이 완료되었습니다.");
				} else {
					resultMap.put("collectionName", collectionVo.getCollectionName());
					resultMap.put("result", "F");
					resultMap.put("resultMsg", "컬렉션 등록 작업을 실패하였습니다.");
				}
			} else {
				resultMap.put("collectionName", collectionVo.getCollectionName());
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "기존에 등록된 컬렉션 이름이 있어 등록 작업을 실패하였습니다.");
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
	public Map<String, Object> updateCollection(CollectionVo collectionVo) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			
			int result = collectionMapper.updateCollection(collectionVo);
			
			if (result > 0) {
				resultMap.put("result", "S");
				resultMap.put("resultMsg", "컬렉션 수정 작업이 완료되었습니다.");
			} else {
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "컬렉션 수정 권한이 없습니다.");
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
	
	// 컬렉션 삭제
	public Map<String, Object> deleteCollection(CollectionVo collectionVo) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			
			int result = collectionMapper.deleteCollection(collectionVo);
			
			// 컬렉션 업로드 이력, 문서 데이터 삭제
			if (result > 0) {
				ImportProgressVo importProgressVo = new ImportProgressVo();
				importProgressVo.setResourceId(collectionVo.getCollectionId());
				importProgressVo.setResourceType(TextMinerConstants.PROGRESS_TYPE_COLLECTION);
				
				importProgressMapper.deleteImportProgress(importProgressVo);
				
				documentMapper.deleteDocument(collectionVo.getCollectionId());
				
				resultMap.put("result", "S");
				resultMap.put("resultMsg", "컬렉션 삭제 작업이 완료되었습니다.");
			} else {
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "컬렉션 삭제 권한이 없습니다.");
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
	
	public String convertHtmlTagForCollectionList(List<CollectionVo> resultList, int pageRow, String contextPath) {
		StringBuffer convertHtml = new StringBuffer();
		
		// colgroup 설정시작
		convertHtml.append("<colgroup>\n");
		convertHtml.append("\t<col width=\"3%;\">\n");
		convertHtml.append("\t<col width=\"5%;\">\n");
		convertHtml.append("\t<col width=\"10%;\">\n");
		convertHtml.append("\t<col width=\"5%;\">\n");
		convertHtml.append("\t<col width=\"5%;\">\n");
		convertHtml.append("\t<col width=\"*;\">\n");
		convertHtml.append("\t<col width=\"6%;\">\n");
		convertHtml.append("\t<col width=\"9%;\">\n");
//		convertHtml.append("\t<col width=\"9%;\">\n");
		convertHtml.append("</colgroup>\n\n");
		// colgroup 설정종료

		// thead 설정시작
		convertHtml.append("<thead>\n");
		convertHtml.append("\t<th>선택</th>\n");
		convertHtml.append("\t<th>NO.</th>\n");
		convertHtml.append("\t<th>컬렉션 명</th>\n");
		convertHtml.append("\t<th>컬렉션 구분</th>\n");
		convertHtml.append("\t<th>작업구분</th>\n");
		convertHtml.append("\t<th>설명</th>\n");
		convertHtml.append("\t<th>문서수</th>\n");
		convertHtml.append("\t<th>생성날짜</th>\n");
//		convertHtml.append("\t<th>업로드 상태</th>\n");
		convertHtml.append("</thead>\n\n");
		// thead 설정종료

		// tbody 설정시작
		convertHtml.append("<tbody>\n");
		
		if (resultList == null || resultList.isEmpty()) {
			convertHtml.append("\t<tr>\n");
			convertHtml.append("\t\t<td colspan=\"8\">생성된 컬렉션이 없습니다. 컬렉션을 생성해주세요.</td>");
			convertHtml.append("\t</tr>\n");
		} else {
			for (int i = 0; i < resultList.size(); i++) {
				convertHtml.append("\t<tr class=\"collection\">\n");
				
				// 선택
				convertHtml.append("\t\t<td>\n");
				convertHtml.append("\t\t\t<input type=\"radio\" name=\"coll_choice\" value=\"" + resultList.get(i).getCollectionId() + "\"");
				convertHtml.append(" data-collection-name=\"" + resultList.get(i).getCollectionName() + "\"");
				convertHtml.append(" data-count=\"" + resultList.get(i).getDocumentCount() + "\"");
				convertHtml.append(">");
				convertHtml.append("\t\t</td>\n");
				
				// NO.
				convertHtml.append("\t\t<td>\n");
				convertHtml.append((i + 1) + (pageRow * 10) - 10);
				convertHtml.append("\t\t</td>\n");
				
				// 컬렉션 명
				convertHtml.append("\t\t<td>\n");
				convertHtml.append("\t\t\t<a href=\"" + contextPath + "/collection/" + resultList.get(i).getCollectionId() +  "/documents\" title=\"" + resultList.get(i).getCollectionName());
				convertHtml.append(" 컬렉션의 문서조회(" + resultList.get(i).getDocumentCount() + " 건) 페이지로 이동\">");
				convertHtml.append(resultList.get(i).getCollectionName() + "</a>");
				convertHtml.append("\t\t</td>\n");
				
				// 컬렉션 구분
				convertHtml.append("\t\t<td class=\"coll-desc\">\n");
				convertHtml.append(resultList.get(i).getCollectionTypeName());
				convertHtml.append("\t\t</td>\n");
				
				// 작업 구분
				convertHtml.append("\t\t<td class=\"coll-desc\">\n");
				convertHtml.append(resultList.get(i).getCollectionJobName());
				convertHtml.append("\t\t</td>\n");
				
				// 설명
				convertHtml.append("\t\t<td class=\"coll-desc\">\n");
				convertHtml.append(resultList.get(i).getCollectionDesc());
				convertHtml.append("\t\t</td>\n");
				
				// 문서수
				convertHtml.append("\t\t<td class=\"coll-count\">\n");
				convertHtml.append(resultList.get(i).getDocumentCount());
				convertHtml.append("\t\t</td>\n");
				
				// 생성날짜
				convertHtml.append("\t\t<td class=\"coll-create-time\">\n");
				convertHtml.append(resultList.get(i).getCreDt());
				convertHtml.append("\t\t</td>\n");
				
				// 업로드 상태
	//			convertHtml.append("\t\t<td class=\"uploadState\">...</td>\n");
				convertHtml.append("\t</tr>\n");
			}
		}
		
		convertHtml.append("<tbody>\n");
		// tbody 설정종료
		
		return convertHtml.toString();
	}
}
