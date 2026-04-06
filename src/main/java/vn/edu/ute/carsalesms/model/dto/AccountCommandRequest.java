package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.Status;

/**
 * DTO (Data Transfer Object) dạng record, chứa dữ liệu cho lệnh tạo mới hoặc cập nhật một tài khoản đăng nhập.
 *
 * @param id           ID của tài khoản. Nếu là `null`, đây là yêu cầu tạo mới. Nếu có giá trị, đây là yêu cầu cập nhật.
 * @param staffId      ID của nhân viên được cấp tài khoản (bắt buộc).
 * @param username     Tên đăng nhập (bắt buộc, phải là duy nhất).
 * @param rawPassword  Mật khẩu ở dạng văn bản thô. Mật khẩu này sẽ được mã hóa (hash) ở tầng service trước khi lưu.
 *                     Trường này là bắt buộc khi tạo mới. Khi cập nhật, nếu giá trị là `null` hoặc rỗng, mật khẩu cũ sẽ được giữ nguyên.
 * @param status       Trạng thái của tài khoản (ACTIVE / INACTIVE).
 */
public record AccountCommandRequest(
        Long id,
        Long staffId,
        String username,
        String rawPassword,
        Status status
) {
}
