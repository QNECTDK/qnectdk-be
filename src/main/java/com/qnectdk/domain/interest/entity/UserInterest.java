package com.qnectdk.domain.interest.entity;

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

@Entity
@Table(name = "user_interests", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_interests_user_interest", columnNames = {"user_id", "interest_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "interest_id", nullable = false)
    private Long interestId;

    @Builder
    private UserInterest(Long userId, Long interestId) {
        this.userId = userId;
        this.interestId = interestId;
    }

    public static UserInterest of(Long userId, Long interestId) {
        return UserInterest.builder().userId(userId).interestId(interestId).build();
    }
}
