package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.Status;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO (Data Transfer Object) dạng record, dùng để hiển thị thông tin chi tiết của một chương trình khuyến mãi.
 * Đối tượng này là chỉ đọc và ánh xạ trực tiếp các thuộc tính của thực thể `Promotion` để truyền dữ liệu lên tầng view.
 *
 * @param id            Khóa chính của chương trình khuyến mãi.
 * @param promotionCode Mã định danh duy nhất của khuyến mãi.
 * @param promotionName Tên của chương trình khuyến mãi.
 * @param discountType  Loại giảm giá (ví dụ: "PERCENTAGE", "FIXED_AMOUNT").
 * @param discountValue Giá trị của việc giảm giá (có thể là % hoặc số tiền cụ thể).
 * @param startDate     Ngày bắt đầu chương trình khuyến mãi.
 * @param endDate       Ngày kết thúc chương trình khuyến mãi.
 * @param description   Mô tả chi tiết về chương trình.
 * @param status        Trạng thái của chương trình (ACTIVE hoặc INACTIVE).
 */
public record PromotionItem(
        Long id,
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
