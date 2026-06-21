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
        // 대분류(category) → 상세(name) 2단계. category 문자열은 프론트 아이콘 매핑 키와 정확히 일치해야 한다
        // (InterestSelect/InterestEdit 의 categoryIcons:
        // 여행·운동·음악·게임·요리·자기계발·반려동물·독서·영화·사진·예술·기술/IT·패션·비즈니스).
        addCategory(interests, "여행", "동남아", "일본", "중국", "미국", "유럽", "호주", "제주", "국내여행", "캠핑", "백패킹");
        addCategory(interests, "운동", "헬스", "러닝", "등산", "클라이밍", "요가", "수영", "축구", "농구", "테니스", "골프");
        addCategory(interests, "음악", "K-POP", "힙합", "R&B", "밴드", "발라드", "인디", "재즈", "클래식", "EDM", "팝");
        addCategory(interests, "게임", "롤", "오버워치", "발로란트", "콘솔게임", "모바일게임", "스팀", "RPG", "FPS", "닌텐도", "PC방");
        addCategory(interests, "요리", "맛집탐방", "디저트", "커피", "베이킹", "한식", "양식", "일식", "비건", "홈쿠킹", "분식");
        addCategory(interests, "자기계발", "독서모임", "글쓰기", "어학", "자격증", "재테크", "강연", "명상", "갓생", "코딩", "스터디");
        addCategory(interests, "반려동물", "강아지", "고양이", "반려식물", "햄스터", "파충류", "앵무새", "물고기");
        addCategory(interests, "독서", "소설", "에세이", "자기계발서", "인문", "과학", "경제", "시", "만화", "웹소설", "추리");
        addCategory(interests, "영화", "액션", "로맨스", "공포", "코미디", "SF", "스릴러", "애니메이션", "다큐", "드라마", "판타지");
        addCategory(interests, "사진", "인물사진", "풍경사진", "필름카메라", "스냅", "여행사진", "일상사진", "흑백사진");
        addCategory(interests, "예술", "미술관", "전시관람", "그림", "드로잉", "공방", "도예", "캘리그라피", "뮤지컬", "연극", "콘서트");
        addCategory(interests, "기술/IT", "프로그래밍", "AI", "가젯", "스타트업", "블록체인", "앱개발", "웹개발", "UX디자인", "데이터", "보안");
        addCategory(interests, "패션", "스트릿", "미니멀", "빈티지", "명품", "액세서리", "신발", "코디", "쇼핑", "메이크업", "헤어");
        addCategory(interests, "비즈니스", "창업", "마케팅", "주식", "부동산", "경영", "네트워킹", "사이드프로젝트", "투자");
        return interests;
    }

    private void addCategory(List<Interest> target, String category, String... names) {
        for (String name : names) {
            target.add(Interest.create(category, name));
        }
    }
}
