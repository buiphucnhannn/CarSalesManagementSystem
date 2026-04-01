package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.StaffRole;

public record AuthenticatedUser(
		Long accountId,
		Long staffId,
		String staffCode,
		String fullName,
		String username,
		StaffRole role,
		String branchName,
		Long branchId
) {
}

