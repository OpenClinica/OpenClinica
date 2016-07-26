package org.akaza.openclinica.config;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;
import org.springframework.util.StringUtils;

public class DistributedSessionFilterInitializer extends AbstractHttpSessionApplicationInitializer{

    public static final String SPRING_PROFILES_PROPERTY_NAME = "spring.profiles.active";

    public void onStartup(ServletContext servletContext) throws ServletException {
        System.out.println("Executing DistributedSessionFilterInitializer");
        String profiles = System.getProperty(SPRING_PROFILES_PROPERTY_NAME);
        if (!StringUtils.isEmpty(profiles)) {
            String[] profileArray = profiles.split(",");
            if (ArrayUtils.contains(profileArray, DistributedSessionConfig.DISTRIBUTED_SESSION_SPRING_PROFILE)) {
                System.out.println("Found heroku profile.  Creating Filter.");
                super.onStartup(servletContext);
            } else System.out.println("Did not find heroku profile.  Skipping filter.");
        } else System.out.println("No spring profiles specified.  Skipping filter.");
    }
}


