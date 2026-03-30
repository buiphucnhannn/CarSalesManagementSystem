package vn.edu.ute.carsalesms.service.impl;

import vn.edu.ute.carsalesms.dao.*;
import vn.edu.ute.carsalesms.model.dto.*;
import vn.edu.ute.carsalesms.model.entity.*;
import vn.edu.ute.carsalesms.model.enums.OrderStatus;
import vn.edu.ute.carsalesms.model.enums.Status;
import vn.edu.ute.carsalesms.service.SaleOrderService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Cài đặt nghiệp vụ xử lý tạo Mới, Truy xuất dữ liệu đơn bán.
 */
public class SaleOrderServiceImpl implements SaleOrderService {

    private final SaleOrderDao orderDao;
    private final CarDao carDao;
    private final CustomerDao customerDao;
    private final StaffDao staffDao;
    private final PromotionDao promotionDao;

    public SaleOrderServiceImpl(SaleOrderDao orderDao, CarDao carDao, CustomerDao customerDao, StaffDao staffDao, PromotionDao promotionDao) {
        this.orderDao = orderDao;
        this.carDao = carDao;
        this.customerDao = customerDao;
        this.staffDao = staffDao;
        this.promotionDao = promotionDao;
    }

    @Override
    public List<SaleOrderItem> findOrders(String keyword, OrderStatus statusFilter) {
        List<SaleOrder> orders = orderDao.findOrders(keyword, statusFilter);
        return orders.stream()
                .map(this::mapToItem)
                .collect(Collectors.toList());
    }

    @Override
    public List<SaleOrderDetailItem> findDetailsByOrderId(Long orderId) {
        return orderDao.findDetailsByOrderId(orderId).stream()
                .map(d -> new SaleOrderDetailItem(
                        d.getId(),
                        d.getCar().getId(),
                        d.getCar().getCarCode(),
                        d.getCar().getCarName(),
                        d.getQuantity(),
                        d.getUnitPrice(),
                        d.getDiscountAmount(),
                        d.getLineTotal()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public SaleOrderMetadata loadMetadata() {
        // Lấy KH
        List<CarLookupItem> customers = customerDao.findCustomers(null).stream()
                .map(c -> new CarLookupItem(c.getId(), c.getCustomerCode(), c.getFullName()))
                .collect(Collectors.toList());

        // Lấy Staff (ACTIVE)
        List<CarLookupItem> staffs = staffDao.findStaffs(null, Status.ACTIVE).stream()
                .map(s -> new CarLookupItem(s.getId(), s.getStaffCode(), s.getFullName()))
                .collect(Collectors.toList());

        // Lấy Promotion
        List<CarLookupItem> promos = promotionDao.findActivePromotions().stream()
                .map(p -> new CarLookupItem(p.getId(), p.getPromotionCode(), p.getPromotionName()))
                .collect(Collectors.toList());

        // Lấy Các Xe đang bán còn hàng (avail > 0)
        List<CarLookupItem> cars = carDao.findCars(null, Status.ACTIVE).stream()
                .filter(c -> c.getAvailableQuantity() > 0)
                .map(c -> new CarLookupItem(c.getId(), c.getCarCode(), c.getCarName() + " - " + c.getColor() + " (" + c.getSalePrice() + ")"))
                .collect(Collectors.toList());

        return new SaleOrderMetadata(customers, staffs, promos, cars);
    }

    @Override
    public void createOrder(CreateOrderRequest request) {
        if (request.details() == null || request.details().isEmpty()) {
            throw new IllegalArgumentException("Đơn bán phải có ít nhất 1 mặt hàng (xe).");
        }

        // Lấy Entities liên quan
        Customer customer = customerDao.findById(request.customerId())
                .orElseThrow(() -> new IllegalArgumentException("Khách hàng không tồn tại."));
        Staff staff = staffDao.findStaffById(request.staffId())
                .orElseThrow(() -> new IllegalArgumentException("Nhân viên duyệt đơn không tồn tại."));
        
        Promotion promo = null;
        if (request.promotionId() != null) {
            promo = promotionDao.findById(request.promotionId())
                    .orElseThrow(() -> new IllegalArgumentException("Khuyến mãi không tìm thấy."));
        }

        // Sinh mã đơn
        String newOrderCode = generateOrderCode();

        // Chuẩn bị tổng ban đầu
        BigDecimal totalAmount = BigDecimal.ZERO;

        // Tạo Entity đơn (cơ bản chưa có total)
        SaleOrder newOrder = new SaleOrder();
        newOrder.setOrderCode(newOrderCode);
        newOrder.setCustomer(customer);
        newOrder.setStaff(staff);
        newOrder.setPromotion(promo);
        newOrder.setOrderDate(LocalDateTime.now());
        newOrder.setPaymentMethod(request.paymentMethod());
        newOrder.setOrderStatus(OrderStatus.PENDING);
        newOrder.setNote(request.note());

        // Cần lưu SaleOrder trước để có ID tạo detail, 
        // Lấy order vừa lưu để set cho detail.
        SaleOrder savedOrder = orderDao.save(newOrder);

        // Duyệt chi tiết giỏ hàng
        for (OrderDetailRequest detailReq : request.details()) {
            Car car = carDao.findById(detailReq.carId())
                    .orElseThrow(() -> new IllegalArgumentException("Xe không tồn tại."));

            if (car.getAvailableQuantity() < detailReq.quantity()) {
                throw new IllegalStateException("Số lượng xe " + car.getCarCode() + " trong kho chỉ còn " + car.getAvailableQuantity() + " chiếc.");
            }

            // Tính tiền thành phần (tạm thời chưa áp mã giảm cấp độ dòng, hoặc có thể quy ước mã apply cả giỏ)
            // Trong đề bài, promotion áp dụng trên Order tổng, nên detail discount = 0
            BigDecimal unitP = detailReq.unitPrice();
            int qty = detailReq.quantity();
            BigDecimal dLineTotal = unitP.multiply(BigDecimal.valueOf(qty));

            SaleOrderDetail detEntity = new SaleOrderDetail();
            detEntity.setSaleOrder(savedOrder);
            detEntity.setCar(car);
            detEntity.setQuantity(qty);
            detEntity.setUnitPrice(unitP);
            detEntity.setDiscountAmount(BigDecimal.ZERO); 
            detEntity.setLineTotal(dLineTotal);
            // Lưu detail
            orderDao.saveDetail(detEntity);

            // Cập nhật stock
            car.setAvailableQuantity(car.getAvailableQuantity() - qty);
            carDao.save(car); 

            totalAmount = totalAmount.add(dLineTotal);
        }

        // Tính giảm giá ở mức tổng đơn
        BigDecimal discountTotalAmount = BigDecimal.ZERO;
        if (promo != null) {
            String type = promo.getDiscountType();
            BigDecimal value = promo.getDiscountValue();
            if ("PERCENT".equalsIgnoreCase(type)) {
                discountTotalAmount = totalAmount.multiply(value).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            } else if ("AMOUNT".equalsIgnoreCase(type)) {
                discountTotalAmount = value;
                if (discountTotalAmount.compareTo(totalAmount) > 0) {
                    discountTotalAmount = totalAmount; // Không hoàn tiền
                }
            }
        }
        
        BigDecimal finalAmt = totalAmount.subtract(discountTotalAmount);

        // Cập nhật lại hóa đơn (số tiền)
        savedOrder.setTotalAmount(totalAmount);
        savedOrder.setDiscountAmount(discountTotalAmount);
        savedOrder.setFinalAmount(finalAmt);

        orderDao.save(savedOrder); // Lưu lại giá trị
    }

    @Override
    public void cancelOrder(Long orderId) {
        SaleOrder order = orderDao.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn bán với ID = " + orderId));

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Đơn hàng này đã được huỷ từ trước.");
        }
        if (order.getOrderStatus() == OrderStatus.PAID) {
            throw new IllegalStateException("Đơn hàng đã thanh toán không thể huỷ trực tiếp. Vui lòng xử lý hoàn tiền nếu cần.");
        }

        // Hoàn trả số lượng xe vào kho (availableQuantity)
        List<SaleOrderDetail> details = orderDao.findDetailsByOrderId(orderId);
        for (SaleOrderDetail d : details) {
            Car car = d.getCar();
            car.setAvailableQuantity(car.getAvailableQuantity() + d.getQuantity());
            carDao.save(car); // Hoàn số lượng
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        orderDao.save(order);
    }

    private SaleOrderItem mapToItem(SaleOrder o) {
        return new SaleOrderItem(
                o.getId(),
                o.getOrderCode(),
                o.getCustomer() != null ? o.getCustomer().getFullName() : "N/A",
                o.getStaff() != null ? o.getStaff().getFullName() : "N/A",
                o.getPromotion() != null ? o.getPromotion().getPromotionCode() : null,
                o.getOrderDate(),
                o.getTotalAmount(),
                o.getDiscountAmount(),
                o.getFinalAmount(),
                o.getPaymentMethod(),
                o.getOrderStatus(),
                o.getNote()
        );
    }

    private String generateOrderCode() {
        // Sinh mã SO- random
        String uuidPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "SO-" + uuidPart;
    }
}
