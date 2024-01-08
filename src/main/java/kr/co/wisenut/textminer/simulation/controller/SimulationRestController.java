package kr.co.wisenut.textminer.simulation.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.co.wisenut.textminer.project.service.ProjectService;
import kr.co.wisenut.textminer.simulation.service.SimulationService;
import kr.co.wisenut.textminer.user.vo.TmUser;

@RestController
@RequestMapping("/simulationRest")
public class SimulationRestController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private SimulationService simulationService;
	
	// 시뮬레이션 리스트 조회
	@PostMapping("/getSimulationList")
	public Map<String, Object> getSimulationList( @RequestBody Map<String, Object> paramMap
											 , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user ){
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			paramMap.put("role", user.getAuthorities().toString());
			paramMap.put("projectOwner", user.getUsername());
			
			resultMap = simulationService.getSimulationList(paramMap);
		} catch (Exception e) {
			logger.error("tm-user getSimulationList failed. {}", e.getMessage());
		}
		
		return resultMap; 
	}
	
	// 서비스 모델 선택에 따른 시뮬레이션 모듈 로드
	@PostMapping("/getTestModule")
	public Map<String, Object> getTestModule(@RequestBody Map<String, Object> paramMap){
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			resultMap = simulationService.getTestModule(paramMap);
		} catch (Exception e) {
			logger.error("tm-user getTestModule failed. {}", e.getMessage());
		}
		
		return resultMap; 
	}
	
	// 서비스 모델 선택에 따른 시뮬레이션 모듈 로드
	@PostMapping("/callSimulation")
	public Map<String, Object> callSimulation(@RequestBody Map<String, Object> paramMap){
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			resultMap = simulationService.callSimulation(paramMap);
		} catch (Exception e) {
			logger.error("tm-user callSimulation failed. {}", e.getMessage());
		}
		
		return resultMap; 
	}

}
