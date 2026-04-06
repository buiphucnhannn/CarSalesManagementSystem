package vn.edu.ute.carsalesms.controller;

import vn.edu.ute.carsalesms.model.dto.PromotionItem;
import vn.edu.ute.carsalesms.model.dto.PromotionRequest;
import vn.edu.ute.carsalesms.service.AuditLogService;
import vn.edu.ute.carsalesms.service.NoOpAuditLogService;
import vn.edu.ute.carsalesms.service.PromotionService;

import java.util.List;
import java.util.Objects;

/**
 * PromotionController xử lý các yêu cầu liên quan đến khuyến mãi.
 * Nó tuân theo Nguyên tắc Trách nhiệm Đơn lẻ (SRP) bằng cách chỉ tập trung vào logic khuyến mãi.
 * Nó cũng tuân theo Nguyên tắc Đảo ngược Phụ thuộc (DIP) bằng cách phụ thuộc vào các giao diện
 * (PromotionService, AuditLogService) thay vì các triển khai cụ thể.
 */
public class PromotionController {

    private final PromotionService promotionService;
    private final AuditLogService auditLogService;

    /**
     * Xây dựng một PromotionController mới với PromotionService đã cho.
     * @param promotionService dịch vụ sẽ được sử dụng để quản lý khuyến mãi.
     */
    public PromotionController(PromotionService promotionService) {
        this(promotionService, new NoOpAuditLogService());
    }

    /**
     * Xây dựng một PromotionController mới với PromotionService và AuditLogService đã cho.
     * @param promotionService dịch vụ sẽ được sử dụng để quản lý khuyến mãi.
     * @param auditLogService dịch vụ sẽ được sử dụng để ghi lại các hành động.
     */
    public PromotionController(PromotionService promotionService, AuditLogService auditLogService) {
        this.promotionService = Objects.requireNonNull(promotionService, "promotionService is required");
        this.auditLogService = Objects.requireNonNull(auditLogService, "auditLogService is required");
    }

    /**
     * Tìm tất cả các khuyến mãi khớp với từ khóa đã cho.
     * @param keyword từ khóa để tìm kiếm.
     * @return danh sách các mục khuyến mãi.
     */
    public List<PromotionItem> findAll(String keyword) {
        return promotionService.findAll(keyword);
    }

    /**
     * Tạo một khuyến mãi mới.
     * @param request yêu cầu khuyến mãi.
     */
    public void createPromotion(PromotionRequest request) {
        promotionService.createPromotion(request);
        auditLogService.log("CREATE", "PROMOTION", null, null, request.toString());
    }

    /**
     * Cập nhật một khuyến mãi hiện có.
     * @param id ID của khuyến mãi cần cập nhật.
     * @param request yêu cầu khuyến mãi.
     */
    public void updatePromotion(Long id, PromotionRequest request) {
        promotionService.updatePromotion(id, request);
        auditLogService.log("UPDATE", "PROMOTION", id, null, request.toString());
    }

    /**
     * Chuyển đổi trạng thái của một khuyến mãi.
     * @param id ID của khuyến mãi cần chuyển đổi.
     */
    public void toggleStatus(Long id) {
        promotionService.toggleStatus(id);
        auditLogService.log("TOGGLE_STATUS", "PROMOTION", id, null, null);
    }
}
