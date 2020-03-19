package org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.dao.hibernate.*;
import core.org.akaza.openclinica.domain.Status;
import core.org.akaza.openclinica.domain.datamap.*;
import core.org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.controller.dto.CommonEventContainerDTO;
import org.akaza.openclinica.controller.dto.ViewStudySubjectDTO;
import org.akaza.openclinica.domain.enumsupport.StudyEventWorkflowStatusEnum;
import org.apache.commons.lang.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;

/**
 * This Service class is used with View Study Subject Page
 *
 * @author joekeremian
 */


@Service("viewStudySubjectService")
public class ViewStudySubjectServiceImpl implements ViewStudySubjectService {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private StudyDao studyDao;
    private UserAccountDao userAccountDao;
    private StudySubjectDao studySubjectDao;
    private CrfDao crfDao;
    private EventDefinitionCrfDao eventDefinitionCrfDao;
    private StudyEventDao studyEventDao;
    private EventCrfDao eventCrfDao;
    private StudyEventDefinitionDao studyEventDefintionDao;
    private PageLayoutDao pageLayoutDao;
    private static final String CHECKBOX = "checkbox";
    private static final String MULTI_SELECT = "multi-select";
    private static final String RADIO = "radio";
    private static final String SINGLE_SELECT = "single-select";

    private ItemDataDao itemDataDao;
    private ItemDao itemDao;
    private ItemFormMetadataDao itemFormMetadataDao;
    private CrfVersionDao crfVersionDao;



    public ViewStudySubjectServiceImpl(StudyDao studyDao, UserAccountDao userAccountDao, StudySubjectDao studySubjectDao, CrfDao crfDao,
                                       EventDefinitionCrfDao eventDefinitionCrfDao, StudyEventDao studyEventDao, EventCrfDao eventCrfDao, StudyEventDefinitionDao studyEventDefintionDao,
                                       PageLayoutDao pageLayoutDao) {
        super();
        this.studyDao = studyDao;
        this.userAccountDao = userAccountDao;
        this.studySubjectDao = studySubjectDao;
        this.crfDao = crfDao;
        this.eventDefinitionCrfDao = eventDefinitionCrfDao;
        this.studyEventDao = studyEventDao;
        this.eventCrfDao = eventCrfDao;
        this.studyEventDefintionDao = studyEventDefintionDao;
        this.pageLayoutDao = pageLayoutDao;
    }

    public ViewStudySubjectDTO addNewForm(HttpServletRequest request, String studyOid, String studyEventDefinitionOid, String crfOid, String studySubjectOid) {
        final String COMMON = "common";

        request.setAttribute("requestSchema", "public");
        HttpSession session = request.getSession();

        Study publicStudy = studyDao.findByOcOID(studyOid);

        UserAccountBean ub = (UserAccountBean) session.getAttribute("userBean");
        if (ub == null) {
            logger.error("userAccount  is null");
            return null;
        }
        UserAccount userAccount = userAccountDao.findById(ub.getId());
        if (userAccount == null) {
            logger.error("userAccount  is null");
            return null;
        }

        request.setAttribute("requestSchema", publicStudy.getSchemaName());
        //Study study = studyDao.findByOcOID(studyOid);

        CommonEventContainerDTO commonEventContainerDTO =
                addCommonForm(studyEventDefinitionOid,crfOid,studySubjectOid,userAccount,studyOid);

        StudyEvent studyEvent = null;
        if (commonEventContainerDTO.getEventCrfId() == 0) {
            // schedule new Study Event
            studyEvent = scheduleNewStudyEvent(commonEventContainerDTO.getStudySubject(),
                    commonEventContainerDTO.getStudyEventDefinition(),
                    commonEventContainerDTO.getMaxOrdinal(),
                    commonEventContainerDTO.getUserAccount());
        } else {
            // use existing study Event
            studyEvent = commonEventContainerDTO.getEventCrf().getStudyEvent();
        }


        String url = "/EnketoFormServlet?formLayoutId=" + commonEventContainerDTO.getFormLayout().getFormLayoutId()
                + "&studyEventId=" + studyEvent.getStudyEventId()
                + "&eventCrfId=" + commonEventContainerDTO.getEventCrfId()
                + "&originatingPage=ViewStudySubject%3Fid%3D" + commonEventContainerDTO.getStudySubject().getStudySubjectId()
                + "&mode=edit";

        ViewStudySubjectDTO viewStudySubjectDTO = new ViewStudySubjectDTO();
        viewStudySubjectDTO.setUrl(url);
        return viewStudySubjectDTO;
    }



    public CommonEventContainerDTO addCommonForm(String studyEventDefinitionOid, String crfOid, String studySubjectOid,
                                                 UserAccount userAccount, String studyOid) {

        final String COMMON = "common";


        Study study = studyDao.findByOcOID(studyOid);
        if (study == null) {
            logger.error("Study  is null");
            return null;
        } else if (study.getStudy() == null) {
            logger.debug("the study with Oid {} is a Parent study", study.getOc_oid());
        } else {
            logger.debug("the study with Oid {} is a Site study", study.getOc_oid());
        }

        StudySubject studySubject = studySubjectDao.findByOcOID(studySubjectOid);
        if (studySubject == null) {
            logger.error("StudySubject is null");
            return null;
        }
        StudyEventDefinition studyEventDefinition = studyEventDefintionDao.findByOcOID(studyEventDefinitionOid);
        if (studyEventDefinition == null) {
            logger.error("StudyEventDefinition is null");
            return null;
        } else if (!studyEventDefinition.getType().equals(COMMON)) {
            logger.error("StudyEventDefinition with Oid {} is not a Common Type Event", studyEventDefinition.getOc_oid());
            return null;
        }
        CrfBean crf = crfDao.findByOcOID(crfOid);
        if (crf == null) {
            logger.error("Crf is null");
            return null;
        }

        EventDefinitionCrf edc = eventDefinitionCrfDao.findByStudyEventDefinitionIdAndCRFIdAndStudyId(studyEventDefinition.getStudyEventDefinitionId(),
                crf.getCrfId(), study.getStudyId());
        if (edc == null) {
            edc = eventDefinitionCrfDao.findByStudyEventDefinitionIdAndCRFIdAndStudyId(studyEventDefinition.getStudyEventDefinitionId(), crf.getCrfId(),
                    study.checkAndGetParentStudyId());
        }
        if (edc == null || edc.getStatusId().equals(Status.DELETED.getCode()) || edc.getStatusId().equals(Status.AUTO_DELETED.getCode())) {
            logger.error("EventDefinitionCrf for StudyEventDefinition Oid {},Crf Oid {} and Study Oid {}is null or has Removed Status",
                    studyEventDefinition.getOc_oid(), crf.getOcOid(), study.getOc_oid());
            return null;
        }
        FormLayout formLayout = edc.getFormLayout();
        if (formLayout == null) {
            logger.error("FormLayout is null");
            return null;
        }

        List<StudyEvent> studyEvents = studyEventDao.fetchListByStudyEventDefOID(studyEventDefinitionOid, studySubject.getStudySubjectId());
        Integer maxOrdinal;
        StudyEvent studyEvent;
        int eventCrfId = 0;
        EventCrf eventCrf = null;
        if (studyEvents.size() == 0) {
            logger.debug("No previous study event found for this studyEventDef Oid {} and subject Oid{}", studyEventDefinition.getOc_oid(),
                    studySubject.getOcOid());
            maxOrdinal = 0;
        } else {
            maxOrdinal = studyEventDao.findMaxOrdinalByStudySubjectStudyEventDefinition(studySubject.getStudySubjectId(),
                    studyEventDefinition.getStudyEventDefinitionId());
        }

        if (!studyEventDefinition.getRepeating()) {
            logger.debug("StudyEventDefinition with Oid {} is Non Repeating", studyEventDefinition.getOc_oid());
            for (StudyEvent stEvent : studyEvents) {
                eventCrf = eventCrfDao.findByStudyEventIdStudySubjectIdFormLayoutId(stEvent.getStudyEventId(), studySubject.getStudySubjectId(), formLayout.getFormLayoutId());
                if (eventCrf != null) {
                    eventCrfId = eventCrf.getEventCrfId();
                    logger.debug("EventCrf with StudyEventDefinition Oid {},Crf Oid {} and StudySubjectOid {} already exist in the System",
                            studyEventDefinition.getOc_oid(), crf.getOcOid(), studySubject.getOcOid());
                    break;
                }
            }
        }

        CommonEventContainerDTO commonEventContainerDTO = new CommonEventContainerDTO();
        commonEventContainerDTO.setEventCrfId(eventCrfId);
        commonEventContainerDTO.setFormLayout(formLayout);
        commonEventContainerDTO.setEventCrf(eventCrf);
        commonEventContainerDTO.setStudyEventDefinition(studyEventDefinition);
        commonEventContainerDTO.setUserAccount(userAccount);
        commonEventContainerDTO.setStudySubject(studySubject);
        commonEventContainerDTO.setMaxOrdinal(maxOrdinal);

        return commonEventContainerDTO;
    }

    public Page getPage(String name) {
        Page page = null;
        PageLayout pageLayout = pageLayoutDao.findByPageLayoutName(name);
        if (pageLayout != null) {
            page = (Page) SerializationUtils.deserialize(pageLayout.getDefinition());
            logger.info("Page Object retrieved from database with page name: {}", pageLayout.getName());
        }
        return page;
    }

    public List<Component> getPageComponents(String name){
        Page page =  getPage(name);
        if(page!=null && page.getComponents()!=null){
            return page.getComponents();
        }
        return null;
    }



    /**
     * populate new study event object and save in db
     *
     * @param studySubject
     * @param studyEventDefinition
     * @param maxOrdinal
     * @param userAccount
     * @return
     */
    private StudyEvent scheduleNewStudyEvent(StudySubject studySubject, StudyEventDefinition studyEventDefinition, Integer maxOrdinal,
                                             UserAccount userAccount) {
        StudyEvent studyEvent = new StudyEvent();
        studyEvent.setStudyEventDefinition(studyEventDefinition);
        studyEvent.setSampleOrdinal(maxOrdinal + 1);
        studyEvent.setWorkflowStatus(StudyEventWorkflowStatusEnum.NOT_SCHEDULED);
        studyEvent.setStatusId(Status.AVAILABLE.getCode());
        studyEvent.setStudySubject(studySubject);
        studyEvent.setDateCreated(new Date());
        studyEvent.setUserAccount(userAccount);
        studyEvent.setDateStart(null);
        studyEvent.setStartTimeFlag(false);
        studyEvent.setEndTimeFlag(false);
        studyEvent = studyEventDao.saveOrUpdate(studyEvent);
        return studyEvent;

    }

    public StudyDao getStudyDao() {
        return studyDao;
    }

    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    public UserAccountDao getUserAccountDao() {
        return userAccountDao;
    }

    public void setUserAccountDao(UserAccountDao userAccountDao) {
        this.userAccountDao = userAccountDao;
    }

    public StudySubjectDao getStudySubjectDao() {
        return studySubjectDao;
    }

    public void setStudySubjectDao(StudySubjectDao studySubjectDao) {
        this.studySubjectDao = studySubjectDao;
    }

    public CrfDao getCrfDao() {
        return crfDao;
    }

    public void setCrfDao(CrfDao crfDao) {
        this.crfDao = crfDao;
    }


    public StudyEventDao getStudyEventDao() {
        return studyEventDao;
    }

    public void setStudyEventDao(StudyEventDao studyEventDao) {
        this.studyEventDao = studyEventDao;
    }

    public EventCrfDao getEventCrfDao() {
        return eventCrfDao;
    }

    public void setEventCrfDao(EventCrfDao eventCrfDao) {
        this.eventCrfDao = eventCrfDao;
    }


    public String[] getTableColumns(String pageName,String componentName) {
        List<Component> components = getPageComponents(pageName);
        if (components != null) {
            for (Component component : components) {
                if (component.getName().equals(componentName)) {
                    return component.getColumns();
                }
            }
        }
        return null;
    }


}
