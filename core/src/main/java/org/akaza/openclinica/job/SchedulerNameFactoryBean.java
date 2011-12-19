package org.akaza.openclinica.job;

import org.akaza.openclinica.dao.core.CoreResources;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.FactoryBean;

/**
 * This factory bean generates the name for the Quartz scheduler to be used by this application. The objective is to
 * have a unique name for each scheduler instance when multiple applications are deployed to the same application
 * server.
 *
 * @author Doug Rodrigues (drodrigues@openclinica.com)
 *
 */
public class SchedulerNameFactoryBean implements FactoryBean<String> {

    private static final String PREFIX = "OpenClinicaJobScheduler_";

    public String getObject() throws Exception {
        String webapp = CoreResources.getWebapp();
        if (webapp != null) {
            return PREFIX + webapp;
        }
        return PREFIX + RandomStringUtils.randomAlphanumeric(8);
    }

    public Class<?> getObjectType() {
        return String.class;
    }

    public boolean isSingleton() {
        return true;
    }

}
