package vn.edu.ute.carsalesms.dao;

import vn.edu.ute.carsalesms.model.entity.TestDrive;

import java.util.List;
import java.util.Optional;

public interface TestDriveDao {
    List<TestDrive> findAll();
    /**
     * Tìm lịch hẹn lái thử dựa trên từ khoá (Tên khách hàng, mã Đơn hẹn hoặc mã xe)
     */
    List<TestDrive> findByKeyword(String keyword);
    Optional<TestDrive> findById(Long id);
    TestDrive save(TestDrive testDrive);
    TestDrive update(TestDrive testDrive);
}
