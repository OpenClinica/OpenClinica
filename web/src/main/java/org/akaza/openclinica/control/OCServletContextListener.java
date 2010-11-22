package org.akaza.openclinica.control;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.akaza.openclinica.dao.core.CoreResources;
/**
 * ServletContextListener used as a controller for throwing an error when reading up the properties
 * @author jnyayapathi
 *
 */
public class OCServletContextListener implements ServletContextListener {

	public void contextDestroyed(ServletContextEvent event) {
		 

	}

	public void contextInitialized(ServletContextEvent event) {
		// TODO JN:UNCOMMENT ME AFTER FIGURING OUT WHAT TO DO WITH XSLT FILES 
		// CoreResources cr = (CoreResources) SpringServletAccess.getApplicationContext(event.getServletContext()).getBean("coreResources");
	}

}
