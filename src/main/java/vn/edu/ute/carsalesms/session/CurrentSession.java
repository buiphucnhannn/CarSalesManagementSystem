package vn.edu.ute.carsalesms.session;

import vn.edu.ute.carsalesms.model.dto.AuthenticatedUser;
import vn.edu.ute.carsalesms.model.enums.StaffRole;

public final class CurrentSession {

	public static final String BRANCH_PERMISSION_MESSAGE_PREFIX = "Bạn không có quyền để tác động đến chi nhánh";

	private static AuthenticatedUser currentUser;

	private CurrentSession() {
	}

	public static void setCurrentUser(AuthenticatedUser user) {
		currentUser = user;
	}

	public static AuthenticatedUser getCurrentUser() {
		return currentUser;
	}

	public static boolean isAuthenticated() {
		return currentUser != null;
	}

	public static boolean hasRole(StaffRole role) {
		return currentUser != null && currentUser.role() == role;
	}

	public static boolean isAdmin() {
		return hasRole(StaffRole.ADMIN);
	}

	public static Long currentBranchId() {
		return currentUser == null ? null : currentUser.branchId();
	}

	public static void assertBranchAccess(Long targetBranchId, String targetBranchName) {
		if (targetBranchId == null || isAdmin()) {
			return;
		}
		Long sessionBranchId = currentBranchId();
		if (sessionBranchId != null && sessionBranchId.equals(targetBranchId)) {
			return;
		}
		throw new IllegalStateException(BRANCH_PERMISSION_MESSAGE_PREFIX + " " + resolveBranchLabel(targetBranchName, targetBranchId) + ".");
	}

	private static String resolveBranchLabel(String targetBranchName, Long targetBranchId) {
		if (targetBranchName != null && !targetBranchName.isBlank()) {
			return "'" + targetBranchName + "'";
		}
		return "ID=" + targetBranchId;
	}

	public static void clear() {
		currentUser = null;
	}
}

