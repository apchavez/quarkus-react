package com.products.application.port.in;

import com.products.application.dto.LoginRequest;
import com.products.application.dto.LoginResponse;

public interface AuthServicePort {

    LoginResponse login(LoginRequest request);
}
