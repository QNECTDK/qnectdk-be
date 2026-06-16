# qnectdk-be

qnectdk 백엔드 API 서버 (Spring Boot).

## 기술 스택

| 항목 | 내용 |
|------|------|
| 언어 | Java 21 (LTS) |
| 프레임워크 | Spring Boot 4.1.0 (Web MVC, Data JPA, Security, Validation) |
| DB | MySQL 8 |
| 빌드 | Gradle (Wrapper 포함) |
| 인증 | JWT (jjwt) |
| API 문서 | springdoc-openapi (Swagger UI) |
| 부가 | Lombok, Spring Boot DevTools |

## 준비물

| 도구 | 비고 |
|------|------|
| **JDK 21** (LTS) | IntelliJ·VSCode 공통. 프로젝트 컴파일과 VSCode 언어 서버 모두 21 사용 |
| **Docker Desktop** | MySQL 컨테이너 실행용. 백그라운드(트레이)에 떠 있어야 `docker compose` 동작 |
| **Git** | |

## 빠른 시작

```bash
# 1. 클론
git clone <repo-url>
cd qnectdk-be

# 2. 환경 변수 (선택 — 생략 시 기본값으로 동작)
cp .env.example .env        # .env 에서 비밀번호 등 수정

# 3. MySQL 기동 (qnectdk DB 자동 생성)
docker compose up -d

# 4. 앱 실행
./gradlew bootRun           # 또는 IDE에서 QnectdkApplication 실행
```

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

## IDE 설정

### Lombok (공통, 중요)

이 프로젝트는 Lombok을 사용합니다. **의존성은 `build.gradle`에 이미 포함**되어 있어 빌드(`./gradlew`)는 어느 환경에서든 됩니다.
다만 **IDE가 Lombok 생성 코드(`@Getter`, `@Builder` 등)를 이해하도록** IDE별 설정이 1회 필요합니다.
(이 설정을 안 해도 빌드는 되지만, 에디터에 빨간 줄이 표시됩니다.)

#### IntelliJ IDEA
1. **Annotation Processing 켜기** — `Settings → Build, Execution, Deployment → Compiler → Annotation Processors → ☑ Enable annotation processing`
2. Lombok 플러그인은 2020.3+ 기본 번들 (별도 설치 불필요)
3. `Settings → Build Tools → Gradle → Gradle JVM` 을 21로 설정

#### VS Code
1. 확장 설치: **Extension Pack for Java**, **Spring Boot Extension Pack**, **Gradle for Java**, **Lombok Annotations Support**
2. 설치 후 `Ctrl+Shift+P → Developer: Reload Window` 한 번
3. (개인 환경별 설정인 `.vscode/`는 gitignore 처리되어 공유되지 않음)

## 데이터베이스

### 접속 정보 (DB 클라이언트)

| 항목 | 값 |
|------|-----|
| Host | localhost |
| Port | 3306 |
| User | root |
| Password | `.env`의 `SPRING_DATASOURCE_PASSWORD` (기본값 `password`) |
| Database | qnectdk |

- **IntelliJ Ultimate**: 내장 Database 툴에서 데이터 소스 `+` → **MySQL** 선택 (JDBC 드라이버 자동 다운로드)
- **그 외**: DBeaver 등 외부 클라이언트. MySQL **네이티브 연결**을 쓰면 되고, JDBC를 직접 설정할 필요 없음

### Docker (MySQL) 명령어

```bash
docker compose up -d      # 기동 (백그라운드)
docker compose logs -f    # 로그 확인
docker compose down       # 중지
docker compose down -v    # 데이터 볼륨까지 초기화
```

- 데이터는 `qnectdk-mysql-data` 볼륨에 영속됩니다.
- `restart: unless-stopped` 설정 + Docker Desktop "로그인 시 자동 시작"을 켜두면, 재부팅 후에도 MySQL이 자동 복귀합니다.

## 환경 변수

`application.yml`은 환경 변수가 있으면 그 값을, 없으면 로컬 기본값을 사용합니다.
로컬에서는 `.env` 파일(같은 디렉터리)에 정의하면 앱이 자동으로 읽어들입니다.
전체 목록은 [`.env.example`](.env.example) 참고.

> `.env`는 비밀번호를 포함하므로 git에 커밋되지 않습니다(`.gitignore` 처리됨). `.env.example`만 추적됩니다.

## 빌드 / 테스트

```bash
./gradlew build           # 빌드 (테스트 포함)
./gradlew test            # 테스트만
./gradlew compileJava     # 컴파일만
```
