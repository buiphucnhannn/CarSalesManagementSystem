package vn.edu.ute.carsalesms.controller;

import vn.edu.ute.carsalesms.model.dto.BranchCommandRequest;
import vn.edu.ute.carsalesms.model.dto.BranchItem;
import vn.edu.ute.carsalesms.model.dto.BranchManagementMetadata;
import vn.edu.ute.carsalesms.model.dto.BranchSalesReportItem;
import vn.edu.ute.carsalesms.model.enums.Status;
import vn.edu.ute.carsalesms.service.AuditLogService;
import vn.edu.ute.carsalesms.service.BranchService;
import vn.edu.ute.carsalesms.service.impl.NoOpAuditLogService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class BranchManagementController {

    private final BranchService branchService;
    private final AuditLogService auditLogService;

    public BranchManagementController(BranchService branchService) {
        this(branchService, new NoOpAuditLogService());
    }

    public BranchManagementController(BranchService branchService, AuditLogService auditLogService) {
        this.branchService = Objects.requireNonNull(branchService, "branchService is required");
        this.auditLogService = Objects.requireNonNull(auditLogService, "auditLogService is required");
    }

    public List<BranchItem> loadBranches(String keyword, Status statusFilter) {
        return branchService.getBranches(keyword, statusFilter);
    }

    public BranchManagementMetadata loadMetadata() {
        return branchService.getMetadata();
    }

    public BranchItem createBranch(BranchCommandRequest request) {
        BranchItem created = branchService.createBranch(request);
        auditLogService.log("CREATE", "BRANCH", created.id(), null, request.toString());
        return created;
    }

    public BranchItem updateBranch(BranchCommandRequest request) {
        BranchItem updated = branchService.updateBranch(request);
        auditLogService.log("UPDATE", "BRANCH", updated.id(), null, request.toString());
        return updated;
    }

    public void deactivateBranch(Long branchId) {
        branchService.deactivateBranch(branchId);
        auditLogService.log("DEACTIVATE", "BRANCH", branchId, null, "status=INACTIVE");
    }

    public List<BranchSalesReportItem> loadBranchSalesReports(LocalDateTime fromInclusive,
                                                              LocalDateTime toExclusive,
                                                              Status statusFilter) {
        return branchService.getBranchSalesReports(fromInclusive, toExclusive, statusFilter);
    }
}

