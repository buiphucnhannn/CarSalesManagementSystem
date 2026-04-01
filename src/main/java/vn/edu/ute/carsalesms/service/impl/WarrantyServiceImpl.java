package vn.edu.ute.carsalesms.service.impl;

import vn.edu.ute.carsalesms.dao.SaleOrderDao;
import vn.edu.ute.carsalesms.dao.WarrantyDao;
import vn.edu.ute.carsalesms.model.dto.WarrantyItem;
import vn.edu.ute.carsalesms.model.entity.SaleOrder;
import vn.edu.ute.carsalesms.model.entity.SaleOrderDetail;
import vn.edu.ute.carsalesms.model.entity.Warranty;
import vn.edu.ute.carsalesms.model.enums.OrderStatus;
import vn.edu.ute.carsalesms.model.enums.WarrantyStatus;
import vn.edu.ute.carsalesms.service.AuditLogService;
import vn.edu.ute.carsalesms.service.WarrantyService;
import vn.edu.ute.carsalesms.session.CurrentSession;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class WarrantyServiceImpl implements WarrantyService {

    private final WarrantyDao warrantyDao;
    private final SaleOrderDao saleOrderDao; // Lấy List OrderDetail để sinh warranty
    private final AuditLogService auditLogService;

    public WarrantyServiceImpl(WarrantyDao warrantyDao, SaleOrderDao saleOrderDao) {
        this(warrantyDao, saleOrderDao, new NoOpAuditLogService());
    }

    public WarrantyServiceImpl(WarrantyDao warrantyDao,
                               SaleOrderDao saleOrderDao,
                               AuditLogService auditLogService) {
        this.warrantyDao = warrantyDao;
        this.saleOrderDao = saleOrderDao;
        this.auditLogService = auditLogService;
    }

    @Override
    public List<WarrantyItem> findByKeyword(String keyword) {
        // --- 1. TỰ ĐỘNG BÙ ĐẮP BẢO HÀNH CHO CÁC XE ĐÃ BÁN TRONG QUÁ KHỨ ---
        // (Auto-Migrate Legacy PAID Orders)
        List<SaleOrder> legacyOrders = saleOrderDao.findOrders(null, OrderStatus.PAID);
        for (SaleOrder order : legacyOrders) {
             if (!canAccessOrder(order)) {
                 continue;
             }
             generateWarrantyForOrder(order.getId());
        }

        // --- 2. XỬ LÝ GỐC VÀ CẬP NHẬT TRẠNG THÁI HẾT HẠN NGẦM ---
        List<Warranty> lists = warrantyDao.findByKeyword(keyword);
        if (!CurrentSession.isAdmin()) {
            lists = lists.stream().filter(this::canAccessWarranty).collect(Collectors.toList());
        }
        LocalDate now = LocalDate.now();
        
        for (Warranty w : lists) {
            if (w.getWarrantyStatus() == WarrantyStatus.ACTIVE && w.getEndDate().isBefore(now)) {
                w.setWarrantyStatus(WarrantyStatus.EXPIRED);
                Warranty updated = warrantyDao.update(w);
                auditLogService.log("EXPIRE", "WARRANTY", updated.getId(), "status=ACTIVE", "status=EXPIRED");
            }
        }
        
        return lists.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public void generateWarrantyForOrder(Long saleOrderId) {
        SaleOrder order = saleOrderDao.findById(saleOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Đơn bán để kích hoạt Bảo hành."));
        assertOrderAccess(order);

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
                
                Warranty saved = warrantyDao.save(nv);
                auditLogService.log(
                        "CREATE",
                        "WARRANTY",
                        saved.getId(),
                        null,
                        "saleOrderId=" + saleOrderId + ", saleOrderDetailId=" + sod.getId() + ", code=" + saved.getWarrantyCode()
                );
            }
        }
    }

    @Override
    public void addNoteToWarranty(Long warrantyId, String note) {
        Warranty w = warrantyDao.findById(warrantyId)
                .orElseThrow(() -> new IllegalArgumentException("Thẻ bảo hành không tồn tại."));
        assertWarrantyAccess(w);
        String oldNote = w.getNote() != null ? w.getNote() : "";
        w.setNote(oldNote + " | " + LocalDate.now() + ": " + note);
        Warranty updated = warrantyDao.update(w);
        auditLogService.log("UPDATE_NOTE", "WARRANTY", updated.getId(), oldNote, updated.getNote());
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

    private boolean canAccessOrder(SaleOrder order) {
        if (CurrentSession.isAdmin()) {
            return true;
        }
        Long sessionBranchId = CurrentSession.currentBranchId();
        Long orderBranchId = order == null || order.getStaff() == null || order.getStaff().getBranch() == null
                ? null
                : order.getStaff().getBranch().getId();
        return sessionBranchId == null || (orderBranchId != null && orderBranchId.equals(sessionBranchId));
    }

    private boolean canAccessWarranty(Warranty warranty) {
        if (CurrentSession.isAdmin()) {
            return true;
        }
        Long sessionBranchId = CurrentSession.currentBranchId();
        Long warrantyBranchId = resolveWarrantyBranchId(warranty);
        return sessionBranchId == null || (warrantyBranchId != null && warrantyBranchId.equals(sessionBranchId));
    }

    private void assertOrderAccess(SaleOrder order) {
        Long branchId = order == null || order.getStaff() == null || order.getStaff().getBranch() == null
                ? null
                : order.getStaff().getBranch().getId();
        String branchName = order == null || order.getStaff() == null || order.getStaff().getBranch() == null
                ? null
                : order.getStaff().getBranch().getBranchName();
        CurrentSession.assertBranchAccess(branchId, branchName);
    }

    private void assertWarrantyAccess(Warranty warranty) {
        CurrentSession.assertBranchAccess(resolveWarrantyBranchId(warranty), resolveWarrantyBranchName(warranty));
    }

    private Long resolveWarrantyBranchId(Warranty warranty) {
        if (warranty == null
                || warranty.getSaleOrderDetail() == null
                || warranty.getSaleOrderDetail().getSaleOrder() == null
                || warranty.getSaleOrderDetail().getSaleOrder().getStaff() == null
                || warranty.getSaleOrderDetail().getSaleOrder().getStaff().getBranch() == null) {
            return null;
        }
        return warranty.getSaleOrderDetail().getSaleOrder().getStaff().getBranch().getId();
    }

    private String resolveWarrantyBranchName(Warranty warranty) {
        if (warranty == null
                || warranty.getSaleOrderDetail() == null
                || warranty.getSaleOrderDetail().getSaleOrder() == null
                || warranty.getSaleOrderDetail().getSaleOrder().getStaff() == null
                || warranty.getSaleOrderDetail().getSaleOrder().getStaff().getBranch() == null) {
            return null;
        }
        return warranty.getSaleOrderDetail().getSaleOrder().getStaff().getBranch().getBranchName();
    }
}
