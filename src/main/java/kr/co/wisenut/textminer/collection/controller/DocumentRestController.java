package kr.co.wisenut.textminer.collection.controller;

import java.net.URI;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.groovy.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
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
import kr.co.wisenut.textminer.common.resource.ExportProgress;
import kr.co.wisenut.textminer.common.resource.FileType;
import kr.co.wisenut.textminer.common.resource.ImportErrorHandle;
import kr.co.wisenut.textminer.common.resource.StorageResourceType;
import kr.co.wisenut.textminer.common.service.StorageService;
import kr.co.wisenut.textminer.common.vo.ImportProgressVo;
import kr.co.wisenut.textminer.history.service.ActionHistoryService;
import kr.co.wisenut.textminer.history.vo.ActionHistoryVo;
import kr.co.wisenut.textminer.user.vo.TmUser;

@RestController
@RequestMapping("/documentRest")
public class DocumentRestController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	StorageService storageService;
	
	@Autowired
	private CollectionService collectionService;

	@Autowired
	private DocumentService documentService;
	
	@Autowired
	private ActionHistoryService actionHistoryService;

	// 문서 리스트 조회
	@PostMapping("/getDataList")
	public Map<String, Object> getDataList( @RequestBody Map<String, Object> paramMap
										  , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
    	
		try {
			paramMap.put("pageSize", 10);
			paramMap.put("role", user.getAuthorities().toString());
			paramMap.put("collectionOwner", user.getUsername());
			
			resultMap = documentService.getDocumentList(paramMap);
		} catch (Exception e) {
			logger.error("tm-user getDataList failed. {}", e.getMessage());
		}

		return resultMap;
	}
	
	// 문서 상세조회
	@PostMapping("/getDataDetail")
	public Map<String, Object> getDataDetail( @RequestBody Map<String, Object> paramMap
											, @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			paramMap.put("role", user.getAuthorities().toString());
			paramMap.put("collectionOwner", user.getUsername());
			paramMap.put("pageSize", 10);
			
			resultMap = documentService.getDocumentDetail(paramMap);
		} catch (Exception e) {
			logger.error("tm-user getDataDetail failed. {}", e.getMessage());
		}
		
		return resultMap;
	}
	
	// 미분류 데이터 존재여부 체크
	@PostMapping("/getNoClsDateCheck")
	public Map<String, Object> getNoClsDateCheck(@RequestParam String dateRange) {
		
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		
		String [] date = dateRange.split(" - ");
		paramMap.put("startDate", date[0].replaceAll("\\.", ""));
		paramMap.put("endDate", date[1].replaceAll("\\.", ""));
		
		int count = documentService.getNoneClassifiedDataCnt(paramMap);
		
		if (count > 0) {
			resultMap.put("result", "S");
			resultMap.put("resultMsg", "다운로드 가능");
		} else {
			resultMap.put("result", "F");
			resultMap.put("resultMsg", "해당 기간에 대한 미분류 데이터가 존재하지 않습니다.");
		}
		
		return resultMap;
	}
	
	@PostMapping(value = "imports", params = "errorHandle")
    public ResponseEntity importAsync(HttpServletRequest request,
    								  @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user,
                                      @RequestParam("errorHandle") ImportErrorHandle errorHandle,
                                      @RequestParam(value = "fieldNames[]", required = false) List<String> fieldNames,
                                      @RequestParam(value = "fieldChecks[]", required = false) List<Boolean> fieldChecks,
                                      @RequestParam("collectionId") Long collectionId) {

		// 접속 IP 추가
        if (request.getRemoteAddr().equals("0:0:0:0:0:0:0:1")) {
        	user.setUserIp("127.0.0.1");
        } else {
        	user.setUserIp(request.getRemoteAddr());
        }
		
		CollectionVo collectionVo = new CollectionVo();
		collectionVo.setCollectionId(Integer.parseInt(String.valueOf(collectionId)));
		collectionVo.setCollectionOwner(user.getUsername());
		collectionVo.setRole(user.getAuthorities().toString());
        collectionVo = collectionService.getCollectionDetail(collectionVo);

        // 최초 업로드일 경우, request param 체크
        if (collectionVo.getFieldInfo() == null || collectionVo.getFieldInfo().trim().equals("")) {
            // if DOCID not include
            if (fieldNames == null || fieldNames.isEmpty() || fieldChecks == null || fieldChecks.isEmpty() || !fieldChecks.contains(true)) {
                return ResponseEntity.badRequest().body("fieldNames ars required.");
            }

            // if DOCID not checked,
            int docidIndex = fieldNames.indexOf("docid");
            if (fieldChecks == null || docidIndex == -1 || !fieldChecks.get(docidIndex)) {
                return ResponseEntity.badRequest().body(String.format("'%s' field is required.", "docid"));
            }

            if (fieldNames.size() != fieldChecks.size()) {
                return ResponseEntity.badRequest().body("Length mismatch between fieldNames fieldCheck lists.");
            }
        } else if (!collectionVo.getFieldInfo().contains("docid")) {
            logger.error("Invalid collection.fieldInfo. Must have to include docid. {}", collectionVo);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Failed to import document, because invalid collection.fieldInfo(" + collectionVo.getFieldInfo() + "). Must have to include docid.");
        }

        // FIXME : move into doc-service
        List<Path> uploadedFiles = storageService.listUploadedFiles(StorageResourceType.COLLECTION, collectionId);
        
        // start async task
        documentService.asyncImportAllFile(user, collectionId, errorHandle, fieldNames, fieldChecks, uploadedFiles)
		                .addCallback((List<ImportProgressVo> result) -> {
		                    // if success
		                    logger.info("tm-collection document-import complete successfully. {}", result);
		                }, (Throwable ex) -> {
		                    // if failed
		                    logger.error("tm-collection document-import failed. {}", ex.toString());
		                });

        Map<String, Object> links = new HashMap<>();
        links.put("status",
                Maps.of("method", "GET",
                        "href", URI.create(String.format("/documentRest/%d/document/import-status", collectionId)).toString()));

        return ResponseEntity.accepted().body(links);
        
    }
	
	@GetMapping("/{parentId:\\d+}/importStatus")
    public ResponseEntity importStatus(@PathVariable("parentId") Long collectionId,
                                       @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {

        // fixme 직접 접근하면 안될듯 --> service 에 메서드 추가
        List<ImportProgressVo> progresses = DocumentService.IMPORT_PROGRESS.get().get(collectionId);

        // 진행중
        if (progresses != null) {
            return ResponseEntity.accepted().body(progresses);
        }

        return ResponseEntity.ok().location(URI.create("collectionRest/importHistory?id=" + collectionId)).build();
    }
	

	// 컬렉션 수정
	@PostMapping(value="/updateDocument")
	public Map<String, Object> updateDocument( @RequestParam Map<String, Object> paramMap
											 , HttpServletRequest request
											 , @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
		Map<String, Object> paramMapInq = new HashMap<String, Object>();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			paramMapInq.put("collectionId", paramMap.get("collectionId"));
			paramMapInq.put("docId", paramMap.get("docid"));
			
			paramMap.remove("collectionId");
			
			JSONObject json = new JSONObject();
			for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
				json.put(entry.getKey(), entry.getValue());
			}
			
			paramMapInq.put("documentContent", json.toString());

			resultMap = documentService.updateDocument(paramMapInq);
			
			// 변경 작업 후 이력 저장
			CollectionVo collectionVo = new CollectionVo();
			collectionVo.setCollectionId(Integer.parseInt(paramMap.get("collectionId").toString()));
			collectionVo = collectionService.getCollectionDetail(collectionVo);
			
			if (resultMap.get("result").toString().equals("S")) {
				ActionHistoryVo actionHistoryVo = new ActionHistoryVo();
	
		        actionHistoryVo.setActionUser(user.getUsername());
		        actionHistoryVo.setResourceId("0");
		        actionHistoryVo.setResourceType(TextMinerConstants.COMMON_BLANK);
	        	actionHistoryVo.setActionType(TextMinerConstants.ACTION_HISTORY_TYPE_COLLECTION_UPDATE);
		        actionHistoryVo.setActionMsg(user.getUsername() + " 컬렉션 문서 변경 ( 대상 컬렉션 명 : " + collectionVo.getCollectionName() + ", docid : " + paramMap.get("docid").toString() + " )");

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
	
	// 다운로드
	@PostMapping(value = "/{collectionId:\\d+}/exports", params = "format")
    public ResponseEntity exports(@PathVariable("collectionId") int collectionId,
                                  @RequestParam(value = "format", defaultValue = "CSV") FileType format,
                                  @RequestParam(value = "dateRange") String dateRange,
                                  @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user,
                                  HttpServletRequest request) {

		if (collectionId > 0) {
			CollectionVo collectionVo = new CollectionVo();
			collectionVo.setCollectionId(Integer.parseInt(String.valueOf(collectionId)));
			collectionVo.setCollectionOwner(user.getUsername());
			collectionVo.setRole(user.getAuthorities().toString());
	        collectionVo = collectionService.getCollectionDetail(collectionVo);
	
	        if (collectionVo.getDocumentCount() == 0) {
	            return ResponseEntity.noContent().build();
	        }
	
	        if (collectionVo.getFieldInfo() == null || collectionVo.getFieldInfo().isEmpty()) {
	            // TODO: throw new MalformedTMResource, patch fieldInfo? correct collection-schema?based on sampling?
	            if (collectionVo.getDocumentCount() != 0) {
	                logger.warn("Malformed TMCollection! Invalid FieldInfo. {}", collectionVo);
	            }
	            return ResponseEntity.unprocessableEntity().body(collectionVo);
	        }
	
	        // call async-export-job
	        documentService.asyncExportDocuments(user, collectionId, format, user.getUsername(), null);
		        
	        logger.info("tm-coillection document-export requested. {}", collectionVo);
	    	
	        // 보안이슈로 해당 Vo Class 초기화
	        collectionVo = new CollectionVo();
		} else {
			// call async-export-job
	        documentService.asyncExportDocuments(user, collectionId, format, user.getUsername(), dateRange);
	        
	        logger.info("tm-coillection None Classified Data document-export requested.");
		}
		
        // add links : rel=status, rel=stop, rel=download?
        Map<String, Object> links = new HashMap<>();
        links.put("status",
                Maps.of("method", "GET",
                        "href", URI.create(String.format(request.getContextPath() + "/documentRest/%d/exportStatus", collectionId)).toString()));

        return ResponseEntity.accepted().body(links);
    }
	
	@GetMapping(value = "/{collectionId:\\d+}/exportStatus")
    public ResponseEntity exportStatus(@PathVariable("collectionId") long collectionId,
                                       @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user,
                                       HttpServletRequest request) {
        ExportProgress exportProgress = DocumentService.EXPORT_PROGRESS.get().get(collectionId);
        
        if (exportProgress == null) {
            return ResponseEntity.accepted().body("waiting for starting...");
        }

        if (exportProgress.isInProgress()) {
            return ResponseEntity.accepted().body(exportProgress);
        }

        if (exportProgress.isFailed()) {
            return ResponseEntity.noContent().build();
        }

        String exportedFileName = exportProgress.getExportedFileName();
        DocumentService.EXPORT_PROGRESS.get().remove(collectionId);

        return ResponseEntity.created(URI.create(String.format(request.getContextPath() + "/file/export/collection/%d/%s", collectionId, exportedFileName)))
                .body(exportProgress);
    }
}
