package com.qnectdk.domain.friend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "friend_memos",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"owner_id", "friend_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendMemo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "friend_id", nullable = false)
    private Long friendId;

    @Column(length = 200)
    private String content;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // --- 도메인 메서드 ---

    public static FriendMemo create(Long ownerId, Long friendId, String content) {
        FriendMemo m = new FriendMemo();
        m.ownerId = ownerId;
        m.friendId = friendId;
        m.content = content;
        m.updatedAt = LocalDateTime.now();
        return m;
    }

    public void updateContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }
}