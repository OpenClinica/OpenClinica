/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;

import java.util.Locale;

/**
 * Lists all the CRF and their CRF versions
 *
 * @author jxu
 */
public class NoAccessServlet extends SecureController {
    Locale locale;

    // < ResourceBundle resexception,respage,resword,restext,resworkflow;
    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {

    }

    /**
     * Finds all the crfs
     *
     */
    @Override
    public void processRequest() throws Exception {
        request.setAttribute(ORIGINATING_PAGE, request.getParameter(ORIGINATING_PAGE));
        forwardPage(Page.NO_ACCESS);
    }

}
