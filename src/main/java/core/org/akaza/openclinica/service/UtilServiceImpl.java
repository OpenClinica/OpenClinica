package core.org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.login.RestReponseDTO;
import core.org.akaza.openclinica.bean.login.StudyUserRoleBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import core.org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.datamap.StudySubject;
import core.org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.web.restful.errors.ErrorConstants;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This Service class is used with View Study Subject Page
 *
 * @author joekeremian
 */

@Service( "utilService" )
public class UtilServiceImpl implements UtilService {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    @Qualifier( "dataSource" )
    private BasicDataSource dataSource;

    @Autowired
    StudyDao studyDao;

    @Autowired
    StudySubjectDao studySubjectDao;

    @Autowired
    RestfulServiceHelper restfulServiceHelper;

    @Autowired
    StudyBuildService studyBuildService;


    public String getAccessTokenFromRequest(HttpServletRequest request) {
        return (String) request.getSession().getAttribute("accessToken");
    }

    public void setSchemaFromStudyOid(String studyOid) {
        studyBuildService.setRequestSchemaByStudy(studyOid);
    }

    public String getCustomerUuidFromRequest(HttpServletRequest request) {
        Map<String, Object> userContextMap = (LinkedHashMap<String, Object>) request.getSession().getAttribute("userContextMap");
        return (String) userContextMap.get("customerUuid");
    }

    public UserAccountBean getUserAccountFromRequest(HttpServletRequest request){
        return getRestfulServiceHelper().getUserAccount(request);
    }


    public RestfulServiceHelper getRestfulServiceHelper() {
        if (restfulServiceHelper == null) {
            restfulServiceHelper = new RestfulServiceHelper(this.dataSource, studyBuildService, studyDao);
        }
        return restfulServiceHelper;
    }


    public boolean isParticipantIDSystemGenerated(Study tenantStudy) {
        String idSetting = "";
        if(tenantStudy.isSite())
            idSetting = tenantStudy.getStudy().getSubjectIdGeneration();
        else
            idSetting = tenantStudy.getSubjectIdGeneration();

        logger.info("subject Id Generation :" + idSetting);

        if (idSetting.equals("auto editable") || idSetting.equals("auto non-editable"))
            return true;

        return false;
    }

    public boolean isParticipantUniqueToSite(String siteOID, String studySubjectId) {
        Study site = studyDao.findByOcOID(siteOID);
        StudySubject studySubject = null;
        if (site != null) {
            if (site.getStudy() != null) {
                ArrayList<StudySubject> studySubjects = studySubjectDao.findByLabelAndParentStudy(studySubjectId, site.getStudy());
                if (studySubjects.size() == 0) {
                    return true;
                } else {
                    studySubject = studySubjectDao.findByLabelAndStudyOrParentStudy(studySubjectId, site);
                    if (studySubject != null)
                        return true;
                }
            } else {
                studySubject = studySubjectDao.findByLabelAndStudyOrParentStudy(studySubjectId, site);
                if (studySubject == null)
                    return true;
            }
        }
        return false;
    }

    public void checkFileFormat(MultipartFile file, String fileHeaderMapping) {
        ResponseEntity response = null;
        RestReponseDTO responseDTO = new RestReponseDTO();
        String finalMsg = null;

        //only support csv file
        if (file != null && file.getSize() > 0) {
            String fileNm = file.getOriginalFilename();

            if (fileNm != null && fileNm.endsWith(".csv")) {
                String line;
                BufferedReader reader;
                InputStream is;
                try {
                    is = file.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(is));
                    CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(fileHeaderMapping).withFirstRecordAsHeader().withTrim();

                    CSVParser csvParser = new CSVParser(reader, csvFileFormat);
                    csvParser.parse(reader, csvFileFormat);
                } catch (IllegalArgumentException e) {
                    throw new OpenClinicaSystemException(ErrorConstants.ERR_MULTIPLE_PARTICIPANT_ID_HEADERS);
                } catch (Exception e) {
                    throw new OpenClinicaSystemException(ErrorConstants.ERR_NOT_CSV_FILE);
                }

            } else {
                throw new OpenClinicaSystemException(ErrorConstants.ERR_NOT_CSV_FILE);
            }


        } else {
            throw new OpenClinicaSystemException(ErrorConstants.ERR_BLANK_FILE);
        }

    }

    public boolean checkStudyLevelUser(ArrayList<StudyUserRoleBean> userRoles, String siteOid) {
        Study publicSite = studyDao.findPublicStudy(siteOid);
        for (StudyUserRoleBean userRole : userRoles) {
            if(publicSite.getStudyId() == userRole.getStudyId())
                return false;
        }
        return true;
    }

}