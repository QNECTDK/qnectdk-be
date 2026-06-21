package com.qnectdk.domain.friend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 친구 관계(방향성 레코드). 한 행 = "ownerId 가 friendId 를 자기 친구로 저장함" (단방향).
 * QR 스캔 후 '수락' 시 (나→상대), (상대→나) 두 행을 함께 만들어 상호 친구가 되고,
 * 삭제는 각자 자기 행만 지운다(상대 무영향). 수락/거절은 클라이언트 동작이며 서버엔 PENDING 상태가 없다.
 */
@Entity
@Table(
        name = "friendships",
        uniqueConstraints = {
        @UniqueConstraint(name = "uk_friendships_owner_friend", columnNames = { "owner_id", "friend_id" })
        },
        indexes = {
        @Index(name = "idx_friendships_owner_id", columnList = "owner_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "friend_id", nullable = false)
    private Long friendId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /** "ownerId 가 friendId 를 친구로 저장" 한 행을 만든다. */
    public static Friendship of(Long ownerId, Long friendId) {
        Friendship f = new Friendship();
        f.ownerId = ownerId;
        f.friendId = friendId;
        f.createdAt = LocalDateTime.now();
        return f;
      }
  }
