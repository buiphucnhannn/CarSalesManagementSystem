package vn.edu.ute.carsalesms.dao;

import vn.edu.ute.carsalesms.model.entity.Promotion;

import java.util.List;
import java.util.Optional;

/**
 * Interface DAO cho Promotion.
 */
public interface PromotionDao {

    /**
     * Lấy danh sách khuyến mãi đang còn hiệu lực tại thời điểm hiện tại.
     * Điều kiện: status = ACTIVE và startDate <= today <= endDate.
     *
     * @return danh sách Promotion còn hiệu lực
     */
    List<Promotion> findActivePromotions();

    /**
     * Tìm khuyến mãi theo id.
     */
    Optional<Promotion> findById(Long id);

    /**
     * Lấy toàn bộ danh sách khuyến mãi (cả ACTIVE, INACTIVE, hết hạn).
     */
    List<Promotion> findAll();

    /**
     * Lưu một khuyến mãi mới.
     */
    Promotion save(Promotion promotion);

    /**
     * Cập nhật khuyến mãi.
     */
    Promotion update(Promotion promotion);
}
