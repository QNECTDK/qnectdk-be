# Requirements Document

## Introduction

개발자 A(담당: `domain/user`·`profile`·`interest`·`quiz`·`daily`)가 **시연용 디테일 기능**을 마저 개발한다. 이 스펙은 세 갈래를 함께 다룬다.

1. **B 도메인 연동(유지)**: B(담당: `point`·`notification`)가 이미 구현·제공하는 서비스를 A가 적절한 시점에 **서비스 호출**로 연동한다. (프로필 최초 생성 적립, 퀴즈 최초 설정 적립, 데일리 퀴즈 생성 시 전체 사용자 알림)
2. **A 도메인 신규/변경 기능**: 내 퀴즈 삭제(신규 엔드포인트), 프로필 이미지 설정 방식을 **파일 업로드 → 서버 제공 캐릭터 카탈로그(17종) 선택**으로 변경.
3. **검증 산출물**: 위 연동·기능을 수동으로 검증할 수 있는 `.http` 파일.

B 도메인 코드(엔티티·서비스·리포지토리)는 **수정하지 않는다.** A는 B의 `Long` ID 기반 서비스 시그니처와 기존 enum 값만 사용한다. 도메인 경계 규칙상 A는 B의 **`@Entity` 클래스를 import하지 않고**(`@ManyToOne` 금지, `Long` ID 참조만), B가 제공하는 서비스 시그니처와 `PointReason`·`NotificationType` enum 값만 사용한다.

`PROFILE_CREATE`·`QUIZ_FIRST_SETUP` 적립은 `refId`가 `null`이어서 B의 원장 기반 멱등성 보호(`existsByUserIdAndReasonAndRefId`)를 받지 못한다. 따라서 **"최초 1회만 적립"의 책임은 전적으로 호출부(A)**에 있다.

### 적립 멱등성에 관한 설계 전제 (Requirement 2·4 정합성)

퀴즈 삭제(Requirement 4)는 퀴즈 레코드를 물리 삭제하지 않고 **비활성화(`active=false`) + 콘텐츠(문항·보기) 정리**하는 **소프트 삭제**다(기존 `QuizWriter` 패턴 일관). 그 결과 `QUIZ_FIRST_SETUP` 적립의 "최초" 판정 근거를 단순히 "활성 퀴즈 부재"로 두면 **삭제 후 재설정 시 재적립**이 발생한다. 이를 막기 위해 본 스펙은 적립 "최초" 판정 근거를 **"해당 사용자에 대해 활성·비활성을 불문하고 퀴즈 레코드가 한 건도 존재한 적 없는 경우"(`quizRepository.existsByOwnerId(ownerId)`)**로 규정한다. 소프트 삭제로 퀴즈 레코드가 보존되므로 `QUIZ_FIRST_SETUP`은 **사용자당 생애 1회**가 보장된다.

### 비범위 (만들지 않음)

본 작업은 시연 후 종료되는 프로젝트이므로 다음 비핵심 기능은 **구현하지 않는다.**

- 회원 탈퇴
- 비밀번호 변경
- QR 꾸미기(150포인트 사용) 등 포인트 **사용(차감)** 기능

## Glossary

- **A 도메인**: 본 작업에서 호출·기능을 추가하는 쪽. `user`·`profile`·`quiz`·`daily` 도메인의 구성요소.
- **B 도메인**: 이미 구현되어 있고 본 작업에서 수정하지 않는 쪽. `point`·`notification` 도메인.
- **Profile_Creation_Handler**: 프로필 최초 생성 시점을 담당하는 A 도메인 구성요소(현재 `ProfileService.upsert`의 신규 생성 분기 `if (profile == null)`).
- **Quiz_Setup_Handler**: 사용자가 자기 퀴즈를 최초로 저장·확정하는 시점을 담당하는 A 도메인 구성요소(현재 `QuizService.saveMyQuiz` 경로).
- **Quiz_Deletion_Handler**: 본인 활성 퀴즈를 삭제하는 A 도메인 구성요소(신규 `QuizService` 삭제 메서드 + `DELETE /api/quizzes/me`).
- **Daily_Quiz_Creator**: 새 데일리 퀴즈를 생성하는 A 도메인 구성요소(현재 `DailyQuizSeeder`).
- **Character_Catalog_Provider**: 서버가 고정 제공하는 캐릭터 이미지 카탈로그(17종)를 조회 수단으로 노출하는 A 도메인 구성요소(신규 `GET /api/profiles/characters`).
- **Profile_Image_Handler**: 사용자가 선택한 캐릭터를 검증하여 프로필 이미지로 설정하는 A 도메인 구성요소(현재 `ProfileService.updateImage`를 대체할 캐릭터 선택 저장 경로 + `PUT /api/profiles/me/image`).
- **User_Query_Service**: A의 `user` 도메인 읽기 전용 조회 서비스(`UserQueryService`). 현재 `getById`·`getByPublicCode`·`getByIds`만 존재하며, 본 작업에서 **전체 사용자 ID 조회 메서드**를 추가한다.
- **Point_Service**: B 도메인의 포인트 서비스. 시그니처 `PointService#earn(Long userId, int amount, PointReason reason, Long refId)`. `refId != null`일 때만 `(userId, reason, refId)` 기준 멱등성을 보장한다.
- **Notification_Service**: B 도메인의 알림 서비스. 시그니처 `NotificationService#push(Long userId, NotificationType type, String title, String body, Long refId)`. 한 호출당 알림 1건을 저장한다.
- **PointReason**: B 도메인의 적립 사유 enum. 본 작업에서 사용하는 값은 `PROFILE_CREATE`, `QUIZ_FIRST_SETUP`.
- **NotificationType**: B 도메인의 알림 종류 enum. 본 작업에서 사용하는 값은 `DAILY_QUIZ`.
- **활성 퀴즈**: 한 사용자(ownerId)에 대해 `active=true`인 퀴즈(`quizRepository.findFirstByOwnerIdAndActiveTrueOrderByIdDesc(ownerId)`로 조회되는 대상).
- **소프트 삭제**: 퀴즈 레코드 행은 보존한 채 `active=false`로 전이하고 콘텐츠(문항·보기)만 제거하는 삭제 방식.
- **최초 설정 판정**: 해당 사용자(ownerId)에 대해 활성·비활성을 불문하고 퀴즈 레코드가 한 건도 존재하지 않을 때(`quizRepository.existsByOwnerId(ownerId) == false`)만 "최초"로 본다.
- **캐릭터 식별자(characterId)**: 서버가 정의한 17종 캐릭터를 가리키는 안정적 문자열 키(예: `character01`~`character17`).
- **캐릭터 카탈로그**: 서버가 고정 제공하는 17종 캐릭터의 식별자와 이미지 URL의 집합.
- **검증_HTTP_파일**: 본 연동·기능을 수동으로 확인하기 위한 `http/` 디렉터리의 `.http` 산출물.

## Requirements

### Requirement 1: 프로필 최초 생성 시 포인트 적립

**User Story:** 신규 사용자로서, 프로필을 처음 만들면 포인트 20P를 받고 싶다. 그래야 온보딩 보상을 통해 서비스 사용을 시작할 동기를 얻는다.

#### Acceptance Criteria

1. WHEN 해당 사용자의 프로필이 기존에 존재하지 않는 상태에서 신규 프로필 레코드가 최초로 저장되면, THE Profile_Creation_Handler SHALL Point_Service의 `earn`을 인자 `(userId, 20, PointReason.PROFILE_CREATE, null)`로 1회 호출한다.
2. WHEN 동일 사용자에 대해 이미 존재하는 프로필 레코드가 갱신(업데이트)되면, THE Profile_Creation_Handler SHALL Point_Service의 `earn`을 호출하지 않는다.
3. WHERE 동일 사용자에 대해 프로필 업서트가 N회(N≥1) 수행되는 경우, THE Profile_Creation_Handler SHALL `PROFILE_CREATE` 적립을 최대 1회만 발생시킨다.
4. WHILE 프로필 최초 생성과 포인트 적립이 함께 수행되는 동안, THE Profile_Creation_Handler SHALL 프로필 저장과 `earn` 호출을 하나의 트랜잭션(`upsert`의 `@Transactional`) 처리 단위로 수행한다.
5. IF 프로필 저장 또는 `earn` 호출 중 어느 하나라도 실패하면, THEN THE Profile_Creation_Handler SHALL 프로필 저장과 적립을 모두 미반영(롤백)하고 오류를 전파한다.
6. THE Profile_Creation_Handler SHALL `point` 도메인의 `@Entity` 클래스를 import하지 않고 `Long userId`와 Point_Service 시그니처·`PointReason` enum 값만 사용하여 연동한다.

### Requirement 2: 퀴즈 최초 설정 시 포인트 적립 (생애 1회)

**User Story:** 사용자로서, 내 퀴즈를 처음 설정하면 포인트 20P를 받고 싶다. 그래야 퀴즈 작성에 대한 보상을 받는다. 다만 보상은 계정당 한 번만 받는다.

#### Acceptance Criteria

1. WHEN 한 사용자(ownerId)에 대해 퀴즈 레코드가 활성·비활성을 불문하고 한 건도 존재하지 않는 상태에서 자기 퀴즈를 최초로 저장·확정(`saveMyQuiz`)하면, THE Quiz_Setup_Handler SHALL Point_Service의 `earn`을 인자 `(ownerId, 20, PointReason.QUIZ_FIRST_SETUP, null)`로 1회 호출한다.
2. THE Quiz_Setup_Handler SHALL 저장(`replaceActiveContent`) 직전에 "최초 설정 판정"을 `quizRepository.existsByOwnerId(ownerId)` 결과로 수행하여, 레코드가 한 건도 없을 때(`false`)만 적립을 발생시킨다.
3. WHEN 동일 사용자가 이미 퀴즈 레코드를 보유한(활성 또는 비활성) 상태에서 퀴즈를 수정·재저장하면, THE Quiz_Setup_Handler SHALL Point_Service의 `earn`을 호출하지 않는다.
4. WHERE 사용자가 퀴즈를 삭제(소프트 삭제)한 뒤 다시 최초 설정하는 경우, THE Quiz_Setup_Handler SHALL `QUIZ_FIRST_SETUP` 적립을 발생시키지 않는다(소프트 삭제로 퀴즈 레코드가 보존되어 `existsByOwnerId`가 `true`이므로).
5. WHERE 동일 사용자에 대해 퀴즈 저장이 N회(N≥1) 수행되는 경우, THE Quiz_Setup_Handler SHALL `QUIZ_FIRST_SETUP` 적립을 사용자당 최대 1회(생애 1회)만 발생시킨다.
6. THE Quiz_Setup_Handler SHALL 퀴즈 저장과 `earn` 호출을 하나의 트랜잭션(`saveMyQuiz`의 `@Transactional`) 처리 단위로 수행한다.
7. IF 퀴즈 저장 또는 `earn` 호출 중 어느 하나라도 실패하면, THEN THE Quiz_Setup_Handler SHALL 퀴즈 저장과 적립을 모두 미반영(롤백)하고 오류를 전파한다.
8. THE Quiz_Setup_Handler SHALL `point` 도메인의 `@Entity` 클래스를 import하지 않고 `Long ownerId`와 Point_Service 시그니처·`PointReason` enum 값만 사용하여 연동한다.

### Requirement 3: 데일리 퀴즈 생성 시 전체 사용자 알림 발송

**User Story:** 사용자로서, 새 데일리 퀴즈가 만들어지면 알림을 받고 싶다. 그래야 매일 새 퀴즈를 놓치지 않고 풀 수 있다.

#### Acceptance Criteria

1. WHEN 오늘 날짜의 데일리 퀴즈가 신규 생성되어 영속화(저장 성공)되면, THE Daily_Quiz_Creator SHALL 전체 사용자를 순회하며 각 사용자마다 Notification_Service의 `push`를 1회 호출한다.
2. WHEN 각 사용자에게 알림을 발송하면, THE Daily_Quiz_Creator SHALL `push`를 인자 `(userId, NotificationType.DAILY_QUIZ, "오늘의 퀴즈가 생성되었습니다!", "지금 바로 풀어보세요!", dailyQuizId)`로 호출하며, 이때 `userId`는 순회 중인 사용자의 ID, `dailyQuizId`는 방금 생성된 데일리 퀴즈의 ID이다.
3. WHERE 사용자가 N명(N≥1) 존재하는 경우, THE Daily_Quiz_Creator SHALL 한 명당 알림 1건씩 정확히 N건의 `push` 호출을 수행한다(중복 0건·누락 0건).
4. WHEN 전체 사용자 알림 발송을 수행하면, THE Daily_Quiz_Creator SHALL 전체 사용자 ID 목록을 User_Query_Service의 전체 사용자 ID 조회 메서드를 통해 조회한다.
5. IF 오늘 날짜의 데일리 퀴즈가 이미 존재하여 신규 생성이 일어나지 않으면, THEN THE Daily_Quiz_Creator SHALL `push`를 0회 호출한다.
6. IF 전체 사용자 목록이 비어 있으면(N=0), THEN THE Daily_Quiz_Creator SHALL `push`를 0회 호출하고 정상 종료한다.
7. IF 개별 사용자에 대한 `push` 호출이 실패하면, THEN THE Daily_Quiz_Creator SHALL 나머지 사용자에 대한 발송을 계속 수행하고 데일리 퀴즈 생성을 롤백하지 않는다.
8. THE Daily_Quiz_Creator SHALL `notification` 도메인의 `@Entity` 클래스를 import하지 않고 `Long userId`·`Long dailyQuizId`와 Notification_Service 시그니처·`NotificationType` enum 값만 사용하여 연동한다.

### Requirement 4: 내 퀴즈 삭제

**User Story:** 사용자로서, 내가 만든 퀴즈를 삭제하고 싶다. 그래야 더 이상 노출하고 싶지 않은 퀴즈를 내릴 수 있다.

#### Acceptance Criteria

1. THE Quiz_Deletion_Handler SHALL `DELETE /api/quizzes/me` 엔드포인트를 제공하고, 삭제 대상 사용자를 `@AuthenticationPrincipal CustomUserDetails`의 `userId`로 식별한다.
2. WHEN 인증 사용자에게 활성 퀴즈(`active=true`)가 존재하는 상태에서 삭제 요청이 들어오면, THE Quiz_Deletion_Handler SHALL 해당 활성 퀴즈의 문항(`QuizQuestion`)·보기(`QuizOption`)를 제거하고 해당 퀴즈를 비활성(`active=false`) 상태로 전이한다(소프트 삭제).
3. WHEN 활성 퀴즈의 문항·보기를 제거하면, THE Quiz_Deletion_Handler SHALL 기존 `QuizWriter.deleteContent` 패턴(보기 → 문항 순서의 벌크 삭제)을 따른다.
4. WHEN 삭제가 성공적으로 완료되면, THE Quiz_Deletion_Handler SHALL 대상 퀴즈를 `active=false`로 만들어 이후 동일 사용자의 활성 퀴즈 조회(`GET /api/quizzes/me`)가 `QUIZ_NOT_FOUND`로 응답되도록 한다.
5. WHEN 소프트 삭제가 완료되면, THE Quiz_Deletion_Handler SHALL 퀴즈 레코드 행 자체는 보존하여 `quizRepository.existsByOwnerId(ownerId)`가 계속 `true`를 유지하도록 한다(Requirement 2의 생애 1회 적립 근거 보존).
6. IF 인증 사용자에게 활성 퀴즈(`active=true`)가 없는 상태에서 삭제 요청이 들어오면, THEN THE Quiz_Deletion_Handler SHALL `BusinessException(ErrorCode.QUIZ_NOT_FOUND)`로 응답한다.
7. THE Quiz_Deletion_Handler SHALL 삭제 작업(문항·보기 제거 및 퀴즈 비활성화)을 하나의 `@Transactional` 처리 단위로 수행하며, 작업 중 실패 시 모두 미반영(롤백)하여 기존 활성 퀴즈 상태를 보존한다.
8. THE Quiz_Deletion_Handler SHALL 삭제 대상을 인증 사용자 본인 소유 퀴즈(`ownerId == userId`)로 한정하여, 타 사용자의 퀴즈를 삭제하지 않는다.
9. WHEN 활성 퀴즈를 삭제하면, THE Quiz_Deletion_Handler SHALL 해당 퀴즈에 대한 기존 응시 기록(`QuizAttempt`·`QuizAnswer`)은 삭제하지 않고 보존한다.
10. WHEN 삭제가 성공하면, THE Quiz_Deletion_Handler SHALL 결과를 `ApiResponse`의 성공(`ok`) 형태로 감싸 반환한다.
11. IF 인증 정보가 없는 요청이 들어오면, THEN THE Quiz_Deletion_Handler SHALL `401`로 응답하고 어떤 퀴즈도 삭제하지 않는다.

### Requirement 5: 캐릭터 이미지 카탈로그 조회

**User Story:** 사용자로서, 프로필 이미지로 고를 수 있는 캐릭터 목록을 보고 싶다. 그래야 어떤 캐릭터가 있는지 확인하고 선택할 수 있다.

#### Acceptance Criteria

1. THE Character_Catalog_Provider SHALL `GET /api/profiles/characters` 엔드포인트를 제공한다.
2. WHEN 인증 사용자가 카탈로그를 조회하면, THE Character_Catalog_Provider SHALL 서버가 고정 정의한 캐릭터 17종 전체를 반환한다(정확히 17건).
3. THE Character_Catalog_Provider SHALL 각 캐릭터 항목에 대해 캐릭터 식별자(characterId, 예: `character01`~`character17`)와 해당 이미지 URL을 포함하여 반환한다.
4. THE Character_Catalog_Provider SHALL 카탈로그를 서버 측 고정 정의(상수/enum)에서 제공하며, 동일 요청에 대해 항상 동일한 17종을 동일한 식별자로 반환한다.
5. THE Character_Catalog_Provider SHALL 응답을 `ApiResponse<T>`로 감싸 반환한다.
6. IF 인증 정보가 없는 요청이 들어오면, THEN THE Character_Catalog_Provider SHALL `401`로 응답한다.

### Requirement 6: 캐릭터 선택으로 프로필 이미지 설정

**User Story:** 사용자로서, 카탈로그의 캐릭터 중 하나를 골라 내 프로필 이미지로 설정하고 싶다. 그래야 파일 업로드 없이 캐릭터로 프로필을 꾸밀 수 있다.

#### Acceptance Criteria

1. THE Profile_Image_Handler SHALL `PUT /api/profiles/me/image` 엔드포인트에서 JSON 요청 본문으로 캐릭터 식별자(characterId)를 수신한다.
2. WHEN 인증 사용자가 카탈로그(17종)에 존재하는 유효한 캐릭터 식별자를 전송하면, THE Profile_Image_Handler SHALL 해당 캐릭터에 대응하는 이미지 참조 값을 `Profile.imageUrl`에 저장하고 저장 결과를 응답으로 반환한다.
3. WHERE 동일 사용자가 캐릭터를 여러 번 설정하는 경우, THE Profile_Image_Handler SHALL 가장 최근 선택 값으로 `Profile.imageUrl`을 덮어쓴다.
4. IF 전송된 캐릭터 식별자가 카탈로그(17종)에 존재하지 않으면, THEN THE Profile_Image_Handler SHALL `400`(검증 오류) 응답을 반환하고 `Profile.imageUrl`을 변경하지 않는다.
5. IF 캐릭터 식별자가 비어 있거나 공백뿐이면, THEN THE Profile_Image_Handler SHALL 컨트롤러 경계의 Bean Validation(`@NotBlank`)으로 `400`(`INVALID_INPUT`)을 반환하고 `Profile.imageUrl`을 변경하지 않는다.
6. IF 프로필이 존재하지 않는 사용자가 캐릭터 설정을 요청하면, THEN THE Profile_Image_Handler SHALL `BusinessException(ErrorCode.PROFILE_NOT_FOUND)`로 응답하고 아무 값도 저장하지 않는다.
7. THE Profile_Image_Handler SHALL 응답을 성공·실패 모두 `ApiResponse<T>`로 감싸 반환한다.
8. IF 인증 정보가 없는 요청이 들어오면, THEN THE Profile_Image_Handler SHALL `401`로 응답하고 `Profile.imageUrl`을 변경하지 않는다.

> 비고: 기존 multipart 업로드 경로(`POST /api/profiles/me/image`, `StorageService` 기반)는 본 변경으로 폐기한다. 폐기/대체의 구체적 처리(엔드포인트 제거 vs 보존, `StorageService` 의존 정리)와 `Profile.imageUrl`에 저장할 값의 최종 형태(식별자 문자열 vs 카탈로그 이미지 URL), 카탈로그 무효 식별자에 대한 `ErrorCode`(예: `INVALID_INPUT` 재사용 vs 신규 추가)는 설계 단계에서 확정한다.

### Requirement 7: B 도메인 무수정 경계 준수

**User Story:** 개발자 A로서, 연동을 추가하되 B 도메인 코드는 건드리지 않고 싶다. 그래야 역할 경계를 지키고 B의 구현과 충돌하지 않는다.

#### Acceptance Criteria

1. THE A 도메인 SHALL `domain/point`·`domain/notification` 패키지 하위의 모든 소스 파일에 대해 추가·삭제·변경을 0건으로 유지한다.
2. THE A 도메인 SHALL B 도메인 연동을 `PointService`·`NotificationService`의 기존 시그니처(파라미터 타입·개수·순서·반환 타입 불변) 호출로만 수행한다.
3. WHERE 연동에 enum 값이 필요한 경우, THE A 도메인 SHALL `PointReason`·`NotificationType`의 기존 값(`PROFILE_CREATE`, `QUIZ_FIRST_SETUP`, `DAILY_QUIZ`)만 사용하고 B 도메인에 enum 상수를 새로 추가하지 않는다.
4. IF 연동에 필요한 시그니처나 enum 값이 B 도메인에 존재하지 않으면, THEN THE A 도메인 SHALL B 도메인을 수정하지 않고, 합리적으로 가정한 시그니처를 사용하며 `// B 합의 필요` 주석을 남긴다.
5. THE A 도메인 SHALL 도메인 경계 규칙(타 도메인 `@Entity` import 금지, `@ManyToOne` 금지, `Long` ID 참조만)을 준수하여 연동한다.
6. WHERE 전체 사용자 ID 조회 메서드 또는 퀴즈 존재 여부 조회 메서드가 필요한 경우, THE A 도메인 SHALL 그 메서드를 A 소유 도메인(`UserQueryService`·`QuizRepository`)에 추가하고 B 도메인은 수정하지 않는다.

### Requirement 8: 연동·기능 검증용 HTTP 산출물

**User Story:** 개발자/검수자로서, 추가된 연동과 기능을 수동으로 확인할 수 있는 `.http` 파일을 원한다. 그래야 적립·알림·삭제·캐릭터 설정이 실제로 동작하는지 검증할 수 있다.

#### Acceptance Criteria

1. THE 검증_HTTP_파일 SHALL 프로필 최초 생성 요청을 포함하고, 동일 파일 내에서 해당 요청 다음에 포인트 잔액 조회(`GET /api/points/balance`)와 포인트 내역 조회(`GET /api/points/transactions`) 요청을 그 순서로 배치하여, 잔액 값과 `PROFILE_CREATE` 적립 항목으로 적립 발생 여부를 확인할 수 있게 한다.
2. THE 검증_HTTP_파일 SHALL 퀴즈 최초 설정 요청을 포함하고, 동일 파일 내에서 해당 요청 다음에 포인트 잔액 조회와 포인트 내역 조회 요청을 그 순서로 배치하여, 잔액 값과 `QUIZ_FIRST_SETUP` 적립 항목으로 적립 발생 여부를 확인할 수 있게 한다.
3. THE 검증_HTTP_파일 SHALL 데일리 퀴즈 생성 이후 실행되는 사용자의 알림 목록 조회(`GET /api/notifications`) 요청을 포함하고, 알림 목록 응답에 `DAILY_QUIZ` 알림 항목이 나타나는지로 알림 발생 여부를 확인할 수 있게 한다.
4. THE 검증_HTTP_파일 SHALL 내 퀴즈 삭제 요청(`DELETE /api/quizzes/me`)을 포함하고, 삭제 다음에 내 퀴즈 조회(`GET /api/quizzes/me`)를 배치하여 `QUIZ_NOT_FOUND` 응답으로 삭제 결과를 확인할 수 있게 한다.
5. THE 검증_HTTP_파일 SHALL 캐릭터 카탈로그 조회 요청(`GET /api/profiles/characters`)을 포함하여 17종 목록과 식별자를 확인할 수 있게 한다.
6. THE 검증_HTTP_파일 SHALL 카탈로그에서 고른 식별자로 프로필 이미지 설정 요청(`PUT /api/profiles/me/image`)을 포함하고, 설정 다음에 내 프로필 조회(`GET /api/profiles/me`)를 배치하여 응답의 `imageUrl`로 설정 결과를 확인할 수 있게 한다.
7. THE 검증_HTTP_파일 SHALL 카탈로그에 없는 잘못된 식별자로 설정을 시도하는 요청을 1개 포함하여 `400`(검증 오류) 응답을 확인할 수 있게 한다.
8. THE 검증_HTTP_파일 SHALL 인증이 필요한 요청들보다 앞에 `# @name login` 라벨이 붙은 로그인 요청을 1개 포함하고, 인증이 필요한 모든 요청은 `Authorization: Bearer {{login.response.body.$.data.accessToken}}` 형식으로 해당 로그인 응답의 액세스 토큰을 참조한다.
9. THE 검증_HTTP_파일 SHALL 기존 `http/` 디렉터리의 `.http` 파일과 동일하게 기본 URL을 `@baseUrl` 변수로 정의하고, 응답을 후속 요청에서 참조하는 모든 요청에 `# @name` 라벨을 부여하며, 각 요청을 `###` 구분자로 구분한다.
