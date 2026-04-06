package vn.edu.ute.carsalesms.session;

import vn.edu.ute.carsalesms.model.dto.AuthenticatedUser;

/**
 * Giao diện để lưu trữ và truy xuất người dùng đã xác thực.
 * Mở rộng CurrentUserProvider, có thể cung cấp một phương thức để lấy người dùng hiện tại.
 */
public interface AuthSessionStore extends CurrentUserProvider {

    /**
     * Đặt người dùng hiện tại.
     * @param user người dùng cần đặt.
     */
    void setCurrentUser(AuthenticatedUser user);

    /**
     * Xóa người dùng hiện tại.
     */
    void clear();
}
