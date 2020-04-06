package core.org.akaza.openclinica.service.auth;

import java.util.Map;

public interface TokenService {
    Map<String, Object> decodeAndVerify (String token);
    String getUserType(String token);
    String getCustomerUuid(String token);
}
