package com.qnectdk.domain.quiz.port;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * point 도메인(B) 연동 전까지 쓰는 임시 무동작 {@link PointPort} 빈. // B 합의 필요
 * B 가 PointPort 빈을 제공하면 {@code @ConditionalOnMissingBean} 으로 자동 대체된다.
 */
@Configuration
class PointPortStubConfig {

    private static final Logger log = LoggerFactory.getLogger(PointPortStubConfig.class);

    @Bean
    @ConditionalOnMissingBean(PointPort.class)
    PointPort noOpPointPort() {
        return (solverId, ownerId, quizId) ->
                log.info("[stub] earnQuizFirstSolve solverId={}, ownerId={}, quizId={} — point 도메인 연동 전 무동작",
                        solverId, ownerId, quizId);
    }
}
