package com.mango.fukuoka.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Fukuoka DB용 Flyway.
 *
 * Spring Boot의 Flyway 자동 설정은 기본 데이터소스 하나만 다루므로,
 * 두 번째 DB는 이렇게 직접 등록해야 한다.
 *
 * initMethod로 빈 생성 시점에 마이그레이션을 실행한다.
 * FukuokaJpaConfig의 EntityManagerFactory가 이 빈에 의존하도록 해서,
 * Hibernate가 ddl-auto=validate로 스키마를 검증하기 전에 끝나도록 보장한다.
 */
@Configuration
public class FukuokaFlywayConfig {

    @Bean(initMethod = "migrate")
    public Flyway fukuokaFlyway(
            @Qualifier("fukuokaDataSource")
            DataSource dataSource,

            @Value("${fukuoka.flyway.locations}")
            String locations
    ) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations(locations)
                /*
                 * 이 DB는 그동안 Flyway 없이 운영되어 이력 테이블이 없다.
                 * 기존 DB에서는 baseline 기록만 남기고 V1을 건너뛰게 한다.
                 */
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .load();
    }
}
