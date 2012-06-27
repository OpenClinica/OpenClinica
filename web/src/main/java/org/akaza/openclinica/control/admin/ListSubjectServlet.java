/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.domain.rule.RuleBean;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.domain.rule.action.DiscrepancyNoteActionBean;
import org.akaza.openclinica.domain.rule.action.InsertPropertyActionBean;
import org.akaza.openclinica.domain.rule.action.PropertyBean;
import org.akaza.openclinica.domain.rule.expression.Context;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.service.rule.RuleSetService;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.mvel2.MVEL;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Processes user request and generate subject list
 *
 * @author jxu
 */
public class ListSubjectServlet extends SecureController {
    Locale locale;

    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);

        if (ub.isSysAdmin()) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.ADMIN_SYSTEM_SERVLET, resexception.getString("not_admin"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        SubjectDAO sdao = new SubjectDAO(sm.getDataSource());

        StudySubjectDAO subdao = new StudySubjectDAO(sm.getDataSource());
        StudyDAO studyDao = new StudyDAO(sm.getDataSource());
        UserAccountDAO uadao = new UserAccountDAO(sm.getDataSource());

        ListSubjectTableFactory factory = new ListSubjectTableFactory();
        factory.setSubjectDao(sdao);
        factory.setStudySubjectDao(subdao);
        factory.setUserAccountDao(uadao);
        factory.setStudyDao(studyDao);
        factory.setCurrentStudy(currentStudy);


        String auditLogsHtml = factory.createTable(request, response).render();
        request.setAttribute("listSubjectsHtml", auditLogsHtml);

        // Get Study Subject by its OID
        StudySubjectBean ss = subdao.findByOidAndStudy("SS_SSID3",currentStudy.getId());
        // Allows me to call operations on the object itself
        System.out.println(MVEL.eval("label == 'SSID1'", ss));
        System.out.println(MVEL.eval("enrollmentDate", ss));
        //System.out.println(MVEL.eval("doSomething", ss));
        //System.out.println(MVEL.eval("doSomething(10)", ss));

        //StudyBean currentStudy, UserAccountBean ub, HttpServletRequest,request,StudySubjectBean studySubjectBean

    //    getRuleSetService().runRulesInBeanProperty(createRuleSet(),currentStudy,ub,request,ss);


        forwardPage(Page.SUBJECT_LIST);
    }

    @Override
    protected String getAdminServlet() {
        if (ub.isSysAdmin()) {
            return SecureController.ADMIN_SERVLET_CODE;
        } else {
            return "";
        }
    }

    private ArrayList<RuleSetBean> createRuleSet(){
        RuleBean rule = createRuleBean();
        RuleSetBean ruleSet = getRuleSet(rule.getOid(),rule);

        ArrayList<RuleSetBean> list = new ArrayList<RuleSetBean>();
        list.add(ruleSet);
        return list;
    }

//Calendar configuration or set up should take parameters from something like this....
    private RuleSetBean getRuleSet(String ruleOid, RuleBean ruleBean) {
        RuleSetBean ruleSet = new RuleSetBean();
        ruleSet.setTarget(createExpression(Context.OC_RULES_V1, "SS.ENROLLMENT_DATE"));
        RuleSetRuleBean ruleSetRule = createRuleSetRule(ruleSet, ruleOid,ruleBean);
        ruleSet.addRuleSetRule(ruleSetRule);
        return ruleSet;

    }

    private RuleSetRuleBean createRuleSetRule(RuleSetBean ruleSet, String ruleOid,RuleBean ruleBean) {
        RuleSetRuleBean ruleSetRule = new RuleSetRuleBean();
        ruleSetRule.setRuleBean(ruleBean);
        InsertPropertyActionBean ruleAction = new InsertPropertyActionBean();
        ruleAction.addProperty(createPropertyBean("SE_RANDOMIZATION.dateStarted","SS.enrollmentDate + 0"));
        ruleAction.addProperty(createPropertyBean("SE_RANDOMIZATION.subjectEventStatus","Scheduled"));
        ruleAction.addProperty(createPropertyBean("SE_TREATMENTVISIT1.dateStarted","SS.enrollmentDate + 2"));
        ruleAction.addProperty(createPropertyBean("SE_TREATMENTVISIT1.subjectEventStatus","Scheduled"));
        ruleAction.addProperty(createPropertyBean("SE_TREATMENTVISIT2.dateStarted","SS.enrollmentDate + 6"));
        ruleAction.addProperty(createPropertyBean("SE_TREATMENTVISIT2.subjectEventStatus","Scheduled"));
        ruleAction.addProperty(createPropertyBean("SE_FOLLOWUPVISIT1.dateStarted","SS.enrollmentDate + 13"));
        ruleAction.addProperty(createPropertyBean("SE_FOLLOWUPVISIT1.subjectEventStatus","Scheduled"));
        ruleAction.addProperty(createPropertyBean("SE_FOLLOWUPVISIT2.dateStarted","SE_RANDOMIZATION.dateStarted + 30"));
        ruleAction.addProperty(createPropertyBean("SE_FOLLOWUPVISIT2.subjectEventStatus","Scheduled"));
        ruleAction.setExpressionEvaluatesTo(true);// this would evaluate to false in case the subject is rejected for example at screening stage.
        ruleSetRule.addAction(ruleAction);
        ruleSetRule.setRuleSetBean(ruleSet);
        ruleSetRule.setOid(ruleOid);

        return ruleSetRule;
    }

    
    private PropertyBean createPropertyBean(String Oid,String expression)
    {
        PropertyBean propertyBean = new PropertyBean();
        propertyBean.setOid(Oid);
        ExpressionBean expressionBean = createExpression(Context.OC_RULES_V1,expression);
        propertyBean.setValueExpression(expressionBean);
        return propertyBean;
    }

    private RuleBean createRuleBean() {
        RuleBean ruleBean = new RuleBean();
        ruleBean.setName("TEST");
        ruleBean.setOid("BOY");
        ruleBean.setDescription("Yellow");
        ruleBean.setExpression(createExpression(Context.OC_RULES_V1,
                "SS.label eq \"SSID3\""));
        return ruleBean;
    }

    private ExpressionBean createExpression(Context context, String value) {
        ExpressionBean expression = new ExpressionBean();
        expression.setContext(context);
        expression.setValue(value);
        return expression;
    }

    private RuleSetService getRuleSetService() {
        return (RuleSetService) SpringServletAccess.getApplicationContext(context).getBean("ruleSetService");
    }

}
