package vn.edu.ute.carsalesms.view.component;

import vn.edu.ute.carsalesms.controller.PromotionController;
import vn.edu.ute.carsalesms.model.dto.PromotionItem;
import vn.edu.ute.carsalesms.model.dto.PromotionRequest;
import vn.edu.ute.carsalesms.model.enums.Status;
import vn.edu.ute.carsalesms.view.theme.DialogUiUtil;
import vn.edu.ute.carsalesms.view.theme.UiPalette;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Lớp PromotionPanel là giao diện người dùng để quản lý các chương trình khuyến mãi.
 * Người dùng có thể tìm kiếm, thêm mới, chỉnh sửa và thay đổi trạng thái (khóa/mở) các khuyến mãi.
 * Lớp này tương tác với PromotionController để xử lý logic nghiệp vụ.
 */
public class PromotionPanel extends JPanel {

    @FunctionalInterface
    private interface SaveAction {
        void save(PromotionRequest request) throws Exception;
    }

    // Định dạng ngày tháng được sử dụng để hiển thị và nhập liệu.
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final PromotionController promotionController; // Controller để xử lý các thao tác liên quan đến khuyến mãi.

    private final JTextField txtSearch = new JTextField(20); // Ô nhập liệu để tìm kiếm khuyến mãi.
    private final DefaultTableModel tableModel; // Model dữ liệu cho bảng khuyến mãi.
    private final JTable table; // Bảng hiển thị danh sách khuyến mãi.
    private List<PromotionItem> rowData = new ArrayList<>(); // Danh sách các đối tượng PromotionItem hiện tại.

    /**
     * Constructor khởi tạo PromotionPanel.
     * @param promotionController Controller để xử lý các thao tác liên quan đến khuyến mãi.
     */
    public PromotionPanel(PromotionController promotionController) {
        this.promotionController = promotionController;

        setLayout(new BorderLayout(8, 8)); // Sử dụng BorderLayout với khoảng cách 8px.
        setOpaque(false); // Đặt panel không trong suốt.
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12)); // Đặt đường viền rỗng.

        // Định nghĩa tên các cột cho bảng khuyến mãi.
        String[] cols = {
                "ID", "Mã KM", "Tên KM", "Kiểu giảm", "Mức giảm", "Ngày BĐ", "Ngày KT", "Trạng thái", "Ghi chú"
        };
        // Khởi tạo table model, không cho phép chỉnh sửa trực tiếp trên bảng.
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28); // Đặt chiều cao hàng.

        // Ẩn cột ID (cột 0) vì nó chỉ dùng nội bộ.
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        // Cấu hình renderer để căn phải cho cột "Mức giảm".
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter); // Gán bộ sắp xếp cho bảng.

        add(buildToolbar(), BorderLayout.NORTH); // Thêm thanh công cụ vào phía Bắc.
        add(createTableCard(table), BorderLayout.CENTER); // Thêm bảng vào giữa, bọc trong card.

        refreshData(); // Tải dữ liệu khuyến mãi ban đầu.
    }

    /**
     * Xây dựng thanh công cụ (toolbar) cho panel.
     * Bao gồm ô tìm kiếm, nút "Tìm" và các nút hành động (Thêm, Sửa, Khóa/Mở).
     * @return JPanel chứa thanh công cụ.
     */
    private JPanel buildToolbar() {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);

        // Cụm trái: Tìm kiếm.
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        left.setOpaque(false);
        JButton btnSearch = createActionButton("Tìm");
        btnSearch.addActionListener(e -> refreshData()); // Gán hành động tìm kiếm.
        
        left.add(new JLabel("Mã/Tên KM:"));
        left.add(txtSearch);
        left.add(btnSearch);

        // Cụm phải: Thêm, Sửa, Đổi trạng thái.
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        right.setOpaque(false);

        JButton btnAdd = createActionButton("Thêm KM");
        btnAdd.setBackground(UiPalette.SUCCESS); // Màu nền xanh lá cây.
        btnAdd.setForeground(Color.WHITE); // Màu chữ trắng.
        btnAdd.addActionListener(e -> showDialog(null)); // Mở dialog thêm mới.

        JButton btnEdit = createActionButton("Sửa KM");
        btnEdit.addActionListener(e -> {
            PromotionItem item = getSelectedItem();
            if (item != null) {
                showDialog(item); // Mở dialog chỉnh sửa.
            }
        });

        JButton btnToggle = createActionButton("Khóa/Mở");
        btnToggle.addActionListener(e -> {
            PromotionItem item = getSelectedItem();
            if (item != null) {
                int confirm = JOptionPane.showConfirmDialog(getDialogParent(),
                        "Đổi trạng thái chương trình [" + item.promotionCode() + "]?",
                        "Xác nhận", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        promotionController.toggleStatus(item.id()); // Gọi controller để đổi trạng thái.
                        showInfo("Cập nhật trạng thái thành công.");
                        refreshData(); // Tải lại dữ liệu để cập nhật bảng.
                    } catch (Exception ex) {
                        showError("Lỗi cập nhật: " + ex.getMessage());
                    }
                }
            }
        });

        right.add(btnAdd);
        right.add(btnEdit);
        right.add(btnToggle);

        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    /**
     * Tải lại dữ liệu khuyến mãi từ controller và cập nhật bảng hiển thị.
     */
    private void refreshData() {
        try {
            rowData = promotionController.findAll(txtSearch.getText()); // Lấy dữ liệu từ controller.
            tableModel.setRowCount(0); // Xóa tất cả các hàng hiện có.

            // Duyệt qua danh sách dữ liệu và thêm từng mục vào bảng.
            for (PromotionItem i : rowData) {
                tableModel.addRow(new Object[]{
                        i.id(),
                        i.promotionCode(),
                        i.promotionName(),
                        i.discountType(),
                        String.format("%,.0f", i.discountValue()), // Định dạng số tiền/phần trăm giảm giá.
                        i.startDate() != null ? i.startDate().format(DATE_FMT) : "",
                        i.endDate() != null ? i.endDate().format(DATE_FMT) : "",
                        i.status().name(),
                        i.description()
                });
            }
        } catch (Exception ex) {
            showError("Lỗi tải DS Khuyến Mãi: " + ex.getMessage()); // Hiển thị lỗi.
        }
    }

    /**
     * Lấy đối tượng PromotionItem đang được chọn trong bảng.
     * @return PromotionItem được chọn, hoặc null nếu không có hàng nào được chọn hoặc không tìm thấy.
     */
    private PromotionItem getSelectedItem() {
        int view = table.getSelectedRow();
        if (view < 0) {
            showInfo("Vui lòng chọn 1 hạng mục để thao tác.");
            return null;
        }
        int modelIdx = table.convertRowIndexToModel(view); // Chuyển đổi chỉ số hàng từ view sang model.
        Long id = (Long) tableModel.getValueAt(modelIdx, 0); // Lấy ID từ cột ẩn.
        return rowData.stream().filter(p -> p.id().equals(id)).findFirst().orElse(null); // Tìm đối tượng tương ứng.
    }

    /**
     * Hiển thị dialog để thêm mới hoặc chỉnh sửa khuyến mãi.
     * @param editItem Đối tượng PromotionItem để chỉnh sửa, hoặc null nếu thêm mới.
     */
    private void showDialog(PromotionItem editItem) {
        SaveAction saveAction = editItem == null
                ? promotionController::createPromotion
                : req -> promotionController.updatePromotion(editItem.id(), req);
        PromotionDialog dialog = new PromotionDialog(getDialogWindow(), editItem, saveAction);
        dialog.setVisible(true); // Hiển thị dialog.

        dialog.getResult().ifPresent(req -> {
            showInfo(editItem == null
                    ? "Thêm mới khuyến mãi [" + req.promotionCode() + "] thành công."
                    : "Cập nhật thành công.");
            refreshData(); // Tải lại dữ liệu để cập nhật bảng.
        });
    }

    // =========================================================================
    // UI HELPER
    // =========================================================================

    /**
     * Tạo một nút hành động với phong cách nhất quán.
     * @param title Tiêu đề của nút.
     * @return JButton đã được định kiểu.
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
     * Tạo một JPanel chứa JTable với phong cách nhất quán.
     * @param tbl JTable cần bọc.
     * @return JPanel đã được định kiểu.
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
     * Hiển thị hộp thoại lỗi.
     * @param msg Thông báo lỗi.
     */
    private void showError(String msg) {
        JOptionPane.showMessageDialog(getDialogParent(), msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Hiển thị hộp thoại thông tin.
     * @param msg Thông báo thông tin.
     */
    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(getDialogParent(), msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Lấy component cha cho các hộp thoại, đảm bảo chúng hiển thị đúng vị trí.
     * @return Component cha.
     */
    private Component getDialogParent() {
        Component owner = DialogUiUtil.appDialogParent(this);
        return owner != null ? owner : this;
    }

    /**
     * Lấy cửa sổ cha cho các hộp thoại, đảm bảo chúng hiển thị đúng vị trí.
     * @return Window cha.
     */
    private Window getDialogWindow() {
        Component owner = DialogUiUtil.appDialogParent(this);
        if (owner instanceof Window) {
            return (Window) owner;
        }
        return SwingUtilities.getWindowAncestor(owner);
    }

    // =========================================================================
    // DIALOG THÊM / SỬA KHUYẾN MÃI
    // =========================================================================

    /**
     * Lớp nội tĩnh PromotionDialog là một JDialog dùng để thêm mới hoặc chỉnh sửa thông tin khuyến mãi.
     * Nó cung cấp các trường nhập liệu cho mã, tên, loại giảm giá, mức giảm, ngày bắt đầu, ngày kết thúc,
     * mô tả và trạng thái của khuyến mãi.
     */
    private static class PromotionDialog extends JDialog {
        private final JTextField txtCode = new JTextField(); // Trường nhập mã khuyến mãi.
        private final JTextField txtName = new JTextField(); // Trường nhập tên khuyến mãi.
        private final JComboBox<String> cbType = new JComboBox<>(new String[]{"PERCENTAGE", "FIXED_AMOUNT"}); // ComboBox chọn loại giảm giá.
        private final JTextField txtValue = new JTextField(); // Trường nhập mức giảm giá.
        private final JTextField txtStart = new JTextField(); // Trường nhập ngày bắt đầu.
        private final JTextField txtEnd = new JTextField(); // Trường nhập ngày kết thúc.
        private final JTextArea txtDesc = new JTextArea(); // Trường nhập mô tả.
        private final JComboBox<Status> cbStatus = new JComboBox<>(Status.values()); // ComboBox chọn trạng thái.

        private PromotionRequest result; // Đối tượng PromotionRequest được tạo sau khi người dùng lưu.

        /**
         * Constructor khởi tạo PromotionDialog.
         * @param owner Cửa sổ cha của dialog.
         * @param item Đối tượng PromotionItem để chỉnh sửa, hoặc null nếu thêm mới.
         */
        public PromotionDialog(Window owner, PromotionItem item, SaveAction saveAction) {
            super(owner, item == null ? "Thêm Khuyến Mãi (F13)" : "Chỉnh sửa: " + item.promotionCode(), ModalityType.APPLICATION_MODAL);

            JPanel form = new JPanel(new GridLayout(8, 2, 10, 10)); // Sử dụng GridLayout cho form nhập liệu.
            form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            form.add(new JLabel("Mã KM *")); form.add(txtCode);
            form.add(new JLabel("Tên KM *")); form.add(txtName);
            form.add(new JLabel("Loại giảm")); form.add(cbType);
            form.add(new JLabel("Mức (Số định mức)")); form.add(txtValue);
            form.add(new JLabel("Ngày Bắt đầu (dd/MM/yyyy) *")); form.add(txtStart);
            form.add(new JLabel("Ngày Kết thúc (dd/MM/yyyy) *")); form.add(txtEnd);
            form.add(new JLabel("Mô tả")); form.add(new JScrollPane(txtDesc)); // Bọc JTextArea trong JScrollPane.
            form.add(new JLabel("Trạng thái")); form.add(cbStatus);

            // Nếu là chỉnh sửa, điền dữ liệu hiện có vào các trường.
            if (item != null) {
                txtCode.setText(item.promotionCode());
                txtCode.setEditable(true); // Cho phép sửa mã khuyến mãi theo yêu cầu nghiệp vụ.
                txtName.setText(item.promotionName());
                cbType.setSelectedItem(item.discountType());
                txtValue.setText(item.discountValue().toPlainString());
                txtStart.setText(item.startDate().format(DATE_FMT));
                txtEnd.setText(item.endDate().format(DATE_FMT));
                txtDesc.setText(item.description());
                cbStatus.setSelectedItem(item.status());
            } else { // Nếu là thêm mới, đặt giá trị mặc định.
                txtStart.setText(LocalDate.now().format(DATE_FMT));
                txtEnd.setText(LocalDate.now().plusMonths(1).format(DATE_FMT));
                cbStatus.setSelectedItem(Status.ACTIVE);
            }

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10)); // Panel chứa các nút hành động.
            JButton btnCancel = new JButton("Hủy");
            JButton btnSubmit = new JButton(item == null ? "Tạo" : "Lưu Thay Đổi");
            btnSubmit.setBackground(UiPalette.PRIMARY);
            btnSubmit.setForeground(Color.WHITE);

            btnCancel.addActionListener(e -> dispose()); // Đóng dialog khi nhấn Hủy.
            btnSubmit.addActionListener(e -> {
                try {
                    // Phân tích cú pháp ngày bắt đầu và ngày kết thúc.
                    LocalDate start = LocalDate.parse(txtStart.getText().trim(), DATE_FMT);
                    LocalDate end = LocalDate.parse(txtEnd.getText().trim(), DATE_FMT);
                    // Lấy giá trị giảm giá, loại bỏ dấu phân cách hàng nghìn.
                    BigDecimal val = new BigDecimal(txtValue.getText().trim().replaceAll("[,.]", ""));
                    
                    if(val.compareTo(BigDecimal.ZERO) < 0) {
                        throw new RuntimeException("Giảm giá không được âm!");
                    }

                    // Tạo đối tượng PromotionRequest.
                    result = new PromotionRequest(
                            txtCode.getText().trim(),
                            txtName.getText().trim(),
                            (String) cbType.getSelectedItem(),
                            val,
                            start,
                            end,
                            txtDesc.getText().trim(),
                            (Status) cbStatus.getSelectedItem()
                    );
                    // Chỉ đóng dialog sau khi lưu thành công ở tầng nghiệp vụ.
                    saveAction.save(result);
                    dispose();
                } catch (DateTimeParseException dte) {
                    JOptionPane.showMessageDialog(DialogUiUtil.appDialogParent(this), "Định dạng ngày phải là dd/MM/yyyy", "Lỗi ngày tháng", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    result = null;
                    JOptionPane.showMessageDialog(DialogUiUtil.appDialogParent(this), "Dữ liệu nhập sai: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            });

            actions.add(btnSubmit);
            actions.add(btnCancel);

            setLayout(new BorderLayout());
            add(form, BorderLayout.CENTER);
            add(actions, BorderLayout.SOUTH);

            setSize(450, 480); // Đặt kích thước cố định.
            setLocationRelativeTo(DialogUiUtil.appDialogParent(owner)); // Đặt vị trí tương đối với cửa sổ cha.
        }

        /**
         * Trả về đối tượng PromotionRequest sau khi dialog đóng.
         * @return Optional chứa PromotionRequest nếu người dùng đã lưu, hoặc Optional.empty() nếu hủy.
         */
        public Optional<PromotionRequest> getResult() {
            return Optional.ofNullable(result);
        }
    }
}
