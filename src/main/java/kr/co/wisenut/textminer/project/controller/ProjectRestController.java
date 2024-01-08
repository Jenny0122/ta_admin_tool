package kr.co.wisenut.textminer.project.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.co.wisenut.textminer.common.TextMinerConstants;
import kr.co.wisenut.textminer.history.service.ActionHistoryService;
import kr.co.wisenut.textminer.history.vo.ActionHistoryVo;
import kr.co.wisenut.textminer.project.service.ProjectService;
import kr.co.wisenut.textminer.project.vo.ProjectVo;
import kr.co.wisenut.textminer.user.vo.TmUser;

@RestController
@RequestMapping("/projectRest")
public class ProjectRestController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private ProjectService projectService;
	
	@Autowired
	private ActionHistoryService actionHistoryService;
	
	// 프로젝트 리스트 조회
	@PostMapping("/getProjectList")
	public Map<String, Object> getProjectList( @RequestBody Map<String, Object> paramMap
											 , HttpServletRequest request
											 , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user ){
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			paramMap.put("role", user.getAuthorities().toString());
			paramMap.put("projectOwner", user.getUsername());
			paramMap.put("contextPath", request.getContextPath());
			
			resultMap = projectService.getProjectList(paramMap);
		} catch (Exception e) {
			logger.error("tm-user getDataList failed. {}", e.getMessage());
		}
		
		return resultMap; 
	}
	
	// 프로젝트 상세조회
	@GetMapping("/getProjectDetail/{projectId:\\d+}")
	public Map<String, Object> getProjectDetail ( @PathVariable("projectId") int projectId
												, @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			ProjectVo projectVo = new ProjectVo();
			projectVo.setProjectId(projectId);
			projectVo.setRole(user.getAuthorities().toString());
			projectVo.setProjectOwner(user.getUsername());
			
			resultMap = projectService.getProjectDetail(projectVo);
		} catch (Exception e) {
			logger.error("tm-user getDataList failed. {}", e.getMessage());
		}
		return resultMap; 
	}
	
	// 프로젝트 추가
	@PostMapping("/insertProject")
	public Map<String, Object> insertProject( ProjectVo projectVo
											, HttpServletRequest request
											, @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user ){
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			projectVo.setRole(user.getAuthorities().toString());
			projectVo.setProjectOwner(user.getUsername());
			projectVo.setCreUser(user.getUsername());
			projectVo.setModUser(user.getUsername());
			
			resultMap = projectService.insertProject(projectVo);
			
			// 등록 완료 후 이력 저장
			if (resultMap.get("result").toString().equals("S")) {
				ActionHistoryVo actionHistoryVo = new ActionHistoryVo();
	
		        actionHistoryVo.setActionUser(user.getUsername());
		        actionHistoryVo.setResourceId("0");
		        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
		        actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_PROJECT_INSERT);
		        actionHistoryVo.setActionMsg(user.getUsername() + " 프로젝트 등록 ( 프로젝트명 : " + projectVo.getProjectName() + " )");
		        
		        if (request.getRemoteAddr().equals("0:0:0:0:0:0:0:1")) {
		        	actionHistoryVo.setUserIp("127.0.0.1");
		        } else {
		        	actionHistoryVo.setUserIp(request.getRemoteAddr());
		        }
		        
		        actionHistoryService.insertActionHistory(actionHistoryVo);
			}
		} catch (Exception e) {
			logger.error("tm-user getDataList failed. {}", e.getMessage());
		}
		
		return resultMap; 
	}
	
	// 프로젝트 수정
	@PostMapping("/updateProject")
	public Map<String, Object> updateProject( ProjectVo projectVo
											, HttpServletRequest request
											, @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user ){
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			projectVo.setRole(user.getAuthorities().toString());
			projectVo.setProjectOwner(user.getUsername());
			projectVo.setModUser(user.getUsername());
			
			resultMap = projectService.updateProject(projectVo);

			// 변경 완료 후 이력 저장
			if (resultMap.get("result").toString().equals("S")) {
				ActionHistoryVo actionHistoryVo = new ActionHistoryVo();
	
		        actionHistoryVo.setActionUser(user.getUsername());
		        actionHistoryVo.setResourceId("0");
		        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
		        actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_PROJECT_UPDATE);
		        actionHistoryVo.setActionMsg(user.getUsername() + " 프로젝트 정보 변경 ( 대상 프로젝트명 : " + projectVo.getProjectName() + " )");
		        
		        if (request.getRemoteAddr().equals("0:0:0:0:0:0:0:1")) {
		        	actionHistoryVo.setUserIp("127.0.0.1");
		        } else {
		        	actionHistoryVo.setUserIp(request.getRemoteAddr());
		        }
		        
		        actionHistoryService.insertActionHistory(actionHistoryVo);
			}
		} catch (Exception e) {
			logger.error("tm-user getDataList failed. {}", e.getMessage());
		}
		
		return resultMap; 
	}
	
	// 프로젝트 삭제
	@PostMapping("/deleteProject")
	public Map<String, Object> deleteProject( ProjectVo projectVo
											, HttpServletRequest request
											, @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user ){
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			projectVo.setRole(user.getAuthorities().toString());
			projectVo.setProjectOwner(user.getUsername());

			// 삭제 전 로그 저장 목적으로 프로젝트 정보 조회
			Map<String,Object> projectDetail = projectService.getProjectDetail(projectVo);
			projectVo = (ProjectVo) projectDetail.get("project");
			
			resultMap = projectService.deleteProject(projectVo);
			
			// 삭제 완료 후 이력 저장
			if (resultMap.get("result").toString().equals("S")) {
				ActionHistoryVo actionHistoryVo = new ActionHistoryVo();
	
		        actionHistoryVo.setActionUser(user.getUsername());
		        actionHistoryVo.setResourceId("0");
		        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
		        actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_PROJECT_DELETE);
		        actionHistoryVo.setActionMsg(user.getUsername() + " 프로젝트 삭제 ( 대상 프로젝트명 : " + projectVo.getProjectName() + " )");

		        if (request.getRemoteAddr().equals("0:0:0:0:0:0:0:1")) {
		        	actionHistoryVo.setUserIp("127.0.0.1");
		        } else {
		        	actionHistoryVo.setUserIp(request.getRemoteAddr());
		        }
		        
		        actionHistoryService.insertActionHistory(actionHistoryVo);
			}
		} catch (Exception e) {
			logger.error("tm-user getDataList failed. {}", e.getMessage());
		}
		
		return resultMap; 
	}
	

	// 테스트 모듈의 상태 조회
	@GetMapping("/statusTestModule")
	public Map<String, Object> statusTestModule(){
								
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			resultMap = projectService.getTestModuleStatus();
		} catch (Exception e) {
			logger.error("tm-user statusTestModule failed. {}", e.getMessage());
		}
		
		return resultMap; 
	}
}
