package com.qnectdk.domain.group.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "friend_group_members",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"group_id", "friend_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendGroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "friend_id", nullable = false)
    private Long friendId;

    // --- 도메인 메서드 ---

    public static FriendGroupMember of(Long groupId, Long friendId) {
        FriendGroupMember m = new FriendGroupMember();
        m.groupId = groupId;
        m.friendId = friendId;
        return m;
    }
}