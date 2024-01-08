package kr.co.wisenut.textminer.collection.mapper;

import java.util.List;
import java.util.Map;

import kr.co.wisenut.textminer.collection.vo.CollectionVo;

public interface DocumentMapper {

	public List<Map<String, Object>> getDocumentList(Map<String, Object> paramMap);						// 컬렉션 문서 조회
	public int getDocumentListCount(Map<String, Object> paramMap);										// 컬렉션 문서 건수 조회
	public List<Map<String, Object>> getDocumentListForFile(Map<String, Object> paramMap);				// 컬렉션 문서 조회(파일생성용)
	public Map<String, Object> getDocumentDetail(Map<String, Object> paramMap);							// 컬렉션 문서 상세조회
	
	public List<String> getNotClsCallIdList(Map<String, Object> paramMap);								// 전일 기준, 분류되지 않은 CALL_ID 리스트 가져오기
	
	public int insertDocument(Map<String, Object> paramMap);											// 컬렉션 문서 등록(파일 업로드)
	public int updateDocument(Map<String, Object> paramMap);											// 컬렉션 문서 변경
	public int deleteDocument(int collectionId);														// 컬렉션 문서 삭제(컬렉션 삭제 시 호출)
}