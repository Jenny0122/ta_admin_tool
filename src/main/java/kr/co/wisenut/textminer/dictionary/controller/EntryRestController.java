package kr.co.wisenut.textminer.dictionary.controller;

import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.groovy.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import kr.co.wisenut.textminer.collection.service.DocumentService;
import kr.co.wisenut.textminer.collection.vo.CollectionVo;
import kr.co.wisenut.textminer.common.TextMinerConstants;
import kr.co.wisenut.textminer.common.resource.ExportProgress;
import kr.co.wisenut.textminer.common.resource.FileType;
import kr.co.wisenut.textminer.common.resource.ImportErrorHandle;
import kr.co.wisenut.textminer.common.resource.StorageResourceType;
import kr.co.wisenut.textminer.common.service.ImportProgressService;
import kr.co.wisenut.textminer.common.service.StorageService;
import kr.co.wisenut.textminer.common.vo.ImportProgressVo;
import kr.co.wisenut.textminer.dictionary.service.DictionaryService;
import kr.co.wisenut.textminer.dictionary.service.EntryService;
import kr.co.wisenut.textminer.dictionary.vo.DictionaryVo;
import kr.co.wisenut.textminer.history.service.ActionHistoryService;
import kr.co.wisenut.textminer.history.vo.ActionHistoryVo;
import kr.co.wisenut.textminer.user.vo.TmUser;

@RestController
@RequestMapping("/entryRest")
public class EntryRestController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	StorageService storageService;
	
	@Autowired
	private DictionaryService dictionaryService;
	
	@Autowired
	private ActionHistoryService actionHistoryService;
	
	@Autowired
	private EntryService entryService;
	
	/**
     * 전체 파일 임포트 - 동기.
     *
     * @param dictionaryId
     * @param user
     * @param errorHandle
     * @param requestParams
     * @return
     */
    @PostMapping("/{dictionaryId}/entry/import")
    public ResponseEntity imports(@PathVariable("dictionaryId") Long dictionaryId,
    							  HttpServletRequest request,
                                  @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user,
                                  @RequestParam("errorHandle") ImportErrorHandle errorHandle,
                                  @RequestParam Map<String, Object> requestParams) {
        
        if (request.getRemoteAddr().equals("0:0:0:0:0:0:0:1")) {
        	user.setUserIp("127.0.0.1");
        } else {
        	user.setUserIp(request.getRemoteAddr());
        }
    	
        DictionaryVo dictionary = new DictionaryVo();
        dictionary.setDictionaryId(Integer.parseInt(String.valueOf(dictionaryId)));
        dictionary.setDictionaryOwner(user.getUsername());
        dictionary.setRole(user.getAuthorities().toString());
        dictionary = dictionaryService.getDictionaryDetail(dictionary);

        List<Path> uploadedFiles = storageService.listUploadedFiles(StorageResourceType.DICTIONARY, dictionaryId);

        // success: 엔트리 업로드 성공 수, failure: 엔트리 업로드 실패 수
        LinkedHashMap<String, Map> totalResult = entryService.importAllFile(dictionary, errorHandle, uploadedFiles, user);

        if (logger.isTraceEnabled()) {
//            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            logger.trace("request({} {}, remoteHost={}, username={})", request.getMethod(), request.getRequestURI(), request.getRemoteHost(), user.getUsername());
        }
        logger.info("tm-dictionary entry-import requested. dictionaryId={}, {}", dictionaryId, uploadedFiles);

        return ResponseEntity.accepted().body(totalResult);
    }
    
    /* 엔트리 조회 */
    @PostMapping("/getDataList")
    public Map<String, Object> getDataList( @RequestBody Map<String, Object> paramMap
			  							  , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
    	
    	Map<String, Object> resultMap = new HashMap<String, Object>();
    	
		try {
			paramMap.put("role", user.getAuthorities().toString());
			paramMap.put("dictionaryOwner", user.getUsername());
			
			resultMap = entryService.getEntryList(paramMap);
		} catch (Exception e) {
			logger.error("tm-user getDataList failed. {}", e.getMessage());
		}

		return resultMap;
    }
    
    /* 엔트리 중복 조회 */
    @PostMapping(value = "/{dictionaryId}/chkDuplicateEntry", params = "entry") 
    public ResponseEntity chkDuplicateEntry( @PathVariable("dictionaryId") int dictionaryId
									 , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user
									 , @RequestParam("entry") String entry) {
    	Map<String, Object> resultMap = new HashMap<String, Object>();

    	resultMap = entryService.chkDuplicateEntry(dictionaryId, entry);
    	
        return ResponseEntity.ok(resultMap);
    }
        
    /* 엔트리 등록 */
    @PostMapping(value = "/{dictionaryId}/insertEntry", params = "entry")
    public ResponseEntity insertEntry( @PathVariable("dictionaryId") int dictionaryId
									 , HttpServletRequest request
            						 , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user
            						 , @RequestParam("entry") String entry) {
    	Map<String, Object> resultMap = new HashMap<String, Object>();
    	
		try {
			resultMap = entryService.insertEntry(dictionaryId, entry);
			
			// 등록 작업 후 이력 저장
			
			if (resultMap.get("result").toString().equals("S")) {
				DictionaryVo dictionaryVo = new DictionaryVo();
				dictionaryVo.setDictionaryId(dictionaryId);
				dictionaryVo.setRole(user.getAuthorities().toString());
				dictionaryVo.setDictionaryOwner(user.getUsername());
				dictionaryVo = dictionaryService.getDictionaryDetail(dictionaryVo);

				ActionHistoryVo actionHistoryVo = new ActionHistoryVo();
	
		        actionHistoryVo.setActionUser(user.getUsername());
		        actionHistoryVo.setResourceId("0");
		        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
	        	actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_COLLECTION_INSERT);
		        actionHistoryVo.setActionMsg(user.getUsername() + " 엔트리 등록 ( 대상 사전 : " + dictionaryVo.getDictionaryName() + " / 등록 엔트리 : " + entry + " )");

		        if (request.getRemoteAddr().equals("0:0:0:0:0:0:0:1")) {
		        	actionHistoryVo.setUserIp("127.0.0.1");
		        } else {
		        	actionHistoryVo.setUserIp(request.getRemoteAddr());
		        }
		        
		        actionHistoryService.insertActionHistory(actionHistoryVo);
			}
		} catch (Exception e) {
			logger.error("tm-user insertEntry failed. {}", e.getMessage());
		}
    	
        return ResponseEntity.ok(resultMap);
    }
    	
    /* 엔트리 삭제 */
    @PostMapping(value = "/{dictionaryId}/deleteEntry", params = "id[]")
    public ResponseEntity deleteEntry( @PathVariable("dictionaryId") int dictionaryId
									 , HttpServletRequest request
            						 , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user
            						 , @RequestParam("id[]") String... id) {

    	Map<String, Object> resultMap = new HashMap<String, Object>();
    	
		try {
			// 삭제 작업 후 이력 저장
			DictionaryVo dictionaryVo = new DictionaryVo();
			dictionaryVo.setDictionaryId(dictionaryId);
			dictionaryVo.setRole(user.getAuthorities().toString());
			dictionaryVo.setDictionaryOwner(user.getUsername());
			dictionaryVo = dictionaryService.getDictionaryDetail(dictionaryVo);
			
			resultMap = entryService.deleteEntry(dictionaryId, id);
			
			if (resultMap.get("result").toString().equals("S")) {
				ActionHistoryVo actionHistoryVo = new ActionHistoryVo();
	
		        actionHistoryVo.setActionUser(user.getUsername());
		        actionHistoryVo.setResourceId("0");
		        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
	        	actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_ENTRY_DELETE);
		        actionHistoryVo.setActionMsg(user.getUsername() + " 엔트리 삭제 ( 대상 사전 : " + dictionaryVo.getDictionaryName() + " )");

		        if (request.getRemoteAddr().equals("0:0:0:0:0:0:0:1")) {
		        	actionHistoryVo.setUserIp("127.0.0.1");
		        } else {
		        	actionHistoryVo.setUserIp(request.getRemoteAddr());
		        }
		        
		        actionHistoryService.insertActionHistory(actionHistoryVo);
			}
		} catch (Exception e) {
			logger.error("tm-user deleteEntry failed. {}", e.getMessage());
		}
    	
        return ResponseEntity.ok(resultMap);
    }
    
    /* 동의어 변경 */
    @PostMapping("/modifySynonym")
    public Map<String, Object> modifySynonym( @RequestBody Map<String, Object> paramMap
    										, HttpServletRequest request
    										, @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
    	
    	Map<String, Object> resultMap = new HashMap<String, Object>();
    	
    	try {
			resultMap = entryService.modifySynonym(paramMap);
			
			// 동의어 변경 작업 후 이력 저장
			if (resultMap.get("result").toString().equals("S")) {

				// 사전 정보 조회
				DictionaryVo dictionaryVo = new DictionaryVo();
				dictionaryVo.setDictionaryId(Integer.parseInt(paramMap.get("dictionaryId").toString()));
				dictionaryVo.setRole(user.getAuthorities().toString());
				dictionaryVo.setDictionaryOwner(user.getUsername());
				dictionaryVo = dictionaryService.getDictionaryDetail(dictionaryVo);
				
				ActionHistoryVo actionHistoryVo = new ActionHistoryVo();
	
		        actionHistoryVo.setActionUser(user.getUsername());
		        actionHistoryVo.setResourceId("0");
		        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
	        	actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_ENTRY_UPDATE);
		        actionHistoryVo.setActionMsg(user.getUsername() + " 동의어 변경 ( 대상 사전 : " + dictionaryVo.getDictionaryName() + " )");

		        if (request.getRemoteAddr().equals("0:0:0:0:0:0:0:1")) {
		        	actionHistoryVo.setUserIp("127.0.0.1");
		        } else {
		        	actionHistoryVo.setUserIp(request.getRemoteAddr());
		        }
		        
		        actionHistoryService.insertActionHistory(actionHistoryVo);
			}
		} catch (Exception e) {
			logger.error("tm-user deleteEntry failed. {}", e.getMessage());
		}
    	
    	
    	return resultMap;
    }
    
    /**
     * 전체 엔트리 추출 - 동기.
     *
     * @param dictionaryId
     * @param format
     * @param user
     * @return
     */
    @PostMapping(value = "/{dictionaryId:\\d+}/exports", params = "format")
    public ResponseEntity exports(@PathVariable("dictionaryId") Long dictionaryId,
                                  @RequestParam(value = "format", defaultValue = "TEXT") FileType format,
                                  @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user,
                                  HttpServletRequest request) {

        DictionaryVo dictionary = new DictionaryVo();
        dictionary.setDictionaryId(Integer.parseInt(String.valueOf(dictionaryId)));
        dictionary.setDictionaryOwner(user.getUsername());
        dictionary.setRole(user.getAuthorities().toString());
        dictionary = dictionaryService.getDictionaryDetail(dictionary);

        // sync export dictionary's entries.
        ExportProgress<DictionaryVo> exportResult = entryService.exportEntries(user, dictionary, format, user.getUsername());

        if (logger.isTraceEnabled()) {
            HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            logger.trace("request({} {}, remoteHost={}, username={})", servletRequest.getMethod(), servletRequest.getRequestURI(), servletRequest.getRemoteHost(), user.getUsername());
        }
        logger.info("tm-dictionary entry-export requested. {}", dictionary);

        // response the exported file's location to download (GET existing file).
        return ResponseEntity.created(URI.create(String.format(request.getContextPath() + "/file/export/dictionary/%d/%s", dictionaryId, exportResult.getExportedFileName())))
                .build();
    }
}
