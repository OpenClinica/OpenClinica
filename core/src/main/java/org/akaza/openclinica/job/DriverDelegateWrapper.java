package org.akaza.openclinica.job;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.quartz.Calendar;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.jdbcjobstore.DriverDelegate;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.utils.Key;
import org.quartz.utils.TriggerStatus;

/**
 * Wraps a {@link DiverDelegate}, modifying the {@link #selectTriggerToAcquire(Connection, long, long)} method to return
 * only jobs that were sheduled by the executing application instance.
 *
 * @author Doug Rodrigues (drodrigues@openclinica.com)
 *
 */
public class DriverDelegateWrapper implements DriverDelegate {

    private final DriverDelegate wrapped;

    private final String instanceName;

    public DriverDelegateWrapper(DriverDelegate wrapped, String instanceName) {
        this.wrapped = wrapped;
        this.instanceName = instanceName;
    }

    @SuppressWarnings("rawtypes")
    public List selectTriggerToAcquire(Connection conn, long noLaterThan, long noEarlierThan) throws SQLException {
        List triggers = wrapped.selectTriggerToAcquire(conn, noLaterThan, noEarlierThan);
        Iterator it = triggers.iterator();
        while (it.hasNext()) {
            Key key = (Key) it.next();
            if (!key.getGroup().equals(instanceName)) {
                it.remove();
            }
        }
        return triggers;
    }

    /* ===================== UNMODIFIED DELEGATE METHODS ===================== */

    public int updateTriggerStatesFromOtherStates(Connection conn, String newState, String oldState1, String oldState2)
            throws SQLException {
        return wrapped.updateTriggerStatesFromOtherStates(conn, newState, oldState1, oldState2);
    }

    public Key[] selectMisfiredTriggers(Connection conn, long ts) throws SQLException {
        return wrapped.selectMisfiredTriggers(conn, ts);
    }

    public Key[] selectMisfiredTriggersInState(Connection conn, String state, long ts) throws SQLException {
        return wrapped.selectMisfiredTriggersInState(conn, state, ts);
    }

    public boolean selectMisfiredTriggersInStates(Connection conn, String state1, String state2, long ts, int count,
            @SuppressWarnings("rawtypes") List resultList) throws SQLException {
        return wrapped.selectMisfiredTriggersInStates(conn, state1, state2, ts, count, resultList);
    }

    public int countMisfiredTriggersInStates(Connection conn, String state1, String state2, long ts)
            throws SQLException {
        return wrapped.countMisfiredTriggersInStates(conn, state1, state2, ts);
    }

    public Key[] selectMisfiredTriggersInGroupInState(Connection conn, String groupName, String state, long ts)
            throws SQLException {
        return wrapped.selectMisfiredTriggersInGroupInState(conn, groupName, state, ts);
    }

    public Trigger[] selectTriggersForRecoveringJobs(Connection conn) throws SQLException, IOException,
            ClassNotFoundException {
        return wrapped.selectTriggersForRecoveringJobs(conn);
    }

    public int deleteFiredTriggers(Connection conn) throws SQLException {
        return wrapped.deleteFiredTriggers(conn);
    }

    public int deleteFiredTriggers(Connection conn, String instanceId) throws SQLException {
        return wrapped.deleteFiredTriggers(conn, instanceId);
    }

    public int deleteVolatileFiredTriggers(Connection conn) throws SQLException {
        return wrapped.deleteVolatileFiredTriggers(conn);
    }

    public Key[] selectVolatileTriggers(Connection conn) throws SQLException {
        return wrapped.selectVolatileTriggers(conn);
    }

    public Key[] selectVolatileJobs(Connection conn) throws SQLException {
        return wrapped.selectVolatileJobs(conn);
    }

    public int insertJobDetail(Connection conn, JobDetail job) throws IOException, SQLException {
        return wrapped.insertJobDetail(conn, job);
    }

    public int updateJobDetail(Connection conn, JobDetail job) throws IOException, SQLException {
        return wrapped.updateJobDetail(conn, job);
    }

    public Key[] selectTriggerNamesForJob(Connection conn, String jobName, String groupName) throws SQLException {
        return wrapped.selectTriggerNamesForJob(conn, jobName, groupName);
    }

    public int deleteJobListeners(Connection conn, String jobName, String groupName) throws SQLException {
        return wrapped.deleteJobListeners(conn, jobName, groupName);
    }

    public int deleteJobDetail(Connection conn, String jobName, String groupName) throws SQLException {
        return wrapped.deleteJobDetail(conn, jobName, groupName);
    }

    public boolean isJobStateful(Connection conn, String jobName, String groupName) throws SQLException {
        return wrapped.isJobStateful(conn, jobName, groupName);
    }

    public boolean jobExists(Connection conn, String jobName, String groupName) throws SQLException {
        return wrapped.jobExists(conn, jobName, groupName);
    }

    public int updateJobData(Connection conn, JobDetail job) throws IOException, SQLException {
        return wrapped.updateJobData(conn, job);
    }

    public int insertJobListener(Connection conn, JobDetail job, String listener) throws SQLException {
        return wrapped.insertJobListener(conn, job, listener);
    }

    public String[] selectJobListeners(Connection conn, String jobName, String groupName) throws SQLException {
        return wrapped.selectJobListeners(conn, jobName, groupName);
    }

    public JobDetail selectJobDetail(Connection conn, String jobName, String groupName, ClassLoadHelper loadHelper)
            throws ClassNotFoundException, IOException, SQLException {
        return wrapped.selectJobDetail(conn, jobName, groupName, loadHelper);
    }

    public int selectNumJobs(Connection conn) throws SQLException {
        return wrapped.selectNumJobs(conn);
    }

    public String[] selectJobGroups(Connection conn) throws SQLException {
        return wrapped.selectJobGroups(conn);
    }

    public String[] selectJobsInGroup(Connection conn, String groupName) throws SQLException {
        return wrapped.selectJobsInGroup(conn, groupName);
    }

    public int insertTrigger(Connection conn, Trigger trigger, String state, JobDetail jobDetail) throws SQLException,
            IOException {
        return wrapped.insertTrigger(conn, trigger, state, jobDetail);
    }

    public int insertSimpleTrigger(Connection conn, SimpleTrigger trigger) throws SQLException {
        return wrapped.insertSimpleTrigger(conn, trigger);
    }

    public int insertBlobTrigger(Connection conn, Trigger trigger) throws SQLException, IOException {
        return wrapped.insertBlobTrigger(conn, trigger);
    }

    public int insertCronTrigger(Connection conn, CronTrigger trigger) throws SQLException {
        return wrapped.insertCronTrigger(conn, trigger);
    }

    public int updateTrigger(Connection conn, Trigger trigger, String state, JobDetail jobDetail) throws SQLException,
            IOException {
        return wrapped.updateTrigger(conn, trigger, state, jobDetail);
    }

    public int updateSimpleTrigger(Connection conn, SimpleTrigger trigger) throws SQLException {
        return wrapped.updateSimpleTrigger(conn, trigger);
    }

    public int updateCronTrigger(Connection conn, CronTrigger trigger) throws SQLException {
        return wrapped.updateCronTrigger(conn, trigger);
    }

    public int updateBlobTrigger(Connection conn, Trigger trigger) throws SQLException, IOException {
        return wrapped.updateBlobTrigger(conn, trigger);
    }

    public boolean triggerExists(Connection conn, String triggerName, String groupName) throws SQLException {
        return wrapped.triggerExists(conn, triggerName, groupName);
    }

    public int updateTriggerState(Connection conn, String triggerName, String groupName, String state)
            throws SQLException {
        return wrapped.updateTriggerState(conn, triggerName, groupName, state);
    }

    public int updateTriggerStateFromOtherState(Connection conn, String triggerName, String groupName, String newState,
            String oldState) throws SQLException {
        return wrapped.updateTriggerStateFromOtherState(conn, triggerName, groupName, newState, oldState);
    }

    public int updateTriggerStateFromOtherStates(Connection conn, String triggerName, String groupName,
            String newState, String oldState1, String oldState2, String oldState3) throws SQLException {
        return wrapped.updateTriggerStateFromOtherStates(conn, triggerName, groupName, newState, oldState1, oldState2,
                oldState3);
    }

    public int updateTriggerStateFromOtherStatesBeforeTime(Connection conn, String newState, String oldState1,
            String oldState2, long time) throws SQLException {
        return wrapped.updateTriggerStateFromOtherStatesBeforeTime(conn, newState, oldState1, oldState2, time);
    }

    public int updateTriggerGroupStateFromOtherStates(Connection conn, String groupName, String newState,
            String oldState1, String oldState2, String oldState3) throws SQLException {
        return wrapped.updateTriggerGroupStateFromOtherStates(conn, groupName, newState, oldState1, oldState2,
                oldState3);
    }

    public int updateTriggerGroupStateFromOtherState(Connection conn, String groupName, String newState, String oldState)
            throws SQLException {
        return wrapped.updateTriggerGroupStateFromOtherState(conn, groupName, newState, oldState);
    }

    public int updateTriggerStatesForJob(Connection conn, String jobName, String groupName, String state)
            throws SQLException {
        return wrapped.updateTriggerStatesForJob(conn, jobName, groupName, state);
    }

    public int updateTriggerStatesForJobFromOtherState(Connection conn, String jobName, String groupName, String state,
            String oldState) throws SQLException {
        return wrapped.updateTriggerStatesForJobFromOtherState(conn, jobName, groupName, state, oldState);
    }

    public int deleteTriggerListeners(Connection conn, String triggerName, String groupName) throws SQLException {
        return wrapped.deleteTriggerListeners(conn, triggerName, groupName);
    }

    public int insertTriggerListener(Connection conn, Trigger trigger, String listener) throws SQLException {
        return wrapped.insertTriggerListener(conn, trigger, listener);
    }

    public String[] selectTriggerListeners(Connection conn, String triggerName, String groupName) throws SQLException {
        return wrapped.selectTriggerListeners(conn, triggerName, groupName);
    }

    public int deleteSimpleTrigger(Connection conn, String triggerName, String groupName) throws SQLException {
        return wrapped.deleteSimpleTrigger(conn, triggerName, groupName);
    }

    public int deleteBlobTrigger(Connection conn, String triggerName, String groupName) throws SQLException {
        return wrapped.deleteBlobTrigger(conn, triggerName, groupName);
    }

    public int deleteCronTrigger(Connection conn, String triggerName, String groupName) throws SQLException {
        return wrapped.deleteCronTrigger(conn, triggerName, groupName);
    }

    public int deleteTrigger(Connection conn, String triggerName, String groupName) throws SQLException {
        return wrapped.deleteTrigger(conn, triggerName, groupName);
    }

    public int selectNumTriggersForJob(Connection conn, String jobName, String groupName) throws SQLException {
        return wrapped.selectNumTriggersForJob(conn, jobName, groupName);
    }

    public JobDetail selectJobForTrigger(Connection conn, String triggerName, String groupName,
            ClassLoadHelper loadHelper) throws ClassNotFoundException, SQLException {
        return wrapped.selectJobForTrigger(conn, triggerName, groupName, loadHelper);
    }

    @SuppressWarnings("rawtypes")
    public List selectStatefulJobsOfTriggerGroup(Connection conn, String groupName) throws SQLException {
        return wrapped.selectStatefulJobsOfTriggerGroup(conn, groupName);
    }

    public Trigger[] selectTriggersForJob(Connection conn, String jobName, String groupName) throws SQLException,
            ClassNotFoundException, IOException {
        return wrapped.selectTriggersForJob(conn, jobName, groupName);
    }

    public Trigger[] selectTriggersForCalendar(Connection conn, String calName) throws SQLException,
            ClassNotFoundException, IOException {
        return wrapped.selectTriggersForCalendar(conn, calName);
    }

    public Trigger selectTrigger(Connection conn, String triggerName, String groupName) throws SQLException,
            ClassNotFoundException, IOException {
        return wrapped.selectTrigger(conn, triggerName, groupName);
    }

    public JobDataMap selectTriggerJobDataMap(Connection conn, String triggerName, String groupName)
            throws SQLException, ClassNotFoundException, IOException {
        return wrapped.selectTriggerJobDataMap(conn, triggerName, groupName);
    }

    public String selectTriggerState(Connection conn, String triggerName, String groupName) throws SQLException {
        return wrapped.selectTriggerState(conn, triggerName, groupName);
    }

    public TriggerStatus selectTriggerStatus(Connection conn, String triggerName, String groupName) throws SQLException {
        return wrapped.selectTriggerStatus(conn, triggerName, groupName);
    }

    public int selectNumTriggers(Connection conn) throws SQLException {
        return wrapped.selectNumTriggers(conn);
    }

    public String[] selectTriggerGroups(Connection conn) throws SQLException {
        return wrapped.selectTriggerGroups(conn);
    }

    public String[] selectTriggersInGroup(Connection conn, String groupName) throws SQLException {
        return wrapped.selectTriggersInGroup(conn, groupName);
    }

    public Key[] selectTriggersInState(Connection conn, String state) throws SQLException {
        return wrapped.selectTriggersInState(conn, state);
    }

    public int insertPausedTriggerGroup(Connection conn, String groupName) throws SQLException {
        return wrapped.insertPausedTriggerGroup(conn, groupName);
    }

    public int deletePausedTriggerGroup(Connection conn, String groupName) throws SQLException {
        return wrapped.deletePausedTriggerGroup(conn, groupName);
    }

    public int deleteAllPausedTriggerGroups(Connection conn) throws SQLException {
        return wrapped.deleteAllPausedTriggerGroups(conn);
    }

    public boolean isTriggerGroupPaused(Connection conn, String groupName) throws SQLException {
        return wrapped.isTriggerGroupPaused(conn, groupName);
    }

    @SuppressWarnings("rawtypes")
    public Set selectPausedTriggerGroups(Connection conn) throws SQLException {
        return wrapped.selectPausedTriggerGroups(conn);
    }

    public boolean isExistingTriggerGroup(Connection conn, String groupName) throws SQLException {
        return wrapped.isExistingTriggerGroup(conn, groupName);
    }

    public int insertCalendar(Connection conn, String calendarName, Calendar calendar) throws IOException, SQLException {
        return wrapped.insertCalendar(conn, calendarName, calendar);
    }

    public int updateCalendar(Connection conn, String calendarName, Calendar calendar) throws IOException, SQLException {
        return wrapped.updateCalendar(conn, calendarName, calendar);
    }

    public boolean calendarExists(Connection conn, String calendarName) throws SQLException {
        return wrapped.calendarExists(conn, calendarName);
    }

    public Calendar selectCalendar(Connection conn, String calendarName) throws ClassNotFoundException, IOException,
            SQLException {
        return wrapped.selectCalendar(conn, calendarName);
    }

    public boolean calendarIsReferenced(Connection conn, String calendarName) throws SQLException {
        return wrapped.calendarIsReferenced(conn, calendarName);
    }

    public int deleteCalendar(Connection conn, String calendarName) throws SQLException {
        return wrapped.deleteCalendar(conn, calendarName);
    }

    public int selectNumCalendars(Connection conn) throws SQLException {
        return wrapped.selectNumCalendars(conn);
    }

    public String[] selectCalendars(Connection conn) throws SQLException {
        return wrapped.selectCalendars(conn);
    }

    @SuppressWarnings("deprecation")
    public long selectNextFireTime(Connection conn) throws SQLException {
        return wrapped.selectNextFireTime(conn);
    }

    public Key selectTriggerForFireTime(Connection conn, long fireTime) throws SQLException {
        return wrapped.selectTriggerForFireTime(conn, fireTime);
    }

    public int insertFiredTrigger(Connection conn, Trigger trigger, String state, JobDetail jobDetail)
            throws SQLException {
        return wrapped.insertFiredTrigger(conn, trigger, state, jobDetail);
    }

    @SuppressWarnings("rawtypes")
    public List selectFiredTriggerRecords(Connection conn, String triggerName, String groupName) throws SQLException {
        return wrapped.selectFiredTriggerRecords(conn, triggerName, groupName);
    }

    @SuppressWarnings("rawtypes")
    public List selectFiredTriggerRecordsByJob(Connection conn, String jobName, String groupName) throws SQLException {
        return wrapped.selectFiredTriggerRecordsByJob(conn, jobName, groupName);
    }

    @SuppressWarnings("rawtypes")
    public List selectInstancesFiredTriggerRecords(Connection conn, String instanceName) throws SQLException {
        return wrapped.selectInstancesFiredTriggerRecords(conn, instanceName);
    }

    @SuppressWarnings("rawtypes")
    public Set selectFiredTriggerInstanceNames(Connection conn) throws SQLException {
        return wrapped.selectFiredTriggerInstanceNames(conn);
    }

    public int deleteFiredTrigger(Connection conn, String entryId) throws SQLException {
        return wrapped.deleteFiredTrigger(conn, entryId);
    }

    public int selectJobExecutionCount(Connection conn, String jobName, String jobGroup) throws SQLException {
        return wrapped.selectJobExecutionCount(conn, jobName, jobGroup);
    }

    public int insertSchedulerState(Connection conn, String instanceId, long checkInTime, long interval)
            throws SQLException {
        return wrapped.insertSchedulerState(conn, instanceId, checkInTime, interval);
    }

    public int deleteSchedulerState(Connection conn, String instanceId) throws SQLException {
        return wrapped.deleteSchedulerState(conn, instanceId);
    }

    public int updateSchedulerState(Connection conn, String instanceId, long checkInTime) throws SQLException {
        return wrapped.updateSchedulerState(conn, instanceId, checkInTime);
    }

    @SuppressWarnings("rawtypes")
    public List selectSchedulerStateRecords(Connection conn, String instanceId) throws SQLException {
        return wrapped.selectSchedulerStateRecords(conn, instanceId);
    }

}
