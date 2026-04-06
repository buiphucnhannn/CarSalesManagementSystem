package vn.edu.ute.carsalesms.view.component;

import vn.edu.ute.carsalesms.controller.InvoiceController;
import vn.edu.ute.carsalesms.model.dto.InvoiceItem;
import vn.edu.ute.carsalesms.view.theme.DialogUiUtil;
import vn.edu.ute.carsalesms.view.theme.UiPalette;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp InvoicePanel là giao diện người dùng để quản lý và hiển thị các hóa đơn.
 * Người dùng có thể tìm kiếm hóa đơn, xem danh sách và xuất hóa đơn ra file PDF.
 * Lớp này tương tác với InvoiceController để xử lý logic nghiệp vụ.
 */
public class InvoicePanel extends JPanel {

    // Định dạng ngày và giờ được sử dụng để hiển thị trong bảng.
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final InvoiceController invoiceController;

    private final JTextField txtSearch = new JTextField(20); // Ô nhập liệu để tìm kiếm hóa đơn.
    private final DefaultTableModel tableModel; // Model dữ liệu cho bảng hóa đơn.
    private final JTable table; // Bảng hiển thị danh sách hóa đơn.
    private List<InvoiceItem> rows = new ArrayList<>(); // Danh sách các đối tượng InvoiceItem hiện tại.

    /**
     * Constructor khởi tạo InvoicePanel.
     * @param invoiceController Controller để xử lý các thao tác liên quan đến hóa đơn.
     */
    public InvoicePanel(InvoiceController invoiceController) {
        this.invoiceController = invoiceController;

        setLayout(new BorderLayout(8, 8));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Định nghĩa tên các cột cho bảng hóa đơn.
        String[] cols = {
                "Số HĐ", "Mã đơn (Ref)", "Ngày xuất", "Thuế VAT(10%)", "Tổng thu (Sau thuế)", "Trạng thái", "Ghi chú"
        };
        // Khởi tạo table model, không cho phép chỉnh sửa trực tiếp trên bảng.
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28); // Đặt chiều cao hàng cho bảng.

        // Cấu hình renderer để căn phải cho các cột số tiền.
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer); // Cột Thuế VAT.
        table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer); // Cột Tổng thu.

        add(buildToolbar(), BorderLayout.NORTH); // Thêm thanh công cụ vào phía Bắc của panel.
        add(new JScrollPane(table), BorderLayout.CENTER); // Thêm bảng vào giữa panel, có thanh cuộn.

        refreshData(); // Tải dữ liệu hóa đơn ban đầu.
    }

    /**
     * Xây dựng thanh công cụ (toolbar) cho panel.
     * Bao gồm ô tìm kiếm, nút "Tra cứu" và nút "In hóa đơn (PDF)".
     * @return JPanel chứa thanh công cụ.
     */
    private JPanel buildToolbar() {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);

        // Phần bên trái của toolbar: tìm kiếm.
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        left.setOpaque(false);
        JButton btnSearch = createActionButton("Tra cứu");
        btnSearch.addActionListener(e -> refreshData()); // Gán hành động tìm kiếm khi nhấn nút.
        
        left.add(new JLabel("Mã HĐ / Tiêu chí:"));
        left.add(txtSearch);
        left.add(btnSearch);

        // Phần bên phải của toolbar: nút in hóa đơn.
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        right.setOpaque(false);

        JButton btnPrint = createActionButton("In hóa đơn (PDF)");
        btnPrint.setBackground(UiPalette.PRIMARY); // Đặt màu nền cho nút.
        btnPrint.setForeground(Color.WHITE); // Đặt màu chữ cho nút.
        btnPrint.addActionListener(e -> printInvoice()); // Gán hành động in hóa đơn khi nhấn nút.

        right.add(btnPrint);

        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    /**
     * Tải lại dữ liệu hóa đơn từ controller và cập nhật bảng hiển thị.
     */
    private void refreshData() {
        try {
            rows = invoiceController.findAllInvoices(txtSearch.getText()); // Lấy danh sách hóa đơn từ controller.
            tableModel.setRowCount(0); // Xóa tất cả các hàng hiện có trong bảng.

            // Duyệt qua danh sách hóa đơn và thêm từng hóa đơn vào bảng.
            for (InvoiceItem i : rows) {
                tableModel.addRow(new Object[]{
                        i.invoiceCode(),
                        i.orderCode(),
                        i.issuedDate() != null ? i.issuedDate().format(DATE_FMT) : "",
                        String.format("%,.0f", i.taxAmount()), // Định dạng số tiền thuế.
                        String.format("%,.0f", i.totalAmount()), // Định dạng tổng số tiền.
                        i.invoiceStatus(),
                        i.note()
                });
            }
        } catch (Exception ex) {
            showError("Lỗi tải hóa đơn: " + ex.getMessage()); // Hiển thị lỗi nếu có.
        }
    }

    /**
     * Xử lý việc in hóa đơn ra file PDF.
     * Cho phép người dùng chọn hóa đơn từ bảng, sau đó chọn vị trí lưu file PDF.
     */
    private void printInvoice() {
        int row = table.getSelectedRow(); // Lấy chỉ số hàng được chọn.
        if (row < 0 || row >= rows.size()) {
            showInfo("Vui lòng chọn 1 hóa đơn để in."); // Thông báo nếu chưa chọn hóa đơn.
            return;
        }

        InvoiceItem inv = rows.get(row); // Lấy đối tượng InvoiceItem từ hàng đã chọn.
        JFileChooser chooser = new JFileChooser(); // Tạo hộp thoại chọn file.
        chooser.setDialogTitle("Lưu hóa đơn PDF");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("PDF files (*.pdf)", "pdf")); // Chỉ cho phép chọn file PDF.
        chooser.setSelectedFile(new java.io.File(inv.invoiceCode() + ".pdf")); // Đặt tên file mặc định.

        int result = chooser.showSaveDialog(getDialogParent()); // Hiển thị hộp thoại lưu file.
        if (result != JFileChooser.APPROVE_OPTION) {
            return; // Nếu người dùng hủy, thoát.
        }

        Path outputPath = chooser.getSelectedFile().toPath(); // Lấy đường dẫn file đã chọn.
        // Đảm bảo phần mở rộng là .pdf.
        if (!outputPath.getFileName().toString().toLowerCase().endsWith(".pdf")) {
            outputPath = outputPath.resolveSibling(outputPath.getFileName() + ".pdf");
        }

        // Kiểm tra nếu file đã tồn tại và hỏi người dùng có muốn ghi đè.
        if (outputPath.toFile().exists()) {
            int overwrite = JOptionPane.showConfirmDialog(
                    getDialogParent(),
                    "File đã tồn tại. Bạn có muốn ghi đè không?\n" + outputPath,
                    "Xác nhận ghi đè",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (overwrite != JOptionPane.YES_OPTION) {
                return; // Nếu không ghi đè, thoát.
            }
        }

        try {
            invoiceController.exportInvoicePdf(inv.id(), outputPath); // Gọi controller để xuất PDF.
            showInfo("Đã xuất hóa đơn PDF thành công:\n" + outputPath); // Thông báo thành công.
        } catch (Exception ex) {
            showError("Không thể xuất PDF: " + ex.getMessage()); // Hiển thị lỗi nếu xuất thất bại.
        }
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
