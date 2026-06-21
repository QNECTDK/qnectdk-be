package com.qnectdk.global.config;

import com.qnectdk.global.exception.ErrorCode;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    private static final String API_DESCRIPTION = """
        Qnect 백엔드 API — 대학생 인맥 관리 앱 (인증 · 프로필 · 관심사 · 퀴즈 · 데일리 · 친구 · 그룹 · 포인트 · 상점 · 알림).

        ## 공통 응답 포맷 (ApiResponse)
        모든 응답은 아래 봉투(envelope) 형태로 내려간다. (null 필드는 직렬화에서 제외)
        - **성공:** `{ "success": true, "data": {...}, "error": null }`
        - **실패:** `{ "success": false, "data": null, "error": { "code": "INVALID_INPUT", "message": "...", "fields": { "필드명": "메시지" } } }`

        `error.fields`는 **Bean Validation(요청 바디 검증) 실패 시에만** 채워지는 필드별 메시지 맵이다. 그 외 에러는 `fields=null`.

        ## 에러 처리 가이드 (프론트 공통)
        프론트는 HTTP status 와 `error.code`(문자열)로 분기하면 된다. 모든 에러 바디는 동일한 봉투 형태다.

        ### 전역 에러 (모든 엔드포인트 공통 — 각 엔드포인트에 매번 표기하지 않음)
        | status | code | 발생 상황 |
        |--------|------|-----------|
        | 400 | `INVALID_INPUT` | 요청 바디 Bean Validation 실패(→ `fields`), JSON 파싱 실패/바디 누락, 경로·쿼리 파라미터 타입 불일치(enum 오타·숫자 자리 문자), 필수 파라미터 누락 |
        | 401 | `UNAUTHORIZED` | 토큰 없음/유효하지 않음 — 인증 필요한 모든 엔드포인트에서 가능 |
        | 403 | `ACCESS_DENIED` | 인가 실패(권한 없음) |
        | 405 | `METHOD_NOT_ALLOWED` | 지원하지 않는 HTTP 메서드 |
        | 409 | `RESOURCE_CONFLICT` | 동시 요청 레이스로 unique 제약 위반 |
        | 500 | `INTERNAL_ERROR` | 처리되지 않은 서버 오류 |

        > 인증: `Authorization: Bearer {accessToken}` 헤더 필요. **`/api/auth/**` 만 인증 불필요.**
        > 그 외 모든 엔드포인트는 토큰이 없거나 만료되면 **401 `UNAUTHORIZED`** 로 응답한다(개별 표기 생략).

        ### 도메인 에러 코드 카탈로그
        각 엔드포인트의 응답 목록에 코드명을 표기한다. 전체 코드는 다음과 같다.
        | code | status | message |
        |------|--------|---------|
        | `INVALID_CREDENTIALS` | 401 | 아이디 또는 비밀번호가 올바르지 않습니다. |
        | `INVALID_TOKEN` | 401 | 유효하지 않은 토큰입니다. |
        | `EXPIRED_TOKEN` | 401 | 만료된 토큰입니다. |
        | `DUPLICATE_LOGIN_ID` | 409 | 이미 사용 중인 아이디입니다. |
        | `DUPLICATE_PHONE` | 409 | 이미 가입된 전화번호입니다. |
        | `USER_NOT_FOUND` | 404 | 사용자를 찾을 수 없습니다. |
        | `PROFILE_NOT_FOUND` | 404 | 프로필을 찾을 수 없습니다. |
        | `INTEREST_NOT_FOUND` | 404 | 존재하지 않는 관심사가 포함되어 있습니다. |
        | `QUIZ_NOT_FOUND` | 404 | 퀴즈를 찾을 수 없습니다. |
        | `QUIZ_ATTEMPT_NOT_FOUND` | 404 | 퀴즈 응시 기록을 찾을 수 없습니다. |
        | `QUIZ_FORBIDDEN` | 403 | 해당 퀴즈에 대한 권한이 없습니다. |
        | `QUIZ_NOT_SOLVABLE` | 403 | 프로필을 먼저 완성해야 퀴즈를 풀 수 있습니다. |
        | `QUIZ_INVALID_CONTENT` | 400 | 퀴즈 구성이 올바르지 않습니다. |
        | `QUIZ_GENERATION_FAILED` | 502 | 퀴즈 자동 생성에 실패했습니다. |
        | `DAILY_QUIZ_NOT_FOUND` | 404 | 오늘의 데일리 퀴즈가 없습니다. |
        | `DAILY_ALREADY_ANSWERED` | 409 | 이미 오늘의 데일리에 답했습니다. |
        | `DAILY_NOT_ANSWERED_YET` | 403 | 먼저 답해야 결과를 볼 수 있습니다. |
        | `FRIENDSHIP_NOT_FOUND` | 404 | 친구 요청을 찾을 수 없습니다. |
        | `ALREADY_FRIENDS` | 409 | 이미 친구이거나 요청이 존재합니다. |
        | `FRIENDSHIP_NOT_PENDING` | 409 | 대기 중인 요청만 처리할 수 있습니다. |
        | `NOT_FRIENDSHIP_ADDRESSEE` | 403 | 요청을 받은 사람만 수락/거절할 수 있습니다. |
        | `MEMO_NOT_FOUND` | 404 | 메모를 찾을 수 없습니다. |
        | `GROUP_NOT_FOUND` | 404 | 그룹을 찾을 수 없습니다. |
        | `NOT_GROUP_OWNER` | 403 | 본인의 그룹만 관리할 수 있습니다. |
        | `DUPLICATE_GROUP_NAME` | 409 | 이미 같은 이름의 그룹이 있습니다. |
        | `NOT_ACCEPTED_FRIEND` | 400 | 수락된 친구만 그룹에 추가할 수 있습니다. |
        | `ALREADY_GROUP_MEMBER` | 409 | 이미 그룹에 추가된 친구입니다. |
        | `GROUP_MEMBER_NOT_FOUND` | 404 | 그룹에 없는 멤버입니다. |
        | `INSUFFICIENT_POINT` | 409 | 포인트가 부족합니다. |
        """;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Qnect API")
                .description(API_DESCRIPTION)
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    /**
     * 생성되는 OpenAPI 문서의 ErrorDetail.code 를 전체 ErrorCode enum 으로 노출한다.
     * (ErrorCode enum 을 단일 소스로 두어 문서와 코드가 어긋나지 않게 한다.)
     */
    @Bean
    public OpenApiCustomizer errorCodeEnumCustomizer() {
      List<String> codes = Arrays.stream(ErrorCode.values()).map(Enum::name).toList();
      return openApi -> {
        if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
          return;
        }
        Map<String, Schema> schemas = openApi.getComponents().getSchemas();
        Schema errorDetail = schemas.get("ErrorDetail");
        if (errorDetail == null || errorDetail.getProperties() == null) {
          return;
        }
        Schema codeProperty = (Schema) errorDetail.getProperties().get("code");
        if (codeProperty != null) {
          codeProperty.setEnum(new ArrayList<>(codes));
          codeProperty.setDescription("에러 코드. HTTP status 와 함께 분기 키로 사용한다.");
        }
      };
    }

    /**
     * 보안 엔드포인트 공통 에러 응답을 일괄 주입한다(컨트롤러마다 반복 선언하지 않기 위함).
     * - 401 UNAUTHORIZED: {@code /api/auth/**} 를 제외한 모든 엔드포인트(토큰 필요).
     * - 400 INVALID_INPUT: 요청 바디가 있는(=Bean Validation 대상) 엔드포인트.
     * 이미 해당 코드를 선언한 오퍼레이션은 건드리지 않는다(개별 도메인 설명 우선).
     */
    @Bean
    public GlobalOpenApiCustomizer commonErrorResponsesCustomizer() {
      return openApi -> {
        if (openApi.getPaths() == null) {
          return;
        }
        boolean hasVoidSchema = openApi.getComponents() != null
            && openApi.getComponents().getSchemas() != null
            && openApi.getComponents().getSchemas().containsKey("ApiResponseVoid");
        openApi.getPaths().forEach((path, pathItem) -> pathItem.readOperations().forEach(operation -> {
          ApiResponses responses = operation.getResponses();
          if (responses == null) {
            return;
          }
          if (!path.startsWith("/api/auth") && !responses.containsKey("401")) {
            responses.addApiResponse("401", errorResponse(
                "인증 필요 — UNAUTHORIZED. 토큰이 없거나 유효하지 않음/만료됨.", hasVoidSchema));
          }
          if (operation.getRequestBody() != null && !responses.containsKey("400")) {
            responses.addApiResponse("400", errorResponse(
                "입력값 검증 실패 — INVALID_INPUT. Bean Validation 실패 시 error.fields 에 필드별 메시지.",
                hasVoidSchema));
          }
        }));
      };
    }

    private io.swagger.v3.oas.models.responses.ApiResponse errorResponse(String description, boolean hasVoidSchema) {
      io.swagger.v3.oas.models.responses.ApiResponse response = new io.swagger.v3.oas.models.responses.ApiResponse()
          .description(description);
      if (hasVoidSchema) {
        response.content(new Content().addMediaType("*/*",
            new MediaType().schema(new Schema<>().$ref("#/components/schemas/ApiResponseVoid"))));
      }
      return response;
    }
}
