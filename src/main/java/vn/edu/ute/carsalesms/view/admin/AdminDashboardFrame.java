package vn.edu.ute.carsalesms.view.admin;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import vn.edu.ute.carsalesms.view.component.SidebarMenuPanel;
import vn.edu.ute.carsalesms.view.component.StatCardPanel;
import vn.edu.ute.carsalesms.view.theme.AdminUiPalette;
import vn.edu.ute.carsalesms.view.theme.AdminUiSizing;

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

    public AdminDashboardFrame() {
        setTitle("Car Sales Management - Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(AdminUiSizing.WINDOW_MIN_SIZE);
        setSize(AdminUiSizing.WINDOW_INITIAL_SIZE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AdminUiPalette.APP_BACKGROUND);

        SidebarMenuPanel sidebar = new SidebarMenuPanel(
                "Car Sales Admin", "ADMIN",
                ADMIN_SIDEBAR_ITEMS, CARD_DASHBOARD,
                this::switchContent,
                () -> {
                }
        );
        root.add(sidebar, BorderLayout.WEST);

        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setOpaque(false);
        mainArea.setBorder(BorderFactory.createEmptyBorder(6, 10, 10, 10));
        mainArea.add(createContentCards(), BorderLayout.CENTER);

        root.add(mainArea, BorderLayout.CENTER);
        setContentPane(root);
    }
    private JPanel createContentCards() {
        contentCards.setOpaque(false);
        contentCards.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        contentCards.add(createDashboardPanel(), CARD_DASHBOARD);
        MODULE_ITEMS.stream()
                .filter(item -> item.description() != null)
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
        dashboard.setBackground(AdminUiPalette.APP_BACKGROUND);

        // Stat cards row - fill width equally
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 8, 0));
        statsGrid.setOpaque(false);
        statsGrid.add(new StatCardPanel("Doanh thu tháng", "4.8 tỷ", AdminUiPalette.KPI_BLUE));
        statsGrid.add(new StatCardPanel("Đơn bán hôm nay", "18", AdminUiPalette.KPI_GREEN));
        statsGrid.add(new StatCardPanel("Khách chờ tư vấn", "32", AdminUiPalette.KPI_AMBER));
        statsGrid.add(new StatCardPanel("Lịch lái thử", "11", AdminUiPalette.KPI_PURPLE));

        // Bảng đơn bán chiếm phần lớn, thao tác nhanh gọn bên phải
        JPanel bottomSection = new JPanel(new BorderLayout(8, 0));
        bottomSection.setOpaque(false);
        bottomSection.add(createRecentOrdersPanel(), BorderLayout.CENTER);

        JPanel rightPanel = createQuickActionsPanel();
        rightPanel.setPreferredSize(new Dimension(180, 0));
        bottomSection.add(rightPanel, BorderLayout.EAST);

        dashboard.add(statsGrid, BorderLayout.NORTH);
        dashboard.add(bottomSection, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(dashboard);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        return scrollPane;
    }

    private JPanel createRecentOrdersPanel() {
        JPanel container = createWhiteCard();
        container.setLayout(new BorderLayout(0, 8));

        JLabel title = new JLabel("Đơn bán gần nhất");
        title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        title.setForeground(AdminUiPalette.TEXT_PRIMARY);

        String[] columns = {"Mã đơn", "Khách hàng", "Mẫu xe", "Trạng thái"};
        Object[][] data = {
                {"SO-2401", "Nguyễn Văn A", "Toyota Vios", "Đặt cọc"},
                {"SO-2402", "Trần Minh K", "Mazda CX-5", "Đã thanh toán"},
                {"SO-2403", "Lê Thu H", "Kia Seltos", "Chờ duyệt"},
                {"SO-2404", "Phạm Gia B", "Hyundai Accent", "Đang xử lý"}
        };

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
        table.setGridColor(AdminUiPalette.BORDER_SOFT);
        table.setBackground(AdminUiPalette.TABLE_BACKGROUND);
        table.setSelectionBackground(AdminUiPalette.PRIMARY_SOFT);
        table.setSelectionForeground(AdminUiPalette.TEXT_PRIMARY);
        table.getTableHeader().setBackground(AdminUiPalette.PRIMARY_SOFT);
        table.getTableHeader().setForeground(AdminUiPalette.TEXT_PRIMARY);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(AdminUiPalette.BORDER_SOFT));
        scrollPane.getViewport().setBackground(AdminUiPalette.TABLE_BACKGROUND);

        container.add(title, BorderLayout.NORTH);
        container.add(scrollPane, BorderLayout.CENTER);
        return container;
    }

    private JPanel createQuickActionsPanel() {
        JPanel container = createWhiteCard();
        container.setLayout(new BorderLayout(0, 8));

        JLabel title = new JLabel("Thao tác nhanh");
        title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        title.setForeground(AdminUiPalette.TEXT_PRIMARY);

        JPanel actions = new JPanel(new GridLayout(4, 1, 0, 5));
        actions.setOpaque(false);
        actions.add(createActionButton("Thêm khách hàng"));
        actions.add(createActionButton("Tạo lịch lái thử"));
        actions.add(createActionButton("Cập nhật KM"));
        actions.add(createActionButton("Xuất báo cáo"));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(title, BorderLayout.NORTH);
        top.add(actions, BorderLayout.CENTER);

        container.add(top, BorderLayout.NORTH);
        return container;
    }

    private JButton createActionButton(String title) {
        JButton button = new JButton(title);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AdminUiPalette.PRIMARY_BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 8)
        ));
        button.setBackground(AdminUiPalette.ACTION_BG);
        button.setForeground(AdminUiPalette.ACTION_FG);
        return button;
    }

    private JPanel createModulePlaceholderPanel(String moduleTitle, String description) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        JPanel card = createWhiteCard();
        card.setLayout(new GridLayout(2, 1));

        JLabel title = new JLabel(moduleTitle);
        title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 20));
        title.setForeground(AdminUiPalette.TEXT_PRIMARY);

        JLabel desc = new JLabel(description);
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        desc.setForeground(AdminUiPalette.TEXT_SECONDARY);

        card.add(title);
        card.add(desc);
        wrapper.add(card, BorderLayout.NORTH);
        return wrapper;
    }

    private JPanel createWhiteCard() {
        JPanel panel = new JPanel();
        panel.setOpaque(true);
        panel.setBackground(AdminUiPalette.SURFACE_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AdminUiPalette.BORDER_SOFT),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        return panel;
    }

    private void switchContent(String cardKey) {
        contentCardLayout.show(contentCards, cardKey);
    }
}
