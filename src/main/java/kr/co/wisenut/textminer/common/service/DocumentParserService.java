package kr.co.wisenut.textminer.common.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import kr.co.wisenut.exception.MalformedCSVFile;
import kr.co.wisenut.exception.MalformedJSONFile;
import kr.co.wisenut.exception.UnsupportedFileException;
import kr.co.wisenut.textminer.common.resource.StagingFileInfo;
import kr.co.wisenut.textminer.common.resource.StorageResourceType;
import org.apache.commons.io.FilenameUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 문서 파일 파싱 관련 기능 제공. todo convert to interface? for aop
 * 필요 기능 리스트업, 묶어야함.
 * <ul>
 *     <li>findFirst(Path) - 해당 파일의 첫번째 파일을 가져옴. upload 페이지에서 문서 파일 미리보기 == 필드 인포 로 사용 </li>
 *     <li>?streamAll(Path) - 필요한지? </li>
 *     <li>?findAll(Path) - 필요한지? </li>
 *     <li>?count(Path) - 필요한지? </li>
 *     <li>?sampling?? findAny?(Path) - 필요한듯. 문서 둘러보기 가능할듯  </li>
 * </ul>
 */
@Service
public class DocumentParserService {
    public static final Document EMPTY_DOCUMENT = new Document();
    /**
     * empty schema csv reader - jackson
     */
    final ObjectReader csvReader;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * json reader - jackson
     */
    @Autowired
    ObjectMapper objectMapper;

    public DocumentParserService() {
        // csv parser build
        CsvSchema csvSchema = CsvSchema.emptySchema().withHeader();

        CsvMapper csvMapper = CsvMapper.builder()
                .enable(com.fasterxml.jackson.dataformat.csv.CsvParser.Feature.WRAP_AS_ARRAY)
                .enable(com.fasterxml.jackson.dataformat.csv.CsvParser.Feature.INSERT_NULLS_FOR_MISSING_COLUMNS)
                .build();
        csvReader = csvMapper.readerFor(Document.class).with(csvSchema);
    }

    /**
     * documents file 의 첫번째 document 를 가져온다
     *
     * @param documentFilePath - path of documents file = upload staging file.
     * @return if readable, first document of given documents file. <b>Else empty document. (with silent error log)</b>
     */
    public Document findFirst(Path documentFilePath) {
        final String extension = FilenameUtils.getExtension(documentFilePath.getFileName().toString()).toUpperCase();

        Document firstDocument = DocumentParserService.EMPTY_DOCUMENT;
        try {
            StagingFileInfo.validate(StorageResourceType.COLLECTION, documentFilePath.toFile());

            switch (extension) {
                case "JSON":
                    firstDocument = findFirstFromJSON(documentFilePath);
                    break;
                case "SCD":
                    firstDocument = findFirstFromSCD(documentFilePath);
                    break;
                case "CSV":
                    firstDocument = findFirstFromCSV(documentFilePath);
                    break;
            }
        } catch (UnsupportedFileException e) {
            logger.error("Failed to findFirst document from {}. {}", documentFilePath.getFileName(), e.toString());
        } catch (IOException e) {
            // File IOException 은 한번에 처리
            logger.error("Failed to read file '{}'. {}", documentFilePath.getFileName(), e.toString());
        }

        return firstDocument;
    }

    public Document findFirstFromSCD(Path scdFilePath) throws IOException {
        Document startDocument = EMPTY_DOCUMENT;

        try (BufferedReader bufferedReader = Files.newBufferedReader(scdFilePath)) {
            List<String> tempDocumentLines = new ArrayList<>();
            List<String> startDocumentLines = null;
            // get first doc
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("<DOCID>")) {
                    // 새로운 문서
                    startDocumentLines = new ArrayList<>(tempDocumentLines);
                    tempDocumentLines.clear();

                    // 일단 첫번째 문서만 read
                    if (!startDocumentLines.isEmpty()) {
                        break;
                    }
                }
                // 문서의 일부
                tempDocumentLines.add(line);
            }

            // if the file contains only one document
            if (startDocumentLines.isEmpty() && !tempDocumentLines.isEmpty() && tempDocumentLines.get(0).startsWith("<DOCID>")) {
                startDocumentLines = new ArrayList<>(tempDocumentLines);
            }

            // schema detect : parse SCD first doc --> <$fieldName>$fieldContent
            if (!startDocumentLines.isEmpty()) {
                startDocument = new Document();
            }

            // must line start with <\\w>
            final Pattern patternScdFieldName = Pattern.compile("^<([\\w]+)>"); // group 0: '<$FieldName>', group 1: '$FieldName'
            String fieldName = null, fieldValue;
            for (String tempLine : startDocumentLines) {
                Matcher matcher = patternScdFieldName.matcher(tempLine);
                if (matcher.find()) {
                    fieldName = matcher.group(1);
                    fieldValue = patternScdFieldName.split(tempLine, 2)[1];
                    startDocument.append(fieldName, fieldValue);
                } else {
                    if (fieldName != null) {
                        startDocument.computeIfPresent(fieldName, (s1, oldVal) -> ((String) oldVal).concat("\n").concat(tempLine));
                    }
                }
            }

        }
        return startDocument;
    }

    /**
     * Find first document from json file.
     *
     * @param jsonFilePath
     * @return
     * @throws IOException
     */
    public Document findFirstFromJSON(Path jsonFilePath) throws IOException {
        Document startDocument = EMPTY_DOCUMENT;

        JsonFactory jsonFactory = new JsonFactory();
        try (BufferedReader bufferedReader = Files.newBufferedReader(jsonFilePath, StandardCharsets.UTF_8);
             JsonParser jsonParser = jsonFactory.createParser(bufferedReader)) {

            //logger.info("Start document parsing. '{}'", filePath.getFileName());

            ObjectMapper jsonMapper = objectMapper;
            JsonToken token = jsonParser.nextToken();

            // if JSON_ARRAY document file , skip start_array token '['
            if (JsonToken.START_ARRAY.equals(token)) {
                token = jsonParser.nextToken();
            }

            // for-each remaining
            while (true) {
                try {
                    if (token == null || !JsonToken.START_OBJECT.equals(token)) {
                        // end or malformed json documents file
                        break;
                    }

                    Document document = jsonMapper.readValue(jsonParser, Document.class);
                    if (document != null && !document.isEmpty()) {
                        startDocument = document;
                        break;
                    }
                } catch (JsonMappingException | NullPointerException documentParsingException) {
                    logger.debug("{} {}", documentParsingException.getMessage(), jsonParser.getCurrentLocation());
                    throw new MalformedJSONFile(jsonFilePath.toFile(), documentParsingException);
                } catch (JsonParseException jsonParseException) {
                    // Error caused non-well-formed json file --> do not import
                    logger.debug("Malformed JSON document file '{}'. {}", jsonFilePath.getFileName(), jsonParseException.getMessage());
                    throw new MalformedJSONFile(jsonFilePath.toFile(), jsonParseException);
                }
                token = jsonParser.nextToken();
            } // end while
        }
        return startDocument;
    }

    /**
     * Find first document from csv file.
     *
     * @param csvFilePath
     * @return
     * @throws IOException
     */
    public Document findFirstFromCSV(Path csvFilePath) throws IOException {
        Document startDocument = EMPTY_DOCUMENT;

        try (BufferedReader bufferedReader = Files.newBufferedReader(csvFilePath, StandardCharsets.UTF_8);
             MappingIterator<Document> mappingIterator = csvReader.readValues(bufferedReader)) {

            if (mappingIterator.hasNext()) {
                Document document = mappingIterator.nextValue();
                startDocument = document;
            }

        } catch (JsonMappingException | NullPointerException | JsonParseException e) {
            logger.debug("Malformed CSV document file '{}'. {}", csvFilePath.getFileName(), e.toString());
            throw new MalformedCSVFile(csvFilePath.toFile(), e);
        }

        return startDocument;
    }

    public long countsDocument() {
        return 0;
    }

    public long estimateDocument() {

        return 0;
    }

}
