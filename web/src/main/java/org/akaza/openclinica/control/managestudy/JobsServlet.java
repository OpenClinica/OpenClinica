package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

/**
 * @author ronpanduwana
 *
 *         Processes 'jobs (bulk logfile)' request
 */
public class JobsServlet extends SecureController {

    /**
     * Checks whether the user has the right permission to proceed function
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
    }


    @Override
    public void processRequest() throws Exception {
        int parentStudyId = currentStudy.getParentStudyId();
        if (parentStudyId > 0) {
            request.setAttribute("atSiteLevel", true);
            StudyDAO stdao = new StudyDAO(sm.getDataSource());
            StudyBean parentStudy = (StudyBean) stdao.findByPK(parentStudyId);
            request.setAttribute("theStudy", parentStudy);
            request.setAttribute("theSite", currentStudy);
        } else {
            request.setAttribute("atSiteLevel", false);
            request.setAttribute("theStudy", currentStudy);
        }
        forwardPage(Page.JOBS);
    }
}
