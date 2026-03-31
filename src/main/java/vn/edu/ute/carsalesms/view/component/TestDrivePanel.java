package vn.edu.ute.carsalesms.view.component;

import vn.edu.ute.carsalesms.dao.CarDao;
import vn.edu.ute.carsalesms.dao.CustomerDao;
import vn.edu.ute.carsalesms.dao.StaffDao;
import vn.edu.ute.carsalesms.model.dto.TestDriveItem;
import vn.edu.ute.carsalesms.model.dto.TestDriveRequest;
import vn.edu.ute.carsalesms.model.entity.Car;
import vn.edu.ute.carsalesms.model.entity.Customer;
import vn.edu.ute.carsalesms.model.entity.Staff;
import vn.edu.ute.carsalesms.model.enums.TestDriveStatus;
import vn.edu.ute.carsalesms.service.TestDriveService;
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

public class TestDrivePanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    private final TestDriveService testDriveService;
    private final CustomerDao customerDao;
    private final CarDao carDao;
    private final StaffDao staffDao;

    private final JTextField txtSearch = new JTextField(20);
    private final DefaultTableModel tableModel;
    private final JTable table;
    private List<TestDriveItem> rowData;

    public TestDrivePanel(TestDriveService testDriveService, CustomerDao customerDao, CarDao carDao, StaffDao staffDao) {
        this.testDriveService = testDriveService;
        this.customerDao = customerDao;
        this.carDao = carDao;
        this.staffDao = staffDao;

        setLayout(new BorderLayout(8, 8));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        String[] cols = {
                "ID", "Mã Phiếu", "Khách Hàng", "Xe Đăng Ký Lái", "Nhân Viên Hỗ Trợ", "Lịch Hẹn", "Trạng Thái Báo Cáo", "Ghi Chú Kết Quả"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);

        // Ẩn ID (Column 0)
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        add(buildToolbar(), BorderLayout.NORTH);
        add(createTableCard(table), BorderLayout.CENTER);

        refreshData();
    }

    private JPanel buildToolbar() {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);

        // Cụm trái: Tìm kiếm
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        left.setOpaque(false);
        JButton btnSearch = createActionButton("Tìm");
        btnSearch.addActionListener(e -> refreshData());
        
        left.add(new JLabel("Tra cứu Tên (KH/Xe/Mã):"));
        left.add(txtSearch);
        left.add(btnSearch);

        // Cụm phải: Đặt lịch, Báo cáo KQ, Hủy lịch
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        right.setOpaque(false);

        JButton btnAdd = createActionButton("Đặt Lịch Mới");
        btnAdd.setBackground(UiPalette.PRIMARY);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.addActionListener(e -> showBookDialog());

        JButton btnReport = createActionButton("Chốt Kết Quả Lái");
        btnReport.setBackground(UiPalette.SUCCESS);
        btnReport.setForeground(Color.WHITE);
        btnReport.addActionListener(e -> showReportDialog());

        JButton btnCancel = createActionButton("Khách Hủy Lịch");
        btnCancel.setBackground(UiPalette.DANGER);
        btnCancel.setForeground(Color.WHITE);
        btnCancel.addActionListener(e -> {
            TestDriveItem item = getSelectedItem();
            if (item != null) {
                if (item.status() == TestDriveStatus.CANCELLED || item.status() == TestDriveStatus.COMPLETED) {
                    showError("Phiếu lái này đã kết thúc, không thể Khách hủy!");
                    return;
                }
                String reason = JOptionPane.showInputDialog(this, "Nhập lý do Khách hủy lịch:", "Báo Hủy", JOptionPane.QUESTION_MESSAGE);
                if (reason != null && !reason.trim().isEmpty()) {
                    testDriveService.cancelTestDrive(item.id(), "Lý do hủy: " + reason);
                    showInfo("Đã xác nhận hủy lịch.");
                    refreshData();
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

    private void refreshData() {
        try {
            rowData = testDriveService.findByKeyword(txtSearch.getText());
            tableModel.setRowCount(0);

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

    private TestDriveItem getSelectedItem() {
        int view = table.getSelectedRow();
        if (view < 0) {
            showInfo("Vui lòng chọn 1 hạng mục Lịch hẹn để thao tác.");
            return null;
        }
        int modelIdx = table.convertRowIndexToModel(view);
        Long id = (Long) tableModel.getValueAt(modelIdx, 0);
        return rowData.stream().filter(p -> p.id().equals(id)).findFirst().orElse(null);
    }

    private void showBookDialog() {
        TestDriveDialog dialog = new TestDriveDialog(SwingUtilities.getWindowAncestor(this), customerDao, carDao, staffDao);
        dialog.setVisible(true);

        dialog.getResult().ifPresent(req -> {
            try {
                testDriveService.bookTestDrive(req);
                showInfo("Đã lên lịch lái thử thành công.");
                refreshData();
            } catch (Exception ex) {
                showError("Lỗi đặt lịch: " + ex.getMessage());
            }
        });
    }

    private void showReportDialog() {
        TestDriveItem item = getSelectedItem();
        if (item == null) return;
        
        if (item.status() != TestDriveStatus.SCHEDULED) {
            showError("Chỉ có thể chốt KQ cho những Ca đăng ký Đang Chờ (SCHEDULED).");
            return;
        }

        JPanel p = new JPanel(new GridLayout(4, 1, 5, 5));
        p.add(new JLabel("Báo Cáo cho Phiếu: " + item.testDriveCode()));
        
        JComboBox<TestDriveStatus> cbStatus = new JComboBox<>(new TestDriveStatus[]{TestDriveStatus.COMPLETED, TestDriveStatus.CANCELLED});
        p.add(cbStatus);
        
        p.add(new JLabel("Đánh giá của Khách hàng:"));
        JTextField txtResult = new JTextField();
        p.add(txtResult);
        
        int r = JOptionPane.showConfirmDialog(this, p, "Ghi nhận Chuyến Lái Thử", JOptionPane.OK_CANCEL_OPTION);
        if (r == JOptionPane.OK_OPTION) {
            try {
                testDriveService.updateResult(item.id(), (TestDriveStatus) cbStatus.getSelectedItem(), txtResult.getText());
                showInfo("Ghi nhận Ký Kết thành công.");
                refreshData();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        }
    }

    // =========================================================================
    // UI HELPER
    // =========================================================================

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

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    // =========================================================================
    // DIALOG THÊM MỚI LỊCH LÁI THỬ (JCOMBOBOX SELECT)
    // =========================================================================

    private static class TestDriveDialog extends JDialog {
        private final JComboBox<CustomerComboItem> cbCustomer = new JComboBox<>();
        private final JComboBox<CarComboItem> cbCar = new JComboBox<>();
        private final JComboBox<StaffComboItem> cbStaff = new JComboBox<>();
        private final JTextField txtTime = new JTextField(LocalDateTime.now().plusDays(1).format(DATE_FMT));
        private final JTextArea txtDesc = new JTextArea();

        private TestDriveRequest result;

        public TestDriveDialog(Window owner, CustomerDao customerDao, CarDao carDao, StaffDao staffDao) {
            super(owner, "Đăng Ký Hệ Thống Lái Mẫu Xe", ModalityType.APPLICATION_MODAL);

            // Load Data
            customerDao.findCustomers(null).forEach(c -> cbCustomer.addItem(new CustomerComboItem(c)));
            carDao.findCars(null, null).forEach(c -> cbCar.addItem(new CarComboItem(c)));
            staffDao.findStaffs(null, null).forEach(s -> cbStaff.addItem(new StaffComboItem(s)));

            JPanel form = new JPanel(new GridLayout(5, 2, 10, 10));
            form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            form.add(new JLabel("Chọn Khách Hàng *")); form.add(cbCustomer);
            form.add(new JLabel("Xe Muốn Lái Thử *")); form.add(cbCar);
            form.add(new JLabel("Nhân Viên Chăm Sóc *")); form.add(cbStaff);
            form.add(new JLabel("Lịch Hẹn (dd/MM/yyyy HH:mm) *")); form.add(txtTime);
            form.add(new JLabel("Ghi chú bổ sung:")); form.add(new JScrollPane(txtDesc));

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            JButton btnCancel = new JButton("Đóng");
            JButton btnSubmit = new JButton("Chốt Kèo Lái");
            btnSubmit.setBackground(UiPalette.WARNING);
            btnSubmit.setForeground(Color.BLACK);

            btnCancel.addActionListener(e -> dispose());
            btnSubmit.addActionListener(e -> {
                try {
                    CustomerComboItem cust = (CustomerComboItem) cbCustomer.getSelectedItem();
                    CarComboItem car = (CarComboItem) cbCar.getSelectedItem();
                    StaffComboItem staff = (StaffComboItem) cbStaff.getSelectedItem();
                    
                    if (cust == null || car == null || staff == null) {
                        throw new RuntimeException("Vui lòng chọn đầy đủ Khách, Xe, Và Nhân Viên");
                    }
                    
                    LocalDateTime time = LocalDateTime.parse(txtTime.getText().trim(), DATE_FMT);
                    
                    result = new TestDriveRequest(cust.id, car.id, staff.id, time, txtDesc.getText());
                    dispose();
                } catch (DateTimeParseException dte) {
                    JOptionPane.showMessageDialog(this, "Định dạng lịch báo không khớp chuẩn (VD: 30/04/2026 14:00)", "Cảnh Báo AI", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi Input: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            });

            actions.add(btnCancel);
            actions.add(btnSubmit);

            setLayout(new BorderLayout());
            add(form, BorderLayout.CENTER);
            add(actions, BorderLayout.SOUTH);

            setSize(500, 380);
            setLocationRelativeTo(owner);
        }

        public Optional<TestDriveRequest> getResult() {
            return Optional.ofNullable(result);
        }
        
        // Vài class bọc ID cho Combobox
        private record CustomerComboItem(Long id, String name) {
            public CustomerComboItem(Customer c) { this(c.getId(), c.getFullName() + " (" + c.getPhone() + ")"); }
            @Override public String toString() { return name; }
        }
        private record CarComboItem(Long id, String name) {
            public CarComboItem(Car c) { this(c.getId(), c.getCarName() + " - " + c.getColor()); }
            @Override public String toString() { return name; }
        }
        private record StaffComboItem(Long id, String name) {
            public StaffComboItem(Staff s) { this(s.getId(), s.getFullName() + " (MS:" + s.getStaffCode() + ")"); }
            @Override public String toString() { return name; }
        }
    }
}
