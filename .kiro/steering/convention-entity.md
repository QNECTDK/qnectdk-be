---
inclusion: fileMatch
fileMatchPattern: '**/entity/*.java'
---

# 엔티티 컨벤션

- `@Entity` + `@Table(name = "...")`, 테이블/컬럼은 snake_case.
- `@Getter` + `@NoArgsConstructor(access = AccessLevel.PROTECTED)` + `@Builder`. **setter 금지.**
- 시간 필드는 직접 만들지 말고 `BaseTimeEntity`(JPA Auditing)를 상속.
- 생성은 빌더 기반 정적 팩토리(`create(...)`)로 의도를 드러내고, 필수값을 검증.
- 상태 변경은 의도가 드러나는 도메인 메서드로.
- 타 도메인은 `Long` ID로만 참조(엔티티 import 금지).

```java
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 11)
    private String phone;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Builder
    private User(String phone, String passwordHash, String name,
                 LocalDate birthDate, String publicCode) {
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.name = name;
        this.birthDate = birthDate;
        this.publicCode = publicCode;
    }

    public static User create(String phone, String passwordHash, String name,
                              LocalDate birthDate, String publicCode) {
        return User.builder()
                .phone(phone).passwordHash(passwordHash).name(name)
                .birthDate(birthDate).publicCode(publicCode)
                .build();
    }

    // 상태 변경은 도메인 메서드로 (setter 대신)
    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }
}
```
