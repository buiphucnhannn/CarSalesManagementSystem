package vn.edu.ute.carsalesms.view.component;

import vn.edu.ute.carsalesms.controller.InstallmentController;
import vn.edu.ute.carsalesms.controller.SaleOrderController;
import vn.edu.ute.carsalesms.model.dto.InstallmentItem;
import vn.edu.ute.carsalesms.model.dto.SaleOrderItem;
import vn.edu.ute.carsalesms.model.enums.InstallmentStatus;
import vn.edu.ute.carsalesms.view.theme.DialogUiUtil;
import vn.edu.ute.carsalesms.view.theme.UiPalette;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lớp InstallmentPanel định nghĩa giao diện người dùng cho chức năng quản lý trả góp.
 * Giao diện này cho phép xem danh sách các đơn hàng trả góp và chi tiết các kỳ thanh toán của từng đơn.
 * Người dùng có thể thực hiện thanh toán cho các kỳ trả góp.
 */
public class InstallmentPanel extends JPanel {

    // Định dạng ngày giờ để hiển thị.
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    // Controller để xử lý logic nghiệp vụ liên quan đến trả góp.
    private final InstallmentController installmentController;
    // Controller để xử lý logic nghiệp vụ liên quan đến đơn hàng.
    private final SaleOrderController saleOrderController;

    // Bảng và model cho danh sách đơn hàng trả góp.
    private JTable orderTable;
    private DefaultTableModel orderModel;
    private List<SaleOrderItem> orderList = new ArrayList<>();

    // Bảng và model cho chi tiết các kỳ trả góp.
    private JTable planTable;
    private DefaultTableModel planModel;
    private List<InstallmentItem> planList = new ArrayList<>();

    // Đơn hàng đang được chọn.
    private SaleOrderItem currentOrder;

    /**
     * Constructor của InstallmentPanel.
     * @param installmentController controller xử lý nghiệp vụ trả góp.
     * @param saleOrderController controller xử lý nghiệp vụ đơn hàng.
     */
    public InstallmentPanel(InstallmentController installmentController, SaleOrderController saleOrderController) {
        this.installmentController = installmentController;
        this.saleOrderController = saleOrderController;

        // Cấu hình layout và giao diện cho panel.
        setLayout(new BorderLayout(8, 8));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Sử dụng JSplitPane để chia giao diện thành hai phần: danh sách đơn hàng và chi tiết kỳ trả góp.
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(buildOrderSection());
        splitPane.setBottomComponent(buildPlanSection());
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.5);

        add(splitPane, BorderLayout.CENTER);

        // Tải danh sách đơn hàng ban đầu.
        refreshOrders();
    }

    /**
     * Xây dựng phần giao diện hiển thị danh sách các đơn hàng trả góp.
     * @return một JPanel chứa bảng danh sách đơn hàng và thanh công cụ tìm kiếm.
     */
    private JPanel buildOrderSection() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);

        // Thanh công cụ tìm kiếm đơn hàng.
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        toolbar.setOpaque(false);
        JTextField txtSearch = new JTextField(20);
        JButton btnSearch = createActionButton("Tìm đơn trả góp");
        btnSearch.addActionListener(e -> refreshOrders(txtSearch.getText()));
        toolbar.add(new JLabel("Mã/Tên KH:"));
        toolbar.add(txtSearch);
        toolbar.add(btnSearch);

        // Bảng danh sách đơn hàng.
        String[] cols = { "Mã đơn", "Ngày lập", "Khách hàng", "Thực thu", "Còn thiếu", "Trạng thái" };
        orderModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho phép chỉnh sửa trực tiếp trên bảng.
            }
        };
        orderTable = new JTable(orderModel);
        orderTable.setRowHeight(28);

        // Thêm listener để xử lý sự kiện khi người dùng chọn một đơn hàng.
        orderTable.getSelectionModel().addListSelectionListener(this::onOrderSelected);

        p.add(toolbar, BorderLayout.NORTH);
        p.add(new JScrollPane(orderTable), BorderLayout.CENTER);
        return p;
    }

    /**
     * Xây dựng phần giao diện hiển thị chi tiết các kỳ trả góp của đơn hàng được chọn.
     * @return một JPanel chứa bảng chi tiết và nút thanh toán.
     */
    private JPanel buildPlanSection() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createTitledBorder("Chi tiết hợp đồng (Các kỳ trả góp)"));

        // Thanh công cụ chứa nút "Thanh toán kỳ trả góp".
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        toolbar.setOpaque(false);

        JButton btnPayPlan = createActionButton("Thanh toán kỳ trả góp");
        btnPayPlan.setBackground(UiPalette.SUCCESS);
        btnPayPlan.setForeground(Color.WHITE);
        btnPayPlan.addActionListener(e -> showPayDialog());
        toolbar.add(btnPayPlan);

        // Bảng chi tiết các kỳ trả góp.
        String[] cols = { "Kỳ số", "Hạn chót đóng", "Phải đóng", "Đã đóng", "Còn thiếu", "Trạng thái", "Ghi chú" };
        planModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho phép chỉnh sửa.
            }
        };
        planTable = new JTable(planModel);
        planTable.setRowHeight(28);
        // Căn phải cho các cột số tiền.
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        planTable.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        planTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        planTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        p.add(new JScrollPane(planTable), BorderLayout.CENTER);
        p.add(toolbar, BorderLayout.SOUTH);

        return p;
    }

    /**
     * Tải lại danh sách đơn hàng trả góp (không có từ khóa tìm kiếm).
     */
    private void refreshOrders() {
        refreshOrders(null);
    }

    /**
     * Tải lại danh sách đơn hàng trả góp dựa trên từ khóa tìm kiếm.
     * @param keyword từ khóa để tìm kiếm (mã đơn hàng hoặc tên khách hàng).
     */
    private void refreshOrders(String keyword) {
        try {
            // Lấy tất cả đơn hàng khớp từ khóa.
            List<SaleOrderItem> tmp = saleOrderController.findOrders(keyword, null);
            List<SaleOrderItem> filteredOrders = new ArrayList<>();
            Map<Long, List<InstallmentItem>> plansByOrderId = new HashMap<>();

            // Lọc ra những đơn hàng có kế hoạch trả góp.
            for (SaleOrderItem o : tmp) {
                List<InstallmentItem> plans = installmentController.findByOrderId(o.id());
                if (plans != null && !plans.isEmpty()) {
                    filteredOrders.add(o);
                    plansByOrderId.put(o.id(), plans);
                }
            }
            orderList = filteredOrders;

            // Reset trạng thái giao diện.
            currentOrder = null;
            planModel.setRowCount(0);
            planList.clear();
            orderModel.setRowCount(0);
            // Đổ dữ liệu đã lọc vào bảng đơn hàng.
            for (SaleOrderItem o : orderList) {
                List<InstallmentItem> pList = plansByOrderId.getOrDefault(o.id(), List.of());

                // Tính toán tổng số tiền phải đóng, đã đóng và còn thiếu từ các kỳ trả góp.
                BigDecimal sumAmount = pList.stream()
                        .map(i -> i.amount() == null ? BigDecimal.ZERO : i.amount())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal sumPaid = pList.stream()
                        .map(i -> i.paidAmount() == null ? BigDecimal.ZERO : i.paidAmount())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal debt = sumAmount.subtract(sumPaid).max(BigDecimal.ZERO);
                BigDecimal finalAmount = o.finalAmount() == null ? BigDecimal.ZERO : o.finalAmount();
                BigDecimal actualReceived = finalAmount.subtract(debt).max(BigDecimal.ZERO);

                // Thêm dòng mới vào bảng đơn hàng.
                orderModel.addRow(new Object[] {
                        o.orderCode(),
                        o.orderDate() == null ? "" : o.orderDate().format(DATE_FMT),
                        o.customerName(),
                        String.format("%,.0f", actualReceived),
                        String.format("%,.0f", debt),
                        o.orderStatus()
                });
            }
        } catch (Exception ex) {
            showError("Lỗi tải danh sách: " + ex.getMessage());
        }
    }

    /**
     * Xử lý sự kiện khi người dùng chọn một đơn hàng trong bảng.
     * @param e sự kiện lựa chọn.
     */
    private void onOrderSelected(ListSelectionEvent e) {
        if (e.getValueIsAdjusting())
            return;
        int row = toModelRow(orderTable, orderTable.getSelectedRow());
        if (row >= 0 && row < orderList.size()) {
            // Lấy đơn hàng được chọn và tải chi tiết các kỳ trả góp.
            currentOrder = orderList.get(row);
            refreshPlans();
        } else {
            // Nếu không có đơn hàng nào được chọn, xóa chi tiết.
            currentOrder = null;
            planModel.setRowCount(0);
            planList.clear();
        }
    }

    /**
     * Tải và hiển thị chi tiết các kỳ trả góp cho đơn hàng đang được chọn (currentOrder).
     */
    private void refreshPlans() {
        if (currentOrder == null)
            return;
        try {
            // Lấy danh sách các kỳ trả góp từ controller.
            planList = installmentController.findByOrderId(currentOrder.id());
            planModel.setRowCount(0); // Xóa dữ liệu cũ.
            // Đổ dữ liệu mới vào bảng chi tiết.
            for (InstallmentItem p : planList) {
                planModel.addRow(new Object[] {
                        p.installmentNo(),
                        p.dueDate(),
                        String.format("%,.0f", p.amount()),
                        String.format("%,.0f", p.paidAmount()),
                        String.format("%,.0f", p.getDueRemaining().max(BigDecimal.ZERO)),
                        p.status(),
                        p.note()
                });
            }
        } catch (Exception ex) {
            showError("Lỗi lấy chi tiết trả góp: " + ex.getMessage());
        }
    }

    /**
     * Hiển thị dialog để người dùng nhập số tiền thanh toán cho một kỳ trả góp.
     */
    private void showPayDialog() {
        if (currentOrder == null) {
            showInfo("Vui lòng chọn 1 Đơn bán trả góp.");
            return;
        }
        int row = toModelRow(planTable, planTable.getSelectedRow());
        if (row < 0 || row >= planList.size()) {
            showInfo("Vui lòng chọn 1 kỳ hạn để thanh toán.");
            return;
        }
        InstallmentItem p = planList.get(row);
        if (p.status() == InstallmentStatus.PAID) {
            showInfo("Kỳ hạn này đã được thanh toán đủ.");
            return;
        }

        // Gợi ý số tiền cần thanh toán là số tiền còn lại.
        String suggestedAmount = p.getDueRemaining().max(BigDecimal.ZERO).setScale(0, java.math.RoundingMode.HALF_UP).toPlainString();
        String input = (String) JOptionPane.showInputDialog(getDialogParent(),
                "Nhập số tiền đóng cho kỳ " + p.installmentNo() + " (còn nợ "
                        + String.format("%,.0f đ", p.getDueRemaining().max(BigDecimal.ZERO)) + "):",
                "Thanh toán Trả góp", JOptionPane.QUESTION_MESSAGE, null, null, suggestedAmount);

        if (input != null && !input.trim().isEmpty()) {
            try {
                // Xử lý và xác thực số tiền nhập vào.
                BigDecimal amount = parsePaymentAmount(input);
                // Gọi controller để thực hiện thanh toán.
                installmentController.payInstallment(p.id(), amount, "Khách hàng thanh toán qua quầy");
                showInfo("Ghi nhận số tiền thành công!");
                // Tải lại dữ liệu để cập nhật giao diện.
                refreshPlans();
                refreshOrders(null);
            } catch (IllegalArgumentException ex) {
                showInfo(ex.getMessage());
            } catch (Exception ex) {
                showError("Lỗi: " + ex.getMessage());
            }
        }
    }

    /**
     * Chuyển đổi chỉ số dòng từ view sang model (quan trọng khi có sắp xếp).
     * @param table bảng đang xét.
     * @param viewRow chỉ số dòng trên view.
     * @return chỉ số dòng tương ứng trên model.
     */
    private int toModelRow(JTable table, int viewRow) {
        if (viewRow < 0) {
            return -1;
        }
        return table.convertRowIndexToModel(viewRow);
    }

    /**
     * Phân tích và xác thực chuỗi nhập vào cho số tiền thanh toán.
     * @param rawInput chuỗi người dùng nhập.
     * @return một đối tượng BigDecimal nếu hợp lệ.
     * @throws IllegalArgumentException nếu chuỗi không hợp lệ.
     */
    private BigDecimal parsePaymentAmount(String rawInput) {
        String cleaned = rawInput == null ? "" : rawInput.trim().replaceAll("\\s+", "");
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Số tiền không được để trống.");
        }

        // Xử lý trường hợp người dùng nhập dấu phẩy hoặc chấm làm dấu phân cách hàng nghìn.
        if (cleaned.contains(",") || cleaned.contains(".")) {
            if (!cleaned.matches("^\\d{1,3}([.,]\\d{3})+$")) {
                throw new IllegalArgumentException("Số tiền không hợp lệ. Chỉ nhập số nguyên, ví dụ: 12000000 hoặc 12,000,000.");
            }
            cleaned = cleaned.replaceAll("[,.]", "");
        }

        if (!cleaned.matches("^\\d+$")) {
            throw new IllegalArgumentException("Số tiền không hợp lệ. Vui lòng chỉ nhập chữ số.");
        }

        BigDecimal amount = new BigDecimal(cleaned);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0.");
        }
        return amount;
    }

    /**
     * Tạo một nút bấm với kiểu dáng chung.
     * @param title tiêu đề của nút.
     * @return một đối tượng JButton.
     */
    private JButton createActionButton(String title) {
        JButton btn = new JButton(title);
        btn.setFocusPainted(false);
        btn.setBackground(UiPalette.ACTION_BG);
        btn.setForeground(UiPalette.ACTION_FG);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiPalette.PRIMARY_BORDER),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        return btn;
    }

    /**
     * Hiển thị dialog thông báo lỗi.
     * @param msg nội dung thông báo.
     */
    private void showError(String msg) {
        JOptionPane.showMessageDialog(getDialogParent(), msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Hiển thị dialog thông tin.
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
}
