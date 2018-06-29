package org.akaza.openclinica.core;

public class LockInfo {
    Integer userId;
    String sessionId;

    public LockInfo(Integer userId, String sessionId) {
        this.userId = userId;
        this.sessionId = sessionId;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String toString() {
        return "userId:" + userId + " sessionId:" + sessionId;
    }
}
