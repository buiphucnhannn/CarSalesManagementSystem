package vn.edu.ute.carsalesms.session;

/**
 * Adapter giu tuong thich voi CurrentSession hien tai.
 */
public class CurrentSessionContextAdapter implements UserSessionContext {

    @Override
    public boolean isAuthenticated() {
        return CurrentSession.isAuthenticated();
    }

    @Override
    public boolean isAdmin() {
        return CurrentSession.isAdmin();
    }

    @Override
    public Long currentBranchId() {
        return CurrentSession.currentBranchId();
    }

    @Override
    public void assertBranchAccess(Long targetBranchId, String targetBranchName) {
        CurrentSession.assertBranchAccess(targetBranchId, targetBranchName);
    }
}

