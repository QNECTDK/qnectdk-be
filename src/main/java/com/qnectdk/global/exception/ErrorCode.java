package com.qnectdk.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    RESOURCE_CONFLICT(HttpStatus.CONFLICT, "이미 존재하거나 현재 상태와 충돌하는 요청입니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    // 인증 / 토큰
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),

    // user
    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다."),
    DUPLICATE_PHONE(HttpStatus.CONFLICT, "이미 가입된 전화번호입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    // profile
    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "프로필을 찾을 수 없습니다."),

    // interest
    INTEREST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 관심사가 포함되어 있습니다."),

    // storage
    INVALID_FILE(HttpStatus.BAD_REQUEST, "유효하지 않은 파일입니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),

    // quiz
    QUIZ_NOT_FOUND(HttpStatus.NOT_FOUND, "퀴즈를 찾을 수 없습니다."),
    QUIZ_ATTEMPT_NOT_FOUND(HttpStatus.NOT_FOUND, "퀴즈 응시 기록을 찾을 수 없습니다."),
    QUIZ_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 퀴즈에 대한 권한이 없습니다."),
    QUIZ_NOT_SOLVABLE(HttpStatus.FORBIDDEN, "프로필을 먼저 완성해야 퀴즈를 풀 수 있습니다."),
    QUIZ_INVALID_CONTENT(HttpStatus.BAD_REQUEST, "퀴즈 구성이 올바르지 않습니다."),
    QUIZ_GENERATION_FAILED(HttpStatus.BAD_GATEWAY, "퀴즈 자동 생성에 실패했습니다."),

    // daily
    DAILY_QUIZ_NOT_FOUND(HttpStatus.NOT_FOUND, "오늘의 데일리 퀴즈가 없습니다."),
    DAILY_ALREADY_ANSWERED(HttpStatus.CONFLICT, "이미 오늘의 데일리에 답했습니다."),
    DAILY_NOT_ANSWERED_YET(HttpStatus.FORBIDDEN, "먼저 답해야 결과를 볼 수 있습니다."),

    // friend (B 도메인)
    FRIENDSHIP_NOT_FOUND(HttpStatus.NOT_FOUND, "친구 요청을 찾을 수 없습니다."),
    ALREADY_FRIENDS(HttpStatus.CONFLICT, "이미 친구이거나 요청이 존재합니다."),
    FRIENDSHIP_NOT_PENDING(HttpStatus.CONFLICT, "대기 중인 요청만 처리할 수 있습니다."),
    NOT_FRIENDSHIP_ADDRESSEE(HttpStatus.FORBIDDEN, "요청을 받은 사람만 수락/거절할 수 있습니다."),
    MEMO_NOT_FOUND(HttpStatus.NOT_FOUND, "메모를 찾을 수 없습니다."),

    // group (B 도메인)
    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "그룹을 찾을 수 없습니다."),
    NOT_GROUP_OWNER(HttpStatus.FORBIDDEN, "본인의 그룹만 관리할 수 있습니다."),
    DUPLICATE_GROUP_NAME(HttpStatus.CONFLICT, "이미 같은 이름의 그룹이 있습니다."),
    NOT_ACCEPTED_FRIEND(HttpStatus.BAD_REQUEST, "수락된 친구만 그룹에 추가할 수 있습니다."),
    ALREADY_GROUP_MEMBER(HttpStatus.CONFLICT, "이미 그룹에 추가된 친구입니다."),
    GROUP_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "그룹에 없는 멤버입니다.");

    private final HttpStatus status;
    private final String message;
}