package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) dạng record, dùng để hiển thị một dòng trong báo cáo doanh số theo chi nhánh.
 *
 * @param branchId        ID của chi nhánh.
 * @param branchCode      Mã của chi nhánh.
 * @param branchName      Tên của chi nhánh.
 * @param branchStatus    Trạng thái hoạt động của chi nhánh.
 * @param totalOrders     Tổng số đơn hàng của chi nhánh trong kỳ báo cáo.
 * @param paidOrders      Số đơn hàng đã thanh toán thành công.
 * @param pendingOrders   Số đơn hàng đang chờ xử lý.
 * @param cancelledOrders Số đơn hàng đã bị hủy.
 * @param revenue         Tổng doanh thu của chi nhánh trong kỳ báo cáo.
 * @param latestOrderAt   Thời điểm của đơn hàng gần nhất tại chi nhánh.
 */
public record BranchSalesReportItem(
        Long branchId,
        String branchCode,
        String branchName,
        Status branchStatus,
        long totalOrders,
        long paidOrders,
        long pendingOrders,
        long cancelledOrders,
        BigDecimal revenue,
        LocalDateTime latestOrderAt
) {
}
