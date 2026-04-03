package vn.edu.ute.carsalesms.session;

import vn.edu.ute.carsalesms.model.dto.AuthenticatedUser;

/**
 * Truu tuong hoa nguon lay thong tin user hien tai.
 */
public interface CurrentUserProvider {

    AuthenticatedUser getCurrentUser();
}

