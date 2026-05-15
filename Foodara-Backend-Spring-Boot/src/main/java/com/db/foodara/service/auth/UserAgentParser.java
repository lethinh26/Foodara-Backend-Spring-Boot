package com.db.foodara.service.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserAgentParser {

    public String extractBrowser(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }

        userAgent = userAgent.toLowerCase();
        
        if (userAgent.contains("edg")) return "Edge";
        if (userAgent.contains("chrome")) return "Chrome";
        if (userAgent.contains("safari") && !userAgent.contains("chrome")) return "Safari";
        if (userAgent.contains("firefox")) return "Firefox";
        if (userAgent.contains("opera") || userAgent.contains("opr")) return "Opera";
        if (userAgent.contains("msie") || userAgent.contains("trident")) return "Internet Explorer";
        
        return "Unknown";
    }

    public String extractOS(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }

        userAgent = userAgent.toLowerCase();
        
        if (userAgent.contains("windows nt 10.0")) return "Windows 10";
        if (userAgent.contains("windows nt 6.3")) return "Windows 8.1";
        if (userAgent.contains("windows nt 6.2")) return "Windows 8";
        if (userAgent.contains("windows nt 6.1")) return "Windows 7";
        if (userAgent.contains("windows")) return "Windows";
        
        if (userAgent.contains("mac os x")) return "macOS";
        if (userAgent.contains("macintosh")) return "Mac";
        
        if (userAgent.contains("android")) return "Android";
        if (userAgent.contains("iphone") || userAgent.contains("ipad")) return "iOS";
        
        if (userAgent.contains("linux")) return "Linux";
        if (userAgent.contains("ubuntu")) return "Ubuntu";
        
        return "Unknown";
    }

    public String extractDeviceType(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Desktop";
        }

        userAgent = userAgent.toLowerCase();
        
        if (userAgent.contains("mobile") || 
            userAgent.contains("android") || 
            userAgent.contains("iphone") ||
            userAgent.contains("ipod") ||
            userAgent.contains("blackberry") ||
            userAgent.contains("windows phone")) {
            return "Mobile";
        }
        
        if (userAgent.contains("tablet") || userAgent.contains("ipad")) {
            return "Tablet";
        }
        
        return "Desktop";
    }
}
