package vn.edu.ute.carsalesms.service;

import vn.edu.ute.carsalesms.model.dto.WarrantyItem;

import java.util.List;

public interface WarrantyService {
    List<WarrantyItem> findByKeyword(String keyword);
    
    /**
     * Sự kiện lắng nghe khi Hóa Đơn Trạng Thái PAID -> Đẻ thẻ Bảo hành
     */
    void generateWarrantyForOrder(Long saleOrderId);
    
    /**
     * (Bonus) Nhân viên Gara note lại sửa chữa cho Thẻ
     */
    void addNoteToWarranty(Long warrantyId, String note);
}
