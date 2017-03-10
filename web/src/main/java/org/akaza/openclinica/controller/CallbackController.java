package org.akaza.openclinica.controller;

/**
 * Created by krikorkrumlian on 3/9/17.
 */
import com.auth0.NonceUtils;
import com.auth0.spring.security.mvc.Auth0CallbackHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class CallbackController extends Auth0CallbackHandler {

    @RequestMapping(value = "/callback", method = RequestMethod.GET)
    protected void callback(final HttpServletRequest req, final HttpServletResponse res)
            throws ServletException, IOException {

        NonceUtils.addNonceToStorage(req);
        super.handle(req, res);
    }

}
