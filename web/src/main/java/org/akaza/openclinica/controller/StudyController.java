package org.akaza.openclinica.controller;

import org.akaza.openclinica.bean.core.NumericComparisonOperator;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.UserType;
import org.akaza.openclinica.bean.login.*;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.service.StudyParameterConfig;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.controller.helper.AsyncStudyHelper;
import org.akaza.openclinica.controller.helper.OCUserDTO;
import org.akaza.openclinica.controller.helper.StudyEnvironmentRoleDTO;
import org.akaza.openclinica.controller.helper.StudyInfoObject;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudyUserRoleDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEnvEnum;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.LiquibaseOnDemandService;
import org.akaza.openclinica.service.SchemaCleanupService;
import org.akaza.openclinica.service.SiteBuildService;
import org.akaza.openclinica.service.StudyBuildService;
import org.apache.commons.lang.StringUtils;
import org.json.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
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

@Controller @RequestMapping(value = "/auth/api/v1/studies") public class StudyController {

    public static ResourceBundle resadmin, resaudit, resexception, resformat, respage, resterm, restext, resword, resworkflow;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    @Autowired UserAccountController userAccountController;
    UserAccountDAO udao;
    StudyDAO sdao;
    StudyEventDefinitionDAO seddao;
    @Autowired @Qualifier("dataSource") private DataSource dataSource;
    @Autowired private StudyDao studyDao;
    @Autowired private StudyUserRoleDao studyUserRoleDao;
    @Autowired private StudyBuildService studyBuildService;
    @Autowired private LiquibaseOnDemandService liquibaseOnDemandService;
    @Autowired private SiteBuildService siteBuildService;
    @Autowired private SchemaCleanupService schemaCleanupService;

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

    @RequestMapping(value = "/{studyOid}/changeStatus/{status}", method = RequestMethod.POST) public ResponseEntity<Object> changeStudyStatus(
            @PathVariable("studyOid") String studyOid, @PathVariable("status") int status, HttpServletRequest request) {
        UserAccountBean ub = getStudyOwnerAccount(request);

        if (ub == null)
            return new ResponseEntity<Object>("Not permitted.", HttpStatus.FORBIDDEN);
        StudyDAO studyDAO = new StudyDAO(dataSource);
        StudyBean currentPublicStudy = studyDAO.findByOid(studyOid);
        String schema = currentPublicStudy.getSchemaName();
        CoreResources.setRequestSchema(request, schema);
        StudyDAO studyDAO1 = new StudyDAO(dataSource);
        StudyBean currentStudy = studyDAO1.findByOid(studyOid);
        currentStudy.setOldStatus(currentPublicStudy.getStatus());
        currentStudy.setStatus(Status.get(status));
        studyDAO.updateStudyStatus(currentStudy);
        ArrayList siteList = (ArrayList) studyDAO.findAllByParent(currentStudy.getId());
        if (siteList.size() > 0) {
            studyDAO.updateSitesStatus(currentStudy);
        }

        return new ResponseEntity<Object>("Success", HttpStatus.OK);

    }


    private StudyParameterConfig processStudyConfigParameters(HashMap<String, Object> map, ArrayList<ErrorObject> errorObjects) {
        StudyParameterConfig spc = new StudyParameterConfig();
        String collectBirthDate = (String) map.get("collectDateOfBirth");
        Boolean collectSex = (Boolean) map.get("collectSex");
        String collectPersonId = (String) map.get("collectPersonId");
        Boolean showSecondaryId = (Boolean) map.get("showSecondaryId");
        if (collectBirthDate == null) {
            ErrorObject errorOBject = createErrorObject("Study Object", "Missing Field", "CollectBirthDate");
            errorObjects.add(errorOBject);
        } else {
            collectBirthDate = collectBirthDate.trim();
        }
        spc.setCollectDob(collectBirthDate);
        if (collectSex == null) {
            ErrorObject errorOBject = createErrorObject("Study Object", "Missing Field", "CollectSex");
            errorObjects.add(errorOBject);
        }
        spc.setGenderRequired(Boolean.toString(collectSex));
        if (collectPersonId == null) {
            ErrorObject errorOBject = createErrorObject("Study Object", "Missing Field", "CollectPersonId");
            errorObjects.add(errorOBject);
        } else {
            collectPersonId = collectPersonId.trim();
        }
        spc.setSubjectPersonIdRequired(collectPersonId);
        if (showSecondaryId == null) {
            ErrorObject errorOBject = createErrorObject("Study Object", "Missing Field", "ShowSecondaryId");
            errorObjects.add(errorOBject);
        }
        spc.setSecondaryLabelViewable(Boolean.toString(showSecondaryId));
        return spc;
    }
    @RequestMapping(value = "/", method = RequestMethod.POST) public ResponseEntity<Object> createNewStudy(HttpServletRequest request,
            @RequestBody HashMap<String, Object> map) throws Exception {
        ArrayList<ErrorObject> errorObjects = new ArrayList();
        StudyDTO studyDTO = new StudyDTO();
        logger.info("In Create Study");
        ResponseEntity<Object> response = null;

        String validation_failed_message = "VALIDATION FAILED";
        String validation_passed_message = "SUCCESS";

        String uniqueStudyID = (String) map.get("uniqueStudyID");
        String name = (String) map.get("briefTitle");
        String studyOid = (String) map.get("studyEnvOid");
        String studyEnvUuid = (String) map.get("studyEnvUuid");
        String description = (String) map.get("description");
        String studyType = (String) map.get("type");
        String phase = (String) map.get("phase");
        String startDateStr = (String) map.get("expectedStartDate");
        String endDateStr = (String) map.get("expectedEndDate");
        Integer expectedTotalEnrollment = (Integer) map.get("expectedTotalEnrollment");

        Matcher m = Pattern.compile("(.+)\\((.+)\\)").matcher(studyOid);
        String envType = "";
        if (m.find()) {
            if (m.groupCount() != 2) {
                ErrorObject errorOBject = createErrorObject("Study Object", "Missing Field", "envType");
                errorObjects.add(errorOBject);
            } else {
                envType = m.group(2);
            }
        }

        AsyncStudyHelper asyncStudyHelper = new AsyncStudyHelper("Study Creation Started", "PENDING", LocalTime.now());
        AsyncStudyHelper.put(uniqueStudyID, asyncStudyHelper);


        if (StringUtils.isEmpty(uniqueStudyID)) {
            ErrorObject errorOBject = createErrorObject("Study Object", "Missing Field", "UniqueStudyID");
            errorObjects.add(errorOBject);
        } else {
            uniqueStudyID = uniqueStudyID.trim();
        }
        if (StringUtils.isEmpty(name)) {
            ErrorObject errorOBject = createErrorObject("Study Object", "Missing Field", "BriefTitle");
            errorObjects.add(errorOBject);
        } else {
            name = name.trim();
        }
        if (description == null) {
            ErrorObject errorOBject = createErrorObject("Study Object", "Missing Field", "Description");
            errorObjects.add(errorOBject);
        } else {
            description = description.trim();
        }

        if (expectedTotalEnrollment == null) {
            ErrorObject errorOBject = createErrorObject("Study Object", "Missing Field", "ExpectedTotalEnrollment");
            errorObjects.add(errorOBject);
        }

        if (startDateStr == null) {
            ErrorObject errorOBject = createErrorObject("Study Object", "Missing Field", "StartDate");
            errorObjects.add(errorOBject);
        } else {
            startDateStr = startDateStr.trim();
        }

        Date startDate = formatDateString(startDateStr, "StartDate", errorObjects);
        if (endDateStr == null) {
            ErrorObject errorOBject = createErrorObject("Study Object", "Missing Field", "EndDate");
            errorObjects.add(errorOBject);
        } else {
            endDateStr = endDateStr.trim();
        }
        Date endDate = formatDateString(endDateStr, "EndDate", errorObjects);

        if (studyType != null ) {
            studyType = studyType.toLowerCase();
            if (!verifyStudyTypeExist(studyType)) {
                ErrorObject errorOBject = createErrorObject("Study Object", "Study Type is not Valid", "StudyType");
                errorObjects.add(errorOBject);
            }
        }

        if (StringUtils.isEmpty(studyOid)) {
            ErrorObject errorOBject = createErrorObject("Study Object", "Missing Field", "oid");
            errorObjects.add(errorOBject);
        } else {
            studyOid = studyOid.trim();
        }

        if (StringUtils.isEmpty(studyEnvUuid)) {
            ErrorObject errorOBject = createErrorObject("Study Object", "Missing Field", "studyEnvUuid");
            errorObjects.add(errorOBject);
        } else {
            studyEnvUuid = studyEnvUuid.trim();
        }

        StudyParameterConfig studyParameterConfig = processStudyConfigParameters(map, errorObjects);
        Locale locale = new Locale("en_US");
        request.getSession().setAttribute(LocaleResolver.getLocaleSessionAttributeName(), locale);
        ResourceBundleProvider.updateLocale(locale);

        request.setAttribute("uniqueProId", uniqueStudyID);
        request.setAttribute("name", name); // Brief Title
        request.setAttribute("oid", studyOid);
        request.setAttribute("envType", envType);
        request.setAttribute("studyEnvUuid", studyEnvUuid);
        ResponseEntity<HashMap> responseEntity = processSSOUserContext(request, studyEnvUuid);

        UserAccountBean ownerUserAccount = getStudyOwnerAccountWithCreatedUser(request, responseEntity);
        if (ownerUserAccount == null) {
            ErrorObject errorOBject = createErrorObject("Study Object", "The Owner User Account is not Valid Account or Does not have Admin user type",
                    "Owner Account");
            errorObjects.add(errorOBject);

        }
        Validator v0 = new Validator(request);
        v0.addValidation("name", Validator.NO_BLANKS);

        HashMap vError0 = v0.validate();
        if (!vError0.isEmpty()) {
            ErrorObject errorOBject = createErrorObject("Study Object", "This field cannot be blank.", "BriefTitle");
            errorObjects.add(errorOBject);
        }

        Validator v1 = new Validator(request);
        v1.addValidation("uniqueProId", Validator.NO_BLANKS);
        HashMap vError1 = v1.validate();
        if (!vError1.isEmpty()) {
            ErrorObject errorOBject = createErrorObject("Study Object", "This field cannot be blank.", "UniqueStudyId");
            errorObjects.add(errorOBject);
        }

        Validator v2 = new Validator(request);
        v2.addValidation("oid", Validator.NO_BLANKS);
        HashMap vError2 = v2.validate();
        if (!vError2.isEmpty()) {
            ErrorObject errorOBject = createErrorObject("Study Object", "This field cannot be blank.", "oid");
            errorObjects.add(errorOBject);
        }

        Validator v4 = new Validator(request);
        v4.addValidation("role", Validator.NO_LEADING_OR_TRAILING_SPACES);
        HashMap vError4 = v4.validate();
        if (!vError4.isEmpty()) {
            ErrorObject errorOBject = createErrorObject("Study Object", "This field cannot have leading or trailing spaces.", "role");
            errorObjects.add(errorOBject);
        }

        if (errorObjects != null && errorObjects.size() != 0) {
            studyDTO.setMessage(validation_failed_message);
            response = new ResponseEntity(studyDTO, org.springframework.http.HttpStatus.BAD_REQUEST);
            return response;
        }
        studyDTO.setErrors(errorObjects);

        Study study = new Study();
        study.setUniqueIdentifier(uniqueStudyID);
        study.setName(name);
        study.setOc_oid(studyOid);
        study.setEnvType(StudyEnvEnum.valueOf(envType));
        study.setStudyEnvUuid(studyEnvUuid);
        study.setPhase(phase);
        Study byOidEnvType = studyDao.findByOidEnvType(studyOid, StudyEnvEnum.valueOf(envType));
        if (byOidEnvType != null && byOidEnvType.getOc_oid() != null) {
            return getResponseSuccess(byOidEnvType);
        }
        study.setDatePlannedStart(startDate);
        study.setDatePlannedEnd(endDate);
        study.setExpectedTotalEnrollment((expectedTotalEnrollment));
        study.setProtocolType(studyType.toLowerCase());
        Study schemaStudy = createSchemaStudy(request, study, ownerUserAccount);
        setStudyParameters(request, study, schemaStudy, studyParameterConfig);
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
        AsyncStudyHelper.put(uniqueStudyID, asyncStudyDone);

        return response;

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
    private void updateParameter(StudyParameterValueDAO spvdao, StudyParameterValueBean spv) {
        StudyParameterValueBean spv1 = spvdao.findByHandleAndStudy(spv.getStudyId(), spv.getParameter());
        logger.debug("found parameter: " + spv.getParameter());
        if (spv1.getId() > 0) {
            spvdao.update(spv);
            logger.debug("updating");
        } else {
            spvdao.create(spv);
            logger.debug("creating");
        }
    }
    private void setStudyParameters(HttpServletRequest request, Study study, Study schemaStudy, StudyParameterConfig studyParameterConfig) {
        String schema = CoreResources.getRequestSchema(request);
        CoreResources.setRequestSchema(request, study.getSchemaName());
        sdao = new StudyDAO(dataSource);
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(dataSource);

        StudyBean sb = (StudyBean) sdao.findByPK(schemaStudy.getStudyId());
        sb.setStudyParameterConfig(studyParameterConfig);
        StudyParameterValueBean spv = new StudyParameterValueBean();

        spv.setStudyId(sb.getId());
        spv.setParameter("collectDob");
        int collectDob;
        if (StringUtils.isEmpty(sb.getStudyParameterConfig().getCollectDob())) {
            collectDob = 3;
        } else {
            switch (sb.getStudyParameterConfig().getCollectDob().toLowerCase()) {
            case "always":
                collectDob = 1;
                break;
            case "only the year":
                collectDob = 2;
                break;
            default:
                collectDob = 3;
                break;
            }
        }
        spv.setValue(new Integer(collectDob).toString());
        updateParameter(spvdao, spv);

        spv.setParameter("discrepancyManagement");
        spv.setValue(sb.getStudyParameterConfig().getDiscrepancyManagement());
        updateParameter(spvdao, spv);

        spv.setParameter("genderRequired");
        spv.setValue(sb.getStudyParameterConfig().getGenderRequired());
        updateParameter(spvdao, spv);

        spv.setParameter("subjectPersonIdRequired");
        spv.setValue(sb.getStudyParameterConfig().getSubjectPersonIdRequired());
        updateParameter(spvdao, spv);

        spv.setParameter("interviewerNameRequired");
        spv.setValue(sb.getStudyParameterConfig().getInterviewerNameRequired());
        updateParameter(spvdao, spv);

        spv.setParameter("interviewerNameDefault");
        spv.setValue(sb.getStudyParameterConfig().getInterviewerNameDefault());
        updateParameter(spvdao, spv);

        spv.setParameter("interviewerNameEditable");
        spv.setValue(sb.getStudyParameterConfig().getInterviewerNameEditable());
        updateParameter(spvdao, spv);

        spv.setParameter("interviewDateRequired");
        spv.setValue(sb.getStudyParameterConfig().getInterviewDateRequired());
        updateParameter(spvdao, spv);

        spv.setParameter("interviewDateDefault");
        spv.setValue(sb.getStudyParameterConfig().getInterviewDateDefault());
        updateParameter(spvdao, spv);

        spv.setParameter("interviewDateEditable");
        spv.setValue(sb.getStudyParameterConfig().getInterviewDateEditable());
        updateParameter(spvdao, spv);

        spv.setParameter("subjectIdGeneration");
        spv.setValue(sb.getStudyParameterConfig().getSubjectIdGeneration());
        updateParameter(spvdao, spv);

        spv.setParameter("subjectIdPrefixSuffix");
        spv.setValue(sb.getStudyParameterConfig().getSubjectIdPrefixSuffix());
        updateParameter(spvdao, spv);

        spv.setParameter("personIdShownOnCRF");
        spv.setValue(sb.getStudyParameterConfig().getPersonIdShownOnCRF());
        updateParameter(spvdao, spv);
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
                ErrorObject errorOBject = createErrorObject("Study Object",
                        "The StartDate format is not a valid 'yyyy-MM-dd' format", "fieldName");
                errorObjects.add(errorOBject);
            }
            if (formattedDate != null) {
                if (!dateStr.equals(formatter.format(formattedDate))) {
                    ErrorObject errorOBject = createErrorObject("Study Object",
                            "The StartDate format is not a valid 'yyyy-MM-dd' format", fieldName);
                    errorObjects.add(errorOBject);
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

        for (StudyEnvironmentRoleDTO role: studyUserRoles.getBody()) {
            LinkedHashMap<String,String> studyRole = new LinkedHashMap<>();
            studyRole.put("roleName",role.getRoleName());
            studyRole.put("studyEnvUuid",role.getStudyEnvironmentUuid());
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
        userContextMap.put("roles",roles);
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

    @RequestMapping(value = "/asyncStudyStatus", method = RequestMethod.GET) public ResponseEntity<Object> getAyncStudyStatus(HttpServletRequest request,
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


    private ResponseEntity<Object> processStudyAsync(HttpServletRequest request, String validation_passed_message, Study study,
            UserAccountBean ownerUserAccount) throws Exception {
        ResponseEntity<Object> response;
        Locale locale = new Locale("en_US");
        request.getSession().setAttribute(LocaleResolver.getLocaleSessionAttributeName(), locale);
        ResourceBundleProvider.updateLocale(locale);
        StudyInfoObject studyInfoObject = studyBuildService.process(request, study, ownerUserAccount);
        AsyncStudyHelper asyncStudyHelper = new AsyncStudyHelper("Study added to Public schema", "PENDING");
        AsyncStudyHelper.put(study.getUniqueIdentifier(), asyncStudyHelper);
        liquibaseOnDemandService.createForeignTables(studyInfoObject);
        Study schemaStudy = liquibaseOnDemandService.process(studyInfoObject, studyInfoObject.getUb());

        logger.debug("returning from liquibase study:" + schemaStudy.getStudyId());
        logger.debug("study oc_id:" + schemaStudy.getOc_oid());

        ResponseSuccessStudyDTO responseSuccess = new ResponseSuccessStudyDTO();
        responseSuccess.setMessage(validation_passed_message);
        responseSuccess.setStudyOid(schemaStudy.getOc_oid());
        responseSuccess.setUniqueStudyID(schemaStudy.getUniqueIdentifier());
        responseSuccess.setSchemaName(studyInfoObject.getSchema());
        response = new ResponseEntity(responseSuccess, org.springframework.http.HttpStatus.OK);

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
        if (StringUtils.isNotEmpty(facilityState))
            facilityInfo.setFacilityState(facilityState.trim());
        if (StringUtils.isNotEmpty(facilityZip))
            facilityInfo.setFacilityZip(facilityZip.trim());
        if (StringUtils.isNotEmpty(facilityCountry))
            facilityInfo.setFacilityCountry(facilityCountry.trim());
        if (StringUtils.isNotEmpty(facilityContact))
            facilityInfo.setFacilityContact(facilityContact.trim());
        if (StringUtils.isNotEmpty(facilityEmail))
            facilityInfo.setFacilityEmail(facilityEmail.trim());
        if (StringUtils.isNotEmpty(facilityPhone))
            facilityInfo.setFacilityPhone(facilityPhone.trim());

        return facilityInfo;
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
    @RequestMapping(value = "/{studyEnvUuid}/sites", method = RequestMethod.POST) public ResponseEntity<Object> createNewSites(HttpServletRequest request,
            @RequestBody HashMap<String, Object> map, @PathVariable("studyEnvUuid") String studyEnvUuid) throws Exception {
        logger.debug("Creating site(s) for study:" + studyEnvUuid);
        ArrayList<ErrorObject> errorObjects = new ArrayList();
        StudyBean siteBean = null;
        ResponseEntity<Object> response = null;

        String validation_failed_message = "VALIDATION FAILED";
        String validation_passed_message = "SUCCESS";
        Locale locale = new Locale("en_US");
        request.getSession().setAttribute(LocaleResolver.getLocaleSessionAttributeName(), locale);
        ResourceBundleProvider.updateLocale(locale);
        String name = (String) map.get("briefTitle");
        String principalInvestigator = (String) map.get("principalInvestigator");
        String uniqueIdentifier = (String) map.get("uniqueIdentifier");
        Integer expectedTotalEnrollment = (Integer) map.get("expectedTotalEnrollment");
        String studyEnvSiteUuid = (String) map.get("studyEnvSiteUuid");
        String ocOid = (String) map.get("ocOid");
        String statusStr = (String) map.get("status");
        FacilityInfo facilityInfo = processFacilityInfo(map);
        String studyVerificationDate = (String) map.get("studyVerificationDate");
        String startDate = (String) map.get("startDate");

        if (uniqueIdentifier == null) {
            ErrorObject errorOBject = createErrorObject("Site Object", "Missing Field", "uniqueIdentifier");
            errorObjects.add(errorOBject);
        } else {
            uniqueIdentifier = uniqueIdentifier.trim();
        }
        if (name == null) {
            ErrorObject errorOBject = createErrorObject("Site Object", "Missing Field", "BriefTitle");
            errorObjects.add(errorOBject);
        } else {
            name = name.trim();
        }
        if (principalInvestigator == null) {
            ErrorObject errorOBject = createErrorObject("Site Object", "Missing Field", "PrincipalInvestigator");
            errorObjects.add(errorOBject);
        } else {
            principalInvestigator = principalInvestigator.trim();
        }

        if (expectedTotalEnrollment == null) {
            ErrorObject errorOBject = createErrorObject("Site Object", "Missing Field", "ExpectedTotalEnrollment");
            errorObjects.add(errorOBject);
        }

        if (studyEnvSiteUuid == null) {
            ErrorObject errorOBject = createErrorObject("Site Object", "Missing Field", "studyEnvSiteUuid");
            errorObjects.add(errorOBject);
        } else {
            studyEnvSiteUuid = studyEnvSiteUuid.trim();
        }

        if (ocOid == null) {
            ErrorObject errorOBject = createErrorObject("Site Object", "Missing Field", "ocOid");
            errorObjects.add(errorOBject);
        } else {
            ocOid = ocOid.trim();
        }
        if (StringUtils.isEmpty(statusStr)) {
            ErrorObject errorOBject = createErrorObject("Site Object", "Missing Field", "status");
            errorObjects.add(errorOBject);
        } else {
            statusStr = statusStr.toLowerCase();
        }
        Status status = Status.getByName(statusStr);

        if (status == null) {
            ErrorObject errorOBject = createErrorObject("Site Object", "Missing Field", "status");
            errorObjects.add(errorOBject);
        }

        Study envSiteUuidStudy = studyDao.findByStudyEnvUuid(studyEnvSiteUuid);
        if (envSiteUuidStudy != null && envSiteUuidStudy.getStudyId() != 0) {
            ErrorObject errorOBject = createErrorObject("Site Object", "studyEnvSiteUuid already exists", "studySiteEnvUuid");
            errorObjects.add(errorOBject);
        }
        SiteDTO siteDTO = buildSiteDTO(uniqueIdentifier, name, principalInvestigator, expectedTotalEnrollment, status, facilityInfo);

        request.setAttribute("uniqueProId", uniqueIdentifier);
        request.setAttribute("name", name);
        request.setAttribute("prinInvestigator", principalInvestigator);
        request.setAttribute("expectedTotalEnrollment", expectedTotalEnrollment);

        String format = "yyyy-MM-dd";
        SimpleDateFormat formatter = null;
        Date formattedStartDate = null;
        Date formattedStudyDate = null;

        if (startDate != "" && startDate != null) {
            try {
                formatter = new SimpleDateFormat(format);
                formattedStartDate = formatter.parse(startDate);
            } catch (ParseException e) {
                ErrorObject errorOBject = createErrorObject("Site Object", "The StartDate format is not a valid 'yyyy-MM-dd' format", "StartDate");
                errorObjects.add(errorOBject);
            }
            if (formattedStartDate != null) {
                if (!startDate.equals(formatter.format(formattedStartDate))) {
                    ErrorObject errorOBject = createErrorObject("Site Object", "The StartDate format is not a valid 'yyyy-MM-dd' format", "StartDate");
                    errorObjects.add(errorOBject);
                }
            }
        }

        if (studyVerificationDate != "" && studyVerificationDate != null) {
            try {
                formatter = new SimpleDateFormat(format);
                formattedStudyDate = formatter.parse(studyVerificationDate);
            } catch (ParseException e) {
                ErrorObject errorOBject = createErrorObject("Site Object", "The Study Verification Date format is not a valid 'yyyy-MM-dd' format",
                        "StudyDateVerification");
                errorObjects.add(errorOBject);
            }
            if (formattedStudyDate != null) {
                if (!studyVerificationDate.equals(formatter.format(formattedStudyDate))) {
                    ErrorObject errorOBject = createErrorObject("Site Object", "The Study Verification Date format is not a valid 'yyyy-MM-dd' format",
                            "StudyDateVerification");
                    errorObjects.add(errorOBject);
                }
            }
        }

        StudyBean parentStudy = getStudyByEnvId(studyEnvUuid);
        if (parentStudy == null) {
            ErrorObject errorOBject = createErrorObject("Study Object", "The Study Study Id provided in the URL is not a valid Study Id",
                    "Study Env Uuid");
            errorObjects.add(errorOBject);
        } else if (parentStudy.getParentStudyId() != 0) {
            ErrorObject errorOBject = createErrorObject("Study Object", "The Study Study Id provided in the URL is not a valid Study Study Id",
                    "Study Env Uuid");
            errorObjects.add(errorOBject);
        }

        UserAccountBean ownerUserAccount = null;

        if (parentStudy != null) {
            ownerUserAccount = getSiteOwnerAccount(request, parentStudy);
            if (ownerUserAccount == null) {
                ErrorObject errorOBject = createErrorObject("Site Object",
                        "The Owner User Account is not Valid Account or Does not have rights to Create Sites", "Owner Account");
                errorObjects.add(errorOBject);
            }
        }

        Validator v1 = new Validator(request);
        v1.addValidation("uniqueProId", Validator.NO_BLANKS);
        HashMap vError1 = v1.validate();
        if (!vError1.isEmpty()) {
            ErrorObject errorOBject = createErrorObject("Site Object", "This field cannot be blank.", "UniqueStudyId");
            errorObjects.add(errorOBject);
        }
        Validator v2 = new Validator(request);
        v2.addValidation("name", Validator.NO_BLANKS);
        HashMap vError2 = v2.validate();
        if (!vError2.isEmpty()) {
            ErrorObject errorOBject = createErrorObject("Site Object", "This field cannot be blank.", "BriefTitle");
            errorObjects.add(errorOBject);
        }
        Validator v3 = new Validator(request);
        v3.addValidation("prinInvestigator", Validator.NO_BLANKS);
        HashMap vError3 = v3.validate();
        if (!vError3.isEmpty()) {
            ErrorObject errorOBject = createErrorObject("Site Object", "This field cannot be blank.", "PrincipleInvestigator");
            errorObjects.add(errorOBject);
        }

        Validator v7 = new Validator(request);
        v7.addValidation("expectedTotalEnrollment", Validator.NO_BLANKS);
        HashMap vError7 = v7.validate();
        if (!vError7.isEmpty()) {
            ErrorObject errorOBject = createErrorObject("Site Object", "This field cannot be blank.", "ExpectedTotalEnrollment");
            errorObjects.add(errorOBject);
        }

        if (request.getAttribute("name") != null && ((String) request.getAttribute("name")).length() > 100) {
            ErrorObject errorOBject = createErrorObject("Site Object", "BriefTitle Length exceeds the max length 100", "BriefTitle");
            errorObjects.add(errorOBject);
        }
        if (request.getAttribute("uniqueProId") != null && ((String) request.getAttribute("uniqueProId")).length() > 30) {
            ErrorObject errorOBject = createErrorObject("Site Object", "UniqueStudyId Length exceeds the max length 30", "UniqueStudyId");
            errorObjects.add(errorOBject);
        }
        if (request.getAttribute("prinInvestigator") != null && ((String) request.getAttribute("prinInvestigator")).length() > 255) {
            ErrorObject errorOBject = createErrorObject("Site Object", "PrincipleInvestigator Length exceeds the max length 255", "PrincipleInvestigator");
            errorObjects.add(errorOBject);
        }
        if ((request.getAttribute("expectedTotalEnrollment") != null)
                && ((Integer) request.getAttribute("expectedTotalEnrollment") <= 0)) {
            ErrorObject errorOBject = createErrorObject("Site Object", "ExpectedTotalEnrollment Length can't be negative or zero", "ExpectedTotalEnrollment");
            errorObjects.add(errorOBject);
        }

        siteDTO.setErrors(errorObjects);

        if (errorObjects != null && errorObjects.size() != 0) {
            siteDTO.setMessage(validation_failed_message);
            response = new ResponseEntity(siteDTO, HttpStatus.BAD_REQUEST);
        } else {
            siteBean = buildSiteBean(ocOid, uniqueIdentifier, name, principalInvestigator, Integer.valueOf(expectedTotalEnrollment),
                    ownerUserAccount, parentStudy.getId(), status, facilityInfo, formattedStartDate, formattedStudyDate);
            siteBean.setSchemaName(parentStudy.getSchemaName());
            siteBean.setStudyEnvSiteUuid(studyEnvSiteUuid);
            StudyBean sBean = createStudy(siteBean, ownerUserAccount);
            // get the schema study
            request.setAttribute("requestSchema", parentStudy.getSchemaName());
            StudyBean schemaStudy = getStudyByEnvId(studyEnvUuid);
            siteBuildService.process(schemaStudy, sBean, ownerUserAccount);
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
    @RequestMapping(value = "/{uniqueStudyID}/eventdefinitions", method = RequestMethod.POST) public ResponseEntity<Object> createEventDefinition(
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

        String validation_failed_message = "VALIDATION FAILED";
        String validation_passed_message = "SUCCESS";

        String name = (String) map.get("name");
        String description = (String) map.get("description");
        String category = (String) map.get("category");
        String type = (String) map.get("type");
        String repeating = (String) map.get("repeating");

        EventDefinitionDTO eventDefinitionDTO = buildEventDefnDTO(name, description, category, repeating, type);

        if (name == null) {
            ErrorObject errorOBject = createErrorObject("Event Definition Object", "Missing Field", "Name");
            errorObjects.add(errorOBject);
        } else {
            name = name.trim();
        }
        if (description == null) {
            ErrorObject errorOBject = createErrorObject("Event Definition Object", "Missing Field", "Description");
            errorObjects.add(errorOBject);
        } else {
            description = description.trim();
        }
        if (category == null) {
            ErrorObject errorOBject = createErrorObject("Event Definition Object", "Missing Field", "Category");
            errorObjects.add(errorOBject);
        } else {
            category = category.trim();
        }
        if (type == null) {
            ErrorObject errorOBject = createErrorObject("Event Definition Object", "Missing Field", "Type");
            errorObjects.add(errorOBject);
        } else {
            type = type.trim();
        }
        if (repeating == null) {
            ErrorObject errorOBject = createErrorObject("Event Definition Object", "Missing Field", "Repeating");
            errorObjects.add(errorOBject);
        } else {
            repeating = repeating.trim();
        }
        if (repeating != null) {
            if (!repeating.equalsIgnoreCase("true") && !repeating.equalsIgnoreCase("false")) {
                ErrorObject errorOBject = createErrorObject("Event Definition Object", "Repeating Field should be Either 'True' or 'False'", "Repeating");
                errorObjects.add(errorOBject);
            }
        }

        if (type != null) {
            if (!type.equalsIgnoreCase("scheduled") && !type.equalsIgnoreCase("unscheduled") && !type.equalsIgnoreCase("common")) {
                ErrorObject errorOBject = createErrorObject("Event Definition Object", "Type Field should be Either 'Scheduled' , 'UnScheduled' or 'Common'",
                        "Type");
                errorObjects.add(errorOBject);
            }
        }

        request.setAttribute("name", name);
        request.setAttribute("description", description);
        request.setAttribute("category", category);
        request.setAttribute("type", type);
        request.setAttribute("repeating", repeating);

        StudyBean parentStudy = getStudyByUniqId(uniqueStudyID);
        if (parentStudy == null) {
            ErrorObject errorOBject = createErrorObject("Event Definition Object", "The Study Study Id provided in the URL is not a valid Study Id",
                    "Unique Study Study Id");
            errorObjects.add(errorOBject);
        } else if (parentStudy.getParentStudyId() != 0) {
            ErrorObject errorOBject = createErrorObject("Event Definition Object", "The Study Study Id provided in the URL is not a valid Study Study Id",
                    "Unique Study Study Id");
            errorObjects.add(errorOBject);
        }

        UserAccountBean ownerUserAccount = getStudyOwnerAccount(request);
        if (ownerUserAccount == null) {
            ErrorObject errorOBject = createErrorObject("Study Object", "The Owner User Account is not Valid Account or Does not have Admin user type",
                    "Owner Account");
            errorObjects.add(errorOBject);
        }

        Validator v1 = new Validator(request);
        v1.addValidation("name", Validator.NO_BLANKS);
        HashMap vError1 = v1.validate();
        if (!vError1.isEmpty()) {
            ErrorObject errorOBject = createErrorObject("Event Definition Object", "This field cannot be blank.", "Name");
            errorObjects.add(errorOBject);
        }

        if (name != null) {
            Validator v2 = new Validator(request);
            v2.addValidation("name", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 2000);
            HashMap vError2 = v2.validate();
            if (!vError2.isEmpty()) {
                ErrorObject errorOBject = createErrorObject("Event Definition Object", "The Length Should not exceed 2000.", "Name");
                errorObjects.add(errorOBject);
            }
        }
        if (description != null) {
            Validator v3 = new Validator(request);
            v3.addValidation("description", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 2000);
            HashMap vError3 = v3.validate();
            if (!vError3.isEmpty()) {
                ErrorObject errorOBject = createErrorObject("Event Definition Object", "The Length Should not exceed 2000.", "Description");
                errorObjects.add(errorOBject);
            }
        }
        if (category != null) {
            Validator v4 = new Validator(request);
            v4.addValidation("category", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 2000);
            HashMap vError4 = v4.validate();
            if (!vError4.isEmpty()) {
                ErrorObject errorOBject = createErrorObject("Event Definition Object", "The Length Should not exceed 2000.", "Category");
                errorObjects.add(errorOBject);
            }
        }
        Validator v5 = new Validator(request);
        v5.addValidation("repeating", Validator.NO_BLANKS);
        HashMap vError5 = v5.validate();
        if (!vError5.isEmpty()) {
            ErrorObject errorOBject = createErrorObject("Event Definition Object", "This field cannot be blank.", "Repeating");
            errorObjects.add(errorOBject);
        }

        Validator v6 = new Validator(request);
        v6.addValidation("type", Validator.NO_BLANKS);
        HashMap vError6 = v6.validate();
        if (!vError6.isEmpty()) {
            ErrorObject errorOBject = createErrorObject("Event Definition Object", "This field cannot be blank.", "Type");
            errorObjects.add(errorOBject);
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

    public StudyBean buildSiteBean(String ocOid, String uniqueSiteStudyId, String name, String principalInvestigator, int expectedTotalEnrollment,
            UserAccountBean owner, int parentStudyId, Status status, FacilityInfo facilityInfo,
            Date startDate, Date approvalDate) {

        StudyBean study = new StudyBean();
        ResourceBundle resadmin = org.akaza.openclinica.i18n.util.ResourceBundleProvider.getAdminBundle();
        study.setOid(ocOid);
        study.setIdentifier(uniqueSiteStudyId);
        study.setName(name);
        study.setPrincipalInvestigator(principalInvestigator);
        study.setExpectedTotalEnrollment(expectedTotalEnrollment);
        study.setParentStudyId(parentStudyId);
        study.setOwner(owner);
        study.setStatus(status);
        study.setDatePlannedStart(startDate);
        study.setProtocolDateVerification(approvalDate);
        study.setFacilityCity(facilityInfo.getFacilityCity());
        study.setFacilityState(facilityInfo.getFacilityState());
        study.setFacilityZip(facilityInfo.getFacilityZip());
        study.setFacilityCountry(facilityInfo.getFacilityCountry());
        study.setFacilityContactName(facilityInfo.getFacilityContact());
        study.setFacilityContactPhone(facilityInfo.getFacilityPhone());
        study.setFacilityContactEmail(facilityInfo.getFacilityEmail());
        return study;
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

    public void validateUniqueProId(HttpServletRequest request, HashMap errors) {
        StudyDAO studyDAO = new StudyDAO(dataSource);
        ArrayList<StudyBean> allStudies = (ArrayList<StudyBean>) studyDAO.findAll();
        for (StudyBean thisBean : allStudies) {
            if (request.getAttribute("uniqueProId") != null && request.getAttribute("uniqueProId").equals(thisBean.getIdentifier())) {
                ResourceBundle resexception = org.akaza.openclinica.i18n.util.ResourceBundleProvider.getExceptionsBundle();
                Validator.addError(errors, "uniqueProId", resexception.getString("unique_study_id_existed"));
                break;
            }
        }

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
        ErrorObject errorOBject = new ErrorObject();
        errorOBject.setResource(resource);
        errorOBject.setCode(code);
        errorOBject.setField(field);
        return errorOBject;
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

}


