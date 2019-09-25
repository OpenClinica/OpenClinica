package org.akaza.openclinica.service;

import liquibase.util.StringUtils;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.managestudy.StudyType;
import org.akaza.openclinica.bean.managestudy.SubjectTransferBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.bean.submit.crfdata.ODMContainer;
import org.akaza.openclinica.controller.dto.*;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.dao.hibernate.SubjectDao;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.domain.datamap.*;
import org.akaza.openclinica.domain.enumsupport.JobType;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.web.restful.errors.ErrorConstants;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This Service class is used with Add Participant Rest Api
 *
 * @author joekeremian
 */

@Service( "studyParticipantService" )
public class StudyParticipantServiceImpl implements StudyParticipantService {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    @Qualifier( "dataSource" )
    private BasicDataSource dataSource;

    @Autowired
    StudyDao studyHibDao;

    @Autowired
    SubjectDao subjectHibDao;

    @Autowired
    StudySubjectDao studySubjectHibDao;

    @Autowired
    UserService userService;

    @Autowired
    CSVService csvService;

    @Autowired
    ImportService importService;

    @Autowired
    UtilService utilService;
    @Autowired
    ValidateService validateService;

    private StudyDAO studyDao;
    private StudySubjectDAO studySubjectDao;
    private SubjectDAO subjectDao;  

    SimpleDateFormat sdf_fileName = new SimpleDateFormat("yyyy-MM-dd'-'HHmmssSSS'Z'");
    public static final String DASH = "-";
    public static final String SCHEDULE_EVENT = "_Schedule Event";
    public static final String SUCCESS = "Success";
    public static final String FAILED = "Failed";
    public static final String US_PHONE_PATTERN = "^[0-9]{10,10}$";
    public static final String INTL_PHONE_PATTERN = "^\\+[0-9]{1,3} [0-9]{1,14}$";
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?$";


   

    public AddParticipantResponseDTO addParticipant(AddParticipantRequestDTO addParticipantRequestDTO, UserAccountBean userAccountBean, String studyOid, String siteOid , String customerUuid, ResourceBundle textsBundle,String accessToken, String register ) {
        boolean createNewParticipant=false;
        Study tenantStudy = studyHibDao.findByOcOID(studyOid);
        Study tenantSite = studyHibDao.findByOcOID(siteOid);
        StudyBean tenantStudyBean = getStudyDao().findByOid(studyOid);
        StudyBean tenantSiteBean = getStudyDao().findByOid(siteOid);
       
        if (isEnrollmentCapped(tenantStudyBean,tenantSiteBean))
            throw new OpenClinicaSystemException( ErrorConstants.ERR_PARTICIPANTS_ENROLLMENT_CAP_REACHED);
        
        if (StringUtils.isEmpty(addParticipantRequestDTO.getSubjectKey()))
            throw new OpenClinicaSystemException(ErrorConstants.ERR_MISSING_PARTICIPANT_ID_DATA);

        if (!StringUtils.isEmpty(addParticipantRequestDTO.getSubjectKey()) && addParticipantRequestDTO.getSubjectKey() .length() > 30)
            throw new OpenClinicaSystemException(ErrorConstants.ERR_PARTICIPANT_ID_TOO_LONG);

        if (!StringUtils.isEmpty(addParticipantRequestDTO.getFirstName()) && addParticipantRequestDTO.getFirstName().length() > 35)
            throw new OpenClinicaSystemException(ErrorConstants.ERR_FIRST_NAME_TOO_LONG);


        if (!StringUtils.isEmpty(addParticipantRequestDTO.getLastName()) && addParticipantRequestDTO.getLastName().length() > 35)
            throw new OpenClinicaSystemException(ErrorConstants.ERR_LAST_NAME_TOO_LONG);

        if (!StringUtils.isEmpty(addParticipantRequestDTO.getIdentifier()) && addParticipantRequestDTO.getIdentifier().length() > 35)
            throw new OpenClinicaSystemException(ErrorConstants.ERR_IDENTIFIER_TOO_LONG);

        if (!StringUtils.isEmpty(addParticipantRequestDTO.getEmailAddress())) {
            Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);
            Matcher emailMatch = emailPattern.matcher(addParticipantRequestDTO.getEmailAddress());
            if (addParticipantRequestDTO.getEmailAddress().length() > 255)
                throw new OpenClinicaSystemException(ErrorConstants.ERR_EMAIL_ADDRESS_TOO_LONG);
            if (!emailMatch.matches())
                throw new OpenClinicaSystemException( ErrorConstants.ERR_INVALID_EMAIL_ADDRESS);
        }

        if (!StringUtils.isEmpty(addParticipantRequestDTO.getPhoneNumber())) {
            Pattern intlPhonePattern = Pattern.compile(INTL_PHONE_PATTERN);
            Matcher intlPhoneMatch = intlPhonePattern.matcher(addParticipantRequestDTO.getPhoneNumber());
            if (addParticipantRequestDTO.getPhoneNumber().length() > 17)
                throw new OpenClinicaSystemException( ErrorConstants.ERR_PHONE_NUMBER_TOO_LONG);
            if (!(intlPhoneMatch.matches() && addParticipantRequestDTO.getPhoneNumber().length() < 18))
                throw new OpenClinicaSystemException(ErrorConstants.ERR_INVALID_PHONE_NUMBER);
        }


        if (!utilService.isParticipantUniqueToSite(siteOid, addParticipantRequestDTO.getSubjectKey()))
            throw new OpenClinicaSystemException( ErrorConstants.ERR_PARTICIPANT_NOT_FOUND);


        Subject subject = null;
        StudySubject studySubject = null;
        StudySubjectBean studySubjectBean = getStudySubjectDao().findByLabelAndStudyForCreatingParticipant(addParticipantRequestDTO.getSubjectKey(), tenantStudyBean.getId());

        StudySubjectBean studySubjectBeanInParent = new StudySubjectBean();
        if (tenantStudyBean.getParentStudyId() > 0) {
            studySubjectBeanInParent = getStudySubjectDao().findByLabelAndStudyForCreatingParticipant(addParticipantRequestDTO.getSubjectKey(), tenantStudyBean.getParentStudyId());// <
        }
        if (studySubjectBean == null || (!studySubjectBean.isActive() && !studySubjectBeanInParent.isActive())) {
            createNewParticipant=true;
            // Create New Study Subject
            SubjectBean subjectBean = new SubjectBean();
            subjectBean.setStatus(Status.AVAILABLE);
            subjectBean.setOwner(userAccountBean);
            subjectBean = getSubjectDao().create(subjectBean);
            studySubjectBean = new StudySubjectBean();

            studySubjectBean.setSubjectId(subjectBean.getId());
            if (tenantSiteBean != null) {
                studySubjectBean.setStudyId(tenantSiteBean.getId());
            } else {
                studySubjectBean.setStudyId(tenantStudyBean.getId());
            }

            studySubjectBean.setLabel(addParticipantRequestDTO.getSubjectKey());
            studySubjectBean.setStatus(Status.AVAILABLE);
            studySubjectBean.setOwner(userAccountBean);
            Date now = new Date();
            studySubjectBean.setCreatedDate(now);
            studySubjectBean = this.getStudySubjectDao().createWithoutGroup(studySubjectBean);
        }

        // OC-11095 Adding Contact information Through Rest API on a signed participant is failing
        if(studySubjectBean!=null && !(studySubjectBean.getStatus().equals(Status.AVAILABLE) || studySubjectBean.getStatus().equals(Status.SIGNED)))
            throw new OpenClinicaSystemException(ErrorConstants.ERR_PARTICIPANT_ID_NOT_AVAILABLE);

        studySubject = saveOrUpdateStudySubjectDetails(studySubjectBean, addParticipantRequestDTO, userAccountBean);

        if(createNewParticipant) {
                //update at site level
                updateStudySubjectSize(tenantSite,tenantSiteBean);
                // update at parent level
                updateStudySubjectSize(tenantStudy,tenantStudyBean);
        }

        if ((register.equalsIgnoreCase("y") || register.equalsIgnoreCase("yes") ) && validateService.isParticipateActive(tenantStudy)) {
            OCParticipantDTO oCParticipantDTO = new OCParticipantDTO();
            oCParticipantDTO.setFirstName(addParticipantRequestDTO.getFirstName());
            oCParticipantDTO.setLastName(addParticipantRequestDTO.getLastName());
            oCParticipantDTO.setEmail(addParticipantRequestDTO.getEmailAddress());
            oCParticipantDTO.setPhoneNumber(addParticipantRequestDTO.getPhoneNumber());
            oCParticipantDTO.setIdentifier(addParticipantRequestDTO.getIdentifier());
            userService.connectParticipant(studyOid, addParticipantRequestDTO.getSubjectKey(),
                    oCParticipantDTO, accessToken, userAccountBean, customerUuid, textsBundle);
            studySubject=studySubjectHibDao.findById(studySubject.getStudySubjectId());
        }



        AddParticipantResponseDTO addParticipantResponseDTO = new AddParticipantResponseDTO();
        addParticipantResponseDTO.setSubjectKey(studySubject.getLabel());
        addParticipantResponseDTO.setSubjectOid(studySubject.getOcOid());
        addParticipantResponseDTO.setStatus(studySubject.getStatus().getName());
        addParticipantResponseDTO.setParticipateStatus(studySubject.getUserStatus() != null ? studySubject.getUserStatus().getValue() : "");

        return addParticipantResponseDTO;
    }


    public void startBulkAddParticipantJob(MultipartFile file, Study study, Study site, UserAccountBean userAccountBean,  JobDetail jobDetail, String schema,String customerUuid, ResourceBundle textsBundle,String accessToken, String register) {
        ResponseEntity response = null;
        String logFileName = null;
        CoreResources.setRequestSchema(schema);

        sdf_fileName.setTimeZone(TimeZone.getTimeZone("GMT"));
        String fileName = study.getUniqueIdentifier() + DASH + study.getEnvType() + SCHEDULE_EVENT + "_" + sdf_fileName.format(new Date()) + ".csv";

        String filePath = userService.getFilePath(JobType.SCHEDULE_EVENT) + File.separator + fileName;
        jobDetail.setLogPath(filePath);
        List<DataImportReport> dataImportReports = new ArrayList<>();
        try {
            // read csv file
            List<AddParticipantRequestDTO> addParticipantRequestDTOs = csvService.readAddParticipantBulkCSVFile(file, study.getOc_oid(), site.getOc_oid());

            for (AddParticipantRequestDTO addParticipantRequestDTO : addParticipantRequestDTOs) {
                String participantId = addParticipantRequestDTO.getSubjectKey();
                Integer rowNumber = addParticipantRequestDTO.getRowNumber();
                AddParticipantResponseDTO result = null;
                DataImportReport dataImportReport = null;
                try {
                    result = addParticipant(addParticipantRequestDTO, userAccountBean, study.getOc_oid(), site.getOc_oid(), customerUuid, textsBundle, accessToken, register);
                    dataImportReport = new DataImportReport(participantId, ((AddParticipantResponseDTO) result).getSubjectOid(), ((AddParticipantResponseDTO) result).getStatus(), ((AddParticipantResponseDTO) result).getParticipateStatus(), SUCCESS, rowNumber);
                } catch (OpenClinicaSystemException ose) {
                    dataImportReport = new DataImportReport(rowNumber, participantId, FAILED, ose.getMessage());
                }
                dataImportReports.add(dataImportReport);
            }
            importService.writeToFile(dataImportReports, fileName, JobType.BULK_ADD_PARTICIPANTS);
            userService.persistJobCompleted(jobDetail, fileName);

        } catch (Exception e) {
            userService.persistJobFailed(jobDetail, fileName);
            logger.error("Error " + e.getMessage());
        }


    }

    public StudySubjectDAO getStudySubjectDao() {
        studySubjectDao = studySubjectDao != null ? studySubjectDao : new StudySubjectDAO(dataSource);
        return studySubjectDao;
    }

    public StudyDAO getStudyDao() {
        studyDao = studyDao != null ? studyDao : new StudyDAO(dataSource);
        return studyDao;
    }

    public SubjectDAO getSubjectDao() {
        subjectDao = subjectDao != null ? subjectDao : new SubjectDAO(dataSource);
        return subjectDao;
    }

    private StudySubject saveOrUpdateStudySubjectDetails(StudySubjectBean studySubjectBean, AddParticipantRequestDTO addParticipantRequestDTO,
                                                         UserAccountBean userAccountBean) {
        StudySubject studySubject = studySubjectHibDao.findById(studySubjectBean.getId());



        StudySubjectDetail studySubjectDetail = studySubject.getStudySubjectDetail();
        studySubject.setUpdateId(userAccountBean.getId());
        studySubject.setDateUpdated(new Date());

        if (studySubjectDetail == null) {
         //   studySubjectBean.setUpdater(userAccountBean);
         //   studySubjectBean.setUpdatedDate(new Date());
         //   studySubjectDao.update(studySubjectBean);
            studySubjectHibDao.saveOrUpdate(studySubject);

            studySubjectDetail = new StudySubjectDetail();
        }

        if (addParticipantRequestDTO.getFirstName() != null)
            studySubjectDetail.setFirstName(addParticipantRequestDTO.getFirstName() != null ? addParticipantRequestDTO.getFirstName() : "");
        if (addParticipantRequestDTO.getLastName() != null)
            studySubjectDetail.setLastName(addParticipantRequestDTO.getLastName() != null ? addParticipantRequestDTO.getLastName() : "");
        if (addParticipantRequestDTO.getEmailAddress() != null)
            studySubjectDetail.setEmail(addParticipantRequestDTO.getEmailAddress() != null ? addParticipantRequestDTO.getEmailAddress() : "");
        if (addParticipantRequestDTO.getPhoneNumber() != null)
            studySubjectDetail.setPhone(addParticipantRequestDTO.getPhoneNumber() != null ? addParticipantRequestDTO.getPhoneNumber() : "");
        if (addParticipantRequestDTO.getIdentifier() != null)
            studySubjectDetail.setIdentifier(addParticipantRequestDTO.getIdentifier() != null ? addParticipantRequestDTO.getIdentifier() : "");

        studySubject.setStudySubjectDetail(studySubjectDetail);

        studySubject = studySubjectHibDao.saveOrUpdate(studySubject);

        return studySubject;

    }

    public int getSubjectCount(StudyBean currentStudy) {
        int subjectCount = 0;
        StudyDAO sdao = new StudyDAO(dataSource);
        StudyBean studyBean = (StudyBean) sdao.findByPK(currentStudy.getId());
        if (studyBean != null)
            subjectCount = studyBean.getSubjectCount();

        if (subjectCount == 0) {
            StudySubjectDAO ssdao = this.getStudySubjectDao();
            ArrayList ss = ssdao.findAllBySiteId(currentStudy.getId());
            if (ss != null) {
                subjectCount = ss.size();
            }
        }
        return subjectCount;
    }



    private void updateStudySubjectSize(Study study, StudyBean currentStudy) {
        int subjectCount = getSubjectCount(currentStudy);
        study.setSubjectCount(subjectCount+1);
        studyHibDao.saveOrUpdate(study);
    }


    private boolean isEnrollmentCapped(StudyBean currentStudy,StudyBean siteStudy){

        boolean capIsOn = isEnrollmentCapEnforced(currentStudy,siteStudy);

        StudySubjectDAO studySubjectDAO = this.getStudySubjectDao();
        int numberOfSubjects = studySubjectDAO.getCountofActiveStudySubjects();

        StudyDAO studyDAO = this.getStudyDao();
        StudyBean sb = null;
        if(currentStudy.getParentStudyId()!=0){
            sb = (StudyBean) studyDAO.findByPK(currentStudy.getParentStudyId());
        }else{
             sb = (StudyBean) studyDAO.findByPK(currentStudy.getId());
        }
        int  expectedTotalEnrollment = sb.getExpectedTotalEnrollment();

        if (numberOfSubjects >= expectedTotalEnrollment && capIsOn)
            return true;
        else
            return false;
    }

    /**
     * if it's site level, then also need to check study 
     * @return
     */
    private boolean isEnrollmentCapEnforced(StudyBean currentStudy,StudyBean siteStudy){
        StudyParameterValueDAO studyParameterValueDAO = new StudyParameterValueDAO(this.dataSource);
       
        boolean capEnforcedSite = false;
        String  enrollmentCapStatusSite = null;
        
        String enrollmentCapStatus = studyParameterValueDAO.findByHandleAndStudy(currentStudy.getId(), "enforceEnrollmentCap").getValue();
        boolean capEnforced = Boolean.valueOf(enrollmentCapStatus);
        
        // check at the site level
        if(siteStudy != null) {
        	int siteId = siteStudy.getId();
        	enrollmentCapStatusSite = studyParameterValueDAO.findByHandleAndStudy(siteId, "enforceEnrollmentCap").getValue();
        	capEnforcedSite = Boolean.valueOf(enrollmentCapStatusSite);        	
        }
        
        if(capEnforcedSite || capEnforced) {
        	return true;
        }else {
        	return false;
        }
        
    }


}