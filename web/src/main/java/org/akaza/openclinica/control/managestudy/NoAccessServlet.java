/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.submit.FormLayoutBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.submit.FormLayoutDAO;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.SQLInitServlet;
import org.akaza.openclinica.web.bean.EntityBeanTable;
import org.akaza.openclinica.web.bean.ListCRFRow;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
