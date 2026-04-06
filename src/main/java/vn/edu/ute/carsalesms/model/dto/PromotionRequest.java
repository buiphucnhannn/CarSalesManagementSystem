package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.Status;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO (Data Transfer Object) dạng record, chứa dữ liệu cần thiết để tạo mới hoặc cập nhật một chương trình khuyến mãi.
 * Đối tượng này được gửi từ tầng view (giao diện người dùng) lên tầng service để xử lý.
 *
 * @param promotionCode Mã định danh cho chương trình khuyến mãi.
 * @param promotionName Tên của chương trình khuyến mãi.
 * @param discountType  Loại giảm giá (ví dụ: "PERCENTAGE", "FIXED_AMOUNT").
 * @param discountValue Giá trị của việc giảm giá.
 * @param startDate     Ngày bắt đầu áp dụng khuyến mãi.
 * @param endDate       Ngày kết thúc khuyến mãi.
 * @param description   Mô tả chi tiết về chương trình.
 * @param status        Trạng thái của chương trình (thường là ACTIVE khi tạo).
 */
public record PromotionRequest(
        String promotionCode,
        String promotionName,
        String discountType,
        BigDecimal discountValue,
        LocalDate startDate,
        LocalDate endDate,
        String description,
        Status status
) {
}
