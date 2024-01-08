package kr.co.wisenut.textminer.common.service;

import kr.co.wisenut.exception.StorageException;
import kr.co.wisenut.textminer.common.resource.FileType;
import kr.co.wisenut.textminer.common.resource.StagingFileInfo;
import kr.co.wisenut.textminer.common.resource.StorageResourceType;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * tm-v3-manager 의 storage servcie
 */
public interface StorageService<K> {
    /**
     * HTTP Request, Response 에서 한글, 띄어쓰기 등을 포함한 파일명을 다루기 위한 escape 작업
     * Spring 에서 한글을 공통처리해줘서 브라우저마다 인코딩 다르게 구현할 필요는 없음.
     * 띄어쓰기 " " 만 %20로 치환해주면 URL 에도 문제없이 사용가능
     *
     * @param filePath
     * @return
     */
    static String escapeFileName(@NotNull Path filePath) {
        try {
            // uri escaped file name : space " " --> %20
            String rawPath = filePath.toUri().getRawPath(); // uri --> path separator == "/"
            String escapeFileName = rawPath.substring(rawPath.lastIndexOf("/") + 1);
            return escapeFileName;
        } catch (ArrayIndexOutOfBoundsException e) {
            return filePath.getFileName().toString().replace(" ", "%20");
        }
    }

    void init();

    /**
     * 보존 기간({@code storage.expirationTimeHour. default=24 Hour})이 만료된 파일 및 경로를 모두 삭제
     */
    void deleteExpired();

    @Deprecated
    Stream<Path> streamUploadedFiles(StorageResourceType resourceType, K tmResourceId) throws StorageException;

    List<Path> listUploadedFiles(StorageResourceType resourceType, K tmResourceId) throws StorageException;

    List<StagingFileInfo> loadAllAsStagingFile(StorageResourceType resourceType, K tmResourceId);

    /**
     * 해당 리소스를 저장
     *
     * @param resourceType
     * @param tmResourceId
     * @param multipartFile
     */
    void storeUploadFile(StorageResourceType resourceType, K tmResourceId, MultipartFile multipartFile) throws StorageException;

    /**
     * export file path 생성 및 리턴
     *
     * @param resourceType
     * @param tmResourceId
     * @param resourceName
     * @param exportFormat
     * @return
     */
    Path exportPath(StorageResourceType resourceType, Long tmResourceId, @NotEmpty String resourceName, final FileType exportFormat);

    /**
     * 업로드 된 파일을 {@link Resource} 타입으로 로드
     *
     * @param resourceType
     * @param tmResourceId
     * @param fileName
     * @return
     */
    Resource getUploadedFile(StorageResourceType resourceType, K tmResourceId, String fileName);

    /**
     * 익스포트된 파일을 {@link Resource} 타입으로 로드
     *
     * @param resourceType
     * @param tmResourceId
     * @param fileName
     * @return
     */
    Resource getExportedFile(StorageResourceType resourceType, K tmResourceId, String fileName);

    void deleteUploadedFile(StorageResourceType resourceType, K tmResourceId, String filename);

    void deleteUploadDirectory(StorageResourceType resourceType, K tmResourceId);

    void deleteExportedDirectory(StorageResourceType resourceType, K tmResourceId);

    /**
     * for download. 해당 directory 를 압축
     *
     * @param dirname2zip
     * @param resourceName
     * @return
     * @throws IOException
     */
    Path zipDirectory(String dirname2zip, String resourceName) throws IOException;
}

