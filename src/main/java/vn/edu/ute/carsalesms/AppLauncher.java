package vn.edu.ute.carsalesms;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import vn.edu.ute.carsalesms.controller.AuthController;
import vn.edu.ute.carsalesms.controller.BranchManagementController;
import vn.edu.ute.carsalesms.controller.CarManagementController;
import vn.edu.ute.carsalesms.controller.CustomerManagementController;
import vn.edu.ute.carsalesms.controller.SaleOrderController;
import vn.edu.ute.carsalesms.controller.PaymentController;
import vn.edu.ute.carsalesms.controller.InstallmentController;
import vn.edu.ute.carsalesms.controller.InvoiceController;
import vn.edu.ute.carsalesms.controller.PromotionController;
import vn.edu.ute.carsalesms.controller.StatisticsController;
import vn.edu.ute.carsalesms.controller.StaffManagementController;
import vn.edu.ute.carsalesms.dao.AccountDao;
import vn.edu.ute.carsalesms.dao.BranchDao;
import vn.edu.ute.carsalesms.dao.CarDao;
import vn.edu.ute.carsalesms.dao.impl.*;
import vn.edu.ute.carsalesms.dao.impl.DashboardDaoImpl;
import vn.edu.ute.carsalesms.dao.impl.StaffDaoImpl;
import vn.edu.ute.carsalesms.model.dto.AuthenticatedUser;
import vn.edu.ute.carsalesms.model.enums.StaffRole;
import vn.edu.ute.carsalesms.service.AuthService;
import vn.edu.ute.carsalesms.service.AuditLogService;
import vn.edu.ute.carsalesms.service.BranchService;
import vn.edu.ute.carsalesms.service.CarService;
import vn.edu.ute.carsalesms.service.DashboardService;
import vn.edu.ute.carsalesms.service.impl.*;
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
            AuditLogService auditLogService = buildAuditLogService();
            AuthController authController = buildAuthController(auditLogService);
            DashboardService dashboardService = buildDashboardService();
            CarManagementController carManagementController = buildCarManagementController(auditLogService);
            BranchManagementController branchManagementController = buildBranchManagementController(auditLogService);
            CustomerManagementController customerManagementController = buildCustomerManagementController(auditLogService);
            StaffManagementController staffManagementController = buildStaffManagementController(auditLogService);
            SaleOrderController saleOrderController = buildSaleOrderController(auditLogService);
            PaymentController paymentController = buildPaymentController(auditLogService);
            InstallmentController installmentController = buildInstallmentController(auditLogService);
            InvoiceController invoiceController = buildInvoiceController(auditLogService);
            PromotionController promotionController = buildPromotionController(auditLogService);
            StatisticsController statisticsController = buildStatisticsController();
            showLoginFrame(authController, dashboardService, carManagementController, branchManagementController,
                    customerManagementController, staffManagementController, saleOrderController, paymentController, installmentController, invoiceController, promotionController, statisticsController);
        } catch (Exception e) {
            throw new IllegalStateException("Không thể khởi chạy ứng dụng", e);
        }
    }

    private static AuditLogService buildAuditLogService() {
        return new AuditLogServiceImpl(new AuditLogDaoImpl(), new StaffDaoImpl());
    }

    private static AuthController buildAuthController(AuditLogService auditLogService) {
        AccountDao accountDao = new AccountDaoImpl();
        AuthService authService = new AuthServiceImpl(accountDao, auditLogService);
        return new AuthController(authService);
    }

    private static DashboardService buildDashboardService() {
        return new DashboardServiceImpl(new DashboardDaoImpl());
    }

    private static CarManagementController buildCarManagementController(AuditLogService auditLogService) {
        CarDao carDao = new CarDaoImpl();
        CarService carService = new CarServiceImpl(carDao);
        return new CarManagementController(carService, auditLogService);
    }

    private static BranchManagementController buildBranchManagementController(AuditLogService auditLogService) {
        BranchDao branchDao = new BranchDaoImpl();
        BranchService branchService = new BranchServiceImpl(branchDao);
        return new BranchManagementController(branchService, auditLogService);
    }

    /** Khởi tạo CustomerManagementController. */
    private static CustomerManagementController buildCustomerManagementController(AuditLogService auditLogService) {
        return new CustomerManagementController(new CustomerServiceImpl(new CustomerDaoImpl()), auditLogService);
    }

    /** Khởi tạo StaffManagementController. */
    private static StaffManagementController buildStaffManagementController(AuditLogService auditLogService) {
        return new StaffManagementController(new StaffServiceImpl(new StaffDaoImpl()), auditLogService);
    }

    private static SaleOrderController buildSaleOrderController(AuditLogService auditLogService) {
        return new SaleOrderController(new SaleOrderServiceImpl(new SaleOrderDaoImpl(), new CarDaoImpl(), new CustomerDaoImpl(), new StaffDaoImpl(), new PromotionDaoImpl()), auditLogService);
    }

    private static PaymentController buildPaymentController(AuditLogService auditLogService) {
        return new PaymentController(new PaymentServiceImpl(new PaymentDaoImpl(), new SaleOrderDaoImpl(), new InvoiceDaoImpl(), new InstallmentPlanDaoImpl()), auditLogService);
    }

    private static InstallmentController buildInstallmentController(AuditLogService auditLogService) {
        return new InstallmentController(new InstallmentServiceImpl(new InstallmentPlanDaoImpl(), new PaymentServiceImpl(new PaymentDaoImpl(), new SaleOrderDaoImpl(), new InvoiceDaoImpl(), new InstallmentPlanDaoImpl())), auditLogService);
    }

    private static InvoiceController buildInvoiceController(AuditLogService auditLogService) {
        return new InvoiceController(new InvoiceServiceImpl(new InvoiceDaoImpl(), new InvoicePdfExporterImpl()), auditLogService);
    }

    private static PromotionController buildPromotionController(AuditLogService auditLogService) {
        return new PromotionController(new PromotionServiceImpl(new PromotionDaoImpl()), auditLogService);
    }

    private static StatisticsController buildStatisticsController() {
        return new StatisticsController(new StatisticsServiceImpl(new StatisticsDaoImpl()));
    }

    private static void showLoginFrame(AuthController authController,
                                       DashboardService dashboardService,
                                       CarManagementController carManagementController,
                                       BranchManagementController branchManagementController,
                                       CustomerManagementController customerManagementController,
                                       StaffManagementController staffManagementController,
                                       SaleOrderController saleOrderController,
                                       PaymentController paymentController,
                                       InstallmentController installmentController,
                                       InvoiceController invoiceController,
                                       PromotionController promotionController,
                                       StatisticsController statisticsController) {
        LoginFrame loginFrame = new LoginFrame(
                authController,
                user -> openDashboardByRole(user, dashboardService, carManagementController,
                        branchManagementController,
                        customerManagementController, staffManagementController, saleOrderController, paymentController, installmentController, invoiceController, promotionController, statisticsController)
        );
        loginFrame.setVisible(true);
    }

    private static void openDashboardByRole(AuthenticatedUser user,
                                            DashboardService dashboardService,
                                            CarManagementController carManagementController,
                                            BranchManagementController branchManagementController,
                                            CustomerManagementController customerManagementController,
                                            StaffManagementController staffManagementController,
                                            SaleOrderController saleOrderController,
                                            PaymentController paymentController,
                                            InstallmentController installmentController,
                                            InvoiceController invoiceController,
                                            PromotionController promotionController,
                                            StatisticsController statisticsController) {
        JFrame dashboard;
        if (user.role() == StaffRole.ADMIN) {
            JFrame[] frameRef = new JFrame[1];
            // Truyền đủ 10 tham số cho constructor mới của AdminDashboardFrame
            frameRef[0] = new AdminDashboardFrame(
                    user,
                    dashboardService,
                    carManagementController,
                    branchManagementController,
                    customerManagementController,
                    staffManagementController,
                    saleOrderController,
                    paymentController,
                    installmentController,
                    invoiceController,
                    promotionController,
                    statisticsController,
                    () -> logoutAndBackToLogin(frameRef[0])
            );
            dashboard = frameRef[0];
        } else if (user.role() == StaffRole.STAFF) {
            JFrame[] frameRef = new JFrame[1];
            frameRef[0] = new StaffDashboardFrame(
                    user,
                    dashboardService,
                    carManagementController,
                    customerManagementController,
                    saleOrderController,
                    paymentController,
                    installmentController,
                    invoiceController,
                    statisticsController,
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
        AuditLogService auditLogService = buildAuditLogService();

        CurrentSession.clear();
        dashboard.dispose();
        AuthController authController = buildAuthController(auditLogService);
        DashboardService dashboardService = buildDashboardService();
        CarManagementController carManagementController = buildCarManagementController(auditLogService);
        BranchManagementController branchManagementController = buildBranchManagementController(auditLogService);
        CustomerManagementController customerManagementController = buildCustomerManagementController(auditLogService);
        StaffManagementController staffManagementController = buildStaffManagementController(auditLogService);
        SaleOrderController saleOrderController = buildSaleOrderController(auditLogService);
        PaymentController paymentController = buildPaymentController(auditLogService);
        InstallmentController installmentController = buildInstallmentController(auditLogService);
        InvoiceController invoiceController = buildInvoiceController(auditLogService);
        PromotionController promotionController = buildPromotionController(auditLogService);
        StatisticsController statisticsController = buildStatisticsController();
        showLoginFrame(authController, dashboardService, carManagementController, branchManagementController,
                customerManagementController, staffManagementController, saleOrderController, paymentController, installmentController, invoiceController, promotionController, statisticsController);
    }
}