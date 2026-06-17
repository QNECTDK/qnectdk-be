# 컨트롤러 컨벤션

- 얇게: 요청 받기 → service 호출 → 응답. 비즈니스 로직 금지.
- 모든 응답은 `ApiResponse<T>`로 감싼다. DTO는 `record`, 엔티티 직접 노출 금지.
- 입력 검증은 `@Valid` + Bean Validation으로 경계에서.
- 인증 사용자는 `@AuthenticationPrincipal CustomUserDetails`로 받는다 (`X-USER-ID` 헤더 금지).
- 경로는 소문자 복수형(`/api/profiles`).

```java
@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping
    public ApiResponse<ProfileResponse> create(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody ProfileCreateRequest request) {
        return ApiResponse.ok(profileService.create(user.getUserId(), request));
    }

    @GetMapping("/me")
    public ApiResponse<ProfileResponse> getMine(
            @AuthenticationPrincipal CustomUserDetails user) {
        return ApiResponse.ok(profileService.getMine(user.getUserId()));
    }
}
```

DTO 예시:

```java
public record ProfileCreateRequest(
        @NotBlank String school,
        @NotNull Gender gender,
        @Size(min = 4, max = 4) String mbti) {
}

public record ProfileResponse(Long id, String school, String zodiac, int age) {
    public static ProfileResponse of(Profile profile, LocalDate birthDate) {
        return new ProfileResponse(
                profile.getId(), profile.getSchool(),
                ZodiacUtil.of(birthDate), AgeUtil.of(birthDate));
    }
}
```
