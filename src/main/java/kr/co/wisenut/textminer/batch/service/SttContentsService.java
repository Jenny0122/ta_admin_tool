package kr.co.wisenut.textminer.batch.service;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import kr.co.wisenut.config.ClassifierProperties;
import kr.co.wisenut.config.TMProperties;
import kr.co.wisenut.textminer.batch.mapper.CommonMapper;
import kr.co.wisenut.textminer.batch.mapper.SttContentsMapper;
import kr.co.wisenut.textminer.batch.vo.SttContentsVo;
import kr.co.wisenut.textminer.batch.vo.preprocess.PreprocessVo;
import kr.co.wisenut.textminer.common.TextMinerConstants;
import kr.co.wisenut.util.AesCryptoUtil;

@Service
public class SttContentsService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TMProperties tmProperties;
	
	@Autowired
	private ClassifierProperties classifierProperties;
	
	@Autowired
	private SttContentsMapper sttContentsMapper;
	
	@Autowired
	private CommonMapper commonMapper;
	
	// API Call을 위한 RestTemplate 객체 선언
	URI uri;
	HttpComponentsClientHttpRequestFactory factory;
	RestTemplate restTemplate;
	ResponseEntity<String> responseEntity;
	HttpHeaders headers;
	HttpEntity<?> entity;

	// parser
	JsonParser parser = new JsonParser();
	
	// STT 변환상태 조회
	public String chkSttStatus (String applicationId) {
	    String result = "";
	    try {
	        result = sttContentsMapper.chkSttStatus(applicationId);	
	        
	    } catch (NullPointerException e) {
	    	updateSttFailed(applicationId);
	        logger.error("조회 시 누락된 값에 의한 오류발생!");
	        e.printStackTrace();
	    } catch (Exception e) {
	    	updateSttFailed(applicationId);
	        logger.error("조회 작업을 실패하였습니다.");
	        e.printStackTrace();
	    }
	    
	    return result;
	}
	
	// STT 테이블에서 텍스트 수집
	public SttContentsVo getSttContents (String applicationId) {

	    SttContentsVo sttContentsVo = new SttContentsVo();
	    sttContentsVo.setApplicationId(applicationId);
	    
	    try {
	    	List<Map<String, Object>> resultList = sttContentsMapper.getSttContents(applicationId);	
	    	
	    	logger.info("@@sttContents : {}", resultList);
	    	
	    	if (resultList != null) { 
	    		// MergedSttContents (original)
	    		StringBuilder originSb = new StringBuilder();
	    		// MergedSttContents (for preprocess)
	    		StringBuilder preSb = new StringBuilder();
	    		StringBuilder preResult = new StringBuilder();
	    		// 문장단위 감성분석을 위해 별도로 리스트화
	    		List<String> sttListRx = new ArrayList<String>();
//	    		List<String> sttListTx = new ArrayList<String>();
//	    		List<String> startTimeListRx = new ArrayList<String>();
	    		int lastSeq = 0;
	    		int muteCount = 0;
	    		int muteTime = 0;

	    		for (int i=0; i < resultList.size(); i++) {
	    			String thisResult = String.valueOf(resultList.get(i).get("sttResult"));
	    			thisResult = AesCryptoUtil.decryptStt(thisResult);
	    			
	    			// original MergeStt, preprocess용 MergeStt 생성
	    			if (thisResult.length() > 0) {
	    				originSb.append(thisResult);
	    				originSb.append(". ");
	    				preSb.append("("+String.valueOf(resultList.get(i).get("speakerType"))+")");
    					preSb.append(thisResult);
	    			}
	    			
	    			// 묵음시간, 묵음횟수 계산
	    			if (i == 0) {
	    				// 마지막 발화시간 기록
	    				lastSeq = Integer.valueOf(String.valueOf(resultList.get(i).get("endSeq")));
	    			} else {
	    				// 기록된 마지막 발화시간보다 이번 발화의 startSeq가 늦은 경우 간격 체크, 묵음시간 계산
	    				if (Integer.valueOf(String.valueOf(resultList.get(i).get("startSeq"))) > lastSeq) {
	    					int mute = Integer.valueOf(String.valueOf(resultList.get(i).get("startSeq"))) - lastSeq;
	    					// 간격이 5초 이상이면 묵음시간으로 간주
		    				if (mute >= 5000){
			    				muteCount++;
			    				muteTime += mute;
			    			}
	    				}
	    				// 기록된 마지막 발화시간보다 이번 발화의 endSeq가 늦은 경우 마지막 발화시간 업데잉트
	    				if (Integer.valueOf(String.valueOf(resultList.get(i).get("endSeq"))) > lastSeq) {
    						lastSeq = Integer.valueOf(String.valueOf(resultList.get(i).get("endSeq")));
    					}
	    			}
	    		}
	    		
	    		// simulation URL 설정
				StringBuffer urlString = new StringBuffer();
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
				body.addProperty("serverIp", classifierProperties.getEmotionPreprocessHost());
				body.addProperty("serverPort", classifierProperties.getEmotionPreprocessPort());
				body.addProperty("taskType", TextMinerConstants.TASK_TYPE_EMOTION_PREPROECESS);
				body.addProperty("simulationText", preSb.toString());

	    		body.addProperty("apply_black_words_removal", true);		// 불용어 어절 제거 여부 
	    		body.addProperty("apply_pattern_removal", false);			// 불용어 패턴 제거 여부 (감성분석: false)
	    		body.addProperty("apply_speaker_combination", true);		// 발화 결합 여부
	    		body.addProperty("apply_sentence_separation", true);		// 문장 분리 여부
	    		body.addProperty("apply_spacing_correction", false);		// 띄어쓰기 교정 여부
	    		body.addProperty("apply_split_sentence_word",true);			// 문장 분리 어절 적용 여부
				
				// entity 설정 및 통신 시작
				entity = new HttpEntity<>(body.toString(), headers);
				
				factory = new HttpComponentsClientHttpRequestFactory();
				factory.setConnectTimeout(10000);
				factory.setReadTimeout(10000);		
				
				restTemplate = new RestTemplate(factory);
				responseEntity = restTemplate.postForEntity(uri, entity, String.class);	
				
				// 결과 Json Parsing
				JsonObject apiResult = (JsonObject) parser.parse(responseEntity.getBody());
				JsonObject result = (JsonObject) parser.parse(apiResult.get("apiResult").getAsString());
	    		logger.info("result : " + result);
				
	    		// 감성분석용 전처리 결과 파싱
				ObjectMapper mapper = new ObjectMapper();
				
	    		if (result.get("code").getAsInt() == 105200) {
	    			PreprocessVo preprocessVo = mapper.readValue(result.toString(), PreprocessVo.class);
	    			for (int i=0; i < preprocessVo.getResult().getConversation().size(); i++) {
	    				if (preprocessVo.getResult().getConversation().get(i).get("speaker").equals("customer")) {
		    				sttListRx.add(preprocessVo.getResult().getConversation().get(i).get("speech"));
		    			} else if (preprocessVo.getResult().getConversation().get(i).get("speaker").equals("counselor")) {
//		    				sttListTx.add(conversation.get(i).get("speech"));
		    			}
	    			}
	    		} else {
					logger.error("감성분석 preprocess 모듈 오류: "+ result.get("message").getAsString());
				}	    		
				
				// 문서요약 전처리 수행
				body.addProperty("serverIp", classifierProperties.getSummaryPreprocessHost());
				body.addProperty("serverPort", classifierProperties.getSummaryPreprocessPort());
				body.addProperty("taskType", TextMinerConstants.TASK_TYPE_SUMMARY_PREPROECESS);
	    		body.addProperty("apply_pattern_removal", true);			// 불용어 패턴 제거 여부 (문서요약: true)
	    		
	    		// entity 재설정 및 통신 시작
				entity = new HttpEntity<>(body.toString(), headers);
	    		
				restTemplate = new RestTemplate(factory);
				responseEntity = restTemplate.postForEntity(uri, entity, String.class);	

				apiResult = (JsonObject) parser.parse(responseEntity.getBody());
				result = (JsonObject) parser.parse(apiResult.get("apiResult").getAsString());
				
				// 문서요약용 전처리 결과 파싱
	    		if (result.get("code").getAsInt() == 105200) {
	    			PreprocessVo preprocessVo = mapper.readValue(result.toString(), PreprocessVo.class);
	    			for (int i=0; i < preprocessVo.getResult().getConversation().size(); i++) {
		    			preResult.append(preprocessVo.getResult().getConversation().get(i).get("speech")+". ");
	    			}
	    		} else {
					logger.error("문서요약 preprocess 모듈 오류: "+ result.get("message").getAsString());
				}
	    		
	    		
	    		// 인바운드일 경우에만 자동유형분류 필요 체크 
	    		if (String.valueOf(resultList.get(0).get("isAnaClass")).equals("I")) {
	    			sttContentsVo.setIsAnaClass("Y");
	    		} else if (String.valueOf(resultList.get(0).get("isAnaClass")).equals("O")) {
	    			sttContentsVo.setIsAnaClass("N");
	    		}
	    		
	    		sttContentsVo.setMergedSttContents(originSb.toString());
	    		sttContentsVo.setMergedSttContentsPre(preResult.toString());
//	    		sttContentsVo.setStartTimeListRx(startTimeListRx);
	    		sttContentsVo.setSttListRx(sttListRx);
//	    		sttContentsVo.setSttListTx(sttListTx);
	    		sttContentsVo.setMuteCount(muteCount);
	    		sttContentsVo.setMuteTime(muteTime);
	    		
	    		//세션테이블에 묵음정보 업데이트 
	    		commonMapper.updateMuteInfo(sttContentsVo);
	    		
	    		logger.info("@@sttContentsVo : {}", sttContentsVo);
	    	}
	    } catch (NullPointerException e) {
	    	updateSttFailed(applicationId);
	        logger.error("조회 시 누락된 값에 의한 오류발생!");
	        e.printStackTrace();
	    } catch (Exception e) {
	    	updateSttFailed(applicationId);
	        logger.error("조회 작업을 실패하였습니다.");
	        e.printStackTrace();
	    }
	    
	    return sttContentsVo;
	}
	
	// STT 재수집 TA 재분석 대상 CallId 조회
	public List<String> getSttReprocessCallId () {
		List<String> resultList = new ArrayList<String>();
		
		try {
			resultList = sttContentsMapper.getSttReprocessCallId();
			
		} catch (NullPointerException e) {
	        logger.error("조회 시 누락된 값에 의한 오류발생!");
	        e.printStackTrace();
	    } catch (Exception e) {
	        logger.error("조회 작업을 실패하였습니다.");
	        e.printStackTrace();
	    }
		
		return resultList;
	}
	
	// STT 수집 실패 - 세션 업데이트
	public void updateSttFailed (String applicationId) {
		try {
			sttContentsMapper.updateSttFailed(applicationId);
			// notmatch테이블에도 업데이트할지 필요여부 확인 
			
		} catch (Exception e) {
	        logger.error("미매칭 업데이트 작업을 실패하였습니다.");
	        e.printStackTrace();
	    }
	}
}
