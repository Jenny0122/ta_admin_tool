package kr.co.wisenut.textminer.deploy.service;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import kr.co.wisenut.config.TMProperties;
import kr.co.wisenut.textminer.common.TextMinerConstants;
import kr.co.wisenut.textminer.deploy.mapper.DeployMapper;
import kr.co.wisenut.textminer.deploy.vo.DeployVo;

@Service
public class DeployService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TMProperties tmProperties;
	
	@Autowired
	private DeployMapper deployMapper;
	
	// API Call을 위한 RestTemplate 객체 선언
	URI uri;
	HttpHeaders headers;
	HttpEntity<?> entity;
	HttpComponentsClientHttpRequestFactory factory;
	RestTemplate restTemplate;
	ResponseEntity<String> responseEntity;
	
	// 배포 서버 정보 리스트 조회
	public Map<String, Object> getDeployList(DeployVo deployVo) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();

		try {
			// 조회결과 리스트
			List<DeployVo> resultList = deployMapper.getDeployList(deployVo);
			resultMap.put("dataTable", convertHtmlTagForDeployList(resultList));
			
		} catch (NullPointerException e) {
			logger.error("조회 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("조회 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	// 배포 서버 정보 상세 조회
	public Map<String, Object> getDeployDetail(DeployVo deployVo) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			// 상세조회
			resultMap.put("deploy", deployMapper.getDeployDetail(deployVo));
			
		} catch (NullPointerException e) {
			logger.error("조회 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("조회 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	
	// 배포 서버 정보 등록 
	public Map<String, Object> insertDeploy(DeployVo deployVo) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			
			int result = deployMapper.insertDeploy(deployVo);
			
			if (result > 0) {
				resultMap.put("serverId", deployVo.getServerId());
				resultMap.put("serverName", deployVo.getServerName());
				resultMap.put("result", "S");
				resultMap.put("resultMsg", "배포 서버 정보 등록 작업이 완료되었습니다.");
			} else {
				resultMap.put("serverName", deployVo.getServerName());
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "배포 서버 정보 등록 작업을 실패하였습니다.");
			}
			
		} catch (NullPointerException e) {
			logger.error("등록 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
			resultMap.put("serverName", deployVo.getServerName());
			resultMap.put("result", "F");
			resultMap.put("resultMsg", "배포 서버 정보 등록 작업을 실패하였습니다.");
		} catch (Exception e) {
			logger.error("등록 작업을 실패하였습니다.");
			e.printStackTrace();
			resultMap.put("serverName", deployVo.getServerName());
			resultMap.put("result", "F");
			resultMap.put("resultMsg", "배포 서버 정보 등록 작업을 실패하였습니다.");
		}
		
		return resultMap;
	}
	
	// 배포 서버 정보 수정
	public Map<String, Object> updateDeploy(DeployVo deployVo) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			
			int result = deployMapper.updateDeploy(deployVo);
			
			if (result > 0) {
				resultMap.put("result", "S");
				resultMap.put("resultMsg", "배포 서버 정보 수정 작업이 완료되었습니다.");
			} else {
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "배포 서버 정보 수정 권한이 없습니다.");
			}
			
		} catch (NullPointerException e) {
			logger.error("수정 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
			resultMap.put("result", "F");
			resultMap.put("resultMsg", "수정 시 누락된 값에 의한 오류발생!");
		} catch (Exception e) {
			logger.error("수정 작업을 실패하였습니다.");
			e.printStackTrace();
			resultMap.put("result", "F");
			resultMap.put("resultMsg", "수정 작업을 실패하였습니다.");
		}
		
		return resultMap;
	}
	
	// 배포 서버 정보 삭제
	public Map<String, Object> deleteDeploy(DeployVo deployVo) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			
			int result = deployMapper.deleteDeploy(deployVo);
			
			if (result > 0) {
				resultMap.put("result", "S");
				resultMap.put("resultMsg", "배포 서버 정보 삭제 작업이 완료되었습니다.");
			} else {
				resultMap.put("result", "F");
				resultMap.put("resultMsg", "배포 서버 정보 삭제 권한이 없습니다.");
			}
			
		} catch (NullPointerException e) {
			logger.error("삭제 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("삭제 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}

	// 상태체크
	public Map<String, Object> healthCheck(int serverId) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			// 조회결과 리스트
			DeployVo serverInfo = new DeployVo();
			serverInfo.setServerId(serverId);
			serverInfo = deployMapper.getDeployDetail(serverInfo);
			
			resultMap = requestHealthCheckModule(serverInfo.getServerIp(), serverInfo.getServerPort());

			// 자동분류, 감성분석은 학습상태를 확인하여 리턴할 수 있도록 한다.
			if (serverInfo.getServerTask().equals(TextMinerConstants.TASK_TYPE_AUTO_CLASSIFICATION) 
			 || serverInfo.getServerTask().equals(TextMinerConstants.TASK_TYPE_EMOTION_ANALYZE)) {
				resultMap = requestStatusCheckModule(serverInfo.getServerIp(), serverInfo.getServerPort());
			}
			
		} catch (NullPointerException e) {
			logger.error("모듈 상태체크 요청 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("모듈 상태체크 요청을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}

	// 모듈 상태체크
	public Map<String, Object> requestHealthCheckModule(String hostIp, int port) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
//			TmInterface를 통한 healthCheck
			uri = UriComponentsBuilder.fromUriString("http://" + hostIp + ":" + tmProperties.getCorePort() + "/api/health").build().toUri();
			
			// Header 설정
			headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			JsonObject body = new JsonObject();
			body.addProperty("serverIp", hostIp);
			body.addProperty("serverPort", port);
			
			entity = new HttpEntity<>(body.toString(), headers);
			
			factory = new HttpComponentsClientHttpRequestFactory();
			factory.setConnectTimeout(5 * 1000);
			factory.setReadTimeout(5 * 1000);	
			
			// API Call
			restTemplate = new RestTemplate(factory);
//			responseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);	
			responseEntity = restTemplate.postForEntity(uri, entity, String.class);	
			
			// 결과 Json Parsing
			JsonParser parser = new JsonParser();
			JsonObject apiResult = (JsonObject) parser.parse(responseEntity.getBody());
			
			logger.info("@@ healthCheckModule apiResult : " + apiResult);
			
			// result가 200이 아니면 실패 code return
			if(apiResult.get("result").getAsString().equals("200")) {
				resultMap.put("result", "S");
				resultMap.put("resultCss", "traf_green");
				resultMap.put("resultTxt", "작업 가능");
			} else {
				resultMap.put("result", "F");
				resultMap.put("resultCss", "traf_red");
				resultMap.put("resultTxt", "연결 오류");
			}
		} catch (ResourceAccessException e) {
			resultMap.put("result", "F");
			resultMap.put("resultCss", "traf_red");
			resultMap.put("resultTxt", "연결 오류");
		}
		
		return resultMap;
	}
	
	// 재부팅 상태체크
	public Map<String, Object> rebootStatusCheck(int serverId) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			DeployVo serverInfo = new DeployVo();
			
			if (serverId == 0) {
				serverInfo.setServerIp(tmProperties.getCoreHost());
				serverInfo.setServerPort(0);
			} else {
				// 조회결과 리스트
				serverInfo.setServerId(serverId);
				serverInfo = deployMapper.getDeployDetail(serverInfo);
			}
			
			resultMap = requestRebootStatusCheck(serverInfo.getServerIp(), serverInfo.getServerPort());
			
		} catch (NullPointerException e) {
			logger.error("재부팅 상태체크 요청 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("요청을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	// 모듈 재부팅
	public Map<String, Object> rebootServer(int serverId) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			if (serverId == 0) {
				rebootAllModule();
			} else {
				// 조회결과 리스트
				DeployVo serverInfo = new DeployVo();
				serverInfo.setServerId(serverId);
				serverInfo = deployMapper.getDeployDetail(serverInfo);
				
				requestRebootModule(serverInfo.getServerIp(), serverInfo.getServerPort());
			}
		} catch (NullPointerException e) {
			logger.error("재부팅 요청 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("재부팅 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	// 재기동 진행상태 체크
	public Map<String, Object> requestRebootStatusCheck(String hostIp, int port) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
//				TmInterface를 통한 Reboot Module
			uri = UriComponentsBuilder.fromUriString("http://" + hostIp + ":" + tmProperties.getCorePort() + "/api/rebootStatusCheck").build().toUri();
			
			// Header 설정
			headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			JsonObject body = new JsonObject();
			body.addProperty("serverIp", hostIp);
			body.addProperty("serverPort", port);
			
			entity = new HttpEntity<>(body.toString(), headers);
			
			factory = new HttpComponentsClientHttpRequestFactory();
			factory.setConnectTimeout(5 * 1000);
			factory.setReadTimeout(5 * 1000);		
			
			// API Call
			restTemplate = new RestTemplate(factory);
			responseEntity = restTemplate.postForEntity(uri, entity, String.class);
			
			// 결과 Json Parsing
			JsonParser parser = new JsonParser();
			JsonObject apiResult = (JsonObject) parser.parse(responseEntity.getBody());
			
			logger.info("@@ rebootStatusCheck apiResult : " + apiResult);
			
			resultMap.put("result", apiResult.get("result").getAsString());
			resultMap.put("message", apiResult.get("message").getAsString());
			
		} catch (ResourceAccessException e) {
			e.printStackTrace();
			resultMap.put("result", "500");
			resultMap.put("message", "rebootStatusCheck Error.");
		}
		
		return resultMap;
	}
	
	// 모든 모듈 재부팅
	public void rebootAllModule() {
		try {
//			TmInterface를 통한 Reboot Module
			uri = UriComponentsBuilder.fromUriString("http://" + tmProperties.getCoreHost() + ":" + tmProperties.getCorePort() + "/api/rebootAllModule").build().toUri();
			
			// Header 설정
			headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			entity = new HttpEntity<>(headers);
			
			factory = new HttpComponentsClientHttpRequestFactory();
			factory.setConnectTimeout(5 * 1000);
			factory.setReadTimeout(5 * 1000);		
			
			// API Call
			restTemplate = new RestTemplate(factory);
			responseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);	
			
			// 결과 Json Parsing
			JsonParser parser = new JsonParser();
			JsonObject apiResult = (JsonObject) parser.parse(responseEntity.getBody());
			
			logger.info("@@ rebootAllModule apiResult : " + apiResult);
		} catch (ResourceAccessException e) {
			e.printStackTrace();
		}
	}
	
	// 모듈 재부팅
	public void requestRebootModule(String hostIp, int port) {
		try {
//			TmInterface를 통한 Reboot Module
			uri = UriComponentsBuilder.fromUriString("http://" + hostIp + ":" + tmProperties.getCorePort() + "/api/rebootModule").build().toUri();
			
			// Header 설정
			headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			JsonObject body = new JsonObject();
			body.addProperty("serverIp", hostIp);
			body.addProperty("serverPort", port);
			
			entity = new HttpEntity<>(body.toString(), headers);
			
			factory = new HttpComponentsClientHttpRequestFactory();
			factory.setConnectTimeout(5 * 1000);
			factory.setReadTimeout(5 * 1000);		
			
			// API Call
			restTemplate = new RestTemplate(factory);
			responseEntity = restTemplate.postForEntity(uri, entity, String.class);
			
			// 결과 Json Parsing
			JsonParser parser = new JsonParser();
			JsonObject apiResult = (JsonObject) parser.parse(responseEntity.getBody());
			
			logger.info("@@ rebootModule apiResult : " + apiResult);
		} catch (ResourceAccessException e) {
			e.printStackTrace();
		}
	}
	
	// 모듈 배포
	public Map<String, Object> deployModel(int serverId) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			// 조회결과 리스트
			DeployVo serverInfo = new DeployVo();
			serverInfo.setServerId(serverId);
			serverInfo = deployMapper.getDeployDetail(serverInfo);
			
			if (serverInfo.getServerTask().equals(TextMinerConstants.TASK_TYPE_AUTO_CLASSIFICATION)
			 || serverInfo.getServerTask().equals(TextMinerConstants.TASK_TYPE_EMOTION_ANALYZE)) {
				resultMap = requestDeployModule(serverInfo.getServerIp(), serverInfo.getServerPort());
			} else if (serverInfo.getServerTask().equals(TextMinerConstants.TASK_TYPE_EMOTION_PREPROECESS)
					|| serverInfo.getServerTask().equals(TextMinerConstants.TASK_TYPE_SUMMARY_PREPROECESS)
					|| serverInfo.getServerTask().equals(TextMinerConstants.TASK_TYPE_STRING_MATCHER)) {
				resultMap = requestDeployPattern(serverInfo.getServerIp(), serverInfo.getServerPort(), serverInfo.getServerTask());
			} else if (serverInfo.getServerTask().equals(TextMinerConstants.TASK_TYPE_AUTO_QA)) {
				resultMap = requestDeployAutoQaData(serverInfo.getServerIp(), serverInfo.getServerPort());
			} else {
				resultMap = requestDeployDictionary(serverInfo.getServerIp(), serverInfo.getServerPort());
			}
		} catch (NullPointerException e) {
			logger.error("배포 요청 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("배포 작업을 실패하였습니다.");
			e.printStackTrace();
		}
		
		return resultMap;
	}
	
	// Classifier 배포
	public Map<String, Object> requestDeployModule(String hostIp, int port) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
//			TmInterface를 통한 Reboot Module
			uri = UriComponentsBuilder.fromUriString("http://" + hostIp + ":" + tmProperties.getCorePort() + "/api/deployModel").build().toUri();
			
			// Header 설정
			headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			JsonObject body = new JsonObject();
			body.addProperty("serverIp", hostIp);
			body.addProperty("serverPort", port);
			
			entity = new HttpEntity<>(body.toString(), headers);
			
			factory = new HttpComponentsClientHttpRequestFactory();
			factory.setConnectTimeout(30 * 1000);
			factory.setReadTimeout(30 * 1000);		
			
			// API Call
			restTemplate = new RestTemplate(factory);
			responseEntity = restTemplate.postForEntity(uri, entity, String.class);	
			
			// 결과 Json Parsing
			JsonParser parser = new JsonParser();
			JsonObject apiResult = (JsonObject) parser.parse(responseEntity.getBody());
			
			logger.info("@@ requestDeployModule apiResult : " + apiResult);
			
			if (apiResult.get("result").getAsString().equals("200")) {
				resultMap.put("result", "S");
				resultMap.put("resultMsg", apiResult.get("message").getAsString());
			} else {
				resultMap.put("result", "F");
				resultMap.put("resultMsg", apiResult.get("message").getAsString());
			}
		} catch (ResourceAccessException e) {
			e.printStackTrace();
			resultMap.put("result", "F");
			resultMap.put("resultMsg", "배포 요청을 실패하였습니다.");
		}
		
		return resultMap;
	}
	
	// 키워드, 연관어 추출 사전반영
	public Map<String, Object> requestDeployDictionary(String hostIp, int port) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			// TmInterface를 통한 반영 요청
			uri = UriComponentsBuilder
				 .fromUriString("http://" + hostIp + ":" + tmProperties.getCorePort() + "/api/replaceDictionaries")
				 .build()
				 .toUri();
			
			// Header & Body 설정
			headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			// parameter 설정
			JsonObject paramMap = new JsonObject();
			paramMap.addProperty("serverIp", hostIp);
			paramMap.addProperty("serverPort", port);
			paramMap.addProperty("projectId", "");
			
			// 수용어, 불용어 사전 적용결과 체크 변수
			int [] failFlag = {0, 0};
			String failDictionaryType = null;
			
			for (int i = 0; i < 2; i++) {
				paramMap.addProperty("dictionaryType", (i == 0 ? TextMinerConstants.DICTIONARY_TYPE_WHITE : TextMinerConstants.DICTIONARY_TYPE_BLACK));
				
				entity = new HttpEntity<>(paramMap.toString(), headers);
				
				// API Call
				factory = new HttpComponentsClientHttpRequestFactory();
				factory.setConnectTimeout(5 * 1000);
				
				restTemplate = new RestTemplate(factory);
				responseEntity = restTemplate.postForEntity(uri, entity, String.class);	
				
				JsonParser parser = new JsonParser();
	    		JsonObject result = (JsonObject) parser.parse(responseEntity.getBody());
	    		
	    		if (!result.get("result").getAsString().equals("200")) {
	    			failFlag[i]++;
    				failDictionaryType = (i == 0 ? TextMinerConstants.DICTIONARY_TYPE_NAME_WHITE : TextMinerConstants.DICTIONARY_TYPE_NAME_BLACK);
	    		}
			}
			
    		if (failFlag[0] + failFlag[1]  == 0) {
    			resultMap.put("result", "S");
    			resultMap.put("resultMsg", "배포 작업이 완료되었습니다.");
    		} else if (failFlag[0] + failFlag[1]  == 2) {
    			resultMap.put("result", "S");
    			resultMap.put("resultMsg", "배포 작업을 실패하었습니다.");
    		} else {
        		resultMap.put("result","F");
        		resultMap.put("resultMsg", failDictionaryType + " 사전 설정에 실패하였습니다.");
    		}
		} catch (ResourceAccessException e) {
			e.printStackTrace();
			resultMap.put("result", "N");
			resultMap.put("resultMsg", "배포 요청을 실패하였습니다.");
		}
		
		return resultMap;
	}
	
	// AUTO QA 지식 반영
	public Map<String, Object> requestDeployAutoQaData(String hostIp, int port) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			// TmInterface를 통한 반영 요청
			uri = UriComponentsBuilder
				 .fromUriString("http://" + hostIp + ":" + tmProperties.getCorePort() + "/api/replaceAutoQaData")
				 .build()
				 .toUri();
			
			// Header & Body 설정
			headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			// parameter 설정
			JsonObject paramMap = new JsonObject();
			paramMap.addProperty("serverIp", hostIp);
			paramMap.addProperty("serverPort", port);
			paramMap.addProperty("projectId", "");
			
			// AUTO QA 지식데이터
			paramMap.addProperty("dictionaryType", TextMinerConstants.DICTIONARY_TYPE_AUTO_QA_DATA);
			
			entity = new HttpEntity<>(paramMap.toString(), headers);
			
			// API Call
			factory = new HttpComponentsClientHttpRequestFactory();
			factory.setConnectTimeout(10000);
			
			restTemplate = new RestTemplate(factory);
			responseEntity = restTemplate.postForEntity(uri, entity, String.class);	
			
			JsonParser parser = new JsonParser();
    		JsonObject result = (JsonObject) parser.parse(responseEntity.getBody());
			
    		if (result.get("result").getAsString().equals("200")) {
    			resultMap.put("result", "S");
    			resultMap.put("resultMsg", "배포 작업이 완료되었습니다.");
    		} else {
    			resultMap.put("result", "F");
    			resultMap.put("resultMsg", "배포 작업에 실패하었습니다.");
    		}
			
		} catch (ResourceAccessException e) {
			e.printStackTrace();
			resultMap.put("result", "N");
			resultMap.put("resultMsg", "배포 요청을 실패하였습니다.");
		}
		
		return resultMap;
	}
	
	// 전처리 패턴 반영
	public Map<String, Object> requestDeployPattern(String hostIp, int port, String taskType) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			// TmInterface를 통한 반영 요청
			uri = UriComponentsBuilder
					.fromUriString("http://" + hostIp + ":" + tmProperties.getCorePort() + "/api/replacePattern")
					.build()
					.toUri();
			
			// Header & Body 설정
			headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			
			// parameter 설정
			JsonObject paramMap = new JsonObject();
			paramMap.addProperty("serverIp", hostIp);
			paramMap.addProperty("serverPort", port);
			paramMap.addProperty("projectId", "");
			
			// 불용패턴, 불용어절, 문장분리어절 적용결과 체크 변수
			int [] failFlag = {0, 0, 0};
			String failDictionaryType = null;
			
			if (taskType.equals(TextMinerConstants.TASK_TYPE_STRING_MATCHER)) {
				paramMap.addProperty("dictionaryType", TextMinerConstants.DICTIONARY_TYPE_GROUP_PATTERN);
				
				entity = new HttpEntity<>(paramMap.toString(), headers);
				
				// API Call
				factory = new HttpComponentsClientHttpRequestFactory();
				factory.setConnectTimeout(5 * 1000);
				
				restTemplate = new RestTemplate(factory);
				responseEntity = restTemplate.postForEntity(uri, entity, String.class);	
				
				JsonParser parser = new JsonParser();
				JsonObject result = (JsonObject) parser.parse(responseEntity.getBody());
				
	    		if (result.get("result").getAsString().equals("200")) {
	    			resultMap.put("result", "S");
	    			resultMap.put("resultMsg", "배포 작업이 완료되었습니다.");
	    		} else {
	    			resultMap.put("result", "F");
	    			resultMap.put("resultMsg", "배포 작업에 실패하었습니다.");
	    		} 
			} else {
				for (int i = 0; i < 3; i++) {
					switch (i) {
						case 0:
							paramMap.addProperty("dictionaryType", TextMinerConstants.DICTIONARY_TYPE_BLACK_PATTERN);
							break;
						case 1:
							paramMap.addProperty("dictionaryType", TextMinerConstants.DICTIONARY_TYPE_BLACK_WORD);
							break;
						case 2:
							paramMap.addProperty("dictionaryType", TextMinerConstants.DICTIONARY_TYPE_SPLIT_SENT_WORD);
							break;
					}
					
					entity = new HttpEntity<>(paramMap.toString(), headers);
					
					// API Call
					factory = new HttpComponentsClientHttpRequestFactory();
					factory.setConnectTimeout(5 * 1000);
					
					restTemplate = new RestTemplate(factory);
					responseEntity = restTemplate.postForEntity(uri, entity, String.class);	
					
					JsonParser parser = new JsonParser();
					JsonObject result = (JsonObject) parser.parse(responseEntity.getBody());
					
					if (!result.get("result").getAsString().equals("200")) {
						failFlag[i]++;
					}
				}
				
	    		if (failFlag[0] + failFlag[1] + failFlag[2] == 0) {
	    			resultMap.put("result", "S");
	    			resultMap.put("resultMsg", "배포 작업이 완료되었습니다.");
	    		} else {
	    			resultMap.put("result", "F");
	    			resultMap.put("resultMsg", "배포 작업에 실패하었습니다.");
	    		} 
			}
		} catch (ResourceAccessException e) {
			e.printStackTrace();
			resultMap.put("result", "N");
			resultMap.put("resultMsg", "배포 요청을 실패하였습니다.");
		}
		
		return resultMap;
	}
	
	public String convertHtmlTagForDeployList(List<DeployVo> resultList) {
		StringBuffer convertHtml = new StringBuffer();
		Map<String, Object> statusMap = null;
		
		// UI 처리 변수
		String ip = null;
		String service = null;
		
		if (resultList == null || resultList.isEmpty()) {
			convertHtml.append("\t<tr>\n");
			convertHtml.append("\t\t<td colspan=\"8\">등록된 배포 서버 정보가 없습니다.</td>");
			convertHtml.append("\t</tr>\n");
		} else {
			for (int i = 0; i < resultList.size(); i++) {
				DeployVo server = resultList.get(i);
				
				convertHtml.append("\t<tr class=\"collection\">\n");
				
				// IP 주소 
				if (i == 0) {
					ip = server.getServerIp();
					
					convertHtml.append("\t\t<td rowspan=\"" + server.getServerCnt() + "\">\n");
					convertHtml.append(server.getServerIp() + "\n");
					convertHtml.append("\t\t</td>\n");
				} else if (!server.getServerIp().equals(ip)) {
					ip = server.getServerIp();
					
					convertHtml.append("\t\t<td rowspan=\"" + server.getServerCnt() + "\">\n");
					convertHtml.append(server.getServerIp() + "\n");
					convertHtml.append("\t\t</td>\n");
				}
				
				// 서비스명 
				if (i == 0) {
					service = server.getLabel();
					
					convertHtml.append("\t\t<td rowspan=\"" + server.getTaskCnt() + "\">\n");
					convertHtml.append(server.getLabel() + "\n");
					convertHtml.append("\t\t</td>\n");
				} else if (!server.getLabel().equals(service)) {
					service = server.getLabel();
					
					convertHtml.append("\t\t<td rowspan=\"" + server.getTaskCnt() + "\">\n");
					convertHtml.append(server.getLabel() + "\n");
					convertHtml.append("\t\t</td>\n");
				}
				
				// 서비스 포트 
				convertHtml.append("\t\t<td>\n");
				convertHtml.append(server.getServerPort() + "\n");
				convertHtml.append("\t\t</td>\n");
				
				// 서버구분 
				convertHtml.append("\t\t<td>\n");
				convertHtml.append(server.getServerType() + "\n");
				convertHtml.append("\t\t</td>\n");
				
				// 서버 이름 
				convertHtml.append("\t\t<td>\n");
				convertHtml.append("<a href=\"javascript:showUpdatePopup('" + server.getServerId() + "');\" title=\"클릭 시 수정 가능\">" + server.getServerName() + "\n");
				convertHtml.append("\t\t</td>\n");
				
				// 설명
				convertHtml.append("\t\t<td>\n");
				convertHtml.append(server.getServerDesc() + "\n");
				convertHtml.append("\t\t</td>\n");
				
				// 상태 (health check 확인)
				statusMap = requestHealthCheckModule(server.getServerIp(), server.getServerPort());
				
				// 자동분류, 감성분석은 학습상태를 확인하여 리턴할 수 있도록 한다.
				if (server.getServerTask().equals(TextMinerConstants.TASK_TYPE_AUTO_CLASSIFICATION) 
				 || server.getServerTask().equals(TextMinerConstants.TASK_TYPE_EMOTION_ANALYZE)) {
					statusMap = requestStatusCheckModule(server.getServerIp(), server.getServerPort());
				}
				
				convertHtml.append("\t\t<td>\n");
				convertHtml.append("\t\t<button type=\"button\" id=\"statusBtn_" + server.getServerId() + "\" class=\"radiusBtn w120\" style=\"cursor:default;\" disabled>\n");
				
				convertHtml.append("\t\t\t<div class=\"" + statusMap.get("resultCss") + " mr5\"></div>");
				convertHtml.append(statusMap.get("resultTxt") + "\n");
				convertHtml.append("\t\t</button>\n");
				convertHtml.append("\t\t</td>\n");
				
				// 작업 
				convertHtml.append("\t\t<td class=\"module_job\">\n");
				
				// 서버 구분 - TEST, 문서요약 서비스는 배포 버튼을 표시하지 않는다.
				convertHtml.append("<button type=\"button\" class=\"btn test_btn btn_gray w98\" onclick=\"healthCheck(" + server.getServerId() + ")\"><i class=\"fas fa-link mr5\"></i>테스트</button>\n");
				convertHtml.append("<button type=\"button\" class=\"btn restart_btn btn_gray w98\" onclick=\"rebootServer(" + server.getServerId() + ", '" + server.getServerName() + "')\"><i class=\"fas fa-power-off mr5\"></i>재시작</button>\n");
				convertHtml.append("<button type=\"button\" class=\"btn delete_btn btn_gray w98\" onclick=\"deleteDeploy(" + server.getServerId() + ");\"><i class=\"fas fa-trash-alt mr5\"></i>삭제</button>\n");
				convertHtml.append("\t\t</td>\n");

				// 베포
				convertHtml.append("\t\t<td>\n");
				if (server.getServerType().equals(TextMinerConstants.SERVER_TYPE_PROD) 
				 && !server.getServerTask().equals(TextMinerConstants.TASK_TYPE_DOCUMENT_SUMMARY)
				 && server.getUseTaskCnt() > 0) {
					convertHtml.append("<button type=\"button\" class=\"btn btn_orange w96\" onclick=\"deployModel(" + server.getServerId() + ")\"><i class=\"fas fa-upload mr5\"></i>배포</button>\n");
				}
				convertHtml.append("\t\t</td>\n");
				
//				if ( server.getServerType().equals(TextMinerConstants.SERVER_TYPE_PROD) 
//						&& !server.getServerTask().equals(TextMinerConstants.TASK_TYPE_DOCUMENT_SUMMARY)) {
//					convertHtml.append("<button type=\"button\" class=\"btn test_btn btn_gray w98\" onclick=\"healthCheck(" + server.getServerId() + ")\"><i class=\"fas fa-link mr5\"></i>테스트</button>\n");
//					convertHtml.append("<button type=\"button\" class=\"btn restart_btn btn_gray w98\" onclick=\"rebootServer(" + server.getServerId() + ", '" + server.getServerName() + "')\"><i class=\"fas fa-power-off mr5\"></i>재시작</button>\n");
//					convertHtml.append("<button type=\"button\" class=\"btn deploy_btn btn_gray w96\" onclick=\"deployModel(" + server.getServerId() + ")\"><i class=\"fas fa-upload mr5\"></i>배포</button>\n");
//					convertHtml.append("<button type=\"button\" class=\"btn delete_btn btn_gray w98\" onclick=\"deleteDeploy(" + server.getServerId() + ");\"><i class=\"fas fa-trash-alt mr5\"></i>삭제</button>\n");
//				} else {
//					convertHtml.append("<button type=\"button\" class=\"btn test_btn btn_gray w130\" onclick=\"healthCheck(" + server.getServerId() + ")\"><i class=\"fas fa-link mr5\"></i>테스트</button>\n");
//					convertHtml.append("<button type=\"button\" class=\"btn restart_btn btn_gray w130\" onclick=\"rebootServer(" + server.getServerId() + ", '" + server.getServerName() + "')\"><i class=\"fas fa-power-off mr5\"></i>재시작</button>\n");
//					convertHtml.append("<button type=\"button\" class=\"btn delete_btn btn_gray w130\" onclick=\"deleteDeploy(" + server.getServerId() + ");\"><i class=\"fas fa-trash-alt mr5\"></i>삭제</button>\n");
//				}
				
				
				convertHtml.append("\t</tr>\n");
			}
		}
		
		return convertHtml.toString();
	}

	// 자동분류, 감성분석의 학습 및 분석여부 체크
	public Map<String, Object> requestStatusCheckModule(String hostIp, int port) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		String [] jobDiv = {TextMinerConstants.COLLECTION_JOB_TRAINING, TextMinerConstants.COLLECTION_JOB_ANALYSIS}; 
		String [] jobDivName = {TextMinerConstants.COLLECTION_JOB_NAME_TRAINING, TextMinerConstants.COLLECTION_JOB_NAME_ANALYSIS}; 
		
		try {
//			TmInterface를 통한 status Check
			uri = UriComponentsBuilder.fromUriString("http://" + hostIp + ":" + tmProperties.getCorePort() + "/api/status").build().toUri();
			
			// Header 설정
			headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			JsonObject body = new JsonObject();
			body.addProperty("serverIp", hostIp);
			body.addProperty("serverPort", port);
			
			// 학습상태, 분석상태 조회
			for (int i = 0; i < jobDiv.length; i++) {
				body.addProperty("jobDiv", jobDiv[i]);
				
				entity = new HttpEntity<>(body.toString(), headers);
				
				factory = new HttpComponentsClientHttpRequestFactory();
				factory.setConnectTimeout(5 * 1000);
				factory.setReadTimeout(5 * 1000);	
				
				// API Call
				restTemplate = new RestTemplate(factory);
				responseEntity = restTemplate.postForEntity(uri, entity, String.class);	
				
				// 결과 Json Parsing
				JsonParser parser = new JsonParser();
				JsonObject apiResult = (JsonObject) parser.parse(responseEntity.getBody());
				
				logger.info("@@ statusCheckModule apiResult : " + apiResult);
				
				// Status가 UP이 아니면 실패 code return
				if(apiResult.get("result").getAsString().equals("200")) {
					resultMap.put("result", "S");
					
					if (apiResult.get("resultContent").getAsString().equals("READY")) {
						resultMap.put("resultCss", "traf_green");
						resultMap.put("resultTxt", "작업 가능");
					} else {
						resultMap.put("resultCss", "traf_yellow");
						resultMap.put("resultTxt", jobDivName[i] + " 작업중");
						break;
					}
				} else {
					resultMap.put("result", "F");
					resultMap.put("resultCss", "traf_red");
					resultMap.put("resultTxt", "연결 오류");
					break;
				}
			}
		} catch (ResourceAccessException e) {
			resultMap.put("result", "F");
			resultMap.put("resultCss", "traf_red");
			resultMap.put("resultTxt", "연결 오류");
		}
		
		return resultMap;
	}
}
