package kr.co.wisenut.textminer.batch.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import kr.co.wisenut.config.TMProperties;
import kr.co.wisenut.textminer.batch.mapper.BatchMapper;
import kr.co.wisenut.textminer.batch.mapper.CommonMapper;
import kr.co.wisenut.textminer.batch.vo.ApiResultErrorVo;
import kr.co.wisenut.textminer.batch.vo.SttContentsVo;
import kr.co.wisenut.textminer.batch.vo.AnaSummarize.AnaSummarizeVo;
import kr.co.wisenut.textminer.batch.vo.anaKeyword.AnaKeywordVo;
import kr.co.wisenut.textminer.batch.vo.anaRelatedKeyword.AnaRelatedKeywordVo;
import kr.co.wisenut.textminer.batch.vo.anaSentiment.AnaSentimentErrorVo;
import kr.co.wisenut.textminer.batch.vo.anaSentiment.AnaSentimentVo;
import kr.co.wisenut.textminer.batch.vo.stringMatch.AnaStringMatchErrorVo;
import kr.co.wisenut.textminer.batch.vo.stringMatch.AnaStringMatchVo;
import kr.co.wisenut.textminer.common.TextMinerConstants;
import kr.co.wisenut.textminer.deploy.mapper.DeployMapper;
import kr.co.wisenut.textminer.deploy.vo.DeployVo;

@Service
public class TaModuleService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TMProperties tmProperties;
	
	@Autowired
	private DeployMapper deployMapper;
	
	@Autowired
	private CommonMapper commonMapper;
	
	@Autowired
	private BatchMapper batchMapper;
	
	// API Call을 위한 RestTemplate 객체 선언
	URI uri;
	HttpComponentsClientHttpRequestFactory factory;
	RestTemplate restTemplate;
	ResponseEntity<String> responseEntity;
	HttpHeaders headers;
	HttpEntity<?> entity;
	
	// parser
	JsonParser parser = new JsonParser();
	
	// TA Module 호출
	public boolean callModule (String taskType, SttContentsVo sttContentsVo) {
		
		Boolean isCompleted = true;		// 감성분석, 문서요약 결과 true/false
		
		try {
			// 재분석을 위한 모듈정보 조회하기(운영)
			DeployVo deployVo = new DeployVo();
			StringBuffer urlString = new StringBuffer();
			JsonObject apiResult = null;
			JsonObject result = null;
			JsonObject smResult = null;
			
			if (taskType.equals(TextMinerConstants.TASK_TYPE_EMOTION_ANALYZE)) {
				deployVo.setServerType(TextMinerConstants.SERVER_TYPE_PROD);
				deployVo.setServerTask(taskType);
				deployVo = deployMapper.getDeployDetail(deployVo);
				
				// URL 설정
				urlString.append("http://")
						 .append(tmProperties.getCoreHost())
						 .append(":")
						 .append(tmProperties.getCorePort())
						 .append("/api/simulationBulk");
				uri = UriComponentsBuilder.fromUriString(urlString.toString()).build().toUri();
				
				// header 설정
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
		
				// Parameter 설정
				JsonObject body = new JsonObject();
				body.addProperty("serverIp", deployVo.getServerIp());
				body.addProperty("serverPort", deployVo.getServerPort());
				body.addProperty("taskType", taskType);
				body.addProperty("simulationType", TextMinerConstants.EXECUTE_SIMULATION_TYPE_BATCH);
				body.addProperty("preprocessOption", "N");
				
				List<Map<String, String>> texts = new ArrayList<Map<String, String>>();
				List<String> textsSM = new ArrayList<String>();
				
				for (int i = 0; i < sttContentsVo.getSttListRx().size(); i++) {
					if (sttContentsVo.getSttListRx().get(i).length() > 0) {
						Map<String, String> rxText = new HashMap<String, String>();
						rxText.put("id", String.valueOf(i));
						rxText.put("text", sttContentsVo.getSttListRx().get(i));
						texts.add(rxText);
						textsSM.add(sttContentsVo.getSttListRx().get(i));
					}
				}
				Gson gson = new Gson();
				body.add("simulationTexts", gson.toJsonTree(texts));
				
				// entity 설정 및 통신 시작
				entity = new HttpEntity<>(body.toString(), headers);
				
				factory = new HttpComponentsClientHttpRequestFactory();
				factory.setConnectTimeout(10 * 1000);
				factory.setReadTimeout(10 * 1000);		
				
				restTemplate = new RestTemplate(factory);
				responseEntity = restTemplate.postForEntity(uri, entity, String.class);	
				
				// 결과 Json Parsing
				apiResult = (JsonObject) parser.parse(responseEntity.getBody());
				result = (JsonObject) parser.parse(apiResult.get("apiResult").getAsString());

				logger.info("@@ result : " + result);
				
				// classifier는 처음 결과가 실패인 경우 최대 3번까지 재호출하여 결과를 확인할 수 있도록 한다.
				int retryCnt = 0;
				while(result.get("code").getAsInt() != 102200 && retryCnt < 3) {
					responseEntity = restTemplate.postForEntity(uri, entity, String.class);	
					apiResult = (JsonObject) parser.parse(responseEntity.getBody());
					result = (JsonObject) parser.parse(apiResult.get("apiResult").getAsString());
					retryCnt++;
				}

				logger.info("@@ smResult : start..");
				
				// String Match 
				deployVo = new DeployVo();
				deployVo.setServerType(TextMinerConstants.SERVER_TYPE_PROD);
				deployVo.setServerTask(TextMinerConstants.TASK_TYPE_STRING_MATCHER);
				deployVo = deployMapper.getDeployDetail(deployVo);

				body = new JsonObject();
				body.addProperty("serverIp", deployVo.getServerIp());
				body.addProperty("serverPort", deployVo.getServerPort());
				body.addProperty("taskType", TextMinerConstants.TASK_TYPE_STRING_MATCHER);
				body.addProperty("simulationType", TextMinerConstants.EXECUTE_SIMULATION_TYPE_BATCH);
				body.add("simulationTexts", gson.toJsonTree(textsSM));
				
				// entity 설정 및 통신 시작
				entity = new HttpEntity<>(body.toString(), headers);
				
				factory = new HttpComponentsClientHttpRequestFactory();
				factory.setConnectTimeout(10 * 1000);
				factory.setReadTimeout(10 * 1000);		
				
				restTemplate = new RestTemplate(factory);
				responseEntity = restTemplate.postForEntity(uri, entity, String.class);	

				// 결과 Json Parsing
				apiResult = (JsonObject) parser.parse(responseEntity.getBody());
				smResult = (JsonObject) parser.parse(apiResult.get("apiResult").getAsString());
				
				logger.info("@@ smResult : " + smResult);
				
			} else {
				deployVo.setServerType(TextMinerConstants.SERVER_TYPE_PROD);
				deployVo.setServerTask(taskType);
				deployVo = deployMapper.getDeployDetail(deployVo);
				
				// URL 설정
				urlString.append("http://")
						 .append(tmProperties.getCoreHost())
						 .append(":")
						 .append(tmProperties.getCorePort())
						 .append("/api/simulation");
				uri = UriComponentsBuilder.fromUriString(urlString.toString()).build().toUri();
				
				// header 설정
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
		
				// Parameter 설정
				JsonObject body = new JsonObject();
				body.addProperty("serverIp", deployVo.getServerIp());
				body.addProperty("serverPort", deployVo.getServerPort());
				body.addProperty("taskType", taskType);
				body.addProperty("simulationType", TextMinerConstants.EXECUTE_SIMULATION_TYPE_BATCH);
				body.addProperty("simulationText", sttContentsVo.getMergedSttContents());
				
				if (taskType.equals(TextMinerConstants.TASK_TYPE_DOCUMENT_SUMMARY)) {
					body.addProperty("preprocessOption", "Y");
				} else {
					body.addProperty("preprocessOption", "N");
				}
				
				// entity 설정 및 통신 시작
				entity = new HttpEntity<>(body.toString(), headers);
				
				factory = new HttpComponentsClientHttpRequestFactory();
				factory.setConnectTimeout(10 * 1000);
				factory.setReadTimeout(10 * 1000);		
				
				restTemplate = new RestTemplate(factory);
				responseEntity = restTemplate.postForEntity(uri, entity, String.class);	
				
				// 결과 Json Parsing
				apiResult = (JsonObject) parser.parse(responseEntity.getBody());
				result = (JsonObject) parser.parse(apiResult.get("apiResult").getAsString());
				
				// classifier는 처음 결과가 실패인 경우 최대 3번까지 재호출하여 결과를 확인할 수 있도록 한다.
				if (taskType.equals(TextMinerConstants.TASK_TYPE_AUTO_CLASSIFICATION)) {
					int retryCnt = 0;
					while(result.get("code").getAsInt() != 102200 && retryCnt < 3) {
						responseEntity = restTemplate.postForEntity(uri, entity, String.class);	
						apiResult = (JsonObject) parser.parse(responseEntity.getBody());
						result = (JsonObject) parser.parse(apiResult.get("apiResult").getAsString());
						retryCnt++;
					}
				}
			}
			
			// taskType에 따른 결과처리
			// 1) 결과값 성공일 경우 미매칭 end_dt 업데이트
			Map<String, Object> paramMap = null;
			
			switch(taskType) {
				case TextMinerConstants.TASK_TYPE_AUTO_CLASSIFICATION:
					// 자동분류 처리결과 저장 
			   	    if (sttContentsVo.getIsAnaClass().equals("Y")) {
			   	    	isCompleted = resultAutoProcess(result, sttContentsVo);
			   	    }
					break;
				case TextMinerConstants.TASK_TYPE_EMOTION_ANALYZE:
					
		 	        // STT 재처리일시 미매칭 업데이트
	 				if (result.get("code").getAsString().equals("102200")
			         && sttContentsVo.getTbDiv().equals("1")) {
	 					paramMap = new HashMap<String, Object>();
			        	paramMap.put("applicationId", sttContentsVo.getApplicationId());
			        	paramMap.put("resultCd", sttContentsVo.getResultCd());
			        	commonMapper.updateNotMatch(paramMap);
			        }
					
					// 감성분석 처리결과 저장 
					isCompleted = resultEmotionProcess(smResult, result, sttContentsVo);
					break;
				case TextMinerConstants.TASK_TYPE_KEYWORD_EXTRACTION:
					// 키워드 추출 처리결과 저장 
					isCompleted = resultKeywordProcess(result, sttContentsVo);
					break;
				case TextMinerConstants.TASK_TYPE_RELATED_EXTRACTION:
					// 연관어 추출 처리결과 저장 
					isCompleted = resultRelatedProcess(result, sttContentsVo);
					break;
				case TextMinerConstants.TASK_TYPE_DOCUMENT_SUMMARY:
					// 문서요약 처리결과 저장
					isCompleted = resultSummaryProcess(result, sttContentsVo);
					break;
			}
		} catch (ResourceAccessException e) {
			// 분석 에러처리
			logger.error("{} predict failed. -> {}", taskType, e.getMessage());
			commonMapper.updateAnaFailed(sttContentsVo.getApplicationId());
		}
		
		return isCompleted;
	}
	
	// 자동분류 처리결과 저장
	public boolean resultAutoProcess(JsonObject moduleResult, SttContentsVo sttContentsVo) {
		
		boolean isCompleted = false;
		
		try {
			Map<String, Object> param = new HashMap<String, Object>();
			
	        if (moduleResult.get("code").getAsInt() == 102200) {
        		param.put("applicationId", sttContentsVo.getApplicationId());
	        	
	        	// confidence가 임계치 이하 -> 유효한 분류유형이 없는 경우
	        	if (moduleResult.get("result").getAsJsonObject().get("label").getAsString().equalsIgnoreCase("NO_LABEL")) {
	        		param.put("resultCd", "102500"); 	// NO_LABEL 코드
	        	} else {
	        		param.put("resultCd", moduleResult.get("result").getAsJsonObject().get("label").getAsString());
	        	}
	        	
	        	JsonArray predictArray = moduleResult.get("result").getAsJsonObject().get("predict").getAsJsonArray();
	        	JsonObject predict = null;
        		
	        	int insertCount = 3;
	        	if (predictArray.size() < 3) {
	        		insertCount = predictArray.size();
	        	}
	        	
	        	for (int i = 0; i < insertCount; i++) {
	        		predict = predictArray.get(i).getAsJsonObject();
	        		
	        		param.put("ldClsCd", predict.get("label").getAsString().substring(0, 2));
	        		param.put("mdClsCd", predict.get("label").getAsString().substring(2, 4));
	        		param.put("mdClsCdForNm", predict.get("label").getAsString().substring(0, 4));
	        		param.put("sdClsCd", predict.get("label").getAsString().substring(4));
	        		param.put("clsLabel", predict.get("label").getAsString());
	        		param.put("confidence", predict.get("confidence").getAsString());
	        		param.put("score", predict.get("score").getAsString());
	        		
	        		if (i == 0) {
	        			param.put("clsFlag", "1");
	        		} else {
	        			param.put("clsFlag", "");
	        		}
	        		
	        		// 분석결과 DB 입력
	        		batchMapper.insertClassifireResult(param);
	        		
	        		isCompleted = true;
	        	}
	        } else {
	        	logger.error("module failed" + moduleResult.get("description").getAsString());
	        	
	        	param.put("applicationId", sttContentsVo.getApplicationId());
	        	param.put("resultCd", "102410"); // 유형분류 실패코드
	        	
	        	commonMapper.insertNotMatch(param);
	        	commonMapper.updateAnaFailed(sttContentsVo.getApplicationId());
	        }
			
		} catch (NullPointerException e) {
			commonMapper.updateAnaFailed(sttContentsVo.getApplicationId());
	        logger.error(e.getMessage());
	        e.printStackTrace();
	    } catch (Exception e) {
	    	commonMapper.updateAnaFailed(sttContentsVo.getApplicationId());
	        logger.error(e.getMessage());
	        e.printStackTrace();
	    }		
		
		return isCompleted;
	}
	
	// 감성분석 처리결과 저장
	public boolean resultEmotionProcess(JsonObject smResult, JsonObject moduleResult, SttContentsVo sttContentsVo) {

		logger.info("@@ debug : " + smResult);
		
		int result = 0;
		boolean isCompleted = false;
		
		try {
			if (sttContentsVo.getSttListRx().size() == 0) {
				isCompleted = true;
				Map<String, Object> param = new HashMap<String, Object>();
				// 콜단위 긍부정 결과 저장 - 고객발화가 모두 전처리되어 없는 경우 긍정으로 분류
				param.clear();
				param.put("applicationId", sttContentsVo.getApplicationId());
				param.put("sentPt", "");
				param.put("sentClsCd", "300001");
				result = batchMapper.updateSentimentCallResult(param);
				
				return isCompleted;
			}
			
			ObjectMapper mapper = new ObjectMapper();
//			double positive = 0;
			double negative = 0;
			int anaCompContent = 0;
			Map<String, Object> param = new HashMap<String, Object>();
			
			// classifier sentiment result code에 따라 vo파싱
			AnaSentimentVo anaSentimentVo = new AnaSentimentVo();
			AnaSentimentErrorVo anaSentimentErrorVo = new AnaSentimentErrorVo();
			
			if (moduleResult.get("code").getAsInt() == 102200) {
				anaSentimentVo = mapper.readValue(moduleResult.toString(), AnaSentimentVo.class);
			} else {
				anaSentimentErrorVo = mapper.readValue(moduleResult.toString(), AnaSentimentErrorVo.class);
			}
						
			// 분석 성공으로 판단 기준: 모든 고객발화(rx)에 대해 분석이 완료됨
			// stringMatch에서 검출되지 않은 row에 한해 sentiment 분석결과로 판별 
			// -> stringMatch 단계에서 모든 row가 분석완료되었다면 sentiment가 실패여도 분석완료 처리 
			
			// stringMatcher result code check
			if (moduleResult.get("code").getAsInt() == 102200) {
				AnaStringMatchVo anaStringMatchVo = mapper.readValue(smResult.toString(), AnaStringMatchVo.class);
	        	
				for (int i = 0; i < anaSentimentVo.getResult().size(); i++) {
					if (anaStringMatchVo.getResult().getMatchInfoList().get(i).getMatchInfo().size() > 0) {
						// 우선순위로 정렬된 라벨 리스트, 가중치
						// todo :: 라벨명 수정, 가중치 확정 및 수정 
						String[] labelArr = {"NL1", "NL2", "NL3", "PL1"};
						double[] addConfidenceArr = { sttContentsVo.getSttListRx().size()*0.6 // NL1
										            , sttContentsVo.getSttListRx().size()*0.5 // NL2
										            , sttContentsVo.getSttListRx().size()*0.2 // NL3
										            , 0           							  // PL1
										            };
						ArrayList<String> labels = new ArrayList<String>(Arrays.asList(labelArr));
						String label = "";
						
						double count = 0;
						int index = -1;
						// 우선순위가 가장 높은 라벨을 적용
						for (int j = 0; j < anaStringMatchVo.getResult().getMatchInfoList().get(i).getMatchInfo().size(); j++) {
							if (j == 0) {
								label = anaStringMatchVo.getResult().getMatchInfoList().get(i).getMatchInfo().get(j).getPattern().getLabel();
								index = labels.indexOf(label);
								count = 1;
							} else {
								if (labels.indexOf(anaStringMatchVo.getResult().getMatchInfoList().get(i).getMatchInfo().get(j).getPattern().getLabel()) < index) {
									// 상위 라벨 -> 검출 라벨 변경, 검출 수 초기화 
									label = anaStringMatchVo.getResult().getMatchInfoList().get(i).getMatchInfo().get(j).getPattern().getLabel();
									index = labels.indexOf(label);
									count = 1;
								} else if (labels.indexOf(anaStringMatchVo.getResult().getMatchInfoList().get(i).getMatchInfo().get(j).getPattern().getLabel()) == index) {
									 // 동일 라벨 -> 검출 수 ++
							         count ++;
								}	 // 하위 라벨 -> 무시
							}
						}
						// 파라미터 세팅
						param.put("applicationId", sttContentsVo.getApplicationId());
			        	param.put("contentId", ""); 
			        	param.put("contents", anaStringMatchVo.getResult().getMatchInfoList().get(i).getText());
			        	param.put("resultCd", anaStringMatchVo.getCode());
			        	param.put("clsLabel", label);
			        	param.put("confidence", addConfidenceArr[labels.indexOf(label)]);	// 이 라벨의 가중치값을 적용
			        	param.put("score", 1.99);
			        	
			        	// 분석결과 DB 입력
			        	result = batchMapper.insertSentimentResult(param);
			        	
			        	// 콜단위 긍부정 판단을 위한 연산 - 이 라벨의 가중치값을 부정 비교값에 합산
			        	negative += addConfidenceArr[labels.indexOf(label)]*(count/2+0.5);
			        	anaCompContent ++;
					} else {
						if (moduleResult.get("code").getAsInt() == 102200) {
							param.put("applicationId", sttContentsVo.getApplicationId());
				        	param.put("contentId", ""); 
				        	param.put("contents", sttContentsVo.getSttListRx().get(i));
				        	param.put("resultCd", anaSentimentVo.getCode());
				        	
				        	if (anaSentimentVo.getResult().get(i).getLabel().equalsIgnoreCase("NO_LABEL")){
				        		param.put("clsLabel", "없음");
					        	param.put("confidence", "");
					        	param.put("score", "");
				        	} else {
				        		param.put("clsLabel", anaSentimentVo.getResult().get(i).getLabel());
					        	param.put("confidence", anaSentimentVo.getResult().get(i).getPredict().get(0).getConfidence());
					        	param.put("score", anaSentimentVo.getResult().get(i).getPredict().get(0).getScore());
					        	
					        	// 콜단위 긍부정 판단을 위한 연산 - 부정인 경우 이 문장의 confidence값을 부정 비교값에 연산
					        	if (anaSentimentVo.getResult().get(i).getLabel().equals("부정")) {
					        		negative += anaSentimentVo.getResult().get(i).getPredict().get(0).getConfidence();
					        	}
				        	}
				        	
				        	// 분석결과 DB 입력
				        	result = batchMapper.insertSentimentResult(param);
				        	anaCompContent ++;
						}
					}
				}
	        } else {
	        	// String Matcher 실패로 떨어진 경우 classifier 결과로 판단
				if (moduleResult.get("code").getAsInt() == 102200) {
					for (int i=0; i<anaSentimentVo.getResult().size(); i++) {
						// 파라미터 세팅
						param.put("applicationId", sttContentsVo.getApplicationId());
			        	param.put("contentId", ""); 
			        	param.put("contents", sttContentsVo.getSttListRx().get(i));
			        	param.put("resultCd", anaSentimentVo.getCode());
			        	
			        	if (anaSentimentVo.getResult().get(i).getLabel().equalsIgnoreCase("NO_LABEL")){
			        		param.put("clsLabel", "없음");
				        	param.put("confidence", "");
				        	param.put("score", "");
			        	} else {
			        		param.put("clsLabel", anaSentimentVo.getResult().get(i).getLabel());
				        	param.put("confidence", anaSentimentVo.getResult().get(i).getPredict().get(0).getConfidence());
				        	param.put("score", anaSentimentVo.getResult().get(i).getPredict().get(0).getScore());
				        	
				        	// 콜단위 긍부정 판단을 위한 연산 - 부정인 경우 이 문장의 confidence값을 부정 비교값에 연산
				        	if (anaSentimentVo.getResult().get(i).getLabel().equals("부정")) {
				        		negative += anaSentimentVo.getResult().get(i).getPredict().get(0).getConfidence();
				        	}
			        	}
			        	
			        	// 분석결과 DB 입력
			        	result = batchMapper.insertSentimentResult(param);
			        	anaCompContent ++;
					}
					
					// 콜단위 긍부정 결과 저장
					param.clear();
					param.put("applicationId", sttContentsVo.getApplicationId());
					param.put("sentPt", (double)(negative/((double)sttContentsVo.getSttListRx().size()))*100);
					
					// todo :: 퍼센트 조정 필요
					if (negative > (double)sttContentsVo.getSttListRx().size()*0.6) {		
						param.put("sentClsCd", "300002");	// negative
					} else {	
						param.put("sentClsCd", "300001");	// positive
					}
					result = batchMapper.updateSentimentCallResult(param);
					isCompleted = true;
					
				} else {	
					// 두 모듈 모두 오류일때 fail
					AnaStringMatchErrorVo anaStringMatchErrorVo = mapper.readValue(smResult.toString(), AnaStringMatchErrorVo.class);
					
					logger.error("module failed:: "+anaStringMatchErrorVo.getMessage());
		        	
		        	param.put("applicationId", sttContentsVo.getApplicationId());
		        	param.put("resultCd", "102420"); // 감성분석 실패코드
		        	
		        	result = commonMapper.insertNotMatch(param);
		        	result = commonMapper.updateAnaFailed(sttContentsVo.getApplicationId());
				}
	        }
			
		} catch (NullPointerException e) {
			commonMapper.updateAnaFailed(sttContentsVo.getApplicationId());
	        logger.error(e.getMessage());
	        e.printStackTrace();
	    } catch (Exception e) {
	    	commonMapper.updateAnaFailed(sttContentsVo.getApplicationId());
	        logger.error(e.getMessage());
	        e.printStackTrace();
	    }
		
		return isCompleted;
	}
	
	// 키워드 추출 처리결과 저장
	public boolean resultKeywordProcess(JsonObject moduleResult, SttContentsVo sttContentsVo) {
		
		boolean isCompleted = false;
		
		try {
			Map<String, Object> param = new HashMap<String, Object>();
			ObjectMapper mapper = new ObjectMapper();
			
	        // todo :: 성공 code 확인 필요 
	        if (moduleResult.get("code").getAsInt() == 101200) {
	        	AnaKeywordVo anaKeywordVo = mapper.readValue(moduleResult.toString(), AnaKeywordVo.class);
	        	
	        	for (int i = 0; i < anaKeywordVo.getResult().getKeywords().size(); i++) {
	        		param.put("applicationId", sttContentsVo.getApplicationId());
	        		param.put("resultCd", anaKeywordVo.getCode());
	        		param.put("keyword", anaKeywordVo.getResult().getKeywords().get(i).getWord());
	        		param.put("score", anaKeywordVo.getResult().getKeywords().get(i).getScore());
	        		param.put("count", anaKeywordVo.getResult().getKeywords().get(i).getCount());
	        		param.put("tag", anaKeywordVo.getResult().getKeywords().get(i).getTag());
	        		
	        		batchMapper.insertKeywordExtractionResult(param);
	        		
	        		isCompleted = true;
	        	}
	        } else {
	        	ApiResultErrorVo apiResultErrorVo = mapper.readValue(moduleResult.toString(), ApiResultErrorVo.class);
	        	logger.error("module failed" + apiResultErrorVo.getMessage());
	        	
//	        	param.put("applicationId", sttContentsVo.getApplicationId());
//	        	param.put("resultCd", apiResultErrorVo.getCode());
//	        	
//	        	commonMapper.insertNotMatch(param);
//	        	commonMapper.updateAnaFailed(sttContentsVo.getApplicationId());
	        }
			
		} catch (NullPointerException e) {
//			commonMapper.updateAnaFailed(sttContentsVo.getApplicationId());
			logger.error(e.getMessage());
	        e.printStackTrace();
	    } catch (Exception e) {
//			commonMapper.updateAnaFailed(sttContentsVo.getApplicationId());
			logger.error(e.getMessage());
	        e.printStackTrace();
	    }
		
		return isCompleted;
	}

	// 연관어 추출 처리결과 저장
	public boolean  resultRelatedProcess(JsonObject moduleResult, SttContentsVo sttContentsVo) {

		boolean isCompleted = false;
		
		try {
			Map<String, Object> param = new HashMap<String, Object>();
			ObjectMapper mapper = new ObjectMapper();

	        // todo :: 성공 code 확인 필요 
	        if (moduleResult.get("code").getAsInt() == 104200) {
	        	AnaRelatedKeywordVo anaRelatedKeywordVo = mapper.readValue(moduleResult.toString(), AnaRelatedKeywordVo.class);
	        	
	        	for (String key : anaRelatedKeywordVo.getResult().getKeywords().keySet()) {
	        		int order = 1;
	        		
	        		param.put("applicationId", sttContentsVo.getApplicationId());
	        		param.put("resultCd", anaRelatedKeywordVo.getCode());
	        		param.put("keyword", key);
	        		for (String keyword : anaRelatedKeywordVo.getResult().getKeywords().get(key).keySet()) {
	        			if (order == 1) {
	        				param.put("rword01", keyword);
	        				param.put("score01", anaRelatedKeywordVo.getResult().getKeywords().get(key).get(keyword));
	        			} else if (order == 2) {
	        				param.put("rword02", keyword);
	        				param.put("score02", anaRelatedKeywordVo.getResult().getKeywords().get(key).get(keyword));
	        			} else if (order == 3) {
	        				param.put("rword03", keyword);
	        				param.put("score03", anaRelatedKeywordVo.getResult().getKeywords().get(key).get(keyword));
	        			} else if (order == 4) {
	        				param.put("rword04", keyword);
	        				param.put("score04", anaRelatedKeywordVo.getResult().getKeywords().get(key).get(keyword));
	        			} else if (order == 5) {
	        				param.put("rword05", keyword);
	        				param.put("score05", anaRelatedKeywordVo.getResult().getKeywords().get(key).get(keyword));
	        			}
	        			order ++;
	        		}
	        		
	        		// 분석결과 DB 입력
	        		batchMapper.insertRelatedKeywordExtractionResult(param);
	        		
	        		isCompleted = true;
	        	}
	        	
	        } else {
	        	ApiResultErrorVo apiResultErrorVo = mapper.readValue(moduleResult.toString(), ApiResultErrorVo.class);
	        	logger.error("module failed"+apiResultErrorVo.getMessage());
	        	
//	        	param.put("applicationId", sttContentsVo.getApplicationId());
//	        	param.put("resultCd", apiResultErrorVo.getCode());
//	        	
//	        	commonMapper.insertNotMatch(param);
//	        	commonMapper.updateAnaFailed(sttContentsVo.getApplicationId());
	        }
			
		} catch (NullPointerException e) {
//			commonMapper.updateAnaFailed(sttContentsVo.getApplicationId());
	        logger.error(e.getMessage());
	        e.printStackTrace();
	    } catch (Exception e) {
//	    	commonMapper.updateAnaFailed(sttContentsVo.getApplicationId());
	        logger.error(e.getMessage());
	        e.printStackTrace();
	    }		
		
		return isCompleted;
	}
	
	// 문서요약 처리결과 저장
	public boolean resultSummaryProcess(JsonObject moduleResult, SttContentsVo sttContentsVo) {
		
		int result;
		boolean isCompleted = false;
		
		try {
			Map<String, Object> param = new HashMap<String, Object>();
			ObjectMapper mapper = new ObjectMapper();
			
			if (sttContentsVo.getMergedSttContentsPre().length() == 0) {
				isCompleted = true;
				
				// 발화가 모두 전처리되어 없는 경우
				param.put("applicationId", sttContentsVo.getApplicationId());
        		param.put("resultCd", "103400");
        		param.put("summString", "요약없음");
        		result = batchMapper.insertSummarizeResult(param);
        		
				return isCompleted;
			}
			
			// todo :: 성공 code 확인 필요 
	        if (moduleResult.get("code").getAsInt() == 103200) {
	        	AnaSummarizeVo anaSummarizeVo = mapper.readValue(moduleResult.toString(), AnaSummarizeVo.class);
	        	
        		param.put("applicationId", sttContentsVo.getApplicationId());
        		param.put("resultCd", anaSummarizeVo.getCode());
        		
        		StringBuilder sb = new StringBuilder(); // summarized text
        		for (int i = 0; i < anaSummarizeVo.getResult().getSummary().size(); i++) {
        			sb.append(anaSummarizeVo.getResult().getSummary().get(i));
        			sb.append(" ");
        		}
        		param.put("summString", sb.toString());
        		
        		// todo :: 분석에는 성공했지만 데이터 저장 실패한 경우 에러코드 따로 필요?
        		// 분석결과 DB 입력
        		batchMapper.insertSummarizeResult(param);
        		isCompleted = true;
	        	
	        } else {
	        	ApiResultErrorVo apiResultErrorVo = mapper.readValue(moduleResult.toString(), ApiResultErrorVo.class);

	        	logger.error("module failed"+apiResultErrorVo.getMessage());
	        	
	        	param.put("applicationId", sttContentsVo.getApplicationId());
	        	param.put("resultCd", apiResultErrorVo.getCode());
	        	
	        	commonMapper.insertNotMatch(param);
	        	commonMapper.updateAnaFailed(sttContentsVo.getApplicationId());
	        }
			
		} catch (NullPointerException e) {
			commonMapper.updateAnaFailed(sttContentsVo.getApplicationId());
	        logger.error(e.getMessage());
	        e.printStackTrace();
	    } catch (Exception e) {
	    	commonMapper.updateAnaFailed(sttContentsVo.getApplicationId());
	        logger.error(e.getMessage());
	        e.printStackTrace();
	    }
		
		return isCompleted;
	}
	
	public List<String> getNotMatchList(String applicationId){
		return commonMapper.getNotMatchList(applicationId);
	}
	
	public int updateAnaStatus(Map<String, Object> paramMap) {
		return commonMapper.updateAnaStatus(paramMap);
	}
}
