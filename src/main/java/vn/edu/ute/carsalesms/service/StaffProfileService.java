package vn.edu.ute.carsalesms.service;

import java.util.List;
import vn.edu.ute.carsalesms.model.dto.StaffCommandRequest;
import vn.edu.ute.carsalesms.model.dto.StaffItem;
import vn.edu.ute.carsalesms.model.dto.StaffManagementMetadata;
import vn.edu.ute.carsalesms.model.enums.Status;

/**
 * Nhom use-case quan ly ho so nhan vien.
 */
public interface StaffProfileService {

    List<StaffItem> getStaffs(String keyword, Status statusFilter);

    List<StaffItem> getActiveStaffsWithoutAccount();

    StaffManagementMetadata getMetadata();

    StaffItem createStaff(StaffCommandRequest request);

    StaffItem updateStaff(StaffCommandRequest request);
}

