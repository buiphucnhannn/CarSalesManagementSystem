package vn.edu.ute.carsalesms.view.component;

import vn.edu.ute.carsalesms.controller.BranchManagementController;
import vn.edu.ute.carsalesms.model.dto.BranchCommandRequest;
import vn.edu.ute.carsalesms.model.dto.BranchItem;
import vn.edu.ute.carsalesms.model.dto.BranchSalesReportItem;
import vn.edu.ute.carsalesms.model.enums.Status;
import vn.edu.ute.carsalesms.view.theme.DialogUiUtil;
import vn.edu.ute.carsalesms.view.theme.UiPalette;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Lớp BranchManagementPanel là giao diện người dùng chính cho việc quản lý chi nhánh.
 * Nó chứa hai tab chính: một để quản lý thông tin chi nhánh và một để xem báo cáo bán hàng theo chi nhánh.
 * Lớp này sử dụng BranchManagementController để tương tác với logic nghiệp vụ và xử lý dữ liệu.
 */
public class BranchManagementPanel extends JPanel {

    // Định dạng ngày và giờ được sử dụng trong toàn bộ panel.
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final BranchManagementController controller;

    private final BranchTabPanel branchTabPanel;
    private final ReportTabPanel reportTabPanel;

    /**
     * Constructor chính, khởi tạo panel quản lý chi nhánh.
     * @param controller Controller để xử lý các hoạt động liên quan đến chi nhánh.
     */
    public BranchManagementPanel(BranchManagementController controller) {
        this.controller = Objects.requireNonNull(controller, "controller is required");

        setLayout(new BorderLayout());
        setOpaque(false);

        // Khởi tạo hai tab con
        branchTabPanel = new BranchTabPanel();
        reportTabPanel = new ReportTabPanel();

        // Tạo JTabbedPane để chứa các tab
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Chi nhánh", branchTabPanel);
        tabs.addTab("Báo cáo theo chi nhánh", reportTabPanel);
        // Thêm listener để làm mới dữ liệu khi chuyển tab
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 0) {
                branchTabPanel.refreshData();
            } else {
                reportTabPanel.refreshData();
            }
        });

        add(tabs, BorderLayout.CENTER);
    }

    /**
     * Tạo một JButton với kiểu dáng chung cho các hành động chính.
     * @param title Tiêu đề của nút.
     * @return một JButton đã được định kiểu.
     */
    private JButton createActionButton(String title) {
        JButton btn = new JButton(title);
        btn.setFocusPainted(false);
        btn.setBackground(UiPalette.ACTION_BG);
        btn.setForeground(UiPalette.ACTION_FG);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiPalette.PRIMARY_BORDER),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return btn;
    }

    /**
     * Tạo một JButton với kiểu dáng cho các hành động nguy hiểm (ví dụ: xóa, ngừng hoạt động).
     * @param title Tiêu đề của nút.
     * @return một JButton đã được định kiểu nguy hiểm.
     */
    private JButton createDangerButton(String title) {
        JButton btn = createActionButton(title);
        btn.setForeground(UiPalette.DANGER);
        return btn;
    }

    /**
     * Tạo một panel chứa JTable với kiểu dáng chung.
     * @param table Bảng để đặt bên trong panel.
     * @return một JPanel chứa bảng.
     */
    private JPanel createTableCard(JTable table) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UiPalette.SURFACE_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiPalette.BORDER_SOFT),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        // Cấu hình giao diện cho bảng
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setGridColor(UiPalette.BORDER_SOFT);
        table.setBackground(UiPalette.TABLE_BACKGROUND);
        table.setSelectionBackground(UiPalette.PRIMARY_SOFT);
        table.setSelectionForeground(UiPalette.TEXT_PRIMARY);
        table.getTableHeader().setBackground(UiPalette.PRIMARY_SOFT);
        table.getTableHeader().setForeground(UiPalette.TEXT_PRIMARY);
        table.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // Các phương thức tiện ích để hiển thị hộp thoại
    private void showError(String message) {
        JOptionPane.showMessageDialog(getDialogParent(), message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(getDialogParent(), message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private int showConfirm(String message, String title, int optionType) {
        return JOptionPane.showConfirmDialog(getDialogParent(), message, title, optionType);
    }

    private Component getDialogParent() {
        Component owner = DialogUiUtil.appDialogParent(this);
        return owner != null ? owner : this;
    }

    private Window getDialogWindow() {
        Component owner = getDialogParent();
        if (owner instanceof Window) {
            return (Window) owner;
        }
        return SwingUtilities.getWindowAncestor(owner);
    }

    /**
     * Lớp nội cho tab quản lý danh sách chi nhánh.
     */
    private final class BranchTabPanel extends JPanel {

        private static final String[] COLUMNS = {
                "Mã CN", "Tên chi nhánh", "Địa chỉ", "Điện thoại", "Email", "Trạng thái", "Ngày tạo"
        };

        private final JTextField searchField = new JTextField();
        private final JComboBox<String> statusFilter = new JComboBox<>(new String[]{"Tất cả", "ACTIVE", "INACTIVE"});

        private final DefaultTableModel tableModel;
        private final JTable table;
        private final TableRowSorter<DefaultTableModel> sorter;
        private List<BranchItem> rows = new ArrayList<>();

        private BranchTabPanel() {
            setLayout(new BorderLayout(0, 8));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

            // Khởi tạo bảng và model
            tableModel = new DefaultTableModel(COLUMNS, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false; // Không cho phép sửa trực tiếp trên bảng
                }
            };
            table = new JTable(tableModel);
            sorter = new TableRowSorter<>(tableModel);
            table.setRowSorter(sorter);
            configureColumns();

            add(buildToolbar(), BorderLayout.NORTH);
            add(createTableCard(table), BorderLayout.CENTER);

            refreshData(); // Tải dữ liệu lần đầu
        }

        /**
         * Xây dựng thanh công cụ chứa các bộ lọc và nút hành động.
         */
        private JPanel buildToolbar() {
            JPanel panel = new JPanel(new BorderLayout(8, 0));
            panel.setOpaque(false);

            // Phần bên trái: tìm kiếm và lọc
            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            left.setOpaque(false);
            searchField.setPreferredSize(new Dimension(230, 30));
            statusFilter.setPreferredSize(new Dimension(110, 30));

            JButton searchBtn = createActionButton("Tìm");
            searchBtn.addActionListener(e -> refreshData());
            statusFilter.addActionListener(e -> refreshData());
            searchField.addActionListener(e -> applyQuickFilter());

            left.add(new JLabel("Tìm kiếm:"));
            left.add(searchField);
            left.add(searchBtn);
            left.add(new JLabel("Trạng thái:"));
            left.add(statusFilter);

            // Phần bên phải: các nút hành động
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            right.setOpaque(false);

            JButton refreshBtn = createActionButton("Làm mới");
            JButton addBtn = createActionButton("Thêm CN");
            JButton editBtn = createActionButton("Sửa");
            JButton deactivateBtn = createDangerButton("Ngừng HĐ");

            refreshBtn.addActionListener(e -> refreshData());
            addBtn.addActionListener(e -> showEditor(null)); // Mở dialog thêm mới
            editBtn.addActionListener(e -> selected()
                    .ifPresentOrElse(this::showEditor, () -> showInfo("Vui lòng chọn chi nhánh cần sửa.")));
            deactivateBtn.addActionListener(e -> selected()
                    .ifPresentOrElse(this::deactivateBranch, () -> showInfo("Vui lòng chọn chi nhánh cần ngừng hoạt động.")));

            right.add(refreshBtn);
            right.add(addBtn);
            right.add(editBtn);
            right.add(deactivateBtn);

            panel.add(left, BorderLayout.WEST);
            panel.add(right, BorderLayout.EAST);
            return panel;
        }

        /**
         * Tải lại dữ liệu từ controller và cập nhật bảng.
         */
        private void refreshData() {
            try {
                rows = controller.loadBranches(searchField.getText(), parseStatusFilter());
                tableModel.setRowCount(0); // Xóa dữ liệu cũ
                // Thêm dữ liệu mới vào bảng
                rows.stream()
                        .map(item -> new Object[]{
                                item.branchCode(),
                                item.branchName(),
                                item.address(),
                                item.phone(),
                                item.email(),
                                item.status().name(),
                                item.createdAt() == null ? "" : item.createdAt().toLocalDate().format(DATE_FMT)
                        })
                        .forEach(tableModel::addRow);
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        }

        /**
         * Chuyển đổi giá trị được chọn trong combobox trạng thái thành enum Status.
         */
        private Status parseStatusFilter() {
            String raw = (String) statusFilter.getSelectedItem();
            if (raw == null || raw.equalsIgnoreCase("Tất cả")) {
                return null;
            }
            return Status.valueOf(raw);
        }

        /**
         * Lấy đối tượng BranchItem tương ứng với hàng đang được chọn trong bảng.
         */
        private Optional<BranchItem> selected() {
            int view = table.getSelectedRow();
            if (view < 0) {
                return Optional.empty();
            }
            int model = table.convertRowIndexToModel(view);
            if (model < 0 || model >= rows.size()) {
                return Optional.empty();
            }
            return Optional.of(rows.get(model));
        }

        /**
         * Áp dụng bộ lọc nhanh trên dữ liệu đã tải mà không cần gọi lại controller.
         */
        private void applyQuickFilter() {
            String kw = searchField.getText();
            if (kw == null || kw.isBlank()) {
                sorter.setRowFilter(null);
                return;
            }
            // Lọc không phân biệt chữ hoa chữ thường trên nhiều cột
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(kw.trim()), 0, 1, 2, 3, 4));
        }

        /**
         * Hiển thị dialog để thêm hoặc sửa thông tin chi nhánh.
         * @param existing Chi nhánh hiện tại để sửa, hoặc null để thêm mới.
         */
        private void showEditor(BranchItem existing) {
            BranchEditorDialog dialog = new BranchEditorDialog(
                    getDialogWindow(),
                    existing);
            dialog.setVisible(true);

            // Xử lý kết quả sau khi dialog đóng
            dialog.getResult().ifPresent(request -> {
                try {
                    if (existing == null) {
                        controller.createBranch(request);
                        showInfo("Thêm chi nhánh thành công.");
                    } else {
                        controller.updateBranch(request);
                        showInfo("Cập nhật chi nhánh thành công.");
                    }
                    refreshData(); // Tải lại dữ liệu để hiển thị thay đổi
                } catch (Exception ex) {
                    showError(ex.getMessage());
                }
            });
        }

        /**
         * Xử lý logic ngừng hoạt động một chi nhánh.
         * @param branch Chi nhánh cần ngừng hoạt động.
         */
        private void deactivateBranch(BranchItem branch) {
            int confirm = showConfirm(
                    "Xác nhận ngừng hoạt động chi nhánh: " + branch.branchCode() + " - " + branch.branchName() + "?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                controller.deactivateBranch(branch.id());
                showInfo("Đã ngừng hoạt động chi nhánh: " + branch.branchName());
                refreshData();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        }

        /**
         * Cấu hình độ rộng và renderer cho các cột trong bảng.
         */
        private void configureColumns() {
            table.getColumnModel().getColumn(0).setPreferredWidth(90);
            table.getColumnModel().getColumn(1).setPreferredWidth(180);
            table.getColumnModel().getColumn(2).setPreferredWidth(260);
            table.getColumnModel().getColumn(3).setPreferredWidth(120);
            table.getColumnModel().getColumn(4).setPreferredWidth(180);
            table.getColumnModel().getColumn(5).setPreferredWidth(90);
            table.getColumnModel().getColumn(6).setPreferredWidth(100);

            // Căn giữa cho một số cột
            DefaultTableCellRenderer center = new DefaultTableCellRenderer();
            center.setHorizontalAlignment(SwingConstants.CENTER);
            table.getColumnModel().getColumn(5).setCellRenderer(center);
            table.getColumnModel().getColumn(6).setCellRenderer(center);
        }
    }

    /**
     * Lớp nội cho tab báo cáo bán hàng theo chi nhánh.
     */
    private final class ReportTabPanel extends JPanel {

        private static final String[] COLUMNS = {
                "Mã CN", "Chi nhánh", "Trạng thái", "Tổng đơn", "Đã thanh toán", "Đang xử lý", "Đã hủy", "Doanh thu", "Đơn gần nhất"
        };

        private final JTextField fromDateField = new JTextField();
        private final JTextField toDateField = new JTextField();
        private final JTextField searchBranchField = new JTextField();
        private final JComboBox<String> statusFilter = new JComboBox<>(new String[]{"Tất cả", "ACTIVE", "INACTIVE"});

        private final DefaultTableModel tableModel;
        private final JTable table;
        private final TableRowSorter<DefaultTableModel> sorter;
        private List<BranchSalesReportItem> rows = new ArrayList<>();

        private ReportTabPanel() {
            setLayout(new BorderLayout(0, 8));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

            // Khởi tạo bảng và model
            tableModel = new DefaultTableModel(COLUMNS, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            table = new JTable(tableModel);
            sorter = new TableRowSorter<>(tableModel);
            table.setRowSorter(sorter);
            configureColumns();

            // Đặt ngày mặc định là ngày đầu tháng và ngày hiện tại
            LocalDate today = LocalDate.now();
            fromDateField.setText(today.withDayOfMonth(1).format(DATE_FMT));
            toDateField.setText(today.format(DATE_FMT));

            add(buildToolbar(), BorderLayout.NORTH);
            add(createTableCard(table), BorderLayout.CENTER);

            refreshData(); // Tải dữ liệu lần đầu
        }

        /**
         * Xây dựng thanh công cụ cho tab báo cáo.
         */
        private JPanel buildToolbar() {
            JPanel panel = new JPanel(new BorderLayout(0, 6));
            panel.setOpaque(false);

            JPanel rowOne = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            rowOne.setOpaque(false);
            JPanel rowTwo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            rowTwo.setOpaque(false);

            // Cấu hình kích thước các trường nhập liệu
            fromDateField.setPreferredSize(new Dimension(100, 30));
            toDateField.setPreferredSize(new Dimension(100, 30));
            searchBranchField.setPreferredSize(new Dimension(220, 30));
            statusFilter.setPreferredSize(new Dimension(110, 30));

            JButton viewBtn = createActionButton("Xem báo cáo");
            viewBtn.addActionListener(e -> refreshData());
            JButton searchBtn = createActionButton("Lọc CN");
            searchBtn.addActionListener(e -> applyBranchFilter());
            searchBranchField.addActionListener(e -> applyBranchFilter());

            // Hàng đầu tiên của thanh công cụ: bộ lọc ngày, trạng thái
            rowOne.add(new JLabel("Từ ngày (dd/MM/yyyy):"));
            rowOne.add(fromDateField);
            rowOne.add(new JLabel("Đến ngày:"));
            rowOne.add(toDateField);
            rowOne.add(new JLabel("Trạng thái CN:"));
            rowOne.add(statusFilter);
            rowOne.add(viewBtn);

            // Hàng thứ hai: bộ lọc theo tên/mã chi nhánh
            rowTwo.add(new JLabel("Chi nhánh (Mã/Tên):"));
            rowTwo.add(searchBranchField);
            rowTwo.add(searchBtn);

            panel.add(rowOne, BorderLayout.NORTH);
            panel.add(rowTwo, BorderLayout.CENTER);

            return panel;
        }

        /**
         * Tải dữ liệu báo cáo từ controller và cập nhật bảng.
         */
        private void refreshData() {
            try {
                // Phân tích và kiểm tra ngày
                LocalDate fromDate = parseDate(fromDateField.getText(), "Từ ngày");
                LocalDate toDate = parseDate(toDateField.getText(), "Đến ngày");
                if (toDate.isBefore(fromDate)) {
                    throw new IllegalArgumentException("Đến ngày phải lớn hơn hoặc bằng Từ ngày.");
                }

                LocalDateTime from = fromDate.atStartOfDay();
                LocalDateTime toExclusive = toDate.plusDays(1).atStartOfDay();

                // Tải dữ liệu báo cáo
                rows = controller.loadBranchSalesReports(from, toExclusive, parseStatusFilter());
                NumberFormat currency = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));

                tableModel.setRowCount(0); // Xóa dữ liệu cũ
                // Thêm dữ liệu mới vào bảng
                rows.stream()
                        .map(item -> new Object[]{
                                item.branchCode(),
                                item.branchName(),
                                item.branchStatus().name(),
                                item.totalOrders(),
                                item.paidOrders(),
                                item.pendingOrders(),
                                item.cancelledOrders(),
                                currency.format(item.revenue() == null ? BigDecimal.ZERO : item.revenue()),
                                item.latestOrderAt() == null ? "-" : item.latestOrderAt().format(DATE_TIME_FMT)
                        })
                        .forEach(tableModel::addRow);

                applyBranchFilter(); // Áp dụng bộ lọc chi nhánh trên dữ liệu mới tải
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        }

        /**
         * Áp dụng bộ lọc theo mã/tên chi nhánh trên dữ liệu đã có trong bảng.
         */
        private void applyBranchFilter() {
            String keyword = searchBranchField.getText();
            if (keyword == null || keyword.isBlank()) {
                sorter.setRowFilter(null);
                return;
            }
            // Lọc trên cột Mã CN và Chi nhánh
            sorter.setRowFilter(RowFilter.regexFilter(
                    "(?i)" + java.util.regex.Pattern.quote(keyword.trim()), 0, 1));
        }

        /**
         * Phân tích chuỗi ngày tháng theo định dạng dd/MM/yyyy.
         */
        private LocalDate parseDate(String raw, String fieldName) {
            if (raw == null || raw.isBlank()) {
                throw new IllegalArgumentException(fieldName + " không được để trống.");
            }
            try {
                return LocalDate.parse(raw.trim(), DATE_FMT);
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException(fieldName + " không đúng định dạng dd/MM/yyyy.");
            }
        }

        /**
         * Chuyển đổi giá trị được chọn trong combobox trạng thái thành enum Status.
         */
        private Status parseStatusFilter() {
            String raw = (String) statusFilter.getSelectedItem();
            if (raw == null || raw.equalsIgnoreCase("Tất cả")) {
                return null;
            }
            return Status.valueOf(raw);
        }

        /**
         * Cấu hình độ rộng và renderer cho các cột trong bảng báo cáo.
         */
        private void configureColumns() {
            table.getColumnModel().getColumn(0).setPreferredWidth(90);
            table.getColumnModel().getColumn(1).setPreferredWidth(180);
            table.getColumnModel().getColumn(2).setPreferredWidth(90);
            table.getColumnModel().getColumn(3).setPreferredWidth(90);
            table.getColumnModel().getColumn(4).setPreferredWidth(100);
            table.getColumnModel().getColumn(5).setPreferredWidth(100);
            table.getColumnModel().getColumn(6).setPreferredWidth(90);
            table.getColumnModel().getColumn(7).setPreferredWidth(130);
            table.getColumnModel().getColumn(8).setPreferredWidth(135);

            // Căn giữa cho các cột số liệu
            DefaultTableCellRenderer center = new DefaultTableCellRenderer();
            center.setHorizontalAlignment(SwingConstants.CENTER);
            for (int col : new int[]{2, 3, 4, 5, 6}) {
                table.getColumnModel().getColumn(col).setCellRenderer(center);
            }

            // Căn phải cho cột doanh thu
            DefaultTableCellRenderer right = new DefaultTableCellRenderer();
            right.setHorizontalAlignment(SwingConstants.RIGHT);
            table.getColumnModel().getColumn(7).setCellRenderer(right);
        }
    }

    /**
     * Lớp nội cho dialog thêm/sửa thông tin chi nhánh.
     */
    private static final class BranchEditorDialog extends JDialog {

        private final JTextField codeField = new JTextField();
        private final JTextField nameField = new JTextField();
        private final JTextField addressField = new JTextField();
        private final JTextField phoneField = new JTextField();
        private final JTextField emailField = new JTextField();
        private final JComboBox<Status> statusCombo = new JComboBox<>(Status.values());

        private final Long editingId; // ID của chi nhánh đang sửa, null nếu là thêm mới
        private BranchCommandRequest result; // Kết quả trả về sau khi người dùng lưu

        private BranchEditorDialog(Window owner, BranchItem existing) {
            super(owner, existing == null ? "Thêm chi nhánh" : "Sửa chi nhánh", ModalityType.APPLICATION_MODAL);
            this.editingId = existing == null ? null : existing.id();

            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            setResizable(false);
            setLayout(new BorderLayout(0, 8));

            // Panel chứa form nhập liệu
            JPanel form = new JPanel(new GridLayout(6, 2, 8, 6));
            form.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));

            form.add(new JLabel("Mã chi nhánh *"));
            form.add(codeField);
            form.add(new JLabel("Tên chi nhánh *"));
            form.add(nameField);
            form.add(new JLabel("Địa chỉ"));
            form.add(addressField);
            form.add(new JLabel("Điện thoại"));
            form.add(phoneField);
            form.add(new JLabel("Email"));
            form.add(emailField);
            form.add(new JLabel("Trạng thái"));
            form.add(statusCombo);

            // Nếu là sửa, điền thông tin có sẵn
            if (existing != null) {
                codeField.setText(existing.branchCode());
                codeField.setEditable(true); // Mã chi nhánh có thể không cho sửa tùy yêu cầu
                nameField.setText(existing.branchName());
                addressField.setText(existing.address());
                phoneField.setText(existing.phone());
                emailField.setText(existing.email());
                statusCombo.setSelectedItem(existing.status());
            } else { // Nếu là thêm mới
                codeField.setText("");
                codeField.setEditable(true);
                statusCombo.setSelectedItem(Status.ACTIVE);
            }

            // Panel chứa các nút hành động
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
            JButton cancelBtn = new JButton("Hủy");
            JButton saveBtn = new JButton("Lưu");
            saveBtn.setBackground(UiPalette.PRIMARY);
            saveBtn.setForeground(Color.WHITE);
            saveBtn.setFocusPainted(false);

            saveBtn.addActionListener(e -> onSave());
            cancelBtn.addActionListener(e -> dispose());

            actions.add(saveBtn);
            actions.add(cancelBtn);

            add(form, BorderLayout.CENTER);
            add(actions, BorderLayout.SOUTH);

            pack();
            setMinimumSize(new Dimension(520, 320));
            setSize(getMinimumSize());
            setLocationRelativeTo(owner);
        }

        /**
         * Xử lý khi người dùng nhấn nút "Lưu".
         */
        private void onSave() {
            // Kiểm tra các trường bắt buộc
            if (codeField.getText().isBlank()) {
                JOptionPane.showMessageDialog(DialogUiUtil.appDialogParent(this),
                        "Vui lòng nhập mã chi nhánh.",
                        "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (nameField.getText().isBlank()) {
                JOptionPane.showMessageDialog(DialogUiUtil.appDialogParent(this),
                        "Vui lòng nhập tên chi nhánh.",
                        "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Tạo đối tượng request để trả về
            result = new BranchCommandRequest(
                    editingId,
                    codeField.getText().trim().toUpperCase(),
                    nameField.getText().trim(),
                    addressField.getText().trim().isEmpty() ? null : addressField.getText().trim(),
                    phoneField.getText().trim().isEmpty() ? null : phoneField.getText().trim(),
                    emailField.getText().trim().isEmpty() ? null : emailField.getText().trim(),
                    (Status) statusCombo.getSelectedItem()
            );
            dispose(); // Đóng dialog
        }

        /**
         * Lấy kết quả sau khi dialog đóng.
         * @return Optional chứa BranchCommandRequest nếu người dùng đã lưu.
         */
        private Optional<BranchCommandRequest> getResult() {
            return Optional.ofNullable(result);
        }
    }
}
