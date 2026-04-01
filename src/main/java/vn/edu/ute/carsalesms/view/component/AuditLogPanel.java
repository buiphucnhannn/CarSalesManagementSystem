package vn.edu.ute.carsalesms.view.component;

import vn.edu.ute.carsalesms.controller.AuditLogController;
import vn.edu.ute.carsalesms.model.dto.AuditLogItem;
import vn.edu.ute.carsalesms.view.theme.DialogUiUtil;
import vn.edu.ute.carsalesms.view.theme.UiPalette;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class AuditLogPanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final AuditLogController auditLogController;

    private final JTextField txtSearch = new JTextField(20);
    private final JComboBox<FilterOption> cbAction = new JComboBox<>();
    private final JComboBox<FilterOption> cbEntity = new JComboBox<>();

    private final DefaultTableModel tableModel;
    private final JTable table;
    private List<AuditLogItem> rows = new ArrayList<>();

    public AuditLogPanel(AuditLogController auditLogController) {
        this.auditLogController = Objects.requireNonNull(auditLogController, "auditLogController is required");

        setLayout(new BorderLayout(8, 8));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        String[] cols = {"Thời gian", "Nhân viên", "Vai trò", "Chức năng", "Thao tác", "Mã dữ liệu", "Mô tả thao tác"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        configureColumns();

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showDetailDialog();
                }
            }
        });

        add(buildToolbar(), BorderLayout.NORTH);
        add(createTableCard(table), BorderLayout.CENTER);

        loadFilters();
        refreshData();
    }

    private JPanel buildToolbar() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setOpaque(false);

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topRow.setOpaque(false);

        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bottomRow.setOpaque(false);

        cbAction.setPreferredSize(new Dimension(180, 30));
        cbEntity.setPreferredSize(new Dimension(180, 30));
        txtSearch.setPreferredSize(new Dimension(300, 30));
        txtSearch.setToolTipText("Tìm kiếm/lọc theo mã NV hoặc tên nhân viên.");

        JButton btnSearch = createActionButton("Tìm");
        btnSearch.setBackground(UiPalette.PRIMARY);
        btnSearch.setForeground(Color.WHITE);
        btnSearch.addActionListener(e -> refreshData());

        JButton btnReset = createActionButton("Làm mới");
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cbAction.setSelectedIndex(0);
            cbEntity.setSelectedIndex(0);
            refreshData();
        });

        topRow.add(new JLabel("Tìm kiếm/lọc theo mã NV hoặc tên:"));
        topRow.add(txtSearch);
        topRow.add(btnSearch);
        topRow.add(btnReset);

        bottomRow.add(new JLabel("Thao tác:"));
        bottomRow.add(cbAction);
        bottomRow.add(new JLabel("Chức năng:"));
        bottomRow.add(cbEntity);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JButton btnDetail = createActionButton("Xem chi tiết");
        btnDetail.addActionListener(e -> showDetailDialog());
        right.add(btnDetail);

        JPanel left = new JPanel(new BorderLayout(0, 8));
        left.setOpaque(false);
        left.add(topRow, BorderLayout.NORTH);
        left.add(bottomRow, BorderLayout.CENTER);

        p.add(left, BorderLayout.CENTER);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private void loadFilters() {
        cbAction.removeAllItems();
        cbEntity.removeAllItems();

        cbAction.addItem(new FilterOption("Tất cả thao tác", null));
        cbEntity.addItem(new FilterOption("Tất cả chức năng", null));

        mergeFilterOptions(defaultActionOptions(), auditLogController.loadActionFilters()).values()
                .forEach(cbAction::addItem);

        mergeFilterOptions(defaultEntityOptions(), auditLogController.loadEntityFilters()).values()
                .forEach(cbEntity::addItem);
    }


    private Map<String, FilterOption> mergeFilterOptions(List<String> defaults, List<String> discovered) {
        Map<String, FilterOption> merged = new LinkedHashMap<>();
        Map<String, Boolean> usedLabels = new LinkedHashMap<>();

        defaults.stream()
                .filter(v -> v != null && !v.isBlank())
                .forEach(raw -> {
                    String label = resolveFilterLabel(raw);
                    if (!usedLabels.containsKey(label)) {
                        merged.put(raw, new FilterOption(label, raw));
                        usedLabels.put(label, true);
                    }
                });

        discovered.stream()
                .filter(v -> v != null && !v.isBlank())
                .forEach(raw -> {
                    String label = resolveFilterLabel(raw);
                    if (!usedLabels.containsKey(label)) {
                        merged.putIfAbsent(raw, new FilterOption(label, raw));
                        usedLabels.put(label, true);
                    }
                });

        return merged;
    }

    private List<String> defaultActionOptions() {
        return List.of(
                "CREATE", "UPDATE", "DELETE", "DEACTIVATE", "CANCEL",
                "PAY", "TOGGLE_LOCK", "TOGGLE_STATUS", "EXPORT_PDF",
                "LOGIN_SUCCESS", "LOGIN_FAILED"
        );
    }

    private List<String> defaultEntityOptions() {
        return List.of(
                "AUTH", "ACCOUNT", "STAFF", "CUSTOMER", "BRANCH",
                "CAR", "BRAND", "CAR_CATEGORY", "PROMOTION",
                "SALE_ORDER", "PAYMENT", "INVOICE",
                "INSTALLMENT", "INSTALLMENT_PLAN", "TEST_DRIVE", "WARRANTY"
        );
    }

    private String resolveFilterLabel(String raw) {
        String actionVi = toActionVi(raw);
        if (!actionVi.equals(raw)) {
            return actionVi;
        }
        String entityVi = toEntityVi(raw);
        if (!entityVi.equals(raw)) {
            return entityVi;
        }
        return raw;
    }

    private void refreshData() {
        try {
            rows = auditLogController.loadLogs(
                    txtSearch.getText(),
                    selectedValue(cbAction),
                    selectedValue(cbEntity),
                    500
            );

            tableModel.setRowCount(0);
            rows.stream()
                    .map(this::toRow)
                    .forEach(tableModel::addRow);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(getDialogParent(), "Lỗi tải nhật ký: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Object[] toRow(AuditLogItem item) {
        String actionVi = toActionVi(item.action());
        String entityVi = toEntityVi(item.entityName());
        String moduleVi = toModuleVi(item.entityName());
        String summary = buildFriendlySummary(item, actionVi, entityVi);

        return new Object[]{
                item.createdAt() == null ? "" : item.createdAt().format(DATE_FMT),
                item.staffCode() + " - " + item.staffName(),
                item.staffRole(),
                moduleVi,
                actionVi,
                item.entityId(),
                summary
        };
    }

    private void showDetailDialog() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0 || rows.isEmpty()) {
            JOptionPane.showMessageDialog(getDialogParent(), "Vui lòng chọn 1 dòng nhật ký.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);
        AuditLogItem item = rows.get(modelRow);

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        area.setText(
                "Thời gian: " + (item.createdAt() == null ? "" : item.createdAt().format(DATE_FMT)) + "\n" +
                "Nhân viên: " + item.staffCode() + " - " + item.staffName() + " (" + item.staffRole() + ")\n" +
                "Thao tác: " + toActionVi(item.action()) + "\n" +
                "Chức năng: " + toModuleVi(item.entityName()) + "\n" +
                "Đối tượng: " + toEntityVi(item.entityName()) + " | ID: " + item.entityId() + "\n\n" +
                "Giá trị cũ:\n" + toFriendlyAuditValue(item.oldValue()) + "\n\n" +
                "Giá trị mới:\n" + toFriendlyAuditValue(item.newValue())
        );

        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setPreferredSize(new Dimension(760, 420));
        JOptionPane.showMessageDialog(getDialogParent(), scrollPane, "Chi tiết nhật ký", JOptionPane.INFORMATION_MESSAGE);
    }


    private void configureColumns() {
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(240);
        table.getColumnModel().getColumn(2).setPreferredWidth(90);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(130);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);
        table.getColumnModel().getColumn(6).setPreferredWidth(410);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(2).setCellRenderer(center);
        table.getColumnModel().getColumn(5).setCellRenderer(center);
    }

    private String selectedValue(JComboBox<FilterOption> comboBox) {
        FilterOption selected = (FilterOption) comboBox.getSelectedItem();
        return selected == null ? null : selected.value();
    }

    private String buildFriendlySummary(AuditLogItem item, String actionVi, String entityVi) {
        String base = actionVi + " " + entityVi.toLowerCase();
        if (item.entityId() != null) {
            base += " (ID=" + item.entityId() + ")";
        }

        String payload = Stream.of(item.newValue(), item.oldValue())
                .filter(v -> v != null && !v.isBlank())
                .findFirst()
                .map(this::toFriendlyAuditValue)
                .orElse("");

        if (payload.isBlank()) {
            return base;
        }

        String summary = base + " - " + payload;
        return summary.length() > 130 ? summary.substring(0, 130) + "..." : summary;
    }

    private String toFriendlyAuditValue(String raw) {
        if (raw == null || raw.isBlank()) {
            return "(trống)";
        }

        String clean = raw
                .replace("AccountCommandRequest", "")
                .replace("StaffCommandRequest", "")
                .replace("CustomerCommandRequest", "")
                .replace("CarCommandRequest", "")
                .replace("BranchCommandRequest", "")
                .replace("PromotionRequest", "")
                .replace("PaymentRequest", "")
                .replace('{', ' ')
                .replace('}', ' ')
                .replace('"', ' ')
                .replace('[', ' ')
                .replace(']', ' ')
                .replace(':', '=')
                .replaceAll("\\s+", " ")
                .trim();

        clean = Pattern.compile(",").matcher(clean).replaceAll("; ");

        clean = clean
                .replace("id=", "Mã: ")
                .replace("staffId=", "Mã nhân viên: ")
                .replace("staffCode=", "Mã nhân viên: ")
                .replace("fullName=", "Họ tên: ")
                .replace("username=", "Tên đăng nhập: ")
                .replace("rawPassword=", "Mật khẩu mới: ")
                .replace("status=", "Trạng thái: ")
                .replace("locked=", "Khóa tài khoản: ")
                .replace("failedLoginAttempts=", "Số lần đăng nhập sai: ")
                .replace("branchCode=", "Mã chi nhánh: ")
                .replace("branchName=", "Tên chi nhánh: ")
                .replace("customerCode=", "Mã khách hàng: ")
                .replace("customerName=", "Khách hàng: ")
                .replace("orderCode=", "Mã đơn: ")
                .replace("saleOrder=", "Mã đơn: ")
                .replace("orderStatus=", "Trạng thái đơn: ")
                .replace("paymentMethod=", "Hình thức thanh toán: ")
                .replace("transactionReference=", "Mã tham chiếu: ")
                .replace("amount=", "Số tiền: ")
                .replace("note=", "Ghi chú: ")
                .replace("categoryCode=", "Mã loại xe: ")
                .replace("brandCode=", "Mã hãng xe: ")
                .replace("carCode=", "Mã xe: ")
                .replace("promotionCode=", "Mã khuyến mãi: ");

        clean = clean
                .replace("=ACTIVE", "=Đang hoạt động")
                .replace("=INACTIVE", "=Ngừng hoạt động")
                .replace("=PENDING", "=Đang xử lý")
                .replace("=CONFIRMED", "=Đã xác nhận")
                .replace("=PAID", "=Đã thanh toán")
                .replace("=CANCELLED", "=Đã hủy")
                .replace("=true", "=Có")
                .replace("=false", "=Không");

        return clean;
    }

    private String toActionVi(String action) {
        if (action == null) {
            return "Không xác định";
        }
        return switch (action.toUpperCase()) {
            case "CREATE" -> "Tạo mới";
            case "UPDATE" -> "Cập nhật";
            case "DELETE" -> "Xóa";
            case "DEACTIVATE" -> "Ngừng hoạt động";
            case "CANCEL" -> "Hủy";
            case "PAY" -> "Thanh toán";
            case "TOGGLE_LOCK" -> "Khóa/Mở khóa";
            case "TOGGLE_STATUS" -> "Bật/Tắt trạng thái";
            case "EXPORT_PDF" -> "Xuất PDF";
            case "LOGIN_SUCCESS" -> "Đăng nhập thành công";
            case "LOGIN_FAILED" -> "Đăng nhập thất bại";
            default -> action;
        };
    }

    private String toEntityVi(String entity) {
        if (entity == null) {
            return "Đối tượng hệ thống";
        }
        return switch (entity.toUpperCase()) {
            case "CAR" -> "Xe";
            case "BRAND" -> "Hãng xe";
            case "CAR_CATEGORY" -> "Loại xe";
            case "CUSTOMER" -> "Khách hàng";
            case "STAFF" -> "Nhân viên";
            case "ACCOUNT" -> "Tài khoản";
            case "BRANCH" -> "Chi nhánh";
            case "PROMOTION" -> "Khuyến mãi";
            case "SALE_ORDER" -> "Đơn bán";
            case "PAYMENT" -> "Thanh toán";
            case "INVOICE" -> "Hóa đơn";
            case "INSTALLMENT", "INSTALLMENT_PLAN" -> "Trả góp";
            case "TEST_DRIVE" -> "Lái thử";
            case "WARRANTY" -> "Bảo hành";
            case "AUTH" -> "Đăng nhập";
            default -> entity;
        };
    }

    private String toModuleVi(String entity) {
        if (entity == null) {
            return "Hệ thống";
        }
        return switch (entity.toUpperCase()) {
            case "CAR", "BRAND", "CAR_CATEGORY" -> "Quản lý xe";
            case "CUSTOMER" -> "Khách hàng";
            case "STAFF", "ACCOUNT", "AUTH" -> "Nhân viên/Tài khoản";
            case "BRANCH" -> "Chi nhánh";
            case "PROMOTION" -> "Khuyến mãi";
            case "SALE_ORDER" -> "Đơn bán";
            case "PAYMENT" -> "Thanh toán";
            case "INVOICE" -> "Hóa đơn";
            case "INSTALLMENT", "INSTALLMENT_PLAN" -> "Trả góp";
            case "TEST_DRIVE" -> "Lái thử";
            case "WARRANTY" -> "Bảo hành";
            default -> "Khác";
        };
    }

    private record FilterOption(String label, String value) {
        @Override
        public String toString() {
            return label;
        }
    }

    private JPanel createTableCard(JTable tbl) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UiPalette.SURFACE_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiPalette.BORDER_SOFT),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tbl.setGridColor(UiPalette.BORDER_SOFT);
        tbl.setBackground(UiPalette.TABLE_BACKGROUND);
        tbl.setSelectionBackground(UiPalette.PRIMARY_SOFT);
        tbl.setSelectionForeground(UiPalette.TEXT_PRIMARY);
        tbl.getTableHeader().setBackground(UiPalette.PRIMARY_SOFT);
        tbl.getTableHeader().setForeground(UiPalette.TEXT_PRIMARY);
        tbl.getTableHeader().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));

        JScrollPane scroll = new JScrollPane(tbl);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

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

    private Component getDialogParent() {
        Component owner = DialogUiUtil.appDialogParent(this);
        return owner != null ? owner : this;
    }
}

