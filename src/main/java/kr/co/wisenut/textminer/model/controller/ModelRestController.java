package kr.co.wisenut.textminer.model.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;

import kr.co.wisenut.textminer.common.TextMinerConstants;
import kr.co.wisenut.textminer.model.service.ModelService;
import kr.co.wisenut.textminer.model.vo.ModelVo;
import kr.co.wisenut.textminer.task.service.TaskService;
import kr.co.wisenut.textminer.task.vo.TaskVo;
import kr.co.wisenut.textminer.user.vo.TmUser;

@RestController
@RequestMapping("/modelRest")
public class ModelRestController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private ModelService modelService;
	
	// 모델생성 및 분석 가능여부 확인
	@PostMapping("/chkStatus")
	public Map<String, Object> chkStatus( @RequestBody Map<String, Object> paramMap
										, @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user ) {

		Map<String, Object> resultMap = new HashMap<String, Object>();

		ModelVo modelVo = new ModelVo();
		modelVo.setProjectId(Integer.parseInt(paramMap.get("projectId").toString()));
		modelVo.setTaskId(Integer.parseInt(paramMap.get("taskId").toString()));
		modelVo.setTaskType(paramMap.get("taskType").toString());
		modelVo.setRole(user.getAuthorities().toString());
		modelVo.setCollectionOwner(user.getUsername());
		
		// Classifier 작업 변수 (TRAINING, ANALYSIS)
		if (paramMap.get("jobDiv") != null) {
			modelVo.setJobDiv(paramMap.get("jobDiv").toString());
		}
		
		try {
			resultMap = modelService.chkStatus(modelVo);
		} catch (Exception e) {
			logger.error("tm-project chkStatus failed. {}", e.getMessage());
		}
		
		return resultMap; 
	}
	
	// 모델 생성요청
	@PostMapping("/createModel")
	public Map<String, Object> createModel( @RequestBody Map<String, Object> paramMap
										  , HttpServletRequest request
										  , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user ) {

        if (request.getRemoteAddr().equals("0:0:0:0:0:0:0:1")) {
        	user.setUserIp("127.0.0.1");
        } else {
        	user.setUserIp(request.getRemoteAddr());
        }
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		ModelVo modelVo = new ModelVo();
		modelVo.setProjectId(Integer.parseInt(paramMap.get("projectId").toString()));
		modelVo.setTaskId(Integer.parseInt(paramMap.get("taskId").toString()));
		modelVo.setTaskType(paramMap.get("taskType").toString());
		modelVo.setRole(user.getAuthorities().toString());
		modelVo.setCollectionOwner(user.getUsername());
		
		modelService.createModel(modelVo, user)
					.addCallback((Map<String, Object> result) -> {
									logger.info("tm-project model-create complete successfully. {}", result);
								}, (Throwable ex) -> {
									logger.info("tm-project model-create failed. {}", ex.toString());
								});

		return resultMap; 
	}
	
	// 사전 및 패턴 설정요청
	@PostMapping("/replaceDictionary")
	public Map<String, Object> replaceDictionary( @RequestBody Map<String, Object> paramMap
											    , HttpServletRequest request
											    , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user ) {
		
        if (request.getRemoteAddr().equals("0:0:0:0:0:0:0:1")) {
        	user.setUserIp("127.0.0.1");
        } else {
        	user.setUserIp(request.getRemoteAddr());
        }
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		ModelVo modelVo = new ModelVo();
		modelVo.setProjectId(Integer.parseInt(paramMap.get("projectId").toString()));
		modelVo.setTaskId(Integer.parseInt(paramMap.get("taskId").toString()));
		modelVo.setTaskType(paramMap.get("taskType").toString());
		modelVo.setRole(user.getAuthorities().toString());
		modelVo.setCollectionOwner(user.getUsername());
		
		if (modelVo.getTaskType().equals(TextMinerConstants.TASK_TYPE_KEYWORD_EXTRACTION)
				 || modelVo.getTaskType().equals(TextMinerConstants.TASK_TYPE_RELATED_EXTRACTION)) {
					resultMap = modelService.replaceDictionary(modelVo, user);
				} else if (modelVo.getTaskType().equals(TextMinerConstants.TASK_TYPE_AUTO_QA)) {
					resultMap = modelService.replaceAutoQaData(modelVo, user);
				} else {
					resultMap = modelService.replacePattern(modelVo, user);
				}
		
		return resultMap; 
	}
	
	// 모델 분석요청
	@PostMapping("/analyzeModel")
	public Map<String, Object> analyzeModel( @RequestBody Map<String, Object> paramMap
										   , HttpServletRequest request
										   , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user ) {
		
        if (request.getRemoteAddr().equals("0:0:0:0:0:0:0:1")) {
        	user.setUserIp("127.0.0.1");
        } else {
        	user.setUserIp(request.getRemoteAddr());
        }
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		ModelVo modelVo = new ModelVo();
		modelVo.setProjectId(Integer.parseInt(paramMap.get("projectId").toString()));
		modelVo.setTaskId(Integer.parseInt(paramMap.get("taskId").toString()));
		modelVo.setTaskType(paramMap.get("taskType").toString());
		modelVo.setRole(user.getAuthorities().toString());
		modelVo.setCollectionOwner(user.getUsername());
		
		try {
			modelService.analyzeModel(modelVo, Integer.parseInt(paramMap.get("collectionId").toString()), user)
						.addCallback((Map<String, Object> result) -> {
							logger.info("tm-project model-analyze complete successfully. {}", result);
						}, (Throwable ex) -> {
							logger.info("tm-project model-analyze failed. {}", ex.toString());
						});
		} catch (NumberFormatException | InterruptedException e) {
			e.printStackTrace();
		}
		
		return resultMap; 
	}
}
