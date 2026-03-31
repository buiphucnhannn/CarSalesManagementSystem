package vn.edu.ute.carsalesms.controller;

import vn.edu.ute.carsalesms.model.dto.BranchCommandRequest;
import vn.edu.ute.carsalesms.model.dto.BranchItem;
import vn.edu.ute.carsalesms.model.dto.BranchManagementMetadata;
import vn.edu.ute.carsalesms.model.dto.BranchSalesReportItem;
import vn.edu.ute.carsalesms.model.enums.Status;
import vn.edu.ute.carsalesms.service.BranchService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class BranchManagementController {

    private final BranchService branchService;

    public BranchManagementController(BranchService branchService) {
        this.branchService = Objects.requireNonNull(branchService, "branchService is required");
    }

    public List<BranchItem> loadBranches(String keyword, Status statusFilter) {
        return branchService.getBranches(keyword, statusFilter);
    }

    public BranchManagementMetadata loadMetadata() {
        return branchService.getMetadata();
    }

    public BranchItem createBranch(BranchCommandRequest request) {
        return branchService.createBranch(request);
    }

    public BranchItem updateBranch(BranchCommandRequest request) {
        return branchService.updateBranch(request);
    }

    public void deactivateBranch(Long branchId) {
        branchService.deactivateBranch(branchId);
    }

    public List<BranchSalesReportItem> loadBranchSalesReports(LocalDateTime fromInclusive,
                                                              LocalDateTime toExclusive,
                                                              Status statusFilter) {
        return branchService.getBranchSalesReports(fromInclusive, toExclusive, statusFilter);
    }
}

