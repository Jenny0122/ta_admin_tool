package kr.co.wisenut.util.document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Jongho Lee on 2018-03-16.
 */
public class Document implements Serializable {
    public static abstract class Format {
        protected String delimiter = "";
        protected String prefix = "";
        protected String suffix = "";
        
        public abstract String format(Field field);
        
        
        public static final Format DEFAULT_FORMAT = new Format() {
            {
                this.delimiter = "\n";
            }
            
            @Override
            public String format(Field field) {
                return String.format("[%s]%s", field.getName(), field.getContent());
            }
        };
    }
    
    protected Map<String, Field> fields;
    
    public Document() {
        this.fields = new LinkedHashMap<>();
    }
    
    public final void addField(Field newField) {
        fields.put(newField.getName(), newField);
    }
    
    public final Field getField(String fieldName) {
        return fields.get(fieldName);
    }
    
    public final List<Field> getFieldList() {
        return new ArrayList<>(fields.values());
    }
    
    public final Field removeField(String fieldName) {
        return fields.remove(fieldName);
    }

    public final boolean containsKey(String fieldName) {
        return fields.containsKey(fieldName);
    }
    
    public String toString() {
        return this.format(Format.DEFAULT_FORMAT);
    }
    
    public final String format(Format format) {
        List<Field> fieldList = getFieldList();
        return fieldList.stream().map(format::format).collect(Collectors.joining(format.delimiter, format.prefix, format.suffix));
    }
    
    
    public static class Field implements Serializable {
        private String name;
        private String content;
        
        public Field() {
        }
        
        public Field(String name, String content) {
            this.name = name;
            this.content = content;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
    }
}
