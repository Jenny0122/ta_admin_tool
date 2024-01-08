package kr.co.wisenut.textminer.model.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModelVo {

	private int modelId;
	private String modelName;
	private int taskId;
	private String taskType;
	private int projectId;
	private String enabled;
	private String creDt;
	private String creUser;

	private String role;									// 로그인 아이디의 권한
	private String collectionOwner;							// 컬렉션 소유자
	private String jobDiv;									// Classifier 작업구분
}
