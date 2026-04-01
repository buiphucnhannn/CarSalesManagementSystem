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

public class StatisticsPanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final StatisticsController controller;
    private final Long staffId;

    private final JTextField fromDateField = new JTextField(10);
    private final JTextField toDateField = new JTextField(10);

    private final JLabel lblRevenue = createKpiValueLabel();
    private final JLabel lblOrders = createKpiValueLabel();
    private final JLabel lblPaidOrders = createKpiValueLabel();
    private final JLabel lblAvgOrder = createKpiValueLabel();

    private final LineTrendChartPanel trendChart = new LineTrendChartPanel();
    private final BarBreakdownChartPanel statusChart = new BarBreakdownChartPanel("Cơ cấu trạng thái đơn");
    private final BarBreakdownChartPanel paymentChart = new BarBreakdownChartPanel("Cơ cấu phương thức thanh toán");

    private final DefaultTableModel topCarModel = new DefaultTableModel(
            new String[]{"Mã xe", "Tên xe", "Số lượng bán", "Doanh thu"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final DefaultTableModel branchModel = new DefaultTableModel(
            new String[]{"Mã CN", "Tên chi nhánh", "Số đơn", "Doanh thu"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public StatisticsPanel(StatisticsController controller, Long staffId) {
        this.controller = controller;
        this.staffId = staffId;

        setLayout(new BorderLayout(8, 8));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        LocalDate now = LocalDate.now();
        fromDateField.setText(now.minusDays(29).format(DATE_FMT));
        toDateField.setText(now.format(DATE_FMT));

        add(buildFilterPanel(), BorderLayout.NORTH);
        add(buildBodyPanel(), BorderLayout.CENTER);

        loadData();
    }

    private JPanel buildFilterPanel() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(8, 8));

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

    private JPanel buildBodyPanel() {
        JPanel body = new JPanel(new BorderLayout(8, 8));
        body.setOpaque(false);

        JSplitPane charts = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                wrapWithTitle("Xu hướng doanh thu theo ngày", trendChart),
                buildBreakdownGroup());
        charts.setOpaque(false);
        charts.setBorder(BorderFactory.createEmptyBorder());
        charts.setResizeWeight(0.5);

        JSplitPane tables = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                wrapWithTitle("Top xe bán chạy", createTable(topCarModel)),
                wrapWithTitle(staffId == null ? "Doanh thu theo chi nhánh" : "Cơ cấu phương thức thanh toán", createTable(branchModel)));
        tables.setOpaque(false);
        tables.setBorder(BorderFactory.createEmptyBorder());
        tables.setResizeWeight(0.5);

        JSplitPane root = new JSplitPane(JSplitPane.VERTICAL_SPLIT, charts, tables);
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder());
        root.setResizeWeight(0.5);

        // Enforce equal 2x2 layout after first render pass.
        javax.swing.SwingUtilities.invokeLater(() -> {
            charts.setDividerLocation(0.5);
            tables.setDividerLocation(0.5);
            root.setDividerLocation(0.5);
        });

        body.add(root, BorderLayout.CENTER);
        return body;
    }

    private JPanel buildBreakdownGroup() {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 8));
        p.setOpaque(false);
        p.add(createBreakdownScroll(statusChart));
        p.add(createBreakdownScroll(paymentChart));
        return p;
    }

    private JScrollPane createBreakdownScroll(BarBreakdownChartPanel chartPanel) {
        JScrollPane scrollPane = new JScrollPane(chartPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

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

    private JLabel createKpiValueLabel() {
        JLabel lbl = new JLabel("0", SwingConstants.LEFT);
        lbl.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 18));
        lbl.setForeground(UiPalette.TEXT_PRIMARY);
        return lbl;
    }

    private void setRange(int backDays) {
        LocalDate now = LocalDate.now();
        fromDateField.setText(now.minusDays(backDays).format(DATE_FMT));
        toDateField.setText(now.format(DATE_FMT));
        loadData();
    }

    private void loadData() {
        try {
            LocalDate from = parseDate(fromDateField.getText(), "Từ ngày");
            LocalDate to = parseDate(toDateField.getText(), "Đến ngày");

            StatisticsDashboardData data = (staffId == null)
                    ? controller.getAdminStatistics(from, to)
                    : controller.getStaffStatistics(staffId, from, to);

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

    private void bindKpi(StatisticsKpiItem kpi) {
        lblRevenue.setText(formatMoney(kpi.totalRevenue()));
        lblOrders.setText(String.valueOf(kpi.totalOrders()));
        lblPaidOrders.setText(String.valueOf(kpi.paidOrders()));
        lblAvgOrder.setText(formatMoney(kpi.averageOrderValue()));
    }

    private void bindTopCars(List<TopCarStatisticsItem> rows) {
        topCarModel.setRowCount(0);
        rows.forEach(r -> topCarModel.addRow(new Object[]{
                r.carCode(),
                r.carName(),
                r.soldQuantity(),
                formatMoney(r.revenue())
        }));
    }

    private void bindBranchesOrPayments(StatisticsDashboardData data) {
        branchModel.setRowCount(0);
        if (staffId == null) {
            data.branchStatistics().forEach(r -> branchModel.addRow(new Object[]{
                    r.branchCode(),
                    r.branchName(),
                    r.totalOrders(),
                    formatMoney(r.revenue())
            }));
            return;
        }
        localizePaymentMethod(data.paymentMethodBreakdown()).forEach(r -> branchModel.addRow(new Object[]{
                r.label(),
                "-",
                r.count(),
                formatMoney(r.amount())
        }));
    }

    private List<StatisticsBreakdownItem> localizeOrderStatus(List<StatisticsBreakdownItem> rows) {
        return rows.stream()
                .map(r -> new StatisticsBreakdownItem(toDisplayOrderStatus(r.label()), r.count(), r.amount()))
                .toList();
    }

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

    private String formatMoney(BigDecimal value) {
        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        return fmt.format(value == null ? BigDecimal.ZERO : value) + " VND";
    }

    private static final class LineTrendChartPanel extends JPanel {

        private static final int HIT_RADIUS_X = 14;

        private List<StatisticsTrendPoint> points = List.of();
        private final List<Point> plottedPixels = new ArrayList<>();
        private int hoveredIndex = -1;
        private int plotLeft;
        private int plotRight;
        private int plotTop;
        private int plotBottom;

        private LineTrendChartPanel() {
            setOpaque(true);
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(UiPalette.BORDER_SOFT));
            setToolTipText("");

            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int idx = findPointIndex(e.getPoint());
                    if (idx != hoveredIndex) {
                        hoveredIndex = idx;
                        repaint();
                    }
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    if (hoveredIndex != -1) {
                        hoveredIndex = -1;
                        repaint();
                    }
                }
            });
        }

        private void setData(List<StatisticsTrendPoint> points) {
            this.points = points == null ? List.of() : points;
            hoveredIndex = -1;
            repaint();
        }

        @Override
        public String getToolTipText(MouseEvent event) {
            int idx = findPointIndex(event.getPoint());
            if (idx < 0 || idx >= points.size()) {
                return null;
            }
            StatisticsTrendPoint p = points.get(idx);
            NumberFormat fmt = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
            return "Ngày " + p.date().format(DATE_FMT)
                    + " | Doanh thu: " + fmt.format(p.revenue()) + " VND"
                    + " | Số đơn: " + p.orderCount();
        }

        private int findPointIndex(Point mousePoint) {
            if (points == null || points.isEmpty()) {
                return -1;
            }
            if (mousePoint.x < plotLeft || mousePoint.x > plotRight ||
                    mousePoint.y < plotTop - 8 || mousePoint.y > plotBottom + 8) {
                return -1;
            }

            int n = points.size();
            int idx = (int) Math.round((mousePoint.x - plotLeft) * 1.0 / Math.max(1, plotRight - plotLeft) * (n - 1));
            idx = Math.max(0, Math.min(n - 1, idx));

            int px = pointXForIndex(idx, n);
            if (Math.abs(mousePoint.x - px) > HIT_RADIUS_X) {
                return -1;
            }
            return idx;
        }

        private int pointXForIndex(int index, int n) {
            if (n <= 1) {
                return plotLeft;
            }
            return plotLeft + (int) Math.round((index * 1.0 / (n - 1)) * (plotRight - plotLeft));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            plottedPixels.clear();

            int w = getWidth();
            int h = getHeight();
            int left = 34;
            int right = 34;
            int top = 44;
            int bottom = 30;

            plotLeft = left;
            plotRight = w - right;
            plotTop = top;
            plotBottom = h - bottom;

            g2.setColor(UiPalette.BORDER_SOFT);
            g2.drawLine(left, h - bottom, w - right, h - bottom);
            g2.drawLine(left, top, left, h - bottom);

            if (points == null || points.isEmpty()) {
                g2.setColor(UiPalette.TEXT_MUTED);
                g2.drawString("Chưa có dữ liệu", left + 12, top + 18);
                g2.dispose();
                return;
            }

            BigDecimal max = points.stream()
                    .map(StatisticsTrendPoint::revenue)
                    .max(Comparator.naturalOrder())
                    .orElse(BigDecimal.ONE);
            if (max.compareTo(BigDecimal.ZERO) <= 0) {
                max = BigDecimal.ONE;
            }

            int n = points.size();
            int chartW = w - left - right;
            int chartH = h - top - bottom;

            // Chu thich duong doanh thu can giua vung tren, khong de vao vung duong ve.
            int legendX = left + Math.max(8, ((w - left - right) - 150) / 2);
            g2.setColor(UiPalette.PRIMARY);
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawLine(legendX, 20, legendX + 25, 20);
            g2.fillOval(legendX + 11, 16, 8, 8);
            g2.setColor(UiPalette.TEXT_SECONDARY);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g2.drawString("Doanh thu theo ngày", legendX + 30, 24);

            g2.setStroke(new BasicStroke(2.2f));
            g2.setColor(UiPalette.PRIMARY);

            int prevX = -1;
            int prevY = -1;
            for (int i = 0; i < n; i++) {
                StatisticsTrendPoint p = points.get(i);
                int x = left + (int) Math.round((i * 1.0 / Math.max(1, n - 1)) * chartW);
                int y = top + chartH - (int) Math.round(p.revenue().doubleValue() / max.doubleValue() * chartH);

                if (prevX >= 0) {
                    g2.drawLine(prevX, prevY, x, y);
                }
                int dotSize = (i == hoveredIndex) ? 10 : 6;
                g2.fillOval(x - dotSize / 2, y - dotSize / 2, dotSize, dotSize);
                plottedPixels.add(new Point(x, y));
                prevX = x;
                prevY = y;
            }

            // Nhan moc thoi gian dau - giua - cuoi de nguoi dung de dinh huong
            g2.setColor(UiPalette.TEXT_MUTED);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            StatisticsTrendPoint first = points.get(0);
            StatisticsTrendPoint middle = points.get(points.size() / 2);
            StatisticsTrendPoint last = points.get(points.size() - 1);
            g2.drawString(first.date().format(DATE_FMT), left, h - 8);
            g2.drawString(middle.date().format(DATE_FMT), left + chartW / 2 - 24, h - 8);
            g2.drawString(last.date().format(DATE_FMT), w - right - 62, h - 8);

            g2.dispose();
        }
    }

    private static final class BarBreakdownChartPanel extends JPanel {

        private static final Color[] BAR_COLORS = {
                new Color(0x37, 0x5F, 0xEB),
                new Color(0x22, 0xC5, 0x5E),
                new Color(0xF5, 0x9E, 0x0B),
                new Color(0x8B, 0x5C, 0xF6),
                new Color(0xEF, 0x44, 0x44),
                new Color(0x14, 0xB8, 0xA6)
        };

        private final String title;
        private List<StatisticsBreakdownItem> rows = new ArrayList<>();

        private BarBreakdownChartPanel(String title) {
            this.title = title;
            setOpaque(true);
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UiPalette.BORDER_SOFT),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
            setPreferredSize(new Dimension(340, 200));
        }

        private void setData(List<StatisticsBreakdownItem> rows) {
            this.rows = rows == null ? List.of() : rows;
            int preferredHeight = Math.max(170, 36 + this.rows.size() * 38);
            setPreferredSize(new Dimension(Math.max(320, getWidth()), preferredHeight));
            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(UiPalette.TEXT_PRIMARY);
            g2.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
            g2.drawString(title, 8, 16);

            if (rows == null || rows.isEmpty()) {
                g2.setColor(UiPalette.TEXT_MUTED);
                g2.drawString("Chưa có dữ liệu", 8, 34);
                g2.dispose();
                return;
            }

            List<StatisticsBreakdownItem> displayRows = rows;
            long total = displayRows.stream().mapToLong(StatisticsBreakdownItem::count).sum();

            int y = 32;
            int itemH = 38;
            int barH = 10;
            int left = 8;
            int rightPad = 120;
            int maxW = Math.max(80, getWidth() - left - rightPad);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            for (int i = 0; i < displayRows.size(); i++) {
                StatisticsBreakdownItem row = displayRows.get(i);
                double ratio = total == 0 ? 0D : (row.count() * 1.0D / total);
                int barW = (int) Math.round(ratio * maxW);

                g2.setColor(UiPalette.TEXT_PRIMARY);
                g2.drawString(row.label(), left, y - 2);

                g2.setColor(UiPalette.PRIMARY_SOFT);
                g2.fillRoundRect(left, y + 4, maxW, barH, 10, 10);

                g2.setColor(BAR_COLORS[i % BAR_COLORS.length]);
                g2.fillRoundRect(left, y + 4, Math.max(2, barW), barH, 10, 10);

                String metric = row.count() + " (" + (int) Math.round(ratio * 100) + "%)";
                if (row.amount() != null && row.amount().compareTo(BigDecimal.ZERO) > 0) {
                    metric = metric + " - " + compactMoney(row.amount());
                }
                g2.setColor(UiPalette.TEXT_SECONDARY);
                g2.drawString(metric, left + maxW + 8, y + 13);

                y += itemH;
            }
            g2.dispose();
        }

        private String compactMoney(BigDecimal amount) {
            BigDecimal safe = amount == null ? BigDecimal.ZERO : amount;
            if (safe.compareTo(BigDecimal.valueOf(1_000_000_000L)) >= 0) {
                return safe.divide(BigDecimal.valueOf(1_000_000_000L), 1, java.math.RoundingMode.HALF_UP) + " tỷ";
            }
            if (safe.compareTo(BigDecimal.valueOf(1_000_000L)) >= 0) {
                return safe.divide(BigDecimal.valueOf(1_000_000L), 1, java.math.RoundingMode.HALF_UP) + " triệu";
            }
            return NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(safe) + " VND";
        }
    }
}

