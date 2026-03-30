package vn.edu.ute.carsalesms.service;

import vn.edu.ute.carsalesms.model.dto.AuthenticatedUser;

public interface AuthService {

    AuthenticatedUser login(String username, String rawPassword);
}

