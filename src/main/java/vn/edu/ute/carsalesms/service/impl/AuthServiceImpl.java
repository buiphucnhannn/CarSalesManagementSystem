package vn.edu.ute.carsalesms.service.impl;

import vn.edu.ute.carsalesms.dao.AccountDao;
import vn.edu.ute.carsalesms.model.dto.AuthenticatedUser;
import vn.edu.ute.carsalesms.model.entity.Account;
import vn.edu.ute.carsalesms.model.entity.Staff;
import vn.edu.ute.carsalesms.model.enums.StaffRole;
import vn.edu.ute.carsalesms.model.enums.Status;
import vn.edu.ute.carsalesms.service.AuditLogService;
import vn.edu.ute.carsalesms.service.AuthService;
import vn.edu.ute.carsalesms.session.CurrentSession;
import vn.edu.ute.carsalesms.util.PasswordUtil;

import java.time.LocalDateTime;

public class AuthServiceImpl implements AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;

    private final AccountDao accountDao;
    private final AuditLogService auditLogService;

    public AuthServiceImpl(AccountDao accountDao) {
        this(accountDao, new NoOpAuditLogService());
    }

    public AuthServiceImpl(AccountDao accountDao, AuditLogService auditLogService) {
        this.accountDao = accountDao;
        this.auditLogService = auditLogService;
    }

    @Override
    public AuthenticatedUser login(String username, String rawPassword) {
        String normalizedUsername = username == null ? "" : username.trim().toLowerCase();
        if (normalizedUsername.isEmpty() || rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.");
        }

        Account account = accountDao.findByUsername(normalizedUsername)
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản hoặc mật khẩu không đúng."));

        validateAccountState(account);

        if (!PasswordUtil.matches(rawPassword, account.getPasswordHash())) {
            handleFailedLogin(account);
            if (account.getStaff() != null) {
                auditLogService.logByStaffId(
                        account.getStaff().getId(),
                        "LOGIN_FAILED",
                        "AUTH",
                        account.getId(),
                        null,
                        "Sai mat khau. Username=" + normalizedUsername
                );
            }
            throw new IllegalArgumentException("Tài khoản hoặc mật khẩu không đúng.");
        }

        Staff staff = account.getStaff();
        if (staff == null || staff.getRole() == null) {
            throw new IllegalStateException("Tài khoản chưa được gán nhân viên hoặc vai trò.");
        }

        account.setFailedLoginAttempts(0);
        account.setLastLoginAt(LocalDateTime.now());
        Account savedAccount = accountDao.save(account);

        AuthenticatedUser user = toAuthenticatedUser(savedAccount);
        CurrentSession.setCurrentUser(user);
        auditLogService.logByStaffId(
                user.staffId(),
                "LOGIN_SUCCESS",
                "AUTH",
                account.getId(),
                null,
                "Dang nhap thanh cong. Username=" + user.username() + ", Role=" + user.role()
        );
        return user;
    }

    private void validateAccountState(Account account) {
        if (account.getStatus() != Status.ACTIVE) {
            throw new IllegalStateException("Tài khoản đang tạm ngưng hoạt động.");
        }
        if (account.isLocked()) {
            throw new IllegalStateException("Tài khoản đã bị khóa do đăng nhập sai nhiều lần.");
        }
        if (account.getStaff() == null || account.getStaff().getStatus() != Status.ACTIVE) {
            throw new IllegalStateException("Nhân viên đã ngừng hoạt động, không thể đăng nhập.");
        }
    }

    private void handleFailedLogin(Account account) {
        int failedAttempts = account.getFailedLoginAttempts() == null ? 0 : account.getFailedLoginAttempts();
        failedAttempts++;
        account.setFailedLoginAttempts(failedAttempts);
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            account.setLocked(true);
        }
        accountDao.save(account);
    }

    private AuthenticatedUser toAuthenticatedUser(Account account) {
        Staff staff = account.getStaff();
        String branchName = staff.getBranch() == null ? "N/A" : staff.getBranch().getBranchName();
        Long branchId = staff.getBranch() == null ? null : staff.getBranch().getId();
        StaffRole role = staff.getRole();
        return new AuthenticatedUser(
                account.getId(),
                staff.getId(),
                staff.getStaffCode(),
                staff.getFullName(),
                account.getUsername(),
                role,
                branchName,
                branchId
        );
    }
}


