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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.util.Enumeration;

@Controller
@EnableStormpath
public class RestrictedController {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    @Autowired
    Application app;
    @Autowired
    DataSource dataSource;

    @RequestMapping("/restricted/secret")
    public String secret(HttpServletRequest request, Model model) {
        AccountResult accountResult = app.newIdSiteCallbackHandler(request).getAccountResult();

        Account account = accountResult.getAccount();

        if (account == null) {
            return "redirect:/pages/login";
        }


        Authentication authentication = new UsernamePasswordAuthenticationToken(account.getUsername(), null,
                AuthorityUtils.createAuthorityList("ROLE_USER"));
        SecurityContextHolder.getContext().setAuthentication(authentication);


        logger.info("After in SSO restricted controller");
        return "redirect:/MainMenu";
    }
    protected Application getApplication(HttpServletRequest request) {
        return (Application)request.getAttribute(Application.class.getName());
    }

    //TODO
    /*
    - Get the dataSource as done above
    - Create new UserAccountDAO(dataSource)
    -
     */
}
