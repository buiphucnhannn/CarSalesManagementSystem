package vn.edu.ute.carsalesms.view.staff;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import vn.edu.ute.carsalesms.view.component.SidebarMenuPanel;
import vn.edu.ute.carsalesms.view.component.StatCardPanel;
import vn.edu.ute.carsalesms.view.theme.AdminUiPalette;
import vn.edu.ute.carsalesms.view.theme.AdminUiSizing;

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

    public StaffDashboardFrame() {
        setTitle("Car Sales Management - Staff Dashboard");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(AdminUiSizing.WINDOW_MIN_SIZE);
        setSize(AdminUiSizing.WINDOW_INITIAL_SIZE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AdminUiPalette.APP_BACKGROUND);

        SidebarMenuPanel sidebar = new SidebarMenuPanel(
                "Sales Staff", "STAFF",
                STAFF_SIDEBAR_ITEMS, CARD_OVERVIEW,
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

        contentCards.add(createOverviewPanel(), CARD_OVERVIEW);
        MODULE_ITEMS.stream()
                .filter(item -> item.description() != null)
                .forEach(item -> contentCards.add(
                        createPlaceholderPanel(item.title(), item.description()),
                        item.key()
                ));

        contentCardLayout.show(contentCards, CARD_OVERVIEW);
        return contentCards;
    }


    private JScrollPane createOverviewPanel() {
        JPanel overview = new JPanel(new BorderLayout(0, 8));
        overview.setOpaque(true);
        overview.setBackground(AdminUiPalette.APP_BACKGROUND);

        JPanel statGrid = new JPanel(new GridLayout(1, 4, 8, 0));
        statGrid.setOpaque(false);
        statGrid.add(new StatCardPanel("Đơn cần xử lý", "07", AdminUiPalette.WARNING));
        statGrid.add(new StatCardPanel("Doanh thu hôm nay", "165 triệu", AdminUiPalette.KPI_BLUE));
        statGrid.add(new StatCardPanel("Lịch lái thử", "04", AdminUiPalette.SECONDARY));
        statGrid.add(new StatCardPanel("Bảo hành mở", "02", AdminUiPalette.DANGER));

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
        scrollPane.getViewport().setBackground(AdminUiPalette.APP_BACKGROUND);
        return scrollPane;
    }

    private JPanel createTaskQueuePanel() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(0, 8));

        JLabel title = new JLabel("Danh sách công việc hôm nay");
        title.setForeground(AdminUiPalette.TEXT_PRIMARY);
        title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));

        String[] columns = {"Tác vụ", "Khách", "Hạn", "Trạng thái"};
        Object[][] data = {
                {"Lập đơn bán", "Trần Minh K", "10:30", "Đang xử lý"},
                {"Thu đợt 2", "Lê Thu H", "11:15", "Cần thu"},
                {"Hẹn lái thử", "Phạm Gia B", "14:00", "Sắp đến"},
                {"Bảo hành", "Nguyễn Văn A", "15:30", "Mới tạo"}
        };

        JTable table = new JTable(new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setBackground(AdminUiPalette.TABLE_BACKGROUND);
        table.setGridColor(AdminUiPalette.BORDER_SOFT);
        table.setSelectionBackground(AdminUiPalette.PRIMARY_SOFT);
        table.setSelectionForeground(AdminUiPalette.TEXT_PRIMARY);
        table.getTableHeader().setBackground(AdminUiPalette.PRIMARY_SOFT);
        table.getTableHeader().setForeground(AdminUiPalette.TEXT_PRIMARY);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(AdminUiPalette.BORDER_SOFT));

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
        header.setForeground(AdminUiPalette.TEXT_PRIMARY);
        inner.add(header);

        inner.add(createTimelineItem("09:00 - Tư vấn KH", AdminUiPalette.KPI_BLUE));
        inner.add(createTimelineItem("11:00 - Chốt đơn", AdminUiPalette.SUCCESS));
        inner.add(createTimelineItem("14:00 - Lái thử CX-5", AdminUiPalette.WARNING));
        inner.add(createTimelineItem("16:30 - Thanh toán", AdminUiPalette.DANGER));

        card.add(inner, BorderLayout.NORTH);
        return card;
    }

    private JLabel createTimelineItem(String text, java.awt.Color barColor) {
        JLabel label = new JLabel(text);
        label.setOpaque(true);
        label.setBackground(AdminUiPalette.SURFACE_ELEVATED);
        label.setForeground(AdminUiPalette.TEXT_PRIMARY);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, barColor),
                BorderFactory.createEmptyBorder(5, 6, 5, 6)
        ));
        return label;
    }

    private JPanel createPlaceholderPanel(String titleText, String descriptionText) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        JPanel card = createCard();
        card.setLayout(new GridLayout(2, 1));

        JLabel title = new JLabel(titleText);
        title.setForeground(AdminUiPalette.TEXT_PRIMARY);
        title.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 20));

        JLabel desc = new JLabel(descriptionText);
        desc.setForeground(AdminUiPalette.TEXT_SECONDARY);
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        card.add(title);
        card.add(desc);
        wrapper.add(card, BorderLayout.NORTH);
        return wrapper;
    }

    private JPanel createCard() {
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
