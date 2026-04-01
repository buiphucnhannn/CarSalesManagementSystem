package vn.edu.ute.carsalesms.view.staff;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import vn.edu.ute.carsalesms.controller.CarManagementController;
import vn.edu.ute.carsalesms.controller.CustomerManagementController;
import vn.edu.ute.carsalesms.controller.SaleOrderController;
import vn.edu.ute.carsalesms.controller.PaymentController;
import vn.edu.ute.carsalesms.controller.InstallmentController;
import vn.edu.ute.carsalesms.controller.InvoiceController;
import vn.edu.ute.carsalesms.controller.StatisticsController;
import vn.edu.ute.carsalesms.dao.CarDao;
import vn.edu.ute.carsalesms.dao.impl.CarDaoImpl;
import vn.edu.ute.carsalesms.dao.impl.CustomerDaoImpl;
import vn.edu.ute.carsalesms.dao.impl.DashboardDaoImpl;
import vn.edu.ute.carsalesms.dao.impl.InstallmentPlanDaoImpl;
import vn.edu.ute.carsalesms.dao.impl.InvoiceDaoImpl;
import vn.edu.ute.carsalesms.dao.impl.PaymentDaoImpl;
import vn.edu.ute.carsalesms.dao.impl.PromotionDaoImpl;
import vn.edu.ute.carsalesms.dao.impl.SaleOrderDaoImpl;
import vn.edu.ute.carsalesms.dao.impl.StaffDaoImpl;
import vn.edu.ute.carsalesms.model.dto.AuthenticatedUser;
import vn.edu.ute.carsalesms.model.dto.DashboardTaskItem;
import vn.edu.ute.carsalesms.model.dto.StaffOverviewData;
import vn.edu.ute.carsalesms.service.CarService;
import vn.edu.ute.carsalesms.service.DashboardService;
import vn.edu.ute.carsalesms.service.impl.CarServiceImpl;
import vn.edu.ute.carsalesms.service.impl.CustomerServiceImpl;
import vn.edu.ute.carsalesms.service.impl.DashboardServiceImpl;
import vn.edu.ute.carsalesms.service.impl.InstallmentServiceImpl;
import vn.edu.ute.carsalesms.service.impl.InvoicePdfExporterImpl;
import vn.edu.ute.carsalesms.service.impl.InvoiceServiceImpl;
import vn.edu.ute.carsalesms.service.impl.PaymentServiceImpl;
import vn.edu.ute.carsalesms.service.impl.SaleOrderServiceImpl;
import vn.edu.ute.carsalesms.view.component.CarManagementPanel;
import vn.edu.ute.carsalesms.view.component.CustomerManagementPanel;
import vn.edu.ute.carsalesms.view.component.SaleOrderPanel;
import vn.edu.ute.carsalesms.view.component.PaymentPanel;
import vn.edu.ute.carsalesms.view.component.InstallmentPanel;
import vn.edu.ute.carsalesms.view.component.InvoicePanel;
import vn.edu.ute.carsalesms.view.component.SidebarMenuPanel;
import vn.edu.ute.carsalesms.view.component.StatCardPanel;
import vn.edu.ute.carsalesms.view.component.StatisticsPanel;
import vn.edu.ute.carsalesms.view.theme.UiPalette;
import vn.edu.ute.carsalesms.view.theme.UiSizing;

/**
 * Dashboard cho nhân viên bán hàng (STAFF).
 * Sidebar 10 modules theo phân quyền Staff.
 */
public class StaffDashboardFrame extends JFrame {

    private static final String CARD_OVERVIEW     = "overview";
    private static final String CARD_CARS         = "cars";
    private static final String CARD_CUSTOMERS    = "customers";
    private static final String CARD_ORDERS       = "orders";
    private static final String CARD_PAYMENTS     = "payments";
    private static final String CARD_INVOICES     = "invoices";
    private static final String CARD_INSTALLMENTS = "installments";
    private static final String CARD_TESTDRIVES   = "testdrives";
    private static final String CARD_WARRANTIES   = "warranties";
    private static final String CARD_STATISTICS   = "statistics";

    private record ModuleItem(String key, String title, String description) {
    }

    private static final List<ModuleItem> MODULE_ITEMS = List.of(
            new ModuleItem(CARD_OVERVIEW,     "Tổng quan", null),
            new ModuleItem(CARD_CARS,         "Quản lý xe",     "F03: CRUD xe, hãng, loại xe; tìm kiếm nhanh."),
            new ModuleItem(CARD_CUSTOMERS,    "Khách hàng",     "F04: CRUD khách hàng, lịch sử mua hàng."),
            new ModuleItem(CARD_ORDERS,       "Đơn bán",        "F08: Tạo đơn bán hàng."),
            new ModuleItem(CARD_PAYMENTS,     "Thanh toán",      "F09: Ghi nhận thanh toán."),
            new ModuleItem(CARD_INVOICES,     "Hóa đơn",        "F10: Xuất hóa đơn."),
            new ModuleItem(CARD_INSTALLMENTS, "Trả góp",        "F11: Theo dõi kế hoạch trả góp."),
            new ModuleItem(CARD_TESTDRIVES,   "Lái thử",        "F12: Đặt lịch, cập nhật trạng thái lái thử."),
            new ModuleItem(CARD_WARRANTIES,   "Bảo hành",       "F13: Tra cứu trạng thái bảo hành."),
            new ModuleItem(CARD_STATISTICS,   "Thống kê",       "F14: Thống kê doanh thu cá nhân.")
    );

    private static final List<SidebarMenuPanel.MenuItem> STAFF_SIDEBAR_ITEMS = MODULE_ITEMS.stream()
            .map(item -> new SidebarMenuPanel.MenuItem(item.key(), item.title()))
            .collect(Collectors.toList());

    private final CardLayout contentCardLayout = new CardLayout();
    private final JPanel contentCards = new JPanel(contentCardLayout);
    private final Runnable onLogoutRequested;
    private final Long currentStaffId;
    private final StaffOverviewData overviewData;
    private final CarManagementController carManagementController;
    private final CustomerManagementController customerManagementController;
    private final SaleOrderController saleOrderController;
    private final PaymentController paymentController;
    private final InstallmentController installmentController;
    private final InvoiceController invoiceController;
    private final StatisticsController statisticsController;

    public StaffDashboardFrame() {
        this(null,
                buildDefaultDashboardService(),
                buildDefaultCarManagementController(),
                buildDefaultCustomerManagementController(),
                buildDefaultSaleOrderController(),
                buildDefaultPaymentController(),
                buildDefaultInstallmentController(),
                buildDefaultInvoiceController(),
                buildDefaultStatisticsController(),
                () -> {
        });
    }

    public StaffDashboardFrame(Runnable onLogoutRequested) {
        this(null,
                buildDefaultDashboardService(),
                buildDefaultCarManagementController(),
                buildDefaultCustomerManagementController(),
                buildDefaultSaleOrderController(),
                buildDefaultPaymentController(),
                buildDefaultInstallmentController(),
                buildDefaultInvoiceController(),
                buildDefaultStatisticsController(),
                onLogoutRequested);
    }

    public StaffDashboardFrame(AuthenticatedUser currentUser, Runnable onLogoutRequested) {
        this(currentUser,
                buildDefaultDashboardService(),
                buildDefaultCarManagementController(),
                buildDefaultCustomerManagementController(),
                buildDefaultSaleOrderController(),
                buildDefaultPaymentController(),
                buildDefaultInstallmentController(),
                buildDefaultInvoiceController(),
                buildDefaultStatisticsController(),
                onLogoutRequested);
    }

    public StaffDashboardFrame(AuthenticatedUser currentUser,
                               DashboardService dashboardService,
                               CarManagementController carManagementController,
                               CustomerManagementController customerManagementController,
                               SaleOrderController saleOrderController,
                               PaymentController paymentController,
                               InstallmentController installmentController,
                               InvoiceController invoiceController,
                               StatisticsController statisticsController,
                               Runnable onLogoutRequested) {
        this.onLogoutRequested = Objects.requireNonNull(onLogoutRequested, "onLogoutRequested is required");
        this.currentStaffId = currentUser == null ? null : currentUser.staffId();
        this.overviewData = loadOverviewData(dashboardService, currentUser);
        this.carManagementController = Objects.requireNonNull(carManagementController, "carManagementController is required");
        this.customerManagementController = Objects.requireNonNull(customerManagementController, "customerManagementController is required");
        this.saleOrderController = Objects.requireNonNull(saleOrderController, "saleOrderController is required");
        this.paymentController = Objects.requireNonNull(paymentController, "paymentController is required");
        this.installmentController = Objects.requireNonNull(installmentController, "installmentController is required");
        this.invoiceController = Objects.requireNonNull(invoiceController, "invoiceController is required");
        this.statisticsController = Objects.requireNonNull(statisticsController, "statisticsController is required");
        setTitle("Car Sales Management - Staff Dashboard");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(UiSizing.WINDOW_MIN_SIZE);
        setSize(UiSizing.WINDOW_INITIAL_SIZE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UiPalette.APP_BACKGROUND);

        SidebarMenuPanel sidebar = new SidebarMenuPanel(
                resolveDisplayName(currentUser), resolveDisplayRole(currentUser),
                STAFF_SIDEBAR_ITEMS, CARD_OVERVIEW,
                this::switchContent,
                this.onLogoutRequested
        );
        root.add(sidebar, BorderLayout.WEST);

        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setOpaque(false);
        mainArea.setBorder(BorderFactory.createEmptyBorder(6, 10, 10, 10));
        mainArea.add(createContentCards(), BorderLayout.CENTER);

        root.add(mainArea, BorderLayout.CENTER);
        setContentPane(root);
    }

    private static DashboardService buildDefaultDashboardService() {
        return new DashboardServiceImpl(new DashboardDaoImpl());
    }

    private static CarManagementController buildDefaultCarManagementController() {
        CarDao carDao = new CarDaoImpl();
        CarService carService = new CarServiceImpl(carDao);
        return new CarManagementController(carService);
    }

    private static CustomerManagementController buildDefaultCustomerManagementController() {
        return new CustomerManagementController(new CustomerServiceImpl(new CustomerDaoImpl()));
    }

    private static SaleOrderController buildDefaultSaleOrderController() {
        return new SaleOrderController(new SaleOrderServiceImpl(
                new SaleOrderDaoImpl(),
                new CarDaoImpl(),
                new CustomerDaoImpl(),
                new StaffDaoImpl(),
                new PromotionDaoImpl()));
    }

    private static PaymentController buildDefaultPaymentController() {
        return new PaymentController(new PaymentServiceImpl(
                new PaymentDaoImpl(),
                new SaleOrderDaoImpl(),
                new InvoiceDaoImpl(),
                new InstallmentPlanDaoImpl()));
    }

    private static InstallmentController buildDefaultInstallmentController() {
        return new InstallmentController(new InstallmentServiceImpl(
                new InstallmentPlanDaoImpl(),
                new PaymentServiceImpl(new PaymentDaoImpl(), new SaleOrderDaoImpl(), new InvoiceDaoImpl(), new InstallmentPlanDaoImpl())));
    }

    private static InvoiceController buildDefaultInvoiceController() {
        return new InvoiceController(new InvoiceServiceImpl(new InvoiceDaoImpl(), new InvoicePdfExporterImpl()));
    }

    private static StatisticsController buildDefaultStatisticsController() {
        return new StatisticsController(new vn.edu.ute.carsalesms.service.impl.StatisticsServiceImpl(new vn.edu.ute.carsalesms.dao.impl.StatisticsDaoImpl()));
    }

    private StaffOverviewData loadOverviewData(DashboardService dashboardService, AuthenticatedUser currentUser) {
        if (dashboardService == null || currentUser == null || currentUser.staffId() == null) {
            return StaffOverviewData.empty();
        }
        try {
            return dashboardService.getStaffOverview(currentUser.staffId());
        } catch (Exception ex) {
            return StaffOverviewData.empty();
        }
    }

    private String resolveDisplayName(AuthenticatedUser currentUser) {
        if (currentUser == null || currentUser.fullName() == null || currentUser.fullName().isBlank()) {
            return "Sales Staff";
        }
        return currentUser.fullName();
    }

    private String resolveDisplayRole(AuthenticatedUser currentUser) {
        if (currentUser == null || currentUser.role() == null) {
            return "STAFF";
        }
        return currentUser.role().name();
    }

    private JPanel createContentCards() {
        contentCards.setOpaque(false);
        contentCards.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        addCardComponent(createOverviewPanel(), CARD_OVERVIEW);
        addCardComponent(createCarsPanel(), CARD_CARS);
        addCardComponent(createCustomersPanel(), CARD_CUSTOMERS);
        addCardComponent(createOrdersPanel(), CARD_ORDERS);
        addCardComponent(createPaymentsPanel(), CARD_PAYMENTS);
        addCardComponent(createInvoicesPanel(), CARD_INVOICES);
        addCardComponent(createInstallmentsPanel(), CARD_INSTALLMENTS);
        addCardComponent(createTestDrivesPanel(), CARD_TESTDRIVES);
        addCardComponent(createWarrantiesPanel(), CARD_WARRANTIES);
        addCardComponent(createStatisticsPanel(), CARD_STATISTICS);
        
        MODULE_ITEMS.stream()
                .filter(item -> item.description() != null
                        && !CARD_CARS.equals(item.key())
                        && !CARD_CUSTOMERS.equals(item.key())
                        && !CARD_ORDERS.equals(item.key())
                        && !CARD_PAYMENTS.equals(item.key())
                        && !CARD_INVOICES.equals(item.key())
                        && !CARD_INSTALLMENTS.equals(item.key())
                        && !CARD_TESTDRIVES.equals(item.key())
                        && !CARD_WARRANTIES.equals(item.key())
                        && !CARD_STATISTICS.equals(item.key()))
                .forEach(item -> addCardComponent(createPlaceholderPanel(item.title(), item.description()), item.key()));

        contentCardLayout.show(contentCards, CARD_OVERVIEW);
        return contentCards;
    }

    private void addCardComponent(JComponent card, String key) {
        card.setName(key);
        contentCards.add(card, key);
    }


    private JScrollPane createOverviewPanel() {
        JPanel overview = new JPanel(new BorderLayout(0, 8));
        overview.setOpaque(true);
        overview.setBackground(UiPalette.APP_BACKGROUND);

        JPanel statGrid = new JPanel(new GridLayout(1, 4, 8, 0));
        statGrid.setOpaque(false);
        statGrid.add(new StatCardPanel("Đơn cần xử lý", String.format("%02d", overviewData.pendingOrderCount()), UiPalette.WARNING));
        statGrid.add(new StatCardPanel("Doanh thu hôm nay", formatCurrencyShort(overviewData.todayRevenue()), UiPalette.KPI_BLUE));
        statGrid.add(new StatCardPanel("Lịch lái thử", String.format("%02d", overviewData.todayTestDriveCount()), UiPalette.SECONDARY));
        statGrid.add(new StatCardPanel("Bảo hành mở", String.format("%02d", overviewData.activeWarrantyCount()), UiPalette.DANGER));

        // Bảng công việc rộng (CENTER), lịch trình gọn bên phải (EAST)
        JPanel body = new JPanel(new BorderLayout(8, 0));
        body.setOpaque(false);
        body.add(createTaskQueuePanel(), BorderLayout.CENTER);

        JPanel timelinePanel = createTimelinePanel();
        timelinePanel.setPreferredSize(new Dimension(180, 0));
        body.add(timelinePanel, BorderLayout.EAST);

        overview.add(statGrid, BorderLayout.NORTH);
        overview.add(body, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(overview);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UiPalette.APP_BACKGROUND);
        return scrollPane;
    }

    private JPanel createTaskQueuePanel() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(0, 8));

        JLabel title = new JLabel("Danh sách công việc hôm nay");
        title.setForeground(UiPalette.TEXT_PRIMARY);
        title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));

        String[] columns = {"Tác vụ", "Khách", "Hạn", "Trạng thái"};
        Object[][] data = overviewData.taskItems().stream()
                .map(item -> new Object[]{
                        item.action(),
                        item.customerName(),
                        toDisplayTime(item.dueAt()),
                        toDisplayStatus(item.status())
                })
                .toArray(Object[][]::new);
        if (data.length == 0) {
            data = new Object[][]{{"Không có công việc", "-", "-", "-"}};
        }

        JTable table = new JTable(new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setBackground(UiPalette.TABLE_BACKGROUND);
        table.setGridColor(UiPalette.BORDER_SOFT);
        table.setSelectionBackground(UiPalette.PRIMARY_SOFT);
        table.setSelectionForeground(UiPalette.TEXT_PRIMARY);
        table.getTableHeader().setBackground(UiPalette.PRIMARY_SOFT);
        table.getTableHeader().setForeground(UiPalette.TEXT_PRIMARY);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UiPalette.BORDER_SOFT));

        card.add(title, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private JPanel createTimelinePanel() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout());

        JPanel inner = new JPanel(new GridLayout(5, 1, 0, 4));
        inner.setOpaque(false);

        JLabel header = new JLabel("Lịch trình hôm nay");
        header.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        header.setForeground(UiPalette.TEXT_PRIMARY);
        inner.add(header);

        List<DashboardTaskItem> timelineItems = overviewData.taskItems().stream().limit(4).toList();
        if (timelineItems.isEmpty()) {
            inner.add(createTimelineItem("Chưa có lịch hôm nay", UiPalette.TEXT_MUTED));
        } else {
            timelineItems.forEach(item -> inner.add(createTimelineItem(
                    toDisplayTime(item.dueAt()) + " - " + item.action(),
                    resolveTimelineColor(item.status())
            )));
        }

        card.add(inner, BorderLayout.NORTH);
        return card;
    }

    private JLabel createTimelineItem(String text, java.awt.Color barColor) {
        JLabel label = new JLabel(text);
        label.setOpaque(true);
        label.setBackground(UiPalette.SURFACE_ELEVATED);
        label.setForeground(UiPalette.TEXT_PRIMARY);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, barColor),
                BorderFactory.createEmptyBorder(5, 6, 5, 6)
        ));
        return label;
    }

    private String toDisplayTime(LocalDateTime dueAt) {
        if (dueAt == null) {
            return "--:--";
        }
        return String.format("%02d:%02d", dueAt.getHour(), dueAt.getMinute());
    }

    private String toDisplayStatus(String status) {
        return switch (status) {
            case "PENDING" -> "Đang xử lý";
            case "CONFIRMED" -> "Đã xác nhận";
            case "SCHEDULED" -> "Sắp đến";
            case "COMPLETED" -> "Hoàn tất";
            case "CANCELLED" -> "Đã hủy";
            default -> status;
        };
    }

    private java.awt.Color resolveTimelineColor(String status) {
        return switch (status) {
            case "PENDING" -> UiPalette.KPI_BLUE;
            case "CONFIRMED" -> UiPalette.SUCCESS;
            case "SCHEDULED" -> UiPalette.WARNING;
            default -> UiPalette.SECONDARY;
        };
    }

    private String formatCurrencyShort(BigDecimal amount) {
        BigDecimal safeAmount = amount == null ? BigDecimal.ZERO : amount;
        if (safeAmount.compareTo(BigDecimal.valueOf(1_000_000_000L)) >= 0) {
            return safeAmount.divide(BigDecimal.valueOf(1_000_000_000L), 1, java.math.RoundingMode.HALF_UP) + " tỷ";
        }
        if (safeAmount.compareTo(BigDecimal.valueOf(1_000_000L)) >= 0) {
            return safeAmount.divide(BigDecimal.valueOf(1_000_000L), 0, java.math.RoundingMode.HALF_UP) + " triệu";
        }
        return NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(safeAmount) + " đ";
    }

    private JPanel createCarsPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);

        JPanel headerCard = createCard();
        headerCard.setLayout(new BorderLayout());

        JLabel title = new JLabel("Quản lý xe");
        title.setForeground(UiPalette.TEXT_PRIMARY);
        title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 22));

        headerCard.add(title, BorderLayout.CENTER);

        wrapper.add(headerCard, BorderLayout.NORTH);
        wrapper.add(new CarManagementPanel(carManagementController, false), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createCustomersPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);

        JPanel headerCard = createCard();
        headerCard.setLayout(new BorderLayout());

        JLabel title = new JLabel("Quản lý Khách hàng");
        title.setForeground(UiPalette.TEXT_PRIMARY);
        title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 22));

        headerCard.add(title, BorderLayout.CENTER);

        wrapper.add(headerCard, BorderLayout.NORTH);
        wrapper.add(new CustomerManagementPanel(customerManagementController), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createOrdersPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);

        JPanel headerCard = createCard();
        headerCard.setLayout(new BorderLayout());

        JLabel title = new JLabel("Phân hệ Bán hàng (Đơn bán)");
        title.setForeground(UiPalette.TEXT_PRIMARY);
        title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 22));

        headerCard.add(title, BorderLayout.CENTER);

        wrapper.add(headerCard, BorderLayout.NORTH);
        wrapper.add(new SaleOrderPanel(saleOrderController), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createPaymentsPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);

        JPanel headerCard = createCard();
        headerCard.setLayout(new BorderLayout());

        JLabel title = new JLabel("Lịch sử & Ghi nhận Thanh toán");
        title.setForeground(UiPalette.TEXT_PRIMARY);
        title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 22));

        headerCard.add(title, BorderLayout.CENTER);

        wrapper.add(headerCard, BorderLayout.NORTH);
        wrapper.add(new PaymentPanel(saleOrderController, paymentController), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createInvoicesPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);

        JPanel headerCard = createCard();
        headerCard.setLayout(new BorderLayout());

        JLabel title = new JLabel("Quản lý Hóa đơn");
        title.setForeground(UiPalette.TEXT_PRIMARY);
        title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 22));

        headerCard.add(title, BorderLayout.CENTER);

        wrapper.add(headerCard, BorderLayout.NORTH);
        wrapper.add(new InvoicePanel(invoiceController), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createInstallmentsPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);

        JPanel headerCard = createCard();
        headerCard.setLayout(new BorderLayout());

        JLabel title = new JLabel("Quản lý Trả góp");
        title.setForeground(UiPalette.TEXT_PRIMARY);
        title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 22));

        headerCard.add(title, BorderLayout.CENTER);

        wrapper.add(headerCard, BorderLayout.NORTH);
        wrapper.add(new InstallmentPanel(installmentController, saleOrderController), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createTestDrivesPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);

        JPanel headerCard = createCard();
        headerCard.setLayout(new BorderLayout());

        JLabel title = new JLabel("Đăng Ký Khách Lái Thử");
        title.setForeground(UiPalette.TEXT_PRIMARY);
        title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 22));

        headerCard.add(title, BorderLayout.CENTER);

        // Khởi tạo các Service
        vn.edu.ute.carsalesms.dao.TestDriveDao tDao = new vn.edu.ute.carsalesms.dao.impl.TestDriveDaoImpl();
        vn.edu.ute.carsalesms.dao.CustomerDao cDao = new vn.edu.ute.carsalesms.dao.impl.CustomerDaoImpl();
        vn.edu.ute.carsalesms.dao.CarDao carDao = new vn.edu.ute.carsalesms.dao.impl.CarDaoImpl();
        vn.edu.ute.carsalesms.dao.StaffDao sDao = new vn.edu.ute.carsalesms.dao.impl.StaffDaoImpl();
        vn.edu.ute.carsalesms.service.AuditLogService auditService = new vn.edu.ute.carsalesms.service.impl.AuditLogServiceImpl(new vn.edu.ute.carsalesms.dao.impl.AuditLogDaoImpl(), sDao);
        vn.edu.ute.carsalesms.service.TestDriveService tService = new vn.edu.ute.carsalesms.service.impl.TestDriveServiceImpl(tDao, cDao, carDao, sDao, auditService);

        wrapper.add(headerCard, BorderLayout.NORTH);
        wrapper.add(new vn.edu.ute.carsalesms.view.component.TestDrivePanel(tService, cDao, carDao, sDao), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createWarrantiesPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);

        JPanel headerCard = createCard();
        headerCard.setLayout(new BorderLayout());

        JLabel title = new JLabel("Hồ Sơ Bảo Hành Tự Động 3 Năm");
        title.setForeground(UiPalette.TEXT_PRIMARY);
        title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 22));

        headerCard.add(title, BorderLayout.CENTER);

        vn.edu.ute.carsalesms.dao.WarrantyDao wDao = new vn.edu.ute.carsalesms.dao.impl.WarrantyDaoImpl();
        vn.edu.ute.carsalesms.dao.SaleOrderDao oDao = new vn.edu.ute.carsalesms.dao.impl.SaleOrderDaoImpl();
        vn.edu.ute.carsalesms.dao.StaffDao sDao = new vn.edu.ute.carsalesms.dao.impl.StaffDaoImpl();
        vn.edu.ute.carsalesms.service.AuditLogService auditService = new vn.edu.ute.carsalesms.service.impl.AuditLogServiceImpl(new vn.edu.ute.carsalesms.dao.impl.AuditLogDaoImpl(), sDao);
        vn.edu.ute.carsalesms.service.WarrantyService wService = new vn.edu.ute.carsalesms.service.impl.WarrantyServiceImpl(wDao, oDao, auditService);

        wrapper.add(headerCard, BorderLayout.NORTH);
        wrapper.add(new vn.edu.ute.carsalesms.view.component.WarrantyPanel(wService), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createStatisticsPanel() {
        if (currentStaffId == null) {
            return createPlaceholderPanel("Thống kê", "Không xác định được nhân viên hiện tại để tải dữ liệu thống kê.");
        }

        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);

        JPanel headerCard = createCard();
        headerCard.setLayout(new BorderLayout());

        JLabel title = new JLabel("Thống kê");
        title.setForeground(UiPalette.TEXT_PRIMARY);
        title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 22));

        headerCard.add(title, BorderLayout.CENTER);

        wrapper.add(headerCard, BorderLayout.NORTH);
        wrapper.add(new StatisticsPanel(statisticsController, currentStaffId), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createPlaceholderPanel(String titleText, String descriptionText) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        JPanel card = createCard();
        card.setLayout(new GridLayout(2, 1));

        JLabel title = new JLabel(titleText);
        title.setForeground(UiPalette.TEXT_PRIMARY);
        title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 20));

        JLabel desc = new JLabel(descriptionText);
        desc.setForeground(UiPalette.TEXT_SECONDARY);
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        card.add(title);
        card.add(desc);
        wrapper.add(card, BorderLayout.NORTH);
        return wrapper;
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

    private void switchContent(String cardKey) {
        refreshCardByKey(cardKey);
        contentCardLayout.show(contentCards, cardKey);
        contentCards.revalidate();
        contentCards.repaint();
    }

    private void refreshCardByKey(String cardKey) {
        JComponent refreshed = createCardByKey(cardKey);
        if (refreshed == null) {
            return;
        }

        for (Component c : contentCards.getComponents()) {
            if (cardKey.equals(c.getName())) {
                contentCards.remove(c);
                break;
            }
        }

        addCardComponent(refreshed, cardKey);
    }

    private JComponent createCardByKey(String cardKey) {
        return switch (cardKey) {
            case CARD_OVERVIEW -> createOverviewPanel();
            case CARD_CARS -> createCarsPanel();
            case CARD_CUSTOMERS -> createCustomersPanel();
            case CARD_ORDERS -> createOrdersPanel();
            case CARD_PAYMENTS -> createPaymentsPanel();
            case CARD_INVOICES -> createInvoicesPanel();
            case CARD_INSTALLMENTS -> createInstallmentsPanel();
            case CARD_TESTDRIVES -> createTestDrivesPanel();
            case CARD_WARRANTIES -> createWarrantiesPanel();
            case CARD_STATISTICS -> createStatisticsPanel();
            default -> MODULE_ITEMS.stream()
                    .filter(item -> item.key().equals(cardKey) && item.description() != null)
                    .findFirst()
                    .map(item -> createPlaceholderPanel(item.title(), item.description()))
                    .orElse(null);
        };
    }
}
