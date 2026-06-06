// StateTransition.java
package com.pathkeeper.processor.domain.state;

public enum StateTransition {

    /** 변화 없음 */
    NO_CHANGE,

    /** 의심 단계 (히스테리시스 카운터 증가 중) */
    PENDING,

    /** INSIDE → OUTSIDE 확정 (이탈) */
    INSIDE_TO_OUTSIDE,

    /** OUTSIDE → INSIDE 확정 (복귀) */
    OUTSIDE_TO_INSIDE,

    /** 신규 사용자 초기화 */
    INITIALIZED
}