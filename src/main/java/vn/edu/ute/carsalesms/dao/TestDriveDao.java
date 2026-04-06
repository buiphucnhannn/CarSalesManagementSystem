package vn.edu.ute.carsalesms.dao;

import vn.edu.ute.carsalesms.model.entity.TestDrive;

import java.util.List;
import java.util.Optional;

/**
 * Giao diện DAO (Data Access Object) cho thực thể Lái thử (TestDrive).
 * Cung cấp các phương thức để quản lý và truy cập dữ liệu các lịch hẹn lái thử xe.
 */
public interface TestDriveDao {
    /**
     * Lấy tất cả các lịch hẹn lái thử đã được tạo.
     * @return Danh sách tất cả các đối tượng TestDrive.
     */
    List<TestDrive> findAll();

    /**
     * Tìm kiếm các lịch hẹn lái thử dựa trên một từ khóa.
     * Từ khóa có thể là tên khách hàng, mã đơn hẹn, hoặc mã xe.
     * @param keyword Từ khóa để thực hiện tìm kiếm.
     * @return Danh sách các đối tượng TestDrive phù hợp với từ khóa.
     */
    List<TestDrive> findByKeyword(String keyword);

    /**
     * Tìm một lịch hẹn lái thử cụ thể dựa trên ID của nó.
     * @param id ID của lịch hẹn lái thử cần tìm.
     * @return Một Optional chứa đối tượng TestDrive nếu tìm thấy, ngược lại là Optional rỗng.
     */
    Optional<TestDrive> findById(Long id);

    /**
     * Lưu một lịch hẹn lái thử mới vào cơ sở dữ liệu.
     * @param testDrive Đối tượng TestDrive cần lưu.
     * @return Đối tượng TestDrive sau khi đã được lưu.
     */
    TestDrive save(TestDrive testDrive);

    /**
     * Cập nhật thông tin của một lịch hẹn lái thử đã có.
     * @param testDrive Đối tượng TestDrive chứa thông tin cần cập nhật.
     * @return Đối tượng TestDrive sau khi đã được cập nhật.
     */
    TestDrive update(TestDrive testDrive);
}
