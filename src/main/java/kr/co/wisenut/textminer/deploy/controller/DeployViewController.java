package kr.co.wisenut.textminer.deploy.controller;

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

import kr.co.wisenut.textminer.deploy.service.DeployService;
import kr.co.wisenut.textminer.user.vo.TmUser;


@Controller
@RequestMapping("/deploy")
public class DeployViewController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    
    @GetMapping
    public String deploy(Model model, @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {

    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("role", user.getAuthorities().toString());
    	paramMap.put("projectOwner", user.getUsername());
    	    	
        return "deploy/deploy";
    }
}