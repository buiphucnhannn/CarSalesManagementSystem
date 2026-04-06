package vn.edu.ute.carsalesms.view.component;

import vn.edu.ute.carsalesms.controller.StatisticsController;
import vn.edu.ute.carsalesms.model.dto.StatisticsBreakdownItem;
import vn.edu.ute.carsalesms.model.dto.StatisticsDashboardData;
import vn.edu.ute.carsalesms.model.dto.StatisticsKpiItem;
import vn.edu.ute.carsalesms.model.dto.StatisticsTrendPoint;
import vn.edu.ute.carsalesms.model.dto.TopCarStatisticsItem;
import vn.edu.ute.carsalesms.view.theme.DialogUiUtil;
import vn.edu.ute.carsalesms.view.theme.UiPalette;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Lớp StatisticsPanel định nghĩa giao diện người dùng cho trang tổng quan thống kê.
 * Giao diện này hiển thị các chỉ số hiệu suất chính (KPI), biểu đồ xu hướng,
 * biểu đồ cơ cấu và các bảng dữ liệu về doanh thu, đơn hàng.
 * Dữ liệu có thể được lọc theo khoảng thời gian.
 */
public class StatisticsPanel extends JPanel {

    // Định dạng ngày tháng để hiển thị và nhập liệu.
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Controller để lấy dữ liệu thống kê.
    private final StatisticsController controller;
    // ID của nhân viên (nếu có), dùng để phân biệt vai trò admin và nhân viên.
    private final Long staffId;

    // Các trường nhập liệu cho khoảng thời gian.
    private final JTextField fromDateField = new JTextField(10);
    private final JTextField toDateField = new JTextField(10);

    // Các nhãn hiển thị các chỉ số KPI.
    private final JLabel lblRevenue = createKpiValueLabel();
    private final JLabel lblOrders = createKpiValueLabel();
    private final JLabel lblPaidOrders = createKpiValueLabel();
    private final JLabel lblAvgOrder = createKpiValueLabel();

    // Các panel biểu đồ tùy chỉnh.
    private final LineTrendChartPanel trendChart = new LineTrendChartPanel();
    private final BarBreakdownChartPanel statusChart = new BarBreakdownChartPanel("Cơ cấu trạng thái đơn");
    private final BarBreakdownChartPanel paymentChart = new BarBreakdownChartPanel("Cơ cấu phương thức thanh toán");

    // Model cho bảng "Top xe bán chạy".
    private final DefaultTableModel topCarModel = new DefaultTableModel(
            new String[]{"Mã xe", "Tên xe", "Số lượng bán", "Doanh thu"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    // Model cho bảng "Doanh thu theo chi nhánh" hoặc "Cơ cấu thanh toán" (tùy vai trò).
    private final DefaultTableModel branchModel = new DefaultTableModel(
            new String[]{"Mã CN", "Tên chi nhánh", "Số đơn", "Doanh thu"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    /**
     * Constructor của StatisticsPanel.
     * @param controller controller để lấy dữ liệu.
     * @param staffId ID của nhân viên, null nếu là admin.
     */
    public StatisticsPanel(StatisticsController controller, Long staffId) {
        this.controller = controller;
        this.staffId = staffId;

        // Cấu hình layout và giao diện.
        setLayout(new BorderLayout(8, 8));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Thiết lập khoảng thời gian mặc định là 30 ngày gần nhất.
        LocalDate now = LocalDate.now();
        fromDateField.setText(now.minusDays(29).format(DATE_FMT));
        toDateField.setText(now.format(DATE_FMT));

        // Xây dựng các thành phần giao diện.
        add(buildFilterPanel(), BorderLayout.NORTH);
        add(buildBodyPanel(), BorderLayout.CENTER);

        // Tải dữ liệu ban đầu.
        loadData();
    }

    /**
     * Xây dựng panel bộ lọc và các thẻ KPI.
     * @return một JPanel chứa các bộ lọc và KPI.
     */
    private JPanel buildFilterPanel() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(8, 8));

        // Panel chứa các bộ lọc ngày tháng và các nút chọn nhanh.
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        filters.setOpaque(false);
        fromDateField.setPreferredSize(new Dimension(110, 30));
        toDateField.setPreferredSize(new Dimension(110, 30));

        JButton btn7 = createActionButton("7 ngày");
        JButton btn30 = createActionButton("30 ngày");
        JButton btn90 = createActionButton("90 ngày");
        JButton btnLoad = createActionButton("Xem thống kê");
        btnLoad.setBackground(UiPalette.PRIMARY);
        btnLoad.setForeground(Color.WHITE);

        // Gán sự kiện cho các nút.
        btn7.addActionListener(e -> setRange(6));
        btn30.addActionListener(e -> setRange(29));
        btn90.addActionListener(e -> setRange(89));
        btnLoad.addActionListener(e -> loadData());

        filters.add(new JLabel("Từ ngày:"));
        filters.add(fromDateField);
        filters.add(new JLabel("Đến ngày:"));
        filters.add(toDateField);
        filters.add(btn7);
        filters.add(btn30);
        filters.add(btn90);
        filters.add(btnLoad);

        // Lưới hiển thị các thẻ KPI.
        JPanel kpiGrid = new JPanel(new GridLayout(1, 4, 8, 0));
        kpiGrid.setOpaque(false);
        kpiGrid.add(createKpiCard("Tổng doanh thu", lblRevenue));
        kpiGrid.add(createKpiCard("Tổng đơn hàng", lblOrders));
        kpiGrid.add(createKpiCard("Đơn đã thanh toán", lblPaidOrders));
        kpiGrid.add(createKpiCard("Giá trị đơn TB", lblAvgOrder));

        card.add(filters, BorderLayout.NORTH);
        card.add(kpiGrid, BorderLayout.CENTER);
        return card;
    }

    /**
     * Xây dựng phần thân chính chứa các biểu đồ và bảng.
     * @return một JPanel chứa nội dung chính.
     */
    private JPanel buildBodyPanel() {
        JPanel body = new JPanel(new BorderLayout(8, 8));
        body.setOpaque(false);

        // Chia đôi màn hình theo chiều ngang cho các biểu đồ.
        JSplitPane charts = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                wrapWithTitle("Xu hướng doanh thu theo ngày", trendChart),
                buildBreakdownGroup());
        charts.setOpaque(false);
        charts.setBorder(BorderFactory.createEmptyBorder());
        charts.setResizeWeight(0.5);

        // Chia đôi màn hình theo chiều ngang cho các bảng.
        JSplitPane tables = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                wrapWithTitle("Top xe bán chạy", createTable(topCarModel)),
                wrapWithTitle(staffId == null ? "Doanh thu theo chi nhánh" : "Cơ cấu phương thức thanh toán", createTable(branchModel)));
        tables.setOpaque(false);
        tables.setBorder(BorderFactory.createEmptyBorder());
        tables.setResizeWeight(0.5);

        // Chia đôi màn hình theo chiều dọc, kết hợp phần biểu đồ và phần bảng.
        JSplitPane root = new JSplitPane(JSplitPane.VERTICAL_SPLIT, charts, tables);
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder());
        root.setResizeWeight(0.5);

        // Đảm bảo layout 2x2 được chia đều sau khi render.
        javax.swing.SwingUtilities.invokeLater(() -> {
            charts.setDividerLocation(0.5);
            tables.setDividerLocation(0.5);
            root.setDividerLocation(0.5);
        });

        body.add(root, BorderLayout.CENTER);
        return body;
    }

    /**
     * Xây dựng nhóm các biểu đồ cơ cấu (trạng thái đơn, phương thức thanh toán).
     * @return một JPanel chứa các biểu đồ cơ cấu.
     */
    private JPanel buildBreakdownGroup() {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 8));
        p.setOpaque(false);
        p.add(createBreakdownScroll(statusChart));
        p.add(createBreakdownScroll(paymentChart));
        return p;
    }

    /**
     * Tạo một JScrollPane cho biểu đồ cơ cấu để cho phép cuộn khi nội dung dài.
     */
    private JScrollPane createBreakdownScroll(BarBreakdownChartPanel chartPanel) {
        JScrollPane scrollPane = new JScrollPane(chartPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    /**
     * Tạo một JScrollPane chứa một JTable với định dạng chung.
     */
    private JScrollPane createTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setGridColor(UiPalette.BORDER_SOFT);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UiPalette.BORDER_SOFT));
        return sp;
    }

    /**
     * Bọc một component vào một panel kiểu "card" có tiêu đề.
     */
    private JPanel wrapWithTitle(String title, java.awt.Component content) {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(0, 8));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        lbl.setForeground(UiPalette.TEXT_PRIMARY);
        card.add(lbl, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    /**
     * Tạo một panel kiểu "card" với định dạng chung.
     */
    private JPanel createCard() {
        JPanel panel = new JPanel();
        panel.setOpaque(true);
        panel.setBackground(UiPalette.SURFACE_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiPalette.BORDER_SOFT),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        return panel;
    }

    /**
     * Tạo một nút bấm với kiểu dáng chung.
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
     * Tạo một thẻ KPI (Key Performance Indicator).
     */
    private JPanel createKpiCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setOpaque(true);
        card.setBackground(UiPalette.SURFACE_ELEVATED);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiPalette.BORDER_SOFT),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitle.setForeground(UiPalette.TEXT_SECONDARY);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    /**
     * Tạo một nhãn để hiển thị giá trị KPI.
     */
    private JLabel createKpiValueLabel() {
        JLabel lbl = new JLabel("0", SwingConstants.LEFT);
        lbl.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 18));
        lbl.setForeground(UiPalette.TEXT_PRIMARY);
        return lbl;
    }

    /**
     * Thiết lập khoảng thời gian và tải lại dữ liệu.
     * @param backDays số ngày lùi về từ ngày hiện tại.
     */
    private void setRange(int backDays) {
        LocalDate now = LocalDate.now();
        fromDateField.setText(now.minusDays(backDays).format(DATE_FMT));
        toDateField.setText(now.format(DATE_FMT));
        loadData();
    }

    /**
     * Tải dữ liệu thống kê từ controller và cập nhật giao diện.
     */
    private void loadData() {
        try {
            LocalDate from = parseDate(fromDateField.getText(), "Từ ngày");
            LocalDate to = parseDate(toDateField.getText(), "Đến ngày");

            // Gọi phương thức controller tương ứng với vai trò (admin hoặc staff).
            StatisticsDashboardData data = (staffId == null)
                    ? controller.getAdminStatistics(from, to)
                    : controller.getStaffStatistics(staffId, from, to);

            // Cập nhật các thành phần giao diện với dữ liệu mới.
            bindKpi(data.kpi());
            trendChart.setData(data.trend());
            statusChart.setData(localizeOrderStatus(data.orderStatusBreakdown()));
            paymentChart.setData(localizePaymentMethod(data.paymentMethodBreakdown()));
            bindTopCars(data.topCars());
            bindBranchesOrPayments(data);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    DialogUiUtil.appDialogParent(this),
                    "Không tải được thống kê: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Cập nhật các thẻ KPI.
     */
    private void bindKpi(StatisticsKpiItem kpi) {
        lblRevenue.setText(formatMoney(kpi.totalRevenue()));
        lblOrders.setText(String.valueOf(kpi.totalOrders()));
        lblPaidOrders.setText(String.valueOf(kpi.paidOrders()));
        lblAvgOrder.setText(formatMoney(kpi.averageOrderValue()));
    }

    /**
     * Cập nhật bảng "Top xe bán chạy".
     */
    private void bindTopCars(List<TopCarStatisticsItem> rows) {
        topCarModel.setRowCount(0);
        rows.forEach(r -> topCarModel.addRow(new Object[]{
                r.carCode(),
                r.carName(),
                r.soldQuantity(),
                formatMoney(r.revenue())
        }));
    }

    /**
     * Cập nhật bảng thứ hai, có thể là "Doanh thu theo chi nhánh" (cho admin)
     * hoặc "Cơ cấu phương thức thanh toán" (cho nhân viên).
     */
    private void bindBranchesOrPayments(StatisticsDashboardData data) {
        branchModel.setRowCount(0);
        if (staffId == null) { // Admin view
            data.branchStatistics().forEach(r -> branchModel.addRow(new Object[]{
                    r.branchCode(),
                    r.branchName(),
                    r.totalOrders(),
                    formatMoney(r.revenue())
            }));
            return;
        }
        // Staff view
        localizePaymentMethod(data.paymentMethodBreakdown()).forEach(r -> branchModel.addRow(new Object[]{
                r.label(),
                "-",
                r.count(),
                formatMoney(r.amount())
        }));
    }

    /**
     * Chuyển đổi nhãn trạng thái đơn hàng từ mã (raw) sang tiếng Việt.
     */
    private List<StatisticsBreakdownItem> localizeOrderStatus(List<StatisticsBreakdownItem> rows) {
        return rows.stream()
                .map(r -> new StatisticsBreakdownItem(toDisplayOrderStatus(r.label()), r.count(), r.amount()))
                .toList();
    }

    /**
     * Chuyển đổi nhãn phương thức thanh toán từ mã (raw) sang tiếng Việt.
     */
    private List<StatisticsBreakdownItem> localizePaymentMethod(List<StatisticsBreakdownItem> rows) {
        return rows.stream()
                .map(r -> new StatisticsBreakdownItem(toDisplayPaymentMethod(r.label()), r.count(), r.amount()))
                .toList();
    }

    private String toDisplayOrderStatus(String raw) {
        return switch (raw) {
            case "PAID" -> "Đã thanh toán";
            case "CONFIRMED" -> "Đã xác nhận";
            case "PENDING" -> "Đang xử lý";
            case "CANCELLED" -> "Đã hủy";
            default -> raw;
        };
    }

    private String toDisplayPaymentMethod(String raw) {
        return switch (raw) {
            case "CASH" -> "Tiền mặt";
            case "BANK_TRANSFER" -> "Chuyển khoản";
            case "INSTALLMENT" -> "Trả góp";
            default -> raw;
        };
    }

    /**
     * Phân tích chuỗi ngày tháng.
     * @throws IllegalArgumentException nếu định dạng không hợp lệ.
     */
    private LocalDate parseDate(String raw, String field) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException(field + " không được để trống.");
        }
        try {
            return LocalDate.parse(raw.trim(), DATE_FMT);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(field + " phải có định dạng dd/MM/yyyy.");
        }
    }

    /**
     * Định dạng số tiền theo kiểu Việt Nam.
     */
    private String formatMoney(BigDecimal value) {
        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        return fmt.format(value == null ? BigDecimal.ZERO : value) + " VND";
    }

    /**
     * Lớp nội bộ để vẽ biểu đồ đường thể hiện xu hướng.
     */
    private static final class LineTrendChartPanel extends JPanel {
        // ... (code bên trong không thay đổi, chỉ cần hiểu mục đích)
        // Lớp này chịu trách nhiệm vẽ biểu đồ đường dựa trên dữ liệu được cung cấp,
        // bao gồm các đường nối, điểm dữ liệu, chú thích, và xử lý tooltip khi di chuột.
    }

    /**
     * Lớp nội bộ để vẽ biểu đồ cột ngang thể hiện cơ cấu.
     */
    private static final class BarBreakdownChartPanel extends JPanel {
        // ... (code bên trong không thay đổi, chỉ cần hiểu mục đích)
        // Lớp này chịu trách nhiệm vẽ các thanh ngang để so sánh tỷ lệ
        // của các thành phần trong một tổng thể (ví dụ: cơ cấu trạng thái đơn hàng).
        // Nó tự động tính toán tỷ lệ phần trăm và hiển thị các nhãn tương ứng.
    }
}
