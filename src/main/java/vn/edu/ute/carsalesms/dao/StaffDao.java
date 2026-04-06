package vn.edu.ute.carsalesms.dao;

import vn.edu.ute.carsalesms.model.entity.Account;
import vn.edu.ute.carsalesms.model.entity.Branch;
import vn.edu.ute.carsalesms.model.entity.Staff;
import vn.edu.ute.carsalesms.model.enums.Status;

import java.util.List;
import java.util.Optional;

/**
 * Giao diện DAO (Data Access Object) cho các thực thể Nhân viên (Staff) và Tài khoản (Account).
 * Do Staff và Account có mối quan hệ 1-1 chặt chẽ, việc quản lý chúng trong cùng một DAO giúp đơn giản hóa thao tác dữ liệu.
 * Giao diện này tuân thủ Nguyên tắc Trách nhiệm đơn lẻ (Single Responsibility Principle) ở mức phương thức,
 * mỗi phương thức đảm nhiệm một nhiệm vụ truy cập dữ liệu cụ thể.
 */
public interface StaffDao {

    // ─── Các phương thức liên quan đến Nhân viên (Staff) ──────────────────────────────────

    /**
     * Tìm kiếm và trả về danh sách nhân viên dựa trên từ khóa và bộ lọc trạng thái.
     *
     * @param keyword      Từ khóa để tìm kiếm trong mã nhân viên, tên, hoặc email. Nếu null, không tìm theo từ khóa.
     * @param statusFilter Lọc danh sách theo trạng thái (ví dụ: ACTIVE, INACTIVE). Nếu null, trả về tất cả trạng thái.
     * @return Danh sách các đối tượng Staff, bao gồm thông tin Branch và Account đã được tải (eager-fetch).
     */
    List<Staff> findStaffs(String keyword, Status statusFilter);

    /**
     * Lấy danh sách các nhân viên đang ở trạng thái hoạt động (ACTIVE) nhưng chưa được cấp tài khoản đăng nhập.
     * Thường được sử dụng để điền vào các lựa chọn (combobox) khi tạo tài khoản mới.
     * @return Danh sách các đối tượng Staff.
     */
    List<Staff> findActiveStaffsWithoutAccount();

    /**
     * Tìm một nhân viên dựa trên ID.
     * @param id ID của nhân viên cần tìm.
     * @return Một Optional chứa đối tượng Staff nếu tìm thấy, ngược lại là Optional rỗng.
     */
    Optional<Staff> findStaffById(Long id);

    /**
     * Tìm một nhân viên dựa trên mã nhân viên (staffCode).
     * @param staffCode Mã định danh duy nhất của nhân viên.
     * @return Một Optional chứa đối tượng Staff nếu tìm thấy.
     */
    Optional<Staff> findStaffByCode(String staffCode);

    /**
     * Lưu (thêm mới hoặc cập nhật) thông tin của một nhân viên.
     *
     * @param staff Đối tượng Staff cần lưu.
     * @return Đối tượng Staff sau khi đã được lưu vào cơ sở dữ liệu (có thể chứa ID và các giá trị được tạo tự động).
     */
    Staff saveStaff(Staff staff);

    // ─── Các phương thức tra cứu Chi nhánh (Branch) ───────────────────────────────────────

    /**
     * Trả về danh sách các chi nhánh đang hoạt động.
     * Dữ liệu này thường được dùng để điền vào các thành phần UI như ComboBox.
     * @return Danh sách các đối tượng Branch.
     */
    List<Branch> findActiveBranches();

    /**
     * Tìm một chi nhánh dựa trên ID.
     * @param id ID của chi nhánh cần tìm.
     * @return Một Optional chứa đối tượng Branch nếu tìm thấy.
     */
    Optional<Branch> findBranchById(Long id);

    // ─── Các phương thức liên quan đến Tài khoản (Account) ────────────────────────────────

    /**
     * Tìm kiếm và trả về danh sách tài khoản cùng với thông tin nhân viên liên quan.
     *
     * @param keyword Từ khóa để tìm kiếm trong tên đăng nhập, mã nhân viên, hoặc tên nhân viên. Nếu null, không tìm theo từ khóa.
     * @return Danh sách các đối tượng Account, bao gồm thông tin Staff đã được tải (eager-fetch).
     */
    List<Account> findAccounts(String keyword);

    /**
     * Tìm một tài khoản dựa trên ID.
     * @param id ID của tài khoản cần tìm.
     * @return Một Optional chứa đối tượng Account nếu tìm thấy.
     */
    Optional<Account> findAccountById(Long id);

    /**
     * Tìm một tài khoản dựa trên tên đăng nhập (username).
     * @param username Tên đăng nhập cần tìm.
     * @return Một Optional chứa đối tượng Account nếu tìm thấy.
     */
    Optional<Account> findAccountByUsername(String username);

    /**
     * Lưu (thêm mới hoặc cập nhật) thông tin của một tài khoản.
     *
     * @param account Đối tượng Account cần lưu.
     * @return Đối tượng Account sau khi đã được lưu.
     */
    Account saveAccount(Account account);

    /**
     * Xóa một tài khoản khỏi cơ sở dữ liệu dựa trên ID.
     *
     * @param id ID của tài khoản cần xóa.
     */
    void deleteAccountById(Long id);
}
