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

public class PromotionPanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final PromotionController promotionController;

    private final JTextField txtSearch = new JTextField(20);
    private final DefaultTableModel tableModel;
    private final JTable table;
    private List<PromotionItem> rowData = new ArrayList<>();

    public PromotionPanel(PromotionController promotionController) {
        this.promotionController = promotionController;

        setLayout(new BorderLayout(8, 8));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        String[] cols = {
                "ID", "Mã KM", "Tên KM", "Kiểu giảm", "Mức giảm", "Ngày BĐ", "Ngày KT", "Trạng thái", "Ghi chú"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);

        // Ẩn ID (Column 0)
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

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
        
        left.add(new JLabel("Mã/Tên KM:"));
        left.add(txtSearch);
        left.add(btnSearch);

        // Cụm phải: Thêm, Sửa, Đổi trạng thái
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        right.setOpaque(false);

        JButton btnAdd = createActionButton("Thêm KM");
        btnAdd.setBackground(UiPalette.SUCCESS);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.addActionListener(e -> showDialog(null));

        JButton btnEdit = createActionButton("Sửa KM");
        btnEdit.addActionListener(e -> {
            PromotionItem item = getSelectedItem();
            if (item != null) {
                showDialog(item);
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
                        promotionController.toggleStatus(item.id());
                        showInfo("Cập nhật trạng thái thành công.");
                        refreshData();
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

    private void refreshData() {
        try {
            rowData = promotionController.findAll(txtSearch.getText());
            tableModel.setRowCount(0);

            for (PromotionItem i : rowData) {
                tableModel.addRow(new Object[]{
                        i.id(),
                        i.promotionCode(),
                        i.promotionName(),
                        i.discountType(),
                        String.format("%,.0f", i.discountValue()),
                        i.startDate() != null ? i.startDate().format(DATE_FMT) : "",
                        i.endDate() != null ? i.endDate().format(DATE_FMT) : "",
                        i.status().name(),
                        i.description()
                });
            }
        } catch (Exception ex) {
            showError("Lỗi tải DS Khuyến Mãi: " + ex.getMessage());
        }
    }

    private PromotionItem getSelectedItem() {
        int view = table.getSelectedRow();
        if (view < 0) {
            showInfo("Vui lòng chọn 1 hạng mục để thao tác.");
            return null;
        }
        int modelIdx = table.convertRowIndexToModel(view);
        Long id = (Long) tableModel.getValueAt(modelIdx, 0);
        return rowData.stream().filter(p -> p.id().equals(id)).findFirst().orElse(null);
    }

    private void showDialog(PromotionItem editItem) {
        PromotionDialog dialog = new PromotionDialog(getDialogWindow(), editItem);
        dialog.setVisible(true);

        dialog.getResult().ifPresent(req -> {
            try {
                if (editItem == null) {
                    promotionController.createPromotion(req);
                    showInfo("Thêm mới khuyến mãi [" + req.promotionCode() + "] thành công.");
                } else {
                    promotionController.updatePromotion(editItem.id(), req);
                    showInfo("Cập nhật thành công.");
                }
                refreshData();
            } catch (Exception ex) {
                showError("Lỗi: " + ex.getMessage());
            }
        });
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
        Component owner = DialogUiUtil.appDialogParent(this);
        if (owner instanceof Window) {
            return (Window) owner;
        }
        return SwingUtilities.getWindowAncestor(owner);
    }

    // =========================================================================
    // DIALOG THÊM / SỬA KHUYẾN MÃI
    // =========================================================================

    private static class PromotionDialog extends JDialog {
        private final JTextField txtCode = new JTextField();
        private final JTextField txtName = new JTextField();
        private final JComboBox<String> cbType = new JComboBox<>(new String[]{"PERCENTAGE", "FIXED_AMOUNT"});
        private final JTextField txtValue = new JTextField();
        private final JTextField txtStart = new JTextField();
        private final JTextField txtEnd = new JTextField();
        private final JTextArea txtDesc = new JTextArea();
        private final JComboBox<Status> cbStatus = new JComboBox<>(Status.values());

        private PromotionRequest result;

        public PromotionDialog(Window owner, PromotionItem item) {
            super(owner, item == null ? "Thêm Khuyến Mãi (F13)" : "Chỉnh sửa: " + item.promotionCode(), ModalityType.APPLICATION_MODAL);

            JPanel form = new JPanel(new GridLayout(8, 2, 10, 10));
            form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            form.add(new JLabel("Mã KM *")); form.add(txtCode);
            form.add(new JLabel("Tên KM *")); form.add(txtName);
            form.add(new JLabel("Loại giảm")); form.add(cbType);
            form.add(new JLabel("Mức (Số định mức)")); form.add(txtValue);
            form.add(new JLabel("Ngày Bắt đầu (dd/MM/yyyy) *")); form.add(txtStart);
            form.add(new JLabel("Ngày Kết thúc (dd/MM/yyyy) *")); form.add(txtEnd);
            form.add(new JLabel("Mô tả")); form.add(new JScrollPane(txtDesc));
            form.add(new JLabel("Trạng thái")); form.add(cbStatus);

            if (item != null) {
                txtCode.setText(item.promotionCode());
                txtCode.setEditable(false); // Không cho sửa mã vì nó unique và có logic riêng
                txtName.setText(item.promotionName());
                cbType.setSelectedItem(item.discountType());
                txtValue.setText(item.discountValue().toPlainString());
                txtStart.setText(item.startDate().format(DATE_FMT));
                txtEnd.setText(item.endDate().format(DATE_FMT));
                txtDesc.setText(item.description());
                cbStatus.setSelectedItem(item.status());
            } else {
                txtStart.setText(LocalDate.now().format(DATE_FMT));
                txtEnd.setText(LocalDate.now().plusMonths(1).format(DATE_FMT));
                cbStatus.setSelectedItem(Status.ACTIVE);
            }

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            JButton btnCancel = new JButton("Hủy");
            JButton btnSubmit = new JButton(item == null ? "Tạo" : "Lưu Thay Đổi");
            btnSubmit.setBackground(UiPalette.PRIMARY);
            btnSubmit.setForeground(Color.WHITE);

            btnCancel.addActionListener(e -> dispose());
            btnSubmit.addActionListener(e -> {
                try {
                    LocalDate start = LocalDate.parse(txtStart.getText().trim(), DATE_FMT);
                    LocalDate end = LocalDate.parse(txtEnd.getText().trim(), DATE_FMT);
                    BigDecimal val = new BigDecimal(txtValue.getText().trim().replaceAll("[,.]", ""));
                    
                    if(val.compareTo(BigDecimal.ZERO) < 0) {
                        throw new RuntimeException("Giảm giá không được âm!");
                    }

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
                    dispose();
                } catch (DateTimeParseException dte) {
                    JOptionPane.showMessageDialog(DialogUiUtil.appDialogParent(this), "Định dạng ngày phải là dd/MM/yyyy", "Lỗi ngày tháng", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(DialogUiUtil.appDialogParent(this), "Dữ liệu nhập sai: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            });

            actions.add(btnSubmit);
            actions.add(btnCancel);

            setLayout(new BorderLayout());
            add(form, BorderLayout.CENTER);
            add(actions, BorderLayout.SOUTH);

            setSize(450, 480);
            setLocationRelativeTo(DialogUiUtil.appDialogParent(owner));
        }

        public Optional<PromotionRequest> getResult() {
            return Optional.ofNullable(result);
        }
    }
}
