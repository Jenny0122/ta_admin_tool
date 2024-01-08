package kr.co.wisenut.textminer.model.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.codecs.configuration.CodecConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import kr.co.wisenut.config.ClassifierProperties;
import kr.co.wisenut.config.TMProperties;
import kr.co.wisenut.textminer.collection.mapper.CollectionMapper;
import kr.co.wisenut.textminer.collection.mapper.DocumentMapper;
import kr.co.wisenut.textminer.collection.vo.CollectionVo;
import kr.co.wisenut.textminer.common.TextMinerConstants;
import kr.co.wisenut.textminer.dictionary.mapper.EntryMapper;
import kr.co.wisenut.textminer.dictionary.vo.EntryVo;
import kr.co.wisenut.textminer.history.mapper.ActionHistoryMapper;
import kr.co.wisenut.textminer.history.vo.ActionHistoryVo;
import kr.co.wisenut.textminer.model.vo.ModelVo;
import kr.co.wisenut.textminer.task.mapper.TaskMapper;
import kr.co.wisenut.textminer.task.vo.TaskVo;
import kr.co.wisenut.textminer.user.vo.TmUser;

@Service
public class ModelService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	TMProperties tmProperties;

	@Autowired
	ClassifierProperties classifierProperties;
	
	@Autowired
	private TaskMapper taskMapper;
	
	@Autowired
	private EntryMapper entryMapper;

	@Autowired
	private CollectionMapper collectionMapper;
	
	@Autowired
	private DocumentMapper documentMapper;
	
	@Autowired
	private ActionHistoryMapper actionHistoryMapper;
	
	// API Call을 위한 RestTemplate 객체 선언
	URI uri;
	HttpComponentsClientHttpRequestFactory factory;
	RestTemplate restTemplate;
	ResponseEntity<String> responseEntity;
	HttpHeaders headers;
	HttpEntity<?> entity;
	
	public Map<String, Object> chkStatus(ModelVo modelVo) {

		Map<String, Object> resultMap = new HashMap<String, Object>();

		// 모델을 통한 task 및 Collection 가져오기
		TaskVo taskVo = taskMapper.getTaskInfoUseModel(modelVo);
//		CollectionVo collectionVo = collectionMapper.getCollectionDetailUseModel(modelVo);
		
		// Classifier 모듈 상태 체크
		try {
			// 자동분류, 감성분석
			if (modelVo.getTaskType().equals(TextMinerConstants.TASK_TYPE_AUTO_CLASSIFICATION)
			 || modelVo.getTaskType().equals(TextMinerConstants.TASK_TYPE_EMOTION_ANALYZE)) {
				
				// 학습, 분석상태 체크한 후에 진행될 수 있도록한다.
				for (int i = 0; i < 2; i++) {
					
					// api URI 설정
					uri = UriComponentsBuilder
						 .fromUriString("http://" + tmProperties.getCoreHost() + ":" + tmProperties.getCorePort() + "/api/status")
						 .build()
						 .toUri();
					
					// Parameter Setting
					JsonObject paramMap = new JsonObject();
					paramMap.addProperty("jobDiv", i==0?TextMinerConstants.COLLECTION_JOB_TRAINING:TextMinerConstants.COLLECTION_JOB_ANALYSIS);
					
					// 분석(Analysis)은 생성된 모델이 있는 경우에 요청가능
					if (modelVo.getJobDiv().equals(TextMinerConstants.COLLECTION_JOB_ANALYSIS)) {
						if (taskVo.getModelFile() == null) {
							resultMap.put("result", "N");
							resultMap.put("resultMsg", "생성된 모델이 없어 분석을 요청할 수 없습니다.");
							break;
						} else {
							// 분석 IP & Port 설정
							if (modelVo.getTaskType().equals(TextMinerConstants.TASK_TYPE_AUTO_CLASSIFICATION)) {
								paramMap.addProperty("serverIp", classifierProperties.getAutoListenerHost());
								paramMap.addProperty("serverPort", classifierProperties.getAutoListenerPort());
							} else {
								paramMap.addProperty("serverIp", classifierProperties.getEmotionListenerHost());
								paramMap.addProperty("serverPort", classifierProperties.getEmotionListenerPort());
							}
						}
					} else {
						// 학습 IP & Port 설정
						if (modelVo.getTaskType().equals(TextMinerConstants.TASK_TYPE_AUTO_CLASSIFICATION)) {
							paramMap.addProperty("serverIp", classifierProperties.getAutoListenerHost());
							paramMap.addProperty("serverPort", classifierProperties.getAutoListenerPort());
						} else {
							paramMap.addProperty("serverIp", classifierProperties.getEmotionListenerHost());
							paramMap.addProperty("serverPort", classifierProperties.getEmotionListenerPort());
						}
					}
					
					// Header 설정
					headers = new HttpHeaders();
					headers.setContentType(MediaType.APPLICATION_JSON);
					entity = new HttpEntity<>(paramMap.toString(), headers);
					
					// API Call
					factory = new HttpComponentsClientHttpRequestFactory();
					factory.setConnectTimeout(10000);
					factory.setReadTimeout(10000);			
					
					restTemplate = new RestTemplate(factory);
					responseEntity = restTemplate.postForEntity(uri, entity, String.class);	
					
					// 결과 Json Parsing
					JsonParser parser = new JsonParser();
					JsonObject result = (JsonObject) parser.parse(responseEntity.getBody());
	
					logger.info("@@ result : {}", result);
					
					if(result.get("resultContent").getAsString().equals("READY")) {
						resultMap.put("result", "Y");
						resultMap.put("resultMsg", "수행가능");
					} else {
						resultMap.put("result", "N");
						resultMap.put("resultMsg", "현재 진행 중인 작업이 있어 요청을 수행할 수 없습니다.");
						break;
					}
				}
				
			} else {
				// 키워드추출, 연관어추출, 문서요약
				// api URI 설정
				uri = UriComponentsBuilder
					 .fromUriString("http://" + tmProperties.getCoreHost() + ":" + tmProperties.getCorePort() + "/api/health")
					 .build()
					 .toUri();
				
				// parameter 설정
				JsonObject paramMap = new JsonObject();
				
				// 각 서비스별 IP & Port 설정
				switch(modelVo.getTaskType()) {
					case TextMinerConstants.TASK_TYPE_DOCUMENT_SUMMARY :
						paramMap.addProperty("serverIp", classifierProperties.getSummaryHost());
						paramMap.addProperty("serverPort", classifierProperties.getSummaryPort());
						break;
					case TextMinerConstants.TASK_TYPE_KEYWORD_EXTRACTION :
						paramMap.addProperty("serverIp", classifierProperties.getKeywordHost());
						paramMap.addProperty("serverPort", classifierProperties.getKeywordPort());
						break;
					case TextMinerConstants.TASK_TYPE_RELATED_EXTRACTION :
						paramMap.addProperty("serverIp", classifierProperties.getRelatedHost());
						paramMap.addProperty("serverPort", classifierProperties.getRelatedPort());
						break;
					case TextMinerConstants.TASK_TYPE_EMOTION_PREPROECESS :
						paramMap.addProperty("serverIp", classifierProperties.getEmotionPreprocessHost());
						paramMap.addProperty("serverPort", classifierProperties.getEmotionPreprocessPort());
						break;
					case TextMinerConstants.TASK_TYPE_SUMMARY_PREPROECESS :
						paramMap.addProperty("serverIp", classifierProperties.getSummaryPreprocessHost());
						paramMap.addProperty("serverPort", classifierProperties.getSummaryPreprocessPort());
						break;
					case TextMinerConstants.TASK_TYPE_STRING_MATCHER :
						paramMap.addProperty("serverIp", classifierProperties.getStringMatcherHost());
						paramMap.addProperty("serverPort", classifierProperties.getStringMatcherPort());
						break;
				}		
				
				// Header 설정
				headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				headers.set("x-token", "wisenut");
				entity = new HttpEntity<>(paramMap.toString(), headers);
				
				// API Call
				factory = new HttpComponentsClientHttpRequestFactory();
				factory.setConnectTimeout(10000);
				
				restTemplate = new RestTemplate(factory);
				responseEntity = restTemplate.postForEntity(uri, entity, String.class);	
				
				// 결과 Json Parsing
				JsonParser parser = new JsonParser();
				JsonObject result = (JsonObject) parser.parse(responseEntity.getBody());
				
				logger.info("@@ result : {}", result);
				
				if(result.get("result").getAsString().equals("200")) {
					resultMap.put("result", "Y");
					resultMap.put("resultMsg", "수행가능");
				}
			}
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			// 통신은 되었지만 404 에러가 떴을 경우의 예외처리
			logger.warn("@@ tm-server connection success, but failed to handle request. {} {}: {}", e.getStatusCode(), e.getStatusText(), e.getMessage());

			resultMap.put("result", "N");
			resultMap.put("resultMsg", "오류가 발생하였습니다.\n관리자에게 문의해주세요.");
		} catch (ResourceAccessException e) {
			logger.error("Failed to connect. {}.", e.getMessage());

			resultMap.put("result", "N");
			resultMap.put("resultMsg", "오류가 발생하였습니다.\n관리자에게 문의해주세요.");
		}
		
		return resultMap;
	}
	
	// 모델 생성
	@Async("threadPoolTaskExecutor")
	public ListenableFuture<Map<String, Object>> createModel(ModelVo modelVo, TmUser user) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		// 모델을 통한 task 및 Collection 가져오기
		TaskVo taskVo = taskMapper.getTaskInfoUseModel(modelVo);
		CollectionVo collectionVo = collectionMapper.getCollectionDetailUseModel(modelVo);
		
		try {
			if (collectionVo != null) {
				// api URI 설정
				uri = UriComponentsBuilder
					 .fromUriString("http://" + tmProperties.getCoreHost() + ":" + tmProperties.getCorePort() + "/api/train")
					 .build()
					 .toUri();
				
				// Header & Body 설정
				// parameter 설정
				JsonObject paramMap = new JsonObject();
				
				// UserId
				paramMap.addProperty("userId", modelVo.getCollectionOwner());
				
				// IP & Port
				if (modelVo.getTaskType().equals(TextMinerConstants.TASK_TYPE_AUTO_CLASSIFICATION)) {
					paramMap.addProperty("serverIp", classifierProperties.getAutoListenerHost());
					paramMap.addProperty("serverPort", classifierProperties.getAutoListenerPort());
				} else {
					paramMap.addProperty("serverIp", classifierProperties.getEmotionListenerHost());
					paramMap.addProperty("serverPort", classifierProperties.getEmotionListenerPort());
				}
				
				// 프로젝트, 서비스, 컬렉션 ID
				paramMap.addProperty("projectId", taskVo.getProjectId());
				paramMap.addProperty("taskId", taskVo.getTaskId());
				paramMap.addProperty("collectionId", collectionVo.getCollectionId());
				
				headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				entity = new HttpEntity<>(paramMap.toString(), headers);
				
				// API Call
				factory = new HttpComponentsClientHttpRequestFactory();
				factory.setConnectTimeout(10000);
				
				restTemplate = new RestTemplate(factory);
				responseEntity = restTemplate.postForEntity(uri, entity, String.class);	
				
				logger.info("@@ {} train result {}", modelVo.getModelName(), responseEntity.getBody());
				
				// 학습 요청 후 이력 저장
				ActionHistoryVo actionHistoryVo = new ActionHistoryVo();
	
		        actionHistoryVo.setActionUser(user.getUsername());
		        actionHistoryVo.setResourceId("0");
		        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
		        actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_TRAIN_MODEL);
		        actionHistoryVo.setActionMsg(user.getUsername() + " 학습요청 ( " + taskVo.getTaskName() + "_" + collectionVo.getCollectionId() + " )");
	    		actionHistoryVo.setUserIp(user.getUserIp());
		        
		        actionHistoryMapper.insertActionHistory(actionHistoryVo);
			}
		} catch (ResourceAccessException e) {
			logger.error("Failed to connect. {}.", e.getMessage());
		}
		
		return new AsyncResult<Map<String, Object>>(resultMap);
	}
	
	// 사전변경
	public Map<String, Object> replaceDictionary(ModelVo modelVo, TmUser user) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		// task 정보 가져오기
		TaskVo taskVo = taskMapper.getTaskInfoUseModel(modelVo);
		
    	JsonParser parser = new JsonParser();
    	
    	try {
    		// api URI 설정
			uri = UriComponentsBuilder
				 .fromUriString("http://" + tmProperties.getCoreHost() + ":" + tmProperties.getCorePort() + "/api/replaceDictionaries")
				 .build()
				 .toUri();
			
			// Header & Body 설정
			headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			// parameter 설정
			JsonObject paramMap = new JsonObject();
			paramMap.addProperty("dictionaryType", TextMinerConstants.DICTIONARY_TYPE_WHITE);
			paramMap.addProperty("projectId", taskVo.getProjectId());
			
			// IP & Port
			switch(modelVo.getTaskType()) {
				case TextMinerConstants.TASK_TYPE_KEYWORD_EXTRACTION:
					paramMap.addProperty("serverIp", classifierProperties.getKeywordHost());
					paramMap.addProperty("serverPort", classifierProperties.getKeywordPort());
					break;
				case TextMinerConstants.TASK_TYPE_RELATED_EXTRACTION:
					paramMap.addProperty("serverIp", classifierProperties.getRelatedHost());
					paramMap.addProperty("serverPort", classifierProperties.getRelatedPort());
					break;
			}
			
			// 수용어, 불용어 사전 적용결과 체크 변수
			int [] failFlag = {0, 0};
			String failDictionaryType = null;
			
			for (int i = 0; i < 2; i++) {
				paramMap.addProperty("dictionaryType", (i == 0 ? TextMinerConstants.DICTIONARY_TYPE_WHITE : TextMinerConstants.DICTIONARY_TYPE_BLACK));
				
				entity = new HttpEntity<>(paramMap.toString(), headers);
				
				// API Call
				factory = new HttpComponentsClientHttpRequestFactory();
				factory.setConnectTimeout(10000);
				
				restTemplate = new RestTemplate(factory);
				responseEntity = restTemplate.postForEntity(uri, entity, String.class);	
	    		JsonObject result = (JsonObject) parser.parse(responseEntity.getBody());
	    		
	    		if (!result.get("result").getAsString().equals("200")) {
	    			failFlag[i]++;
    				failDictionaryType = (i == 0 ? TextMinerConstants.DICTIONARY_TYPE_NAME_WHITE : TextMinerConstants.DICTIONARY_TYPE_NAME_BLACK);
	    		}
			}
			
    		if (failFlag[0] + failFlag[1]  == 0) {
    			resultMap.put("result", "S");
    			resultMap.put("resultMsg", "사전 설정이 완료되었습니다.");
    		} else if (failFlag[0] + failFlag[1]  == 2) {
    			resultMap.put("result", "F");
    			resultMap.put("resultMsg", "사전 설정에 실패하었습니다.");
    		} else {
        		resultMap.put("result","S");
        		resultMap.put("resultMsg", failDictionaryType + " 사전 설정에 실패하였습니다.");
    		}
    		
    		// 사전 요청 후 이력 저장
    		ActionHistoryVo actionHistoryVo = new ActionHistoryVo();
    		
    		actionHistoryVo.setActionUser(user.getUsername());
    		actionHistoryVo.setResourceId("0");
    		actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
    		actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_TRAIN_MODEL);
    		actionHistoryVo.setActionMsg(user.getUsername() + " 사전 반영 ( " + taskVo.getModelFile() + " )");
    		actionHistoryVo.setUserIp(user.getUserIp());
    		
    		actionHistoryMapper.insertActionHistory(actionHistoryVo);
    	} catch (Exception e) {
    		e.printStackTrace();
    		resultMap.put("result","F");
    		resultMap.put("resultMsg", "사전 설정에 실패하였습니다.");
    	} finally {
    		return resultMap;
    	}
	}
	
	// 패턴변경
	public Map<String, Object> replacePattern(ModelVo modelVo, TmUser user) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		// task 정보 가져오기
		TaskVo taskVo = taskMapper.getTaskInfoUseModel(modelVo);
    	
		JsonParser parser = new JsonParser();
    	
    	try {
    		// api URI 설정
			uri = UriComponentsBuilder
				 .fromUriString("http://" + tmProperties.getCoreHost() + ":" + tmProperties.getCorePort() + "/api/replacePattern")
				 .build()
				 .toUri();
			
			// Header & Body 설정
			headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			// parameter 설정
			JsonObject paramMap = new JsonObject();
			paramMap.addProperty("projectId", taskVo.getProjectId());
			
			// IP & Port
			switch(modelVo.getTaskType()) {
				case TextMinerConstants.TASK_TYPE_EMOTION_PREPROECESS:
					paramMap.addProperty("serverIp", classifierProperties.getEmotionPreprocessHost());
					paramMap.addProperty("serverPort", classifierProperties.getEmotionPreprocessPort());
					break;
				case TextMinerConstants.TASK_TYPE_SUMMARY_PREPROECESS:
					paramMap.addProperty("serverIp", classifierProperties.getSummaryPreprocessHost());
					paramMap.addProperty("serverPort", classifierProperties.getSummaryPreprocessPort());
					break;
				case TextMinerConstants.TASK_TYPE_STRING_MATCHER:
					paramMap.addProperty("serverIp", classifierProperties.getStringMatcherHost());
					paramMap.addProperty("serverPort", classifierProperties.getStringMatcherPort());
					break;
			}
			
			if (modelVo.getTaskType().equals(TextMinerConstants.TASK_TYPE_STRING_MATCHER)) {
				// 매칭패턴
				paramMap.addProperty("dictionaryType", TextMinerConstants.DICTIONARY_TYPE_GROUP_PATTERN);
				
				entity = new HttpEntity<>(paramMap.toString(), headers);
				
				// API Call
				factory = new HttpComponentsClientHttpRequestFactory();
				factory.setConnectTimeout(10000);
				
				restTemplate = new RestTemplate(factory);
				responseEntity = restTemplate.postForEntity(uri, entity, String.class);	
	    		JsonObject result = (JsonObject) parser.parse(responseEntity.getBody());
				
	    		if (result.get("result").getAsString().equals("200")) {
	    			resultMap.put("result", "S");
	    			resultMap.put("resultMsg", "패턴 설정이 완료되었습니다.");
	    		} else {
	    			resultMap.put("result", "F");
	    			resultMap.put("resultMsg", "패턴 설정에 실패하었습니다.");
	    		}
	    		
			} else {
				// 불용 패턴, 불용 어절, 문장분리 어절 적용결과 체크 변수
				int [] failFlag = {0, 0, 0};
				
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
					factory.setConnectTimeout(10000);
					
					restTemplate = new RestTemplate(factory);
					responseEntity = restTemplate.postForEntity(uri, entity, String.class);	
		    		JsonObject result = (JsonObject) parser.parse(responseEntity.getBody());
		    		
		    		if (!result.get("result").getAsString().equals("200")) {
		    			failFlag[i]++;
		    		}
				}
				
	    		if (failFlag[0] + failFlag[1] + failFlag[2] == 0) {
	    			resultMap.put("result", "S");
	    			resultMap.put("resultMsg", "패턴 설정이 완료되었습니다.");
	    		} else {
	    			resultMap.put("result", "F");
	    			resultMap.put("resultMsg", "패턴 설정에 실패하었습니다.");
	    		} 
			}
    		// 사전 요청 후 이력 저장
			ActionHistoryVo actionHistoryVo = new ActionHistoryVo();

	        actionHistoryVo.setActionUser(user.getUsername());
	        actionHistoryVo.setResourceId("0");
	        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
	        actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_TRAIN_MODEL);
	        actionHistoryVo.setActionMsg(user.getUsername() + " 패턴 반영 ( " + taskVo.getModelFile() + " )");
    		actionHistoryVo.setUserIp(user.getUserIp());
	        
	        actionHistoryMapper.insertActionHistory(actionHistoryVo);
    	} catch (Exception e) {
    		e.printStackTrace();
    		resultMap.put("result","F");
    		resultMap.put("resultMsg", "사전 설정에 실패하였습니다.");
    	} finally {
    		return resultMap;
    	}
	}
	
	// 모델 분석
	@Async("threadPoolTaskExecutor")
	public ListenableFuture<Map<String, Object>> analyzeModel(ModelVo modelVo, int collectionId, TmUser user) throws InterruptedException {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			// 자동분류, 감성분석
			if (modelVo.getTaskType().equals(TextMinerConstants.TASK_TYPE_AUTO_CLASSIFICATION)
			 || modelVo.getTaskType().equals(TextMinerConstants.TASK_TYPE_EMOTION_ANALYZE)) {
				
				// 모델을 통한 task 및 Collection 가져오기
				TaskVo taskVo = taskMapper.getTaskInfoUseModel(modelVo);
				CollectionVo collectionVo = collectionMapper.getCollectionDetailUseModel(modelVo);
								
				// 분석대상 컬렉션 조회
				CollectionVo analyzeCollectionVo = new CollectionVo();
				analyzeCollectionVo.setCollectionId(collectionId);
				analyzeCollectionVo.setRole(modelVo.getRole());
				analyzeCollectionVo.setCollectionOwner(modelVo.getCollectionOwner());
				analyzeCollectionVo = collectionMapper.getCollectionDetail(analyzeCollectionVo);
			
				if (analyzeCollectionVo != null) {
					// api URI 설정
					uri = UriComponentsBuilder
						 .fromUriString("http://" + tmProperties.getCoreHost() + ":" + tmProperties.getCorePort() + "/api/classifier/analyze")
						 .build()
						 .toUri();
					
					// Header & Body 설정
					// parameter 설정
					JsonObject paramMap = new JsonObject();
					
					// UserId
					paramMap.addProperty("userId", modelVo.getCollectionOwner());
					
					// IP & Port
					if (modelVo.getTaskType().equals(TextMinerConstants.TASK_TYPE_AUTO_CLASSIFICATION)) {
						paramMap.addProperty("serverIp", classifierProperties.getAutoListenerHost());
						paramMap.addProperty("serverPort", classifierProperties.getAutoListenerPort());
					} else {
						paramMap.addProperty("serverIp", classifierProperties.getEmotionListenerHost());
						paramMap.addProperty("serverPort", classifierProperties.getEmotionListenerPort());
						
						// 감성분석 StringMatcher 실행을 위한 IP & Port 추가
						paramMap.addProperty("smServerIp", classifierProperties.getStringMatcherHost());
						paramMap.addProperty("smServerPort", classifierProperties.getStringMatcherPort());
					}
					
					// 프로젝트, 서비스, 컬렉션 ID
					paramMap.addProperty("projectId", taskVo.getProjectId());
					paramMap.addProperty("taskId", taskVo.getTaskId());
					paramMap.addProperty("trainCollectionId", collectionVo.getCollectionId());
					paramMap.addProperty("analyzeCollectionId", analyzeCollectionVo.getCollectionId());
					
					headers = new HttpHeaders();
					headers.setContentType(MediaType.APPLICATION_JSON);
					entity = new HttpEntity<>(paramMap.toString(), headers);
					
					// API Call
					factory = new HttpComponentsClientHttpRequestFactory();
					factory.setConnectTimeout(10000);
					
					restTemplate = new RestTemplate(factory);
					responseEntity = restTemplate.postForEntity(uri, entity, String.class);	
					
					logger.info("@@ {} classifier analyze result {}", modelVo.getModelName(), responseEntity.getBody());
					
					// 분석 요청 후 이력 저장
					ActionHistoryVo actionHistoryVo = new ActionHistoryVo();

			        actionHistoryVo.setActionUser(user.getUsername());
			        actionHistoryVo.setResourceId("0");
			        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
			        actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_ANALYZE_MODEL);
			        actionHistoryVo.setActionMsg(user.getUsername() + " 분석 요청 ( " + modelVo.getTaskType() + " -> \"" + taskVo.getModelFile() + "\" - " + analyzeCollectionVo.getCollectionName() + " )");
		    		actionHistoryVo.setUserIp(user.getUserIp());
			        
			        actionHistoryMapper.insertActionHistory(actionHistoryVo);
				}
			} else {
				// 문서요약, 키워드 추출, 연관어 추출
				// task 정보 가져오기
				TaskVo taskVo = taskMapper.getTaskInfoUseModel(modelVo);

				// 분석대상 컬렉션 조회
				CollectionVo analyzeCollectionVo = new CollectionVo();
				analyzeCollectionVo.setCollectionId(collectionId);
				analyzeCollectionVo.setRole(modelVo.getRole());
				analyzeCollectionVo.setCollectionOwner(modelVo.getCollectionOwner());
				analyzeCollectionVo = collectionMapper.getCollectionDetail(analyzeCollectionVo);
				
	        	if (analyzeCollectionVo != null) {
					// api URI 설정
					uri = UriComponentsBuilder
						 .fromUriString("http://" + tmProperties.getCoreHost() + ":" + tmProperties.getCorePort() + "/api/ta/analyze")
						 .build()
						 .toUri();
					
					// Header & Body 설정
					// parameter 설정
					JsonObject paramMap = new JsonObject();
					
					// UserId
					paramMap.addProperty("userId", modelVo.getCollectionOwner());
					
					// IP & Port
					if (modelVo.getTaskType().equals(TextMinerConstants.TASK_TYPE_KEYWORD_EXTRACTION)) {
						paramMap.addProperty("serverIp", classifierProperties.getKeywordHost());
						paramMap.addProperty("serverPort", classifierProperties.getKeywordPort());
					} else if (modelVo.getTaskType().equals(TextMinerConstants.TASK_TYPE_RELATED_EXTRACTION)) {
						paramMap.addProperty("serverIp", classifierProperties.getRelatedHost());
						paramMap.addProperty("serverPort", classifierProperties.getRelatedPort());
					} else if (modelVo.getTaskType().equals(TextMinerConstants.TASK_TYPE_DOCUMENT_SUMMARY)) {
						paramMap.addProperty("serverIp", classifierProperties.getSummaryHost());
						paramMap.addProperty("serverPort", classifierProperties.getSummaryPort());
					} else if (modelVo.getTaskType().equals(TextMinerConstants.TASK_TYPE_EMOTION_PREPROECESS)) {
						paramMap.addProperty("serverIp", classifierProperties.getEmotionPreprocessHost());
						paramMap.addProperty("serverPort", classifierProperties.getEmotionPreprocessPort());
					} else if (modelVo.getTaskType().equals(TextMinerConstants.TASK_TYPE_SUMMARY_PREPROECESS)) {
						paramMap.addProperty("serverIp", classifierProperties.getSummaryPreprocessHost());
						paramMap.addProperty("serverPort", classifierProperties.getSummaryPreprocessPort());
					}
					
					// 프로젝트, 서비스, 컬렉션 ID
					paramMap.addProperty("projectId", taskVo.getProjectId());
					paramMap.addProperty("taskId", taskVo.getTaskId());
					paramMap.addProperty("taskType", modelVo.getTaskType());
					paramMap.addProperty("analyzeCollectionId", analyzeCollectionVo.getCollectionId());
					
					headers = new HttpHeaders();
					headers.setContentType(MediaType.APPLICATION_JSON);
					entity = new HttpEntity<>(paramMap.toString(), headers);
					
					// API Call
					factory = new HttpComponentsClientHttpRequestFactory();
					factory.setConnectTimeout(10000);
					
					restTemplate = new RestTemplate(factory);
					responseEntity = restTemplate.postForEntity(uri, entity, String.class);	
					
					logger.info("@@ {} ta analyze result {}", modelVo.getModelName(), responseEntity.getBody());
					
					// 분석 요청 후 이력 저장
					ActionHistoryVo actionHistoryVo = new ActionHistoryVo();

			        actionHistoryVo.setActionUser(user.getUsername());
			        actionHistoryVo.setResourceId("0");
			        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
			        actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_ANALYZE_MODEL);
			        actionHistoryVo.setActionMsg(user.getUsername() + " 분석 요청 ( " + modelVo.getTaskType() + " -> \"" + taskVo.getModelFile() + "\" - " + analyzeCollectionVo.getCollectionId() + " )");
		    		actionHistoryVo.setUserIp(user.getUserIp());
			        
			        actionHistoryMapper.insertActionHistory(actionHistoryVo);
				}
			}
		} catch (ResourceAccessException e) {
			logger.error("Failed to connect. {}.", e.getMessage());
		}
		
		return new AsyncResult<Map<String, Object>>(resultMap);
	}
}
