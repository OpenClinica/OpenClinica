package core.org.akaza.openclinica.service;

import org.quartz.Scheduler;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;

public interface SchedulerUtilService {
    Scheduler getSchemaScheduler(ApplicationContext applicationContext, HttpServletRequest request);
}
