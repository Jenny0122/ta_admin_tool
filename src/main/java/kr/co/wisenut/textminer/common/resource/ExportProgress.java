package kr.co.wisenut.textminer.common.resource;

import kr.co.wisenut.textminer.common.TextMinerConstants;
import kr.co.wisenut.textminer.common.service.StorageService;
import kr.co.wisenut.textminer.common.vo.ImportProgressVo;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * 컬렉션 > 문서 다운로드 비동기 진행 상황, 사전 > 엔트리 다운로드 동기 진행 상황<br>
 * {@link kr.wisenut.manager.service.resource.DocumentService#asyncExportDocuments(long, FileType, String)}<br>
 * {@link kr.wisenut.manager.service.resource.EntryService#exportEntries(long, FileType, String)}
 */
@Data
@Builder
public class ExportProgress<T extends ImportProgressVo> {

    /**
     * 진행 상태 : FAILURE, SUCCESS, IN_PROGRESS 만 사용
     */
    @NotNull
    ProgressState progress;

    /**
     * download 요청 유저네임
     */
    @NotEmpty
    String username;

    /**
     * export 파일 타입
     */
    @NotNull
    FileType fileType;

    /**
     * export 대상 리소스. 현재는 Collection 만
     */
    @NotNull
    T resourceInfo;

    /**
     * 진행 상황 표시를 위한 현재 export 된 문서 수.
     */
    long writeCount;

    /**
     * 요청 시각
     */
    LocalDateTime requestedTime;

    /**
     * 완료 시각
     */
    LocalDateTime completedTime;

    /**
     * export 된 파일 유효 기간.
     * todo(wisnt65) : 1시간? 24시간? up to 5 files?
     */
    LocalDateTime expiredTime;

    /**
     * export 된 파일 경로
     */
    Path exportedFilePath;

    /**
     * export 된 파일명. 띄어쓰기 escaped 처리
     */
    String exportedFileName;

    /**
     * export 된 파일 다운로드 링크.
     */
    URI downloadLink;


    public boolean isInProgress() {
        return this.progress.equals(ProgressState.IN_PROGRESS);
    }

    public boolean isComplete() {
        return this.progress.equals(ProgressState.SUCCESS) ||
                this.progress.equals(ProgressState.PARTIAL_SUCCESS) ||
                this.progress.equals(ProgressState.FAILURE);
    }

    /**
     * export 완료된 경우 상태 변경하면서 생성된 파일 정보 업데이트
     *
     * @param exportFilePath
     */
    public void setComplete(Path exportFilePath) {
        try {
            Files.isReadable(exportFilePath);

            this.completedTime = LocalDateTime.now();
            this.expiredTime = this.completedTime.plusMinutes(60); // 60 min

//            보안이슈로 해당 정보 주석처리
//            this.exportedFilePath = exportFilePath;

            // uri escaped file name - " " --> %20
            String escapedFileName = StorageService.escapeFileName(exportFilePath);
            this.exportedFileName = escapedFileName;

            String typeStr = (this.resourceInfo.getResourceType().equals(TextMinerConstants.PROGRESS_TYPE_DICTIONARY)) ? "dictionary" : "collection";

//            보안이슈로 해당 정보 주석처리
//            this.downloadLink = URI.create(String.format("/file/export/%s/%d/%s", typeStr, this.resourceInfo.getImportId(), escapedFileName));

            this.progress = ProgressState.SUCCESS;
        } catch (SecurityException e) {
            this.progress = ProgressState.FAILURE;
        } catch (RuntimeException e) {
            this.progress = ProgressState.FAILURE;
        }
    }

    public boolean isFailed() {
        return this.progress.equals(ProgressState.FAILURE);
    }

    public void setFailed() {
        this.progress = ProgressState.FAILURE;
    }

    public void setInProgress() {
        this.progress = ProgressState.IN_PROGRESS;
    }

}
