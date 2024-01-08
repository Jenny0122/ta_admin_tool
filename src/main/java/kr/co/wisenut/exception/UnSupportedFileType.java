package kr.co.wisenut.exception;

import java.io.File;

public class UnSupportedFileType extends UnsupportedFileException {
    public UnSupportedFileType(File file) {
        super("Unsupported file type. ", file);
    }
}