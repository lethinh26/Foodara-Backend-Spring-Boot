package com.db.foodara.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.sepay")
public class SepayConfig {

    private String secretKey;
    private String bankCode = "MBBank";
    private String accountNumber = "0943941773";
    private String qrTemplate = "compact";
}
