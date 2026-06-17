# CLAUDE.md

Qnect 백엔드 (대학생 인맥 관리 앱). 개발자 두 명이 도메인을 나눠 작업한다.
**나(개발자 A) 담당:** `global/` 공통 인프라 + `domain/user` · `domain/profile` · `domain/interest`.
**개발자 B 담당(건드리지 말 것):** `domain/friend` · `domain/group` (추후 포인트·알림·퀴즈).

## 기술 스택 (실제 빌드 기준)

- Java 21 / **Spring Boot 4.1.x** / Gradle (Wrapper)
- MySQL 8 (로컬은 Docker, `docker compose up -d`)
- Spring Security **7** + JWT(jjwt 0.12.x), 비밀번호 BCrypt
- Spring Data JPA, Bean Validation
- API 문서: **springdoc-openapi 3.x** (Swagger UI)
- Lombok
- base package: `com.qnectdk` (`com.qnect` 아님)

> Spring Boot 4.x이므로 Security DSL은 **람다 전용**(`csrf(AbstractHttpConfigurer::disable)` 등),
> web starter는 `spring-boot-starter-webmvc`, springdoc은 `springdoc-openapi-starter-webmvc-ui:3.x`를 쓴다.
> 0.11 이하 jjwt deprecated API(`setSubject`, `signWith(key, alg)`, `parserBuilder`) 금지.

## 개발 환경

- **WSL/Linux(bash)에서 개발·빌드한다.** Windows 호스트에서 직접 작업하지 말 것(라인엔딩·gradlew 권한 문제).
- `.claude/` 하네스와 훅은 모두 bash 기준(`.sh`). PowerShell 훅은 쓰지 않는다.

## 아키텍처 규칙

- **도메인형 패키지 구조.** 레이어(`controller`/`service`)를 최상위에 두지 않는다.
- **도메인 경계는 `Long` ID 참조.** 타 도메인 엔티티를 import/`@ManyToOne` 하지 마라.
  (단 같은 도메인 내부 연관관계는 자유. user가 A의 루트.)
- 계층: `controller → service → repository`. 트랜잭션 경계는 service의 `@Transactional`.
- **`SecurityFilterChain` 빈은 앱 전체에 단 하나** (A의 `global/config/SecurityConfig`가 소유).
- **`@RestControllerAdvice`는 `GlobalExceptionHandler` 하나만.** 중복 금지.
- 모든 (A의) API 응답은 공통 `ApiResponse<T>`로 감싼다.
- 시간 필드는 `BaseTimeEntity`(JPA Auditing)로 자동 관리.
- `global/`은 도메인 패키지를 import하지 않는다 (의존 방향: domain → global).

## 코드 품질 원칙 (A의 모든 코드에 적용)

- **객체 생성은 빌더(`@Builder`).** 엔티티는 `@Builder` + `@NoArgsConstructor(access = PROTECTED)`.
  무분별한 `@AllArgsConstructor`/public 기본 생성자 금지. 의도는 정적 팩토리(`User.create(...)`)로.
- **setter 금지.** 상태 변경은 의도가 드러나는 도메인 메서드(`profile.updateBasicInfo(...)`)로.
- **DTO는 `record`.** 엔티티를 컨트롤러에 노출하지 말 것. 변환은 DTO 정적 메서드(`from(entity)`).
- **얇은 컨트롤러:** 요청 받기 → service 호출 → 응답. 비즈니스 로직 금지.
- 도메인 규칙은 가능한 한 엔티티 안으로(빈약한 도메인 모델 지양). service는 오케스트레이션.
- 검증/매핑/외부연동은 별도 컴포넌트(`Validator`, `StorageService` 등)로 분리.
- **YAGNI.** 안 쓰는 필드/메서드/추상화 금지. 인터페이스는 실제 2곳 이상에서 필요할 때만.
- 주석은 "왜"만. 매직 넘버·문자열은 상수/enum. 조기 반환으로 중첩 줄이기. 축약어(`req`,`tmp`) 지양.
- 예외: 도메인 위반은 `BusinessException` + `ErrorCode`. 의미 없는 `RuntimeException` 금지.
  입력 검증은 컨트롤러(Bean Validation), 비즈니스 불변식은 도메인/서비스.

패턴별 모범 예시는 `.claude/conventions/`(entity·controller·exception)를 참조.

## 금지 사항

- **임의 커밋 금지.** 사용자가 명시적으로 "커밋해"라고 요청할 때만 `git commit` 한다.
  작업이 끝나도 자동으로 커밋하지 말고, 변경사항은 워킹 트리에 둔 채 보고만 한다.
  - 강제 수단: `.claude/hooks/block-commit.sh` (PreToolUse 훅)이 `git commit`/`--amend`/force-push/`reset --hard`를
    기본 차단한다. 사용자가 커밋을 허용한 세션에서만 `.claude/.commit-allowed` 마커(gitignore)를 만들어 예외 처리한다.
    세션 종료 시 마커를 지우면 다음 세션은 다시 차단된다. (bash 훅, WSL/Linux 기준; settings.json 변경은 새 세션부터 활성화)
- B의 도메인(`friend`/`group` 등) 구현·수정 금지. B의 코드 스타일을 A 기준으로 리팩터링하지 마라.
- `SecurityFilterChain` 빈 중복 금지. `@RestControllerAdvice` 중복 금지.
- 임의의 "더 멋진" 패턴 도입 금지. 이 문서의 컨벤션과 일관성이 최우선.

## 빌드 / 실행 / 테스트

```bash
./gradlew compileJava     # 컴파일만 (가장 빠른 점검, DB 불필요)
./gradlew build           # 빌드 + 테스트
./gradlew test            # 테스트만
./gradlew bootRun         # 실행 (MySQL 필요: docker compose up -d)
```

- API: http://localhost:8080 · Swagger UI: http://localhost:8080/swagger-ui.html
- 환경변수는 `.env`(미추적)에서 주입, 기본값은 `application.yml` fallback. 목록은 `.env.example`.

## 도메인 용어집

- **publicCode**: QR/공유용 고유 코드 (URL-safe 영숫자, `users.public_code`). `PublicCodeGenerator`로 발급, 충돌 시 재생성.
- **띠(zodiac) / 나이**: DB에 저장하지 않는다. 응답 시 `birth_date`로 `ZodiacUtil` 등에서 계산.
- **관심사 칩(interest)**: 카테고리별 마스터 시드 + `user_interests` join. 내 관심사는 전체 교체(PUT).
- **friendship**: B 도메인. A는 참조하지 않는다.

## 규칙 변경 시

규칙이 추가/변경되면 이 파일과 `.claude/conventions/`를 갱신하는 것을 습관으로 삼아라.
