package vn.edu.ute.carsalesms.dao;

import vn.edu.ute.carsalesms.model.entity.Account;
import vn.edu.ute.carsalesms.model.entity.Branch;
import vn.edu.ute.carsalesms.model.entity.Staff;
import vn.edu.ute.carsalesms.model.enums.Status;

import java.util.List;
import java.util.Optional;

/**
 * Interface DAO kết hợp Staff và Account.
 * Staff và Account gắn chặt 1-1 nên quản lý chung trong một DAO.
 * Nguyên tắc: mỗi phương thức chỉ làm một việc (Single Responsibility).
 */
public interface StaffDao {

    // ─── Staff ───────────────────────────────────────────────────────────

    /**
     * Tìm danh sách nhân viên theo từ khóa và trạng thái.
     *
     * @param keyword      tìm theo mã / tên / email (null = tất cả)
     * @param statusFilter lọc theo Status (null = tất cả)
     * @return danh sách Staff có eager-fetch Branch và Account
     */
    List<Staff> findStaffs(String keyword, Status statusFilter);

    /**
     * Tìm nhân viên theo id.
     */
    Optional<Staff> findStaffById(Long id);

    /**
     * Tìm nhân viên theo mã nhân viên.
     */
    Optional<Staff> findStaffByCode(String staffCode);

    /**
     * Lưu nhân viên (thêm mới hoặc cập nhật).
     *
     * @param staff entity cần lưu
     * @return entity đã persist với id và timestamps được điền
     */
    Staff saveStaff(Staff staff);

    // ─── Branch lookup (dùng khi chọn chi nhánh trong dialog) ────────────

    /**
     * Trả về danh sách chi nhánh đang hoạt động để điền ComboBox.
     */
    List<Branch> findActiveBranches();

    /**
     * Tìm chi nhánh theo id.
     */
    Optional<Branch> findBranchById(Long id);

    // ─── Account ─────────────────────────────────────────────────────────

    /**
     * Tìm danh sách tất cả tài khoản cùng thông tin nhân viên.
     *
     * @param keyword tìm theo username / mã nhân viên / tên nhân viên (null = tất cả)
     * @return danh sách Account có eager-fetch Staff
     */
    List<Account> findAccounts(String keyword);

    /**
     * Tìm tài khoản theo id.
     */
    Optional<Account> findAccountById(Long id);

    /**
     * Tìm tài khoản theo tên đăng nhập.
     */
    Optional<Account> findAccountByUsername(String username);

    /**
     * Lưu tài khoản (thêm mới hoặc cập nhật).
     *
     * @param account entity cần lưu
     * @return entity đã persist
     */
    Account saveAccount(Account account);

    /**
     * Xóa tài khoản theo id.
     */
    void deleteAccountById(Long id);
}
