package kr.co.wisenut.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import kr.co.wisenut.exception.MalformedCSVFile;
import kr.co.wisenut.exception.UnSupportedFileType;
import kr.co.wisenut.textminer.common.resource.ImportErrorHandle;
import kr.co.wisenut.textminer.dictionary.vo.Entry;
import kr.co.wisenut.textminer.dictionary.vo.EntryVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.StreamUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 사전 파일 read & parse 임시 구현. TODO(wisnt65) 재정의, 구현 필요. DocumentParserService 와 콤바인?
 * <ul>
 *     <li>{@link DictionaryFileParser.CsvParser} - *.csv 포맷의 사전 파일 read & parse</li>
 *     <li>{@link DictionaryFileParser.TextParser} - *.txt 포맷의 사전 파일 read & parse</li>
 * </ul>
 */
@Slf4j
public abstract class DictionaryFileParser {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    File file;

    protected DictionaryFileParser(File file) {
        this.file = file;
    }

    public static DictionaryFileParser path(Path path) {
        return file(path.toFile());
    }

    public static DictionaryFileParser file(File file) throws UnSupportedFileType {
        String type = FilenameUtils.getExtension(file.getName()).toLowerCase();

        switch (type) {
            case "csv":
                return new CsvParser(file);
            case "text":
            case "txt":
                return new TextParser(file);
            default:
                // throw new unsupported-File-type-exception
                throw new UnSupportedFileType(file);
        }
    }

    /**
     * read file's contents as {@link List}.
     *
     * @param hasCategory
     * @param errors
     * @param errorHandle
     * @return
     * @throws IOException
     */
    public abstract List<EntryVo> findAll(boolean hasCategory, List<String> errors, ImportErrorHandle errorHandle) throws IOException;

    /**
     * read file's contents as {@link Stream}
     *
     * @return
     * @throws IOException
     */
    public abstract Stream<EntryVo> streamAll() throws IOException;

    /**
     * csv file parser
     */
    private static class CsvParser extends DictionaryFileParser {
        public CsvParser(File file) {
            super(file);
        }

        @Override
        public List<EntryVo> findAll(boolean hasCategory, List<String> errors, ImportErrorHandle errorHandle) throws IOException {
        	logger.debug("@@ schema...");
            CsvSchema csvSchema = CsvSchema.emptySchema().withHeader();

            CsvMapper csvMapper = CsvMapper.builder()
                    .enable(com.fasterxml.jackson.dataformat.csv.CsvParser.Feature.WRAP_AS_ARRAY)
                    .enable(com.fasterxml.jackson.dataformat.csv.CsvParser.Feature.INSERT_NULLS_FOR_MISSING_COLUMNS)
                    .build();

            ObjectReader csvReader = csvMapper.readerFor(Entry.class).with(csvSchema);

            List<Entry> tempEntry = null;
            List<EntryVo> entries = null;

            try (BufferedReader bufferedReader = Files.newBufferedReader(this.file.toPath(), StandardCharsets.UTF_8);
                 MappingIterator<Entry> mappingIterator = csvReader.readValues(bufferedReader)) {
                
            	entries = new ArrayList<>();
            	
                if (errorHandle.equals(ImportErrorHandle.SKIP)) {
                    // read iterable, skip parse error + add log
                    while (mappingIterator.hasNext()) {
                        try {
                        	Entry temp = mappingIterator.nextValue();
                            EntryVo entry = new EntryVo();
                            
                            entry.setEntryContent(temp.getEntry());
                            if (temp.getSynonyms() != null) {
                            	entry.setSynonyms(temp.getSynonyms());
                            }
                            
                            entries.add(entry);
                        } catch (JsonMappingException e) {
                            errors.add(e.toString());
                            logger.trace(e.toString());
                        }
                    }
                } else {
                    // read once, if error occurred, failed to read
                    try {
                    	tempEntry = mappingIterator.readAll();
                    	
                    	for (int i = 0 ; i < tempEntry.size(); i++) {
                    		EntryVo entry = new EntryVo();
                    		
                    		entry.setEntryContent(tempEntry.get(i).getEntry());
                            if (tempEntry.get(i).getSynonyms() != null) {
                            	entry.setSynonyms(tempEntry.get(i).getSynonyms());
                            }
                            
                            entries.add(entry);
                    	}
                    	
                    } catch (JsonMappingException e) {
                        logger.error(e.toString());
                        throw new MalformedCSVFile(this.file, e);
                    }
                }

            } catch (JsonParseException e) {
                logger.error(e.toString());
                throw new MalformedCSVFile(this.file, e);
            }

            return entries;
        }

        public Stream<EntryVo> streamAll() throws IOException {
            CsvSchema csvSchema = CsvSchema.emptySchema().withHeader();

            CsvMapper csvMapper = CsvMapper.builder()
                    .enable(com.fasterxml.jackson.dataformat.csv.CsvParser.Feature.WRAP_AS_ARRAY)
                    .enable(com.fasterxml.jackson.dataformat.csv.CsvParser.Feature.INSERT_NULLS_FOR_MISSING_COLUMNS)
                    .build();
            ObjectReader csvReader = csvMapper.readerFor(Entry.class).with(csvSchema);

            Stream<EntryVo> entries = Stream.empty();

            try (BufferedReader bufferedReader = Files.newBufferedReader(this.file.toPath(), StandardCharsets.UTF_8);
                 MappingIterator<Entry> mappingIterator = csvReader.readValues(bufferedReader)) {

            	List<EntryVo> list = new ArrayList<EntryVo>();
            	
            	while(mappingIterator.hasNext()) {
            		Entry temp = mappingIterator.nextValue();
            		EntryVo entry =  new EntryVo();
            		
            		entry.setEntryContent(temp.getEntry());
            		if (temp.getSynonyms() != null) {
                    	entry.setSynonyms(temp.getSynonyms());
                    }
            		
            		list.add(entry);
            	}
            	
            	Iterator<EntryVo> iter = list.iterator();
            	
                return StreamUtils.createStreamFromIterator(iter);

            } catch (JsonMappingException | JsonParseException e) {
                logger.error(e.toString());
                throw new MalformedCSVFile(this.file, e);
            }
        }
    }

    /**
     * text file parser
     */
    private static class TextParser extends DictionaryFileParser {
//        private static final Pattern SEPARATOR_PATTERN = Pattern.compile("\t+");

        protected TextParser(File file) {
            super(file);
        }

        @Override
        public List<EntryVo> findAll(boolean hasCategory, List<String> errors, ImportErrorHandle errorHandle) throws IOException {
            List<EntryVo> entries = Collections.EMPTY_LIST;

            try (BufferedReader bufferedReader = Files.newBufferedReader(this.file.toPath())) {
                entries = new ArrayList<>();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    // text file 의 주석 ;
                    if (line.startsWith("*")) {
                        continue;
                    }
//                        String[] ss = SEPARATOR_PATTERN.split(line, 2);
                        String[] ss = line.split("\\|");
                        
                        // 엔트리 담기
                        EntryVo e = new EntryVo();
                        e.setEntryContent(ss[0]);

                        // 동의어 저장
                        if (ss.length > 1) {
                        	e.setSynonyms(ss[1]);
                        }
                        
                        entries.add(e);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                return entries;
            }
        }

        @Override
        public Stream<EntryVo> streamAll() {
            return Stream.empty();
        }
    }

}
