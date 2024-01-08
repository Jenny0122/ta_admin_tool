package kr.co.wisenut.textminer.resource.service;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import kr.co.wisenut.config.TMProperties;
import kr.co.wisenut.textminer.resource.mapper.ResourceMapper;

@Service
public class ResourceService{
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private ResourceMapper resourceMapper;
	
	public ResponseEntity<?> testConnection(TMProperties tmProperties) {
		
		try {
			URI uri = UriComponentsBuilder
					 .fromUriString("http://" + tmProperties.getCoreHost() + ":" + tmProperties.getCorePort())
					 .build()
					 .toUri();
			
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, null, String.class);

			return ResponseEntity.ok("TM-Server Connection succed.");
			
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			// 통신은 되었지만 404 에러가 떴을 경우의 예외처리
			logger.warn("@@ tm-server connection success, but failed to handle request. {} {}: {}", e.getStatusCode(), e.getStatusText(), e.getMessage());
			
			return ResponseEntity.ok("TM-Server Connection succed.");
		} catch (ResourceAccessException e) {
			logger.error("Failed to connect. {}.", e.getMessage());
			
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
		}
	}	
	
	// 리소스 조회
	public ResponseEntity<?> getResourceInfo(TMProperties tmProperties) {
		
		try {
			URI uri = UriComponentsBuilder
					 .fromUriString("http://" + tmProperties.getCoreHost() + ":" + tmProperties.getCorePort() + "/api/resourceCheck")
					 .build()
					 .toUri();
			
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, null, String.class);

			JsonParser parser = new JsonParser();
			JsonObject resourceList = (JsonObject) parser.parse(responseEntity.getBody());
			JsonArray resourceArray = resourceList.get("resourceList").getAsJsonArray();
			
			Map<String, Object> resourceMap = new HashMap<String, Object>();
			Map<String, Object> tempMap = null;
			
			// 리턴받은 결과에 대하여 리턴할 Map 설정
			for (int i = 0; i < resourceArray.size(); i++) {
				tempMap = new HashMap<String, Object>();
				tempMap.put("total", resourceArray.get(i).getAsJsonObject().get("total").getAsDouble());
				tempMap.put("usage", resourceArray.get(i).getAsJsonObject().get("usage").getAsDouble());
				tempMap.put("used", resourceArray.get(i).getAsJsonObject().get("used").getAsDouble());
				tempMap.put("ghz", resourceArray.get(i).getAsJsonObject().get("ghz").getAsString());
				
				resourceMap.put(resourceArray.get(i).getAsJsonObject().get("name").getAsString(), tempMap);
			}
			
			return ResponseEntity.status(HttpStatus.OK).body(resourceMap);
		} catch (ResourceAccessException e) {
			logger.error("Failed to connect. {}.", e.getMessage());
			
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
		}
	}
	
	// 프로젝트 현황 조회
	public ResponseEntity<?> getProjectStatus() {
		
		try {
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap = resourceMapper.getProjectStatus();
			
			return ResponseEntity.status(HttpStatus.OK).body(resultMap);
		} catch (ResourceAccessException e) {
			logger.error("Failed to connect. {}.", e.getMessage());
			
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
		}
	}
}
