package  kr.co.wisenut.textminer.batch.mapper;

import java.util.List;
import java.util.Map;

import kr.co.wisenut.textminer.batch.vo.SttContentsVo;

public interface CommonMapper {
	
	public int updateAnaStatus(Map<String, Object> paramMap);					// 분석상태 업데이트
	public int updateAnaFailed (String applicationId);							// 분석 실패 - 세션 업데이트
	
	public int insertNotMatch(Map<String, Object> paramMap);					// 분석 실패(미 매칭) 정보 입력
	public int updateNotMatch(Map<String, Object> paramMap);					// 미매칭 테이블 분석 완료 시간 업데이트 
	
	public int updateMuteInfo (SttContentsVo sttContentsVo);					// 묵음시간 분석정보 업데이트
	
	public List<String> getNotMatchList (String applicationId);					// 잔여 미매칭 resultCd 목록 조회
}
