package org.akaza.openclinica.controller;

import org.akaza.openclinica.bean.core.NumericComparisonOperator;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.UserType;
import org.akaza.openclinica.bean.login.*;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.service.StudyParameterConfig;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.controller.dto.SiteStatusDTO;
import org.akaza.openclinica.controller.dto.StudyEnvStatusDTO;
import org.akaza.openclinica.controller.helper.AsyncStudyHelper;
import org.akaza.openclinica.controller.helper.OCUserDTO;
import org.akaza.openclinica.controller.helper.StudyEnvironmentRoleDTO;
import org.akaza.openclinica.controller.helper.StudyInfoObject;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudyParameterDao;
import org.akaza.openclinica.dao.hibernate.StudyUserRoleDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEnvEnum;
import org.akaza.openclinica.domain.datamap.StudyParameter;
import org.akaza.openclinica.domain.datamap.StudyParameterValue;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.LiquibaseOnDemandService;
import org.akaza.openclinica.service.SchemaCleanupService;
import org.akaza.openclinica.service.SiteBuildService;
import org.akaza.openclinica.service.StudyBuildService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping(value = "/auth/api/v1/studies")
public class StudyController {

    public static ResourceBundle resadmin, resaudit, resexception, resformat, respage, resterm, restext, resword, resworkflow;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    @Autowired
    UserAccountController userAccountController;
    UserAccountDAO udao;
    StudyDAO sdao;
    StudyEventDefinitionDAO seddao;
    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;
    @Autowired
    private StudyDao studyDao;
    @Autowired
    private StudyUserRoleDao studyUserRoleDao;
    @Autowired
    private StudyBuildService studyBuildService;
    @Autowired
    private LiquibaseOnDemandService liquibaseOnDemandService;
    @Autowired
    private SiteBuildService siteBuildService;
    @Autowired
    private SchemaCleanupService schemaCleanupService;
    @Autowired
    StudyParameterDao studyParameterDao;


    private static final String validation_failed_message = "VALIDATION FAILED";
    private static final String validation_passed_message = "SUCCESS";

    /**
     * @api {post} /pages/auth/api/v1/studies/ Create a study
     * @apiName createNewStudy
     * @apiPermission Authenticate using api-key. admin
     * @apiVersion 3.8.0
     * @apiParam {String} uniqueProtococlId Study unique study ID.
     * @apiParam {String} briefTitle Brief Title .
     * @apiParam {String} principalInvestigator Principal Investigator Name.
     * @apiParam {Integer} expectedTotalEnrollment Expected Total Enrollment number
     * @apiParam {String} sponsor Sponsor name.
     * @apiParam {String} studyType 'Interventional' or ' Observational'
     * @apiParam {String} status 'Available' or 'Design'
     * @apiParam {String} briefSummary Study Summary
     * @apiParam {Date} startDate Start date
     * @apiParam {Array} assignUserRoles Assign Users to Roles for this Study.
     * @apiGroup Study
     * @apiHeader {String} api_key Users unique access-key.
     * @apiDescription This API is to create a New Study in OC.
     * All the fields are required fields and can't be left blank.
     * You need to provide your Api-key to be connected.
     * @apiParamExample {json} Request-Example:
     * {
     * "briefTitle": "Study Study ID Name",
     * "principalInvestigator": "Principal Investigator Name",
     * "expectedTotalEnrollment": "10",
     * "sponsor": "Sponsor Name",
     * "studyType": "Interventional",
     * "status": "available",
     * "assignUserRoles": [
     * { "username": "usera", "role": "Data Manager" },
     * { "username": "userb", "role": "Study Director" },
     * { "username": "userc", "role": "Data Specialist" },
     * { "username": "userd", "role": "Monitor" },
     * { "username": "usere", "role": "Data Entry Person" }
     * ],
     * "uniqueStudyID": "Study Study ID",
     * "briefSummary": "Study Summary",
     * "startDate": "2011-11-11"
     * }
     * @apiErrorExample {json} Error-Response:
     * HTTP/1.1 400 Bad Request
     * {
     * "message": "VALIDATION FAILED",
     * "status": "available",
     * "principalInvestigator": "Principal Investigator Name",
     * "expectedTotalEnrollment": "10",
     * "sponsor": "Sponsor Name",
     * "studyType": "Interventional",
     * "errors": [
     * {"field": "UniqueStudyId","resource": "Study Object","code": "Unique Study Id exist in the System"}
     * ],
     * "startDate": "2011-11-11",
     * "assignUserRoles": [
     * {"username": "usera","role": "Data Manager"},
     * {"username": "userb","role": "Study Director"},
     * {"username": "userc","role": "Data Specialist"}
     * ],
     * "uniqueStudyID": "Study Study ID",
     * "briefTitle": "Study Study ID",
     * "briefSummary": "Study Summary",
     * "studyOid": null
     * }
     * @apiSuccessExample {json} Success-Response:
     * HTTP/1.1 200 OK
     * {
     * "message": "SUCCESS",
     * "uniqueStudyID": "Study Study ID",
     * "studyOid": "S_STUDYPRO",
     * }
     */

    @RequestMapping(value = "/{studyEnvUuid}/status", method = RequestMethod.PUT)
    public ResponseEntity<Object> changeStudyStatus(
            @RequestBody HashMap<String, Object> requestDTO,
            @PathVariable("studyEnvUuid") String studyEnvUuid,
            HttpServletRequest request) {

        ResponseEntity response = null;
        ArrayList<ErrorObject> errorObjects = new ArrayList<ErrorObject>();
        StudyDTO studyDTO = new StudyDTO();

        // Set the locale, status object needs this
        Locale locale = new Locale("en_US");
        request.getSession().setAttribute(LocaleResolver.getLocaleSessionAttributeName(), locale);
        ResourceBundleProvider.updateLocale(locale);

        UserAccountBean ub = (UserAccountBean) request.getSession().getAttribute("userBean");
        if (ub == null)
            return new ResponseEntity<Object>("Not permitted.", HttpStatus.FORBIDDEN);

        // Get public study
        StudyDAO studyDAO = new StudyDAO(dataSource);
        StudyBean currentPublicStudy = studyDAO.findByStudyEnvUuid(studyEnvUuid);
        // Get tenant study
        String tenantSchema = currentPublicStudy.getSchemaName();
        CoreResources.setRequestSchema(request, tenantSchema);
        StudyBean currentStudy = studyDAO.findByStudyEnvUuid(studyEnvUuid);
        // Validate study exists
        if (currentPublicStudy == null || currentStudy == null) {
            ErrorObject errorObject = createErrorObject("Study Object", "Missing or invalid", "studyEnvUuid");
            errorObjects.add(errorObject);
            studyDTO.setErrors(errorObjects);
            studyDTO.setMessage(validation_failed_message);
            return new ResponseEntity(studyDTO, org.springframework.http.HttpStatus.BAD_REQUEST);
        }
        if (ub != null){
            if (!isDataManagerOrStudyDirector(ub,currentPublicStudy)){
                return new ResponseEntity<Object>("Not permitted.", HttpStatus.FORBIDDEN);
            }
        }
        // Get Status object from requestDTO
        Status status = getStatus((String) requestDTO.get("status"));
        // Validate status field
        if (status == null ) {
            ErrorObject errorObject = createErrorObject("Study Object", "Missing Field", "status");
            errorObjects.add(errorObject);
            studyDTO.setErrors(errorObjects);
            studyDTO.setMessage(validation_failed_message);
            return new ResponseEntity(studyDTO, org.springframework.http.HttpStatus.BAD_REQUEST);
        } else if (!status.equals(Status.PENDING)
                && !status.equals(Status.AVAILABLE)
                && !status.equals(Status.FROZEN)
                && !status.equals(Status.LOCKED) ){
            ErrorObject errorObject = createErrorObject("Study Object", "Invalid status", "status");
            errorObjects.add(errorObject);
            studyDTO.setErrors(errorObjects);
            studyDTO.setMessage(validation_failed_message);
            return new ResponseEntity(studyDTO, org.springframework.http.HttpStatus.BAD_REQUEST);
        }


        // Update tenant study & sites
        currentStudy.setOldStatus(currentStudy.getStatus());
        currentStudy.setStatus(status);
        studyDAO.updateStudyStatus(currentStudy);
        ArrayList siteList = (ArrayList) studyDAO.findAllByParent(currentStudy.getId());
        if (siteList.size() > 0) {
            studyDAO.updateSitesStatus(currentStudy);
        }

        // Update public study & sites
        CoreResources.setRequestSchema(request, "public");
        currentPublicStudy.setOldStatus(currentPublicStudy.getStatus());
        currentPublicStudy.setStatus(status);
        studyDAO.updateStudyStatus(currentPublicStudy);
        ArrayList publicSiteList = (ArrayList) studyDAO.findAllByParent(currentPublicStudy.getId());
        if (publicSiteList.size() > 0) {
            studyDAO.updateSitesStatus(currentPublicStudy);
        }

        StudyEnvStatusDTO studyEnvStatusDTO = new StudyEnvStatusDTO();
        studyEnvStatusDTO.setStudyEnvUuid(currentPublicStudy.getStudyEnvUuid());
        studyEnvStatusDTO.setStatus(currentPublicStudy.getStatus().getName());
        ArrayList updatedPublicSiteList = (ArrayList) studyDAO.findAllByParent(currentPublicStudy.getId());
        for(StudyBean site:  (ArrayList<StudyBean>)updatedPublicSiteList){
            SiteStatusDTO siteStatusDTO = new SiteStatusDTO();
            siteStatusDTO.setSiteUuid(site.getStudyEnvSiteUuid());
            siteStatusDTO.setStatus(site.getStatus().getName());
            studyEnvStatusDTO.getSiteStatuses().add(siteStatusDTO);
        }

        return  new ResponseEntity(studyEnvStatusDTO, org.springframework.http.HttpStatus.OK);
    }

    private StudyParameterConfig processStudyConfigParameters(HashMap<String, Object> map, ArrayList<ErrorObject> errorObjects) {
        StudyParameterConfig spc = new StudyParameterConfig();
        String collectBirthDate = (String) map.get("collectDateOfBirth");
        Boolean collectSex = (Boolean) map.get("collectSex");
        String collectPersonId = (String) map.get("collectPersonId");
        if (collectBirthDate == null) {
            ErrorObject errorObject = createErrorObject("Study Object", "Missing Field", "CollectBirthDate");
            errorObjects.add(errorObject);
        } else {
            collectBirthDate = collectBirthDate.trim();
        }
        spc.setCollectDob(collectBirthDate);
        if (collectSex == null) {
            ErrorObject errorObject = createErrorObject("Study Object", "Missing Field", "CollectSex");
            errorObjects.add(errorObject);
        }
        spc.setGenderRequired(Boolean.toString(collectSex));
        if (collectPersonId == null) {
            ErrorObject errorObject = createErrorObject("Study Object", "Missing Field", "CollectPersonId");
            errorObjects.add(errorObject);
        } else {
            collectPersonId = collectPersonId.trim();
        }
        spc.setSubjectPersonIdRequired(collectPersonId);
        return spc;
    }

    @RequestMapping(value = "/", method = RequestMethod.PUT)
    public ResponseEntity<Object> UpdateStudy(HttpServletRequest request,
                                              @RequestBody HashMap<String, Object> map) throws Exception {
        ArrayList<ErrorObject> errorObjects = new ArrayList();
        StudyDTO studyDTO = new StudyDTO();
        logger.info("In Update Study Settings");
        ResponseEntity<Object> response = null;

        StudyParameters parameters = new StudyParameters(map);
        parameters.setParameters();
        errorObjects = parameters.validateParameters(request);

        // get the study to update
        CoreResources.setRequestSchema(request, "public");
        Study existingStudy = studyDao.findByStudyEnvUuid(parameters.studyEnvUuid);
        if (existingStudy == null) {
            ErrorObject errorObject = createErrorObject("Study Object", "Missing Study", "studyEnvUuid");
            errorObjects.add(errorObject);
        }

        if (errorObjects != null && errorObjects.size() != 0) {
            studyDTO.setErrors(errorObjects);
            studyDTO.setMessage(validation_failed_message);
            response = new ResponseEntity(studyDTO, org.springframework.http.HttpStatus.BAD_REQUEST);
            return response;
        }
        setChangeableStudySettings(existingStudy, parameters);
        studyDao.saveOrUpdate(existingStudy);
        String schema = existingStudy.getSchemaName();
        CoreResources.setRequestSchema(request, schema);
        Study schemaStudy = studyDao.findByStudyEnvUuid(existingStudy.getStudyEnvUuid());
        setChangeableStudySettings(schemaStudy, parameters);
        updateStudyConfigParameters(request, schemaStudy, parameters.studyParameterConfig);
        ResponseSuccessStudyDTO responseSuccess = new ResponseSuccessStudyDTO();
        responseSuccess.setMessage(validation_passed_message);
        responseSuccess.setStudyOid(schemaStudy.getOc_oid());
        responseSuccess.setUniqueStudyID(schemaStudy.getUniqueIdentifier());
        responseSuccess.setSchemaName(schema);
        response = new ResponseEntity(responseSuccess, org.springframework.http.HttpStatus.OK);
        return response;
    }


    private class StudyParameters {
        HashMap<String, Object> map;
        String uniqueStudyID;
        String name;
        String studyOid;
        String studyEnvUuid;
        String description;
        String studyType;
        String phase;
        String startDateStr;
        String endDateStr;
        Integer expectedTotalEnrollment;
        Date startDate;
        Date endDate;
        StudyParameterConfig studyParameterConfig;
        org.akaza.openclinica.domain.Status status;


        public StudyParameters(HashMap<String, Object> map) {
            this.map = map;
        }

        void setParameters() {
            uniqueStudyID = (String) map.get("uniqueStudyID");
            name = (String) map.get("briefTitle");
            studyOid = (String) map.get("studyEnvOid");
            studyEnvUuid = (String) map.get("studyEnvUuid");
            description = (String) map.get("description");
            studyType = (String) map.get("type");
            phase = (String) map.get("phase");
            startDateStr = (String) map.get("expectedStartDate");
            endDateStr = (String) map.get("expectedEndDate");
            expectedTotalEnrollment = (Integer) map.get("expectedTotalEnrollment");
            status = setStatus((String) map.get("status"));
        }

        org.akaza.openclinica.domain.Status setStatus(String myStatus) {

            // set status object if no status pass default it to "PENDING"
            org.akaza.openclinica.domain.Status statusObj = org.akaza.openclinica.domain.Status.PENDING;

            if (myStatus != null) {
                myStatus = myStatus.equals("DESIGN") ? "PENDING" : myStatus;
                statusObj = org.akaza.openclinica.domain.Status.getByName(myStatus);
            }
            return statusObj;
        }

        ArrayList<ErrorObject> validateParameters(HttpServletRequest request) throws ParseException {
            ArrayList<ErrorObject> errorObjects = new ArrayList();

            if (StringUtils.isEmpty(uniqueStudyID)) {
                ErrorObject errorObject = createErrorObject("Study Object", "Missing Field", "UniqueStudyID");
                errorObjects.add(errorObject);
            } else {
                uniqueStudyID = uniqueStudyID.trim();
            }
            if (StringUtils.isEmpty(name)) {
                ErrorObject errorObject = createErrorObject("Study Object", "Missing Field", "BriefTitle");
                errorObjects.add(errorObject);
            } else {
                name = name.trim();
            }
            if (description == null) {
                ErrorObject errorObject = createErrorObject("Study Object", "Missing Field", "Description");
                errorObjects.add(errorObject);
            } else {
                description = description.trim();
            }

            if (expectedTotalEnrollment == null) {
                ErrorObject errorObject = createErrorObject("Study Object", "Missing Field", "ExpectedTotalEnrollment");
                errorObjects.add(errorObject);
            }

            if (startDateStr == null) {
                ErrorObject errorObject = createErrorObject("Study Object", "Missing Field", "StartDate");
                errorObjects.add(errorObject);
            } else {
                startDateStr = startDateStr.trim();
            }
            startDate = formatDateString(startDateStr, "StartDate", errorObjects);

            if (endDateStr == null) {
                ErrorObject errorObject = createErrorObject("Study Object", "Missing Field", "EndDate");
                errorObjects.add(errorObject);
            } else {
                endDateStr = endDateStr.trim();
            }
            endDate = formatDateString(endDateStr, "EndDate", errorObjects);

            if (studyType != null) {
                studyType = studyType.toLowerCase();
                if (!verifyStudyTypeExist(studyType)) {
                    ErrorObject errorObject = createErrorObject("Study Object", "Study Type is not Valid", "StudyType");
                    errorObjects.add(errorObject);
                }
            }

            if (StringUtils.isEmpty(studyOid)) {
                ErrorObject errorObject = createErrorObject("Study Object", "Missing Field", "oid");
                errorObjects.add(errorObject);
            } else {
                studyOid = studyOid.trim();
            }

            if (StringUtils.isEmpty(studyEnvUuid)) {
                ErrorObject errorObject = createErrorObject("Study Object", "Missing Field", "studyEnvUuid");
                errorObjects.add(errorObject);
            } else {
                studyEnvUuid = studyEnvUuid.trim();
            }

            if (status == null ) {
                ErrorObject errorObject = createErrorObject("Study Object", "Missing Field", "status");
                errorObjects.add(errorObject);
            } else if (!status.equals(org.akaza.openclinica.domain.Status.PENDING)
                    && !status.equals(org.akaza.openclinica.domain.Status.AVAILABLE)
                    && !status.equals(org.akaza.openclinica.domain.Status.FROZEN)
                    && !status.equals(org.akaza.openclinica.domain.Status.LOCKED) ){
                ErrorObject errorObject = createErrorObject("Study Object", "Invalid status", "status");
                errorObjects.add(errorObject);
            }

            studyParameterConfig = processStudyConfigParameters(map, errorObjects);
            Locale locale = new Locale("en_US");
            request.getSession().setAttribute(LocaleResolver.getLocaleSessionAttributeName(), locale);
            ResourceBundleProvider.updateLocale(locale);

            request.setAttribute("uniqueStudyID", uniqueStudyID);
            request.setAttribute("name", name); // Brief Title
            request.setAttribute("oid", studyOid);
            request.setAttribute("studyEnvUuid", studyEnvUuid);
            Validator v0 = new Validator(request);
            v0.addValidation("name", Validator.NO_BLANKS);

            HashMap vError0 = v0.validate();
            if (!vError0.isEmpty()) {
                ErrorObject errorObject = createErrorObject("Study Object", "This field cannot be blank.", "BriefTitle");
                errorObjects.add(errorObject);
            }

            Validator v1 = new Validator(request);
            v1.addValidation("uniqueStudyID", Validator.NO_BLANKS);
            HashMap vError1 = v1.validate();
            if (!vError1.isEmpty()) {
                ErrorObject errorObject = createErrorObject("Study Object", "This field cannot be blank.", "UniqueStudyId");
                errorObjects.add(errorObject);
            }

            Validator v2 = new Validator(request);
            v2.addValidation("oid", Validator.NO_BLANKS);
            HashMap vError2 = v2.validate();
            if (!vError2.isEmpty()) {
                ErrorObject errorObject = createErrorObject("Study Object", "This field cannot be blank.", "oid");
                errorObjects.add(errorObject);
            }

            return errorObjects;
        }
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ResponseEntity<Object> createNewStudy(HttpServletRequest request,
                                                 @RequestBody HashMap<String, Object> map) throws Exception {
        StudyDTO studyDTO = new StudyDTO();
        logger.info("In Create Study");
        ResponseEntity<Object> response = null;

        StudyParameters parameters = new StudyParameters(map);
        parameters.setParameters();
        ArrayList<ErrorObject> errorObjects = parameters.validateParameters(request);
        Matcher m = Pattern.compile("(.+)\\((.+)\\)").matcher(parameters.studyOid);
        String envType = "";
        if (m.find()) {
            if (m.groupCount() != 2) {
                ErrorObject errorObject = createErrorObject("Study Object", "Missing Field", "envType");
                errorObjects.add(errorObject);
            } else {
                envType = m.group(2).toUpperCase();
            }
        }

        AsyncStudyHelper asyncStudyHelper = new AsyncStudyHelper("Study Creation Started", "PENDING", LocalTime.now());
        AsyncStudyHelper.put(parameters.uniqueStudyID, asyncStudyHelper);

        ResponseEntity<HashMap> responseEntity = processSSOUserContext(request, parameters.studyEnvUuid);

        UserAccountBean ownerUserAccount = getStudyOwnerAccountWithCreatedUser(request, responseEntity);
        if (ownerUserAccount == null) {
            ErrorObject errorObject = createErrorObject("Study Object", "The Owner User Account is not Valid Account or Does not have Admin user type",
                    "Owner Account");
            errorObjects.add(errorObject);

        }

        Validator v4 = new Validator(request);
        v4.addValidation("role", Validator.NO_LEADING_OR_TRAILING_SPACES);
        HashMap vError4 = v4.validate();
        if (!vError4.isEmpty()) {
            ErrorObject errorObject = createErrorObject("Study Object", "This field cannot have leading or trailing spaces.", "role");
            errorObjects.add(errorObject);
        }

        if (errorObjects != null && errorObjects.size() != 0) {
            studyDTO.setErrors(errorObjects);
            studyDTO.setMessage(validation_failed_message);
            response = new ResponseEntity(studyDTO, org.springframework.http.HttpStatus.BAD_REQUEST);
            return response;
        }

        Study study = new Study();
        setChangeableStudySettings(study, parameters);
        study.setEnvType(StudyEnvEnum.valueOf(envType));
        Study byOidEnvType = studyDao.findByOidEnvType(parameters.studyOid, StudyEnvEnum.valueOf(envType));
        if (byOidEnvType != null && byOidEnvType.getOc_oid() != null) {
            return getResponseSuccess(byOidEnvType);
        }
        Study schemaStudy = createSchemaStudy(request, study, ownerUserAccount);
        setStudyConfigParameters(request, study, schemaStudy, parameters.studyParameterConfig);
        logger.debug("returning from liquibase study:" + schemaStudy.getStudyId());

        if (errorObjects != null && errorObjects.size() != 0) {
            studyDTO.setMessage(validation_failed_message);
            response = new ResponseEntity(studyDTO, org.springframework.http.HttpStatus.BAD_REQUEST);
        } else {
            studyDTO.setStudyOid(schemaStudy.getOc_oid());
            studyDTO.setMessage(validation_passed_message);
            studyDTO.setUniqueProtocolID(schemaStudy.getUniqueIdentifier());
            logger.debug("study oc_id:" + schemaStudy.getOc_oid());

            ResponseSuccessStudyDTO responseSuccess = new ResponseSuccessStudyDTO();
            responseSuccess.setMessage(studyDTO.getMessage());
            responseSuccess.setStudyOid(studyDTO.getStudyOid());
            responseSuccess.setUniqueStudyID(studyDTO.getUniqueProtocolID());
            responseSuccess.setSchemaName(study.getSchemaName());
            response = new ResponseEntity(responseSuccess, org.springframework.http.HttpStatus.OK);
        }
        request.getSession().setAttribute("userContextMap", null);
        AsyncStudyHelper asyncStudyDone = new AsyncStudyHelper("Finished creating study", "ACTIVE");
        AsyncStudyHelper.put(parameters.uniqueStudyID, asyncStudyDone);

        return response;

    }

    private void setChangeableStudySettings(Study study, StudyParameters parameters) {
        study.setUniqueIdentifier(parameters.uniqueStudyID);
        study.setName(parameters.name);
        study.setOc_oid(parameters.studyOid);
        study.setStudyEnvUuid(parameters.studyEnvUuid);
        study.setPhase(parameters.phase);
        study.setDatePlannedStart(parameters.startDate);
        study.setDatePlannedEnd(parameters.endDate);
        study.setExpectedTotalEnrollment(parameters.expectedTotalEnrollment);
        study.setProtocolType(parameters.studyType.toLowerCase());
        if(study.getStatus() != null){
            study.setOldStatusId(study.getStatus().getCode());
        }
        study.setStatus(parameters.status);
    }

    private Study createSchemaStudy(HttpServletRequest request, Study study, UserAccountBean ownerUserAccount) throws Exception {
        StudyInfoObject studyInfoObject = null;
        Study schemaStudy = null;
        try {

            studyInfoObject = studyBuildService.process(request, study, ownerUserAccount);
            liquibaseOnDemandService.createForeignTables(studyInfoObject);
            schemaStudy = liquibaseOnDemandService.process(studyInfoObject, studyInfoObject.getUb());
        } catch (Exception e) {
            try {
                schemaCleanupService.dropSchema(studyInfoObject);
            } catch (Exception schemaEx) {
                throw new Exception("Schema cleanup failed.");
            }
            throw e;
        }
        return schemaStudy;
    }

    private void updateStudyConfigParameters(HttpServletRequest request, Study schemaStudy, StudyParameterConfig studyParameterConfig) {
        List<StudyParameterValue> studyParameterValues = schemaStudy.getStudyParameterValues();

        for (StudyParameterValue spv : studyParameterValues) {
            switch (spv.getStudyParameter().getHandle()) {
                case "collectDob":
                    String collectDobValue;
                    if (StringUtils.isEmpty(studyParameterConfig.getCollectDob())) {
                        collectDobValue = "3";
                    } else {
                        switch (studyParameterConfig.getCollectDob().toLowerCase()) {
                            case "always":
                                collectDobValue = "1";
                                break;
                            case "only the year":
                                collectDobValue = "2";
                                break;
                            default:
                                collectDobValue = "3";
                                break;
                        }
                    }
                    spv.setValue(collectDobValue);
                    break;
                case "discrepancyManagement":
                    spv.setValue(studyParameterConfig.getDiscrepancyManagement());
                    break;
                case "genderRequired":
                    spv.setValue(studyParameterConfig.getGenderRequired());
                    break;
                case "subjectPersonIdRequired":
                    spv.setValue(handlePersonIdRequired(studyParameterConfig.getSubjectPersonIdRequired()));
                    break;
                case "interviewerNameRequired":
                    spv.setValue(studyParameterConfig.getInterviewerNameRequired());
                    break;
                case "interviewerNameEditable":
                    spv.setValue(studyParameterConfig.getInterviewerNameEditable());
                    break;
                case "interviewDateRequired":
                    spv.setValue(studyParameterConfig.getInterviewDateRequired());
                    break;
                case "interviewDateDefault":
                    spv.setValue(studyParameterConfig.getInterviewDateDefault());
                    break;
                case "interviewDateEditable":
                    spv.setValue(studyParameterConfig.getInterviewDateEditable());
                    break;
                case "subjectIdGeneration":
                    spv.setValue(studyParameterConfig.getSubjectIdGeneration());
                    break;
                case "subjectIdPrefixSuffix":
                    spv.setValue(studyParameterConfig.getSubjectIdPrefixSuffix());
                    break;
                case "personIdShownOnCRF":
                    spv.setValue(studyParameterConfig.getPersonIdShownOnCRF());
                    break;

            }
        }
        studyDao.saveOrUpdate(schemaStudy);
    }

    private String handlePersonIdRequired(String input) {
        String outputStr = "";
        switch (input.toLowerCase()) {
            case "always":
                outputStr = "required";
                break;
            case "optional":
                outputStr = "optional";
                break;
            case "never":
                outputStr = "never";
                break;
            default:
                break;
        }
        return outputStr;
    }

    private void setStudyConfigParameters(HttpServletRequest request, Study study, Study schemaStudy, StudyParameterConfig studyParameterConfig) {
        String schema = CoreResources.getRequestSchema(request);
        CoreResources.setRequestSchema(request, study.getSchemaName());
        List<StudyParameterValue> studyParameterValues = new ArrayList<>();

        schemaStudy.setStudyParameterValues(studyParameterValues);
        StudyParameterValue collectDobValue = new StudyParameterValue();
        collectDobValue.setStudy(schemaStudy);
        StudyParameter collectDob = studyParameterDao.findByHandle("collectDob");
        collectDobValue.setStudyParameter(collectDob);
        if (StringUtils.isEmpty(studyParameterConfig.getCollectDob())) {
            collectDobValue.setValue("3");
        } else {
            switch (studyParameterConfig.getCollectDob().toLowerCase()) {
                case "always":
                    collectDobValue.setValue("1");
                    break;
                case "only the year":
                    collectDobValue.setValue("2");
                    break;
                default:
                    collectDobValue.setValue("3");
                    break;
            }
        }
        studyParameterValues.add(collectDobValue);

        StudyParameterValue discrepancyManagementValue = new StudyParameterValue();
        discrepancyManagementValue.setStudy(schemaStudy);
        StudyParameter discrepancyManagement = studyParameterDao.findByHandle("discrepancyManagement");
        discrepancyManagementValue.setStudyParameter(discrepancyManagement);
        discrepancyManagementValue.setValue(studyParameterConfig.getDiscrepancyManagement());
        studyParameterValues.add(discrepancyManagementValue);

        StudyParameterValue genderRequiredValue = new StudyParameterValue();
        genderRequiredValue.setStudy(schemaStudy);
        StudyParameter genderRequired = studyParameterDao.findByHandle("genderRequired");
        genderRequiredValue.setStudyParameter(genderRequired);
        genderRequiredValue.setValue(studyParameterConfig.getGenderRequired());
        studyParameterValues.add(genderRequiredValue);

        StudyParameterValue subjectPersonIdRequiredValue = new StudyParameterValue();
        subjectPersonIdRequiredValue.setStudy(schemaStudy);
        StudyParameter subjectPersonIdRequired = studyParameterDao.findByHandle("subjectPersonIdRequired");
        subjectPersonIdRequiredValue.setStudyParameter(subjectPersonIdRequired);
        subjectPersonIdRequiredValue.setValue(handlePersonIdRequired(studyParameterConfig.getSubjectPersonIdRequired()));
        studyParameterValues.add(subjectPersonIdRequiredValue);

        StudyParameterValue interviewerNameRequiredValue = new StudyParameterValue();
        interviewerNameRequiredValue.setStudy(schemaStudy);
        StudyParameter interviewerNameRequired = studyParameterDao.findByHandle("interviewerNameRequired");
        interviewerNameRequiredValue.setStudyParameter(interviewerNameRequired);
        interviewerNameRequiredValue.setValue(studyParameterConfig.getInterviewerNameRequired());
        studyParameterValues.add(interviewerNameRequiredValue);

        StudyParameterValue interviewerNameEditableValue = new StudyParameterValue();
        interviewerNameEditableValue.setStudy(schemaStudy);
        StudyParameter interviewerNameEditable = studyParameterDao.findByHandle("interviewerNameEditable");
        interviewerNameEditableValue.setStudyParameter(interviewerNameEditable);
        interviewerNameEditableValue.setValue(studyParameterConfig.getInterviewerNameEditable());
        studyParameterValues.add(interviewerNameEditableValue);

        StudyParameterValue interviewDateRequiredValue = new StudyParameterValue();
        interviewDateRequiredValue.setStudy(schemaStudy);
        StudyParameter interviewDateRequired = studyParameterDao.findByHandle("interviewDateRequired");
        interviewDateRequiredValue.setStudyParameter(interviewDateRequired);
        interviewDateRequiredValue.setValue(studyParameterConfig.getInterviewDateRequired());
        studyParameterValues.add(interviewDateRequiredValue);

        StudyParameterValue interviewDateDefaultValue = new StudyParameterValue();
        interviewDateDefaultValue.setStudy(schemaStudy);
        StudyParameter interviewDateDefault = studyParameterDao.findByHandle("interviewDateDefault");
        interviewDateDefaultValue.setStudyParameter(interviewDateDefault);
        interviewDateDefaultValue.setValue(studyParameterConfig.getInterviewDateDefault());
        studyParameterValues.add(interviewDateDefaultValue);

        StudyParameterValue interviewDateEditableValue = new StudyParameterValue();
        interviewDateEditableValue.setStudy(schemaStudy);
        StudyParameter interviewDateEditable = studyParameterDao.findByHandle("interviewDateEditable");
        interviewDateEditableValue.setStudyParameter(interviewDateEditable);
        interviewDateEditableValue.setValue(studyParameterConfig.getInterviewDateEditable());
        studyParameterValues.add(interviewDateEditableValue);

        StudyParameterValue subjectIdGenerationValue = new StudyParameterValue();
        subjectIdGenerationValue.setStudy(schemaStudy);
        StudyParameter subjectIdGeneration = studyParameterDao.findByHandle("subjectIdGeneration");
        subjectIdGenerationValue.setStudyParameter(subjectIdGeneration);
        subjectIdGenerationValue.setValue(studyParameterConfig.getSubjectIdGeneration());
        studyParameterValues.add(subjectIdGenerationValue);

        StudyParameterValue subjectIdPrefixSuffixValue = new StudyParameterValue();
        subjectIdPrefixSuffixValue.setStudy(schemaStudy);
        StudyParameter subjectIdPrefixSuffix = studyParameterDao.findByHandle("subjectIdPrefixSuffix");
        subjectIdPrefixSuffixValue.setStudyParameter(subjectIdPrefixSuffix);
        subjectIdPrefixSuffixValue.setValue(studyParameterConfig.getSubjectIdPrefixSuffix());
        studyParameterValues.add(subjectIdPrefixSuffixValue);

        StudyParameterValue personIdShownOnCRFValue = new StudyParameterValue();
        personIdShownOnCRFValue.setStudy(schemaStudy);
        StudyParameter personIdShownOnCRF = studyParameterDao.findByHandle("personIdShownOnCRF");
        personIdShownOnCRFValue.setStudyParameter(personIdShownOnCRF);
        personIdShownOnCRFValue.setValue(studyParameterConfig.getPersonIdShownOnCRF());
        studyParameterValues.add(personIdShownOnCRFValue);
        studyDao.saveOrUpdate(schemaStudy);
        if (StringUtils.isNotEmpty(schema))
            CoreResources.setRequestSchema(request, schema);
    }

    private Date formatDateString(String dateStr, String fieldName, List<ErrorObject> errorObjects) throws ParseException {
        String format = "yyyy-MM-dd";
        SimpleDateFormat formatter = null;
        Date formattedDate = null;
        if (dateStr != "" && dateStr != null) {
            try {
                formatter = new SimpleDateFormat(format);
                formattedDate = formatter.parse(dateStr);
            } catch (ParseException e) {
                ErrorObject errorObject = createErrorObject("Study Object",
                        "The StartDate format is not a valid 'yyyy-MM-dd' format", "fieldName");
                errorObjects.add(errorObject);
            }
            if (formattedDate != null) {
                if (!dateStr.equals(formatter.format(formattedDate))) {
                    ErrorObject errorObject = createErrorObject("Study Object",
                            "The StartDate format is not a valid 'yyyy-MM-dd' format", fieldName);
                    errorObjects.add(errorObject);
                }
            }
        }
        return formattedDate;
    }

    private ResponseEntity<Object> getResponseSuccess(Study existingStudy) {

        ResponseSuccessStudyDTO responseSuccess = new ResponseSuccessStudyDTO();
        responseSuccess.setMessage("Existing Study");
        responseSuccess.setStudyOid(existingStudy.getOc_oid());
        responseSuccess.setUniqueStudyID(existingStudy.getUniqueIdentifier());
        responseSuccess.setSchemaName(existingStudy.getSchemaName());
        ResponseEntity<Object> response = new ResponseEntity(responseSuccess, HttpStatus.SEE_OTHER);
        return response;
    }

    private ResponseEntity<HashMap> processSSOUserContext(HttpServletRequest request, String studyEnvUuid) throws Exception {
        ResponseEntity<HashMap> responseEntity = null;
        HttpSession session = request.getSession();
        if (session == null) {
            logger.error("Cannot proceed without a valid session.");
            return responseEntity;
        }
        Map<String, Object> userContextMap = (LinkedHashMap<String, Object>) session.getAttribute("userContextMap");
        if (userContextMap == null)
            return responseEntity;
        ResponseEntity<StudyEnvironmentRoleDTO[]> studyUserRoles = studyBuildService.getUserRoles(request);
        HashMap<String, String> userMap = getUserInfo(request, userContextMap, studyUserRoles);
        UserAccountBean ub = (UserAccountBean) request.getSession().getAttribute("userBean");

        if ((ub == null || ub.getId() == 0) ||
                (userMap.get("username") != null &&
                        StringUtils.equals(ub.getName(), userMap.get("username")) != true)) {
            // we need to create the user
            try {
                responseEntity = userAccountController.createOrUpdateAccount(request, userMap);
                request.getSession().setAttribute("userBean", request.getAttribute("createdUaBean"));
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage());
                throw e;
            }
        } else {
            HashMap<String, Object> userDTO = new HashMap<String, Object>();
            userDTO.put("username", ub.getName());
            userDTO.put("password", ub.getPasswd());
            userDTO.put("firstName", ub.getFirstName());
            userDTO.put("lastName", ub.getLastName());
            userDTO.put("apiKey", ub.getApiKey());
            responseEntity = new ResponseEntity<HashMap>(userDTO, org.springframework.http.HttpStatus.OK);
        }
        return responseEntity;
    }

    private HashMap<String, String> getUserInfo(HttpServletRequest request, Map<String, Object> userContextMap, ResponseEntity<StudyEnvironmentRoleDTO[]> studyUserRoles) throws Exception {
        String studyEnvUuid = (String) request.getAttribute("studyEnvUuid");
        HashMap<String, String> map = new HashMap<>();
        ArrayList<LinkedHashMap<String, String>> roles = new ArrayList<>();

        for (StudyEnvironmentRoleDTO role : studyUserRoles.getBody()) {
            LinkedHashMap<String, String> studyRole = new LinkedHashMap<>();
            studyRole.put("roleName", role.getRoleName());
            studyRole.put("studyEnvUuid", role.getStudyEnvironmentUuid());
            roles.add(studyRole);
            if (role.getStudyEnvironmentUuid().equals(studyEnvUuid)) {
                map.put("role_name", role.getRoleName());
                UserAccountBean ub = (UserAccountBean) request.getSession().getAttribute("userBean");
                String userUuid = (String) userContextMap.get("userUuid");
                if ((ub == null || ub.getId() == 0)
                        || (StringUtils.isNotEmpty(userUuid) &&
                        StringUtils.equals(ub.getUserUuid(), userUuid) != true)) {
                    ResponseEntity<OCUserDTO> userInfo = studyBuildService.getUserDetails(request);
                    if (userInfo == null)
                        return null;
                    OCUserDTO userDTO = userInfo.getBody();
                    map.put("email", userDTO.getEmail());
                    if (StringUtils.isEmpty(userDTO.getOrganization()))
                        map.put("institution", "");
                    else
                        map.put("institution", userDTO.getOrganization());
                    map.put("fName", userDTO.getFirstName());
                    map.put("lName", userDTO.getLastName());
                    map.put("user_uuid", userDTO.getUuid());
                    map.put("username", userDTO.getUsername());
                } else {
                    map.put("email", ub.getEmail());
                    map.put("institution", ub.getInstitutionalAffiliation());
                    map.put("fName", ub.getFirstName());
                    map.put("lName", ub.getLastName());
                    map.put("user_uuid", ub.getUserUuid());
                }
            }
        }
        userContextMap.put("roles", roles);
        switch ((String) userContextMap.get("userType")) {
            case "Business Admin":
                map.put("user_type", UserType.SYSADMIN.getName());
                break;
            case "Tech Admin":
                map.put("user_type", UserType.TECHADMIN.getName());
                break;
            case "User":
                map.put("user_type", UserType.USER.getName());
                break;
            default:
                String error = "Invalid userType:" + (String) userContextMap.get("userType");
                logger.error(error);
                throw new Exception(error);
        }
        map.put("authorize_soap", "false");
        return map;
    }

    @RequestMapping(value = "/asyncStudyStatus", method = RequestMethod.GET)
    public ResponseEntity<Object> getAyncStudyStatus(HttpServletRequest request,
                                                     @RequestParam("uniqueId") String uniqueId) throws Exception {
        ResponseEntity<Object> response;

        AsyncStudyHelper asyncStudyHelper = AsyncStudyHelper.get(uniqueId);
        if (asyncStudyHelper != null) {
            response = new ResponseEntity<Object>(asyncStudyHelper, HttpStatus.OK);
        } else {
            // database lookup
            Study s = studyDao.findByColumnName(uniqueId, "uniqueIdentifier");
            HttpStatus httpStatus;
            if (s != null && StringUtils.isNotEmpty(s.getSchemaName())) {
                if (studyDao.doesStudyExist(uniqueId, s.getSchemaName())) {
                    asyncStudyHelper = new AsyncStudyHelper("Study Found", "ACTIVE");
                    httpStatus = HttpStatus.OK;
                } else {
                    asyncStudyHelper = new AsyncStudyHelper("Study Not Found", "ERROR");
                    httpStatus = HttpStatus.NOT_FOUND;
                }
            } else {
                asyncStudyHelper = new AsyncStudyHelper("Study Not Found", "ERROR");
                httpStatus = HttpStatus.NOT_FOUND;
            }
            response = new ResponseEntity<Object>(asyncStudyHelper, httpStatus);
        }

        return response;
    }

    private FacilityInfo processFacilityInfo(HashMap<String, Object> map) {
        FacilityInfo facilityInfo = new FacilityInfo();
        String facilityCity = (String) map.get("facilityCity");
        String facilityState = (String) map.get("facilityState");
        String facilityZip = (String) map.get("facilityZip");
        String facilityCountry = (String) map.get("facilityCountry");
        String facilityContact = (String) map.get("facilityContact");
        String facilityEmail = (String) map.get("facilityEmail");
        String facilityPhone = (String) map.get("facilityPhone");

        if (StringUtils.isNotEmpty(facilityCity))
            facilityInfo.setFacilityCity(facilityCity.trim());
        else
            facilityInfo.setFacilityCity("");
        if (StringUtils.isNotEmpty(facilityState))
            facilityInfo.setFacilityState(facilityState.trim());
        else
            facilityInfo.setFacilityState("");
        if (StringUtils.isNotEmpty(facilityZip))
            facilityInfo.setFacilityZip(facilityZip.trim());
        else
            facilityInfo.setFacilityZip("");
        if (StringUtils.isNotEmpty(facilityCountry))
            facilityInfo.setFacilityCountry(facilityCountry.trim());
        else
            facilityInfo.setFacilityCountry("");
        if (StringUtils.isNotEmpty(facilityContact))
            facilityInfo.setFacilityContact(facilityContact.trim());
        else
            facilityInfo.setFacilityContact("");
        if (StringUtils.isNotEmpty(facilityEmail))
            facilityInfo.setFacilityEmail(facilityEmail.trim());
        else
            facilityInfo.setFacilityEmail("");
        if (StringUtils.isNotEmpty(facilityPhone))
            facilityInfo.setFacilityPhone(facilityPhone.trim());
        else
            facilityInfo.setFacilityPhone("");

        return facilityInfo;
    }

    private class SiteParameters {
        String name;
        String principalInvestigator;
        String uniqueIdentifier;
        Integer expectedTotalEnrollment;
        String studyEnvSiteUuid;
        String ocOid;
        String statusStr;
        FacilityInfo facilityInfo;
        String studyVerificationDate;
        String startDate;
        HashMap<String, Object> map;
        Status status;
        StudyBean parentStudy;
        String studyEnvUuid;
        UserAccountBean ownerUserAccount = null;
        Date formattedStartDate = null;
        Date formattedStudyDate = null;

        public SiteParameters(HashMap<String, Object> map, String studyEnvUuid) {
            this.map = map;
            this.studyEnvUuid = studyEnvUuid;
        }

        private void setParameters() {
            name = (String) map.get("briefTitle");
            principalInvestigator = (String) map.get("principalInvestigator");
            uniqueIdentifier = (String) map.get("uniqueIdentifier");
            expectedTotalEnrollment = (Integer) map.get("expectedTotalEnrollment");
            studyEnvSiteUuid = (String) map.get("studyEnvSiteUuid");
            ocOid = (String) map.get("ocOid");
            statusStr = (String) map.get("status");
            facilityInfo = processFacilityInfo(map);
            studyVerificationDate = (String) map.get("studyVerificationDate");
            startDate = (String) map.get("startDate");
        }

        ArrayList<ErrorObject> validateParameters(HttpServletRequest request) throws ParseException {
            ArrayList<ErrorObject> errorObjects = new ArrayList();


            if (uniqueIdentifier == null) {
                ErrorObject errorObject = createErrorObject("Site Object", "Missing Field", "uniqueIdentifier");
                errorObjects.add(errorObject);
            } else {
                uniqueIdentifier = uniqueIdentifier.trim();
            }
            if (name == null) {
                ErrorObject errorObject = createErrorObject("Site Object", "Missing Field", "BriefTitle");
                errorObjects.add(errorObject);
            } else {
                name = name.trim();
            }
            if (principalInvestigator == null) {
                ErrorObject errorObject = createErrorObject("Site Object", "Missing Field", "PrincipalInvestigator");
                errorObjects.add(errorObject);
            } else {
                principalInvestigator = principalInvestigator.trim();
            }

            if (expectedTotalEnrollment == null) {
                ErrorObject errorObject = createErrorObject("Site Object", "Missing Field", "ExpectedTotalEnrollment");
                errorObjects.add(errorObject);
            }

            if (studyEnvSiteUuid == null) {
                ErrorObject errorObject = createErrorObject("Site Object", "Missing Field", "studyEnvSiteUuid");
                errorObjects.add(errorObject);
            } else {
                studyEnvSiteUuid = studyEnvSiteUuid.trim();
            }

            if (ocOid == null) {
                ErrorObject errorObject = createErrorObject("Site Object", "Missing Field", "ocOid");
                errorObjects.add(errorObject);
            } else {
                ocOid = ocOid.trim();
            }
            if (StringUtils.isEmpty(statusStr)) {
                ErrorObject errorObject = createErrorObject("Site Object", "Missing Field", "status");
                errorObjects.add(errorObject);
            } else {
                statusStr = statusStr.toLowerCase();
            }
            status = Status.getByName(statusStr);

            if (status == null) {
                ErrorObject errorObject = createErrorObject("Site Object", "Missing Field", "status");
                errorObjects.add(errorObject);
            }
            String format = "yyyy-MM-dd";
            SimpleDateFormat formatter = null;

            if (startDate != "" && startDate != null) {
                try {
                    formatter = new SimpleDateFormat(format);
                    formattedStartDate = formatter.parse(startDate);
                } catch (ParseException e) {
                    ErrorObject errorObject = createErrorObject("Site Object", "The StartDate format is not a valid 'yyyy-MM-dd' format", "StartDate");
                    errorObjects.add(errorObject);
                }
                if (formattedStartDate != null) {
                    if (!startDate.equals(formatter.format(formattedStartDate))) {
                        ErrorObject errorObject = createErrorObject("Site Object", "The StartDate format is not a valid 'yyyy-MM-dd' format", "StartDate");
                        errorObjects.add(errorObject);
                    }
                }
            }

            if (studyVerificationDate != "" && studyVerificationDate != null) {
                try {
                    formatter = new SimpleDateFormat(format);
                    formattedStudyDate = formatter.parse(studyVerificationDate);
                } catch (ParseException e) {
                    ErrorObject errorObject = createErrorObject("Site Object", "The Study Verification Date format is not a valid 'yyyy-MM-dd' format",
                            "StudyDateVerification");
                    errorObjects.add(errorObject);
                }
                if (formattedStudyDate != null) {
                    if (!studyVerificationDate.equals(formatter.format(formattedStudyDate))) {
                        ErrorObject errorObject = createErrorObject("Site Object", "The Study Verification Date format is not a valid 'yyyy-MM-dd' format",
                                "StudyDateVerification");
                        errorObjects.add(errorObject);
                    }
                }
            }
            request.setAttribute("uniqueSiteId", uniqueIdentifier);
            request.setAttribute("name", name);
            request.setAttribute("prinInvestigator", principalInvestigator);
            request.setAttribute("expectedTotalEnrollment", expectedTotalEnrollment);

            parentStudy = getStudyByEnvId(studyEnvUuid);
            if (parentStudy == null) {
                ErrorObject errorObject = createErrorObject("Study Object", "The Study Study Id provided in the URL is not a valid Study Id",
                        "Study Env Uuid");
                errorObjects.add(errorObject);
            } else if (parentStudy.getParentStudyId() != 0) {
                ErrorObject errorObject = createErrorObject("Study Object", "The Study Study Id provided in the URL is not a valid Study Study Id",
                        "Study Env Uuid");
                errorObjects.add(errorObject);
            }

            if (parentStudy != null) {
                ownerUserAccount = getSiteOwnerAccount(request, parentStudy);
                if (ownerUserAccount == null) {
                    ErrorObject errorObject = createErrorObject("Site Object",
                            "The Owner User Account is not Valid Account or Does not have rights to Create Sites", "Owner Account");
                    errorObjects.add(errorObject);
                }
            }

            Validator v1 = new Validator(request);
            v1.addValidation("uniqueSiteId", Validator.NO_BLANKS);
            HashMap vError1 = v1.validate();
            if (!vError1.isEmpty()) {
                ErrorObject errorObject = createErrorObject("Site Object", "This field cannot be blank.", "UniqueStudyId");
                errorObjects.add(errorObject);
            }
            Validator v2 = new Validator(request);
            v2.addValidation("name", Validator.NO_BLANKS);
            HashMap vError2 = v2.validate();
            if (!vError2.isEmpty()) {
                ErrorObject errorObject = createErrorObject("Site Object", "This field cannot be blank.", "BriefTitle");
                errorObjects.add(errorObject);
            }
            Validator v3 = new Validator(request);
            v3.addValidation("prinInvestigator", Validator.NO_BLANKS);
            HashMap vError3 = v3.validate();
            if (!vError3.isEmpty()) {
                ErrorObject errorObject = createErrorObject("Site Object", "This field cannot be blank.", "PrincipleInvestigator");
                errorObjects.add(errorObject);
            }

            Validator v7 = new Validator(request);
            v7.addValidation("expectedTotalEnrollment", Validator.NO_BLANKS);
            HashMap vError7 = v7.validate();
            if (!vError7.isEmpty()) {
                ErrorObject errorObject = createErrorObject("Site Object", "This field cannot be blank.", "ExpectedTotalEnrollment");
                errorObjects.add(errorObject);
            }

            if (request.getAttribute("name") != null && ((String) request.getAttribute("name")).length() > 100) {
                ErrorObject errorObject = createErrorObject("Site Object", "BriefTitle Length exceeds the max length 100", "BriefTitle");
                errorObjects.add(errorObject);
            }
            if (request.getAttribute("uniqueSiteId") != null && ((String) request.getAttribute("uniqueSiteId")).length() > 30) {
                ErrorObject errorObject = createErrorObject("Site Object", "UniqueStudyId Length exceeds the max length 30", "UniqueStudyId");
                errorObjects.add(errorObject);
            }
            if (request.getAttribute("prinInvestigator") != null && ((String) request.getAttribute("prinInvestigator")).length() > 255) {
                ErrorObject errorObject = createErrorObject("Site Object", "PrincipleInvestigator Length exceeds the max length 255", "PrincipleInvestigator");
                errorObjects.add(errorObject);
            }
            if ((request.getAttribute("expectedTotalEnrollment") != null)
                    && ((Integer) request.getAttribute("expectedTotalEnrollment") <= 0)) {
                ErrorObject errorObject = createErrorObject("Site Object", "ExpectedTotalEnrollment Length can't be negative or zero", "ExpectedTotalEnrollment");
                errorObjects.add(errorObject);
            }

            return errorObjects;
        }

    }

    /**
     * @api {post} /pages/auth/api/v1/studies/:studyEnvUuid/sites Create a site
     * @apiName createNewSite
     * @apiPermission Authenticate using api-key. admin
     * @apiVersion 3.8.0
     * @apiParam {String} studyEnvUuid Study environment uuid.
     * @apiParam {String} briefTitle Brief Title .
     * @apiParam {String} principalInvestigator Principal Investigator Name.
     * @apiParam {Integer} expectedTotalEnrollment Expected Total Enrollment number
     * @apiParam {String} secondaryStudyID Site Secondary Study Id  (Optional)
     * @apiParam {Date} startDate Start date
     * @apiParam {Date} studyDateVerification study Verification date
     * @apiParam {Array} assignUserRoles Assign Users to Roles for this Study.
     * @apiGroup Site
     * @apiHeader {String} api_key Users unique access-key.
     * @apiDescription Create a Site
     * @apiParamExample {json} Request-Example:
     * {
     * "briefTitle": "Site Study ID Name",
     * "principalInvestigator": "Principal Investigator Name",
     * "expectedTotalEnrollment": "10",
     * "assignUserRoles": [
     * { "username" : "userc", "role" : "Investigator"},
     * { "username" : "userb", "role" : "Clinical Research Coordinator"},
     * { "username" : "dm_normal", "role" : "Monitor"},
     * { "username" : "sd_root", "role" : "Data Entry Person"}
     * ],
     * "uniqueStudyID": "Site Study ID",
     * "startDate": "2011-11-11",
     * "secondaryStudyID" : "Secondary Study ID 1" ,
     * "studyDateVerification" : "2011-10-14"
     * }
     * @apiErrorExample {json} Error-Response:
     * HTTP/1.1 400 Bad Request
     * {
     * "message": "VALIDATION FAILED",
     * "studyDateVerification": "2011-10-14",
     * "principalInvestigator": "Principal Investigator Name",
     * "expectedTotalEnrollment": "10",
     * "errors": [
     * { "field": "studyEnvUuid", "resource": "Site Object","code": "Unique Study Id exist in the System" }
     * ],
     * "secondaryProId": "Secondary Study ID 1",
     * "siteOid": null,
     * "briefTitle": "Site Study ID Name",
     * "assignUserRoles": [
     * { "role": "Investigator", "username": "userc"},
     * { "role": "Clinical Research Coordinator", "username": "userb"},
     * { "role": "Monitor","username": "dm_normal"},
     * { "role": "Data Entry Person","username": "sd_root"}
     * ],
     * "studyEnvUuid": "Site Study ID",
     * "startDate": "2011-11-11"
     * }
     * @apiSuccessExample {json} Success-Response:
     * HTTP/1.1 200 OK
     * {
     * "message": "SUCCESS",
     * "siteOid": "S_SITEPROT",
     * "uniqueSiteStudyID": "Site Study IDqq"
     * }
     */

   /* {

        "uniqueIdentifier": "Site26 A",
            "ocOid" :"S_TONY(TEST)",
            "briefTitle": "Site26-A",
            "briefSummary": "Vauge summary of events.",
            "studyEnvSiteUuid": "07fe0825-8a42-4b4a-9ed3-91ac27e0a861",

            "principalInvestigator": "Dr. Dorian",
            "expectedTotalEnrollment": "100",
            "status":"available|frozen|pending|locked"
    }
*/
    @RequestMapping(value = "/{studyEnvUuid}/sites", method = RequestMethod.POST)
    public ResponseEntity<Object> createNewSites(HttpServletRequest request,
                                                 @RequestBody HashMap<String, Object> map, @PathVariable("studyEnvUuid") String studyEnvUuid) throws Exception {
        logger.debug("Creating site(s) for study:" + studyEnvUuid);
        StudyBean siteBean = null;
        ResponseEntity<Object> response = null;

        Locale locale = new Locale("en_US");
        request.getSession().setAttribute(LocaleResolver.getLocaleSessionAttributeName(), locale);
        ResourceBundleProvider.updateLocale(locale);
        SiteParameters siteParameters = new SiteParameters(map, studyEnvUuid);
        siteParameters.setParameters();
        ArrayList<ErrorObject> errorObjects = siteParameters.validateParameters(request);
        Study envSiteUuidStudy = studyDao.findByStudyEnvUuid(siteParameters.studyEnvSiteUuid);
        if (envSiteUuidStudy != null && envSiteUuidStudy.getStudyId() != 0) {
            ErrorObject errorObject = createErrorObject("Site Object", "studyEnvSiteUuid already exists", "studySiteEnvUuid");
            errorObjects.add(errorObject);
        }
        SiteDTO siteDTO = buildSiteDTO(siteParameters.uniqueIdentifier, siteParameters.name, siteParameters.principalInvestigator,
                siteParameters.expectedTotalEnrollment, siteParameters.status, siteParameters.facilityInfo);
        siteDTO.setErrors(errorObjects);

        if (errorObjects != null && errorObjects.size() != 0) {
            siteDTO.setMessage(validation_failed_message);
            response = new ResponseEntity(siteDTO, HttpStatus.BAD_REQUEST);
        } else {
            siteBean = buildSiteBean(siteParameters);
            siteBean.setSchemaName(siteParameters.parentStudy.getSchemaName());
            siteBean.setStudyEnvSiteUuid(siteParameters.studyEnvSiteUuid);
            siteBean.setEnvType(siteParameters.parentStudy.getEnvType());
            StudyBean sBean = createStudy(siteBean, siteParameters.ownerUserAccount);
            // get the schema study
            request.setAttribute("requestSchema", siteParameters.parentStudy.getSchemaName());
            StudyBean schemaStudy = getStudyByEnvId(studyEnvUuid);
            siteBuildService.process(schemaStudy, sBean, siteParameters.ownerUserAccount);
            siteDTO.setSiteOid(sBean.getOid());
            siteDTO.setMessage(validation_passed_message);
            StudyUserRoleBean sub = null;
            ResponseSuccessSiteDTO responseSuccess = new ResponseSuccessSiteDTO();
            responseSuccess.setMessage(siteDTO.getMessage());
            responseSuccess.setSiteOid(siteDTO.getSiteOid());
            responseSuccess.setUniqueSiteStudyID(siteDTO.getUniqueSiteProtocolID());

            response = new ResponseEntity(responseSuccess, HttpStatus.OK);

        }
        return response;

    }

    @RequestMapping(value = "/{studyEnvUuid}/sites", method = RequestMethod.PUT)
    public ResponseEntity<Object> updateSiteSettings(HttpServletRequest request,
                                                     @RequestBody HashMap<String, Object> map, @PathVariable("studyEnvUuid") String studyEnvUuid) throws Exception {
        logger.debug("Updating site settings for study:" + studyEnvUuid);
        ResponseEntity<Object> response = null;

        Locale locale = new Locale("en_US");
        request.getSession().setAttribute(LocaleResolver.getLocaleSessionAttributeName(), locale);
        ResourceBundleProvider.updateLocale(locale);
        SiteParameters siteParameters = new SiteParameters(map, studyEnvUuid);
        siteParameters.setParameters();
        ArrayList<ErrorObject> errorObjects = siteParameters.validateParameters(request);
        Study envSiteUuidStudy = studyDao.findByStudyEnvUuid(siteParameters.studyEnvSiteUuid);
        if (envSiteUuidStudy == null || envSiteUuidStudy.getStudyId() == 0) {
            ErrorObject errorObject = createErrorObject("Site Object", "studyEnvSiteUuid does not exist", "studySiteEnvUuid");
            errorObjects.add(errorObject);
        }
        SiteDTO siteDTO = buildSiteDTO(siteParameters.uniqueIdentifier, siteParameters.name, siteParameters.principalInvestigator,
                siteParameters.expectedTotalEnrollment, siteParameters.status, siteParameters.facilityInfo);
        siteDTO.setErrors(errorObjects);
        siteDTO.setSiteOid(envSiteUuidStudy.getOc_oid());
        if (errorObjects != null && errorObjects.size() != 0) {
            siteDTO.setMessage(validation_failed_message);
            response = new ResponseEntity(siteDTO, HttpStatus.BAD_REQUEST);
        } else {
            sdao = new StudyDAO(dataSource);
            StudyBean siteBean = sdao.findByStudyEnvUuid(siteParameters.studyEnvSiteUuid);
            setChangeableSiteSettings(siteBean, siteParameters);
            sdao.update(siteBean);

            // get the schema study
            request.setAttribute("requestSchema", siteBean.getSchemaName());
            StudyBean schemaStudy = getStudyByEnvId(siteParameters.studyEnvSiteUuid);
            setChangeableSiteSettings(schemaStudy, siteParameters);
            ResponseSuccessSiteDTO responseSuccess = new ResponseSuccessSiteDTO();
            sdao.update(schemaStudy);
            siteDTO.setMessage(validation_passed_message);
            responseSuccess.setMessage(siteDTO.getMessage());
            responseSuccess.setSiteOid(siteDTO.getSiteOid());
            responseSuccess.setUniqueSiteStudyID(siteDTO.getUniqueSiteProtocolID());
            response = new ResponseEntity(responseSuccess, HttpStatus.OK);

        }
        return response;

    }

    /**
     * @api {post} /pages/auth/api/v1/studies/:uniqueStudyId/eventdefinitions Create a study event
     * @apiName createEventDefinition
     * @apiPermission Authenticate using api-key. admin
     * @apiVersion 3.8.0
     * @apiParam {String} uniqueStudyId Study unique study ID.
     * @apiParam {String} name Event Name.
     * @apiParam {String} description Event Description.
     * @apiParam {String} category Category Name.
     * @apiParam {Boolean} repeating 'True' or 'False'.
     * @apiParam {String} type 'Scheduled' , 'UnScheduled' or 'Common'.
     * @apiGroup Study Event
     * @apiHeader {String} api_key Users unique access-key.
     * @apiDescription Creates a study event definition.
     * @apiParamExample {json} Request-Example:
     * {
     * "name": "Event Name",
     * "description": "Event Description",
     * "category": "Category Name",
     * "repeating": "true",
     * "type":"Scheduled"
     * }
     * @apiErrorExample {json} Error-Response:
     * HTTP/1.1 400 Bad Request
     * {
     * "name": "Event Name",
     * "message": "VALIDATION FAILED",
     * "type": "",
     * "errors": [
     * {"field": "Type","resource": "Event Definition Object","code": "Type Field should be Either 'Scheduled' , 'UnScheduled' or 'Common'"},
     * {"field": "Type","resource": "Event Definition Object","code": "This field cannot be blank."}
     * ],
     * "category": "Category Name",
     * "description": "Event Description",
     * "eventDefOid": null,
     * "repeating": "true"
     * }
     * @apiSuccessExample {json} Success-Response:
     * HTTP/1.1 200 OK
     * {
     * "message": "SUCCESS",
     * "name": "Event Name",
     * "eventDefOid": "SE_EVENTNAME"
     * }
     */
    @RequestMapping(value = "/{uniqueStudyID}/eventdefinitions", method = RequestMethod.POST)
    public ResponseEntity<Object> createEventDefinition(
            HttpServletRequest request, @RequestBody HashMap<String, Object> map, @PathVariable("uniqueStudyID") String uniqueStudyID) throws Exception {
        logger.debug("In Create Event Definition ");
        StudyBean publicStudy = getStudyByUniqId(uniqueStudyID);
        request.setAttribute("requestSchema", publicStudy.getSchemaName());
        ArrayList<ErrorObject> errorObjects = new ArrayList();
        StudyEventDefinitionBean eventBean = null;
        ResponseEntity<Object> response = null;
        Locale locale = new Locale("en_US");
        request.getSession().setAttribute(LocaleResolver.getLocaleSessionAttributeName(), locale);
        ResourceBundleProvider.updateLocale(locale);

        String name = (String) map.get("name");
        String description = (String) map.get("description");
        String category = (String) map.get("category");
        String type = (String) map.get("type");
        String repeating = (String) map.get("repeating");

        EventDefinitionDTO eventDefinitionDTO = buildEventDefnDTO(name, description, category, repeating, type);

        if (name == null) {
            ErrorObject errorObject = createErrorObject("Event Definition Object", "Missing Field", "Name");
            errorObjects.add(errorObject);
        } else {
            name = name.trim();
        }
        if (description == null) {
            ErrorObject errorObject = createErrorObject("Event Definition Object", "Missing Field", "Description");
            errorObjects.add(errorObject);
        } else {
            description = description.trim();
        }
        if (category == null) {
            ErrorObject errorObject = createErrorObject("Event Definition Object", "Missing Field", "Category");
            errorObjects.add(errorObject);
        } else {
            category = category.trim();
        }
        if (type == null) {
            ErrorObject errorObject = createErrorObject("Event Definition Object", "Missing Field", "Type");
            errorObjects.add(errorObject);
        } else {
            type = type.trim();
        }
        if (repeating == null) {
            ErrorObject errorObject = createErrorObject("Event Definition Object", "Missing Field", "Repeating");
            errorObjects.add(errorObject);
        } else {
            repeating = repeating.trim();
        }
        if (repeating != null) {
            if (!repeating.equalsIgnoreCase("true") && !repeating.equalsIgnoreCase("false")) {
                ErrorObject errorObject = createErrorObject("Event Definition Object", "Repeating Field should be Either 'True' or 'False'", "Repeating");
                errorObjects.add(errorObject);
            }
        }

        if (type != null) {
            if (!type.equalsIgnoreCase("scheduled") && !type.equalsIgnoreCase("unscheduled") && !type.equalsIgnoreCase("common")) {
                ErrorObject errorObject = createErrorObject("Event Definition Object", "Type Field should be Either 'Scheduled' , 'UnScheduled' or 'Common'",
                        "Type");
                errorObjects.add(errorObject);
            }
        }

        request.setAttribute("name", name);
        request.setAttribute("description", description);
        request.setAttribute("category", category);
        request.setAttribute("type", type);
        request.setAttribute("repeating", repeating);

        StudyBean parentStudy = getStudyByUniqId(uniqueStudyID);
        if (parentStudy == null) {
            ErrorObject errorObject = createErrorObject("Event Definition Object", "The Study Study Id provided in the URL is not a valid Study Id",
                    "Unique Study Study Id");
            errorObjects.add(errorObject);
        } else if (parentStudy.getParentStudyId() != 0) {
            ErrorObject errorObject = createErrorObject("Event Definition Object", "The Study Study Id provided in the URL is not a valid Study Study Id",
                    "Unique Study Study Id");
            errorObjects.add(errorObject);
        }

        UserAccountBean ownerUserAccount = getStudyOwnerAccount(request);
        if (ownerUserAccount == null) {
            ErrorObject errorObject = createErrorObject("Study Object", "The Owner User Account is not Valid Account or Does not have Admin user type",
                    "Owner Account");
            errorObjects.add(errorObject);
        }

        Validator v1 = new Validator(request);
        v1.addValidation("name", Validator.NO_BLANKS);
        HashMap vError1 = v1.validate();
        if (!vError1.isEmpty()) {
            ErrorObject errorObject = createErrorObject("Event Definition Object", "This field cannot be blank.", "Name");
            errorObjects.add(errorObject);
        }

        if (name != null) {
            Validator v2 = new Validator(request);
            v2.addValidation("name", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 2000);
            HashMap vError2 = v2.validate();
            if (!vError2.isEmpty()) {
                ErrorObject errorObject = createErrorObject("Event Definition Object", "The Length Should not exceed 2000.", "Name");
                errorObjects.add(errorObject);
            }
        }
        if (description != null) {
            Validator v3 = new Validator(request);
            v3.addValidation("description", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 2000);
            HashMap vError3 = v3.validate();
            if (!vError3.isEmpty()) {
                ErrorObject errorObject = createErrorObject("Event Definition Object", "The Length Should not exceed 2000.", "Description");
                errorObjects.add(errorObject);
            }
        }
        if (category != null) {
            Validator v4 = new Validator(request);
            v4.addValidation("category", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 2000);
            HashMap vError4 = v4.validate();
            if (!vError4.isEmpty()) {
                ErrorObject errorObject = createErrorObject("Event Definition Object", "The Length Should not exceed 2000.", "Category");
                errorObjects.add(errorObject);
            }
        }
        Validator v5 = new Validator(request);
        v5.addValidation("repeating", Validator.NO_BLANKS);
        HashMap vError5 = v5.validate();
        if (!vError5.isEmpty()) {
            ErrorObject errorObject = createErrorObject("Event Definition Object", "This field cannot be blank.", "Repeating");
            errorObjects.add(errorObject);
        }

        Validator v6 = new Validator(request);
        v6.addValidation("type", Validator.NO_BLANKS);
        HashMap vError6 = v6.validate();
        if (!vError6.isEmpty()) {
            ErrorObject errorObject = createErrorObject("Event Definition Object", "This field cannot be blank.", "Type");
            errorObjects.add(errorObject);
        }

        eventDefinitionDTO.setErrors(errorObjects);

        if (errorObjects != null && errorObjects.size() != 0) {
            eventDefinitionDTO.setMessage(validation_failed_message);
            response = new ResponseEntity(eventDefinitionDTO, org.springframework.http.HttpStatus.BAD_REQUEST);
        } else {
            eventBean = buildEventDefBean(name, description, category, type, repeating, ownerUserAccount, parentStudy);

            StudyEventDefinitionBean sedBean = createEventDefn(eventBean, ownerUserAccount);
            eventDefinitionDTO.setEventDefOid(sedBean.getOid());
            eventDefinitionDTO.setMessage(validation_passed_message);
        }
        ResponseSuccessEventDefDTO responseSuccess = new ResponseSuccessEventDefDTO();
        responseSuccess.setMessage(eventDefinitionDTO.getMessage());
        responseSuccess.setEventDefOid(eventDefinitionDTO.getEventDefOid());
        responseSuccess.setName(eventDefinitionDTO.getName());

        response = new ResponseEntity(responseSuccess, org.springframework.http.HttpStatus.OK);
        return response;

    }

    public Boolean verifyStudyTypeExist(String studyType) {
        ResourceBundle resadmin = org.akaza.openclinica.i18n.util.ResourceBundleProvider.getAdminBundle();
        if (!studyType.equalsIgnoreCase(resadmin.getString("interventional"))
                && !studyType.equalsIgnoreCase(resadmin.getString("observational"))
                && !studyType.equalsIgnoreCase(resadmin.getString("other"))) {
            System.out.println("Study Type not supported");
            return false;
        }
        return true;
    }

    public StudyEventDefinitionBean buildEventDefBean(String name, String description, String category, String type, String repeating, UserAccountBean owner,
                                                      StudyBean parentStudy) {

        StudyEventDefinitionBean sed = new StudyEventDefinitionBean();
        seddao = new StudyEventDefinitionDAO(dataSource);
        ArrayList defs = seddao.findAllByStudy(parentStudy);
        if (defs == null || defs.isEmpty()) {
            sed.setOrdinal(1);
        } else {
            int lastCount = defs.size() - 1;
            StudyEventDefinitionBean last = (StudyEventDefinitionBean) defs.get(lastCount);
            sed.setOrdinal(last.getOrdinal() + 1);
        }

        sed.setName(name);
        sed.setCategory(category);
        sed.setType(type.toLowerCase());
        sed.setDescription(description);
        sed.setRepeating(Boolean.valueOf(repeating));
        sed.setStudyId(parentStudy.getId());
        sed.setOwner(owner);
        sed.setStatus(Status.AVAILABLE);
        return sed;
    }

    public StudyBean buildSiteBean(SiteParameters parameters) {
        StudyBean study = new StudyBean();
        ResourceBundle resadmin = org.akaza.openclinica.i18n.util.ResourceBundleProvider.getAdminBundle();
        study.setOid(parameters.ocOid);
        study.setIdentifier(parameters.uniqueIdentifier);
        study.setParentStudyId(parameters.parentStudy.getId());
        study.setPublished(parameters.parentStudy.isPublished());
        study.setOwner(parameters.ownerUserAccount);
        setChangeableSiteSettings(study, parameters);
        return study;
    }

    public void setChangeableSiteSettings(StudyBean study, SiteParameters parameters) {
        study.setName(parameters.name);
        study.setPrincipalInvestigator(parameters.principalInvestigator);
        study.setExpectedTotalEnrollment(parameters.expectedTotalEnrollment);
        study.setStatus(parameters.status);
        study.setDatePlannedStart(parameters.formattedStartDate);
        study.setProtocolDateVerification(parameters.formattedStudyDate);
        study.setFacilityCity(parameters.facilityInfo.getFacilityCity());
        study.setFacilityState(parameters.facilityInfo.getFacilityState());
        study.setFacilityZip(parameters.facilityInfo.getFacilityZip());
        study.setFacilityCountry(parameters.facilityInfo.getFacilityCountry());
        study.setFacilityContactName(parameters.facilityInfo.getFacilityContact());
        study.setFacilityContactPhone(parameters.facilityInfo.getFacilityPhone());
        study.setFacilityContactEmail(parameters.facilityInfo.getFacilityEmail());
    }

    public StudyBean createStudy(StudyBean studyBean, UserAccountBean owner) {
        sdao = new StudyDAO(dataSource);
        StudyBean sBean = (StudyBean) sdao.create(studyBean);
        sBean = (StudyBean) sdao.findByPK(sBean.getId());
        return sBean;
    }

    public StudyBean createStudyWithDatasource(StudyBean studyBean, DataSource ds) {
        sdao = new StudyDAO(ds);
        StudyBean sBean = (StudyBean) sdao.create(studyBean);
        sBean = (StudyBean) sdao.findByPK(sBean.getId());
        return sBean;
    }

    public StudyEventDefinitionBean createEventDefn(StudyEventDefinitionBean sedBean, UserAccountBean owner) {
        seddao = new StudyEventDefinitionDAO(dataSource);
        StudyEventDefinitionBean sdBean = (StudyEventDefinitionBean) seddao.create(sedBean);
        sdBean = (StudyEventDefinitionBean) seddao.findByPK(sdBean.getId());
        return sdBean;
    }

    public StudyUserRoleBean createRole(UserAccountBean ownerUserAccount, StudyUserRoleBean sub, DataSource dataSource) {
        udao = new UserAccountDAO(dataSource);
        StudyUserRoleBean studyUserRoleBean = (StudyUserRoleBean) udao.createStudyUserRole(ownerUserAccount, sub);
        return studyUserRoleBean;
    }

    public StudyUserRoleBean createUserRole(UserAccountBean ownerUserAccount, StudyBean study) {
        udao = new UserAccountDAO(dataSource);
        StudyUserRoleBean surBean = udao.findRoleByUserNameAndStudyId(ownerUserAccount.getName(), study.getId());
        return surBean;
    }

    public StudyBean updateStudy(StudyBean studyBean, UserAccountBean owner) {
        sdao = new StudyDAO(dataSource);
        StudyBean sBean = (StudyBean) sdao.update(studyBean);
        return sBean;
    }

    public void addValidationToDefinitionFields(Validator v) {

        v.addValidation("name", Validator.NO_BLANKS);
        v.addValidation("name", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 2000);
        v.addValidation("description", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 2000);
        v.addValidation("category", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 2000);

    }

    private UserAccountBean getUserAccount(String userName) {
        udao = new UserAccountDAO(dataSource);
        UserAccountBean userAccountBean = (UserAccountBean) udao.findByUserName(userName);
        return userAccountBean;
    }

    private StudyBean getStudyByUniqId(String uniqueId) {
        sdao = new StudyDAO(dataSource);
        StudyBean studyBean = (StudyBean) sdao.findByUniqueIdentifier(uniqueId);
        return studyBean;
    }

    private StudyBean getStudyByEnvId(String envUuid) {
        sdao = new StudyDAO(dataSource);
        StudyBean studyBean = (StudyBean) sdao.findByStudyEnvUuid(envUuid);
        return studyBean;
    }

    public Boolean isDataManagerOrStudyDirector(UserAccountBean userAccount, StudyBean currentStudy){

        long result = userAccount.getRoles()
                .stream()
                .filter(role -> currentStudy.getId() == (role.getStudyId())
                        && (role.getRole().equals(Role.STUDYDIRECTOR)
                                || role.getRole().equals(Role.COORDINATOR)))
                .count();

        return result > 0;
    }


    public UserAccountBean getStudyOwnerAccount(HttpServletRequest request) {
        UserAccountBean ownerUserAccount = (UserAccountBean) request.getSession().getAttribute("userBean");
        if (!ownerUserAccount.isTechAdmin() && !ownerUserAccount.isSysAdmin()) {
            logger.info("The Owner User Account is not Valid Account or Does not have Admin user type");
            System.out.println("The Owner User Account is not Valid Account or Does not have Admin user type");
            return null;
        }
        return ownerUserAccount;
    }

    public UserAccountBean getStudyOwnerAccountWithCreatedUser(HttpServletRequest request, ResponseEntity<HashMap> responseEntity) {
        UserAccountBean ownerUserAccount = null;
        if (responseEntity != null) {
            HashMap hashMap = responseEntity.getBody();
            if (hashMap != null && hashMap.get("username") != null) {
                String usernmae = (String) hashMap.get("username");
                UserAccountDAO userAccountDAO = new UserAccountDAO(dataSource);
                ownerUserAccount = (UserAccountBean) userAccountDAO.findByUserName(usernmae);
            }
        } else {
            ownerUserAccount = (UserAccountBean) request.getSession().getAttribute("userBean");
        }

        if (!ownerUserAccount.isTechAdmin() && !ownerUserAccount.isSysAdmin()) {
            logger.info("The Owner User Account is not Valid Account or Does not have Admin user type");
            System.out.println("The Owner User Account is not Valid Account or Does not have Admin user type");
            return null;
        }
        return ownerUserAccount;
    }

    public UserAccountBean getSiteOwnerAccount(HttpServletRequest request, StudyBean study) {
        UserAccountBean ownerUserAccount = (UserAccountBean) request.getSession().getAttribute("userBean");
        StudyUserRoleBean currentRole = createUserRole(ownerUserAccount, study);

        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return ownerUserAccount;
        }

        return null;
    }

    public StudyDTO buildStudyDTO(String uniqueStudyID, String name, String briefSummary, String principalInvestigator, String sponsor,
                                  String expectedTotalEnrollment, String studyType, String status, String startDate, ArrayList<UserRole> userList) {
        if (status != null) {
            if (status.equals(""))
                status = "design";
        }

        StudyDTO studyDTO = new StudyDTO();
        studyDTO.setUniqueProtocolID(uniqueStudyID);
        studyDTO.setBriefTitle(name);
        studyDTO.setPrincipalInvestigator(principalInvestigator);
        studyDTO.setBriefSummary(briefSummary);
        studyDTO.setSponsor(sponsor);
        studyDTO.setProtocolType(studyType);
        studyDTO.setStatus(status);
        studyDTO.setExpectedTotalEnrollment(expectedTotalEnrollment);
        studyDTO.setStartDate(startDate);
        studyDTO.setAssignUserRoles(userList);
        return studyDTO;
    }

    public StudyDTO buildNewStudyDTO(String uniqueStudyID, String name) {
        StudyDTO studyDTO = new StudyDTO();
        studyDTO.setUniqueProtocolID(uniqueStudyID);
        studyDTO.setBriefTitle(name);
        studyDTO.setStatus("design");
        return studyDTO;
    }

    public SiteDTO buildSiteDTO(String uniqueSiteStudyID, String name, String principalInvestigator,
                                Integer expectedTotalEnrollment, Status status, FacilityInfo facilityInfo) {

        SiteDTO siteDTO = new SiteDTO();
        siteDTO.setUniqueSiteProtocolID(uniqueSiteStudyID);
        siteDTO.setBriefTitle(name);
        siteDTO.setPrincipalInvestigator(principalInvestigator);
        siteDTO.setExpectedTotalEnrollment(expectedTotalEnrollment);
        siteDTO.setStatus(status);
        siteDTO.setFacilityInfo(facilityInfo);
        return siteDTO;
    }

    public EventDefinitionDTO buildEventDefnDTO(String name, String description, String category, String repeating, String type) {
        EventDefinitionDTO eventDefinitionDTO = new EventDefinitionDTO();
        eventDefinitionDTO.setName(name);
        eventDefinitionDTO.setDescription(description);
        eventDefinitionDTO.setCategory(category);
        eventDefinitionDTO.setType(type);
        eventDefinitionDTO.setRepeating(repeating);

        return eventDefinitionDTO;
    }

    public StudyBean buildStudyBean(String uniqueStudyId, String name, String briefSummary, String principalInvestigator, String sponsor,
                                    int expectedTotalEnrollment, String studyType, String status, Date startDate, UserAccountBean owner) {

        StudyBean study = new StudyBean();
        ResourceBundle resadmin = org.akaza.openclinica.i18n.util.ResourceBundleProvider.getAdminBundle();
        if (studyType.equals(resadmin.getString("interventional"))) {
            study.setProtocolType("interventional");
        } else if (studyType.equals(resadmin.getString("observational"))) {
            study.setProtocolType("observational");
        }
        ResourceBundle resword = org.akaza.openclinica.i18n.util.ResourceBundleProvider.getWordsBundle();
        if (resword.getString("available").equalsIgnoreCase(status))
            study.setStatus(Status.AVAILABLE);
        else if (resword.getString("design").equalsIgnoreCase(status) || status.equals(""))
            study.setStatus(Status.PENDING);

        study.setIdentifier(uniqueStudyId);
        study.setName(name);
        study.setPrincipalInvestigator(principalInvestigator);
        study.setSummary(briefSummary);
        study.setSponsor(sponsor);
        study.setExpectedTotalEnrollment(expectedTotalEnrollment);
        study.setDatePlannedStart(startDate);

        study.setOwner(owner);

        return study;
    }

    public StudyBean buildNewStudyBean(String uniqueStudyId, String name, UserAccountBean accountBean) {

        StudyBean study = new StudyBean();
        ResourceBundle resadmin = org.akaza.openclinica.i18n.util.ResourceBundleProvider.getAdminBundle();

        ResourceBundle resword = org.akaza.openclinica.i18n.util.ResourceBundleProvider.getWordsBundle();
        study.setStatus(Status.PENDING);

        study.setIdentifier(uniqueStudyId);
        study.setName(name);
        study.setOwner(accountBean);
        return study;
    }

    public ErrorObject createErrorObject(String resource, String code, String field) {
        ErrorObject errorObject = new ErrorObject();
        errorObject.setResource(resource);
        errorObject.setCode(code);
        errorObject.setField(field);
        return errorObject;
    }

    public Role getStudyRole(String roleName, ResourceBundle resterm) {
        if (roleName.equalsIgnoreCase(resterm.getString("Study_Director").trim())) {
            return Role.STUDYDIRECTOR;
        } else if (roleName.equalsIgnoreCase(resterm.getString("Study_Coordinator").trim())) {
            return Role.COORDINATOR;
        } else if (roleName.equalsIgnoreCase(resterm.getString("Investigator").trim())) {
            return Role.INVESTIGATOR;
        } else if (roleName.equalsIgnoreCase(resterm.getString("Data_Entry_Person").trim())) {
            return Role.RESEARCHASSISTANT;
        } else if (roleName.equalsIgnoreCase(resterm.getString("Monitor").trim())) {
            return Role.MONITOR;
        } else
            return null;
    }

    public Role getSiteRole(String roleName, ResourceBundle resterm) {
        if (roleName.equalsIgnoreCase(resterm.getString("site_investigator").trim())) {
            return Role.INVESTIGATOR;
        } else if (roleName.equalsIgnoreCase(resterm.getString("site_Data_Entry_Person").trim())) {
            return Role.RESEARCHASSISTANT;
        } else if (roleName.equalsIgnoreCase(resterm.getString("site_monitor").trim())) {
            return Role.MONITOR;
        } else if (roleName.equalsIgnoreCase(resterm.getString("site_Data_Entry_Person2").trim())) {
            return Role.RESEARCHASSISTANT2;
        } else
            return null;
    }


    private Status getStatus(String myStatus) {

        Status statusObj = null;

        if (myStatus != null) {
            myStatus = myStatus.equals("DESIGN") ? "PENDING" : myStatus;
            statusObj = Status.getByName(myStatus.toLowerCase());
        }
        return statusObj;
    }

}


