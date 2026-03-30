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

/**
 * Panel quản lý nhân viên và tài khoản đăng nhập – Module F05.
 *
 * Cấu trúc:
 *  - Hai tab: "Nhân viên" và "Tài khoản"
 *  - Tab Nhân viên  : CRUD nhân viên (Thêm / Sửa / Ngừng hoạt động)
 *  - Tab Tài khoản  : Tạo / Sửa / Khóa / Xóa tài khoản đăng nhập
 *
 * Inner classes:
 *  - StaffTabPanel    – nội dung tab Nhân viên (private final)
 *  - AccountTabPanel  – nội dung tab Tài khoản (private final)
 *  - StaffEditorDialog   – dialog thêm/sửa nhân viên (static final)
 *  - AccountEditorDialog – dialog thêm/sửa tài khoản (static final)
 *
 * Thiết kế tuân thủ SOLID:
 *  - SRP : mỗi inner class chịu trách nhiệm một tab duy nhất
 *  - OCP : mở rộng bằng subclass, không sửa code panel gốc
 *  - DIP : controller inject qua constructor
 */
public class StaffManagementPanel extends JPanel {

    /** Định dạng ngày hiển thị. */
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Controller nhận lệnh từ View. */
    private final StaffManagementController controller;

    /** Hai tab panel nội dung. */
    private final StaffTabPanel staffTabPanel;
    private final AccountTabPanel accountTabPanel;

    /**
     * Khởi tạo panel với hai tab.
     *
     * @param controller controller quản lý nhân viên/tài khoản (không null)
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

        // Khi chuyển tab thì reload dữ liệu tab đó
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 0) staffTabPanel.refreshData();
            else accountTabPanel.refreshData();
        });

        add(tabs, BorderLayout.CENTER);
    }

    // ─── Shared UI factory helpers (dùng chung cho cả hai tab) ──────────

    /**
     * Tạo nút hành động màu xanh, style nhất quán.
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

    /** Tạo nút nguy hiểm (đỏ) cho các thao tác xóa / khóa. */
    private JButton createDangerButton(String title) {
        JButton btn = createActionButton(title);
        btn.setForeground(UiPalette.DANGER);
        return btn;
    }

    /** Bọc JTable trong card có border nhất quán với toàn ứng dụng. */
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

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Tab 1: Nhân viên
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Panel nội dung của tab "Nhân viên".
     * Chịu trách nhiệm duy nhất cho việc hiển thị và CRUD nhân viên.
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

        /** Metadata (danh sách chi nhánh) để dùng trong dialog. */
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

        /** Xây toolbar: tìm kiếm bên trái, CRUD bên phải. */
        private JPanel buildToolbar() {
            JPanel panel = new JPanel(new BorderLayout(8, 0));
            panel.setOpaque(false);

            // Trái
            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            left.setOpaque(false);
            searchField.setPreferredSize(new Dimension(220, 30));
            statusFilter.setPreferredSize(new Dimension(110, 30));

            JButton searchBtn = createActionButton("Tìm");
            searchBtn.addActionListener(e -> refreshData());
            // Lọc theo status filter tự động không cần nhấn Tìm
            statusFilter.addActionListener(e -> refreshData());

            left.add(new JLabel("Tìm kiếm:"));
            left.add(searchField);
            left.add(searchBtn);
            left.add(new JLabel("Trạng thái:"));
            left.add(statusFilter);

            // Phải
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
         * Tải lại danh sách nhân viên, dùng Stream API map entity→bảng.
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

        /** Reload meta khi mở dialog (cần danh sách branch mới nhất). */
        private void reloadMetadata() {
            try {
                metadata = controller.loadMetadata();
            } catch (Exception ex) {
                metadata = StaffManagementMetadata.empty();
                showError("Không tải được dữ liệu danh sách chi nhánh.");
            }
        }

        /** Đọc enum Status từ combobox filter. Null = tất cả. */
        private Status parseStatusFilter() {
            String raw = (String) statusFilter.getSelectedItem();
            if (raw == null || raw.equalsIgnoreCase("Tất cả")) return null;
            return Status.valueOf(raw);
        }

        /** Lấy nhân viên đang chọn trong bảng. */
        private Optional<StaffItem> selectedStaff() {
            int view = table.getSelectedRow();
            if (view < 0) return Optional.empty();
            int model = table.convertRowIndexToModel(view);
            if (model < 0 || model >= rows.size()) return Optional.empty();
            return Optional.of(rows.get(model));
        }

        /** Mở dialog thêm/sửa nhân viên. */
        private void showEditor(StaffItem existing) {
            reloadMetadata();
            StaffEditorDialog dialog = new StaffEditorDialog(
                    SwingUtilities.getWindowAncestor(this), metadata, existing);
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

        /** Đặt trạng thái nhân viên thành INACTIVE (ngừng hoạt động). */
        private void deactivateStaff(StaffItem item) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Xác nhận ngừng hoạt động nhân viên: " + item.fullName() + " (" + item.staffCode() + ")?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) return;

            try {
                // Tạo request update chỉ thay đổi status
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

        /** Cấu hình chiều rộng cột và căn chữ. */
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
     * Panel nội dung của tab "Tài khoản".
     * Hiển thị và quản lý tài khoản đăng nhập (1-1 với Staff).
     * Hỗ trợ: Tạo / Sửa / Khóa / Xóa tài khoản.
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

            // Trái: tìm kiếm
            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            left.setOpaque(false);
            searchField.setPreferredSize(new Dimension(240, 30));

            JButton searchBtn = createActionButton("Tìm");
            searchBtn.addActionListener(e -> refreshData());

            left.add(new JLabel("Tìm kiếm:"));
            left.add(searchField);
            left.add(searchBtn);

            // Phải: CRUD + Khóa
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            right.setOpaque(false);

            JButton refreshBtn = createActionButton("Làm mới");
            JButton addBtn     = createActionButton("Tạo TK");
            JButton editBtn    = createActionButton("Sửa");
            JButton lockBtn    = createDangerButton("Khóa/Mở");
            JButton deleteBtn  = createDangerButton("Xóa TK");

            refreshBtn.addActionListener(e -> refreshData());
            addBtn.addActionListener(e -> showAccountEditor(null));
            editBtn.addActionListener(e -> selectedAccount()
                    .ifPresentOrElse(this::showAccountEditor,
                            () -> showInfo("Vui lòng chọn tài khoản cần sửa.")));
            // Nút Khóa/Mở: toggle trạng thái khóa tài khoản
            lockBtn.addActionListener(e -> selectedAccount()
                    .ifPresentOrElse(this::toggleLock,
                            () -> showInfo("Vui lòng chọn tài khoản cần khóa/mở.")));
            deleteBtn.addActionListener(e -> selectedAccount()
                    .ifPresentOrElse(this::deleteAccount,
                            () -> showInfo("Vui lòng chọn tài khoản cần xóa.")));

            right.add(refreshBtn);
            right.add(addBtn);
            right.add(editBtn);
            right.add(lockBtn);
            right.add(deleteBtn);

            panel.add(left, BorderLayout.WEST);
            panel.add(right, BorderLayout.EAST);
            return panel;
        }

        /**
         * Load danh sách tài khoản từ service, dùng Stream map → bảng.
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

        /** Mở dialog tạo/sửa tài khoản. */
        private void showAccountEditor(AccountItem existing) {
            // Lấy danh sách nhân viên chưa có tài khoản để điền combo khi tạo mới
            AccountEditorDialog dialog = new AccountEditorDialog(
                    SwingUtilities.getWindowAncestor(this), existing,
                    controller.loadStaffs(null, Status.ACTIVE));
            dialog.setVisible(true);

            dialog.getResult().ifPresent(request -> {
                try {
                    if (existing == null) {
                        controller.createAccount(request);
                        showInfo("Tạo tài khoản thành công.");
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

        /**
         * Toggle khóa/mở tài khoản với xác nhận.
         *
         * @param item tài khoản cần thao tác
         */
        private void toggleLock(AccountItem item) {
            boolean willLock = !item.locked();
            String action = willLock ? "khóa" : "mở khóa";
            int confirm = JOptionPane.showConfirmDialog(
                    this,
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

        /** Xóa tài khoản với xác nhận. */
        private void deleteAccount(AccountItem item) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
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
            // Ẩn cột ID (dùng internal)
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
            // Căn giữa: Trạng thái, Khóa, Lỗi đăng nhập
            for (int col : new int[]{4, 5, 6}) {
                table.getColumnModel().getColumn(col).setCellRenderer(center);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Dialog: Thêm / Sửa Nhân viên
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Dialog modal nhập thông tin nhân viên.
     * Static để không giữ tham chiếu về outer panel.
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

            // Điền danh sách chi nhánh vào combo
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

            // Điền dữ liệu cũ nếu đang sửa
            if (existing != null) {
                codeField.setText(existing.staffCode());
                fullNameField.setText(existing.fullName());
                emailField.setText(existing.email());
                phoneField.setText(existing.phone());
                roleCombo.setSelectedItem(existing.role());
                statusCombo.setSelectedItem(existing.status());
                // Chọn chi nhánh khớp branchId
                selectBranchById(existing.branchId());
            } else {
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

            actions.add(cancelBtn);
            actions.add(saveBtn);

            add(form, BorderLayout.CENTER);
            add(actions, BorderLayout.SOUTH);

            pack();
            setMinimumSize(new Dimension(460, 340));
            setSize(getMinimumSize());
            setLocationRelativeTo(owner);
        }

        /** Đọc form và tạo request hoặc hiển thị lỗi validate. */
        private void onSave() {
            if (codeField.getText().isBlank() || fullNameField.getText().isBlank()) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng điền đủ các trường bắt buộc (*).",
                        "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }
            CarLookupItem branch = (CarLookupItem) branchCombo.getSelectedItem();
            if (branch == null) {
                JOptionPane.showMessageDialog(this,
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

        /** Chọn chi nhánh trong combo theo branchId. */
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
     * Dialog modal nhập thông tin tài khoản đăng nhập.
     * Khi tạo mới: cho chọn nhân viên + nhập password.
     * Khi sửa: chỉ cho đổi username / password (để trống = giữ nguyên) / status.
     */
    private static final class AccountEditorDialog extends JDialog {

        /** ComboBox chọn nhân viên (chỉ hiển thị khi tạo mới). */
        private final JComboBox<StaffItem> staffCombo = new JComboBox<>();
        private final JTextField usernameField = new JTextField();
        /** Để trống khi sửa = giữ mật khẩu cũ. */
        private final JPasswordField passwordField = new JPasswordField();
        private final JComboBox<Status> statusCombo = new JComboBox<>(Status.values());

        private AccountCommandRequest result;
        private final Long editingId;

        private AccountEditorDialog(Window owner, AccountItem existing, List<StaffItem> allStaffs) {
            super(owner,
                    existing == null ? "Tạo tài khoản" : "Sửa tài khoản",
                    ModalityType.APPLICATION_MODAL);
            this.editingId = existing == null ? null : existing.id();

            setResizable(false);
            setLayout(new BorderLayout(0, 8));

            JPanel form = new JPanel(new GridLayout(4, 2, 8, 6));
            form.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));

            if (existing == null) {
                // Tạo mới: điền combo nhân viên chưa có tài khoản
                allStaffs.stream()
                        .filter(s -> !s.hasAccount()) // Chỉ nhân viên chưa có TK
                        .forEach(staffCombo::addItem);
                form.add(new JLabel("Nhân viên *"));
                form.add(staffCombo);
            } else {
                // Sửa: hiển thị thông tin nhân viên (read-only)
                JLabel staffLabel = new JLabel(existing.staffFullName() + " (" + existing.staffCode() + ")");
                staffLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
                form.add(new JLabel("Nhân viên"));
                form.add(staffLabel);
            }

            passwordField.setToolTipText(existing != null ? "Để trống = giữ mật khẩu cũ" : "Bắt buộc");

            form.add(new JLabel("Tên đăng nhập *")); form.add(usernameField);
            form.add(new JLabel(existing != null ? "Mật khẩu mới (tùy chọn)" : "Mật khẩu *"));
            form.add(passwordField);
            form.add(new JLabel("Trạng thái"));       form.add(statusCombo);

            // Điền dữ liệu cũ nếu sửa
            if (existing != null) {
                usernameField.setText(existing.username());
                statusCombo.setSelectedItem(existing.status());
            } else {
                statusCombo.setSelectedItem(Status.ACTIVE);
            }

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
            JButton cancelBtn = new JButton("Hủy");
            JButton saveBtn   = new JButton("Lưu");
            saveBtn.setBackground(UiPalette.PRIMARY);
            saveBtn.setForeground(Color.WHITE);
            saveBtn.setFocusPainted(false);

            cancelBtn.addActionListener(e -> dispose());
            saveBtn.addActionListener(e -> onSave(existing));

            actions.add(cancelBtn);
            actions.add(saveBtn);

            add(form, BorderLayout.CENTER);
            add(actions, BorderLayout.SOUTH);

            pack();
            setMinimumSize(new Dimension(420, 260));
            setSize(getMinimumSize());
            setLocationRelativeTo(owner);
        }

        /** Đọc form và tạo AccountCommandRequest. */
        private void onSave(AccountItem existing) {
            if (usernameField.getText().isBlank()) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng nhập tên đăng nhập.",
                        "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String rawPassword = new String(passwordField.getPassword()).trim();

            // Khi tạo mới bắt buộc phải có mật khẩu
            if (existing == null && rawPassword.isBlank()) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng nhập mật khẩu.",
                        "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Long staffId;
            if (existing == null) {
                // Lấy id nhân viên từ combo
                StaffItem selectedStaff = (StaffItem) staffCombo.getSelectedItem();
                if (selectedStaff == null) {
                    JOptionPane.showMessageDialog(this,
                            "Vui lòng chọn nhân viên.",
                            "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                staffId = selectedStaff.id();
            } else {
                staffId = existing.staffId();
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
    }
}
