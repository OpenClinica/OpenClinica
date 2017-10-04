package org.akaza.openclinica.service;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by yogi on 9/6/17.
 */
public class Auth0User {
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    private UserContext userContext;
    public Auth0User(DecodedJWT jwt) {
        this.jwt = jwt;
        this.userId = jwt.getSubject();
        Claim contextClaim = jwt.getClaim("https://www.openclinica.com/userContext");
        email = jwt.getClaim("email").asString();
        userContext = new UserContext(contextClaim);
        nickname = jwt.getClaim("nickname").asString();
        givenName = jwt.getClaim("given_name").asString();
        familyName = jwt.getClaim("family_name").asString();
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
