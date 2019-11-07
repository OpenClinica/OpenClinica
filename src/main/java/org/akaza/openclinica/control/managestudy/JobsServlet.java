package org.akaza.openclinica.control.managestudy;

import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author ronpanduwana
 *
 *         Processes 'jobs (bulk logfile)' request
 */
public class JobsServlet extends SecureController {

    @Autowired
    private StudyDao studyDao;
    /**
     * Checks whether the user has the right permission to proceed function
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
    }


    @Override
    public void processRequest() throws Exception {
        int parentStudyId = currentStudy.checkAndGetParentStudyId();
        if (parentStudyId > 0) {
            request.setAttribute("atSiteLevel", true);
            Study parentStudy = (Study) studyDao.findByPK(parentStudyId);
            request.setAttribute("theStudy", parentStudy);
            request.setAttribute("theSite", currentStudy);
        } else {
            request.setAttribute("atSiteLevel", false);
            request.setAttribute("theStudy", currentStudy);
        }
        forwardPage(Page.JOBS);
    }
}
