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

    private String merchantId;
    private String secretKey;
    private String mode = "sandbox";


    public String getBaseUrl() {
        if ("production".equalsIgnoreCase(mode)) {
            return "https://pay.sepay.vn";
        }
        return "https://pgapi-sandbox.sepay.vn";
    }

    public String getCheckoutUrl() {
        return getBaseUrl() + "/v1/checkout/init";
    }
}
