/**
 * 
 */
package org.akaza.openclinica.dao.hibernate;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.akaza.openclinica.domain.usageStats.LogUsageStatsBean;
import org.akaza.openclinica.service.usageStats.LogUsageStatsService;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author pgawade
 *
 */
public class UsageStatsServiceDAO extends AbstractDomainDao<LogUsageStatsBean> {
    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass().getName());

    @Override
    public Class<LogUsageStatsBean> domainClass() {
        return LogUsageStatsBean.class;
    }

    public LogUsageStatsBean findLatestUsageStatParamValue(String param_key) {
        // logger.debug("UsageStatsServiceDAO -> findLatestUsageStatParamValue");

        String query =
 "from " + getDomainClassName() + " usageStatParams where param_key = :param_key order by update_timestamp desc";

        List<LogUsageStatsBean> logUsageStatsBeanLst = new ArrayList<LogUsageStatsBean>();
        LogUsageStatsBean logUsageStatsBeanRet = new LogUsageStatsBean();
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setString("param_key", param_key);
        q.setMaxResults(1);
        logUsageStatsBeanLst = q.list();
        if ((null != logUsageStatsBeanLst) && (logUsageStatsBeanLst.size() != 0)) {
            logUsageStatsBeanRet = logUsageStatsBeanLst.get(0);
        }

        return logUsageStatsBeanRet;

    }

    @Transactional
    public void saveOCStartTimeToDB() {
        Timestamp ocStartTime = new Timestamp(new java.util.Date().getTime());
        LogUsageStatsBean logUsageStatsBean = new LogUsageStatsBean();
        logUsageStatsBean.setParam_key(LogUsageStatsService.OC_start_time);
        logUsageStatsBean.setParam_value(ocStartTime.toString());
        logUsageStatsBean.setUpdate_timestamp(ocStartTime);

        saveOrUpdate(logUsageStatsBean);
    }

    @Transactional
    public void saveOCStopTimeToDB() {
        Timestamp ocStopTime = new Timestamp(new java.util.Date().getTime());
        LogUsageStatsBean logUsageStatsBean = new LogUsageStatsBean();
        logUsageStatsBean.setParam_key(LogUsageStatsService.OC_stop_time);
        logUsageStatsBean.setParam_value(ocStopTime.toString());
        logUsageStatsBean.setUpdate_timestamp(ocStopTime);

        saveOrUpdate(logUsageStatsBean);
    }

    @Transactional
    public Map<String, String> getEventDetailsOCStart() {
        Map<String, String> OCStartedEventDetails = new HashMap<String, String>();

        // Get Last system start from table "usage_statistics_data"
        LogUsageStatsBean lastOCStartBean = new LogUsageStatsBean();
        lastOCStartBean = findLatestUsageStatParamValue(LogUsageStatsService.OC_start_time);
        if (null != lastOCStartBean) {
            OCStartedEventDetails.put(LogUsageStatsService.OC_last_system_start, lastOCStartBean.getParam_value());
            logger.debug(LogUsageStatsService.OC_last_system_start + ": " + lastOCStartBean.getParam_value());
        }

        // Get Last system stop time from table "usage_statistics_data"
        LogUsageStatsBean lastOCStopBean = new LogUsageStatsBean();
        lastOCStopBean = findLatestUsageStatParamValue(LogUsageStatsService.OC_stop_time);

        // get last system Uptime
        if ((null != lastOCStopBean) && (null != lastOCStartBean)) {
            String stopTime = lastOCStopBean.getParam_value();
            String startTime = lastOCStartBean.getParam_value();

            Date parsedStopDate = null;
            Date parsedStartDate = null;
            try {
                if (null != stopTime) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
                    parsedStopDate = dateFormat.parse(stopTime);
                }
            } catch (ParseException pe) {
                logger.error("Last OpenClinica stop time from database cannot be parsed");
            }
            try {
                if (null != startTime) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
                    parsedStartDate = dateFormat.parse(startTime);
                }
            } catch (ParseException pe) {
                logger.error("Last OpenClinica start time from database cannot be parsed");
            }
            if ((null != parsedStopDate) && (null != parsedStartDate)) {
                long diff = (parsedStopDate.getTime()) - (parsedStartDate.getTime());
                long hrs = 0;
                long min = 0;
                long sec = 0;

                long days = diff / (1000 * 60 * 60 * 24);
                if (days > 0) {
                    diff -= days * (1000 * 60 * 60 * 24);
                }
                hrs = (((diff / (1000 * 60 * 60)) > 0) ? (diff / (1000 * 60 * 60)) : 0);
                if (hrs > 0) {
                    diff -= hrs * (1000 * 60 * 60);
                }
                min = (((diff / (1000 * 60)) > 0) ? (diff / (1000 * 60)) : 0);
                if (min > 0) {
                    diff -= min * (1000 * 60);
                }
                sec = (((diff / (1000)) > 0) ? (diff / (1000)) : 0);

                StringBuffer finalDiff = new StringBuffer();
                if (days > 0) {
                    finalDiff.append(days + " days ");
                }
                if (hrs > 0) {
                    finalDiff.append(hrs + " hrs ");
                }
                if (min > 0) {
                    finalDiff.append(min + " min ");
                }
                if (sec > 0) {
                    finalDiff.append(sec + " sec ");
                }
                logger.debug("Last System Uptime: " + finalDiff.toString());
                OCStartedEventDetails.put(LogUsageStatsService.OC_last_up_time, finalDiff.toString());
            }
        }

        return OCStartedEventDetails;
    }

}
