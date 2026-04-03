package vn.edu.ute.carsalesms.session;

import vn.edu.ute.carsalesms.model.dto.AuthenticatedUser;

/**
 * Adapter tuong thich voi CurrentSession static.
 */
public class CurrentAuthSessionStore implements AuthSessionStore {

	@Override
	public AuthenticatedUser getCurrentUser() {
		return CurrentSession.getCurrentUser();
	}

	@Override
	public void setCurrentUser(AuthenticatedUser user) {
		CurrentSession.setCurrentUser(user);
	}

	@Override
	public void clear() {
		CurrentSession.clear();
	}
}

