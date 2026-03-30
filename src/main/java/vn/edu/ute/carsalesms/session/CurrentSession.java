package vn.edu.ute.carsalesms.session;

import vn.edu.ute.carsalesms.model.dto.AuthenticatedUser;
import vn.edu.ute.carsalesms.model.enums.StaffRole;

public final class CurrentSession {

	private static AuthenticatedUser currentUser;

	private CurrentSession() {
	}

	public static void setCurrentUser(AuthenticatedUser user) {
		currentUser = user;
	}

	public static AuthenticatedUser getCurrentUser() {
		return currentUser;
	}

	public static boolean isAuthenticated() {
		return currentUser != null;
	}

	public static boolean hasRole(StaffRole role) {
		return currentUser != null && currentUser.role() == role;
	}

	public static void clear() {
		currentUser = null;
	}
}

