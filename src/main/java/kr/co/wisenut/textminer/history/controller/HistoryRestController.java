package kr.co.wisenut.textminer.history.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.co.wisenut.textminer.history.service.ActionHistoryService;
import kr.co.wisenut.textminer.user.vo.TmUser;

@RestController
@RequestMapping("/historyRest")
public class HistoryRestController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private ActionHistoryService actionHistoryService;
	
	@PostMapping("/getActionHistoryList")
	public Map<String, Object> getActionhistoryList( @RequestBody Map<String, Object> paramMap
			  									   , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			paramMap.put("role", user.getAuthorities().toString());
			paramMap.put("actionUser", user.getUsername());
			
			resultMap = actionHistoryService.getActionHistoryList(paramMap);
		} catch (Exception e) {
			logger.error("tm-user getActionHistoryList failed. {}", e.getMessage());
		}
		
		return resultMap;
	}
}
