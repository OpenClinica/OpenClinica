package org.akaza.openclinica.config;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;
import org.springframework.util.StringUtils;

public class DistributedSessionFilterInitializer extends AbstractHttpSessionApplicationInitializer{

    public static final String SPRING_PROFILES_PROPERTY_NAME = "spring.profiles.active";

    public void onStartup(ServletContext servletContext) throws ServletException {
        String profiles = System.getProperty(SPRING_PROFILES_PROPERTY_NAME);
        if (!StringUtils.isEmpty(profiles)) {
            if (ArrayUtils.contains(profiles.split(","), DistributedSessionConfig.DISTRIBUTED_SESSION_SPRING_PROFILE))
                super.onStartup(servletContext);
        }
    }
}


