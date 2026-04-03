package vn.edu.ute.carsalesms.service;

import java.util.List;
import vn.edu.ute.carsalesms.model.dto.AccountCommandRequest;
import vn.edu.ute.carsalesms.model.dto.AccountItem;

/**
 * Nhom use-case quan ly tai khoan nhan vien.
 */
public interface StaffAccountService {

    List<AccountItem> getAccounts(String keyword);

    AccountItem createAccount(AccountCommandRequest request);

    AccountItem updateAccount(AccountCommandRequest request);

    AccountItem toggleLock(Long accountId);

    void deleteAccount(Long accountId);
}

