package vn.edu.ute.carsalesms;

import java.util.Map;
import java.util.function.Supplier;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import vn.edu.ute.carsalesms.view.admin.AdminDashboardFrame;
import vn.edu.ute.carsalesms.view.staff.StaffDashboardFrame;
import vn.edu.ute.carsalesms.view.theme.LookAndFeelConfig;

/**
 * Lớp khởi chạy ứng dụng.
 */
public class AppLauncher {

    private static final String ROLE_STAFF = "STAFF";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final Map<String, Supplier<JFrame>> DASHBOARD_FACTORIES = Map.of(
            ROLE_STAFF, StaffDashboardFrame::new,
            ROLE_ADMIN, AdminDashboardFrame::new
    );

    public static void main(String[] args) {
        String role = parseRoleArg(args);
        SwingUtilities.invokeLater(() -> startDashboardUi(role));
    }

    private static String parseRoleArg(String[] args) {
        if (args == null || args.length == 0) {
            return ROLE_ADMIN;
        }
        if (args[0] == null) {
            return ROLE_ADMIN;
        }

        String normalizedRole = args[0].trim().toUpperCase();
        if (ROLE_ADMIN.equals(normalizedRole) || ROLE_STAFF.equals(normalizedRole)) {
            return normalizedRole;
        }
        return ROLE_ADMIN;
    }

    private static void startDashboardUi(String role) {
        try {
            LookAndFeelConfig.apply();
            Supplier<JFrame> frameFactory = DASHBOARD_FACTORIES.getOrDefault(role, DASHBOARD_FACTORIES.get(ROLE_STAFF));
            JFrame frame = frameFactory.get();
            frame.setVisible(true);
        } catch (Exception e) {
            throw new IllegalStateException("Không thể khởi chạy dashboard", e);
        }
    }
}