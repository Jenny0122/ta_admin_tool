package kr.co.wisenut.textminer.autoqa.controller;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.tomcat.util.json.ParseException;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Sort;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kr.co.wisenut.textminer.autoqa.service.AutoQAService;
import kr.co.wisenut.textminer.autoqa.vo.AutoQaScriptVo;
import kr.co.wisenut.textminer.collection.service.CollectionService;
import kr.co.wisenut.textminer.collection.vo.CollectionVo;
import kr.co.wisenut.textminer.common.TextMinerConstants;
import kr.co.wisenut.textminer.common.resource.StagingFileInfo;
import kr.co.wisenut.textminer.common.resource.StorageResourceType;
import kr.co.wisenut.textminer.common.service.DocumentParserService;
import kr.co.wisenut.textminer.common.service.ImportProgressService;
import kr.co.wisenut.textminer.common.service.StorageService;
import kr.co.wisenut.textminer.common.vo.ImportProgressVo;
import kr.co.wisenut.textminer.dictionary.service.DictionaryService;
import kr.co.wisenut.textminer.dictionary.vo.DictionaryVo;
import kr.co.wisenut.textminer.history.vo.ActionHistoryVo;
import kr.co.wisenut.textminer.history.service.ActionHistoryService;
import kr.co.wisenut.config.TMProperties;
import kr.co.wisenut.exception.StorageException;
import kr.co.wisenut.exception.StorageFileNotFoundException;
import kr.co.wisenut.textminer.user.service.UserService;
import kr.co.wisenut.textminer.user.vo.TmUser;

@Controller
@RequestMapping("/autoqa")
public class AutoQAViewController {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private ImportProgressService importProgressService;
	
	@Autowired
	private MultipartProperties multipartProperties;
	
	@Autowired
	private DocumentParserService documentParser;
	
	@Autowired
	private StorageService storageService;
	
	@Autowired
	private ActionHistoryService actionHistoryService;
	
	@Autowired
	private AutoQAService autoQAService;
	
	// 스크립트 정보 화면
    @GetMapping
    public String autoqa( Model model, @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
    	
        return "autoqa/autoqaIB";
        
    }
    
 // 엔트리 조회 (사전 ID 사용안함)
    @GetMapping("/autoqaOB")
    public String autoqaOB( Model model
    					   , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
			
    	return "autoqa/autoqaOB";
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
   
}
