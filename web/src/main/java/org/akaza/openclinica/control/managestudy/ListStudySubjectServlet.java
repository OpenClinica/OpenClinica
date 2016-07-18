/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.*;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.SubjectGroupMapBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormDiscrepancyNotes;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.submit.AddNewSubjectServlet;
import org.akaza.openclinica.dao.managestudy.*;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.SubjectGroupMapDAO;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.bean.DisplayStudySubjectRow;
import org.akaza.openclinica.web.bean.EntityBeanTable;

import javax.sql.DataSource;
import java.util.*;

/**
 * @author jxu
 */
public abstract class ListStudySubjectServlet extends SecureController {

    // Shaoyu Su
    Locale locale;
    // BWP Issue 3195, 3330 <<
    public static String SUBJECT_PAGE_NUMBER = "ebl_page";
    public static String PAGINATING_QUERY = "paginatingQuery";
    public static String FILTER_KEYWORD = "ebl_filterKeyword";
    public static String SEARCH_SUBMITTED = "submitted";

    // >>

    // < ResourceBundleresword;

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        locale = LocaleResolver.getLocale(request);
        // < resword =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.words",locale);

        // BWP 3098 << close the info side panel and show icons
        request.setAttribute("closeInfoShowIcons", true);
        // >>

        // BWP 3195, 3330 : designed to address pagination issues
        String pageNumber = fp.getString(SUBJECT_PAGE_NUMBER);
        StringBuilder paginatingQuery = new StringBuilder("");

        String filterKeyword = fp.getString(FILTER_KEYWORD);

        String tmpSearch = fp.getString(SEARCH_SUBMITTED);
        boolean searchSubmitted =
            !(tmpSearch == null || "".equalsIgnoreCase(tmpSearch)) && !"".equalsIgnoreCase(filterKeyword) && !"+".equalsIgnoreCase(filterKeyword);

        if (pageNumber != null && !"".equalsIgnoreCase(pageNumber)) {
            int tempNum = 0;
            try {
                tempNum = Integer.parseInt(pageNumber);
            } catch (NumberFormatException nfe) {
                // tempNum is already initialized to 0
            }
            if (tempNum > 0) {
                paginatingQuery = new StringBuilder(SUBJECT_PAGE_NUMBER).append("=").append(pageNumber);
                paginatingQuery.append("&ebl_paginated=1");

            }
        }

        // URL encode the search keyword, since it will be a parameter in the
        // URL
        String filterKeywordURLEncode = java.net.URLEncoder.encode(filterKeyword, "UTF-8");
        if (searchSubmitted) {
            paginatingQuery.append("&ebl_sortColumnInd=0&submitted=1&ebl_sortAscending=1&ebl_filtered=1");
            paginatingQuery.append("&").append(FILTER_KEYWORD).append("=").append(filterKeywordURLEncode);
        }

        request.setAttribute(PAGINATING_QUERY, paginatingQuery.toString());

        StudyDAO stdao = new StudyDAO(sm.getDataSource());
        StudySubjectDAO sdao = new StudySubjectDAO(sm.getDataSource());
        StudyEventDAO sedao = new StudyEventDAO(sm.getDataSource());
        StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());
        SubjectGroupMapDAO sgmdao = new SubjectGroupMapDAO(sm.getDataSource());
        StudyGroupClassDAO sgcdao = new StudyGroupClassDAO(sm.getDataSource());
        StudyGroupDAO sgdao = new StudyGroupDAO(sm.getDataSource());
        StudySubjectDAO ssdao = new StudySubjectDAO(sm.getDataSource());

        // YW << update study parameters of current study.
        // "collectDob" and "genderRequired" are set as the same as the parent
        // study
        // tbh, also add the params "subjectPersonIdRequired",
        // "subjectIdGeneration", "subjectIdPrefixSuffix"
        int parentStudyId = currentStudy.getParentStudyId();
        ArrayList studyGroupClasses = new ArrayList();
        ArrayList allDefs = new ArrayList();
        // allDefs holds the list of study event definitions used in the table,
        // tbh
        if (parentStudyId > 0) {
            StudyBean parentStudy = (StudyBean) stdao.findByPK(parentStudyId);
            studyGroupClasses = sgcdao.findAllActiveByStudy(parentStudy);
            allDefs = seddao.findAllActiveByStudy(parentStudy);
        } else {
            parentStudyId = currentStudy.getId();
            studyGroupClasses = sgcdao.findAllActiveByStudy(currentStudy);
            allDefs = seddao.findAllActiveByStudy(currentStudy);
        }
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(sm.getDataSource());
        StudyParameterValueBean parentSPV = spvdao.findByHandleAndStudy(parentStudyId, "collectDob");
        currentStudy.getStudyParameterConfig().setCollectDob(parentSPV.getValue());
        parentSPV = spvdao.findByHandleAndStudy(parentStudyId, "genderRequired");
        currentStudy.getStudyParameterConfig().setGenderRequired(parentSPV.getValue());
        parentSPV = spvdao.findByHandleAndStudy(parentStudyId, "subjectPersonIdRequired");
        currentStudy.getStudyParameterConfig().setSubjectPersonIdRequired(parentSPV.getValue());
        parentSPV = spvdao.findByHandleAndStudy(parentStudyId, "subjectIdGeneration");
        currentStudy.getStudyParameterConfig().setSubjectIdGeneration(parentSPV.getValue());
        parentSPV = spvdao.findByHandleAndStudy(parentStudyId, "subjectIdPrefixSuffix");
        currentStudy.getStudyParameterConfig().setSubjectIdPrefixSuffix(parentSPV.getValue());

        // YW >>

        // for all the study groups for each group class
        for (int i = 0; i < studyGroupClasses.size(); i++) {
            StudyGroupClassBean sgc = (StudyGroupClassBean) studyGroupClasses.get(i);
            ArrayList groups = sgdao.findAllByGroupClass(sgc);
            sgc.setStudyGroups(groups);
        }
        request.setAttribute("studyGroupClasses", studyGroupClasses);

        // information for the event tabs
        session.setAttribute("allDefsArray", allDefs);
        session.setAttribute("allDefsNumber", new Integer(allDefs.size()));
        session.setAttribute("groupSize", new Integer(studyGroupClasses.size()));

        // find all the subjects in current study
        ArrayList subjects = sdao.findAllByStudyId(currentStudy.getId());

        ArrayList<DisplayStudySubjectBean> displayStudySubs = new ArrayList<DisplayStudySubjectBean>();
        // BEGIN LOOPING THROUGH SUBJECTS
        for (int i = 0; i < subjects.size(); i++) {
            StudySubjectBean studySub = (StudySubjectBean) subjects.get(i);

            ArrayList groups = (ArrayList) sgmdao.findAllByStudySubject(studySub.getId());

            ArrayList subGClasses = new ArrayList();
            for (int j = 0; j < studyGroupClasses.size(); j++) {
                StudyGroupClassBean sgc = (StudyGroupClassBean) studyGroupClasses.get(j);
                boolean hasClass = false;
                for (int k = 0; k < groups.size(); k++) {
                    SubjectGroupMapBean sgmb = (SubjectGroupMapBean) groups.get(k);
                    if (sgmb.getGroupClassName().equalsIgnoreCase(sgc.getName())) {
                        subGClasses.add(sgmb);
                        hasClass = true;
                        break;
                    }

                }
                if (!hasClass) {
                    subGClasses.add(new SubjectGroupMapBean());
                }

            }

            ArrayList subEvents = new ArrayList();
            // find all events order by definition ordinal
            ArrayList events = sedao.findAllByStudySubject(studySub);

            for (int j = 0; j < allDefs.size(); j++) {
                StudyEventDefinitionBean sed = (StudyEventDefinitionBean) allDefs.get(j);
                boolean hasDef = false;
                // StudyEventBean first = (StudyEventBean) events.get(0);
                // whack idea to try and set things right with the code
                // below, tbh
                // below needs to change, we need to reach into the list and
                // pick the next one, tbh
                // int blankid = first.getId();
                // logger.info("...set blank to "+blankid);
                for (int k = 0; k < events.size(); k++) {
                    StudyEventBean se = (StudyEventBean) events.get(k);
                    if (se.getStudyEventDefinitionId() == sed.getId()) {
                        se.setStudyEventDefinition(sed);

                        // logger.info(">>>found assigned id "+sed.getId()+" sed
                        // name: "+sed.getName()+" "+se.getId());
                        subEvents.add(se);
                        hasDef = true;
                    }

                }
                if (!hasDef) {
                    StudyEventBean blank = new StudyEventBean();
                    blank.setSubjectEventStatus(SubjectEventStatus.NOT_SCHEDULED);
                    blank.setStudyEventDefinitionId(sed.getId());
                    // how can we set the following:

                    // logger.info("...resetting blank id: "+blank.getId()+" to
                    // "+blankid);
                    // blank.setId(blankid);
                    blank.setStudyEventDefinition(sed);
                    // logger.info(">>>blank id: "+blank.getId());
                    // logger.info(">>>found unassigned id "+sed.getId()+" sed
                    // name: "+sed.getName()+" ");
                    subEvents.add(blank);
                }

            }

            // logger.info("subevents size after all adds: "+subEvents.size());
            // reorganize the events and find the repeating ones
            // subEvents:[aa bbb cc d ee]
            // finalEvents:[a(2) b(3) c(2) d e(2)]
            int prevDefId = 0;
            int currDefId = 0;
            ArrayList finalEvents = new ArrayList();
            int repeatingNum = 1;
            int count = 0;
            StudyEventBean event = new StudyEventBean();

            // begin looping through subject events
            for (int j = 0; j < subEvents.size(); j++) {
                StudyEventBean se = (StudyEventBean) subEvents.get(j);
                currDefId = se.getStudyEventDefinitionId();
                if (currDefId != prevDefId) {// find a new event
                    if (repeatingNum > 1) {
                        event.setRepeatingNum(repeatingNum);
                        repeatingNum = 1;
                    }
                    finalEvents.add(se); // add current event to final
                    event = se;
                    count++;
                    // logger.info("event id? "+event.getId());
                } else {// repeating event
                    repeatingNum++;
                    event.getRepeatEvents().add(se);
                    // logger.info("repeating size:" +
                    // event.getStudySubjectId()+
                    // event.getRepeatEvents().size());
                    if (j == subEvents.size() - 1) {
                        event.setRepeatingNum(repeatingNum);
                        repeatingNum = 1;

                    }
                }
                prevDefId = currDefId;
            }
            // end looping through subject events

            DisplayStudySubjectBean dssb = new DisplayStudySubjectBean();
            // logger.info("final events size: "+finalEvents.size());
            dssb.setStudyEvents(finalEvents);
            dssb.setStudySubject(studySub);
            dssb.setStudyGroups(subGClasses);
            displayStudySubs.add(dssb);
        }
        // END LOOPING THROUGH SUBJECTS

        // Set a subject property to determine whether to show a signed-type
        // icon (electronic signature)
        // in the JSP view or not
        // Get all event crfs by studyevent id; then use
        // EventDefinitionCRFDAO.isRequired to
        // determine whether any uncompleted CRFs are required.
        boolean isRequiredUncomplete = false;
        for (DisplayStudySubjectBean subject : displayStudySubs) {
            for (Iterator it = subject.getStudyEvents().iterator(); it.hasNext();) {
                StudyEventBean event = (StudyEventBean) it.next();
                if (event.getSubjectEventStatus() != null && event.getSubjectEventStatus().getId() == 3) {
                    // disallow the subject from signing any studies
                    subject.setStudySignable(false);
                    break;
                } else {
                    // determine whether the subject has any required,
                    // uncompleted event CRFs
                    isRequiredUncomplete = eventHasRequiredUncompleteCRFs(event);
                    if (isRequiredUncomplete) {
                        subject.setStudySignable(false);
                        break;
                    }
                }
            }
        }

        fp = new FormProcessor(request);
        EntityBeanTable table = fp.getEntityBeanTable();
        ArrayList allStudyRows = DisplayStudySubjectRow.generateRowsFromBeans(displayStudySubs);

        ArrayList columnArray = new ArrayList();

        columnArray.add(resword.getString("study_subject_ID"));
        columnArray.add(resword.getString("subject_status"));

        columnArray.add(resword.getString("OID"));
        columnArray.add(resword.getString("gender"));
        // new
        columnArray.add(resword.getString("secondary_ID"));
        for (int i = 0; i < studyGroupClasses.size(); i++) {
            StudyGroupClassBean sgc = (StudyGroupClassBean) studyGroupClasses.get(i);
            columnArray.add(sgc.getName());
        }
        for (int i = 0; i < allDefs.size(); i++) {
            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) allDefs.get(i);
            columnArray.add(sed.getName());
        }
        columnArray.add(resword.getString("actions"));
        String columns[] = new String[columnArray.size()];
        columnArray.toArray(columns);

        // String[] columns = {"ID", "Subject Status", "Gender", "Enrollment
        // Date",
        // "Study Events", "Actions" };
        table.setColumns(new ArrayList(Arrays.asList(columns)));
        table.setQuery(getBaseURL(), new HashMap());

        table.hideColumnLink(columnArray.size() - 1);

        // table.addLink("Enroll a new subject",
        // "javascript:leftnavExpand('addSubjectRowExpress');");
        table.setRows(allStudyRows);
        if (filterKeyword != null && !"".equalsIgnoreCase(filterKeyword)) {
            table.setKeywordFilter(filterKeyword);
        }
        table.computeDisplay();

        request.setAttribute("table", table);
        // request.setAttribute("subjects", subjects);

        String idSetting = currentStudy.getStudyParameterConfig().getSubjectIdGeneration();
        // logger.info("subject id setting :" + idSetting);
        // set up auto study subject id
        if (idSetting.equals("auto editable") || idSetting.equals("auto non-editable")) {
            //Shaoyu Su
            //int nextLabel = ssdao.findTheGreatestLabel() + 1;
            //request.setAttribute("label", new Integer(nextLabel).toString());
            request.setAttribute("label", resword.getString("id_generated_Save_Add"));
        }

        FormDiscrepancyNotes discNotes = new FormDiscrepancyNotes();
        session.setAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME, discNotes);

        forwardPage(getJSP());
    }

    protected abstract String getBaseURL();

    protected abstract Page getJSP();

    private boolean eventHasRequiredUncompleteCRFs(StudyEventBean studyEvent) {

        if (studyEvent == null)
            return false;

        EventCRFDAO eventCRFDAO = new EventCRFDAO(sm.getDataSource());
        EventDefinitionCRFDAO eventDefinitionDAO = new EventDefinitionCRFDAO(sm.getDataSource());
        List<EventCRFBean> crfBeans = new ArrayList<EventCRFBean>();

        crfBeans.addAll(eventCRFDAO.findAllByStudyEvent(studyEvent));
        // If the EventCRFBean has a completionStatusId of 0
        // (indicating that it is not complete),
        // then find
        // out whether it's required. If so, then return from the method false.
        for (EventCRFBean crfBean : crfBeans) {
            if (crfBean != null && crfBean.getCompletionStatusId() == 0) {
                if (eventDefinitionDAO.isRequiredInDefinition(crfBean.getCRFVersionId(), studyEvent)) {
                    return true;
                }
            }
        }

        return false;

    }

    public static DisplayStudyEventBean getDisplayStudyEventsForStudySubject(StudySubjectBean studySub, StudyEventBean event, DataSource ds,
            UserAccountBean ub, StudyUserRoleBean currentRole, StudyBean study) {
        StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(ds);
        StudyEventDAO sedao = new StudyEventDAO(ds);
        EventCRFDAO ecdao = new EventCRFDAO(ds);
        EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(ds);

        StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seddao.findByPK(event.getStudyEventDefinitionId());
        event.setStudyEventDefinition(sed);

        // find all active crfs in the definition
        ArrayList eventDefinitionCRFs = edcdao.findAllActiveByEventDefinitionId(sed.getId());

        ArrayList eventCRFs = ecdao.findAllByStudyEvent(event);

        // construct info needed on view study event page
        DisplayStudyEventBean de = new DisplayStudyEventBean();
        de.setStudyEvent(event);
        de.setDisplayEventCRFs(ViewStudySubjectServlet.getDisplayEventCRFs(ds, eventCRFs, eventDefinitionCRFs, ub, currentRole, event.getSubjectEventStatus(),
                study));
        ArrayList al = ViewStudySubjectServlet.getUncompletedCRFs(ds, eventDefinitionCRFs, eventCRFs, event.getSubjectEventStatus());
        // ViewStudySubjectServlet.populateUncompletedCRFsWithCRFAndVersions(ds,
        // al);
        de.setUncompletedCRFs(al);

        return de;
    }

}
