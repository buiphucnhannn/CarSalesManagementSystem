package vn.edu.ute.carsalesms;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import vn.edu.ute.carsalesms.controller.AuthController;
import vn.edu.ute.carsalesms.dao.AccountDao;
import vn.edu.ute.carsalesms.dao.impl.AccountDaoImpl;
import vn.edu.ute.carsalesms.model.dto.AuthenticatedUser;
import vn.edu.ute.carsalesms.model.enums.StaffRole;
import vn.edu.ute.carsalesms.service.AuthService;
import vn.edu.ute.carsalesms.service.impl.AuthServiceImpl;
import vn.edu.ute.carsalesms.session.CurrentSession;
import vn.edu.ute.carsalesms.view.admin.AdminDashboardFrame;
import vn.edu.ute.carsalesms.view.auth.LoginFrame;
import vn.edu.ute.carsalesms.view.staff.StaffDashboardFrame;
import vn.edu.ute.carsalesms.view.theme.LookAndFeelConfig;

/**
 * Lớp khởi chạy ứng dụng.
 */
public class AppLauncher {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AppLauncher::startApplication);
    }

    private static void startApplication() {
        try {
            LookAndFeelConfig.apply();
            AuthController authController = buildAuthController();
            showLoginFrame(authController);
        } catch (Exception e) {
            throw new IllegalStateException("Không thể khởi chạy ứng dụng", e);
        }
    }

    private static AuthController buildAuthController() {
        AccountDao accountDao = new AccountDaoImpl();
        AuthService authService = new AuthServiceImpl(accountDao);
        return new AuthController(authService);
    }

    private static void showLoginFrame(AuthController authController) {
        LoginFrame loginFrame = new LoginFrame(authController, AppLauncher::openDashboardByRole);
        loginFrame.setVisible(true);
    }

    private static void openDashboardByRole(AuthenticatedUser user) {
        JFrame dashboard;
        if (user.role() == StaffRole.ADMIN) {
            JFrame[] frameRef = new JFrame[1];
            frameRef[0] = new AdminDashboardFrame(() -> logoutAndBackToLogin(frameRef[0]));
            dashboard = frameRef[0];
        } else if (user.role() == StaffRole.STAFF) {
            JFrame[] frameRef = new JFrame[1];
            frameRef[0] = new StaffDashboardFrame(() -> logoutAndBackToLogin(frameRef[0]));
            dashboard = frameRef[0];
        } else {
            CurrentSession.clear();
            JOptionPane.showMessageDialog(
                    null,
                    "Vai trò không hợp lệ: " + user.role(),
                    "Đăng nhập thất bại",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        dashboard.setTitle(dashboard.getTitle() + " - " + user.fullName());
        dashboard.setVisible(true);
    }

    private static void logoutAndBackToLogin(JFrame dashboard) {
        CurrentSession.clear();
        dashboard.dispose();
        AuthController authController = buildAuthController();
        showLoginFrame(authController);
    }
}