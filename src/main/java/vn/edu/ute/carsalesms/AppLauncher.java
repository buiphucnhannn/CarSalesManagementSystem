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
            AuthController authController = buildAuthController();
            DashboardService dashboardService = buildDashboardService();
            CarManagementController carManagementController = buildCarManagementController();
            BranchManagementController branchManagementController = buildBranchManagementController();
            CustomerManagementController customerManagementController = buildCustomerManagementController();
            StaffManagementController staffManagementController = buildStaffManagementController();
            SaleOrderController saleOrderController = buildSaleOrderController();
            PaymentController paymentController = buildPaymentController();
            InstallmentController installmentController = buildInstallmentController();
            InvoiceController invoiceController = buildInvoiceController();
            PromotionController promotionController = buildPromotionController();
            showLoginFrame(authController, dashboardService, carManagementController, branchManagementController,
                    customerManagementController, staffManagementController, saleOrderController, paymentController, installmentController, invoiceController, promotionController);
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

    private static BranchManagementController buildBranchManagementController() {
        BranchDao branchDao = new BranchDaoImpl();
        BranchService branchService = new BranchServiceImpl(branchDao);
        return new BranchManagementController(branchService);
    }

    /** Khởi tạo CustomerManagementController. */
    private static CustomerManagementController buildCustomerManagementController() {
        return new CustomerManagementController(new CustomerServiceImpl(new CustomerDaoImpl()));
    }

    /** Khởi tạo StaffManagementController. */
    private static StaffManagementController buildStaffManagementController() {
        return new StaffManagementController(new StaffServiceImpl(new StaffDaoImpl()));
    }

    private static SaleOrderController buildSaleOrderController() {
        return new SaleOrderController(new SaleOrderServiceImpl(new SaleOrderDaoImpl(), new CarDaoImpl(), new CustomerDaoImpl(), new StaffDaoImpl(), new PromotionDaoImpl()));
    }

    private static PaymentController buildPaymentController() {
        return new PaymentController(new PaymentServiceImpl(new PaymentDaoImpl(), new SaleOrderDaoImpl(), new InvoiceDaoImpl(), new InstallmentPlanDaoImpl()));
    }

    private static InstallmentController buildInstallmentController() {
        return new InstallmentController(new InstallmentServiceImpl(new InstallmentPlanDaoImpl(), new PaymentServiceImpl(new PaymentDaoImpl(), new SaleOrderDaoImpl(), new InvoiceDaoImpl(), new InstallmentPlanDaoImpl())));
    }

    private static InvoiceController buildInvoiceController() {
        return new InvoiceController(new InvoiceServiceImpl(new InvoiceDaoImpl(), new InvoicePdfExporterImpl()));
    }

    private static PromotionController buildPromotionController() {
        return new PromotionController(new PromotionServiceImpl(new PromotionDaoImpl()));
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
                                       PromotionController promotionController) {
        LoginFrame loginFrame = new LoginFrame(
                authController,
                user -> openDashboardByRole(user, dashboardService, carManagementController,
                        branchManagementController,
                        customerManagementController, staffManagementController, saleOrderController, paymentController, installmentController, invoiceController, promotionController)
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
                                            PromotionController promotionController) {
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
        BranchManagementController branchManagementController = buildBranchManagementController();
        CustomerManagementController customerManagementController = buildCustomerManagementController();
        StaffManagementController staffManagementController = buildStaffManagementController();
        SaleOrderController saleOrderController = buildSaleOrderController();
        PaymentController paymentController = buildPaymentController();
        InstallmentController installmentController = buildInstallmentController();
        InvoiceController invoiceController = buildInvoiceController();
        PromotionController promotionController = buildPromotionController();
        showLoginFrame(authController, dashboardService, carManagementController, branchManagementController,
                customerManagementController, staffManagementController, saleOrderController, paymentController, installmentController, invoiceController, promotionController);
    }
}