package vn.edu.ute.carsalesms.dao;

import vn.edu.ute.carsalesms.model.dto.AdminRecentOrderItem;
import vn.edu.ute.carsalesms.model.dto.DashboardTaskItem;
import vn.edu.ute.carsalesms.model.enums.OrderStatus;
import vn.edu.ute.carsalesms.model.enums.TestDriveStatus;
import vn.edu.ute.carsalesms.model.enums.WarrantyStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface DashboardDao {

    long countOrdersByStaffAndStatuses(Long staffId, List<OrderStatus> statuses);

    BigDecimal sumCompletedPaymentsByStaffInRange(Long staffId, LocalDateTime from, LocalDateTime to);

    long countTestDrivesByStaffAndStatusInRange(Long staffId, TestDriveStatus status, LocalDateTime from, LocalDateTime to);

    long countWarrantiesByStaffAndStatus(Long staffId, WarrantyStatus status);

    List<DashboardTaskItem> findOrderTasksByStaff(Long staffId, int limit);

    List<DashboardTaskItem> findUpcomingTestDriveTasksByStaff(Long staffId, LocalDateTime from, LocalDateTime to, int limit);

    BigDecimal sumCompletedPaymentsInRange(LocalDateTime from, LocalDateTime to);

    long countOrdersInRange(LocalDateTime from, LocalDateTime to);

    long countOrdersByStatuses(List<OrderStatus> statuses);

    long countTestDrivesByStatusInRange(TestDriveStatus status, LocalDateTime from, LocalDateTime to);

    List<AdminRecentOrderItem> findRecentOrders(int limit);
}

