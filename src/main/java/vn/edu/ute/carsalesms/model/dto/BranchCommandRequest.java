package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.Status;

public record BranchCommandRequest(
        Long id,
        String branchCode,
        String branchName,
        String address,
        String phone,
        String email,
        Status status
) {
}

