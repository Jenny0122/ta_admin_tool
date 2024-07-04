package kr.co.wisenut.textminer.autoqa.vo;

import kr.co.wisenut.textminer.common.vo.ImportProgressVo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AutoQaCateVo extends ImportProgressVo {
	
	private int categoryId;										// 상담분류ID
	private String categoryName;							// 상담분류이름
	private String pcategoryId;								// 부모상담분류ID
	private String pcategoryName;						// 부모상담분류이름
	private String categoryType;							// 카테고리구분(01:인바운드/02:아웃바운드)
	private String classCategoryId;						// 분류카테고리ID
	private String useYn;											// 사용여부
	private String creDt;											// 등록일자
	private String creUser;										// 등록자ID
	private String modDt;											// 수정날짜
	private String modUser;									// 수정자ID
	
	private String uploadStatus;								// 업로드 상테
	
	private String role;												// 로그인 아이디의 권한
	
}
