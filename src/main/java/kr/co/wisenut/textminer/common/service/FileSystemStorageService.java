package kr.co.wisenut.textminer.common.service;

import kr.co.wisenut.config.TMStorageProperties;
import kr.co.wisenut.exception.StorageException;
import kr.co.wisenut.exception.StorageFileNotFoundException;
import kr.co.wisenut.textminer.common.resource.FileType;
import kr.co.wisenut.textminer.common.resource.StagingFileInfo;
import kr.co.wisenut.textminer.common.resource.StorageResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * tm-v3-manager 의 localFileSystem 기반으로 storageService 구현
 */
@Service
public class FileSystemStorageService implements StorageService<Long> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Path UPLOAD_PATH;
    private final Path COLLECTION_UPLOAD_PATH;
    private final Path DICTIONARY_UPLOAD_PATH;
    private final Path DOWNLOAD_PATH;
    private final Path COLLECTION_DOWNLOAD_PATH;
    private final Path DICTIONARY_DOWNLOAD_PATH;

    /**
     * File Expiration time (hour)
     */
    private final Long EXPIRATION_TIME_HOUR;

    /**
     * {@code {Resource's name}-{FormattedDateTime}.{extension}}
     */
    private final String EXPORT_FILE_NAME = "%s-%s.%s";//"%s-%d-%s.%s";
    /**
     * {@code 년월일시분-초밀리초. ex)201901011100-00000}
     */
    private final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyyMMddHHmm-ssSSS");

    private final TMStorageProperties storageProperties;

    @Autowired
    public FileSystemStorageService(TMStorageProperties properties) {
        this.storageProperties = properties;

        this.UPLOAD_PATH = Paths.get(properties.getUploadDir());
        this.COLLECTION_UPLOAD_PATH = Paths.get(properties.getUploadDir(), "collection");
        this.DICTIONARY_UPLOAD_PATH = Paths.get(properties.getUploadDir(), "dictionary");

        this.DOWNLOAD_PATH = Paths.get(properties.getDownloadDir());
        this.COLLECTION_DOWNLOAD_PATH = Paths.get(properties.getDownloadDir(), "collection");
        this.DICTIONARY_DOWNLOAD_PATH = Paths.get(properties.getDownloadDir(), "dictionary");

        this.EXPIRATION_TIME_HOUR = properties.getExpirationTimeHour();
    }

    @Override
    public void init() throws StorageException {
        try {
            final String osName = System.getProperty("os.name");
            final FileSystem fileSystem = FileSystems.getDefault();
            logger.info("system.os.name={}, fileSystem={}", osName, fileSystem);

            // if os is not WINDOW, NT --> do not start with e:/ c:/ E:/
            if (osName.toLowerCase().startsWith("window")) {
                if (storageProperties.getUploadDir().matches("^/.+")) {
                    logger.error("Malformed StorageProperties 'storage.uploadDir' in '{}'. {}", fileSystem, UPLOAD_PATH);
                    throw new StorageException("Malformed StorageProperties.uploadDir= " + UPLOAD_PATH);
                }

                if (storageProperties.getDownloadDir().matches("^/.+")) {
                    logger.error("Malformed StorageProperties 'storage.downloadDir' in '{}'. {}", fileSystem, DOWNLOAD_PATH);
                    throw new StorageException("Malformed StorageProperties.downloadDir= " + DOWNLOAD_PATH);
                }
            } else {
                if (storageProperties.getUploadDir().matches("^[a-zA-Z]:/.+")) {
                    logger.error("Malformed StorageProperties 'storage.uploadDir' in '{}'. {}", fileSystem, UPLOAD_PATH);
                    throw new StorageException("Malformed StorageProperties.uploadDir= " + UPLOAD_PATH);
                }

                if (storageProperties.getDownloadDir().matches("^[a-zA-Z]:/.+")) {
                    logger.error("Malformed StorageProperties 'storage.downloadDir' in '{}'. {}", fileSystem, DOWNLOAD_PATH);
                    throw new StorageException("Malformed StorageProperties.downloadDir= " + DOWNLOAD_PATH);
                }
            }
        } catch (NullPointerException | IllegalArgumentException | SecurityException e) {
            throw new StorageException("Malformed StorageProperties. " + storageProperties.toString(), e);
        }


        try {
            Files.createDirectories(UPLOAD_PATH);
            Files.createDirectories(DOWNLOAD_PATH);

            Files.createDirectories(COLLECTION_UPLOAD_PATH);
            Files.createDirectories(COLLECTION_DOWNLOAD_PATH);

            Files.createDirectories(DICTIONARY_UPLOAD_PATH);
            Files.createDirectories(DICTIONARY_DOWNLOAD_PATH);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage.", e);
        }
    }

    /**
     * {@code TMResource[id=tmResourceId]} 에 종속되는 파일 업로드시 저장할 경로를 리턴.
     *
     * @param resourceType storage service가 다룰 리소스 유형
     * @param tmResourceId TMResource's id
     * @return 업로드 리소스를 저장할 경로
     */
    private Path getUploadPath(StorageResourceType resourceType, Long tmResourceId) {
        switch (resourceType) {
            case DICTIONARY:
                return DICTIONARY_UPLOAD_PATH.resolve(String.valueOf(tmResourceId));
            case COLLECTION:
                return COLLECTION_UPLOAD_PATH.resolve(String.valueOf(tmResourceId));
            case PROJECT:
            default:
                throw new RuntimeException("storage - project file read, upload 기능은 현재 미구현");
        }
    }

    private Path getDownloadPath(StorageResourceType resourceType, Long tmResourceId) {
        switch (resourceType) {
            case DICTIONARY:
                return DICTIONARY_DOWNLOAD_PATH.resolve(String.valueOf(tmResourceId));
            case COLLECTION:
                return COLLECTION_DOWNLOAD_PATH.resolve(String.valueOf(tmResourceId));
            case PROJECT:
            default:
                throw new RuntimeException("storage - project file write, download 기능은 현재 미구현");
        }
    }

    @Override
    public void deleteExpired() {
        Long EXPIRATION_TIME = Duration.ofHours(EXPIRATION_TIME_HOUR).toHours(); // 파일 보존 시간 = 24
        logger.warn("Reset directories if more than {} hour have elapsed.", EXPIRATION_TIME);

        //TODO 만료된 파일만 삭제 vs 폴더가 만료됐을 경우만 삭제

        Long elapsedTimeMillis = System.currentTimeMillis() - UPLOAD_PATH.toFile().lastModified();
        if (TimeUnit.MILLISECONDS.toHours(elapsedTimeMillis) >= EXPIRATION_TIME) {
            logger.warn("Delete upload directory [{}].", UPLOAD_PATH);
            FileSystemUtils.deleteRecursively(UPLOAD_PATH.toFile());
        }

        elapsedTimeMillis = System.currentTimeMillis() - DOWNLOAD_PATH.toFile().lastModified();
        if (TimeUnit.MILLISECONDS.toHours(elapsedTimeMillis) >= EXPIRATION_TIME) {
            logger.warn("Delete download directory [{}].", DOWNLOAD_PATH);
            FileSystemUtils.deleteRecursively(DOWNLOAD_PATH.toFile());
        }
    }

    @Override
    public void storeUploadFile(StorageResourceType type, Long tmResourceId, MultipartFile multipartFile) {
        String filename = StringUtils.cleanPath(multipartFile.getOriginalFilename());
        try {
            if (multipartFile.isEmpty()) {
                throw new StorageException("Failed to store empty file " + filename);
            }
            if (filename.contains("..")) {
                // This is a security check
                throw new StorageException(
                        "Cannot store file with relative path outside current directory. "
                                + filename);
            }
            try (InputStream inputStream = multipartFile.getInputStream()) {
                Path pathToStore = getUploadPath(type, tmResourceId);
                if (!pathToStore.toFile().exists() || !pathToStore.toFile().isDirectory()) {
                    Files.createDirectories(pathToStore);
                }

                // 덮어쓰기
                Files.copy(inputStream, pathToStore.resolve(filename),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + filename, e);
        }

    }

    /**
     * export file path 생성 및 리턴
     *
     * @param resourceType
     * @param tmResourceId
     * @param resourceName
     * @param exportFormat
     * @return
     */
    @Override
    public Path exportPath(StorageResourceType resourceType, Long tmResourceId, @NotEmpty String resourceName, final FileType exportFormat) {
        Path dirPath = getDownloadPath(resourceType, tmResourceId);

        try {
            Files.createDirectories(dirPath);
        } catch (IOException e) {
            throw new StorageException("Failed to create directories. " + dirPath);
        }

        // filename {resource's Name}-{timestamp}.{extension}
        Path filePath = Paths.get(dirPath.toAbsolutePath().toString(),
                String.format(EXPORT_FILE_NAME, resourceName, DATE_FORMATTER.format(new Date()), exportFormat.getExtension()));
        try {
            Files.createFile(filePath);
        } catch (IOException e) {
            throw new StorageException("Failed to create exportFile. " + filePath);
        }

        return filePath;
    }


    @Override
    public Stream<Path> streamUploadedFiles(StorageResourceType type, Long tmResourceId) throws StorageException {
        Path dir = getUploadPath(type, tmResourceId);

        try {
            return Files.list(dir)
                    .sorted((p1, p2) -> {
                        // lastModified ASC ↗
                        int result = Long.compare(p1.toFile().lastModified(), p2.toFile().lastModified());
                        if (result == 0) {
                            // is same, file size ASC ↗
                            result = Long.compare(p1.toFile().length(), p2.toFile().length());
                            if (result == 0) {
                                result = p1.getFileName().compareTo(p2.getFileName());
                            }
                        }
                        return result;
                    })
                    //.filter(path -> !path.equals(dir))
//                    .map(dir::relativize);
                    ;
        } catch (NoSuchFileException e) {
            throw new StorageFileNotFoundException(e.getMessage());
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }
    }

    @Override
    public List<Path> listUploadedFiles(StorageResourceType resourceType, Long tmResourceId) throws StorageException {
        Path dir = getUploadPath(resourceType, tmResourceId);

        try (Stream<Path> stream = Files.list(dir)
                .sorted((p1, p2) -> {
                    // lastModified ASC ↗
                    int result = Long.compare(p1.toFile().lastModified(), p2.toFile().lastModified());
                    if (result == 0) {
                        // is same, file size ASC ↗
                        result = Long.compare(p1.toFile().length(), p2.toFile().length());
                        if (result == 0) {
                            result = p1.getFileName().compareTo(p2.getFileName());
                        }
                    }
                    return result;
                })//.filter(path -> !path.equals(dir)).map(dir::relativize);
             ;
        ) {
            return stream.collect(Collectors.toList());
        } catch (NoSuchFileException e) {
            throw new StorageFileNotFoundException(e.getMessage());
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }
    }

    @Override
    public List<StagingFileInfo> loadAllAsStagingFile(StorageResourceType resourceType, Long tmResourceId) {
        Path dir = getUploadPath(resourceType, tmResourceId);

        if (Files.notExists(dir) || !Files.isDirectory(dir)) {
            logger.warn("Directory no exists. [{}]", dir);
            return Collections.EMPTY_LIST;
        }

        try (Stream<File> fileStream = Files.list(dir)
                .map(Path::toFile)
                .filter(File::isFile)
                .sorted((o1, o2) -> (int) (o1.lastModified() - o2.lastModified()))) {

            List<StagingFileInfo> stagingFileInfos = fileStream
                    .map(file -> new StagingFileInfo(resourceType, tmResourceId, file))
                    .collect(Collectors.toList());

            return stagingFileInfos;
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }
    }

    @Override
    public Resource getUploadedFile(StorageResourceType type, Long tmResourceId, String fileName) {
        Path filePath = getUploadPath(type, tmResourceId).resolve(fileName);

        if (!Files.exists(filePath))
            throw new StorageFileNotFoundException(String.format("Failed to read stored files. %s", filePath));

        try {

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException(
                        "Could not read file: " + fileName);
            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + fileName, e);
        }
    }

    @Override
    public Resource getExportedFile(StorageResourceType resourceType, Long tmResourceId, String fileName) {
        Path filePath = getDownloadPath(resourceType, tmResourceId).resolve(fileName);

        if (!Files.exists(filePath))
            throw new StorageFileNotFoundException(String.format("Failed to read stored files. %s", filePath));

        try {

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException(
                        "Could not read file: " + fileName);
            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + fileName, e);
        }

    }

    @Override
    public void deleteUploadedFile(StorageResourceType type, Long tmResourceId, String fileName) {
        Path dirPath = getUploadPath(type, tmResourceId);
        File file = dirPath.resolve(fileName).toFile();

        if (!file.exists())
            throw new StorageFileNotFoundException(String.format("Could not read file: %s/%d/%s", type, tmResourceId, fileName));

        if (!file.delete()) {
            throw new StorageException("Failed to delete.");
        }
    }

    @Override
    public void deleteUploadDirectory(StorageResourceType type, Long tmResourceId) {
        Path dir = getUploadPath(type, tmResourceId);

        try {
            FileSystemUtils.deleteRecursively(dir);

            if (Files.exists(dir)) {
                logger.debug("directory exist yet {}", dir);
            }
        } catch (IOException e) {
            logger.warn("Failed to delete directory '{}'. {}", dir, e.getMessage());
        }
    }

    @Override
    public void deleteExportedDirectory(StorageResourceType type, Long tmResourceId) {
        Path dir = getDownloadPath(type, tmResourceId);

        try {
            FileSystemUtils.deleteRecursively(dir);

            if (Files.exists(dir)) {
                logger.debug("directory exist yet {}", dir);
            }
        } catch (IOException e) {
            logger.warn("Failed to delete directory '{}'. {}", dir, e.getMessage());
        }
    }


    /**
     * dirname2zip 경로를 압축하고 생성된 zip 파일의 경로를 리턴
     *
     * @param dirname2zip : pathname of directory to zip.
     * @return
     * @throws IOException
     */
    @Override
    public Path zipDirectory(String dirname2zip, String resourceName) throws IOException {
        // source file = dir or a file
        File fileToZip = new File(dirname2zip);

        // output zip file name
        String zipName = String.format("%s/%s/%s.zip", DOWNLOAD_PATH, resourceName, fileToZip.getName());

        try {
            Files.createDirectories(Paths.get(zipName).getParent());
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }

        FileOutputStream fos = new FileOutputStream(zipName);
        ZipOutputStream zipOut = new ZipOutputStream(fos);

        // start zipDirectory archive
        zipFile(fileToZip, fileToZip.getName(), zipOut);
        zipOut.close();
        fos.close();

        Path zipFile = Paths.get(zipName);
        if (!Files.exists(zipFile)) {
            throw new FileNotFoundException();
        }

        return zipFile.toAbsolutePath();
    }


    /**
     * @param file2zip file or directory to zip
     * @param fileName file or directory's name (last name of path)
     * @param zipOut   output stream of zip
     * @throws IOException
     */
    public void zipFile(File file2zip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (file2zip.isHidden()) {
            return;
        }
        if (file2zip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = file2zip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(file2zip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }

    @Override
    public String toString() {
        return String.format("{ 'storage.uploadDir' : '%s', 'storage.downloadDir' : '%s', 'storage.expirationTimeHour' : '%d' }",
                UPLOAD_PATH, DOWNLOAD_PATH, EXPIRATION_TIME_HOUR);
    }
}

