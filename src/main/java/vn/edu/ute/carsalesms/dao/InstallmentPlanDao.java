package vn.edu.ute.carsalesms.dao;

import vn.edu.ute.carsalesms.model.entity.InstallmentPlan;

import java.util.List;
import java.util.Optional;

/**
 * Interface DAO cho các kỳ hạn trả góp.
 */
public interface InstallmentPlanDao {

    /**
     * Lấy toàn bộ hợp đồng (các kỳ) trả góp của một đơn bán.
     */
    List<InstallmentPlan> findByOrderId(Long orderId);

    /**
     * Tìm kỳ trả góp theo ID.
     */
    Optional<InstallmentPlan> findById(Long planId);

    /**
     * Cập nhật thông tin của một kỳ trả góp.
     */
    InstallmentPlan update(InstallmentPlan plan);

    /**
     * Lưu hàng loạt kỳ trả góp.
     */
    void saveAll(List<InstallmentPlan> plans);
}
