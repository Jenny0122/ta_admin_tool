package kr.co.wisenut.exception;

import kr.co.wisenut.textminer.common.vo.ImportProgressVo;
import lombok.Data;

/**
 * text-miner 리소스 예외
 */
@Data
public class TMResourceException extends RuntimeException {

    private final Class<?> classTMResource;

    private final Object id;

    public TMResourceException(String message, ImportProgressVo resource) {
        super(message);
        this.classTMResource = resource.getClass();
        this.id = resource.getImportId();
    }

    public TMResourceException(String message, Class<?> classTMResource, Object id) {
        super(message);
        this.classTMResource = classTMResource;
        this.id = id;
    }

}
