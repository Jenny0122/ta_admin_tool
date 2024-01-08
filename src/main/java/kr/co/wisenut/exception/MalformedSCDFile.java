package kr.co.wisenut.exception;

import java.io.File;

public class MalformedSCDFile extends UnsupportedFileException {
    public MalformedSCDFile(File file) {
        super("SCD document file must start with \"<DOCID>\" [%s]", file);
    }
}