package org.akaza.openclinica.control;

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
    
    public static String getPropertiesDir(ServletContext servletContext) {
    	String resource = "properties/placeholder.properties";
    	ServletContextResource scr = (ServletContextResource)getApplicationContext(servletContext).getResource(resource);
    	String absolutePath = null;
		try {
			absolutePath = scr.getFile().getAbsolutePath();
		} catch (IOException e) {
			throw new OpenClinicaSystemException(e.getMessage(),e.fillInStackTrace());
		}
    	absolutePath = absolutePath.replaceAll("placeholder.properties", "");
    	return absolutePath;
    }

}
