package core.org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.admin.CRFBean;
import core.org.akaza.openclinica.bean.core.DataEntryStage;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.*;
import core.org.akaza.openclinica.bean.service.StudyParameterValueBean;
import core.org.akaza.openclinica.bean.submit.EventCRFBean;
import core.org.akaza.openclinica.bean.submit.FormLayoutBean;
import core.org.akaza.openclinica.dao.admin.CRFDAO;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.*;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import core.org.akaza.openclinica.dao.submit.EventCRFDAO;
import core.org.akaza.openclinica.dao.submit.FormLayoutDAO;
import core.org.akaza.openclinica.dao.submit.ItemDataDAO;
import core.org.akaza.openclinica.domain.Status;
import core.org.akaza.openclinica.domain.datamap.*;
import core.org.akaza.openclinica.domain.xform.XformParserHelper;
import core.org.akaza.openclinica.domain.xform.dto.Bind;
import core.org.akaza.openclinica.ocobserver.StudyEventChangeDetails;
import core.org.akaza.openclinica.ocobserver.StudyEventContainer;
import core.org.akaza.openclinica.service.crfdata.xform.EnketoAPI;
import core.org.akaza.openclinica.service.randomize.ModuleProcessor;
import core.org.akaza.openclinica.service.randomize.RandomizationService;
import core.org.akaza.openclinica.web.pform.OpenRosaServices;
import core.org.akaza.openclinica.web.pform.PFormCache;
import org.akaza.openclinica.domain.enumsupport.EventCrfWorkflowStatusEnum;
import org.akaza.openclinica.domain.enumsupport.StudyEventWorkflowStatusEnum;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.cdisc.ns.odm.v130.*;
import org.openclinica.ns.odm_ext_v130.v31.OCodmComplexTypeDefinitionLink;
import org.openclinica.ns.odm_ext_v130.v31.OCodmComplexTypeDefinitionLinks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletContext;
import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.*;

/**
 * This Service class is used with View Study Subject Page
 *
 * @author joekeremian
 */

@Service("participateService")
public class ParticipateServiceImpl implements ParticipateService {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @Autowired
    private ServletContext context;

    @Autowired
    private EventCrfDao eventCrfDao;

    @Autowired
    private StudyEventDao studyEventDao;

    @Autowired
    private StudySubjectDao studySubjectDao;

    @Autowired
    private StudyDao studyDao;

    @Autowired
    private OpenRosaServices openRosaServices;

    @Autowired
    private FormLayoutDao formLayoutDao;

    @Autowired
    private RandomizationService randomizationService;

    @Autowired
    private StudyBuildService studyBuildService;
    @Autowired
    @Qualifier("eventCRFJDBCDao")
    private EventCRFDAO eventCrfDAO;

    @Autowired
    private XformParserHelper xformParserHelper;
    public static final String FORM_CONTEXT = "ecid";
    public static final String DASH = "-";
    public static final String PARTICIPATE_EDIT = "participate-edit";
    public static final String PARTICIPATE_ADD_NEW = "participate-add-new";
    public static final String PARTICIPATE_FLAVOR = "-participate";



    /**
     * @api {get} /pages/odmk/studies/:studyOid/metadata Retrieve metadata
     * @apiName getStudyMetadata
     * @apiPermission admin
     * @apiVersion 3.8.0
     * @apiParam {String} studyOid Study Oid.
     * @apiGroup Study
     * @apiDescription Retrieve the metadata of the specified study
     * @apiParamExample {json} Request-Example:
     *                  {
     *                  "studyOid": "S_BL101",
     *                  }
     * @apiSuccessExample {json} Success-Response:
     *                    HTTP/1.1 200 OK
     *                    {
     *                    The whole Study Metadata
     *                    }
     */




    private StudyEventDefinitionBean getStudyEventDefinitionBean(int ID) {
        StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(dataSource);
        StudyEventDefinitionBean studyEventDefinitionBean = (StudyEventDefinitionBean) seddao.findByPK(ID);
        return studyEventDefinitionBean;
    }

    public ODM createOdm(Study study, StudySubjectBean studySubjectBean, StudyEventBean nextEvent, List<ODMcomplexTypeDefinitionFormData> formDatas) {
        ODM odm = new ODM();

        ODMcomplexTypeDefinitionClinicalData clinicalData = generateClinicalData(study);
        ODMcomplexTypeDefinitionSubjectData subjectData = generateSubjectData(studySubjectBean);
        ODMcomplexTypeDefinitionStudyEventData studyEventData = generateStudyEventData(nextEvent);
        // Create the object graph
        studyEventData.getFormData().addAll(formDatas);
        subjectData.getStudyEventData().add(studyEventData);
        clinicalData.getSubjectData().add(subjectData);
        odm.getClinicalData().add(clinicalData);

        return odm;
    }

    private String createEnketoUrl(String studyOID, FormLayoutBean formLayout, StudyEventBean nextEvent, String ssoid ,String userAccountID) throws Exception {
        PFormCache cache = PFormCache.getInstance(context);

        StudyEvent studyEvent = studyEventDao.findById(nextEvent.getId());
        String contextHash = cache.putSubjectContext(ssoid, String.valueOf(nextEvent.getStudyEventDefinitionId()), String.valueOf(nextEvent.getSampleOrdinal()),
                formLayout.getOid(),userAccountID, String.valueOf(nextEvent.getId()), studyOID, EnketoAPI.PARTICIPATE_MODE);

        String crfOID= formLayout.getOid()+DASH+formLayout.getXform()+PARTICIPATE_FLAVOR;

        String enketoURL = cache.getPFormURL(studyOID, crfOID, studyEvent,false,contextHash);

        String url = enketoURL + "?" + FORM_CONTEXT + "=" + contextHash;
        logger.debug("Enketo URL for " + formLayout.getName() + "= " + url);
        return url;

    }

    public ODM getODM(String studyOID, String subjectKey,UserAccountBean ub) {
        String ssoid = subjectKey;
        if (ssoid == null) {
            return null;
        }

        FormLayoutDAO formLayoutDAO = new FormLayoutDAO(dataSource);
        StudySubjectDAO studySubjectDAO = new StudySubjectDAO(dataSource);
        ItemDataDAO itemDataDAO = new ItemDataDAO(dataSource);
        CRFDAO crfDAO = new CRFDAO(dataSource);
        List<ODMcomplexTypeDefinitionFormData> formDatas = new ArrayList<>();
        try {
            // Retrieve crfs for next event
            StudySubjectBean studySubjectBean = studySubjectDAO.findByOid(ssoid);
            ParticipantEventService participantEventService = new ParticipantEventService(dataSource, studyDao);
            StudyEventBean nextEvent = participantEventService.getNextParticipantEvent(studySubjectBean);
            if (nextEvent != null) {
                logger.debug("Found event: " + nextEvent.getName() + " - ID: " + nextEvent.getId());

                List<EventCRFBean> eventCrfs = eventCrfDAO.findAllByStudyEvent(nextEvent);
                Study study = studyDao.findByOcOID(studyOID);


                List<EventDefinitionCRFBean> eventDefCrfs = participantEventService.getEventDefCrfsForStudyEvent(studySubjectBean, nextEvent);
                for (EventDefinitionCRFBean eventDefCrf : eventDefCrfs) {
                    if (eventDefCrf.isParticipantForm()) {
                        EventCRFBean eventCRF = participantEventService.getExistingEventCRF(studySubjectBean, nextEvent, eventDefCrf);
                        boolean itemDataExists = false;
                        boolean validStatus = true;
                        FormLayoutBean formLayout = null;
                        if (eventCRF != null) {
                            if (eventCRF.isRemoved() || eventCRF.isArchived())
                                validStatus = false;
                            if (itemDataDAO.findAllByEventCRFId(eventCRF.getId()).size() > 0)
                                itemDataExists = true;
                            formLayout = (FormLayoutBean) formLayoutDAO.findByPK(eventCRF.getFormLayoutId());
                        } else
                            formLayout = (FormLayoutBean) formLayoutDAO.findByPK(eventDefCrf.getDefaultVersionId());

                        if (validStatus) {
                            String formUrl = null;
                            FormLayout fl = formLayoutDao.findById(formLayout.getId());
                            List<Bind> binds = xformParserHelper.getBinds(fl,PARTICIPATE_FLAVOR,studyOID);
                            if (!formLayout.getStatus().getName().equals("Removed")) {
                                if (!itemDataExists && !openRosaServices.isFormContainsContactData(binds)) {
                                    formUrl = createEnketoUrl(studyOID, formLayout, nextEvent, ssoid, String.valueOf(ub.getId()));
                                }else {
                                    formUrl = createEditUrl(studyOID, formLayout, nextEvent, ssoid, String.valueOf(ub.getId()));
                                }
                                formDatas.add(getFormDataPerCrf(formLayout, nextEvent, eventCrfs, crfDAO, formUrl, itemDataExists));
                            }
                        }
                    }
                }
                return createOdm(study, studySubjectBean, nextEvent, formDatas);
            } else {
                logger.debug("Unable to find next event for subject.");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        }

        return null;

    }

    private String createEditUrl(String studyOID, FormLayoutBean formLayout, StudyEventBean nextEvent, String ssoid, String userAccountID) throws Exception {
        PFormCache cache = PFormCache.getInstance(context);
        String contextHash = cache.putSubjectContext(ssoid, String.valueOf(nextEvent.getStudyEventDefinitionId()), String.valueOf(nextEvent.getSampleOrdinal()),
                formLayout.getOid(),userAccountID ,String.valueOf(nextEvent.getId()), studyOID, EnketoAPI.PARTICIPATE_MODE);
        String editURL = CoreResources.getField("sysURL.base") + "pages/auth/api/editform/" + studyOID + "/url";

        String url = editURL + "?" + FORM_CONTEXT + "=" + contextHash;
        logger.debug("Edit URL for " + formLayout.getName() + "= " + url);
        return url;

    }

    private ODMcomplexTypeDefinitionFormData getFormDataPerCrf(FormLayoutBean formLayout, StudyEventBean nextEvent, List<EventCRFBean> eventCrfs, CRFDAO crfDAO,
                                                               String formUrl, boolean itemDataExists) {
        EventCRFBean selectedEventCRFBean = null;
        CRFBean crfBean = (CRFBean) crfDAO.findByLayoutId(formLayout.getId());
        for (EventCRFBean eventCRFBean : eventCrfs) {
            if (eventCRFBean.getFormLayoutId() == formLayout.getId()) {
                selectedEventCRFBean = eventCRFBean;
                break;
            }
        }
        return generateFormData(formLayout, nextEvent, selectedEventCRFBean, crfBean, formUrl, itemDataExists);

    }

    private ODMcomplexTypeDefinitionClinicalData generateClinicalData(Study study) {
        ODMcomplexTypeDefinitionClinicalData clinicalData = new ODMcomplexTypeDefinitionClinicalData();
        clinicalData.setStudyName(getParentStudy(study.getOc_oid()).getName());
        clinicalData.setStudyOID(study.getOc_oid());
        return clinicalData;
    }

    private ODMcomplexTypeDefinitionSubjectData generateSubjectData(StudySubjectBean studySubject) {
        ODMcomplexTypeDefinitionSubjectData subjectData = new ODMcomplexTypeDefinitionSubjectData();
        subjectData.setSubjectKey(studySubject.getOid());
        subjectData.setStudySubjectID(studySubject.getLabel());
        subjectData.setStatus(studySubject.getStatus().getName());
        return subjectData;
    }

    private ODMcomplexTypeDefinitionStudyEventData generateStudyEventData(StudyEventBean studyEvent) {
        ODMcomplexTypeDefinitionStudyEventData studyEventData = new ODMcomplexTypeDefinitionStudyEventData();
        if (studyEvent.getDateStarted() != null)
            studyEventData.setStartDate(studyEvent.getDateStarted().toString());
        StudyEventDefinitionBean studyEventDefBean = getStudyEventDefinitionBean(studyEvent.getStudyEventDefinitionId());
        studyEventData.setEventName(studyEventDefBean.getName());
        studyEventData.setStudyEventOID(studyEventDefBean.getOid());
        studyEventData.setStudyEventRepeatKey(String.valueOf(studyEvent.getSampleOrdinal()));
        studyEventData.setWorkflowStatus(studyEvent.getWorkflowStatus().getDisplayValue());
        return studyEventData;
    }

    private ODMcomplexTypeDefinitionFormData generateFormData(FormLayoutBean formLayout, StudyEventBean nextEvent, EventCRFBean eventCRFBean, CRFBean crfBean,
                                                              String formUrl, boolean itemDataExists) {
        ODMcomplexTypeDefinitionFormData formData = new ODMcomplexTypeDefinitionFormData();
        formData.setFormOID(formLayout.getOid());
        formData.setFormName(crfBean.getName());
        formData.setVersionDescription(formLayout.getDescription());
        OCodmComplexTypeDefinitionLinks odmLinks = new OCodmComplexTypeDefinitionLinks();

        OCodmComplexTypeDefinitionLink link = new OCodmComplexTypeDefinitionLink();

        FormLayout fl = formLayoutDao.findById(formLayout.getId());
        Study study = studyDao.findById(crfBean.getStudyId());
        List<Bind> binds=null;
        try {
             binds = xformParserHelper.getBinds(fl, PARTICIPATE_FLAVOR, study.getOc_oid());
        }catch(Exception e){
            logger.debug(e.getMessage());
        }

        if (!itemDataExists && !openRosaServices.isFormContainsContactData(binds)) {
            link.setRel(PARTICIPATE_ADD_NEW);
        }else{
            link.setRel(PARTICIPATE_EDIT);
        }
        link.setHref(formUrl);
        odmLinks.getLink().add(link);
        formData.getFormDataElementExtension().add(odmLinks);

        if (eventCRFBean == null) {
            formData.setWorkflowStatus(EventCrfWorkflowStatusEnum.NOT_STARTED.getDisplayValue());
        } else {
            EventCrf eventCrf = eventCrfDao.findById(eventCRFBean.getId());
            if (!itemDataExists && !openRosaServices.isFormContainsContactData(binds)) {
                formData.setWorkflowStatus(EventCrfWorkflowStatusEnum.INITIAL_DATA_ENTRY.getDisplayValue());
              //  formData.setStatus("Not Started");
            } else {
                formData.setWorkflowStatus(eventCRFBean.getWorkflowStatus().getDisplayValue());
            }

            if (eventCrf.getDateUpdated() != null) {
                // returns time as UTC
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                formData.setStatusChangeTimeStamp(sdf.format(eventCrf.getDateUpdated()));
            }
        }
        return formData;
    }

    /**
     * Currently not used, but keep here for future unit test
     *
     * @param clazz
     * @param odm
     * @return
     * @throws Exception
     */
    private String generateXmlFromObj(Class clazz, ODM odm) throws Exception {

        JAXBContext context = JAXBContext.newInstance(clazz);

        Marshaller m = context.createMarshaller();
        StringWriter w = new StringWriter();

        m.marshal(odm, w);
        return w.toString();
    }

    @SuppressWarnings("unchecked")
    private void sortList(ArrayList<EventDefinitionCRFBean> edcBeans) {

        Collections.sort(edcBeans, new Comparator() {

            public int compare(Object o1, Object o2) {

                Integer x1 = ((EventDefinitionCRFBean) o1).getOrdinal();
                Integer x2 = ((EventDefinitionCRFBean) o2).getOrdinal();
                int sComp = x1.compareTo(x2);

                return sComp;
            }
        });
    }

    public Study getStudyById(int id) {
        Study studyBean = (Study) studyDao.findByPK(id);
        return studyBean;
    }

    public Study getStudy(String oid) {
        Study studyBean = (Study) studyDao.findByOcOID(oid);
        return studyBean;
    }

    public Study getParentStudy(String studyOid) {
        Study study = getStudy(studyOid);
        if (!study.isSite()) {
            return study;
        } else {
            Study parentStudy = study.getStudy();
            return parentStudy;
        }

    }




    public boolean mayProceed(String studyOid) throws Exception {
        boolean accessPermission = false;
        Study study = getStudy(studyOid);
        Study pStudy = getParentStudy(studyOid);

        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(dataSource);
        StudyParameterValueBean pStatus = spvdao.findByHandleAndStudy(pStudy.getStudyId(), "participantPortal");
        String participateStatus = pStatus.getValue().toString();

        if( participateStatus.equals(ModuleProcessor.ModuleStatus.ENABLED.getValue()) && study.getStatus().isAvailable()){
            accessPermission = true;
        }
        return accessPermission;
    }


    public ODM getOdmHeader(ODM odm , Study currentStudy , StudySubjectBean studySubject){
            odm = new ODM();
            ODMcomplexTypeDefinitionClinicalData clinicalData = generateClinicalData(currentStudy);
            if(studySubject!=null && studySubject.isActive()) {
                ODMcomplexTypeDefinitionSubjectData subjectData = generateSubjectData(studySubject);
                clinicalData.getSubjectData().add(subjectData);
            }
            odm.getClinicalData().add(clinicalData);
        return odm;
    }

    @Transactional
    public void completeData(StudyEvent studyEvent, List<EventDefinitionCrf> eventDefCrfs, List<EventCrf> eventCrfs
            , String accessToken, String studyOid, String subjectOid, UserAccountBean ub) throws Exception{
        boolean completeStudyEvent = true;
        Study parentPublicStudy = studyBuildService.getParentPublicStudy(studyOid);
        // Loop thru event CRFs and complete all that are participant events.
        for (EventDefinitionCrf eventDefCrf:eventDefCrfs) {
            boolean foundEventCrfMatch = false;
            for (EventCrf eventCrf:eventCrfs) {
                if (eventDefCrf.getCrf().getCrfId() == eventCrf.getFormLayout().getCrf().getCrfId()) {
                    foundEventCrfMatch = true;
                    if (eventDefCrf.getParicipantForm()) {
                         eventCrf.setWorkflowStatus(EventCrfWorkflowStatusEnum.COMPLETED);
                        eventCrf.setDateCompleted(new Date());
                        eventCrfDao.saveOrUpdate(eventCrf);
                        randomizationService.processRandomization(parentPublicStudy, accessToken, subjectOid);
                    } else if (!eventCrf.getWorkflowStatus().equals(EventCrfWorkflowStatusEnum.COMPLETED)) completeStudyEvent = false;
                }
            }
            if (!foundEventCrfMatch && !eventDefCrf.getParicipantForm()) completeStudyEvent = false;
        }

        // Complete study event only if there are no uncompleted, non-participant forms.
        boolean statusChanged=false;
        if (completeStudyEvent) {

            if (!studyEvent.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.COMPLETED)) {
                studyEvent.setWorkflowStatus(StudyEventWorkflowStatusEnum.COMPLETED);
                if(studyEvent.isCurrentlySigned())
                    studyEvent.setSigned(Boolean.FALSE);
                statusChanged = true;
            }
            StudyEventChangeDetails changeDetails = new StudyEventChangeDetails(statusChanged,false);
            StudyEventContainer container = new StudyEventContainer(studyEvent,changeDetails);
            studyEventDao.saveOrUpdateTransactional(container);
            StudySubject studySubject = studyEvent.getStudySubject();
            if (statusChanged && studySubject.getStatus().isSigned())
            {
                studySubject.setStatus(Status.AVAILABLE);
                studySubject.setUpdateId(ub.getId());
                studySubject.setDateUpdated(new Date());
                studySubjectDao.saveOrUpdate(studySubject);
            }
        }


    }

    @Override
    public void processModule(Study study, String isModuleEnabled, String accessToken) {
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(dataSource);
        StudyParameterValueBean spv = spvdao.findByHandleAndStudy(study.getStudyId(), "participantPortal");
        String statusValue = isModuleEnabled;
        if (!spv.isActive()) {
            spv = new StudyParameterValueBean();
            spv.setStudyId(study.getStudyId());
            spv.setParameter("participantPortal");
            spv.setValue(statusValue);
            spvdao.create(spv);
        } else if (spv.isActive() && !spv.getValue().equals(statusValue)) {
            spv.setValue(statusValue);
            spvdao.update(spv);
        }
    }
}
