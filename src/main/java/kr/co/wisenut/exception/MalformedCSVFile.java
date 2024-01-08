package kr.co.wisenut.exception;

import java.io.File;

public class MalformedCSVFile extends UnsupportedFileException {
    public MalformedCSVFile(File file) {
        super("CSV document file must have 'header' that contains \"entry\". " +
                "CSV dictionary file must have 'header' that contains \"synonyms\"", file);
    }

    public MalformedCSVFile(File file, Throwable throwable) {
        super("Malformed CSV file. " + throwable.toString(), file);
    }

}