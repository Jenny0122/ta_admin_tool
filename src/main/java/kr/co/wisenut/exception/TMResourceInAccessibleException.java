package kr.co.wisenut.exception;

import kr.co.wisenut.textminer.common.vo.ImportProgressVo;

/**
 * 해당 리소스에 접근 권한이 없을 경우. not public and not owner,
 */
public class TMResourceInAccessibleException extends TMResourceException {

    public TMResourceInAccessibleException(ImportProgressVo resource) {
        super(String.format("%s(id=%s) is inAccessible.", resource.getClass().getSimpleName(), resource.getImportId()), resource);
    }

    public TMResourceInAccessibleException(Class<?> classTMResource, Object id) {
        super(String.format("%s(id=%s) is inAccessible.", classTMResource, id), classTMResource, id);
    }

}
