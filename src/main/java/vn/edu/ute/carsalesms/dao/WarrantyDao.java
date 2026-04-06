package vn.edu.ute.carsalesms.dao;

import vn.edu.ute.carsalesms.model.entity.Warranty;

import java.util.List;
import java.util.Optional;

/**
 * Giao diện DAO (Data Access Object) cho thực thể Bảo hành (Warranty).
 * Định nghĩa các phương thức để truy cập và quản lý thông tin bảo hành trong hệ thống.
 */
public interface WarrantyDao {
    /**
     * Lấy tất cả các bản ghi bảo hành hiện có trong hệ thống.
     * @return Danh sách tất cả các đối tượng Warranty.
     */
    List<Warranty> findAll();

    /**
     * Tìm kiếm các bản ghi bảo hành dựa trên từ khóa.
     * Từ khóa có thể là mã phiếu bảo hành, biển kiểm soát/số khung xe, hoặc tên khách hàng.
     * @param keyword Từ khóa tìm kiếm.
     * @return Danh sách các đối tượng Warranty phù hợp với từ khóa.
     */
    List<Warranty> findByKeyword(String keyword);

    /**
     * Tìm một bản ghi bảo hành dựa trên ID của nó.
     * @param id ID của bản ghi bảo hành cần tìm.
     * @return Một Optional chứa đối tượng Warranty nếu tìm thấy, ngược lại là Optional rỗng.
     */
    Optional<Warranty> findById(Long id);

    /**
     * Tìm một bản ghi bảo hành dựa trên ID của chi tiết đơn hàng bán (SaleOrderDetail).
     * @param detailId ID của chi tiết đơn hàng bán.
     * @return Một Optional chứa đối tượng Warranty nếu tìm thấy.
     */
    Optional<Warranty> findBySaleOrderDetailId(Long detailId);

    /**
     * Lưu một bản ghi bảo hành mới vào cơ sở dữ liệu.
     * @param warranty Đối tượng Warranty cần lưu.
     * @return Đối tượng Warranty sau khi đã được lưu.
     */
    Warranty save(Warranty warranty);

    /**
     * Cập nhật thông tin của một bản ghi bảo hành hiện có.
     * @param warranty Đối tượng Warranty cần cập nhật.
     * @return Đối tượng Warranty sau khi đã được cập nhật.
     */
    Warranty update(Warranty warranty);
}
