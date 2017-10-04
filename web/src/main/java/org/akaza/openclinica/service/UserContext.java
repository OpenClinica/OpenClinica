package org.akaza.openclinica.service;

import com.auth0.jwt.interfaces.Claim;

/**
 * Created by yogi on 9/6/17.
 */
public class UserContext {
    Claim contextClaim;
    String userType;
    public UserContext(Claim contextClaim) {
        this.contextClaim = contextClaim;

    }

    public String getUserType() {
        return userType;
    }
}
