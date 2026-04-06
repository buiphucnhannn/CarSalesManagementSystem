package vn.edu.ute.carsalesms.model.dto;

import vn.edu.ute.carsalesms.model.enums.StaffRole;

/**
 * DTO (Đối tượng truyền dữ liệu) để lưu trữ thông tin về người dùng đã xác thực hiện tại.
 * Sử dụng một bản ghi để tạo một lớp dữ liệu bất biến một cách ngắn gọn.
 *
 * @param accountId ID của tài khoản.
 * @param staffId ID của nhân viên.
 * @param staffCode mã của nhân viên.
 * @param fullName tên đầy đủ của nhân viên.
 * @param username tên người dùng của tài khoản.
 * @param role vai trò của nhân viên.
 * @param branchName tên của chi nhánh.
 * @param branchId ID của chi nhánh.
 */
public record AuthenticatedUser(
		Long accountId,
		Long staffId,
		String staffCode,
		String fullName,
		String username,
		StaffRole role,
		String branchName,
		Long branchId
) {
}
