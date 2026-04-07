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
 * Giao diện này được chia thành hai phần chính:
 * 1. Nửa trên: Hiển thị danh sách các đơn bán hàng (Sale Orders).
 * 2. Nửa dưới: Hiển thị lịch sử thanh toán chi tiết cho đơn hàng được chọn ở nửa trên.
 * Người dùng có thể tìm kiếm đơn hàng, xem lịch sử thanh toán và ghi nhận các khoản thanh toán mới.
 */
public class PaymentPanel extends JPanel {

    @FunctionalInterface
    private interface SaveAction {
        void save(PaymentRequest request) throws Exception;
    }

    // Định dạng ngày và giờ để hiển thị trong bảng.
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    private final SaleOrderController orderController; // Controller để quản lý các đơn bán hàng.
    private final PaymentController paymentController; // Controller để quản lý các thanh toán.

    // --- Order List (Nửa trên) ---
    private final JTextField searchOrderField = new JTextField(); // Ô tìm kiếm đơn hàng.
    // ComboBox để lọc đơn hàng theo trạng thái.
    private final JComboBox<String> statusFilter = new JComboBox<>(new String[]{"Tất cả", "PENDING", "CONFIRMED", "PAID", "CANCELLED"});
    private final DefaultTableModel orderTableModel; // Model dữ liệu cho bảng đơn hàng.
    private final JTable orderTable; // Bảng hiển thị danh sách đơn hàng.
    private List<SaleOrderItem> orderRows = new ArrayList<>(); // Danh sách các đối tượng SaleOrderItem hiện tại.

    // --- Payment List (Nửa dưới) ---
    private final DefaultTableModel paymentTableModel; // Model dữ liệu cho bảng thanh toán.
    private final JTable paymentTable; // Bảng hiển thị danh sách thanh toán.
    private List<PaymentItem> paymentRows = new ArrayList<>(); // Danh sách các đối tượng PaymentItem hiện tại.
    private final JButton btnMakePayment; // Nút để ghi nhận thanh toán mới.

    /**
     * Constructor khởi tạo PaymentPanel.
     * @param orderController Controller cho các đơn bán hàng.
     * @param paymentController Controller cho các thanh toán.
     */
    public PaymentPanel(SaleOrderController orderController, PaymentController paymentController) {
        this.orderController = Objects.requireNonNull(orderController);
        this.paymentController = Objects.requireNonNull(paymentController);

        setLayout(new BorderLayout(0, 8));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // -- Bảng Đơn hàng (Order Table)
        String[] oCols = {"ID", "Mã đơn", "Khách hàng", "Tổng cần thu", "Trạng thái"};
        orderTableModel = new DefaultTableModel(oCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; } // Không cho phép chỉnh sửa trực tiếp.
        };
        orderTable = new JTable(orderTableModel);
        TableRowSorter<DefaultTableModel> oSorter = new TableRowSorter<>(orderTableModel);
        orderTable.setRowSorter(oSorter);
        
        // Cấu hình cột cho bảng đơn hàng.
        orderTable.getColumnModel().getColumn(0).setMinWidth(0); // Ẩn cột ID.
        orderTable.getColumnModel().getColumn(0).setMaxWidth(0);
        orderTable.getColumnModel().getColumn(3).setCellRenderer(new RightAlignRenderer()); // Căn phải cho cột "Tổng cần thu".
        // Thêm listener để khi chọn một đơn hàng, lịch sử thanh toán sẽ được làm mới.
        orderTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                refreshPayments();
            }
        });

        // -- Bảng Thanh toán (Payment Table)
        String[] pCols = {"Mã TT", "Mã Đơn", "Ngày", "Số tiền trả", "Phương thức", "Trạng thái", "Tham chiếu"};
        paymentTableModel = new DefaultTableModel(pCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; } // Không cho phép chỉnh sửa trực tiếp.
        };
        paymentTable = new JTable(paymentTableModel);
        paymentTable.getColumnModel().getColumn(3).setCellRenderer(new RightAlignRenderer()); // Căn phải cho cột "Số tiền trả".

        btnMakePayment = createActionButton("Ghi nhận Thanh toán");
        btnMakePayment.setBackground(UiPalette.SUCCESS);
        btnMakePayment.setForeground(Color.WHITE);
        btnMakePayment.setEnabled(false); // Ban đầu vô hiệu hóa nút này, chỉ kích hoạt khi có đơn hàng được chọn.

        btnMakePayment.addActionListener(e -> showPaymentDialog()); // Gán hành động mở dialog thanh toán.

        // Chia panel thành hai phần dọc bằng JSplitPane.
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(buildOrderSection()); // Phần trên là khu vực đơn hàng.
        splitPane.setBottomComponent(buildPaymentSection()); // Phần dưới là khu vực thanh toán.
        splitPane.setResizeWeight(0.5); // Chia đôi không gian.
        splitPane.setDividerSize(4); // Kích thước đường phân chia.

        add(splitPane, BorderLayout.CENTER);

        refreshOrders(); // Tải dữ liệu đơn hàng ban đầu.
    }

    /**
     * Xây dựng phần giao diện cho khu vực hiển thị đơn hàng.
     * @return JPanel chứa thanh công cụ tìm kiếm và bảng đơn hàng.
     */
    private JPanel buildOrderSection() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createTitledBorder("Chọn Đơn bán hàng")); // Đặt tiêu đề cho phần này.

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        searchOrderField.setPreferredSize(new Dimension(200, 30));
        statusFilter.setPreferredSize(new Dimension(110, 30));

        JButton btnSearch = createActionButton("Tìm");
        btnSearch.addActionListener(e -> refreshOrders()); // Gán hành động tìm kiếm.
        statusFilter.addActionListener(e -> refreshOrders()); // Gán hành động lọc theo trạng thái.

        toolbar.add(new JLabel("Mã/Tên:"));
        toolbar.add(searchOrderField);
        toolbar.add(new JLabel("Trạng thái:"));
        toolbar.add(statusFilter);
        toolbar.add(btnSearch);

        p.add(toolbar, BorderLayout.NORTH);
        p.add(createTableCard(orderTable), BorderLayout.CENTER); // Bọc bảng trong một card có style.
        return p;
    }

    /**
     * Xây dựng phần giao diện cho khu vực hiển thị lịch sử thanh toán.
     * @return JPanel chứa nút "Ghi nhận Thanh toán" và bảng lịch sử thanh toán.
     */
    private JPanel buildPaymentSection() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createTitledBorder("Lịch sử Thanh toán")); // Đặt tiêu đề cho phần này.

        JPanel topP = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topP.setOpaque(false);
        topP.add(btnMakePayment); // Thêm nút "Ghi nhận Thanh toán".

        p.add(topP, BorderLayout.NORTH);
        p.add(createTableCard(paymentTable), BorderLayout.CENTER); // Bọc bảng trong một card có style.
        return p;
    }

    /**
     * Tải lại danh sách đơn hàng từ controller và cập nhật bảng đơn hàng.
     */
    private void refreshOrders() {
        try {
            OrderStatus fStatus = null;
            String stStr = (String) statusFilter.getSelectedItem();
            if (!"Tất cả".equals(stStr)) {
                fStatus = OrderStatus.valueOf(stStr); // Chuyển đổi chuỗi trạng thái thành enum.
            }
            orderRows = orderController.findOrders(searchOrderField.getText(), fStatus); // Lấy đơn hàng từ controller.
            orderTableModel.setRowCount(0); // Xóa dữ liệu cũ.

            // Thêm dữ liệu mới vào bảng đơn hàng.
            for (SaleOrderItem o : orderRows) {
                orderTableModel.addRow(new Object[]{
                        o.id(),
                        o.orderCode(),
                        o.customerName(),
                        String.format("%,.0f", o.finalAmount()), // Định dạng số tiền.
                        o.orderStatus().name()
                });
            }
            refreshPayments(); // Làm mới bảng thanh toán (sẽ xóa nếu không có đơn hàng được chọn).
        } catch (Exception ex) {
            showError("Lỗi tải đơn: " + ex.getMessage()); // Hiển thị lỗi.
        }
    }

    /**
     * Tải lại lịch sử thanh toán cho đơn hàng được chọn và cập nhật bảng thanh toán.
     * Đồng thời quản lý trạng thái của nút "Ghi nhận Thanh toán".
     */
    private void refreshPayments() {
        int view = orderTable.getSelectedRow();
        if (view < 0) { // Nếu không có đơn hàng nào được chọn.
            paymentTableModel.setRowCount(0); // Xóa bảng thanh toán.
            btnMakePayment.setEnabled(false); // Vô hiệu hóa nút thanh toán.
            return;
        }

        int model = orderTable.convertRowIndexToModel(view);
        SaleOrderItem order = orderRows.get(model); // Lấy đơn hàng được chọn.

        try {
            paymentRows = paymentController.findPaymentsByOrderId(order.id()); // Lấy lịch sử thanh toán từ controller.
            paymentTableModel.setRowCount(0); // Xóa dữ liệu cũ.

            BigDecimal paidAmt = BigDecimal.ZERO;
            // Thêm dữ liệu mới vào bảng thanh toán và tính tổng số tiền đã trả.
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

            // Kiểm tra xem đơn hàng đã có khoản trả góp nào chưa.
            boolean hasInstallment = paymentRows.stream().anyMatch(p -> p.paymentMethod() == PaymentMethod.INSTALLMENT);

            // Bật/tắt nút "Ghi nhận Thanh toán" dựa trên trạng thái đơn hàng và phương thức thanh toán.
            if (order.orderStatus() == OrderStatus.PAID || order.orderStatus() == OrderStatus.CANCELLED) {
                btnMakePayment.setEnabled(false);
                btnMakePayment.setToolTipText(null); // Xóa tooltip.
            } else if (hasInstallment) {
                btnMakePayment.setEnabled(false);
                btnMakePayment.setToolTipText("Đơn này đang lập Trả góp. Bạn PHẢI qua thẻ 'Quản lý Trả Góp' để đóng tiền đợt sau!");
            } else {
                btnMakePayment.setEnabled(true);
                btnMakePayment.setToolTipText("Thanh toán tiền Cọc hoặc Tiền mặt toàn phần.");
            }

        } catch (Exception ex) {
            showError("Lỗi tải lịch sử thanh toán: " + ex.getMessage()); // Hiển thị lỗi.
        }
    }

    /**
     * Hiển thị dialog để ghi nhận một khoản thanh toán mới cho đơn hàng được chọn.
     */
    private void showPaymentDialog() {
        int view = orderTable.getSelectedRow();
        if (view < 0) return; // Không làm gì nếu không có đơn hàng nào được chọn.
        SaleOrderItem order = orderRows.get(orderTable.convertRowIndexToModel(view)); // Lấy đơn hàng được chọn.

        // Tính số tiền còn lại phải trả.
        BigDecimal totalPaid = paymentRows.stream().map(PaymentItem::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal amountDues = order.finalAmount().subtract(totalPaid);

        SaveAction saveAction = paymentController::addPayment;
        PaymentDialog dialog = new PaymentDialog(DialogUiUtil.appDialogParent(this), order, amountDues, saveAction);
        dialog.setVisible(true); // Hiển thị dialog.

        dialog.getResult().ifPresent(req -> {
            showInfo(); // Hiển thị thông báo thành công.
            refreshOrders(); // Làm mới danh sách đơn hàng để cập nhật trạng thái.
            // Chọn lại đơn hàng vừa thao tác để hiển thị lịch sử thanh toán của nó.
            for (int i=0; i<orderTable.getRowCount(); i++) {
                if (orderTable.getValueAt(i, 1).equals(order.orderCode())) {
                    orderTable.setRowSelectionInterval(i, i);
                    break;
                }
            }
        });
    }

    // --- Các phương thức trợ giúp ---
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
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return btn;
    }

    /**
     * Bọc một JTable trong một card có đường viền và phong cách nhất quán.
     * @param tbl JTable cần bọc.
     * @return JPanel chứa bảng đã được định kiểu.
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
     * Hiển thị hộp thoại lỗi.
     * @param msg Thông báo lỗi.
     */
    private void showError(String msg) {
        JOptionPane.showMessageDialog(getDialogParent(), msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Hiển thị hộp thoại thông báo thành công.
     */
    private void showInfo() {
        JOptionPane.showMessageDialog(getDialogParent(), "Ghi nhận thanh toán thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
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
     * Renderer để căn phải nội dung trong ô bảng.
     */
    static class RightAlignRenderer extends DefaultTableCellRenderer {
        public RightAlignRenderer() {
            setHorizontalAlignment(SwingConstants.RIGHT);
        }
    }

    // =========================================================================
    // Dialog Ghi nhận Thanh toán
    // =========================================================================
    /**
     * Lớp nội tĩnh cho dialog ghi nhận một khoản thanh toán mới.
     */
    private static final class PaymentDialog extends JDialog {

        private final JTextField txtAmount = new JTextField(); // Trường nhập số tiền thanh toán.
        private final JComboBox<PaymentMethod> cbMethod = new JComboBox<>(PaymentMethod.values()); // ComboBox chọn phương thức thanh toán.
        private final JTextField txtRef = new JTextField(); // Trường nhập mã tham chiếu giao dịch.
        private final JTextField txtNote = new JTextField(); // Trường nhập ghi chú.

        private final JSpinner spinMonths = new JSpinner(new SpinnerNumberModel(6, 1, 60, 1)); // Spinner cho số kỳ trả góp.
        private final JLabel lblMonths = new JLabel("Số kỳ trả góp (Tháng)"); // Label cho spinner số kỳ trả góp.

        private PaymentRequest result; // Đối tượng PaymentRequest được tạo sau khi người dùng lưu.
        private final SaveAction saveAction;

        /**
         * Constructor khởi tạo PaymentDialog.
         * @param owner Component cha của dialog.
         * @param order Đơn hàng liên quan đến thanh toán.
         * @param amountDues Số tiền còn thiếu cần thanh toán.
         */
        private PaymentDialog(Component owner, SaleOrderItem order, BigDecimal amountDues, SaveAction saveAction) {
            super(resolveOwnerWindow(owner), "Thanh toán Đơn " + order.orderCode(), ModalityType.APPLICATION_MODAL);
            this.saveAction = saveAction;
            setLayout(new BorderLayout(8, 8));

            JPanel info = new JPanel(new GridLayout(2, 1));
            info.setBorder(BorderFactory.createEmptyBorder(8, 12, 0, 12));
            info.add(new JLabel("Tổng cần trả: " + String.format("%,.0f đ", order.finalAmount())));
            info.add(new JLabel("Còn thiếu: " + String.format("%,.0f đ", amountDues)));

            JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
            form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            
            form.add(new JLabel("Số tiền trả *"));
            // Đặt giá trị mặc định là số tiền còn thiếu, loại bỏ phần thập phân và dấu phân cách hàng nghìn để dễ parse.
            txtAmount.setText(amountDues.setScale(0, RoundingMode.HALF_UP).toPlainString());
            form.add(txtAmount);

            form.add(new JLabel("Hình thức *"));
            cbMethod.setSelectedItem(order.paymentMethod()); // Gợi ý phương thức thanh toán mặc định theo đơn hàng.
            form.add(cbMethod);

            form.add(lblMonths);
            form.add(spinMonths);
            
            form.add(new JLabel("Mã tham chiếu (Bank/Momo)"));
            form.add(txtRef);

            form.add(new JLabel("Ghi chú"));
            form.add(txtNote);
            
            // Xử lý bật/tắt tùy chọn trả góp dựa trên phương thức thanh toán được chọn.
            updateMonthsVisibility();
            cbMethod.addActionListener(e -> updateMonthsVisibility());

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
            JButton btnCancel = new JButton("Hủy");
            JButton btnSave = new JButton("Xác nhận");
            btnSave.setBackground(UiPalette.SUCCESS);
            btnSave.setForeground(Color.WHITE);

            btnCancel.addActionListener(e -> dispose()); // Đóng dialog khi nhấn Hủy.
            btnSave.addActionListener(e -> {
                try {
                    // Lấy số tiền từ trường nhập liệu, loại bỏ dấu phân cách hàng nghìn.
                    BigDecimal amt = new BigDecimal(txtAmount.getText().replaceAll("[,.]", ""));
                    PaymentMethod method = (PaymentMethod) cbMethod.getSelectedItem();
                    // Lấy số tháng trả góp nếu phương thức là INSTALLMENT, ngược lại là null.
                    Integer months = method == PaymentMethod.INSTALLMENT ? (Integer) spinMonths.getValue() : null;

                    PaymentRequest request = new PaymentRequest(
                            order.id(),
                            amt,
                            method,
                            txtRef.getText(),
                            txtNote.getText(),
                            months
                    );
                    // Chỉ đóng khi service xử lý thành công.
                    saveAction.save(request);
                    result = request;
                    dispose();
                } catch (Exception ex) {
                    result = null;
                    JOptionPane.showMessageDialog(DialogUiUtil.appDialogParent(this),
                            "Dữ liệu không hợp lệ: " + ex.getMessage(),
                            "Lỗi nhập liệu",
                            JOptionPane.ERROR_MESSAGE);
                }
            });

            actions.add(btnSave);
            actions.add(btnCancel);

            add(info, BorderLayout.NORTH);
            add(form, BorderLayout.CENTER);
            add(actions, BorderLayout.SOUTH);

            pack(); // Tự động điều chỉnh kích thước dialog.
            setSize(450, 360); // Đặt kích thước cố định.
            setLocationRelativeTo(owner); // Đặt dialog ở giữa component cha.
        }

        /**
         * Giải quyết cửa sổ cha cho dialog.
         * @param owner Component cha.
         * @return Window cha.
         */
        private static Window resolveOwnerWindow(Component owner) {
            if (owner instanceof Window) {
                return (Window) owner;
            }
            return owner == null ? null : SwingUtilities.getWindowAncestor(owner);
        }

        /**
         * Cập nhật trạng thái hiển thị của các trường liên quan đến số kỳ trả góp.
         * Chỉ hiển thị khi phương thức thanh toán là "INSTALLMENT".
         */
        private void updateMonthsVisibility() {
            boolean isInstallment = (cbMethod.getSelectedItem() == PaymentMethod.INSTALLMENT);
            lblMonths.setEnabled(isInstallment);
            spinMonths.setEnabled(isInstallment);
        }

        /**
         * Trả về đối tượng PaymentRequest sau khi dialog đóng.
         * @return Optional chứa PaymentRequest nếu người dùng đã lưu, hoặc Optional.empty() nếu hủy.
         */
        public Optional<PaymentRequest> getResult() {
            return Optional.ofNullable(result);
        }
    }
}
