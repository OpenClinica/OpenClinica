/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2008-2009 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import core.org.akaza.openclinica.bean.submit.CRFVersionBean;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.dao.submit.CRFVersionDAO;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Builds on top of PrintCRFServlet
 * 
 * @author Krikor Krumlian
 */
public class PrintCRFByIdServlet extends PrintCRFServlet {

    private static final long serialVersionUID = 1L;

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.control.managestudy.ViewSectionDataEntryServlet#mayProceed()
     */
    @Override
    public void mayProceed(HttpServletRequest request, HttpServletResponse response) throws InsufficientPermissionException {
        return;
    }

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.control.managestudy.ViewSectionDataEntryServlet#processRequest()
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        Study currentStudy =    (Study) request.getSession().getAttribute("study");
        currentStudy = (Study) getStudyDao().findByPK(1);
        CRFVersionDAO crfVersionDao = new CRFVersionDAO(getDataSource());
        if (request.getParameter("id") == null) {
            forwardPage(Page.LOGIN, request, response);
        }
        CRFVersionBean crfVersion = crfVersionDao.findByOid(request.getParameter("id"));
        request.setAttribute("study", currentStudy);
        if (crfVersion != null) {
            request.setAttribute("id", String.valueOf(crfVersion.getId()));
            super.processRequest(request, response);
        } else {
            forwardPage(Page.LOGIN, request, response);
        }
    }
}
