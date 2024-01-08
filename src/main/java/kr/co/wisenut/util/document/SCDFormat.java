package kr.co.wisenut.util.document;


/**
 * Created by Jongho Lee on 2018-03-16.
 */
public class SCDFormat extends Document.Format {
    private static SCDFormat instance = null;
    public static SCDFormat getInstance(){
        if(instance == null)
            instance = new SCDFormat();
        return instance;
    }
    private SCDFormat() {
        suffix = "";
        prefix = "";
        delimiter = "\n";
    }
    
    @Override
    public String format(Document.Field field) {
        return "<" + field.getName() + ">" + field.getContent();
    }
}
