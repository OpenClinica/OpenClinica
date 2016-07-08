package org.akaza.openclinica.controller;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.idsite.AccountResult;
import com.stormpath.sdk.servlet.account.AccountResolver;
import com.stormpath.spring.config.EnableStormpath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.PropertySource;
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
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SSOAppConfig.class);
        context.registerShutdownHook();

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
        //AccountResult accountResult = app.newIdSiteCallbackHandler(request).getAccountResult();

        //Account account = accountResult.getAccount();

        System.out.println("****in SSO restricted controller:" + app);
        System.out.println("Previous app from the request:" + getApplication(request));
        Account account = AccountResolver.INSTANCE.getAccount(request);
        System.out.println("****Account=" + account);
        /*
        if (account == null) {
            return "redirect:/pages/login";
        }
        */
        logger.info("After in SSO restricted controller");
        return "redirect:/MainMenu";
    }
    protected Application getApplication(HttpServletRequest request) {
        return (Application)request.getAttribute(Application.class.getName());
    }
}
