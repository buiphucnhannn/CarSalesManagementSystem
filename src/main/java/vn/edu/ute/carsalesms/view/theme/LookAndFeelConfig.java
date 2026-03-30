package vn.edu.ute.carsalesms.view.theme;

import java.awt.Color;
import java.awt.Font;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;

/**
 * Cấu hình FlatLaf sáng, hiện đại, tone xanh dương premium.
 */
public final class LookAndFeelConfig {

    private LookAndFeelConfig() {
    }

    public static void apply() {
        try {
            Class<?> lafClass = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            LookAndFeel flatLaf = (LookAndFeel) lafClass.getDeclaredConstructor().newInstance();
            UIManager.setLookAndFeel(flatLaf);
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
        }

        // ── Shape ───────────────────────────────────────────
        UIManager.put("Component.arc", 14);
        UIManager.put("Button.arc", 14);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 10);

        // ── Font ────────────────────────────────────────────
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("Button.font", new Font("Segoe UI Semibold", Font.PLAIN, 14));
        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("TableHeader.font", new Font("Segoe UI Semibold", Font.PLAIN, 13));

        // ── Colors ──────────────────────────────────────────
        UIManager.put("Button.background", UiPalette.PRIMARY);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.hoverBackground", UiPalette.PRIMARY_HOVER);
        UIManager.put("Button.pressedBackground", UiPalette.PRIMARY_HOVER);
        UIManager.put("Button.default.hoverBackground", UiPalette.SECONDARY);

        UIManager.put("Panel.background", UiPalette.APP_BACKGROUND);
        UIManager.put("TextField.background", UiPalette.SURFACE_BACKGROUND);
        UIManager.put("TextField.focusedBackground", UiPalette.SURFACE_BACKGROUND);

        UIManager.put("Table.background", UiPalette.TABLE_BACKGROUND);
        UIManager.put("Table.alternateRowColor", UiPalette.SURFACE_ELEVATED);
        UIManager.put("TableHeader.background", UiPalette.PRIMARY_SOFT);
        UIManager.put("TableHeader.foreground", UiPalette.TEXT_PRIMARY);
        UIManager.put("Table.gridColor", UiPalette.BORDER_LIGHTER);
        UIManager.put("Table.selectionBackground", UiPalette.PRIMARY_SOFT);
        UIManager.put("Table.selectionForeground", UiPalette.TEXT_PRIMARY);

        UIManager.put("ProgressBar.foreground", UiPalette.PRIMARY);
        UIManager.put("Component.focusColor", UiPalette.PRIMARY_BORDER);
        UIManager.put("Component.borderColor", UiPalette.BORDER_SOFT);
        UIManager.put("Component.focusedBorderColor", UiPalette.PRIMARY);

        UIManager.put("ScrollPane.border", null);
    }
}
