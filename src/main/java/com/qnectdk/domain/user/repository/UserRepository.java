package com.qnectdk.domain.user.repository;

import com.qnectdk.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLoginId(String loginId);

    Optional<User> findByPublicCode(String publicCode);

    boolean existsByLoginId(String loginId);

    boolean existsByPhone(String phone);

    boolean existsByPublicCode(String publicCode);
}
