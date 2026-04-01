package vn.edu.ute.carsalesms.service.impl;

import vn.edu.ute.carsalesms.dao.PromotionDao;
import vn.edu.ute.carsalesms.model.dto.PromotionItem;
import vn.edu.ute.carsalesms.model.dto.PromotionRequest;
import vn.edu.ute.carsalesms.model.entity.Promotion;
import vn.edu.ute.carsalesms.model.enums.Status;
import vn.edu.ute.carsalesms.service.PromotionService;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class PromotionServiceImpl implements PromotionService {

    private final PromotionDao promotionDao;

    public PromotionServiceImpl(PromotionDao promotionDao) {
        this.promotionDao = promotionDao;
    }

    @Override
    public List<PromotionItem> findAll(String keyword) {
        List<Promotion> list = promotionDao.findAll();

        if (keyword != null && !keyword.trim().isEmpty()) {
            final String kw = keyword.trim().toLowerCase();
            list = list.stream()
                    .filter(p -> p.getPromotionCode().toLowerCase().contains(kw)
                            || p.getPromotionName().toLowerCase().contains(kw))
                    .collect(Collectors.toList());
        }

        return list.stream().map(this::mapToItem).collect(Collectors.toList());
    }

    @Override
    public void createPromotion(PromotionRequest req) {
        validateRequest(req);

        // Kiểm tra xem mã đã tồn tại chưa ở database (Có thể bỏ qua phần này cho mục đích demo ngắn gọn
        // nhưng nên có xử lý catch Duplicate Key từ JPA, hoặc query. Tạm dùng try catch db).
        Promotion p = new Promotion(
                req.promotionCode(), req.promotionName(), req.discountType(), req.discountValue(),
                req.startDate(), req.endDate(), req.description(), req.status()
        );

        promotionDao.save(p);
    }

    @Override
    public void updatePromotion(Long id, PromotionRequest req) {
        validateRequest(req);

        Promotion p = promotionDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chương trình khuyến mãi"));

        p.setPromotionCode(req.promotionCode());
        p.setPromotionName(req.promotionName());
        p.setDiscountType(req.discountType());
        p.setDiscountValue(req.discountValue());
        p.setStartDate(req.startDate());
        p.setEndDate(req.endDate());
        p.setDescription(req.description());
        p.setStatus(req.status());

        promotionDao.update(p);
    }

    @Override
    public void toggleStatus(Long id) {
        Promotion p = promotionDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chương trình khuyến mãi"));
        if (p.getStatus() == Status.ACTIVE) {
            p.setStatus(Status.INACTIVE);
        } else {
            p.setStatus(Status.ACTIVE);
        }
        promotionDao.update(p);
    }

    private void validateRequest(PromotionRequest req) {
        if (req.promotionCode() == null || req.promotionCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khuyến mãi không được trống.");
        }
        if (req.promotionName() == null || req.promotionName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên khuyến mãi không được trống.");
        }
        if (req.startDate() == null || req.endDate() == null) {
            throw new IllegalArgumentException("Ngày áp dụng không hợp lệ.");
        }
        if (req.startDate().isAfter(req.endDate())) {
            throw new IllegalArgumentException("Ngày bắt đầu không được lớn hơn ngày kết thúc.");
        }
        if (req.discountValue() == null || req.discountValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá trị chiết khấu phải lớn hơn hoặc bằng 0.");
        }
    }

    private PromotionItem mapToItem(Promotion p) {
        return new PromotionItem(
                p.getId(), p.getPromotionCode(), p.getPromotionName(), p.getDiscountType(),
                p.getDiscountValue(), p.getStartDate(), p.getEndDate(), p.getDescription(), p.getStatus()
        );
    }
}
