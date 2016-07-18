package org.akaza.openclinica.controller;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.idsite.AccountResult;
import com.stormpath.spring.config.EnableStormpath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

@Controller
@EnableStormpath
public class RestrictedController {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    @Autowired
    Application app;

    @RequestMapping("/restricted/secret")
    public String secret(HttpServletRequest request, Model model) {

        System.out.println("++++++++++++++++++++++++++++++++++++++++++++Request attr names:");
        Enumeration params = request.getParameterNames();
        while(params.hasMoreElements()){
            String paramName = (String)params.nextElement();
            System.out.println("Parameter Name - "+paramName+", Value - "+request.getParameter(paramName));
        }

        Enumeration attrs = request.getAttributeNames();
        while(attrs.hasMoreElements()){
            String paramName = (String)attrs.nextElement();
            System.out.println("Attribute Name - "+paramName+", Value - "+request.getAttribute(paramName));
        }
        AccountResult accountResult = app.newIdSiteCallbackHandler(request).getAccountResult();

        Account account = accountResult.getAccount();

        if (account == null) {
            return "redirect:/pages/login";
        }


        System.out.println("****in SSO restricted controller:" + app);
        System.out.println("Previous app from the request:" + getApplication(request));
        System.out.println("****Account=" + account);
        Authentication authentication = new UsernamePasswordAuthenticationToken(account.getUsername(), null,
                AuthorityUtils.createAuthorityList("ROLE_USER"));
        System.out.println("**************Auth=" + authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);


        logger.info("After in SSO restricted controller");
        return "redirect:/MainMenu";
    }
    protected Application getApplication(HttpServletRequest request) {
        return (Application)request.getAttribute(Application.class.getName());
    }
}
