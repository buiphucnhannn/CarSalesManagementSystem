package vn.edu.ute.carsalesms.session;

import vn.edu.ute.carsalesms.model.dto.AuthenticatedUser;

/**
 * Truu tuong hoa thao tac gan user dang nhap vao session hien hanh.
 */
public interface AuthSessionStore extends CurrentUserProvider {

    void setCurrentUser(AuthenticatedUser user);

    void clear();
}

