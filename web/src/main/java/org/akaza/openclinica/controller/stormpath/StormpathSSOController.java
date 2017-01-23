package org.akaza.openclinica.controller.stormpath;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.idsite.AccountResult;
import com.stormpath.spring.config.EnableStormpath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
@EnableStormpath
@ConditionalOnProperty(name = "stormpath.application.href", matchIfMissing = false)
@Controller
/*
To includde this controller, put the following in the Tomcat setenv.sh
JAVA_OPTS="$JAVA_OPTS -Dstormpath.application.href=<stormpath application href> -DSTORMPATH_API_KEY_FILE=<path to stormpath apiKey.properties>"
 */
public class StormpathSSOController {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    DataSource dataSource;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired(required = false)
    protected Application app;

    @RequestMapping("/restricted/secret")
    public String secret(HttpServletRequest request, Model model) {

        String stormpathApp = applicationContext.getEnvironment().getProperty("stormpath.application.href");
        if (StringUtils.isEmpty(stormpathApp)) {
            logger.error("Environment variable STORMPATH_APPLICATION_HREF is not set");
            return "redirect:/pages/login/login";
        }
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
