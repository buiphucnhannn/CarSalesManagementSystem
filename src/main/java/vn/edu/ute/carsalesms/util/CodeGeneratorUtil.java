package vn.edu.ute.carsalesms.util;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lớp tiện ích để tạo mã tăng dần với một hậu tố số.
 * Ví dụ: CUST-0001 -> CUST-0002.
 */
public final class CodeGeneratorUtil {

    private static final Pattern TRAILING_NUMBER_PATTERN = Pattern.compile("(\\d+)$");

    private CodeGeneratorUtil() {
    }

    /**
     * Tạo mã tiếp theo từ danh sách các mã hiện có.
     * @param existingCodes danh sách các mã hiện có.
     * @param defaultPrefix tiền tố mặc định.
     * @param numberWidth độ rộng của số.
     * @return mã tiếp theo.
     */
    public static String nextCodeFromExisting(List<String> existingCodes, String defaultPrefix, int numberWidth) {
        Objects.requireNonNull(existingCodes, "existingCodes is required");
        Objects.requireNonNull(defaultPrefix, "defaultPrefix is required");

        int next = existingCodes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(TRAILING_NUMBER_PATTERN::matcher)
                .filter(Matcher::find)
                .map(matcher -> matcher.group(1))
                .mapToInt(value -> {
                    try {
                        return Integer.parseInt(value);
                    } catch (NumberFormatException ex) {
                        return 0;
                    }
                })
                .max()
                .orElse(0) + 1;

        String format = "%0" + Math.max(numberWidth, 1) + "d";
        return defaultPrefix + String.format(format, next);
    }
}
