package vn.edu.ute.carsalesms.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import vn.edu.ute.carsalesms.dao.StaffDao;
import vn.edu.ute.carsalesms.model.dto.CarLookupItem;
import vn.edu.ute.carsalesms.model.dto.StaffCommandRequest;
import vn.edu.ute.carsalesms.model.dto.StaffItem;
import vn.edu.ute.carsalesms.model.dto.StaffManagementMetadata;
import vn.edu.ute.carsalesms.model.entity.Branch;
import vn.edu.ute.carsalesms.model.entity.Staff;
import vn.edu.ute.carsalesms.model.enums.Status;
import vn.edu.ute.carsalesms.service.StaffProfileService;
import vn.edu.ute.carsalesms.util.CodeGeneratorUtil;

public class StaffProfileServiceImpl implements StaffProfileService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{9,11}$");

    private final StaffDao staffDao;

    public StaffProfileServiceImpl(StaffDao staffDao) {
        this.staffDao = Objects.requireNonNull(staffDao, "staffDao is required");
    }

    @Override
    public List<StaffItem> getStaffs(String keyword, Status statusFilter) {
        return staffDao.findStaffs(keyword, statusFilter).stream().map(this::toStaffItem).toList();
    }

    @Override
    public List<StaffItem> getActiveStaffsWithoutAccount() {
        return staffDao.findActiveStaffsWithoutAccount().stream().map(this::toStaffItem).toList();
    }

    @Override
    public StaffManagementMetadata getMetadata() {
        List<CarLookupItem> branches = staffDao.findActiveBranches().stream()
                .map(b -> new CarLookupItem(b.getId(), b.getBranchCode(), b.getBranchName()))
                .toList();
        return new StaffManagementMetadata(branches, nextStaffCode());
    }

    @Override
    public StaffItem createStaff(StaffCommandRequest request) {
        StaffCommandRequest validated = validateStaff(request, false);

        String generatedCode = nextStaffCode();
        StaffCommandRequest createRequest = new StaffCommandRequest(
                validated.id(),
                generatedCode,
                validated.fullName(),
                validated.email(),
                validated.phone(),
                validated.role(),
                validated.branchId(),
                validated.status()
        );

        staffDao.findStaffByCode(createRequest.staffCode()).ifPresent(existing -> {
            throw new IllegalArgumentException("Mã nhân viên đã tồn tại: " + createRequest.staffCode());
        });

        Branch branch = staffDao.findBranchById(createRequest.branchId())
                .orElseThrow(() -> new IllegalArgumentException("Chi nhánh không tồn tại."));

        Staff staff = new Staff();
        applyStaffData(staff, createRequest, branch);
        return toStaffItem(staffDao.saveStaff(staff));
    }

    @Override
    public StaffItem updateStaff(StaffCommandRequest request) {
        StaffCommandRequest validated = validateStaff(request, true);

        Staff staff = staffDao.findStaffById(validated.id())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên cần cập nhật."));

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

    private StaffCommandRequest validateStaff(StaffCommandRequest request, boolean requireId) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu nhân viên không hợp lệ.");
        }
        if (requireId && request.id() == null) {
            throw new IllegalArgumentException("Thiếu mã định danh nhân viên.");
        }
        if (requireId && (request.staffCode() == null || request.staffCode().isBlank())) {
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
        String normalizedEmail = request.email() == null ? null : request.email().trim();
        if (normalizedEmail != null && !normalizedEmail.isBlank() && !EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new IllegalArgumentException("Email không hợp lệ (ví dụ: name@example.com).");
        }

        String normalizedPhone = request.phone() == null ? null : request.phone().trim();
        if (normalizedPhone != null && !normalizedPhone.isBlank() && !PHONE_PATTERN.matcher(normalizedPhone).matches()) {
            throw new IllegalArgumentException("Số điện thoại phải gồm 9-11 chữ số.");
        }

        String normalizedStaffCode = request.staffCode() == null ? null : request.staffCode().trim().toUpperCase();
        Status normalizedStatus = request.status() == null ? Status.ACTIVE : request.status();
        return new StaffCommandRequest(
                request.id(),
                normalizedStaffCode,
                request.fullName().trim(),
                normalizedEmail == null || normalizedEmail.isEmpty() ? null : normalizedEmail,
                normalizedPhone == null || normalizedPhone.isEmpty() ? null : normalizedPhone,
                request.role(),
                request.branchId(),
                normalizedStatus
        );
    }

    private String nextStaffCode() {
        List<String> existingCodes = staffDao.findStaffs(null, null).stream().map(Staff::getStaffCode).toList();
        return CodeGeneratorUtil.nextCodeFromExisting(existingCodes, "ST-STAFF-", 2);
    }

    private void applyStaffData(Staff staff, StaffCommandRequest request, Branch branch) {
        staff.setStaffCode(request.staffCode());
        staff.setFullName(request.fullName());
        staff.setEmail(request.email());
        staff.setPhone(request.phone());
        staff.setRole(request.role());
        staff.setBranch(branch);
        staff.setStatus(request.status());
    }

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
}

