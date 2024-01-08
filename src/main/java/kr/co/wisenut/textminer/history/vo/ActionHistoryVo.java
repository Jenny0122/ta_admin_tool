package kr.co.wisenut.textminer.history.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActionHistoryVo {
	
	private String actionId;							// 활동ID
	private String actionDt;							// 활동일시
	private String actionType;							// 활동유형
	private String resourceId;							// 리소스 ID
	private String resourceType;						// 리소스 유형
	private String actionMsg;							// 활동내역
	private String actionUser;							// 사용자 ID
	private String userIp;								// 사용자 접속 IP
	
	private String role;								// 로그인 아이디의 권한
}
