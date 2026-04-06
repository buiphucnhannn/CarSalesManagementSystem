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

/**
 * Lớp AuditLogPanel là giao diện người dùng để xem và lọc nhật ký kiểm toán (audit logs).
 * Nó hiển thị các hoạt động của người dùng trong hệ thống, bao gồm thời gian, nhân viên thực hiện,
 * chức năng, thao tác, mã dữ liệu liên quan và mô tả chi tiết.
 * Người dùng có thể tìm kiếm, lọc theo thao tác và chức năng, và xem chi tiết từng bản ghi.
 */
public class AuditLogPanel extends JPanel {

    // Định dạng ngày và giờ để hiển thị trong bảng và chi tiết nhật ký.
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final AuditLogController auditLogController; // Controller để tương tác với logic nghiệp vụ của nhật ký kiểm toán.

    private final JTextField txtSearch = new JTextField(20); // Ô nhập liệu để tìm kiếm.
    private final JComboBox<FilterOption> cbAction = new JComboBox<>(); // ComboBox để lọc theo loại thao tác.
    private final JComboBox<FilterOption> cbEntity = new JComboBox<>(); // ComboBox để lọc theo loại đối tượng (chức năng).

    private final DefaultTableModel tableModel; // Model dữ liệu cho bảng nhật ký.
    private final JTable table; // Bảng hiển thị danh sách nhật ký.
    private List<AuditLogItem> rows = new ArrayList<>(); // Danh sách các đối tượng AuditLogItem hiện tại.

    /**
     * Constructor khởi tạo AuditLogPanel.
     * @param auditLogController Controller để xử lý các thao tác liên quan đến nhật ký kiểm toán.
     */
    public AuditLogPanel(AuditLogController auditLogController) {
        this.auditLogController = Objects.requireNonNull(auditLogController, "auditLogController is required");

        setLayout(new BorderLayout(8, 8));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Định nghĩa tên các cột cho bảng nhật ký.
        String[] cols = {"Thời gian", "Nhân viên", "Vai trò", "Chức năng", "Thao tác", "Mã dữ liệu", "Mô tả thao tác"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho phép chỉnh sửa trực tiếp trên bảng.
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setAutoCreateRowSorter(true); // Tự động tạo bộ sắp xếp hàng.
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Chỉ cho phép chọn một hàng.
        table.getTableHeader().setReorderingAllowed(false); // Không cho phép sắp xếp lại cột.

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter); // Gán bộ sắp xếp cho bảng.
        configureColumns(); // Cấu hình các cột.

        // Thêm MouseListener để hiển thị chi tiết khi nhấp đúp vào một hàng.
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showDetailDialog();
                }
            }
        });

        add(buildToolbar(), BorderLayout.NORTH); // Thêm thanh công cụ vào phía Bắc.
        add(createTableCard(table), BorderLayout.CENTER); // Thêm bảng vào giữa.

        loadFilters(); // Tải các tùy chọn cho bộ lọc.
        refreshData(); // Tải dữ liệu nhật ký ban đầu.
    }

    /**
     * Xây dựng thanh công cụ (toolbar) cho panel.
     * Bao gồm các ô tìm kiếm, bộ lọc và các nút hành động.
     * @return JPanel chứa thanh công cụ.
     */
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
        btnSearch.addActionListener(e -> refreshData()); // Gán hành động tìm kiếm khi nhấn nút.

        JButton btnReset = createActionButton("Làm mới");
        btnReset.addActionListener(e -> {
            txtSearch.setText(""); // Xóa nội dung ô tìm kiếm.
            cbAction.setSelectedIndex(0); // Đặt lại bộ lọc thao tác về "Tất cả".
            cbEntity.setSelectedIndex(0); // Đặt lại bộ lọc chức năng về "Tất cả".
            refreshData(); // Tải lại dữ liệu.
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
        btnDetail.addActionListener(e -> showDetailDialog()); // Gán hành động xem chi tiết.
        right.add(btnDetail);

        JPanel left = new JPanel(new BorderLayout(0, 8));
        left.setOpaque(false);
        left.add(topRow, BorderLayout.NORTH);
        left.add(bottomRow, BorderLayout.CENTER);

        p.add(left, BorderLayout.CENTER);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    /**
     * Tải các tùy chọn cho ComboBox lọc "Thao tác" và "Chức năng".
     * Kết hợp các tùy chọn mặc định với các tùy chọn được tải từ controller.
     */
    private void loadFilters() {
        cbAction.removeAllItems();
        cbEntity.removeAllItems();

        cbAction.addItem(new FilterOption("Tất cả thao tác", null));
        cbEntity.addItem(new FilterOption("Tất cả chức năng", null));

        // Hợp nhất các tùy chọn thao tác mặc định và các tùy chọn từ controller.
        mergeFilterOptions(defaultActionOptions(), auditLogController.loadActionFilters()).values()
                .forEach(cbAction::addItem);

        // Hợp nhất các tùy chọn chức năng mặc định và các tùy chọn từ controller.
        mergeFilterOptions(defaultEntityOptions(), auditLogController.loadEntityFilters()).values()
                .forEach(cbEntity::addItem);
    }

    /**
     * Hợp nhất danh sách các tùy chọn lọc.
     * Ưu tiên các tùy chọn mặc định và đảm bảo không có tùy chọn trùng lặp.
     * @param defaults Danh sách các chuỗi tùy chọn mặc định.
     * @param discovered Danh sách các chuỗi tùy chọn được phát hiện từ dữ liệu.
     * @return Map chứa các FilterOption đã hợp nhất.
     */
    private Map<String, FilterOption> mergeFilterOptions(List<String> defaults, List<String> discovered) {
        Map<String, FilterOption> merged = new LinkedHashMap<>();
        Map<String, Boolean> usedLabels = new LinkedHashMap<>();

        // Thêm các tùy chọn mặc định.
        defaults.stream()
                .filter(v -> v != null && !v.isBlank())
                .forEach(raw -> {
                    String label = resolveFilterLabel(raw);
                    if (!usedLabels.containsKey(label)) {
                        merged.put(raw, new FilterOption(label, raw));
                        usedLabels.put(label, true);
                    }
                });

        // Thêm các tùy chọn được phát hiện, nếu chúng chưa có trong danh sách.
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

    /**
     * Cung cấp danh sách các tùy chọn thao tác mặc định.
     * @return List các chuỗi đại diện cho các thao tác.
     */
    private List<String> defaultActionOptions() {
        return List.of(
                "CREATE", "UPDATE", "DELETE", "DEACTIVATE", "CANCEL",
                "PAY", "TOGGLE_LOCK", "TOGGLE_STATUS", "EXPORT_PDF",
                "LOGIN_SUCCESS", "LOGIN_FAILED"
        );
    }

    /**
     * Cung cấp danh sách các tùy chọn đối tượng (chức năng) mặc định.
     * @return List các chuỗi đại diện cho các đối tượng.
     */
    private List<String> defaultEntityOptions() {
        return List.of(
                "AUTH", "ACCOUNT", "STAFF", "CUSTOMER", "BRANCH",
                "CAR", "BRAND", "CAR_CATEGORY", "PROMOTION",
                "SALE_ORDER", "PAYMENT", "INVOICE",
                "INSTALLMENT", "INSTALLMENT_PLAN", "TEST_DRIVE", "WARRANTY"
        );
    }

    /**
     * Giải quyết nhãn hiển thị cho một tùy chọn lọc dựa trên giá trị thô.
     * @param raw Giá trị thô của tùy chọn.
     * @return Nhãn hiển thị đã được dịch sang tiếng Việt nếu có, nếu không thì trả về giá trị thô.
     */
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

    /**
     * Tải lại dữ liệu nhật ký từ controller dựa trên các bộ lọc hiện tại và cập nhật bảng.
     */
    private void refreshData() {
        try {
            rows = auditLogController.loadLogs(
                    txtSearch.getText(),
                    selectedValue(cbAction),
                    selectedValue(cbEntity),
                    500 // Giới hạn số lượng bản ghi để tránh quá tải.
            );

            tableModel.setRowCount(0); // Xóa tất cả các hàng hiện có trong bảng.
            // Chuyển đổi mỗi AuditLogItem thành một hàng trong bảng.
            rows.stream()
                    .map(this::toRow)
                    .forEach(tableModel::addRow);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(getDialogParent(), "Lỗi tải nhật ký: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Chuyển đổi một đối tượng AuditLogItem thành một mảng Object[] để hiển thị trong bảng.
     * @param item Đối tượng AuditLogItem.
     * @return Mảng Object[] đại diện cho một hàng trong bảng.
     */
    private Object[] toRow(AuditLogItem item) {
        String actionVi = toActionVi(item.action()); // Dịch thao tác sang tiếng Việt.
        String entityVi = toEntityVi(item.entityName()); // Dịch tên đối tượng sang tiếng Việt.
        String moduleVi = toModuleVi(item.entityName()); // Dịch tên module sang tiếng Việt.
        String summary = buildFriendlySummary(item, actionVi, entityVi); // Xây dựng tóm tắt thân thiện.

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

    /**
     * Hiển thị một dialog chứa thông tin chi tiết của bản ghi nhật ký được chọn.
     */
    private void showDetailDialog() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0 || rows.isEmpty()) {
            JOptionPane.showMessageDialog(getDialogParent(), "Vui lòng chọn 1 dòng nhật ký.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);
        AuditLogItem item = rows.get(modelRow); // Lấy đối tượng AuditLogItem từ hàng đã chọn.

        JTextArea area = new JTextArea();
        area.setEditable(false); // Không cho phép chỉnh sửa.
        area.setLineWrap(true); // Tự động xuống dòng.
        area.setWrapStyleWord(true); // Xuống dòng theo từ.
        area.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Đặt nội dung chi tiết cho JTextArea.
        area.setText(
                "Thời gian: " + (item.createdAt() == null ? "" : item.createdAt().format(DATE_FMT)) + "\n" +
                "Nhân viên: " + item.staffCode() + " - " + item.staffName() + " (" + item.staffRole() + ")\n" +
                "Thao tác: " + toActionVi(item.action()) + "\n" +
                "Chức năng: " + toModuleVi(item.entityName()) + "\n" +
                "Đối tượng: " + toEntityVi(item.entityName()) + " | ID: " + item.entityId() + "\n\n" +
                "Giá trị cũ:\n" + toFriendlyAuditValue(item.oldValue()) + "\n\n" +
                "Giá trị mới:\n" + toFriendlyAuditValue(item.newValue())
        );

        JScrollPane scrollPane = new JScrollPane(area); // Bọc JTextArea trong JScrollPane.
        scrollPane.setPreferredSize(new Dimension(760, 420)); // Đặt kích thước ưu tiên.
        JOptionPane.showMessageDialog(getDialogParent(), scrollPane, "Chi tiết nhật ký", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Cấu hình chiều rộng ưu tiên và renderer cho các cột trong bảng.
     */
    private void configureColumns() {
        table.getColumnModel().getColumn(0).setPreferredWidth(150); // Thời gian
        table.getColumnModel().getColumn(1).setPreferredWidth(240); // Nhân viên
        table.getColumnModel().getColumn(2).setPreferredWidth(90);  // Vai trò
        table.getColumnModel().getColumn(3).setPreferredWidth(150); // Chức năng
        table.getColumnModel().getColumn(4).setPreferredWidth(130); // Thao tác
        table.getColumnModel().getColumn(5).setPreferredWidth(90);  // Mã dữ liệu
        table.getColumnModel().getColumn(6).setPreferredWidth(410); // Mô tả thao tác

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(2).setCellRenderer(center); // Căn giữa cột Vai trò.
        table.getColumnModel().getColumn(5).setCellRenderer(center); // Căn giữa cột Mã dữ liệu.
    }

    /**
     * Lấy giá trị đã chọn từ một JComboBox chứa FilterOption.
     * @param comboBox JComboBox chứa các FilterOption.
     * @return Giá trị String của FilterOption được chọn, hoặc null nếu không có gì được chọn hoặc là "Tất cả".
     */
    private String selectedValue(JComboBox<FilterOption> comboBox) {
        FilterOption selected = (FilterOption) comboBox.getSelectedItem();
        return selected == null ? null : selected.value();
    }

    /**
     * Xây dựng một chuỗi tóm tắt thân thiện từ AuditLogItem.
     * @param item Đối tượng AuditLogItem.
     * @param actionVi Chuỗi thao tác đã được dịch sang tiếng Việt.
     * @param entityVi Chuỗi đối tượng đã được dịch sang tiếng Việt.
     * @return Chuỗi tóm tắt.
     */
    private String buildFriendlySummary(AuditLogItem item, String actionVi, String entityVi) {
        String base = actionVi + " " + entityVi.toLowerCase();
        if (item.entityId() != null) {
            base += " (ID=" + item.entityId() + ")";
        }

        // Lấy giá trị cũ hoặc mới để làm phần mô tả chi tiết.
        String payload = Stream.of(item.newValue(), item.oldValue())
                .filter(v -> v != null && !v.isBlank())
                .findFirst()
                .map(this::toFriendlyAuditValue)
                .orElse("");

        if (payload.isBlank()) {
            return base;
        }

        String summary = base + " - " + payload;
        // Cắt ngắn chuỗi tóm tắt nếu quá dài.
        return summary.length() > 130 ? summary.substring(0, 130) + "..." : summary;
    }

    /**
     * Chuyển đổi một chuỗi JSON thô từ audit log thành một chuỗi thân thiện dễ đọc.
     * Thực hiện làm sạch và thay thế các từ khóa tiếng Anh bằng tiếng Việt.
     * @param raw Chuỗi JSON thô.
     * @return Chuỗi đã được định dạng thân thiện.
     */
    private String toFriendlyAuditValue(String raw) {
        if (raw == null || raw.isBlank()) {
            return "(trống)";
        }

        // Làm sạch chuỗi: loại bỏ tên DTO, dấu ngoặc, dấu nháy kép, thay thế dấu hai chấm, và chuẩn hóa khoảng trắng.
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

        // Thay thế dấu phẩy bằng dấu chấm phẩy để phân tách các cặp key-value.
        clean = Pattern.compile(",").matcher(clean).replaceAll("; ");

        // Thay thế các từ khóa tiếng Anh bằng tiếng Việt.
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

        // Thay thế các giá trị enum bằng tiếng Việt.
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

    /**
     * Chuyển đổi tên thao tác tiếng Anh sang tiếng Việt.
     * @param action Tên thao tác tiếng Anh.
     * @return Tên thao tác tiếng Việt tương ứng.
     */
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

    /**
     * Chuyển đổi tên đối tượng (entity) tiếng Anh sang tiếng Việt.
     * @param entity Tên đối tượng tiếng Anh.
     * @return Tên đối tượng tiếng Việt tương ứng.
     */
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

    /**
     * Chuyển đổi tên đối tượng (entity) tiếng Anh sang tên module tiếng Việt.
     * @param entity Tên đối tượng tiếng Anh.
     * @return Tên module tiếng Việt tương ứng.
     */
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

    /**
     * Record đại diện cho một tùy chọn trong ComboBox lọc.
     * Bao gồm nhãn hiển thị (label) và giá trị thực tế (value).
     */
    private record FilterOption(String label, String value) {
        @Override
        public String toString() {
            return label; // Hiển thị nhãn trong ComboBox.
        }
    }

    /**
     * Tạo một JPanel chứa JTable với phong cách nhất quán.
     * @param tbl JTable cần bọc.
     * @return JPanel đã được định kiểu.
     */
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

    /**
     * Tạo một nút hành động với phong cách nhất quán.
     * @param title Tiêu đề của nút.
     * @return JButton đã được định kiểu.
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

    /**
     * Lấy component cha cho các hộp thoại, đảm bảo chúng hiển thị đúng vị trí.
     * @return Component cha.
     */
    private Component getDialogParent() {
        Component owner = DialogUiUtil.appDialogParent(this);
        return owner != null ? owner : this;
    }
}
