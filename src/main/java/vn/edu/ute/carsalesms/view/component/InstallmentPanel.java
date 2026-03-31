package vn.edu.ute.carsalesms.view.component;

import vn.edu.ute.carsalesms.controller.InstallmentController;
import vn.edu.ute.carsalesms.controller.SaleOrderController;
import vn.edu.ute.carsalesms.model.dto.InstallmentItem;
import vn.edu.ute.carsalesms.model.dto.SaleOrderItem;
import vn.edu.ute.carsalesms.model.enums.InstallmentStatus;
import vn.edu.ute.carsalesms.model.enums.OrderStatus;
import vn.edu.ute.carsalesms.view.theme.UiPalette;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InstallmentPanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final InstallmentController installmentController;
    private final SaleOrderController saleOrderController;

    private JTable orderTable;
    private DefaultTableModel orderModel;
    private List<SaleOrderItem> orderList = new ArrayList<>();

    private JTable planTable;
    private DefaultTableModel planModel;
    private List<InstallmentItem> planList = new ArrayList<>();

    private SaleOrderItem currentOrder;

    public InstallmentPanel(InstallmentController installmentController, SaleOrderController saleOrderController) {
        this.installmentController = installmentController;
        this.saleOrderController = saleOrderController;

        setLayout(new BorderLayout(8, 8));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(buildOrderSection());
        splitPane.setBottomComponent(buildPlanSection());
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.5);

        add(splitPane, BorderLayout.CENTER);

        refreshOrders();
    }

    private JPanel buildOrderSection() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);

        // Toolbar tra cứu order
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        toolbar.setOpaque(false);
        JTextField txtSearch = new JTextField(20);
        JButton btnSearch = createActionButton("Tìm đơn trả góp");
        btnSearch.addActionListener(e -> refreshOrders(txtSearch.getText()));
        toolbar.add(new JLabel("Mã/Tên KH:"));
        toolbar.add(txtSearch);
        toolbar.add(btnSearch);

        // Bảng order
        String[] cols = { "Mã đơn", "Ngày lập", "Khách hàng", "Thực thu", "Còn thiếu", "Trạng thái" };
        orderModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        orderTable = new JTable(orderModel);
        orderTable.setRowHeight(28);

        orderTable.getSelectionModel().addListSelectionListener(this::onOrderSelected);

        p.add(toolbar, BorderLayout.NORTH);
        p.add(new JScrollPane(orderTable), BorderLayout.CENTER);
        return p;
    }

    private JPanel buildPlanSection() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createTitledBorder("Chi tiết hợp đồng (Các kỳ trả góp)"));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        toolbar.setOpaque(false);

        JButton btnPayPlan = createActionButton("Thanh toán kỳ trả góp");
        btnPayPlan.setBackground(UiPalette.SUCCESS);
        btnPayPlan.setForeground(Color.WHITE);
        btnPayPlan.addActionListener(e -> showPayDialog());
        toolbar.add(btnPayPlan);

        String[] cols = { "Kỳ số", "Hạn chót đóng", "Phải đóng", "Đã đóng", "Còn thiếu", "Trạng thái", "Ghi chú" };
        planModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        planTable = new JTable(planModel);
        planTable.setRowHeight(28);
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        planTable.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        planTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        planTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        p.add(new JScrollPane(planTable), BorderLayout.CENTER);
        p.add(toolbar, BorderLayout.SOUTH);

        return p;
    }

    private void refreshOrders() {
        refreshOrders(null);
    }

    private void refreshOrders(String keyword) {
        try {
            List<SaleOrderItem> tmp = saleOrderController.findOrders(keyword, null);

            // Dùng Java Stream lặp để tìm và phân biệt chính xác Đơn nào là Trả Góp (CÓ danh sách Plan trong db)
            orderList = tmp.stream().filter(o -> {
                List<InstallmentItem> checkPlans = installmentController.findByOrderId(o.id());
                return checkPlans != null && !checkPlans.isEmpty();
            }).collect(java.util.stream.Collectors.toList());

            orderModel.setRowCount(0);
            for (SaleOrderItem o : orderList) {
                // Tính Thực Thu và Còn Nợ dự trên danh sách Kỳ Hạn
                List<InstallmentItem> pList = installmentController.findByOrderId(o.id());
                
                // Dùng Java Stream Reduce tính tổng
                BigDecimal sumAmount = pList.stream().map(InstallmentItem::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal sumPaid = pList.stream().map(InstallmentItem::paidAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                
                // Chặn không cho Lượng nợ rớt xuống biến số âm (nếu Database khách hâm lỡ đóng lố tỷ bạc)
                BigDecimal debt = sumAmount.subtract(sumPaid).max(BigDecimal.ZERO);
                BigDecimal actualReceived = o.finalAmount().subtract(debt);

                orderModel.addRow(new Object[] {
                        o.orderCode(),
                        o.orderDate() == null ? "" : o.orderDate().format(DATE_FMT),
                        o.customerName(),
                        String.format("%,.0f", actualReceived), // Thực thu (Đã lấy)
                        String.format("%,.0f", debt),           // Còn thiếu (Đang nợ)
                        o.orderStatus()
                });
            }
        } catch (Exception ex) {
            showError("Lỗi tải danh sách: " + ex.getMessage());
        }
    }

    private void onOrderSelected(ListSelectionEvent e) {
        if (e.getValueIsAdjusting())
            return;
        int row = orderTable.getSelectedRow();
        if (row >= 0 && row < orderList.size()) {
            currentOrder = orderList.get(row);
            refreshPlans();
        } else {
            currentOrder = null;
            planModel.setRowCount(0);
            planList.clear();
        }
    }

    private void refreshPlans() {
        if (currentOrder == null)
            return;
        try {
            planList = installmentController.findByOrderId(currentOrder.id());
            planModel.setRowCount(0);
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

    private void showPayDialog() {
        if (currentOrder == null) {
            showInfo("Vui lòng chọn 1 Đơn bán trả góp.");
            return;
        }
        int row = planTable.getSelectedRow();
        if (row < 0 || row >= planList.size()) {
            showInfo("Vui lòng chọn 1 kỳ hạn để thanh toán.");
            return;
        }
        InstallmentItem p = planList.get(row);
        if (p.status() == InstallmentStatus.PAID) {
            showInfo("Kỳ hạn này đã được thanh toán đủ.");
            return;
        }

        String suggestedAmount = p.getDueRemaining().max(BigDecimal.ZERO).stripTrailingZeros().toPlainString();
        String input = (String) JOptionPane.showInputDialog(this,
                "Nhập số tiền đóng cho kỳ " + p.installmentNo() + " (còn nợ "
                        + String.format("%,.0f đ", p.getDueRemaining().max(BigDecimal.ZERO)) + "):",
                "Thanh toán Trả góp", JOptionPane.QUESTION_MESSAGE, null, null, suggestedAmount);

        if (input != null && !input.trim().isEmpty()) {
            try {
                BigDecimal amount = new BigDecimal(input.replaceAll("[,.]", ""));
                installmentController.payInstallment(p.id(), amount, "Khách hàng thanh toán qua quầy");
                showInfo("Ghi nhận số tiền thành công!");
                refreshPlans();
                refreshOrders(null); // Load master tránh bị đơ state cũ
            } catch (Exception ex) {
                showError("Lỗi: " + ex.getMessage());
            }
        }
    }

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

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }
}
