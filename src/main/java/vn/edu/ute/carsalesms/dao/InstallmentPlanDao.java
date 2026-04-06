package vn.edu.ute.carsalesms.dao;

import vn.edu.ute.carsalesms.model.entity.InstallmentPlan;

import java.util.List;
import java.util.Optional;

/**
 * Giao diện DAO (Data Access Object) cho thực thể Kế hoạch trả góp (InstallmentPlan).
 * Mỗi bản ghi trong bảng này đại diện cho một kỳ hạn thanh toán trong một hợp đồng trả góp.
 */
public interface InstallmentPlanDao {

    /**
     * Lấy danh sách tất cả các kỳ hạn trả góp thuộc về một đơn hàng cụ thể.
     *
     * @param orderId ID của đơn hàng (SaleOrder).
     * @return Danh sách các đối tượng InstallmentPlan.
     */
    List<InstallmentPlan> findByOrderId(Long orderId);

    /**
     * Tìm một kỳ hạn trả góp cụ thể dựa trên ID của nó.
     *
     * @param planId ID của kỳ hạn trả góp cần tìm.
     * @return Một Optional chứa đối tượng InstallmentPlan nếu tìm thấy, ngược lại là Optional rỗng.
     */
    Optional<InstallmentPlan> findById(Long planId);

    /**
     * Cập nhật thông tin của một kỳ hạn trả góp.
     * Thường được sử dụng sau khi khách hàng thực hiện thanh toán cho kỳ hạn đó.
     *
     * @param plan Đối tượng InstallmentPlan chứa thông tin cần cập nhật.
     * @return Đối tượng InstallmentPlan sau khi đã được cập nhật.
     */
    InstallmentPlan update(InstallmentPlan plan);

    /**
     * Lưu một danh sách các kỳ hạn trả góp vào cơ sở dữ liệu.
     * Thường được sử dụng khi tạo mới một hợp đồng trả góp cho đơn hàng.
     *
     * @param plans Danh sách các đối tượng InstallmentPlan cần lưu.
     */
    void saveAll(List<InstallmentPlan> plans);
}
