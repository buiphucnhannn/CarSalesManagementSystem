package vn.edu.ute.carsalesms.controller;

import vn.edu.ute.carsalesms.model.dto.AuthenticatedUser;
import vn.edu.ute.carsalesms.service.AuthService;

/**
 * AuthController xử lý các yêu cầu xác thực người dùng.
 * Nó tuân theo Nguyên tắc Trách nhiệm Đơn lẻ (SRP) bằng cách chỉ tập trung vào logic xác thực.
 * Nó cũng tuân theo Nguyên tắc Đảo ngược Phụ thuộc (DIP) bằng cách phụ thuộc vào AuthService trừu tượng
 * thay vì một triển khai cụ thể.
 */
public class AuthController {

    private final AuthService authService;

    /**
     * Xây dựng một AuthController mới với AuthService đã cho.
     * @param authService dịch vụ sẽ được sử dụng để xác thực người dùng.
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Cố gắng đăng nhập người dùng với tên người dùng và mật khẩu đã cho.
     * @param username tên người dùng để xác thực.
     * @param passwordChars mật khẩu để xác thực.
     * @return một đối tượng AuthenticatedUser nếu xác thực thành công, nếu không trả về null.
     */
    public AuthenticatedUser login(String username, char[] passwordChars) {
        String password = passwordChars == null ? "" : new String(passwordChars);
        return authService.login(username, password);
    }
}
