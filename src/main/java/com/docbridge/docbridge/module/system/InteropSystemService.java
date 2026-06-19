package com.docbridge.docbridge.module.system;

import com.docbridge.docbridge.module.system.dto.*;
import com.docbridge.docbridge.shared.kernel.ApiResponse;
import com.docbridge.docbridge.shared.kernel.AppException;
import com.docbridge.docbridge.shared.kernel.ErrorCode;
import com.docbridge.docbridge.shared.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InteropSystemService {

    private final InteropSystemRepository systemRepository;

    // ----------------------------------------------------------------
    // UC1.1 — Thêm mới hệ thống liên thông
    // ----------------------------------------------------------------
    @Transactional
    public SystemResponse create(CreateSystemRequest request, Long actorId) {
        if (systemRepository.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.SYSTEM_CODE_DUPLICATED);
        }

        InteropSystemEntity entity = InteropSystemEntity.builder()
                .code(request.getCode().toUpperCase())
                .name(request.getName())
                .description(request.getDescription())
                .status(InteropSystemStatus.ACTIVE)
                .createdBy(actorId)
                .build();

        return SystemResponse.from(systemRepository.save(entity));
    }

    // ----------------------------------------------------------------
    // UC1.2 / UC1.7 — Danh sách + tìm kiếm hệ thống liên thông
    // ----------------------------------------------------------------
    @Transactional(readOnly = true)
    public Page<SystemResponse> findAll(SystemFilterRequest filter) {
        PageRequest pageable = PageRequest.of(
                filter.getPage(),
                filter.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return systemRepository
                .findAllWithFilter(filter.getName(), filter.getStatus(), pageable)
                .map(e -> SystemResponse.from(e, systemRepository.countUnitBySystemId(e.getId())));
    }

    // ----------------------------------------------------------------
    // UC1.3 — Xem chi tiết hệ thống liên thông
    // ----------------------------------------------------------------
    @Transactional(readOnly = true)
    public SystemResponse findById(Long id) {
        InteropSystemEntity entity = getOrThrow(id);
        return SystemResponse.from(entity, systemRepository.countUnitBySystemId(id));
    }

    // ----------------------------------------------------------------
    // UC1.4 — Cập nhật thông tin hệ thống liên thông
    // ----------------------------------------------------------------
    @Transactional
    public SystemResponse update(Long id, UpdateSystemRequest request) {
        InteropSystemEntity entity = getOrThrow(id);

        entity.setName(request.getName());
        entity.setDescription(request.getDescription());

        return SystemResponse.from(systemRepository.save(entity));
    }

    // ----------------------------------------------------------------
    // UC1.5 — Khoá / mở khoá hệ thống liên thông
    // Cascade khoá đơn vị xử lý ở tầng runtime giao dịch (không UPDATE interop_unit)
    // ----------------------------------------------------------------
    @Transactional
    public SystemResponse toggleLock(Long id) {
        InteropSystemEntity entity = getOrThrow(id);

        entity.setStatus(
                entity.getStatus() == InteropSystemStatus.ACTIVE
                        ? InteropSystemStatus.LOCKED
                        : InteropSystemStatus.ACTIVE
        );

        return SystemResponse.from(systemRepository.save(entity));
    }

    // ----------------------------------------------------------------
    // UC1.6 — Xoá hệ thống liên thông (soft delete)
    // Chỉ xoá khi chưa có đơn vị nào
    // ----------------------------------------------------------------
    @Transactional
    public void delete(Long id) {
        getOrThrow(id); // xác nhận tồn tại trước

        if (systemRepository.existsUnitBySystemId(id)) {
            throw new AppException(ErrorCode.SYSTEM_HAS_UNITS);
        }

        systemRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<SystemSummaryResponse> findAllForDropdown() {
        return systemRepository.findAllByOrderByNameAsc()
                .stream()
                .map(SystemSummaryResponse::from)
                .toList();
    }

    // ----------------------------------------------------------------
    // Helper
    // ----------------------------------------------------------------
    private InteropSystemEntity getOrThrow(Long id) {
        return systemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SYSTEM_NOT_FOUND));
    }
}
