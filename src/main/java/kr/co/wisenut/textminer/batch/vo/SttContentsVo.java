package kr.co.wisenut.textminer.batch.vo;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SttContentsVo {
	
	private String tbDiv;						// 조회 테이블 구분 ( 1 : 미매칭 테이블 / 2 : 세션 테이블 )
	private String applicationId;				// 상담 어플리케이션 ID
	private String isAnaClass;					// 유형분석 여부
	private List<String> sttListRx;				// 고객 STT데이터 (감성분석용 전처리)
//	private List<String> startTimeListRx;		// 고객 발화시작시간 데이터
//	private List<String> sttListTx;				// 상담사 STT데이터
	private String mergedSttContents;			// 통합 STT데이터
	private String mergedSttContentsPre;		// 통합 STT데이터 (요약용 전처리)
	private int muteCount;						// 묵음 횟수
	private int muteTime;						// 묵음 시간 

	private String resultCd;					// 재분석: 기존 resultCd
	private String isReStt = "N";				// STT 재처리건 여부
	
	@Override
	public String toString() {
		return "SttContentsVo [tbDiv=" + tbDiv + ", applicationId=" + applicationId + ", isAnaClass=" + isAnaClass + ", sttListRx="
				+ sttListRx + ", mergedSttContents=" + mergedSttContents + ", mergedSttContentsPre="
				+ mergedSttContentsPre + ", muteCount=" + muteCount + ", muteTime=" + muteTime + ", resultCd="
				+ resultCd + ", isReStt=" + isReStt + "]";
	}	


}
