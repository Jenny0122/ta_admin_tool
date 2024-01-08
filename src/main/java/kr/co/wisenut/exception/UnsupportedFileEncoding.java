package kr.co.wisenut.exception;

import java.io.File;

public class UnsupportedFileEncoding extends UnsupportedFileException {

    public UnsupportedFileEncoding(File file) {
        super("Supports only 'UTF-8 without BOM' encoding files.", file);
    }
}
