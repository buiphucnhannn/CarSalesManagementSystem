package vn.edu.ute.carsalesms.view.component;

import vn.edu.ute.carsalesms.controller.SaleOrderController;
import vn.edu.ute.carsalesms.model.dto.*;
import vn.edu.ute.carsalesms.model.enums.OrderStatus;
import vn.edu.ute.carsalesms.model.enums.PaymentMethod;
import vn.edu.ute.carsalesms.view.theme.DialogUiUtil;
import vn.edu.ute.carsalesms.view.theme.UiPalette;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Lớp SaleOrderPanel là giao diện người dùng để quản lý các Đơn bán hàng – Module F06.
 *
 * Tính năng chính:
 * - Hiển thị danh sách các Đơn bán hàng hiện có trong hệ thống.
 * - Cho phép tạo mới Đơn bán hàng với chi tiết giỏ hàng và khả năng áp dụng mã khuyến mãi.
 * - Cung cấp chức năng xem chi tiết các dòng xe (sản phẩm) trong một đơn hàng cụ thể.
 * - Hỗ trợ hủy đơn hàng với các điều kiện kiểm tra hợp lệ.
 */
public class SaleOrderPanel extends JPanel {

    // Định dạng ngày và giờ được sử dụng để hiển thị trong bảng.
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final SaleOrderController controller; // Controller để xử lý các thao tác liên quan đến đơn bán hàng.

    // Định nghĩa tên các cột trong bảng hiển thị danh sách đơn hàng.
    private static final String[] COLUMNS = {
            "ID", "Mã đơn", "Khách hàng", "Nhân viên (Sale)",
            "Ngày tạo", "Tổng tiền", "Thực thu", "PT. TT", "Trạng thái"
    };

    private final JTextField searchField = new JTextField(); // Ô nhập liệu để tìm kiếm đơn hàng.
    // ComboBox để lọc đơn hàng theo trạng thái.
    private final JComboBox<String> statusFilter = new JComboBox<>(new String[]{"Tất cả", "PENDING", "CONFIRMED", "PAID", "CANCELLED"});

    private final DefaultTableModel tableModel; // Model dữ liệu cho bảng đơn hàng.
    private final JTable table; // Bảng hiển thị danh sách đơn hàng.
    private final TableRowSorter<DefaultTableModel> sorter; // Bộ sắp xếp và lọc cho bảng.
    private List<SaleOrderItem> rows = new ArrayList<>(); // Danh sách các đối tượng SaleOrderItem hiện tại.

    /**
     * Constructor khởi tạo SaleOrderPanel.
     * @param controller Controller để xử lý các thao tác liên quan đến đơn bán hàng.
     */
    public SaleOrderPanel(SaleOrderController controller) {
        this.controller = Objects.requireNonNull(controller, "controller is required");

        setLayout(new BorderLayout(0, 8)); // Sử dụng BorderLayout với khoảng cách 8px.
        setOpaque(false); // Đặt panel không trong suốt.
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12)); // Đặt đường viền rỗng.

        // Khởi tạo table model, không cho phép chỉnh sửa trực tiếp trên bảng.
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel); // Khởi tạo bộ sắp xếp.
        table.setRowSorter(sorter); // Gán bộ sắp xếp cho bảng.
        configureColumns(); // Cấu hình các cột của bảng.

        // Thêm MouseListener để xử lý sự kiện nhấp đúp chuột để xem chi tiết đơn hàng.
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showOrderDetail();
                }
            }
        });

        add(buildToolbar(), BorderLayout.NORTH); // Thêm thanh công cụ vào phía Bắc.
        add(createTableCard(table), BorderLayout.CENTER); // Thêm bảng vào giữa, bọc trong card.

        refreshData(); // Tải dữ liệu đơn hàng ban đầu.
    }

    /**
     * Xây dựng thanh công cụ (toolbar) cho panel.
     * Bao gồm ô tìm kiếm, bộ lọc trạng thái và các nút hành động (Tìm, Tạo đơn, Xem chi tiết, Hủy đơn).
     * @return JPanel chứa thanh công cụ.
     */
    private JPanel buildToolbar() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);

        // Phần bên trái của toolbar: tìm kiếm và lọc trạng thái.
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        searchField.setPreferredSize(new Dimension(220, 30));
        statusFilter.setPreferredSize(new Dimension(110, 30));

        JButton searchBtn = createActionButton("Tìm");
        searchBtn.addActionListener(e -> refreshData()); // Gán hành động tìm kiếm.
        statusFilter.addActionListener(e -> refreshData()); // Gán hành động lọc theo trạng thái.

        left.add(new JLabel("Tìm kiếm:"));
        left.add(searchField);
        left.add(searchBtn);
        left.add(new JLabel("Trạng thái:"));
        left.add(statusFilter);

        // Phần bên phải của toolbar: các nút hành động.
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JButton refreshBtn = createActionButton("Làm mới");
        JButton addBtn     = createActionButton("Tạo đơn");
        addBtn.setBackground(UiPalette.PRIMARY); // Đặt màu nền cho nút "Tạo đơn".
        addBtn.setForeground(Color.WHITE); // Đặt màu chữ cho nút "Tạo đơn".
        JButton viewBtn    = createActionButton("Xem chi tiết");
        JButton cancelBtn  = createActionButton("Huỷ đơn");
        cancelBtn.setBackground(UiPalette.DANGER); // Đặt màu nền cho nút "Hủy đơn".
        cancelBtn.setForeground(Color.WHITE); // Đặt màu chữ cho nút "Hủy đơn".

        refreshBtn.addActionListener(e -> refreshData()); // Gán hành động làm mới dữ liệu.
        addBtn.addActionListener(e -> showCreateOrderDialog()); // Gán hành động mở dialog tạo đơn.
        viewBtn.addActionListener(e -> showOrderDetail()); // Gán hành động xem chi tiết đơn hàng.
        cancelBtn.addActionListener(e -> cancelSelectedOrder()); // Gán hành động hủy đơn hàng.

        right.add(refreshBtn);
        right.add(addBtn);
        right.add(viewBtn);
        right.add(cancelBtn);

        panel.add(left, BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    /**
     * Tải lại dữ liệu đơn hàng từ controller và cập nhật bảng hiển thị.
     */
    private void refreshData() {
        try {
            OrderStatus fStatus = null;
            String stStr = (String) statusFilter.getSelectedItem();
            if (!"Tất cả".equals(stStr)) {
                fStatus = OrderStatus.valueOf(stStr); // Chuyển đổi chuỗi trạng thái thành enum.
            }
            rows = controller.findOrders(searchField.getText(), fStatus); // Lấy danh sách đơn hàng từ controller.
            tableModel.setRowCount(0); // Xóa tất cả các hàng hiện có trong bảng.

            // Duyệt qua danh sách đơn hàng và thêm từng đơn hàng vào bảng.
            for (SaleOrderItem o : rows) {
                tableModel.addRow(new Object[]{
                        o.id(),
                        o.orderCode(),
                        o.customerName(),
                        o.staffName(),
                        o.orderDate() == null ? "" : o.orderDate().format(DATE_FMT),
                        String.format("%,.0f", o.totalAmount()), // Định dạng tổng tiền.
                        String.format("%,.0f", o.finalAmount()), // Định dạng thực thu.
                        o.paymentMethod().name(),
                        o.orderStatus().name()
                });
            }
        } catch (Exception ex) {
            showError("Lỗi tải danh sách: " + ex.getMessage()); // Hiển thị lỗi.
        }
    }

    /**
     * Tải lại dữ liệu metadata cần thiết cho việc tạo đơn hàng (ví dụ: danh sách khách hàng, nhân viên, khuyến mãi, xe).
     * @return Đối tượng SaleOrderMetadata chứa các dữ liệu cần thiết.
     */
    private SaleOrderMetadata reloadMetadata() {
        try {
            return controller.loadMetadata();
        } catch (Exception ex) {
            showError("Không tải được dữ liệu danh mục tạo đơn.");
            return SaleOrderMetadata.empty(); // Trả về metadata rỗng nếu có lỗi.
        }
    }

    /**
     * Lấy đối tượng SaleOrderItem đang được chọn trong bảng.
     * @return Optional chứa SaleOrderItem được chọn, hoặc Optional.empty() nếu không có hàng nào được chọn.
     */
    private Optional<SaleOrderItem> selectedOrder() {
        int view = table.getSelectedRow();
        if (view < 0) return Optional.empty();
        int model = table.convertRowIndexToModel(view); // Chuyển đổi chỉ số hàng từ view sang model.
        if (model < 0 || model >= rows.size()) return Optional.empty();
        return Optional.of(rows.get(model));
    }

    /**
     * Hiển thị dialog chi tiết đơn hàng cho đơn hàng được chọn.
     */
    private void showOrderDetail() {
        selectedOrder().ifPresentOrElse(order -> {
            try {
                List<SaleOrderDetailItem> details = controller.findDetailsByOrderId(order.id()); // Lấy chi tiết đơn hàng.
                SaleOrderDetailDialog dialog = new SaleOrderDetailDialog(
                        getDialogParent(), order, details);
                dialog.setVisible(true); // Hiển thị dialog.
            } catch (Exception ex) {
                showError("Lỗi lấy chi tiết đơn: " + ex.getMessage()); // Hiển thị lỗi.
            }
        }, () -> showInfo("Vui lòng chọn một đơn hàng để xem duyệt.")); // Thông báo nếu chưa chọn đơn hàng.
    }

    /**
     * Hủy đơn hàng đã chọn sau khi xác nhận từ người dùng.
     * Kiểm tra các điều kiện để đảm bảo đơn hàng có thể hủy.
     */
    private void cancelSelectedOrder() {
        selectedOrder().ifPresentOrElse(order -> {
            if (order.orderStatus() == OrderStatus.CANCELLED) {
                showInfo("Đơn hàng này đã bị huỷ trước đó.");
                return;
            }
            if (order.orderStatus() == OrderStatus.PAID) {
                showError("Đơn hàng đã được thanh toán, không thể huỷ trực tiếp.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(getDialogParent(),
                    "Bạn có chắc chắn muốn huỷ đơn hàng " + order.orderCode() + " không?\n" +
                    "Số lượng xe sẽ được tự động hoàn trả về kho.",
                    "Xác nhận huỷ đơn", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    controller.cancelOrder(order.id()); // Gọi controller để hủy đơn hàng.
                    showInfo("Đã huỷ đơn hàng thành công.");
                    refreshData(); // Tải lại dữ liệu để cập nhật bảng.
                } catch (Exception ex) {
                    showError("Lỗi khi huỷ đơn: " + ex.getMessage()); // Hiển thị lỗi.
                }
            }
        }, () -> showInfo("Vui lòng chọn một đơn hàng để huỷ.")); // Thông báo nếu chưa chọn đơn hàng.
    }

    /**
     * Hiển thị dialog để tạo một đơn hàng mới.
     */
    private void showCreateOrderDialog() {
        SaleOrderMetadata metadata = reloadMetadata(); // Tải metadata mới nhất.
        // Giả sử có một nhân viên đang đăng nhập (trong hệ thống thực tế sẽ dùng session).
        // Hiện tại, để đơn giản, cho phép chọn nhân viên từ metadata.
        SaleOrderCreateDialog dialog = new SaleOrderCreateDialog(
                getDialogParent(), metadata);
        dialog.setVisible(true); // Hiển thị dialog.

        dialog.getResult().ifPresent(req -> {
            try {
                controller.createOrder(req); // Tạo đơn hàng thông qua controller.
                showInfo("Tạo đơn hàng thành công.");
                refreshData(); // Tải lại dữ liệu để cập nhật bảng.
            } catch (Exception ex) {
                showError(ex.getMessage()); // Hiển thị lỗi.
            }
        });
    }

    // --- Cấu hình giao diện phụ trợ ---
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

    /**
     * Cấu hình chiều rộng ưu tiên và renderer cho các cột trong bảng đơn hàng.
     */
    private void configureColumns() {
        table.getColumnModel().getColumn(0).setMinWidth(0); // Ẩn cột ID.
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        table.getColumnModel().getColumn(1).setPreferredWidth(100); // Mã đơn
        table.getColumnModel().getColumn(2).setPreferredWidth(160); // Khách hàng
        table.getColumnModel().getColumn(3).setPreferredWidth(160); // Nhân viên (Sale)
        table.getColumnModel().getColumn(4).setPreferredWidth(120); // Ngày tạo
        table.getColumnModel().getColumn(5).setPreferredWidth(100); // Tổng tiền
        table.getColumnModel().getColumn(6).setPreferredWidth(100); // Thực thu
        table.getColumnModel().getColumn(7).setPreferredWidth(80);  // PT. TT
        table.getColumnModel().getColumn(8).setPreferredWidth(90);  // Trạng thái

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(5).setCellRenderer(rightRenderer); // Căn phải cột Tổng tiền.
        table.getColumnModel().getColumn(6).setCellRenderer(rightRenderer); // Căn phải cột Thực thu.

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(7).setCellRenderer(center); // Căn giữa cột PT. TT.
        table.getColumnModel().getColumn(8).setCellRenderer(center); // Căn giữa cột Trạng thái.
    }

    /**
     * Hiển thị hộp thoại lỗi.
     * @param msg Thông báo lỗi.
     */
    private void showError(String msg) {
        JOptionPane.showMessageDialog(getDialogParent(), msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Hiển thị hộp thoại thông tin.
     * @param msg Thông báo thông tin.
     */
    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(getDialogParent(), msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Lấy component cha cho các hộp thoại, đảm bảo chúng hiển thị đúng vị trí.
     * @return Component cha.
     */
    private Component getDialogParent() {
        Component owner = DialogUiUtil.appDialogParent(this);
        return owner != null ? owner : this;
    }

    // =========================================================================
    // Dialog Xem chi tiết đơn
    // =========================================================================
    /**
     * Lớp nội tĩnh SaleOrderDetailDialog là một JDialog dùng để hiển thị chi tiết của một đơn hàng.
     * Nó hiển thị thông tin chung về đơn hàng và danh sách các mặt hàng (xe) trong đơn.
     */
    private static final class SaleOrderDetailDialog extends JDialog {
        /**
         * Constructor khởi tạo SaleOrderDetailDialog.
         * @param owner Component cha của dialog.
         * @param order Đối tượng SaleOrderItem chứa thông tin chung về đơn hàng.
         * @param details Danh sách các SaleOrderDetailItem chứa chi tiết từng mặt hàng trong đơn.
         */
        private SaleOrderDetailDialog(Component owner, SaleOrderItem order, List<SaleOrderDetailItem> details) {
            super(resolveOwnerWindow(owner), "Chi tiết đơn hàng " + order.orderCode(), ModalityType.APPLICATION_MODAL);
            setLayout(new BorderLayout(8, 8));

            // Phần Header hiển thị thông tin chung của đơn hàng.
            JPanel header = new JPanel(new GridLayout(3, 2, 8, 4));
            header.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
            header.add(new JLabel("Mã đơn: " + order.orderCode()));
            header.add(new JLabel("Ngày lập: " + (order.orderDate() != null ? order.orderDate().format(DATE_FMT) : "")));
            header.add(new JLabel("Khách hàng: " + order.customerName()));
            header.add(new JLabel("Tổng thực thu: " + String.format("%,.0f", order.finalAmount())));
            header.add(new JLabel("Nhân viên: " + order.staffName()));
            header.add(new JLabel("Trạng thái: " + order.orderStatus()));

            // Bảng hiển thị chi tiết các mặt hàng trong đơn hàng.
            String[] cols = { "Mã xe", "Tên xe", "Đơn giá", "Số lượng", "Giảm (dòng)", "Thành tiền" };
            DefaultTableModel dm = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
            // Thêm dữ liệu chi tiết vào bảng.
            for (SaleOrderDetailItem d : details) {
                dm.addRow(new Object[]{
                        d.carCode(),
                        d.carName(),
                        String.format("%,.0f", d.unitPrice()),
                        d.quantity(),
                        String.format("%,.0f", d.discountAmount()),
                        String.format("%,.0f", d.lineTotal())
                });
            }
            JTable tbl = new JTable(dm);
            JScrollPane scroll = new JScrollPane(tbl);
            scroll.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(0, 12, 0, 12),
                    BorderFactory.createLineBorder(Color.LIGHT_GRAY)
            ));

            // Nút đóng dialog.
            JButton btnClose = new JButton("Đóng");
            btnClose.addActionListener(e -> dispose());
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
            footer.add(btnClose);

            add(header, BorderLayout.NORTH);
            add(scroll, BorderLayout.CENTER);
            add(footer, BorderLayout.SOUTH);

            setSize(600, 400); // Đặt kích thước cố định.
            setLocationRelativeTo(owner); // Đặt vị trí tương đối với component cha.
        }

        /**
         * Giải quyết cửa sổ cha cho dialog.
         * @param owner Component cha.
         * @return Window cha.
         */
        private static Window resolveOwnerWindow(Component owner) {
            if (owner instanceof Window) {
                return (Window) owner;
            }
            return owner == null ? null : SwingUtilities.getWindowAncestor(owner);
        }
    }

    // =========================================================================
    // Dialog Tạo Đơn (Form phức tạp)
    // =========================================================================
    /**
     * Lớp nội tĩnh SaleOrderCreateDialog là một JDialog dùng để tạo đơn hàng mới.
     * Nó cung cấp một form phức tạp cho phép chọn khách hàng, nhân viên, khuyến mãi,
     * phương thức thanh toán, thêm/bớt xe vào giỏ hàng và ghi chú.
     */
    private static final class SaleOrderCreateDialog extends JDialog {
        private final JComboBox<CarLookupItem> cbCustomer = new JComboBox<>(); // ComboBox chọn khách hàng.
        private final JComboBox<CarLookupItem> cbStaff = new JComboBox<>(); // ComboBox chọn nhân viên.
        private final JComboBox<CarLookupItem> cbPromo = new JComboBox<>(); // ComboBox chọn khuyến mãi.
        private final JComboBox<PaymentMethod> cbPaymentMethod = new JComboBox<>(PaymentMethod.values()); // ComboBox chọn phương thức thanh toán.
        private final JTextField txtNote = new JTextField(); // Trường nhập ghi chú.

        private final JComboBox<CarLookupItem> cbCarAdd = new JComboBox<>(); // ComboBox chọn xe để thêm vào giỏ hàng.
        private final JSpinner spinQtyAdd = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1)); // Spinner chọn số lượng xe.

        private final DefaultTableModel cartModel; // Model dữ liệu cho bảng giỏ hàng.
        private final JTable cartTable; // Bảng hiển thị các mặt hàng trong giỏ.
        private final List<OrderDetailRequest> cart = new ArrayList<>(); // Danh sách các mặt hàng trong giỏ hàng.

        private CreateOrderRequest result; // Đối tượng CreateOrderRequest được tạo sau khi người dùng lưu.

        /**
         * Constructor khởi tạo SaleOrderCreateDialog.
         * @param owner Component cha của dialog.
         * @param metadata Đối tượng SaleOrderMetadata chứa các dữ liệu cần thiết (khách hàng, nhân viên, khuyến mãi, xe).
         */
        private SaleOrderCreateDialog(Component owner, SaleOrderMetadata metadata) {
            super(resolveOwnerWindow(owner), "Tạo đơn bán hàng", ModalityType.APPLICATION_MODAL);

            setLayout(new BorderLayout());

            // Điền dữ liệu cho các ComboBox từ metadata.
            metadata.customers().forEach(cbCustomer::addItem);
            metadata.staffs().forEach(cbStaff::addItem);
            cbPromo.addItem(new CarLookupItem(-1L, "NONE", "Không áp dụng (0%)")); // Thêm tùy chọn "Không áp dụng" cho khuyến mãi.
            metadata.promotions().forEach(cbPromo::addItem);
            metadata.cars().forEach(cbCarAdd::addItem);

            // Khởi tạo bảng giỏ hàng.
            cartModel = new DefaultTableModel(new String[]{"ID Xe", "Mã Xe", "Tên Xe", "Số lượng", "Đơn giá", "Thành tiền"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            cartTable = new JTable(cartModel);

            add(buildFormPanel(), BorderLayout.NORTH); // Thêm panel form vào phía Bắc.
            add(buildCartPanel(), BorderLayout.CENTER); // Thêm panel giỏ hàng vào giữa.
            add(buildFooterPanel(), BorderLayout.SOUTH); // Thêm panel footer vào phía Nam.

            setSize(780, 560); // Đặt kích thước cố định.
            setLocationRelativeTo(owner); // Đặt vị trí tương đối với component cha.
        }

        /**
         * Xây dựng panel chứa các trường nhập liệu chính của form tạo đơn hàng.
         * @return JPanel chứa form.
         */
        private JPanel buildFormPanel() {
            JPanel f = new JPanel(new GridLayout(3, 4, 8, 8)); // Sử dụng GridLayout cho form.
            f.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

            f.add(new JLabel("Khách hàng *")); f.add(cbCustomer);
            f.add(new JLabel("Nhân viên *"));  f.add(cbStaff);

            f.add(new JLabel("Khuyến mãi"));   f.add(cbPromo);
            f.add(new JLabel("PT Thanh toán *"));f.add(cbPaymentMethod);

            f.add(new JLabel("Ghi chú"));      
            txtNote.setColumns(15); // Đặt số cột ưu tiên cho trường ghi chú.
            f.add(txtNote);
            f.add(new JLabel("")); f.add(new JLabel("")); // Các JLabel rỗng để căn chỉnh layout.

            return f;
        }

        /**
         * Xây dựng panel chứa chức năng thêm/bớt xe vào giỏ hàng và bảng giỏ hàng.
         * @return JPanel chứa giỏ hàng.
         */
        private JPanel buildCartPanel() {
            JPanel p = new JPanel(new BorderLayout(8, 8));
            p.setBorder(BorderFactory.createTitledBorder("Chi tiết mặt hàng (Xe)")); // Đặt tiêu đề cho panel.

            JPanel addBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
            addBar.add(new JLabel("Chọn xe:"));
            cbCarAdd.setPreferredSize(new Dimension(300, 28)); // Giới hạn độ dài để không đẩy mất các nút.
            addBar.add(cbCarAdd);
            addBar.add(new JLabel("SL:"));
            spinQtyAdd.setPreferredSize(new Dimension(60, 28));
            addBar.add(spinQtyAdd);

            JButton btnAdd = new JButton("Thêm vào giỏ");
            btnAdd.addActionListener(e -> addCarToCart()); // Gán hành động thêm xe vào giỏ.
            JButton btnRemove = new JButton("Xóa dòng");
            btnRemove.addActionListener(e -> removeSelectedCartItem()); // Gán hành động xóa mặt hàng khỏi giỏ.
            
            addBar.add(btnAdd);
            addBar.add(btnRemove);

            p.add(addBar, BorderLayout.NORTH);
            p.add(new JScrollPane(cartTable), BorderLayout.CENTER); // Bọc bảng giỏ hàng trong JScrollPane.

            return p;
        }

        /**
         * Xây dựng panel footer chứa các nút "Hủy" và "Tạo Đơn".
         * @return JPanel chứa footer.
         */
        private JPanel buildFooterPanel() {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
            JButton btnCancel = new JButton("Hủy");
            JButton btnSave = new JButton("Tạo Đơn");
            btnSave.setBackground(UiPalette.PRIMARY);
            btnSave.setForeground(Color.WHITE);

            btnCancel.addActionListener(e -> dispose()); // Đóng dialog khi nhấn Hủy.
            btnSave.addActionListener(e -> submitOrder()); // Gán hành động gửi đơn hàng.

            p.add(btnSave);
            p.add(btnCancel);
            return p;
        }

        /**
         * Thêm xe được chọn vào giỏ hàng.
         * Nếu xe đã có trong giỏ, sẽ cộng dồn số lượng.
         */
        private void addCarToCart() {
            CarLookupItem selectedCar = (CarLookupItem) cbCarAdd.getSelectedItem();
            if (selectedCar == null) return;
            int qty = (Integer) spinQtyAdd.getValue();

            // Trích xuất giá từ text hiển thị của xe (giả định format: "Tên Xe - Màu (Giá)").
            // Cách chuẩn là load entity xe để lấy giá chính xác.
            String display = selectedCar.name();
            BigDecimal unitP = BigDecimal.ZERO;
            try {
                int start = display.lastIndexOf('(');
                int end = display.lastIndexOf(')');
                if(start != -1 && end != -1) {
                    unitP = new BigDecimal(display.substring(start + 1, end).trim());
                }
            } catch (Exception ignored) { }

            // Kiểm tra xem xe đã có trong giỏ hàng chưa.
            boolean found = false;
            for (int i = 0; i < cart.size(); i++) {
                OrderDetailRequest r = cart.get(i);
                if (r.carId().equals(selectedCar.id())) {
                    found = true;
                    int newQty = r.quantity() + qty;
                    cart.set(i, new OrderDetailRequest(r.carId(), newQty, r.unitPrice())); // Cộng dồn số lượng.
                    break;
                }
            }
            if (!found) {
                cart.add(new OrderDetailRequest(selectedCar.id(), qty, unitP)); // Thêm xe mới vào giỏ.
            }

            refreshCartTable(); // Cập nhật lại bảng giỏ hàng.
        }

        /**
         * Xóa mặt hàng được chọn khỏi giỏ hàng.
         */
        private void removeSelectedCartItem() {
            int row = cartTable.getSelectedRow();
            if (row >= 0) {
                cart.remove(row); // Xóa mặt hàng khỏi danh sách.
                refreshCartTable(); // Cập nhật lại bảng giỏ hàng.
            }
        }

        /**
         * Cập nhật lại bảng giỏ hàng dựa trên danh sách `cart`.
         */
        private void refreshCartTable() {
            cartModel.setRowCount(0); // Xóa tất cả các hàng hiện có.
            for (OrderDetailRequest r : cart) {
                String carName = "";
                String carCode = "";
                // Tìm tên và mã xe từ ComboBox `cbCarAdd` dựa trên `carId`.
                for (int i=0; i<cbCarAdd.getItemCount(); i++){
                    if (cbCarAdd.getItemAt(i).id().equals(r.carId())) { 
                        carName = cbCarAdd.getItemAt(i).name();
                        carCode = cbCarAdd.getItemAt(i).code();
                    }
                }
                BigDecimal total = r.unitPrice().multiply(BigDecimal.valueOf(r.quantity())); // Tính thành tiền.
                cartModel.addRow(new Object[]{
                    r.carId(), carCode, carName, r.quantity(), 
                    String.format("%,.0f", r.unitPrice()), 
                    String.format("%,.0f", total)
                });
            }
        }

        /**
         * Gửi yêu cầu tạo đơn hàng sau khi kiểm tra các trường bắt buộc.
         */
        private void submitOrder() {
            if (cart.isEmpty()) {
                JOptionPane.showMessageDialog(DialogUiUtil.appDialogParent(this), "Vui lòng chọn xe vào đơn hàng.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            CarLookupItem cust = (CarLookupItem) cbCustomer.getSelectedItem();
            CarLookupItem staff = (CarLookupItem) cbStaff.getSelectedItem();
            CarLookupItem promo = (CarLookupItem) cbPromo.getSelectedItem();
            PaymentMethod pm = (PaymentMethod) cbPaymentMethod.getSelectedItem();

            if (cust == null || staff == null) {
                JOptionPane.showMessageDialog(DialogUiUtil.appDialogParent(this), "Thiếu thông tin khách hàng / nhân viên.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Long promoId = null;
            if (promo != null && promo.id() > 0) { // Chỉ lấy promoId nếu có khuyến mãi được chọn (không phải "Không áp dụng").
                promoId = promo.id();
            }

            // Tạo đối tượng CreateOrderRequest.
            result = new CreateOrderRequest(
                    cust.id(), staff.id(), promoId, pm, cart, txtNote.getText().trim()
            );
            dispose(); // Đóng dialog.
        }

        /**
         * Trả về đối tượng CreateOrderRequest sau khi dialog đóng.
         * @return Optional chứa CreateOrderRequest nếu người dùng đã lưu, hoặc Optional.empty() nếu hủy.
         */
        public Optional<CreateOrderRequest> getResult() {
            return Optional.ofNullable(result);
        }

        /**
         * Giải quyết cửa sổ cha cho dialog.
         * @param owner Component cha.
         * @return Window cha.
         */
        private static Window resolveOwnerWindow(Component owner) {
            if (owner instanceof Window) {
                return (Window) owner;
            }
            return owner == null ? null : SwingUtilities.getWindowAncestor(owner);
        }
    }
}
