package org.akaza.openclinica.dao.core;

import javax.servlet.ServletContextEvent;
import org.slf4j.bridge.SLF4JBridgeHandler;
import liquibase.log.LogFactory;

import org.slf4j.MDC;
import org.springframework.web.context.ContextLoaderListener;

public class OCContextLoaderListener extends ContextLoaderListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        String path = event.getServletContext().getRealPath("/");
        String webAppName = getWebAppName(path);
        // Put the web application name into the logging context. This value is
        // used inside the logback.xml
        MDC.put("WEBAPP", webAppName);
        // Get the liquibase logs inside the application log files using
        // SLF4JBridgeHandler
        LogFactory.getLogger().addHandler(new SLF4JBridgeHandler());
        super.contextInitialized(event);
    }

    public String getWebAppName(String servletCtxRealPath) {
        String webAppName = null;
        if (null != servletCtxRealPath) {
            String[] tokens = servletCtxRealPath.split("\\\\");
            webAppName = tokens[(tokens.length - 1)].trim();
        }
        return webAppName;
    }
}
