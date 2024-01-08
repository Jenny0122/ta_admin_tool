package kr.co.wisenut.textminer.dictionary.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.co.wisenut.textminer.common.TextMinerConstants;
import kr.co.wisenut.textminer.common.service.ImportProgressService;
import kr.co.wisenut.textminer.common.vo.ImportProgressVo;
import kr.co.wisenut.textminer.dictionary.service.DictionaryService;
import kr.co.wisenut.textminer.dictionary.vo.DictionaryVo;
import kr.co.wisenut.textminer.history.service.ActionHistoryService;
import kr.co.wisenut.textminer.history.vo.ActionHistoryVo;
import kr.co.wisenut.textminer.user.vo.TmUser;

@RestController
@RequestMapping("/dictionaryRest")
public class DictionaryRestController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private DictionaryService dictionaryService;
	
	@Autowired
	private ImportProgressService importProgressService;
	
	@Autowired
	private ActionHistoryService actionHistoryService;
	
	// 사전 리스트 조회
	@PostMapping("/getDataList")
	public Map<String, Object> getDataList( @RequestBody Map<String, Object> paramMap
										  , HttpServletRequest request
										  , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
    	
		try {
			paramMap.put("role", user.getAuthorities().toString());
			paramMap.put("dictionaryOwner", user.getUsername());
			paramMap.put("contextPath", request.getContextPath());
			
			resultMap = dictionaryService.getDictionaryList(paramMap);
		} catch (Exception e) {
			logger.error("tm-user getDataList failed. {}", e.getMessage());
		}

		return resultMap;
	}
	
	// 사전 상세 조회
	@GetMapping("/getDataDetail")
	public DictionaryVo getDataList( @RequestParam int dictionaryId
								   , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {

		DictionaryVo dictionaryVo = new DictionaryVo();
		dictionaryVo.setDictionaryId(dictionaryId);
		dictionaryVo.setRole(user.getAuthorities().toString());
		dictionaryVo.setDictionaryOwner(user.getUsername());
		
		try {
			dictionaryVo = dictionaryService.getDictionaryDetail(dictionaryVo);
		} catch (Exception e) {
			logger.error("tm-user getDataList failed. {}", e.getMessage());
		}
		
		return dictionaryVo;
	}
	
	// 사전 등록
	@RequestMapping(value="/insertDictionary")
	public Map<String, Object> insertDictionary( DictionaryVo dictionaryVo
											   , HttpServletRequest request
			 								   , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			dictionaryVo.setDictionaryOwner(user.getUsername());
			dictionaryVo.setCreUser(user.getUsername());
			dictionaryVo.setModUser(user.getUsername());
			
			resultMap = dictionaryService.insertDictionary(dictionaryVo);
			
			// 등록 완료 후 이력 저장
			if (resultMap.get("result").toString().equals("S")) {
				ActionHistoryVo actionHistoryVo = new ActionHistoryVo();
	
		        actionHistoryVo.setActionUser(user.getUsername());
		        actionHistoryVo.setResourceId("0");
		        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
		        actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_DICTIONARY_INSERT);
		        actionHistoryVo.setActionMsg(user.getUsername() + " 사전 등록 ( 등록 사전 : " + dictionaryVo.getDictionaryName() + " )");

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
	
	// 사전 수정
	@RequestMapping(value="/updateDictionary")
	public Map<String, Object> updateDictionary( DictionaryVo dictionaryVo
			   								   , HttpServletRequest request
											   , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			dictionaryVo.setDictionaryOwner(user.getUsername());
			dictionaryVo.setCreUser(user.getUsername());
			dictionaryVo.setModUser(user.getUsername());
			dictionaryVo.setRole(user.getAuthorities().toString());
			
			resultMap = dictionaryService.updateDictionary(dictionaryVo);
			
			// 변경 완료 후 이력 저장
			if (resultMap.get("result").toString().equals("S")) {
				ActionHistoryVo actionHistoryVo = new ActionHistoryVo();
	
		        actionHistoryVo.setActionUser(user.getUsername());
		        actionHistoryVo.setResourceId("0");
		        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
		        actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_DICTIONARY_UPDATE);
		        actionHistoryVo.setActionMsg(user.getUsername() + " 사전 정보 변경 ( 변경대상 사전 : " + dictionaryVo.getDictionaryName() + " )");

		        if (request.getRemoteAddr().equals("0:0:0:0:0:0:0:1")) {
		        	actionHistoryVo.setUserIp("127.0.0.1");
		        } else {
		        	actionHistoryVo.setUserIp(request.getRemoteAddr());
		        }
		        
		        actionHistoryService.insertActionHistory(actionHistoryVo);
			}
		} catch (Exception e) {
			logger.error("tm-user updateDictionary failed. {}", e.getMessage());
		}
		
		return resultMap;
	}

	// 사전 삭제
	@GetMapping("/deleteDictionary")
	public Map<String, Object> deleteDictionary( @RequestParam int dictionaryId
											   , HttpServletRequest request
			 								   , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		DictionaryVo dictionaryVo = new DictionaryVo();
		dictionaryVo.setDictionaryId(dictionaryId);
		dictionaryVo.setRole(user.getAuthorities().toString());
		dictionaryVo.setDictionaryOwner(user.getUsername());
		dictionaryVo = dictionaryService.getDictionaryDetail(dictionaryVo);
		
		try {
			resultMap = dictionaryService.deleteDictionary(dictionaryVo);
			
			// 삭제 완료 후 이력 저장
			if (resultMap.get("result").toString().equals("S")) {
				ActionHistoryVo actionHistoryVo = new ActionHistoryVo();
	
		        actionHistoryVo.setActionUser(user.getUsername());
		        actionHistoryVo.setResourceId("0");
		        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
		        actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_DICTIONARY_DELETE);
		        actionHistoryVo.setActionMsg(user.getUsername() + " 사전 삭제 ( 삭제 대상 사전 : " + dictionaryVo.getDictionaryName() + " )");

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
	
	// 업로드 이력 조회
	@GetMapping(value = "/importHistory", params = "fileName")
    public Map<String, Object> importHistory(@RequestParam int dictionaryId,
                                        @RequestParam(value = "fileName", required = true) String fileName,
                                        @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
		
		Map<String, Object> result = new HashMap<String, Object>();
		
		ImportProgressVo importProgressVo = new ImportProgressVo();
		importProgressVo.setFileName(fileName);
		importProgressVo.setResourceId(dictionaryId);
		importProgressVo.setResourceType(TextMinerConstants.PROGRESS_TYPE_DICTIONARY);
		importProgressVo.setProgress(TextMinerConstants.PROGRESS_STATUS_FAILURE);
		
		List<ImportProgressVo> history = importProgressService.getFileList(importProgressVo);
		
		if (history.size() == 0) {
			result.put("result", "S");
		} else {
			result.put("result", "F");
		}

        return result;
    }
	
	// 업로드 이력 팝업
	@GetMapping(value = "/importHistoryData", params = {"importId", "fileName"})
	public ResponseEntity importHistoryData( @RequestParam int dictionaryId
			, @RequestParam int importId
			, @RequestParam String fileName
			, @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
		
		Map<String, Object> result = new HashMap<String, Object>();
		
		ImportProgressVo importProgressVo = new ImportProgressVo();
		importProgressVo.setImportId(importId);
		importProgressVo.setFileName(fileName);
		importProgressVo.setResourceId(dictionaryId);
		importProgressVo.setResourceType(TextMinerConstants.PROGRESS_TYPE_DICTIONARY);
		importProgressVo.setProgress(TextMinerConstants.PROGRESS_STATUS_FAILURE);
		
		ImportProgressVo history = importProgressService.getImportProgressDetail(importProgressVo);
		
		return ResponseEntity.ok(history);
	}
	
	@GetMapping(value = "/{dictionaryId:\\d+}/importHistoryLogs", params = {"importId", "fileName"})
    public ResponseEntity importHistoryLogDownload(@PathVariable int dictionaryId,
    											   @RequestParam(value = "importId", required = true) int importId,
                                                   @RequestParam(value = "fileName", required = true) String fileName,
                                                   @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
		
		ImportProgressVo importProgressVo = new ImportProgressVo();
		importProgressVo.setImportId(importId);
		importProgressVo.setFileName(fileName);
		importProgressVo.setResourceId(dictionaryId);
		importProgressVo.setResourceType(TextMinerConstants.PROGRESS_TYPE_DICTIONARY);
		importProgressVo = importProgressService.getImportProgressDetail(importProgressVo);
		
        List<String> logs = Arrays.asList(importProgressVo.getLogText());

        return ResponseEntity.ok()
                .header("Content-Disposition", String.format("attachment; filename=\"dictionary-%d-%s-log.txt\"", dictionaryId, importProgressVo.getImportId()))
                .body(logs.stream().collect(Collectors.joining("\n")));
        
    }
}
