/*
 * Created on Sep 1, 2005
 *
 *
 */
package org.akaza.openclinica.logic.core;

import org.akaza.openclinica.bean.core.EntityBean;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author thickerson
 *
 *
 */
public abstract class BusinessEvaluator implements Runnable {
    protected ArrayList ruleSet;
    protected boolean hasBeenUpdated;
    protected EntityBean businessObject;

    // the 'subject' that shall be affected:
    // could be subject, crf, etc.

    public BusinessEvaluator(EntityBean o) {
        ruleSet = new ArrayList();
        hasBeenUpdated = true;
        businessObject = o;
    }

    public void run() {
        if (hasBeenUpdated) {
            evaluateRuleSet();
        }
    }

    protected void evaluateRuleSet() {
        synchronized (this) {
            for (Iterator it = ruleSet.iterator(); it.hasNext();) {
                BusinessRule bRule = (BusinessRule) it.next();
                if (bRule.isPropertyTrue(bRule.getClass().getName())) {
                    bRule.doAction(businessObject);
                }
            }
            hasBeenUpdated = false;
        }
    }

    protected abstract void assertRuleSet();
}
