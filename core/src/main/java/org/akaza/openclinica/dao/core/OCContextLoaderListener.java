package org.akaza.openclinica.dao.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.context.ContextLoaderListener;

import javax.servlet.ServletContextEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class OCContextLoaderListener extends ContextLoaderListener {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());


    @Override
    public void contextInitialized(ServletContextEvent event) {
        String path = event.getServletContext().getRealPath("/");
        String webAppName = getWebAppName(path);

        // Put the web application name into the logging context. This value is
        // used inside the logback.xml
        MDC.put("WEBAPP", webAppName);
        // @pgawade 18-July-2011: Get hostname to send it through usage
        // statistics information
        String hostName = "";
        try {
            hostName = getHostName();
        } catch (UnknownHostException uhe) {
            logger.error("UnknownHostException when fetching the hostname");
        }
        MDC.put("HOSTNAME", hostName);
        // MDC.put("WEBAPP", webAppName + " FROM " + hostName);
        // Get the liquibase logs inside the application log files using
        // SLF4JBridgeHandler
        //       LogFactory.getLogger().addHandler(new SLF4JBridgeHandler());
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

    // @pgawade 18-July-2011
    public String getHostName() throws UnknownHostException {
        InetAddress addr = InetAddress.getLocalHost();
        String cHostName = addr.getCanonicalHostName();
        return cHostName;
    }
}
