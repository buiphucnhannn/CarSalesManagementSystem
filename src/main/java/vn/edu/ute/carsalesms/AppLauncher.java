package vn.edu.ute.carsalesms;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import vn.edu.ute.carsalesms.controller.AuthController;
import vn.edu.ute.carsalesms.controller.AuditLogController;
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
import vn.edu.ute.carsalesms.dao.CustomerDao;
import vn.edu.ute.carsalesms.dao.StaffDao;
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
import vn.edu.ute.carsalesms.service.InvoiceAutoIssueService;
import vn.edu.ute.carsalesms.service.PaymentInstallmentPlanService;
import vn.edu.ute.carsalesms.service.PaymentOrderFinalizationService;
import vn.edu.ute.carsalesms.service.PaymentRecordFactory;
import vn.edu.ute.carsalesms.service.TestDriveService;
import vn.edu.ute.carsalesms.service.PaymentValidationService;
import vn.edu.ute.carsalesms.service.WarrantyAutoActivationService;
import vn.edu.ute.carsalesms.service.WarrantyService;
import vn.edu.ute.carsalesms.service.impl.*;
import vn.edu.ute.carsalesms.session.CurrentSessionContextAdapter;
import vn.edu.ute.carsalesms.session.CurrentSession;
import vn.edu.ute.carsalesms.session.UserSessionContext;
import vn.edu.ute.carsalesms.view.admin.AdminDashboardFrame;
import vn.edu.ute.carsalesms.view.auth.LoginFrame;
import vn.edu.ute.carsalesms.view.staff.StaffDashboardFrame;
import vn.edu.ute.carsalesms.view.theme.LookAndFeelConfig;

/**
 * Lớp khởi chạy ứng dụng.
 * Chịu trách nhiệm khởi tạo tất cả các phụ thuộc, hiển thị màn hình đăng nhập và sau đó mở bảng điều khiển thích hợp
 * dựa trên vai trò của người dùng.
 */
public class AppLauncher {

    private record ApplicationDependencies(
            AuthController authController,
            DashboardService dashboardService,
            CarManagementController carManagementController,
            BranchManagementController branchManagementController,
            CustomerManagementController customerManagementController,
            StaffManagementController staffManagementController,
            AuditLogController auditLogController,
            SaleOrderController saleOrderController,
            PaymentController paymentController,
            InstallmentController installmentController,
            InvoiceController invoiceController,
            PromotionController promotionController,
            StatisticsController statisticsController,
            TestDriveService testDriveService,
            WarrantyService warrantyService
    ) {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AppLauncher::startApplication);
    }

    private static void startApplication() {
        try {
            LookAndFeelConfig.apply();
            ApplicationDependencies deps = bootstrapDependencies();
            showLoginFrame(
                    deps
            );
        } catch (Exception e) {
            throw new IllegalStateException("Không thể khởi chạy ứng dụng", e);
        }
    }

    private static ApplicationDependencies bootstrapDependencies() {
        AuditLogService auditLogService = buildAuditLogService();
        CustomerDao testDriveCustomerDao = new CustomerDaoImpl();
        CarDao testDriveCarDao = new CarDaoImpl();
        StaffDao testDriveStaffDao = new StaffDaoImpl();
        UserSessionContext sessionContext = new CurrentSessionContextAdapter();
        InvoiceAutoIssueService invoiceAutoIssueService = new InvoiceAutoIssueServiceImpl(new InvoiceDaoImpl());
        WarrantyAutoActivationService warrantyAutoActivationService = new WarrantyAutoActivationServiceImpl(new WarrantyDaoImpl());
        PaymentInstallmentPlanService paymentInstallmentPlanService = new PaymentInstallmentPlanServiceImpl(new InstallmentPlanDaoImpl());
        PaymentValidationService paymentValidationService = new PaymentValidationServiceImpl();
        PaymentOrderFinalizationService paymentOrderFinalizationService =
                new PaymentOrderFinalizationServiceImpl(invoiceAutoIssueService, warrantyAutoActivationService);
        PaymentRecordFactory paymentRecordFactory = new PaymentRecordFactoryImpl();
        return new ApplicationDependencies(
                buildAuthController(auditLogService),
                buildDashboardService(),
                buildCarManagementController(auditLogService),
                buildBranchManagementController(auditLogService),
                buildCustomerManagementController(auditLogService),
                buildStaffManagementController(auditLogService),
                buildAuditLogController(auditLogService),
                buildSaleOrderController(auditLogService),
                buildPaymentController(auditLogService, sessionContext, paymentValidationService, paymentInstallmentPlanService, paymentOrderFinalizationService, paymentRecordFactory),
                buildInstallmentController(auditLogService, sessionContext, paymentValidationService, paymentInstallmentPlanService, paymentOrderFinalizationService, paymentRecordFactory),
                buildInvoiceController(auditLogService),
                buildPromotionController(auditLogService),
                buildStatisticsController(),
                new TestDriveServiceImpl(new TestDriveDaoImpl(), testDriveCustomerDao, testDriveCarDao, testDriveStaffDao, auditLogService),
                new WarrantyServiceImpl(new WarrantyDaoImpl(), new SaleOrderDaoImpl(), auditLogService)
        );
    }

    private static AuditLogController buildAuditLogController(AuditLogService auditLogService) {
        return new AuditLogController(auditLogService);
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

    private static PaymentController buildPaymentController(AuditLogService auditLogService,
                                                            UserSessionContext sessionContext,
                                                            PaymentValidationService paymentValidationService,
                                                            PaymentInstallmentPlanService paymentInstallmentPlanService,
                                                            PaymentOrderFinalizationService paymentOrderFinalizationService,
                                                            PaymentRecordFactory paymentRecordFactory) {
        return new PaymentController(new PaymentServiceImpl(
                new PaymentDaoImpl(),
                new SaleOrderDaoImpl(),
                sessionContext,
                paymentValidationService,
                paymentInstallmentPlanService,
                paymentOrderFinalizationService,
                paymentRecordFactory
        ), auditLogService);
    }

    private static InstallmentController buildInstallmentController(AuditLogService auditLogService,
                                                                    UserSessionContext sessionContext,
                                                                    PaymentValidationService paymentValidationService,
                                                                    PaymentInstallmentPlanService paymentInstallmentPlanService,
                                                                    PaymentOrderFinalizationService paymentOrderFinalizationService,
                                                                    PaymentRecordFactory paymentRecordFactory) {
        PaymentServiceImpl paymentService = new PaymentServiceImpl(
                new PaymentDaoImpl(),
                new SaleOrderDaoImpl(),
                sessionContext,
                paymentValidationService,
                paymentInstallmentPlanService,
                paymentOrderFinalizationService,
                paymentRecordFactory
        );
        return new InstallmentController(new InstallmentServiceImpl(new InstallmentPlanDaoImpl(), paymentService, sessionContext), auditLogService);
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

    private static void showLoginFrame(ApplicationDependencies deps) {
        LoginFrame loginFrame = new LoginFrame(
                deps.authController(),
                user -> openDashboardByRole(user, deps)
        );
        loginFrame.setVisible(true);
    }

    private static void openDashboardByRole(AuthenticatedUser user, ApplicationDependencies deps) {
        JFrame dashboard;
        if (user.role() == StaffRole.ADMIN) {
            JFrame[] frameRef = new JFrame[1];
            frameRef[0] = new AdminDashboardFrame(
                    user,
                    deps.dashboardService(),
                    deps.carManagementController(),
                    deps.branchManagementController(),
                    deps.customerManagementController(),
                    deps.staffManagementController(),
                    deps.saleOrderController(),
                    deps.paymentController(),
                    deps.installmentController(),
                    deps.invoiceController(),
                    deps.promotionController(),
                    deps.statisticsController(),
                    deps.auditLogController(),
                    deps.testDriveService(),
                    deps.warrantyService(),
                    () -> logoutAndBackToLogin(frameRef[0])
            );
            dashboard = frameRef[0];
        } else if (user.role() == StaffRole.STAFF) {
            JFrame[] frameRef = new JFrame[1];
            frameRef[0] = new StaffDashboardFrame(
                    user,
                    deps.dashboardService(),
                    deps.carManagementController(),
                    deps.customerManagementController(),
                    deps.saleOrderController(),
                    deps.paymentController(),
                    deps.installmentController(),
                    deps.invoiceController(),
                    deps.statisticsController(),
                    deps.testDriveService(),
                    deps.warrantyService(),
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
        ApplicationDependencies deps = bootstrapDependencies();
        showLoginFrame(deps);
    }
}