package kr.co.wisenut.textminer.collection.vo;

import kr.co.wisenut.textminer.common.TextMinerConstants;
import kr.co.wisenut.textminer.common.vo.ImportProgressVo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CollectionVo extends ImportProgressVo {
	
	private int collectionId;								// 컬랙션 ID
	private String collectionName;							// 이름
	private String collectionType;							// 컬렉션 구분	( CLASSIFICATION : 분류 / EMOTION : 감성 )
	private String collectionJob;							// 작업구분 ( ANALYSIS : 분석 / VERIFIACATION : 검증 )
	private String collectionDesc;							// 설명
	private String collectionOwner;							// 소유자ID
	private String fieldInfo;								// 문서 구조
	private String originalFieldInfo;						// 원본 문서 구조
	private String documentField;							// 문서 필드
	private String documentList;							// 문서 내용
	private int documentCount;								// 문서 건수
	private String creDt;									// 등록일자
	private String creUser;									// 등록자ID
	private String modDt;									// 수정날짜
	private String modUser;									// 수정자ID
	
	private String uploadStatus;							// 업로드 상테
	
	private String role;									// 로그인 아이디의 권한
	
	// 컬렉션 구분 명칭 리턴
	public String getCollectionTypeName() {
		switch (this.collectionType) {
			case TextMinerConstants.COLLECTION_TYPE_CLASSIFICATION :
				return TextMinerConstants.COLLECTION_TYPE_NAME_CLASSIFICATION;
			case TextMinerConstants.COLLECTION_TYPE_EMOTION :
				return TextMinerConstants.COLLECTION_TYPE_NAME_EMOTION;
			default:
				return TextMinerConstants.COMMON_BLANK;
		}
	}
	
	// 작업 구분 명칭 리턴
	public String getCollectionJobName() {
		switch (this.collectionJob) {
			case TextMinerConstants.COLLECTION_JOB_ANALYSIS :
				return TextMinerConstants.COLLECTION_JOB_NAME_ANALYSIS;
			case TextMinerConstants.COLLECTION_JOB_TRAINING :
				return TextMinerConstants.COLLECTION_JOB_NAME_TRAINING;
		}
		
		return TextMinerConstants.COMMON_BLANK;
	}
}
