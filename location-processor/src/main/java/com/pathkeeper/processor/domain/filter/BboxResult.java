// BboxResult.java
package com.pathkeeper.processor.domain.filter;

/**
 * Bounding Box 검사 결과
 */
public enum BboxResult {

    /** Bounding Box 안 - 다각형 검사 필요 */
    INSIDE_BBOX,

    /** Bounding Box 밖 - 다각형 안에 있을 수 없음 (확정 OUTSIDE) */
    OUTSIDE,

    /** 안심존 정보 없음 (신규 사용자 등) */
    UNKNOWN
}