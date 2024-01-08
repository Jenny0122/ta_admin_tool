package kr.co.wisenut.textminer.common.resource;

/**
 * How to handle errors on import.<br>
 * <b>컬렉션 > 문서 업로드</b> 중 <b>오류 문서</b> 발견 시 처리 방안
 *
 * <h3>문서 오류 유형</h3>
 * <ol>
 *     <li></li>
 * </ol>
 *
 */
public enum ImportErrorHandle {

    /**
     * 문서 파싱 or db insert 오류 발생시,
     * 해당 문서 건너뛰고 리포팅
     */
    SKIP("오류 건너뛰기"),

    /**
     * 문서 파싱 or db insert 오류 발생시,
     * 업로드 강제 중단 (throw).
     */
    STOP("파일 업로드 중지"),
    ;
    //STOP_ROLLBACK("파일 업로드 취소"),

    final String label;

    ImportErrorHandle(String label) {
        this.label = label;
    }
}
