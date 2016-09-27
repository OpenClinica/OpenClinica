package org.akaza.openclinica.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilderFactory;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.dao.hibernate.CrfDao;
import org.akaza.openclinica.dao.hibernate.CrfVersionDao;
import org.akaza.openclinica.dao.hibernate.EventDefinitionCrfDao;
import org.akaza.openclinica.dao.hibernate.EventDefinitionCrfTagDao;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDefinitionDao;
import org.akaza.openclinica.dao.hibernate.StudyUserRoleDao;
import org.akaza.openclinica.dao.hibernate.UserAccountDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.domain.datamap.CrfBean;
import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrfTag;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import org.akaza.openclinica.domain.datamap.StudyUserRole;
import org.akaza.openclinica.domain.datamap.StudyUserRoleId;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.domain.xform.XformContainer;
import org.akaza.openclinica.domain.xform.XformGroup;
import org.akaza.openclinica.domain.xform.XformItem;
import org.akaza.openclinica.domain.xform.XformParser;
import org.akaza.openclinica.domain.xform.dto.Html;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.crfdata.XformMetaDataService;
import org.akaza.openclinica.service.dto.Crf;
import org.akaza.openclinica.service.dto.Version;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.cdisc.ns.odm.v130_sb.ODMcomplexTypeDefinitionFormDef;
import org.cdisc.ns.odm.v130_sb.ODMcomplexTypeDefinitionFormRef;
import org.cdisc.ns.odm.v130_sb.ODMcomplexTypeDefinitionGlobalVariables;
import org.cdisc.ns.odm.v130_sb.ODMcomplexTypeDefinitionMetaDataVersion;
import org.cdisc.ns.odm.v130_sb.ODMcomplexTypeDefinitionStudy;
import org.cdisc.ns.odm.v130_sb.ODMcomplexTypeDefinitionStudyEventDef;
import org.cdisc.ns.odm.v130_sb.ODMcomplexTypeDefinitionStudyEventRef;
import org.openclinica.ns.odm_ext_v130.v31_sb.OCodmComplexTypeDefinitionConfigurationParameters;
import org.openclinica.ns.odm_ext_v130.v31_sb.OCodmComplexTypeDefinitionEventDefinitionDetails;
import org.openclinica.ns.odm_ext_v130.v31_sb.OCodmComplexTypeDefinitionFormLayoutDef;
import org.openclinica.ns.odm_ext_v130.v31_sb.OCodmComplexTypeDefinitionFormLayoutRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private StudyDao studyDao;
    private EventDefinitionCrfTagDao eventDefinitionCrfTagDao;
    private DataSource dataSource;
    @Autowired
    private XformParser xformParser;

    @Autowired
    private XformMetaDataService xformService;

    ResourceBundle resword = ResourceBundleProvider.getWordsBundle();
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public OdmImportServiceImpl(DataSource dataSource) {
        super();
        this.dataSource = dataSource;
    }

    @Transactional
    public void importOdmToOC(org.cdisc.ns.odm.v130_sb.ODM odm) {

        UserAccount userAccount = getCurrentUser();
        // TODO add validation to all entities
        ODMcomplexTypeDefinitionStudy odmStudy = odm.getStudy().get(0);
        Study study = saveOrUpdateStudy(odm, userAccount, odmStudy);

        ParticipantPortalRegistrar portal = new ParticipantPortalRegistrar();
        String str = portal.registerStudy(study.getOc_oid(), study.getOc_oid(), study.getName());

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
        CrfVersion crfVersion = null;
        saveOrUpdateCrf(userAccount, study, odmMetadataVersions, fmCrfs);
        List<ODMcomplexTypeDefinitionStudyEventRef> odmStudyEventRefs = odmMetadataVersions.get(0).getProtocol().getStudyEventRef();
        for (ODMcomplexTypeDefinitionStudyEventRef odmStudyEventRef : odmStudyEventRefs) {
            for (ODMcomplexTypeDefinitionStudyEventDef odmStudyEventDef : odmStudyEventDefs) {
                if (odmStudyEventDef.getOID().equals(odmStudyEventRef.getStudyEventOID())) {
                    studyEventDefinition = getStudyEventDefDao().findByOcOID(odmStudyEventDef.getOID());
                    studyEventDefinition.setOrdinal(odmStudyEventRef.getOrderNumber().intValue());
                    studyEventDefinition = getStudyEventDefDao().saveOrUpdate(studyEventDefinition);

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

                        crfVersion = getCrfVersionDao().findByOcOID(defaultVersionOid);
                        eventDefinitionCrf = saveOrUpdateEventDefnCrf(userAccount, study, studyEventDefinition, crf, crfVersion, eventDefinitionCrf, conf);
                        saveOrUpdateEventDefnCrfTag(userAccount, studyEventDefinition, crf, eventDefinitionCrf, conf);
                    }
                }
            }

        }

    }

    private void saveOrUpdateEventDefnCrfTag(UserAccount userAccount, StudyEventDefinition studyEventDefinition, CrfBean crf,
            EventDefinitionCrf eventDefinitionCrf, OCodmComplexTypeDefinitionConfigurationParameters conf) {
        EventDefinitionCrfTag eventDefinitionCrfTag;
        int tagId = 2; // Offline
        String crfPath = studyEventDefinition.getOc_oid() + "." + crf.getOcOid();
        eventDefinitionCrfTag = getEventDefinitionCrfTagDao().findByCrfPathAndTagId(tagId, crfPath);
        if (eventDefinitionCrfTag == null) {
            eventDefinitionCrfTag = new EventDefinitionCrfTag();
            eventDefinitionCrfTag = getEventDefinitionCrfTagDao()
                    .saveOrUpdate(populateEDCTag(eventDefinitionCrf, userAccount, conf, tagId, crfPath, eventDefinitionCrfTag));
        } else {
            eventDefinitionCrfTag = getEventDefinitionCrfTagDao()
                    .saveOrUpdate(updateEDCTag(eventDefinitionCrf, userAccount, conf, tagId, crfPath, eventDefinitionCrfTag));
        }
    }

    private EventDefinitionCrf saveOrUpdateEventDefnCrf(UserAccount userAccount, Study study, StudyEventDefinition studyEventDefinition, CrfBean crf,
            CrfVersion crfVersion, EventDefinitionCrf eventDefinitionCrf, OCodmComplexTypeDefinitionConfigurationParameters conf) {
        if (eventDefinitionCrf == null) {
            eventDefinitionCrf = new EventDefinitionCrf();
            eventDefinitionCrf = getEventDefinitionCrfDao()
                    .saveOrUpdate(populateEventDefinitionCrf(eventDefinitionCrf, userAccount, crf, crfVersion, conf, study, studyEventDefinition));
        } else {
            eventDefinitionCrf = getEventDefinitionCrfDao()
                    .saveOrUpdate(updateEventDefinitionCrf(eventDefinitionCrf, userAccount, crf, crfVersion, conf, study, studyEventDefinition));
        }
        return eventDefinitionCrf;
    }

    private void saveOrUpdateCrf(UserAccount userAccount, Study study, List<ODMcomplexTypeDefinitionMetaDataVersion> odmMetadataVersions, Crf[] fmCrfs) {
        CrfBean crf;
        CrfVersion version;
        for (ODMcomplexTypeDefinitionFormDef odmFormDef : odmMetadataVersions.get(0).getFormDef()) {
            crf = getCrfDao().findByOcOID(odmFormDef.getOID());

            if (crf == null) {
                crf = new CrfBean();
                crf.setOcOid(odmFormDef.getOID());
                crf = getCrfDao().saveOrUpdate(populateCrf(odmFormDef, userAccount, crf, study));
            } else {
                crf = getCrfDao().saveOrUpdate(updateCrf(odmFormDef, userAccount, crf, study));
            }
            version = saveOrUpdateCrfVersion(userAccount, crf, odmFormDef, fmCrfs);
            if (version.getXform() != null) {
                try {
                    parseCrfVersion(crf, version, study, userAccount);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    private CrfVersion saveOrUpdateCrfVersion(UserAccount userAccount, CrfBean crf, ODMcomplexTypeDefinitionFormDef odmFormDef, Crf[] fmCrfs) {
        CrfVersion crfVersion = null;
        List<OCodmComplexTypeDefinitionFormLayoutDef> formLayoutDefs = odmFormDef.getFormLayoutDef();
        for (OCodmComplexTypeDefinitionFormLayoutDef formLayoutDef : formLayoutDefs) {

            crfVersion = getCrfVersionDao().findByOcOID(formLayoutDef.getOID());
            String url = formLayoutDef.getURL();
            if (crfVersion == null) {
                crfVersion = new CrfVersion();
                crfVersion.setOcOid(formLayoutDef.getOID());
                crfVersion = getCrfVersionDao().saveOrUpdate(populateCrfVersion(odmFormDef, userAccount, crfVersion, crf, url, fmCrfs));
            } else {
                crfVersion = getCrfVersionDao().saveOrUpdate(updateCrfVersion(odmFormDef, userAccount, crfVersion, crf, url, fmCrfs));
            }
        }
        return crfVersion;
    }

    private List<ODMcomplexTypeDefinitionStudyEventDef> saveOrUpdateEvent(UserAccount userAccount, Study study,
            List<ODMcomplexTypeDefinitionMetaDataVersion> odmMetadataVersions) {
        StudyEventDefinition studyEventDefinition;
        List<ODMcomplexTypeDefinitionStudyEventDef> odmStudyEventDefs = odmMetadataVersions.get(0).getStudyEventDef();
        for (ODMcomplexTypeDefinitionStudyEventDef odmStudyEventDef : odmStudyEventDefs) {

            studyEventDefinition = getStudyEventDefDao().findByOcOID(odmStudyEventDef.getOID());
            if (studyEventDefinition == null || studyEventDefinition.getStudyEventDefinitionId() == 0) {
                studyEventDefinition = new StudyEventDefinition();
                studyEventDefinition.setOc_oid(odmStudyEventDef.getOID());
                studyEventDefinition = getStudyEventDefDao().saveOrUpdate(populateEvent(odmStudyEventDef, userAccount, studyEventDefinition, study));
            } else {
                studyEventDefinition = getStudyEventDefDao().saveOrUpdate(updateEvent(odmStudyEventDef, userAccount, studyEventDefinition, study));
            }
        }
        return odmStudyEventDefs;
    }

    private Study saveOrUpdateStudy(org.cdisc.ns.odm.v130_sb.ODM odm, UserAccount userAccount, ODMcomplexTypeDefinitionStudy odmStudy) {
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

    private CrfVersion populateCrfVersion(ODMcomplexTypeDefinitionFormDef odmFormDef, UserAccount userAccount, CrfVersion crfVersion, CrfBean crf, String url,
            Crf[] fmCrfs) {
        for (Crf fmCrf : fmCrfs) {
            if (fmCrf.getOcoid().equals(crf.getOcOid())) {
                for (Version version : fmCrf.getVersions()) {
                    if (version.getOcoid().equals(crfVersion.getOcOid())) {
                        crfVersion.setDescription(version.getDescription());
                        crfVersion.setName(version.getName());
                        // crfVersion.setXform(version.get);
                    }
                }
            }
        }
        crfVersion.setCrf(crf);
        crfVersion.setUrl(url);
        crfVersion.setUserAccount(userAccount);
        crfVersion.setStatus(org.akaza.openclinica.domain.Status.AVAILABLE);
        return crfVersion;
    }

    private CrfVersion updateCrfVersion(ODMcomplexTypeDefinitionFormDef odmFormDef, UserAccount userAccount, CrfVersion crfVersion, CrfBean crf, String url,
            Crf[] fmCrfs) {
        crfVersion = populateCrfVersion(odmFormDef, userAccount, crfVersion, crf, url, fmCrfs);
        crfVersion.setUpdateId(userAccount.getUserId());
        crfVersion.setDateUpdated(new Date());
        return crfVersion;
    }

    private UserAccount getCurrentUser() {
        UserAccount ub = getUserAccountDao().findById(1);
        return ub;
    }

    private EventDefinitionCrf populateEventDefinitionCrf(EventDefinitionCrf eventDefinitionCrf, UserAccount userAccount, CrfBean crf, CrfVersion crfVersion,
            OCodmComplexTypeDefinitionConfigurationParameters conf, Study study, StudyEventDefinition studyEventDefinition) {
        eventDefinitionCrf.setStudy(study);
        eventDefinitionCrf.setStudyEventDefinition(studyEventDefinition);
        eventDefinitionCrf.setCrf(crf);
        eventDefinitionCrf.setStatusId(org.akaza.openclinica.domain.Status.AVAILABLE.getCode());
        eventDefinitionCrf.setUserAccount(userAccount);
        eventDefinitionCrf.setCrfVersion(crfVersion);
        setConfigurationProperties(conf, eventDefinitionCrf);
        return eventDefinitionCrf;
    }

    private EventDefinitionCrf updateEventDefinitionCrf(EventDefinitionCrf eventDefinitionCrf, UserAccount userAccount, CrfBean crf, CrfVersion crfVersion,
            OCodmComplexTypeDefinitionConfigurationParameters conf, Study study, StudyEventDefinition studyEventDefinition) {
        eventDefinitionCrf = populateEventDefinitionCrf(eventDefinitionCrf, userAccount, crf, crfVersion, conf, study, studyEventDefinition);
        eventDefinitionCrf.setUpdateId(userAccount.getUserId());
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

    private EventDefinitionCrfTag populateEDCTag(EventDefinitionCrf eventDefinitionCrf, UserAccount userAccount,
            OCodmComplexTypeDefinitionConfigurationParameters conf, int tagId, String crfPath, EventDefinitionCrfTag eventDefinitionCrfTag) {
        if (conf.getOffline().equalsIgnoreCase("Yes")) {
            eventDefinitionCrfTag.setActive(true);
        } else {
            eventDefinitionCrfTag.setActive(false);
        }
        eventDefinitionCrfTag.setTagId(tagId);
        eventDefinitionCrfTag.setPath(crfPath);
        eventDefinitionCrfTag.setDateCreated(new Date());
        eventDefinitionCrfTag.setUserAccount(userAccount);
        return eventDefinitionCrfTag;
    }

    private EventDefinitionCrfTag updateEDCTag(EventDefinitionCrf eventDefinitionCrf, UserAccount userAccount,
            OCodmComplexTypeDefinitionConfigurationParameters conf, int tagId, String crfPath, EventDefinitionCrfTag eventDefinitionCrfTag) {
        eventDefinitionCrfTag = populateEDCTag(eventDefinitionCrf, userAccount, conf, tagId, crfPath, eventDefinitionCrfTag);
        eventDefinitionCrfTag.setUpdateId(userAccount.getUserId());
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

    public Crf[] getCrfsFromFormManager(Study study) {
        String protocolId = study.getName().toLowerCase();
        String url = "http://159.203.83.212:8080/api/protocol/" + protocolId + "/forms";
        RestTemplate restTemplate = new RestTemplate();
        Crf[] crfs = null;
        try {
            crfs = (Crf[]) restTemplate.getForObject(url, Crf[].class);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return crfs;
    }

    @SuppressWarnings("rawtypes")
    private void parseCrfVersion(CrfBean crf, CrfVersion version, Study study, UserAccount userAccount) throws Exception {
        String submittedCrfName = crf.getName();
        String submittedCrfVersionName = version.getName();
        String submittedCrfVersionDescription = version.getDescription();
        String submittedRevisionNotes = version.getRevisionNotes();
        String submittedXformText = version.getXform();

        // Create container for holding validation errors
        DataBinder dataBinder = new DataBinder(new CrfVersion());
        Errors errors = dataBinder.getBindingResult();

        // Validate all upload form fields were populated
        CRFVersionDAO cvdao = new CRFVersionDAO<>(dataSource);
        CRFVersionBean crfVersion = (CRFVersionBean) cvdao.findByPK(version.getCrfVersionId());

        validateFormFields(errors, crfVersion, submittedCrfName, submittedCrfVersionName, submittedCrfVersionDescription, submittedRevisionNotes,
                submittedXformText);

        if (!errors.hasErrors()) {

            // Parse instance and xform
            XformContainer container = parseInstance(submittedXformText);
            Html html = xformParser.unMarshall(submittedXformText);
            List<FileItem> items = new ArrayList<>();
            StudyDAO sdao = new StudyDAO<>(dataSource);
            StudyBean currentStudy = (StudyBean) sdao.findByPK(study.getStudyId());
            UserAccountDAO udao = new UserAccountDAO(dataSource);
            UserAccountBean ub = (UserAccountBean) udao.findByPK(userAccount.getUserId());
            // Save meta-data in database

            try {
                xformService.createCRFMetaData(crfVersion, container, currentStudy, ub, html, submittedCrfName, submittedCrfVersionName,
                        submittedCrfVersionDescription, submittedRevisionNotes, submittedXformText, items, errors);
            } catch (Exception e) {
                // TODO Auto-generated catch block
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

    private void validateFormFields(Errors errors, CRFVersionBean version, String submittedCrfName, String submittedCrfVersionName,
            String submittedCrfVersionDescription, String submittedRevisionNotes, String submittedXformText) {

        // Verify CRF Name is populated
        if (version.getCrfId() == 0 && (submittedCrfName == null || submittedCrfName.equals(""))) {
            DataBinder crfDataBinder = new DataBinder(new CrfBean());
            Errors crfErrors = crfDataBinder.getBindingResult();
            crfErrors.rejectValue("name", "crf_val_crf_name_blank", resword.getString("CRF_name"));
            errors.addAllErrors(crfErrors);
        }

        DataBinder crfVersionDataBinder = new DataBinder(new CrfVersion());
        Errors crfVersionErrors = crfVersionDataBinder.getBindingResult();

        // Verify CRF Version Name is populated
        if (submittedCrfVersionName == null || submittedCrfVersionName.equals("")) {
            crfVersionErrors.rejectValue("name", "crf_ver_val_name_blank", resword.getString("version_name"));
        }

        // Verify CRF Version Description is populated
        if (submittedCrfVersionDescription == null || submittedCrfVersionDescription.equals("")) {
            crfVersionErrors.rejectValue("description", "crf_ver_val_desc_blank", resword.getString("crf_version_description"));
        }

        // Verify CRF Version Revision Notes is populated
        if (submittedRevisionNotes == null || submittedRevisionNotes.equals("")) {
            crfVersionErrors.rejectValue("revisionNotes", "crf_ver_val_rev_notes_blank", resword.getString("revision_notes"));
        }

        // Verify Xform text is populated
        if (submittedXformText == null || submittedXformText.equals("")) {
            crfVersionErrors.rejectValue("xform", "crf_ver_val_xform_blank", resword.getString("xform"));
        }
        errors.addAllErrors(crfVersionErrors);
    }

}
