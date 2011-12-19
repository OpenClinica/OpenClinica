package org.akaza.openclinica.job;

import org.quartz.impl.jdbcjobstore.DriverDelegate;
import org.quartz.impl.jdbcjobstore.NoSuchDelegateException;
import org.springframework.scheduling.quartz.LocalDataSourceJobStore;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * Wraps the {@link DriverDelegate} used by {@link SchedulerFactoryBean} to ignore Quartz jobs that were not scheduled
 * by the running instance.
 *
 * @author Doug Rodrigues (drodrigues@openclinica.com)
 *
 */
public class OpenClinicaDataSourceStore extends LocalDataSourceJobStore {

    private DriverDelegate driverDelegate;

    @Override
    protected DriverDelegate getDelegate() throws NoSuchDelegateException {
        if (driverDelegate == null) {
            driverDelegate = new DriverDelegateWrapper(super.getDelegate(), instanceName);
        }
        return driverDelegate;
    }

}
