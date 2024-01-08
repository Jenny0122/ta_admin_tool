package kr.co.wisenut.textminer.batch.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApCallProcessVo {
	
	private int sessId;
	
	private String serviceType;					// 서비스 유형 코드 
	private String apCallID;					// 상담 어플리케이션 ID
	private String channelCode;					// 채널 코드 
	private String deviceCode;					// 매체구분 
	private String fcId;						// 상담사 ID 	
	private String fcName;						// 상담사 이름 
	private String officeCode;					// 지점 번호 
	private String custId;						// 고객 식별 번호 
	private String gndrCd;						// 식별번호 -> 성별 코드 
	private String ageCd;						// 식별번호 -> 연령 구간 코드 
	private String custScCd;					// 식별번호 -> 은행/회원 조합 
//	private String areaCode;					// 지역 코드 

	@Override
	public String toString() {
		return "ApCallProcessVo [sessId=" + sessId + ", serviceType=" + serviceType + ", apCallID=" + apCallID
				+ ", channelCode=" + channelCode + ", deviceCode=" + deviceCode + ", fcId=" + fcId + ", fcName="
				+ fcName + ", officeCode=" + officeCode + ", custId=" + custId + ", gndrCd=" + gndrCd + ", ageCd="
				+ ageCd + ", custScCd=" + custScCd + "]";
	}	

}
