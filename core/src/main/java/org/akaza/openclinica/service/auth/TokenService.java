package org.akaza.openclinica.service.auth;

import java.util.Map;

public interface TokenService {
    Map<String, Object> decodeAndVerify (String token);
}
