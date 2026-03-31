package vn.edu.ute.carsalesms.service;

import vn.edu.ute.carsalesms.model.dto.TestDriveItem;
import vn.edu.ute.carsalesms.model.dto.TestDriveRequest;
import vn.edu.ute.carsalesms.model.enums.TestDriveStatus;

import java.util.List;

public interface TestDriveService {
    List<TestDriveItem> findByKeyword(String keyword);
    void bookTestDrive(TestDriveRequest req);
    void updateResult(Long id, TestDriveStatus status, String result);
    void cancelTestDrive(Long id, String reason);
}
