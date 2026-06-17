package com.qnectdk.domain.user.entity;

import com.qnectdk.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_login_id", columnNames = "login_id"),
        @UniqueConstraint(name = "uk_users_phone", columnNames = "phone"),
        @UniqueConstraint(name = "uk_users_public_code", columnNames = "public_code")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id", nullable = false, unique = true, length = 20)
    private String loginId;

    @Column(nullable = false, unique = true, length = 11)
    private String phone;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "public_code", nullable = false, unique = true, length = 16)
    private String publicCode;

    @Builder
    private User(String loginId, String phone, String passwordHash, String name,
                 LocalDate birthDate, String publicCode) {
        this.loginId = loginId;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.name = name;
        this.birthDate = birthDate;
        this.publicCode = publicCode;
    }

    public static User create(String loginId, String phone, String passwordHash, String name,
                              LocalDate birthDate, String publicCode) {
        return User.builder()
                .loginId(loginId)
                .phone(phone)
                .passwordHash(passwordHash)
                .name(name)
                .birthDate(birthDate)
                .publicCode(publicCode)
                .build();
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }
}
