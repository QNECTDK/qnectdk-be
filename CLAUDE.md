# CLAUDE.md

Qnect 백엔드 — 대학생 인맥 관리 앱. 두 명이 도메인을 나눠 작업.
**현재 단계: 개발자 A의 Phase 2~3 → `domain/quiz`(AI 퀴즈) · `domain/daily`(데일리 밸런스 + 친구 통계).**

## 역할 경계
- **나(A):** `global/`(완료) + `domain/user·profile·interest`(Phase 1, 완료) + **`domain/quiz·daily`(현재).**
- **B(건드리지 말 것):** `domain/friend·group`(완료·병합) + 추후 `notification·point` + 리마인드 스케줄러. B 코드를 A 스타일로 리팩터링 금지.
- **A→B 호출(인터페이스만):** 첫 퀴즈 완료 시 포인트 적립(`PointPort`), 데일리 친구 통계용 친구 ID 목록. 시그니처 미확정이면 합리적으로 가정하고 `// B 합의 필요` 주석.
- **B→A 노출:** `QuizService.getActiveQuiz(ownerId)` · `generateReminderQuiz(ownerId)` — B의 리마인드 스케줄러가 호출(스케줄러 자체는 A가 안 만든다).

## 스택 (실제 빌드 기준)
Java 21 / **Spring Boot 4.1** / Gradle / MySQL 8 / Spring Security 7 + JWT(jjwt 0.12, BCrypt) / Spring Data JPA / Bean Validation / springdoc-openapi 3.x / Lombok. base package `com.qnectdk`.
> Security DSL은 **람다 전용**. web starter는 `spring-boot-starter-webmvc`. 외부 HTTP는 **새 의존성 없이** Spring `RestClient` + Jackson `ObjectMapper`. jjwt 0.11 이하 deprecated API 금지.

## 개발 환경
**WSL/Linux(bash)에서 개발·빌드.** `.claude/` 훅은 전부 bash(`.sh`). PowerShell 훅 금지. (settings.json 변경은 새 세션부터 활성화.)

## 아키텍처 (불변)
- **도메인형 패키지:** `domain/{name}/{controller·service·repository·entity·dto}`. 레이어를 최상위에 두지 않는다.
- **도메인 경계 = `Long` ID 참조. 타 도메인 엔티티 import/`@ManyToOne` 금지** (같은 도메인 내부 연관관계만 허용). 타 도메인 정보는 그 도메인의 **service를 호출**해 DTO/ID로 받는다 (예: `ProfileService` → `UserQueryService`).
- 계층: `controller(얇게) → service(@Transactional) → repository`.
- **`SecurityFilterChain` 빈은 앱 전체에 하나**(`global/config/SecurityConfig`). **`@RestControllerAdvice`는 `GlobalExceptionHandler` 하나만** — 새 핸들러 메서드만 추가.
- 모든 (A의) 응답은 `ApiResponse<T>`로 감싼다(`ok`/`fail`). 시간 필드는 `BaseTimeEntity` 상속. `global/`은 도메인을 import하지 않는다.

## 코드 패턴 (A의 모든 코드)
- **엔티티:** `@Entity @Table` + `@Getter @NoArgsConstructor(PROTECTED) @Builder`, `@Id @GeneratedValue(IDENTITY)`, 정적 팩토리 `create(...)`, 상태 변경은 도메인 메서드. **setter 금지.** enum은 `@Enumerated(STRING)`.
- **service:** `@RequiredArgsConstructor @Transactional(readOnly = true)` 클래스, 쓰기 메서드에 `@Transactional`. `@Value` 주입 시에만 명시적 생성자.
- **DTO = `record`** + `@Schema`, 변환은 정적 `from`/`of`. 엔티티를 컨트롤러에 노출 금지.
- **컨트롤러:** `@Tag`/`@Operation`(springdoc 필수), `@AuthenticationPrincipal CustomUserDetails`로 userId, `ApiResponse.ok(...)` 반환. 경로는 소문자 복수형.
- 예외: 도메인 위반은 `BusinessException(ErrorCode)`. 새 상황은 **`ErrorCode` enum에 추가**(의미 없는 `RuntimeException` 금지). 입력 검증은 컨트롤러 Bean Validation.
- 매직값은 상수/enum. 조기 반환으로 중첩↓. **YAGNI** — 안 쓰는 필드/추상화 금지. 인터페이스는 실제 2곳 이상에서 필요할 때만.
- 공통 유틸 재사용(중복 구현 금지): `PublicCodeGenerator` / `ZodiacUtil`·`AgeUtil`(띠·나이는 저장 안 하고 `birthDate`로 계산).

## 금지
- **임의 커밋 금지.** 사용자가 "커밋해"라 명시할 때만. `.claude/hooks/block-commit.sh`가 `git commit`/`--amend`/force-push/`reset --hard`를 기본 차단, `.claude/.commit-allowed` 마커 있을 때만 예외.
- Phase 1/`global` 코드 임의 수정 금지(연동 필요 시 명시). B 도메인 구현·수정 금지. "더 멋진" 패턴 임의 도입 금지 — 이 문서·`conventions/`와 일관성이 최우선.
- **Gemini API 키 클라이언트 노출·하드코딩 절대 금지** — 서버 env(`GEMINI_API_KEY`)만. 미설정 시 Mock 클라이언트로 동작.

## 빌드 / 실행 / 테스트
```bash
./gradlew compileJava   # 컴파일만 (DB 불필요, 가장 빠른 점검)
./gradlew build         # 빌드+테스트 (contextLoads는 MySQL 필요)
./gradlew bootRun       # 실행 (docker compose up -d 로 MySQL 먼저)
```
API http://localhost:8080 · Swagger http://localhost:8080/swagger-ui.html. env는 `.env`(미추적), 기본값은 `application.yml` fallback(`.env.example` 참조).

## 용어집
- **publicCode**: QR/공유용 고유 코드(`PublicCodeGenerator`). **띠/나이**: 저장 안 함, `birthDate`로 계산.
- **퀴즈**: 첫 만남(`FIRST_MEET`, 본인 편집본) / 리마인드(`REMIND`, D+30 Gemini 재생성). 문제 3~5개, `isRequired`는 항상 출제. 채점=정답률(score/total) → **케미 점수** 근거. 풀려면 응시자 프로필 완성 필요.
- **데일리**: 전체 사용자 매일 1개 **밸런스 게임(양자택일 A/B 전용)**. **내가 답해야 결과(비율) 공개.** 통계 = 전체 비율 + 친구 비율.

## 패턴 예시 / 절차
도메인별 모범 예시는 `.claude/conventions/`(entity·controller·exception). 새 도메인 추가 절차는 `.claude/skills/add-domain.md`. 규칙이 바뀌면 이 파일과 위 문서를 함께 갱신.
