package kr.co.wisenut.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * tm-v3-manager 의 storage 관련 설정값
 */
@Data
@ConfigurationProperties("storage")
public class TMStorageProperties {

    /**
     * Folder uploadDir for storing files
     */
    String uploadDir = "upload-dir";

    /**
     * Folder downloadDir for serving files
     */
    String downloadDir = "download-dir";

    /**
     * Expiration time (hour) for up&download Files.
     * 스테이징 파일 보존 기한. 기본값 = 24시간
     */
    Long expirationTimeHour = Long.valueOf(24);

}
