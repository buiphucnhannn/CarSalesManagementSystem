package vn.edu.ute.carsalesms.view.component;

import vn.edu.ute.carsalesms.controller.CustomerManagementController;
import vn.edu.ute.carsalesms.model.dto.CustomerCommandRequest;
import vn.edu.ute.carsalesms.model.dto.CustomerItem;
import vn.edu.ute.carsalesms.model.enums.Gender;
import vn.edu.ute.carsalesms.view.theme.DialogUiUtil;
import vn.edu.ute.carsalesms.view.theme.UiPalette;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Panel quản lý khách hàng – Module F04.
 * Áp dụng mô hình MVC:
 *  - View (JPanel này) chỉ hiển thị dữ liệu
 *  - Controller nhận lệnh, gọi Service
 *  - Service thực hiện nghiệp vụ, DAO thao tác CSDL
 *
 * Cấu trúc nội bộ:
 *  - CustomerManagementPanel    (outer panel - layout + factory methods)
 *  - CustomerEditorDialog       (static inner class - dialog thêm/sửa)
 *
 * Tuân thủ Open/Closed: thêm tính năng bằng cách mở rộng, không sửa code cũ.
 */
public class CustomerManagementPanel extends JPanel {

    // ─── Constants ────────────────────────────────────────────────────────

    /** Định dạng ngày hiển thị trong bảng và dialog. */
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Tên các cột trong JTable. */
    private static final String[] COLUMNS = {
            "Mã KH", "Họ tên", "Điện thoại", "Email", "Giới tính",
            "Ngày sinh", "CCCD", "Địa chỉ", "Ngày tạo"
    };

    // ─── Fields ───────────────────────────────────────────────────────────

    /** Controller nhận lệnh từ View, gọi Service tương ứng. */
    private final CustomerManagementController controller;

    /** Model dữ liệu của JTable. */
    private final DefaultTableModel tableModel;

    /** JTable hiển thị danh sách khách hàng. */
    private final JTable table;

    /** Row sorter hỗ trợ sắp xếp và lọc nhanh client-side. */
    private final TableRowSorter<DefaultTableModel> sorter;

    /** Ô tìm kiếm trên thanh toolbar. */
    private final JTextField searchField = new JTextField();

    /** Cache danh sách row hiện tại để resolve selection → entity. */
    private List<CustomerItem> rows = new ArrayList<>();

    // ─── Constructor ─────────────────────────────────────────────────────

    /**
     * Khởi tạo panel, build UI và load dữ liệu ban đầu.
     *
     * @param controller controller quản lý khách hàng (không null)
     */
    public CustomerManagementPanel(CustomerManagementController controller) {
        this.controller = Objects.requireNonNull(controller, "controller is required");

        setLayout(new BorderLayout(0, 8));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        // Khởi tạo model - không cho edit trực tiếp trên cell
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Chỉ sửa qua dialog
            }
        };
        table = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        configureTableColumns();

        add(buildToolbar(), BorderLayout.NORTH);
        add(createTableCard(table), BorderLayout.CENTER);

        // Load dữ liệu khi panel được tạo
        refreshData();
    }

    // ─── Toolbar ─────────────────────────────────────────────────────────

    /**
     * Xây thanh toolbar gồm:
     *  - Trái: ô tìm kiếm + nút Tìm
     *  - Phải: Làm mới, Thêm, Sửa, Xóa
     */
    private JPanel buildToolbar() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);

        // ── Phần trái: tìm kiếm ──
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        searchField.setPreferredSize(new Dimension(260, 30));

        JButton searchBtn = createActionButton("Tìm");
        // Lambda: gọi refreshData khi nhấn Tìm
        searchBtn.addActionListener(e -> refreshData());
        // Lọc client-side nhanh khi nhấn Enter
        searchField.addActionListener(e -> applyQuickFilter());

        left.add(new JLabel("Tìm kiếm:"));
        left.add(searchField);
        left.add(searchBtn);

        // ── Phần phải: CRUD buttons ──
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JButton refreshBtn = createActionButton("Làm mới");
        JButton addBtn     = createActionButton("Thêm KH");
        JButton editBtn    = createActionButton("Sửa");
        JButton deleteBtn  = createDangerButton("Xóa");

        // Lambda: mỗi nút gọi phương thức xử lý tương ứng
        refreshBtn.addActionListener(e -> refreshData());
        addBtn.addActionListener(e -> showEditor(null));
        editBtn.addActionListener(e -> selectedRow()
                .ifPresentOrElse(this::showEditor, () -> showInfo("Vui lòng chọn khách hàng cần sửa.")));
        deleteBtn.addActionListener(e -> selectedRow()
                .ifPresentOrElse(this::deleteCustomer, () -> showInfo("Vui lòng chọn khách hàng cần xóa.")));

        right.add(refreshBtn);
        right.add(addBtn);
        right.add(editBtn);
        right.add(deleteBtn);

        panel.add(left, BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    // ─── Data operations ─────────────────────────────────────────────────

    /**
     * Tải lại dữ liệu từ service theo keyword hiện tại.
     * Dùng Stream API để map DTO → hàng trong bảng.
     */
    public void refreshData() {
        try {
            rows = controller.loadCustomers(searchField.getText());
            tableModel.setRowCount(0);
            // Stream: map từng CustomerItem thành mảng Object[] cho bảng
            rows.stream()
                    .map(item -> new Object[]{
                            item.customerCode(),
                            item.fullName(),
                            item.phone(),
                            item.email(),
                            item.gender() == null ? "" : genderLabel(item.gender()),
                            item.dateOfBirth() == null ? "" : item.dateOfBirth().format(DATE_FMT),
                            item.identityNumber(),
                            item.address(),
                            item.createdAt() == null ? "" : item.createdAt().toLocalDate().format(DATE_FMT)
                    })
                    .forEach(tableModel::addRow);
        } catch (Exception ex) {
            showError("Lỗi tải danh sách khách hàng: " + ex.getMessage());
        }
    }

    /**
     * Lọc nhanh client-side theo keyword đang nhập (không query DB).
     */
    private void applyQuickFilter() {
        String kw = searchField.getText();
        if (kw == null || kw.isBlank()) {
            sorter.setRowFilter(null);
            return;
        }
        // RowFilter regex không phân biệt hoa/thường, khớp cột Mã KH và Họ tên
        sorter.setRowFilter(RowFilter.regexFilter(
                "(?i)" + java.util.regex.Pattern.quote(kw.trim()), 0, 1, 2));
    }

    /**
     * Mở dialog thêm mới hoặc sửa khách hàng.
     *
     * @param existing null nếu thêm mới, non-null nếu sửa
     */
    private void showEditor(CustomerItem existing) {
        String nextCode = existing == null ? controller.loadNextCustomerCode() : null;
        CustomerEditorDialog dialog = new CustomerEditorDialog(
                getDialogWindow(), existing, nextCode);
        dialog.setVisible(true);

        // Nếu người dùng nhấn Lưu thì dialog trả về request
        dialog.getResult().ifPresent(request -> {
            try {
                if (existing == null) {
                    controller.createCustomer(request);
                    showInfo("Thêm khách hàng thành công.");
                } else {
                    controller.updateCustomer(request);
                    showInfo("Cập nhật khách hàng thành công.");
                }
                refreshData(); // Reload bảng sau khi thao tác
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });
    }

    /**
     * Xác nhận và thực hiện xóa khách hàng.
     *
     * @param item khách hàng cần xóa
     */
    private void deleteCustomer(CustomerItem item) {
        int confirm = JOptionPane.showConfirmDialog(
                getDialogParent(),
                "Xác nhận xóa khách hàng: " + item.fullName() + " (" + item.customerCode() + ")?\n" +
                "Lưu ý: không thể xóa nếu khách hàng có đơn hàng liên quan.",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            controller.deleteCustomer(item.id());
            showInfo("Đã xóa khách hàng: " + item.fullName());
            refreshData();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    // ─── Selection helper ─────────────────────────────────────────────────

    /**
     * Lấy CustomerItem đang được chọn trong bảng.
     * Chuyển đổi selectedRow → modelRow để tránh sai lệch khi sorter bật.
     */
    private Optional<CustomerItem> selectedRow() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return Optional.empty();
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= rows.size()) return Optional.empty();
        return Optional.of(rows.get(modelRow));
    }

    // ─── UI factory helpers ───────────────────────────────────────────────

    /**
     * Tạo nút hành động với style nhất quán (màu xanh).
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
     * Tạo nút nguy hiểm (màu đỏ, dùng cho xóa).
     */
    private JButton createDangerButton(String title) {
        JButton btn = createActionButton(title);
        btn.setForeground(UiPalette.DANGER);
        return btn;
    }

    /**
     * Bao JTable trong JScrollPane có style nhất quán với toàn ứng dụng.
     */
    private JPanel createTableCard(JTable tbl) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UiPalette.SURFACE_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiPalette.BORDER_SOFT),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        tbl.setRowHeight(28);
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
     * Cấu hình chiều rộng ưu tiên cho các cột.
     */
    private void configureTableColumns() {
        // Mã KH hẹp, Họ tên + Địa chỉ rộng hơn
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(160);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);
        table.getColumnModel().getColumn(6).setPreferredWidth(110);
        table.getColumnModel().getColumn(7).setPreferredWidth(200);
        table.getColumnModel().getColumn(8).setPreferredWidth(90);

        // Căn giữa cột giới tính và ngày
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(4).setCellRenderer(center);
        table.getColumnModel().getColumn(5).setCellRenderer(center);
        table.getColumnModel().getColumn(8).setCellRenderer(center);
    }

    // ─── Utils ────────────────────────────────────────────────────────────

    /** Chuyển enum Gender sang nhãn tiếng Việt. */
    private String genderLabel(Gender gender) {
        return switch (gender) {
            case MALE   -> "Nam";
            case FEMALE -> "Nữ";
            case OTHER  -> "Khác";
        };
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(getDialogParent(), msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(getDialogParent(), msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
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

    // ─── Inner Dialog ─────────────────────────────────────────────────────

    /**
     * Dialog thêm mới hoặc sửa thông tin khách hàng.
     * Là {@code static final} inner class để không giữ tham chiếu đến outer panel
     * (tránh memory leak, tuân thủ encapsulation).
     *
     * Luồng:
     * 1. Mở dialog (setVisible true)
     * 2. Người dùng nhập liệu → nhấn Lưu → gọi onSave()
     * 3. Outer panel gọi getResult() để nhận Optional<CustomerCommandRequest>
     */
    private static final class CustomerEditorDialog extends JDialog {

        // ── Form fields ──
        private final JTextField codeField        = new JTextField();
        private final JTextField fullNameField     = new JTextField();
        private final JTextField phoneField        = new JTextField();
        private final JTextField emailField        = new JTextField();
        private final JComboBox<Gender> genderCombo =
                new JComboBox<>(new Gender[]{null, Gender.MALE, Gender.FEMALE, Gender.OTHER});
        private final JTextField dobField          = new JTextField();  // dd/MM/yyyy
        private final JTextField identityField     = new JTextField();
        private final JTextField addressField      = new JTextField();
        private final JTextArea  noteArea          = new JTextArea(3, 20);

        /** Kết quả trả về sau khi người dùng nhấn Lưu. */
        private CustomerCommandRequest result;

        /** Id khách hàng đang sửa (null nếu thêm mới). */
        private final Long editingId;

        /**
         * @param owner    frame/dialog cha
         * @param existing null = thêm mới; non-null = sửa
         */
        private CustomerEditorDialog(Window owner, CustomerItem existing, String nextCode) {
            super(owner,
                    existing == null ? "Thêm khách hàng" : "Sửa khách hàng",
                    ModalityType.APPLICATION_MODAL);
            this.editingId = existing == null ? null : existing.id();

            setResizable(false);
            setLayout(new BorderLayout(0, 8));

            // ── Form grid ──
            JPanel form = new JPanel(new GridLayout(9, 2, 8, 6));
            form.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));

            // Ghi chú định dạng ngày
            dobField.setToolTipText("Định dạng: dd/MM/yyyy");

            form.add(new JLabel("Mã khách hàng *"));  form.add(codeField);
            form.add(new JLabel("Họ tên *"));          form.add(fullNameField);
            form.add(new JLabel("Điện thoại *"));      form.add(phoneField);
            form.add(new JLabel("Email"));              form.add(emailField);
            form.add(new JLabel("Giới tính"));         form.add(genderCombo);
            form.add(new JLabel("Ngày sinh (dd/MM/yyyy)")); form.add(dobField);
            form.add(new JLabel("CCCD/CMND"));         form.add(identityField);
            form.add(new JLabel("Địa chỉ"));           form.add(addressField);
            form.add(new JLabel("Ghi chú"));
            JScrollPane noteScroll = new JScrollPane(noteArea);
            form.add(noteScroll);

            // Điền dữ liệu nếu đang sửa
            if (existing != null) {
                codeField.setText(existing.customerCode());
                codeField.setEditable(true);
                fullNameField.setText(existing.fullName());
                phoneField.setText(existing.phone());
                emailField.setText(existing.email());
                genderCombo.setSelectedItem(existing.gender());
                if (existing.dateOfBirth() != null) {
                    dobField.setText(existing.dateOfBirth().format(DATE_FMT));
                }
                identityField.setText(existing.identityNumber());
                addressField.setText(existing.address());
                noteArea.setText(existing.note());
            } else {
                codeField.setText(nextCode);
                codeField.setEditable(false);
            }

            // ── Action buttons ──
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
            JButton cancelBtn = new JButton("Hủy");
            JButton saveBtn   = new JButton("Lưu");
            saveBtn.setBackground(UiPalette.PRIMARY);
            saveBtn.setForeground(Color.WHITE);
            saveBtn.setFocusPainted(false);

            cancelBtn.addActionListener(e -> dispose());
            saveBtn.addActionListener(e -> onSave());

            actions.add(saveBtn);
            actions.add(cancelBtn);

            add(form, BorderLayout.CENTER);
            add(actions, BorderLayout.SOUTH);

            pack();
            setMinimumSize(new Dimension(500, 460));
            setSize(getMinimumSize());
            setLocationRelativeTo(owner);
        }

        /**
         * Đọc dữ liệu từ form, validate cơ bản, tạo request.
         * Nếu parse thất bại sẽ hiển thị thông báo lỗi và không đóng dialog.
         */
        private void onSave() {
            // Kiểm tra trường bắt buộc
            if (codeField.getText().isBlank() ||
                    fullNameField.getText().isBlank() ||
                    phoneField.getText().isBlank()) {
                JOptionPane.showMessageDialog(DialogUiUtil.appDialogParent(this),
                        "Vui lòng điền đủ các trường bắt buộc (*).",
                        "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Parse ngày sinh (tùy chọn)
            LocalDate dob = null;
            String dobText = dobField.getText().trim();
            if (!dobText.isBlank()) {
                try {
                    dob = LocalDate.parse(dobText, DATE_FMT);
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(DialogUiUtil.appDialogParent(this),
                            "Ngày sinh không đúng định dạng dd/MM/yyyy.",
                            "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // Tạo request để trả về outer panel
            result = new CustomerCommandRequest(
                    editingId,
                    codeField.getText().trim().toUpperCase(),
                    fullNameField.getText().trim(),
                    phoneField.getText().trim(),
                    emailField.getText().trim().isEmpty() ? null : emailField.getText().trim(),
                    (Gender) genderCombo.getSelectedItem(),
                    dob,
                    identityField.getText().trim().isEmpty() ? null : identityField.getText().trim(),
                    addressField.getText().trim().isEmpty() ? null : addressField.getText().trim(),
                    noteArea.getText().trim().isEmpty() ? null : noteArea.getText().trim()
            );
            dispose();
        }

        /**
         * Trả về request sau khi dialog đóng.
         * Optional.empty() nếu người dùng nhấn Hủy hoặc đóng dialog.
         */
        private Optional<CustomerCommandRequest> getResult() {
            return Optional.ofNullable(result);
        }
    }
}
