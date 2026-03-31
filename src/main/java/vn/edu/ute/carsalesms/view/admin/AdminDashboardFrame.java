package vn.edu.ute.carsalesms.view.admin;

import java.awt.BorderLayout;
import java.awt.CardLayout;
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import vn.edu.ute.carsalesms.controller.CarManagementController;
import vn.edu.ute.carsalesms.controller.BranchManagementController;
import vn.edu.ute.carsalesms.controller.CustomerManagementController;
import vn.edu.ute.carsalesms.controller.StaffManagementController;
import vn.edu.ute.carsalesms.controller.SaleOrderController;
import vn.edu.ute.carsalesms.controller.PaymentController;
import vn.edu.ute.carsalesms.controller.InstallmentController;
import vn.edu.ute.carsalesms.controller.InvoiceController;
import vn.edu.ute.carsalesms.controller.PromotionController;
import vn.edu.ute.carsalesms.dao.CarDao;
import vn.edu.ute.carsalesms.dao.impl.*;
import vn.edu.ute.carsalesms.model.dto.AdminOverviewData;
import vn.edu.ute.carsalesms.model.dto.AuthenticatedUser;
import vn.edu.ute.carsalesms.service.CarService;
import vn.edu.ute.carsalesms.service.DashboardService;
import vn.edu.ute.carsalesms.service.SaleOrderService;
import vn.edu.ute.carsalesms.service.PaymentService;
import vn.edu.ute.carsalesms.service.impl.*;
import vn.edu.ute.carsalesms.view.component.CarManagementPanel;
import vn.edu.ute.carsalesms.view.component.BranchManagementPanel;
import vn.edu.ute.carsalesms.view.component.CustomerManagementPanel;
import vn.edu.ute.carsalesms.view.component.SaleOrderPanel;
import vn.edu.ute.carsalesms.view.component.PaymentPanel;
import vn.edu.ute.carsalesms.view.component.InstallmentPanel;
import vn.edu.ute.carsalesms.view.component.InvoicePanel;
import vn.edu.ute.carsalesms.view.component.PromotionPanel;
import vn.edu.ute.carsalesms.view.component.SidebarMenuPanel;
import vn.edu.ute.carsalesms.view.component.StaffManagementPanel;
import vn.edu.ute.carsalesms.view.component.StatCardPanel;
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
    private final AdminOverviewData overviewData;
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

    public AdminDashboardFrame() {
        this(null, buildDefaultDashboardService(), buildDefaultCarManagementController(), buildDefaultBranchManagementController(),
                buildDefaultCustomerManagementController(), buildDefaultStaffManagementController(),
                buildDefaultSaleOrderController(), buildDefaultPaymentController(), new InstallmentController(new InstallmentServiceImpl(new InstallmentPlanDaoImpl(), null)), new InvoiceController(new InvoiceServiceImpl(new InvoiceDaoImpl())), new PromotionController(new PromotionServiceImpl(new PromotionDaoImpl())), () -> {});
    }

    public AdminDashboardFrame(Runnable onLogoutRequested) {
        this(null, buildDefaultDashboardService(), buildDefaultCarManagementController(), buildDefaultBranchManagementController(),
                buildDefaultCustomerManagementController(), buildDefaultStaffManagementController(),
                buildDefaultSaleOrderController(), buildDefaultPaymentController(), new InstallmentController(new InstallmentServiceImpl(new InstallmentPlanDaoImpl(), null)), new InvoiceController(new InvoiceServiceImpl(new InvoiceDaoImpl())), new PromotionController(new PromotionServiceImpl(new PromotionDaoImpl())), onLogoutRequested);
    }

    public AdminDashboardFrame(AuthenticatedUser currentUser, Runnable onLogoutRequested) {
        this(currentUser, buildDefaultDashboardService(), buildDefaultCarManagementController(), buildDefaultBranchManagementController(),
                buildDefaultCustomerManagementController(), buildDefaultStaffManagementController(),
                buildDefaultSaleOrderController(), buildDefaultPaymentController(), new InstallmentController(new InstallmentServiceImpl(new InstallmentPlanDaoImpl(), null)), new InvoiceController(new InvoiceServiceImpl(new InvoiceDaoImpl())), new PromotionController(new PromotionServiceImpl(new PromotionDaoImpl())), onLogoutRequested);
    }

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
                               Runnable onLogoutRequested) {
        this.onLogoutRequested = Objects.requireNonNull(onLogoutRequested, "onLogoutRequested is required");
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

        contentCards.add(createDashboardPanel(), CARD_DASHBOARD);
        contentCards.add(createCarsPanel(), CARD_CARS);
        contentCards.add(createBranchesPanel(), CARD_BRANCHES);
        contentCards.add(createCustomersPanel(), CARD_CUSTOMERS);
        contentCards.add(createStaffPanel(), CARD_STAFF);
        contentCards.add(createOrdersPanel(), CARD_ORDERS);
        contentCards.add(createPaymentsPanel(), CARD_PAYMENTS);
        contentCards.add(new InstallmentPanel(installmentController, saleOrderController), CARD_INSTALLMENTS);
        contentCards.add(new InvoicePanel(invoiceController), CARD_INVOICES);
        contentCards.add(new PromotionPanel(promotionController), CARD_PROMOTIONS);
        contentCards.add(createTestDrivesPanel(), CARD_TESTDRIVES);
        contentCards.add(createWarrantiesPanel(), CARD_WARRANTIES);

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
                        && !CARD_TESTDRIVES.equals(item.key())
                        && !CARD_WARRANTIES.equals(item.key()))
                .forEach(item -> contentCards.add(
                        createModulePlaceholderPanel(item.label(), item.description()),
                        item.key()
                ));

        contentCardLayout.show(contentCards, CARD_DASHBOARD);
        return contentCards;
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

        JPanel headerCard = createWhiteCard();
        headerCard.setLayout(new BorderLayout());

        JLabel title = new JLabel("Quản lý xe");
        title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 22));
        title.setForeground(UiPalette.TEXT_PRIMARY);

        headerCard.add(title, BorderLayout.CENTER);

        wrapper.add(headerCard, BorderLayout.NORTH);
        wrapper.add(new CarManagementPanel(carManagementController, true), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createBranchesPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);

        JPanel headerCard = createWhiteCard();
        headerCard.setLayout(new BorderLayout());

        JLabel title = new JLabel("Quản lý Chi nhánh");
        title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 22));
        title.setForeground(UiPalette.TEXT_PRIMARY);

        headerCard.add(title, BorderLayout.CENTER);

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

        JPanel headerCard = createWhiteCard();
        headerCard.setLayout(new BorderLayout());
        JLabel title = new JLabel("Quản lý Khách hàng");
        title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 22));
        title.setForeground(UiPalette.TEXT_PRIMARY);
        headerCard.add(title, BorderLayout.CENTER);

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

        JPanel headerCard = createWhiteCard();
        headerCard.setLayout(new BorderLayout());
        JLabel title = new JLabel("Quản lý Nhân viên & Tài khoản");
        title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 22));
        title.setForeground(UiPalette.TEXT_PRIMARY);
        headerCard.add(title, BorderLayout.CENTER);

        wrapper.add(headerCard, BorderLayout.NORTH);
        wrapper.add(new StaffManagementPanel(staffManagementController), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createOrdersPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);

        JPanel headerCard = createWhiteCard();
        headerCard.setLayout(new BorderLayout());
        JLabel title = new JLabel("Phân hệ Bán hàng (Đơn bán)");
        title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 22));
        title.setForeground(UiPalette.TEXT_PRIMARY);
        headerCard.add(title, BorderLayout.CENTER);

        wrapper.add(headerCard, BorderLayout.NORTH);
        wrapper.add(new SaleOrderPanel(saleOrderController), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createPaymentsPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);

        JPanel headerCard = createWhiteCard();
        headerCard.setLayout(new BorderLayout());
        JLabel title = new JLabel("Lịch sử & Ghi nhận Thanh toán");
        title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 22));
        title.setForeground(UiPalette.TEXT_PRIMARY);
        headerCard.add(title, BorderLayout.CENTER);

        wrapper.add(headerCard, BorderLayout.NORTH);
        wrapper.add(new PaymentPanel(saleOrderController, paymentController), BorderLayout.CENTER);
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

    private void switchContent(String cardKey) {
        contentCardLayout.show(contentCards, cardKey);
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

        JPanel headerCard = createWhiteCard();
        headerCard.setLayout(new BorderLayout());

        JLabel title = new JLabel("Quản Lý Phân Năng Lái Thử");
        title.setForeground(UiPalette.TEXT_PRIMARY);
        title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 22));

        headerCard.add(title, BorderLayout.CENTER);

        // Khởi tạo các Service
        vn.edu.ute.carsalesms.dao.TestDriveDao tDao = new vn.edu.ute.carsalesms.dao.impl.TestDriveDaoImpl();
        vn.edu.ute.carsalesms.dao.CustomerDao cDao = new vn.edu.ute.carsalesms.dao.impl.CustomerDaoImpl();
        vn.edu.ute.carsalesms.dao.CarDao carDao = new vn.edu.ute.carsalesms.dao.impl.CarDaoImpl();
        vn.edu.ute.carsalesms.dao.StaffDao sDao = new vn.edu.ute.carsalesms.dao.impl.StaffDaoImpl();
        vn.edu.ute.carsalesms.service.TestDriveService tService = new vn.edu.ute.carsalesms.service.impl.TestDriveServiceImpl(tDao, cDao, carDao, sDao);

        wrapper.add(headerCard, BorderLayout.NORTH);
        wrapper.add(new vn.edu.ute.carsalesms.view.component.TestDrivePanel(tService, cDao, carDao, sDao), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createWarrantiesPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);

        JPanel headerCard = createWhiteCard();
        headerCard.setLayout(new BorderLayout());

        JLabel title = new JLabel("Hồ Sơ Bảo Hành Gara Toàn Cầu");
        title.setForeground(UiPalette.TEXT_PRIMARY);
        title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 22));

        headerCard.add(title, BorderLayout.CENTER);

        vn.edu.ute.carsalesms.dao.WarrantyDao wDao = new vn.edu.ute.carsalesms.dao.impl.WarrantyDaoImpl();
        vn.edu.ute.carsalesms.dao.SaleOrderDao oDao = new vn.edu.ute.carsalesms.dao.impl.SaleOrderDaoImpl();
        vn.edu.ute.carsalesms.service.WarrantyService wService = new vn.edu.ute.carsalesms.service.impl.WarrantyServiceImpl(wDao, oDao);

        wrapper.add(headerCard, BorderLayout.NORTH);
        wrapper.add(new vn.edu.ute.carsalesms.view.component.WarrantyPanel(wService), BorderLayout.CENTER);
        return wrapper;
    }
}
