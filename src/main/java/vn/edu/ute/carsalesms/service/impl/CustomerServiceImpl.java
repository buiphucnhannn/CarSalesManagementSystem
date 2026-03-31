package vn.edu.ute.carsalesms.service.impl;

import vn.edu.ute.carsalesms.dao.CustomerDao;
import vn.edu.ute.carsalesms.model.dto.CustomerCommandRequest;
import vn.edu.ute.carsalesms.model.dto.CustomerItem;
import vn.edu.ute.carsalesms.model.entity.Customer;
import vn.edu.ute.carsalesms.service.CustomerService;
import vn.edu.ute.carsalesms.util.CodeGeneratorUtil;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Triển khai CustomerService.
 * Chứa toàn bộ logic nghiệp vụ: validate, mapping entity ↔ DTO.
 * Không phụ thuộc vào framework UI (tách biệt Service khỏi View).
 */
public class CustomerServiceImpl implements CustomerService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{9,11}$");

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
    public String getNextCustomerCode() {
        List<String> existingCodes = customerDao.findCustomers(null).stream()
                .map(Customer::getCustomerCode)
                .toList();
        return CodeGeneratorUtil.nextCodeFromExisting(existingCodes, "CUST-", 4);
    }

    @Override
    public CustomerItem createCustomer(CustomerCommandRequest request) {
        // 1. Validate dữ liệu đầu vào
        CustomerCommandRequest validated = validate(request, false);

        String generatedCode = getNextCustomerCode();
        CustomerCommandRequest createRequest = new CustomerCommandRequest(
                validated.id(),
                generatedCode,
                validated.fullName(),
                validated.phone(),
                validated.email(),
                validated.gender(),
                validated.dateOfBirth(),
                validated.identityNumber(),
                validated.address(),
                validated.note()
        );

        // 2. Kiểm tra trùng mã khách hàng
        customerDao.findByCode(createRequest.customerCode()).ifPresent(existing -> {
            throw new IllegalArgumentException("Mã khách hàng đã tồn tại: " + createRequest.customerCode());
        });

        // 3. Tạo entity và điền dữ liệu
        Customer customer = new Customer();
        applyData(customer, createRequest);

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
        if (requireId && (request.customerCode() == null || request.customerCode().isBlank())) {
            throw new IllegalArgumentException("Vui lòng nhập mã khách hàng.");
        }
        if (request.fullName() == null || request.fullName().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập họ tên khách hàng.");
        }
        if (request.phone() == null || request.phone().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập số điện thoại.");
        }
        if (!PHONE_PATTERN.matcher(request.phone().trim()).matches()) {
            throw new IllegalArgumentException("Số điện thoại phải gồm 9-11 chữ số.");
        }

        String normalizedEmail = request.email() == null ? null : request.email().trim();
        if (normalizedEmail != null && !normalizedEmail.isBlank() && !EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new IllegalArgumentException("Email không hợp lệ (ví dụ: name@example.com).");
        }

        String normalizedCode = request.customerCode() == null ? null : request.customerCode().trim().toUpperCase();
        // Trả về record mới với dữ liệu đã trim
        return new CustomerCommandRequest(
                request.id(),
                normalizedCode,
                request.fullName().trim(),
                request.phone().trim(),
                normalizedEmail == null || normalizedEmail.isEmpty() ? null : normalizedEmail,
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
