package vn.edu.ute.carsalesms.service.impl;

import vn.edu.ute.carsalesms.dao.DashboardDao;
import vn.edu.ute.carsalesms.model.dto.AdminOverviewData;
import vn.edu.ute.carsalesms.model.dto.DashboardTaskItem;
import vn.edu.ute.carsalesms.model.dto.StaffOverviewData;
import vn.edu.ute.carsalesms.model.enums.OrderStatus;
import vn.edu.ute.carsalesms.model.enums.TestDriveStatus;
import vn.edu.ute.carsalesms.model.enums.WarrantyStatus;
import vn.edu.ute.carsalesms.service.DashboardService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class DashboardServiceImpl implements DashboardService {

    private static final int STAFF_TASK_LIMIT = 8;
    private static final int ADMIN_RECENT_ORDER_LIMIT = 8;

    private final DashboardDao dashboardDao;

    public DashboardServiceImpl(DashboardDao dashboardDao) {
        this.dashboardDao = Objects.requireNonNull(dashboardDao, "dashboardDao is required");
    }

    @Override
    public StaffOverviewData getStaffOverview(Long staffId) {
        if (staffId == null) {
            return StaffOverviewData.empty();
        }

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        List<DashboardTaskItem> taskItems = Stream.concat(
                        dashboardDao.findOrderTasksByStaff(staffId, STAFF_TASK_LIMIT).stream(),
                        dashboardDao.findUpcomingTestDriveTasksByStaff(staffId, startOfDay, endOfDay, STAFF_TASK_LIMIT).stream())
                .sorted(Comparator.comparing(DashboardTaskItem::dueAt, Comparator.nullsLast(LocalDateTime::compareTo)))
                .limit(STAFF_TASK_LIMIT)
                .toList();

        // Mở rộng vi mô thời gian tìm kiếm Lịch lái thử SCHEDULED
        // Để nó đếm TOÀN BỘ các Lịch báo cáo chưa chạy thay vì bị giới hạn chỉ vào startOfDay - endOfDay
        LocalDateTime farPast = startOfDay.minusYears(1);
        LocalDateTime farFuture = startOfDay.plusYears(5);

        return new StaffOverviewData(
                dashboardDao.countOrdersByStaffAndStatuses(staffId, List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED)),
                dashboardDao.sumCompletedPaymentsByStaffInRange(staffId, startOfDay, endOfDay),
                dashboardDao.countTestDrivesByStaffAndStatusInRange(staffId, TestDriveStatus.SCHEDULED, farPast, farFuture),
                dashboardDao.countWarrantiesByStaffAndStatus(staffId, WarrantyStatus.ACTIVE),
                taskItems
        );
    }

    @Override
    public AdminOverviewData getAdminOverview() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        YearMonth currentMonth = YearMonth.from(today);
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.plusMonths(1).atDay(1).atStartOfDay();

        // Mở rộng vi mô thời gian tìm kiếm test drive
        LocalDateTime farPast = startOfDay.minusYears(1);
        LocalDateTime farFuture = startOfDay.plusYears(5);

        return new AdminOverviewData(
                dashboardDao.sumCompletedPaymentsInRange(startOfMonth, endOfMonth),
                dashboardDao.countOrdersInRange(startOfDay, endOfDay),
                dashboardDao.countOrdersByStatuses(List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED)),
                dashboardDao.countTestDrivesByStatusInRange(TestDriveStatus.SCHEDULED, farPast, farFuture),
                dashboardDao.findRecentOrders(ADMIN_RECENT_ORDER_LIMIT)
        );
    }
}

