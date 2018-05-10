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
 * @version 1.0 (22/Nov/2010) Logback log filter to get logs for facility MAIL
 * 
 */
public class LogFilterFacilityMAIL extends LogFilterBase {

        @Override
    public FilterReply decide(LoggingEvent event) {
        if ((MDC.get(FACILITY_CODE_KEY) != null) && (Integer.parseInt(MDC.get(FACILITY_CODE_KEY)) == (SYSLOG_FACILITY_MAIL)))
 {
            return FilterReply.ACCEPT;
        } else {
            return FilterReply.DENY;
          }
    }// decide

}// class LogFilterFacilityMAIL


