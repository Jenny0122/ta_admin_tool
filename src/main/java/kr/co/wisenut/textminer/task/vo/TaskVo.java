package kr.co.wisenut.textminer.task.vo;

import kr.co.wisenut.textminer.common.TextMinerConstants;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskVo {

	private int taskId;					// 테스크 ID
	private String taskName;			// 테스크명
	private String taskType;			// 테스크 구분
	private int projectId;				// 프로젝트 ID
	private int collectionId;			// 컬렉션 ID
	private String taskConfig;			// 테스크 설정정보
	private String modelName;			// 모델명
	private String modelFile;			// 모델파일명
	private String enabled;				// 사용여부
	private String creDt;				// 생성일자
	private String creUser;				// 생성자
	private String modDt;				// 수정일자
	private String modUser;				// 수정자
	private String lastAnalyzeDt;		// 마지막 분석일시
	
	private String role;
	private String enableFlag;
	
	public String getLabel() {
		switch(this.taskType) {
			case TextMinerConstants.TASK_TYPE_AUTO_CLASSIFICATION:
				return TextMinerConstants.TASK_TYPE_NAME_AUTO_CLASSIFICATION;
			case TextMinerConstants.TASK_TYPE_DOCUMENT_SUMMARY:
				return TextMinerConstants.TASK_TYPE_NAME_DOCUMENT_SUMMARY;
			case TextMinerConstants.TASK_TYPE_EMOTION_ANALYZE:
				return TextMinerConstants.TASK_TYPE_NAME_EMOTION_ANALYZE;
			case TextMinerConstants.TASK_TYPE_KEYWORD_EXTRACTION:
				return TextMinerConstants.TASK_TYPE_NAME_KEYWORD_EXTRACTION;
			case TextMinerConstants.TASK_TYPE_RELATED_EXTRACTION:
				return TextMinerConstants.TASK_TYPE_NAME_RELATED_EXTRACTION;
			case TextMinerConstants.TASK_TYPE_EMOTION_PREPROECESS:
				return TextMinerConstants.TASK_TYPE_NAME_EMOTION_PREPROECESS;
			case TextMinerConstants.TASK_TYPE_SUMMARY_PREPROECESS:
				return TextMinerConstants.TASK_TYPE_NAME_SUMMARY_PREPROECESS;
			case TextMinerConstants.TASK_TYPE_STRING_MATCHER:
				return TextMinerConstants.TASK_TYPE_NAME_STRING_MATCHER;
			default:
				return TextMinerConstants.COMMON_BLANK;
		}
	}
}
