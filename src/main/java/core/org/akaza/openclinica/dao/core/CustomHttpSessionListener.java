package core.org.akaza.openclinica.dao.core;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.core.EventCRFLocker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

@WebListener
public class CustomHttpSessionListener implements HttpSessionListener {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void sessionCreated(HttpSessionEvent httpSessionEvent) {

    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        logger.debug("Destroying the session");
        HttpSession session = event.getSession();
        ApplicationContext ctx =
                WebApplicationContextUtils.
                        getWebApplicationContext(session.getServletContext());

        UserAccountBean ub = (UserAccountBean) session.getAttribute("userBean");
        if (ub == null) {
            logger.debug("UserAccountBean is null");
            return;
        }
        EventCRFLocker eventCRFLocker =
                (EventCRFLocker) ctx.getBean("eventCrfLocker");
        eventCRFLocker.unlockAllForUserPerSession(ub.getId(), session.getId());
    }

    // ...
}
