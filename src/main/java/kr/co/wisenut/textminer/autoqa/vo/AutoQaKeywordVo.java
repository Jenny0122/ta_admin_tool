package kr.co.wisenut.textminer.autoqa.vo;

import kr.co.wisenut.textminer.common.vo.ImportProgressVo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AutoQaKeywordVo extends ImportProgressVo {
	
	private String keywordId;										// 키워드ID
	private String keywordName;								// 키워드명
	private String delYn;												// 삭제여부
	private String creDt;												// 등록일자
	private String creUser;											// 등록자ID
	private String modDt;												// 수정날짜
	private String modUser;										// 수정자ID
	
	private String uploadStatus;							// 업로드 상테
	
	private String role;									// 로그인 아이디의 권한
	
}
