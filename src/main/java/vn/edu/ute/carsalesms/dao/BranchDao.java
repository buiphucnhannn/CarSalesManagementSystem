package vn.edu.ute.carsalesms.dao;

import vn.edu.ute.carsalesms.model.entity.Branch;
import vn.edu.ute.carsalesms.model.enums.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BranchDao {

    List<Branch> findBranches(String keyword, Status statusFilter);

    Optional<Branch> findById(Long id);

    Optional<Branch> findByCode(String branchCode);

    Branch save(Branch branch);

    long countActiveStaffByBranchId(Long branchId);

    long countActiveCarsByBranchId(Long branchId);

    List<Object[]> findBranchSalesReportRows(LocalDateTime fromInclusive, LocalDateTime toExclusive, Status statusFilter);
}

