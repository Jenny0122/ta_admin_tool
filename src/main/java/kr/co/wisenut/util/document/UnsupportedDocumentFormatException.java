package kr.co.wisenut.util.document;

/**
 * Created by Jongho Lee on 2018-03-20.
 */
public class UnsupportedDocumentFormatException extends Exception{
    public UnsupportedDocumentFormatException() {
        super("Unsupported Document Format");
    }
    
    public UnsupportedDocumentFormatException(String message) {
        super(message);
    }
}
