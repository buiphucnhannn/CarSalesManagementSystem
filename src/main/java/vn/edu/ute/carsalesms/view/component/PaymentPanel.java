package vn.edu.ute.carsalesms.view.component;

import vn.edu.ute.carsalesms.controller.PaymentController;
import vn.edu.ute.carsalesms.controller.SaleOrderController;
import vn.edu.ute.carsalesms.model.dto.PaymentItem;
import vn.edu.ute.carsalesms.model.dto.PaymentRequest;
import vn.edu.ute.carsalesms.model.dto.SaleOrderItem;
import vn.edu.ute.carsalesms.model.enums.OrderStatus;
import vn.edu.ute.carsalesms.model.enums.PaymentMethod;
import vn.edu.ute.carsalesms.view.theme.DialogUiUtil;
import vn.edu.ute.carsalesms.view.theme.UiPalette;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Panel quản lý Lịch sử Thanh toán & Ghi nhận Thanh toán nhiều đợt.
 */
public class PaymentPanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    private final SaleOrderController orderController;
    private final PaymentController paymentController;

    // --- Order List (Nửa trên) ---
    private final JTextField searchOrderField = new JTextField();
    private final JComboBox<String> statusFilter = new JComboBox<>(new String[]{"Tất cả", "PENDING", "CONFIRMED", "PAID", "CANCELLED"});
    private final DefaultTableModel orderTableModel;
    private final JTable orderTable;
    private List<SaleOrderItem> orderRows = new ArrayList<>();

    // --- Payment List (Nửa dưới) ---
    private final DefaultTableModel paymentTableModel;
    private final JTable paymentTable;
    private List<PaymentItem> paymentRows = new ArrayList<>();
    private final JButton btnMakePayment;

    public PaymentPanel(SaleOrderController orderController, PaymentController paymentController) {
        this.orderController = Objects.requireNonNull(orderController);
        this.paymentController = Objects.requireNonNull(paymentController);

        setLayout(new BorderLayout(0, 8));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // -- Order Table
        String[] oCols = {"ID", "Mã đơn", "Khách hàng", "Tổng cần thu", "Trạng thái"};
        orderTableModel = new DefaultTableModel(oCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        orderTable = new JTable(orderTableModel);
        TableRowSorter<DefaultTableModel> oSorter = new TableRowSorter<>(orderTableModel);
        orderTable.setRowSorter(oSorter);
        
        // Cấu hình cột Order
        orderTable.getColumnModel().getColumn(0).setMinWidth(0);
        orderTable.getColumnModel().getColumn(0).setMaxWidth(0);
        orderTable.getColumnModel().getColumn(3).setCellRenderer(new RightAlignRenderer());
        orderTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                refreshPayments();
            }
        });

        // -- Payment Table
        String[] pCols = {"Mã TT", "Mã Đơn", "Ngày", "Số tiền trả", "Phương thức", "Trạng thái", "Tham chiếu"};
        paymentTableModel = new DefaultTableModel(pCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        paymentTable = new JTable(paymentTableModel);
        paymentTable.getColumnModel().getColumn(3).setCellRenderer(new RightAlignRenderer());

        btnMakePayment = createActionButton("Ghi nhận Thanh toán");
        btnMakePayment.setBackground(UiPalette.SUCCESS);
        btnMakePayment.setForeground(Color.WHITE);
        btnMakePayment.setEnabled(false); // Enable khi chọn đơn

        btnMakePayment.addActionListener(e -> showPaymentDialog());

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(buildOrderSection());
        splitPane.setBottomComponent(buildPaymentSection());
        splitPane.setResizeWeight(0.5); // Chia đôi
        splitPane.setDividerSize(4);

        add(splitPane, BorderLayout.CENTER);

        refreshOrders();
    }

    private JPanel buildOrderSection() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createTitledBorder("Chọn Đơn bán hàng"));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        searchOrderField.setPreferredSize(new Dimension(200, 30));
        statusFilter.setPreferredSize(new Dimension(110, 30));

        JButton btnSearch = createActionButton("Tìm");
        btnSearch.addActionListener(e -> refreshOrders());
        statusFilter.addActionListener(e -> refreshOrders());

        toolbar.add(new JLabel("Mã/Tên:"));
        toolbar.add(searchOrderField);
        toolbar.add(new JLabel("Trạng thái:"));
        toolbar.add(statusFilter);
        toolbar.add(btnSearch);

        p.add(toolbar, BorderLayout.NORTH);
        p.add(createTableCard(orderTable), BorderLayout.CENTER);
        return p;
    }

    private JPanel buildPaymentSection() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createTitledBorder("Lịch sử Thanh toán"));

        JPanel topP = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topP.setOpaque(false);
        topP.add(btnMakePayment);

        p.add(topP, BorderLayout.NORTH);
        p.add(createTableCard(paymentTable), BorderLayout.CENTER);
        return p;
    }

    private void refreshOrders() {
        try {
            OrderStatus fStatus = null;
            String stStr = (String) statusFilter.getSelectedItem();
            if (!"Tất cả".equals(stStr)) {
                fStatus = OrderStatus.valueOf(stStr);
            }
            orderRows = orderController.findOrders(searchOrderField.getText(), fStatus);
            orderTableModel.setRowCount(0);

            for (SaleOrderItem o : orderRows) {
                orderTableModel.addRow(new Object[]{
                        o.id(),
                        o.orderCode(),
                        o.customerName(),
                        String.format("%,.0f", o.finalAmount()),
                        o.orderStatus().name()
                });
            }
            refreshPayments(); // Xóa bảng dưới
        } catch (Exception ex) {
            showError("Lỗi tải đơn: " + ex.getMessage());
        }
    }

    private void refreshPayments() {
        int view = orderTable.getSelectedRow();
        if (view < 0) {
            paymentTableModel.setRowCount(0);
            btnMakePayment.setEnabled(false);
            return;
        }

        int model = orderTable.convertRowIndexToModel(view);
        SaleOrderItem order = orderRows.get(model);

        try {
            paymentRows = paymentController.findPaymentsByOrderId(order.id());
            paymentTableModel.setRowCount(0);

            BigDecimal paidAmt = BigDecimal.ZERO;
            for (PaymentItem p : paymentRows) {
                paymentTableModel.addRow(new Object[]{
                        p.paymentCode(),
                        p.orderCode(),
                        p.paymentDate() == null ? "" : p.paymentDate().format(DATE_FMT),
                        String.format("%,.0f", p.amount()),
                        p.paymentMethod().name(),
                        p.paymentStatus().name(),
                        p.transactionReference() != null ? p.transactionReference() : ""
                });
                paidAmt = paidAmt.add(p.amount());
            }

            // Kiểm tra xem Đơn hàng có dính một phát Trả Góp (Lần nạp đầu tiên) hay chưa
            boolean hasInstallment = paymentRows.stream().anyMatch(p -> p.paymentMethod() == PaymentMethod.INSTALLMENT);

            // Bật tắt Button với Điều kiện & Giải thích (SOLID - UX)
            if (order.orderStatus() == OrderStatus.PAID || order.orderStatus() == OrderStatus.CANCELLED) {
                btnMakePayment.setEnabled(false);
                btnMakePayment.setToolTipText(null);
            } else if (hasInstallment) {
                btnMakePayment.setEnabled(false);
                btnMakePayment.setToolTipText("Đơn này đang lập Trả góp. Bạn PHẢI qua thẻ 'Quản lý Trả Góp' để đóng tiền đợt sau!");
            } else {
                btnMakePayment.setEnabled(true);
                btnMakePayment.setToolTipText("Thanh toán tiền Cọc hoặc Tiền mặt toàn phần.");
            }

        } catch (Exception ex) {
            showError("Lỗi tải lịch sử thanh toán: " + ex.getMessage());
        }
    }

    private void showPaymentDialog() {
        int view = orderTable.getSelectedRow();
        if (view < 0) return;
        SaleOrderItem order = orderRows.get(orderTable.convertRowIndexToModel(view));

        // Tính số tiền còn lại phải trả
        BigDecimal totalPaid = paymentRows.stream().map(PaymentItem::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal amountDues = order.finalAmount().subtract(totalPaid);

        PaymentDialog dialog = new PaymentDialog(DialogUiUtil.appDialogParent(this), order, amountDues);
        dialog.setVisible(true);

        dialog.getResult().ifPresent(req -> {
            try {
                paymentController.addPayment(req);
                showInfo();
                refreshOrders(); // Render cập nhật trạng thái
                // Reselect
                for (int i=0; i<orderTable.getRowCount(); i++) {
                    if (orderTable.getValueAt(i, 1).equals(order.orderCode())) {
                        orderTable.setRowSelectionInterval(i, i);
                        break;
                    }
                }
            } catch (Exception ex) {
                showError("Lỗi: " + ex.getMessage());
            }
        });
    }

    // --- Helpers ---
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

    private void showError(String msg) {
        JOptionPane.showMessageDialog(getDialogParent(), msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo() {
        JOptionPane.showMessageDialog(getDialogParent(), "Ghi nhận thanh toán thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private Component getDialogParent() {
        Component owner = DialogUiUtil.appDialogParent(this);
        return owner != null ? owner : this;
    }

    static class RightAlignRenderer extends DefaultTableCellRenderer {
        public RightAlignRenderer() {
            setHorizontalAlignment(SwingConstants.RIGHT);
        }
    }

    // =========================================================================
    // Dialog Ghi nhận Thanh toán
    // =========================================================================
    private static final class PaymentDialog extends JDialog {

        private final JTextField txtAmount = new JTextField();
        private final JComboBox<PaymentMethod> cbMethod = new JComboBox<>(PaymentMethod.values());
        private final JTextField txtRef = new JTextField();
        private final JTextField txtNote = new JTextField();

        private final JSpinner spinMonths = new JSpinner(new SpinnerNumberModel(6, 1, 60, 1));
        private final JLabel lblMonths = new JLabel("Số kỳ trả góp (Tháng)");

        private PaymentRequest result;

        private PaymentDialog(Component owner, SaleOrderItem order, BigDecimal amountDues) {
            super(resolveOwnerWindow(owner), "Thanh toán Đơn " + order.orderCode(), ModalityType.APPLICATION_MODAL);
            setLayout(new BorderLayout(8, 8));

            JPanel info = new JPanel(new GridLayout(2, 1));
            info.setBorder(BorderFactory.createEmptyBorder(8, 12, 0, 12));
            info.add(new JLabel("Tổng cần trả: " + String.format("%,.0f đ", order.finalAmount())));
            info.add(new JLabel("Còn thiếu: " + String.format("%,.0f đ", amountDues)));

            JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
            form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            
            form.add(new JLabel("Số tiền trả *"));
            // Loại bỏ phần thập phân (.00) để ngăn lỗi parse bằng cách xoá dấu , .
            txtAmount.setText(amountDues.setScale(0, RoundingMode.HALF_UP).toPlainString());
            form.add(txtAmount);

            form.add(new JLabel("Hình thức *"));
            cbMethod.setSelectedItem(order.paymentMethod()); // Gợi ý mặc định theo order
            form.add(cbMethod);

            form.add(lblMonths);
            form.add(spinMonths);
            
            form.add(new JLabel("Mã tham chiếu (Bank/Momo)"));
            form.add(txtRef);

            form.add(new JLabel("Ghi chú"));
            form.add(txtNote);
            
            // Xử lý bật tắt tùy chọn trả góp
            updateMonthsVisibility();
            cbMethod.addActionListener(e -> updateMonthsVisibility());

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
            JButton btnCancel = new JButton("Hủy");
            JButton btnSave = new JButton("Xác nhận");
            btnSave.setBackground(UiPalette.SUCCESS);
            btnSave.setForeground(Color.WHITE);

            btnCancel.addActionListener(e -> dispose());
            btnSave.addActionListener(e -> {
                try {
                    BigDecimal amt = new BigDecimal(txtAmount.getText().replaceAll("[,.]", ""));
                    PaymentMethod method = (PaymentMethod) cbMethod.getSelectedItem();
                    Integer months = method == PaymentMethod.INSTALLMENT ? (Integer) spinMonths.getValue() : null;

                    result = new PaymentRequest(
                            order.id(),
                            amt,
                            method,
                            txtRef.getText(),
                            txtNote.getText(),
                            months
                    );
                    dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(DialogUiUtil.appDialogParent(this), "Số tiền không hợp lệ.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                }
            });

            actions.add(btnSave);
            actions.add(btnCancel);

            add(info, BorderLayout.NORTH);
            add(form, BorderLayout.CENTER);
            add(actions, BorderLayout.SOUTH);

            pack();
            setSize(450, 360);
            setLocationRelativeTo(owner);
        }

        private static Window resolveOwnerWindow(Component owner) {
            if (owner instanceof Window) {
                return (Window) owner;
            }
            return owner == null ? null : SwingUtilities.getWindowAncestor(owner);
        }

        private void updateMonthsVisibility() {
            boolean isInstallment = (cbMethod.getSelectedItem() == PaymentMethod.INSTALLMENT);
            lblMonths.setEnabled(isInstallment);
            spinMonths.setEnabled(isInstallment);
        }

        public Optional<PaymentRequest> getResult() {
            return Optional.ofNullable(result);
        }
    }
}
