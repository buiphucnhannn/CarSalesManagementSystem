package vn.edu.ute.carsalesms.dao;

import vn.edu.ute.carsalesms.model.entity.Branch;
import vn.edu.ute.carsalesms.model.enums.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Giao diện DAO (Data Access Object) cho thực thể Chi nhánh (Branch).
 * Cung cấp các phương thức trừu tượng để truy cập và thao tác dữ liệu liên quan đến chi nhánh.
 */
public interface BranchDao {

    /**
     * Tìm kiếm và trả về danh sách các chi nhánh dựa trên từ khóa và bộ lọc trạng thái.
     * @param keyword Từ khóa tìm kiếm (có thể là tên, mã, địa chỉ chi nhánh).
     * @param statusFilter Lọc theo trạng thái (ACTIVE, INACTIVE).
     * @return Danh sách các đối tượng Branch phù hợp.
     */
    List<Branch> findBranches(String keyword, Status statusFilter);

    /**
     * Tìm một chi nhánh dựa trên ID của nó.
     * @param id ID của chi nhánh cần tìm.
     * @return Một Optional chứa đối tượng Branch nếu tìm thấy, ngược lại là Optional rỗng.
     */
    Optional<Branch> findById(Long id);

    /**
     * Tìm một chi nhánh dựa trên mã chi nhánh (branchCode).
     * @param branchCode Mã định danh duy nhất của chi nhánh.
     * @return Một Optional chứa đối tượng Branch nếu tìm thấy.
     */
    Optional<Branch> findByCode(String branchCode);

    /**
     * Lưu (thêm mới hoặc cập nhật) thông tin của một chi nhánh.
     * @param branch Đối tượng Branch cần lưu.
     * @return Đối tượng Branch sau khi đã được lưu.
     */
    Branch save(Branch branch);

    /**
     * Đếm số lượng nhân viên đang hoạt động tại một chi nhánh cụ thể.
     * @param branchId ID của chi nhánh.
     * @return Số lượng nhân viên đang hoạt động.
     */
    long countActiveStaffByBranchId(Long branchId);

    /**
     * Đếm số lượng xe đang có sẵn (hoạt động) tại một chi nhánh cụ thể.
     * @param branchId ID của chi nhánh.
     * @return Số lượng xe đang hoạt động.
     */
    long countActiveCarsByBranchId(Long branchId);

    /**
     * Lấy dữ liệu thô cho báo cáo doanh số bán hàng theo từng chi nhánh trong một khoảng thời gian.
     * Mỗi phần tử trong danh sách là một mảng Object, chứa các thông tin tổng hợp như doanh thu, số lượng xe bán được.
     * @param fromInclusive Thời điểm bắt đầu (bao gồm).
     * @param toExclusive Thời điểm kết thúc (không bao gồm).
     * @param statusFilter Lọc báo cáo theo trạng thái của chi nhánh.
     * @return Danh sách các hàng dữ liệu báo cáo.
     */
    List<Object[]> findBranchSalesReportRows(LocalDateTime fromInclusive, LocalDateTime toExclusive, Status statusFilter);
}
