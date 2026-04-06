package vn.edu.ute.carsalesms.session;

import vn.edu.ute.carsalesms.model.dto.AuthenticatedUser;
import vn.edu.ute.carsalesms.model.enums.StaffRole;

/**
 * Lớp tiện ích để quản lý phiên người dùng hiện tại.
 * Sử dụng một biến tĩnh để lưu trữ người dùng đã xác thực.
 */
public final class CurrentSession {

	public static final String BRANCH_PERMISSION_MESSAGE_PREFIX = "Bạn không có quyền để tác động đến chi nhánh";

	private static AuthenticatedUser currentUser;

	private CurrentSession() {
	}

	/**
	 * Đặt người dùng hiện tại.
	 * @param user người dùng cần đặt.
	 */
	public static void setCurrentUser(AuthenticatedUser user) {
		currentUser = user;
	}

	/**
	 * Lấy người dùng đã xác thực hiện tại.
	 * @return người dùng đã xác thực hiện tại.
	 */
	public static AuthenticatedUser getCurrentUser() {
		return currentUser;
	}

	/**
	 * Kiểm tra xem người dùng có được xác thực hay không.
	 * @return true nếu người dùng được xác thực, nếu không trả về false.
	 */
	public static boolean isAuthenticated() {
		return currentUser != null;
	}

	/**
	 * Kiểm tra xem người dùng có một vai trò cụ thể hay không.
	 * @param role vai trò cần kiểm tra.
	 * @return true nếu người dùng có vai trò, nếu không trả về false.
	 */
	public static boolean hasRole(StaffRole role) {
		return currentUser != null && currentUser.role() == role;
	}

	/**
	 * Kiểm tra xem người dùng có phải là quản trị viên hay không.
	 * @return true nếu người dùng là quản trị viên, nếu không trả về false.
	 */
	public static boolean isAdmin() {
		return hasRole(StaffRole.ADMIN);
	}

	/**
	 * Lấy ID của chi nhánh hiện tại.
	 * @return ID của chi nhánh hiện tại.
	 */
	public static Long currentBranchId() {
		return currentUser == null ? null : currentUser.branchId();
	}

	/**
	 * Xác nhận quyền truy cập vào một chi nhánh.
	 * @param targetBranchId ID của chi nhánh mục tiêu.
	 * @param targetBranchName tên của chi nhánh mục tiêu.
	 */
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

	/**
	 * Giải quyết nhãn của một chi nhánh.
	 * @param targetBranchName tên của chi nhánh mục tiêu.
	 * @param targetBranchId ID của chi nhánh mục tiêu.
	 * @return nhãn của chi nhánh.
	 */
	private static String resolveBranchLabel(String targetBranchName, Long targetBranchId) {
		if (targetBranchName != null && !targetBranchName.isBlank()) {
			return "'" + targetBranchName + "'";
		}
		return "ID=" + targetBranchId;
	}

	/**
	 * Xóa người dùng hiện tại.
	 */
	public static void clear() {
		currentUser = null;
	}
}
