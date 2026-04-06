package vn.edu.ute.carsalesms.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Lớp tiện ích để băm và xác minh mật khẩu.
 */
public final class PasswordUtil {

	private PasswordUtil() {
	}

	/**
	 * Băm một giá trị bằng thuật toán SHA-256.
	 * @param rawValue giá trị cần băm.
	 * @return giá trị đã băm.
	 */
	public static String sha256(String rawValue) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashBytes = digest.digest(rawValue.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder(hashBytes.length * 2);
			for (byte b : hashBytes) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 is not available", ex);
		}
	}

	/**
	 * Xác minh một giá trị thô so với một giá trị được lưu trữ.
	 * @param rawValue giá trị thô.
	 * @param storedValue giá trị được lưu trữ.
	 * @return true nếu các giá trị khớp, nếu không trả về false.
	 */
	public static boolean matches(String rawValue, String storedValue) {
		if (rawValue == null || storedValue == null) {
			return false;
		}
		if (storedValue.length() == 64 && storedValue.matches("[0-9a-fA-F]+")) {
			return sha256(rawValue).equalsIgnoreCase(storedValue);
		}
		return rawValue.equals(storedValue);
	}
}
