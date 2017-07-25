package org.akaza.openclinica.controller.helper;

import org.akaza.openclinica.bean.login.UserAccountBean;

/**
 * Created by yogi on 7/25/17.
 */
public class UserAccountHelper {
    private UserAccountBean ub;

    public UserAccountHelper(UserAccountBean ub, boolean isUpdated) {
        this.ub = ub;
        this.isUpdated = isUpdated;
    }

    public UserAccountBean getUb() {
        return ub;

    }

    public void setUb(UserAccountBean ub) {
        this.ub = ub;
    }

    public boolean isUpdated() {
        return isUpdated;
    }

    public void setUpdated(boolean updated) {
        isUpdated = updated;
    }

    boolean isUpdated;

}
