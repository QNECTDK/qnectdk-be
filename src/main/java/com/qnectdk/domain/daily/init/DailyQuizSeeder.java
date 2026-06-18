package com.qnectdk.domain.daily.init;

import com.qnectdk.domain.daily.entity.DailyQuiz;
import com.qnectdk.domain.daily.repository.DailyQuizRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * 오늘의 데일리 시드. 기동 시 오늘 날짜의 데일리가 없으면 풀에서 날짜 기준으로 골라 1건 적재(멱등).
 * 자정 자동 갱신 스케줄러는 추후(B 또는 운영 도구)에 둔다 — 지금은 시연용 시드로 항상 오늘 질문을 보장한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DailyQuizSeeder implements ApplicationRunner {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final List<String[]> POOL = List.of(
            new String[]{"탕수육은 찍먹 vs 부먹?", "찍먹", "부먹"},
            new String[]{"민트초코는?", "맛있다", "치약맛"},
            new String[]{"여행 스타일은?", "계획파", "즉흥파"},
            new String[]{"아침형 vs 저녁형?", "아침형", "저녁형"},
            new String[]{"라면 면발은?", "꼬들면", "퍼진면"},
            new String[]{"치킨은?", "양념", "후라이드"},
            new String[]{"연애할 때 더 중요한 건?", "표현", "행동"}
    );

    private final DailyQuizRepository dailyQuizRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        LocalDate today = LocalDate.now(ZONE);
        if (dailyQuizRepository.findByQuizDate(today).isPresent()) {
            return;
        }
        String[] pick = POOL.get((int) Math.floorMod(today.toEpochDay(), POOL.size()));
        dailyQuizRepository.save(DailyQuiz.create(today, pick[0], pick[1], pick[2]));
        log.info("오늘의 데일리 시드 적재: {} ({} vs {})", pick[0], pick[1], pick[2]);
    }
}
