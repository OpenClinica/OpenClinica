package org.akaza.openclinica.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilderFactory;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Utils;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.FormLayoutBean;
import org.akaza.openclinica.dao.hibernate.CrfDao;
import org.akaza.openclinica.dao.hibernate.CrfVersionDao;
import org.akaza.openclinica.dao.hibernate.EventDefinitionCrfDao;
import org.akaza.openclinica.dao.hibernate.EventDefinitionCrfTagDao;
import org.akaza.openclinica.dao.hibernate.FormLayoutDao;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDefinitionDao;
import org.akaza.openclinica.dao.hibernate.StudyParameterValueDao;
import org.akaza.openclinica.dao.hibernate.StudyUserRoleDao;
import org.akaza.openclinica.dao.hibernate.UserAccountDao;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.CrfBean;
import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrfTag;
import org.akaza.openclinica.domain.datamap.FormLayout;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import org.akaza.openclinica.domain.datamap.StudyParameterValue;
import org.akaza.openclinica.domain.datamap.StudyUserRole;
import org.akaza.openclinica.domain.datamap.StudyUserRoleId;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.domain.xform.XformContainer;
import org.akaza.openclinica.domain.xform.XformGroup;
import org.akaza.openclinica.domain.xform.XformItem;
import org.akaza.openclinica.domain.xform.XformParser;
import org.akaza.openclinica.domain.xform.dto.Html;
import org.akaza.openclinica.service.crfdata.XformMetaDataService;
import org.akaza.openclinica.service.dto.Crf;
import org.akaza.openclinica.service.dto.Version;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.cdisc.ns.odm.v130_sb.EventType;
import org.cdisc.ns.odm.v130_sb.ODM;
import org.cdisc.ns.odm.v130_sb.ODMcomplexTypeDefinitionFormDef;
import org.cdisc.ns.odm.v130_sb.ODMcomplexTypeDefinitionFormRef;
import org.cdisc.ns.odm.v130_sb.ODMcomplexTypeDefinitionGlobalVariables;
import org.cdisc.ns.odm.v130_sb.ODMcomplexTypeDefinitionMetaDataVersion;
import org.cdisc.ns.odm.v130_sb.ODMcomplexTypeDefinitionStudy;
import org.cdisc.ns.odm.v130_sb.ODMcomplexTypeDefinitionStudyEventDef;
import org.cdisc.ns.odm.v130_sb.ODMcomplexTypeDefinitionStudyEventRef;
import org.cdisc.ns.odm.v130_sb.YesOrNo;
import org.openclinica.ns.odm_ext_v130.v31_sb.OCodmComplexTypeDefinitionConfigurationParameters;
import org.openclinica.ns.odm_ext_v130.v31_sb.OCodmComplexTypeDefinitionEventDefinitionDetails;
import org.openclinica.ns.odm_ext_v130.v31_sb.OCodmComplexTypeDefinitionFormLayoutDef;
import org.openclinica.ns.odm_ext_v130.v31_sb.OCodmComplexTypeDefinitionFormLayoutRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class OdmImportServiceImpl implements OdmImportService {

    private UserAccountDao userAccountDao;
    private StudyUserRoleDao studyUserRoleDao;
    private StudyEventDefinitionDao studyEventDefDao;
    private EventDefinitionCrfDao eventDefinitionCrfDao;
    private CrfDao crfDao;
    private CrfVersionDao crfVersionDao;
    private FormLayoutDao formLayoutDao;
    private StudyDao studyDao;
    private EventDefinitionCrfTagDao eventDefinitionCrfTagDao;
    private StudyParameterValueDao studyParameterValueDao;

    private XformParser xformParser;

    private XformMetaDataService xformService;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public OdmImportServiceImpl(DataSource dataSource) {
    }

    @Transactional
    public void importOdmToOC(ODM odm) {

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

        Crf[] fmCrfs = getCrfsFromFormManager(study);

        ArrayList<StudyUserRole> surRoles = getStudyUserRoleDao().findAllUserRolesByUserAccount(userAccount, study.getStudyId(), study.getStudyId());
        if (surRoles.size() == 0) {
            studyUserRoleId = new StudyUserRoleId();
            studyUserRole = new StudyUserRole();
            studyUserRole = getStudyUserRoleDao().saveOrUpdate(populateUserRole(study, userAccount, studyUserRole, studyUserRoleId));
        }

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

                        String defaultVersionOid = null;
                        OCodmComplexTypeDefinitionConfigurationParameters conf = odmFormRef.getConfigurationParameters();
                        List<OCodmComplexTypeDefinitionFormLayoutRef> formLayoutRefs = odmFormRef.getFormLayoutRef();
                        if (formLayoutRefs.size() == 1 && formLayoutRefs.get(0).getIsDefaultVersion() == null) {
                            defaultVersionOid = formLayoutRefs.get(0).getOID();
                        } else {
                            for (OCodmComplexTypeDefinitionFormLayoutRef formLayoutRef : formLayoutRefs) {
                                if (formLayoutRef.getIsDefaultVersion().equalsIgnoreCase("Yes")) {
                                    defaultVersionOid = formLayoutRef.getOID();
                                }
                            }
                        }
                        if (defaultVersionOid == null) {
                            defaultVersionOid = formLayoutRefs.get(0).getOID();
                        }
                        formLayout = getFormLayoutDao().findByOcOID(defaultVersionOid);
                        PopulateEventDefinitionCrfParameter paramObj = new PopulateEventDefinitionCrfParameter();
                        paramObj.setUserAccount(userAccount);
                        paramObj.setConf(conf);
                        paramObj.setCrf(crf);
                        paramObj.setEventDefinitionCrf(eventDefinitionCrf);
                        paramObj.setOdmFormRef(odmFormRef);
                        paramObj.setStudy(study);
                        paramObj.setFormLayout(formLayout);
                        paramObj.setStudyEventDefinition(studyEventDefinition);

                        PopulateEDCTagParameter populateEDCTagParameter = new PopulateEDCTagParameter();
                        populateEDCTagParameter.setConf(conf);
                        populateEDCTagParameter.setEventDefinitionCrf(eventDefinitionCrf);
                        populateEDCTagParameter.setUserAccount(userAccount);

                        eventDefinitionCrf = saveOrUpdateEventDefnCrf(new PopulateEventDefinitionCrfParameter(paramObj));
                        saveOrUpdateEDCTag(new PopulateEDCTagParameter(populateEDCTagParameter), studyEventDefinition, crf);
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

    private void saveOrUpdateEDCTag(PopulateEDCTagParameter paramObj, StudyEventDefinition studyEventDefinition, CrfBean crf) {
        EventDefinitionCrfTag eventDefinitionCrfTag;
        int tagId = 2; // Offline
        String crfPath = studyEventDefinition.getOc_oid() + "." + crf.getOcOid();
        eventDefinitionCrfTag = getEventDefinitionCrfTagDao().findByCrfPathAndTagId(tagId, crfPath);
        paramObj.setTagId(tagId);
        paramObj.setCrfPath(crfPath);
        if (eventDefinitionCrfTag == null) {
            eventDefinitionCrfTag = new EventDefinitionCrfTag();
            paramObj.setEventDefinitionCrfTag(eventDefinitionCrfTag);
            eventDefinitionCrfTag = getEventDefinitionCrfTagDao().saveOrUpdate(populateEDCTag(new PopulateEDCTagParameter(paramObj)));
        } else {
            paramObj.setEventDefinitionCrfTag(eventDefinitionCrfTag);
            eventDefinitionCrfTag = getEventDefinitionCrfTagDao().saveOrUpdate(updateEDCTag(new PopulateEDCTagParameter(paramObj)));
        }
    }

    private EventDefinitionCrf saveOrUpdateEventDefnCrf(PopulateEventDefinitionCrfParameter paramObj) {
        EventDefinitionCrf eventDefinitionCrf = paramObj.getEventDefinitionCrf();
        if (eventDefinitionCrf == null) {
            eventDefinitionCrf = new EventDefinitionCrf();
            paramObj.setEventDefinitionCrf(eventDefinitionCrf);
            eventDefinitionCrf = getEventDefinitionCrfDao().saveOrUpdate(populateEventDefinitionCrf(new PopulateEventDefinitionCrfParameter(paramObj)));
        } else {
            eventDefinitionCrf = getEventDefinitionCrfDao().saveOrUpdate(updateEventDefinitionCrf(new PopulateEventDefinitionCrfParameter(paramObj)));
        }
        return eventDefinitionCrf;
    }

    private void saveOrUpdateCrf(UserAccount userAccount, Study study, List<ODMcomplexTypeDefinitionMetaDataVersion> odmMetadataVersions, Crf[] fmCrfs) {
        CrfBean crf;
        // CrfVersion version;
        FormLayout formLayout;
        for (ODMcomplexTypeDefinitionFormDef odmFormDef : odmMetadataVersions.get(0).getFormDef()) {
            crf = getCrfDao().findByOcOID(odmFormDef.getOID());

            if (crf == null) {
                crf = new CrfBean();
                crf.setOcOid(odmFormDef.getOID());
                crf = getCrfDao().saveOrUpdate(populateCrf(odmFormDef, userAccount, crf, study));
            } else {
                crf = getCrfDao().saveOrUpdate(updateCrf(odmFormDef, userAccount, crf, study));
            }
            formLayout = saveOrUpdateCrfVersion(userAccount, crf, odmFormDef, fmCrfs);
            if (formLayout.getXform() != null) {
                try {
                    parseCrfVersion(crf, formLayout, study, userAccount);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private FormLayout saveOrUpdateCrfVersion(UserAccount userAccount, CrfBean crf, ODMcomplexTypeDefinitionFormDef odmFormDef, Crf[] fmCrfs) {
        // CrfVersion crfVersion = null;
        FormLayout formLayout = null;
        List<OCodmComplexTypeDefinitionFormLayoutDef> formLayoutDefs = odmFormDef.getFormLayoutDef();
        for (OCodmComplexTypeDefinitionFormLayoutDef formLayoutDef : formLayoutDefs) {
            formLayout = getFormLayoutDao().findByOcOID(formLayoutDef.getOID());
            String url = formLayoutDef.getURL();

            PopulateCrfVersionParameter populateCrfVersionParameter = new PopulateCrfVersionParameter();
            populateCrfVersionParameter.setCrf(crf);
            populateCrfVersionParameter.setFmCrfs(fmCrfs);
            populateCrfVersionParameter.setOdmFormDef(odmFormDef);
            populateCrfVersionParameter.setUserAccount(userAccount);
            populateCrfVersionParameter.setUrl(url);

            if (formLayout == null) {
                formLayout = new FormLayout();
                formLayout.setOcOid(formLayoutDef.getOID());
                populateCrfVersionParameter.setFormLayout(formLayout);
                formLayout = getFormLayoutDao().saveOrUpdate(populateCrfVersion(new PopulateCrfVersionParameter(populateCrfVersionParameter)));
            } else {
                populateCrfVersionParameter.setFormLayout(formLayout);
                formLayout = getFormLayoutDao().saveOrUpdate(updateCrfVersion(new PopulateCrfVersionParameter(populateCrfVersionParameter)));
            }
        }
        return formLayout;
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
        surId.setRoleName(Role.STUDYDIRECTOR.getName());
        surId.setStudyId(study.getStudyId());
        surId.setStatusId(1);
        surId.setOwnerId(userAccount.getUserId());
        surId.setDateCreated(new Date());
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
        // studyEventDefinition.setType(odmStudyEventDef.getType().value());
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

    private CrfBean populateCrf(ODMcomplexTypeDefinitionFormDef odmFormDef, UserAccount userAccount, CrfBean crf, Study study) {
        crf.setStudy(study);
        crf.setName(odmFormDef.getName());
        crf.setUserAccount(userAccount);
        crf.setStatus(org.akaza.openclinica.domain.Status.AVAILABLE);
        return crf;
    }

    private CrfBean updateCrf(ODMcomplexTypeDefinitionFormDef odmFormDef, UserAccount userAccount, CrfBean crf, Study study) {
        crf = populateCrf(odmFormDef, userAccount, crf, study);
        crf.setUpdateId(userAccount.getUserId());
        crf.setDateUpdated(new Date());
        return crf;
    }

    private FormLayout populateCrfVersion(PopulateCrfVersionParameter paramObj) {
        for (Crf fmCrf : paramObj.getFmCrfs()) {
            if (fmCrf.getOcoid().equals(paramObj.getCrf().getOcOid())) {
                for (Version version : fmCrf.getVersions()) {
                    if (version.getOcoid().equals(paramObj.getFormLayout().getOcOid())) {
                        paramObj.getFormLayout().setDescription("Description");
                        paramObj.getFormLayout().setRevisionNotes("Revision");
                        paramObj.getFormLayout().setName(version.getName());
                        if (version.getFileLinks() != null) {
                            List<FileItem> fileItems = new ArrayList<>();
                            for (String fileLink : version.getFileLinks()) {
                                if (fileLink.endsWith(".xml")) {
                                    paramObj.getFormLayout().setXform(getXFormFromFormManager(fileLink));
                                    paramObj.getFormLayout().setXformName("default");
                                }
                                FileItem fileItem = getMediaFileItemFromFormManager(fileLink, paramObj.getCrf(), paramObj.getFormLayout());
                                fileItems.add(fileItem);
                            }
                            paramObj.getFormLayout().setFileItems(fileItems);
                        }
                    }
                }
            }
        }
        paramObj.getFormLayout().setCrf(paramObj.getCrf());
        paramObj.getFormLayout().setUrl(paramObj.getUrl());
        paramObj.getFormLayout().setUserAccount(paramObj.getUserAccount());
        paramObj.getFormLayout().setStatus(org.akaza.openclinica.domain.Status.AVAILABLE);
        return paramObj.getFormLayout();
    }

    private FormLayout updateCrfVersion(PopulateCrfVersionParameter paramObj) {
        FormLayout formLayout = paramObj.getFormLayout();
        formLayout = populateCrfVersion(new PopulateCrfVersionParameter(paramObj));
        formLayout.setUpdateId(paramObj.getUserAccount().getUserId());
        formLayout.setDateUpdated(new Date());
        return formLayout;
    }

    private UserAccount getCurrentUser() {
        UserAccount ub = getUserAccountDao().findById(1);
        return ub;
    }

    private EventDefinitionCrf populateEventDefinitionCrf(PopulateEventDefinitionCrfParameter paramObj) {
        paramObj.getEventDefinitionCrf().setStudy(paramObj.getStudy());
        paramObj.getEventDefinitionCrf().setStudyEventDefinition(paramObj.getStudyEventDefinition());
        paramObj.getEventDefinitionCrf().setCrf(paramObj.getCrf());
        paramObj.getEventDefinitionCrf().setStatusId(org.akaza.openclinica.domain.Status.AVAILABLE.getCode());
        paramObj.getEventDefinitionCrf().setUserAccount(paramObj.getUserAccount());
        paramObj.getEventDefinitionCrf().setCrfVersion(paramObj.getCrfVersion());
        setConfigurationProperties(paramObj.getConf(), paramObj.getEventDefinitionCrf());
        if (paramObj.getOdmFormRef().getMandatory().equals(YesOrNo.YES)) {
            paramObj.getEventDefinitionCrf().setRequiredCrf(true);
        } else {
            paramObj.getEventDefinitionCrf().setRequiredCrf(false);
        }
        return paramObj.getEventDefinitionCrf();
    }

    private EventDefinitionCrf updateEventDefinitionCrf(PopulateEventDefinitionCrfParameter paramObj) {
        EventDefinitionCrf eventDefinitionCrf = paramObj.getEventDefinitionCrf();
        eventDefinitionCrf = populateEventDefinitionCrf(new PopulateEventDefinitionCrfParameter(paramObj));
        eventDefinitionCrf.setUpdateId(paramObj.getUserAccount().getUserId());
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
        return eventDefinitionCrf;
    }

    private EventDefinitionCrfTag populateEDCTag(PopulateEDCTagParameter paramObj) {
        if (paramObj.getConf().getOffline().equalsIgnoreCase("Yes")) {
            paramObj.getEventDefinitionCrfTag().setActive(true);
        } else {
            paramObj.getEventDefinitionCrfTag().setActive(false);
        }
        paramObj.getEventDefinitionCrfTag().setTagId(paramObj.getTagId());
        paramObj.getEventDefinitionCrfTag().setPath(paramObj.getCrfPath());
        paramObj.getEventDefinitionCrfTag().setDateCreated(new Date());
        paramObj.getEventDefinitionCrfTag().setUserAccount(paramObj.getUserAccount());
        return paramObj.getEventDefinitionCrfTag();
    }

    private EventDefinitionCrfTag updateEDCTag(PopulateEDCTagParameter paramObj) {
        EventDefinitionCrfTag eventDefinitionCrfTag = paramObj.getEventDefinitionCrfTag();
        eventDefinitionCrfTag = populateEDCTag(new PopulateEDCTagParameter(paramObj));
        eventDefinitionCrfTag.setUpdateId(paramObj.getUserAccount().getUserId());
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

    public Crf[] getCrfsFromFormManager(Study study) {
        String protocolId = study.getUniqueIdentifier();
        String url = "http://fm.openclinica.info:8080/api/protocol/" + protocolId + "/forms";
        RestTemplate restTemplate = new RestTemplate();
        Crf[] crfs = null;
        try {
            crfs = (Crf[]) restTemplate.getForObject(url, Crf[].class);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        return crfs;
    }

    public FileItem getMediaFileItemFromFormManager(String fileLink, CrfBean crf, FormLayout formLayout) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(fileLink, HttpMethod.GET, entity, byte[].class, "1");
        FileItem fileItem = null;
        if (response.getStatusCode() == HttpStatus.OK) {
            String fileName = "";
            FileOutputStream output = null;
            try {
                String disposition = response.getHeaders().get("Content-Disposition").get(0);
                fileName = disposition.replaceFirst("(?i)^.*filename=\"([^\"]+)\".*$", "$1");
                String dir = Utils.getCrfMediaFilePath(crf, formLayout);
                if (!new File(dir).exists()) {
                    new File(dir).mkdirs();
                    logger.debug("Made the directory " + dir);
                }
                File file = new File(dir + fileName);
                output = new FileOutputStream(file);
                IOUtils.write(response.getBody(), output);
                fileItem = new DiskFileItem("media_file", response.getHeaders().get("Content-Type").get(0), false, fileName, 100000000, file);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (output != null) {
                        output.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return fileItem;

    }

    public File getMediaFilesFromFormManagerOrig(String fileLink) {
        File file = null;
        RestTemplate restTemplate = new RestTemplate();
        try {
            file = restTemplate.getForObject(fileLink, File.class);
        } catch (Exception e) {
            logger.info(e.getMessage());

        }
        return file;
    }

    public String getXFormFromFormManager(String fileLink) {
        String xform = "";
        RestTemplate restTemplate = new RestTemplate();
        try {
            xform = restTemplate.getForObject(fileLink, String.class);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        return xform;
    }

    private void parseCrfVersion(CrfBean crf, FormLayout formLayout, Study study, UserAccount userAccount) throws Exception {
        String submittedCrfName = crf.getName();
        String submittedCrfVersionName = formLayout.getName();
        String submittedCrfVersionDescription = formLayout.getDescription();
        String submittedRevisionNotes = formLayout.getRevisionNotes();
        String submittedXformText = formLayout.getXform();

        // Create container for holding validation errors
        DataBinder dataBinder = new DataBinder(new CrfVersion());
        Errors errors = dataBinder.getBindingResult();

        // Validate all upload form fields were populated
        // CRFVersionBean crfVersion = new CRFVersionBean();
        FormLayoutBean formLayoutBean = new FormLayoutBean();
        formLayoutBean.setOid(formLayout.getOcOid());
        formLayoutBean.setCrfId(formLayout.getCrf().getCrfId());
        ValidateFormFieldsParameter validateFormFieldsParameter = new ValidateFormFieldsParameter();
        validateFormFieldsParameter.setErrors(errors);
        validateFormFieldsParameter.setSubmittedCrfName(submittedCrfName);
        validateFormFieldsParameter.setSubmittedCrfVersionDescription(submittedCrfVersionDescription);
        validateFormFieldsParameter.setSubmittedCrfVersionName(submittedCrfVersionName);
        validateFormFieldsParameter.setSubmittedRevisionNotes(submittedRevisionNotes);
        validateFormFieldsParameter.setSubmittedXformText(submittedXformText);
        validateFormFieldsParameter.setFormLayoutBean(formLayoutBean);

        validateFormFields(validateFormFieldsParameter);

        if (!errors.hasErrors()) {
            // Parse instance and xform
            XformContainer container = parseInstance(submittedXformText);
            Html html = xformParser.unMarshall(submittedXformText);
            List<FileItem> items = formLayout.getFileItems();

            StudyBean currentStudy = new StudyBean();
            currentStudy.setId(study.getStudyId());

            UserAccountBean ub = new UserAccountBean();
            ub.setId(userAccount.getUserId());
            ub.setActiveStudyId(currentStudy.getId());

            // Save meta-data in database

            // Below section requires change to accommodate multiple crf versions
            try {
                // xformService.createCRFMetaData(crfVersion, container, currentStudy, ub, html, submittedCrfName,
                // submittedCrfVersionName,
                // submittedCrfVersionDescription, submittedRevisionNotes, submittedXformText, items, errors);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private XformContainer parseInstance(String xform) throws Exception {

        // Could use the following xpath to get all leaf nodes in the case
        // of multiple levels of groups: //*[count(./*) = 0]
        // For now will assume a structure of /form/item or /form/group/item
        Document doc = null;
        try {
            InputStream stream = new ByteArrayInputStream(xform.getBytes(StandardCharsets.UTF_8));
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);

            NodeList instances = doc.getElementsByTagName("instance");

            // All whitespace outside tags gets parsed as Text objects and returned
            // by the various Node methods. We need to ignore these and
            // focus on actual Elements

            Element instance = null;
            // List<XformItem> items = new ArrayList<XformItem>();
            List<XformGroup> groups = new ArrayList<XformGroup>();

            // Get the primary instance
            for (int i = 0; i < instances.getLength(); i++) {
                Element curInstance = (Element) instances.item(i);
                if (curInstance instanceof Element) {
                    instance = curInstance;
                    break;
                }
            }

            // Get the form element
            Element form = null;
            for (int i = 0; i < instance.getChildNodes().getLength(); i++) {
                Node curNode = instance.getChildNodes().item(i);
                if (curNode instanceof Element) {
                    form = (Element) curNode;
                    break;
                }
            }

            // Get the groups and grouped items
            for (int i = 0; i < form.getChildNodes().getLength(); i++) {
                if (form.getChildNodes().item(i) instanceof Element && ((Element) form.getChildNodes().item(i)).hasChildNodes()
                        && !((Element) form.getChildNodes().item(i)).getTagName().equals("meta")) {
                    Element group = (Element) form.getChildNodes().item(i);
                    XformGroup newGroup = new XformGroup();
                    newGroup.setGroupName(group.getTagName());
                    newGroup.setGroupPath("/" + form.getTagName() + "/" + group.getTagName());
                    groups.add(newGroup);
                    for (int j = 0; j < group.getChildNodes().getLength(); j++) {
                        if (group.getChildNodes().item(j) instanceof Element) {
                            Element item = (Element) group.getChildNodes().item(j);
                            XformItem newItem = new XformItem();
                            newItem.setItemPath("/" + form.getTagName() + "/" + group.getTagName() + "/" + item.getTagName());
                            newItem.setItemName(item.getTagName());
                            // group is null;
                            newGroup.getItems().add(newItem);
                        }
                    }
                }
            }
            XformContainer container = new XformContainer();
            container.setGroups(groups);
            container.setInstanceName(form.getTagName());
            return container;
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new Exception(e);
        }
    }

    private void validateFormFields(ValidateFormFieldsParameter paramObj) {

        // Verify CRF Name is populated
        if (paramObj.getVersion().getCrfId() == 0 && (paramObj.getSubmittedCrfName() == null || paramObj.getSubmittedCrfName().equals(""))) {
            DataBinder crfDataBinder = new DataBinder(new CrfBean());
            Errors crfErrors = crfDataBinder.getBindingResult();
            crfErrors.rejectValue("name", "crf_val_crf_name_blank", "CRF Name");
            paramObj.getErrors().addAllErrors(crfErrors);
        }

        DataBinder crfVersionDataBinder = new DataBinder(new CrfVersion());
        Errors crfVersionErrors = crfVersionDataBinder.getBindingResult();

        // Verify CRF Version Name is populated
        if (paramObj.getSubmittedCrfVersionName() == null || paramObj.getSubmittedCrfVersionName().equals("")) {
            crfVersionErrors.rejectValue("name", "crf_ver_val_name_blank", "Version Name");
        }

        // Verify CRF Version Description is populated
        if (paramObj.getSubmittedCrfVersionDescription() == null || paramObj.getSubmittedCrfVersionDescription().equals("")) {
            crfVersionErrors.rejectValue("description", "crf_ver_val_desc_blank", "Version Description");
        }

        // Verify CRF Version Revision Notes is populated
        if (paramObj.getSubmittedRevisionNotes() == null || paramObj.getSubmittedRevisionNotes().equals("")) {
            crfVersionErrors.rejectValue("revisionNotes", "crf_ver_val_rev_notes_blank", "Revision Notes");
        }

        // Verify Xform text is populated
        if (paramObj.getSubmittedXformText() == null || paramObj.getSubmittedXformText().equals("")) {
            crfVersionErrors.rejectValue("xform", "crf_ver_val_xform_blank", "Xform");
        }
        paramObj.getErrors().addAllErrors(crfVersionErrors);
    }

    public FormLayoutDao getFormLayoutDao() {
        return formLayoutDao;
    }

    public void setFormLayoutDao(FormLayoutDao formLayoutDao) {
        this.formLayoutDao = formLayoutDao;
    }

}
