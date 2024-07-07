package kr.co.wisenut.textminer.autoqa.vo;

import kr.co.wisenut.textminer.common.vo.ImportProgressVo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AutoQaSimScriptVo extends ImportProgressVo {
	
	private int simScriptId;									// 유사스크립트ID
	private String simScriptCont;							// 유사스크립트내용
	private String delYn;											// 삭제여부
	private String creDt;											// 등록일자
	private String creUser;										// 등록자ID
	private String modDt;											// 수정날짜
	private String modUser;									// 수정자ID
	private int scriptId;									  // 스크립트ID
	
	private String uploadStatus;								// 업로드 상테
	
	private String role;												// 로그인 아이디의 권한
	
}
