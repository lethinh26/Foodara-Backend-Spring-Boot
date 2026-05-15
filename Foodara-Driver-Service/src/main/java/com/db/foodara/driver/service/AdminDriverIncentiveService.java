package com.db.foodara.driver.service;

import com.db.foodara.driver.dto.request.IncentiveProgramRequest;
import com.db.foodara.driver.dto.response.DriverIncentiveProgressResponse;
import com.db.foodara.driver.dto.response.DriverIncentiveProgramResponse;
import com.db.foodara.driver.dto.response.PageResponse;
import com.db.foodara.driver.entity.DriverIncentiveProgram;
import com.db.foodara.driver.entity.DriverIncentiveProgress;
import com.db.foodara.driver.exception.AppException;
import com.db.foodara.driver.exception.ErrorCode;
import com.db.foodara.driver.repository.DriverIncentiveProgramRepository;
import com.db.foodara.driver.repository.DriverIncentiveProgressRepository;
import com.db.foodara.driver.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDriverIncentiveService {

    private final DriverIncentiveProgramRepository programRepository;
    private final DriverIncentiveProgressRepository progressRepository;
    private final DriverRepository driverRepository;

    private static final Set<String> VALID_TARGET_TYPES = Set.of(
            "daily_orders", "weekly_orders", "acceptance_rate", "peak_hour", "completion_rate"
    );

    public PageResponse<DriverIncentiveProgramResponse> getPrograms(int page, int size, String search, Boolean isActive) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<DriverIncentiveProgram> programPage;

        if (search != null && !search.isBlank()) {
            programPage = programRepository.searchByName(search.trim(), pageRequest);
        } else if (isActive != null) {
            programPage = programRepository.findByIsActive(isActive, pageRequest);
        } else {
            programPage = programRepository.findAll(pageRequest);
        }

        List<DriverIncentiveProgramResponse> content = programPage.getContent().stream()
                .map(this::mapProgramToResponse)
                .toList();

        return PageResponse.<DriverIncentiveProgramResponse>builder()
                .content(content)
                .page(programPage.getNumber())
                .number(programPage.getNumber())
                .size(programPage.getSize())
                .totalElements(programPage.getTotalElements())
                .totalPages(programPage.getTotalPages())
                .last(programPage.isLast())
                .build();
    }

    @Transactional
    public DriverIncentiveProgramResponse createProgram(IncentiveProgramRequest request) {
        validateTargetType(request.getTargetType());

        DriverIncentiveProgram program = new DriverIncentiveProgram();
        program.setName(request.getName());
        program.setDescription(request.getDescription());
        program.setTargetType(request.getTargetType());
        program.setTargetValue(request.getTargetValue());
        program.setBonusAmount(request.getBonusAmount());
        program.setMaxParticipants(request.getMaxParticipants());
        program.setStartsAt(request.getStartsAt());
        program.setEndsAt(request.getEndsAt());
        program.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        DriverIncentiveProgram saved = programRepository.save(program);
        log.info("Created incentive program: {} (id={})", saved.getName(), saved.getId());
        return mapProgramToResponse(saved);
    }

    @Transactional
    public DriverIncentiveProgramResponse updateProgram(String id, IncentiveProgramRequest request) {
        DriverIncentiveProgram program = programRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INCENTIVE_NOT_FOUND));

        if (request.getTargetType() != null) {
            validateTargetType(request.getTargetType());
            program.setTargetType(request.getTargetType());
        }

        if (request.getName() != null) {
            program.setName(request.getName());
        }
        if (request.getDescription() != null) {
            program.setDescription(request.getDescription());
        }
        if (request.getTargetValue() != null) {
            program.setTargetValue(request.getTargetValue());
        }
        if (request.getBonusAmount() != null) {
            program.setBonusAmount(request.getBonusAmount());
        }
        if (request.getMaxParticipants() != null) {
            program.setMaxParticipants(request.getMaxParticipants());
        }
        if (request.getStartsAt() != null) {
            program.setStartsAt(request.getStartsAt());
        }
        if (request.getEndsAt() != null) {
            program.setEndsAt(request.getEndsAt());
        }
        if (request.getIsActive() != null) {
            program.setIsActive(request.getIsActive());
        }

        DriverIncentiveProgram saved = programRepository.save(program);
        log.info("Updated incentive program: {} (id={})", saved.getName(), saved.getId());
        return mapProgramToResponse(saved);
    }

    public PageResponse<DriverIncentiveProgressResponse> getProgramProgress(String programId, int page, int size) {
        DriverIncentiveProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new AppException(ErrorCode.INCENTIVE_NOT_FOUND));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<DriverIncentiveProgress> progressPage = progressRepository.findByProgramId(programId, pageRequest);

        List<DriverIncentiveProgressResponse> content = progressPage.getContent().stream()
                .map(p -> mapProgressToResponse(p, program.getTargetValue()))
                .toList();

        return PageResponse.<DriverIncentiveProgressResponse>builder()
                .content(content)
                .page(progressPage.getNumber())
                .number(progressPage.getNumber())
                .size(progressPage.getSize())
                .totalElements(progressPage.getTotalElements())
                .totalPages(progressPage.getTotalPages())
                .last(progressPage.isLast())
                .build();
    }

    // --- Validators ---

    private void validateTargetType(String targetType) {
        if (!VALID_TARGET_TYPES.contains(targetType)) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }
    }

    // --- Mappers ---

    private DriverIncentiveProgramResponse mapProgramToResponse(DriverIncentiveProgram p) {
        long currentParticipants = progressRepository.countByProgramId(p.getId());

        return DriverIncentiveProgramResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .targetType(p.getTargetType())
                .targetValue(p.getTargetValue())
                .bonusAmount(p.getBonusAmount())
                .maxParticipants(p.getMaxParticipants())
                .currentParticipants(currentParticipants)
                .startsAt(p.getStartsAt())
                .endsAt(p.getEndsAt())
                .isActive(p.getIsActive())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private DriverIncentiveProgressResponse mapProgressToResponse(DriverIncentiveProgress p, Integer targetValue) {
        String driverName = driverRepository.findById(p.getDriverId())
                .map(d -> d.getFullName())
                .orElse(null);

        return DriverIncentiveProgressResponse.builder()
                .id(p.getId())
                .programId(p.getProgramId())
                .driverId(p.getDriverId())
                .driverName(driverName)
                .currentValue(p.getCurrentValue())
                .targetValue(targetValue)
                .isCompleted(p.getIsCompleted())
                .completedAt(p.getCompletedAt())
                .bonusPaid(p.getBonusPaid())
                .paidAt(p.getPaidAt())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
