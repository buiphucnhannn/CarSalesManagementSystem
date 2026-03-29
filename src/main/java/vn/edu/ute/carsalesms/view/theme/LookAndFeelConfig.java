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
        UIManager.put("Button.background", AdminUiPalette.PRIMARY);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.hoverBackground", AdminUiPalette.PRIMARY_HOVER);
        UIManager.put("Button.pressedBackground", AdminUiPalette.PRIMARY_HOVER);
        UIManager.put("Button.default.hoverBackground", AdminUiPalette.SECONDARY);

        UIManager.put("Panel.background", AdminUiPalette.APP_BACKGROUND);
        UIManager.put("TextField.background", AdminUiPalette.SURFACE_BACKGROUND);
        UIManager.put("TextField.focusedBackground", AdminUiPalette.SURFACE_BACKGROUND);

        UIManager.put("Table.background", AdminUiPalette.TABLE_BACKGROUND);
        UIManager.put("Table.alternateRowColor", AdminUiPalette.SURFACE_ELEVATED);
        UIManager.put("TableHeader.background", AdminUiPalette.PRIMARY_SOFT);
        UIManager.put("TableHeader.foreground", AdminUiPalette.TEXT_PRIMARY);
        UIManager.put("Table.gridColor", AdminUiPalette.BORDER_LIGHTER);
        UIManager.put("Table.selectionBackground", AdminUiPalette.PRIMARY_SOFT);
        UIManager.put("Table.selectionForeground", AdminUiPalette.TEXT_PRIMARY);

        UIManager.put("ProgressBar.foreground", AdminUiPalette.PRIMARY);
        UIManager.put("Component.focusColor", AdminUiPalette.PRIMARY_BORDER);
        UIManager.put("Component.borderColor", AdminUiPalette.BORDER_SOFT);
        UIManager.put("Component.focusedBorderColor", AdminUiPalette.PRIMARY);

        UIManager.put("ScrollPane.border", null);
    }
}
