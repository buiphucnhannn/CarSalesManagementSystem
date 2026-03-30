package vn.edu.ute.carsalesms.service.impl;

import vn.edu.ute.carsalesms.dao.CustomerDao;
import vn.edu.ute.carsalesms.model.dto.CustomerCommandRequest;
import vn.edu.ute.carsalesms.model.dto.CustomerItem;
import vn.edu.ute.carsalesms.model.entity.Customer;
import vn.edu.ute.carsalesms.service.CustomerService;

import java.util.List;
import java.util.Objects;

/**
 * Triển khai CustomerService.
 * Chứa toàn bộ logic nghiệp vụ: validate, mapping entity ↔ DTO.
 * Không phụ thuộc vào framework UI (tách biệt Service khỏi View).
 */
public class CustomerServiceImpl implements CustomerService {

    /** DAO được inject qua constructor (Dependency Inversion). */
    private final CustomerDao customerDao;

    public CustomerServiceImpl(CustomerDao customerDao) {
        this.customerDao = Objects.requireNonNull(customerDao, "customerDao is required");
    }

    @Override
    public List<CustomerItem> getCustomers(String keyword) {
        // Dùng Stream API để map entity → DTO
        return customerDao.findCustomers(keyword).stream()
                .map(this::toItem)
                .toList();
    }

    @Override
    public CustomerItem createCustomer(CustomerCommandRequest request) {
        // 1. Validate dữ liệu đầu vào
        CustomerCommandRequest validated = validate(request, false);

        // 2. Kiểm tra trùng mã khách hàng
        customerDao.findByCode(validated.customerCode()).ifPresent(existing -> {
            throw new IllegalArgumentException("Mã khách hàng đã tồn tại: " + validated.customerCode());
        });

        // 3. Tạo entity và điền dữ liệu
        Customer customer = new Customer();
        applyData(customer, validated);

        // 4. Lưu và trả về DTO
        return toItem(customerDao.save(customer));
    }

    @Override
    public CustomerItem updateCustomer(CustomerCommandRequest request) {
        // 1. Validate (yêu cầu id)
        CustomerCommandRequest validated = validate(request, true);

        // 2. Tìm entity cần cập nhật
        Customer customer = customerDao.findById(validated.id())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng cần cập nhật."));

        // 3. Kiểm tra trùng mã (trừ bản thân)
        customerDao.findByCode(validated.customerCode())
                .filter(existing -> !existing.getId().equals(validated.id()))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Mã khách hàng đã tồn tại: " + validated.customerCode());
                });

        // 4. Cập nhật và lưu
        applyData(customer, validated);
        return toItem(customerDao.save(customer));
    }

    @Override
    public void deleteCustomer(Long customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("Id khách hàng không hợp lệ.");
        }
        // Kiểm tra tồn tại trước khi xóa
        customerDao.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng cần xóa."));
        try {
            customerDao.deleteById(customerId);
        } catch (Exception ex) {
            // ConstraintViolationException nếu còn đơn bán / lái thử liên kết
            throw new IllegalStateException(
                    "Không thể xóa khách hàng này vì còn dữ liệu liên quan (đơn hàng, lái thử…).", ex);
        }
    }

    // ─── Private helpers ──────────────────────────────────────────────────

    /**
     * Validate request đầu vào.
     *
     * @param request   dữ liệu cần validate
     * @param requireId true nếu thao tác cần id (update)
     * @return request đã được chuẩn hóa (trim whitespace, v.v.)
     */
    private CustomerCommandRequest validate(CustomerCommandRequest request, boolean requireId) {
        if (request == null) {
            throw new IllegalArgumentException("Dữ liệu khách hàng không hợp lệ.");
        }
        if (requireId && request.id() == null) {
            throw new IllegalArgumentException("Thiếu mã định danh khách hàng.");
        }
        if (request.customerCode() == null || request.customerCode().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập mã khách hàng.");
        }
        if (request.fullName() == null || request.fullName().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập họ tên khách hàng.");
        }
        if (request.phone() == null || request.phone().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập số điện thoại.");
        }
        // Trả về record mới với dữ liệu đã trim
        return new CustomerCommandRequest(
                request.id(),
                request.customerCode().trim().toUpperCase(),
                request.fullName().trim(),
                request.phone().trim(),
                request.email() == null ? null : request.email().trim(),
                request.gender(),
                request.dateOfBirth(),
                request.identityNumber() == null ? null : request.identityNumber().trim(),
                request.address() == null ? null : request.address().trim(),
                request.note()
        );
    }

    /**
     * Điền dữ liệu từ request vào entity Customer.
     * Dùng cho cả create và update.
     */
    private void applyData(Customer customer, CustomerCommandRequest request) {
        customer.setCustomerCode(request.customerCode());
        customer.setFullName(request.fullName());
        customer.setPhone(request.phone());
        customer.setEmail(request.email());
        customer.setGender(request.gender());
        customer.setDateOfBirth(request.dateOfBirth());
        customer.setIdentityNumber(request.identityNumber());
        customer.setAddress(request.address());
        customer.setNote(request.note());
    }

    /**
     * Mapping entity Customer → DTO CustomerItem.
     */
    private CustomerItem toItem(Customer customer) {
        return new CustomerItem(
                customer.getId(),
                customer.getCustomerCode(),
                customer.getFullName(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getGender(),
                customer.getDateOfBirth(),
                customer.getIdentityNumber(),
                customer.getAddress(),
                customer.getNote(),
                customer.getCreatedAt()
        );
    }
}
