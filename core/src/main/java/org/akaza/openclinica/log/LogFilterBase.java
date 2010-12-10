/**
 * 
 */
package org.akaza.openclinica.log;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.filter.Filter;

/**
 * @author pgawade
 *
 */
public abstract class LogFilterBase extends Filter<LoggingEvent> {

    public final static int SYSLOG_FACILITY_DEFAULT = -1;
    public final static int SYSLOG_FACILITY_KERN = 0;
    public final static int SYSLOG_FACILITY_USER = 1;
    public final static int SYSLOG_FACILITY_MAIL = 2;
    public final static int SYSLOG_FACILITY_DAEMON = 3;
    public final static int SYSLOG_FACILITY_AUTH = 4;
    public final static int SYSLOG_FACILITY_LPR = 6;// 5 is skipped just to
                                                    // match with the standard
                                                    // Syslog facilty codes. 5
                                                    // facilty code is used for
                                                    // internal Syslog messages.
    public final static int SYSLOG_FACILITY_NEWS = 7;
    public final static int SYSLOG_FACILITY_UUCP = 8;
    public final static int SYSLOG_FACILITY_CRON = 9;
    public final static int SYSLOG_FACILITY_AUTHPRIV = 10;
    public final static int SYSLOG_FACILITY_FTP = 11;
    public final static int SYSLOG_FACILITY_AUDIT = 12;

    public final static String FACILITY_CODE_KEY = "FACILITY_CODE";

}
