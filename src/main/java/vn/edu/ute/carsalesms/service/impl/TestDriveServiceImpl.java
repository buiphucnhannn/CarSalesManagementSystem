package vn.edu.ute.carsalesms.service.impl;

import vn.edu.ute.carsalesms.dao.CarDao;
import vn.edu.ute.carsalesms.dao.CustomerDao;
import vn.edu.ute.carsalesms.dao.StaffDao;
import vn.edu.ute.carsalesms.dao.TestDriveDao;
import vn.edu.ute.carsalesms.model.dto.TestDriveItem;
import vn.edu.ute.carsalesms.model.dto.TestDriveRequest;
import vn.edu.ute.carsalesms.model.entity.Car;
import vn.edu.ute.carsalesms.model.entity.Customer;
import vn.edu.ute.carsalesms.model.entity.Staff;
import vn.edu.ute.carsalesms.model.entity.TestDrive;
import vn.edu.ute.carsalesms.model.enums.TestDriveStatus;
import vn.edu.ute.carsalesms.service.AuditLogService;
import vn.edu.ute.carsalesms.service.TestDriveService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TestDriveServiceImpl implements TestDriveService {

    private final TestDriveDao testDriveDao;
    private final CustomerDao customerDao;
    private final CarDao carDao;
    private final StaffDao staffDao;
    private final AuditLogService auditLogService;

    public TestDriveServiceImpl(TestDriveDao testDriveDao, CustomerDao customerDao, CarDao carDao, StaffDao staffDao) {
        this(testDriveDao, customerDao, carDao, staffDao, new NoOpAuditLogService());
    }

    public TestDriveServiceImpl(TestDriveDao testDriveDao,
                                CustomerDao customerDao,
                                CarDao carDao,
                                StaffDao staffDao,
                                AuditLogService auditLogService) {
        this.testDriveDao = testDriveDao;
        this.customerDao = customerDao;
        this.carDao = carDao;
        this.staffDao = staffDao;
        this.auditLogService = auditLogService;
    }

    @Override
    public List<TestDriveItem> findByKeyword(String keyword) {
        return testDriveDao.findByKeyword(keyword)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void bookTestDrive(TestDriveRequest req) {
        if (req.scheduledTime() == null || req.scheduledTime().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("Thời gian đặt lịch phải lớn hơn hiện tại.");
        }

        Customer customer = customerDao.findById(req.customerId())
                .orElseThrow(() -> new IllegalArgumentException("Khách hàng không tồn tại."));
        Car car = carDao.findById(req.carId())
                .orElseThrow(() -> new IllegalArgumentException("Xe không tồn tại."));
        Staff staff = staffDao.findStaffById(req.staffId())
                .orElseThrow(() -> new IllegalArgumentException("Nhân viên tư vấn không tồn tại."));

        String code = "TD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        TestDrive newDrive = new TestDrive(
                code,
                customer,
                car,
                staff,
                req.scheduledTime(),
                null,
                TestDriveStatus.SCHEDULED,
                req.note()
        );

        TestDrive saved = testDriveDao.save(newDrive);
        auditLogService.log(
                "CREATE",
                "TEST_DRIVE",
                saved.getId(),
                null,
                "code=" + saved.getTestDriveCode() + ", customerId=" + req.customerId() + ", carId=" + req.carId() + ", staffId=" + req.staffId() + ", scheduled=" + req.scheduledTime()
        );
    }

    @Override
    public void updateResult(Long id, TestDriveStatus status, String result) {
        TestDrive td = testDriveDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Phiếu hẹn lái thử không tồn tại."));
        
        td.setStatus(status);
        td.setResult(result);
        TestDrive updated = testDriveDao.update(td);
        auditLogService.log(
                "UPDATE",
                "TEST_DRIVE",
                updated.getId(),
                null,
                "status=" + status + ", result=" + result
        );
    }

    @Override
    public void cancelTestDrive(Long id, String reason) {
        TestDrive td = testDriveDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Phiếu hẹn lái thử không tồn tại."));
        td.setStatus(TestDriveStatus.CANCELLED);
        td.setNote(reason);
        TestDrive updated = testDriveDao.update(td);
        auditLogService.log(
                "CANCEL",
                "TEST_DRIVE",
                updated.getId(),
                null,
                "reason=" + reason
        );
    }

    private TestDriveItem mapToDto(TestDrive td) {
        return new TestDriveItem(
                td.getId(),
                td.getTestDriveCode(),
                td.getCustomer() != null ? td.getCustomer().getFullName() : "N/A",
                td.getCar() != null ? td.getCar().getCarName() : "N/A",
                td.getStaff() != null ? td.getStaff().getFullName() : "N/A",
                td.getScheduledTime(),
                td.getResult(),
                td.getStatus(),
                td.getNote()
        );
    }
}
