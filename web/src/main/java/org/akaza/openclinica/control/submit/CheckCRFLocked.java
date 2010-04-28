package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.OCSessionListener;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.bean.login.UserAccountBean;

import javax.servlet.http.HttpSessionListener;

/**
 * Created by IntelliJ IDEA.
 * User: A. Hamid
 * Date: Apr 12, 2010
 * Time: 3:32:44 PM
 */
public class CheckCRFLocked extends SecureController {
    protected void processRequest() throws Exception {
        String user;
        String ecId = request.getParameter("ecId");
        if (ecId != null && !ecId.equals("")) {
            if (unavailableCRFList.containsKey(Integer.parseInt(ecId))) {
                user = (String)unavailableCRFList.get(Integer.parseInt(ecId));
                response.getWriter().print(resword.getString("CRF_unavailable") +
                        "\n"+user + " "+ resword.getString("Currently_entering_data")
                        + "\n"+resword.getString("Leave_the_CRF"));
            } else {
                response.getWriter().print("true");
            }
            return;
        }else if(request.getParameter("userName")!=null) {
            remove(request.getParameter("userName"));
            return;
        }
    }

    public void remove(String userName){
        removeLockedCRF(userName);
    }

    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        return;
    }
}
