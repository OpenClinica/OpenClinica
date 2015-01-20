package org.akaza.openclinica.web.pmanage;

public class Authorization {
  private Study study = null;    
  private AuthorizationStatus authorizationStatus;
  public Study getStudy() {
      return study;
  }

  public void setStudy(Study study) {
      this.study = study;
  }

  public AuthorizationStatus getAuthorizationStatus() {
      return authorizationStatus;
  }

  public void setAuthorizationStatus(AuthorizationStatus authorizationStatus) {
      this.authorizationStatus = authorizationStatus;
  }
}
