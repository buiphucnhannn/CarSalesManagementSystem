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
 *  - View (JPanel này) chỉ hiển thị dữ liệu và nhận tương tác người dùng.
 *  - Controller (CustomerManagementController) nhận lệnh từ View, gọi Service để xử lý nghiệp vụ.
 *  - Service thực hiện nghiệp vụ, và DAO (Data Access Object) thao tác với cơ sở dữ liệu.
 *
 * Cấu trúc nội bộ:
 *  - CustomerManagementPanel: Lớp chính, chứa layout và các phương thức factory để tạo UI.
 *  - CustomerEditorDialog: Lớp nội tĩnh (static inner class) cho dialog thêm/sửa thông tin khách hàng.
 *
 * Thiết kế này tuân thủ nguyên tắc Open/Closed: có thể thêm tính năng mới bằng cách mở rộng (ví dụ, thêm panel mới)
 * mà không cần sửa đổi mã nguồn đã có.
 */
public class CustomerManagementPanel extends JPanel {

    // ─── Hằng số ────────────────────────────────────────────────────────

    /** Định dạng ngày tháng được sử dụng trong bảng và dialog (ví dụ: 25/12/2023). */
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Tên các cột trong bảng JTable hiển thị danh sách khách hàng. */
    private static final String[] COLUMNS = {
            "Mã KH", "Họ tên", "Điện thoại", "Email", "Giới tính",
            "Ngày sinh", "CCCD", "Địa chỉ", "Ngày tạo"
    };

    // ─── Các trường (Fields) ───────────────────────────────────────────────────────────

    /** Controller xử lý logic nghiệp vụ, nhận lệnh từ View này. */
    private final CustomerManagementController controller;

    /** Model chứa dữ liệu cho JTable. */
    private final DefaultTableModel tableModel;

    /** Bảng hiển thị danh sách khách hàng. */
    private final JTable table;

    /** Hỗ trợ sắp xếp và lọc dữ liệu trên JTable phía client. */
    private final TableRowSorter<DefaultTableModel> sorter;

    /** Ô văn bản để người dùng nhập từ khóa tìm kiếm. */
    private final JTextField searchField = new JTextField();

    /** Lưu trữ danh sách các đối tượng CustomerItem hiện đang hiển thị trong bảng. */
    private List<CustomerItem> rows = new ArrayList<>();

    // ─── Constructor ─────────────────────────────────────────────────────

    /**
     * Khởi tạo panel, xây dựng giao diện người dùng và tải dữ liệu ban đầu.
     *
     * @param controller Controller quản lý khách hàng (không được null).
     */
    public CustomerManagementPanel(CustomerManagementController controller) {
        this.controller = Objects.requireNonNull(controller, "controller is required");

        setLayout(new BorderLayout(0, 8));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        // Khởi tạo table model, không cho phép sửa trực tiếp trên ô của bảng.
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Việc sửa đổi chỉ được thực hiện thông qua dialog.
            }
        };
        table = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        configureTableColumns();

        add(buildToolbar(), BorderLayout.NORTH);
        add(createTableCard(table), BorderLayout.CENTER);

        // Tải dữ liệu lần đầu khi panel được tạo.
        refreshData();
    }

    // ─── Thanh công cụ (Toolbar) ─────────────────────────────────────────────────────────

    /**
     * Xây dựng thanh công cụ (toolbar) bao gồm:
     *  - Bên trái: Ô tìm kiếm và nút "Tìm".
     *  - Bên phải: Các nút "Làm mới", "Thêm", "Sửa", "Xóa".
     */
    private JPanel buildToolbar() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);

        // ── Phần bên trái: tìm kiếm ──
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        searchField.setPreferredSize(new Dimension(260, 30));

        JButton searchBtn = createActionButton("Tìm");
        // Sử dụng biểu thức lambda để gọi refreshData() khi nhấn nút "Tìm".
        searchBtn.addActionListener(e -> refreshData());
        // Lọc nhanh phía client khi người dùng nhấn Enter trong ô tìm kiếm.
        searchField.addActionListener(e -> applyQuickFilter());

        left.add(new JLabel("Tìm kiếm:"));
        left.add(searchField);
        left.add(searchBtn);

        // ── Phần bên phải: các nút CRUD ──
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JButton refreshBtn = createActionButton("Làm mới");
        JButton addBtn     = createActionButton("Thêm KH");
        JButton editBtn    = createActionButton("Sửa");
        JButton deleteBtn  = createDangerButton("Xóa");

        // Gán hành động cho từng nút bằng biểu thức lambda.
        refreshBtn.addActionListener(e -> refreshData());
        addBtn.addActionListener(e -> showEditor(null)); // Mở dialog thêm mới.
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

    // ─── Thao tác dữ liệu ─────────────────────────────────────────────────

    /**
     * Tải lại dữ liệu từ service dựa trên từ khóa tìm kiếm hiện tại.
     * Sử dụng Stream API để chuyển đổi (map) từ DTO (Data Transfer Object) sang các hàng của bảng.
     */
    public void refreshData() {
        try {
            rows = controller.loadCustomers(searchField.getText());
            tableModel.setRowCount(0); // Xóa dữ liệu cũ trong bảng.
            // Sử dụng stream để xử lý: map mỗi CustomerItem thành một mảng Object[] cho bảng.
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
                    .forEach(tableModel::addRow); // Thêm hàng mới vào table model.
        } catch (Exception ex) {
            showError("Lỗi tải danh sách khách hàng: " + ex.getMessage());
        }
    }

    /**
     * Lọc nhanh dữ liệu phía client dựa trên từ khóa đang nhập (không cần truy vấn lại CSDL).
     */
    private void applyQuickFilter() {
        String kw = searchField.getText();
        if (kw == null || kw.isBlank()) {
            sorter.setRowFilter(null); // Bỏ lọc nếu không có từ khóa.
            return;
        }
        // Sử dụng RowFilter với regex để lọc không phân biệt chữ hoa/thường, khớp trên các cột Mã KH, Họ tên, và Điện thoại.
        sorter.setRowFilter(RowFilter.regexFilter(
                "(?i)" + java.util.regex.Pattern.quote(kw.trim()), 0, 1, 2));
    }

    /**
     * Mở dialog để thêm mới hoặc sửa thông tin khách hàng.
     *
     * @param existing `null` nếu thêm mới, hoặc đối tượng `CustomerItem` nếu sửa.
     */
    private void showEditor(CustomerItem existing) {
        String nextCode = existing == null ? controller.loadNextCustomerCode() : null;
        SaveAction saveAction = existing == null ? controller::createCustomer : controller::updateCustomer;
        CustomerEditorDialog dialog = new CustomerEditorDialog(
                getDialogWindow(), existing, nextCode, saveAction);
        dialog.setVisible(true);

        dialog.getResult().ifPresent(request -> {
            showInfo(existing == null ? "Thêm khách hàng thành công." : "Cập nhật khách hàng thành công.");
            refreshData(); // Tải lại dữ liệu bảng sau khi thao tác thành công.
        });
    }

    @FunctionalInterface
    private interface SaveAction {
        void save(CustomerCommandRequest request) throws Exception;
    }

    /**
     * Hiển thị hộp thoại xác nhận và thực hiện xóa khách hàng.
     *
     * @param item Khách hàng cần xóa.
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

    // ─── Trợ giúp lựa chọn (Selection helper) ─────────────────────────────────────────────────

    /**
     * Lấy đối tượng CustomerItem tương ứng với hàng đang được chọn trong bảng.
     * Chuyển đổi chỉ số hàng từ view sang model để đảm bảo chính xác khi có bộ lọc (sorter).
     */
    private Optional<CustomerItem> selectedRow() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return Optional.empty();
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= rows.size()) return Optional.empty();
        return Optional.of(rows.get(modelRow));
    }

    // ─── Trợ giúp tạo UI (UI factory helpers) ───────────────────────────────────────────────

    /**
     * Tạo một nút hành động với phong cách nhất quán (thường là màu xanh).
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
     * Tạo một nút hành động nguy hiểm (thường là màu đỏ, dùng cho việc xóa).
     */
    private JButton createDangerButton(String title) {
        JButton btn = createActionButton(title);
        btn.setForeground(UiPalette.DANGER);
        return btn;
    }

    /**
     * Bọc JTable trong một JScrollPane và áp dụng phong cách nhất quán với toàn bộ ứng dụng.
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
     * Cấu hình chiều rộng ưu tiên cho các cột của bảng.
     */
    private void configureTableColumns() {
        // Cột Mã KH hẹp, trong khi Họ tên và Địa chỉ rộng hơn.
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(160);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);
        table.getColumnModel().getColumn(6).setPreferredWidth(110);
        table.getColumnModel().getColumn(7).setPreferredWidth(200);
        table.getColumnModel().getColumn(8).setPreferredWidth(90);

        // Căn giữa nội dung cho các cột Giới tính và Ngày.
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(4).setCellRenderer(center);
        table.getColumnModel().getColumn(5).setCellRenderer(center);
        table.getColumnModel().getColumn(8).setCellRenderer(center);
    }

    // ─── Tiện ích (Utils) ────────────────────────────────────────────────────────────

    /** Chuyển đổi enum Gender sang nhãn tiếng Việt để hiển thị. */
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

    // ─── Lớp nội Dialog ─────────────────────────────────────────────────────

    /**
     * Dialog để thêm mới hoặc sửa thông tin khách hàng.
     * Đây là một lớp nội tĩnh (static final inner class) để không giữ tham chiếu đến panel bên ngoài,
     * giúp tránh rò rỉ bộ nhớ (memory leak) và tuân thủ nguyên tắc đóng gói (encapsulation).
     *
     * Luồng hoạt động:
     * 1. Mở dialog (gọi `setVisible(true)`).
     * 2. Người dùng nhập liệu và nhấn "Lưu", gọi phương thức `onSave()`.
     * 3. Panel bên ngoài gọi `getResult()` để nhận `Optional<CustomerCommandRequest>`.
     */
    private static final class CustomerEditorDialog extends JDialog {

        // ── Các trường của form ──
        private final JTextField codeField        = new JTextField();
        private final JTextField fullNameField     = new JTextField();
        private final JTextField phoneField        = new JTextField();
        private final JTextField emailField        = new JTextField();
        private final JComboBox<Gender> genderCombo =
                new JComboBox<>(new Gender[]{null, Gender.MALE, Gender.FEMALE, Gender.OTHER});
        private final JTextField dobField          = new JTextField();  // Định dạng: dd/MM/yyyy
        private final JTextField identityField     = new JTextField();
        private final JTextField addressField      = new JTextField();
        private final JTextArea  noteArea          = new JTextArea(3, 20);

        /** Kết quả được trả về sau khi người dùng nhấn "Lưu". */
        private CustomerCommandRequest result;

        /** ID của khách hàng đang được sửa (null nếu là thêm mới). */
        private final Long editingId;
        private final SaveAction saveAction;

        /**
         * @param owner    Cửa sổ cha (frame hoặc dialog).
         * @param existing `null` nếu thêm mới; khác `null` nếu sửa.
         */
        private CustomerEditorDialog(Window owner, CustomerItem existing, String nextCode, SaveAction saveAction) {
            super(owner,
                    existing == null ? "Thêm khách hàng" : "Sửa khách hàng",
                    ModalityType.APPLICATION_MODAL);
            this.editingId = existing == null ? null : existing.id();
            this.saveAction = Objects.requireNonNull(saveAction, "saveAction is required");

            setResizable(false);
            setLayout(new BorderLayout(0, 8));

            // ── Lưới form ──
            JPanel form = new JPanel(new GridBagLayout());
            form.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));

            // Ghi chú về định dạng ngày.
            dobField.setToolTipText("Định dạng: dd/MM/yyyy");

            codeField.setPreferredSize(new Dimension(280, 34));
            fullNameField.setPreferredSize(new Dimension(280, 34));
            phoneField.setPreferredSize(new Dimension(280, 34));
            emailField.setPreferredSize(new Dimension(280, 34));
            dobField.setPreferredSize(new Dimension(280, 34));
            identityField.setPreferredSize(new Dimension(280, 34));
            addressField.setPreferredSize(new Dimension(280, 34));

            addFormRow(form, 0, "Mã khách hàng *", codeField, false);
            addFormRow(form, 1, "Họ tên *", fullNameField, false);
            addFormRow(form, 2, "Điện thoại *", phoneField, false);
            addFormRow(form, 3, "Email", emailField, false);
            addFormRow(form, 4, "Giới tính", genderCombo, false);
            addFormRow(form, 5, "Ngày sinh (dd/MM/yyyy)", dobField, false);
            addFormRow(form, 6, "CCCD/CMND", identityField, false);
            addFormRow(form, 7, "Địa chỉ", addressField, false);

            noteArea.setRows(6);
            noteArea.setColumns(28);
            noteArea.setLineWrap(true);
            noteArea.setWrapStyleWord(true);
            JScrollPane noteScroll = new JScrollPane(noteArea);
            noteScroll.setPreferredSize(new Dimension(280, 130));
            addFormRow(form, 8, "Ghi chú", noteScroll, true);

            // Điền dữ liệu có sẵn nếu đang ở chế độ sửa.
            if (existing != null) {
                codeField.setText(existing.customerCode());
                // Mã KH được hệ thống quản lý, không cho sửa ở cả chế độ thêm/sửa.
                codeField.setEditable(false);
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
                // Thêm mới: để trống các trường khác.
            }

            // ── Các nút hành động ──
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

        private void addFormRow(JPanel form, int row, String labelText, JComponent input, boolean expandable) {
            GridBagConstraints labelGbc = new GridBagConstraints();
            labelGbc.gridx = 0;
            labelGbc.gridy = row;
            labelGbc.anchor = GridBagConstraints.NORTHWEST;
            labelGbc.insets = new Insets(4, 4, 4, 10);
            form.add(new JLabel(labelText), labelGbc);

            GridBagConstraints inputGbc = new GridBagConstraints();
            inputGbc.gridx = 1;
            inputGbc.gridy = row;
            inputGbc.fill = GridBagConstraints.HORIZONTAL;
            inputGbc.weightx = 1.0;
            inputGbc.weighty = expandable ? 1.0 : 0;
            inputGbc.insets = new Insets(4, 0, 4, 4);
            if (expandable) {
                inputGbc.fill = GridBagConstraints.BOTH;
            }
            form.add(input, inputGbc);
        }

        /**
         * Đọc dữ liệu từ form, thực hiện xác thực cơ bản và tạo đối tượng request.
         * Nếu việc phân tích cú pháp thất bại, sẽ hiển thị thông báo lỗi và không đóng dialog.
         */
        private void onSave() {
            // Kiểm tra các trường bắt buộc.
            if (codeField.getText().isBlank() ||
                    fullNameField.getText().isBlank() ||
                    phoneField.getText().isBlank()) {
                JOptionPane.showMessageDialog(DialogUiUtil.appDialogParent(this),
                        "Vui lòng điền đủ các trường bắt buộc (*).",
                        "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Phân tích cú pháp ngày sinh (tùy chọn).
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

            // Tạo đối tượng request để trả về cho panel bên ngoài.
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
            try {
                saveAction.save(result);
                dispose(); // Chỉ đóng khi lưu thật sự thành công.
            } catch (Exception ex) {
                result = null;
                JOptionPane.showMessageDialog(DialogUiUtil.appDialogParent(this),
                        ex.getMessage(),
                        "Lỗi nhập liệu",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        /**
         * Trả về đối tượng request sau khi dialog đã đóng.
         * Sẽ là `Optional.empty()` nếu người dùng nhấn "Hủy" hoặc đóng dialog.
         */
        private Optional<CustomerCommandRequest> getResult() {
            return Optional.ofNullable(result);
        }
    }
}
