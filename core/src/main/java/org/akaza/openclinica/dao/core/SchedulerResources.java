package org.akaza.openclinica.dao.core;

import org.apache.commons.lang.StringUtils;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;
import java.util.Properties;

import static org.akaza.openclinica.dao.hibernate.multitenant.CurrentTenantIdentifierResolverImpl.CURRENT_TENANT_ID;

public class SchedulerResources {
    Properties properties = new Properties();

    protected final static Logger logger = LoggerFactory.getLogger("org.akaza.openclinica.dao.core.SchedulerResources");

    // default no arg constructor
    public SchedulerResources() {

    }

    public void setSchemaInfo() {
        String schema = null;
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null && requestAttributes.getRequest() != null) {
            HttpSession session = requestAttributes.getRequest().getSession();
            schema = (String) session.getAttribute(CURRENT_TENANT_ID);
        }
        if (StringUtils.isEmpty(schema))
            schema = CoreResources.getField("schema");
        System.out.println("SchedulerResources**********Using schema:" + schema);
        properties.setProperty("schema", schema);
    }
    public Properties getSchemaInfo() {
        setSchemaInfo();
        return properties;
    }

}
