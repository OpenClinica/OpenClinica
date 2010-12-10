/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2010 Akaza Research
 */
package org.akaza.openclinica.log;

import org.slf4j.MDC;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.spi.FilterReply;

/**
 * @author pgawade
 * @version 1.0 (22/Nov/2010) Logback log filter to get logs for facility AUTH
 */
public class LogFilterOther extends LogFilterBase {


    @Override
    public FilterReply decide(LoggingEvent event) {
        MDC.remove(FACILITY_CODE_KEY);
        int logFacilityCode = getLogFacilityCode(event.getLoggerName());
        MDC.put(FACILITY_CODE_KEY, new Integer(logFacilityCode).toString());// This
                                                                            // is
                                                                            // done
                                                                // only once
                                                    // for each log request

        if (logFacilityCode == SYSLOG_FACILITY_DEFAULT) {
            return FilterReply.ACCEPT;
        } else {
            return FilterReply.DENY;
        }
    }// decide

    public int getLogFacilityCode(String loggerName) {
        int logFacilityCode = -1;

        if ((loggerName.contains(".login")) || (loggerName.contains("core.OpenClinicaPasswordEncoder"))
 || (loggerName.contains("domain.user")))
            {
            logFacilityCode = SYSLOG_FACILITY_AUTH;
            }
 else if ((loggerName.contains("service.crfdata")) || (loggerName.contains("control.admin"))) {
            logFacilityCode = SYSLOG_FACILITY_UUCP;
        }
 else if ((loggerName.contains(".submit"))
            || (loggerName.contains("domain.crfdata"))
            // commented out following packages as it is
            // overlapping with the LPR facility
            // || (loggerName.contains("dao.rule"))
            // || (loggerName.contains("domain.rule"))
            // || (loggerName.contains("logic.rulerunner")) // ||
            // (loggerName.contains("dao.rule"))
            // || (loggerName.contains("service.rule"))
            || (loggerName.contains("logic.score")) || (loggerName.contains("service.crfdata")) || (loggerName.contains("validator.rule.action"))
            || (loggerName.contains("control.form")) || (loggerName.contains("web.crfdata"))) // from
                                                                                              // ws
        {
            logFacilityCode = SYSLOG_FACILITY_USER;
        } else if ((loggerName.contains("dao.admin")) || (loggerName.contains("dao.core")) || (loggerName.contains("dao.extract"))
            || (loggerName.contains("dao.hibernate")) || (loggerName.contains("dao.logic")) || (loggerName.contains("dao.login"))
            || (loggerName.contains("dao.managestudy")) || (loggerName.contains("dao.rule")) || (loggerName.contains("dao.rule.action"))
            || (loggerName.contains("dao.service")) || (loggerName.contains("dao.submit")) || (loggerName.contains("dao.ws"))) {
            logFacilityCode = SYSLOG_FACILITY_AUTHPRIV;
        }
 else if ((loggerName.contains("bean.rule")) || (loggerName.contains("dao.rule")) || (loggerName.contains("domain.rule"))
            || (loggerName.contains("logic.rulerunner")) || (loggerName.contains("service.rule")) || (loggerName.contains("control.submit"))) {
            logFacilityCode = SYSLOG_FACILITY_LPR;
        }
 else if ((loggerName.contains("bean.extract"))
            || (loggerName.contains("dao.extract"))
            || (loggerName.contains("logic.odmExport")) || (loggerName.contains("service.extract")))
            {
            logFacilityCode = SYSLOG_FACILITY_CRON;
            }
 else if ((loggerName.contains("core.EmailEngine")) || (loggerName.contains("core.OpenClinicaMailSender"))) // ws
        {
            logFacilityCode = SYSLOG_FACILITY_MAIL;
        }

 else if ((loggerName.contains(".exception"))) {
            logFacilityCode = SYSLOG_FACILITY_DAEMON;
        }
        return logFacilityCode;
    }

}// class LogFilterFacilityAUTH

