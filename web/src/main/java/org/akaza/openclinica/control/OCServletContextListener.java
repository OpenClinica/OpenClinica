package org.akaza.openclinica.control;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.OpenClinicaVersionDAO;
import org.akaza.openclinica.dao.hibernate.UsageStatsServiceDAO;
import org.akaza.openclinica.domain.OpenClinicaVersionBean;
import org.akaza.openclinica.service.usageStats.LogUsageStatsService;

/**
 * ServletContextListener used as a controller for throwing an error when
 * reading up the properties
 *
 * @author jnyayapathi, pgawade
 *
 */
public class OCServletContextListener implements ServletContextListener {
    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass().getName());

    UsageStatsServiceDAO usageStatsServiceDAO;
    OpenClinicaVersionDAO openClinicaVersionDAO;
    public static String OpenClinicaVersion = "OpenClinica.version";

	@Override
    public void contextDestroyed(ServletContextEvent event) {
        logger.debug("OCServletContextListener -> contextDestroyed");

        // Save the OpenClinica stop time into database
        ServletContext context = event.getServletContext();
        getUsageStatsServiceDAO(context).saveOCStopTimeToDB();
	}

	@Override
    public void contextInitialized(ServletContextEvent event) {
        logger.debug("OCServletContextListener -> contextInitialized");
		 CoreResources cr = (CoreResources) SpringServletAccess.getApplicationContext(event.getServletContext()).getBean("coreResources");

        // @pgawade 25-March-2011 changes for sending usage statistics from
        // OpenClinica instance
        ServletContext context = event.getServletContext();
        // Save OpenClinica version to database
        getOpenClinicaVersionDAO(context).saveOCVersionToDB(CoreResources.getField(OpenClinicaVersion));

        // Fetch the OpenClinica started event details
        Map<String, String> OCStartEventDetails = getEventDetailsOCStart(context);

        // JsonLog usage statistics event OpenClinca started
        LogUsageStatsService.logEventOCStart(OCStartEventDetails);

        // Save the OpenClinica start time into database
        getUsageStatsServiceDAO(context).saveOCStartTimeToDB();
    }

    private Map<String, String> getEventDetailsOCStart(ServletContext context) {
        Map<String, String> OCStartEventDetails = getUsageStatsServiceDAO(context).getEventDetailsOCStart();
        // add OpenClinica version into OC start event details

        OpenClinicaVersionBean openClinicaVersionBean = getOpenClinicaVersionDAO(context).findDefault();
        if (null != openClinicaVersionBean) {
            OCStartEventDetails.put(LogUsageStatsService.OC_version, openClinicaVersionBean.getName());
        }
        return OCStartEventDetails;
    }

    private UsageStatsServiceDAO getUsageStatsServiceDAO(ServletContext context) {
        usageStatsServiceDAO =
            this.usageStatsServiceDAO != null ? usageStatsServiceDAO : (UsageStatsServiceDAO) SpringServletAccess.getApplicationContext(context).getBean(
                    "usageStatsServiceDAO");
        return usageStatsServiceDAO;
    }

    private OpenClinicaVersionDAO getOpenClinicaVersionDAO(ServletContext context) {
        openClinicaVersionDAO =
            this.openClinicaVersionDAO != null ? openClinicaVersionDAO : (OpenClinicaVersionDAO) SpringServletAccess.getApplicationContext(context).getBean(
                    "openClinicaVersionDAO");
        return openClinicaVersionDAO;
    }


}
