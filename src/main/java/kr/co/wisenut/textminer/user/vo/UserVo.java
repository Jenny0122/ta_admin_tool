package kr.co.wisenut.textminer.user.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserVo{

	// Table Column
	private String userId;			// 사용자ID
	private String userPw;			// 패스워드
	private String userName;		// 사용자명
	private String userEmail;		// 이메일
	private String userAuth;		// 권한
	private String useYn;			// 사용여부
	private String creDt;			// 등록날짜
	private String creUser;			// 등록자ID
	private String modDt;			// 수정날짜
	private String modUser;			// 수정자ID
	
	/* 수협은행 SSO 추가 */
	private String hlfcDscd;		// 재직구분코드
	private String blntBrno;		// 소속점 번호
	
	// Business Column
	private String jobDiv;			// 작업구분 (1: 회원가입, 2: 사용자등록, 3. 사용자 변경, 4. 활성화/비활성화)
	
	// 사용여부 체크
	public boolean isEnabled() {
		if (useYn.equals("Y")) {
			return true; 
		} else {
			return false; 
		}
	}
	
	// 관리자계정 체크
	public boolean isAdministrator() {
		if (userAuth.equals("ADMIN")) {
			return true; 
		} else {
			return false; 
		}
	}

	@Override
	public String toString() {
		return "UserVo [userId=" + userId + ", userPw=" + userPw + ", userName=" + userName + ", userEmail=" + userEmail
				+ ", userAuth=" + userAuth + ", useYn=" + useYn + ", creDt=" + creDt + ", creUser=" + creUser
				+ ", modDt=" + modDt + ", modUser=" + modUser + ", jobDiv=" + jobDiv + "]";
	}
	
	
}
