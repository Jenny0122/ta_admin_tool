package kr.co.wisenut.textminer.collection.controller;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.co.wisenut.textminer.collection.service.CollectionService;
import kr.co.wisenut.textminer.collection.service.DocumentService;
import kr.co.wisenut.textminer.collection.vo.CollectionVo;
import kr.co.wisenut.textminer.common.TextMinerConstants;
import kr.co.wisenut.textminer.common.service.ImportProgressService;
import kr.co.wisenut.textminer.common.vo.ImportProgressVo;
import kr.co.wisenut.textminer.history.service.ActionHistoryService;
import kr.co.wisenut.textminer.history.vo.ActionHistoryVo;
import kr.co.wisenut.textminer.user.vo.TmUser;

@RestController
@RequestMapping("/collectionRest")
public class CollectionRestController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private CollectionService collectionService;
	
	@Autowired
	private ImportProgressService importProgressService;

	@Autowired
	private ActionHistoryService actionHistoryService;
	
	// 컬렉션 리스트 조회
	@PostMapping("/getDataList")
	public Map<String, Object> getDataList( @RequestBody Map<String, Object> paramMap
										  , HttpServletRequest request
										  , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
    	
		try {
			paramMap.put("role", user.getAuthorities().toString());		// 사용자 권한
			paramMap.put("collectionOwner", user.getUsername());
			paramMap.put("contextPath", request.getContextPath());
			
			resultMap = collectionService.getCollectionList(paramMap);
		} catch (Exception e) {
			logger.error("tm-user getDataList failed. {}", e.getMessage());
		}

		return resultMap;
	}
	
	// 컬렉션 리스트 조회
	@PostMapping("/getAnalyzeCollections")
	public Map<String, Object> getAnalyzeCollections( @RequestBody Map<String, Object> paramMap
										  , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
    	
		try {
			paramMap.put("role", user.getAuthorities().toString());		// 사용자 권한
			paramMap.put("collectionOwner", user.getUsername());
			paramMap.put("collectionJob", TextMinerConstants.COLLECTION_JOB_ANALYSIS);
			
			List<CollectionVo> collectionList = collectionService.getCollectionNames(paramMap);
			resultMap.put("analyzeCollection", collectionList);
			
		} catch (Exception e) {
			logger.error("tm-user getDataList failed. {}", e.getMessage());
		}

		return resultMap;
	}
	
	// 컬렉션 상세 조회
	@GetMapping("/getDataDetail")
	public CollectionVo getDataList( @RequestParam int collectionId
								   , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {

		CollectionVo collectionVo = new CollectionVo();
		collectionVo.setCollectionId(collectionId);
		collectionVo.setRole(user.getAuthorities().toString());
		collectionVo.setCollectionOwner(user.getUsername());
		
		try {
			collectionVo = collectionService.getCollectionDetail(collectionVo);
		} catch (Exception e) {
			logger.error("tm-user getDataList failed. {}", e.getMessage());
		}
		
		return collectionVo;
	}
		
	// 컬렉션 등록
	@RequestMapping(value="/insertCollection")
	public Map<String, Object> insertCollection( CollectionVo collectionVo
											   , HttpServletRequest request
			 								   , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			collectionVo.setCollectionOwner(user.getUsername());
			collectionVo.setCreUser(user.getUsername());
			collectionVo.setModUser(user.getUsername());
			
			resultMap = collectionService.insertCollection(collectionVo);
			
			// 등록 작업 후 이력 저장
			if (resultMap.get("result").toString().equals("S")) {
				ActionHistoryVo actionHistoryVo = new ActionHistoryVo();
	
		        actionHistoryVo.setActionUser(user.getUsername());
		        actionHistoryVo.setResourceId("0");
		        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
	        	actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_COLLECTION_INSERT);
		        actionHistoryVo.setActionMsg(user.getUsername() + " 컬렉션 등록 ( 컬렉션 명 : " + collectionVo.getCollectionName() + " )");
		        
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
	
	// 컬렉션 수정
	@RequestMapping(value="/updateCollection")
	public Map<String, Object> updateCollection( CollectionVo collectionVo
			   								   , HttpServletRequest request
											   , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			collectionVo.setCollectionOwner(user.getUsername());
			collectionVo.setCreUser(user.getUsername());
			collectionVo.setModUser(user.getUsername());
			collectionVo.setRole(user.getAuthorities().toString());		// 사용자 권한
			
			resultMap = collectionService.updateCollection(collectionVo);
			
			// 변경 작업 후 이력 저장
			if (resultMap.get("result").toString().equals("S")) {
				ActionHistoryVo actionHistoryVo = new ActionHistoryVo();
	
		        actionHistoryVo.setActionUser(user.getUsername());
		        actionHistoryVo.setResourceId("0");
		        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
	        	actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_COLLECTION_UPDATE);
		        actionHistoryVo.setActionMsg(user.getUsername() + " 컬렉션 정보 변경 ( 대상 컬렉션 명 : " + collectionVo.getCollectionName() + " )");
		        
		        if (request.getRemoteAddr().equals("0:0:0:0:0:0:0:1")) {
		        	actionHistoryVo.setUserIp("127.0.0.1");
		        } else {
		        	actionHistoryVo.setUserIp(request.getRemoteAddr());
		        }
		        
		        actionHistoryService.insertActionHistory(actionHistoryVo);
			}
		} catch (Exception e) {
			logger.error("tm-user updateCollection failed. {}", e.getMessage());
		}
		
		return resultMap;
	}

	// 컬렉션 삭제
	@GetMapping("/deleteCollection")
	public Map<String, Object> deleteCollection( @RequestParam int collectionId
			   								   , HttpServletRequest request
			 								   , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		CollectionVo collectionVo = new CollectionVo();
		collectionVo.setRole(user.getAuthorities().toString());
		collectionVo.setCollectionId(collectionId);
		collectionVo.setCollectionOwner(user.getUsername());
		collectionVo = collectionService.getCollectionDetail(collectionVo);
		
		try {
			resultMap = collectionService.deleteCollection(collectionVo);

			// 변경 작업 후 이력 저장
			if (resultMap.get("result").toString().equals("S")) {
				ActionHistoryVo actionHistoryVo = new ActionHistoryVo();
	
		        actionHistoryVo.setActionUser(user.getUsername());
		        actionHistoryVo.setResourceId("0");
		        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
	        	actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_COLLECTION_DELETE);
		        actionHistoryVo.setActionMsg(user.getUsername() + " 컬렉션 삭제 ( 대상 컬렉션 명 : " + collectionVo.getCollectionName() + " )");
		        
		        if (request.getRemoteAddr().equals("0:0:0:0:0:0:0:1")) {
		        	actionHistoryVo.setUserIp("127.0.0.1");
		        } else {
		        	actionHistoryVo.setUserIp(request.getRemoteAddr());
		        }
		        
		        actionHistoryService.insertActionHistory(actionHistoryVo);
			}
		} catch (Exception e) {
			logger.error("tm-user deleteCollection failed. {}", e.getMessage());
		}

		return resultMap;
	}
	
    @GetMapping("/importHistory")
    public ResponseEntity importHistory( @RequestParam int collectionId
                                       , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
    	
		ImportProgressVo importProgressVo = new ImportProgressVo();
		importProgressVo.setResourceId(collectionId);
		importProgressVo.setResourceType(TextMinerConstants.PROGRESS_TYPE_COLLECTION);
		importProgressVo.setProgress(TextMinerConstants.PROGRESS_STATUS_FAILURE);
		
		List<ImportProgressVo> history = importProgressService.getFileList(importProgressVo);

        return ResponseEntity.ok(history);
    }
	
	// 파일 업로드 이력 조회
	@GetMapping(value = "/importHistory", params = "fileName")
	public Map<String, Object> importHistory( @RequestParam int collectionId
									   , @RequestParam String fileName
									   , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
	
		Map<String, Object> result = new HashMap<String, Object>();
		
		ImportProgressVo importProgressVo = new ImportProgressVo();
		importProgressVo.setFileName(fileName);
		importProgressVo.setResourceId(collectionId);
		importProgressVo.setResourceType(TextMinerConstants.PROGRESS_TYPE_COLLECTION);
		importProgressVo.setProgress(TextMinerConstants.PROGRESS_STATUS_FAILURE);
		
		List<ImportProgressVo> history = importProgressService.getFileList(importProgressVo);
		
		if (history.size() == 0) {
			result.put("result", "S");
		} else {
			result.put("result", "F");
		}
		
		return result;
	}
	
	// 파일 업로드 팝업
	@GetMapping(value = "/importHistoryData", params = {"importId", "fileName"})
	public ResponseEntity importHistoryData( @RequestParam int collectionId
			, @RequestParam int importId
			, @RequestParam String fileName
			, @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
		
		Map<String, Object> result = new HashMap<String, Object>();
		
		ImportProgressVo importProgressVo = new ImportProgressVo();
		importProgressVo.setImportId(importId);
		importProgressVo.setFileName(fileName);
		importProgressVo.setResourceId(collectionId);
		importProgressVo.setResourceType(TextMinerConstants.PROGRESS_TYPE_COLLECTION);
		importProgressVo.setProgress(TextMinerConstants.PROGRESS_STATUS_FAILURE);
		
		ImportProgressVo history = importProgressService.getImportProgressDetail(importProgressVo);
		
		return ResponseEntity.ok(history);
	}
	
	@GetMapping(value = "/{collectionId:\\d+}/importHistoryLogs", params = {"importId", "fileName"})
    public ResponseEntity importHistoryLogDownload(@PathVariable int collectionId,
										    		@RequestParam(value = "importId", required = true) int importId,
                                                   @RequestParam(value = "fileName", required = true) String fileName,
                                                   @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
		
		ImportProgressVo importProgressVo = new ImportProgressVo();
		importProgressVo.setImportId(importId);
		importProgressVo.setFileName(fileName);
		importProgressVo.setResourceId(collectionId);
		importProgressVo.setResourceType(TextMinerConstants.PROGRESS_TYPE_COLLECTION);
		importProgressVo = importProgressService.getImportProgressDetail(importProgressVo);
		
        List<String> logs = Arrays.asList(importProgressVo.getLogText());

        logger.info("@@ logs : " + logs.toString());
        
        return ResponseEntity.ok()
                .header("Content-Disposition", String.format("attachment; filename=\"collection-%d-%d-log.txt\"", collectionId, importProgressVo.getImportId()))
                .body(logs.stream().collect(Collectors.joining("\n")));
    }
}
