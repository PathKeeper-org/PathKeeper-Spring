package com.pathkeeper.backend.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "존재하지 않는 회원입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U002", "이미 사용 중인 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "U003", "비밀번호가 틀렸습니다."),

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),

    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "SC001", "인증이 필요합니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "SC002", "해당 요청에 권한이 없습니다."),

    MISSING_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "CK001", "쿠키에 refreshToken이 없습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "CK002", "유효하지 않거나 만료된 refreshToken입니다. "),

    INVALID_INVITE_CODE(HttpStatus.BAD_REQUEST, "I001", "유효하지 않거나 만료된 초대 코드입니다."),
    CANNOT_LINK_SELF(HttpStatus.BAD_REQUEST, "I002", "자신의 코드는 등록할 수 없습니다."),

    PARTNER_NOT_LINKED(HttpStatus.NOT_FOUND, "U004", "연결된 파트너가 없습니다."),

    LOCATION_NOT_FOUND(HttpStatus.NOT_FOUND, "L001", "위치 기록이 없습니다."),
    INVALID_GPS_COORDINATE(HttpStatus.BAD_REQUEST, "G001", "유효하지 않은 GPS 좌표입니다."),
    GPS_SPEED_ANOMALY(HttpStatus.BAD_REQUEST, "G002", "비정상적인 GPS 이동이 감지되어 위치를 저장하지 않았습니다."),

    SAFE_ZONE_NOT_FOUND(HttpStatus.NOT_FOUND, "SZ001", "안심존이 존재하지 않습니다."),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S001", "서버 내부에 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}