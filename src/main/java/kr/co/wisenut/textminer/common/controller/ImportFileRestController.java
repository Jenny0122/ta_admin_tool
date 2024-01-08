package kr.co.wisenut.textminer.common.controller;

import kr.co.wisenut.textminer.collection.service.CollectionService;
import kr.co.wisenut.textminer.collection.vo.CollectionVo;
import kr.co.wisenut.textminer.common.TextMinerConstants;
import kr.co.wisenut.textminer.common.resource.StorageResourceType;
import kr.co.wisenut.textminer.common.service.StorageService;
import kr.co.wisenut.textminer.common.vo.ImportProgressVo;
import kr.co.wisenut.textminer.dictionary.service.DictionaryService;
import kr.co.wisenut.textminer.dictionary.vo.DictionaryVo;
import kr.co.wisenut.textminer.user.vo.TmUser;
import kr.co.wisenut.exception.StorageFileNotFoundException;
import kr.co.wisenut.exception.TMResourceInAccessibleException;
import kr.co.wisenut.exception.TMResourceNotFoundException;
import kr.co.wisenut.textminer.common.vo.ImportProgressVo;
//import kr.wisenut.manager.service.resource.DictionaryService;
//import kr.wisenut.manager.service.resource.DocumentService;
import org.apache.groovy.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;


/**
 * Import 관련 File API Controller.
 * TMResource 로 import 하기 위한 file 관리.
 * <ul>
 * <li>[HTTP method] : GET POST DELETE</li>
 * <li>{resourceType} : collection({@link kr.wisenut.manager.model.Collection}, dictionary({@link kr.wisenut.manager.model.Dictionary}</li>
 * <li>{resourceId} : resource's id(key)</li>
 * <li>?{filename} : {@code unique} target file name </li>
 * </ul>
 */
@RestController
@RequestMapping("/file/import/{resourceType:collection|dictionary}/{resourceId}")
public class ImportFileRestController {
	
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Autowired
    StorageService storageService;
    
    @Autowired
    private CollectionService collectionService;
    
    @Autowired
    private DictionaryService dictionaryService;
    
//    @Autowired
//    DictionaryService dictionaryService;
//    
//    @Autowired
//    DocumentService documentService;

    /**
     * 해당 리소스에 수정 권한이 있는지 확인
     *
     * @param type
     * @param resourceId
     * @param user
     */
    void checkResourceAuthority(StorageResourceType type, long resourceId, TmUser user)
            throws TMResourceNotFoundException, TMResourceInAccessibleException {
        ImportProgressVo resource = null;
        switch (type) {
            case COLLECTION:
            	// 권한있는 데이터 조회
            	CollectionVo collectionVo = new CollectionVo();
            	collectionVo.setCollectionId(Integer.parseInt(String.valueOf(resourceId)));
            	collectionVo.setCollectionOwner(user.getUsername());
            	collectionVo.setRole(user.getAuthorities().toString());
            	
                resource = collectionService.getCollectionDetailForImportProgress(collectionVo);
                break;
            case DICTIONARY:
            	// 권한있는 데이터 조회
            	DictionaryVo dictionaryVo = new DictionaryVo();
            	dictionaryVo.setDictionaryId(Integer.parseInt(String.valueOf(resourceId)));
            	dictionaryVo.setDictionaryOwner(user.getUsername());
            	dictionaryVo.setRole(user.getAuthorities().toString());
            	
                resource = dictionaryService.getDictionaryDetailForImportProgress(dictionaryVo);
                break;
//            case PROJECT:
            default:
                logger.warn("Not Implemented for Project File Handle");
                break;
        }
    }

    @PostMapping(value = "/", consumes = "multipart/form-data")
    public ResponseEntity upload( @PathVariable String resourceType
    							, @PathVariable long resourceId
    							, @RequestParam("file") MultipartFile multipartFile
    							, @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user ) {
    	
    	
    	
        // 업로드 가능한 파일 확장자 체크
    	String ext = StringUtils.getFilenameExtension(multipartFile.getOriginalFilename());
        switch (resourceType.toUpperCase()) {
	        case TextMinerConstants.PROGRESS_TYPE_COLLECTION:
	        	// Collection은 json과 csv만 업로드 가능
	        	if (!ext.equals("json") && !ext.equals("csv")) {
	        		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("문서 업로드 시 확장자가 " + ext + "인 파일은 업로드 할 수 없습니다.");
	        	}
	        	break;
	        case TextMinerConstants.PROGRESS_TYPE_DICTIONARY:
	        	// Dictionary는 txt와 csv만 업로드 가능
	        	if (!ext.equals("txt") && !ext.equals("csv")) {
	        		 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("엔트리 업로드 시 확장자가 " + ext + "인 파일은 업로드 할 수 없습니다.");
	        	}
	        	break;
        }
    	
        final StorageResourceType type = StorageResourceType.valueOf(resourceType.toUpperCase());

        checkResourceAuthority(type, resourceId, user);

        storageService.storeUploadFile(type, resourceId, multipartFile);

        logger.info("upload-file stored. {}-{}/{} {} bytes", type, resourceId, multipartFile.getOriginalFilename(), multipartFile.getSize());

        //fixme defect(wisnt65) ms edge 에서 파일 업로드시 mulpartFile.getOriginalFilename() 에 파일이름 뿐만아니라 루트경로까지의 절대경로가 노출됨.
        return ResponseEntity
                .created(URI.create("file/import/" + resourceType + "/" + resourceId + "/" + multipartFile.getOriginalFilename().replace(" ", "%20")))
                .body(Maps.of("resourceType", type, "resourceId", resourceId, "filename", multipartFile.getOriginalFilename(), "size", multipartFile.getSize()));
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity download(@PathVariable String resourceType,
                                   @PathVariable long resourceId,
                                   @PathVariable String filename,
                                   @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user
    ) {
        final StorageResourceType type = StorageResourceType.valueOf(resourceType.toUpperCase());

        checkResourceAuthority(type, resourceId, user);

        Resource file = storageService.getUploadedFile(type, resourceId, filename);

        logger.info("upload-file served. {}-{}/{}", type, resourceId, file.getFilename());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    @DeleteMapping(value = "/{filename:.+}")
    public ResponseEntity<?> delete(@PathVariable String resourceType,
                                    @PathVariable long resourceId,
                                    @PathVariable String filename,
                                    @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
        final StorageResourceType type = StorageResourceType.valueOf(resourceType.toUpperCase());

        checkResourceAuthority(type, resourceId, user);

        // 해당 파일 삭제
        storageService.deleteUploadedFile(type, resourceId, filename);

        logger.info("upload-file deleted. {}-{}/{}", resourceType, resourceId, filename);
        return ResponseEntity
                .ok(Maps.of("deleteResult", Maps.of("resourceType", type, "resourceId", resourceId, "deletedFilename", filename)));
    }

    @DeleteMapping(value = "/")
    public ResponseEntity<?> deleteAll(@PathVariable String resourceType,
                                       @PathVariable long resourceId,
                                       @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
        final StorageResourceType type = StorageResourceType.valueOf(resourceType.toUpperCase());
        
        checkResourceAuthority(type, resourceId, user);

        storageService.deleteUploadDirectory(type, resourceId);

        logger.info("upload-file deleted. {}-{}", resourceType, resourceId);
        return ResponseEntity.ok(Maps.of("deleteAllResult", Maps.of("resourceType", type, "resourceId", resourceId)));
    }


    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        logger.error("upload-file not found. {}", exc);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exc.getMessage());
    }
}
