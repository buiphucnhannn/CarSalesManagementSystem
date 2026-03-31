package vn.edu.ute.carsalesms.service.impl;

import vn.edu.ute.carsalesms.dao.BranchDao;
import vn.edu.ute.carsalesms.model.dto.BranchCommandRequest;
import vn.edu.ute.carsalesms.model.dto.BranchItem;
import vn.edu.ute.carsalesms.model.dto.BranchManagementMetadata;
import vn.edu.ute.carsalesms.model.dto.BranchSalesReportItem;
import vn.edu.ute.carsalesms.model.entity.Branch;
import vn.edu.ute.carsalesms.model.enums.Status;
import vn.edu.ute.carsalesms.service.BranchService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class BranchServiceImpl implements BranchService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{9,11}$");

    private final BranchDao branchDao;

    public BranchServiceImpl(BranchDao branchDao) {
        this.branchDao = Objects.requireNonNull(branchDao, "branchDao is required");
    }

    @Override
    public List<BranchItem> getBranches(String keyword, Status statusFilter) {
        return branchDao.findBranches(keyword, statusFilter).stream()
                .map(this::toItem)
                .toList();
    }

    @Override
    public BranchManagementMetadata getMetadata() {
        return new BranchManagementMetadata("");
    }

    @Override
    public BranchItem createBranch(BranchCommandRequest request) {
        BranchCommandRequest validated = validate(request, false);
        branchDao.findByCode(validated.branchCode()).ifPresent(existing -> {
            throw new IllegalArgumentException("Mã chi nhánh đã tồn tại: " + validated.branchCode());
        });

        Branch branch = new Branch();
        applyData(branch, validated);
        return toItem(branchDao.save(branch));
    }

    @Override
    public BranchItem updateBranch(BranchCommandRequest request) {
        BranchCommandRequest validated = validate(request, true);
        Branch branch = branchDao.findById(validated.id())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi nhánh cần cập nhật."));

        branchDao.findByCode(validated.branchCode())
                .filter(existing -> !existing.getId().equals(validated.id()))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Mã chi nhánh đã tồn tại: " + validated.branchCode());
                });

        applyData(branch, validated);
        return toItem(branchDao.save(branch));
    }

    @Override
    public void deactivateBranch(Long branchId) {
        if (branchId == null) {
            throw new IllegalArgumentException("Chi nhánh không hợp lệ.");
        }
        Branch branch = branchDao.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi nhánh cần ngừng hoạt động."));

        long activeStaffCount = branchDao.countActiveStaffByBranchId(branchId);
        if (activeStaffCount > 0) {
            throw new IllegalStateException("Không thể ngừng chi nhánh khi còn " + activeStaffCount + " nhân viên ACTIVE.");
        }

        long activeCarCount = branchDao.countActiveCarsByBranchId(branchId);
        if (activeCarCount > 0) {
            throw new IllegalStateException("Không thể ngừng chi nhánh khi còn " + activeCarCount + " xe ACTIVE.");
        }

        branch.setStatus(Status.INACTIVE);
        branchDao.save(branch);
    }

    @Override
    public List<BranchSalesReportItem> getBranchSalesReports(LocalDateTime fromInclusive,
                                                             LocalDateTime toExclusive,
                                                             Status statusFilter) {
        if (fromInclusive == null || toExclusive == null || !toExclusive.isAfter(fromInclusive)) {
            throw new IllegalArgumentException("Khoảng thời gian báo cáo không hợp lệ.");
        }

        return branchDao.findBranchSalesReportRows(fromInclusive, toExclusive, statusFilter).stream()
                .map(row -> new BranchSalesReportItem(
                        (Long) row[0],
                        (String) row[1],
                        (String) row[2],
                        (Status) row[3],
                        toLong(row[4]),
                        toLong(row[5]),
                        toLong(row[6]),
                        toLong(row[7]),
                        (BigDecimal) row[8],
                        (LocalDateTime) row[9]
                ))
                .toList();
    }

    private BranchCommandRequest validate(BranchCommandRequest request, boolean requireId) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu chi nhánh không hợp lệ.");
        }
        if (requireId && request.id() == null) {
            throw new IllegalArgumentException("Thiếu mã định danh chi nhánh.");
        }
        if (request.branchCode() == null || request.branchCode().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập mã chi nhánh.");
        }
        if (request.branchName() == null || request.branchName().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập tên chi nhánh.");
        }

        String normalizedEmail = request.email() == null ? null : request.email().trim();
        if (normalizedEmail != null && !normalizedEmail.isBlank() && !EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new IllegalArgumentException("Email chi nhánh không hợp lệ.");
        }

        String normalizedPhone = request.phone() == null ? null : request.phone().trim();
        if (normalizedPhone != null && !normalizedPhone.isBlank() && !PHONE_PATTERN.matcher(normalizedPhone).matches()) {
            throw new IllegalArgumentException("Số điện thoại chi nhánh phải gồm 9-11 chữ số.");
        }

        Status normalizedStatus = request.status() == null ? Status.ACTIVE : request.status();
        return new BranchCommandRequest(
                request.id(),
                request.branchCode() == null ? null : request.branchCode().trim().toUpperCase(),
                request.branchName().trim(),
                request.address() == null || request.address().isBlank() ? null : request.address().trim(),
                normalizedPhone == null || normalizedPhone.isEmpty() ? null : normalizedPhone,
                normalizedEmail == null || normalizedEmail.isEmpty() ? null : normalizedEmail,
                normalizedStatus
        );
    }

    private void applyData(Branch branch, BranchCommandRequest request) {
        branch.setBranchCode(request.branchCode());
        branch.setBranchName(request.branchName());
        branch.setAddress(request.address());
        branch.setPhone(request.phone());
        branch.setEmail(request.email());
        branch.setStatus(request.status());
    }

    private BranchItem toItem(Branch branch) {
        return new BranchItem(
                branch.getId(),
                branch.getBranchCode(),
                branch.getBranchName(),
                branch.getAddress(),
                branch.getPhone(),
                branch.getEmail(),
                branch.getStatus(),
                branch.getCreatedAt()
        );
    }


    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return 0L;
    }
}

