package vn.edu.ute.carsalesms.view.component;

import vn.edu.ute.carsalesms.controller.InvoiceController;
import vn.edu.ute.carsalesms.model.dto.InvoiceItem;
import vn.edu.ute.carsalesms.view.theme.UiPalette;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class InvoicePanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final InvoiceController invoiceController;

    private final JTextField txtSearch = new JTextField(20);
    private final DefaultTableModel tableModel;
    private final JTable table;
    private List<InvoiceItem> rows = new ArrayList<>();

    public InvoicePanel(InvoiceController invoiceController) {
        this.invoiceController = invoiceController;

        setLayout(new BorderLayout(8, 8));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        String[] cols = {
                "Số HĐ", "Mã đơn (Ref)", "Ngày xuất", "Thuế VAT(10%)", "Tổng thu (Sau thuế)", "Trạng thái", "Ghi chú"
        };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        add(buildToolbar(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        refreshData();
    }

    private JPanel buildToolbar() {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        left.setOpaque(false);
        JButton btnSearch = createActionButton("Tra cứu");
        btnSearch.addActionListener(e -> refreshData());
        
        left.add(new JLabel("Mã HĐ / Tiêu chí:"));
        left.add(txtSearch);
        left.add(btnSearch);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        right.setOpaque(false);

        JButton btnPrint = createActionButton("In hóa đơn (PDF)");
        btnPrint.setBackground(UiPalette.PRIMARY);
        btnPrint.setForeground(Color.WHITE);
        btnPrint.addActionListener(e -> printInvoice());

        right.add(btnPrint);

        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private void refreshData() {
        try {
            rows = invoiceController.findAllInvoices(txtSearch.getText());
            tableModel.setRowCount(0);

            for (InvoiceItem i : rows) {
                tableModel.addRow(new Object[]{
                        i.invoiceCode(),
                        i.orderCode(),
                        i.issuedDate() != null ? i.issuedDate().format(DATE_FMT) : "",
                        String.format("%,.0f", i.taxAmount()),
                        String.format("%,.0f", i.totalAmount()),
                        i.invoiceStatus(),
                        i.note()
                });
            }
        } catch (Exception ex) {
            showError("Lỗi tải hóa đơn: " + ex.getMessage());
        }
    }

    private void printInvoice() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= rows.size()) {
            showInfo("Vui lòng chọn 1 hóa đơn để in.");
            return;
        }

        InvoiceItem inv = rows.get(row);
        JOptionPane.showMessageDialog(this, 
            "Đang thực hiện xuất file PDF cho hóa đơn [" + inv.invoiceCode() + "].\n(Tính năng chờ tích hợp JasperReport/iText)", 
            "In PDF Hoá đơn", JOptionPane.INFORMATION_MESSAGE);
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

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }
}
