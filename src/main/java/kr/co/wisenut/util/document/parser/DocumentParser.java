package kr.co.wisenut.util.document.parser;

import kr.co.wisenut.util.document.Document;
import kr.co.wisenut.util.document.UnsupportedDocumentFormatException;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by Jongho Lee on 2018-03-20.
 */
public interface DocumentParser extends Closeable {
    public boolean hasNext() throws IOException;
    public void close() throws IOException;
    public Document nextDocument() throws IOException, UnsupportedDocumentFormatException;
}
