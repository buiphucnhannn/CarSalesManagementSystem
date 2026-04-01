package vn.edu.ute.carsalesms.service.impl;

import com.itextpdf.text.pdf.BaseFont;
import vn.edu.ute.carsalesms.service.InvoiceFontProvider;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Font provider mac dinh: uu tien font Unicode tren Windows, fallback Helvetica.
 */
public class SystemInvoiceFontProvider implements InvoiceFontProvider {

    @Override
    public BaseFont provideBaseFont() {
        List<String> fontCandidates = List.of(
                "C:/Windows/Fonts/arial.ttf",
                "C:/Windows/Fonts/tahoma.ttf",
                "C:/Windows/Fonts/times.ttf"
        );

        for (String path : fontCandidates) {
            try {
                Path fontPath = Path.of(path);
                if (Files.exists(fontPath)) {
                    return BaseFont.createFont(fontPath.toString(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                }
            } catch (Exception ignored) {
                // Thu tiep font khac.
            }
        }

        try {
            return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
        } catch (Exception ex) {
            throw new IllegalStateException("Khong the khoi tao font cho PDF.", ex);
        }
    }
}

