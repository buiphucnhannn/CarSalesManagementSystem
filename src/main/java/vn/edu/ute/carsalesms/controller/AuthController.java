package vn.edu.ute.carsalesms.controller;

import vn.edu.ute.carsalesms.model.dto.AuthenticatedUser;
import vn.edu.ute.carsalesms.service.AuthService;

public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public AuthenticatedUser login(String username, char[] passwordChars) {
        String password = passwordChars == null ? "" : new String(passwordChars);
        return authService.login(username, password);
    }
}

