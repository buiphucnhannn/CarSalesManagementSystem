package vn.edu.ute.carsalesms.view.admin;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.text.NumberFormat;
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
import vn.edu.ute.carsalesms.controller.BranchManagementController;
import vn.edu.ute.carsalesms.controller.AuditLogController;
import vn.edu.ute.carsalesms.controller.CustomerManagementController;
import vn.edu.ute.carsalesms.controller.StaffManagementController;
import vn.edu.ute.carsalesms.controller.SaleOrderController;
import vn.edu.ute.carsalesms.controller.PaymentController;
import vn.edu.ute.carsalesms.controller.InstallmentController;
import vn.edu.ute.carsalesms.controller.InvoiceController;
import vn.edu.ute.carsalesms.controller.PromotionController;
import vn.edu.ute.carsalesms.controller.StatisticsController;
import vn.edu.ute.carsalesms.dao.CarDao;
import vn.edu.ute.carsalesms.dao.impl.*;
import vn.edu.ute.carsalesms.model.dto.AdminOverviewData;
import vn.edu.ute.carsalesms.model.dto.AuthenticatedUser;
import vn.edu.ute.carsalesms.service.CarService;
import vn.edu.ute.carsalesms.service.DashboardService;
import vn.edu.ute.carsalesms.service.impl.*;
import vn.edu.ute.carsalesms.view.component.CarManagementPanel;
import vn.edu.ute.carsalesms.view.component.BranchManagementPanel;
import vn.edu.ute.carsalesms.view.component.AuditLogPanel;
import vn.edu.ute.carsalesms.view.component.CustomerManagementPanel;
import vn.edu.ute.carsalesms.view.component.SaleOrderPanel;
import vn.edu.ute.carsalesms.view.component.PaymentPanel;
import vn.edu.ute.carsalesms.view.component.InstallmentPanel;
import vn.edu.ute.carsalesms.view.component.InvoicePanel;
import vn.edu.ute.carsalesms.view.component.PromotionPanel;
import vn.edu.ute.carsalesms.view.component.SidebarMenuPanel;
import vn.edu.ute.carsalesms.view.component.StaffManagementPanel;
import vn.edu.ute.carsalesms.view.component.StatCardPanel;
import vn.edu.ute.carsalesms.view.component.StatisticsPanel;
import vn.edu.ute.carsalesms.view.theme.UiPalette;
import vn.edu.ute.carsalesms.view.theme.UiSizing;

/**
 * Màn hình dashboard chính cho admin hệ thống quản lý bán xe.
 * Sidebar đầy đủ 14 modules (F01-F15).
 */
public class AdminDashboardFrame extends JFrame {

    private record ModuleItem(String key, String label, String description) {
    }

    private static final String CARD_DASHBOARD    = "dashboard";
    private static final String CARD_CARS         = "cars";
    private static final String CARD_CUSTOMERS    = "customers";
    private static final String CARD_STAFF        = "staff";
    private static final String CARD_BRANCHES     = "branches";
    private static final String CARD_PROMOTIONS   = "promotions";
    private static final String CARD_ORDERS       = "orders";
    private static final String CARD_PAYMENTS     = "payments";
    private static final String CARD_INVOICES     = "invoices";
    private static final String CARD_INSTALLMENTS = "installments";
    private static final String CARD_TESTDRIVES   = "testdrives";
    private static final String CARD_WARRANTIES   = "warranties";
    private static final String CARD_STATISTICS   = "statistics";
    private static final String CARD_AUDITLOG     = "auditlog";

    private static final List<ModuleItem> MODULE_ITEMS = List.of(
            new ModuleItem(CARD_DASHBOARD,    "Tổng quan", null),
            new ModuleItem(CARD_CARS,         "Quản lý xe", "F03: CRUD xe, hãng, loại xe; quản lý tồn kho và tìm kiếm nhanh."),
            new ModuleItem(CARD_CUSTOMERS,    "Khách hàng", "F04: CRUD khách hàng, theo dõi lịch sử mua hàng."),
            new ModuleItem(CARD_STAFF,        "Nhân viên", "F05: Thêm, sửa, khóa tài khoản nhân viên, phân quyền Admin/Staff."),
            new ModuleItem(CARD_BRANCHES,     "Chi nhánh", "F06: Quản lý xe và doanh thu theo chi nhánh."),
            new ModuleItem(CARD_PROMOTIONS,   "Khuyến mãi", "F07: Chương trình giảm giá, thời hạn và điều kiện áp dụng."),
            new ModuleItem(CARD_ORDERS,       "Đơn bán", "F08: Tạo đơn, dòng chi tiết, cập nhật trạng thái đơn hàng."),
            new ModuleItem(CARD_PAYMENTS,     "Thanh toán", "F09: Ghi nhận thanh toán (Cash/Bank/Installment)."),
            new ModuleItem(CARD_INVOICES,     "Hóa đơn", "F10: Xuất hóa đơn cho đơn hàng đã thanh toán."),
            new ModuleItem(CARD_INSTALLMENTS, "Trả góp", "F11: Kế hoạch trả góp, theo dõi các kỳ thanh toán."),
            new ModuleItem(CARD_TESTDRIVES,   "Lái thử", "F12: Đặt lịch, xác nhận và cập nhật trạng thái lái thử."),
            new ModuleItem(CARD_WARRANTIES,   "Bảo hành", "F13: Khởi tạo bảo hành, tra cứu trạng thái."),
            new ModuleItem(CARD_STATISTICS,   "Thống kê", "F14: Dashboard doanh thu ngày/tháng/quý, top xe bán chạy."),
            new ModuleItem(CARD_AUDITLOG,     "Nhật ký", "F15: Audit log - ghi lại mọi thao tác trong hệ thống.")
    );

    private static final List<SidebarMenuPanel.MenuItem> ADMIN_SIDEBAR_ITEMS = MODULE_ITEMS.stream()
            .map(item -> new SidebarMenuPanel.MenuItem(item.key(), item.label()))
            .collect(Collectors.toList());

    private final CardLayout contentCardLayout = new CardLayout();
    private final JPanel contentCards = new JPanel(contentCardLayout);
    private final Runnable onLogoutRequested;
    private final DashboardService dashboardService;
    // Đổi AdminOverviewData sang không final để load đè
    private AdminOverviewData overviewData;
    private final CarManagementController carManagementController;
    private final BranchManagementController branchManagementController;
    /** Controller quản lý khách hàng (F04). */
    private final CustomerManagementController customerManagementController;
    /** Controller quản lý nhân viên và tài khoản (F05). */
    private final StaffManagementController staffManagementController;
    private final SaleOrderController saleOrderController;
    private final PaymentController paymentController;
    private final InstallmentController installmentController;
    private final InvoiceController invoiceController;
    private final PromotionController promotionController;
    private final StatisticsController statisticsController;
    private final AuditLogController auditLogController;

    // Các Constructor Test/Rỗng đã bị gỡ bỏ để tránh gây lỗi Build và giúp luồng tập trung vào AppLauncher.

    public AdminDashboardFrame(AuthenticatedUser currentUser,
                               DashboardService dashboardService,
                               CarManagementController carManagementController,
                               BranchManagementController branchManagementController,
                               CustomerManagementController customerManagementController,
                               StaffManagementController staffManagementController,
                               SaleOrderController saleOrderController,
                               PaymentController paymentController,
                               InstallmentController installmentController,
                               InvoiceController invoiceController,
                               PromotionController promotionController,
                               StatisticsController statisticsController,
                               Runnable onLogoutRequested) {
        this.onLogoutRequested = Objects.requireNonNull(onLogoutRequested, "onLogoutRequested is required");
        this.dashboardService = Objects.requireNonNull(dashboardService, "dashboardService is required");
        this.overviewData = loadOverviewData(dashboardService);
        this.carManagementController = Objects.requireNonNull(carManagementController);
        this.branchManagementController = Objects.requireNonNull(branchManagementController);
        this.customerManagementController = Objects.requireNonNull(customerManagementController);
        this.staffManagementController = Objects.requireNonNull(staffManagementController);
        this.saleOrderController = Objects.requireNonNull(saleOrderController);
        this.paymentController = Objects.requireNonNull(paymentController);
        this.installmentController = Objects.requireNonNull(installmentController);
        this.invoiceController = Objects.requireNonNull(invoiceController);
        this.promotionController = Objects.requireNonNull(promotionController);
        this.statisticsController = Objects.requireNonNull(statisticsController);
        this.auditLogController = buildDefaultAuditLogController();
        setTitle("Car Sales Management - Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(UiSizing.WINDOW_MIN_SIZE);
        setSize(UiSizing.WINDOW_INITIAL_SIZE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UiPalette.APP_BACKGROUND);

        SidebarMenuPanel sidebar = new SidebarMenuPanel(
                resolveDisplayName(currentUser), resolveDisplayRole(currentUser),
                ADMIN_SIDEBAR_ITEMS, CARD_DASHBOARD,
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

    private static BranchManagementController buildDefaultBranchManagementController() {
        return new BranchManagementController(new BranchServiceImpl(new BranchDaoImpl()));
    }

    /** Khởi tạo CustomerManagementController với dependency mặc định. */
    private static CustomerManagementController buildDefaultCustomerManagementController() {
        return new CustomerManagementController(new CustomerServiceImpl(new CustomerDaoImpl()));
    }

    /** Khởi tạo StaffManagementController với dependency mặc định. */
    private static StaffManagementController buildDefaultStaffManagementController() {
        return new StaffManagementController(new StaffServiceImpl(new StaffDaoImpl()));
    }

    private static SaleOrderController buildDefaultSaleOrderController() {
        return new SaleOrderController(new SaleOrderServiceImpl(new SaleOrderDaoImpl(), new CarDaoImpl(), new CustomerDaoImpl(), new StaffDaoImpl(), new PromotionDaoImpl()));
    }

    private static PaymentController buildDefaultPaymentController() {
        return new PaymentController(new PaymentServiceImpl(new PaymentDaoImpl(), new SaleOrderDaoImpl(), new InvoiceDaoImpl(), new InstallmentPlanDaoImpl()));
    }

    private static AuditLogController buildDefaultAuditLogController() {
        return new AuditLogController(new AuditLogServiceImpl(new AuditLogDaoImpl(), new StaffDaoImpl()));
    }

    private static StatisticsController buildDefaultStatisticsController() {
        return new StatisticsController(new StatisticsServiceImpl(new StatisticsDaoImpl()));
    }

    private AdminOverviewData loadOverviewData(DashboardService dashboardService) {
        if (dashboardService == null) {
            return AdminOverviewData.empty();
        }
        try {
            return dashboardService.getAdminOverview();
        } catch (Exception ex) {
            return AdminOverviewData.empty();
        }
    }

    private String resolveDisplayName(AuthenticatedUser currentUser) {
        if (currentUser == null || currentUser.fullName() == null || currentUser.fullName().isBlank()) {
            return "Car Sales Admin";
        }
        return currentUser.fullName();
    }

    private String resolveDisplayRole(AuthenticatedUser currentUser) {
        if (currentUser == null || currentUser.role() == null) {
            return "ADMIN";
        }
        return currentUser.role().name();
    }

    private JPanel createContentCards() {
        contentCards.setOpaque(false);
        contentCards.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        addCardComponent(createDashboardPanel(), CARD_DASHBOARD);
        addCardComponent(createCarsPanel(), CARD_CARS);
        addCardComponent(createBranchesPanel(), CARD_BRANCHES);
        addCardComponent(createCustomersPanel(), CARD_CUSTOMERS);
        addCardComponent(createStaffPanel(), CARD_STAFF);
        addCardComponent(createOrdersPanel(), CARD_ORDERS);
        addCardComponent(createPaymentsPanel(), CARD_PAYMENTS);
        addCardComponent(createInstallmentsPanel(), CARD_INSTALLMENTS);
        addCardComponent(createInvoicesPanel(), CARD_INVOICES);
        addCardComponent(createPromotionsPanel(), CARD_PROMOTIONS);
        addCardComponent(createStatisticsPanel(), CARD_STATISTICS);
        addCardComponent(createAuditLogPanel(), CARD_AUDITLOG);
        addCardComponent(createTestDrivesPanel(), CARD_TESTDRIVES);
        addCardComponent(createWarrantiesPanel(), CARD_WARRANTIES);

        // Các module còn lại vẫn là placeholder chờ triển khai
        MODULE_ITEMS.stream()
                .filter(item -> item.description() != null
                        && !CARD_CARS.equals(item.key())
                        && !CARD_BRANCHES.equals(item.key())
                        && !CARD_CUSTOMERS.equals(item.key())
                        && !CARD_STAFF.equals(item.key())
                        && !CARD_ORDERS.equals(item.key())
                        && !CARD_PAYMENTS.equals(item.key())
                        && !CARD_INSTALLMENTS.equals(item.key())
                        && !CARD_INVOICES.equals(item.key())
                        && !CARD_PROMOTIONS.equals(item.key())
                        && !CARD_STATISTICS.equals(item.key())
                        && !CARD_AUDITLOG.equals(item.key())
                        && !CARD_TESTDRIVES.equals(item.key())
                        && !CARD_WARRANTIES.equals(item.key()))
                .forEach(item -> addCardComponent(createModulePlaceholderPanel(item.label(), item.description()), item.key()));

        contentCardLayout.show(contentCards, CARD_DASHBOARD);
        return contentCards;
    }

    private void addCardComponent(JComponent card, String key) {
        card.setName(key);
        contentCards.add(card, key);
    }


    private JScrollPane createDashboardPanel() {
        JPanel dashboard = new JPanel(new BorderLayout(0, 8));
        dashboard.setOpaque(true);
        dashboard.setBackground(UiPalette.APP_BACKGROUND);

        // Stat cards row - fill width equally
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 8, 0));
        statsGrid.setOpaque(false);
        statsGrid.add(new StatCardPanel("Doanh thu tháng", formatCurrencyShort(overviewData.monthlyRevenue()), UiPalette.KPI_BLUE));
        statsGrid.add(new StatCardPanel("Đơn bán hôm nay", String.valueOf(overviewData.todayOrderCount()), UiPalette.KPI_GREEN));
        statsGrid.add(new StatCardPanel("Khách chờ tư vấn", String.valueOf(overviewData.pendingOrderCount()), UiPalette.KPI_AMBER));
        statsGrid.add(new StatCardPanel("Lịch lái thử", String.valueOf(overviewData.todayTestDriveCount()), UiPalette.KPI_PURPLE));

        // Bảng đơn bán chiếm toàn bộ vùng nội dung chính
        JPanel bottomSection = new JPanel(new BorderLayout());
        bottomSection.setOpaque(false);
        bottomSection.add(createRecentOrdersPanel(), BorderLayout.CENTER);

        dashboard.add(statsGrid, BorderLayout.NORTH);
        dashboard.add(bottomSection, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(dashboard);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        return scrollPane;
    }

    private JPanel createCarsPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);

        JPanel headerCard = createModuleHeaderCard(
                "Quản lý xe",
                "Theo dõi xe, hãng xe và loại xe; hỗ trợ thêm/sửa/ngừng kinh doanh và tra cứu nhanh."
        );

        wrapper.add(headerCard, BorderLayout.NORTH);
        wrapper.add(new CarManagementPanel(carManagementController, true), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createBranchesPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);

        JPanel headerCard = createModuleHeaderCard(
                "Quản lý Chi nhánh",
                "Quản lý thông tin chi nhánh và theo dõi hiệu quả bán hàng theo từng chi nhánh."
        );

        wrapper.add(headerCard, BorderLayout.NORTH);
        wrapper.add(new BranchManagementPanel(branchManagementController), BorderLayout.CENTER);
        return wrapper;
    }

    /**
     * Tạo panel Quản lý Khách hàng (F04).
     * Tích hợp CustomerManagementPanel vào content area với header card.
     */
    private JPanel createCustomersPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);

        JPanel headerCard = createModuleHeaderCard(
                "Quản lý Khách hàng",
                "Lưu trữ hồ sơ khách hàng, cập nhật thông tin liên hệ và hỗ trợ tra cứu lịch sử giao dịch."
        );

        wrapper.add(headerCard, BorderLayout.NORTH);
        wrapper.add(new CustomerManagementPanel(customerManagementController), BorderLayout.CENTER);
        return wrapper;
    }

    /**
     * Tạo panel Quản lý Nhân viên (F05).
     * Tích hợp StaffManagementPanel (2 tab) vào content area.
     */
    private JPanel createStaffPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);

        JPanel headerCard = createModuleHeaderCard(
                "Quản lý Nhân viên & Tài khoản",
                "Quản trị hồ sơ nhân viên, phân quyền, tạo tài khoản, khóa/mở khóa và đặt lại mật khẩu."
        );

        wrapper.add(headerCard, BorderLayout.NORTH);
        wrapper.add(new StaffManagementPanel(staffManagementController), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createOrdersPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);

        JPanel headerCard = createModuleHeaderCard(
                "Phân hệ Bán hàng (Đơn bán)",
                "Tạo đơn bán, theo dõi trạng thái đơn và quản lý chi tiết các xe trong từng đơn hàng."
        );

        wrapper.add(headerCard, BorderLayout.NORTH);
        wrapper.add(new SaleOrderPanel(saleOrderController), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createPaymentsPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);

        JPanel headerCard = createModuleHeaderCard(
                "Lịch sử & Ghi nhận Thanh toán",
                "Ghi nhận các lần thanh toán, theo dõi dư nợ và kiểm soát tiến độ thu tiền theo đơn bán."
        );

        wrapper.add(headerCard, BorderLayout.NORTH);
        wrapper.add(new PaymentPanel(saleOrderController, paymentController), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createInstallmentsPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);

        JPanel headerCard = createModuleHeaderCard(
                "Quản lý Trả góp",
                "Theo dõi lịch trả góp, trạng thái từng kỳ và ghi nhận thanh toán cho hợp đồng trả góp."
        );

        wrapper.add(headerCard, BorderLayout.NORTH);
        wrapper.add(new InstallmentPanel(installmentController, saleOrderController), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createInvoicesPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);

        JPanel headerCard = createModuleHeaderCard(
                "Quản lý Hóa đơn",
                "Tra cứu hóa đơn đã phát hành và xuất file PDF phục vụ đối soát, lưu trữ."
        );

        wrapper.add(headerCard, BorderLayout.NORTH);
        wrapper.add(new InvoicePanel(invoiceController), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createPromotionsPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);

        JPanel headerCard = createModuleHeaderCard(
                "Quản lý Khuyến mãi",
                "Thiết lập chương trình khuyến mãi, cập nhật điều kiện áp dụng và quản lý trạng thái hoạt động."
        );

        wrapper.add(headerCard, BorderLayout.NORTH);
        wrapper.add(new PromotionPanel(promotionController), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createAuditLogPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);

        JPanel headerCard = createModuleHeaderCard(
                "Nhật ký hệ thống",
                "Theo dõi toàn bộ thao tác người dùng theo thời gian: tạo/sửa/xóa, đăng nhập, thanh toán, xuất hóa đơn..."
        );

        wrapper.add(headerCard, BorderLayout.NORTH);
        wrapper.add(new AuditLogPanel(auditLogController), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createStatisticsPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);

        JPanel headerCard = createModuleHeaderCard(
                "Thống kê",
                "Báo cáo doanh thu theo thời gian, phân tích trạng thái đơn hàng, top xe bán chạy và hiệu quả chi nhánh."
        );

        wrapper.add(headerCard, BorderLayout.NORTH);
        wrapper.add(new StatisticsPanel(statisticsController, null), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createRecentOrdersPanel() {
        JPanel container = createWhiteCard();
        container.setLayout(new BorderLayout(0, 8));

        JLabel title = new JLabel("Đơn bán gần nhất");
        title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        title.setForeground(UiPalette.TEXT_PRIMARY);

        String[] columns = {"Mã đơn", "Khách hàng", "Mẫu xe", "Trạng thái"};
        Object[][] data = overviewData.recentOrders().stream()
                .map(item -> new Object[]{
                        item.orderCode(),
                        item.customerName(),
                        item.carName(),
                        toDisplayOrderStatus(item.status())
                })
                .toArray(Object[][]::new);
        if (data.length == 0) {
            data = new Object[][]{{"-", "Chưa có dữ liệu", "-", "-"}};
        }

        JTable table = new JTable(new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setGridColor(UiPalette.BORDER_SOFT);
        table.setBackground(UiPalette.TABLE_BACKGROUND);
        table.setSelectionBackground(UiPalette.PRIMARY_SOFT);
        table.setSelectionForeground(UiPalette.TEXT_PRIMARY);
        table.getTableHeader().setBackground(UiPalette.PRIMARY_SOFT);
        table.getTableHeader().setForeground(UiPalette.TEXT_PRIMARY);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UiPalette.BORDER_SOFT));
        scrollPane.getViewport().setBackground(UiPalette.TABLE_BACKGROUND);

        container.add(title, BorderLayout.NORTH);
        container.add(scrollPane, BorderLayout.CENTER);
        return container;
    }


    private JPanel createModulePlaceholderPanel(String moduleTitle, String description) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        JPanel card = createWhiteCard();
        card.setLayout(new GridLayout(2, 1));

        JLabel title = new JLabel(moduleTitle);
        title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 20));
        title.setForeground(UiPalette.TEXT_PRIMARY);

        JLabel desc = new JLabel(description);
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        desc.setForeground(UiPalette.TEXT_SECONDARY);

        card.add(title);
        card.add(desc);
        wrapper.add(card, BorderLayout.NORTH);
        return wrapper;
    }

    private JPanel createWhiteCard() {
        JPanel panel = new JPanel();
        panel.setOpaque(true);
        panel.setBackground(UiPalette.SURFACE_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiPalette.BORDER_SOFT),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        return panel;
    }

    private JPanel createModuleHeaderCard(String titleText, String descriptionText) {
        JPanel card = createWhiteCard();
        card.setLayout(new BorderLayout(0, 4));

        JLabel title = new JLabel(titleText);
        title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 22));
        title.setForeground(UiPalette.TEXT_PRIMARY);

        JLabel desc = new JLabel(descriptionText);
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        desc.setForeground(UiPalette.TEXT_SECONDARY);

        card.add(title, BorderLayout.NORTH);
        card.add(desc, BorderLayout.CENTER);
        return card;
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
            case CARD_DASHBOARD -> {
                // Tự động Cập nhật lại Số Liệu Tổng Quan (Live Update) khi User Bấm vào Tab Dashboard
                this.overviewData = loadOverviewData(this.dashboardService);
                yield createDashboardPanel();
            }
            case CARD_CARS -> createCarsPanel();
            case CARD_BRANCHES -> createBranchesPanel();
            case CARD_CUSTOMERS -> createCustomersPanel();
            case CARD_STAFF -> createStaffPanel();
            case CARD_ORDERS -> createOrdersPanel();
            case CARD_PAYMENTS -> createPaymentsPanel();
            case CARD_INSTALLMENTS -> createInstallmentsPanel();
            case CARD_INVOICES -> createInvoicesPanel();
            case CARD_PROMOTIONS -> createPromotionsPanel();
            case CARD_STATISTICS -> createStatisticsPanel();
            case CARD_AUDITLOG -> createAuditLogPanel();
            case CARD_TESTDRIVES -> createTestDrivesPanel();
            case CARD_WARRANTIES -> createWarrantiesPanel();
            default -> MODULE_ITEMS.stream()
                    .filter(item -> item.key().equals(cardKey) && item.description() != null)
                    .findFirst()
                    .map(item -> createModulePlaceholderPanel(item.label(), item.description()))
                    .orElse(null);
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

    private String toDisplayOrderStatus(String status) {
        return switch (status) {
            case "PENDING" -> "Đang xử lý";
            case "CONFIRMED" -> "Đã xác nhận";
            case "PAID" -> "Đã thanh toán";
            case "CANCELLED" -> "Đã hủy";
            default -> status;
        };
    }

    private JPanel createTestDrivesPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);

        JPanel headerCard = createModuleHeaderCard(
                "Quản lý Lái thử",
                "Lập lịch lái thử, phân công nhân viên phụ trách và theo dõi tiến độ xác nhận lịch hẹn."
        );

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

        JPanel headerCard = createModuleHeaderCard(
                "Quản lý Bảo hành",
                "Tra cứu và cập nhật hồ sơ bảo hành theo xe, theo đơn bán và theo thời gian hiệu lực."
        );

        vn.edu.ute.carsalesms.dao.WarrantyDao wDao = new vn.edu.ute.carsalesms.dao.impl.WarrantyDaoImpl();
        vn.edu.ute.carsalesms.dao.SaleOrderDao oDao = new vn.edu.ute.carsalesms.dao.impl.SaleOrderDaoImpl();
        vn.edu.ute.carsalesms.dao.StaffDao sDao = new vn.edu.ute.carsalesms.dao.impl.StaffDaoImpl();
        vn.edu.ute.carsalesms.service.AuditLogService auditService = new vn.edu.ute.carsalesms.service.impl.AuditLogServiceImpl(new vn.edu.ute.carsalesms.dao.impl.AuditLogDaoImpl(), sDao);
        vn.edu.ute.carsalesms.service.WarrantyService wService = new vn.edu.ute.carsalesms.service.impl.WarrantyServiceImpl(wDao, oDao, auditService);

        wrapper.add(headerCard, BorderLayout.NORTH);
        wrapper.add(new vn.edu.ute.carsalesms.view.component.WarrantyPanel(wService), BorderLayout.CENTER);
        return wrapper;
    }
}
