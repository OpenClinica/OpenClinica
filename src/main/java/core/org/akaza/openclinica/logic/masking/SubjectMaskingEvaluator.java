/*
 * Created on Sep 1, 2005
 *
 *
 */
package core.org.akaza.openclinica.logic.masking;

import core.org.akaza.openclinica.bean.masking.MaskingBean;
import core.org.akaza.openclinica.bean.submit.SubjectBean;
import core.org.akaza.openclinica.logic.core.BusinessEvaluator;
import core.org.akaza.openclinica.logic.core.BusinessRule;
import core.org.akaza.openclinica.logic.masking.rules.MaskSubjectDOBRule;

import java.util.Iterator;

/**
 * @author thickerson
 *
 *
 */
public class SubjectMaskingEvaluator extends BusinessEvaluator {
    protected MaskingBean mBean;

    public SubjectMaskingEvaluator(SubjectBean sb, MaskingBean mBean) {
        super(sb);
        assertRuleSet();
        this.mBean = mBean;
    }

    @Override
    public void assertRuleSet() {
        // TODO accept a MaskingObject from a DAO and have a big if-then
        // chain????
        // ruleSet.add(new maskDOBRule());
        if (mBean.getRuleMap().containsKey("core.org.akaza.openclinica.logic.masking.rule.MaskSubjectDOBRule"))
            ruleSet.add(new MaskSubjectDOBRule());
    }

    @Override
    protected void evaluateRuleSet() {
        // can modify this as necessary? tbh
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
}
