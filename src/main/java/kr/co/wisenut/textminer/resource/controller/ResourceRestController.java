package kr.co.wisenut.textminer.resource.controller;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.co.wisenut.config.TMProperties;
import kr.co.wisenut.textminer.resource.service.ResourceService;

@RestController
public class ResourceRestController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	TMProperties tmProperties;
	
	@Autowired
	private ResourceService resourceService;
	
	@GetMapping("/test/tm-server")
	public ResponseEntity<?> testConnection() {
		return resourceService.testConnection(tmProperties);
	}
	
	// 리소스 조회
	@GetMapping("/dashboard/resource")
	public ResponseEntity<?> getResourceUsage() {
		return resourceService.getResourceInfo(tmProperties);
	}
	
	// 프로젝트 현황 조회
	@GetMapping("/dashboard/projectStatus")
	public ResponseEntity<?> getProjectStatus() {
		return resourceService.getProjectStatus();
	}

}
