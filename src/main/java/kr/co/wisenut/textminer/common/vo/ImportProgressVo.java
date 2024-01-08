package kr.co.wisenut.textminer.common.vo;

import java.util.List;

import kr.co.wisenut.textminer.common.TextMinerConstants;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImportProgressVo {
	
	private int importId;			// 업로드 ID
	private String resourceType;	// 업로드 대상	(COLLECTION : 컬렉션, DICTIONARY : 사전)
	private int resourceId;			// 업로드 대상 ID (COLLECTION_ID or DICTIONARY_ID)
	private String fileName;		// 파일명
	private String lastModified;	// 업로드 일자
	private long fileSize;			// 파일크기
	private int totalCount;			// 전체건수
	private int validCount;			// 성공건수
	private String errorHandle;		// 에러 제어방식 (SKIP : 무시하고 진행, STOP : 진행 중단)
	private String progress;		// 결과 (WAITING : 대기중, IN_PROGRESS : 진행중, SUCCESS : 전체성공, PARTIAL_SUCCESS : 부분성공, FAILURE : 실패)
	private String logText;			// 로그
	private String importDt;		// 등록일자
	
	public ImportProgressVo() {}

	public ImportProgressVo(String resourceType, int resourceId, String fileName, String lastModified,
			long fileSize, int totalCount, int validCount, String errorHandle, String progress) {
		super();
		this.resourceType = resourceType;
		this.resourceId = resourceId;
		this.fileName = fileName;
		this.lastModified = lastModified;
		this.fileSize = fileSize;
		this.totalCount = totalCount;
		this.validCount = validCount;
		this.errorHandle = errorHandle;
		this.progress = progress;
	};
	
	// 진행상태 리턴
	public String getLabel() {
		
		if (this.progress != null) {
			switch (this.progress) {
				case TextMinerConstants.PROGRESS_STATUS_SUCCESS:
					return TextMinerConstants.PROGRESS_STATUS_MSG_SUCCESS;
				case TextMinerConstants.PROGRESS_STATUS_IN_PROGRESS:
					return TextMinerConstants.PROGRESS_STATUS_MSG_IN_PROGRESS;
				case TextMinerConstants.PROGRESS_STATUS_PARTIAL_SUCCESS:
					return TextMinerConstants.PROGRESS_STATUS_MSG_PARTIAL_SUCCESS;
				case TextMinerConstants.PROGRESS_STATUS_WAITING:
					return TextMinerConstants.PROGRESS_STATUS_MSG_WAITING;
				case TextMinerConstants.PROGRESS_STATUS_FAILURE:
					return TextMinerConstants.PROGRESS_STATUS_MSG_FAILURE;
				default :
					return TextMinerConstants.COMMON_BLANK;
			}
		}
		return TextMinerConstants.COMMON_BLANK;
	}
}
