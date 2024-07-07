package kr.co.wisenut.textminer.autoqa.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import kr.co.wisenut.textminer.autoqa.service.AutoQAService;
import kr.co.wisenut.textminer.autoqa.vo.AutoQaCateInfoVo;
import kr.co.wisenut.textminer.autoqa.vo.AutoQaScriptVo;
import kr.co.wisenut.textminer.common.TextMinerConstants;
import kr.co.wisenut.textminer.history.service.ActionHistoryService;
import kr.co.wisenut.textminer.history.vo.ActionHistoryVo;
import kr.co.wisenut.textminer.user.vo.TmUser;

@Controller
@RequestMapping("/autoqaRest")
public class AutoQARestController {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ActionHistoryService actionHistoryService;
	
	@Autowired
	private AutoQAService autoQAService;
	
	// 스크립트 정보 화면
    @GetMapping
    public String autoqa( Model model, @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
    	
        return "autoqa/autoqaIB";
        
    }
    
 // 스크립트 리스트 조회
 	@PostMapping("/getQAScriptList")
 	public Map<String, Object> getQAScriptList( @RequestBody Map<String, Object> paramMap
 										  , HttpServletRequest request
 										  , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
 		Map<String, Object> resultMap = new HashMap<String, Object>();
     	
 		try {
 			paramMap.put("pageSize", 10);
 			paramMap.put("role", user.getAuthorities().toString());
 			paramMap.put("contextPath", request.getContextPath());
 			
 			resultMap = autoQAService.getQAScriptList(paramMap);
 		} catch (Exception e) {
 			logger.error("tm-user getDataList failed. {}", e.getMessage());
 		}

 		return resultMap;
 	}

 // 스크립트 등록
 	@RequestMapping(value="/insertQAScript")
 	public Map<String, Object> insertQAScript( AutoQaScriptVo autoqascriptVo
 											   , HttpServletRequest request
 			 								   , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
 		
 		Map<String, Object> resultMap = new HashMap<String, Object>();
 		
 		try { 			
 			autoqascriptVo.setCreUser(user.getUsername());
 			autoqascriptVo.setModUser(user.getUsername());
 			
 			resultMap = autoQAService.insertQAScript(autoqascriptVo);
 			
 			// 등록 완료 후 이력 저장
 			if (resultMap.get("result").toString().equals("S")) {
 				ActionHistoryVo actionHistoryVo = new ActionHistoryVo();
 	
 		        actionHistoryVo.setActionUser(user.getUsername());
 		        actionHistoryVo.setResourceId("0");
 		        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
 		        actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_DICTIONARY_INSERT);
 		        actionHistoryVo.setActionMsg(user.getUsername() + " 스크립트 등록 ( 등록 스크립트 : " + autoqascriptVo.getScriptCont() + " )");

 		        if (request.getRemoteAddr().equals("0:0:0:0:0:0:0:1")) {
 		        	actionHistoryVo.setUserIp("127.0.0.1");
 		        } else {
 		        	actionHistoryVo.setUserIp(request.getRemoteAddr());
 		        }
 		        
 		        actionHistoryService.insertActionHistory(actionHistoryVo);
 			}
 		} catch (Exception e) {
 			logger.error("tm-user insertCollection failed. {}", e.getMessage());
 		}
 		
 		return resultMap;
 	}
    
 // 스크립트 삭제
 	@GetMapping("/deleteQAScript")
 	public Map<String, Object> deleteQAScript( @RequestParam int scriptId
 											   , HttpServletRequest request
 			 								   , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
 		Map<String, Object> resultMap = new HashMap<String, Object>();
 		
 		AutoQaScriptVo autoqascriptVo = new AutoQaScriptVo();
 		autoqascriptVo.setScriptId(scriptId);
 		autoqascriptVo.setRole(user.getAuthorities().toString());
 		
 		try {
 			resultMap = autoQAService.deleteQAScript(autoqascriptVo);
 			
 			// 삭제 완료 후 이력 저장
 			if (resultMap.get("result").toString().equals("S")) {
 				ActionHistoryVo actionHistoryVo = new ActionHistoryVo();
 	
 		        actionHistoryVo.setActionUser(user.getUsername());
 		        actionHistoryVo.setResourceId("0");
 		        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
 		        actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_DICTIONARY_DELETE);
 		        actionHistoryVo.setActionMsg(user.getUsername() + " 사전 삭제 ( 삭제 대상 사전 : " + scriptId + " )");

 		        if (request.getRemoteAddr().equals("0:0:0:0:0:0:0:1")) {
 		        	actionHistoryVo.setUserIp("127.0.0.1");
 		        } else {
 		        	actionHistoryVo.setUserIp(request.getRemoteAddr());
 		        }
 		        
 		        actionHistoryService.insertActionHistory(actionHistoryVo);
 			}
 		} catch (Exception e) {
 			logger.error("tm-user deleteScript failed. {}", e.getMessage());
 		}

 		return resultMap;
 	}
 	
 // 준수항목 가져오기
 	@PostMapping("/getCompCdInfo")
 	public Map<String, Object> getCompCdInfo( @RequestBody Map<String, Object> paramMap
 			   							  , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user ){

 		paramMap.put("role", user.getAuthorities().toString());
 		paramMap.put("collectionOwner", user.getUsername());
 		paramMap.put("dictionaryOwner", user.getUsername());
 		
 		Map<String, Object> resultMap = new HashMap<String, Object>();
 		
 		try {
 			resultMap = autoQAService.getCompCdInfo(paramMap);
 			
 			
 		} catch (Exception e) {
 			logger.error("tm-user getModelList failed. {}", e.getMessage());
 		}
 				
 		return resultMap; 
 	}
 	
 	// 상담카테고리 Depth 구조 조회
 	@PostMapping("/getQACategory")
 	@ResponseBody
 	public List<AutoQaCateInfoVo> getQACategory(@RequestBody Map<String, Object> paramMap, HttpServletRequest request) {
 		return autoQAService.getQACategory(paramMap);
 	}
}
