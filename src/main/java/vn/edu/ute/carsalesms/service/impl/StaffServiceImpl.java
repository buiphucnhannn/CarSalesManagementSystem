package vn.edu.ute.carsalesms.service.impl;

import vn.edu.ute.carsalesms.dao.StaffDao;
import vn.edu.ute.carsalesms.model.dto.AccountCommandRequest;
import vn.edu.ute.carsalesms.model.dto.AccountItem;
import vn.edu.ute.carsalesms.model.dto.CarLookupItem;
import vn.edu.ute.carsalesms.model.dto.StaffCommandRequest;
import vn.edu.ute.carsalesms.model.dto.StaffItem;
import vn.edu.ute.carsalesms.model.dto.StaffManagementMetadata;
import vn.edu.ute.carsalesms.model.entity.Account;
import vn.edu.ute.carsalesms.model.entity.Branch;
import vn.edu.ute.carsalesms.model.entity.Staff;
import vn.edu.ute.carsalesms.model.enums.Status;
import vn.edu.ute.carsalesms.service.StaffService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;

/**
 * Triển khai StaffService.
 * Đảm nhiệm toàn bộ logic nghiệp vụ cho Staff và Account:
 *  - Validate input, kiểm tra trùng mã/username
 *  - Hash mật khẩu bằng SHA-256 trước khi lưu
 *  - Mapping entity ↔ DTO
 *  - Khóa/mở khóa tài khoản
 */
public class StaffServiceImpl implements StaffService {

    /** DAO được inject qua constructor (Dependency Inversion Principle). */
    private final StaffDao staffDao;

    public StaffServiceImpl(StaffDao staffDao) {
        this.staffDao = Objects.requireNonNull(staffDao, "staffDao is required");
    }

    // ─── Staff ───────────────────────────────────────────────────────────

    @Override
    public List<StaffItem> getStaffs(String keyword, Status statusFilter) {
        return staffDao.findStaffs(keyword, statusFilter).stream()
                .map(this::toStaffItem)
                .toList();
    }

    @Override
    public StaffManagementMetadata getMetadata() {
        // Build lookup list cho ComboBox chi nhánh
        List<CarLookupItem> branches = staffDao.findActiveBranches().stream()
                .map(b -> new CarLookupItem(b.getId(), b.getBranchCode(), b.getBranchName()))
                .toList();
        return new StaffManagementMetadata(branches);
    }

    @Override
    public StaffItem createStaff(StaffCommandRequest request) {
        StaffCommandRequest validated = validateStaff(request, false);

        // Kiểm tra trùng mã nhân viên
        staffDao.findStaffByCode(validated.staffCode()).ifPresent(existing -> {
            throw new IllegalArgumentException("Mã nhân viên đã tồn tại: " + validated.staffCode());
        });

        Branch branch = staffDao.findBranchById(validated.branchId())
                .orElseThrow(() -> new IllegalArgumentException("Chi nhánh không tồn tại."));

        Staff staff = new Staff();
        applyStaffData(staff, validated, branch);
        return toStaffItem(staffDao.saveStaff(staff));
    }

    @Override
    public StaffItem updateStaff(StaffCommandRequest request) {
        StaffCommandRequest validated = validateStaff(request, true);

        Staff staff = staffDao.findStaffById(validated.id())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên cần cập nhật."));

        // Kiểm tra trùng mã (trừ bản thân)
        staffDao.findStaffByCode(validated.staffCode())
                .filter(existing -> !existing.getId().equals(validated.id()))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Mã nhân viên đã tồn tại: " + validated.staffCode());
                });

        Branch branch = staffDao.findBranchById(validated.branchId())
                .orElseThrow(() -> new IllegalArgumentException("Chi nhánh không tồn tại."));

        applyStaffData(staff, validated, branch);
        return toStaffItem(staffDao.saveStaff(staff));
    }

    // ─── Account ─────────────────────────────────────────────────────────

    @Override
    public List<AccountItem> getAccounts(String keyword) {
        return staffDao.findAccounts(keyword).stream()
                .map(this::toAccountItem)
                .toList();
    }

    @Override
    public AccountItem createAccount(AccountCommandRequest request) {
        AccountCommandRequest validated = validateAccount(request, false);

        // Lấy nhân viên tương ứng
        Staff staff = staffDao.findStaffById(validated.staffId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên."));

        // Kiểm tra nhân viên đã có tài khoản chưa
        if (staff.getAccount() != null) {
            throw new IllegalArgumentException(
                    "Nhân viên \"" + staff.getFullName() + "\" đã có tài khoản đăng nhập.");
        }

        // Kiểm tra trùng username
        staffDao.findAccountByUsername(validated.username()).ifPresent(existing -> {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại: " + validated.username());
        });

        // Hash mật khẩu SHA-256
        String passwordHash = hashPassword(validated.rawPassword());

        Account account = new Account();
        account.setUsername(validated.username());
        account.setPasswordHash(passwordHash);
        account.setStaff(staff);
        account.setStatus(validated.status() == null ? Status.ACTIVE : validated.status());
        account.setLocked(false);
        account.setFailedLoginAttempts(0);

        return toAccountItem(staffDao.saveAccount(account));
    }

    @Override
    public AccountItem updateAccount(AccountCommandRequest request) {
        AccountCommandRequest validated = validateAccount(request, true);

        Account account = staffDao.findAccountById(validated.id())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản cần cập nhật."));

        // Kiểm tra trùng username (trừ bản thân)
        staffDao.findAccountByUsername(validated.username())
                .filter(existing -> !existing.getId().equals(validated.id()))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Tên đăng nhập đã tồn tại: " + validated.username());
                });

        account.setUsername(validated.username());

        // Chỉ cập nhật mật khẩu nếu admin nhập mật khẩu mới
        if (validated.rawPassword() != null && !validated.rawPassword().isBlank()) {
            account.setPasswordHash(hashPassword(validated.rawPassword()));
        }

        if (validated.status() != null) {
            account.setStatus(validated.status());
        }

        return toAccountItem(staffDao.saveAccount(account));
    }

    @Override
    public AccountItem toggleLock(Long accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException("Id tài khoản không hợp lệ.");
        }
        Account account = staffDao.findAccountById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));

        // Toggle trạng thái khóa
        boolean nowLocked = !account.isLocked();
        account.setLocked(nowLocked);

        // Mở khóa: reset số lần đăng nhập thất bại
        if (!nowLocked) {
            account.setFailedLoginAttempts(0);
        }

        return toAccountItem(staffDao.saveAccount(account));
    }

    @Override
    public void deleteAccount(Long accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException("Id tài khoản không hợp lệ.");
        }
        staffDao.findAccountById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản cần xóa."));
        staffDao.deleteAccountById(accountId);
    }

    // ─── Private helpers ──────────────────────────────────────────────────

    /**
     * Validate dữ liệu nhân viên đầu vào.
     */
    private StaffCommandRequest validateStaff(StaffCommandRequest request, boolean requireId) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu nhân viên không hợp lệ.");
        }
        if (requireId && request.id() == null) {
            throw new IllegalArgumentException("Thiếu mã định danh nhân viên.");
        }
        if (request.staffCode() == null || request.staffCode().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập mã nhân viên.");
        }
        if (request.fullName() == null || request.fullName().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập họ tên nhân viên.");
        }
        if (request.role() == null) {
            throw new IllegalArgumentException("Vui lòng chọn vai trò nhân viên.");
        }
        if (request.branchId() == null) {
            throw new IllegalArgumentException("Vui lòng chọn chi nhánh.");
        }
        Status normalizedStatus = request.status() == null ? Status.ACTIVE : request.status();
        return new StaffCommandRequest(
                request.id(),
                request.staffCode().trim().toUpperCase(),
                request.fullName().trim(),
                request.email() == null ? null : request.email().trim(),
                request.phone() == null ? null : request.phone().trim(),
                request.role(),
                request.branchId(),
                normalizedStatus
        );
    }

    /**
     * Validate dữ liệu tài khoản đầu vào.
     */
    private AccountCommandRequest validateAccount(AccountCommandRequest request, boolean requireId) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu tài khoản không hợp lệ.");
        }
        if (requireId && request.id() == null) {
            throw new IllegalArgumentException("Thiếu mã định danh tài khoản.");
        }
        if (!requireId && (request.staffId() == null)) {
            throw new IllegalArgumentException("Vui lòng chọn nhân viên cần tạo tài khoản.");
        }
        if (request.username() == null || request.username().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập tên đăng nhập.");
        }
        // Khi tạo mới bắt buộc phải có mật khẩu
        if (!requireId && (request.rawPassword() == null || request.rawPassword().isBlank())) {
            throw new IllegalArgumentException("Vui lòng nhập mật khẩu.");
        }
        return new AccountCommandRequest(
                request.id(),
                request.staffId(),
                request.username().trim().toLowerCase(),
                request.rawPassword(),
                request.status() == null ? Status.ACTIVE : request.status()
        );
    }

    /**
     * Điền dữ liệu từ request vào entity Staff.
     */
    private void applyStaffData(Staff staff, StaffCommandRequest request, Branch branch) {
        staff.setStaffCode(request.staffCode());
        staff.setFullName(request.fullName());
        staff.setEmail(request.email());
        staff.setPhone(request.phone());
        staff.setRole(request.role());
        staff.setBranch(branch);
        staff.setStatus(request.status());
    }

    /**
     * Hash mật khẩu dùng SHA-256 (đơn giản, nhất quán với AuthServiceImpl hiện có).
     * Trong production nên dùng BCrypt.
     *
     * @param rawPassword mật khẩu thô
     * @return chuỗi hex SHA-256
     */
    private String hashPassword(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 luôn có trong JRE, không xảy ra trong runtime
            throw new RuntimeException("Không thể hash mật khẩu.", e);
        }
    }

    /**
     * Mapping entity Staff → DTO StaffItem.
     * hasAccount = true nếu staff.getAccount() != null.
     */
    private StaffItem toStaffItem(Staff staff) {
        return new StaffItem(
                staff.getId(),
                staff.getStaffCode(),
                staff.getFullName(),
                staff.getEmail(),
                staff.getPhone(),
                staff.getRole(),
                staff.getBranch().getId(),
                staff.getBranch().getBranchName(),
                staff.getStatus(),
                staff.getAccount() != null,
                staff.getCreatedAt()
        );
    }

    /**
     * Mapping entity Account → DTO AccountItem.
     */
    private AccountItem toAccountItem(Account account) {
        return new AccountItem(
                account.getId(),
                account.getStaff().getId(),
                account.getStaff().getStaffCode(),
                account.getStaff().getFullName(),
                account.getUsername(),
                account.getStatus(),
                account.isLocked(),
                account.getFailedLoginAttempts(),
                account.getLastLoginAt(),
                account.getCreatedAt()
        );
    }
}
