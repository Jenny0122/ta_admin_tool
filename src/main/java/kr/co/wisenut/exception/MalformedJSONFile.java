package kr.co.wisenut.exception;

import java.io.File;

/**
 * 사용할 수 없는 JSON 파일
 */
public class MalformedJSONFile extends UnsupportedFileException {
    public MalformedJSONFile(File file) {
        super("Malformed JSON document file.", file);
    }

    public MalformedJSONFile(File file, Throwable throwable) {
        super("Malformed JSON document file. " + throwable.toString(), file);
    }
}
