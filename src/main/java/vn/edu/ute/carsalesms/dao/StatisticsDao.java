package vn.edu.ute.carsalesms.dao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Giao diện DAO (Data Access Object) dành riêng cho việc truy vấn các dữ liệu thống kê phức tạp.
 * Các phương thức trong đây được thiết kế để tổng hợp thông tin từ nhiều bảng
 * nhằm phục vụ cho các báo cáo và biểu đồ.
 */
public interface StatisticsDao {

    /**
     * Tính tổng doanh thu từ các đơn hàng trong một khoảng thời gian.
     * Có thể lọc theo nhân viên hoặc tính trên toàn hệ thống.
     * @param fromInclusive Thời điểm bắt đầu (bao gồm).
     * @param toExclusive Thời điểm kết thúc (không bao gồm).
     * @param staffId ID của nhân viên để lọc. Nếu null, tính trên toàn hệ thống.
     * @return Tổng doanh thu.
     */
    BigDecimal sumRevenue(LocalDateTime fromInclusive, LocalDateTime toExclusive, Long staffId);

    /**
     * Đếm tổng số lượng đơn hàng được tạo trong một khoảng thời gian.
     * @param fromInclusive Thời điểm bắt đầu.
     * @param toExclusive Thời điểm kết thúc.
     * @param staffId ID của nhân viên để lọc. Nếu null, đếm trên toàn hệ thống.
     * @return Tổng số lượng đơn hàng.
     */
    long countOrders(LocalDateTime fromInclusive, LocalDateTime toExclusive, Long staffId);

    /**
     * Đếm số lượng đơn hàng đã được thanh toán đầy đủ trong một khoảng thời gian.
     * @param fromInclusive Thời điểm bắt đầu.
     * @param toExclusive Thời điểm kết thúc.
     * @param staffId ID của nhân viên để lọc. Nếu null, đếm trên toàn hệ thống.
     * @return Tổng số lượng đơn hàng đã thanh toán.
     */
    long countPaidOrders(LocalDateTime fromInclusive, LocalDateTime toExclusive, Long staffId);

    /**
     * Lấy dữ liệu doanh thu hàng ngày trong một khoảng thời gian để vẽ biểu đồ xu hướng.
     * @param fromInclusive Thời điểm bắt đầu.
     * @param toExclusive Thời điểm kết thúc.
     * @param staffId ID của nhân viên để lọc. Nếu null, lấy trên toàn hệ thống.
     * @return Danh sách các mảng Object, mỗi mảng chứa [ngày, tổng doanh thu ngày đó].
     */
    List<Object[]> findDailyRevenue(LocalDateTime fromInclusive, LocalDateTime toExclusive, Long staffId);

    /**
     * Lấy dữ liệu số lượng đơn hàng hàng ngày trong một khoảng thời gian.
     * @param fromInclusive Thời điểm bắt đầu.
     * @param toExclusive Thời điểm kết thúc.
     * @param staffId ID của nhân viên để lọc. Nếu null, lấy trên toàn hệ thống.
     * @return Danh sách các mảng Object, mỗi mảng chứa [ngày, tổng số đơn hàng ngày đó].
     */
    List<Object[]> findDailyOrders(LocalDateTime fromInclusive, LocalDateTime toExclusive, Long staffId);

    /**
     * Thống kê phân bổ các đơn hàng theo trạng thái (ví dụ: 50% COMPLETED, 30% PENDING, 20% CANCELLED).
     * @param fromInclusive Thời điểm bắt đầu.
     * @param toExclusive Thời điểm kết thúc.
     * @param staffId ID của nhân viên để lọc. Nếu null, lấy trên toàn hệ thống.
     * @return Danh sách các mảng Object, mỗi mảng chứa [trạng thái, số lượng].
     */
    List<Object[]> findOrderStatusBreakdown(LocalDateTime fromInclusive, LocalDateTime toExclusive, Long staffId);

    /**
     * Thống kê phân bổ các phương thức thanh toán đã được sử dụng.
     * @param fromInclusive Thời điểm bắt đầu.
     * @param toExclusive Thời điểm kết thúc.
     * @param staffId ID của nhân viên để lọc. Nếu null, lấy trên toàn hệ thống.
     * @return Danh sách các mảng Object, mỗi mảng chứa [phương thức thanh toán, số lần sử dụng].
     */
    List<Object[]> findPaymentMethodBreakdown(LocalDateTime fromInclusive, LocalDateTime toExclusive, Long staffId);

    /**
     * Tìm danh sách các xe bán chạy nhất (dựa trên số lượng) trong một khoảng thời gian.
     * @param fromInclusive Thời điểm bắt đầu.
     * @param toExclusive Thời điểm kết thúc.
     * @param staffId ID của nhân viên để lọc. Nếu null, lấy trên toàn hệ thống.
     * @param limit Giới hạn số lượng xe trong top (ví dụ: top 5, top 10).
     * @return Danh sách các mảng Object, mỗi mảng chứa [thông tin xe, số lượng bán được].
     */
    List<Object[]> findTopCars(LocalDateTime fromInclusive, LocalDateTime toExclusive, Long staffId, int limit);

    /**
     * Thống kê doanh thu theo từng chi nhánh.
     * @param fromInclusive Thời điểm bắt đầu.
     * @param toExclusive Thời điểm kết thúc.
     * @param limit Giới hạn số lượng chi nhánh trong top.
     * @return Danh sách các mảng Object, mỗi mảng chứa [thông tin chi nhánh, tổng doanh thu].
     */
    List<Object[]> findBranchRevenue(LocalDateTime fromInclusive, LocalDateTime toExclusive, int limit);
}
