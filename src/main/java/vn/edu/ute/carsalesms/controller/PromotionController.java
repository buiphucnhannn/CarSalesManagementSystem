package vn.edu.ute.carsalesms.controller;

import vn.edu.ute.carsalesms.model.dto.PromotionItem;
import vn.edu.ute.carsalesms.model.dto.PromotionRequest;
import vn.edu.ute.carsalesms.service.PromotionService;

import java.util.List;

/**
 * Controller xử lý luồng thao tác từ View cho đối tượng Khuyến Mãi (F13).
 */
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    public List<PromotionItem> findAll(String keyword) {
        return promotionService.findAll(keyword);
    }

    public void createPromotion(PromotionRequest request) {
        promotionService.createPromotion(request);
    }

    public void updatePromotion(Long id, PromotionRequest request) {
        promotionService.updatePromotion(id, request);
    }

    public void toggleStatus(Long id) {
        promotionService.toggleStatus(id);
    }
}
