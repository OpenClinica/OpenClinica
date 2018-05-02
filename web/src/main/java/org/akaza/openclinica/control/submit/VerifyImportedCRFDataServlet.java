/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.submit;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.DiscrepancyNoteType;
import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.DisplayItemBean;
import org.akaza.openclinica.bean.submit.DisplayItemBeanWrapper;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ODMContainer;
import org.akaza.openclinica.bean.submit.crfdata.SubjectDataBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.logic.rulerunner.ExecutionMode;
import org.akaza.openclinica.logic.rulerunner.ImportDataRuleRunnerContainer;
import org.akaza.openclinica.service.rule.RuleSetServiceInterface;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.job.CrfBusinessLogicHelper;
import org.akaza.openclinica.web.job.ImportSpringJob;
import org.apache.commons.collections.CollectionUtils;

/**
 * View the uploaded data and verify what is going to be saved into the system and what is not.
 * 
 * @author Krikor Krumlian
 */
public class VerifyImportedCRFDataServlet extends SecureController {

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

        Role r = currentRole.getRole();
        if (r.equals(Role.STUDYDIRECTOR) || r.equals(Role.COORDINATOR) || r.equals(Role.INVESTIGATOR) || r.equals(Role.RESEARCHASSISTANT)
                || r.equals(Role.RESEARCHASSISTANT2)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("may_not_submit_data"), "1");
    }

    public static DiscrepancyNoteBean createDiscrepancyNote(ItemBean itemBean, String message, EventCRFBean eventCrfBean, DisplayItemBean displayItemBean,
            Integer parentId, UserAccountBean uab, DataSource ds, StudyBean study) {
        // DisplayItemBean displayItemBean) {
        DiscrepancyNoteBean note = new DiscrepancyNoteBean();
        StudySubjectDAO ssdao = new StudySubjectDAO(ds);
        note.setDescription(message);
        note.setDetailedNotes("Failed Validation Check");
        note.setOwner(uab);
        note.setCreatedDate(new Date());
        note.setResolutionStatusId(ResolutionStatus.OPEN.getId());
        note.setDiscrepancyNoteTypeId(DiscrepancyNoteType.FAILEDVAL.getId());
        if (parentId != null) {
            note.setParentDnId(parentId);
        }

        note.setField(itemBean.getName());
        note.setStudyId(study.getId());
        note.setEntityName(itemBean.getName());
        note.setEntityType(DiscrepancyNoteBean.ITEM_DATA);
        note.setEntityValue(displayItemBean.getData().getValue());

        note.setEventName(eventCrfBean.getName());
        note.setEventStart(eventCrfBean.getCreatedDate());
        note.setCrfName(displayItemBean.getEventDefinitionCRF().getCrfName());

        StudySubjectBean ss = (StudySubjectBean) ssdao.findByPK(eventCrfBean.getStudySubjectId());
        note.setSubjectName(ss.getName());

        note.setEntityId(displayItemBean.getData().getId());
        note.setColumn("value");

        DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(ds);
        note = (DiscrepancyNoteBean) dndao.create(note);
        // so that the below method works, need to set the entity above
        // System.out.println("trying to create mapping with " + note.getId() +
        // " " + note.getEntityId() + " " + note.getColumn() + " " +
        // note.getEntityType());
        dndao.createMapping(note);
        // System.out.println("just created mapping");
        return note;
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public void processRequest() throws Exception {
        ItemDataDAO itemDataDao = new ItemDataDAO(sm.getDataSource());
        itemDataDao.setFormatDates(false);
        EventCRFDAO eventCrfDao = new EventCRFDAO(sm.getDataSource());
        CrfBusinessLogicHelper crfBusinessLogicHelper = new CrfBusinessLogicHelper(sm.getDataSource());
        String action = request.getParameter("action");

        FormProcessor fp = new FormProcessor(request);

        // checks which module the requests are from
        String module = fp.getString(MODULE);
        request.setAttribute(MODULE, module);

        resetPanel();
        panel.setStudyInfoShown(false);
        panel.setOrderedData(true);

        setToPanel(resword.getString("create_CRF"), respage.getString("br_create_new_CRF_entering"));

        setToPanel(resword.getString("create_CRF_version"), respage.getString("br_create_new_CRF_uploading"));
        setToPanel(resword.getString("revise_CRF_version"), respage.getString("br_if_you_owner_CRF_version"));
        setToPanel(resword.getString("CRF_spreadsheet_template"), respage.getString("br_download_blank_CRF_spreadsheet_from"));
        setToPanel(resword.getString("example_CRF_br_spreadsheets"), respage.getString("br_download_example_CRF_instructions_from"));

        if ("confirm".equalsIgnoreCase(action)) {
            List<DisplayItemBeanWrapper> displayItemBeanWrappers = (List<DisplayItemBeanWrapper>) session.getAttribute("importedData");
            logger.info("Size of displayItemBeanWrappers : " + displayItemBeanWrappers.size());
            ImportCRFInfoContainer importCrfInfo = (ImportCRFInfoContainer) session.getAttribute("importCrfInfo");
            for (DisplayItemBeanWrapper wrapper : displayItemBeanWrappers) {
                boolean resetSDV = false;
                EventCRFBean eventCrfBean = wrapper.getEventCrfBean();
                if (!findCRFInfo(importCrfInfo, eventCrfBean)) {
                    wrapper.setOverwrite(false);
                    continue;
                }
            }
            forwardPage(Page.VERIFY_IMPORT_CRF_DATA);
        }

        if ("save".equalsIgnoreCase(action)) {

            // setup ruleSets to run if applicable
            RuleSetServiceInterface ruleSetService = (RuleSetServiceInterface) SpringServletAccess.getApplicationContext(context).getBean("ruleSetService");
            List<ImportDataRuleRunnerContainer> containers = this.ruleRunSetup(sm.getDataSource(), currentStudy, ub, ruleSetService);

            List<DisplayItemBeanWrapper> displayItemBeanWrappers = (List<DisplayItemBeanWrapper>) session.getAttribute("importedData");
            // System.out.println("Size of displayItemBeanWrappers : " +
            // displayItemBeanWrappers.size());
            HashMap<String, String> importedCRFStatuses = (HashMap<String, String>) session.getAttribute("importedCRFStatuses");
            ImportCRFInfoContainer importCrfInfo = (ImportCRFInfoContainer) session.getAttribute("importCrfInfo");
            int skippedCRFCount = 0;
            for (DisplayItemBeanWrapper wrapper : displayItemBeanWrappers) {
                boolean resetSDV = false;
                EventCRFBean eventCrfBean = wrapper.getEventCrfBean();
                if (!findCRFInfo(importCrfInfo, eventCrfBean)) {
                    ++skippedCRFCount;
                    continue;
                }
                String eventCRFStatus = importedCRFStatuses
                        .get(eventCrfBean.getStudySubjectId() + "-" + eventCrfBean.getStudyEventId() + "-" + eventCrfBean.getFormLayoutId());
                if (eventCRFStatus != null &&
                        !eventCRFStatus.equals(DataEntryStage.INITIAL_DATA_ENTRY.getName()) &&
                        !eventCRFStatus.equals(DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE.getName())) {
                    continue;
                }

                if (eventCrfBean.getId() == 0) {
                    eventCrfDao.create(eventCrfBean);
                }

                // TODO : tom , the wrapper object has all the necessary data -
                // as you see we check the
                // is to see if this data is Savable if it is then we go ahead
                // and save it. if not we discard.
                // So the change needs to happen here , instead of discarding we
                // need to file discrepancy notes
                // and save the data. If you look in the
                // Page.VERIFY_IMPORT_CRF_DATA jsp file you can see how I am
                // pulling the errors. and use that in the same way.

                logger.info("right before we check to make sure it is savable: " + wrapper.isSavable());
                if (wrapper.isSavable()) {
                    ArrayList<Integer> eventCrfInts = new ArrayList<Integer>();
                    // based on the use case: "If any of the data does not meet
                    // validations specified in the CRF
                    // Template, a discrepancy note is automatically logged.
                    // The DN will have a type of Failed Validation Check, and
                    // a message of Failed Validation check."
                    // System.out.println("wrapper problems found : " +
                    // wrapper.getValidationErrors().toString());
                    for (DisplayItemBean displayItemBean : wrapper.getDisplayItemBeans()) {
                        displayItemBean.getData().setEventCRFId(eventCrfBean.getId());

                        logger.info("found value here: " + displayItemBean.getData().getValue());
                        logger.info("found status here: " + eventCrfBean.getStatus().getName());
                        // System.out.println("found event crf bean name here: "
                        // +
                        // eventCrfBean.getEventName()+" id "+eventCrfBean.getId
                        // ());
                        // SO, items can be created in a wrapper which is set to
                        // overwrite
                        // we get around this by checking the bean first, to
                        // make sure it's not null
                        ItemDataBean itemDataBean = new ItemDataBean();
                        itemDataBean = itemDataDao.findByItemIdAndEventCRFIdAndOrdinal(displayItemBean.getItem().getId(), eventCrfBean.getId(),
                                displayItemBean.getData().getOrdinal());
                        if (wrapper.isOverwrite() && itemDataBean.getStatus() != null) {
                            // ItemDataBean itemDataBean = new ItemDataBean();
                            // itemDataBean =
                            // itemDataDao.findByItemIdAndEventCRFIdAndOrdinal(
                            // displayItemBean.getItem().getId(),
                            // eventCrfBean.getId(), displayItemBean
                            // .getData().getOrdinal());
                            // itemDataBean =
                            // itemDataDao.findByEventCRFIdAndItemName(
                            // eventCrfBean,
                            // displayItemBean.getItem().getName());

                            if (!itemDataBean.getValue().equals(displayItemBean.getData().getValue()))
                                resetSDV = true;

                            logger.info("just tried to find item data bean on item name " + displayItemBean.getItem().getName());
                            itemDataBean.setUpdatedDate(new Date());
                            itemDataBean.setUpdater(ub);
                            itemDataBean.setValue(displayItemBean.getData().getValue());
                            // set status?
                            itemDataDao.update(itemDataBean);
                            logger.info("updated: " + itemDataBean.getItemId());
                            // need to set pk here in order to create dn
                            displayItemBean.getData().setId(itemDataBean.getId());
                        } else {
                            resetSDV = true;

                            itemDataDao.create(displayItemBean.getData());
                            logger.info("created: " + displayItemBean.getData().getItemId() + "event CRF ID = " + eventCrfBean.getId() + "CRF VERSION ID ="
                                    + eventCrfBean.getCRFVersionId());

                            // does this dao function work for repeating
                            // events/groups?
                            // ItemDataBean itemDataBean =
                            // itemDataDao.findByEventCRFIdAndItemName(
                            // eventCrfBean,
                            // displayItemBean.getItem().getName());
                            ItemDataBean itemDataBean2 = itemDataDao.findByItemIdAndEventCRFIdAndOrdinal(displayItemBean.getItem().getId(),
                                    eventCrfBean.getId(), displayItemBean.getData().getOrdinal());
                            logger.info("found: id " + itemDataBean2.getId() + " name " + itemDataBean2.getName());
                            displayItemBean.getData().setId(itemDataBean2.getId());
                        }
                        // logger.info("created item data bean:
                        // "+displayItemBean.getData().getId());
                        // logger.info("created:
                        // "+displayItemBean.getData().getName());
                        // logger.info("continued:
                        // "+displayItemBean.getData().getItemId());
                        ItemDAO idao = new ItemDAO(sm.getDataSource());
                        ItemBean ibean = (ItemBean) idao.findByPK(displayItemBean.getData().getItemId());
                        // logger.info("continued2: getName " +
                        // ibean.getName());
                        // System.out.println("*** checking for validation errors: "
                        // + ibean.getName());
                        String itemOid = displayItemBean.getItem().getOid() + "_" + wrapper.getStudyEventRepeatKey() + "_"
                                + displayItemBean.getData().getOrdinal() + "_" + wrapper.getStudySubjectOid();
                        if (wrapper.getValidationErrors().containsKey(itemOid)) {
                            ArrayList messageList = (ArrayList) wrapper.getValidationErrors().get(itemOid);
                            // if
                            // (wrapper.getValidationErrors().containsKey(ibean
                            // .getName())) {
                            // ArrayList messageList = (ArrayList)
                            // wrapper.getValidationErrors
                            // ().get(ibean.getName());
                            // could be more then one will have to iterate
                            // could it be more than one? tbh 08/2008
                            for (int iter = 0; iter < messageList.size(); iter++) {
                                String message = (String) messageList.get(iter);
                                DiscrepancyNoteBean parentDn = ImportSpringJob.createDiscrepancyNote(ibean, message, eventCrfBean, displayItemBean, null, ub,
                                        sm.getDataSource(), currentStudy);
                                ImportSpringJob.createDiscrepancyNote(ibean, message, eventCrfBean, displayItemBean, parentDn.getId(), ub, sm.getDataSource(),
                                        currentStudy);
                                // System.out.println("*** created disc note with message: "
                                // + message);
                                // displayItemBean);
                            }
                        }
                        // logger.info("created:
                        // "+displayItemBean.getDbData().getName());

                        if (eventCRFStatus != null && eventCRFStatus.equals(DataEntryStage.INITIAL_DATA_ENTRY.getName())
                                && eventCrfBean.getStatus().isAvailable()) {
                            crfBusinessLogicHelper.markCRFStarted(eventCrfBean, ub);
                        } else {
                            crfBusinessLogicHelper.markCRFComplete(eventCrfBean, ub);
                        }

                    }
                    // Reset the SDV status if item data has been changed or added
                    if (eventCrfBean != null && resetSDV)
                        eventCrfDao.setSDVStatus(false, ub.getId(), eventCrfBean.getId());

                    // end of item datas, tbh
                    // crfBusinessLogicHelper.markCRFComplete(eventCrfBean, ub);
                    // System .out.println("*** just updated event crf bean: "+
                    // eventCrfBean.getId());
                    // need to update the study event status as well, tbh
                    // crfBusinessLogicHelper.updateStudyEvent(eventCrfBean,
                    // ub);
                    // above should do it for us, tbh 08/2008
                }

            }

            if (CollectionUtils.size(displayItemBeanWrappers) == skippedCRFCount)
                addPageMessage(respage.getString("no_data_has_been_imported"));
            else
                addPageMessage(respage.getString("data_has_been_successfully_import"));

            addPageMessage(this.ruleActionWarnings(this.runRules(currentStudy, ub, containers, ruleSetService, ExecutionMode.SAVE)));

            // forwardPage(Page.SUBMIT_DATA_SERVLET);
            forwardPage(Page.LIST_STUDY_SUBJECTS_SERVLET);
            // replaced tbh, 06/2009
        }
    }

    private boolean findCRFInfo(ImportCRFInfoContainer importCrfInfoContainer, EventCRFBean eventCrfBean) {
        if (importCrfInfoContainer == null)
            return true;
        for (ImportCRFInfo importCRFInfo: importCrfInfoContainer.getImportCRFList()) {
            // this record still needs to be inserted
            if (importCRFInfo.getEventCRFID() == null)
                return true;
            if (importCRFInfo.getEventCRFID() == eventCrfBean.getId())
                return importCRFInfo.isProcessImport();
        }
        return false;
    }
    private List<ImportDataRuleRunnerContainer> ruleRunSetup(DataSource dataSource, StudyBean studyBean, UserAccountBean userBean,
            RuleSetServiceInterface ruleSetService) {
        List<ImportDataRuleRunnerContainer> containers = new ArrayList<ImportDataRuleRunnerContainer>();
        ODMContainer odmContainer = (ODMContainer) session.getAttribute("odmContainer");
        if (odmContainer != null) {
            ArrayList<SubjectDataBean> subjectDataBeans = odmContainer.getCrfDataPostImportContainer().getSubjectData();
            if (ruleSetService.getCountByStudy(studyBean) > 0) {
                ImportDataRuleRunnerContainer container;
                for (SubjectDataBean subjectDataBean : subjectDataBeans) {
                    container = new ImportDataRuleRunnerContainer();
                    container.initRuleSetsAndTargets(dataSource, studyBean, subjectDataBean, ruleSetService);
                    if (container.getShouldRunRules())
                        containers.add(container);
                }
                if (containers != null && !containers.isEmpty())
                    ruleSetService.runRulesInImportData(containers, studyBean, userBean, ExecutionMode.DRY_RUN);
            }
        }
        return containers;
    }

    private List<String> runRules(StudyBean studyBean, UserAccountBean userBean, List<ImportDataRuleRunnerContainer> containers,
            RuleSetServiceInterface ruleSetService, ExecutionMode executionMode) {
        List<String> messages = new ArrayList<String>();
        if (containers != null && !containers.isEmpty()) {
            HashMap<String, ArrayList<String>> summary = ruleSetService.runRulesInImportData(containers, studyBean, userBean, executionMode);
            messages = extractRuleActionWarnings(summary);
        }
        return messages;
    }

    private List<String> extractRuleActionWarnings(HashMap<String, ArrayList<String>> summaryMap) {
        List<String> messages = new ArrayList<String>();
        if (summaryMap != null && !summaryMap.isEmpty()) {
            for (String key : summaryMap.keySet()) {
                StringBuilder mesg = new StringBuilder(key + " : ");
                for (String s : summaryMap.get(key)) {
                    mesg.append(s + ", ");
                }
                messages.add(mesg.toString());
            }
        }
        return messages;
    }

    private String ruleActionWarnings(List<String> warnings) {
        if (warnings.isEmpty())
            return "";
        else {
            StringBuilder mesg = new StringBuilder("Rule Action Warnings: ");
            for (String s : warnings) {
                mesg.append(s + "; ");
            }
            return mesg.toString();
        }
    }
}