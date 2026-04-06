package vn.edu.ute.carsalesms.model.dto;

import java.math.BigDecimal;

/**
 * DTO (Data Transfer Object) dạng record, đại diện cho thông tin của một dòng chi tiết trong yêu cầu tạo đơn hàng.
 * Mỗi đối tượng này tương ứng với một loại xe và số lượng được thêm vào "giỏ hàng" khi tạo đơn.
 *
 * @param carId    ID của xe được chọn (bắt buộc).
 * @param quantity Số lượng xe cần mua (phải lớn hơn hoặc bằng 1).
 * @param unitPrice Đơn giá của xe tại thời điểm chốt đơn. Giá trị này được lấy từ `car.salePrice` và được lưu lại
 *                  để đảm bảo tính nhất quán của đơn hàng ngay cả khi giá xe thay đổi trong tương lai.
 */
public record OrderDetailRequest(
        Long carId,
        Integer quantity,
        BigDecimal unitPrice
) {
}
