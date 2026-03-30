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
 * Sidebar trái dark chứa các module, hover effect, avatar/role header, nút Đăng xuất.
 */
public class SidebarMenuPanel extends JPanel {

    public record MenuItem(String key, String label) {
    }

    private final Map<String, JPanel> menuRows = new LinkedHashMap<>();
    private String currentKey;

    public SidebarMenuPanel(String title, String role, List<MenuItem> menuItems,
                            String defaultKey, Consumer<String> onMenuSelected,
                            Runnable onLogoutRequested) {
        Objects.requireNonNull(title, "title is required");
        Objects.requireNonNull(role, "role is required");
        Objects.requireNonNull(defaultKey, "defaultKey is required");
        Objects.requireNonNull(onMenuSelected, "onMenuSelected is required");
        Objects.requireNonNull(onLogoutRequested, "onLogoutRequested is required");
        Objects.requireNonNull(menuItems, "menuItems is required");
        if (menuItems.isEmpty()) {
            throw new IllegalArgumentException("menuItems must not be empty");
        }

        setBackground(UiPalette.SIDEBAR_BACKGROUND);
        setPreferredSize(UiSizing.SIDEBAR_SIZE);
        setLayout(new BorderLayout());

        // ── Gradient header with avatar + role ──
        JPanel header = new JPanel() {
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

        // Avatar circle
        JLabel avatar = new JLabel(resolveAvatarText(title), SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setPreferredSize(new Dimension(36, 36));
        avatar.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
        avatar.setForeground(Color.WHITE);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1));
        titleBlock.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        titleLabel.setForeground(Color.WHITE);
        JLabel roleLabel = new JLabel(role.isEmpty() ? "Menu" : role);
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        roleLabel.setForeground(new Color(255, 255, 255, 180));
        titleBlock.add(titleLabel);
        titleBlock.add(roleLabel);

        header.add(avatar, BorderLayout.WEST);
        header.add(titleBlock, BorderLayout.CENTER);

        // ── Menu items (scrollable) ──
        JPanel menuContainer = new JPanel();
        menuContainer.setLayout(new BoxLayout(menuContainer, BoxLayout.Y_AXIS));
        menuContainer.setBackground(UiPalette.SIDEBAR_BACKGROUND);
        menuContainer.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        for (MenuItem item : menuItems) {
            JPanel row = createMenuRow(item.label(), item.key(), onMenuSelected);
            menuRows.put(item.key(), row);
            menuContainer.add(row);
            menuContainer.add(Box.createVerticalStrut(1));
        }

        JScrollPane menuScroll = new JScrollPane(menuContainer);
        menuScroll.setBorder(BorderFactory.createEmptyBorder());
        menuScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        menuScroll.getVerticalScrollBar().setUnitIncrement(12);
        menuScroll.setOpaque(false);
        menuScroll.getViewport().setOpaque(false);
        menuScroll.getViewport().setBackground(UiPalette.SIDEBAR_BACKGROUND);

        // ── Logout button ──
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(UiPalette.SIDEBAR_BACKGROUND);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UiPalette.SIDEBAR_SEPARATOR),
                BorderFactory.createEmptyBorder(6, 8, 8, 8)
        ));

        JButton logoutButton = new JButton("Đăng xuất");
        logoutButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        logoutButton.setForeground(UiPalette.DANGER);
        logoutButton.setBackground(UiPalette.SIDEBAR_BACKGROUND);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setHorizontalAlignment(SwingConstants.CENTER);
        logoutButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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

        add(header, BorderLayout.NORTH);
        add(menuScroll, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        currentKey = menuRows.containsKey(defaultKey) ? defaultKey : menuItems.getFirst().key();
        setSelected(currentKey);
    }

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

        row.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!key.equals(currentKey)) {
                    row.setBackground(UiPalette.SIDEBAR_HOVER);
                    textLabel.setForeground(UiPalette.SIDEBAR_TEXT_ACTIVE);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!key.equals(currentKey)) {
                    row.setBackground(UiPalette.SIDEBAR_BACKGROUND);
                    textLabel.setForeground(UiPalette.SIDEBAR_TEXT);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                setSelected(key);
                onMenuSelected.accept(key);
            }
        });

        return row;
    }

    public void setSelected(String selectedKey) {
        currentKey = selectedKey;
        menuRows.forEach((key, row) -> {
            boolean selected = key.equals(selectedKey);
            row.setBackground(selected ? UiPalette.SIDEBAR_ACTIVE : UiPalette.SIDEBAR_BACKGROUND);
            if (row.getComponentCount() > 0 && row.getComponent(0) instanceof JLabel label) {
                label.setForeground(selected ? UiPalette.SIDEBAR_TEXT_ACTIVE : UiPalette.SIDEBAR_TEXT);
                label.setFont(new Font("Segoe UI" + (selected ? " Semibold" : ""), Font.PLAIN, 13));
            }
        });
    }

    private String resolveAvatarText(String title) {
        return Arrays.stream(title.trim().split("\\s+"))
                .filter(part -> !part.isBlank())
                .map(part -> part.substring(0, 1).toUpperCase())
                .findFirst()
                .orElse("U");
    }

}
