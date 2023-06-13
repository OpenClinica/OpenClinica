package org.akaza.openclinica.control.form;

import java.io.IOException;

import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;

public class SpringServletAccess {

    public static ApplicationContext getApplicationContext(ServletContext servletContext) {
        return WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
    }

}
