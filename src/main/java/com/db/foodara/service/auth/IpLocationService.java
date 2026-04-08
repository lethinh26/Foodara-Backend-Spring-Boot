package com.db.foodara.service.auth;

import com.db.foodara.dto.response.auth.IpLocationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class IpLocationService {
    
    private static final String IP_API_URL = "http://ip-api.com/json/";
    private final RestTemplate restTemplate;

    public IpLocationService() {
        this.restTemplate = new RestTemplate();
    }

    public IpLocationResponse getLocationByIp(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty() || 
            ipAddress.equals("0:0:0:0:0:0:0:1") || 
            ipAddress.equals("127.0.0.1") ||
            ipAddress.startsWith("192.168.") ||
            ipAddress.startsWith("10.") ||
            ipAddress.startsWith("172.")) {
            log.warn("Local or private IP address detected: {}", ipAddress);
            return createDefaultResponse(ipAddress);
        }

        try {
            String url = IP_API_URL + ipAddress;
            IpLocationResponse response = restTemplate.getForObject(url, IpLocationResponse.class);
            
            if (response != null && response.isSuccess()) {
                log.info("Successfully fetched location for IP: {}", ipAddress);
                return response;
            } else {
                log.warn("Failed to fetch location for IP: {} - Status: {}", 
                    ipAddress, response != null ? response.getStatus() : "null");
                return createDefaultResponse(ipAddress);
            }
        } catch (Exception e) {
            log.error("Error fetching location for IP: {}", ipAddress, e);
            return createDefaultResponse(ipAddress);
        }
    }

    private IpLocationResponse createDefaultResponse(String ipAddress) {
        IpLocationResponse response = new IpLocationResponse();
        response.setStatus("fail");
        response.setQuery(ipAddress);
        response.setCountry("Unknown");
        response.setCity("Unknown");
        response.setRegion("Unknown");
        response.setTimezone("Unknown");
        response.setIsp("Unknown");
        return response;
    }

    public String extractIpFromRequest(String xForwardedFor, String remoteAddr) {
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For có thể chứa nhiều IP, lấy IP đầu tiên (client IP)
            String[] ips = xForwardedFor.split(",");
            return ips[0].trim();
        }
        return remoteAddr;
    }
}
