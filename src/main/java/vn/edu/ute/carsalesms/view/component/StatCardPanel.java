package vn.edu.ute.carsalesms.view.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import vn.edu.ute.carsalesms.view.theme.AdminUiPalette;
import vn.edu.ute.carsalesms.view.theme.AdminUiSizing;

/**
 * Thẻ số liệu tổng quan cho dashboard.
 * Accent bar bên trái, nền trắng sạch.
 */
public class StatCardPanel extends JPanel {

    public StatCardPanel(String title, String value, Color accentColor) {
        setLayout(new BorderLayout(0, 0));
        setOpaque(true);
        setBackground(AdminUiPalette.SURFACE_BACKGROUND);
        setPreferredSize(AdminUiSizing.STAT_CARD_SIZE);

        // ── Accent bar (left side, 3px) ──
        JPanel accentBar = new JPanel();
        accentBar.setBackground(accentColor);
        accentBar.setPreferredSize(new Dimension(3, 0));
        add(accentBar, BorderLayout.WEST);

        // ── Content ──
        JPanel content = new JPanel(new GridLayout(2, 1, 0, 0));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(AdminUiPalette.TEXT_SECONDARY);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setForeground(AdminUiPalette.TEXT_PRIMARY);
        valueLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 22));

        content.add(titleLabel);
        content.add(valueLabel);
        add(content, BorderLayout.CENTER);

        setBorder(BorderFactory.createLineBorder(AdminUiPalette.BORDER_SOFT));
    }
}
