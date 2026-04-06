package vn.edu.ute.carsalesms.view.component;

import vn.edu.ute.carsalesms.model.dto.TestDriveBookingMetadata;
import vn.edu.ute.carsalesms.model.dto.TestDriveItem;
import vn.edu.ute.carsalesms.model.dto.TestDriveRequest;
import vn.edu.ute.carsalesms.model.enums.TestDriveStatus;
import vn.edu.ute.carsalesms.service.TestDriveService;
import vn.edu.ute.carsalesms.view.theme.DialogUiUtil;
import vn.edu.ute.carsalesms.view.theme.UiPalette;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

/**
 * Lớp TestDrivePanel định nghĩa giao diện người dùng cho chức năng quản lý lịch lái thử.
 * Bao gồm các chức năng như xem danh sách, tìm kiếm, thêm, báo cáo kết quả và hủy lịch lái thử.
 * Kế thừa từ JPanel để tạo một panel giao diện.
 */
public class TestDrivePanel extends JPanel {

    // Định dạng ngày giờ cho việc hiển thị và nhập liệu.
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    // Service để xử lý logic nghiệp vụ liên quan đến lái thử.
    private final TestDriveService testDriveService;

    // Các thành phần giao diện
    private final JTextField txtSearch = new JTextField(20); // Ô nhập liệu để tìm kiếm.
    private final DefaultTableModel tableModel; // Model cho bảng dữ liệu.
    private final JTable table; // Bảng hiển thị danh sách lịch lái thử.
    private List<TestDriveItem> rowData; // Danh sách dữ liệu gốc từ service.

    /**
     * Constructor của TestDrivePanel.
     * @param testDriveService service được inject để xử lý nghiệp vụ.
     */
    public TestDrivePanel(TestDriveService testDriveService) {
        this.testDriveService = testDriveService;

        // Cấu hình layout và giao diện cho panel.
        setLayout(new BorderLayout(8, 8));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Định nghĩa các cột cho bảng.
        String[] cols = {
                "ID", "Mã Phiếu", "Khách Hàng", "Xe Đăng Ký Lái", "Nhân Viên Hỗ Trợ", "Lịch Hẹn", "Trạng Thái Báo Cáo", "Ghi Chú Kết Quả"
        };
        // Khởi tạo table model và cấm chỉnh sửa trực tiếp trên ô.
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);

        // Ẩn cột ID khỏi giao diện nhưng vẫn giữ trong model để xử lý.
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        // Cho phép sắp xếp dữ liệu trên bảng.
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // Thêm thanh công cụ và bảng vào panel.
        add(buildToolbar(), BorderLayout.NORTH);
        add(createTableCard(table), BorderLayout.CENTER);

        // Tải dữ liệu ban đầu.
        refreshData();
    }

    /**
     * Xây dựng thanh công cụ (toolbar) chứa các nút chức năng và ô tìm kiếm.
     * @return một JPanel chứa thanh công cụ.
     */
    private JPanel buildToolbar() {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);

        // Cụm trái: Chức năng tìm kiếm.
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        left.setOpaque(false);
        JButton btnSearch = createActionButton("Tìm");
        btnSearch.addActionListener(e -> refreshData()); // Sự kiện click nút tìm kiếm sẽ tải lại dữ liệu.
        
        left.add(new JLabel("Tra cứu Tên (KH/Xe/Mã):"));
        left.add(txtSearch);
        left.add(btnSearch);

        // Cụm phải: Các nút hành động chính.
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        right.setOpaque(false);

        // Nút "Đặt Lịch Mới"
        JButton btnAdd = createActionButton("Đặt Lịch Mới");
        btnAdd.setBackground(UiPalette.PRIMARY);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.addActionListener(e -> showBookDialog()); // Mở dialog đặt lịch mới.

        // Nút "Chốt Kết Quả Lái"
        JButton btnReport = createActionButton("Chốt Kết Quả Lái");
        btnReport.setBackground(UiPalette.SUCCESS);
        btnReport.setForeground(Color.WHITE);
        btnReport.addActionListener(e -> showReportDialog()); // Mở dialog báo cáo kết quả.

        // Nút "Khách Hủy Lịch"
        JButton btnCancel = createActionButton("Khách Hủy Lịch");
        btnCancel.setBackground(UiPalette.DANGER);
        btnCancel.setForeground(Color.WHITE);
        btnCancel.addActionListener(e -> {
            TestDriveItem item = getSelectedItem(); // Lấy mục đang được chọn.
            if (item != null) {
                // Kiểm tra trạng thái của lịch hẹn.
                if (item.status() == TestDriveStatus.CANCELLED || item.status() == TestDriveStatus.COMPLETED) {
                    showError("Phiếu lái này đã kết thúc, không thể Khách hủy!");
                    return;
                }
                // Yêu cầu nhập lý do hủy.
                String reason = JOptionPane.showInputDialog(getDialogParent(), "Nhập lý do Khách hủy lịch:", "Báo Hủy", JOptionPane.QUESTION_MESSAGE);
                if (reason != null && !reason.trim().isEmpty()) {
                    // Gọi service để hủy lịch.
                    testDriveService.cancelTestDrive(item.id(), "Lý do hủy: " + reason);
                    showInfo("Đã xác nhận hủy lịch.");
                    refreshData(); // Tải lại dữ liệu.
                }
            }
        });

        right.add(btnAdd);
        right.add(btnReport);
        right.add(btnCancel);

        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    /**
     * Tải lại dữ liệu từ service dựa trên từ khóa tìm kiếm và cập nhật lại bảng.
     */
    private void refreshData() {
        try {
            // Gọi service để tìm kiếm dữ liệu.
            rowData = testDriveService.findByKeyword(txtSearch.getText());
            tableModel.setRowCount(0); // Xóa dữ liệu cũ trong bảng.

            // Đổ dữ liệu mới vào bảng.
            for (TestDriveItem i : rowData) {
                tableModel.addRow(new Object[]{
                        i.id(),
                        i.testDriveCode(),
                        i.customerName(),
                        i.carModel(),
                        i.staffName(),
                        i.scheduledTime() != null ? i.scheduledTime().format(DATE_FMT) : "",
                        i.status().name(),
                        i.note() != null ? i.note() : (i.result() != null ? i.result() : "")
                });
            }
        } catch (Exception ex) {
            showError("Lỗi tải DS Hẹn: " + ex.getMessage());
        }
    }

    /**
     * Lấy đối tượng TestDriveItem tương ứng với dòng đang được chọn trong bảng.
     * @return TestDriveItem nếu có dòng được chọn, ngược lại trả về null.
     */
    private TestDriveItem getSelectedItem() {
        int view = table.getSelectedRow(); // Lấy chỉ số dòng đang được chọn trên view.
        if (view < 0) {
            showInfo("Vui lòng chọn 1 hạng mục Lịch hẹn để thao tác.");
            return null;
        }
        int modelIdx = table.convertRowIndexToModel(view); // Chuyển đổi chỉ số view sang model (khi có sắp xếp).
        Long id = (Long) tableModel.getValueAt(modelIdx, 0); // Lấy ID từ model.
        // Tìm đối tượng trong danh sách dữ liệu gốc.
        return rowData.stream().filter(p -> p.id().equals(id)).findFirst().orElse(null);
    }

    /**
     * Hiển thị dialog để đặt lịch lái thử mới.
     */
    private void showBookDialog() {
        // Lấy dữ liệu cần thiết (danh sách khách hàng, xe, nhân viên) để hiển thị trong dialog.
        TestDriveBookingMetadata metadata = testDriveService.getBookingMetadata();
        TestDriveDialog dialog = new TestDriveDialog(getDialogWindow(), metadata);
        dialog.setVisible(true);

        // Xử lý kết quả sau khi dialog đóng.
        dialog.getResult().ifPresent(req -> {
            try {
                // Gọi service để đặt lịch.
                testDriveService.bookTestDrive(req);
                showInfo("Đã lên lịch lái thử thành công.");
                refreshData(); // Tải lại dữ liệu.
            } catch (Exception ex) {
                showError("Lỗi đặt lịch: " + ex.getMessage());
            }
        });
    }

    /**
     * Hiển thị dialog để báo cáo kết quả của một buổi lái thử.
     */
    private void showReportDialog() {
        TestDriveItem item = getSelectedItem(); // Lấy mục được chọn.
        if (item == null) return;
        
        // Chỉ cho phép báo cáo kết quả cho các lịch hẹn đang ở trạng thái "SCHEDULED".
        if (item.status() != TestDriveStatus.SCHEDULED) {
            showError("Chỉ có thể chốt KQ cho những Ca đăng ký Đang Chờ (SCHEDULED).");
            return;
        }

        // Tạo giao diện cho dialog báo cáo.
        JPanel p = new JPanel(new GridLayout(4, 1, 5, 5));
        p.add(new JLabel("Báo Cáo cho Phiếu: " + item.testDriveCode()));
        
        JComboBox<TestDriveStatus> cbStatus = new JComboBox<>(new TestDriveStatus[]{TestDriveStatus.COMPLETED, TestDriveStatus.CANCELLED});
        p.add(cbStatus);
        
        p.add(new JLabel("Đánh giá của Khách hàng:"));
        JTextField txtResult = new JTextField();
        p.add(txtResult);
        
        // Hiển thị dialog và chờ người dùng nhập.
        int r = JOptionPane.showConfirmDialog(getDialogParent(), p, "Ghi nhận Chuyến Lái Thử", JOptionPane.OK_CANCEL_OPTION);
        if (r == JOptionPane.OK_OPTION) {
            try {
                // Gọi service để cập nhật kết quả.
                testDriveService.updateResult(item.id(), (TestDriveStatus) cbStatus.getSelectedItem(), txtResult.getText());
                showInfo("Ghi nhận Ký Kết thành công.");
                refreshData(); // Tải lại dữ liệu.
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        }
    }

    // =========================================================================
    // CÁC PHƯƠNG THỨC HỖ TRỢ GIAO DIỆN (UI HELPER)
    // =========================================================================

    /**
     * Tạo một nút bấm với kiểu dáng chung cho các hành động.
     * @param title tiêu đề của nút.
     * @return một đối tượng JButton đã được định kiểu.
     */
    private JButton createActionButton(String title) {
        JButton btn = new JButton(title);
        btn.setFocusPainted(false);
        btn.setBackground(UiPalette.ACTION_BG);
        btn.setForeground(UiPalette.ACTION_FG);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiPalette.PRIMARY_BORDER),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        return btn;
    }

    /**
     * Tạo một panel kiểu "card" để chứa bảng dữ liệu, có định dạng chung.
     * @param tbl bảng dữ liệu cần đặt vào card.
     * @return một JPanel chứa bảng.
     */
    private JPanel createTableCard(JTable tbl) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UiPalette.SURFACE_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiPalette.BORDER_SOFT),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tbl.setGridColor(UiPalette.BORDER_SOFT);
        tbl.setBackground(UiPalette.TABLE_BACKGROUND);
        tbl.setSelectionBackground(UiPalette.PRIMARY_SOFT);
        tbl.setSelectionForeground(UiPalette.TEXT_PRIMARY);
        tbl.getTableHeader().setBackground(UiPalette.PRIMARY_SOFT);
        tbl.getTableHeader().setForeground(UiPalette.TEXT_PRIMARY);
        tbl.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        tbl.setFillsViewportHeight(true);
        tbl.getTableHeader().setReorderingAllowed(false);
        JScrollPane scroll = new JScrollPane(tbl);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    /**
     * Hiển thị một dialog thông báo lỗi.
     * @param msg nội dung thông báo.
     */
    private void showError(String msg) {
        JOptionPane.showMessageDialog(getDialogParent(), msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Hiển thị một dialog thông tin.
     * @param msg nội dung thông báo.
     */
    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(getDialogParent(), msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Lấy component cha phù hợp để hiển thị dialog.
     * @return component cha.
     */
    private Component getDialogParent() {
        Component owner = DialogUiUtil.appDialogParent(this);
        return owner != null ? owner : this;
    }

    /**
     * Lấy cửa sổ (Window) cha để hiển thị dialog.
     * @return cửa sổ cha.
     */
    private Window getDialogWindow() {
        Component owner = DialogUiUtil.appDialogParent(this);
        if (owner instanceof Window) {
            return (Window) owner;
        }
        return SwingUtilities.getWindowAncestor(owner);
    }

    // =========================================================================
    // DIALOG NỘI BỘ ĐỂ THÊM MỚI LỊCH LÁI THỬ
    // =========================================================================

    /**
     * Lớp TestDriveDialog định nghĩa dialog để người dùng nhập thông tin đặt lịch lái thử mới.
     * Là một lớp static lồng bên trong TestDrivePanel.
     */
    private static class TestDriveDialog extends JDialog {
        // Các thành phần giao diện của dialog.
        private final JComboBox<CustomerComboItem> cbCustomer = new JComboBox<>();
        private final JComboBox<CarComboItem> cbCar = new JComboBox<>();
        private final JComboBox<StaffComboItem> cbStaff = new JComboBox<>();
        private final JTextField txtTime = new JTextField(LocalDateTime.now().plusDays(1).format(DATE_FMT));
        private final JTextArea txtDesc = new JTextArea();

        // Kết quả trả về từ dialog.
        private TestDriveRequest result;

        /**
         * Constructor của TestDriveDialog.
         * @param owner cửa sổ cha.
         * @param metadata dữ liệu cần thiết để khởi tạo các combobox.
         */
        public TestDriveDialog(Window owner, TestDriveBookingMetadata metadata) {
            super(owner, "Đăng Ký Hệ Thống Lái Mẫu Xe", ModalityType.APPLICATION_MODAL);

            // Nạp dữ liệu vào các combobox.
            metadata.customers().forEach(c -> cbCustomer.addItem(new CustomerComboItem(c.id(), c.displayName())));
            metadata.cars().forEach(c -> cbCar.addItem(new CarComboItem(c.id(), c.displayName())));
            metadata.staffs().forEach(s -> cbStaff.addItem(new StaffComboItem(s.id(), s.displayName())));

            // Xây dựng form nhập liệu.
            JPanel form = new JPanel(new GridLayout(5, 2, 10, 10));
            form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            form.add(new JLabel("Chọn Khách Hàng *")); form.add(cbCustomer);
            form.add(new JLabel("Xe Muốn Lái Thử *")); form.add(cbCar);
            form.add(new JLabel("Nhân Viên Chăm Sóc *")); form.add(cbStaff);
            form.add(new JLabel("Lịch Hẹn (dd/MM/yyyy HH:mm) *")); form.add(txtTime);
            form.add(new JLabel("Ghi chú bổ sung:")); form.add(new JScrollPane(txtDesc));

            // Xây dựng panel chứa các nút hành động.
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            JButton btnCancel = new JButton("Đóng");
            JButton btnSubmit = new JButton("Chốt Kèo Lái");
            btnSubmit.setBackground(UiPalette.WARNING);
            btnSubmit.setForeground(Color.BLACK);

            btnCancel.addActionListener(e -> dispose()); // Đóng dialog.
            btnSubmit.addActionListener(e -> {
                try {
                    // Lấy thông tin từ các trường nhập liệu.
                    CustomerComboItem cust = (CustomerComboItem) cbCustomer.getSelectedItem();
                    CarComboItem car = (CarComboItem) cbCar.getSelectedItem();
                    StaffComboItem staff = (StaffComboItem) cbStaff.getSelectedItem();
                    
                    if (cust == null || car == null || staff == null) {
                        throw new RuntimeException("Vui lòng chọn đầy đủ Khách, Xe, Và Nhân Viên");
                    }
                    
                    // Chuyển đổi chuỗi thời gian sang đối tượng LocalDateTime.
                    LocalDateTime time = LocalDateTime.parse(txtTime.getText().trim(), DATE_FMT);
                    
                    // Tạo đối tượng request và đóng dialog.
                    result = new TestDriveRequest(cust.id, car.id, staff.id, time, txtDesc.getText());
                    dispose();
                } catch (DateTimeParseException dte) {
                    JOptionPane.showMessageDialog(DialogUiUtil.appDialogParent(this), "Định dạng lịch báo không khớp chuẩn (VD: 30/04/2026 14:00)", "Cảnh Báo AI", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(DialogUiUtil.appDialogParent(this), "Lỗi Input: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            });

            actions.add(btnSubmit);
            actions.add(btnCancel);

            // Bố cục dialog.
            setLayout(new BorderLayout());
            add(form, BorderLayout.CENTER);
            add(actions, BorderLayout.SOUTH);

            setSize(500, 380);
            setLocationRelativeTo(DialogUiUtil.appDialogParent(owner));
        }

        /**
         * Trả về kết quả sau khi dialog đóng.
         * @return Optional chứa TestDriveRequest nếu người dùng submit, ngược lại là Optional rỗng.
         */
        public Optional<TestDriveRequest> getResult() {
            return Optional.ofNullable(result);
        }
        
        // Các lớp record nội bộ để bọc ID và tên cho các mục trong ComboBox.
        // Giúp dễ dàng lấy ID khi người dùng chọn một mục.
        private record CustomerComboItem(Long id, String name) {
            @Override public String toString() { return name; }
        }
        private record CarComboItem(Long id, String name) {
            @Override public String toString() { return name; }
        }
        private record StaffComboItem(Long id, String name) {
            @Override public String toString() { return name; }
        }
    }
}
