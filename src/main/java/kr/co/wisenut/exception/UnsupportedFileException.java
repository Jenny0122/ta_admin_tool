package kr.co.wisenut.exception;

import java.io.File;

/**
 * tm-v3-manager 에서 지원하지 않는 파일 정의.
 * 1. tm-resource : collection, dictionary 업로드 불가 파일
 * - 빈 파일, 확장자, 인코딩, document 시작 라인 패턴 등
 */
public abstract class UnsupportedFileException extends RuntimeException {
    File file;

    public UnsupportedFileException(String message, File file) {
        super(message);
        this.file = file;
    }
}
