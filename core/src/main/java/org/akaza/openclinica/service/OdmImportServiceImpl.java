package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.*;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.*;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.domain.xform.XformParser;
import org.akaza.openclinica.service.crfdata.ExecuteIndividualCrfObject;
import org.akaza.openclinica.service.crfdata.XformMetaDataService;
import org.akaza.openclinica.service.dto.Crf;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.apache.commons.fileupload.FileItem;
import org.cdisc.ns.odm.v130.*;
import org.openclinica.ns.odm_ext_v130.v31.OCodmComplexTypeDefinitionConfigurationParameters;
import org.openclinica.ns.odm_ext_v130.v31.OCodmComplexTypeDefinitionEventDefinitionDetails;
import org.openclinica.ns.odm_ext_v130.v31.OCodmComplexTypeDefinitionFormLayoutDef;
import org.openclinica.ns.odm_ext_v130.v31.OCodmComplexTypeDefinitionFormLayoutRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OdmImportServiceImpl implements OdmImportService {
    public final String FM_BASEURL = "http://fm.openclinica.info:8080/api/protocol/";
    private UserAccountDao userAccountDao;
    private StudyUserRoleDao studyUserRoleDao;
    private StudyEventDefinitionDao studyEventDefDao;
    private EventDefinitionCrfDao eventDefinitionCrfDao;
    private CrfDao crfDao;
    private CrfVersionDao crfVersionDao;
    private FormLayoutDao formLayoutDao;
    private FormLayoutMediaDao formLayoutMediaDao;
    private StudyDao studyDao;
    private EventDefinitionCrfTagDao eventDefinitionCrfTagDao;
    private StudyParameterValueDao studyParameterValueDao;
    private DataSource dataSource;

    private XformParser xformParser;

    private XformMetaDataService xformService;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public OdmImportServiceImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Transactional
    public void importOdmToOC(ODM odm, String boardId) {

        CoreResources.setRequestSchemaByStudy(odm.getStudy().get(0).getOID(),dataSource);

        UserAccount userAccount = getCurrentUser();
        // TODO add validation to all entities
        ODMcomplexTypeDefinitionStudy odmStudy = odm.getStudy().get(0);
        Study study = saveOrUpdateStudy(odm, userAccount, odmStudy);

        ParticipantPortalRegistrar portal = new ParticipantPortalRegistrar();
        portal.registerStudy(study.getOc_oid(), study.getOc_oid(), study.getName());

        StudyParameterValue spv = getStudyParameterValueDao().findByStudyIdParameter(study.getStudyId(), "participantPortal");
        // Update OC Study configuration
        if (spv == null) {
            spv = new StudyParameterValue();
            spv.setStudy(study);
        }
        spv.setValue("enabled");
        spv = getStudyParameterValueDao().saveOrUpdate(spv);

        StudyUserRole studyUserRole = null;
        StudyUserRoleId studyUserRoleId = null;

        Crf[] fmCrfs = getAllCrfsByProtIdFromFormManager(boardId);


/*        ArrayList<StudyUserRole> surRoles = getStudyUserRoleDao().findAllUserRolesByUserAccount(userAccount, study.getStudyId(), study.getStudyId());
        if (surRoles.size() == 0) {
            studyUserRoleId = new StudyUserRoleId();
            studyUserRole = new StudyUserRole();
            studyUserRole = getStudyUserRoleDao().saveOrUpdate(populateUserRole(study, userAccount, studyUserRole, studyUserRoleId));
        }
*/
        StudyEventDefinition studyEventDefinition = null;
        List<ODMcomplexTypeDefinitionMetaDataVersion> odmMetadataVersions = odmStudy.getMetaDataVersion();
        List<ODMcomplexTypeDefinitionStudyEventDef> odmStudyEventDefs = saveOrUpdateEvent(userAccount, study, odmMetadataVersions);

        CrfBean crf = null;
        FormLayout formLayout = null;

        saveOrUpdateCrf(userAccount, study, odmMetadataVersions, fmCrfs);

        List<ODMcomplexTypeDefinitionStudyEventRef> odmStudyEventRefs = odmMetadataVersions.get(0).getProtocol().getStudyEventRef();
        for (ODMcomplexTypeDefinitionStudyEventRef odmStudyEventRef : odmStudyEventRefs) {
            for (ODMcomplexTypeDefinitionStudyEventDef odmStudyEventDef : odmStudyEventDefs) {
                if (odmStudyEventDef.getOID().equals(odmStudyEventRef.getStudyEventOID())) {
                    studyEventDefinition = getStudyEventDefDao().findByOcOID(odmStudyEventDef.getOID());
                    studyEventDefinition.setOrdinal(odmStudyEventRef.getOrderNumber().intValue());
                    studyEventDefinition = getStudyEventDefDao().saveOrUpdate(studyEventDefinition);

                    List<EventDefinitionCrf> jsonEventDefCrfList = new ArrayList<>();
                    EventDefinitionCrf eventDefinitionCrf = null;
                    for (ODMcomplexTypeDefinitionFormRef odmFormRef : odmStudyEventDef.getFormRef()) {
                        crf = getCrfDao().findByOcOID(odmFormRef.getFormOID());

                        eventDefinitionCrf = getEventDefinitionCrfDao().findByStudyEventDefinitionIdAndCRFIdAndStudyId(
                                studyEventDefinition.getStudyEventDefinitionId(), crf.getCrfId(), study.getStudyId());

                        String defaultVersionName = null;
                        OCodmComplexTypeDefinitionConfigurationParameters conf = odmFormRef.getConfigurationParameters();
                        List<OCodmComplexTypeDefinitionFormLayoutRef> formLayoutRefs = odmFormRef.getFormLayoutRef();
                        if (formLayoutRefs.size() == 1 && formLayoutRefs.get(0).getIsDefaultVersion() == null) {
                            defaultVersionName = formLayoutRefs.get(0).getOID();
                        } else {
                            for (OCodmComplexTypeDefinitionFormLayoutRef formLayoutRef : formLayoutRefs) {
                                if (formLayoutRef.getIsDefaultVersion().equalsIgnoreCase("Yes")) {
                                    defaultVersionName = formLayoutRef.getOID();
                                }
                            }
                        }
                        if (defaultVersionName == null) {
                            defaultVersionName = formLayoutRefs.get(0).getOID();
                        }
                        formLayout = getFormLayoutDao().findByNameCrfId(defaultVersionName, crf.getCrfId());
                        EventDefinitionCrfDTO edcObj = new EventDefinitionCrfDTO();
                        edcObj.setUserAccount(userAccount);
                        edcObj.setConf(conf);
                        edcObj.setCrf(crf);
                        edcObj.setEventDefinitionCrf(eventDefinitionCrf);
                        edcObj.setOdmFormRef(odmFormRef);
                        edcObj.setStudy(study);
                        edcObj.setFormLayout(formLayout);
                        edcObj.setStudyEventDefinition(studyEventDefinition);

                        EDCTagDTO populateEDCTagParameter = new EDCTagDTO();
                        populateEDCTagParameter.setConf(conf);

                        populateEDCTagParameter.setConf(conf);
                        populateEDCTagParameter.setEventDefinitionCrf(eventDefinitionCrf);
                        populateEDCTagParameter.setUserAccount(userAccount);

                        eventDefinitionCrf = saveOrUpdateEventDefnCrf(new EventDefinitionCrfDTO(edcObj));
                        saveOrUpdateEDCTag(new EDCTagDTO(populateEDCTagParameter), studyEventDefinition, crf);
                        jsonEventDefCrfList.add(eventDefinitionCrf);
                    }

                    List<EventDefinitionCrf> ocEventDefCrfList = getEventDefinitionCrfDao()
                            .findAvailableByStudyEventDefStudy(studyEventDefinition.getStudyEventDefinitionId(), study.getStudyId());
                    for (EventDefinitionCrf ocEventDefCrf : ocEventDefCrfList) {
                        if (!jsonEventDefCrfList.contains(ocEventDefCrf)) {
                            ocEventDefCrf.setStatusId(Status.DELETED.getCode());
                            getEventDefinitionCrfDao().saveOrUpdate(ocEventDefCrf);
                        }
                    }

                }
            }

        }

    }

    private void saveOrUpdateEDCTag(EDCTagDTO edcTagObj, StudyEventDefinition studyEventDefinition, CrfBean crf) {
        EventDefinitionCrfTag eventDefinitionCrfTag;
        int tagId = 2; // Offline
        String crfPath = studyEventDefinition.getOc_oid() + "." + crf.getOcOid();
        eventDefinitionCrfTag = getEventDefinitionCrfTagDao().findByCrfPathAndTagId(tagId, crfPath);
        edcTagObj.setTagId(tagId);
        edcTagObj.setCrfPath(crfPath);
        if (eventDefinitionCrfTag == null) {
            eventDefinitionCrfTag = new EventDefinitionCrfTag();
            edcTagObj.setEventDefinitionCrfTag(eventDefinitionCrfTag);
            eventDefinitionCrfTag = getEventDefinitionCrfTagDao().saveOrUpdate(populateEDCTag(new EDCTagDTO(edcTagObj)));
        } else {
            edcTagObj.setEventDefinitionCrfTag(eventDefinitionCrfTag);
            eventDefinitionCrfTag = getEventDefinitionCrfTagDao().saveOrUpdate(updateEDCTag(new EDCTagDTO(edcTagObj)));
        }
    }

    private EventDefinitionCrf saveOrUpdateEventDefnCrf(EventDefinitionCrfDTO edcObj) {
        EventDefinitionCrf eventDefinitionCrf = edcObj.getEventDefinitionCrf();
        if (eventDefinitionCrf == null) {
            eventDefinitionCrf = new EventDefinitionCrf();
            edcObj.setEventDefinitionCrf(eventDefinitionCrf);
            eventDefinitionCrf = getEventDefinitionCrfDao().saveOrUpdate(populateEventDefinitionCrf(new EventDefinitionCrfDTO(edcObj)));
        } else {
            eventDefinitionCrf = getEventDefinitionCrfDao().saveOrUpdate(updateEventDefinitionCrf(new EventDefinitionCrfDTO(edcObj)));
        }
        return eventDefinitionCrf;
    }

    private void saveOrUpdateCrf(UserAccount userAccount, Study study, List<ODMcomplexTypeDefinitionMetaDataVersion> odmMetadataVersions, Crf[] fmCrfs) {
        for (ODMcomplexTypeDefinitionFormDef odmFormDef : odmMetadataVersions.get(0).getFormDef()) {
            String crfOid = odmFormDef.getOID();
            List<OCodmComplexTypeDefinitionFormLayoutDef> formLayoutDefs = odmFormDef.getFormLayoutDef();
    //        String crfDescription = odmFormDef.getFormDetails().getDescription();
            String crfDescription="";
            String crfName = odmFormDef.getName();
            saveOrUpdateCrfAndFormLayouts(crfOid, formLayoutDefs, fmCrfs, userAccount, study, crfName, crfDescription);
        }

    }

    private void saveOrUpdateCrfAndFormLayouts(String crfOid, List<OCodmComplexTypeDefinitionFormLayoutDef> formLayoutDefs, Crf[] fmCrfs,
            UserAccount userAccount, Study study, String crfName, String crfDescription) {

        DataBinder dataBinder = new DataBinder(new FormLayout());
        Errors errors = dataBinder.getBindingResult();
        List<FileItem> items = null;
        StudyBean currentStudy = new StudyBean();
        currentStudy.setId(study.getStudyId());

        UserAccountBean ub = new UserAccountBean();
        ub.setId(userAccount.getUserId());
        ub.setActiveStudyId(currentStudy.getId());

        if(fmCrfs!=null){
        for (Crf crf : fmCrfs) {
            if (crf.getOcoid().equals(crfOid)) {
                ExecuteIndividualCrfObject eicObj = new ExecuteIndividualCrfObject(crf, formLayoutDefs, errors, items, currentStudy, ub, true, crfName,
                        crfDescription);
                xformService.executeIndividualCrf(eicObj);
            }
        }
        }
    }

    private List<ODMcomplexTypeDefinitionStudyEventDef> saveOrUpdateEvent(UserAccount userAccount, Study study,
            List<ODMcomplexTypeDefinitionMetaDataVersion> odmMetadataVersions) {
        StudyEventDefinition studyEventDefinition;
        List<ODMcomplexTypeDefinitionStudyEventDef> odmStudyEventDefs = odmMetadataVersions.get(0).getStudyEventDef();
        List<StudyEventDefinition> jsonEventList = new ArrayList<>();
        for (ODMcomplexTypeDefinitionStudyEventDef odmStudyEventDef : odmStudyEventDefs) {

            studyEventDefinition = getStudyEventDefDao().findByOcOID(odmStudyEventDef.getOID());
            if (studyEventDefinition == null || studyEventDefinition.getStudyEventDefinitionId() == 0) {
                studyEventDefinition = new StudyEventDefinition();
                studyEventDefinition.setOc_oid(odmStudyEventDef.getOID());
                studyEventDefinition = getStudyEventDefDao().saveOrUpdate(populateEvent(odmStudyEventDef, userAccount, studyEventDefinition, study));
            } else {
                studyEventDefinition = getStudyEventDefDao().saveOrUpdate(updateEvent(odmStudyEventDef, userAccount, studyEventDefinition, study));
            }
            jsonEventList.add(studyEventDefinition);
        }
        List<StudyEventDefinition> ocEventList = getStudyEventDefDao().findAll(); // findAllNonRemovedEvents
        for (StudyEventDefinition ocEvent : ocEventList) {
            if (!jsonEventList.contains(ocEvent)) {
                ocEvent.setStatus(Status.DELETED);
                getStudyEventDefDao().saveOrUpdate(ocEvent);
            }
        }

        return odmStudyEventDefs;
    }

    private Study saveOrUpdateStudy(ODM odm, UserAccount userAccount, ODMcomplexTypeDefinitionStudy odmStudy) {
        ODMcomplexTypeDefinitionGlobalVariables odmGlobalVariables = odmStudy.getGlobalVariables();
        String studyOid = odm.getStudy().get(0).getOID();
        Study study = getStudyDao().findByOcOID(studyOid);

        if (study == null || study.getStudyId() == 0) {
            study = new Study();
            study.setOc_oid(studyOid);
            study = getStudyDao().saveOrUpdate(populateStudy(odmGlobalVariables, userAccount, study));
        } else {
            study = getStudyDao().saveOrUpdate(updateStudy(odmGlobalVariables, userAccount, study));
        }
        return study;
    }

    private Study populateStudy(ODMcomplexTypeDefinitionGlobalVariables odmGlobalVariables, UserAccount userAccount, Study study) {
        study.setUniqueIdentifier(odmGlobalVariables.getProtocolName().getValue());
        study.setName(odmGlobalVariables.getStudyName().getValue());
        // study.setSummary(odmGlobalVariables.getStudyDescription().getValue());
        study.setSummary("This is the summary");
        study.setStatus(org.akaza.openclinica.domain.Status.AVAILABLE);
        study.setUserAccount(userAccount);
        study.setDateCreated(new Date());
        return study;
    }

    private Study updateStudy(ODMcomplexTypeDefinitionGlobalVariables odmGlobalVariables, UserAccount userAccount, Study study) {
        study = populateStudy(odmGlobalVariables, userAccount, study);
        study.setUpdateId(userAccount.getUserId());
        study.setDateUpdated(new Date());
        return study;
    }

    private StudyUserRole populateUserRole(Study study, UserAccount userAccount, StudyUserRole sur, StudyUserRoleId surId) {
        surId.setUserName(userAccount.getUserName());
        sur.setRoleName(Role.STUDYDIRECTOR.getName());
        surId.setStudyId(study.getStudyId());
        sur.setStatusId(1);
        sur.setOwnerId(userAccount.getUserId());
        sur.setDateCreated(new Date());
        sur.setId(surId);
        return sur;
    }

    private StudyEventDefinition populateEvent(ODMcomplexTypeDefinitionStudyEventDef odmStudyEventDef, UserAccount userAccount,
            StudyEventDefinition studyEventDefinition, Study study) {
        studyEventDefinition.setName(odmStudyEventDef.getName());

        if (odmStudyEventDef.getRepeating().value().equalsIgnoreCase("Yes")) {
            studyEventDefinition.setRepeating(true);
        } else {
            studyEventDefinition.setRepeating(false);
        }
        studyEventDefinition.setType(EventType.SCHEDULED.value().toLowerCase());
        studyEventDefinition.setStudy(study);
        studyEventDefinition.setUserAccount(userAccount);
        studyEventDefinition.setStatus(org.akaza.openclinica.domain.Status.AVAILABLE);
        if (odmStudyEventDef.getStudyEventDefElementExtension().size() != 0) {
            OCodmComplexTypeDefinitionEventDefinitionDetails eventDetails = odmStudyEventDef.getStudyEventDefElementExtension().get(0);
            if (eventDetails != null && eventDetails.getCategory() != null) {
                studyEventDefinition.setCategory(eventDetails.getCategory());
            }
            if (eventDetails != null && eventDetails.getDescription() != null) {
                studyEventDefinition.setDescription(eventDetails.getDescription());
            }
        }

        return studyEventDefinition;
    }

    private StudyEventDefinition updateEvent(ODMcomplexTypeDefinitionStudyEventDef odmStudyEventDef, UserAccount userAccount,
            StudyEventDefinition studyEventDefinition, Study study) {
        studyEventDefinition = populateEvent(odmStudyEventDef, userAccount, studyEventDefinition, study);
        studyEventDefinition.setUpdateId(userAccount.getUserId());
        studyEventDefinition.setDateUpdated(new Date());
        return studyEventDefinition;
    }

    private UserAccount getCurrentUser() {
        UserAccount ub = getUserAccountDao().findById(1);
        return ub;
    }

    private EventDefinitionCrf populateEventDefinitionCrf(EventDefinitionCrfDTO edcObj) {
        edcObj.getEventDefinitionCrf().setStudy(edcObj.getStudy());
        edcObj.getEventDefinitionCrf().setStudyEventDefinition(edcObj.getStudyEventDefinition());
        edcObj.getEventDefinitionCrf().setCrf(edcObj.getCrf());
        edcObj.getEventDefinitionCrf().setStatusId(org.akaza.openclinica.domain.Status.AVAILABLE.getCode());
        edcObj.getEventDefinitionCrf().setUserAccount(edcObj.getUserAccount());
        edcObj.getEventDefinitionCrf().setCrfVersion(edcObj.getCrfVersion());
        setConfigurationProperties(edcObj.getConf(), edcObj.getEventDefinitionCrf());
        if (edcObj.getOdmFormRef().getMandatory().equals(YesOrNo.YES)) {
            edcObj.getEventDefinitionCrf().setRequiredCrf(true);
        } else {
            edcObj.getEventDefinitionCrf().setRequiredCrf(false);
        }
        return edcObj.getEventDefinitionCrf();
    }

    private EventDefinitionCrf updateEventDefinitionCrf(EventDefinitionCrfDTO edcObj) {
        EventDefinitionCrf eventDefinitionCrf = edcObj.getEventDefinitionCrf();
        eventDefinitionCrf = populateEventDefinitionCrf(new EventDefinitionCrfDTO(edcObj));
        eventDefinitionCrf.setUpdateId(edcObj.getUserAccount().getUserId());
        eventDefinitionCrf.setDateUpdated(new Date());

        return eventDefinitionCrf;
    }

    private EventDefinitionCrf setConfigurationProperties(OCodmComplexTypeDefinitionConfigurationParameters conf, EventDefinitionCrf eventDefinitionCrf) {
        if (conf.getAllowAnonymousSubmission().equalsIgnoreCase("Yes")) {
            eventDefinitionCrf.setAllowAnonymousSubmission(true);
        } else {
            eventDefinitionCrf.setAllowAnonymousSubmission(false);
        }
        if (conf.getParticipantForm().equalsIgnoreCase("Yes")) {
            eventDefinitionCrf.setParicipantForm(true);
        } else {
            eventDefinitionCrf.setParicipantForm(false);
        }
        if (conf.getHideCRF().equalsIgnoreCase("Yes")) {
            eventDefinitionCrf.setHideCrf(true);
        } else {
            eventDefinitionCrf.setHideCrf(false);
        }
        eventDefinitionCrf.setSubmissionUrl(conf.getSubmissionUrl());

        return eventDefinitionCrf;
    }

    private EventDefinitionCrfTag populateEDCTag(EDCTagDTO edcTagObj) {
        if (edcTagObj.getConf().getOffline().equalsIgnoreCase("Yes")) {
            edcTagObj.getEventDefinitionCrfTag().setActive(true);
        } else {
            edcTagObj.getEventDefinitionCrfTag().setActive(false);
        }
        edcTagObj.getEventDefinitionCrfTag().setTagId(edcTagObj.getTagId());
        edcTagObj.getEventDefinitionCrfTag().setPath(edcTagObj.getCrfPath());
        edcTagObj.getEventDefinitionCrfTag().setDateCreated(new Date());
        edcTagObj.getEventDefinitionCrfTag().setUserAccount(edcTagObj.getUserAccount());
        return edcTagObj.getEventDefinitionCrfTag();
    }

    private EventDefinitionCrfTag updateEDCTag(EDCTagDTO edcTagObj) {
        EventDefinitionCrfTag eventDefinitionCrfTag = edcTagObj.getEventDefinitionCrfTag();
        eventDefinitionCrfTag = populateEDCTag(new EDCTagDTO(edcTagObj));
        eventDefinitionCrfTag.setUpdateId(edcTagObj.getUserAccount().getUserId());
        eventDefinitionCrfTag.setDateUpdated(new Date());
        return eventDefinitionCrfTag;
    }

    public UserAccountDao getUserAccountDao() {
        return userAccountDao;
    }

    public void setUserAccountDao(UserAccountDao userAccountDao) {
        this.userAccountDao = userAccountDao;
    }

    public StudyUserRoleDao getStudyUserRoleDao() {
        return studyUserRoleDao;
    }

    public void setStudyUserRoleDao(StudyUserRoleDao studyUserRoleDao) {
        this.studyUserRoleDao = studyUserRoleDao;
    }

    public StudyEventDefinitionDao getStudyEventDefDao() {
        return studyEventDefDao;
    }

    public void setStudyEventDefDao(StudyEventDefinitionDao studyEventDefDao) {
        this.studyEventDefDao = studyEventDefDao;
    }

    public EventDefinitionCrfDao getEventDefinitionCrfDao() {
        return eventDefinitionCrfDao;
    }

    public void setEventDefinitionCrfDao(EventDefinitionCrfDao eventDefinitionCrfDao) {
        this.eventDefinitionCrfDao = eventDefinitionCrfDao;
    }

    public CrfDao getCrfDao() {
        return crfDao;
    }

    public void setCrfDao(CrfDao crfDao) {
        this.crfDao = crfDao;
    }

    public CrfVersionDao getCrfVersionDao() {
        return crfVersionDao;
    }

    public void setCrfVersionDao(CrfVersionDao crfVersionDao) {
        this.crfVersionDao = crfVersionDao;
    }

    public StudyDao getStudyDao() {
        return studyDao;
    }

    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    public EventDefinitionCrfTagDao getEventDefinitionCrfTagDao() {
        return eventDefinitionCrfTagDao;
    }

    public void setEventDefinitionCrfTagDao(EventDefinitionCrfTagDao eventDefinitionCrfTagDao) {
        this.eventDefinitionCrfTagDao = eventDefinitionCrfTagDao;
    }

    public XformParser getXformParser() {
        return xformParser;
    }

    public void setXformParser(XformParser xformParser) {
        this.xformParser = xformParser;
    }

    public XformMetaDataService getXformService() {
        return xformService;
    }

    public void setXformService(XformMetaDataService xformService) {
        this.xformService = xformService;
    }

    public StudyParameterValueDao getStudyParameterValueDao() {
        return studyParameterValueDao;
    }

    public void setStudyParameterValueDao(StudyParameterValueDao studyParameterValueDao) {
        this.studyParameterValueDao = studyParameterValueDao;
    }

    public FormLayoutDao getFormLayoutDao() {
        return formLayoutDao;
    }

    public void setFormLayoutDao(FormLayoutDao formLayoutDao) {
        this.formLayoutDao = formLayoutDao;
    }

    public Crf[] getAllCrfsByProtIdFromFormManager(String boardId) {
        // String protocolId = study.getUniqueIdentifier();
      //  String protocolId = study.getOc_oid();

        String url = FM_BASEURL + boardId + "/forms";
        RestTemplate restTemplate = new RestTemplate();
        Crf[] crfs = null;
        try {
            crfs = (Crf[]) restTemplate.getForObject(url, Crf[].class);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        return crfs;
    }

}
