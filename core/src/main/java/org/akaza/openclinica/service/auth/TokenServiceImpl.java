package org.akaza.openclinica.service.auth;

import org.akaza.openclinica.service.user.CreateUserCoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.common.util.JsonParser;
import org.springframework.security.oauth2.common.util.JsonParserFactory;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.stereotype.Service;
import sun.security.rsa.RSAPublicKeyImpl;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Map;

@Service("tokenService")
public class TokenServiceImpl implements TokenService {
    private JsonParser objectMapper = JsonParserFactory.create();
    final String EXP = "exp";

    @Autowired
    private DataSource dataSource;

    @Autowired
    CreateUserCoreService userService;

    private static final String PUBLIC_KEY_LOCATION = "/etc/ssl/certs/keycloak.cer";
    private static final String X509_CERTFICATE = "X509";
    private static final String API_AUDIENCE = "https://www.openclinica.com";

    public Map<String, Object> decodeAndVerify (String token) {
        File file = new File(PUBLIC_KEY_LOCATION);
        try (FileInputStream inputStream = new FileInputStream(file)) {
            CertificateFactory certificateFactory = CertificateFactory.getInstance(X509_CERTFICATE);
            Certificate certificate = certificateFactory.generateCertificate(inputStream);
            RSAPublicKeyImpl publicKey = (RSAPublicKeyImpl) certificate.getPublicKey();
            RsaVerifier verifier = new RsaVerifier(publicKey);
            Jwt jwt = JwtHelper.decodeAndVerify(token, verifier);
            String content = jwt.getClaims();
            Map<String, Object> map = objectMapper.parseMap(content);
            if (map.containsKey(EXP) && map.get(EXP) instanceof Integer) {
                Integer intValue = (Integer) map.get(EXP);
                map.put(EXP, new Long(intValue));
            }
            return map;
        }
        catch (Exception e) {
            throw new InvalidTokenException("Cannot convert access token to JSON", e);
        }
    }
}
