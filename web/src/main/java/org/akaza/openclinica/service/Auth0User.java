package org.akaza.openclinica.service;

/**
 * Created by yogi on 9/6/17.
 */
public class Auth0User {
    private UserContext userContext;
    public Auth0User(UserContext userContext) {
        this.userContext = userContext;
    }

    private String nickname;
    private String userId;
    private String givenName;
    private String familyName;
    public String getNickname() {
        return nickname;
    }

    public Object getUserId() {
        return userId;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getFamilyName() {
        return familyName;
    }
}
