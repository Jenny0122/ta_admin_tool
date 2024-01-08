package kr.co.wisenut.textminer.deploy.vo;

import kr.co.wisenut.textminer.common.TextMinerConstants;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeployVo {
	
	private int serverId;									// 서버ID
	private String serverName;								// 서버명
	private String serverDesc;								// 설명
	private String serverType;								// 구분 /* TEST / PROD */
	private String serverTask;								// 작업구분
	private String serverIp;								// 서버IP
	private int serverPort;									// 서버PORT
	private String creDt;									// 등록일자
	private String creUser;									// 등록자ID
	private String modDt;									// 수정날짜
	private String modUser;									// 수정자ID
	
	private int useTaskCnt;									// 사용으로 설정된 서비스 건수
	
	// 병합용 필드
	private int serverCnt;									// 모듈 갯수
	private int taskCnt;									// 각 서비스별 갯수
	
	public String getLabel() {
		switch(this.serverTask) {
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