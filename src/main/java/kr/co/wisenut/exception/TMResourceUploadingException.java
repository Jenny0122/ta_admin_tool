package kr.co.wisenut.exception;

import kr.co.wisenut.textminer.common.vo.ImportProgressVo;

/**
 * 해당 리소스가 업로드 중인 경우
 */

public class TMResourceUploadingException extends TMResourceException {
    public TMResourceUploadingException(ImportProgressVo resource) {
        super(String.format("%s(id=%s) is uploading by other process..", resource.getClass().getSimpleName(), resource.getImportId()), resource);
    }

    public TMResourceUploadingException(Class<?> classTMResource, long id) {
        super(String.format("%s(id=%s) is uploading by other process..", classTMResource, id), classTMResource, id);
    }
}
