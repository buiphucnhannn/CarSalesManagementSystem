package vn.edu.ute.carsalesms.view.component;

import vn.edu.ute.carsalesms.controller.SaleOrderController;
import vn.edu.ute.carsalesms.model.dto.*;
import vn.edu.ute.carsalesms.model.enums.OrderStatus;
import vn.edu.ute.carsalesms.model.enums.PaymentMethod;
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
 * Panel quản lý Đơn bán hàng – Module F06.
 *
 * Tính năng chính:
 * - Hiển thị danh sách Đơn bán hàng.
 * - Thêm mới Đơn bán với chi tiết giỏ hàng, áp mã khuyến mãi.
 * - Xem chi tiết các dòng xe trong một đơn.
 */
public class SaleOrderPanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final SaleOrderController controller;

    private static final String[] COLUMNS = {
            "ID", "Mã đơn", "Khách hàng", "Nhân viên (Sale)",
            "Ngày tạo", "Tổng tiền", "Thực thu", "PT. TT", "Trạng thái"
    };

    private final JTextField searchField = new JTextField();
    private final JComboBox<String> statusFilter = new JComboBox<>(new String[]{"Tất cả", "PENDING", "CONFIRMED", "PAID", "CANCELLED"});

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private List<SaleOrderItem> rows = new ArrayList<>();

    private SaleOrderMetadata metadata = SaleOrderMetadata.empty();

    public SaleOrderPanel(SaleOrderController controller) {
        this.controller = Objects.requireNonNull(controller, "controller is required");

        setLayout(new BorderLayout(0, 8));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        configureColumns();

        // Double click xem chi tiết
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showOrderDetail();
                }
            }
        });

        add(buildToolbar(), BorderLayout.NORTH);
        add(createTableCard(table), BorderLayout.CENTER);

        refreshData();
    }

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
        statusFilter.addActionListener(e -> refreshData());

        left.add(new JLabel("Tìm kiếm:"));
        left.add(searchField);
        left.add(searchBtn);
        left.add(new JLabel("Trạng thái:"));
        left.add(statusFilter);

        // Phải
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JButton refreshBtn = createActionButton("Làm mới");
        JButton addBtn     = createActionButton("Tạo đơn");
        addBtn.setBackground(UiPalette.PRIMARY);
        addBtn.setForeground(Color.WHITE);
        JButton viewBtn    = createActionButton("Xem chi tiết");
        JButton cancelBtn  = createActionButton("Huỷ đơn");
        cancelBtn.setBackground(UiPalette.DANGER);
        cancelBtn.setForeground(Color.WHITE);

        refreshBtn.addActionListener(e -> refreshData());
        addBtn.addActionListener(e -> showCreateOrderDialog());
        viewBtn.addActionListener(e -> showOrderDetail());
        cancelBtn.addActionListener(e -> cancelSelectedOrder());

        right.add(refreshBtn);
        right.add(addBtn);
        right.add(viewBtn);
        right.add(cancelBtn);

        panel.add(left, BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    private void refreshData() {
        try {
            OrderStatus fStatus = null;
            String stStr = (String) statusFilter.getSelectedItem();
            if (!"Tất cả".equals(stStr)) {
                fStatus = OrderStatus.valueOf(stStr);
            }
            rows = controller.findOrders(searchField.getText(), fStatus);
            tableModel.setRowCount(0);

            for (SaleOrderItem o : rows) {
                tableModel.addRow(new Object[]{
                        o.id(),
                        o.orderCode(),
                        o.customerName(),
                        o.staffName(),
                        o.orderDate() == null ? "" : o.orderDate().format(DATE_FMT),
                        String.format("%,.0f", o.totalAmount()),
                        String.format("%,.0f", o.finalAmount()),
                        o.paymentMethod().name(),
                        o.orderStatus().name()
                });
            }
        } catch (Exception ex) {
            showError("Lỗi tải danh sách: " + ex.getMessage());
        }
    }

    private void reloadMetadata() {
        try {
            metadata = controller.loadMetadata();
        } catch (Exception ex) {
            metadata = SaleOrderMetadata.empty();
            showError("Không tải được dữ liệu danh mục tạo đơn.");
        }
    }

    private Optional<SaleOrderItem> selectedOrder() {
        int view = table.getSelectedRow();
        if (view < 0) return Optional.empty();
        int model = table.convertRowIndexToModel(view);
        if (model < 0 || model >= rows.size()) return Optional.empty();
        return Optional.of(rows.get(model));
    }

    private void showOrderDetail() {
        selectedOrder().ifPresentOrElse(order -> {
            try {
                List<SaleOrderDetailItem> details = controller.findDetailsByOrderId(order.id());
                SaleOrderDetailDialog dialog = new SaleOrderDetailDialog(
                        SwingUtilities.getWindowAncestor(this), order, details);
                dialog.setVisible(true);
            } catch (Exception ex) {
                showError("Lỗi lấy chi tiết đơn: " + ex.getMessage());
            }
        }, () -> showInfo("Vui lòng chọn một đơn hàng để xem duyệt."));
    }

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

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc chắn muốn huỷ đơn hàng " + order.orderCode() + " không?\n" +
                    "Số lượng xe sẽ được tự động hoàn trả về kho.",
                    "Xác nhận huỷ đơn", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    controller.cancelOrder(order.id());
                    showInfo("Đã huỷ đơn hàng thành công.");
                    refreshData();
                } catch (Exception ex) {
                    showError("Lỗi khi huỷ đơn: " + ex.getMessage());
                }
            }
        }, () -> showInfo("Vui lòng chọn một đơn hàng để huỷ."));
    }

    private void showCreateOrderDialog() {
        reloadMetadata();
        // Giả sử có 1 staff đang login (trong hệ thống thực là session)
        // Hiện tại ta cho chọn Staff ở metadata cho đơn giản (phù hợp quản lý cứng)
        SaleOrderCreateDialog dialog = new SaleOrderCreateDialog(
                SwingUtilities.getWindowAncestor(this), metadata);
        dialog.setVisible(true);

        dialog.getResult().ifPresent(req -> {
            try {
                controller.createOrder(req);
                showInfo("Tạo đơn hàng thành công.");
                refreshData();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });
    }

    // --- Cấu hình giao diện phụ ---
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

    private void configureColumns() {
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(160);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(100);
        table.getColumnModel().getColumn(7).setPreferredWidth(80);
        table.getColumnModel().getColumn(8).setPreferredWidth(90);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(7).setCellRenderer(center);
        table.getColumnModel().getColumn(8).setCellRenderer(center);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    // =========================================================================
    // Dialog Xem chi tiết đơn
    // =========================================================================
    private static final class SaleOrderDetailDialog extends JDialog {
        private SaleOrderDetailDialog(Window owner, SaleOrderItem order, List<SaleOrderDetailItem> details) {
            super(owner, "Chi tiết đơn hàng " + order.orderCode(), ModalityType.APPLICATION_MODAL);
            setLayout(new BorderLayout(8, 8));

            // Info Header
            JPanel header = new JPanel(new GridLayout(3, 2, 8, 4));
            header.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
            header.add(new JLabel("Mã đơn: " + order.orderCode()));
            header.add(new JLabel("Ngày lập: " + (order.orderDate() != null ? order.orderDate().format(DATE_FMT) : "")));
            header.add(new JLabel("Khách hàng: " + order.customerName()));
            header.add(new JLabel("Tổng thực thu: " + String.format("%,.0f", order.finalAmount())));
            header.add(new JLabel("Nhân viên: " + order.staffName()));
            header.add(new JLabel("Trạng thái: " + order.orderStatus()));

            // Table details
            String[] cols = { "Mã xe", "Tên xe", "Đơn giá", "Số lượng", "Giảm (dòng)", "Thành tiền" };
            DefaultTableModel dm = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
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

            JButton btnClose = new JButton("Đóng");
            btnClose.addActionListener(e -> dispose());
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
            footer.add(btnClose);

            add(header, BorderLayout.NORTH);
            add(scroll, BorderLayout.CENTER);
            add(footer, BorderLayout.SOUTH);

            setSize(600, 400);
            setLocationRelativeTo(owner);
        }
    }

    // =========================================================================
    // Dialog Tạo Đơn (Form phức tạp)
    // =========================================================================
    private static final class SaleOrderCreateDialog extends JDialog {
        private final SaleOrderMetadata metadata;

        private final JComboBox<CarLookupItem> cbCustomer = new JComboBox<>();
        private final JComboBox<CarLookupItem> cbStaff = new JComboBox<>();
        private final JComboBox<CarLookupItem> cbPromo = new JComboBox<>();
        private final JComboBox<PaymentMethod> cbPaymentMethod = new JComboBox<>(PaymentMethod.values());
        private final JTextField txtNote = new JTextField();

        private final JComboBox<CarLookupItem> cbCarAdd = new JComboBox<>();
        private final JSpinner spinQtyAdd = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));

        private final DefaultTableModel cartModel;
        private final JTable cartTable;
        private final List<OrderDetailRequest> cart = new ArrayList<>();

        private CreateOrderRequest result;

        private SaleOrderCreateDialog(Window owner, SaleOrderMetadata metadata) {
            super(owner, "Tạo đơn bán hàng", ModalityType.APPLICATION_MODAL);
            this.metadata = metadata;

            setLayout(new BorderLayout());

            // Điền dữ liệu cho các combo
            metadata.customers().forEach(cbCustomer::addItem);
            metadata.staffs().forEach(cbStaff::addItem);
            cbPromo.addItem(new CarLookupItem(-1L, "NONE", "Không áp dụng (0%)"));
            metadata.promotions().forEach(cbPromo::addItem);
            metadata.cars().forEach(cbCarAdd::addItem);

            // Bảng giỏ hàng
            cartModel = new DefaultTableModel(new String[]{"ID Xe", "Mã Xe", "Tên Xe", "Số lượng", "Đơn giá", "Thành tiền"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            cartTable = new JTable(cartModel);

            add(buildFormPanel(), BorderLayout.NORTH);
            add(buildCartPanel(), BorderLayout.CENTER);
            add(buildFooterPanel(), BorderLayout.SOUTH);

            setSize(780, 560);
            setLocationRelativeTo(owner);
        }

        private JPanel buildFormPanel() {
            JPanel f = new JPanel(new GridLayout(3, 4, 8, 8));
            f.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

            f.add(new JLabel("Khách hàng *")); f.add(cbCustomer);
            f.add(new JLabel("Nhân viên *"));  f.add(cbStaff);

            f.add(new JLabel("Khuyến mãi"));   f.add(cbPromo);
            f.add(new JLabel("PT Thanh toán *"));f.add(cbPaymentMethod);

            f.add(new JLabel("Ghi chú"));      
            txtNote.setColumns(15);
            f.add(txtNote);
            f.add(new JLabel("")); f.add(new JLabel(""));

            return f;
        }

        private JPanel buildCartPanel() {
            JPanel p = new JPanel(new BorderLayout(8, 8));
            p.setBorder(BorderFactory.createTitledBorder("Chi tiết mặt hàng (Xe)"));

            JPanel addBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
            addBar.add(new JLabel("Chọn xe:"));
            cbCarAdd.setPreferredSize(new Dimension(300, 28)); // <-- Fix: Giới hạn độ dài để không đẩy mất các nút
            addBar.add(cbCarAdd);
            addBar.add(new JLabel("SL:"));
            spinQtyAdd.setPreferredSize(new Dimension(60, 28));
            addBar.add(spinQtyAdd);

            JButton btnAdd = new JButton("Thêm vào giỏ");
            btnAdd.addActionListener(e -> addCarToCart());
            JButton btnRemove = new JButton("Xóa dòng");
            btnRemove.addActionListener(e -> removeSelectedCartItem());
            
            addBar.add(btnAdd);
            addBar.add(btnRemove);

            p.add(addBar, BorderLayout.NORTH);
            p.add(new JScrollPane(cartTable), BorderLayout.CENTER);

            return p;
        }

        private JPanel buildFooterPanel() {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
            JButton btnCancel = new JButton("Hủy");
            JButton btnSave = new JButton("Tạo Đơn");
            btnSave.setBackground(UiPalette.PRIMARY);
            btnSave.setForeground(Color.WHITE);

            btnCancel.addActionListener(e -> dispose());
            btnSave.addActionListener(e -> submitOrder());

            p.add(btnCancel);
            p.add(btnSave);
            return p;
        }

        private void addCarToCart() {
            CarLookupItem selectedCar = (CarLookupItem) cbCarAdd.getSelectedItem();
            if (selectedCar == null) return;
            int qty = (Integer) spinQtyAdd.getValue();

            // Trích xuất giá từ text mảng hiển thị (chỉ giả định parse từ text: "Tên Xe - Màu (Giá)")
            // Cách chuẩn: load entity xe, gán giá. Để đơn giản ở giao diện:
            // Extract raw price from "Tên Xe - Màu (Giá)"
            String display = selectedCar.name();
            BigDecimal unitP = BigDecimal.ZERO;
            try {
                int start = display.lastIndexOf('(');
                int end = display.lastIndexOf(')');
                if(start != -1 && end != -1) {
                    unitP = new BigDecimal(display.substring(start + 1, end).trim());
                }
            } catch (Exception ignored) { }

            // Kiểm tra trùng xe trong giỏ, nếu có thì cộng dồn
            boolean found = false;
            for (int i = 0; i < cart.size(); i++) {
                OrderDetailRequest r = cart.get(i);
                if (r.carId().equals(selectedCar.id())) {
                    found = true;
                    int newQty = r.quantity() + qty;
                    cart.set(i, new OrderDetailRequest(r.carId(), newQty, r.unitPrice()));
                    break;
                }
            }
            if (!found) {
                cart.add(new OrderDetailRequest(selectedCar.id(), qty, unitP));
            }

            refreshCartTable();
        }

        private void removeSelectedCartItem() {
            int row = cartTable.getSelectedRow();
            if (row >= 0) {
                cart.remove(row);
                refreshCartTable();
            }
        }

        private void refreshCartTable() {
            cartModel.setRowCount(0);
            for (OrderDetailRequest r : cart) {
                String carName = "";
                String carCode = "";
                for (int i=0; i<cbCarAdd.getItemCount(); i++){
                    if (cbCarAdd.getItemAt(i).id().equals(r.carId())) { 
                        carName = cbCarAdd.getItemAt(i).name();
                        carCode = cbCarAdd.getItemAt(i).code();
                    }
                }
                BigDecimal total = r.unitPrice().multiply(BigDecimal.valueOf(r.quantity()));
                cartModel.addRow(new Object[]{
                    r.carId(), carCode, carName, r.quantity(), 
                    String.format("%,.0f", r.unitPrice()), 
                    String.format("%,.0f", total)
                });
            }
        }

        private void submitOrder() {
            if (cart.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn xe vào đơn hàng.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            CarLookupItem cust = (CarLookupItem) cbCustomer.getSelectedItem();
            CarLookupItem staff = (CarLookupItem) cbStaff.getSelectedItem();
            CarLookupItem promo = (CarLookupItem) cbPromo.getSelectedItem();
            PaymentMethod pm = (PaymentMethod) cbPaymentMethod.getSelectedItem();

            if (cust == null || staff == null) {
                JOptionPane.showMessageDialog(this, "Thiếu thông tin khách hàng / nhân viên.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Long promoId = null;
            if (promo != null && promo.id() > 0) {
                promoId = promo.id();
            }

            result = new CreateOrderRequest(
                    cust.id(), staff.id(), promoId, pm, cart, txtNote.getText().trim()
            );
            dispose();
        }

        public Optional<CreateOrderRequest> getResult() {
            return Optional.ofNullable(result);
        }
    }
}
