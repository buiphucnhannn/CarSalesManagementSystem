package vn.edu.ute.carsalesms.dao;

import vn.edu.ute.carsalesms.model.entity.AuditLog;

import java.util.List;

/**
 * Giao diện DAO (Data Access Object) cho thực thể Nhật ký kiểm toán (AuditLog).
 * Cung cấp các phương thức để lưu và truy vấn dữ liệu nhật ký hoạt động của người dùng trong hệ thống.
 */
public interface AuditLogDao {

    /**
     * Lưu một bản ghi nhật ký mới vào cơ sở dữ liệu.
     * @param log Đối tượng AuditLog cần lưu.
     * @return Đối tượng AuditLog sau khi đã được lưu.
     */
    AuditLog save(AuditLog log);

    /**
     * Tìm kiếm và lấy danh sách các bản ghi nhật ký dựa trên các tiêu chí lọc.
     * @param keyword Từ khóa tìm kiếm trong các trường của nhật ký (ví dụ: tên người dùng, chi tiết hành động).
     * @param actionFilter Lọc theo loại hành động (ví dụ: CREATE, UPDATE, DELETE).
     * @param entityFilter Lọc theo loại thực thể bị tác động (ví dụ: CAR, CUSTOMER, ORDER).
     * @param limit Giới hạn số lượng bản ghi nhật ký trả về.
     * @return Danh sách các đối tượng AuditLog phù hợp.
     */
    List<AuditLog> findLogs(String keyword, String actionFilter, String entityFilter, int limit);

    /**
     * Lấy danh sách tất cả các loại hành động (action) duy nhất đã được ghi lại trong nhật ký.
     * Dùng để tạo bộ lọc trên giao diện người dùng.
     * @return Danh sách các chuỗi đại diện cho hành động (ví dụ: "LOGIN_SUCCESS", "CREATE_ORDER").
     */
    List<String> findDistinctActions();

    /**
     * Lấy danh sách tất cả các loại thực thể (entity) duy nhất đã được ghi lại trong nhật ký.
     * Dùng để tạo bộ lọc trên giao diện người dùng.
     * @return Danh sách các chuỗi đại diện cho thực thể (ví dụ: "STAFF", "PROMOTION").
     */
    List<String> findDistinctEntities();
}
