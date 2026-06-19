package com.qnectdk.domain.friend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(
        name = "friendships",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"requester_id", "addressee_id"})
        },
        indexes = {
                @Index(name = "idx_friendships_addressee_id", columnList = "addressee_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @Column(name = "addressee_id", nullable = false)
    private Long addresseeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendshipStatus status = FriendshipStatus.PENDING;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // --- 도메인 메서드 ---

    // JPA가 못 쓰니 빌더 대신 정적 팩토리로 생성
    public static Friendship request(Long requesterId, Long addresseeId) {
        Friendship f = new Friendship();
        f.requesterId = requesterId;
        f.addresseeId = addresseeId;
        f.status = FriendshipStatus.PENDING;
        f.createdAt = LocalDateTime.now();
        return f;
    }

    public void accept() {
        this.status = FriendshipStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = FriendshipStatus.REJECTED;
    }
}