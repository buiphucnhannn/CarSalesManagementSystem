package vn.edu.ute.carsalesms.view.auth;

import vn.edu.ute.carsalesms.controller.AuthController;
import vn.edu.ute.carsalesms.model.dto.AuthenticatedUser;
import vn.edu.ute.carsalesms.view.theme.AdminUiPalette;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Màn hình đăng nhập hiện đại, split-panel: branding bên trái + form bên phải.
 */
public class LoginFrame extends JFrame {

    private final AuthController authController;
    private final Consumer<AuthenticatedUser> onLoginSuccess;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel errorLabel;
    private JButton loginButton;
    private boolean passwordVisible = false;

    /* ── Custom Colors (derived from AdminUiPalette for harmony) ── */
    private static final Color BRAND_GRADIENT_START = AdminUiPalette.SIDEBAR_BACKGROUND;   // #0F172A
    private static final Color BRAND_GRADIENT_END   = AdminUiPalette.GRADIENT_END;          // #375FEB
    private static final Color FORM_BG              = AdminUiPalette.SURFACE_BACKGROUND;    // #FFFFFF
    private static final Color INPUT_BG             = new Color(0xF8, 0xFA, 0xFC);          // Slate 50
    private static final Color INPUT_BORDER         = AdminUiPalette.BORDER_SOFT;           // #E2E8F0
    private static final Color INPUT_FOCUS_BORDER   = AdminUiPalette.PRIMARY;               // #375FEB
    private static final Color BUTTON_GRADIENT_L    = AdminUiPalette.PRIMARY;               // #375FEB
    private static final Color BUTTON_GRADIENT_R    = AdminUiPalette.SECONDARY;             // #607DF3
    private static final Color PLACEHOLDER_COLOR    = AdminUiPalette.TEXT_MUTED;            // #94A3B8

    public LoginFrame(AuthController authController, Consumer<AuthenticatedUser> onLoginSuccess) {
        this.authController = Objects.requireNonNull(authController, "authController is required");
        this.onLoginSuccess = Objects.requireNonNull(onLoginSuccess, "onLoginSuccess is required");

        setTitle("Quản Lý Bán Xe - Đăng Nhập");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(820, 520);
        setResizable(false);
        setLocationRelativeTo(null);
        setContentPane(buildContent());
    }

    /* ================================================================
     *  ROOT CONTENT — split left (brand) + right (form)
     * ================================================================ */
    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(FORM_BG);
        root.add(buildBrandPanel(), BorderLayout.WEST);
        root.add(buildFormPanel(), BorderLayout.CENTER);
        return root;
    }

    /* ────────────────────────── BRAND (LEFT) ─────────────────────── */
    private JPanel buildBrandPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();

                // Two-stop gradient: top-dark → bottom-blue
                GradientPaint gp = new GradientPaint(0, 0, BRAND_GRADIENT_START,
                        w, h, BRAND_GRADIENT_END);
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);

                // Subtle radial glow
                RadialGradientPaint radial = new RadialGradientPaint(
                        new Point2D.Float(w * 0.5f, h * 0.65f),
                        w * 0.7f,
                        new float[]{0f, 1f},
                        new Color[]{new Color(55, 95, 235, 50), new Color(55, 95, 235, 0)}
                );
                g2.setPaint(radial);
                g2.fillRect(0, 0, w, h);

                // Decorative circles
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillOval(-40, h - 180, 220, 220);
                g2.fillOval(w - 90, -60, 160, 160);

                g2.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(330, 0));

        // Content wrapper
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(0, 36, 50, 36));

        content.add(Box.createVerticalGlue());

        // Car icon (Unicode)
        JLabel iconLabel = new JLabel("\uD83D\uDE97");  // 🚗
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
        iconLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(iconLabel);
        content.add(Box.createVerticalStrut(18));

        // Brand title
        JLabel brandTitle = new JLabel("<html>Quản Lý<br/>Bán Xe</html>");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        brandTitle.setForeground(Color.WHITE);
        brandTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(brandTitle);
        content.add(Box.createVerticalStrut(14));

        // Accent bar
        JPanel accentBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, AdminUiPalette.SECONDARY,
                        getWidth(), 0, AdminUiPalette.PRIMARY_BORDER);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.dispose();
            }
        };
        accentBar.setOpaque(false);
        accentBar.setPreferredSize(new Dimension(60, 4));
        accentBar.setMaximumSize(new Dimension(60, 4));
        accentBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(accentBar);
        content.add(Box.createVerticalStrut(16));

        // Description
        JLabel desc = new JLabel("<html>Hệ thống quản lý showroom ô tô<br/>chuyên nghiệp & hiện đại</html>");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        desc.setForeground(new Color(255, 255, 255, 180));
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(desc);

        content.add(Box.createVerticalGlue());

        // Footer / copyright
        JLabel footer = new JLabel("© 2026 CarSalesMS");
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footer.setForeground(new Color(255, 255, 255, 100));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(footer);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    /* ────────────────────────── FORM (RIGHT) ─────────────────────── */
    private JPanel buildFormPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(FORM_BG);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(FORM_BG);
        form.setPreferredSize(new Dimension(340, 400));
        form.setMaximumSize(new Dimension(340, 400));

        // Welcome text
        JLabel welcomeLabel = new JLabel("Chào mừng trở lại");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        welcomeLabel.setForeground(AdminUiPalette.TEXT_PRIMARY);
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(welcomeLabel);
        form.add(Box.createVerticalStrut(6));

        JLabel subLabel = new JLabel("Đăng nhập vào tài khoản của bạn");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subLabel.setForeground(AdminUiPalette.TEXT_SECONDARY);
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(subLabel);
        form.add(Box.createVerticalStrut(32));

        // Username
        JLabel userLabel = createFieldLabel("Tên đăng nhập");
        form.add(userLabel);
        form.add(Box.createVerticalStrut(6));

        usernameField = createStyledTextField("Nhập tên đăng nhập");
        form.add(usernameField);
        form.add(Box.createVerticalStrut(18));

        // Password
        JLabel passLabel = createFieldLabel("Mật khẩu");
        form.add(passLabel);
        form.add(Box.createVerticalStrut(6));

        // Password field + toggle button in a row
        passwordField = createStyledPasswordField("Nhập mật khẩu");
        JButton togglePasswordBtn = createTogglePasswordButton();

        JPanel passwordRow = new JPanel(new BorderLayout(6, 0));
        passwordRow.setBackground(FORM_BG);
        passwordRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        passwordRow.add(passwordField, BorderLayout.CENTER);
        passwordRow.add(togglePasswordBtn, BorderLayout.EAST);
        form.add(passwordRow);
        form.add(Box.createVerticalStrut(10));

        // Error label
        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setForeground(AdminUiPalette.DANGER);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(errorLabel);
        form.add(Box.createVerticalStrut(8));

        // Login button
        loginButton = createGradientButton("Đăng nhập");
        loginButton.addActionListener(e -> doLogin());
        form.add(loginButton);

        // Enter-key actions
        passwordField.addActionListener(e -> doLogin());
        usernameField.addActionListener(e -> passwordField.requestFocusInWindow());

        wrapper.add(form);
        return wrapper;
    }

    /* ================================================================
     *  STYLED COMPONENT FACTORIES
     * ================================================================ */

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        label.setForeground(AdminUiPalette.TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    /** Custom-drawn eye toggle button: open eye = visible, slashed eye = hidden. */
    private JButton createTogglePasswordButton() {
        JButton btn = new JButton() {
            private boolean hovering = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
                    @Override
                    public void mouseExited(MouseEvent e) { hovering = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                int arc = 10;

                // Background
                g2.setColor(passwordVisible ? AdminUiPalette.PRIMARY_SOFT : INPUT_BG);
                g2.fillRoundRect(0, 0, w, h, arc, arc);
                g2.setColor(passwordVisible ? INPUT_FOCUS_BORDER : INPUT_BORDER);
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

                // Draw eye icon centered
                Color iconColor = hovering ? AdminUiPalette.PRIMARY : AdminUiPalette.TEXT_SECONDARY;
                g2.setColor(iconColor);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                int cx = w / 2, cy = h / 2;
                int eyeW = 18, eyeH = 10;

                // Eye shape (two arcs forming an almond/eye shape)
                g2.drawArc(cx - eyeW / 2, cy - eyeH / 2, eyeW, eyeH * 2, 0, 180);   // top arc
                g2.drawArc(cx - eyeW / 2, cy - eyeH * 3 / 2, eyeW, eyeH * 2, 180, 180); // bottom arc

                // Pupil (filled circle)
                int pupilR = 3;
                g2.fillOval(cx - pupilR, cy - pupilR, pupilR * 2, pupilR * 2);

                // If password is HIDDEN, draw diagonal slash across the eye
                if (!passwordVisible) {
                    g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(cx - 9, cy + 7, cx + 9, cy - 7);
                }

                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(44, 42));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("Hiện / Ẩn mật khẩu");

        btn.addActionListener(e -> {
            passwordVisible = !passwordVisible;
            if (passwordVisible) {
                passwordField.setEchoChar((char) 0);  // show plain text
            } else {
                passwordField.setEchoChar('●');        // hide with bullets
            }
            btn.repaint();
            passwordField.requestFocusInWindow();
        });

        return btn;
    }

    /** Rounded text field with placeholder support. */
    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(PLACEHOLDER_COLOR);
                    g2.setFont(getFont());
                    Insets ins = getInsets();
                    g2.drawString(placeholder, ins.left, g.getFontMetrics().getMaxAscent() + ins.top);
                    g2.dispose();
                }
            }
        };
        styleInput(field);
        return field;
    }

    /** Rounded password field with placeholder support. */
    private JPasswordField createStyledPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getPassword().length == 0 && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(PLACEHOLDER_COLOR);
                    g2.setFont(getFont().deriveFont(Font.PLAIN));
                    Insets ins = getInsets();
                    g2.drawString(placeholder, ins.left, g.getFontMetrics().getMaxAscent() + ins.top);
                    g2.dispose();
                }
            }
        };
        styleInput(field);
        return field;
    }

    private void styleInput(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(INPUT_BG);
        field.setForeground(AdminUiPalette.TEXT_PRIMARY);
        field.setCaretColor(AdminUiPalette.TEXT_PRIMARY);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        field.setPreferredSize(new Dimension(0, 42));

        field.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(10, INPUT_BORDER),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));

        // Focus highlight
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(10, INPUT_FOCUS_BORDER),
                        BorderFactory.createEmptyBorder(6, 14, 6, 14)
                ));
                field.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(10, INPUT_BORDER),
                        BorderFactory.createEmptyBorder(6, 14, 6, 14)
                ));
                field.repaint();
            }
        });
    }

    /** Gradient rounded login button with hover animation. */
    private JButton createGradientButton(String text) {
        JButton button = new JButton(text) {
            private float hoverProgress = 0f;

            {
                setContentAreaFilled(false);
                setFocusPainted(false);
                setBorderPainted(false);
                setOpaque(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                Timer hoverTimer = new Timer(16, null);
                boolean[] hovering = {false};

                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hovering[0] = true;
                        hoverTimer.restart();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hovering[0] = false;
                        hoverTimer.restart();
                    }
                });

                hoverTimer.addActionListener(ev -> {
                    float target = hovering[0] ? 1f : 0f;
                    hoverProgress += (target - hoverProgress) * 0.25f;
                    if (Math.abs(hoverProgress - target) < 0.01f) {
                        hoverProgress = target;
                        hoverTimer.stop();
                    }
                    repaint();
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                int arc = 12;

                // Background gradient — shifts with hover
                Color left = blendColors(BUTTON_GRADIENT_L, AdminUiPalette.PRIMARY_HOVER, hoverProgress);
                Color right = blendColors(BUTTON_GRADIENT_R, BUTTON_GRADIENT_L, hoverProgress);
                GradientPaint gp = new GradientPaint(0, 0, left, w, 0, right);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w, h, arc, arc);

                // Subtle shine on top
                g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 30),
                        0, h / 2, new Color(255, 255, 255, 0)));
                g2.fillRoundRect(0, 0, w, h / 2, arc, arc);

                // Disabled overlay
                if (!isEnabled()) {
                    g2.setColor(new Color(255, 255, 255, 120));
                    g2.fillRoundRect(0, 0, w, h, arc, arc);
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };

        button.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 15));
        button.setForeground(Color.WHITE);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        button.setPreferredSize(new Dimension(0, 44));

        return button;
    }

    /* ================================================================
     *  ROUNDED BORDER UTILITY
     * ================================================================ */
    private static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;

        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(0, 0, 0, 0);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }

    /* ================================================================
     *  COLOR BLENDING UTILITY
     * ================================================================ */
    private static Color blendColors(Color a, Color b, float ratio) {
        float inv = 1f - ratio;
        return new Color(
                Math.round(a.getRed() * inv + b.getRed() * ratio),
                Math.round(a.getGreen() * inv + b.getGreen() * ratio),
                Math.round(a.getBlue() * inv + b.getBlue() * ratio)
        );
    }

    /* ================================================================
     *  LOGIN LOGIC (unchanged)
     * ================================================================ */
    private void doLogin() {
        setBusy(true);
        errorLabel.setText(" ");

        char[] passwordChars = passwordField.getPassword();
        try {
            AuthenticatedUser user = authController.login(usernameField.getText(), passwordChars);
            onLoginSuccess.accept(user);
            dispose();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            errorLabel.setText(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();  // In lỗi ra console để debug
            errorLabel.setText("Lỗi: " + ex.getMessage());
        } finally {
            Arrays.fill(passwordChars, '\0');
            passwordField.setText("");
            setBusy(false);
        }
    }

    private void setBusy(boolean busy) {
        loginButton.setEnabled(!busy);
        usernameField.setEnabled(!busy);
        passwordField.setEnabled(!busy);
    }
}
