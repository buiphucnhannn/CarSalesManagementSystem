package vn.edu.ute.carsalesms.dao;

import vn.edu.ute.carsalesms.model.entity.Account;

import java.util.Optional;

/**
 * Giao diện cho đối tượng truy cập dữ liệu (DAO) của Account.
 * Nó tuân theo Nguyên tắc tách biệt giao diện (ISP) bằng cách chỉ xác định các phương thức cần thiết
 * để tương tác với dữ liệu tài khoản.
 */
public interface AccountDao {

    /**
     * Tìm một tài khoản theo tên người dùng.
     * @param username tên người dùng để tìm kiếm.
     * @return một Optional chứa tài khoản nếu tìm thấy, nếu không trả về một Optional trống.
     */
    Optional<Account> findByUsername(String username);

    /**
     * Lưu một tài khoản.
     * @param account tài khoản cần lưu.
     * @return tài khoản đã lưu.
     */
    Account save(Account account);
}
