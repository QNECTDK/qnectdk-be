package com.qnectdk.domain.profile.service;

import com.qnectdk.domain.group.service.GroupService;
import com.qnectdk.domain.profile.dto.PersonCard;
import com.qnectdk.domain.profile.dto.PersonInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 친구·그룹 응답에 들어갈 공통 person 객체를 조립한다.
 * person = user·profile·interest(ProfileService) + groupTags(GroupService, viewer 기준).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonCardService {

    private final ProfileService profileService;
    private final GroupService groupService;

    /**
     * viewer가 바라보는 userIds의 완성된 person 카드 목록.
     * @param viewerId  조회자(groupTags는 이 사람의 그룹 기준)
     * @param userIds   카드로 만들 대상 사용자들
     */
    public List<PersonCard> getCards(Long viewerId, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        // 1) user·profile·interest 조합
        List<PersonInfo> infos = profileService.getPersonsByIds(userIds);
        // 2) groupTags (viewer 기준)
        Map<Long, List<String>> tagMap = groupService.getGroupNamesByMember(viewerId, userIds);
        // 3) 합치기
        return infos.stream()
                .map(info -> PersonCard.of(info, tagMap.getOrDefault(info.userId(), List.of())))
                .toList();
    }

    /** 단건 편의 메서드 (멤버 추가 응답 등 한 명만 필요할 때). */
    public PersonCard getCard(Long viewerId, Long userId) {
        List<PersonCard> cards = getCards(viewerId, List.of(userId));
        return cards.isEmpty() ? null : cards.get(0);
    }
}