package com.mgnt.ticketing.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvConfig {
    @Bean
    public Dotenv dotenv(){
        return Dotenv.load();
    }
    @Bean
    public String gmailUsername(Dotenv dotenv) {
        return dotenv.get("GMAIL_USERNAME");
    }

    @Bean
    public String gmailAppPassword(Dotenv dotenv) {
        return dotenv.get("GMAIL_APP_PASSWORD");
    }

    @Bean
    public String mysqlUsername(Dotenv dotenv) {
        return dotenv.get("MYSQL_USERNAME");
    }

    @Bean
    public String mysqlPassword(Dotenv dotenv) {
        return dotenv.get("MYSQL_PASSWORD");
    }

    @Bean
    public String jwtSecretKey(Dotenv dotenv) {
        return dotenv.get("JWT_SECRET_KEY");
    }
}

