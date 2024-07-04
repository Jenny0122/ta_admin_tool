package kr.co.wisenut.textminer.autoqa.vo;

import kr.co.wisenut.textminer.common.vo.ImportProgressVo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AutoQaScriptVo extends ImportProgressVo {
	
	private String categoryId;										// 카테고리ID
	private int scriptId;												// 스크립트ID
	private String scriptCont;									// 스크립트내용
	private String complianceItemCd;					// 준수항목코드
	private int score;											// 배점
	private int order;											// 순서
	private String delYn;											// 삭제여부
	private String creDt;											// 등록일자
	private String creUser;										// 등록자ID
	private String modDt;											// 수정날짜
	private String modUser;									// 수정자ID
		
	private String role;												// 로그인 아이디의 권한
	
}
