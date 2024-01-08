package kr.co.wisenut.util.document.parser;

import kr.co.wisenut.util.document.Document;
import kr.co.wisenut.util.document.UnsupportedDocumentFormatException;

import java.io.*;
import java.util.List;

/**
 * Created by Jongho Lee on 2018-03-20.
 */
public class SCDParser implements DocumentParser, AutoCloseable {
    private static final String DOCID_FIELD = "<DOCID>";
    protected static final int MAX_BUFFER_SIZE = 65536;
    protected File file;
    protected BufferedReader reader;

    //indicate the range of a document in SCD by line numbers
    private int docLineStart = 0;
    private int docLineEnd = 0;
    private List<String> fields;

    public SCDParser(File file, List<String> fields) throws IOException {
        this(file, "UTF8", fields);
    }

    public SCDParser(File file, String encoding, List<String> fields) throws IOException {
        this.file = file;
        this.reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
        this.fields = fields;
        guaranteeNoUTF8BOMCharacter();
    }

    private void guaranteeNoUTF8BOMCharacter() throws IOException {
        reader.mark(4);
        if('\uFEFF' != reader.read())
            reader.reset();
    }

    public boolean hasNext() throws IOException {
        return reader.ready();
    }

    public void close() throws IOException {
        if(reader != null){
            reader.close();
        }
        reader = null;
    }

    private boolean moveToReaderPosition(String fieldname) throws IOException {
        docLineStart = docLineEnd + 1;
        while (true) {
            reader.mark(MAX_BUFFER_SIZE);
            String line = reader.readLine();
            if (line == null)
                return false;
            if (line.startsWith(fieldname)) {
                reader.reset();
                return true;
            }
            docLineStart++;
        }
    }

    private String loadRawTextOfNextDocument() throws IOException {
        if (!moveToReaderPosition(DOCID_FIELD))
            return "";
        docLineEnd = docLineStart;
        StringBuilder builder = new StringBuilder();
        builder.append(reader.readLine()); //DOCID Field
        docLineEnd++;
        //read until next DOCID Field
        while (true) {
            reader.mark(MAX_BUFFER_SIZE);
            String line = reader.readLine();
            if (line == null)
                break;
            if (line.startsWith(DOCID_FIELD)) {
                docLineEnd--;
                reader.reset();
                break;
            }
            builder.append(line);
            builder.append(String.format("%n"));  // to keep new-line characters in the original document
            docLineEnd++;
        }
        return builder.toString();
    }

    private Document buildDocument(String rawText) throws UnsupportedDocumentFormatException {
        if (rawText.length() > 0) {
            Document newDocument = new Document();
            List<String> colConfigFieldList = fields;
            final int size = colConfigFieldList.size();
            for (int cursor = 0; cursor < size - 1; cursor++) {
                String currentFieldName = colConfigFieldList.get(cursor);
                String nextFieldName = colConfigFieldList.get(cursor + 1);
                int index = rawText.indexOf("<" + nextFieldName + ">");
                if (index == -1)
                    throw new UnsupportedDocumentFormatException(String.format("[%s] Field not found, line[%d ~ %d], File[%s]",
                            nextFieldName, docLineStart, docLineEnd, file.getName()));
                String currentFieldValue = rawText.substring(0, index).substring(("<" + currentFieldName + ">").length()).trim();
                newDocument.addField(new Document.Field(currentFieldName, currentFieldValue));
                rawText = rawText.substring(index);
            }
            //handle last tag
            String lastFieldName = colConfigFieldList.get(size - 1);
            String lastFieldContent = rawText.substring(("<" + lastFieldName + ">").length()).trim();
            newDocument.addField(new Document.Field(lastFieldName, lastFieldContent));
            return newDocument;
        } else
            throw new IllegalStateException(file.toString());
    }

    public Document nextDocument() throws IOException, UnsupportedDocumentFormatException {
        if (reader != null) {
            return buildDocument(loadRawTextOfNextDocument());
        }
        throw new IllegalStateException("File should be opened at first.");
    }
}
