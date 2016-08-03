/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.job;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.listeners.TriggerListenerSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 *
 */
public class JobTriggerListener extends TriggerListenerSupport {

    private static final Logger LOG = LoggerFactory.getLogger(JobTriggerListener.class);

    @Override
    public String getName() {
        return "JobTriggerListener";
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        super.triggerFired(trigger, context);
        logTriggerInfo(trigger, "Trigger {} fired");
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        boolean result =  super.vetoJobExecution(trigger, context);
        logTriggerInfo(trigger, "Trigger {} vetoed");
        return result;
    }

    @Override
    public void triggerMisfired(Trigger trigger) {
        super.triggerMisfired(trigger);
        logTriggerInfo(trigger, "Trigger {} misfired");
    }

    public void triggerComplete(Trigger trigger, JobExecutionContext context, Trigger.CompletedExecutionInstruction triggerInstructionCode) {
        super.triggerComplete(trigger, context, triggerInstructionCode);
        logTriggerInfo(trigger, "Trigger {} complete");
    }

    private void logTriggerInfo(Trigger trigger, String message) {
        LOG.debug(message, trigger.getDescription());
    }



}
