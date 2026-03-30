package vn.edu.ute.carsalesms.service;

import vn.edu.ute.carsalesms.model.dto.PromotionItem;
import vn.edu.ute.carsalesms.model.dto.PromotionRequest;
import vn.edu.ute.carsalesms.model.enums.Status;

import java.util.List;

/**
 * Service xử lý nghiệp vụ Khuyến Mãi (F13).
 */
public interface PromotionService {

    /**
     * Lấy danh sách khuyến mãi để hiển thị với tùy chọn tìm kiếm.
     */
    List<PromotionItem> findAll(String keyword);

    /**
     * Tạo mới khuyến mãi.
     */
    void createPromotion(PromotionRequest request);

    /**
     * Cập nhật khuyến mãi.
     */
    void updatePromotion(Long id, PromotionRequest request);

    /**
     * Đổi trạng thái (Khoá/Mở khoá) của khuyến mãi.
     */
    void toggleStatus(Long id);
}
