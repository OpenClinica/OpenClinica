package org.akaza.openclinica.bean.rule;

import org.akaza.openclinica.bean.core.AuditableEntityBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunOnScheduleBean extends AuditableEntityBean {

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private String time;

    public RunOnScheduleBean() {
        // TODO Auto-generated constructor stub
    }

    public RunOnScheduleBean(String time) {
        this.time = time;
    }

    public String getTime() {
    	return time;
    }

    public void setTime(String time) {
    	this.time = time;
    }
}