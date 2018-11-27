package org.akaza.openclinica.service;

import net.sf.json.JSON;
import net.sf.json.xml.XMLSerializer;
import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.*;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.FormLayoutBean;
import org.akaza.openclinica.controller.RuleController;
import org.akaza.openclinica.controller.dto.CommonEventContainerDTO;
import org.akaza.openclinica.controller.dto.ViewStudySubjectDTO;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.*;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.FormLayoutDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.*;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.patterns.ocobserver.StudyEventChangeDetails;
import org.akaza.openclinica.patterns.ocobserver.StudyEventContainer;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.akaza.openclinica.web.pform.PFormCache;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang.SerializationUtils;
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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Comparator;

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
    private BasicDataSource dataSource;

    @Autowired
    ServletContext context;

    @Autowired
    EventCrfDao eventCrfDao;

    @Autowired
    StudyEventDao studyEventDao;

    @Autowired
    StudySubjectDao studySubjectDao;

    @Autowired
    StudyDao studyDao;


    public static final String FORM_CONTEXT = "ecid";
    public static final String DASH = "-";
    public static final String PARTICIPATE_EDIT = "participate-edit";
    public static final String PARTICIPATE_ADD_NEW = "participate-add-new";
    public static final String PARTICIPATE_FLAVOR = "-participate";


    StudyDAO sdao;

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

    public ODM createOdm(StudyBean study, StudySubjectBean studySubjectBean, StudyEventBean nextEvent, List<ODMcomplexTypeDefinitionFormData> formDatas) {
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
                formLayout.getOid(),userAccountID, String.valueOf(nextEvent.getId()), studyOID, PFormCache.PARTICIPATE_MODE);

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
        StudyDAO studyDAO = new StudyDAO(dataSource);
        StudySubjectDAO studySubjectDAO = new StudySubjectDAO(dataSource);
        EventCRFDAO eventCRFDAO = new EventCRFDAO(dataSource);
        ItemDataDAO itemDataDAO = new ItemDataDAO(dataSource);
        CRFDAO crfDAO = new CRFDAO(dataSource);
        List<ODMcomplexTypeDefinitionFormData> formDatas = new ArrayList<>();
        try {
            // Retrieve crfs for next event
            StudySubjectBean studySubjectBean = studySubjectDAO.findByOid(ssoid);
            ParticipantEventService participantEventService = new ParticipantEventService(dataSource);
            StudyEventBean nextEvent = participantEventService.getNextParticipantEvent(studySubjectBean);
            if (nextEvent != null) {
                logger.debug("Found event: " + nextEvent.getName() + " - ID: " + nextEvent.getId());

                List<EventCRFBean> eventCrfs = eventCRFDAO.findAllByStudyEvent(nextEvent);
                StudyBean study = studyDAO.findByOid(studyOID);


                List<EventDefinitionCRFBean> eventDefCrfs = participantEventService.getEventDefCrfsForStudyEvent(studySubjectBean, nextEvent);
                for (EventDefinitionCRFBean eventDefCrf : eventDefCrfs) {
                    if (eventDefCrf.isParticipantForm()) {
                        EventCRFBean eventCRF = participantEventService.getExistingEventCRF(studySubjectBean, nextEvent, eventDefCrf);
                        boolean itemDataExists = false;
                        boolean validStatus = true;
                        FormLayoutBean formLayout = null;
                        if (eventCRF != null) {
                            if (eventCRF.getStatus().getId() != 1 && eventCRF.getStatus().getId() != 2)
                                validStatus = false;
                            if (itemDataDAO.findAllByEventCRFId(eventCRF.getId()).size() > 0)
                                itemDataExists = true;
                            formLayout = (FormLayoutBean) formLayoutDAO.findByPK(eventCRF.getFormLayoutId());
                        } else
                            formLayout = (FormLayoutBean) formLayoutDAO.findByPK(eventDefCrf.getDefaultVersionId());

                        if (validStatus) {
                            String formUrl = null;
                            if (!itemDataExists) {
                                formUrl = createEnketoUrl(studyOID, formLayout, nextEvent, ssoid, String.valueOf(ub.getId()));
                            }else {
                                formUrl = createEditUrl(studyOID, formLayout, nextEvent, ssoid, String.valueOf(ub.getId()));
                            }
                            formDatas.add(getFormDataPerCrf(formLayout, nextEvent, eventCrfs, crfDAO, formUrl, itemDataExists));
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
                formLayout.getOid(),userAccountID ,String.valueOf(nextEvent.getId()), studyOID, PFormCache.PARTICIPATE_MODE);
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

    private ODMcomplexTypeDefinitionClinicalData generateClinicalData(StudyBean study) {
        ODMcomplexTypeDefinitionClinicalData clinicalData = new ODMcomplexTypeDefinitionClinicalData();
        clinicalData.setStudyName(study.getName());
        clinicalData.setStudyOID(study.getOid());
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
        if(!itemDataExists) {
            link.setRel(PARTICIPATE_ADD_NEW);
        }else{
            link.setRel(PARTICIPATE_EDIT);
        }
        link.setHref(formUrl);
        odmLinks.getLink().add(link);
        formData.getFormDataElementExtension().add(odmLinks);

        if (eventCRFBean == null) {
            formData.setStatus("Not Started");
        } else {
            EventCrf eventCrf = eventCrfDao.findById(eventCRFBean.getId());
            if (!itemDataExists) {
                formData.setStatus("Not Started");
            } else {
                org.akaza.openclinica.bean.core.Status status = org.akaza.openclinica.bean.core.Status.get(eventCrf.getStatusId());
                if (status.equals(org.akaza.openclinica.bean.core.Status.AVAILABLE)) {
                    formData.setStatus(DataEntryStage.INITIAL_DATA_ENTRY.getName());
                } else {
                    formData.setStatus(eventCRFBean.getStatus().getName());
                }
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

    public StudyBean getStudyById(int id) {
        sdao = new StudyDAO(dataSource);
        StudyBean studyBean = (StudyBean) sdao.findByPK(id);
        return studyBean;
    }

    public StudyBean getStudy(String oid) {
        sdao = new StudyDAO(dataSource);
        StudyBean studyBean = (StudyBean) sdao.findByOid(oid);
        return studyBean;
    }

    public StudyBean getParentStudy(String studyOid) {
        StudyBean study = getStudy(studyOid);
        if (study.getParentStudyId() == 0) {
            return study;
        } else {
            StudyBean parentStudy = (StudyBean) sdao.findByPK(study.getParentStudyId());
            return parentStudy;
        }

    }




    public boolean mayProceed(String studyOid) throws Exception {
        boolean accessPermission = false;
        StudyBean study = getStudy(studyOid);
        StudyBean pStudy = getParentStudy(studyOid);

        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(dataSource);
        StudyParameterValueBean pStatus = spvdao.findByHandleAndStudy(pStudy.getId(), "participantPortal");
        String participateStatus = pStatus.getValue().toString();

        if( participateStatus.equalsIgnoreCase("enabled") && study.getStatus().isAvailable()){
            accessPermission = true;
        }
        return accessPermission;
    }


    public ODM getOdmHeader(ODM odm , StudyBean currentStudy , StudySubjectBean studySubject){
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
    public void completeData(StudyEvent studyEvent, List<EventDefinitionCrf> eventDefCrfs, List<EventCrf> eventCrfs) throws Exception{
        boolean completeStudyEvent = true;

        // Loop thru event CRFs and complete all that are participant events.
        for (EventDefinitionCrf eventDefCrf:eventDefCrfs) {
            boolean foundEventCrfMatch = false;
            for (EventCrf eventCrf:eventCrfs) {
                if (eventDefCrf.getCrf().getCrfId() == eventCrf.getFormLayout().getCrf().getCrfId()) {
                    foundEventCrfMatch = true;
                    if (eventDefCrf.getParicipantForm()) {
                        eventCrf.setStatusId(Status.UNAVAILABLE.getCode());
                        eventCrfDao.saveOrUpdate(eventCrf);
                    } else if (eventCrf.getStatusId() != Status.UNAVAILABLE.getCode()) completeStudyEvent = false;
                }
            }
            if (!foundEventCrfMatch && !eventDefCrf.getParicipantForm()) completeStudyEvent = false;
        }

        // Complete study event only if there are no uncompleted, non-participant forms.
        if (completeStudyEvent) {
            studyEvent.setSubjectEventStatusId(4);
            StudyEventChangeDetails changeDetails = new StudyEventChangeDetails(true,false);
            StudyEventContainer container = new StudyEventContainer(studyEvent,changeDetails);
            studyEventDao.saveOrUpdateTransactional(container);
        }


    }

}
