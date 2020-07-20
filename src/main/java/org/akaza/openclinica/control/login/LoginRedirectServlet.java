package org.akaza.openclinica.control.login;

import core.org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.view.Page;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class LoginRedirectServlet extends SecureController {

    @Override
    public void mayProceed() throws InsufficientPermissionException {

    }

    @Override
    public void processRequest() throws Exception {
        forwardPage(Page.REDIRECT_LOGIN);
    }

//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, java.io.IOException {
//        try {
//            logger.debug("Request");
//
//            context.getRequestDispatcher(Page.REDIRECT_LOGIN.getFileName()).forward(request, response);
//        } catch (Exception e) {
//            logger.error("Error while calling the process method: ", e);
//        }
//    }

}
