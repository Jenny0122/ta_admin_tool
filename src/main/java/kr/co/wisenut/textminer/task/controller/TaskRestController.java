package kr.co.wisenut.textminer.task.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import kr.co.wisenut.textminer.common.TextMinerConstants;
import kr.co.wisenut.textminer.history.service.ActionHistoryService;
import kr.co.wisenut.textminer.history.vo.ActionHistoryVo;
import kr.co.wisenut.textminer.task.service.TaskService;
import kr.co.wisenut.textminer.task.vo.TaskVo;
import kr.co.wisenut.textminer.user.vo.TmUser;

@RestController
@RequestMapping("/taskRest")
public class TaskRestController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private TaskService taskService;

	@Autowired
	private ActionHistoryService actionHistoryService;
	
	// 서비스 리스트 가져오기
	@PostMapping("/getModelList")
	public Map<String, Object> getModelList( @RequestBody Map<String, Object> paramMap
										   , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user ){
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			resultMap = taskService.getModelList(paramMap);
		} catch (Exception e) {
			logger.error("tm-user getModelList failed. {}", e.getMessage());
		}
		
		return resultMap; 
	}
	
	// 서비스 상세정보 가져오기
	@PostMapping("/getTaskInfo")
	public Map<String, Object> getTaskInfo( @RequestBody Map<String, Object> paramMap
			   							  , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user ){

		paramMap.put("role", user.getAuthorities().toString());
		paramMap.put("collectionOwner", user.getUsername());
		paramMap.put("dictionaryOwner", user.getUsername());
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			resultMap = taskService.getTaskInfo(paramMap);
		} catch (Exception e) {
			logger.error("tm-user getModelList failed. {}", e.getMessage());
		}
				
		return resultMap; 
	}

	// 테스크 정보 저장
	@PostMapping("/saveTask")
	public Map<String, Object> saveTask( @RequestBody Map<String, Object> paramMap
									   , HttpServletRequest request
									   , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user ) {

		paramMap.put("userId", user.getUsername());
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			resultMap = taskService.saveTask(paramMap);
			
			// 저장 완료 후 이력 저장
			if (resultMap.get("result").toString().equals("S")) {
				ActionHistoryVo actionHistoryVo = new ActionHistoryVo();
	
		        actionHistoryVo.setActionUser(user.getUsername());
		        actionHistoryVo.setResourceId("0");
		        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
		        
		        if (paramMap.get("job").toString().equals("I")) {
			        actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_SERVICE_INSERT);
			        actionHistoryVo.setActionMsg(user.getUsername() + " 서비스 저장 ( 서비스 명 : " + paramMap.get("taskName").toString() + " )");
		        } else {
			        actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_SERVICE_UPDATE);
			        actionHistoryVo.setActionMsg(user.getUsername() + " 서비스 정보 변경 ( 대상 서비스 명 : " + paramMap.get("taskName").toString() + " )");
		        }

		        if (request.getRemoteAddr().equals("0:0:0:0:0:0:0:1")) {
		        	actionHistoryVo.setUserIp("127.0.0.1");
		        } else {
		        	actionHistoryVo.setUserIp(request.getRemoteAddr());
		        }
		        
		        actionHistoryService.insertActionHistory(actionHistoryVo);
			}
		} catch (Exception e) {
			logger.error("tm-user saveTask failed. {}", e.getMessage());
		}
		
		return resultMap;
	}
	
	// 사용 / 미사용처리
	@PostMapping("/updateEnabled")
	public Map<String, Object> updateEnabled( @RequestBody Map<String, Object> paramMap
											, HttpServletRequest request
											, @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user ){

		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			paramMap.put("user", user.getUsername());
			resultMap = taskService.updateEnabled(paramMap);
			
			// 사용/미사용 변경 후 이력 저장
			if (resultMap.get("result").toString().equals("S")) {
				ActionHistoryVo actionHistoryVo = new ActionHistoryVo();
	
		        actionHistoryVo.setActionUser(user.getUsername());
		        actionHistoryVo.setResourceId("0");
		        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
		        
		        if (paramMap.get("enabled").toString().equals("Y")) {
		        	actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_SERVICE_UPDATE);
			        actionHistoryVo.setActionMsg(user.getUsername() + " 서비스 사용 처리 ( 대상 서비스 명 : " + paramMap.get("taskName").toString() + " )");
		        } else {
		        	actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_SERVICE_UPDATE);
			        actionHistoryVo.setActionMsg(user.getUsername() + " 서비스 미사용 처리 ( 대상 서비스 명 : " + paramMap.get("taskName").toString() + " )");
		        }

		        if (request.getRemoteAddr().equals("0:0:0:0:0:0:0:1")) {
		        	actionHistoryVo.setUserIp("127.0.0.1");
		        } else {
		        	actionHistoryVo.setUserIp(request.getRemoteAddr());
		        }
		        
		        actionHistoryService.insertActionHistory(actionHistoryVo);
			}
			
		} catch (Exception e) {
			logger.error("tm-user updateEnabled failed. {}", e.getMessage());
		}

		return resultMap; 
	}

	// 테스크 정보 삭제
	@PostMapping("/deleteTask")
	public Map<String, Object> deleteTask( @RequestBody Map<String, Object> paramMap
										 , HttpServletRequest request
									     , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user ) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			TaskVo taskVo = new TaskVo();
			taskVo.setProjectId(Integer.parseInt(paramMap.get("projectId").toString()));
			taskVo.setTaskId(Integer.parseInt(paramMap.get("taskId").toString()));
			
			resultMap = taskService.deleteTask(taskVo);
			
			// 삭제 완료 후 이력저장
			if (resultMap.get("result").toString().equals("S")) {
				ActionHistoryVo actionHistoryVo = new ActionHistoryVo();
	
		        actionHistoryVo.setActionUser(user.getUsername());
		        actionHistoryVo.setResourceId("0");
		        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
	        	actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_SERVICE_DELETE);
		        actionHistoryVo.setActionMsg(user.getUsername() + " 서비스 삭제 ( 대상 서비스 명 : " + resultMap.get("deleteTask").toString() + " )");
		        
		        if (request.getRemoteAddr().equals("0:0:0:0:0:0:0:1")) {
		        	actionHistoryVo.setUserIp("127.0.0.1");
		        } else {
		        	actionHistoryVo.setUserIp(request.getRemoteAddr());
		        }
		        
		        actionHistoryService.insertActionHistory(actionHistoryVo);
			}
		} catch (Exception e) {
			logger.error("tm-user deleteTask failed. {}", e.getMessage());
		}
		
		return resultMap;
	}
	
	@PostMapping("/getTaskForm")
	public Map<String, Object> getTaskForm( @RequestBody Map<String, Object> paramMap
							 			  , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user ){
		
		paramMap.put("role", user.getAuthorities().toString());
		paramMap.put("collectionOwner", user.getUsername());
		paramMap.put("dictionaryOwner", user.getUsername());
		
		return taskService.getTaskForm(paramMap); 
	}
	
	// 컬렉션 및 사전이 현재 사용중인지 체크
	@PostMapping("/chkTaskResouce")
	public Map<String, Object> chkTaskResouce( @RequestBody Map<String, Object> paramMap
			 							  , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user ){
		return taskService.chkTaskResouce(paramMap); 
	}	
}
