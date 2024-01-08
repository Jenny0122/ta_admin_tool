package kr.co.wisenut.textminer.batch.mapper;

import java.util.List;
import java.util.Map;

public interface SttContentsMapper {
	
	public String chkSttStatus (String applicationId);							// STT 변환상태 조회
	public List<Map<String, Object>> getSttContents (String applicationId);		// STT 테이블에서 텍스트 수집
	public List<String> getSttReprocessCallId ();								// STT 재수집 TA 재분석 대상 CallId 조회
	public int updateSttFailed (String applicationId);							// STT 수집 실패 - 세션 업데이트
}
