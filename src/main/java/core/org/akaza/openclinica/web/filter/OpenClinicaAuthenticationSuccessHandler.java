package core.org.akaza.openclinica.web.filter;

import org.akaza.openclinica.view.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class OpenClinicaAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {


        boolean redirectLoginPage = (boolean) request.getAttribute("redirectLoginWarning");

        if (redirectLoginPage) {
            logger.info("redirecting to page with bookmark link");
            response.sendRedirect(request.getContextPath() + Page.REDIRECT_LOGIN_SERVLET.getFileName());
        } else {
            logger.info("redirecting to main menu");
            response.sendRedirect(request.getContextPath() + Page.MENU_SERVLET.getFileName());
        }
    }
}
