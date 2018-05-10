package org.akaza.openclinica.job;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.io.IOException;
import java.util.Properties;

/**
 * Customized {@link SchedulerFactoryBean} which replaces the default scheduler implementation by
 * {@link OpenClinicaStdSchedulerFactory}.
 *
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 * @see OpenClinicaStdSchedulerFactory
 */
public class OpenClinicaSchedulerFactoryBean extends SchedulerFactoryBean {

    @Override public void afterPropertiesSet() throws Exception {
        this.setSchedulerFactoryClass(OpenClinicaStdSchedulerFactory.class);
        super.afterPropertiesSet();
    }

}
