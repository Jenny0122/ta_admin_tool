package kr.co.wisenut.textminer.batch.mapper;

import java.util.Map;

public interface StopwordMapper {
	
	public int doStopword(Map<String, Object> paramMap);			// 금칙어 분석
	public int deleteStopword(Map<String, Object> paramMap);		// 금칙어 당일데이터 삭제
	
}
