package vn.edu.ute.carsalesms.view.component;

import vn.edu.ute.carsalesms.model.dto.WarrantyItem;
import vn.edu.ute.carsalesms.model.enums.WarrantyStatus;
import vn.edu.ute.carsalesms.service.WarrantyService;
import vn.edu.ute.carsalesms.view.theme.DialogUiUtil;
import vn.edu.ute.carsalesms.view.theme.UiPalette;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Lớp WarrantyPanel là giao diện người dùng để quản lý và hiển thị thông tin bảo hành xe.
 * Người dùng có thể tìm kiếm các thẻ bảo hành, xem chi tiết thông tin bảo hành,
 * và ghi chú các lần bảo dưỡng/sửa chữa vào sổ bảo hành điện tử.
 * Lớp này tương tác với WarrantyService để xử lý logic nghiệp vụ.
 */
public class WarrantyPanel extends JPanel {

    // Định dạng ngày tháng được sử dụng để hiển thị.
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final WarrantyService warrantyService; // Service để tương tác với dữ liệu bảo hành.

    private final JTextField txtSearch = new JTextField(15); // Ô nhập liệu để tìm kiếm.
    private final DefaultTableModel tableModel; // Model dữ liệu cho bảng bảo hành.
    private final JTable table; // Bảng hiển thị danh sách bảo hành.
    private List<WarrantyItem> rowData; // Danh sách các đối tượng WarrantyItem hiện tại.

    /**
     * Constructor khởi tạo WarrantyPanel.
     * @param warrantyService Service để xử lý các thao tác liên quan đến bảo hành.
     */
    public WarrantyPanel(WarrantyService warrantyService) {
        this.warrantyService = warrantyService;

        setLayout(new BorderLayout(8, 8)); // Sử dụng BorderLayout với khoảng cách 8px.
        setOpaque(false); // Đặt panel không trong suốt.
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12)); // Đặt đường viền rỗng.

        // Định nghĩa tên các cột cho bảng bảo hành.
        String[] cols = {
                "ID", "Mã Bảo Hành Điện Tử", "Chủ Sở Hữu", "Tên Xe", "Mã Đơn Auto", "Kích Hoạt", "Hạn Chót 3 Năm",
                "Trạng Thái B/H", "Sổ Ghi Chú Sửa Chữa"
        };
        // Khởi tạo table model, không cho phép chỉnh sửa trực tiếp trên bảng.
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(32); // Đặt chiều cao hàng.

        // Ẩn cột ID (cột 0) vì nó chỉ dùng nội bộ.
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter); // Gán bộ sắp xếp cho bảng.

        add(buildToolbar(), BorderLayout.NORTH); // Thêm thanh công cụ vào phía Bắc.
        add(createTableCard(table), BorderLayout.CENTER); // Thêm bảng vào giữa, bọc trong card.

        refreshData(); // Tải dữ liệu bảo hành ban đầu.
    }

    /**
     * Xây dựng thanh công cụ (toolbar) cho panel.
     * Sử dụng BoxLayout để sắp xếp các thành phần một cách linh hoạt,
     * đảm bảo các cụm tìm kiếm và hành động không bị chồng chéo khi thay đổi kích thước cửa sổ.
     * @return JPanel chứa thanh công cụ.
     */
    private JPanel buildToolbar() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS)); // Sử dụng BoxLayout theo trục X.
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        // --- Cụm Trái: Label & Ô Tìm kiếm ---
        JLabel lblSearch = new JLabel("Mã BH / Số Đơn / Tên Khách: ");
        lblSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        p.add(lblSearch);
        
        // Thiết lập kích thước cố định cho ô tìm kiếm để tránh co giãn không mong muốn.
        txtSearch.setMaximumSize(new Dimension(220, 36));
        txtSearch.setPreferredSize(new Dimension(180, 36));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        p.add(txtSearch);
        
        p.add(Box.createHorizontalStrut(8)); // Tạo khoảng cách 8px.

        JButton btnSearch = createActionButton("Tìm kiếm");
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Đổi con trỏ chuột khi di chuyển qua nút.
        btnSearch.putClientProperty("JButton.buttonType", "roundRect"); // Áp dụng kiểu nút bo tròn của FlatLaf.
        btnSearch.addActionListener(e -> refreshData()); // Gán hành động tìm kiếm.
        p.add(btnSearch);

        // --- LÒ XO KÉO GIÃN TRUNG TÂM (Đẩy Cụm Phải Ra Chót) ---
        p.add(Box.createHorizontalGlue()); // Tạo một "lò xo" để đẩy các thành phần sang hai bên.
        p.add(Box.createHorizontalStrut(16)); // Tạo khoảng cách tối thiểu 16px giữa các cụm.

        // --- Cụm Phải: Xem Chi Tiết Sổ ---
        JButton btnView = new JButton("Xem Chi Tiết");
        btnView.putClientProperty("JButton.buttonType", "roundRect"); // Kiểu nút bo tròn.
        btnView.setFocusPainted(false);
        btnView.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnView.setBackground(Color.WHITE);
        btnView.setForeground(UiPalette.TEXT_PRIMARY); // Màu chữ đen tĩnh.
        btnView.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiPalette.BORDER_SOFT, 1),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)));
        btnView.addActionListener(e -> {
            WarrantyItem item = getSelectedItem();
            if (item != null) {
                showWarrantyDetailDialog(item); // Hiển thị dialog chi tiết bảo hành.
            }
        });
        p.add(btnView);

        p.add(Box.createHorizontalStrut(12)); // Khoảng cách 12px giữa hai nút bên phải.

        // --- Cụm Phải: Ghi Sổ Bảo Dưỡng ---
        JButton btnNote = new JButton("Ghi Sổ Bảo Dưỡng");
        btnNote.putClientProperty("JButton.buttonType", "roundRect"); // Kiểu nút bo tròn.
        btnNote.setFocusPainted(false);
        btnNote.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNote.setBackground(UiPalette.SUCCESS); // Màu nền xanh lá cây.
        btnNote.setForeground(Color.WHITE); // Màu chữ trắng.
        btnNote.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiPalette.SUCCESS, 1),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)));
        btnNote.addActionListener(e -> {
            WarrantyItem item = getSelectedItem();
            if (item != null) {
                // Kiểm tra trạng thái bảo hành trước khi cho phép ghi chú.
                if (item.warrantyStatus() == WarrantyStatus.EXPIRED) {
                    showError("Thẻ Hết Hạn 3 Năm! Khách phải tự trả tiền Gara!");
                    return;
                }
                String note = JOptionPane.showInputDialog(getDialogParent(),
                        "Nhập nội dung Sửa/Thay đồ (Ví dụ: Thay bugi, vệ sinh buồng đốt):", "Bảo dưỡng kỳ",
                        JOptionPane.PLAIN_MESSAGE);
                if (note != null && !note.trim().isEmpty()) {
                    warrantyService.addNoteToWarranty(item.id(), note); // Thêm ghi chú vào bảo hành.
                    showInfo("Ghi sổ thành công.");
                    refreshData(); // Tải lại dữ liệu để cập nhật bảng.
                }
            }
        });
        p.add(btnNote);

        return p;
    }

    /**
     * Tải lại dữ liệu bảo hành từ service và cập nhật bảng hiển thị.
     */
    private void refreshData() {
        try {
            rowData = warrantyService.findByKeyword(txtSearch.getText()); // Lấy dữ liệu từ service.
            tableModel.setRowCount(0); // Xóa tất cả các hàng hiện có.

            // Duyệt qua danh sách dữ liệu và thêm từng mục vào bảng.
            for (WarrantyItem i : rowData) {
                tableModel.addRow(new Object[] {
                        i.id(),
                        i.warrantyCode(),
                        i.customerName(),
                        i.carModel(),
                        i.saleOrderCode(),
                        i.startDate() != null ? i.startDate().format(DATE_FMT) : "",
                        i.endDate() != null ? i.endDate().format(DATE_FMT) : "",
                        i.warrantyStatus().name(), // Hiển thị trạng thái bảo hành.
                        i.note() != null ? i.note() : ""
                });
            }
        } catch (Exception ex) {
            showError("Lỗi tải Sổ Bảo Hành: " + ex.getMessage()); // Hiển thị lỗi.
        }
    }

    /**
     * Lấy đối tượng WarrantyItem đang được chọn trong bảng.
     * @return WarrantyItem được chọn, hoặc null nếu không có hàng nào được chọn hoặc không tìm thấy.
     */
    private WarrantyItem getSelectedItem() {
        int view = table.getSelectedRow();
        if (view < 0) {
            showInfo("Vui lòng click chọn 1 Hợp đồng Bảo Hành trong List.");
            return null;
        }
        int modelIdx = table.convertRowIndexToModel(view); // Chuyển đổi chỉ số hàng từ view sang model.
        Long id = (Long) tableModel.getValueAt(modelIdx, 0); // Lấy ID từ cột ẩn.
        return rowData.stream().filter(p -> p.id().equals(id)).findFirst().orElse(null); // Tìm đối tượng tương ứng.
    }

    // =========================================================================
    // DIALOG CHI TIẾT BẢO HÀNH (Custom JDialog - cân đối với design system)
    // =========================================================================

    /**
     * Hiển thị một JDialog tùy chỉnh với thông tin chi tiết bảo hành của một chiếc xe.
     * Dialog được chia thành 3 vùng: Header, Info Card, và Lịch sử bảo dưỡng,
     * sử dụng UiPalette để đảm bảo tính nhất quán về giao diện.
     * @param item Đối tượng WarrantyItem chứa thông tin bảo hành cần hiển thị.
     */
    private void showWarrantyDetailDialog(WarrantyItem item) {
        // Tìm Frame cha để dialog hiển thị đúng vị trí trung tâm.
        Component parent = getDialogParent();
        Window owner = parent instanceof Window w ? w : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner instanceof Frame ? (Frame) owner : null,
                "Chi Tiết Sổ Bảo Hành", true); // Tạo dialog modal.
        dialog.setSize(560, 520); // Đặt kích thước cố định.
        dialog.setLocationRelativeTo(parent); // Đặt vị trí tương đối với component cha.
        dialog.setResizable(false); // Không cho phép thay đổi kích thước.

        // Panel gốc chứa toàn bộ nội dung của dialog.
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(UiPalette.APP_BACKGROUND);

        // ── HEADER: Gradient màu xanh giống sidebar ──
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Tạo hiệu ứng gradient màu.
                GradientPaint gp = new GradientPaint(0, 0, UiPalette.GRADIENT_START,
                        getWidth(), 0, UiPalette.GRADIENT_END);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setLayout(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 64)); // Đặt chiều cao cố định.
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel lblTitle = new JLabel("Sổ Bảo Hành Xe: " + item.carModel());
        lblTitle.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 18));
        lblTitle.setForeground(Color.WHITE);

        // Badge hiển thị trạng thái ACTIVE / EXPIRED.
        String statusText = item.warrantyStatus() == WarrantyStatus.ACTIVE ? "ACTIVE" : "EXPIRED";
        Color badgeBg = item.warrantyStatus() == WarrantyStatus.ACTIVE
                ? UiPalette.SUCCESS
                : UiPalette.DANGER;
        JLabel lblBadge = new JLabel(statusText, SwingConstants.CENTER);
        lblBadge.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
        lblBadge.setForeground(Color.WHITE);
        lblBadge.setOpaque(true);
        lblBadge.setBackground(badgeBg);
        lblBadge.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

        header.add(lblTitle, BorderLayout.WEST);
        header.add(lblBadge, BorderLayout.EAST);

        // ── BODY: 2 Cards xếp dọc ──
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS)); // Sắp xếp các card theo chiều dọc.
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // Card 1: Thông tin chung
        JPanel infoCard = createDetailCard();
        infoCard.setLayout(new GridBagLayout()); // Sử dụng GridBagLayout để căn chỉnh các trường.
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8); // Khoảng cách giữa các thành phần.
        gbc.anchor = GridBagConstraints.WEST; // Căn trái.
        gbc.fill = GridBagConstraints.HORIZONTAL; // Lấp đầy chiều ngang.

        // Dòng 1: Mã thẻ bảo hành.
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        infoCard.add(createFieldLabel("Mã thẻ BH:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        infoCard.add(createFieldValue(item.warrantyCode()), gbc);

        // Dòng 2: Chủ xe.
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        infoCard.add(createFieldLabel("Chủ sở hữu:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        infoCard.add(createFieldValue(item.customerName()), gbc);

        // Dòng 3: Mã đơn hàng.
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        infoCard.add(createFieldLabel("Mã đơn hàng:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        infoCard.add(createFieldValue(item.saleOrderCode()), gbc);

        // Dòng 4: Ngày kích hoạt - Ngày hết hạn.
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        infoCard.add(createFieldLabel("Thời hạn BH:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        String rangeText = (item.startDate() != null ? item.startDate().format(DATE_FMT) : "N/A")
                + "  -->  "
                + (item.endDate() != null ? item.endDate().format(DATE_FMT) : "N/A");
        infoCard.add(createFieldValue(rangeText), gbc);

        infoCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, infoCard.getPreferredSize().height));
        body.add(infoCard);
        body.add(Box.createVerticalStrut(10)); // Khoảng cách dọc 10px.

        // Card 2: Lịch sử bảo dưỡng
        JPanel historyCard = createDetailCard();
        historyCard.setLayout(new BorderLayout(0, 6));

        JLabel lblHistory = new JLabel("Lịch sử bảo dưỡng / sửa chữa");
        lblHistory.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        lblHistory.setForeground(UiPalette.TEXT_PRIMARY);
        historyCard.add(lblHistory, BorderLayout.NORTH);

        // Hiển thị ghi chú lịch sử bảo dưỡng trong JTextArea.
        JTextArea txtHistory = new JTextArea();
        txtHistory.setEditable(false);
        txtHistory.setLineWrap(true);
        txtHistory.setWrapStyleWord(true);
        txtHistory.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtHistory.setForeground(UiPalette.TEXT_SECONDARY);
        txtHistory.setBackground(UiPalette.SURFACE_ELEVATED);
        txtHistory.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        if (item.note() != null && !item.note().isBlank()) {
            // Phân tách mỗi bản ghi bảo dưỡng bằng dấu "|" thành các dòng riêng biệt.
            String formatted = item.note().replace(" | ", "\n\n");
            txtHistory.setText(formatted);
        } else {
            txtHistory.setText("Chưa có ghi chú bảo dưỡng nào.");
            txtHistory.setForeground(UiPalette.TEXT_MUTED);
        }

        JScrollPane historyScroll = new JScrollPane(txtHistory); // Bọc JTextArea trong JScrollPane.
        historyScroll.setBorder(BorderFactory.createLineBorder(UiPalette.BORDER_SOFT));
        historyScroll.setPreferredSize(new Dimension(0, 180));
        historyCard.add(historyScroll, BorderLayout.CENTER);

        body.add(historyCard);

        // ── FOOTER: Nút Đóng ──
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        footer.setOpaque(false);
        JButton btnClose = createActionButton("Đóng");
        btnClose.setPreferredSize(new Dimension(120, 36));
        btnClose.addActionListener(ev -> dialog.dispose()); // Đóng dialog khi nhấn nút.
        footer.add(btnClose);

        root.add(header, BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(root); // Đặt panel gốc làm nội dung của dialog.
        dialog.setVisible(true); // Hiển thị dialog.
    }

    /**
     * Tạo một JPanel có nền trắng và đường viền mềm, nhất quán với các card khác.
     * @return JPanel đã được định kiểu.
     */
    private JPanel createDetailCard() {
        JPanel card = new JPanel();
        card.setBackground(UiPalette.SURFACE_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiPalette.BORDER_SOFT),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        return card;
    }

    /**
     * Tạo một JLabel cho nhãn trường (bên trái), với màu xám nhạt và font semibold.
     * @param text Nội dung nhãn.
     * @return JLabel đã được định kiểu.
     */
    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        lbl.setForeground(UiPalette.TEXT_SECONDARY);
        return lbl;
    }

    /**
     * Tạo một JLabel cho giá trị trường (bên phải), với màu đen đậm và font thường.
     * @param text Nội dung giá trị.
     * @return JLabel đã được định kiểu.
     */
    private JLabel createFieldValue(String text) {
        JLabel lbl = new JLabel(text != null ? text : "N/A");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(UiPalette.TEXT_PRIMARY);
        return lbl;
    }

    // =========================================================================
    // UI HELPER
    // =========================================================================

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
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
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
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
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
}
