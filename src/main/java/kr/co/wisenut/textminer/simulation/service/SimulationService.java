package kr.co.wisenut.textminer.simulation.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import kr.co.wisenut.config.TMProperties;
import kr.co.wisenut.textminer.common.TextMinerConstants;
import kr.co.wisenut.textminer.deploy.mapper.DeployMapper;
import kr.co.wisenut.textminer.deploy.vo.DeployVo;
import kr.co.wisenut.textminer.project.vo.ProjectVo;
import kr.co.wisenut.textminer.simulation.mapper.SimulationMapper;
import kr.co.wisenut.textminer.task.mapper.TaskMapper;
import kr.co.wisenut.textminer.task.vo.TaskVo;

@Service
public class SimulationService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SimulationMapper simulationMapper;
	
	@Autowired
	private TaskMapper taskMapper;
	
	@Autowired
	private DeployMapper deployMapper;
	
	@Autowired
	private TMProperties tmProperties;
	
	// 시뮬레이션 리스트 조회
	public Map<String, Object> getSimulationList(Map<String, Object> paramMap) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();

		try {
			// 조회결과 리스트
			List<Map<String, Object>> resultList = simulationMapper.getSimulationList(paramMap);
			resultMap.put("dataTable", convertHtmlTagForSimulationList(resultList));
			
		} catch (NullPointerException e) {
			logger.error("조회 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("조회 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	// 테스트 모듈정보 조회
	public Map<String, Object> getTestModule(Map<String, Object> paramMap) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();

		try {
			
			// 서비스 정보 가져오기
			TaskVo taskVo = new TaskVo();
			taskVo.setProjectId(Integer.parseInt(paramMap.get("projectId").toString()));
			taskVo.setTaskId(Integer.parseInt(paramMap.get("taskId").toString()));
			taskVo = taskMapper.getTaskInfo(taskVo);
			
			// 테스트 모듈정보 가져오기
			DeployVo deployVo = new DeployVo();
			deployVo.setServerTask(taskVo.getTaskType());
			
			// 문자열 매칭은 PROD, 그 외에는 TEST
			if (taskVo.getTaskType().equals(TextMinerConstants.TASK_TYPE_STRING_MATCHER)){
				deployVo.setServerType(TextMinerConstants.SERVER_TYPE_PROD);
			} else {
				deployVo.setServerType(TextMinerConstants.SERVER_TYPE_TEST);
			}
			
			deployVo = deployMapper.getDeployDetail(deployVo);
			
			// 조회결과 설정
			if (taskVo.getCollectionId() > 0) {
				resultMap.put("collection", taskVo.getModelFile());
			} else {
				resultMap.put("collection", "");
			}
			
			resultMap.put("taskType", taskVo.getTaskType());
			resultMap.put("taskTypeName", taskVo.getLabel());
			resultMap.put("serverIp", deployVo.getServerIp());
			resultMap.put("serverPort", deployVo.getServerPort());
			
			if (taskVo.getTaskType().equals(TextMinerConstants.TASK_TYPE_AUTO_CLASSIFICATION)
			 || taskVo.getTaskType().equals(TextMinerConstants.TASK_TYPE_EMOTION_ANALYZE)) {
				resultMap.put("taskTypeFlag", "C");
			} else if (taskVo.getTaskType().equals(TextMinerConstants.TASK_TYPE_EMOTION_PREPROECESS)
					|| taskVo.getTaskType().equals(TextMinerConstants.TASK_TYPE_SUMMARY_PREPROECESS)) {
				resultMap.put("taskTypeFlag", "P");
			} else if (taskVo.getTaskType().equals(TextMinerConstants.TASK_TYPE_DOCUMENT_SUMMARY)) {
				resultMap.put("taskTypeFlag", "S");
			} else {
				resultMap.put("taskTypeFlag", "N");
			}
			
		} catch (NullPointerException e) {
			logger.error("조회 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("조회 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	// 시뮬레이션 호출
	public Map<String, Object> callSimulation(Map<String, Object> paramMap) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			// body 설정
			JsonObject body = new JsonObject();
			
			// 1) 필수입력값
			body.addProperty("simulationType", TextMinerConstants.EXECUTE_SIMULATION_TYPE_SIMUL);
			body.addProperty("collection", paramMap.get("collection").toString());
			body.addProperty("taskType", paramMap.get("taskType").toString());
			body.addProperty("serverIp", paramMap.get("hostIp").toString());
			body.addProperty("serverPort", paramMap.get("port").toString());
			body.addProperty("simulationText", paramMap.get("simulationText").toString());
			
			// 2) classifier Threadhold Option
			body.addProperty("thresholdOption", paramMap.get("thresholdOption")==null?null:paramMap.get("thresholdOption").toString());
			body.addProperty("predictThreshold", paramMap.get("predictThreshold")==null?"":paramMap.get("predictThreshold").toString());
			body.addProperty("thresholdMultiplier", paramMap.get("thresholdMultiplier")==null?"":paramMap.get("thresholdMultiplier").toString());
			
			// 3) 전처리 Option
			body.addProperty("apply_black_words_removal", paramMap.get("apply_black_words_removal")==null?null:paramMap.get("apply_black_words_removal").toString());
			body.addProperty("apply_pattern_removal", paramMap.get("apply_pattern_removal")==null?null:paramMap.get("apply_pattern_removal").toString());
			body.addProperty("apply_sentence_separation", paramMap.get("apply_sentence_separation")==null?null:paramMap.get("apply_sentence_separation").toString());
			body.addProperty("apply_spacing_correction", paramMap.get("apply_spacing_correction")==null?null:paramMap.get("apply_spacing_correction").toString());
			body.addProperty("apply_speaker_combination", paramMap.get("apply_speaker_combination")==null?null:paramMap.get("apply_speaker_combination").toString());
			body.addProperty("apply_split_sentence_word", paramMap.get("apply_split_sentence_word")==null?null:paramMap.get("apply_split_sentence_word").toString());

			// 4) 문서요약 - 분석 텍스트 전처리 적용여부
			body.addProperty("preprocessOption", paramMap.get("preprocessOption")==null?"N":paramMap.get("preprocessOption").toString());

			// Url 설정
			URI uri = UriComponentsBuilder.fromUriString("http://" + tmProperties.getCoreHost() + ":" + tmProperties.getCorePort() + "/api/simulation").build().toUri();

			// Header 설정
 			HttpHeaders headers = new HttpHeaders();
 			headers.setContentType(MediaType.APPLICATION_JSON);
 			HttpEntity<?> entity = new HttpEntity<>(body.toString(), headers);
			
 			// API Call (Connection, Read Time을 5초로 설정)
 			HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
			factory.setConnectTimeout(5 * 1000);
			factory.setReadTimeout(5 * 1000);	

			// Rest API 호출
 			RestTemplate restTemplate = new RestTemplate(factory);
 			ResponseEntity<String> responseEntity = restTemplate.postForEntity(uri, entity, String.class);
 			
 			JsonParser parser = new JsonParser();
 			JsonObject result = (JsonObject) parser.parse(responseEntity.getBody());
 			
 			resultMap.put("apiResultJson", result.get("apiResult").getAsString());
 			
		} catch (NullPointerException e) {
			logger.error("조회 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("조회 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	public String convertHtmlTagForSimulationList(List<Map<String, Object>> resultList) {
		StringBuffer convertHtml = new StringBuffer();
		int beforeProjectId = 0;
		
		for (int i = 0; i < resultList.size(); i++) {
			
			if (beforeProjectId != Integer.parseInt(resultList.get(i).get("projectId").toString())) {
				// 프로젝트 설명
				convertHtml.append("\t\t<td id=\"project_desc\">\n");
				convertHtml.append(resultList.get(i).get("projectDesc")+"\n");
				convertHtml.append("\t\t</td>\n");
				
				if (resultList.get(i).get("taskId") != null) {
					// 서비스 모델 리스트
					convertHtml.append("\t\t<td id=\"model_list\">\n");
					convertHtml.append("\t\t\t<select id=\"taskId\" class=\"w350\" title=\"서비스 모델 선택\" onChange=\"fnGetTestModule()\">\n");
					convertHtml.append("\t\t\t\t<option selected disabled hidden>서비스 모델 선택</option>\n");
					convertHtml.append("<option value=\""+Integer.parseInt(resultList.get(i).get("taskId").toString())+"\" title=\""
							+Integer.parseInt(resultList.get(i).get("projectId").toString())+" - "+Integer.parseInt(resultList.get(i).get("taskId").toString())
							+"\">"+resultList.get(i).get("modelName")+"</option>");
				} else {
					// 서비스 모델 리스트
					convertHtml.append("\t\t<td id=\"model_list\">\n");
					convertHtml.append("\t\t\t<select class=\"w350\" title=\"서비스 모델 선택\">\n");
					convertHtml.append("\t\t\t\t<option selected disabled hidden>서비스 모델 선택</option>\n");
					convertHtml.append("<option value=\"-1\" title=\"no_model\">생성 모델 없음</option>");
				}
				
				if (i == resultList.size()-1) {
					convertHtml.append("\t\t\t</select>\n");
					convertHtml.append("\t\t</td>\n");
				}
				
				beforeProjectId = Integer.parseInt(resultList.get(i).get("projectId").toString());
			} else {
				convertHtml.append("<option value=\""+Integer.parseInt(resultList.get(i).get("taskId").toString())+"\" title=\""
						+Integer.parseInt(resultList.get(i).get("projectId").toString())+" - "+Integer.parseInt(resultList.get(i).get("taskId").toString())
						+"\">"+resultList.get(i).get("modelName")+"</option>");
			}
			
			
			beforeProjectId = Integer.parseInt(resultList.get(i).get("projectId").toString());
		}
		return convertHtml.toString();
	}
	
	
}
