package org.akaza.openclinica.controller;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.dao.hibernate.CrfDao;
import org.akaza.openclinica.dao.hibernate.CrfVersionDao;
import org.akaza.openclinica.dao.hibernate.EventDefinitionCrfDao;
import org.akaza.openclinica.dao.hibernate.EventDefinitionCrfTagDao;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDefinitionDao;
import org.akaza.openclinica.dao.hibernate.UserAccountDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.domain.datamap.CrfBean;
import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrfTag;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import org.akaza.openclinica.domain.user.UserAccount;
import org.apache.commons.dbcp.BasicDataSource;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/protocolversion")
public class OdmImportController {

    @Autowired
    @Qualifier("dataSource")
    private BasicDataSource dataSource;

    @Autowired
    UserAccountDao userAccountDao;

    @Autowired
    StudyEventDefinitionDao studyEventDefinitionDao;

    @Autowired
    EventDefinitionCrfDao eventDefinitionCrfDao;

    @Autowired
    private CrfDao crfDao;

    @Autowired
    private UserAccountDao userDao;

    @Autowired
    private CrfVersionDao crfVersionDao;

    @Autowired
    StudyDao studyDao;

    @Autowired
    EventDefinitionCrfTagDao eventDefinitionCrfTagDao;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public @ResponseBody void importOdm(@RequestBody org.cdisc.ns.odm.v130_sb.ODM odm, HttpServletResponse response, HttpServletRequest request)
            throws Exception {

        System.out.println("I'm in ImportOdm method for StudyBuild");
        UserAccount userAccount = getCurrentUser();

        ODMcomplexTypeDefinitionStudy odmStudy = odm.getStudy().get(0);
        ODMcomplexTypeDefinitionGlobalVariables odmGlobalVariables = odmStudy.getGlobalVariables();
        String studyOid = odm.getStudy().get(0).getOID();
        Study study = studyDao.findByOcOID(studyOid);

        if (study == null) {
            study = new Study();
            study.setOc_oid(studyOid);
            study = studyDao.saveOrUpdate(buildStudy(odmGlobalVariables, userAccount, study));
        } else {
            study = studyDao.saveOrUpdate(updateStudy(odmGlobalVariables, userAccount, study));
        }

        StudyUserRoleBean studyUserRoleBean = null;
        UserAccountDAO udao = new UserAccountDAO(dataSource);
        UserAccountBean userAccountBean = (UserAccountBean) udao.findByPK(userAccount.getUserId());
        studyUserRoleBean = udao.findByUserNameAndStudy(userAccountBean.getName(), study.getStudyId());
        if (studyUserRoleBean == null) {
            studyUserRoleBean = new StudyUserRoleBean();
            studyUserRoleBean = udao.createStudyUserRole(userAccountBean, buildUserRole(study, userAccountBean, studyUserRoleBean));
        } else {
            studyUserRoleBean = udao.updateStudyUserRole(updateUserRole(study, userAccountBean, studyUserRoleBean), userAccountBean.getName());
        }

        StudyEventDefinition studyEventDefinition = null;
        List<ODMcomplexTypeDefinitionMetaDataVersion> odmMetadataVersions = odmStudy.getMetaDataVersion();
        List<ODMcomplexTypeDefinitionStudyEventRef> odmStudyEventRefs = odmMetadataVersions.get(0).getProtocol().getStudyEventRef();
        List<ODMcomplexTypeDefinitionStudyEventDef> odmStudyEventDefs = odmMetadataVersions.get(0).getStudyEventDef();
        for (ODMcomplexTypeDefinitionStudyEventDef odmStudyEventDef : odmStudyEventDefs) {

            studyEventDefinition = studyEventDefinitionDao.findByOcOID(odmStudyEventDef.getOID());
            if (studyEventDefinition == null) {
                studyEventDefinition = new StudyEventDefinition();
                studyEventDefinition.setOc_oid(odmStudyEventDef.getOID());
                studyEventDefinition.setStudy(study);
                studyEventDefinition = studyEventDefinitionDao.saveOrUpdate(buildEvent(odmStudyEventDef, userAccount, studyEventDefinition));
            } else {
                studyEventDefinition = studyEventDefinitionDao.saveOrUpdate(updateEvent(odmStudyEventDef, userAccount, studyEventDefinition));
            }

        }

        CrfBean crf = null;
        CrfVersion crfVersion = null;
        for (ODMcomplexTypeDefinitionFormDef odmFormDef : odmMetadataVersions.get(0).getFormDef()) {
            crf = crfDao.findByOcOID(odmFormDef.getOID());

            if (crf == null) {
                crf = new CrfBean();
                crf.setOcOid(odmFormDef.getOID());
                crf.setStudy(study);
                crf = crfDao.saveOrUpdate(buildCrf(odmFormDef, userAccount, crf));
            } else {
                crf = crfDao.saveOrUpdate(updateCrf(odmFormDef, userAccount, crf));
            }

            List<OCodmComplexTypeDefinitionFormLayoutDef> formLayoutDefs = odmFormDef.getFormLayoutDef();
            for (OCodmComplexTypeDefinitionFormLayoutDef formLayoutDef : formLayoutDefs) {

                crfVersion = crfVersionDao.findByOcOID(formLayoutDef.getOID());
                String url = formLayoutDef.getURL();
                if (crfVersion == null) {
                    crfVersion = new CrfVersion();
                    crfVersion.setOcOid(formLayoutDef.getOID());
                    crfVersion = crfVersionDao.saveOrUpdate(buildCrfVersion(odmFormDef, userAccount, crfVersion, crf, url));
                } else {
                    crfVersion = crfVersionDao.saveOrUpdate(updateCrfVersion(odmFormDef, userAccount, crfVersion, crf, url));
                }
            }
        }
        for (ODMcomplexTypeDefinitionStudyEventRef odmStudyEventRef : odmStudyEventRefs) {
            for (ODMcomplexTypeDefinitionStudyEventDef odmStudyEventDef : odmStudyEventDefs) {
                if (odmStudyEventDef.getOID().equals(odmStudyEventRef.getStudyEventOID())) {
                    studyEventDefinition = studyEventDefinitionDao.findByOcOID(odmStudyEventDef.getOID());
                    studyEventDefinition.setOrdinal(odmStudyEventRef.getOrderNumber().intValue());
                    studyEventDefinition = studyEventDefinitionDao.saveOrUpdate(studyEventDefinition);

                    EventDefinitionCrf eventDefinitionCrf = null;
                    for (ODMcomplexTypeDefinitionFormRef odmFormRef : odmStudyEventDef.getFormRef()) {
                        crf = crfDao.findByOcOID(odmFormRef.getFormOID());

                        eventDefinitionCrf = eventDefinitionCrfDao.findByStudyEventDefinitionIdAndCRFIdAndStudyId(
                                studyEventDefinition.getStudyEventDefinitionId(), crf.getCrfId(), study.getStudyId());

                        String defaultVersionOid = "";

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

                        crfVersion = crfVersionDao.findByOcOID(defaultVersionOid);
                        EventDefinitionCrfTag eventDefinitionCrfTag = null;
                        if (eventDefinitionCrf == null) {
                            eventDefinitionCrf = new EventDefinitionCrf();
                            eventDefinitionCrf.setStudy(study);
                            eventDefinitionCrf.setStudyEventDefinition(studyEventDefinition);
                            eventDefinitionCrf = eventDefinitionCrfDao
                                    .saveOrUpdate(buildEventDefinitionCrf(eventDefinitionCrf, userAccount, crf, crfVersion, conf));
                        } else {
                            eventDefinitionCrf = eventDefinitionCrfDao
                                    .saveOrUpdate(updateEventDefinitionCrf(eventDefinitionCrf, userAccount, crf, crfVersion, conf));

                        }
                        int tagId = 2; // Offline
                        String crfPath = studyEventDefinition.getOc_oid() + "." + crf.getOcOid();
                        eventDefinitionCrfTag = eventDefinitionCrfTagDao.findByCrfPathAndTagId(tagId, crfPath);
                        if (eventDefinitionCrfTag == null) {
                            eventDefinitionCrfTag = new EventDefinitionCrfTag();
                            eventDefinitionCrfTag = eventDefinitionCrfTagDao
                                    .saveOrUpdate(buildEDCTag(eventDefinitionCrf, userAccount, conf, tagId, crfPath, eventDefinitionCrfTag));
                        } else {
                            eventDefinitionCrfTag = eventDefinitionCrfTagDao
                                    .saveOrUpdate(updateEDCTag(eventDefinitionCrf, userAccount, conf, tagId, crfPath, eventDefinitionCrfTag));
                        }
                    }
                }
            }
        }

    }

    private Study buildStudy(ODMcomplexTypeDefinitionGlobalVariables odmGlobalVariables, UserAccount userAccount, Study study) {
        study.setUniqueIdentifier(odmGlobalVariables.getProtocolName().getValue());
        study.setName(odmGlobalVariables.getStudyName().getValue());
        study.setSummary(odmGlobalVariables.getStudyDescription().getValue());
        study.setStatus(org.akaza.openclinica.domain.Status.PENDING);
        study.setUserAccount(userAccount);
        return study;
    }

    private Study updateStudy(ODMcomplexTypeDefinitionGlobalVariables odmGlobalVariables, UserAccount userAccount, Study study) {
        study = buildStudy(odmGlobalVariables, userAccount, study);
        study.setUpdateId(userAccount.getUserId());
        study.setDateUpdated(new Date());

        return study;
    }

    private StudyUserRoleBean buildUserRole(Study study, UserAccountBean userAccountBean, StudyUserRoleBean surBean) {
        surBean.setName(userAccountBean.getName());
        surBean.setRole(Role.STUDYDIRECTOR);
        surBean.setStudyId(study.getStudyId());
        surBean.setStudyName(study.getName());
        surBean.setStatus(Status.AVAILABLE);
        surBean.setOwner(userAccountBean);
        surBean.setCreatedDate(new Date());
        return surBean;
    }

    private StudyUserRoleBean updateUserRole(Study study, UserAccountBean userAccountBean, StudyUserRoleBean surBean) {
        surBean = buildUserRole(study, userAccountBean, surBean);
        surBean.setUpdatedDate(new Date());
        surBean.setUpdater(userAccountBean);
        return surBean;
    }

    private StudyEventDefinition buildEvent(ODMcomplexTypeDefinitionStudyEventDef odmStudyEventDef, UserAccount userAccount,
            StudyEventDefinition studyEventDefinition) {
        studyEventDefinition.setName(odmStudyEventDef.getName());
        if (odmStudyEventDef.getRepeating().value().equalsIgnoreCase("Yes")) {
            studyEventDefinition.setRepeating(true);
        } else {
            studyEventDefinition.setRepeating(false);
        }
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
            StudyEventDefinition studyEventDefinition) {
        studyEventDefinition = buildEvent(odmStudyEventDef, userAccount, studyEventDefinition);
        studyEventDefinition.setUpdateId(userAccount.getUserId());
        studyEventDefinition.setDateUpdated(new Date());
        return studyEventDefinition;
    }

    private CrfBean buildCrf(ODMcomplexTypeDefinitionFormDef odmFormDef, UserAccount userAccount, CrfBean crf) {
        crf.setName(odmFormDef.getName());
        crf.setUserAccount(userAccount);
        crf.setStatus(org.akaza.openclinica.domain.Status.AVAILABLE);
        return crf;
    }

    private CrfBean updateCrf(ODMcomplexTypeDefinitionFormDef odmFormDef, UserAccount userAccount, CrfBean crf) {
        crf = buildCrf(odmFormDef, userAccount, crf);
        crf.setUpdateId(userAccount.getUserId());
        crf.setDateUpdated(new Date());
        return crf;
    }

    private CrfVersion buildCrfVersion(ODMcomplexTypeDefinitionFormDef odmFormDef, UserAccount userAccount, CrfVersion crfVersion, CrfBean crf, String url) {
        crfVersion.setCrf(crf);
        crfVersion.setUrl(url);
        crfVersion.setUserAccount(userAccount);
        crfVersion.setStatus(org.akaza.openclinica.domain.Status.AVAILABLE);
        return crfVersion;
    }

    private CrfVersion updateCrfVersion(ODMcomplexTypeDefinitionFormDef odmFormDef, UserAccount userAccount, CrfVersion crfVersion, CrfBean crf, String url) {
        crfVersion = buildCrfVersion(odmFormDef, userAccount, crfVersion, crf, url);
        crfVersion.setUpdateId(userAccount.getUserId());
        crfVersion.setDateUpdated(new Date());

        return crfVersion;
    }

    private UserAccount getCurrentUser() {
        UserAccount ub = userDao.findById(1);
        return ub;
    }

    private EventDefinitionCrf buildEventDefinitionCrf(EventDefinitionCrf eventDefinitionCrf, UserAccount userAccount, CrfBean crf, CrfVersion crfVersion,
            OCodmComplexTypeDefinitionConfigurationParameters conf) {
        eventDefinitionCrf.setCrf(crf);
        eventDefinitionCrf.setStatusId(org.akaza.openclinica.domain.Status.AVAILABLE.getCode());
        eventDefinitionCrf.setUserAccount(userAccount);
        eventDefinitionCrf.setCrfVersion(crfVersion);
        setConfigurationProperties(conf, eventDefinitionCrf);
        return eventDefinitionCrf;
    }

    private EventDefinitionCrf updateEventDefinitionCrf(EventDefinitionCrf eventDefinitionCrf, UserAccount userAccount, CrfBean crf, CrfVersion crfVersion,
            OCodmComplexTypeDefinitionConfigurationParameters conf) {
        eventDefinitionCrf = buildEventDefinitionCrf(eventDefinitionCrf, userAccount, crf, crfVersion, conf);
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

    private EventDefinitionCrfTag buildEDCTag(EventDefinitionCrf eventDefinitionCrf, UserAccount userAccount,
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
        eventDefinitionCrfTag = buildEDCTag(eventDefinitionCrf, userAccount, conf, tagId, crfPath, eventDefinitionCrfTag);
        eventDefinitionCrfTag.setUpdateId(userAccount.getUserId());
        eventDefinitionCrfTag.setDateUpdated(new Date());

        return eventDefinitionCrfTag;
    }

    private EventDefinitionCRFDAO getEventDefinitionCRFDao() {
        return (new EventDefinitionCRFDAO(dataSource));
    }

}
