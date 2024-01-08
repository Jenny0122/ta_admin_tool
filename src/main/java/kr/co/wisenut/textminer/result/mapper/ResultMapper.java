package kr.co.wisenut.textminer.result.mapper;

import java.util.List;
import java.util.Map;

public interface ResultMapper {

	public List<Map<String, Object>> getResultList(Map<String, Object> paramMap); 					// 결과조회
	
	public int insertResult(List<Map<String, Object>> paramList);									// 결과저장
	public int deleteResult(Map<String, Object> paramMap);											// 결과삭제
}
