package kr.co.wisenut.textminer.common.controller;

import org.apache.groovy.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import kr.co.wisenut.exception.StorageFileNotFoundException;
import kr.co.wisenut.exception.TMResourceInAccessibleException;
import kr.co.wisenut.exception.TMResourceNotFoundException;
import kr.co.wisenut.textminer.collection.service.CollectionService;
import kr.co.wisenut.textminer.collection.service.DocumentService;
import kr.co.wisenut.textminer.collection.vo.CollectionVo;
import kr.co.wisenut.textminer.common.resource.StorageResourceType;
import kr.co.wisenut.textminer.common.service.StorageService;
import kr.co.wisenut.textminer.common.vo.ImportProgressVo;
import kr.co.wisenut.textminer.dictionary.service.DictionaryService;
import kr.co.wisenut.textminer.dictionary.vo.DictionaryVo;
import kr.co.wisenut.textminer.user.vo.TmUser;


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
@RequestMapping("/file/export/{resourceType:collection|dictionary}/{resourceId:\\d+}")
public class ExportFileRestController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Autowired
    private StorageService storageService;
    
    @Autowired
    private CollectionService collectionService;
    
    @Autowired
    private DictionaryService dictionaryService;
    
    @Autowired
    private DocumentService documentService;

    /**
     * 해당 리소스에 수정 권한이 있는지 확인
     *
     * @param type
     * @param resourceId
     * @param user
     */
    void checkResourceAuthority(StorageResourceType type, Long resourceId, TmUser user)
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

    @GetMapping("/{filename:.+}")
    public ResponseEntity download(@PathVariable String resourceType,
                                   @PathVariable Long resourceId,
                                   @PathVariable String filename,
                                   @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
        final StorageResourceType type = StorageResourceType.valueOf(resourceType.toUpperCase());

        // TODO(wisnt65) 파일 다운로드도 권한체크 필요
        //checkResourceAuthority(type, resourceId, user.getUsername());

        Resource file = storageService.getExportedFile(type, resourceId, filename);
        logger.info("exported-file served. {}-{}/{}", type, resourceId, file.getFilename());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"\"")
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(file);
    }

    @DeleteMapping(value = "/{filename:.+}")
    public ResponseEntity<?> delete(@PathVariable String resourceType,
                                    @PathVariable Long resourceId,
                                    @PathVariable String filename,
                                    @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
        final StorageResourceType type = StorageResourceType.valueOf(resourceType.toUpperCase());

        checkResourceAuthority(type, resourceId, user);

        // 해당 파일 삭제
        storageService.deleteUploadedFile(type, resourceId, filename);

        logger.info("exported-file deleted. {}-{}/{}", resourceType, resourceId, filename);
        return ResponseEntity
                .ok(Maps.of("deleteResult", Maps.of("resourceType", type, "resourceId", resourceId, "deletedFilename", filename)));
    }

    @DeleteMapping(value = "/")
    public ResponseEntity<?> deleteAll(@PathVariable String resourceType,
                                       @PathVariable Long resourceId,
                                       @AuthenticationPrincipal(errorOnInvalidType = true) TmUser user) {
        final StorageResourceType type = StorageResourceType.valueOf(resourceType.toUpperCase());

        checkResourceAuthority(type, resourceId, user);

        storageService.deleteUploadDirectory(type, resourceId);

        logger.info("exported-file deleted. {}-{}", resourceType, resourceId);
        return ResponseEntity.ok(Maps.of("deleteAllResult", Maps.of("resourceType", type, "resourceId", resourceId)));
    }


    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        logger.error("exported-file not found. {}", exc);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exc.getMessage());
    }
}
