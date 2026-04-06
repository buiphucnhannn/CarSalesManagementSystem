package vn.edu.ute.carsalesms.session;

import vn.edu.ute.carsalesms.model.dto.AuthenticatedUser;

/**
 * Giao diện để lấy người dùng đã xác thực hiện tại.
 */
public interface CurrentUserProvider {

    /**
     * Lấy người dùng đã xác thực hiện tại.
     * @return người dùng đã xác thực hiện tại.
     */
    AuthenticatedUser getCurrentUser();
}
