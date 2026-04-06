package vn.edu.ute.carsalesms.view.component;

import vn.edu.ute.carsalesms.controller.StaffManagementController;
import vn.edu.ute.carsalesms.model.dto.AccountCommandRequest;
import vn.edu.ute.carsalesms.model.dto.AccountItem;
import vn.edu.ute.carsalesms.model.dto.CarLookupItem;
import vn.edu.ute.carsalesms.model.dto.StaffCommandRequest;
import vn.edu.ute.carsalesms.model.dto.StaffItem;
import vn.edu.ute.carsalesms.model.dto.StaffManagementMetadata;
import vn.edu.ute.carsalesms.model.enums.StaffRole;
import vn.edu.ute.carsalesms.model.enums.Status;
import vn.edu.ute.carsalesms.view.theme.DialogUiUtil;
import vn.edu.ute.carsalesms.view.theme.UiPalette;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Panel quản lý nhân viên và tài khoản đăng nhập – Module F05.
 *
 * Cấu trúc:
 *  - Hai tab: "Nhân viên" và "Tài khoản".
 *  - Tab Nhân viên: Cung cấp các chức năng CRUD (Thêm, Sửa, Ngừng hoạt động) cho nhân viên.
 *  - Tab Tài khoản: Quản lý tài khoản đăng nhập của nhân viên (Tạo, Sửa, Khóa, Xóa).
 *
 * Inner classes (Lớp nội):
 *  - StaffTabPanel: Lớp nội private final, quản lý giao diện và logic cho tab "Nhân viên".
 *  - AccountTabPanel: Lớp nội private final, quản lý giao diện và logic cho tab "Tài khoản".
 *  - StaffEditorDialog: Lớp nội static final, là dialog để thêm hoặc sửa thông tin nhân viên.
 *  - AccountEditorDialog: Lớp nội static final, là dialog để thêm hoặc sửa tài khoản đăng nhập.
 *
 * Thiết kế tuân thủ các nguyên tắc SOLID:
 *  - SRP (Single Responsibility Principle): Mỗi lớp nội chỉ chịu trách nhiệm cho một chức năng duy nhất (một tab hoặc một dialog).
 *  - OCP (Open/Closed Principle): Có thể mở rộng chức năng bằng cách thêm các lớp con mới mà không cần sửa đổi mã nguồn của panel gốc.
 *  - DIP (Dependency Inversion Principle): Controller được inject thông qua constructor, giúp giảm sự phụ thuộc cứng.
 */
public class StaffManagementPanel extends JPanel {

    /** Định dạng ngày tháng để hiển thị trong giao diện. */
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Controller xử lý các yêu cầu nghiệp vụ từ view. */
    private final StaffManagementController controller;

    /** Hai panel con tương ứng với hai tab. */
    private final StaffTabPanel staffTabPanel;
    private final AccountTabPanel accountTabPanel;

    /**
     * Khởi tạo panel quản lý nhân viên với hai tab.
     *
     * @param controller Controller quản lý nhân viên và tài khoản (không được null).
     */
    public StaffManagementPanel(StaffManagementController controller) {
        this.controller = Objects.requireNonNull(controller, "controller is required");

        setLayout(new BorderLayout());
        setOpaque(false);

        staffTabPanel   = new StaffTabPanel();
        accountTabPanel = new AccountTabPanel();

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Nhân viên",   staffTabPanel);
        tabs.addTab("Tài khoản",   accountTabPanel);

        // Khi người dùng chuyển tab, tải lại dữ liệu cho tab được chọn.
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 0) staffTabPanel.refreshData();
            else accountTabPanel.refreshData();
        });

        add(tabs, BorderLayout.CENTER);
    }

    // ─── Các phương thức trợ giúp tạo UI (dùng chung cho cả hai tab) ──────────

    /**
     * Tạo một nút hành động với màu xanh và phong cách nhất quán.
     */
    private JButton createActionButton(String title) {
        JButton btn = new JButton(title);
        btn.setFocusPainted(false);
        btn.setBackground(UiPalette.ACTION_BG);
        btn.setForeground(UiPalette.ACTION_FG);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiPalette.PRIMARY_BORDER),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return btn;
    }

    /** Tạo một nút hành động nguy hiểm (màu đỏ) cho các thao tác xóa hoặc khóa. */
    private JButton createDangerButton(String title) {
        JButton btn = createActionButton(title);
        btn.setForeground(UiPalette.DANGER);
        return btn;
    }

    /** Bọc một JTable trong một card có đường viền nhất quán với toàn bộ ứng dụng. */
    private JPanel createTableCard(JTable tbl) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UiPalette.SURFACE_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiPalette.BORDER_SOFT),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        tbl.setRowHeight(28);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tbl.setGridColor(UiPalette.BORDER_SOFT);
        tbl.setBackground(UiPalette.TABLE_BACKGROUND);
        tbl.setSelectionBackground(UiPalette.PRIMARY_SOFT);
        tbl.setSelectionForeground(UiPalette.TEXT_PRIMARY);
        tbl.getTableHeader().setBackground(UiPalette.PRIMARY_SOFT);
        tbl.getTableHeader().setForeground(UiPalette.TEXT_PRIMARY);
        tbl.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        tbl.setFillsViewportHeight(true);
        tbl.getTableHeader().setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(tbl);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // Các phương thức hiển thị hộp thoại thông báo
    private void showError(String msg) {
        JOptionPane.showMessageDialog(getAppDialogParent(), msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(getAppDialogParent(), msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private int showConfirm(String message, String title, int optionType) {
        return JOptionPane.showConfirmDialog(getAppDialogParent(), message, title, optionType);
    }

    private int showConfirm(String message, String title, int optionType, int messageType) {
        return JOptionPane.showConfirmDialog(getAppDialogParent(), message, title, optionType, messageType);
    }

    private Component getAppDialogParent() {
        Component owner = DialogUiUtil.appDialogParent(this);
        return owner != null ? owner : this;
    }

    private Window getAppDialogWindow() {
        Component owner = getAppDialogParent();
        if (owner instanceof Window) {
            return (Window) owner;
        }
        return SwingUtilities.getWindowAncestor(owner);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Tab 1: Nhân viên
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Panel nội dung cho tab "Nhân viên".
     * Chịu trách nhiệm duy nhất cho việc hiển thị và thực hiện các thao tác CRUD đối với nhân viên.
     */
    private final class StaffTabPanel extends JPanel {

        private static final String[] COLUMNS = {
                "Mã NV", "Họ tên", "Email", "Điện thoại",
                "Vai trò", "Chi nhánh", "Trạng thái", "Có TK", "Ngày tạo"
        };

        private final JTextField searchField = new JTextField();
        private final JComboBox<String> statusFilter =
                new JComboBox<>(new String[]{"Tất cả", "ACTIVE", "INACTIVE"});

        private final DefaultTableModel tableModel;
        private final JTable table;
        private final TableRowSorter<DefaultTableModel> sorter;
        private List<StaffItem> rows = new ArrayList<>();

        /** Dữ liệu metadata (ví dụ: danh sách chi nhánh) để sử dụng trong dialog. */
        private StaffManagementMetadata metadata = StaffManagementMetadata.empty();

        private StaffTabPanel() {
            setLayout(new BorderLayout(0, 8));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

            tableModel = new DefaultTableModel(COLUMNS, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            table = new JTable(tableModel);
            sorter = new TableRowSorter<>(tableModel);
            table.setRowSorter(sorter);
            configureColumns();

            add(buildToolbar(), BorderLayout.NORTH);
            add(createTableCard(table), BorderLayout.CENTER);

            reloadMetadata();
            refreshData();
        }

        /** Xây dựng thanh công cụ: tìm kiếm bên trái, các nút CRUD bên phải. */
        private JPanel buildToolbar() {
            JPanel panel = new JPanel(new BorderLayout(8, 0));
            panel.setOpaque(false);

            // Phần bên trái
            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            left.setOpaque(false);
            searchField.setPreferredSize(new Dimension(220, 30));
            statusFilter.setPreferredSize(new Dimension(110, 30));

            JButton searchBtn = createActionButton("Tìm");
            searchBtn.addActionListener(e -> refreshData());
            // Lọc theo bộ lọc trạng thái tự động mà không cần nhấn nút "Tìm".
            statusFilter.addActionListener(e -> refreshData());

            left.add(new JLabel("Tìm kiếm:"));
            left.add(searchField);
            left.add(searchBtn);
            left.add(new JLabel("Trạng thái:"));
            left.add(statusFilter);

            // Phần bên phải
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            right.setOpaque(false);

            JButton refreshBtn    = createActionButton("Làm mới");
            JButton addBtn        = createActionButton("Thêm NV");
            JButton editBtn       = createActionButton("Sửa");
            JButton deactivateBtn = createDangerButton("Ngừng HĐ");

            refreshBtn.addActionListener(e -> refreshData());
            addBtn.addActionListener(e -> showEditor(null));
            editBtn.addActionListener(e -> selectedStaff()
                    .ifPresentOrElse(this::showEditor, () -> showInfo("Vui lòng chọn nhân viên cần sửa.")));
            deactivateBtn.addActionListener(e -> selectedStaff()
                    .ifPresentOrElse(this::deactivateStaff, () -> showInfo("Vui lòng chọn nhân viên cần ngừng hoạt động.")));

            right.add(refreshBtn);
            right.add(addBtn);
            right.add(editBtn);
            right.add(deactivateBtn);

            panel.add(left, BorderLayout.WEST);
            panel.add(right, BorderLayout.EAST);
            return panel;
        }

        /**
         * Tải lại danh sách nhân viên, sử dụng Stream API để map từ entity sang dữ liệu cho bảng.
         */
        private void refreshData() {
            try {
                rows = controller.loadStaffs(searchField.getText(), parseStatusFilter());
                tableModel.setRowCount(0);
                rows.stream()
                        .map(s -> new Object[]{
                                s.staffCode(),
                                s.fullName(),
                                s.email(),
                                s.phone(),
                                s.role().name(),
                                s.branchName(),
                                s.status().name(),
                                s.hasAccount() ? "Có" : "Không",
                                s.createdAt() == null ? "" : s.createdAt().toLocalDate()
                                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        })
                        .forEach(tableModel::addRow);
            } catch (Exception ex) {
                showError("Lỗi tải danh sách nhân viên: " + ex.getMessage());
            }
        }

        /** Tải lại metadata khi mở dialog (cần danh sách chi nhánh mới nhất). */
        private void reloadMetadata() {
            try {
                metadata = controller.loadMetadata();
            } catch (Exception ex) {
                metadata = StaffManagementMetadata.empty();
                showError("Không tải được dữ liệu danh sách chi nhánh.");
            }
        }

        /** Chuyển đổi giá trị từ combobox filter sang enum Status. Null có nghĩa là "tất cả". */
        private Status parseStatusFilter() {
            String raw = (String) statusFilter.getSelectedItem();
            if (raw == null || raw.equalsIgnoreCase("Tất cả")) return null;
            return Status.valueOf(raw);
        }

        /** Lấy nhân viên đang được chọn trong bảng. */
        private Optional<StaffItem> selectedStaff() {
            int view = table.getSelectedRow();
            if (view < 0) return Optional.empty();
            int model = table.convertRowIndexToModel(view);
            if (model < 0 || model >= rows.size()) return Optional.empty();
            return Optional.of(rows.get(model));
        }

        /** Mở dialog để thêm hoặc sửa nhân viên. */
        private void showEditor(StaffItem existing) {
            reloadMetadata();
            StaffEditorDialog dialog = new StaffEditorDialog(
                    getAppDialogWindow(), metadata, existing);
            dialog.setVisible(true);

            dialog.getResult().ifPresent(request -> {
                try {
                    if (existing == null) {
                        controller.createStaff(request);
                        showInfo("Thêm nhân viên thành công.");
                    } else {
                        controller.updateStaff(request);
                        showInfo("Cập nhật nhân viên thành công.");
                    }
                    refreshData();
                } catch (Exception ex) {
                    showError(ex.getMessage());
                }
            });
        }

        /** Đặt trạng thái của nhân viên thành INACTIVE (ngừng hoạt động). */
        private void deactivateStaff(StaffItem item) {
            int confirm = showConfirm(
                    "Xác nhận ngừng hoạt động nhân viên: " + item.fullName() + " (" + item.staffCode() + ")?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) return;

            try {
                // Tạo một request cập nhật chỉ để thay đổi trạng thái.
                StaffCommandRequest request = new StaffCommandRequest(
                        item.id(),
                        item.staffCode(),
                        item.fullName(),
                        item.email(),
                        item.phone(),
                        item.role(),
                        item.branchId(),
                        Status.INACTIVE
                );
                controller.updateStaff(request);
                showInfo("Đã ngừng hoạt động nhân viên: " + item.fullName());
                refreshData();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        }

        /** Cấu hình chiều rộng cột và căn chỉnh văn bản. */
        private void configureColumns() {
            table.getColumnModel().getColumn(0).setPreferredWidth(80);
            table.getColumnModel().getColumn(1).setPreferredWidth(160);
            table.getColumnModel().getColumn(2).setPreferredWidth(160);
            table.getColumnModel().getColumn(3).setPreferredWidth(110);
            table.getColumnModel().getColumn(4).setPreferredWidth(80);
            table.getColumnModel().getColumn(5).setPreferredWidth(130);
            table.getColumnModel().getColumn(6).setPreferredWidth(90);
            table.getColumnModel().getColumn(7).setPreferredWidth(60);
            table.getColumnModel().getColumn(8).setPreferredWidth(90);

            DefaultTableCellRenderer center = new DefaultTableCellRenderer();
            center.setHorizontalAlignment(SwingConstants.CENTER);
            table.getColumnModel().getColumn(4).setCellRenderer(center); // Vai trò
            table.getColumnModel().getColumn(6).setCellRenderer(center); // Trạng thái
            table.getColumnModel().getColumn(7).setCellRenderer(center); // Có TK
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Tab 2: Tài khoản
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Panel nội dung cho tab "Tài khoản".
     * Hiển thị và quản lý các tài khoản đăng nhập (quan hệ 1-1 với Nhân viên).
     * Hỗ trợ các chức năng: Tạo, Sửa, Khóa, Xóa tài khoản.
     */
    private final class AccountTabPanel extends JPanel {

        private static final String[] COLUMNS = {
                "ID", "Mã NV", "Nhân viên", "Username",
                "Trạng thái", "Khóa", "Đăng nhập lỗi", "Đăng nhập cuối", "Ngày tạo"
        };

        private final JTextField searchField = new JTextField();
        private final DefaultTableModel tableModel;
        private final JTable table;
        private final TableRowSorter<DefaultTableModel> sorter;
        private List<AccountItem> rows = new ArrayList<>();

        private AccountTabPanel() {
            setLayout(new BorderLayout(0, 8));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

            tableModel = new DefaultTableModel(COLUMNS, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            table = new JTable(tableModel);
            sorter = new TableRowSorter<>(tableModel);
            table.setRowSorter(sorter);
            configureColumns();

            add(buildToolbar(), BorderLayout.NORTH);
            add(createTableCard(table), BorderLayout.CENTER);
            refreshData();
        }

        private JPanel buildToolbar() {
            JPanel panel = new JPanel(new BorderLayout(8, 0));
            panel.setOpaque(false);

            // Phần bên trái: tìm kiếm
            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            left.setOpaque(false);
            searchField.setPreferredSize(new Dimension(240, 30));

            JButton searchBtn = createActionButton("Tìm");
            searchBtn.addActionListener(e -> refreshData());

            left.add(new JLabel("Tìm kiếm:"));
            left.add(searchField);
            left.add(searchBtn);

            // Phần bên phải: các nút CRUD và Khóa
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            right.setOpaque(false);

            JButton refreshBtn = createActionButton("Làm mới");
            JButton addBtn     = createActionButton("Tạo TK");
            JButton editBtn    = createActionButton("Sửa");
            JButton resetPwdBtn = createActionButton("Đặt lại MK");
            JButton lockBtn    = createDangerButton("Khóa/Mở");
            JButton deleteBtn  = createDangerButton("Xóa TK");

            refreshBtn.addActionListener(e -> refreshData());
            addBtn.addActionListener(e -> showAccountEditor(null));
            editBtn.addActionListener(e -> selectedAccount()
                    .ifPresentOrElse(this::showAccountEditor,
                            () -> showInfo("Vui lòng chọn tài khoản cần sửa.")));
            resetPwdBtn.addActionListener(e -> selectedAccount()
                    .ifPresentOrElse(this::showResetPasswordDialog,
                            () -> showInfo("Vui lòng chọn tài khoản cần đặt lại mật khẩu.")));
            // Nút Khóa/Mở: chuyển đổi trạng thái khóa của tài khoản.
            lockBtn.addActionListener(e -> selectedAccount()
                    .ifPresentOrElse(this::toggleLock,
                            () -> showInfo("Vui lòng chọn tài khoản cần khóa/mở.")));
            deleteBtn.addActionListener(e -> selectedAccount()
                    .ifPresentOrElse(this::deleteAccount,
                            () -> showInfo("Vui lòng chọn tài khoản cần xóa.")));

            right.add(refreshBtn);
            right.add(addBtn);
            right.add(editBtn);
            right.add(resetPwdBtn);
            right.add(lockBtn);
            right.add(deleteBtn);

            panel.add(left, BorderLayout.WEST);
            panel.add(right, BorderLayout.EAST);
            return panel;
        }

        /**
         * Tải danh sách tài khoản từ service, sử dụng Stream để map sang dữ liệu cho bảng.
         */
        private void refreshData() {
            try {
                rows = controller.loadAccounts(searchField.getText());
                tableModel.setRowCount(0);
                rows.stream()
                        .map(a -> new Object[]{
                                a.id(),
                                a.staffCode(),
                                a.staffFullName(),
                                a.username(),
                                a.status().name(),
                                a.locked() ? "Khóa" : "Mở",
                                a.failedLoginAttempts(),
                                a.lastLoginAt() == null ? "—" : a.lastLoginAt().format(DATE_FMT),
                                a.createdAt() == null ? "" : a.createdAt().toLocalDate()
                                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        })
                        .forEach(tableModel::addRow);
            } catch (Exception ex) {
                showError("Lỗi tải danh sách tài khoản: " + ex.getMessage());
            }
        }

        private Optional<AccountItem> selectedAccount() {
            int view = table.getSelectedRow();
            if (view < 0) return Optional.empty();
            int model = table.convertRowIndexToModel(view);
            if (model < 0 || model >= rows.size()) return Optional.empty();
            return Optional.of(rows.get(model));
        }

        /** Mở dialog để tạo hoặc sửa tài khoản. */
        private void showAccountEditor(AccountItem existing) {
            // Lấy danh sách nhân viên chưa có tài khoản để điền vào combobox khi tạo mới.
            AccountEditorDialog dialog = new AccountEditorDialog(
                    getAppDialogWindow(), existing,
                    controller.loadStaffsPendingAccount());
            dialog.setVisible(true);

            dialog.getResult().ifPresent(request -> {
                try {
                    if (existing == null) {
                        controller.createAccount(request);
                        dialog.getCreateSummary().ifPresentOrElse(summary ->
                                        showCenteredInfoDialog("<html><div style='width:430px; text-align:left;'>"
                                                + "Đã tạo tài khoản thành công cho nhân viên <b>"
                                                + summary.role() + " - " + summary.fullName() + "</b>.<br/><br/>"
                                                + "<b>Username:</b> " + summary.username() + "<br/>"
                                                + "<b>Mật khẩu:</b> " + summary.rawPassword()
                                                + "</div></html>"),
                                () -> showCenteredInfoDialog("Tạo tài khoản thành công."));
                    } else {
                        controller.updateAccount(request);
                        showInfo("Cập nhật tài khoản thành công.");
                    }
                    refreshData();
                } catch (Exception ex) {
                    showError(ex.getMessage());
                }
            });
        }

        /** Mở dialog để đặt lại mật khẩu cho tài khoản đã được chọn. */
        private void showResetPasswordDialog(AccountItem item) {
            ResetPasswordDialog dialog = new ResetPasswordDialog(
                    getAppDialogWindow(),
                    item);
            dialog.setVisible(true);

            dialog.getResult().ifPresent(newPassword -> {
                int confirm = showConfirm(
                        "Xác nhận đặt lại mật khẩu cho nhân viên "
                                + item.staffCode() + " - " + item.staffFullName() + "?",
                        "Xác nhận đặt lại mật khẩu",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }

                try {
                    controller.updateAccount(new AccountCommandRequest(
                            item.id(),
                            item.staffId(),
                            item.username(),
                            newPassword,
                            item.status()
                    ));
                    showCenteredInfoDialog("<html><div style='width:430px; text-align:left;'>"
                            + "Đã đặt lại mật khẩu cho nhân viên <b>" + item.staffCode()
                            + " - " + item.staffFullName() + "</b>.<br/><br/>"
                            + "<b>Username:</b> " + item.username() + "<br/>"
                            + "<b>Mật khẩu mới:</b> " + newPassword
                            + "</div></html>");
                    refreshData();
                } catch (Exception ex) {
                    showError(ex.getMessage());
                }
            });
        }

        /** Hiển thị một thông báo ở chính giữa cửa sổ ứng dụng. */
        private void showCenteredInfoDialog(String message) {
            JOptionPane optionPane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE);
            Window owner = getAppDialogWindow();
            JDialog dialog = optionPane.createDialog(owner, "Thông báo");
            dialog.setLocationRelativeTo(owner);
            dialog.setVisible(true);
        }

        /**
         * Chuyển đổi trạng thái khóa/mở của tài khoản với hộp thoại xác nhận.
         *
         * @param item Tài khoản cần thực hiện thao tác.
         */
        private void toggleLock(AccountItem item) {
            boolean willLock = !item.locked();
            String action = willLock ? "khóa" : "mở khóa";
            int confirm = showConfirm(
                    "Xác nhận " + action + " tài khoản: " + item.username() + "?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) return;

            try {
                AccountItem updated = controller.toggleLock(item.id());
                String result = updated.locked() ? "Đã khóa" : "Đã mở khóa";
                showInfo(result + " tài khoản: " + updated.username());
                refreshData();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        }

        /** Xóa tài khoản với hộp thoại xác nhận. */
        private void deleteAccount(AccountItem item) {
            int confirm = showConfirm(
                    "Xác nhận xóa tài khoản: " + item.username() + " (NV: " + item.staffFullName() + ")?",
                    "Xác nhận xóa tài khoản",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (confirm != JOptionPane.YES_OPTION) return;

            try {
                controller.deleteAccount(item.id());
                showInfo("Đã xóa tài khoản: " + item.username());
                refreshData();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        }

        private void configureColumns() {
            // Ẩn cột ID (chỉ dùng nội bộ).
            table.getColumnModel().getColumn(0).setMinWidth(0);
            table.getColumnModel().getColumn(0).setMaxWidth(0);
            table.getColumnModel().getColumn(0).setWidth(0);

            table.getColumnModel().getColumn(1).setPreferredWidth(80);
            table.getColumnModel().getColumn(2).setPreferredWidth(150);
            table.getColumnModel().getColumn(3).setPreferredWidth(140);
            table.getColumnModel().getColumn(4).setPreferredWidth(90);
            table.getColumnModel().getColumn(5).setPreferredWidth(80);
            table.getColumnModel().getColumn(6).setPreferredWidth(80);
            table.getColumnModel().getColumn(7).setPreferredWidth(130);
            table.getColumnModel().getColumn(8).setPreferredWidth(90);

            DefaultTableCellRenderer center = new DefaultTableCellRenderer();
            center.setHorizontalAlignment(SwingConstants.CENTER);
            // Căn giữa các cột: Trạng thái, Khóa, Lỗi đăng nhập.
            for (int col : new int[]{4, 5, 6}) {
                table.getColumnModel().getColumn(col).setCellRenderer(center);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Dialog: Thêm / Sửa Nhân viên
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Dialog modal để nhập thông tin nhân viên.
     * Là lớp static để không giữ tham chiếu đến panel bên ngoài.
     */
    private static final class StaffEditorDialog extends JDialog {

        private final JTextField codeField       = new JTextField();
        private final JTextField fullNameField    = new JTextField();
        private final JTextField emailField       = new JTextField();
        private final JTextField phoneField       = new JTextField();
        private final JComboBox<StaffRole> roleCombo =
                new JComboBox<>(StaffRole.values());
        private final JComboBox<CarLookupItem> branchCombo = new JComboBox<>();
        private final JComboBox<Status> statusCombo =
                new JComboBox<>(Status.values());

        private StaffCommandRequest result;
        private final Long editingId;

        private StaffEditorDialog(Window owner, StaffManagementMetadata metadata, StaffItem existing) {
            super(owner,
                    existing == null ? "Thêm nhân viên" : "Sửa nhân viên",
                    ModalityType.APPLICATION_MODAL);
            this.editingId = existing == null ? null : existing.id();

            setResizable(false);
            setLayout(new BorderLayout(0, 8));

            // Điền danh sách chi nhánh vào combobox.
            metadata.branches().forEach(branchCombo::addItem);

            JPanel form = new JPanel(new GridLayout(7, 2, 8, 6));
            form.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));

            form.add(new JLabel("Mã nhân viên *"));  form.add(codeField);
            form.add(new JLabel("Họ tên *"));         form.add(fullNameField);
            form.add(new JLabel("Email"));             form.add(emailField);
            form.add(new JLabel("Điện thoại"));        form.add(phoneField);
            form.add(new JLabel("Vai trò *"));         form.add(roleCombo);
            form.add(new JLabel("Chi nhánh *"));       form.add(branchCombo);
            form.add(new JLabel("Trạng thái"));        form.add(statusCombo);

            // Điền dữ liệu cũ nếu đang ở chế độ sửa.
            if (existing != null) {
                codeField.setText(existing.staffCode());
                codeField.setEditable(true);
                fullNameField.setText(existing.fullName());
                emailField.setText(existing.email());
                phoneField.setText(existing.phone());
                roleCombo.setSelectedItem(existing.role());
                statusCombo.setSelectedItem(existing.status());
                // Chọn chi nhánh khớp với branchId.
                selectBranchById(existing.branchId());
            } else {
                codeField.setText(metadata.nextStaffCode());
                codeField.setEditable(false);
                statusCombo.setSelectedItem(Status.ACTIVE);
            }

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
            JButton cancelBtn = new JButton("Hủy");
            JButton saveBtn   = new JButton("Lưu");
            saveBtn.setBackground(UiPalette.PRIMARY);
            saveBtn.setForeground(Color.WHITE);
            saveBtn.setFocusPainted(false);

            cancelBtn.addActionListener(e -> dispose());
            saveBtn.addActionListener(e -> onSave());

            actions.add(saveBtn);
            actions.add(cancelBtn);

            add(form, BorderLayout.CENTER);
            add(actions, BorderLayout.SOUTH);

            pack();
            setMinimumSize(new Dimension(460, 340));
            setSize(getMinimumSize());
            setLocationRelativeTo(owner);
        }

        /** Đọc dữ liệu từ form, tạo request hoặc hiển thị lỗi xác thực. */
        private void onSave() {
            if (codeField.getText().isBlank() || fullNameField.getText().isBlank()) {
                JOptionPane.showMessageDialog(DialogUiUtil.appDialogParent(this),
                        "Vui lòng điền đủ các trường bắt buộc (*).",
                        "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }
            CarLookupItem branch = (CarLookupItem) branchCombo.getSelectedItem();
            if (branch == null) {
                JOptionPane.showMessageDialog(DialogUiUtil.appDialogParent(this),
                        "Vui lòng chọn chi nhánh.",
                        "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }
            result = new StaffCommandRequest(
                    editingId,
                    codeField.getText().trim().toUpperCase(),
                    fullNameField.getText().trim(),
                    emailField.getText().trim().isEmpty() ? null : emailField.getText().trim(),
                    phoneField.getText().trim().isEmpty() ? null : phoneField.getText().trim(),
                    (StaffRole) roleCombo.getSelectedItem(),
                    branch.id(),
                    (Status) statusCombo.getSelectedItem()
            );
            dispose();
        }

        /** Chọn một chi nhánh trong combobox dựa trên branchId. */
        private void selectBranchById(Long branchId) {
            for (int i = 0; i < branchCombo.getItemCount(); i++) {
                if (branchCombo.getItemAt(i).id().equals(branchId)) {
                    branchCombo.setSelectedIndex(i);
                    return;
                }
            }
        }

        private Optional<StaffCommandRequest> getResult() {
            return Optional.ofNullable(result);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Dialog: Tạo / Sửa Tài khoản
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Dialog modal để nhập thông tin tài khoản đăng nhập.
     * Khi tạo mới: cho phép chọn nhân viên và nhập mật khẩu.
     * Khi sửa: chỉ cho phép thay đổi username, mật khẩu (để trống để giữ nguyên), và trạng thái.
     */
    private static final class AccountEditorDialog extends JDialog {

        private record CreateAccountSummary(String role, String fullName, String username, String rawPassword) {}

        /** ComboBox để chọn nhân viên (chỉ hiển thị khi tạo mới). */
        private final JComboBox<StaffItem> staffCombo = new JComboBox<>();
        private final JTextField usernameField = new JTextField();
        /** Để trống khi sửa có nghĩa là giữ nguyên mật khẩu cũ. */
        private final JPasswordField passwordField = new JPasswordField();
        private final JCheckBox showPasswordCheck = new JCheckBox("Hiện");
        private final JComboBox<Status> statusCombo = new JComboBox<>(Status.values());

        private AccountCommandRequest result;
        private CreateAccountSummary createSummary;
        private final Long editingId;
        private final char defaultEchoChar;

        private AccountEditorDialog(Window owner, AccountItem existing, List<StaffItem> allStaffs) {
            super(owner,
                    existing == null ? "Tạo tài khoản" : "Sửa tài khoản",
                    ModalityType.APPLICATION_MODAL);
            this.editingId = existing == null ? null : existing.id();

            setResizable(false);
            setLayout(new BorderLayout(0, 8));
            defaultEchoChar = passwordField.getEchoChar();

            JPanel form = new JPanel(new GridLayout(4, 2, 8, 6));
            form.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));

            // Hiển thị một label ngắn gọn thay vì chuỗi dài từ StaffItem.toString().
            staffCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list,
                                                              Object value,
                                                              int index,
                                                              boolean isSelected,
                                                              boolean cellHasFocus) {
                    JLabel label = (JLabel) super.getListCellRendererComponent(
                            list, value, index, isSelected, cellHasFocus);
                    if (value instanceof StaffItem staff) {
                        label.setText(staff.fullName() + " (" + staff.staffCode() + ")");
                    }
                    return label;
                }
            });

            if (existing == null) {
                // Tạo mới: điền vào combobox các nhân viên chưa có tài khoản.
                allStaffs.stream()
                        .filter(s -> !s.hasAccount()) // Chỉ những nhân viên chưa có tài khoản.
                        .forEach(staffCombo::addItem);
                staffCombo.addActionListener(e -> populateDefaultUsername());
                form.add(new JLabel("Nhân viên *"));
                form.add(staffCombo);
            } else {
                // Sửa: hiển thị thông tin nhân viên (chỉ đọc).
                JLabel staffLabel = new JLabel(existing.staffFullName() + " (" + existing.staffCode() + ")");
                staffLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
                form.add(new JLabel("Nhân viên"));
                form.add(staffLabel);
            }

            passwordField.setToolTipText(existing != null ? "Để trống = giữ mật khẩu cũ" : "Bắt buộc");
            showPasswordCheck.setOpaque(false);
            showPasswordCheck.addActionListener(e -> applyPasswordVisibility());

            JPanel passwordWrapper = new JPanel(new BorderLayout(6, 0));
            passwordWrapper.setOpaque(false);
            passwordWrapper.add(passwordField, BorderLayout.CENTER);
            passwordWrapper.add(showPasswordCheck, BorderLayout.EAST);

            form.add(new JLabel("Tên đăng nhập *")); form.add(usernameField);
            form.add(new JLabel(existing != null ? "Mật khẩu mới (tùy chọn)" : "Mật khẩu *"));
            form.add(passwordWrapper);
            form.add(new JLabel("Trạng thái"));       form.add(statusCombo);

            // Điền dữ liệu cũ nếu đang sửa.
            if (existing != null) {
                usernameField.setText(existing.username());
                statusCombo.setSelectedItem(existing.status());
                showPasswordCheck.setSelected(false);
            } else {
                statusCombo.setSelectedItem(Status.ACTIVE);
                passwordField.setText(generateRandomPassword(8));
                populateDefaultUsername();
                showPasswordCheck.setSelected(true);
            }
            applyPasswordVisibility();

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
            JButton cancelBtn = new JButton("Hủy");
            JButton saveBtn   = new JButton("Lưu");
            saveBtn.setBackground(UiPalette.PRIMARY);
            saveBtn.setForeground(Color.WHITE);
            saveBtn.setFocusPainted(false);

            cancelBtn.addActionListener(e -> dispose());
            saveBtn.addActionListener(e -> onSave(existing));

            actions.add(saveBtn);
            actions.add(cancelBtn);

            add(form, BorderLayout.CENTER);
            add(actions, BorderLayout.SOUTH);

            pack();
            setMinimumSize(new Dimension(420, 260));
            setSize(getMinimumSize());
            setLocationRelativeTo(owner);
        }

        /** Đọc dữ liệu từ form và tạo một AccountCommandRequest. */
        private void onSave(AccountItem existing) {
            if (usernameField.getText().isBlank()) {
                JOptionPane.showMessageDialog(DialogUiUtil.appDialogParent(this),
                        "Vui lòng nhập tên đăng nhập.",
                        "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String rawPassword = new String(passwordField.getPassword()).trim();

            // Khi tạo mới, mật khẩu là bắt buộc.
            if (existing == null && rawPassword.isBlank()) {
                JOptionPane.showMessageDialog(DialogUiUtil.appDialogParent(this),
                        "Vui lòng nhập mật khẩu.",
                        "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Long staffId;
            if (existing == null) {
                // Lấy ID nhân viên từ combobox.
                StaffItem selectedStaff = (StaffItem) staffCombo.getSelectedItem();
                if (selectedStaff == null) {
                    JOptionPane.showMessageDialog(DialogUiUtil.appDialogParent(this),
                            "Vui lòng chọn nhân viên.",
                            "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                staffId = selectedStaff.id();
                createSummary = new CreateAccountSummary(
                        selectedStaff.role().name(),
                        selectedStaff.fullName(),
                        usernameField.getText().trim().toLowerCase(),
                        rawPassword
                );
            } else {
                staffId = existing.staffId();
                createSummary = null;
            }

            result = new AccountCommandRequest(
                    editingId,
                    staffId,
                    usernameField.getText().trim().toLowerCase(),
                    rawPassword.isEmpty() ? null : rawPassword,
                    (Status) statusCombo.getSelectedItem()
            );
            dispose();
        }

        private Optional<AccountCommandRequest> getResult() {
            return Optional.ofNullable(result);
        }

        private Optional<CreateAccountSummary> getCreateSummary() {
            return Optional.ofNullable(createSummary);
        }

        private void populateDefaultUsername() {
            StaffItem selected = (StaffItem) staffCombo.getSelectedItem();
            if (selected == null) {
                usernameField.setText("");
                return;
            }
            String email = selected.email();
            String fallbackUsername = selected.staffCode() == null ? "" : selected.staffCode().toLowerCase();
            String defaultUsername = (email == null || email.isBlank())
                    ? fallbackUsername
                    : email.trim().toLowerCase();
            usernameField.setText(defaultUsername);
        }

        private String generateRandomPassword(int length) {
            final String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                int idx = ThreadLocalRandom.current().nextInt(charset.length());
                sb.append(charset.charAt(idx));
            }
            return sb.toString();
        }

        private void applyPasswordVisibility() {
            passwordField.setEchoChar(showPasswordCheck.isSelected() ? (char) 0 : defaultEchoChar);
        }
    }

    /** Dialog để đặt lại mật khẩu cho một tài khoản hiện có. */
    private static final class ResetPasswordDialog extends JDialog {

        private final JLabel targetLabel = new JLabel();
        private final JTextField usernameField = new JTextField();
        private final JPasswordField passwordField = new JPasswordField();
        private final JCheckBox showPasswordCheck = new JCheckBox("Hiện");
        private final char defaultEchoChar;

        private String result;

        private ResetPasswordDialog(Window owner, AccountItem account) {
            super(owner, "Đặt lại mật khẩu", ModalityType.APPLICATION_MODAL);
            setResizable(false);
            setLayout(new BorderLayout(0, 8));

            defaultEchoChar = passwordField.getEchoChar();

            JPanel form = new JPanel(new GridLayout(3, 2, 8, 6));
            form.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));

            targetLabel.setText(account.staffCode() + " - " + account.staffFullName());
            targetLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

            usernameField.setText(account.username());
            usernameField.setEditable(false);

            passwordField.setText(generateRandomPassword(8));
            showPasswordCheck.setOpaque(false);
            showPasswordCheck.setSelected(true);
            showPasswordCheck.addActionListener(e -> applyPasswordVisibility());

            JPanel passwordWrapper = new JPanel(new BorderLayout(6, 0));
            passwordWrapper.setOpaque(false);
            passwordWrapper.add(passwordField, BorderLayout.CENTER);
            passwordWrapper.add(showPasswordCheck, BorderLayout.EAST);

            form.add(new JLabel("Đặt lại cho"));
            form.add(targetLabel);
            form.add(new JLabel("Username"));
            form.add(usernameField);
            form.add(new JLabel("Mật khẩu mới *"));
            form.add(passwordWrapper);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
            JButton cancelBtn = new JButton("Hủy");
            JButton saveBtn = new JButton("Lưu");
            saveBtn.setBackground(UiPalette.PRIMARY);
            saveBtn.setForeground(Color.WHITE);
            saveBtn.setFocusPainted(false);

            cancelBtn.addActionListener(e -> dispose());
            saveBtn.addActionListener(e -> onSave());

            actions.add(saveBtn);
            actions.add(cancelBtn);

            add(form, BorderLayout.CENTER);
            add(actions, BorderLayout.SOUTH);

            applyPasswordVisibility();
            pack();
            setMinimumSize(new Dimension(460, 230));
            setSize(getMinimumSize());
            setLocationRelativeTo(owner);
        }

        private void onSave() {
            String password = new String(passwordField.getPassword()).trim();
            if (password.isBlank()) {
                JOptionPane.showMessageDialog(DialogUiUtil.appDialogParent(this),
                        "Vui lòng nhập mật khẩu mới.",
                        "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }
            result = password;
            dispose();
        }

        private Optional<String> getResult() {
            return Optional.ofNullable(result);
        }

        private void applyPasswordVisibility() {
            passwordField.setEchoChar(showPasswordCheck.isSelected() ? (char) 0 : defaultEchoChar);
        }

        private String generateRandomPassword(int length) {
            final String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                int idx = ThreadLocalRandom.current().nextInt(charset.length());
                sb.append(charset.charAt(idx));
            }
            return sb.toString();
        }
    }
}
