package vn.edu.ute.carsalesms.controller;

import vn.edu.ute.carsalesms.model.dto.PromotionItem;
import vn.edu.ute.carsalesms.model.dto.PromotionRequest;
import vn.edu.ute.carsalesms.service.AuditLogService;
import vn.edu.ute.carsalesms.service.PromotionService;
import vn.edu.ute.carsalesms.service.impl.NoOpAuditLogService;

import java.util.List;
import java.util.Objects;

/**
 * Controller xử lý luồng thao tác từ View cho đối tượng Khuyến Mãi (F13).
 */
public class PromotionController {

    private final PromotionService promotionService;
    private final AuditLogService auditLogService;

    public PromotionController(PromotionService promotionService) {
        this(promotionService, new NoOpAuditLogService());
    }

    public PromotionController(PromotionService promotionService, AuditLogService auditLogService) {
        this.promotionService = Objects.requireNonNull(promotionService, "promotionService is required");
        this.auditLogService = Objects.requireNonNull(auditLogService, "auditLogService is required");
    }

    public List<PromotionItem> findAll(String keyword) {
        return promotionService.findAll(keyword);
    }

    public void createPromotion(PromotionRequest request) {
        promotionService.createPromotion(request);
        auditLogService.log("CREATE", "PROMOTION", null, null, request.toString());
    }

    public void updatePromotion(Long id, PromotionRequest request) {
        promotionService.updatePromotion(id, request);
        auditLogService.log("UPDATE", "PROMOTION", id, null, request.toString());
    }

    public void toggleStatus(Long id) {
        promotionService.toggleStatus(id);
        auditLogService.log("TOGGLE_STATUS", "PROMOTION", id, null, null);
    }
}
