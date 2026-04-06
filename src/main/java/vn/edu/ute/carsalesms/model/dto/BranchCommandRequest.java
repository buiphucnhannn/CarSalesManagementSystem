package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.Status;

/**
 * DTO (Data Transfer Object) dạng record, chứa dữ liệu cho lệnh tạo mới hoặc cập nhật thông tin một chi nhánh.
 *
 * @param id         ID của chi nhánh. Nếu là `null`, đây là yêu cầu tạo mới. Nếu có giá trị, đây là yêu cầu cập nhật.
 * @param branchCode Mã chi nhánh (duy nhất).
 * @param branchName Tên chi nhánh.
 * @param address    Địa chỉ của chi nhánh.
 * @param phone      Số điện thoại liên hệ.
 * @param email      Địa chỉ email liên hệ.
 * @param status     Trạng thái hoạt động của chi nhánh (ACTIVE hoặc INACTIVE).
 */
public record BranchCommandRequest(
        Long id,
        String branchCode,
        String branchName,
        String address,
        String phone,
        String email,
        Status status
) {
}
