package com.db.foodara.service.admin;

import com.db.foodara.dto.response.admin.AdminBannerResponse;
import com.db.foodara.entity.home.Banner;
import com.db.foodara.exception.AppException;
import com.db.foodara.exception.ErrorCode;
import com.db.foodara.repository.home.BannerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminBannerService {

    private final BannerRepository bannerRepository;

    public List<AdminBannerResponse> getBanners() {
        return bannerRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public AdminBannerResponse createBanner(Map<String, Object> request) {
        Banner banner = new Banner();
        applyBannerFields(banner, request);
        bannerRepository.save(banner);
        return mapToResponse(banner);
    }

    @Transactional
    public void updateBanner(String id, Map<String, Object> request) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BANNER_NOT_FOUND));
        applyBannerFields(banner, request);
        bannerRepository.save(banner);
    }

    @Transactional
    public void deleteBanner(String id) {
        if (!bannerRepository.existsById(id)) {
            throw new AppException(ErrorCode.BANNER_NOT_FOUND);
        }
        bannerRepository.deleteById(id);
    }

    private void applyBannerFields(Banner b, Map<String, Object> data) {
        if (data.containsKey("title")) b.setTitle((String) data.get("title"));
        if (data.containsKey("imageUrl")) b.setImageUrl((String) data.get("imageUrl"));
        if (data.containsKey("targetUrl")) b.setTargetUrl((String) data.get("targetUrl"));
        if (data.containsKey("targetType")) b.setTargetType((String) data.get("targetType"));
        if (data.containsKey("targetId")) b.setTargetId((String) data.get("targetId"));
        if (data.containsKey("position")) b.setPosition((String) data.get("position"));
        if (data.containsKey("displayOrder")) b.setDisplayOrder(toInteger(data.get("displayOrder")));
        if (data.containsKey("isActive")) b.setIsActive((Boolean) data.get("isActive"));
        if (data.containsKey("startsAt")) b.setStartsAt(toDateTime(data.get("startsAt")));
        if (data.containsKey("endsAt")) b.setEndsAt(toDateTime(data.get("endsAt")));
    }

    private AdminBannerResponse mapToResponse(Banner b) {
        return AdminBannerResponse.builder()
                .id(b.getId())
                .title(b.getTitle())
                .imageUrl(b.getImageUrl())
                .targetUrl(b.getTargetUrl())
                .targetType(b.getTargetType())
                .targetId(b.getTargetId())
                .position(b.getPosition())
                .displayOrder(b.getDisplayOrder())
                .isActive(b.getIsActive())
                .startsAt(b.getStartsAt())
                .endsAt(b.getEndsAt())
                .createdAt(b.getCreatedAt())
                .build();
    }

    private Integer toInteger(Object val) {
        if (val == null) return null;
        if (val instanceof Number n) return n.intValue();
        return Integer.parseInt(val.toString());
    }

    private LocalDateTime toDateTime(Object val) {
        if (val == null) return null;
        return LocalDateTime.parse(val.toString());
    }
}
