package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.Status;

import java.time.LocalDateTime;

public record BranchItem(
        Long id,
        String branchCode,
        String branchName,
        String address,
        String phone,
        String email,
        Status status,
        LocalDateTime createdAt
) {
}

