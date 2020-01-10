/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.job;

import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * Customized {@link SchedulerFactoryBean} which replaces the default scheduler implementation by
 * {@link OpenClinicaStdSchedulerFactory}.
 *
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 * @see OpenClinicaStdSchedulerFactory
 *
 */
public class OpenClinicaSchedulerFactoryBean extends SchedulerFactoryBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        this.setSchedulerFactoryClass(OpenClinicaStdSchedulerFactory.class);
        super.afterPropertiesSet();
    }

}
