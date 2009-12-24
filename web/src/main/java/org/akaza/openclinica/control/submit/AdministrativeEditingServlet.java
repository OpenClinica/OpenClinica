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
import org.akaza.openclinica.bean.submit.DisplayItemBean;
import org.akaza.openclinica.bean.submit.DisplayItemGroupBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.control.form.DiscrepancyValidator;
import org.akaza.openclinica.control.form.RuleValidator;
import org.akaza.openclinica.control.managestudy.ViewNotesServlet;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * @author jxu
 * 
 *         Performs 'administrative editing' action for study director/study
 *         coordinator
 */
public class AdministrativeEditingServlet extends DataEntryServlet {

    Locale locale;

    // < ResourceBundleresexception,respage;

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
            return Page.ADMIN_EDIT_SERVLET;
        } else {
            Page target = Page.ADMIN_EDIT_SERVLET;
            target.setFileName(target.getFileName() + "?eventCRFId=" + eventCRFId + "&sectionId=" + sectionId + "&tab=" + tabId);
            return target;
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.akaza.openclinica.control.submit.DataEntryServlet#getJSPPage()
     */
    @Override
    protected Page getJSPPage() {
        return Page.ADMIN_EDIT;
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

    /*
     * (non-Javadoc)
     * 
     * @see org.akaza.openclinica.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {

        locale = request.getLocale();

        // <
        // resexception=ResourceBundle.getBundle(
        // "org.akaza.openclinica.i18n.exceptions",locale);
        // < respage =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.page_messages",
        // locale);

        getInputBeans();

        String fromResolvingNotes = fp.getString("fromResolvingNotes", true);

        if (StringUtil.isBlank(fromResolvingNotes)) {
            session.removeAttribute(ViewNotesServlet.WIN_LOCATION);
            session.removeAttribute(ViewNotesServlet.NOTES_TABLE);
            checkStudyLocked(Page.LIST_STUDY_SUBJECTS, respage.getString("current_study_locked"));
            checkStudyFrozen(Page.LIST_STUDY_SUBJECTS, respage.getString("current_study_frozen"));
        }
        request.setAttribute("fromResolvingNotes", fromResolvingNotes);
        System.out.println(" +++++++++++++++++++ " + ecb.getStudyEventId());
        // StudyEventDAO sedao = new StudyEventDAO(sm.getDataSource());
        // StudyEventBean sEvent = (StudyEventBean)
        // sedao.findByPK(ecb.getStudyEventId());
        // if (sEvent.getSubjectEventStatus().equals(SubjectEventStatus.SIGNED))
        // {
        // sEvent.setSubjectEventStatus(SubjectEventStatus.COMPLETED);
        // sEvent.setUpdater(ub);
        // sEvent.setUpdatedDate(new Date());
        // sedao.update(sEvent);
        // }
        DataEntryStage stage = ecb.getStage();
        Role r = currentRole.getRole();
        this.session.setAttribute("mayProcessUploading", "true");

        if (!SubmitDataServlet.maySubmitData(ub, currentRole)) {
            this.session.setAttribute("mayProcessUploading", "false");
            String exceptionName = resexception.getString("no_permission_validation");
            String noAccessMessage = respage.getString("not_perform_administrative_editing_CRF");

            addPageMessage(noAccessMessage);
            throw new InsufficientPermissionException(Page.MENU, exceptionName, "1");
        }
        logger.info("stage name:" + stage.getName());
        if (stage.equals(DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE)) {
            // if (!r.equals(Role.STUDYDIRECTOR) && !r.equals(Role.COORDINATOR))
            // {
            if (r.equals(Role.MONITOR)) {
                this.session.setAttribute("mayProcessUploading", "false");
                addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
                throw new InsufficientPermissionException(Page.LIST_STUDY_SUBJECTS_SERVLET, resexception.getString("no_permission_administrative_editing"), "1");
            }
        }

        else {
            this.session.setAttribute("mayProcessUploading", "false");
            addPageMessage(respage.getString("not_perform_administrative_editing_because"));
            throw new InsufficientPermissionException(Page.LIST_STUDY_SUBJECTS_SERVLET, resexception.getString("not_correct_stage"), "1");
        }
        return;
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
     * @see org.akaza.openclinica.control.submit.DataEntryServlet#loadDBValues()
     */
    @Override
    protected boolean shouldLoadDBValues(DisplayItemBean dib) {
        // logger.info("dib" + dib.getData().getValue());
        if (dib.getData().getStatus() == null) {
            return true;
        }
        if (!Status.UNAVAILABLE.equals(dib.getData().getStatus())) {
            return false;
        }

        return true;
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

    @Override
    protected DisplayItemBean validateDisplayItemBean(DiscrepancyValidator v, DisplayItemBean dib, String inputName, RuleValidator rv,
            HashMap<String, ArrayList<String>> groupOrdinalPLusItemOid, Boolean fireRuleValidation, ArrayList<String> messages) {
        if (StringUtil.isBlank(inputName)) {// we pass a blank inputName,which
            // means if not an item from group,
            // doesn't
            // need to get data from form again
            dib = loadFormValue(dib);
        }
        return dib;
    }

    @Override
    protected List<DisplayItemGroupBean> validateDisplayItemGroupBean(DiscrepancyValidator v, DisplayItemGroupBean digb, List<DisplayItemGroupBean> digbs,
            List<DisplayItemGroupBean> formGroups, RuleValidator rv, HashMap<String, ArrayList<String>> groupOrdinalPLusItemOid) {
        formGroups = loadFormValueForItemGroup(digb, digbs, formGroups, edcb.getId());
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
        return Status.UNAVAILABLE;
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
        return Status.UNAVAILABLE;
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

    @Override
    protected boolean shouldRunRules() {
        return false;
    }

    @Override
    protected boolean isAdministrativeEditing() {
        return true;
    }

    @Override
    protected boolean isAdminForcedReasonForChange() {
        // StudyParameterValueDAO spvdao = new
        // StudyParameterValueDAO(sm.getDataSource());
        // ArrayList studyParameters =
        // spvdao.findParamConfigByStudy(currentStudy);

        // currentStudy.setStudyParameters(studyParameters);
        // refresh study params here, tbh 06/2009
        if (currentStudy.getStudyParameterConfig().getAdminForcedReasonForChange().equals("true")) {
            System.out.println("returning true for forced reason for change");
            return true;
        } else {
            return false;
        }
    }

}
