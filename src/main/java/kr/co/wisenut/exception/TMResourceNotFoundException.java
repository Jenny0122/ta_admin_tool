package kr.co.wisenut.exception;

/**
 * 해당 리소스가 존재하지 않는 경우
 */

public class TMResourceNotFoundException extends TMResourceException {

    public TMResourceNotFoundException(Class<?> classTMResource, Object id) {
        super(String.format("%s(id=%s) Not Found.", classTMResource.getSimpleName(), id), classTMResource, id);
    }

    public TMResourceNotFoundException(Class<?> classTMResource, Object parentId, Class<?> classTMSubOrData, Object id) {
        super(String.format("%s(id=%s) in %s(id=%s) Not Found.", classTMSubOrData.getSimpleName(), id, classTMResource.getSimpleName(), parentId), classTMResource, id);
    }

    public TMResourceNotFoundException(Class<?> classTMResource, Object parentId, Class<?> classTMSubOrData, String condition) {
        super(String.format("%s(condition=%s) in %s(id=%s) Not Found.",
                classTMSubOrData.getSimpleName(), condition, classTMResource.getSimpleName(), parentId),
                classTMSubOrData, condition);
    }


}
