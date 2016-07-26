package org.akaza.openclinica.config;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;

public class DistributedSessionFilterInitializer extends AbstractHttpSessionApplicationInitializer{
    public void onStartup(ServletContext servletContext) throws ServletException {
        System.out.println("Executing DistributedSessionFilterInitializer");
        String profiles = System.getProperty("spring.profiles.active");
        if (profiles != null && !profiles.equals("")) {
            String[] profileArray = profiles.split(",");
            if (ArrayUtils.contains(profileArray, "heroku")) {
                System.out.println("Found heroku profile.  Creating Filter.");
                super.onStartup(servletContext);
            } else System.out.println("Did not find heroku profile.  Skipping filter.");
        } else System.out.println("No spring profiles specified.  Skipping filter.");
    }
}


