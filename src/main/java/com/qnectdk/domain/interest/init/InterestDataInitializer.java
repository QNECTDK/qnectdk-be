package com.qnectdk.domain.interest.init;

import com.qnectdk.domain.interest.entity.Interest;
import com.qnectdk.domain.interest.repository.InterestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 관심사 마스터 시드. 테이블이 비어 있을 때만 적재(멱등). 팀이 추후 확장.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InterestDataInitializer implements ApplicationRunner {

    private final InterestRepository interestRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (interestRepository.count() > 0) {
            return;
        }
        List<Interest> seeds = buildSeeds();
        interestRepository.saveAll(seeds);
        log.info("관심사 시드 {}건 적재 완료", seeds.size());
    }

    private List<Interest> buildSeeds() {
        List<Interest> interests = new ArrayList<>();
        addCategory(interests, "여행", "동남아", "일본", "중국", "미국", "유럽", "호주", "제주", "국내여행", "캠핑", "백패킹");
        addCategory(interests, "음악", "K-POP", "힙합", "R&B", "밴드", "발라드", "인디", "재즈", "클래식", "EDM", "팝");
        addCategory(interests, "취미", "영화관람", "드라이브", "카페", "독서", "사진", "그림", "베이킹", "보드게임", "방탈출", "전시관람");
        addCategory(interests, "운동", "헬스", "러닝", "등산", "클라이밍", "요가", "수영", "축구", "농구", "테니스", "골프");
        addCategory(interests, "음식", "맛집탐방", "디저트", "커피", "술", "요리", "비건", "매운음식", "고기", "해산물", "분식");
        addCategory(interests, "게임", "롤", "오버워치", "발로란트", "콘솔게임", "모바일게임", "스팀", "RPG", "FPS", "닌텐도", "PC방");
        addCategory(interests, "문화", "뮤지컬", "연극", "콘서트", "페스티벌", "영화제", "공방", "팝업스토어", "미술관");
        addCategory(interests, "반려", "강아지", "고양이", "반려식물");
        return interests;
    }

    private void addCategory(List<Interest> target, String category, String... names) {
        for (String name : names) {
            target.add(Interest.create(category, name));
        }
    }
}
