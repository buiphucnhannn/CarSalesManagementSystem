package vn.edu.ute.carsalesms.service;

import vn.edu.ute.carsalesms.model.dto.AccountCommandRequest;
import vn.edu.ute.carsalesms.model.dto.AccountItem;
import vn.edu.ute.carsalesms.model.dto.StaffCommandRequest;
import vn.edu.ute.carsalesms.model.dto.StaffItem;
import vn.edu.ute.carsalesms.model.dto.StaffManagementMetadata;
import vn.edu.ute.carsalesms.model.enums.Status;

import java.util.List;

/**
 * Interface dịch vụ quản lý nhân viên và tài khoản.
 * Nhóm logic nghiệp vụ liên quan đến Staff và Account vào cùng một service
 * vì chúng có quan hệ 1-1 và thường thay đổi cùng nhau.
 */
public interface StaffService {

    // ─── Staff ───────────────────────────────────────────────────────────

    /**
     * Lấy danh sách nhân viên theo từ khóa và trạng thái.
     */
    List<StaffItem> getStaffs(String keyword, Status statusFilter);

    /**
     * Lấy metadata để điền ComboBox trong dialog.
     */
    StaffManagementMetadata getMetadata();

    /**
     * Thêm mới nhân viên sau khi validate.
     *
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ hoặc mã trùng
     */
    StaffItem createStaff(StaffCommandRequest request);

    /**
     * Cập nhật thông tin nhân viên.
     *
     * @throws IllegalArgumentException nếu không tìm thấy hoặc mã/email trùng
     */
    StaffItem updateStaff(StaffCommandRequest request);

    // ─── Account ─────────────────────────────────────────────────────────

    /**
     * Lấy danh sách tài khoản theo từ khóa.
     */
    List<AccountItem> getAccounts(String keyword);

    /**
     * Tạo tài khoản mới cho một nhân viên.
     * Hash mật khẩu trước khi lưu.
     *
     * @throws IllegalArgumentException nếu nhân viên không tồn tại, đã có tài khoản, hoặc username trùng
     */
    AccountItem createAccount(AccountCommandRequest request);

    /**
     * Cập nhật username / mật khẩu / trạng thái tài khoản.
     * Nếu rawPassword null thì giữ nguyên mật khẩu cũ.
     *
     * @throws IllegalArgumentException nếu không tìm thấy hoặc username trùng
     */
    AccountItem updateAccount(AccountCommandRequest request);

    /**
     * Khóa hoặc mở khóa tài khoản (toggle locked).
     * Reset failedLoginAttempts về 0 khi mở khóa.
     *
     * @param accountId id tài khoản
     * @throws IllegalArgumentException nếu không tìm thấy
     */
    AccountItem toggleLock(Long accountId);

    /**
     * Xóa tài khoản khỏi hệ thống.
     *
     * @param accountId id tài khoản cần xóa
     */
    void deleteAccount(Long accountId);
}
