package kr.co.wisenut.textminer.common.resource;

import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * tm-v3-manager 에서 관리하는 storage resource 타입(파일)
 */
@Getter
public enum StorageResourceType {
    /**
     * 컬렉션 리소스(파일)
     */
    COLLECTION("컬렉션", Collections.unmodifiableSet(new HashSet<>(Arrays.asList("csv", "json")))),
//    COLLECTION("컬렉션", Collections.unmodifiableSet(new HashSet<>(Arrays.asList("csv", "json", "scd")))),

    /**
     * 사전 리소스(파일)
     */
    DICTIONARY("사전", Collections.unmodifiableSet(new HashSet<>(Arrays.asList("txt", "text", "csv")))),

    /**
     * 프로젝트 리소스(파일)
     */
    PROJECT("프로젝트", Collections.EMPTY_SET);

    /**
     * 레이블
     */
    final String label;

    /**
     * 허용하는 리소스 유형
     */
    final Set<String> accept;

    StorageResourceType(String label, Set<String> accept) {
        this.label = label;
        this.accept = accept;
    }

}
