package com.qnectdk.domain.user.repository;

import com.qnectdk.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLoginId(String loginId);

    Optional<User> findByPublicCode(String publicCode);

    boolean existsByLoginId(String loginId);

    boolean existsByPhone(String phone);

    boolean existsByPublicCode(String publicCode);

    @Query("select u.id from User u")
    List<Long> findAllUserIds();
}
