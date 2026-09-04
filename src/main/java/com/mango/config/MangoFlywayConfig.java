package com.mango.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * MANGO DB용 Flyway.
 *
 * Spring Boot의 Flyway 자동 설정은 @ConditionalOnMissingBean(Flyway.class)라서,
 * Fukuoka용 Flyway 빈을 등록하는 순간 이쪽 자동 설정이 함께 꺼진다.
 * 그래서 두 DB 모두 명시적으로 등록한다.
 *
 * 접속 정보는 데이터소스 빈에서 그대로 가져온다.
 * 예전에는 spring.flyway.url이 spring.datasource.url을 따로 복제하고 있어서
 * 두 값이 서로 다른 포트를 가리키는 일이 있었다.
 */
@Configuration
public class MangoFlywayConfig {

    @Bean(initMethod = "migrate")
    public Flyway mangoFlyway(
            @Qualifier("dataSource")
            DataSource dataSource,

            @Value("${mango.flyway.locations}")
            String locations
    ) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations(locations)
                /*
                 * 기존 운영 DB에는 이미 스키마가 있으므로
                 * baseline 기록만 남기고 V1을 건너뛰게 한다.
                 */
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .load();
    }
}
