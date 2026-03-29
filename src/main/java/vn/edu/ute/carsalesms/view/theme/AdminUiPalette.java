package vn.edu.ute.carsalesms.view.theme;

import java.awt.Color;

/**
 * Bảng màu premium blue dùng chung cho toàn bộ giao diện admin/staff.
 */
public final class AdminUiPalette {

    // ── Background ──────────────────────────────────────────────
    public static final Color APP_BACKGROUND       = new Color(0xF1, 0xF5, 0xF9); // #F1F5F9 Slate 100
    public static final Color SURFACE_BACKGROUND    = new Color(0xFF, 0xFF, 0xFF); // #FFFFFF
    public static final Color SURFACE_ELEVATED      = new Color(0xF8, 0xFA, 0xFC); // #F8FAFC Slate 50
    public static final Color TABLE_BACKGROUND      = new Color(0xFF, 0xFF, 0xFF); // #FFFFFF

    // ── Sidebar (deep blue) ─────────────────────────────────────
    public static final Color SIDEBAR_BACKGROUND    = new Color(0x0F, 0x17, 0x2A); // #0F172A Slate 900
    public static final Color SIDEBAR_HOVER         = new Color(0x1E, 0x29, 0x3B); // #1E293B Slate 800
    public static final Color SIDEBAR_ACTIVE        = new Color(0x33, 0x4F, 0x85); // #334F85 Muted Blue
    public static final Color SIDEBAR_TEXT           = new Color(0x94, 0xA3, 0xB8); // #94A3B8 Slate 400
    public static final Color SIDEBAR_TEXT_ACTIVE    = new Color(0xFF, 0xFF, 0xFF); // #FFFFFF
    public static final Color SIDEBAR_SEPARATOR      = new Color(0x1E, 0x29, 0x3B); // #1E293B

    // ── Border ──────────────────────────────────────────────────
    public static final Color BORDER_SOFT           = new Color(0xE2, 0xE8, 0xF0); // #E2E8F0 Slate 200
    public static final Color BORDER_LIGHTER        = new Color(0xF1, 0xF5, 0xF9); // #F1F5F9 Slate 100

    // ── Text ────────────────────────────────────────────────────
    public static final Color TEXT_PRIMARY           = new Color(0x1E, 0x29, 0x3B); // #1E293B Slate 800
    public static final Color TEXT_SECONDARY         = new Color(0x64, 0x74, 0x8B); // #64748B Slate 500
    public static final Color TEXT_MUTED             = new Color(0x94, 0xA3, 0xB8); // #94A3B8 Slate 400

    // ── Primary Blue ────────────────────────────────────────────
    public static final Color PRIMARY               = new Color(0x37, 0x5F, 0xEB); // #375FEB Vivid Blue
    public static final Color PRIMARY_HOVER          = new Color(0x2F, 0x52, 0xD4); // #2F52D4 Darker Blue
    public static final Color PRIMARY_SOFT           = new Color(0xEE, 0xF2, 0xFF); // #EEF2FF Indigo 50
    public static final Color PRIMARY_BORDER         = new Color(0xA5, 0xB4, 0xFC); // #A5B4FC Indigo 300
    public static final Color SECONDARY             = new Color(0x60, 0x7D, 0xF3); // #607DF3 Softer Blue

    // ── Action buttons ──────────────────────────────────────────
    public static final Color ACTION_BG             = new Color(0xEE, 0xF2, 0xFF); // #EEF2FF Indigo 50
    public static final Color ACTION_FG             = new Color(0x37, 0x5F, 0xEB); // #375FEB

    // ── Status / Semantic ───────────────────────────────────────
    public static final Color SUCCESS               = new Color(0x22, 0xC5, 0x5E); // #22C55E Green 500
    public static final Color WARNING               = new Color(0xF5, 0x9E, 0x0B); // #F59E0B Amber 500
    public static final Color DANGER                = new Color(0xEF, 0x44, 0x44); // #EF4444 Red 500

    // ── KPI Accent Colors ───────────────────────────────────────
    public static final Color KPI_BLUE              = new Color(0x37, 0x5F, 0xEB); // #375FEB
    public static final Color KPI_GREEN             = new Color(0x22, 0xC5, 0x5E); // #22C55E
    public static final Color KPI_AMBER             = new Color(0xF5, 0x9E, 0x0B); // #F59E0B
    public static final Color KPI_PURPLE            = new Color(0x8B, 0x5C, 0xF6); // #8B5CF6

    // ── Gradient helpers (sidebar header) ───────────────────────
    public static final Color GRADIENT_START         = new Color(0x1E, 0x3A, 0x5F); // #1E3A5F Deep Teal
    public static final Color GRADIENT_END           = new Color(0x37, 0x5F, 0xEB); // #375FEB

    private AdminUiPalette() {
    }
}
