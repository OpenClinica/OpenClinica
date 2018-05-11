/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.core.SecurityManager;
import org.akaza.openclinica.web.InsufficientPermissionException;

/**
 * Checks user's password with the one int the session
 *
 * @author shamim
 *
 */
public class MatchPasswordServlet extends SecureController {

    private static final long serialVersionUID = -358927626509831091L;

    @Override
    protected void processRequest() throws Exception {
        String password = request.getParameter("password");
        logger.info("password [" + password + "]");
        if (password != null && !password.equals("")) {
            SecurityManager securityManager =
                    ((SecurityManager) SpringServletAccess.getApplicationContext(context).getBean("securityManager"));
            response.getWriter().print(Boolean.toString(securityManager.verifyPassword(password, getUserDetails())));
            return;
        }
    }

    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        return;
    }
}
