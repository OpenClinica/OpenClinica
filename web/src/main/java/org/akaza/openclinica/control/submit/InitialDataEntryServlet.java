/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.submit.DisplayItemBean;
import org.akaza.openclinica.bean.submit.DisplayItemGroupBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.control.form.DiscrepancyValidator;
import org.akaza.openclinica.control.form.RuleValidator;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.*;

import org.springframework.stereotype.Controller;

import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author ssachs
 */
@Controller
@RequestMapping(value="/InitialDataEntry")
public class InitialDataEntryServlet extends DataEntryServlet {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    Locale locale;

    // < ResourceBundleresexception,respage;

    /*
     * (non-Javadoc)
     * 
     * @see org.akaza.openclinica.control.core.SecureController#mayProceed()
     */

    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        checkStudyLocked(Page.LIST_STUDY_SUBJECTS, respage.getString("current_study_locked"));
        checkStudyFrozen(Page.LIST_STUDY_SUBJECTS, respage.getString("current_study_frozen"));

        locale = request.getLocale();
        this.session.setAttribute("mayProcessUploading", "true");
        // <
        // resexception=ResourceBundle.getBundle(
        // "org.akaza.openclinica.i18n.exceptions",locale);
        // < respage =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.page_messages",
        // locale);

        getInputBeans();

//        DataEntryStage stage = ecb.getStage();
//        Role r = currentRole.getRole();
//
//        if (stage.equals(DataEntryStage.UNCOMPLETED)) {
//            if (!SubmitDataServlet.maySubmitData(ub, currentRole)) {
//                this.session.setAttribute("mayProcessUploading", "false");
//                String exceptionName = resexception.getString("no_permission_to_perform_data_entry");
//                String noAccessMessage =
//                    respage.getString("you_may_not_perform_data_entry_on_a_CRF") + respage.getString("change_study_contact_study_coordinator");
//
//                addPageMessage(noAccessMessage);
//                throw new InsufficientPermissionException(Page.MENU, exceptionName, "1");
//            }
//        } else if (stage.equals(DataEntryStage.INITIAL_DATA_ENTRY)) {
//            if (ub.getId() != ecb.getOwnerId() && !r.equals(Role.STUDYDIRECTOR) && !r.equals(Role.COORDINATOR)) {
//                UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
//                String ownerName = ((UserAccountBean) udao.findByPK(ecb.getOwnerId())).getName();
//                this.session.setAttribute("mayProcessUploading", "false");
//                MessageFormat mf = new MessageFormat("");
//                mf.applyPattern(respage.getString("you_may_not_perform_data_entry_on_event_CRF_because_not_owner"));
//                Object[] arguments = { ownerName };
//                addPageMessage(mf.format(arguments));
//
//                throw new InsufficientPermissionException(Page.LIST_STUDY_SUBJECTS_SERVLET, resexception.getString("non_owner_attempting_DE_on_event"), "1");
//            }
//        } else {
//            this.session.setAttribute("mayProcessUploading", "false");
//            addPageMessage(respage.getString("you_not_enter_data_initial_DE_completed"));
//            throw new InsufficientPermissionException(Page.LIST_STUDY_SUBJECTS_SERVLET, resexception.getString("using_IDE_event_CRF_completed"), "1");
//        }

        return;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.akaza.openclinica.control.submit.DataEntryServlet#
     * validateInputOnFirstRound()
     */
    @Override
    protected boolean validateInputOnFirstRound() {
        return true;
    }

    @Override
    protected DisplayItemBean validateDisplayItemBean(DiscrepancyValidator v, DisplayItemBean dib, String inputName, RuleValidator rv,
            HashMap<String, ArrayList<String>> groupOrdinalPLusItemOid, Boolean fireRuleValidation, ArrayList<String> messages) {

        ItemBean ib = dib.getItem();
        org.akaza.openclinica.bean.core.ResponseType rt = dib.getMetadata().getResponseSet().getResponseType();

        // note that this step sets us up both for
        // displaying the data on the form again, in the event of an error
        // and sending the data to the database, in the event of no error
        if (StringUtil.isBlank(inputName)) {// not an item from group, doesn't
            // need to get data from form again
            dib = loadFormValue(dib);
        }

        // types TEL and ED are not supported yet
        if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.TEXT) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.TEXTAREA)) {
            // dib = validateDisplayItemBeanText(v, dib, inputName);
        } else if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.RADIO) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.SELECT)) {
            // dib = validateDisplayItemBeanSingleCV(v, dib, inputName);
        }
        if (groupOrdinalPLusItemOid.containsKey(dib.getItem().getOid()) || fireRuleValidation) {
            messages = messages == null ? groupOrdinalPLusItemOid.get(dib.getItem().getOid()) : messages;
            dib = validateDisplayItemBeanSingleCV(rv, dib, inputName, messages);
        }
        // I_AGEN_DOSEDATE64
        return dib;
    }

    @Override
    protected List<DisplayItemGroupBean> validateDisplayItemGroupBean(DiscrepancyValidator v, DisplayItemGroupBean digb, List<DisplayItemGroupBean> digbs,
            List<DisplayItemGroupBean> formGroups, RuleValidator rv, HashMap<String, ArrayList<String>> groupOrdinalPLusItemOid) {

        formGroups = loadFormValueForItemGroup(digb, digbs, formGroups, edcb.getId());
        String inputName = "";
        for (int i = 0; i < formGroups.size(); i++) {
            DisplayItemGroupBean displayGroup = formGroups.get(i);

            List<DisplayItemBean> items = displayGroup.getItems();
            int order = displayGroup.getOrdinal();
            if (displayGroup.isAuto() && displayGroup.getFormInputOrdinal() > 0) {
                order = displayGroup.getFormInputOrdinal();
            }
            for (DisplayItemBean displayItem : items) {
                // int manualcount = 0;
                // tbh trying to set this correctly 01/2010
                if (displayGroup.isAuto()) {
                    inputName = getGroupItemInputName(displayGroup, order, displayItem);
                } else {
                    inputName = getGroupItemManualInputName(displayGroup, order, displayItem);
                    // manualcount++;
                }
                logger.debug("THe oid is " + displayItem.getItem().getOid() + " order : " + order + " inputName : " + inputName);

                if (groupOrdinalPLusItemOid.containsKey(displayItem.getItem().getOid())
                    || groupOrdinalPLusItemOid.containsKey(String.valueOf(displayGroup.getIndex() + 1) + displayItem.getItem().getOid())) {
                    logger.debug("IN : " + String.valueOf(displayGroup.getIndex() + 1) + displayItem.getItem().getOid());
                    validateDisplayItemBean(v, displayItem, inputName, rv, groupOrdinalPLusItemOid, true, groupOrdinalPLusItemOid.get(String
                            .valueOf(displayGroup.getIndex() + 1)
                        + displayItem.getItem().getOid()));
                } else {
                    validateDisplayItemBean(v, displayItem, inputName, rv, groupOrdinalPLusItemOid, false, null);
                }
            }
        }
        return formGroups;

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.akaza.openclinica.control.submit.DataEntryServlet#validateDisplayItemBean
     * (org.akaza.openclinica.core.form.Validator,
     * org.akaza.openclinica.bean.submit.DisplayItemBean)
     */
    @Override
    protected DisplayItemBean validateDisplayItemBean(DiscrepancyValidator v, DisplayItemBean dib, String inputName) {

        ItemBean ib = dib.getItem();
        org.akaza.openclinica.bean.core.ResponseType rt = dib.getMetadata().getResponseSet().getResponseType();

        // note that this step sets us up both for
        // displaying the data on the form again, in the event of an error
        // and sending the data to the database, in the event of no error
        if (StringUtil.isBlank(inputName)) {// not an item from group, doesn't
            // need to get data from form again
            dib = loadFormValue(dib);
        }

        // types TEL and ED are not supported yet
        if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.TEXT) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.TEXTAREA)) {
            dib = validateDisplayItemBeanText(v, dib, inputName);
        } else if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.RADIO) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.SELECT)) {
            dib = validateDisplayItemBeanSingleCV(v, dib, inputName);
        } else if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.CHECKBOX) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.SELECTMULTI)) {
            dib = validateDisplayItemBeanMultipleCV(v, dib, inputName);
        }

        logger.debug("just ran validate display item bean on " + inputName);
        return dib;
    }

    @Override
    protected List<DisplayItemGroupBean> validateDisplayItemGroupBean(DiscrepancyValidator v, DisplayItemGroupBean digb, List<DisplayItemGroupBean> digbs,
            List<DisplayItemGroupBean> formGroups) {

        formGroups = loadFormValueForItemGroup(digb, digbs, formGroups, edcb.getId());
        String inputName = "";
        for (int i = 0; i < formGroups.size(); i++) {
            DisplayItemGroupBean displayGroup = formGroups.get(i);

            List<DisplayItemBean> items = displayGroup.getItems();
            int order = displayGroup.getOrdinal();
            if (displayGroup.isAuto() && displayGroup.getFormInputOrdinal() > 0) {
                order = displayGroup.getFormInputOrdinal();
            }
            for (DisplayItemBean displayItem : items) {
                if (displayGroup.isAuto()) {
                    inputName = getGroupItemInputName(displayGroup, order, displayItem);
                } else {
                    inputName = getGroupItemManualInputName(displayGroup, order, displayItem);
                }
                validateDisplayItemBean(v, displayItem, inputName);
            }
        }
        return formGroups;

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.akaza.openclinica.control.submit.DataEntryServlet#getBlankItemStatus
     * ()
     */
    @Override
    protected Status getBlankItemStatus() {
        return Status.AVAILABLE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.akaza.openclinica.control.submit.DataEntryServlet#getNonBlankItemStatus
     * ()
     */
    @Override
    protected Status getNonBlankItemStatus() {
        return edcb.isDoubleEntry() ? Status.PENDING : Status.UNAVAILABLE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.akaza.openclinica.control.submit.DataEntryServlet#getEventCRFAnnotations
     * ()
     */
    @Override
    protected String getEventCRFAnnotations() {
        return ecb.getAnnotations();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.akaza.openclinica.control.submit.DataEntryServlet#setEventCRFAnnotations
     * (java.lang.String)
     */
    @Override
    protected void setEventCRFAnnotations(String annotations) {
        ecb.setAnnotations(annotations);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.akaza.openclinica.control.submit.DataEntryServlet#getJSPPage()
     */
    @Override
    protected Page getJSPPage() {
        // request.setAttribute("newtable","y");
        return Page.INITIAL_DATA_ENTRY_NW;

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.akaza.openclinica.control.submit.DataEntryServlet#getServletPage()
     */
    @Override
    protected Page getServletPage() {
        String tabId = fp.getString("tab", true);
        String sectionId = fp.getString(DataEntryServlet.INPUT_SECTION_ID, true);
        String eventCRFId = fp.getString(INPUT_EVENT_CRF_ID, true);
        if (StringUtil.isBlank(sectionId) || StringUtil.isBlank(tabId)) {
            return Page.INITIAL_DATA_ENTRY_SERVLET;
        } else {
            Page target = Page.INITIAL_DATA_ENTRY_SERVLET;
            target.setFileName(target.getFileName() + "?eventCRFId=" + eventCRFId + "&sectionId=" + sectionId + "&tab=" + tabId);
            return target;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.akaza.openclinica.control.submit.DataEntryServlet#loadDBValues()
     */
    @Override
    protected boolean shouldLoadDBValues(DisplayItemBean dib) {
        return true;
    }

    @Override
    protected boolean shouldRunRules() {
        return true;
    }

    @Override
    protected boolean isAdministrativeEditing() {
        return false;
    }

    @Override
    protected boolean isAdminForcedReasonForChange() {
        return false;
    }

}
