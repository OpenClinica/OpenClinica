package org.akaza.openclinica.web.crfdata;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.ItemDataType;
import org.akaza.openclinica.bean.core.NullValue;
import org.akaza.openclinica.bean.core.ResponseType;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.DisplayItemBean;
import org.akaza.openclinica.bean.submit.DisplayItemBeanWrapper;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.bean.submit.ResponseOptionBean;
import org.akaza.openclinica.bean.submit.crfdata.FormDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ImportItemDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ImportItemGroupDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ODMContainer;
import org.akaza.openclinica.bean.submit.crfdata.StudyEventDataBean;
import org.akaza.openclinica.bean.submit.crfdata.SubjectDataBean;
import org.akaza.openclinica.bean.submit.crfdata.SummaryStatsBean;
import org.akaza.openclinica.bean.submit.crfdata.UpsertOnBean;
import org.akaza.openclinica.control.form.DiscrepancyValidator;
import org.akaza.openclinica.control.form.FormDiscrepancyNotes;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.control.submit.ImportCRFInfoContainer;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.dao.submit.ItemGroupDAO;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

public class ImportCRFDataService {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private final DataSource ds;

    private ItemDataDAO itemDataDao;

    public static ResourceBundle respage;

    public ImportCRFDataService(DataSource ds, Locale locale) {
        ResourceBundleProvider.updateLocale(locale);
        respage = ResourceBundleProvider.getPageMessagesBundle(locale);
        this.ds = ds;
    }

    /*
     * purpose: look up EventCRFBeans by the following: Study Subject, Study Event, CRF Version, using the
     * findByEventSubjectVersion method in EventCRFDAO. May return more than one, hmm.
     */
    public List<EventCRFBean> fetchEventCRFBeans(ODMContainer odmContainer, UserAccountBean ub) {
        ArrayList<EventCRFBean> eventCRFBeans = new ArrayList<EventCRFBean>();
        ArrayList<Integer> eventCRFBeanIds = new ArrayList<Integer>();
        EventCRFDAO eventCrfDAO = new EventCRFDAO(ds);
        StudySubjectDAO studySubjectDAO = new StudySubjectDAO(ds);
        StudyEventDefinitionDAO studyEventDefinitionDAO = new StudyEventDefinitionDAO(ds);
        StudyDAO studyDAO = new StudyDAO(ds);
        StudyEventDAO studyEventDAO = new StudyEventDAO(ds);
        UpsertOnBean upsert = odmContainer.getCrfDataPostImportContainer().getUpsertOn();
        // If Upsert bean is not present, create one with default settings
        if (upsert == null)
            upsert = new UpsertOnBean();
        String studyOID = odmContainer.getCrfDataPostImportContainer().getStudyOID();
        StudyBean studyBean = studyDAO.findByOid(studyOID);
        ArrayList<SubjectDataBean> subjectDataBeans = odmContainer.getCrfDataPostImportContainer().getSubjectData();
        for (SubjectDataBean subjectDataBean : subjectDataBeans) {
            ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();

            StudySubjectBean studySubjectBean = studySubjectDAO.findByOidAndStudy(subjectDataBean.getSubjectOID(), studyBean.getId());
            for (StudyEventDataBean studyEventDataBean : studyEventDataBeans) {
                ArrayList<FormDataBean> formDataBeans = studyEventDataBean.getFormData();

                String sampleOrdinal = studyEventDataBean.getStudyEventRepeatKey() == null ? "1" : studyEventDataBean.getStudyEventRepeatKey();

                StudyEventDefinitionBean studyEventDefinitionBean = studyEventDefinitionDAO.findByOidAndStudy(studyEventDataBean.getStudyEventOID(),
                        studyBean.getId(), studyBean.getParentStudyId());
                logger.info("find all by def and subject " + studyEventDefinitionBean.getName() + " study subject " + studySubjectBean.getName());

                StudyEventBean studyEventBean = (StudyEventBean) studyEventDAO.findByStudySubjectIdAndDefinitionIdAndOrdinal(studySubjectBean.getId(),
                        studyEventDefinitionBean.getId(), Integer.parseInt(sampleOrdinal));
                // @pgawade 16-March-2011 Do not allow the data import
                // if event status is one of the - stopped, signed,
                // locked
                if (studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.LOCKED)
                        || studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.SIGNED)
                        || studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.STOPPED)) {
                    return null;
                }
                for (FormDataBean formDataBean : formDataBeans) {

                    CRFVersionDAO crfVersionDAO = new CRFVersionDAO(ds);

                    ArrayList<CRFVersionBean> crfVersionBeans = crfVersionDAO.findAllByOid(formDataBean.getFormOID());
                    for (CRFVersionBean crfVersionBean : crfVersionBeans) {

                        ArrayList<EventCRFBean> eventCrfBeans = eventCrfDAO.findByEventSubjectVersion(studyEventBean, studySubjectBean, crfVersionBean);
                        // what if we have begun with creating a study
                        // event, but haven't entered data yet? this would
                        // have us with a study event, but no corresponding
                        // event crf, yet.
                        if (eventCrfBeans.isEmpty()) {
                            logger.debug("   found no event crfs from Study Event id " + studyEventBean.getId() + ", location " + studyEventBean.getLocation());
                            // spell out criteria and create a bean if
                            // necessary, avoiding false-positives
                            if ((studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.SCHEDULED)
                                    || studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.DATA_ENTRY_STARTED) || studyEventBean
                                    .getSubjectEventStatus().equals(SubjectEventStatus.COMPLETED)) && upsert.isNotStarted()) {
                                EventCRFBean newEventCrfBean = new EventCRFBean();
                                newEventCrfBean.setStudyEventId(studyEventBean.getId());
                                newEventCrfBean.setStudySubjectId(studySubjectBean.getId());
                                newEventCrfBean.setCRFVersionId(crfVersionBean.getId());
                                newEventCrfBean.setDateInterviewed(new Date());
                                newEventCrfBean.setOwner(ub);
                                newEventCrfBean.setInterviewerName(ub.getName());
                                newEventCrfBean.setCompletionStatusId(1);// place
                                // filler
                                newEventCrfBean.setStatus(Status.AVAILABLE);
                                newEventCrfBean.setStage(DataEntryStage.INITIAL_DATA_ENTRY);
                                // these will be updated later in the
                                // workflow
                                newEventCrfBean = (EventCRFBean) eventCrfDAO.create(newEventCrfBean);
                                // eventCrfBeans.add(newEventCrfBean);
                                logger.debug("   created and added new event crf");

                                if (!eventCRFBeanIds.contains(newEventCrfBean.getId())) {
                                    eventCRFBeans.add(newEventCrfBean);
                                    eventCRFBeanIds.add(newEventCrfBean.getId());
                                }

                            }
                        }

                        // below to prevent duplicates

                        for (EventCRFBean ecb : eventCrfBeans) {
                            Integer ecbId = new Integer(ecb.getId());
                            if ((upsert.isDataEntryStarted() && ecb.getStage().equals(DataEntryStage.INITIAL_DATA_ENTRY))
                                    || (upsert.isDataEntryComplete() && ecb.getStage().equals(DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE)))
                                if (!eventCRFBeanIds.contains(ecbId)) {
                                    eventCRFBeans.add(ecb);
                                    eventCRFBeanIds.add(ecbId);
                                }
                        }
                    }
                }
            }
        }
        // if it's null, throw an error, since they should be existing beans for
        // iteration one
        return eventCRFBeans;
    }

    /*
     * purpose: returns false if any of the forms/EventCRFs fail the UpsertOnBean rules.
     */
    public boolean eventCRFStatusesValid(ODMContainer odmContainer, UserAccountBean ub) {
        ArrayList<EventCRFBean> eventCRFBeans = new ArrayList<EventCRFBean>();
        ArrayList<Integer> eventCRFBeanIds = new ArrayList<Integer>();
        EventCRFDAO eventCrfDAO = new EventCRFDAO(ds);
        StudySubjectDAO studySubjectDAO = new StudySubjectDAO(ds);
        StudyEventDefinitionDAO studyEventDefinitionDAO = new StudyEventDefinitionDAO(ds);
        StudyDAO studyDAO = new StudyDAO(ds);
        StudyEventDAO studyEventDAO = new StudyEventDAO(ds);
        UpsertOnBean upsert = odmContainer.getCrfDataPostImportContainer().getUpsertOn();
        // If Upsert bean is not present, create one with default settings
        if (upsert == null)
            upsert = new UpsertOnBean();
        String studyOID = odmContainer.getCrfDataPostImportContainer().getStudyOID();
        StudyBean studyBean = studyDAO.findByOid(studyOID);
        ArrayList<SubjectDataBean> subjectDataBeans = odmContainer.getCrfDataPostImportContainer().getSubjectData();
        for (SubjectDataBean subjectDataBean : subjectDataBeans) {
            ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();

            StudySubjectBean studySubjectBean = studySubjectDAO.findByOidAndStudy(subjectDataBean.getSubjectOID(), studyBean.getId());
            for (StudyEventDataBean studyEventDataBean : studyEventDataBeans) {
                ArrayList<FormDataBean> formDataBeans = studyEventDataBean.getFormData();

                String sampleOrdinal = studyEventDataBean.getStudyEventRepeatKey() == null ? "1" : studyEventDataBean.getStudyEventRepeatKey();

                StudyEventDefinitionBean studyEventDefinitionBean = studyEventDefinitionDAO.findByOidAndStudy(studyEventDataBean.getStudyEventOID(),
                        studyBean.getId(), studyBean.getParentStudyId());
                logger.info("find all by def and subject " + studyEventDefinitionBean.getName() + " study subject " + studySubjectBean.getName());

                StudyEventBean studyEventBean = (StudyEventBean) studyEventDAO.findByStudySubjectIdAndDefinitionIdAndOrdinal(studySubjectBean.getId(),
                        studyEventDefinitionBean.getId(), Integer.parseInt(sampleOrdinal));
                // @pgawade 16-March-2011 Do not allow the data import
                // if event status is one of the - stopped, signed,
                // locked
                if (studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.LOCKED)
                        || studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.SIGNED)
                        || studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.STOPPED)) {
                    return true;
                }
                for (FormDataBean formDataBean : formDataBeans) {

                    CRFVersionDAO crfVersionDAO = new CRFVersionDAO(ds);

                    ArrayList<CRFVersionBean> crfVersionBeans = crfVersionDAO.findAllByOid(formDataBean.getFormOID());
                    for (CRFVersionBean crfVersionBean : crfVersionBeans) {

                        ArrayList<EventCRFBean> eventCrfBeans = eventCrfDAO.findByEventSubjectVersion(studyEventBean, studySubjectBean, crfVersionBean);
                        // what if we have begun with creating a study
                        // event, but haven't entered data yet? this would
                        // have us with a study event, but no corresponding
                        // event crf, yet.
                        if (eventCrfBeans.isEmpty()) {
                            logger.debug("   found no event crfs from Study Event id " + studyEventBean.getId() + ", location " + studyEventBean.getLocation());
                            if ((studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.SCHEDULED)
                                    || studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.DATA_ENTRY_STARTED) || studyEventBean
                                    .getSubjectEventStatus().equals(SubjectEventStatus.COMPLETED))) {

                                if (!upsert.isNotStarted())
                                    return false;
                            }
                        }

                        // below to prevent duplicates

                        for (EventCRFBean ecb : eventCrfBeans) {
                            Integer ecbId = new Integer(ecb.getId());
                            if (!(ecb.getStage().equals(DataEntryStage.INITIAL_DATA_ENTRY) && upsert.isDataEntryStarted())
                                    && !(ecb.getStage().equals(DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE) && upsert.isDataEntryComplete()))
                                return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /*
     * purpose: Build a map of EventCRFs and the statuses they should have post-import. Assumes EventCRFs have been
     * created for "Not Started" forms.
     */
    public HashMap<Integer, String> fetchEventCRFStatuses(ODMContainer odmContainer) {
        HashMap<Integer, String> eventCRFStatuses = new HashMap<Integer, String>();
        EventCRFDAO eventCrfDAO = new EventCRFDAO(ds);
        StudySubjectDAO studySubjectDAO = new StudySubjectDAO(ds);
        StudyEventDefinitionDAO studyEventDefinitionDAO = new StudyEventDefinitionDAO(ds);
        StudyDAO studyDAO = new StudyDAO(ds);
        StudyEventDAO studyEventDAO = new StudyEventDAO(ds);

        String studyOID = odmContainer.getCrfDataPostImportContainer().getStudyOID();
        StudyBean studyBean = studyDAO.findByOid(studyOID);

        ArrayList<SubjectDataBean> subjectDataBeans = odmContainer.getCrfDataPostImportContainer().getSubjectData();
        for (SubjectDataBean subjectDataBean : subjectDataBeans) {
            ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();

            StudySubjectBean studySubjectBean = studySubjectDAO.findByOidAndStudy(subjectDataBean.getSubjectOID(), studyBean.getId());
            for (StudyEventDataBean studyEventDataBean : studyEventDataBeans) {
                ArrayList<FormDataBean> formDataBeans = studyEventDataBean.getFormData();

                String sampleOrdinal = studyEventDataBean.getStudyEventRepeatKey() == null ? "1" : studyEventDataBean.getStudyEventRepeatKey();

                StudyEventDefinitionBean studyEventDefinitionBean = studyEventDefinitionDAO.findByOidAndStudy(studyEventDataBean.getStudyEventOID(),
                        studyBean.getId(), studyBean.getParentStudyId());
                logger.info("find all by def and subject " + studyEventDefinitionBean.getName() + " study subject " + studySubjectBean.getName());

                StudyEventBean studyEventBean = (StudyEventBean) studyEventDAO.findByStudySubjectIdAndDefinitionIdAndOrdinal(studySubjectBean.getId(),
                        studyEventDefinitionBean.getId(), Integer.parseInt(sampleOrdinal));

                for (FormDataBean formDataBean : formDataBeans) {

                    CRFVersionDAO crfVersionDAO = new CRFVersionDAO(ds);

                    ArrayList<CRFVersionBean> crfVersionBeans = crfVersionDAO.findAllByOid(formDataBean.getFormOID());
                    for (CRFVersionBean crfVersionBean : crfVersionBeans) {
                        ArrayList<EventCRFBean> eventCrfBeans = eventCrfDAO.findByEventSubjectVersion(studyEventBean, studySubjectBean, crfVersionBean);
                        for (EventCRFBean ecb : eventCrfBeans) {
                            Integer ecbId = new Integer(ecb.getId());

                            if (!eventCRFStatuses.keySet().contains(ecbId) && formDataBean.getEventCRFStatus() != null) {
                                eventCRFStatuses.put(ecb.getId(), formDataBean.getEventCRFStatus());
                            }
                        }
                    }

                }
            }
        }
        return eventCRFStatuses;
    }

    public SummaryStatsBean generateSummaryStatsBean(ODMContainer odmContainer, List<DisplayItemBeanWrapper> wrappers, ImportCRFInfoContainer importCrfInfo) {
        int countSubjects = 0;
        int countEventCRFs = 0;
        int discNotesGenerated = 0;
        for (DisplayItemBeanWrapper wr : wrappers) {
            HashMap validations = wr.getValidationErrors();
            discNotesGenerated += validations.size();
        }
        ArrayList<SubjectDataBean> subjectDataBeans = odmContainer.getCrfDataPostImportContainer().getSubjectData();
        countSubjects += subjectDataBeans.size();
        for (SubjectDataBean subjectDataBean : subjectDataBeans) {
            ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();

            for (StudyEventDataBean studyEventDataBean : studyEventDataBeans) {
                ArrayList<FormDataBean> formDataBeans = studyEventDataBean.getFormData();
                // this would be the place to add more stats
                for (FormDataBean formDataBean : formDataBeans) {
                    countEventCRFs += 1;
                }
            }
        }

        SummaryStatsBean ssBean = new SummaryStatsBean();
        ssBean.setDiscNoteCount(discNotesGenerated);
        ssBean.setEventCrfCount(countEventCRFs);
        ssBean.setStudySubjectCount(countSubjects);
        ssBean.setSkippedCrfCount(importCrfInfo.getCountSkippedEventCrfs());
        return ssBean;
    }

    public List<DisplayItemBeanWrapper> lookupValidationErrors(HttpServletRequest request, ODMContainer odmContainer, UserAccountBean ub,
            HashMap<String, String> totalValidationErrors, HashMap<String, String> hardValidationErrors, ArrayList<Integer> permittedEventCRFIds)
            throws OpenClinicaException {

        DisplayItemBeanWrapper displayItemBeanWrapper = null;
        HashMap validationErrors = new HashMap();
        List<DisplayItemBeanWrapper> wrappers = new ArrayList<DisplayItemBeanWrapper>();
        ImportHelper importHelper = new ImportHelper();
        FormDiscrepancyNotes discNotes = new FormDiscrepancyNotes();
        DiscrepancyValidator discValidator = new DiscrepancyValidator(request, discNotes);
        // create a second Validator, this one for hard edit checks
        HashMap<String, String> hardValidator = new HashMap<String, String>();

        StudyEventDAO studyEventDAO = new StudyEventDAO(ds);
        StudyDAO studyDAO = new StudyDAO(ds);
        StudyBean studyBean = studyDAO.findByOid(odmContainer.getCrfDataPostImportContainer().getStudyOID());
        StudySubjectDAO studySubjectDAO = new StudySubjectDAO(ds);
        StudyEventDefinitionDAO sedDao = new StudyEventDefinitionDAO(ds);
        int maxOrdinal = 1;
        HashMap<String, ItemDataBean> blankCheck = new HashMap<String, ItemDataBean>();
        // << TBH #5548, checking for blank item datas
        String hardValidatorErrorMsgs = "";

        ArrayList<SubjectDataBean> subjectDataBeans = odmContainer.getCrfDataPostImportContainer().getSubjectData();
        int totalEventCRFCount = 0;
        int totalItemDataBeanCount = 0;
        // int totalValidationErrors = 0;
        for (SubjectDataBean subjectDataBean : subjectDataBeans) {
            ArrayList<DisplayItemBean> displayItemBeans = new ArrayList<DisplayItemBean>();
            logger.debug("iterating through subject data beans: found " + subjectDataBean.getSubjectOID());
            ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();
            totalEventCRFCount += studyEventDataBeans.size();

            // ArrayList<Integer> eventCRFBeanIds = new ArrayList<Integer>();
            // to stop repeats...?
            StudySubjectBean studySubjectBean = studySubjectDAO.findByOidAndStudy(subjectDataBean.getSubjectOID(), studyBean.getId());

            for (StudyEventDataBean studyEventDataBean : studyEventDataBeans) {
                // resetting max ordinal here, to try and stop 'multiple item creation'
                // 5975, tbh 10/2010
                maxOrdinal = 1;
                // << 5975, tbh 2010
                int parentStudyId = studyBean.getParentStudyId();
                StudyEventDefinitionBean sedBean = sedDao.findByOidAndStudy(studyEventDataBean.getStudyEventOID(), studyBean.getId(), parentStudyId);
                ArrayList<FormDataBean> formDataBeans = studyEventDataBean.getFormData();
                logger.debug("iterating through study event data beans: found " + studyEventDataBean.getStudyEventOID());

                int ordinal = 1;
                try {
                    ordinal = new Integer(studyEventDataBean.getStudyEventRepeatKey()).intValue();
                } catch (Exception e) {
                    // trying to catch NPEs, because tags can be without the
                    // repeat key
                }
                StudyEventBean studyEvent = (StudyEventBean) studyEventDAO.findByStudySubjectIdAndDefinitionIdAndOrdinal(studySubjectBean.getId(),
                        sedBean.getId(), ordinal);

                displayItemBeans = new ArrayList<DisplayItemBean>();
                // INIT here instead, tbh 08/2008

                for (FormDataBean formDataBean : formDataBeans) {
                    maxOrdinal = 1;// JN:Moving maxOrdinal here, so max ordinal is there per form rather than per study
                                   // eventData bean
                    displayItemBeanWrapper = null;
                    // displayItemBeans = new ArrayList<DisplayItemBean>();
                    // list initialized in only one place above, tbh 08/2008
                    CRFVersionDAO crfVersionDAO = new CRFVersionDAO(ds);
                    EventCRFDAO eventCRFDAO = new EventCRFDAO(ds);
                    ArrayList<CRFVersionBean> crfVersionBeans = crfVersionDAO.findAllByOid(formDataBean.getFormOID());
                    ArrayList<ImportItemGroupDataBean> itemGroupDataBeans = formDataBean.getItemGroupData();
                    // >>> tbh 05/22/2010
                    if ((crfVersionBeans == null) || (crfVersionBeans.size() == 0)) {
                        MessageFormat mf = new MessageFormat("");
                        mf.applyPattern(respage.getString("your_crf_version_oid_did_not_generate"));
                        Object[] arguments = { formDataBean.getFormOID() };

                        throw new OpenClinicaException(mf.format(arguments), "");
                    }
                    CRFVersionBean crfVersion = crfVersionBeans.get(0);
                    // if you have a mispelled form oid you get an error here
                    // need to error out gracefully and post an error
                    logger.debug("iterating through form beans: found " + crfVersion.getOid());
                    // may be the point where we cut off item groups etc and
                    // instead work on sections
                    EventCRFBean eventCRFBean = eventCRFDAO.findByEventCrfVersion(studyEvent, crfVersion);
                    // >>tbh, 09/2008
                    // logger.debug("found event crf bean: using study event " + studyEvent.getId() +
                    // " crf version name: " + crfVersion.getOid());
                    // >>
                    EventDefinitionCRFDAO eventDefinitionCRFDAO = new EventDefinitionCRFDAO(ds);
                    EventDefinitionCRFBean eventDefinitionCRF = eventDefinitionCRFDAO.findByStudyEventIdAndCRFVersionId(studyBean, studyEvent.getId(),
                            crfVersion.getId());
                    if (eventCRFBean != null) {
                        if (permittedEventCRFIds.contains(new Integer(eventCRFBean.getId()))) {
                            // if and only if it's in the correct status do we need
                            // to generate the beans
                            // <<tbh, 09/2008
                            // also need to create a group name checker here for
                            // correctness, tbh
                            for (ImportItemGroupDataBean itemGroupDataBean : itemGroupDataBeans) {

                                ArrayList<ItemBean> blankCheckItems = new ArrayList<ItemBean>();
                                // << TBH reset of the blank checks
                                ArrayList<ImportItemDataBean> itemDataBeans = itemGroupDataBean.getItemData();
                                logger.debug("iterating through group beans: " + itemGroupDataBean.getItemGroupOID());
                                // put a checker in here
                                ItemGroupDAO itemGroupDAO = new ItemGroupDAO(ds);
                                ItemGroupBean testBean = itemGroupDAO.findByOid(itemGroupDataBean.getItemGroupOID());
                                if (testBean == null) {
                                    // TODO i18n of message
                                    // logger.debug("hit the exception for item groups! " +
                                    // itemGroupDataBean.getItemGroupOID());
                                    MessageFormat mf = new MessageFormat("");
                                    mf.applyPattern(respage.getString("your_item_group_oid_for_form_oid"));
                                    Object[] arguments = { itemGroupDataBean.getItemGroupOID(), formDataBean.getFormOID() };

                                    throw new OpenClinicaException(mf.format(arguments), "");
                                    // throw new OpenClinicaException("message
                                    // here", "");
                                    // your_item_group_oid_for_form_oid
                                }
                                totalItemDataBeanCount += itemDataBeans.size();

                                for (ImportItemDataBean importItemDataBean : itemDataBeans) {
                                    logger.debug("   iterating through item data beans: " + importItemDataBean.getItemOID());
                                    ItemDAO itemDAO = new ItemDAO(ds);
                                    ItemFormMetadataDAO itemFormMetadataDAO = new ItemFormMetadataDAO(ds);
                                    // ItemDataDAO itemDataDAO = new ItemDataDAO(ds);

                                    List<ItemBean> itemBeans = itemDAO.findByOid(importItemDataBean.getItemOID());
                                    if (!itemBeans.isEmpty()) {
                                        ItemBean itemBean = itemBeans.get(0);
                                        logger.debug("   found " + itemBean.getName());
                                        // throw a
                                        // null
                                        // pointer?
                                        // hopefully
                                        // not if
                                        // its been
                                        // checked...
                                        DisplayItemBean displayItemBean = new DisplayItemBean();
                                        displayItemBean.setItem(itemBean);

                                        ArrayList<ItemFormMetadataBean> metadataBeans = itemFormMetadataDAO.findAllByItemId(itemBean.getId());
                                        logger.debug("      found metadata item beans: " + metadataBeans.size());
                                        // groupOrdinal = the ordinal in item
                                        // groups,
                                        // for repeating items
                                        int groupOrdinal = 1;
                                        if (itemGroupDataBean.getItemGroupRepeatKey() != null) {
                                            try {
                                                groupOrdinal = new Integer(itemGroupDataBean.getItemGroupRepeatKey()).intValue();
                                                if (groupOrdinal > maxOrdinal) {
                                                    maxOrdinal = groupOrdinal;
                                                }
                                            } catch (Exception e) {
                                                // do nothing here currently, we are
                                                // looking for a number format
                                                // exception
                                                // from the above.
                                                logger.debug("found npe for group ordinals, line 344!");
                                            }
                                        }
                                        ItemDataBean itemDataBean = createItemDataBean(itemBean, eventCRFBean, importItemDataBean.getValue(), ub, groupOrdinal);
                                        blankCheckItems.add(itemBean);
                                        String newKey = groupOrdinal + "_" + itemGroupDataBean.getItemGroupOID() + "_" + itemBean.getOid() + "_"
                                                + subjectDataBean.getSubjectOID();
                                        blankCheck.put(newKey, itemDataBean);
                                        logger.info("adding " + newKey + " to blank checks");
                                        if (!metadataBeans.isEmpty()) {
                                            ItemFormMetadataBean metadataBean = metadataBeans.get(0);
                                            // also
                                            // possible
                                            // nullpointer

                                            displayItemBean.setData(itemDataBean);
                                            displayItemBean.setMetadata(metadataBean);
                                            // set event def crf?
                                            displayItemBean.setEventDefinitionCRF(eventDefinitionCRF);
                                            String eventCRFRepeatKey = studyEventDataBean.getStudyEventRepeatKey();
                                            // if you do indeed leave off this in the XML it will pass but return 'null'
                                            // tbh
                                            attachValidator(displayItemBean, importHelper, discValidator, hardValidator, request, eventCRFRepeatKey,
                                                    studySubjectBean.getOid());
                                            displayItemBeans.add(displayItemBean);

                                        } else {
                                            MessageFormat mf = new MessageFormat("");
                                            mf.applyPattern(respage.getString("no_metadata_could_be_found"));
                                            Object[] arguments = { importItemDataBean.getItemOID() };

                                            throw new OpenClinicaException(mf.format(arguments), "");
                                            // respage.getString("No Metadata could
                                            // be
                                            // found for your item key " +
                                            // importItemDataBean.getItemOID(), "");
                                        }
                                    } else {
                                        // report the error there
                                        MessageFormat mf = new MessageFormat("");
                                        mf.applyPattern(respage.getString("no_item_could_be_found"));
                                        Object[] arguments = { importItemDataBean.getItemOID() };

                                        throw new OpenClinicaException(mf.format(arguments), "");

                                        // throw new OpenClinicaException("No Item
                                        // could
                                        // be found with the key " +
                                        // importItemDataBean.getItemOID(), "");
                                    }
                                }// end item data beans
                                logger.debug(".. found blank check: " + blankCheck.toString());
                                // logger.debug(".. and found blank check items: " + blankCheckItems.toString());
                                // >> TBH #5548 did we create any repeated blank spots? If so, fill them in with an Item
                                // Data Bean

                                // JN: this max Ordinal can cause problems as it assumes the latest values, and when
                                // there
                                // is an entry with nongroup towards the
                                // end. Refer: MANTIS 6113

                                for (int i = 1; i <= maxOrdinal; i++) {
                                    for (ItemBean itemBean : blankCheckItems) {
                                        String newKey = i + "_" + itemGroupDataBean.getItemGroupOID() + "_" + itemBean.getOid() + "_"
                                                + subjectDataBean.getSubjectOID();
                                        if (blankCheck.get(newKey) == null) {
                                            // if it already exists, Do Not Add It.
                                            ItemDataBean itemDataCheck = getItemDataDao().findByItemIdAndEventCRFIdAndOrdinal(itemBean.getId(),
                                                    eventCRFBean.getId(), i);
                                            logger.debug("found item data bean id: " + itemDataCheck.getId() + " for ordinal " + i);
                                            if (itemDataCheck.getId() == 0) {
                                                ItemDataBean blank = createItemDataBean(itemBean, eventCRFBean, "", ub, i);
                                                DisplayItemBean displayItemBean = new DisplayItemBean();
                                                displayItemBean.setItem(itemBean);
                                                displayItemBean.setData(blank);
                                                // displayItemBean.setMetadata(metadataBean);
                                                // set event def crf?
                                                displayItemBean.setEventDefinitionCRF(eventDefinitionCRF);
                                                String eventCRFRepeatKey = studyEventDataBean.getStudyEventRepeatKey();
                                                // if you do indeed leave off this in the XML it will pass but return
                                                // 'null'
                                                // tbh

                                                displayItemBeans.add(displayItemBean);
                                                logger.debug("... adding display item bean");
                                            }
                                        }
                                        logger.debug("found a blank at " + i + ", adding " + blankCheckItems.size() + " blank items");
                                    }
                                }
                                // << tbh #5548
                                blankCheckItems = new ArrayList<ItemBean>();
                            }// end item group data beans

                        }// matches if on permittedCRFIDs

                        CRFDAO crfDAO = new CRFDAO(ds);
                        CRFBean crfBean = crfDAO.findByVersionId(crfVersion.getCrfId());
                        // seems like an extravagance, but is not contained in crf
                        // version or event crf bean
                        validationErrors = discValidator.validate();
                        // totalValidationErrors += validationErrors.size();
                        // totalValidationErrors.
                        // totalValidationErrors.addAll(validationErrors);
                        for (Object errorKey : validationErrors.keySet()) {
                            // logger.debug(errorKey.toString() + " -- " + validationErrors.get(errorKey));
                            // JN: to avoid duplicate errors
                            if (!totalValidationErrors.containsKey(errorKey.toString()))
                                totalValidationErrors.put(errorKey.toString(), validationErrors.get(errorKey).toString());
                            // assuming that this will be put back in to the core
                            // method's hashmap, updating statically, tbh 06/2008
                            logger.debug("+++ adding " + errorKey.toString());
                        }
                        logger.debug("-- hard validation checks: --");
                        for (Object errorKey : hardValidator.keySet()) {
                            logger.debug(errorKey.toString() + " -- " + hardValidator.get(errorKey));
                            hardValidationErrors.put(errorKey.toString(), hardValidator.get(errorKey));
                            // updating here 'statically' tbh 06/2008
                            hardValidatorErrorMsgs += hardValidator.get(errorKey) + "<br/><br/>";
                        }

                        String studyEventId = studyEvent.getId() + "";
                        String crfVersionId = crfVersion.getId() + "";

                        logger.debug("creation of wrapper: original count of display item beans " + displayItemBeans.size() + ", count of item data beans "
                                + totalItemDataBeanCount + " count of validation errors " + validationErrors.size() + " count of study subjects "
                                + subjectDataBeans.size() + " count of event crfs " + totalEventCRFCount + " count of hard error checks "
                                + hardValidator.size());
                        // possibly create the import summary here
                        logger.debug("creation of wrapper: max ordinal found " + maxOrdinal);
                        // check if we need to overwrite
                        DataEntryStage dataEntryStage = eventCRFBean.getStage();
                        Status eventCRFStatus = eventCRFBean.getStatus();
                        boolean overwrite = false;
                        // tbh >>
                        // //JN: Commenting out the following 2 lines, coz the prompt should come in the cases on
                        if (// eventCRFStatus.equals(Status.UNAVAILABLE) ||
                        dataEntryStage.equals(DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE) || dataEntryStage.equals(DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE)
                                || dataEntryStage.equals(DataEntryStage.INITIAL_DATA_ENTRY) || dataEntryStage.equals(DataEntryStage.DOUBLE_DATA_ENTRY)) {
                            overwrite = true;
                        }
                        // << tbh, adding extra statuses to prevent appending, 06/2009

                        // SummaryStatsBean ssBean = new SummaryStatsBean();
                        // ssBean.setDiscNoteCount(totalValidationErrors);
                        // ssBean.setEventCrfCount(totalEventCRFCount);
                        // ssBean.setStudySubjectCount(subjectDataBeans.size());
                        // // add other stats here, tbh
                        // not working here, need to do it in a different method,
                        // tbh

                        // summary stats added tbh 05/2008
                        // JN: Changed from validationErrors to totalValidationErrors to create discrepancy notes for
                        // all
                        // the
                        displayItemBeanWrapper = new DisplayItemBeanWrapper(displayItemBeans, true, overwrite, validationErrors, studyEventId, crfVersionId,
                                studyEventDataBean.getStudyEventOID(), studySubjectBean.getLabel(), eventCRFBean.getCreatedDate(), crfBean.getName(),
                                crfVersion.getName(), studySubjectBean.getOid(), studyEventDataBean.getStudyEventRepeatKey());

                        // JN: Commenting out the following code, since we shouldn't re-initialize at this point, as
                        // validationErrors would get overwritten and the
                        // older errors will be overriden. Moving it after the form.
                        // Removing the comments for now, since it seems to be creating duplicate Discrepancy Notes.
                        validationErrors = new HashMap();
                        discValidator = new DiscrepancyValidator(request, discNotes);
                        // reset to allow for new errors...
                    }
                }// after forms
                 // validationErrors = new HashMap();
                 // discValidator = new DiscrepancyValidator(request, discNotes);
                if (displayItemBeanWrapper != null && displayItemBeans.size() > 0)
                    wrappers.add(displayItemBeanWrapper);
            }// after study events

            // remove repeats here? remove them below by only forwarding the
            // first
            // each wrapper represents an Event CRF and a Form, but we don't
            // have all events for all forms
            // need to not add a wrapper for every event + form combination,
            // but instead for every event + form combination which is present
            // look at the hack below and see what happens
        }// after study subjects

        // throw the OC exception here at the end, if hard edit checks are not
        // empty

        // we statically update the hash map here, so it should not have to be
        // thrown, tbh 06/2008
        if (!hardValidator.isEmpty()) {
            // throw new OpenClinicaException(hardValidatorErrorMsgs, "");
        }
        return wrappers;
    }

    private ItemDataBean createItemDataBean(ItemBean itemBean, EventCRFBean eventCrfBean, String value, UserAccountBean ub, int ordinal) {

        ItemDataBean itemDataBean = new ItemDataBean();
        itemDataBean.setItemId(itemBean.getId());
        itemDataBean.setEventCRFId(eventCrfBean.getId());
        itemDataBean.setCreatedDate(new Date());
        itemDataBean.setOrdinal(ordinal);
        itemDataBean.setOwner(ub);
        itemDataBean.setStatus(Status.UNAVAILABLE);
        itemDataBean.setValue(value);

        return itemDataBean;
    }

    private void attachValidator(DisplayItemBean displayItemBean, ImportHelper importHelper, DiscrepancyValidator v, HashMap<String, String> hardv,
            javax.servlet.http.HttpServletRequest request, String eventCRFRepeatKey, String studySubjectOID) throws OpenClinicaException {
        org.akaza.openclinica.bean.core.ResponseType rt = displayItemBean.getMetadata().getResponseSet().getResponseType();
        String itemOid = displayItemBean.getItem().getOid() + "_" + eventCRFRepeatKey + "_" + displayItemBean.getData().getOrdinal() + "_" + studySubjectOID;
        // note the above, generating an ordinal on top of the OID to view
        // errors. adding event crf repeat key here, tbh
        // need to add a per-subject differentator, what if its a diff subject
        // with the same item, repeat key and ordinal???

        if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.TEXT) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.TEXTAREA)) {
            ItemFormMetadataBean ifm = displayItemBean.getMetadata();
            String widthDecimal = ifm.getWidthDecimal();
            // what if it's a date? parse if out so that we go from iso 8601 to
            // mm/dd/yyyy
            if (displayItemBean.getItem().getDataType().equals(ItemDataType.DATE)) {
                // pass it if it is blank, tbh
                if (!"".equals(displayItemBean.getData().getValue())) {
                    String dateValue = displayItemBean.getData().getValue();
                    SimpleDateFormat sdf_sqldate = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        Date originalDate = sdf_sqldate.parse(dateValue);
                        String replacementValue = new SimpleDateFormat("MM/dd/yyyy").format(originalDate);
                        displayItemBean.getData().setValue(dateValue);
                    } catch (ParseException pe1) {

                        // next version; fail if it does not pass iso 8601
                        MessageFormat mf = new MessageFormat("");
                        mf.applyPattern(respage.getString("you_have_a_date_value_which_is_not"));
                        Object[] arguments = { displayItemBean.getItem().getOid() };

                        hardv.put(itemOid, mf.format(arguments));

                    }
                }

            } else if (displayItemBean.getItem().getDataType().equals(ItemDataType.ST)) {
                int width = Validator.parseWidth(widthDecimal);
                if (width > 0 && displayItemBean.getData().getValue().length() > width) {
                    hardv.put(itemOid, "This value exceeds required width=" + width);
                }
            }
            // what if it's a number? should be only numbers
            else if (displayItemBean.getItem().getDataType().equals(ItemDataType.INTEGER)) {
                try {
                    Integer testInt = new Integer(displayItemBean.getData().getValue());
                    int width = Validator.parseWidth(widthDecimal);
                    if (width > 0 && displayItemBean.getData().getValue().length() > width) {
                        hardv.put(itemOid, "This value exceeds required width=" + width);
                    }
                    // now, didn't check decimal for testInt.
                } catch (Exception e) {// should be a sub class
                    // pass if blank, tbh 07/2010
                    if (!"".equals(displayItemBean.getData().getValue())) {
                        hardv.put(itemOid, "This value is not an integer.");
                    }
                }
            }
            // what if it's a float? should be only numbers
            else if (displayItemBean.getItem().getDataType().equals(ItemDataType.REAL)) {
                try {
                    Float testFloat = new Float(displayItemBean.getData().getValue());
                    int width = Validator.parseWidth(widthDecimal);
                    if (width > 0 && displayItemBean.getData().getValue().length() > width) {
                        hardv.put(itemOid, "This value exceeds required width=" + width);
                    }
                    int decimal = Validator.parseDecimal(widthDecimal);
                    if (decimal > 0 && BigDecimal.valueOf(new Double(displayItemBean.getData().getValue()).doubleValue()).scale() > decimal) {
                        hardv.put(itemOid, "This value exceeds required decimal=" + decimal);
                    }
                } catch (Exception ee) {
                    // pass if blank, tbh
                    if (!"".equals(displayItemBean.getData().getValue())) {
                        hardv.put(itemOid, "This value is not a real number.");
                    }
                }
            }
            // what if it's a phone number? how often does that happen?

            request.setAttribute(itemOid, displayItemBean.getData().getValue());
            displayItemBean = importHelper.validateDisplayItemBeanText(v, displayItemBean, itemOid);
            // errors = v.validate();

        } else if (rt.equals(ResponseType.CALCULATION) || rt.equals(ResponseType.GROUP_CALCULATION)) {
            if (displayItemBean.getItem().getDataType().equals(ItemDataType.REAL)) {
                ItemFormMetadataBean ifm = displayItemBean.getMetadata();
                String widthDecimal = ifm.getWidthDecimal();
                int decimal = Validator.parseDecimal(widthDecimal);
                if (decimal > 0) {
                    try {
                        Double d = new Double(displayItemBean.getData().getValue());
                        if (BigDecimal.valueOf(d).scale() > decimal) {
                            hardv.put(itemOid, "This value exceeds required decimal=" + decimal);
                        }
                    } catch (Exception e) {
                        if (!"".equals(displayItemBean.getData().getValue())) {
                            hardv.put(itemOid, "This value is not a real number.");
                        }
                    }
                }
            }
        }
        // JN commenting out the following as we dont need hard edits for these cases, they need to be soft stops and DN
        // should be filed REF MANTIS 6271

        else if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.RADIO) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.SELECT)) {
            // logger.info(itemOid + "is a RADIO or
            // a SELECT ");
            // adding a new hard edit check here; response_option mismatch
            String theValue = matchValueWithOptions(displayItemBean, displayItemBean.getData().getValue(), displayItemBean.getMetadata().getResponseSet()
                    .getOptions());
            request.setAttribute(itemOid, theValue);
            logger.debug("        found the value for radio/single: " + theValue);
            if (theValue == null && displayItemBean.getData().getValue() != null && !displayItemBean.getData().getValue().isEmpty()) {
                // fail it here
                logger.debug("-- theValue was NULL, the real value was " + displayItemBean.getData().getValue());
                hardv.put(itemOid, "This is not in the correct response set.");
                // throw new OpenClinicaException("One of your items did not
                // have the correct response set. Please review Item OID "
                // + displayItemBean.getItem().getOid() + " repeat key " +
                // displayItemBean.getData().getOrdinal(), "");
            }
            displayItemBean = importHelper.validateDisplayItemBeanSingleCV(v, displayItemBean, itemOid);
            // errors = v.validate();
        } else if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.CHECKBOX) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.SELECTMULTI)) {
            // logger.info(itemOid + "is a CHECKBOX
            // or a SELECTMULTI ");
            String theValue = matchValueWithManyOptions(displayItemBean, displayItemBean.getData().getValue(), displayItemBean.getMetadata().getResponseSet()
                    .getOptions());
            request.setAttribute(itemOid, theValue);
            // logger.debug("        found the value for checkbx/multi: " + theValue);
            if (theValue == null && displayItemBean.getData().getValue() != null && !displayItemBean.getData().getValue().isEmpty()) {
                // fail it here? found an 0,1 in the place of a NULL
                // logger.debug("--  theValue was NULL, the real value was " + displayItemBean.getData().getValue());
                hardv.put(itemOid, "This is not in the correct response set.");
            }
            displayItemBean = importHelper.validateDisplayItemBeanMultipleCV(v, displayItemBean, itemOid);
            // errors = v.validate();
        }
    }

    private String matchValueWithOptions(DisplayItemBean displayItemBean, String value, List options) {
        String returnedValue = null;
        if (!options.isEmpty()) {
            for (Object responseOption : options) {
                ResponseOptionBean responseOptionBean = (ResponseOptionBean) responseOption;
                if (responseOptionBean.getValue().equals(value)) {
                    // if (((ResponseOptionBean)
                    // responseOption).getText().equals(value)) {
                    displayItemBean.getData().setValue(((ResponseOptionBean) responseOption).getValue());
                    return ((ResponseOptionBean) responseOption).getValue();

                }
            }
        }
        return returnedValue;
    }

    /*
     * difference from the above is only a 'contains' in the place of an 'equals'. and a few other switches...also need
     * to keep in mind that there are non-null values that need to be taken into account
     */
    private String matchValueWithManyOptions(DisplayItemBean displayItemBean, String value, List options) {
        String returnedValue = null;
        // boolean checkComplete = true;
        String entireOptions = "";

        String[] simValues = value.split(",");

        String simValue = value.replace(",", "");
        // also remove all spaces, so they will fit up with the entire set
        // of options
        simValue = simValue.replace(" ", "");
        boolean checkComplete = true;
        if (!options.isEmpty()) {
            for (Object responseOption : options) {
                ResponseOptionBean responseOptionBean = (ResponseOptionBean) responseOption;
                // logger.debug("testing response option bean get value: " + responseOptionBean.getValue());
                // entireOptions += responseOptionBean.getValue() + "{0,1}|";//
                // once, or not at all
                entireOptions += responseOptionBean.getValue();
                // trying smth new, tbh 01/2009

                // logger.debug("checking on this string: " +
                // entireOptions + " versus this value " + simValue);
                // checkComplete = Pattern.matches(entireOptions, simValue); //
                // switch values, tbh

                // if (!checkComplete) {
                // return returnedValue;
                // }

            }
            // remove spaces, since they are causing problems:
            entireOptions = entireOptions.replace(" ", "");
            // following may be superfluous, tbh

            ArrayList nullValues = displayItemBean.getEventDefinitionCRF().getNullValuesList();

            for (Object nullValue : nullValues) {
                NullValue nullValueTerm = (NullValue) nullValue;
                entireOptions += nullValueTerm.getName();
                // logger.debug("found and added " + nullValueTerm.getName());
            }

            for (String sim : simValues) {
                sim = sim.replace(" ", "");
                // logger.debug("checking on this string: " + entireOptions + " versus this value " + sim);
                checkComplete = entireOptions.contains(sim);// Pattern.matches(
                // entireOptions,
                // sim);
                if (!checkComplete) {
                    return returnedValue;
                }
            }
        }
        return value;
    }

    /*
     * meant to answer the following questions 3.a. is that study subject in that study? 3.b. is that study event def in
     * that study? 3.c. is that site in that study? 3.d. is that crf version in that study event def? 3.e. are those
     * item groups in that crf version? 3.f. are those items in that item group?
     */
    public List<String> validateStudyMetadata(ODMContainer odmContainer, int currentStudyId) {
        List<String> errors = new ArrayList<String>();
        MessageFormat mf = new MessageFormat("");

        // throw new OpenClinicaException(mf.format(arguments), "");
        try {
            StudyDAO studyDAO = new StudyDAO(ds);
            String studyOid = odmContainer.getCrfDataPostImportContainer().getStudyOID();
            StudyBean studyBean = studyDAO.findByOid(studyOid);
            if (studyBean == null) {
                mf.applyPattern(respage.getString("your_study_oid_does_not_reference_an_existing"));
                Object[] arguments = { studyOid };

                errors.add(mf.format(arguments));
                // errors.add("Your Study OID " + studyOid + " does not
                // reference an existing Study or Site in the database. Please
                // check it and try again.");
                // throw an error here because getting the ID would be difficult
                // otherwise
                logger.debug("unknown study OID");
                throw new OpenClinicaException("Unknown Study OID", "");

            } else if (studyBean.getId() != currentStudyId) {
                mf.applyPattern(respage.getString("your_current_study_is_not_the_same_as"));
                Object[] arguments = { studyBean.getName() };
                //
                // errors.add("Your current study is not the same as the Study "
                // + studyBean.getName()
                // + ", for which you are trying to enter data. Please log out
                // of your current study and into the study for which the data
                // is keyed.");
                errors.add(mf.format(arguments));
            }
            ArrayList<SubjectDataBean> subjectDataBeans = odmContainer.getCrfDataPostImportContainer().getSubjectData();

            StudySubjectDAO studySubjectDAO = new StudySubjectDAO(ds);
            StudyEventDefinitionDAO studyEventDefinitionDAO = new StudyEventDefinitionDAO(ds);
            CRFVersionDAO crfVersionDAO = new CRFVersionDAO(ds);
            ItemGroupDAO itemGroupDAO = new ItemGroupDAO(ds);
            ItemDAO itemDAO = new ItemDAO(ds);

            if (subjectDataBeans != null) {// need to do this so as not to
                // throw the exception below and
                // report all available errors, tbh
                for (SubjectDataBean subjectDataBean : subjectDataBeans) {
                    String oid = subjectDataBean.getSubjectOID();
                    StudySubjectBean studySubjectBean = studySubjectDAO.findByOidAndStudy(oid, studyBean.getId());
                    if (studySubjectBean == null) {
                        mf.applyPattern(respage.getString("your_subject_oid_does_not_reference"));
                        Object[] arguments = { oid };
                        errors.add(mf.format(arguments));

                        // errors.add("Your Subject OID " + oid + " does not
                        // reference an existing Subject in the Study.");
                        logger.debug("logged an error with subject oid " + oid);
                    }

                    ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();
                    if (studyEventDataBeans != null) {
                        for (StudyEventDataBean studyEventDataBean : studyEventDataBeans) {
                            String sedOid = studyEventDataBean.getStudyEventOID();
                            StudyEventDefinitionBean studyEventDefintionBean = studyEventDefinitionDAO.findByOidAndStudy(sedOid, studyBean.getId(),
                                    studyBean.getParentStudyId());
                            if (studyEventDefintionBean == null) {
                                mf.applyPattern(respage.getString("your_study_event_oid_for_subject_oid"));
                                Object[] arguments = { sedOid, oid };
                                errors.add(mf.format(arguments));
                                // errors.add("Your Study Event OID " + sedOid +
                                // " for Subject OID " + oid
                                // + " does not reference an existing Study
                                // Event in the Study.");
                                logger.debug("logged an error with se oid " + sedOid + " and subject oid " + oid);
                            }

                            ArrayList<FormDataBean> formDataBeans = studyEventDataBean.getFormData();
                            if (formDataBeans != null) {
                                for (FormDataBean formDataBean : formDataBeans) {
                                    String formOid = formDataBean.getFormOID();
                                    ArrayList<CRFVersionBean> crfVersionBeans = crfVersionDAO.findAllByOid(formOid);
                                    // ideally we should look to compare
                                    // versions within
                                    // seds;
                                    // right now just check nulls
                                    if (crfVersionBeans != null && crfVersionBeans.size() > 0) {
                                        for (CRFVersionBean crfVersionBean : crfVersionBeans) {
                                            if (crfVersionBean == null) {
                                                mf.applyPattern(respage.getString("your_crf_version_oid_for_study_event_oid"));
                                                Object[] arguments = { formOid, sedOid };
                                                errors.add(mf.format(arguments));

                                                // errors.add("Your CRF Version
                                                // OID " + formOid + " for Study
                                                // Event OID " + sedOid
                                                // + " does not reference a
                                                // proper CRF Version in that
                                                // Study Event.");
                                                logger.debug("logged an error with form " + formOid + " and se oid " + sedOid);
                                            }
                                        }
                                    } else {
                                        mf.applyPattern(respage.getString("your_crf_version_oid_did_not_generate"));
                                        Object[] arguments = { formOid };
                                        errors.add(mf.format(arguments));

                                        // errors.add("Your CRF Version OID " +
                                        // formOid
                                        // + " did not generate any results in
                                        // the database. Please check it and try
                                        // again.");
                                    }

                                    ArrayList<ImportItemGroupDataBean> itemGroupDataBeans = formDataBean.getItemGroupData();
                                    if (itemGroupDataBeans != null) {
                                        for (ImportItemGroupDataBean itemGroupDataBean : itemGroupDataBeans) {
                                            String itemGroupOID = itemGroupDataBean.getItemGroupOID();
                                            List<ItemGroupBean> itemGroupBeans = itemGroupDAO.findAllByOid(itemGroupOID);
                                            if (itemGroupBeans != null) {
                                                logger.debug("number of item group beans: " + itemGroupBeans.size());
                                                logger.debug("item group oid: " + itemGroupOID);
                                                for (ItemGroupBean itemGroupBean : itemGroupBeans) {
                                                    if (itemGroupBean == null) {
                                                        mf.applyPattern(respage.getString("your_item_group_oid_for_form_oid"));
                                                        Object[] arguments = { itemGroupOID, formOid };
                                                        errors.add(mf.format(arguments));

                                                        // errors.add("Your Item
                                                        // Group OID " +
                                                        // itemGroupOID + " for
                                                        // Form OID " + formOid
                                                        // + " does not
                                                        // reference a proper
                                                        // Item Group in that
                                                        // CRF Version.");
                                                    }
                                                }
                                            } else {
                                                mf.applyPattern(respage.getString("the_item_group_oid_did_not"));
                                                Object[] arguments = { itemGroupOID };
                                                errors.add(mf.format(arguments));

                                                // errors.add("The Item Group
                                                // OID " + itemGroupOID
                                                // + " did not generate any
                                                // results in the database,
                                                // please check it and try
                                                // again.");
                                            }

                                            ArrayList<ImportItemDataBean> itemDataBeans = itemGroupDataBean.getItemData();
                                            if (itemDataBeans != null) {
                                                for (ImportItemDataBean itemDataBean : itemDataBeans) {
                                                    String itemOID = itemDataBean.getItemOID();
                                                    List<ItemBean> itemBeans = itemDAO.findByOid(itemOID);
                                                    if (itemBeans != null) {

                                                        logger.debug("found itembeans: ");

                                                        for (ItemBean itemBean : itemBeans) {

                                                            if (itemBean == null) {
                                                                mf.applyPattern(respage.getString("your_item_oid_for_item_group_oid"));
                                                                Object[] arguments = { itemOID, itemGroupOID };
                                                                errors.add(mf.format(arguments));

                                                                // errors.add(
                                                                // "Your
                                                                // Item OID " +
                                                                // itemOID + "
                                                                // for Item
                                                                // Group OID " +
                                                                // itemGroupOID
                                                                // + " does not
                                                                // reference a
                                                                // proper Item
                                                                // in the Item
                                                                // Group.");

                                                            } else {
                                                                logger.debug("found " + itemBean.getOid() + ", passing");

                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                mf.applyPattern(respage.getString("the_item_group_oid_did_not_contain_item_data"));
                                                Object[] arguments = { itemGroupOID };
                                                errors.add(mf.format(arguments));

                                                // errors.add("The Item Group
                                                // OID " + itemGroupOID
                                                // + " did not contain any Item
                                                // Data in the XML file, please
                                                // check it and try again.");
                                            }
                                        }
                                    } else {
                                        mf.applyPattern(respage.getString("your_study_event_contains_no_form_data"));
                                        Object[] arguments = { sedOid };
                                        errors.add(mf.format(arguments));

                                        // errors.add("Your Study Event " +
                                        // sedOid
                                        // + " contains no Form Data, or the
                                        // Form OIDs are incorrect. Please check
                                        // it and try again.");
                                    }
                                }

                            }
                        }
                    }
                }
            }
        } catch (OpenClinicaException oce) {

        } catch (NullPointerException npe) {
            logger.debug("found a nullpointer here");
        }
        // if errors == null you pass, if not you fail
        return errors;
    }

    private ItemDataDAO getItemDataDao() {
        itemDataDao = this.itemDataDao != null ? itemDataDao : new ItemDataDAO(ds);
        return itemDataDao;
    }

}