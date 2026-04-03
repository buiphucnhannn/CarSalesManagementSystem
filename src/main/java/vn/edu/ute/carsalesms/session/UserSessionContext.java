package vn.edu.ute.carsalesms.session;

/**
 * Truu tuong hoa ngu canh phien dang nhap de service khong phu thuoc static global state.
 */
public interface UserSessionContext {

    boolean isAuthenticated();

    boolean isAdmin();

    Long currentBranchId();

    void assertBranchAccess(Long targetBranchId, String targetBranchName);
}

