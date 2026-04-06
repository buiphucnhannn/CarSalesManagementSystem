package vn.edu.ute.carsalesms.dao;

import vn.edu.ute.carsalesms.model.dto.AdminRecentOrderItem;
import vn.edu.ute.carsalesms.model.dto.DashboardTaskItem;
import vn.edu.ute.carsalesms.model.enums.OrderStatus;
import vn.edu.ute.carsalesms.model.enums.TestDriveStatus;
import vn.edu.ute.carsalesms.model.enums.WarrantyStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Giao diện DAO (Data Access Object) dành riêng cho việc truy vấn dữ liệu tổng hợp
 * để hiển thị trên các bảng điều khiển (Dashboard) của quản trị viên và nhân viên.
 */
public interface DashboardDao {

    // --- Dành cho Dashboard của Nhân viên ---

    /**
     * Đếm số lượng đơn hàng của một nhân viên cụ thể dựa trên danh sách các trạng thái.
     * @param staffId ID của nhân viên.
     * @param statuses Danh sách các trạng thái đơn hàng cần đếm (ví dụ: PENDING, PROCESSING).
     * @return Tổng số lượng đơn hàng.
     */
    long countOrdersByStaffAndStatuses(Long staffId, List<OrderStatus> statuses);

    /**
     * Tính tổng số tiền từ các thanh toán đã hoàn thành (COMPLETED) của một nhân viên trong một khoảng thời gian.
     * @param staffId ID của nhân viên.
     * @param from Thời điểm bắt đầu.
     * @param to Thời điểm kết thúc.
     * @return Tổng số tiền đã thanh toán.
     */
    BigDecimal sumCompletedPaymentsByStaffInRange(Long staffId, LocalDateTime from, LocalDateTime to);

    /**
     * Đếm số lượng lịch lái thử của một nhân viên với một trạng thái cụ thể trong một khoảng thời gian.
     * @param staffId ID của nhân viên.
     * @param status Trạng thái của lịch lái thử (ví dụ: SCHEDULED).
     * @param from Thời điểm bắt đầu.
     * @param to Thời điểm kết thúc.
     * @return Tổng số lượng lịch lái thử.
     */
    long countTestDrivesByStaffAndStatusInRange(Long staffId, TestDriveStatus status, LocalDateTime from, LocalDateTime to);

    /**
     * Đếm số lượng phiếu bảo hành liên quan đến một nhân viên với một trạng thái cụ thể.
     * @param staffId ID của nhân viên.
     * @param status Trạng thái của phiếu bảo hành (ví dụ: ACTIVE).
     * @return Tổng số lượng phiếu bảo hành.
     */
    long countWarrantiesByStaffAndStatus(Long staffId, WarrantyStatus status);

    /**
     * Tìm các nhiệm vụ liên quan đến đơn hàng cho một nhân viên (ví dụ: các đơn hàng cần xử lý).
     * @param staffId ID của nhân viên.
     * @param limit Giới hạn số lượng nhiệm vụ trả về.
     * @return Danh sách các nhiệm vụ.
     */
    List<DashboardTaskItem> findOrderTasksByStaff(Long staffId, int limit);

    /**
     * Tìm các lịch lái thử sắp diễn ra của một nhân viên.
     * @param staffId ID của nhân viên.
     * @param from Thời điểm bắt đầu tìm kiếm.
     * @param to Thời điểm kết thúc tìm kiếm.
     * @param limit Giới hạn số lượng lịch hẹn trả về.
     * @return Danh sách các nhiệm vụ lái thử.
     */
    List<DashboardTaskItem> findUpcomingTestDriveTasksByStaff(Long staffId, LocalDateTime from, LocalDateTime to, int limit);

    // --- Dành cho Dashboard của Quản trị viên (Admin) ---

    /**
     * Tính tổng doanh thu từ các thanh toán đã hoàn thành trong một khoảng thời gian trên toàn hệ thống.
     * @param from Thời điểm bắt đầu.
     * @param to Thời điểm kết thúc.
     * @return Tổng doanh thu.
     */
    BigDecimal sumCompletedPaymentsInRange(LocalDateTime from, LocalDateTime to);

    /**
     * Đếm tổng số lượng đơn hàng được tạo trong một khoảng thời gian trên toàn hệ thống.
     * @param from Thời điểm bắt đầu.
     * @param to Thời điểm kết thúc.
     * @return Tổng số lượng đơn hàng.
     */
    long countOrdersInRange(LocalDateTime from, LocalDateTime to);

    /**
     * Đếm tổng số lượng đơn hàng theo danh sách các trạng thái trên toàn hệ thống.
     * @param statuses Danh sách các trạng thái cần đếm.
     * @return Tổng số lượng đơn hàng.
     */
    long countOrdersByStatuses(List<OrderStatus> statuses);

    /**
     * Đếm tổng số lượng lịch lái thử theo trạng thái trong một khoảng thời gian trên toàn hệ thống.
     * @param status Trạng thái cần đếm.
     * @param from Thời điểm bắt đầu.
     * @param to Thời điểm kết thúc.
     * @return Tổng số lượng lịch lái thử.
     */
    long countTestDrivesByStatusInRange(TestDriveStatus status, LocalDateTime from, LocalDateTime to);

    /**
     * Lấy danh sách các đơn hàng được tạo gần đây nhất trên toàn hệ thống để hiển thị cho quản trị viên.
     * @param limit Giới hạn số lượng đơn hàng trả về.
     * @return Danh sách các đơn hàng gần đây.
     */
    List<AdminRecentOrderItem> findRecentOrders(int limit);
}
