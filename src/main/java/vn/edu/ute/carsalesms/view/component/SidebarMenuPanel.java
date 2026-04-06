package vn.edu.ute.carsalesms.view.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import vn.edu.ute.carsalesms.view.theme.UiPalette;
import vn.edu.ute.carsalesms.view.theme.UiSizing;

/**
 * Lớp SidebarMenuPanel tạo ra một thanh menu bên (sidebar) ở phía trái của ứng dụng.
 * Thanh menu này có màu tối, hiển thị thông tin người dùng (avatar, tên, vai trò),
 * danh sách các mục menu có thể cuộn, và một nút đăng xuất.
 * Nó cung cấp hiệu ứng khi di chuột qua và làm nổi bật mục menu đang được chọn.
 */
public class SidebarMenuPanel extends JPanel {

    /**
     * Record để định nghĩa một mục trong menu, bao gồm một khóa (key) duy nhất và nhãn (label) hiển thị.
     */
    public record MenuItem(String key, String label) {
    }

    // Map để lưu trữ các hàng menu (JPanel) với key tương ứng để dễ dàng truy cập và thay đổi trạng thái.
    private final Map<String, JPanel> menuRows = new LinkedHashMap<>();
    // Key của mục menu đang được chọn.
    private String currentKey;

    /**
     * Constructor của SidebarMenuPanel.
     *
     * @param title           Tên người dùng hoặc tiêu đề chính, hiển thị ở phần header.
     * @param role            Vai trò của người dùng, hiển thị bên dưới tiêu đề.
     * @param menuItems       Danh sách các mục menu (MenuItem) để hiển thị.
     * @param defaultKey      Khóa của mục menu được chọn mặc định khi khởi tạo.
     * @param onMenuSelected  Một Consumer sẽ được gọi khi một mục menu được chọn, truyền vào key của mục đó.
     * @param onLogoutRequested Một Runnable sẽ được gọi khi nút đăng xuất được nhấn.
     */
    public SidebarMenuPanel(String title, String role, List<MenuItem> menuItems,
                            String defaultKey, Consumer<String> onMenuSelected,
                            Runnable onLogoutRequested) {
        // Kiểm tra các tham số đầu vào không được null hoặc rỗng.
        Objects.requireNonNull(title, "title is required");
        Objects.requireNonNull(role, "role is required");
        Objects.requireNonNull(defaultKey, "defaultKey is required");
        Objects.requireNonNull(onMenuSelected, "onMenuSelected is required");
        Objects.requireNonNull(onLogoutRequested, "onLogoutRequested is required");
        Objects.requireNonNull(menuItems, "menuItems is required");
        if (menuItems.isEmpty()) {
            throw new IllegalArgumentException("menuItems must not be empty");
        }

        // Cấu hình giao diện cơ bản cho sidebar.
        setBackground(UiPalette.SIDEBAR_BACKGROUND);
        setPreferredSize(UiSizing.SIDEBAR_SIZE);
        setLayout(new BorderLayout());

        // --- Phần Header với nền gradient, avatar và thông tin người dùng ---
        JPanel header = new JPanel() {
            // Ghi đè paintComponent để vẽ nền gradient.
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UiPalette.GRADIENT_START,
                        getWidth(), getHeight(), UiPalette.GRADIENT_END);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setLayout(new BorderLayout(8, 0));
        header.setBorder(BorderFactory.createEmptyBorder(14, 12, 14, 12));

        // Avatar hình tròn hiển thị chữ cái đầu của tên.
        JLabel avatar = new JLabel(resolveAvatarText(title), SwingConstants.CENTER) {
            // Ghi đè paintComponent để vẽ hình tròn làm nền cho avatar.
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 40)); // Màu trắng mờ.
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setPreferredSize(new Dimension(36, 36));
        avatar.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
        avatar.setForeground(Color.WHITE);

        // Khối chứa tên và vai trò.
        JPanel titleBlock = new JPanel(new GridLayout(2, 1));
        titleBlock.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        titleLabel.setForeground(Color.WHITE);
        JLabel roleLabel = new JLabel(role.isEmpty() ? "Menu" : role);
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        roleLabel.setForeground(new Color(255, 255, 255, 180)); // Màu trắng hơi mờ.
        titleBlock.add(titleLabel);
        titleBlock.add(roleLabel);

        header.add(avatar, BorderLayout.WEST);
        header.add(titleBlock, BorderLayout.CENTER);

        // --- Phần các mục menu (có thể cuộn) ---
        JPanel menuContainer = new JPanel();
        menuContainer.setLayout(new BoxLayout(menuContainer, BoxLayout.Y_AXIS));
        menuContainer.setBackground(UiPalette.SIDEBAR_BACKGROUND);
        menuContainer.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        // Tạo và thêm từng hàng menu vào container.
        for (MenuItem item : menuItems) {
            JPanel row = createMenuRow(item.label(), item.key(), onMenuSelected);
            menuRows.put(item.key(), row);
            menuContainer.add(row);
            menuContainer.add(Box.createVerticalStrut(1)); // Thêm khoảng cách nhỏ giữa các mục.
        }

        // Đặt container menu vào một JScrollPane để cho phép cuộn khi có nhiều mục.
        JScrollPane menuScroll = new JScrollPane(menuContainer);
        menuScroll.setBorder(BorderFactory.createEmptyBorder());
        menuScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        menuScroll.getVerticalScrollBar().setUnitIncrement(12);
        menuScroll.setOpaque(false);
        menuScroll.getViewport().setOpaque(false);
        menuScroll.getViewport().setBackground(UiPalette.SIDEBAR_BACKGROUND);

        // --- Phần Footer chứa nút Đăng xuất ---
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(UiPalette.SIDEBAR_BACKGROUND);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UiPalette.SIDEBAR_SEPARATOR), // Đường kẻ phân cách.
                BorderFactory.createEmptyBorder(6, 8, 8, 8)
        ));

        JButton logoutButton = new JButton("Đăng xuất");
        logoutButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        logoutButton.setForeground(UiPalette.DANGER); // Màu chữ cảnh báo.
        logoutButton.setBackground(UiPalette.SIDEBAR_BACKGROUND);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setHorizontalAlignment(SwingConstants.CENTER);
        logoutButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Thêm hiệu ứng hover cho nút đăng xuất.
        logoutButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                logoutButton.setBackground(UiPalette.SIDEBAR_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                logoutButton.setBackground(UiPalette.SIDEBAR_BACKGROUND);
            }
        });
        logoutButton.addActionListener(e -> onLogoutRequested.run());
        footer.add(logoutButton, BorderLayout.CENTER);

        // Thêm các phần header, menu và footer vào sidebar.
        add(header, BorderLayout.NORTH);
        add(menuScroll, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        // Thiết lập mục menu được chọn mặc định.
        currentKey = menuRows.containsKey(defaultKey) ? defaultKey : menuItems.getFirst().key();
        setSelected(currentKey);
    }

    /**
     * Tạo một hàng (JPanel) cho một mục menu.
     * @param label Nhãn hiển thị của mục menu.
     * @param key Khóa định danh của mục menu.
     * @param onMenuSelected Callback được gọi khi mục menu này được click.
     * @return một JPanel đại diện cho một hàng menu.
     */
    private JPanel createMenuRow(String label, String key, Consumer<String> onMenuSelected) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(UiPalette.SIDEBAR_BACKGROUND);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        row.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel textLabel = new JLabel(label);
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        textLabel.setForeground(UiPalette.SIDEBAR_TEXT);
        row.add(textLabel, BorderLayout.CENTER);

        // Thêm listener để xử lý hiệu ứng hover và sự kiện click.
        row.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // Nếu không phải là mục đang được chọn, thay đổi màu nền và màu chữ.
                if (!key.equals(currentKey)) {
                    row.setBackground(UiPalette.SIDEBAR_HOVER);
                    textLabel.setForeground(UiPalette.SIDEBAR_TEXT_ACTIVE);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Nếu không phải là mục đang được chọn, trả lại màu mặc định.
                if (!key.equals(currentKey)) {
                    row.setBackground(UiPalette.SIDEBAR_BACKGROUND);
                    textLabel.setForeground(UiPalette.SIDEBAR_TEXT);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // Khi click, cập nhật mục được chọn và gọi callback.
                setSelected(key);
                onMenuSelected.accept(key);
            }
        });

        return row;
    }

    /**
     * Thiết lập mục menu được chọn.
     * Phương thức này cập nhật giao diện của tất cả các mục menu để làm nổi bật mục được chọn.
     * @param selectedKey Khóa của mục menu cần được chọn.
     */
    public void setSelected(String selectedKey) {
        currentKey = selectedKey;
        // Duyệt qua tất cả các hàng menu.
        menuRows.forEach((key, row) -> {
            boolean selected = key.equals(selectedKey);
            // Thay đổi màu nền.
            row.setBackground(selected ? UiPalette.SIDEBAR_ACTIVE : UiPalette.SIDEBAR_BACKGROUND);
            // Thay đổi màu chữ và kiểu chữ (in đậm).
            if (row.getComponentCount() > 0 && row.getComponent(0) instanceof JLabel label) {
                label.setForeground(selected ? UiPalette.SIDEBAR_TEXT_ACTIVE : UiPalette.SIDEBAR_TEXT);
                label.setFont(new Font("Segoe UI" + (selected ? " Semibold" : ""), Font.PLAIN, 13));
            }
        });
    }

    /**
     * Lấy chữ cái đầu tiên của tên để làm avatar.
     * @param title Tên đầy đủ.
     * @return Chữ cái đầu tiên viết hoa.
     */
    private String resolveAvatarText(String title) {
        return Arrays.stream(title.trim().split("\\s+"))
                .filter(part -> !part.isBlank())
                .map(part -> part.substring(0, 1).toUpperCase())
                .findFirst()
                .orElse("U"); // Giá trị mặc định nếu không tìm thấy.
    }

}
