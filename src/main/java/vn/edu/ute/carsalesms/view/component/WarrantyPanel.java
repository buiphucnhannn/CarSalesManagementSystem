package vn.edu.ute.carsalesms.view.component;

import vn.edu.ute.carsalesms.model.dto.WarrantyItem;
import vn.edu.ute.carsalesms.model.enums.WarrantyStatus;
import vn.edu.ute.carsalesms.service.WarrantyService;
import vn.edu.ute.carsalesms.view.theme.UiPalette;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class WarrantyPanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final WarrantyService warrantyService;

    private final JTextField txtSearch = new JTextField(15);
    private final DefaultTableModel tableModel;
    private final JTable table;
    private List<WarrantyItem> rowData;

    public WarrantyPanel(WarrantyService warrantyService) {
        this.warrantyService = warrantyService;

        setLayout(new BorderLayout(8, 8));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        String[] cols = {
                "ID", "Mã Bảo Hành Điện Tử", "Chủ Sở Hữu", "Tên Xe", "Mã Đơn Auto", "Kích Hoạt", "Hạn Chót 3 Năm",
                "Trạng Thái B/H", "Sổ Ghi Chú Sửa Chữa"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(32);

        // Ẩn ID (Column 0)
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        add(buildToolbar(), BorderLayout.NORTH);
        add(createTableCard(table), BorderLayout.CENTER);

        refreshData();
    }

    private JPanel buildToolbar() {
        // Sử dụng BoxLayout trục ngang kết hợp Lò xo (Glue) thay vì BorderLayout
        // để khi thu nhỏ cửa sổ, Cụm Tìm Kiếm và Cụm Action bị dồn nhốt 
        // tuyệt đối KHÔNG bao giờ bị đè đè viền lên nhau hay Dính Khối.
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        // --- Cụm Trái: Label & Ô Tìm kiếm ---
        // Thu gọn text ngắn lại
        JLabel lblSearch = new JLabel("Mã BH / Số Đơn / Tên Khách: ");
        lblSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        p.add(lblSearch);
        
        // Thiết lập Hard-size cho TextSearch chống co giãn lộn xộn
        txtSearch.setMaximumSize(new Dimension(220, 36));
        txtSearch.setPreferredSize(new Dimension(180, 36));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        p.add(txtSearch);
        
        p.add(Box.createHorizontalStrut(8)); // Cọc cách ly 8px

        JButton btnSearch = createActionButton("Tìm kiếm");
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSearch.putClientProperty("JButton.buttonType", "roundRect"); // Bẻ góc FlatLaf
        btnSearch.addActionListener(e -> refreshData());
        p.add(btnSearch);

        // --- LÒ XO KÉO GIÃN TRUNG TÂM (Đẩy Cụm Phải Ra Chót) ---
        p.add(Box.createHorizontalGlue());
        p.add(Box.createHorizontalStrut(16)); // Tạo hàng rào Cứng Nhất 16px chống dính (Min-Gap)

        // --- Cụm Phải: Xem Chi Tiết Sổ ---
        JButton btnView = new JButton("Xem Chi Tiết");
        btnView.putClientProperty("JButton.buttonType", "roundRect"); // Ngắt nối khối tự động FlatLaf
        btnView.setFocusPainted(false);
        btnView.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnView.setBackground(Color.WHITE);
        btnView.setForeground(UiPalette.TEXT_PRIMARY); // Màu chữ đen tĩnh
        btnView.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiPalette.BORDER_SOFT, 1),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)));
        btnView.addActionListener(e -> {
            WarrantyItem item = getSelectedItem();
            if (item != null) {
                showWarrantyDetailDialog(item);
            }
        });
        p.add(btnView);

        p.add(Box.createHorizontalStrut(12)); // Cách 12px giữa 2 Nút Phải

        // --- Cụm Phải: Ghi Sổ Bảo Dưỡng ---
        JButton btnNote = new JButton("Ghi Sổ Bảo Dưỡng");
        btnNote.putClientProperty("JButton.buttonType", "roundRect");
        btnNote.setFocusPainted(false);
        btnNote.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNote.setBackground(UiPalette.SUCCESS);
        btnNote.setForeground(Color.WHITE);
        btnNote.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiPalette.SUCCESS, 1),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)));
        btnNote.addActionListener(e -> {
            WarrantyItem item = getSelectedItem();
            if (item != null) {
                if (item.warrantyStatus() == WarrantyStatus.EXPIRED) {
                    showError("Thẻ Hết Hạn 3 Năm! Khách phải tự trả tiền Gara!");
                    return;
                }
                String note = JOptionPane.showInputDialog(this,
                        "Nhập nội dung Sửa/Thay đồ (Ví dụ: Thay bugi, vệ sinh buồng đốt):", "Bảo dưỡng kỳ",
                        JOptionPane.PLAIN_MESSAGE);
                if (note != null && !note.trim().isEmpty()) {
                    warrantyService.addNoteToWarranty(item.id(), note);
                    showInfo("Ghi sổ thành công.");
                    refreshData();
                }
            }
        });
        p.add(btnNote);

        return p;
    }

    private void refreshData() {
        try {
            rowData = warrantyService.findByKeyword(txtSearch.getText());
            tableModel.setRowCount(0);

            for (WarrantyItem i : rowData) {
                tableModel.addRow(new Object[] {
                        i.id(),
                        i.warrantyCode(),
                        i.customerName(),
                        i.carModel(),
                        i.saleOrderCode(),
                        i.startDate() != null ? i.startDate().format(DATE_FMT) : "",
                        i.endDate() != null ? i.endDate().format(DATE_FMT) : "",
                        i.warrantyStatus().name(), // EXPIRED nếu nay > endDate
                        i.note() != null ? i.note() : ""
                });
            }
        } catch (Exception ex) {
            showError("Lỗi tải Sổ Bảo Hành: " + ex.getMessage());
        }
    }

    private WarrantyItem getSelectedItem() {
        int view = table.getSelectedRow();
        if (view < 0) {
            showInfo("Vui lòng click chọn 1 Hợp đồng Bảo Hành trong List.");
            return null;
        }
        int modelIdx = table.convertRowIndexToModel(view);
        Long id = (Long) tableModel.getValueAt(modelIdx, 0);
        return rowData.stream().filter(p -> p.id().equals(id)).findFirst().orElse(null);
    }

    // =========================================================================
    // DIALOG CHI TIẾT BẢO HÀNH (Custom JDialog - cân đối với design system)
    // =========================================================================

    /**
     * Hiển thị JDialog custom với thông tin chi tiết bảo hành của 1 xe.
     * Chia thành 3 vùng: Header, Info Card, Lịch sử bảo dưỡng.
     * Dùng UiPalette cho nhất quán với toàn bộ giao diện project.
     */
    private void showWarrantyDetailDialog(WarrantyItem item) {
        // Tìm Frame cha để dialog hiện đúng vị trí trung tâm
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner instanceof Frame ? (Frame) owner : null,
                "Chi Tiết Sổ Bảo Hành", true);
        dialog.setSize(560, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        // Panel gốc chứa toàn bộ nội dung
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(UiPalette.APP_BACKGROUND);

        // ── HEADER: Gradient xanh giống sidebar ──
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UiPalette.GRADIENT_START,
                        getWidth(), 0, UiPalette.GRADIENT_END);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setLayout(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 64));
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel lblTitle = new JLabel("Sổ Bảo Hành Xe: " + item.carModel());
        lblTitle.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 18));
        lblTitle.setForeground(Color.WHITE);

        // Badge trạng thái ACTIVE / EXPIRED
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
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // Card 1: Thông tin chung
        JPanel infoCard = createDetailCard();
        infoCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Dòng 1: Mã thẻ
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        infoCard.add(createFieldLabel("Mã thẻ BH:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        infoCard.add(createFieldValue(item.warrantyCode()), gbc);

        // Dòng 2: Chủ xe
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        infoCard.add(createFieldLabel("Chủ sở hữu:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        infoCard.add(createFieldValue(item.customerName()), gbc);

        // Dòng 3: Mã đơn
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        infoCard.add(createFieldLabel("Mã đơn hàng:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        infoCard.add(createFieldValue(item.saleOrderCode()), gbc);

        // Dòng 4: Ngày kích hoạt - Ngày hết hạn
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
        body.add(Box.createVerticalStrut(10));

        // Card 2: Lịch sử bảo dưỡng
        JPanel historyCard = createDetailCard();
        historyCard.setLayout(new BorderLayout(0, 6));

        JLabel lblHistory = new JLabel("Lịch sử bảo dưỡng / sửa chữa");
        lblHistory.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 14));
        lblHistory.setForeground(UiPalette.TEXT_PRIMARY);
        historyCard.add(lblHistory, BorderLayout.NORTH);

        // Tách chuỗi ghi chú dạng " | " thành từng dòng riêng biệt
        JTextArea txtHistory = new JTextArea();
        txtHistory.setEditable(false);
        txtHistory.setLineWrap(true);
        txtHistory.setWrapStyleWord(true);
        txtHistory.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtHistory.setForeground(UiPalette.TEXT_SECONDARY);
        txtHistory.setBackground(UiPalette.SURFACE_ELEVATED);
        txtHistory.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        if (item.note() != null && !item.note().isBlank()) {
            // Phân tách mỗi record bảo dưỡng bằng dấu "|" thành dòng riêng
            String formatted = item.note().replace(" | ", "\n\n");
            txtHistory.setText(formatted);
        } else {
            txtHistory.setText("Chưa có ghi chú bảo dưỡng nào.");
            txtHistory.setForeground(UiPalette.TEXT_MUTED);
        }

        JScrollPane historyScroll = new JScrollPane(txtHistory);
        historyScroll.setBorder(BorderFactory.createLineBorder(UiPalette.BORDER_SOFT));
        historyScroll.setPreferredSize(new Dimension(0, 180));
        historyCard.add(historyScroll, BorderLayout.CENTER);

        body.add(historyCard);

        // ── FOOTER: Nút Đóng ──
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        footer.setOpaque(false);
        JButton btnClose = createActionButton("Đóng");
        btnClose.setPreferredSize(new Dimension(120, 36));
        btnClose.addActionListener(ev -> dialog.dispose());
        footer.add(btnClose);

        root.add(header, BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    /**
     * Tạo Card nền trắng bo viền mềm (nhất quán với createTableCard).
     */
    private JPanel createDetailCard() {
        JPanel card = new JPanel();
        card.setBackground(UiPalette.SURFACE_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiPalette.BORDER_SOFT),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        return card;
    }

    /** Label trái (tên trường) - màu xám nhạt, font semibold */
    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
        lbl.setForeground(UiPalette.TEXT_SECONDARY);
        return lbl;
    }

    /** Label phải (giá trị) - màu đen đậm, font thường */
    private JLabel createFieldValue(String text) {
        JLabel lbl = new JLabel(text != null ? text : "N/A");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(UiPalette.TEXT_PRIMARY);
        return lbl;
    }

    // =========================================================================
    // UI HELPER
    // =========================================================================

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

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }
}
