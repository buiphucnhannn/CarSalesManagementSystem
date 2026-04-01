package vn.edu.ute.carsalesms.service.impl;

import com.itextpdf.text.Document;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import vn.edu.ute.carsalesms.model.entity.Invoice;
import vn.edu.ute.carsalesms.model.entity.SaleOrder;
import vn.edu.ute.carsalesms.model.entity.SaleOrderDetail;
import vn.edu.ute.carsalesms.service.InvoiceFontProvider;
import vn.edu.ute.carsalesms.service.InvoicePdfExporter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import java.nio.file.Path;

/**
 * Triển khai xuất file PDF cho hóa đơn theo bố cục thực tế dễ in/lưu trữ.
 */
public class InvoicePdfExporterImpl implements InvoicePdfExporter {

    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final BaseColor TITLE_COLOR = new BaseColor(25, 53, 102);
    private static final BaseColor BORDER_COLOR = new BaseColor(205, 213, 225);
    private static final BaseColor TABLE_HEADER_BG = new BaseColor(236, 241, 250);

    private final Font fontTitle;
    private final Font fontHeader;
    private final Font fontNormal;
    private final Font fontBold;
    private final Font fontTableHeader;
    private final Font fontTableCell;
    private final BaseFont unicodeBaseFont;

    public InvoicePdfExporterImpl() {
        this(new SystemInvoiceFontProvider());
    }

    public InvoicePdfExporterImpl(InvoiceFontProvider invoiceFontProvider) {
        InvoiceFontProvider provider = Objects.requireNonNull(invoiceFontProvider, "invoiceFontProvider must not be null");
        this.unicodeBaseFont = provider.provideBaseFont();
        this.fontTitle = new Font(unicodeBaseFont, 18f, Font.BOLD, TITLE_COLOR);
        this.fontHeader = new Font(unicodeBaseFont, 11f, Font.BOLD);
        this.fontNormal = new Font(unicodeBaseFont, 10f, Font.NORMAL);
        this.fontBold = new Font(unicodeBaseFont, 10f, Font.BOLD);
        this.fontTableHeader = new Font(unicodeBaseFont, 9.5f, Font.BOLD);
        this.fontTableCell = new Font(unicodeBaseFont, 9f, Font.NORMAL);
    }

    @Override
    public void export(Invoice invoice, Path outputPath) {
        Objects.requireNonNull(invoice, "invoice must not be null");
        Objects.requireNonNull(outputPath, "outputPath must not be null");

        Document document = new Document(PageSize.A4, 32, 32, 28, 28);
        FileOutputStream fos = null;
        try {
            if (outputPath.getParent() != null) {
                Files.createDirectories(outputPath.getParent());
            }

            fos = new FileOutputStream(outputPath.toFile());
            PdfWriter.getInstance(document, fos);
            document.open();

            addHeader(document, invoice);
            addParties(document, invoice.getSaleOrder());
            addLineItems(document, invoice.getSaleOrder());
            addTotals(document, invoice);
            addFooter(document, invoice);
        } catch (IOException | DocumentException ex) {
            throw new IllegalStateException("Không thể xuất PDF hóa đơn: " + ex.getMessage(), ex);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ignored) {
                    // Ignore close exception because export result is already decided.
                }
            }
        }
    }

    private void addHeader(Document document, Invoice invoice) throws DocumentException {
        PdfPTable header = new PdfPTable(new float[]{2.4f, 1.6f});
        header.setWidthPercentage(100f);

        PdfPCell left = borderLessCell();
        left.addElement(new Paragraph("CAR SALES MANAGEMENT", fontHeader));
        left.addElement(new Paragraph("Showroom xe ô tô", fontNormal));
        left.addElement(new Paragraph("Hotline: 0900 000 000", fontNormal));
        left.addElement(new Paragraph("Email: contact@carsalesms.vn", fontNormal));

        PdfPCell right = borderLessCell();
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph title = new Paragraph("HOA DON BAN HANG", fontTitle);
        title.setAlignment(Element.ALIGN_RIGHT);
        right.addElement(title);
        right.addElement(rightText("Số HĐ: " + safe(invoice.getInvoiceCode())));
        right.addElement(rightText("Ngày lập: " + formatDateTime(invoice.getIssuedDate())));
        right.addElement(rightText("Trạng thái: " + safe(invoice.getInvoiceStatus() == null ? "" : invoice.getInvoiceStatus().name())));

        header.addCell(left);
        header.addCell(right);

        document.add(header);
        document.add(new Paragraph(" ", fontNormal));
    }

    private void addParties(Document document, SaleOrder order) throws DocumentException {
        PdfPTable parties = new PdfPTable(new float[]{1f, 1f});
        parties.setWidthPercentage(100f);

        PdfPCell buyer = boxedCell();
        buyer.addElement(new Paragraph("Thông tin khách hàng", fontBold));
        buyer.addElement(new Paragraph("Mã KH: " + safe(order.getCustomer().getCustomerCode()), fontNormal));
        buyer.addElement(new Paragraph("Họ tên: " + safe(order.getCustomer().getFullName()), fontNormal));
        buyer.addElement(new Paragraph("Điện thoại: " + safe(order.getCustomer().getPhone()), fontNormal));
        buyer.addElement(new Paragraph("Email: " + safe(order.getCustomer().getEmail()), fontNormal));
        buyer.addElement(new Paragraph("Địa chỉ: " + safe(order.getCustomer().getAddress()), fontNormal));

        PdfPCell seller = boxedCell();
        seller.addElement(new Paragraph("Thông tin bán hàng", fontBold));
        seller.addElement(new Paragraph("Mã đơn: " + safe(order.getOrderCode()), fontNormal));
        seller.addElement(new Paragraph("Ngày tạo đơn: " + formatDateTime(order.getOrderDate()), fontNormal));
        seller.addElement(new Paragraph("Nhân viên: " + safe(order.getStaff().getFullName()), fontNormal));
        seller.addElement(new Paragraph("Chi nhánh: " + safe(order.getStaff().getBranch().getBranchName()), fontNormal));
        seller.addElement(new Paragraph("Hình thức TT: " + safe(order.getPaymentMethod() == null ? "" : order.getPaymentMethod().name()), fontNormal));

        parties.addCell(buyer);
        parties.addCell(seller);

        document.add(parties);
        document.add(new Paragraph(" ", fontNormal));
    }

    private void addLineItems(Document document, SaleOrder order) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{0.55f, 1.05f, 2.45f, 0.5f, 1.55f, 1.45f, 1.55f});
        table.setWidthPercentage(98f);

        addHeaderCell(table, "STT");
        addHeaderCell(table, "Mã xe");
        addHeaderCell(table, "Tên xe");
        addHeaderCell(table, "SL");
        addHeaderCell(table, "Đơn giá");
        addHeaderCell(table, "Giảm");
        addHeaderCell(table, "Thành tiền");

        List<SaleOrderDetail> rows = Optional.ofNullable(order.getSaleOrderDetails())
                .orElse(List.of())
                .stream()
                .sorted(Comparator.comparing(SaleOrderDetail::getId, Comparator.nullsLast(Long::compareTo)))
                .toList();

        IntStream.range(0, rows.size()).forEach(i -> {
            SaleOrderDetail d = rows.get(i);
            table.addCell(bodyCell(String.valueOf(i + 1), Element.ALIGN_CENTER));
            table.addCell(bodyCell(safe(d.getCar() == null ? "" : d.getCar().getCarCode()), Element.ALIGN_LEFT));
            table.addCell(bodyCell(safe(d.getCar() == null ? "" : d.getCar().getCarName()), Element.ALIGN_LEFT));
            table.addCell(bodyCell(String.valueOf(d.getQuantity() == null ? 0 : d.getQuantity()), Element.ALIGN_CENTER));
            table.addCell(moneyCell(d.getUnitPrice()));
            table.addCell(moneyCell(d.getDiscountAmount()));
            table.addCell(moneyCell(d.getLineTotal()));
        });

        if (rows.isEmpty()) {
            PdfPCell empty = bodyCell("Không có chi tiết sản phẩm", Element.ALIGN_CENTER);
            empty.setColspan(7);
            table.addCell(empty);
        }

        document.add(table);
        document.add(new Paragraph(" ", fontNormal));
    }

    private void addTotals(Document document, Invoice invoice) throws DocumentException {
        SaleOrder order = invoice.getSaleOrder();

        BigDecimal totalAmount = order.getTotalAmount() == null ? BigDecimal.ZERO : order.getTotalAmount();
        BigDecimal discountAmount = order.getDiscountAmount() == null ? BigDecimal.ZERO : order.getDiscountAmount();
        BigDecimal vatAmount = invoice.getTaxAmount() == null ? BigDecimal.ZERO : invoice.getTaxAmount();
        BigDecimal finalPayable = invoice.getTotalAmount() == null ? BigDecimal.ZERO : invoice.getTotalAmount();

        PdfPTable totals = new PdfPTable(new float[]{2.9f, 1.6f});
        totals.setWidthPercentage(62f);
        totals.setHorizontalAlignment(Element.ALIGN_RIGHT);

        totals.addCell(totalLabelCell("Tổng tiền:"));
        totals.addCell(totalValueCell(formatMoney(totalAmount)));

        totals.addCell(totalLabelCell("Giảm giá:"));
        totals.addCell(totalValueCell(formatMoney(discountAmount)));

        totals.addCell(totalLabelCell("Thuế VAT (đã được tính trong giá):"));
        totals.addCell(totalValueCell(formatMoney(vatAmount)));

        totals.addCell(totalLabelCell("Tổng thanh toán:"));
        totals.addCell(totalValueCell(formatMoney(finalPayable), true));

        document.add(totals);
        document.add(new Paragraph(" ", fontNormal));
    }

    private void addFooter(Document document, Invoice invoice) throws DocumentException {
        String defaultNote = "Hóa đơn điện tử được sinh từ hệ thống CarSalesMS.";
        String noteText = safe(invoice.getNote()).trim();
        if (noteText.isEmpty()) {
            noteText = defaultNote;
        }
        Paragraph note = new Paragraph("Ghi chú: " + noteText, fontNormal);

        Paragraph thank = new Paragraph("Cảm ơn quý khách đã tin tưởng và sử dụng dịch vụ.", fontBold);
        thank.setAlignment(Element.ALIGN_CENTER);

        document.add(note);
        document.add(new Paragraph(" ", fontNormal));
        document.add(thank);
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, fontTableHeader));
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPadding(5f);
        c.setBackgroundColor(TABLE_HEADER_BG);
        table.addCell(c);
    }

    private PdfPCell bodyCell(String text, int align) {
        PdfPCell c = new PdfPCell(new Phrase(safe(text), fontTableCell));
        c.setHorizontalAlignment(align);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setUseAscender(true);
        c.setUseDescender(true);
        c.setPadding(4f);
        c.setNoWrap(false); // Cho phép xuống dòng nếu số tiền dài (ví dụ tiền tỷ)
        if (align == Element.ALIGN_RIGHT) {
            c.setPaddingLeft(2f);
            c.setPaddingRight(3f);
        }
        return c;
    }

    private PdfPCell moneyCell(BigDecimal amount) {
        String number = formatMoneyNumber(amount);
        float numberFontSize = resolveMoneyNumberFontSize(number.length());
        Font moneyFont = new Font(unicodeBaseFont, numberFontSize, Font.NORMAL);

        // Cho phép xuống dòng VND khi chuỗi tiền dài để giữ cột không vỡ layout.
        boolean wrapCurrency = number.length() >= 11;
        String display = wrapCurrency ? number + "\nVND" : number + " VND";

        PdfPCell c = new PdfPCell(new Phrase(display, moneyFont));
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setUseAscender(true);
        c.setUseDescender(true);
        c.setPadding(4f);
        c.setNoWrap(false);
        c.setLeading(0f, 1.05f);
        return c;
    }

    private float resolveMoneyNumberFontSize(int digitsLen) {
        if (digitsLen >= 16) {
            return 7.4f;
        }
        if (digitsLen >= 13) {
            return 8.0f;
        }
        if (digitsLen >= 11) {
            return 8.6f;
        }
        return 9.0f;
    }

    private PdfPCell totalLabelCell(String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, fontBold));
        c.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(4f);
        return c;
    }

    private PdfPCell totalValueCell(String value) {
        return totalValueCell(value, false);
    }

    private PdfPCell totalValueCell(String value, boolean emphasize) {
        PdfPCell c = new PdfPCell(new Phrase(value, emphasize ? fontHeader : fontNormal));
        c.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(4f);
        c.setNoWrap(true);
        return c;
    }

    private PdfPCell borderLessCell() {
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(0f);
        return c;
    }

    private PdfPCell boxedCell() {
        PdfPCell c = new PdfPCell();
        c.setPadding(8f);
        c.setBorder(Rectangle.BOX);
        c.setBorderColor(BORDER_COLOR);
        return c;
    }

    private Paragraph rightText(String text) {
        Paragraph p = new Paragraph(text, fontNormal);
        p.setAlignment(Element.ALIGN_RIGHT);
        return p;
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "" : value.format(DATE_TIME_FMT);
    }

    private String formatMoney(BigDecimal value) {
        BigDecimal safeNumber = value == null ? BigDecimal.ZERO : value;
        return String.format("%,.0f", safeNumber) + " VND";
    }

    private String formatMoneyNumber(BigDecimal value) {
        BigDecimal safeNumber = value == null ? BigDecimal.ZERO : value;
        return String.format("%,.0f", safeNumber);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}

