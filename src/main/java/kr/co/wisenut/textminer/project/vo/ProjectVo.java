package kr.co.wisenut.textminer.project.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectVo {
	
	private int projectId;									// 프로젝트 ID
	private String projectName;								// 이름
	private String projectDesc;								// 설명
	private String projectOwner;							// 소유주
	private int collectionId;								// 컬렉션 ID - 프로젝트가 학습(모델생성)/분석 시 사용할 컬렉션 ID - 삭제여부 확인
	private String creDt;									// 등록일자
	private String creUser;									// 등록자ID
	private String modDt;									// 수정날짜
	private String modUser;									// 수정자ID

	private String role;									// 로그인 아이디의 권한
}
