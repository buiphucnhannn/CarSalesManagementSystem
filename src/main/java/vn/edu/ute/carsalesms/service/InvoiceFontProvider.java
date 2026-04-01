package vn.edu.ute.carsalesms.service;

import com.itextpdf.text.pdf.BaseFont;

/**
 * Cung cap font cho luong xuat PDF hoa don (DIP).
 */
public interface InvoiceFontProvider {

    BaseFont provideBaseFont();
}

