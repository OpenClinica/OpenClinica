package org.akaza.openclinica.job;

import java.util.Properties;

import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.JobStore;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * Custom {@link SchedulerFactory} used to replace the default {@link JobStore} used by the
 * {@link SchedulerFactoryBean}. There is no configuration value available to replace it using properties.
 *
 * @author Doug Rodrigues (drodrigues@openclinica.com)
 *
 */
public class OpenClinicaSchedulerFactory extends StdSchedulerFactory {

    @Override
    public void initialize(Properties props) throws SchedulerException {
        props.put("org.quartz.jobStore.class", "org.akaza.openclinica.job.OpenClinicaDataSourceStore");
        super.initialize(props);
    }

}
