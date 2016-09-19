package org.akaza.openclinica.service;

import java.util.Date;
import java.util.List;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.dao.hibernate.CrfDao;
import org.akaza.openclinica.dao.hibernate.CrfVersionDao;
import org.akaza.openclinica.dao.hibernate.EventDefinitionCrfDao;
import org.akaza.openclinica.dao.hibernate.EventDefinitionCrfTagDao;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDefinitionDao;
import org.akaza.openclinica.dao.hibernate.StudyUserRoleDao;
import org.akaza.openclinica.dao.hibernate.UserAccountDao;
import org.akaza.openclinica.domain.datamap.CrfBean;
import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrfTag;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import org.akaza.openclinica.domain.datamap.StudyUserRole;
import org.akaza.openclinica.domain.datamap.StudyUserRoleId;
import org.akaza.openclinica.domain.user.UserAccount;
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
import org.springframework.transaction.annotation.Transactional;

public class OdmImportServiceImpl implements OdmImportService {

    private UserAccountDao userAccountDao;
    private StudyUserRoleDao studyUserRoleDao;
    private StudyEventDefinitionDao studyEventDefDao;
    private EventDefinitionCrfDao eventDefinitionCrfDao;
    private CrfDao crfDao;
    private CrfVersionDao crfVersionDao;
    private StudyDao studyDao;
    private EventDefinitionCrfTagDao eventDefinitionCrfTagDao;

    @Transactional
    public void importOdmToOC(org.cdisc.ns.odm.v130_sb.ODM odm) {

        UserAccount userAccount = getCurrentUser();
        // TODO add validation to all entities
        ODMcomplexTypeDefinitionStudy odmStudy = odm.getStudy().get(0);
        Study study = saveOrUpdateStudy(odm, userAccount, odmStudy);

        StudyUserRole studyUserRole = null;
        StudyUserRoleId studyUserRoleId = null;

        List<StudyUserRole> surRoles = getStudyUserRoleDao().findAllUserRolesByUserAccount(userAccount, study.getStudyId(), study.getStudyId());
        if (surRoles.size() == 0) {
            studyUserRoleId = new StudyUserRoleId();
            studyUserRole = new StudyUserRole();
            studyUserRole = getStudyUserRoleDao().saveOrUpdate(populateUserRole(study, userAccount, studyUserRole, studyUserRoleId));
        } else {
            studyUserRole = surRoles.get(0);
            studyUserRoleId = studyUserRole.getId();
            studyUserRole = getStudyUserRoleDao().saveOrUpdate(updateUserRole(study, userAccount, studyUserRole, studyUserRoleId));
        }

        StudyEventDefinition studyEventDefinition = null;
        List<ODMcomplexTypeDefinitionMetaDataVersion> odmMetadataVersions = odmStudy.getMetaDataVersion();
        List<ODMcomplexTypeDefinitionStudyEventDef> odmStudyEventDefs = saveOrUpdateEvent(userAccount, study, odmMetadataVersions);

        CrfBean crf = null;
        CrfVersion crfVersion = null;
        saveOrUpdateCrf(userAccount, study, odmMetadataVersions);
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

    private void saveOrUpdateCrf(UserAccount userAccount, Study study, List<ODMcomplexTypeDefinitionMetaDataVersion> odmMetadataVersions) {
        CrfBean crf;
        for (ODMcomplexTypeDefinitionFormDef odmFormDef : odmMetadataVersions.get(0).getFormDef()) {
            crf = getCrfDao().findByOcOID(odmFormDef.getOID());

            if (crf == null) {
                crf = new CrfBean();
                crf.setOcOid(odmFormDef.getOID());
                crf = getCrfDao().saveOrUpdate(populateCrf(odmFormDef, userAccount, crf, study));
            } else {
                crf = getCrfDao().saveOrUpdate(updateCrf(odmFormDef, userAccount, crf, study));
            }
            saveOrUpdateCrfVersion(userAccount, crf, odmFormDef);
        }
    }

    private void saveOrUpdateCrfVersion(UserAccount userAccount, CrfBean crf, ODMcomplexTypeDefinitionFormDef odmFormDef) {
        CrfVersion crfVersion;
        List<OCodmComplexTypeDefinitionFormLayoutDef> formLayoutDefs = odmFormDef.getFormLayoutDef();
        for (OCodmComplexTypeDefinitionFormLayoutDef formLayoutDef : formLayoutDefs) {

            crfVersion = getCrfVersionDao().findByOcOID(formLayoutDef.getOID());
            String url = formLayoutDef.getURL();
            if (crfVersion == null) {
                crfVersion = new CrfVersion();
                crfVersion.setOcOid(formLayoutDef.getOID());
                crfVersion = getCrfVersionDao().saveOrUpdate(populateCrfVersion(odmFormDef, userAccount, crfVersion, crf, url));
            } else {
                crfVersion = getCrfVersionDao().saveOrUpdate(updateCrfVersion(odmFormDef, userAccount, crfVersion, crf, url));
            }
        }
    }

    private List<ODMcomplexTypeDefinitionStudyEventDef> saveOrUpdateEvent(UserAccount userAccount, Study study,
            List<ODMcomplexTypeDefinitionMetaDataVersion> odmMetadataVersions) {
        StudyEventDefinition studyEventDefinition;
        List<ODMcomplexTypeDefinitionStudyEventDef> odmStudyEventDefs = odmMetadataVersions.get(0).getStudyEventDef();
        for (ODMcomplexTypeDefinitionStudyEventDef odmStudyEventDef : odmStudyEventDefs) {

            studyEventDefinition = getStudyEventDefDao().findByOcOID(odmStudyEventDef.getOID());
            if (studyEventDefinition == null) {
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

        if (study == null) {
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
        study.setSummary(odmGlobalVariables.getStudyDescription().getValue());
        study.setStatus(org.akaza.openclinica.domain.Status.PENDING);
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
        surId.setRoleName(Role.STUDYDIRECTOR.toString());
        surId.setStudyId(study.getStudyId());
        surId.setStatusId(1);
        surId.setOwnerId(userAccount.getUserId());
        surId.setDateCreated(new Date());
        sur.setId(surId);
        return sur;
    }

    private StudyUserRole updateUserRole(Study study, UserAccount userAccount, StudyUserRole sur, StudyUserRoleId surId) {
        sur = populateUserRole(study, userAccount, sur, surId);
        surId.setUpdateId(userAccount.getUserId());
        surId.setDateUpdated(new Date());
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
        studyEventDefinition.setType(odmStudyEventDef.getType().value());
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

    private CrfVersion populateCrfVersion(ODMcomplexTypeDefinitionFormDef odmFormDef, UserAccount userAccount, CrfVersion crfVersion, CrfBean crf, String url) {
        crfVersion.setCrf(crf);
        crfVersion.setUrl(url);
        crfVersion.setUserAccount(userAccount);
        crfVersion.setStatus(org.akaza.openclinica.domain.Status.AVAILABLE);
        return crfVersion;
    }

    private CrfVersion updateCrfVersion(ODMcomplexTypeDefinitionFormDef odmFormDef, UserAccount userAccount, CrfVersion crfVersion, CrfBean crf, String url) {
        crfVersion = populateCrfVersion(odmFormDef, userAccount, crfVersion, crf, url);
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

}
