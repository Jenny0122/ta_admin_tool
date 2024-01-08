package kr.co.wisenut.textminer.common.resource;

import lombok.Getter;

/**
 * 진행 상태
 */
@Getter
public enum ProgressState {
    WAITING("대기중"),
    IN_PROGRESS("진행중"),
    SUCCESS("전체 성공"),
    PARTIAL_SUCCESS("부분 성공"),
    FAILURE("실패"),
    ;

    /**
     * 레이블
     */
    final String label;

    ProgressState(String label) {
        this.label = label;
    }


}
