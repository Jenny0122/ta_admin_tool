package kr.co.wisenut.textminer.dictionary.controller;

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
import kr.co.wisenut.config.TMProperties;
import kr.co.wisenut.exception.StorageException;
import kr.co.wisenut.exception.StorageFileNotFoundException;
import kr.co.wisenut.textminer.user.service.UserService;
import kr.co.wisenut.textminer.user.vo.TmUser;

@Controller
@RequestMapping("/dictionary")
public class DictionaryViewController {
	
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
	private DictionaryService dictionaryService;
	
	// 사전 정보 화면
    @GetMapping
    public String dictionary( Model model, @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
    	
        return "dictionary/dictionaries";
        
    }
    
    // 업로드 화면
    @GetMapping("/upload")
    public String upload( Model model
    					, @RequestParam int dictionaryId
    					, @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
    	
    	// 1. 업로드 대상 사전 정보 추가
    	DictionaryVo dictionaryVo = new DictionaryVo();
    	dictionaryVo.setDictionaryId(dictionaryId);
    	dictionaryVo.setDictionaryOwner(user.getUsername());
    	dictionaryVo.setRole(user.getAuthorities().toString());
    	
    	model.addAttribute("dictionary", dictionaryService.getDictionaryDetail(dictionaryVo));		
    	
    	// 2. staging file 리스트
    	List<StagingFileInfo> stagingFiles = (List<StagingFileInfo>) storageService
    										 .loadAllAsStagingFile(StorageResourceType.DICTIONARY
    												 			 , Long.parseLong(String.valueOf(dictionaryId)));
    	
    	model.addAttribute("stagingFiles", stagingFiles);
    	
    	// 3. 사전 업로드 정보 추가
    	ImportProgressVo importProgressVo = new ImportProgressVo();
    	importProgressVo.setResourceId(dictionaryId);
    	importProgressVo.setResourceType(TextMinerConstants.PROGRESS_TYPE_DICTIONARY);
    	
    	model.addAttribute("importHistory", importProgressService.getFileList(importProgressVo));
    	
    	// 4. 단일 파일의 최대 크기 to bytes 
    	model.addAttribute("maxFileSize", multipartProperties.getMaxFileSize().toBytes());
    	
    	return "dictionary/upload";
    	
    }

    
    // 엔트리 조회 (사전 ID 사용안함)
    @GetMapping("/entries")
    public String documents( Model model
    					   , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
		
    	Map<String, Object> paramMap = new HashMap<String, Object>();
	
		paramMap.put("role", user.getAuthorities().toString());		// 로그인 사용자의 권한
		paramMap.put("DictionaryOwner", user.getUsername());		// 로그인 아이디
		
		model.addAttribute("dictionaryId", 0);
		model.addAttribute("dictionaries", dictionaryService.getDictionaryNames(paramMap));
		
    	return "dictionary/entries";
    }
    
    // 엔트리 조회 (사전 ID 사용)
    @GetMapping("/{dictionaryId:\\d+}/entries")
    public String documents( Model model
				    	   , @PathVariable int dictionaryId
				    	   , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
    	
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	
    	paramMap.put("role", user.getAuthorities().toString());		// 로그인 사용자의 권한
    	paramMap.put("DictionaryOwner", user.getUsername());		// 로그인 아이디
    	
    	model.addAttribute("dictionaryId", dictionaryId);
    	model.addAttribute("dictionaries", dictionaryService.getDictionaryNames(paramMap));
    	
    	return "dictionary/entries";
    }
}
