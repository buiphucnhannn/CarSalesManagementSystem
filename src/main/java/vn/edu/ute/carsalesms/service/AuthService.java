package vn.edu.ute.carsalesms.service;

import vn.edu.ute.carsalesms.model.dto.AuthenticatedUser;

/**
 * Giao diện cho dịch vụ xác thực.
 * Nó tuân theo Nguyên tắc tách biệt giao diện (ISP) bằng cách chỉ xác định một phương thức duy nhất, `login`.
 */
public interface AuthService {

    /**
     * Cố gắng đăng nhập người dùng với tên người dùng và mật khẩu đã cho.
     * @param username tên người dùng để xác thực.
     * @param rawPassword mật khẩu để xác thực.
     * @return một đối tượng AuthenticatedUser nếu xác thực thành công, nếu không trả về null.
     */
    AuthenticatedUser login(String username, String rawPassword);
}
