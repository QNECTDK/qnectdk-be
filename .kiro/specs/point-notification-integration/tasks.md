# Implementation Plan: point-notification-integration

## Overview

A 도메인(`profile`·`quiz`·`daily`·`user`)에 B 도메인(`point`·`notification`) 서비스 호출 연동과 A 기능 보강(내 퀴즈 삭제, 캐릭터 카탈로그 기반 프로필 이미지 설정)을 점진적으로 구현한다. 구현 순서는 기반 요소(enum·DTO·리포지토리·엔티티) → 서비스 → 컨트롤러 → 시더 → 검증 산출물 순이며, 각 단계는 이전 단계 위에 쌓이고 마지막에 컨트롤러·시더로 배선된다.

언어: **Java 21 / Spring Boot 4.1**(design.md가 Java 구현을 명시). 디자인에 Correctness Properties(P1~P9)가 있으므로 속성 기반 테스트(jqwik)를 선택 하위작업으로 포함한다.

## Tasks

- [x] 1. 기반 요소: 의존성·엔티티·카탈로그·DTO·리포지토리 (B 무수정)
  - [x] 1.1 build.gradle에 jqwik 테스트 의존성 추가
    - `testImplementation`에 jqwik(JUnit 5 연동)을 추가한다(운영 코드·B 도메인과 무관)
    - 추가가 불가하면 JUnit5 파라미터라이즈드 + 난수 100회 반복 동등 구성으로 대체할 수 있도록 주석으로 기록
    - _Requirements: 7.1_

  - [x] 1.2 Profile 엔티티 image_url 컬럼 길이 조정
    - `Profile.imageUrl`에 `@Column(name = "image_url", length = 512)` 적용(카탈로그 URL 저장 대비)
    - 그 외 엔티티 변경 없음(setter 금지, 기존 패턴 유지)
    - _Requirements: 6.2, 6.3_

  - [x] 1.3 CharacterImage enum 신규 작성 (카탈로그 단일 출처)
    - `profile` 도메인에 17종 상수(`character01`~`character17`, `characterId` + `imageUrl`) 정의
    - `all()` → `List<CharacterResponse>`(정확히 17건), `findById(String)` → `Optional<CharacterImage>` 구현
    - 서버 측 고정 정의로 동일 식별자 집합을 항상 동일하게 반환
    - _Requirements: 5.2, 5.3, 5.4, 6.2_

  - [x] 1.4 응답/요청 DTO 신규 작성
    - `CharacterResponse` record(`characterId`, `imageUrl`) + `@Schema`
    - `ProfileImageRequest` record(`@NotBlank String characterId`) + `@Schema`
    - _Requirements: 5.3, 6.1, 6.5_

  - [x] 1.5 QuizRepository에 존재 여부 조회 추가
    - `boolean existsByOwnerId(Long ownerId)` 선언(생애 1회 적립 판정 근거)
    - _Requirements: 2.2, 7.6_

  - [x] 1.6 UserRepository/UserQueryService에 전체 사용자 ID 조회 추가
    - `UserRepository`: `@Query("select u.id from User u") List<Long> findAllUserIds()` (엔티티 전체 로딩 회피)
    - `UserQueryService`: `getAllUserIds()`가 `userRepository.findAllUserIds()` 위임
    - _Requirements: 3.4, 7.6_

- [x] 2. ProfileService: 적립 연동 + 캐릭터 이미지 설정 + multipart 제거
  - [x] 2.1 ProfileService 본문 변경
    - 생성자에서 `storageService` 제거, `PointService pointService` 주입(`@Value` 때문에 명시적 생성자 유지)
    - `upsert`: 프로필 부재(`if (profile == null)`) 신규 생성 분기에서만 `pointService.earn(userId, PointPolicy.PROFILE_CREATE, PointReason.PROFILE_CREATE, null)` 1회 호출, 갱신 분기는 미적립
    - `getCharacters()` → `CharacterImage.all()`
    - `setCharacterImage(userId, characterId)`: `CharacterImage.findById` 무효 시 `BusinessException(INVALID_INPUT)`, 프로필 부재 시 `PROFILE_NOT_FOUND`, 유효 시 카탈로그 `imageUrl`을 `Profile.imageUrl`에 저장 후 반환
    - `updateImage(MultipartFile)` 및 관련 import 제거
    - 적립액은 `PointPolicy.PROFILE_CREATE` 상수 사용(없으면 매직넘버 `20` + `// 정책값` 주석)
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 5.2, 6.2, 6.3, 6.4, 6.6, 6.7_

  - [x] 2.2 StorageService/LocalStorageService 사용처 확인 후 정리
    - `ProfileService.updateImage` 제거 후 `StorageService`/`LocalStorageService`에 다른 참조가 없는지 검색·확인
    - 데드코드면 두 파일 제거, 다른 사용처가 있으면 보존(확인 결과를 근거로 결정)
    - _Requirements: 6.2_

  - [x]* 2.3 ProfileService 단위 테스트(Mockito)
    - 신규 생성 시 `earn` 1회(인자 검증)·갱신 시 0회·`earn` 예외 전파(1.5)
    - `getCharacters` 17건, `setCharacterImage` 유효 저장/무효 `INVALID_INPUT`/프로필 부재 `PROFILE_NOT_FOUND`
    - _Requirements: 1.1, 1.2, 1.5, 5.2, 6.2, 6.4, 6.6_

  - [x]* 2.4 P1 속성 테스트: 프로필 최초 생성 1회 적립
    - **Property 1: 프로필 최초 생성 1회 적립**
    - **Validates: Requirements 1.1, 1.2, 1.3**
    - 최소 100회 반복, B 서비스·리포지토리 mock, 주석 `// Feature: point-notification-integration, Property 1: ...`

  - [x]* 2.5 P8 속성 테스트: 캐릭터 카탈로그 고정성
    - **Property 8: 캐릭터 카탈로그 고정성**
    - **Validates: Requirements 5.2, 5.3, 5.4**
    - 최소 100회 반복, 주석 `// Feature: point-notification-integration, Property 8: ...`

  - [x]* 2.6 P9 속성 테스트: 캐릭터 선택 설정의 유효성 분기
    - **Property 9: 캐릭터 선택 설정의 유효성 분기**
    - **Validates: Requirements 6.2, 6.3, 6.4**
    - 최소 100회 반복, B 리포지토리 mock, 주석 `// Feature: point-notification-integration, Property 9: ...`

- [x] 3. ProfileController: 카탈로그 조회 추가 + 이미지 엔드포인트 교체
  - [x] 3.1 컨트롤러 매핑 변경
    - 기존 multipart `POST /me/image` 메서드와 `MultipartFile`·`RequestPart`·`MediaType` import 제거
    - `GET /characters` 추가(`ApiResponse<List<CharacterResponse>>`)
    - `PUT /me/image` 추가(`@Valid @RequestBody ProfileImageRequest`, `ApiResponse`로 래핑), `@Operation`/`@Tag` springdoc 주석 포함
    - _Requirements: 5.1, 5.5, 6.1, 6.7_

  - [x]* 3.2 ProfileController 슬라이스 테스트(@WebMvcTest)
    - `GET /characters` 17건, `PUT /me/image` `@NotBlank` 400(6.5)·무효 식별자 400(6.4), 미인증 401(5.6/6.8)
    - _Requirements: 5.6, 6.4, 6.5, 6.8_

- [x] 4. QuizWriter: 소프트 삭제용 public 메서드 추가
  - [x] 4.1 clearContentAndDeactivate(Quiz) 추가
    - 기존 private `deleteContent`(보기→문항 벌크 삭제) 재사용 + `quiz.deactivate()`로 `active=false` 전이
    - 호출부 트랜잭션 합류(REQUIRED), 퀴즈 행은 보존
    - _Requirements: 4.2, 4.3, 4.5_

- [x] 5. QuizService: 적립 연동 + 삭제 메서드
  - [x] 5.1 QuizService 본문 변경
    - `private final PointService pointService;` 필드 추가(`PointPort`는 유지)
    - `saveMyQuiz`: 저장 직전 `boolean firstSetup = !quizRepository.existsByOwnerId(ownerId)` 판정, 저장 후 `firstSetup`일 때만 `pointService.earn(ownerId, PointPolicy.QUIZ_FIRST_SETUP, PointReason.QUIZ_FIRST_SETUP, null)` 1회 호출
    - `deleteMyQuiz(ownerId)`: 활성 퀴즈 조회(`findFirstByOwnerIdAndActiveTrueOrderByIdDesc`), 없으면 `BusinessException(QUIZ_NOT_FOUND)`, 있으면 `quizWriter.clearContentAndDeactivate(quiz)` 호출
    - 적립액은 `PointPolicy.QUIZ_FIRST_SETUP` 상수 사용(없으면 매직넘버 `20` + `// 정책값` 주석)
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 4.2, 4.6, 4.7, 4.8_

  - [x]* 5.2 QuizService 단위 테스트(Mockito)
    - 최초(`existsByOwnerId=false`) `saveMyQuiz` 시 `earn` 1회·레코드 존재 시 0회·삭제 후 재설정 0회(2.4)
    - `deleteMyQuiz` 정상/`QUIZ_NOT_FOUND`(4.6)·`clearContentAndDeactivate` 위임 검증
    - _Requirements: 2.1, 2.3, 2.4, 4.6_

  - [x]* 5.3 P2 속성 테스트: 퀴즈 최초 설정 생애 1회 적립
    - **Property 2: 퀴즈 최초 설정 생애 1회 적립**
    - **Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5**
    - 최소 100회 반복, B 서비스·리포지토리 mock, 주석 `// Feature: point-notification-integration, Property 2: ...`

  - [x]* 5.4 P6 속성 테스트: 퀴즈 삭제 효과
    - **Property 6: 퀴즈 삭제 효과**
    - **Validates: Requirements 4.2, 4.4, 4.8**
    - 최소 100회 반복, 임의 문항·보기 구성, mock 사용, 주석 `// Feature: point-notification-integration, Property 6: ...`

  - [x]* 5.5 P7 속성 테스트: 퀴즈 삭제 후 레코드·응시 기록 보존
    - **Property 7: 퀴즈 삭제 후 레코드·응시 기록 보존**
    - **Validates: Requirements 4.5, 4.9**
    - 최소 100회 반복, 임의 응시 기록 집합, mock 사용, 주석 `// Feature: point-notification-integration, Property 7: ...`

- [x] 6. QuizController: 삭제 엔드포인트 추가
  - [x] 6.1 DELETE /me 추가
    - `@DeleteMapping("/me")`로 `quizService.deleteMyQuiz(user.getUserId())` 호출, `ApiResponse.ok()` 반환
    - `@Operation` springdoc 주석, `DeleteMapping` import 추가
    - _Requirements: 4.1, 4.10_

  - [x]* 6.2 QuizController 슬라이스 테스트(@WebMvcTest)
    - `DELETE /me` 매핑·`ApiResponse.ok` 검증, 미인증 401(4.11)
    - _Requirements: 4.1, 4.10, 4.11_

- [x] 7. DailyQuizSeeder: 신규 생성 시 전체 사용자 알림
  - [x] 7.1 notifyAllUsers 구현
    - `UserQueryService`·`NotificationService` 주입
    - 오늘 데일리가 이미 있으면 return(발송 0건), 신규 저장 시에만 발송
    - `getAllUserIds()` 순회하며 사용자당 `push(userId, NotificationType.DAILY_QUIZ, "오늘의 퀴즈가 생성되었습니다!", "지금 바로 풀어보세요!", dailyQuizId)` 1회, 개별 호출은 try/catch로 격리(실패 로깅 후 계속, 롤백 안 함)
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8_

  - [x]* 7.2 DailyQuizSeeder 단위 테스트(Mockito)
    - N명 → N건(인자 검증)·이미 존재 시 save·push 0건(3.5)·일부 `push` 예외 시 나머지 호출·예외 비전파(3.7)·`getAllUserIds` 호출 검증(3.4)
    - _Requirements: 3.2, 3.4, 3.5, 3.7_

  - [x]* 7.3 P3 속성 테스트: 데일리 알림 팬아웃 정확성
    - **Property 3: 데일리 알림 팬아웃 정확성**
    - **Validates: Requirements 3.1, 3.2, 3.3, 3.6**
    - 최소 100회 반복(N=0 포함), B 서비스 mock, 주석 `// Feature: point-notification-integration, Property 3: ...`

  - [x]* 7.4 P4 속성 테스트: 재실행 무발송
    - **Property 4: 재실행 무발송**
    - **Validates: Requirements 3.5**
    - 최소 100회 반복, mock 사용, 주석 `// Feature: point-notification-integration, Property 4: ...`

  - [x]* 7.5 P5 속성 테스트: 발송 실패 격리
    - **Property 5: 발송 실패 격리**
    - **Validates: Requirements 3.7**
    - 최소 100회 반복, 임의 실패 부분집합, mock 사용, 주석 `// Feature: point-notification-integration, Property 5: ...`

- [x] 8. 검증용 HTTP 산출물
  - [x] 8.1 http/point-notification.http 작성
    - 기존 `http/` 컨벤션(`@baseUrl`, `# @name login`, `Authorization: Bearer {{login.response.body.$.data.accessToken}}`, `###` 구분) 준수
    - 6개 흐름 배치: ①프로필 생성→`/points/balance`→`/points/transactions`(PROFILE_CREATE) ②퀴즈 설정→잔액/내역(QUIZ_FIRST_SETUP) ③데일리 후 `/notifications`(DAILY_QUIZ) ④`DELETE /quizzes/me`→`GET /quizzes/me`(QUIZ_NOT_FOUND) ⑤`GET /profiles/characters`(17종)→고른 식별자로 `PUT /profiles/me/image`→`GET /profiles/me`(imageUrl) ⑥무효 식별자 `PUT /profiles/me/image`(400)
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7, 8.8, 8.9_

- [x] 9. 빌드 점검 체크포인트
  - `./gradlew compileJava`로 컴파일 확인 후 `./gradlew test`로 단위/PBT 실행. 모든 테스트가 통과하는지 확인하고, 문제가 생기면 사용자에게 문의한다.
  - B 도메인(`domain/point`·`domain/notification`) 소스가 무수정 상태인지 최종 확인한다.

## Notes

- `*`로 표시된 하위작업은 선택(테스트)이며 MVP에서 건너뛸 수 있다. 코어 구현 작업은 선택 표시하지 않는다.
- **B 도메인 무수정:** `domain/point`·`domain/notification` 하위 파일은 추가·삭제·변경 0건을 유지한다. 연동은 `PointService.earn`·`NotificationService.push`의 기존 시그니처와 기존 enum 값(`PROFILE_CREATE`·`QUIZ_FIRST_SETUP`·`DAILY_QUIZ`)만 사용한다.
- **도메인 경계:** 타 도메인 `@Entity` import 금지, `@ManyToOne` 금지, `Long` ID 참조만. `PointPolicy`는 상수 홀더라 import 가능(엔티티 아님). 필요한 시그니처·enum이 없으면 합리적 가정 + `// B 합의 필요` 주석.
- **새 ErrorCode 추가 금지:** `QUIZ_NOT_FOUND`·`PROFILE_NOT_FOUND`·`INVALID_INPUT`을 재사용한다.
- 각 속성 테스트는 최소 100회 반복하며 `// Feature: point-notification-integration, Property {번호}: {속성}` 주석을 단다. jqwik 추가가 불가하면 JUnit5 파라미터라이즈드 + 난수 100회로 동등 구성한다.
- 파일 충돌 회피를 위해 동일 파일을 수정하는 작업은 서로 다른 wave에 배치했다(QuizWriter→QuizService→QuizController, ProfileService→ProfileController, enum/DTO는 wave 0 독립).
- 체크포인트는 증분 검증을 위한 것이며, WSL/Linux bash에서 `gradlew`를 직접 호출한다.

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.2", "1.3", "1.4", "1.5", "1.6", "4.1", "8.1"] },
    { "id": 1, "tasks": ["2.1", "5.1", "7.1"] },
    { "id": 2, "tasks": ["2.2", "3.1", "6.1", "2.3", "2.4", "2.5", "2.6", "5.2", "5.3", "5.4", "5.5", "7.2", "7.3", "7.4", "7.5"] },
    { "id": 3, "tasks": ["3.2", "6.2"] }
  ]
}
```
