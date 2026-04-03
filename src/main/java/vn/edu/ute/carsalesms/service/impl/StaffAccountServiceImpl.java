package vn.edu.ute.carsalesms.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import vn.edu.ute.carsalesms.dao.StaffDao;
import vn.edu.ute.carsalesms.model.dto.AccountCommandRequest;
import vn.edu.ute.carsalesms.model.dto.AccountItem;
import vn.edu.ute.carsalesms.model.entity.Account;
import vn.edu.ute.carsalesms.model.entity.Staff;
import vn.edu.ute.carsalesms.model.enums.Status;
import vn.edu.ute.carsalesms.service.StaffAccountService;

public class StaffAccountServiceImpl implements StaffAccountService {

	private final StaffDao staffDao;

	public StaffAccountServiceImpl(StaffDao staffDao) {
		this.staffDao = Objects.requireNonNull(staffDao, "staffDao is required");
	}

	@Override
	public List<AccountItem> getAccounts(String keyword) {
		return staffDao.findAccounts(keyword).stream().map(this::toAccountItem).toList();
	}

	@Override
	public AccountItem createAccount(AccountCommandRequest request) {
		AccountCommandRequest validated = validateAccount(request, false);
		Staff staff = staffDao.findStaffById(validated.staffId())
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên."));

		if (staff.getAccount() != null) {
			throw new IllegalArgumentException("Nhân viên \"" + staff.getFullName() + "\" đã có tài khoản đăng nhập.");
		}

		staffDao.findAccountByUsername(validated.username()).ifPresent(existing -> {
			throw new IllegalArgumentException("Tên đăng nhập đã tồn tại: " + validated.username());
		});

		Account account = new Account();
		account.setUsername(validated.username());
		account.setPasswordHash(hashPassword(validated.rawPassword()));
		account.setStaff(staff);
		account.setStatus(validated.status() == null ? Status.ACTIVE : validated.status());
		account.setLocked(false);
		account.setFailedLoginAttempts(0);

		return toAccountItem(staffDao.saveAccount(account));
	}

	@Override
	public AccountItem updateAccount(AccountCommandRequest request) {
		AccountCommandRequest validated = validateAccount(request, true);

		Account account = staffDao.findAccountById(validated.id())
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản cần cập nhật."));

		staffDao.findAccountByUsername(validated.username())
				.filter(existing -> !existing.getId().equals(validated.id()))
				.ifPresent(existing -> {
					throw new IllegalArgumentException("Tên đăng nhập đã tồn tại: " + validated.username());
				});

		account.setUsername(validated.username());
		if (validated.rawPassword() != null && !validated.rawPassword().isBlank()) {
			account.setPasswordHash(hashPassword(validated.rawPassword()));
		}
		if (validated.status() != null) {
			account.setStatus(validated.status());
		}

		return toAccountItem(staffDao.saveAccount(account));
	}

	@Override
	public AccountItem toggleLock(Long accountId) {
		if (accountId == null) {
			throw new IllegalArgumentException("Id tài khoản không hợp lệ.");
		}
		Account account = staffDao.findAccountById(accountId)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));

		boolean nowLocked = !account.isLocked();
		account.setLocked(nowLocked);
		if (!nowLocked) {
			account.setFailedLoginAttempts(0);
		}

		return toAccountItem(staffDao.saveAccount(account));
	}

	@Override
	public void deleteAccount(Long accountId) {
		if (accountId == null) {
			throw new IllegalArgumentException("Id tài khoản không hợp lệ.");
		}
		staffDao.findAccountById(accountId)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản cần xóa."));
		staffDao.deleteAccountById(accountId);
	}

	private AccountCommandRequest validateAccount(AccountCommandRequest request, boolean requireId) {
		if (request == null) {
			throw new IllegalArgumentException("Dữ liệu tài khoản không hợp lệ.");
		}
		if (requireId && request.id() == null) {
			throw new IllegalArgumentException("Thiếu mã định danh tài khoản.");
		}
		if (!requireId && request.staffId() == null) {
			throw new IllegalArgumentException("Vui lòng chọn nhân viên cần tạo tài khoản.");
		}
		if (request.username() == null || request.username().isBlank()) {
			throw new IllegalArgumentException("Vui lòng nhập tên đăng nhập.");
		}
		if (!requireId && (request.rawPassword() == null || request.rawPassword().isBlank())) {
			throw new IllegalArgumentException("Vui lòng nhập mật khẩu.");
		}
		return new AccountCommandRequest(
				request.id(),
				request.staffId(),
				request.username().trim().toLowerCase(),
				request.rawPassword(),
				request.status() == null ? Status.ACTIVE : request.status()
		);
	}

	private String hashPassword(String rawPassword) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] bytes = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for (byte b : bytes) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Không thể hash mật khẩu.", e);
		}
	}

	private AccountItem toAccountItem(Account account) {
		return new AccountItem(
				account.getId(),
				account.getStaff().getId(),
				account.getStaff().getStaffCode(),
				account.getStaff().getFullName(),
				account.getUsername(),
				account.getStatus(),
				account.isLocked(),
				account.getFailedLoginAttempts(),
				account.getLastLoginAt(),
				account.getCreatedAt()
		);
	}
}

