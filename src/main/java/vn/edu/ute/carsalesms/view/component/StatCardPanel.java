package vn.edu.ute.carsalesms.view.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridLayout; // Import GridLayout
import vn.edu.ute.carsalesms.view.theme.UiPalette;
import vn.edu.ute.carsalesms.view.theme.UiSizing;

/**
 * Lớp StatCardPanel đại diện cho một thẻ số liệu tổng quan, thường được sử dụng trên dashboard.
 * Mỗi thẻ hiển thị một tiêu đề và một giá trị, với một thanh màu nhấn (accent bar) ở bên trái
 * để tạo điểm nhấn trực quan.
 */
public class StatCardPanel extends JPanel {

    /**
     * Constructor khởi tạo một StatCardPanel.
     *
     * @param title Tiêu đề của thẻ số liệu (ví dụ: "Tổng doanh thu").
     * @param value Giá trị số liệu (ví dụ: "100,000,000 VNĐ").
     * @param accentColor Màu sắc của thanh nhấn ở bên trái thẻ.
     */
    public StatCardPanel(String title, String value, Color accentColor) {
        // Đặt layout cho panel là BorderLayout để dễ dàng sắp xếp các thành phần.
        setLayout(new BorderLayout(0, 0));
        // Đặt panel là trong suốt (opaque) để màu nền có thể hiển thị.
        setOpaque(true);
        // Đặt màu nền cho thẻ là màu nền bề mặt từ bảng màu UI.
        setBackground(UiPalette.SURFACE_BACKGROUND);
        // Đặt kích thước ưu tiên cho thẻ theo cấu hình kích thước UI.
        setPreferredSize(UiSizing.STAT_CARD_SIZE);

        // ── Thanh nhấn (Accent bar - phía bên trái, rộng 3px) ──
        JPanel accentBar = new JPanel();
        // Đặt màu nền cho thanh nhấn.
        accentBar.setBackground(accentColor);
        // Đặt kích thước ưu tiên cho thanh nhấn (rộng 3px, chiều cao tự động).
        accentBar.setPreferredSize(new Dimension(3, 0));
        // Thêm thanh nhấn vào phía Tây (trái) của panel.
        add(accentBar, BorderLayout.WEST);

        // ── Nội dung chính của thẻ ──
        // Tạo một JPanel để chứa tiêu đề và giá trị, sử dụng GridLayout để sắp xếp 2 hàng 1 cột.
        JPanel content = new JPanel(new GridLayout(2, 1, 0, 0));
        // Đặt panel nội dung là trong suốt.
        content.setOpaque(false);
        // Đặt đường viền rỗng để tạo khoảng cách bên trong.
        content.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        // Tạo JLabel cho tiêu đề.
        JLabel titleLabel = new JLabel(title);
        // Đặt màu chữ cho tiêu đề.
        titleLabel.setForeground(UiPalette.TEXT_SECONDARY);
        // Đặt font cho tiêu đề.
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Tạo JLabel cho giá trị.
        JLabel valueLabel = new JLabel(value);
        // Đặt màu chữ cho giá trị.
        valueLabel.setForeground(UiPalette.TEXT_PRIMARY);
        // Đặt font cho giá trị (in đậm hơn và lớn hơn).
        valueLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 22));

        // Thêm tiêu đề và giá trị vào panel nội dung.
        content.add(titleLabel);
        content.add(valueLabel);
        // Thêm panel nội dung vào giữa của thẻ.
        add(content, BorderLayout.CENTER);

        // Đặt đường viền cho toàn bộ thẻ.
        setBorder(BorderFactory.createLineBorder(UiPalette.BORDER_SOFT));
    }
}
