package vn.edu.ute.carsalesms.controller;

import vn.edu.ute.carsalesms.model.dto.BranchCommandRequest;
import vn.edu.ute.carsalesms.model.dto.BranchItem;
import vn.edu.ute.carsalesms.model.dto.BranchManagementMetadata;
import vn.edu.ute.carsalesms.model.dto.BranchSalesReportItem;
import vn.edu.ute.carsalesms.model.enums.Status;
import vn.edu.ute.carsalesms.service.AuditLogService;
import vn.edu.ute.carsalesms.service.BranchService;
import vn.edu.ute.carsalesms.service.NoOpAuditLogService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * BranchManagementController xử lý các yêu cầu liên quan đến quản lý chi nhánh.
 * Nó tuân theo Nguyên tắc Trách nhiệm Đơn lẻ (SRP) bằng cách chỉ tập trung vào logic quản lý chi nhánh.
 * Nó cũng tuân theo Nguyên tắc Đảo ngược Phụ thuộc (DIP) bằng cách phụ thuộc vào các giao diện
 * (BranchService, AuditLogService) thay vì các triển khai cụ thể.
 */
public class BranchManagementController {

    private final BranchService branchService;
    private final AuditLogService auditLogService;

    /**
     * Xây dựng một BranchManagementController mới với BranchService đã cho.
     * @param branchService dịch vụ sẽ được sử dụng để quản lý chi nhánh.
     */
    public BranchManagementController(BranchService branchService) {
        this(branchService, new NoOpAuditLogService());
    }

    /**
     * Xây dựng một BranchManagementController mới với BranchService và AuditLogService đã cho.
     * @param branchService dịch vụ sẽ được sử dụng để quản lý chi nhánh.
     * @param auditLogService dịch vụ sẽ được sử dụng để ghi lại các hành động.
     */
    public BranchManagementController(BranchService branchService, AuditLogService auditLogService) {
        this.branchService = Objects.requireNonNull(branchService, "branchService is required");
        this.auditLogService = Objects.requireNonNull(auditLogService, "auditLogService is required");
    }

    /**
     * Tải tất cả các chi nhánh khớp với từ khóa và bộ lọc trạng thái đã cho.
     * @param keyword từ khóa để tìm kiếm.
     * @param statusFilter bộ lọc trạng thái.
     * @return danh sách các mục chi nhánh.
     */
    public List<BranchItem> loadBranches(String keyword, Status statusFilter) {
        return branchService.getBranches(keyword, statusFilter);
    }

    /**
     * Tải siêu dữ liệu cho quản lý chi nhánh.
     * @return siêu dữ liệu quản lý chi nhánh.
     */
    public BranchManagementMetadata loadMetadata() {
        return branchService.getMetadata();
    }

    /**
     * Tạo một chi nhánh mới.
     * @param request yêu cầu tạo chi nhánh.
     * @return mục chi nhánh đã tạo.
     */
    public BranchItem createBranch(BranchCommandRequest request) {
        BranchItem created = branchService.createBranch(request);
        auditLogService.log("CREATE", "BRANCH", created.id(), null, request.toString());
        return created;
    }

    /**
     * Cập nhật một chi nhánh hiện có.
     * @param request yêu cầu cập nhật chi nhánh.
     * @return mục chi nhánh đã cập nhật.
     */
    public BranchItem updateBranch(BranchCommandRequest request) {
        BranchItem updated = branchService.updateBranch(request);
        auditLogService.log("UPDATE", "BRANCH", updated.id(), null, request.toString());
        return updated;
    }

    /**
     * Hủy kích hoạt một chi nhánh.
     * @param branchId ID của chi nhánh cần hủy kích hoạt.
     */
    public void deactivateBranch(Long branchId) {
        branchService.deactivateBranch(branchId);
        auditLogService.log("DEACTIVATE", "BRANCH", branchId, null, "status=INACTIVE");
    }

    /**
     * Tải báo cáo bán hàng của chi nhánh.
     * @param fromInclusive ngày bắt đầu.
     * @param toExclusive ngày kết thúc.
     * @param statusFilter bộ lọc trạng thái.
     * @return danh sách các mục báo cáo bán hàng của chi nhánh.
     */
    public List<BranchSalesReportItem> loadBranchSalesReports(LocalDateTime fromInclusive,
                                                              LocalDateTime toExclusive,
                                                              Status statusFilter) {
        return branchService.getBranchSalesReports(fromInclusive, toExclusive, statusFilter);
    }
}
