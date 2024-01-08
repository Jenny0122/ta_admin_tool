package kr.co.wisenut.textminer.batch.mapper;

import java.util.List;
import java.util.Map;

import kr.co.wisenut.textminer.batch.vo.SttContentsVo;

public interface BatchMapper {
	
	// 1. 집계
	public List<Map<String, Object>> getModuleFailedCallInfo ();					// 모듈분석 실패콜 재분석 대상 CallInfo 조회
	
	public int keywordTotal (Map<String, Object> paramMap);							// 키워드 집계
	public int keywordTotalInTable (Map<String, Object> paramMap);
	public int keywordTotalUpMin (Map<String, Object> paramMap);
	public int keywordTotalUpHour (Map<String, Object> paramMap);
	public int keywordDelete (Map<String, Object> paramMap);
	
	public int claSentTotal (Map<String, Object> paramMap);							// 유형/감성분석 집계 
	public int claSentTotalInTable (Map<String, Object> paramMap);	
	public int claSentTotalUpMin (Map<String, Object> paramMap);
	public int claSentTotalUpHour (Map<String, Object> paramMap);
	public int clsSentDelete (Map<String, Object> paramMap);
	
	public int agentSentTotal (Map<String, Object> paramMap);						// 상담사별 감성분석 집계 
	public int agentSentTotalInTable (Map<String, Object> paramMap);
	public int agentSentTotalUpMin (Map<String, Object> paramMap);
	public int agentSentTotalUpHour (Map<String, Object> paramMap);
	public int agentSentDelete (Map<String, Object> paramMap);
	
	public int stopwordTotal (Map<String, Object> paramMap);						// 금칙어 집계 
	public int stopwordTotalInTable (Map<String, Object> paramMap);	
	public int stopwordTotalUpMin (Map<String, Object> paramMap);
	public int stopwordTotalUpHour (Map<String, Object> paramMap);
	public int stopwordDelete (Map<String, Object> paramMap);
	
	public int updateTotalStatus (Map<String, Object> paramMap);					// 세션테이블 status 업데이트처리 (400400)

	public int mergeCustInfo(Map<String, Object> paramMap);							// 세션테이블 고객정보 업데이트 (mapper)
	
	public Map<String, Object> getSendMsgInfo();									// 반복민원 데이터 조회
	public List<Map<String, Object>> getSendEdwList();								// EDW 발송데이터 조회
	
	public int deleteLog();															// 2년 지난 로그삭제
	public int deleteAnalyzeData();													// 5년 지난 분석결과 데이터 삭제
	public int deleteStatClassData();												// 5년 지난 유형분류 통계 데이터 삭제
	public int deleteStatSentimentData();											// 5년 지난 감성분석 통계 데이터 삭제
	public int deleteStatKeywordData();												// 5년 지난 키워드 통계 데이터 삭제
	public int deleteStatStopWordData();											// 5년 지난 금칙어 통계 데이터 삭제
	
	//=============================================================================================================================
	
	// 2. 재분석
	public int insertClassifireResult(Map<String, Object> paramMap);				// 자동분류 결과저장
	public int insertSentimentResult(Map<String, Object> paramMap);					// 감성분석 결과저장
	public int updateSentimentCallResult(Map<String, Object> paramMap);				// 콜단위 감성분석 결과 업데이트
	
	public int insertKeywordExtractionResult(Map<String, Object> paramMap);			// 키워드 추출 결과저장
	public int insertRelatedKeywordExtractionResult(Map<String, Object> paramMap);	// 연관어 추출 결과저장
	public int insertSummarizeResult(Map<String, Object> paramMap);					// 문서요약 결과저장
	
	public int deleteClassifireResult(SttContentsVo sttContentsVo);					// 유형분석 삭제
	public int deleteSentimentResult(SttContentsVo sttContentsVo);					// 감성분석 삭제
	public int deleteKeywordExtractionResult(SttContentsVo sttContentsVo);			// 키워드추출 삭제
	public int deleteRelatedKeywordExtractionResult(SttContentsVo sttContentsVo);	// 연관어추출 삭제
	public int deleteSummarizeResult(SttContentsVo sttContentsVo);					// 문서요약 삭제
}
