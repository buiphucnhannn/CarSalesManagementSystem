package vn.edu.ute.carsalesms;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import vn.edu.ute.carsalesms.controller.AuthController;
import vn.edu.ute.carsalesms.controller.CarManagementController;
import vn.edu.ute.carsalesms.dao.AccountDao;
import vn.edu.ute.carsalesms.dao.CarDao;
import vn.edu.ute.carsalesms.dao.impl.AccountDaoImpl;
import vn.edu.ute.carsalesms.dao.impl.CarDaoImpl;
import vn.edu.ute.carsalesms.dao.impl.DashboardDaoImpl;
import vn.edu.ute.carsalesms.model.dto.AuthenticatedUser;
import vn.edu.ute.carsalesms.model.enums.StaffRole;
import vn.edu.ute.carsalesms.service.AuthService;
import vn.edu.ute.carsalesms.service.CarService;
import vn.edu.ute.carsalesms.service.DashboardService;
import vn.edu.ute.carsalesms.service.impl.AuthServiceImpl;
import vn.edu.ute.carsalesms.service.impl.CarServiceImpl;
import vn.edu.ute.carsalesms.service.impl.DashboardServiceImpl;
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
            DashboardService dashboardService = buildDashboardService();
            CarManagementController carManagementController = buildCarManagementController();
            showLoginFrame(authController, dashboardService, carManagementController);
        } catch (Exception e) {
            throw new IllegalStateException("Không thể khởi chạy ứng dụng", e);
        }
    }

    private static AuthController buildAuthController() {
        AccountDao accountDao = new AccountDaoImpl();
        AuthService authService = new AuthServiceImpl(accountDao);
        return new AuthController(authService);
    }

    private static DashboardService buildDashboardService() {
        return new DashboardServiceImpl(new DashboardDaoImpl());
    }

    private static CarManagementController buildCarManagementController() {
        CarDao carDao = new CarDaoImpl();
        CarService carService = new CarServiceImpl(carDao);
        return new CarManagementController(carService);
    }

    private static void showLoginFrame(AuthController authController,
                                       DashboardService dashboardService,
                                       CarManagementController carManagementController) {
        LoginFrame loginFrame = new LoginFrame(
                authController,
                user -> openDashboardByRole(user, dashboardService, carManagementController)
        );
        loginFrame.setVisible(true);
    }

    private static void openDashboardByRole(AuthenticatedUser user,
                                            DashboardService dashboardService,
                                            CarManagementController carManagementController) {
        JFrame dashboard;
        if (user.role() == StaffRole.ADMIN) {
            JFrame[] frameRef = new JFrame[1];
            frameRef[0] = new AdminDashboardFrame(
                    user,
                    dashboardService,
                    carManagementController,
                    () -> logoutAndBackToLogin(frameRef[0])
            );
            dashboard = frameRef[0];
        } else if (user.role() == StaffRole.STAFF) {
            JFrame[] frameRef = new JFrame[1];
            frameRef[0] = new StaffDashboardFrame(
                    user,
                    dashboardService,
                    carManagementController,
                    () -> logoutAndBackToLogin(frameRef[0])
            );
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
        DashboardService dashboardService = buildDashboardService();
        CarManagementController carManagementController = buildCarManagementController();
        showLoginFrame(authController, dashboardService, carManagementController);
    }
}