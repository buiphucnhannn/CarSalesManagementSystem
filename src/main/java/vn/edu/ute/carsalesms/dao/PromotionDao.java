package vn.edu.ute.carsalesms.dao;

import vn.edu.ute.carsalesms.model.entity.Promotion;

import java.util.List;
import java.util.Optional;

/**
 * Giao diện DAO (Data Access Object) cho thực thể Khuyến mãi (Promotion).
 * Cung cấp các phương thức để truy cập và quản lý dữ liệu khuyến mãi.
 */
public interface PromotionDao {

    /**
     * Lấy danh sách các chương trình khuyến mãi đang có hiệu lực tại thời điểm hiện tại.
     * Một khuyến mãi được coi là có hiệu lực nếu:
     * 1. Trạng thái (status) là ACTIVE.
     * 2. Ngày hiện tại nằm trong khoảng từ ngày bắt đầu (startDate) đến ngày kết thúc (endDate).
     *
     * @return Danh sách các đối tượng Promotion đang hoạt động.
     */
    List<Promotion> findActivePromotions();

    /**
     * Tìm một chương trình khuyến mãi dựa trên ID của nó.
     *
     * @param id ID của khuyến mãi cần tìm.
     * @return Một Optional chứa đối tượng Promotion nếu tìm thấy, ngược lại là Optional rỗng.
     */
    Optional<Promotion> findById(Long id);

    /**
     * Lấy toàn bộ danh sách các chương trình khuyến mãi đã được tạo trong hệ thống,
     * bao gồm cả những khuyến mãi đang hoạt động, không hoạt động, hoặc đã hết hạn.
     *
     * @return Danh sách tất cả các đối tượng Promotion.
     */
    List<Promotion> findAll();

    /**
     * Lưu một chương trình khuyến mãi mới vào cơ sở dữ liệu.
     *
     * @param promotion Đối tượng Promotion cần lưu.
     * @return Đối tượng Promotion sau khi đã được lưu.
     */
    Promotion save(Promotion promotion);

    /**
     * Cập nhật thông tin của một chương trình khuyến mãi hiện có.
     *
     * @param promotion Đối tượng Promotion chứa thông tin cần cập nhật.
     * @return Đối tượng Promotion sau khi đã được cập nhật.
     */
    Promotion update(Promotion promotion);
}
