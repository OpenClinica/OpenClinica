/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2008-2009 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

/**
 * Builds on top of ViewSectionDataEntryServlet, Doesn't add much other than using OIDs to get to the View Screen.
 * 
 * @author Krikor Krumlian
 */
public class ViewSectionDataEntryByIdServlet extends ViewSectionDataEntryServlet {

    private static final long serialVersionUID = 1L;

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.control.managestudy.ViewSectionDataEntryServlet#mayProceed()
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        return;
    }

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.control.managestudy.ViewSectionDataEntryServlet#processRequest()
     */
    @Override
    public void processRequest() throws Exception {
        StudyDAO studyDao = new StudyDAO(sm.getDataSource());
        currentStudy = (StudyBean) studyDao.findByPK(1);
        CRFVersionDAO crfVersionDao = new CRFVersionDAO(sm.getDataSource());
        if (request.getParameter("id") == null) {
            forwardPage(Page.LOGIN);
        }
        CRFVersionBean crfVersion = crfVersionDao.findByOid(request.getParameter("id"));
        if (crfVersion != null) {
            request.setAttribute("crfVersionId", String.valueOf(crfVersion.getId()));
            request.setAttribute("crfId", String.valueOf(crfVersion.getCrfId()));
            super.processRequest();
        } else {
            forwardPage(Page.LOGIN);
        }
    }
}
