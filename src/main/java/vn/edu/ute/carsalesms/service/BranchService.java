package vn.edu.ute.carsalesms.service;

import vn.edu.ute.carsalesms.model.dto.BranchCommandRequest;
import vn.edu.ute.carsalesms.model.dto.BranchItem;
import vn.edu.ute.carsalesms.model.dto.BranchManagementMetadata;
import vn.edu.ute.carsalesms.model.dto.BranchSalesReportItem;
import vn.edu.ute.carsalesms.model.enums.Status;

import java.time.LocalDateTime;
import java.util.List;

public interface BranchService {

    List<BranchItem> getBranches(String keyword, Status statusFilter);

    BranchManagementMetadata getMetadata();

    BranchItem createBranch(BranchCommandRequest request);

    BranchItem updateBranch(BranchCommandRequest request);

    void deactivateBranch(Long branchId);

    List<BranchSalesReportItem> getBranchSalesReports(LocalDateTime fromInclusive,
                                                      LocalDateTime toExclusive,
                                                      Status statusFilter);
}

