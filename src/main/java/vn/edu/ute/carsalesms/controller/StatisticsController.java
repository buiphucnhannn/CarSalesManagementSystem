package vn.edu.ute.carsalesms.controller;

import vn.edu.ute.carsalesms.model.dto.StatisticsDashboardData;
import vn.edu.ute.carsalesms.service.StatisticsService;

import java.time.LocalDate;
import java.util.Objects;

/**
 * StatisticsController xử lý các yêu cầu liên quan đến thống kê.
 * Nó tuân theo Nguyên tắc Trách nhiệm Đơn lẻ (SRP) bằng cách chỉ tập trung vào logic thống kê.
 * Nó cũng tuân theo Nguyên tắc Đảo ngược Phụ thuộc (DIP) bằng cách phụ thuộc vào giao diện StatisticsService
 * thay vì một triển khai cụ thể.
 */
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * Xây dựng một StatisticsController mới với StatisticsService đã cho.
     * @param statisticsService dịch vụ sẽ được sử dụng để quản lý thống kê.
     */
    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = Objects.requireNonNull(statisticsService, "statisticsService is required");
    }

    /**
     * Lấy thống kê cho quản trị viên.
     * @param fromDate ngày bắt đầu.
     * @param toDate ngày kết thúc.
     * @return dữ liệu bảng điều khiển thống kê.
     */
    public StatisticsDashboardData getAdminStatistics(LocalDate fromDate, LocalDate toDate) {
        return statisticsService.getAdminStatistics(fromDate, toDate);
    }

    /**
     * Lấy thống kê cho nhân viên đang đăng nhập (giới hạn theo chi nhánh hiện tại).
     * @param fromDate ngày bắt đầu.
     * @param toDate ngày kết thúc.
     * @return dữ liệu bảng điều khiển thống kê.
     */
    public StatisticsDashboardData getStaffStatistics(LocalDate fromDate, LocalDate toDate) {
        return statisticsService.getStaffStatistics(fromDate, toDate);
    }
}
