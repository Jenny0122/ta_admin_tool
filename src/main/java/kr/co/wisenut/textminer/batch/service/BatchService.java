package kr.co.wisenut.textminer.batch.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import kr.co.wisenut.textminer.batch.mapper.BatchMapper;
import kr.co.wisenut.textminer.batch.mapper.StopwordMapper;
import kr.co.wisenut.textminer.batch.vo.SttContentsVo;

@Service
public class BatchService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private BatchMapper batchMapper;
	
	@Autowired
	private StopwordMapper stopwordMapper;

	@Value("${send.message.url}")
	private String sendMsgUrl;
	
	@Value("${edw.data.path}")
	private String edwPath;
	
	@Value("${spring.profiles.active}")
	private String profile;
	
	// 모듈분석 실패콜 재분석 대상 CallInfo 조회
	public List<Map<String, Object>> getModuleFailedCallInfo () {
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		
		try {
			resultList = batchMapper.getModuleFailedCallInfo();
			
		} catch (NullPointerException e) {
	        logger.error("조회 시 누락된 값에 의한 오류발생!");
	        e.printStackTrace();
	    } catch (Exception e) {
	        logger.error("조회 작업을 실패하였습니다.");
	        e.printStackTrace();
	    }
		
		return resultList;
	}
	
	// 분석실패 데이터 삭제
	public void deleteAnalyzeFailData(SttContentsVo sttContentsVo) {
		
		try {
			batchMapper.deleteClassifireResult(sttContentsVo);					// 유형분석 삭제
			batchMapper.deleteSentimentResult(sttContentsVo);					// 감성분석 삭제
			batchMapper.deleteKeywordExtractionResult(sttContentsVo);			// 키워드추출 삭제
			batchMapper.deleteRelatedKeywordExtractionResult(sttContentsVo);	// 연관어추출 삭제
			batchMapper.deleteSummarizeResult(sttContentsVo);					// 문서요약 삭제
			
		} catch (NullPointerException e) {
	        logger.error("삭제 시 누락된 값에 의한 오류발생!");
	        e.printStackTrace();
	    } catch (Exception e) {
	        logger.error("삭제 작업을 실패하였습니다.");
	        e.printStackTrace();
	    }
		
	}
	
	// 집계 실행
	public int dataTotal (String type, LocalDateTime date) {
	    int result = -1;
	    try {
	    	Map<String, Object> param = new HashMap<String, Object>();
	    	
	    	param.put("div", type);
	    	
			DateTimeFormatter fm = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
			WeekFields weekFields = WeekFields.of(Locale.UK); //월요일을 주차의 시작으로, 1/1이 월요일이 아니라면 주차의 남은 일수는 전년도에 편입
			// 각 집계의 실행일자가 다음날일 것을 고려하여 통계 조건에 해당되는 파라미터를 minus 1 처리
			if (type.equals("YS")) {
				date = date.minusYears(1);
				param.put("year", date.getYear());
			} else if (type.equals("MS")) {
				date = date.minusMonths(1);
				param.put("year", date.getYear());
				param.put("month", date.getMonthValue());
			} else if (type.equals("WS")) {
				date = date.minusWeeks(1);
				param.put("year", date.getYear());
				param.put("week", date.get(weekFields.weekOfWeekBasedYear()));
				if (date.get(weekFields.weekOfWeekBasedYear())==52 || date.get(weekFields.weekOfWeekBasedYear())==53) {
					param.put("isLastWeek", "Y");
					param.put("plusYear", date.plusYears(1).getYear());
				} else {
					param.put("isLastWeek", "N");
				}
			} else if (type.equals("DS")) {
				date = date.minusDays(1);
				param.put("year", date.getYear());
				param.put("month", date.getMonthValue());
				param.put("week", date.get(weekFields.weekOfWeekBasedYear()));
				param.put("day", date.getDayOfMonth());
			} else if (type.equals("HS")) {
				date = date.minusHours(1);
				param.put("year", date.getYear());
				param.put("month", date.getMonthValue());
				param.put("week", date.get(weekFields.weekOfWeekBasedYear()));
				param.put("day", date.getDayOfMonth());
				param.put("hour", date.getHour());
			}
			
			List<String> dicType = Arrays.asList("ISSUE", "INTEREST");
			if (type.equals("TS")) {
				
				date = date.minusMinutes(10);
				param.put("year", date.getYear());
				param.put("month", date.getMonthValue());
				param.put("week", date.get(weekFields.weekOfWeekBasedYear()));
				param.put("day", date.getDayOfMonth());
				param.put("hour", date.getHour());
				param.put("minute", date.getMinute());
				param.put("startTime", date.format(fm)+"00");
				param.put("endTime", date.plusMinutes(10).format(fm)+"00");
				
				logger.info("@@Batch Param 1_1 : " + param);

				// 금칙어 계수/집계 쿼리 각각 실행
				for (int i=0; i<2; i++) {
					param.put("dicType", dicType.get(i));
					result = batchMapper.stopwordTotal(param);
				}
				// 키워드, 유형/감성분석, 상담사별 감성분석, 금칙어 집계 실행
				result = batchMapper.keywordTotal(param);
				result = batchMapper.claSentTotal(param);
		        result = batchMapper.agentSentTotal(param);
		        
		        result = batchMapper.updateTotalStatus(param); // 400400 기본단위 집계 완료
				
			} else if (type.equals("HS") || type.equals("DS") || type.equals("WS") || type.equals("MS") || type.equals("YS")) {

				logger.info("@@Batch Param 1_2 : " + param);
				
				// 키워드, 유형/감성분석, 상담사별 감성분석, 금칙어 집계 실행
				result = batchMapper.keywordTotalInTable(param);
				result = batchMapper.claSentTotalInTable(param);
		        result = batchMapper.agentSentTotalInTable(param);
		        
		        for (int i=0; i<2; i++) {
					param.put("dicType", dicType.get(i));
					result = batchMapper.stopwordTotalInTable(param);
				}
		        
			} else {
				return result;
			}
	        
	    } catch (NullPointerException e) {
	        logger.error(e.getMessage());
	        e.printStackTrace();
	    } catch (Exception e) {
	        logger.error(e.getMessage());
	        e.printStackTrace();
	    }
	    
	    return result;
	}
	
	// 집계 업데이트 실행
	public int updateTotal () {
	    int result = -1;
	    try {
	    	Map<String, Object> param = new HashMap<String, Object>();
	    	List<Map<String, Object>> minList = new ArrayList<Map<String, Object>>();
	    	List<String> hourList = new ArrayList<String>();
	    	List<String> dicType = Arrays.asList("ISSUE", "INTEREST");
	    	
	    	// todo :: 실행일이 익일이기 떄문에 하루 이전으로 처리하는게 맞는지 확인
	    	// 임시 - 당일데이터로
	    	LocalDateTime date = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.of(0, 0, 0));
//	    	LocalDateTime date = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0, 0));
			DateTimeFormatter fm = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
			WeekFields weekFields = WeekFields.of(Locale.UK); //월요일을 주차의 시작으로, 1/1이 월요일이 아니라면 주차의 남은 일수는 전년도에 편입
			
			param.put("year", date.getYear());
	    	param.put("month", date.getMonthValue());
			param.put("week", date.get(weekFields.weekOfWeekBasedYear()));
	    	param.put("day", date.getDayOfMonth());
			
			for (int i = 0; i < (24 * 6); i++) {
				Map<String, Object> paramMap = new HashMap<String, Object>();
				
				paramMap.put("startTime", date.format(fm));
				paramMap.put("hour", date.getHour());
				paramMap.put("minute", date.getMinute());
				
				date = date.plusMinutes(10);
				paramMap.put("endTime", date.format(fm));
				
				minList.add(paramMap);
			}
			for (int i = 0; i < 24; i++) {
				hourList.add(String.valueOf(i));
			}
			
			param.put("minList", minList);
			param.put("hourList", hourList);
			
			// 기존 실시간 집계데이터 당일치 삭제
			result = batchMapper.keywordDelete(param);
			result = batchMapper.clsSentDelete(param);
			result = batchMapper.agentSentDelete(param);
			result = batchMapper.stopwordDelete(param);
			
			// 10분단위 insert
			param.put("div", "TS");
			result = batchMapper.keywordTotalUpMin(param);
			result = batchMapper.claSentTotalUpMin(param);
	        result = batchMapper.agentSentTotalUpMin(param);
	        for (int i = 0; i < 2; i++) {
				param.put("dicType", dicType.get(i));
				result = batchMapper.stopwordTotalUpMin(param);
			}
	        
	        // 시간단위 insert
	        param.put("div", "HS");
 			result = batchMapper.keywordTotalUpHour(param);
 			result = batchMapper.claSentTotalUpHour(param);
 	        result = batchMapper.agentSentTotalUpHour(param);
 	        for (int i = 0; i < 2; i++) {
				param.put("dicType", dicType.get(i));
				result = batchMapper.stopwordTotalUpHour(param);
			}
 	        // 일단위 insert
 	        result = dataTotal("DS", date);
	        
	    } catch (NullPointerException e) {
	        logger.error(e.getMessage());
	        e.printStackTrace();
	    } catch (Exception e) {
	        logger.error(e.getMessage());
	        e.printStackTrace();
	    }
	    
	    return result;
	}
	
	// 금칙어 업데이트 실행
	public int updateStopword () {
	    int result = -1;
	    try {
	    	Map<String, Object> param = new HashMap<String, Object>();
	    	
	    	LocalDateTime date = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0, 0));
			DateTimeFormatter fm = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
			List<String> dicType = Arrays.asList("ISSUE", "INTEREST");
			
			param.put("startTime", date.minusDays(1).format(fm));
	    	param.put("endTime", date.format(fm));
	    	
			// 기존 실시간 집계데이터 당일치 삭제
			result = stopwordMapper.deleteStopword(param);
			
			// insert
			for (int i = 0; i < 2; i++) {
				param.put("dicType", dicType.get(i));
				result = stopwordMapper.doStopword(param);
			}
	        
	    } catch (NullPointerException e) {
	        logger.error(e.getMessage());
	        e.printStackTrace();
	    } catch (Exception e) {
	        logger.error(e.getMessage());
	        e.printStackTrace();
	    }
	    
	    return result;
	}
	
	// 반복민원 고객ID 메시지 발송
	public int sendMsg() {
		int result = -1;
		
		try {
			// 반복민원 데이터 조회
			Map<String, Object> resultMap = batchMapper.getSendMsgInfo();
			
			if (resultMap != null) {
				URI uri = UriComponentsBuilder.fromUriString(sendMsgUrl).build().toUri();
				
				// Header 설정
	 			HttpHeaders headers = new HttpHeaders();
	 			headers.setContentType(MediaType.APPLICATION_JSON);
	 			
	 			// body 설정
	 			JsonParser parser = new JsonParser();
	 			JsonObject body = new JsonObject();
	 			body.add("rcvIds", (JsonArray)parser.parse(resultMap.get("USER_ID_LIST").toString()));
	 			body.addProperty("sndName", "WISENUT");
	 			body.addProperty("subject", "[TA] 반복민원 알림");
	 			
	 			LocalDateTime date = LocalDateTime.now();
	 			DateTimeFormatter fm = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
	 			
	 			StringBuffer content = new StringBuffer();
	 			content.append(date.format(fm)).append(" 고객ID ")
	 										   .append(resultMap.get("CUST_ID_LIST").toString())
	 										   .append("총 ")
	 										   .append(resultMap.get("CUST_ID_CNT").toString())
	 										   .append("건의 반복민원 알림이 발생했습니다.");
	 			
	 			body.addProperty("content", content.toString());
	 			body.addProperty("sysCode", "IPCTAT11");
	 			body.addProperty("sysTitle", "IPCTAT11");
	 			
	 			logger.debug("@@ json : " + body);
	 			
	 			HttpEntity<?> entity = new HttpEntity<>(body.toString(), headers);
	 			
	 			// API Call (Connection, Read Time을 2초로 설정)
	 			HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
				factory.setConnectTimeout(2 * 1000);
				factory.setReadTimeout(2 * 1000);	
				
				// Rest API 호출
	 			RestTemplate restTemplate = new RestTemplate(factory);
	 			ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);	
				
	 			JsonObject resultJson = (JsonObject) parser.parse(responseEntity.getBody());
	 			
	 			if (resultJson.get("code").getAsString().equals("SUCCESS")) {
	 				result = 0;
	 			} else {
	 				result = -2;
	 			}
			} else {
				result = -3;
			}
		} catch (ResourceAccessException e) {
			logger.error(e.getMessage());
	        e.printStackTrace();
		} catch (Exception e) {
			logger.error(e.getMessage());
	        e.printStackTrace();
		}
		
		return result;
	}
	
	// EDW 분석결과 데이터 발송
	public int sendEdw() {
		int result = -1;
		
		try {
			// 분석결과 데이터 조회
			List<Map<String, Object>> resultList = batchMapper.getSendEdwList();
			
			Gson gson = new Gson();
			JsonParser parser = new JsonParser();
			JsonArray resultJsonArray = new JsonArray();
			
			if (resultList.size() > 0) {
				Map<String, Object> tempMap = null;
				JsonObject tempJson = null;
				JsonArray tempArray = null;
				
				// List<Map> -> JsonArray 변환
				for (int i = 0; i < resultList.size(); i++) {
					tempMap = resultList.get(i);
					tempJson = (JsonObject) parser.parse(gson.toJson(tempMap));
					
					// Json String -> Json Array 변환
					tempArray = (JsonArray) parser.parse(tempJson.get("rsltSw").getAsString());
					tempJson.add("rsltSw", tempArray);
					tempArray = (JsonArray) parser.parse(tempJson.get("summString").getAsString());
					tempJson.add("summString", tempArray);
					tempArray = (JsonArray) parser.parse(tempJson.get("rsltKw").getAsString());
					tempJson.add("rsltKw", tempArray);
					
					resultJsonArray.add(tempJson);
				}
				
				// 파일작성
	 			LocalDateTime date = LocalDateTime.now();
	 			DateTimeFormatter fm = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
	 			String fileName = "TA_ANA_" + date.format(fm) + ".json";
	 			Path path = Paths.get(edwPath + "/" + fileName);
				
				gson = new GsonBuilder().setPrettyPrinting().create();
				
				FileOutputStream fileOutputStream = new FileOutputStream(path.toString());
				OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "EUC-KR");
				BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
				
//				BufferedWriter bufferedWriter = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
				bufferedWriter.append(gson.toJson(resultJsonArray));
				bufferedWriter.flush();
				
				// EDW 쉘 스크립트 실행
				// 1) 명령어 생성
	 	        StringBuffer shellCommand = new StringBuffer();
	 	        shellCommand.append("sh /sw/eai/batch_agent/bin/batch.sh -i TASMTSBF0001 -f ");
	 	        shellCommand.append(fileName);
	 	        shellCommand.append(" -t ");
	 	        shellCommand.append(fileName);

	 	        logger.info("@@ pipe command : {}", shellCommand.toString());
	 	        
	 	        String [] command = {"/bin/sh","-c", shellCommand.toString()};
	 	        
	 	        // 2) 명령어 실행을 위한 ProcessBuilder 객체 생성
	 	        ProcessBuilder processBuilder = new ProcessBuilder();
	 	        processBuilder.command(command);
	 	        
	 	        // 3) 실행
	 	        Process process = processBuilder.start();
	 	        
	 	        // 4) 실행결과 확인
	 	        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

	 	        String line;
	 	        StringBuffer log = new StringBuffer();
	 	        while((line = reader.readLine()) != null) {
	 	        	log.append(line);
	 	        	
	 	        	// 처리결과 체크하기 - Batch Agent Exit[0]이면 성공한 것으로 확인
	 	        	if (line.indexOf("Batch Agent Exit") > -1) {
	 	        		if (line.indexOf("[0]") > -1) {
	 	        			result = 0;
	 	        			break;
	 	        		}
	 	        	}
	 	        	process.waitFor();
	 	        }
				
	 	        logger.info("@@ EDW Execute Log\n" + log.toString());
	 	        
	 	        // 처리결과 확인 (데이터 확인을 위하여 삭제로직 Hold)
	 	        if (result == 0) {
	 	        	if (profile.equals("prod")) {
		 	        	// 성공이면 생성한 json파일 삭제
		 	        	// 1) 명령어 재정의
		 	        	shellCommand = new StringBuffer();
		 	        	shellCommand.append("rm -rf ");
		 	        	shellCommand.append(path);
		 	        	
		 	        	command[2] = shellCommand.toString();
		 	        	
		 	        	// 2) ProcessBuilder 재정의 및 실행
			 	        processBuilder = new ProcessBuilder();
			 	        processBuilder.command(command);
			 	        
			 	        // 3) 실행
			 	        process = processBuilder.start();
	 	        	}
	 	        } else {
	 	        	result = -2;
	 	        }
			} else {
				result = -3;
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
	        e.printStackTrace();
		}
		
		return result;
	}
	
	// 세션테이블 고객정보 업데이트
	public int updateCustInfo() {
		int result = 0;
		
		try {
			Map<String, Object> param = new HashMap<String, Object>();
			
			// startTime, endTime 세팅 확인 (현재 시간 초단위 절삭/10분 내 데이터)
			LocalDateTime date = LocalDateTime.now();
			DateTimeFormatter fm = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
			
			String endTime = date.format(fm)+"00";
			String startTime = date.minusMinutes(10).format(fm)+"00";
			param.put("startTime", startTime);
			param.put("endTime", endTime);
			
			// 고객정보 Update(Merge)
			result = batchMapper.mergeCustInfo(param);
			
		} catch (Exception e) {
			e.printStackTrace();
			result = -1;
		}
		return result;
	}
	
	// 2년 지난 로그삭제
	public int deleteLog() {
		return batchMapper.deleteLog();
	}
	
	// 5년 지난 분석결과 및 통계 데이터 삭제
	public void deleteAnalyzeAndStatisticsData() {
		batchMapper.deleteAnalyzeData();
		batchMapper.deleteStatClassData();
		batchMapper.deleteStatSentimentData();
		batchMapper.deleteStatKeywordData();
		batchMapper.deleteStatStopWordData();
	}
}
