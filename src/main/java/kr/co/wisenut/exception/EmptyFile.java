package kr.co.wisenut.exception;

import java.io.File;

public class EmptyFile extends UnsupportedFileException {
    public EmptyFile(File file) {
        super("0 byte file.", file);
    }
}
