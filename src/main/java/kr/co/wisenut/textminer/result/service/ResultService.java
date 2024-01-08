package kr.co.wisenut.textminer.result.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import kr.co.wisenut.textminer.common.TextMinerConstants;
import kr.co.wisenut.textminer.result.mapper.ResultMapper;
import kr.co.wisenut.util.AesCryptoUtil;

@Service
public class ResultService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ResultMapper resultMapper;
	
	// 결과 데이터 조회
	public JsonArray getResultList(Map<String, Object> paramMap) {

		JsonParser parser = new JsonParser();
		JsonObject tempJson = null;
		JsonArray resultArray = new JsonArray();
		List<Map<String, Object>> result = null;					// 조회결과
		List<JsonObject> sortList = new ArrayList<JsonObject>();	// 정렬 처리를 위한 List
		
		try {
			// 조회결과 리스트
			result = resultMapper.getResultList(paramMap);
			
			if (result.size() > 0) {
				for (int i = 0; i < result.size(); i++) {
					tempJson = (JsonObject)  parser.parse(result.get(i).get("resultJson").toString());
					
					// 암호화된 내용 복호화하기
					String content = tempJson.get("content").getAsString();
					tempJson.addProperty("content", AesCryptoUtil.decryption(content));
					
					// 전처리 결과에 대한 복호화
					if (tempJson.get("resultPreprocess") != null) {
						String resultStr = tempJson.get("resultPreprocess").getAsString();
						tempJson.addProperty("resultPreprocess", AesCryptoUtil.decryption(resultStr));
					}
					
					sortList.add(tempJson);
				}
				
				// JsonArray docid 기준 오름차순 정렬처리
				Collections.sort(sortList, new Comparator<JsonObject>() {

					@Override
					public int compare(JsonObject o1, JsonObject o2) {

						String valA = o1.get("docid").getAsString();
						String valB = o2.get("docid").getAsString();
						
						return valA.compareTo(valB);
					}
				});
				
				for (int i = 0; i < sortList.size(); i++) {
					resultArray.add(sortList.get(i));
				}
			}
		} catch (NullPointerException e) {
			logger.error("조회 시 누락된 값에 의한 오류발생!");
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("조회 작업을 실패하였습니다.");
			e.printStackTrace();
		}

		return resultArray;
	}
	
	// 엑셀 헤더 만들기
	public List<String> createExcelHeader(JsonObject jsonObj, String taskType) {
		
		// 헤더 리스트
		List<String> resultHeaderList = new ArrayList<String>();
		
		if (taskType.equals(TextMinerConstants.TASK_TYPE_AUTO_CLASSIFICATION) 
		 || taskType.equals(TextMinerConstants.TASK_TYPE_EMOTION_ANALYZE)) {
			// 자동분류 : 3 / 감성분석 : 2
			int dataLength = 0;
			if (taskType.equals(TextMinerConstants.TASK_TYPE_AUTO_CLASSIFICATION)) {
				dataLength = 3;
			} else {
				dataLength = 2;
			}
			// 1) 자동분류, 감성분석 헤더 생성
			String jsonText = jsonObj.toString().replace("{", "").replace("}", "");
			String [] elements = jsonText.split(",");
			String field = null;
			
			for (int i = 0; i < elements.length; i++) {
				field = elements[i].split(":")[0].replace("\"", "");
				
				// LABEL, CONFIDENCE, SCORE의 경우, 3개까지 출력할 수 있도록 한다.
				if (field.equals("LABEL") || field.equals("CONFIDENCE") || field.equals("SCORE")) {
					// NO_LABEL 필드 추가
					if (field.equals("LABEL")) {
						resultHeaderList.add(field);
					}
					
					for (int j = 1; j <= dataLength; j++) {
						resultHeaderList.add(field + "_" + j);
					}
				} else {
					resultHeaderList.add(field);
				}
			}
		} else if (taskType.equals(TextMinerConstants.TASK_TYPE_KEYWORD_EXTRACTION)) {
			// 2) 키워드 추출 헤더 생성
			resultHeaderList.add("docid");				// dicid는 고정
			resultHeaderList.add("content");			// content는 고정
			JsonArray resultArray = jsonObj.get("result").getAsJsonArray();
			String jsonText = resultArray.get(0).getAsJsonObject().toString().replace("{", "").replace("}", "");
			String [] elements = jsonText.split(",");

			String field = null;
			
			// 20개까지 나오게 설정
//			for (int i = 1; i <= resultArray.size(); i++) {
			for (int i = 1; i <= 20; i++) {
				for (int j = 0; j < elements.length; j++) {
					field = elements[j].split(":")[0].replace("\"", "");
					
//					if (!field.equals("synonyms") && !field.equals("tag")) {
						resultHeaderList.add(field + "_" + i);
//					}
				}
			}
			
		} else if (taskType.equals(TextMinerConstants.TASK_TYPE_RELATED_EXTRACTION)) {
			// 3) 연관어 추출 헤더 생성
			resultHeaderList.add("docid");				// dicid는 고정
			resultHeaderList.add("content");			// content는 고정
			JsonArray resultArray = jsonObj.get("result").getAsJsonArray();

			for (int i = 1; i <= resultArray.size(); i++) {
				resultHeaderList.add("keyword_" + i);
				resultHeaderList.add("relatedKeyword_" + i);
			}
			
		} else if (taskType.equals(TextMinerConstants.TASK_TYPE_DOCUMENT_SUMMARY)) {
			// 4) 문서요약 헤더 생성
			resultHeaderList.add("docid");				// dicid는 고정
			resultHeaderList.add("content");			// content는 고정
			resultHeaderList.add("summary");
		} else {
			// 5) 전처리 헤더 생성
			resultHeaderList.add("docid");
			resultHeaderList.add("label");
			resultHeaderList.add("content");
			resultHeaderList.add("resultPreprocess");
			resultHeaderList.add("json");				// 업로드용 필드
		}
		
		
		return resultHeaderList;
	}
}
