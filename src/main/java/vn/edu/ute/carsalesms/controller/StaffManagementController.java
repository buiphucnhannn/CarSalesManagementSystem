package vn.edu.ute.carsalesms.controller;

import vn.edu.ute.carsalesms.model.dto.AccountCommandRequest;
import vn.edu.ute.carsalesms.model.dto.AccountItem;
import vn.edu.ute.carsalesms.model.dto.StaffCommandRequest;
import vn.edu.ute.carsalesms.model.dto.StaffItem;
import vn.edu.ute.carsalesms.model.dto.StaffManagementMetadata;
import vn.edu.ute.carsalesms.model.enums.Status;
import vn.edu.ute.carsalesms.service.StaffService;

import java.util.List;
import java.util.Objects;

/**
 * Controller (MVC) cho module Quản lý Nhân viên và Tài khoản.
 * Là lớp trung gian mỏng giữa View và Service.
 * Không chứa logic nghiệp vụ – tuân thủ Single Responsibility Principle.
 */
public class StaffManagementController {

    /** Service được inject qua constructor. */
    private final StaffService staffService;

    public StaffManagementController(StaffService staffService) {
        this.staffService = Objects.requireNonNull(staffService, "staffService is required");
    }

    // ─── Staff ───────────────────────────────────────────────────────────

    /**
     * Tải danh sách nhân viên theo từ khóa và trạng thái.
     *
     * @param keyword      từ khóa (null = tất cả)
     * @param statusFilter trạng thái lọc (null = tất cả)
     * @return danh sách StaffItem
     */
    public List<StaffItem> loadStaffs(String keyword, Status statusFilter) {
        return staffService.getStaffs(keyword, statusFilter);
    }

    /**
     * Tải danh sách nhân viên ACTIVE chưa có tài khoản.
     */
    public List<StaffItem> loadStaffsPendingAccount() {
        return staffService.getActiveStaffsWithoutAccount();
    }

    /**
     * Tải metadata cần thiết cho dialog (danh sách chi nhánh).
     */
    public StaffManagementMetadata loadMetadata() {
        return staffService.getMetadata();
    }

    /**
     * Thêm mới nhân viên.
     */
    public StaffItem createStaff(StaffCommandRequest request) {
        return staffService.createStaff(request);
    }

    /**
     * Cập nhật thông tin nhân viên.
     */
    public StaffItem updateStaff(StaffCommandRequest request) {
        return staffService.updateStaff(request);
    }

    // ─── Account ─────────────────────────────────────────────────────────

    /**
     * Tải danh sách tài khoản theo từ khóa.
     *
     * @param keyword từ khóa (null = tất cả)
     * @return danh sách AccountItem
     */
    public List<AccountItem> loadAccounts(String keyword) {
        return staffService.getAccounts(keyword);
    }

    /**
     * Tạo tài khoản mới cho một nhân viên.
     */
    public AccountItem createAccount(AccountCommandRequest request) {
        return staffService.createAccount(request);
    }

    /**
     * Cập nhật tài khoản (username / mật khẩu / trạng thái).
     */
    public AccountItem updateAccount(AccountCommandRequest request) {
        return staffService.updateAccount(request);
    }

    /**
     * Khóa hoặc mở khóa tài khoản (toggle).
     *
     * @param accountId id tài khoản
     * @return AccountItem đã cập nhật trạng thái khóa
     */
    public AccountItem toggleLock(Long accountId) {
        return staffService.toggleLock(accountId);
    }

    /**
     * Xóa tài khoản khỏi hệ thống.
     *
     * @param accountId id tài khoản cần xóa
     */
    public void deleteAccount(Long accountId) {
        staffService.deleteAccount(accountId);
    }
}
