package com.mango.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 관리자 계정. 운영에서는 환경 변수로 주입한다.
 *
 * password는 인코딩된 형태여야 한다. 앞에 인코더 식별자가 붙는다.
 *   개발: {noop}평문
 *   운영: {bcrypt}$2a$10$...
 */
@ConfigurationProperties("mango.admin")
public class AdminAccountProperties {

    private String username;

    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
