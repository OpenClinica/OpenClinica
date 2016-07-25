package org.akaza.openclinica.job;

import java.util.Properties;

import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.ThreadPool;

/**
 * Custom {@link SchedulerFactory} adapted to configure a {@link ThreadPool} with zero threads.
 *
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 *
 */
public class OpenClinicaStdSchedulerFactory extends StdSchedulerFactory {

    @Override
    public void initialize(Properties props) throws SchedulerException {
        String threadCount = props.getProperty("org.quartz.threadPool.threadCount");
        if (threadCount.trim().equals("0")) {
            // Replaces the thread pool class used
            props.put("org.quartz.threadPool.class", "org.akaza.openclinica.job.EmptyThreadPool");
            // Removes "org.quartz.threadPool.*" properties not applicable for this class
            props.remove("org.quartz.threadPool.threadCount");
            props.remove("org.quartz.threadPool.threadPriority");
        }
        super.initialize(props);
    }

}
