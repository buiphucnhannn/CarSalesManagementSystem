package vn.edu.ute.carsalesms.controller;

import vn.edu.ute.carsalesms.model.dto.CustomerCommandRequest;
import vn.edu.ute.carsalesms.model.dto.CustomerItem;
import vn.edu.ute.carsalesms.service.AuditLogService;
import vn.edu.ute.carsalesms.service.CustomerService;
import vn.edu.ute.carsalesms.service.NoOpAuditLogService;

import java.util.List;
import java.util.Objects;

/**
 * CustomerManagementController xử lý các yêu cầu liên quan đến quản lý khách hàng.
 * Nó tuân theo Nguyên tắc Trách nhiệm Đơn lẻ (SRP) bằng cách chỉ tập trung vào logic quản lý khách hàng.
 * Nó cũng tuân theo Nguyên tắc Đảo ngược Phụ thuộc (DIP) bằng cách phụ thuộc vào các giao diện
 * (CustomerService, AuditLogService) thay vì các triển khai cụ thể.
 */
public class CustomerManagementController {

    /** Service được inject qua constructor – Dependency Inversion. */
    private final CustomerService customerService;
    private final AuditLogService auditLogService;

    /**
     * Xây dựng một CustomerManagementController mới với CustomerService đã cho.
     * @param customerService dịch vụ sẽ được sử dụng để quản lý khách hàng.
     */
    public CustomerManagementController(CustomerService customerService) {
        this(customerService, new NoOpAuditLogService());
    }

    /**
     * Xây dựng một CustomerManagementController mới với CustomerService và AuditLogService đã cho.
     * @param customerService dịch vụ sẽ được sử dụng để quản lý khách hàng.
     * @param auditLogService dịch vụ sẽ được sử dụng để ghi lại các hành động.
     */
    public CustomerManagementController(CustomerService customerService, AuditLogService auditLogService) {
        this.customerService = Objects.requireNonNull(customerService, "customerService is required");
        this.auditLogService = Objects.requireNonNull(auditLogService, "auditLogService is required");
    }

    /**
     * Tải danh sách khách hàng theo từ khóa.
     *
     * @param keyword từ khóa tìm kiếm (null = tất cả)
     * @return danh sách CustomerItem để hiển thị trong bảng
     */
    public List<CustomerItem> loadCustomers(String keyword) {
        return customerService.getCustomers(keyword);
    }

    /**
     * Tải mã khách hàng tiếp theo.
     * @return mã khách hàng tiếp theo.
     */
    public String loadNextCustomerCode() {
        return customerService.getNextCustomerCode();
    }

    /**
     * Thêm mới khách hàng.
     *
     * @param request dữ liệu từ dialog
     * @return CustomerItem đã tạo
     */
    public CustomerItem createCustomer(CustomerCommandRequest request) {
        CustomerItem created = customerService.createCustomer(request);
        auditLogService.log("CREATE", "CUSTOMER", created.id(), null, request.toString());
        return created;
    }

    /**
     * Cập nhật thông tin khách hàng.
     *
     * @param request dữ liệu từ dialog (phải có id)
     * @return CustomerItem đã cập nhật
     */
    public CustomerItem updateCustomer(CustomerCommandRequest request) {
        CustomerItem updated = customerService.updateCustomer(request);
        auditLogService.log("UPDATE", "CUSTOMER", updated.id(), null, request.toString());
        return updated;
    }

    /**
     * Xóa khách hàng.
     *
     * @param customerId id khách hàng cần xóa
     */
    public void deleteCustomer(Long customerId) {
        customerService.deleteCustomer(customerId);
        auditLogService.log("DELETE", "CUSTOMER", customerId, null, null);
    }
}
