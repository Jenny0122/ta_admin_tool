package kr.co.wisenut.textminer.project.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.co.wisenut.textminer.project.service.ProjectService;
import kr.co.wisenut.textminer.user.vo.TmUser;


@Controller
@RequestMapping("/project")
public class ProjectViewController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private ProjectService projectService;
    
    @GetMapping
    public String projects(Model model, @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {

    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("role", user.getAuthorities().toString());
    	paramMap.put("projectOwner", user.getUsername());
    	
    	model.addAttribute("projects", projectService.getProjectListForSelectBox(paramMap));
    	
        return "project/projects";
    }
}

