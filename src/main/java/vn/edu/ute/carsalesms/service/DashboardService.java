package vn.edu.ute.carsalesms.service;

import vn.edu.ute.carsalesms.model.dto.AdminOverviewData;
import vn.edu.ute.carsalesms.model.dto.StaffOverviewData;

public interface DashboardService {

    StaffOverviewData getStaffOverview(Long staffId);

    AdminOverviewData getAdminOverview();
}

