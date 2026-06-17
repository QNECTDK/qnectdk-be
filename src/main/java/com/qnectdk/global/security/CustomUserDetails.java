package com.qnectdk.global.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * STATELESS JWT 인증의 principal. DB 조회 없이 토큰의 userId만 담는다.
 * 컨트롤러에서 {@code @AuthenticationPrincipal CustomUserDetails}로 받아 userId를 꺼낸다.
 */
@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final Long userId;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return String.valueOf(userId);
    }
}
