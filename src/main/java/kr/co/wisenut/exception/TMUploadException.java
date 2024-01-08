package kr.co.wisenut.exception;

import kr.co.wisenut.textminer.common.resource.StorageResourceType;
import lombok.Data;

/**
 * tm-v3-manager 업로드 ~ 임포트 시 발생하는 예외 정의
 */
@Data
public class TMUploadException extends RuntimeException {

    private final StorageResourceType resourceType;

    private final long resourceId;

    public TMUploadException(String message, StorageResourceType resourceType, long resourceId) {
        super(message);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public TMUploadException(String message, StorageResourceType resourceType, long resourceId, Throwable cause) {
        super(message, cause);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public TMUploadException(StorageResourceType resourceType, long resourceId, Throwable cause) {
        super(cause);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

}
