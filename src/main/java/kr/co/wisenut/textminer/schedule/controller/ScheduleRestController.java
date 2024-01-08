package kr.co.wisenut.textminer.schedule.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.co.wisenut.textminer.project.service.ProjectService;
import kr.co.wisenut.textminer.schedule.service.ScheduleService;
import kr.co.wisenut.textminer.schedule.vo.ScheduleVo;
import kr.co.wisenut.textminer.user.vo.TmUser;

@RestController
@RequestMapping("/scheduleRest")
public class ScheduleRestController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private ScheduleService scheduleService;

	// 스케줄 리스트 조회
	@PostMapping("/getScheduleList")
	public Map<String, Object> getScheduleList( ScheduleVo paramMap
											  , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user ){
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			resultMap = scheduleService.getScheduleList(paramMap);
		} catch (Exception e) {
			logger.error("tm-user getScheduleList failed. {}", e.getMessage());
		}
		
		return resultMap; 
	}
}
