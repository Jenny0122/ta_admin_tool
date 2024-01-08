package kr.co.wisenut.textminer.task.mapper;

import java.util.List;
import java.util.Map;

import kr.co.wisenut.textminer.model.vo.ModelVo;
import kr.co.wisenut.textminer.task.vo.TaskVo;

public interface TaskMapper {

	public int chkDuplicatedTaskName(TaskVo task);							// 테스크 이름 중복체크 
	public int chkTaskResouce(Map<String, Object> paramMap);				// 컬렉션 및 사전이 현재 사용중인지 체크
	
	public int chkEnabledTask(TaskVo task);									// 사용중인 테스크 확인
	public int getTaskCnt(TaskVo task);										// 테스크 구분 별 기 등록된 테스크 건수 가져오기
	public List<TaskVo> getTaskList(Map<String, Object> paramMap);			// 프로젝트에 해당하는 테스크 리스트 가져오기
	public TaskVo getTaskInfo(TaskVo task);									// 테스크 상세조회
	public TaskVo getTaskInfoUseModel(ModelVo model);						// 테스크 상세조회(모델사용)
	
	public int insertTask(TaskVo task);										// 테스크 등록
	public int updateTask(TaskVo task);										// 테스크 수정
	public int deleteTask(TaskVo task);										// 테스크 삭제
}
