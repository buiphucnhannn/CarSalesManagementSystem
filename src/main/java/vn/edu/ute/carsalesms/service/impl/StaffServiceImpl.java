package vn.edu.ute.carsalesms.service.impl;

import java.util.List;
import java.util.Objects;
import vn.edu.ute.carsalesms.dao.StaffDao;
import vn.edu.ute.carsalesms.model.dto.AccountCommandRequest;
import vn.edu.ute.carsalesms.model.dto.AccountItem;
import vn.edu.ute.carsalesms.model.dto.StaffCommandRequest;
import vn.edu.ute.carsalesms.model.dto.StaffItem;
import vn.edu.ute.carsalesms.model.dto.StaffManagementMetadata;
import vn.edu.ute.carsalesms.model.enums.Status;
import vn.edu.ute.carsalesms.service.StaffAccountService;
import vn.edu.ute.carsalesms.service.StaffProfileService;
import vn.edu.ute.carsalesms.service.StaffService;

public class StaffServiceImpl implements StaffService {

    private final StaffProfileService profileService;
    private final StaffAccountService accountService;

    public StaffServiceImpl(StaffDao staffDao) {
        this(new StaffProfileServiceImpl(staffDao), new StaffAccountServiceImpl(staffDao));
    }

    public StaffServiceImpl(StaffProfileService profileService, StaffAccountService accountService) {
        this.profileService = Objects.requireNonNull(profileService, "profileService is required");
        this.accountService = Objects.requireNonNull(accountService, "accountService is required");
    }

    @Override
    public List<StaffItem> getStaffs(String keyword, Status statusFilter) {
        return profileService.getStaffs(keyword, statusFilter);
    }

    @Override
    public List<StaffItem> getActiveStaffsWithoutAccount() {
        return profileService.getActiveStaffsWithoutAccount();
    }

    @Override
    public StaffManagementMetadata getMetadata() {
        return profileService.getMetadata();
    }

    @Override
    public StaffItem createStaff(StaffCommandRequest request) {
        return profileService.createStaff(request);
    }

    @Override
    public StaffItem updateStaff(StaffCommandRequest request) {
        return profileService.updateStaff(request);
    }

    @Override
    public List<AccountItem> getAccounts(String keyword) {
        return accountService.getAccounts(keyword);
    }

    @Override
    public AccountItem createAccount(AccountCommandRequest request) {
        return accountService.createAccount(request);
    }

    @Override
    public AccountItem updateAccount(AccountCommandRequest request) {
        return accountService.updateAccount(request);
    }

    @Override
    public AccountItem toggleLock(Long accountId) {
        return accountService.toggleLock(accountId);
    }

    @Override
    public void deleteAccount(Long accountId) {
        accountService.deleteAccount(accountId);
    }
}
