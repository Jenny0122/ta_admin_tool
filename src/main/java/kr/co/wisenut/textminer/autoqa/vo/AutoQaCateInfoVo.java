package kr.co.wisenut.textminer.autoqa.vo;

import kr.co.wisenut.textminer.common.vo.ImportProgressVo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AutoQaCateInfoVo extends ImportProgressVo {
	
	private String clsCategoryId;										// 분류카테고리ID
	private String clsCategoryName;								// 분류카테고리명
	private String pClsCategoryId;									// 부모 상담분류 ID
	private String pClsCategoryName;							// 부모분류카테고리명
	private String delYn;														// 삭제여부
	private String creDt;														// 등록일자
	private String creUser;													// 등록자ID
	private String modDt;														// 수정날짜
	private String modUser;												// 수정자ID
	
	private String uploadStatus;							// 업로드 상테
	
	private String role;									// 로그인 아이디의 권한
	
}
