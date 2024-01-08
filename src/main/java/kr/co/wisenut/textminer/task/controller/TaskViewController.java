package kr.co.wisenut.textminer.task.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.co.wisenut.textminer.project.service.ProjectService;
import kr.co.wisenut.textminer.task.service.TaskService;
import kr.co.wisenut.textminer.user.vo.TmUser;


@Controller
@RequestMapping("/task")
public class TaskViewController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private TaskService taskService;
    
    @GetMapping
    public String projects(Model model, @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {

    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("role", user.getAuthorities().toString());
    	paramMap.put("projectOwner", user.getUsername());
    	
    	model.addAttribute("projects", projectService.getProjectListForSelectBox(paramMap));
    	
        return "project/task";
    }
    
    @GetMapping("/{projectId:\\d+}")
    public String projects( Model model
    		, @PathVariable("projectId") String projectId
    		, @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
    	
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("role", user.getAuthorities().toString());
    	paramMap.put("projectOwner", user.getUsername());
    	
    	// 프로젝트 리스트 가져오기
    	model.addAttribute("projects", projectService.getProjectListForSelectBox(paramMap));
    	
    	// 서비스 구분 가져오기
    	model.addAttribute("projectId", projectId);
    	
    	return "project/task";
    }
    
    @GetMapping("/{projectId:\\d+}/{taskType}/{taskId:\\d+}")
    public String projects( Model model
    		, @PathVariable("projectId") String projectId
    		, @PathVariable("taskType") String taskType
    		, @PathVariable("taskId") String taskId
    		, @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
    	
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("role", user.getAuthorities().toString());
    	paramMap.put("projectOwner", user.getUsername());
    	
    	// 프로젝트 리스트 가져오기
    	model.addAttribute("projects", projectService.getProjectListForSelectBox(paramMap));
    	
    	// 서비스 구분 가져오기
    	paramMap.put("projectId", projectId);
    	paramMap.put("taskType", taskType);
    	model.addAttribute("modelList", taskService.getModelList(paramMap).get("modelList"));
    	model.addAttribute("projectId", projectId);
    	model.addAttribute("taskType", taskType);
    	model.addAttribute("taskId", taskId);
    	
    	return "project/task";
    }
}

