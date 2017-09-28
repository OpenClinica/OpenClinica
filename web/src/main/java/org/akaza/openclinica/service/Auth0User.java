package org.akaza.openclinica.service;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

/**
 * Created by yogi on 9/6/17.
 */
public class Auth0User {
    private UserContext userContext;
    public Auth0User(DecodedJWT jwt) {
        this.jwt = jwt;
        this.userId = jwt.getSubject();
        Claim contextClaim = jwt.getClaim("http://com.openclinica/userConext");
        email = jwt.getClaim("email").asString();
        userContext = new UserContext(contextClaim);
        nickname = jwt.getClaim("nickname").asString();
        jwt.getClaim("given_name").asString();
        jwt.getClaim("family_name").asString();
    }
    private DecodedJWT jwt;
    private String nickname;
    private String userId;
    private String givenName;
    private String familyName;
    private String email;
    public String getNickname() {
        return nickname;
    }

    public String getUserId() {
        return userId;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public UserContext getUserContext() {
        return userContext;
    }

    public String getEmail() {
        return email;
    }
}
