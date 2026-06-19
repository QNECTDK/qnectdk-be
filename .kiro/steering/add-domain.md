---
inclusion: manual
---

# 새 도메인 추가 절차 (개발자 A)

프로젝트 가이드를 가볍게 유지하기 위한 반복 절차서. 새 도메인을 추가할 때 이 순서를 따른다.
모범 예시는 병합된 기존 도메인(`domain/user`, `domain/profile`, `domain/interest`)을 그대로 인용한다 — 새 패턴을 발명하지 말 것.

## 0. 경계 확인
- 이 도메인이 A 소유인지 확인(`quiz`·`daily`·`user`·`profile`·`interest`). B 소유(`friend`·`group`·`point`·`notification`)면 만들지 않는다.
- 타 도메인 데이터가 필요하면 그 도메인 **service를 호출**해 DTO/ID로 받는다(엔티티 import 금지).
  예: `UserQueryService.getById(userId) → UserSummary`, `FriendService.getFriends(userId)`.

## 1. 패키지
`com.qnectdk.domain.{name}/` 아래 `controller·service·repository·entity·dto` (외부 연동 시 `client`, 타 도메인 경계 포트는 `port`).

## 2. 엔티티 (`entity/`)
- `@Entity @Table(name = "...", uniqueConstraints = ...)`, 테이블/컬럼은 snake_case.
- `@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED) @Builder` + `extends BaseTimeEntity`.
- `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`. enum 필드는 `@Enumerated(EnumType.STRING)`.
- 생성은 빌더 기반 정적 팩토리 `create(...)`, 상태 변경은 의도가 드러나는 도메인 메서드. **setter 금지.**
- 타 도메인 참조는 `Long ...Id` 컬럼으로만(`@ManyToOne` 금지). 같은 도메인 내부 연관관계는 허용하되, 본 프로젝트는 `Long` ID + 별도 리포지토리 스타일이 우세하다(`UserInterest`).

예시: #[[file:src/main/java/com/qnectdk/domain/user/entity/User.java]]

## 3. 리포지토리 (`repository/`)
- `interface XxxRepository extends JpaRepository<Entity, Long>`.
- 파생 쿼리 우선(`findByOwnerIdAndType...`), 복잡하면 `@Query`(text block) + `@Param`.

## 4. 서비스 (`service/`)
- `@Service @RequiredArgsConstructor @Transactional(readOnly = true)` 클래스. 쓰기 메서드에만 `@Transactional`.
- `@Value` 주입이 필요하면 `@RequiredArgsConstructor` 대신 명시적 생성자.
- 도메인 위반은 `throw new BusinessException(ErrorCode.XXX)`. 오케스트레이션만, 도메인 규칙은 엔티티로.

## 5. DTO (`dto/`)
- `record` + `@Schema`. 엔티티→DTO 변환은 정적 `from`/`of`. 엔티티를 컨트롤러에 노출 금지.
- 요청 DTO에 Bean Validation(`@NotBlank`, `@NotNull`, `@Size`, `@Min` 등).

## 6. 컨트롤러 (`controller/`)
- `@Tag @RestController @RequestMapping("/api/{소문자복수형}") @RequiredArgsConstructor`.
- 메서드마다 `@Operation`(springdoc). 인증 사용자는 `@AuthenticationPrincipal CustomUserDetails user` → `user.getUserId()`.
- 반환은 항상 `ApiResponse.ok(...)`. 얇게: 요청 받기 → service 호출 → 응답. 비즈니스 로직 금지.

## 7. ErrorCode 추가
- `ErrorCode` enum에 새 케이스 추가(`XXX(HttpStatus.YYY, "메시지")`).
- 새 `@RestControllerAdvice`/핸들러 클래스를 만들지 않는다. 필요하면 `GlobalExceptionHandler`에 메서드만 추가.

## 8. (외부 연동 시) 추상화
- 외부 API(Gemini 등)는 인터페이스로 추상화(`client/`). Mock 구현을 기본 활성, 실제 구현은 `@ConditionalOnProperty`. **API 키는 서버 env만**(하드코딩/클라이언트 노출 금지).
- 타 도메인(B) 호출은 consumer-side 포트(`port/`) + 임시 어댑터/스텁으로 두고 `// B 합의 필요` 주석. B가 빈을 제공하면 `@ConditionalOnMissingBean`으로 자동 대체되게 한다.

## 9. 보안 경로
- 인증이 필요한 엔드포인트는 SecurityConfig 수정 불필요(기본 `authenticated`). **공개가 필요할 때만** `SecurityConfig.PUBLIC_PATHS`에 추가.

## 10. 확인
```bash
./gradlew compileJava   # 컴파일만 (DB 불필요)
```
완료 후 `convention-reviewer` 에이전트로 컨벤션 준수 여부를 검수한다.
