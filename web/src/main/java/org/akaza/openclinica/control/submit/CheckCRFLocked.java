package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.apache.commons.lang.StringUtils;

/**
 * Created by IntelliJ IDEA.
 * User: A. Hamid
 * Date: Apr 12, 2010
 * Time: 3:32:44 PM
 */
public class CheckCRFLocked extends SecureController {
    @Override
    protected void processRequest() throws Exception {
        int userId;
        String ecId = request.getParameter("ecId");
        int requestUserId = ub.getId();
        if (StringUtils.isNotEmpty(ecId)) {
            if (getEventCrfLocker().isLocked(ecId, requestUserId)) {
                userId = getEventCrfLocker().getLockOwner(ecId);
                UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
                UserAccountBean ubean = (UserAccountBean)udao.findByPK(userId);
                response.getWriter().print(resword.getString("CRF_unavailable") +
                        "\n"+ubean.getName() + " "+ resword.getString("Currently_entering_data")
                        + "\n"+resword.getString("Leave_the_CRF")
                        + "\n" + resword.getString("Continue_in_read_mode_or_cancel"));
            } else {
                response.getWriter().print("true");
            }
            return;
        }else if(request.getParameter("userId")!=null) {
            getEventCrfLocker().unlockAllForUser(Integer.parseInt(request.getParameter("userId")));
            if(request.getParameter("exitTo")!=null){
                response.sendRedirect(request.getParameter("exitTo"));
            }else{
                response.sendRedirect("ListStudySubjects");
            }

        }
    }
    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        return;
    }
}
