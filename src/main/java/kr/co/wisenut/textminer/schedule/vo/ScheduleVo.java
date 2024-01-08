package kr.co.wisenut.textminer.schedule.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduleVo {
	
	private int scheduleId;									// 스케줄ID
	private String scheduleName;							// 스케줄명
	private String scheduleType;							// 스케줄 타입
	private String startDt;									// 시작일자
	private String startTime;								// 시작시간
	private String endDt;									// 종료일자
	private String endTime;									// 종료시간
	private int executeInterval;							// 실행간격 
	private String executeTimeAndInterval;					// 수행시간 / 반복주기 
	private String lastExecuteDt;							// 마지막 실행시간 
	private String lastExecuteStatus;						// 마지막 실행상태 
	private int taskId;										// 태스크ID
	private String useYn;									// 사용여부
	private String creDt;									// 등록일자
	private String creUser;									// 등록자ID
	private String modDt;									// 수정날짜
	private String modUser;									// 수정자ID
	
	private String projectId;								// 프로젝트ID
	private String projectName;								// 프로젝트명
	private String projectOwner;							// 소유주
	
	private String taskName;
	
	private String role;									// 로그인 아이디의 권한
}
