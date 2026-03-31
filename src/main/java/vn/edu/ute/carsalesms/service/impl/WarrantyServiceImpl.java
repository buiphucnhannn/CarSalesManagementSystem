package vn.edu.ute.carsalesms.service.impl;

import vn.edu.ute.carsalesms.dao.SaleOrderDao;
import vn.edu.ute.carsalesms.dao.WarrantyDao;
import vn.edu.ute.carsalesms.model.dto.WarrantyItem;
import vn.edu.ute.carsalesms.model.entity.SaleOrder;
import vn.edu.ute.carsalesms.model.entity.SaleOrderDetail;
import vn.edu.ute.carsalesms.model.entity.Warranty;
import vn.edu.ute.carsalesms.model.enums.OrderStatus;
import vn.edu.ute.carsalesms.model.enums.WarrantyStatus;
import vn.edu.ute.carsalesms.service.WarrantyService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class WarrantyServiceImpl implements WarrantyService {

    private final WarrantyDao warrantyDao;
    private final SaleOrderDao saleOrderDao; // Lấy List OrderDetail để sinh warranty

    public WarrantyServiceImpl(WarrantyDao warrantyDao, SaleOrderDao saleOrderDao) {
        this.warrantyDao = warrantyDao;
        this.saleOrderDao = saleOrderDao;
    }

    @Override
    public List<WarrantyItem> findByKeyword(String keyword) {
        // --- 1. TỰ ĐỘNG BÙ ĐẮP BẢO HÀNH CHO CÁC XE ĐÃ BÁN TRONG QUÁ KHỨ ---
        // (Auto-Migrate Legacy PAID Orders)
        List<SaleOrder> legacyOrders = saleOrderDao.findOrders(null, OrderStatus.PAID);
        for (SaleOrder order : legacyOrders) {
             generateWarrantyForOrder(order.getId());
        }

        // --- 2. XỬ LÝ GỐC VÀ CẬP NHẬT TRẠNG THÁI HẾT HẠN NGẦM ---
        List<Warranty> lists = warrantyDao.findByKeyword(keyword);
        LocalDate now = LocalDate.now();
        
        for (Warranty w : lists) {
            if (w.getWarrantyStatus() == WarrantyStatus.ACTIVE && w.getEndDate().isBefore(now)) {
                w.setWarrantyStatus(WarrantyStatus.EXPIRED);
                warrantyDao.update(w);
            }
        }
        
        return lists.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public void generateWarrantyForOrder(Long saleOrderId) {
        SaleOrder order = saleOrderDao.findById(saleOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Đơn bán để kích hoạt Bảo hành."));

        if (order.getOrderStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("Đơn Hàng chưa thanh toán, Không được Kích hoạt Bảo hành.");
        }

        // Vòng lặp lấy từng chiếc xe chạy trong Đơn Hàng (SaleOrderDetail qua Query riêng biệt tránh LazyLoad)
        List<SaleOrderDetail> details = saleOrderDao.findDetailsByOrderId(saleOrderId);
        for (SaleOrderDetail sod : details) {
            boolean isExists = warrantyDao.findBySaleOrderDetailId(sod.getId()).isPresent();
            if (!isExists) {
                // Tạo mới nếu xe chưa có Thẻ (vd Order vừa PAID lần đầu)
                Warranty nv = new Warranty();
                nv.setWarrantyCode("WR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                nv.setSaleOrderDetail(sod);
                nv.setStartDate(LocalDate.now());
                nv.setEndDate(LocalDate.now().plusYears(3)); // Bảo hành 3 năm trọn Đời xe.
                nv.setWarrantyStatus(WarrantyStatus.ACTIVE);
                nv.setNote("Kích hoạt tự động khi Mua Xe qua hệ thống.");
                
                warrantyDao.save(nv);
            }
        }
    }

    @Override
    public void addNoteToWarranty(Long warrantyId, String note) {
        Warranty w = warrantyDao.findById(warrantyId)
                .orElseThrow(() -> new IllegalArgumentException("Thẻ bảo hành không tồn tại."));
        String oldNote = w.getNote() != null ? w.getNote() : "";
        w.setNote(oldNote + " | " + LocalDate.now().toString() + ": " + note);
        warrantyDao.update(w);
    }

    private WarrantyItem mapToDto(Warranty w) {
        return new WarrantyItem(
                w.getId(),
                w.getWarrantyCode(),
                w.getSaleOrderDetail().getSaleOrder().getOrderCode(),
                w.getSaleOrderDetail().getSaleOrder().getCustomer().getFullName(),
                w.getSaleOrderDetail().getCar().getCarName(),
                w.getStartDate(),
                w.getEndDate(),
                w.getWarrantyStatus(),
                w.getNote()
        );
    }
}
