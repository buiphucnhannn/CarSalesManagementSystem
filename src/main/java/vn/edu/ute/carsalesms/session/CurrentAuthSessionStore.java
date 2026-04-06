package vn.edu.ute.carsalesms.session;

import vn.edu.ute.carsalesms.model.dto.AuthenticatedUser;

/**
 * Triển khai của AuthSessionStore sử dụng một lớp CurrentSession tĩnh để lưu trữ và truy xuất người dùng đã xác thực.
 */
public class CurrentAuthSessionStore implements AuthSessionStore {

	/**
	 * Lấy người dùng đã xác thực hiện tại.
	 * @return người dùng đã xác thực hiện tại.
	 */
	@Override
	public AuthenticatedUser getCurrentUser() {
		return CurrentSession.getCurrentUser();
	}

	/**
	 * Đặt người dùng hiện tại.
	 * @param user người dùng cần đặt.
	 */
	@Override
	public void setCurrentUser(AuthenticatedUser user) {
		CurrentSession.setCurrentUser(user);
	}

	/**
	 * Xóa người dùng hiện tại.
	 */
	@Override
	public void clear() {
		CurrentSession.clear();
	}
}
