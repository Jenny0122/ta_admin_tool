package kr.co.wisenut.textminer.collection.controller;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
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
import kr.co.wisenut.textminer.collection.service.DocumentService;
import kr.co.wisenut.textminer.collection.vo.CollectionVo;
import kr.co.wisenut.textminer.common.TextMinerConstants;
import kr.co.wisenut.textminer.common.resource.StagingFileInfo;
import kr.co.wisenut.textminer.common.resource.StorageResourceType;
import kr.co.wisenut.textminer.common.service.DocumentParserService;
import kr.co.wisenut.textminer.common.service.ImportProgressService;
import kr.co.wisenut.textminer.common.service.StorageService;
import kr.co.wisenut.textminer.common.vo.ImportProgressVo;
import kr.co.wisenut.config.TMProperties;
import kr.co.wisenut.exception.StorageException;
import kr.co.wisenut.exception.StorageFileNotFoundException;
import kr.co.wisenut.textminer.user.service.UserService;
import kr.co.wisenut.textminer.user.vo.TmUser;

@Controller
@RequestMapping("/collection")
public class CollectionViewController {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private CollectionService collectionService;
	
	@Autowired
	private ImportProgressService importProgressService;
	
	@Autowired
	private MultipartProperties multipartProperties;
	
	@Autowired
	private DocumentParserService documentParser;
	
	@Autowired
	private StorageService storageService;
	
	// 컬렉션 정보 화면
    @GetMapping
    public String collections( Model model, @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
    	
        return "collection/collections";
    }
    
    // 업로드 화면
    @GetMapping("/upload")
    public String upload( Model model
    					, @RequestParam int collectionId
    					, @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
    	
    	// 1. 업로드 대상 컬렉션 정보 추가
    	CollectionVo collectionVo = new CollectionVo();
    	collectionVo.setCollectionId(collectionId);
    	collectionVo.setCollectionOwner(user.getUsername());
    	collectionVo.setRole(user.getAuthorities().toString());
    	
    	model.addAttribute("collection", collectionService.getCollectionDetail(collectionVo));		
    	
    	// 2. 파일 업로드 정보 추가
    	try {
            // staging file list sort by last-modified-time:ASC
            List<Path> paths = storageService.listUploadedFiles(StorageResourceType.COLLECTION, Long.parseLong(String.valueOf(collectionId)));

            // validate staging file. fixme(wisnt65) extract to storageService
            model.addAttribute("files", paths.stream()
                    .map(path -> new StagingFileInfo(StorageResourceType.COLLECTION, collectionId, path.toFile()))
                    .collect(Collectors.toList()));//name, numofdoc, size

            // get first document that using
            if (!paths.isEmpty()) {
                Path headerFile = paths.get(0);
                Document startDoc = documentParser.findFirst(headerFile);

                logger.info("@@ test : " + headerFile);
                logger.info("@@ startDoc : " + startDoc);
                
                model.addAttribute("headerFile", headerFile);
                model.addAttribute("startDocument", startDoc.entrySet()); // linked HashMap.entrySet
            }

        } catch (StorageException e) {
            // staging file not found
            if (!(e instanceof StorageFileNotFoundException)) {
                logger.warn("failed to listing document files. collectionId={}, {}", collectionId, e.toString());
            }
        }
    	
        model.addAttribute("fieldInfo", collectionVo.getFieldInfo() != null ? collectionVo.getFieldInfo() : Collections.EMPTY_LIST);
    	
    	// 3. 컬렉션에 사용된 업로드 정보 추가
    	ImportProgressVo importProgressVo = new ImportProgressVo();
    	importProgressVo.setResourceId(collectionId);
    	importProgressVo.setResourceType(TextMinerConstants.PROGRESS_TYPE_COLLECTION);
    	
    	model.addAttribute("importHistory", importProgressService.getFileList(importProgressVo));
    	
    	// 4. 단일 파일의 최대 크기 to bytes 
    	model.addAttribute("maxFileSize", multipartProperties.getMaxFileSize().toBytes());
    	
    	return "collection/upload";
    	
    }
    
    // 문서 조회 (컬렉션 ID 사용안함)
    @GetMapping("/documents")
    public String documents( Model model
    					   , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
		
    	Map<String, Object> paramMap = new HashMap<String, Object>();
	
		paramMap.put("role", user.getAuthorities().toString());		// 로그인 사용자의 권한
		paramMap.put("collectionOwner", user.getUsername());		// 로그인 아이디
		
		model.addAttribute("collectionId", 0);
		model.addAttribute("collections", collectionService.getCollectionNames(paramMap));
		
    	return "collection/documents";
    }
    
    // 문서 조회 (컬렉션 ID 사용)
    @GetMapping("/{collectionId:\\d+}/documents")
    public String documents( Model model
				    	   , @PathVariable int collectionId
				    	   , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
    	
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	
    	paramMap.put("role", user.getAuthorities().toString());		// 로그인 사용자의 권한
    	paramMap.put("collectionOwner", user.getUsername());		// 로그인 아이디
    	
    	model.addAttribute("collectionId", collectionId);
    	model.addAttribute("collections", collectionService.getCollectionNames(paramMap));
    	
    	return "collection/documents";
    }
}
