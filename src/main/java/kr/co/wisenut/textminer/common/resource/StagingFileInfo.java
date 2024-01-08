package kr.co.wisenut.textminer.common.resource;

import kr.co.wisenut.exception.*;
import kr.co.wisenut.textminer.common.resource.FileType;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Staging file's information = import 영역에 upload 된 파일 정보.
 * import 진행 가능 여부 판단
 */
@Data
public class StagingFileInfo {
    public static final Charset ALLOWED_CHARSET = StandardCharsets.UTF_8; // from application.properties??
    private static final String EXTENSION_SCD = "SCD", EXTENSION_CSV = "CSV", EXTENSION_JSON = "JSON", EXTENSION_TEXT = "TXT";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * 리스소 유형
     */
    StorageResourceType type;
    /**
     * 리소스 id(key)
     */
    long resourceId;
    /**
     * 파일명
     */
    String filename;
    /**
     * 파일 크기 bytes
     */
    long filesize;
    /**
     * 파일 절대 경로
     */
    String absolutePath;
    /**
     * import 할 수 있는 파일인지 판단 --> fixme(wisnt65) enum 으로 관리?
     * <ol>
     *     <li>extension : collection - csv,scd,json / dictionary - txt,csv</li>
     *     <li>file encoding - only UTF-8</li>
     *     <li>validate content. invalid case :
     *      <ol>
     *          <li>collection.csv - no header at first line, header no contains "DOCID"</li>
     *          <li>collection.scd - no startsWith "&lt;DOCID&#47;&gt;"</li>
     *      </ol>
     *     </li>
     * </ol>
     */
    boolean importable;
    /**
     * not importable (importable==false) 일 경우 원인 출력
     */
    String message;
    /**
     * 예상 문서 수
     */
    long estimateCountDoc;

    public StagingFileInfo(StorageResourceType type, long resourceId, File file) {
        this.type = type;
        this.resourceId = resourceId;

        this.absolutePath = file.getAbsolutePath();
        this.filename = file.getName();
        this.filesize = file.length();

        try {
            // check file is importable
            validate(type, file);

            // if importable, count document --> change to lazy
            this.estimateCountDoc = type.equals(StorageResourceType.COLLECTION) ?
                    estimateDocumentCount(file) : 0;

            this.importable = true;
        } catch (UnsupportedFileException e) {
            logger.error("{} {}", e.toString(), file.toURI()); //fixme
            this.message = e.getMessage();
            this.importable = false;
        } catch (SecurityException | IOException e) {
            System.err.printf("처리하지 못한 파일 예외 발생 [%s]", file.toURI()).println();
            e.printStackTrace();
        }
    }

    /**
     * estimate the number of document.
     * Collection 문서 파일의 문서 개수를 카운팅.<br>
     * <b>UTF-8 타입의 "DOCID" 가 반드시 존재하는 SCD, JSON, CSV 파일만 대상으로 함.</b>
     * TODO(wisnt65) lazy 하게 처리 가능?s
     *
     * @param file
     * @return
     * @throws UnSupportedFileType
     * @throws IOException
     * @throws UncheckedIOException
     */
    public static long estimateDocumentCount(@NotNull File file) throws UnSupportedFileType, IOException, UncheckedIOException {
        final Pattern DOCID_PATTERN = Pattern.compile("\"docid\"\\s?:");

        final String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1).toUpperCase();

        try (Stream<String> stream = Files.lines(file.toPath(), ALLOWED_CHARSET)) {
            switch (extension) {
                case EXTENSION_SCD:
                    return stream
                            .filter(line -> line.startsWith("<DOCID>"))
                            .count();
                case EXTENSION_CSV:
                    return stream
                            .count() - 1; // without header, 1 document == 1 line 가정
                case EXTENSION_JSON:
                    return stream
                            .filter(line -> DOCID_PATTERN.matcher(line).find())
                            .count();
            }
        }
        throw new UnSupportedFileType(file);
    }


    /**
     * 파일이 업로드 가능한 유효한 파일인지 검증
     * fixme(wisnt65) 사전 업로드 대상 파일 요구사항에 따라 추가구현 필요.
     *
     * <ol>
     *     <li>파일 타입 by file name extension</li>
     *     <li>파일 크기 length of file (bytes)</li>
     *     <li>파일 인코딩 Only UTF-8</li>
     *     <li>문서 파일 포맷 - SCD는 "&lt;DOCID&#47;&gt; 로 시작, CSV 는 "DOCID"를 포함한 헤더로 시작</li>
     * </ol>
     *
     * @param type
     * @param file
     * @throws UnsupportedFileException
     * @throws IOException
     */
    public static void validate(@NotNull StorageResourceType type, @NotNull File file) throws UnsupportedFileException, IOException {
        try {
            final String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1).toUpperCase();

            // [1] extension
            if (!Arrays.stream(FileType.values()).anyMatch(fileType -> extension.equalsIgnoreCase(fileType.getExtension()))) {
                throw new UnSupportedFileType(file);
            }

            // [2] file size
            if (file.length() == 0) {
                throw new EmptyFile(file);
            }

            // [3] encoding - not UTF-8, UTF-8 with BOM 찾기
            // [4] malformed content, fixme: 각 포맷별 snipper? sampler? 라이브러리 있으면 편할듯 lazy하게 validation
            try (BufferedReader bufferedReader = Files.newBufferedReader(file.toPath(), ALLOWED_CHARSET)) {
                String headerLine = bufferedReader.readLine();

                // find BOM
                if (headerLine != null && headerLine.length() >= 1 && headerLine.charAt(0) == '\uFEFF') {
                    throw new UnsupportedFileEncoding(file);
                }

                if (type.equals(StorageResourceType.COLLECTION)) {
                    switch (extension) {
                        case EXTENSION_SCD:
                            if (!headerLine.startsWith("<DOCID>")) {
                                throw new MalformedSCDFile(file);
                            }
                            // 모든 필드네임 태그가 쌍이 맞는지 체크?
                            break;
                        case EXTENSION_CSV:
                            String[] header = headerLine.split(",");
                            if (!headerLine.contains("docid") || header == null || header.length < 1) {
                                throw new MalformedCSVFile(file);
                            }
                            break;
                        case EXTENSION_JSON:
                            if (!headerLine.startsWith("[") && !headerLine.startsWith("{")) {
                                throw new MalformedJSONFile(file);
                            }
                            // todo(wisnt65) docid, _id 체크
                            break;
                        default:
                            throw new UnSupportedFileType(file);
                    }
                } else {
                    switch (extension) {
                        case EXTENSION_CSV:
                            String[] header = headerLine.split(",");
                            if (!headerLine.toLowerCase().contains("entry") || header == null || header.length < 1) {
                                throw new MalformedCSVFile(file);
                            }
                            break;
                        case EXTENSION_TEXT:
                            // entries.txt 파일 포맷 - ';' 체크? NAMED ENTITY - \t 체크?
                            break;
                        default:
                            throw new UnSupportedFileType(file);
                    }
                }
            }

        } catch (UncheckedIOException | MalformedInputException e) {
            // encoding != UTF-8
            // throw new UnSupportedFileEncoding(file);
            throw new UnsupportedFileEncoding(file);
        }
    }
}