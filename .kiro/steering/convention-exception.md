---
inclusion: fileMatch
fileMatchPattern: '**/{exception,ErrorCode,*Exception,GlobalExceptionHandler}*.java'
---

# 예외 처리 컨벤션

- 도메인 위반은 `throw new BusinessException(ErrorCode.XXX)`. 의미 없는 `RuntimeException` 금지.
- `ErrorCode`는 `(HttpStatus, message)`를 갖는 enum. 새 케이스는 여기에 추가.
- `@RestControllerAdvice`는 `GlobalExceptionHandler` **하나만**. 새 핸들러 메서드만 추가.
- 실패 응답도 `ApiResponse.fail(code)` 형태로 통일.

```java
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    DUPLICATE_PHONE(HttpStatus.CONFLICT, "이미 가입된 전화번호입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "전화번호 또는 비밀번호가 올바르지 않습니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;
}
```

```java
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    public ErrorCode getErrorCode() { return errorCode; }
}
```

던지는 쪽:

```java
userRepository.findByPhone(phone)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

if (userRepository.existsByPhone(phone)) {
    throw new BusinessException(ErrorCode.DUPLICATE_PHONE);
}
```

핸들러(요지):

```java
@ExceptionHandler(BusinessException.class)
public ResponseEntity<ApiResponse<Void>> handle(BusinessException e) {
    ErrorCode code = e.getErrorCode();
    return ResponseEntity.status(code.getStatus()).body(ApiResponse.fail(code));
}
```
