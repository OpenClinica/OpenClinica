package org.akaza.openclinica.service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.CrfDao;
import org.akaza.openclinica.dao.hibernate.CrfVersionDao;
import org.akaza.openclinica.dao.hibernate.EventDefinitionCrfDao;
import org.akaza.openclinica.dao.hibernate.EventDefinitionCrfTagDao;
import org.akaza.openclinica.dao.hibernate.FormLayoutDao;
import org.akaza.openclinica.dao.hibernate.FormLayoutMediaDao;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDefinitionDao;
import org.akaza.openclinica.dao.hibernate.StudyParameterValueDao;
import org.akaza.openclinica.dao.hibernate.StudyUserRoleDao;
import org.akaza.openclinica.dao.hibernate.UserAccountDao;
import org.akaza.openclinica.domain.SourceDataVerification;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.CrfBean;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrfTag;
import org.akaza.openclinica.domain.datamap.FormLayout;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import org.akaza.openclinica.domain.datamap.StudyUserRole;
import org.akaza.openclinica.domain.datamap.StudyUserRoleId;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.domain.xform.XformParser;
import org.akaza.openclinica.service.crfdata.ExecuteIndividualCrfObject;
import org.akaza.openclinica.service.crfdata.XformMetaDataService;
import org.akaza.openclinica.service.dto.Bucket;
import org.akaza.openclinica.service.dto.Form;
import org.cdisc.ns.odm.v130.EventType;
import org.cdisc.ns.odm.v130.ODM;
import org.cdisc.ns.odm.v130.ODMcomplexTypeDefinitionFormDef;
import org.cdisc.ns.odm.v130.ODMcomplexTypeDefinitionFormRef;
import org.cdisc.ns.odm.v130.ODMcomplexTypeDefinitionGlobalVariables;
import org.cdisc.ns.odm.v130.ODMcomplexTypeDefinitionMetaDataVersion;
import org.cdisc.ns.odm.v130.ODMcomplexTypeDefinitionStudy;
import org.cdisc.ns.odm.v130.ODMcomplexTypeDefinitionStudyEventDef;
import org.cdisc.ns.odm.v130.ODMcomplexTypeDefinitionStudyEventRef;
import org.cdisc.ns.odm.v130.YesOrNo;
import org.openclinica.ns.odm_ext_v130.v31.OCodmComplexTypeDefinitionConfigurationParameters;
import org.openclinica.ns.odm_ext_v130.v31.OCodmComplexTypeDefinitionEventDefinitionDetails;
import org.openclinica.ns.odm_ext_v130.v31.OCodmComplexTypeDefinitionFormLayoutDef;
import org.openclinica.ns.odm_ext_v130.v31.OCodmComplexTypeDefinitionFormLayoutRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.web.client.RestTemplate;

public class OdmImportServiceImpl implements OdmImportService {

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
    private CoreResources coreResources;
    private EventService eventService;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public OdmImportServiceImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void printOdm(ODM odm) {
        JAXBContext jaxbContext = null;
        try {
            jaxbContext = JAXBContext.newInstance(ODM.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            // output pretty printed
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            jaxbMarshaller.marshal(odm, System.out);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    @Transactional
    public void importOdmToOC(ODM odm, String boardId) {

        printOdm(odm);

        CoreResources.setRequestSchemaByStudy(odm.getStudy().get(0).getOID(), dataSource);

        UserAccount userAccount = getCurrentUser();
        // TODO add validation to all entities
        ODMcomplexTypeDefinitionStudy odmStudy = odm.getStudy().get(0);
        Study study = retrieveStudy(odm, userAccount, odmStudy);
        Study publicStudy = studyDao.findPublicStudy(study.getOc_oid());
        Form[] fmCrfs = getAllCrfsByProtIdFromFormManager(boardId);

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
                        // Restore CRF From Event Definition
                        if (eventDefinitionCrf != null && !eventDefinitionCrf.getStatusId().equals(Status.AVAILABLE.getCode())) {
                            eventDefinitionCrf.setStatusId(Status.AVAILABLE.getCode());
                            eventService.restoreCrfFromEventDefinition(eventDefinitionCrf.getEventDefinitionCrfId(),
                                    studyEventDefinition.getStudyEventDefinitionId(), userAccount.getUserId());
                        }
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
                        edcObj.setOrdinal(odmFormRef.getOrderNumber().intValue());

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
                            // Remove CRF From Event Definition
                            ocEventDefCrf.setStatusId(Status.DELETED.getCode());
                            ocEventDefCrf.setUpdateId(userAccount.getUserId());
                            ocEventDefCrf.setDateUpdated(new Date());
                            getEventDefinitionCrfDao().saveOrUpdate(ocEventDefCrf);
                            eventService.removeCrfFromEventDefinition(ocEventDefCrf.getEventDefinitionCrfId(), studyEventDefinition.getStudyEventDefinitionId(),
                                    userAccount.getUserId(), study.getStudyId());
                        }
                    }

                }
            }

        }
        CoreResources.setRequestSchema("public");
        publicStudy.setPublished(true);
        studyDao.saveOrUpdate(publicStudy);

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
            edcObj.getEventDefinitionCrf().setStatusId(org.akaza.openclinica.domain.Status.AVAILABLE.getCode());
            eventDefinitionCrf = getEventDefinitionCrfDao().saveOrUpdate(populateEventDefinitionCrf(new EventDefinitionCrfDTO(edcObj)));
        } else {
            eventDefinitionCrf = getEventDefinitionCrfDao().saveOrUpdate(updateEventDefinitionCrf(new EventDefinitionCrfDTO(edcObj)));
        }
        return eventDefinitionCrf;
    }

    private void saveOrUpdateCrf(UserAccount userAccount, Study study, List<ODMcomplexTypeDefinitionMetaDataVersion> odmMetadataVersions, Form[] fmCrfs) {
        Set<Long> publishedVersions = new HashSet<>();
        for (ODMcomplexTypeDefinitionFormDef odmFormDef : odmMetadataVersions.get(0).getFormDef()) {
            String crfOid = odmFormDef.getOID();
            List<OCodmComplexTypeDefinitionFormLayoutDef> formLayoutDefs = odmFormDef.getFormLayoutDef();
            // String crfDescription = odmFormDef.getFormDetails().getDescription();
            String crfName = odmFormDef.getName();

            CrfBean crfBean = getCrfDao().findByOcOID(crfOid);
            if (crfBean != null) {
                List<String> jsonLayoutOids = new ArrayList<>();
                for (OCodmComplexTypeDefinitionFormLayoutDef formLayoutDef : formLayoutDefs) {
                    jsonLayoutOids.add(formLayoutDef.getOID());
                }
                List<FormLayout> formLayouts = getFormLayoutDao().findAllByCrfId(crfBean.getCrfId());
                for (FormLayout formLayout : formLayouts) {
                    if (!jsonLayoutOids.contains(formLayout.getName()) && !formLayout.getStatus().equals(Status.LOCKED)) {
                        formLayout.setStatus(Status.LOCKED);
                        formLayout.setUserAccount(userAccount);
                        formLayout.setDateCreated(new Date());
                        getFormLayoutDao().saveOrUpdate(formLayout);
                    }
                }
            }
            saveOrUpdateCrfAndFormLayouts(crfOid, formLayoutDefs, fmCrfs, userAccount, study, crfName, publishedVersions);
        }
        if (publishedVersions.size() != 0) {
            String fmUrl = getCoreResources().getField("formManager").trim() + "/api/xlsForm/setPublishedEnvironment";
            RestTemplate restTemplate = new RestTemplate();
            PublishingDTO dto = new PublishingDTO();
            dto.setPublishedEnvType(study.getEnvType());
            dto.setVersionIds(publishedVersions);

            try {
                restTemplate.postForObject(fmUrl, dto, PublishingDTO.class);
            } catch (Exception e) {
                logger.info(e.getMessage());
                throw new RuntimeException("Something went wrong !!");
            }
        }

    }

    private void saveOrUpdateCrfAndFormLayouts(String crfOid, List<OCodmComplexTypeDefinitionFormLayoutDef> formLayoutDefs, Form[] fmCrfs,
            UserAccount userAccount, Study study, String crfName, Set<Long> publishedVersions) {

        DataBinder dataBinder = new DataBinder(new FormLayout());
        Errors errors = dataBinder.getBindingResult();
        StudyBean currentStudy = new StudyBean();
        currentStudy.setId(study.getStudyId());
        currentStudy.setEnvType(study.getEnvType());
        currentStudy.setOid(study.getOc_oid());

        UserAccountBean ub = new UserAccountBean();
        ub.setId(userAccount.getUserId());
        ub.setActiveStudyId(currentStudy.getId());
        for (Form crf : fmCrfs) {
            if (crf.getOcoid().equals(crfOid)) {
                crf.setName(crfName);
                ExecuteIndividualCrfObject eicObj = new ExecuteIndividualCrfObject(crf, formLayoutDefs, errors, currentStudy, ub, true, null);
                xformService.executeIndividualCrf(eicObj, publishedVersions);
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
                studyEventDefinition.setStatus(org.akaza.openclinica.domain.Status.AVAILABLE);
                studyEventDefinition = getStudyEventDefDao().saveOrUpdate(populateEvent(odmStudyEventDef, userAccount, studyEventDefinition, study));
            } else {
                if (!studyEventDefinition.getStatus().equals(Status.AVAILABLE)) {
                    // restore study event defn
                    eventService.restoreStudyEventDefn(studyEventDefinition.getStudyEventDefinitionId(), userAccount.getUserId());
                }
                studyEventDefinition = getStudyEventDefDao().saveOrUpdate(updateEvent(odmStudyEventDef, userAccount, studyEventDefinition, study));
            }
            jsonEventList.add(studyEventDefinition);
        }
        List<StudyEventDefinition> ocEventList = getStudyEventDefDao().findAll(); // findAllNonRemovedEvents
        for (StudyEventDefinition ocEvent : ocEventList) {
            if (!jsonEventList.contains(ocEvent)) {
                // remove study event defn
                eventService.removeStudyEventDefn(ocEvent.getStudyEventDefinitionId(), userAccount.getUserId());
            }
        }

        return odmStudyEventDefs;
    }

    private Study retrieveStudy(ODM odm, UserAccount userAccount, ODMcomplexTypeDefinitionStudy odmStudy) {
        String studyOid = odm.getStudy().get(0).getOID();
        Study study = getStudyDao().findByOcOID(studyOid);

        if (study == null) {
            throw new RuntimeException("Study with this oid: " + studyOid + " doesn't exist. Please fix !!! ");
        }
        return study;
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
        edcObj.getEventDefinitionCrf().setUserAccount(edcObj.getUserAccount());
        edcObj.getEventDefinitionCrf().setFormLayout(edcObj.getFormLayout());
        edcObj.getEventDefinitionCrf().setDoubleEntry(false);
        edcObj.getEventDefinitionCrf().setElectronicSignature(false);
        edcObj.getEventDefinitionCrf().setOrdinal(edcObj.getOrdinal());
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
        String sdvDescription = conf.getSourceDataVerificationCode();
        if (!StringUtils.isEmpty(sdvDescription) && SourceDataVerification.getByDescription(sdvDescription) != null) {
            eventDefinitionCrf.setSourceDataVerificationCode(SourceDataVerification.getByDescription(conf.getSourceDataVerificationCode()).getCode());
        } else {
            eventDefinitionCrf.setSourceDataVerificationCode(SourceDataVerification.PARTIALREQUIRED.getCode());
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

    public CoreResources getCoreResources() {
        return coreResources;
    }

    public void setCoreResources(CoreResources coreResources) {
        this.coreResources = coreResources;
    }

    public Form[] getAllCrfsByProtIdFromFormManager(String boardId) {

        String formServiceBaseURL = getCoreResources().getField("formManager").trim() + "/api/buckets?boardUuid={0}";

        String url = MessageFormat.format(formServiceBaseURL, boardId);
        Bucket[] buckets = null;
        RestTemplate restTemplate = new RestTemplate();
        ArrayList<Form> forms = null;

        try {
            buckets = (Bucket[]) restTemplate.getForObject(url, Bucket[].class);
        } catch (Exception e) {
            logger.info(e.getMessage());
            throw new RuntimeException("Something went wrong !!");
        }

        if (buckets != null && buckets.length == 1) {
            forms = buckets[0].getForms();
        } else {
            throw new RuntimeException("No forms found for this board");
        }

        return forms.toArray(new Form[forms.size()]);
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

}
