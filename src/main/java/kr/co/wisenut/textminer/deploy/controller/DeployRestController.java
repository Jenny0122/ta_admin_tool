package kr.co.wisenut.textminer.deploy.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.co.wisenut.textminer.common.TextMinerConstants;
import kr.co.wisenut.textminer.deploy.service.DeployService;
import kr.co.wisenut.textminer.deploy.vo.DeployVo;
import kr.co.wisenut.textminer.history.service.ActionHistoryService;
import kr.co.wisenut.textminer.history.vo.ActionHistoryVo;
import kr.co.wisenut.textminer.schedule.vo.ScheduleVo;
import kr.co.wisenut.textminer.user.vo.TmUser;

@RestController
@RequestMapping("/deployRest")
public class DeployRestController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private DeployService deployService;

	@Autowired
	private ActionHistoryService actionHistoryService;

	// 배포 서버 정보 리스트 조회
	@PostMapping("/getDeployList")
	public Map<String, Object> getDeployList( DeployVo deployVo
											 , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user ){
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			resultMap = deployService.getDeployList(deployVo);
		} catch (Exception e) {
			logger.error("tm-user getDeployList failed. {}", e.getMessage());
		}
		
		return resultMap; 
	}
	
	// 배포 서버 정보 상세 조회
	@GetMapping("/getDeployDetail/{serverId:\\d+}")
	public Map<String, Object> getDeployDetail ( @PathVariable("serverId") int serverId
												, @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			DeployVo dyployVo = new DeployVo();
			dyployVo.setServerId(serverId);
			
			resultMap = deployService.getDeployDetail(dyployVo);
		} catch (Exception e) {
			logger.error("tm-user getDeployDetail failed. {}", e.getMessage());
		}
		return resultMap; 
	}
	
	// 배포 서버 정보 추가 
	@PostMapping("/insertDeploy")
	public Map<String, Object> insertDeploy( DeployVo deployVo
										   , HttpServletRequest request
										   , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user ){
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			deployVo.setCreUser(user.getUsername());
			deployVo.setModUser(user.getUsername());
			
			resultMap = deployService.insertDeploy(deployVo);
			
			// 등록 작업 후 이력 저장
			if (resultMap.get("result").toString().equals("S")) {
				ActionHistoryVo actionHistoryVo = new ActionHistoryVo();
	
		        actionHistoryVo.setActionUser(user.getUsername());
		        actionHistoryVo.setResourceId("0");
		        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
	        	actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_MODULE_INSERT);
		        actionHistoryVo.setActionMsg(user.getUsername() + " 모듈 등록 ( 모듈 명 : " + deployVo.getServerName() + " )");

		        if (request.getRemoteAddr().equals("0:0:0:0:0:0:0:1")) {
		        	actionHistoryVo.setUserIp("127.0.0.1");
		        } else {
		        	actionHistoryVo.setUserIp(request.getRemoteAddr());
		        }
		        
		        actionHistoryService.insertActionHistory(actionHistoryVo);
			}
		} catch (Exception e) {
			logger.error("tm-user insertDeploy failed. {}", e.getMessage());
		}
		
		return resultMap; 
	}
	
	// 배포 서버 정보 수정
	@PostMapping("/updateDeploy")
	public Map<String, Object> updateDeploy( DeployVo deployVo
										   , HttpServletRequest request
										   , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user ){
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			deployVo.setModUser(user.getUsername());
						
			resultMap = deployService.updateDeploy(deployVo);
			
			// 변경 작업 후 이력 저장
			if (resultMap.get("result").toString().equals("S")) {
				ActionHistoryVo actionHistoryVo = new ActionHistoryVo();
	
		        actionHistoryVo.setActionUser(user.getUsername());
		        actionHistoryVo.setResourceId("0");
		        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
	        	actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_MODULE_UPDATE);
		        actionHistoryVo.setActionMsg(user.getUsername() + " 모듈 정보변경 ( 대상 모듈 명 : " + deployVo.getServerName() + " )");

		        if (request.getRemoteAddr().equals("0:0:0:0:0:0:0:1")) {
		        	actionHistoryVo.setUserIp("127.0.0.1");
		        } else {
		        	actionHistoryVo.setUserIp(request.getRemoteAddr());
		        }
		        
		        actionHistoryService.insertActionHistory(actionHistoryVo);
			}
		} catch (Exception e) {
			logger.error("tm-user updateDeploy failed. {}", e.getMessage());
		}
		
		return resultMap; 
	}
	
	// 배포 서버 정보 삭제 
	@PostMapping("/deleteDeploy")
	public Map<String, Object> deleteDeploy( DeployVo deployVo
										   , HttpServletRequest request
										   , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user ){
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {			
			Map<String, Object> deployInfo = deployService.getDeployDetail(deployVo);
			deployVo = (DeployVo) deployInfo.get("deploy");
			
			resultMap = deployService.deleteDeploy(deployVo);
			
			// 삭제 작업 후 이력 저장
			if (resultMap.get("result").toString().equals("S")) {
				ActionHistoryVo actionHistoryVo = new ActionHistoryVo();
	
		        actionHistoryVo.setActionUser(user.getUsername());
		        actionHistoryVo.setResourceId("0");
		        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
	        	actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_MODULE_DELETE);
		        actionHistoryVo.setActionMsg(user.getUsername() + " 모듈 삭제 ( 대상 모듈 명 : " + deployVo.getServerName() + " )");

		        if (request.getRemoteAddr().equals("0:0:0:0:0:0:0:1")) {
		        	actionHistoryVo.setUserIp("127.0.0.1");
		        } else {
		        	actionHistoryVo.setUserIp(request.getRemoteAddr());
		        }
		        
		        actionHistoryService.insertActionHistory(actionHistoryVo);
			}
		} catch (Exception e) {
			logger.error("tm-user deleteDeploy failed. {}", e.getMessage());
		}
		
		return resultMap; 
	}
	
	// health check
	@GetMapping("/healthChk/{serverId:\\d+}")
	public Map<String, Object> healthChkModule(@PathVariable int serverId) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			resultMap = deployService.healthCheck(serverId);
		} catch (Exception e) {
			logger.error("tm-user healthChkModule failed. {}", e.getMessage());
		}
		
		return resultMap; 
	}
	
	// deploy
	@GetMapping("/deployModel/{serverId:\\d+}")
	public Map<String, Object> deployModel( @PathVariable int serverId
										  , HttpServletRequest request
										  , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {			
			resultMap = deployService.deployModel(serverId);

			// 배포 작업 후 이력 저장
			if (resultMap.get("result").toString().equals("S")) {
				// 이력 저장을 위한 모듈 정보 조회
				DeployVo deployVo = new DeployVo();
				deployVo.setServerId(serverId);
				Map<String, Object> deployInfo = deployService.getDeployDetail(deployVo);
				deployVo = (DeployVo) deployInfo.get("deploy");
				
				ActionHistoryVo actionHistoryVo = new ActionHistoryVo();
	
		        actionHistoryVo.setActionUser(user.getUsername());
		        actionHistoryVo.setResourceId("0");
		        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
	        	actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_MODULE_DEPLOY);
		        actionHistoryVo.setActionMsg(user.getUsername() + " 배포 수행 ( 배포 모듈 명 : " + deployVo.getServerName() + " )");
		        
		        if (request.getRemoteAddr().equals("0:0:0:0:0:0:0:1")) {
		        	actionHistoryVo.setUserIp("127.0.0.1");
		        } else {
		        	actionHistoryVo.setUserIp(request.getRemoteAddr());
		        }
		        
		        actionHistoryService.insertActionHistory(actionHistoryVo);
			}
		} catch (Exception e) {
			logger.error("tm-user deployModel failed. {}", e.getMessage());
		}
		
		return resultMap; 
	}
	
	// reboot status check
	@GetMapping("/rebootStatusCheck/{serverId:\\d+}")
	public Map<String, Object> rebootStatusCheck(@PathVariable int serverId) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			resultMap = deployService.rebootStatusCheck(serverId);
		} catch (Exception e) {
			logger.error("tm-user rebootStatusCheck failed. {}", e.getMessage());
		}
		
		return resultMap; 
	}
	
	// reboot
	@GetMapping("/rebootModule/{serverId:\\d+}")
	public Map<String, Object> rebootServer( @PathVariable int serverId
										   , HttpServletRequest request
										   , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			resultMap = deployService.rebootServer(serverId);
			
			// 배포 작업 후 이력 저장
			ActionHistoryVo actionHistoryVo = new ActionHistoryVo();

	        actionHistoryVo.setActionUser(user.getUsername());
	        actionHistoryVo.setResourceId("0");
	        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
        	actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_MODULE_DEPLOY);

	        if (request.getRemoteAddr().equals("0:0:0:0:0:0:0:1")) {
	        	actionHistoryVo.setUserIp("127.0.0.1");
	        } else {
	        	actionHistoryVo.setUserIp(request.getRemoteAddr());
	        }
        	
        	if (serverId == 0) {
        		actionHistoryVo.setActionMsg(user.getUsername() + " 모든 모듈 재기동 수행");
        	} else {
    			// 이력 저장을 위한 모듈 정보 조회
    			DeployVo deployVo = new DeployVo();
    			deployVo.setServerId(serverId);
    			Map<String, Object> deployInfo = deployService.getDeployDetail(deployVo);
    			deployVo = (DeployVo) deployInfo.get("deploy");
    			
        		actionHistoryVo.setActionMsg(user.getUsername() + " 모듈 재기동 수행 ( 재기동 모듈 명 : " + deployVo.getServerName() + " )");
        	}
        	
	        actionHistoryService.insertActionHistory(actionHistoryVo);
		} catch (Exception e) {
			logger.error("tm-user rebootServer failed. {}", e.getMessage());
		}
		
		return resultMap; 
	}
}
