# Qnect 백엔드 — 프로젝트 가이드

Qnect 백엔드 — 대학생 인맥 관리 앱. 두 명이 도메인을 나눠 작업.
**현재 단계: 개발자 A의 Phase 2~3 → `domain/quiz`(AI 퀴즈) · `domain/daily`(데일리 밸런스 + 친구 통계).**

## 역할 경계
- **전 도메인 단독 작업.** `global/` + 모든 `domain/*`(user·profile·interest·quiz·daily·friend·group·notification·point·reminder)를 직접 구현·수정한다. 과거 A/B 분담은 종료 — **B 도메인 수정 금지 규칙은 없다.** 리마인드 스케줄러 포함 전 영역을 다룬다.
- 기존 경계 포트(`PointPort`/`FriendQueryPort` 등)는 순환참조 회피·경계 명확화에 유용하면 유지하되, 불필요하면 직접 service 호출로 단순화해도 된다. (예: `QuizService`는 `ReminderService`가 의존하므로 친구/리마인드 조회를 여기에 직접 넣으면 순환참조가 생긴다 — 별도 빈으로 분리.)
- 도메인 경계 자체(타 도메인 엔티티 import 금지, service/DTO/ID로만 접근)는 계속 지킨다.

## 스택 (실제 빌드 기준)
Java 21 / **Spring Boot 4.1** / Gradle / MySQL 8 / Spring Security 7 + JWT(jjwt 0.12, BCrypt) / Spring Data JPA / Bean Validation / springdoc-openapi 3.x / Lombok. base package `com.qnectdk`.
> Security DSL은 **람다 전용**. web starter는 `spring-boot-starter-webmvc`. 외부 HTTP는 **새 의존성 없이** Spring `RestClient` + Jackson `ObjectMapper`. jjwt 0.11 이하 deprecated API 금지.

## 개발 환경
**WSL/Linux(bash)에서 개발·빌드.** Kiro 훅(`.kiro/hooks/`)은 `gradlew`를 직접 호출한다. PowerShell 훅 금지.

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
- **임의 커밋 금지.** 사용자가 "커밋해"라 명시할 때만 `git commit`/`--amend`/force-push/`reset --hard`를 수행한다.
- Phase 1/`global` 코드 임의 수정 금지(연동 필요 시 명시). "더 멋진" 패턴 임의 도입 금지 — 이 문서·컨벤션 steering과 일관성이 최우선.
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
도메인별 모범 예시는 컨벤션 steering(`convention-controller`·`convention-entity`·`convention-exception`). 새 도메인 추가 절차는 `add-domain` steering(`#add-domain`으로 수동 첨부). 규칙이 바뀌면 이 파일과 위 문서를 함께 갱신.
