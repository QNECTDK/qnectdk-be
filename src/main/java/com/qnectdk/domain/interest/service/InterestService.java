package com.qnectdk.domain.interest.service;

import com.qnectdk.domain.interest.dto.InterestCategoryResponse;
import com.qnectdk.domain.interest.dto.InterestItem;
import com.qnectdk.domain.interest.dto.InterestResponse;
import com.qnectdk.domain.interest.entity.Interest;
import com.qnectdk.domain.interest.entity.UserInterest;
import com.qnectdk.domain.interest.repository.InterestRepository;
import com.qnectdk.domain.interest.repository.UserInterestRepository;
import com.qnectdk.global.exception.BusinessException;
import com.qnectdk.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterestService {

    private final InterestRepository interestRepository;
    private final UserInterestRepository userInterestRepository;

    public List<InterestCategoryResponse> getAllGroupedByCategory() {
        Map<String, List<InterestItem>> grouped = interestRepository.findAllByOrderByCategoryAscNameAsc().stream()
                .collect(Collectors.groupingBy(
                        Interest::getCategory,
                        LinkedHashMap::new,
                        Collectors.mapping(
                                interest -> new InterestItem(interest.getId(), interest.getName()),
                                Collectors.toList())));
        return grouped.entrySet().stream()
                .map(entry -> new InterestCategoryResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<InterestResponse> getMine(Long userId) {
        List<Long> interestIds = userInterestRepository.findByUserId(userId).stream()
                .map(UserInterest::getInterestId)
                .toList();
        if (interestIds.isEmpty()) {
            return List.of();
        }
        // findAllById는 순서를 보장하지 않으므로 카탈로그와 동일하게 카테고리/이름순으로 정렬해 반환
        return interestRepository.findAllById(interestIds).stream()
                .sorted(Comparator.comparing(Interest::getCategory).thenComparing(Interest::getName))
                .map(InterestResponse::from)
                .toList();
    }

    @Transactional
    public List<InterestResponse> replaceMine(Long userId, List<Long> interestIds) {
        List<Long> distinctIds = interestIds == null ? List.of()
                : interestIds.stream().filter(Objects::nonNull).distinct().toList();
        List<Interest> interests = loadAndValidate(distinctIds);

        userInterestRepository.deleteByUserId(userId);
        userInterestRepository.flush();
        userInterestRepository.saveAll(distinctIds.stream()
                .map(interestId -> UserInterest.of(userId, interestId))
                .toList());

        return interests.stream().map(InterestResponse::from).toList();
    }

    private List<Interest> loadAndValidate(List<Long> interestIds) {
        if (interestIds.isEmpty()) {
            return List.of();
        }
        List<Interest> found = interestRepository.findAllById(interestIds);
        if (found.size() != interestIds.size()) {
            throw new BusinessException(ErrorCode.INTEREST_NOT_FOUND);
        }
        return found;
    }
}
