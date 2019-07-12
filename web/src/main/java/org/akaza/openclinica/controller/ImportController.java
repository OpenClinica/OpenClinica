package org.akaza.openclinica.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.rule.XmlSchemaValidationHelper;
import org.akaza.openclinica.bean.submit.crfdata.CRFDataPostImportContainer;
import org.akaza.openclinica.bean.submit.crfdata.ODMContainer;
import org.akaza.openclinica.bean.submit.crfdata.StudyEventDataBean;
import org.akaza.openclinica.bean.submit.crfdata.SubjectDataBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.controller.dto.StudyEventScheduleRequestDTO;
import org.akaza.openclinica.controller.dto.StudyEventUpdateRequestDTO;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.UserAccountDao;
import org.akaza.openclinica.domain.datamap.JobDetail;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.SubjectEventStatus;
import org.akaza.openclinica.domain.enumsupport.JobType;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.service.ImportService;
import org.akaza.openclinica.service.UserService;
import org.akaza.openclinica.service.UtilService;
import org.akaza.openclinica.service.ValidateService;
import org.akaza.openclinica.service.rest.errors.ParameterizedErrorVM;
import org.akaza.openclinica.web.restful.errors.ErrorConstants;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

/**
 * A simple example of an annotated Spring Controller. Notice that it is a POJO; it
 * does not implement any Spring interfaces or extend Spring classes.
 */
@RestController
@RequestMapping( value = "/auth/api" )
@Api( value = "ImportController", tags = {"Clinical Data"}, description = "REST API for Data Import" )
public class ImportController {

    @Autowired
    private UserService userService;

    @Autowired
    private ImportService importService;

    @Autowired
    private ValidateService validateService;

    @Autowired
    private UtilService utilService;

    @Autowired
    private StudyDao studyDao;
    @Autowired
    UserAccountDao userAccountDao;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private static final String ENTITY_NAME = "ImportController";
    private XmlSchemaValidationHelper schemaValidator = new XmlSchemaValidationHelper();

    public ImportController() {
    }


    @ApiOperation( value = "To import data in an .xml file", notes = "Will import the data in a xml file " )
    @RequestMapping( value = "/clinicaldata/import", method = RequestMethod.POST )
    public ResponseEntity<Object> importDataXMLFile(HttpServletRequest request, MultipartFile file) throws Exception {
        String fileNm = "";
        String importXml = null;
        if (file != null) {
            fileNm = file.getOriginalFilename();
            if (fileNm != null && fileNm.endsWith(".xml")) {
                importXml = RestfulServiceHelper.readFileToString(file);
            } else {
                logger.error("file is not an xml extension file");
                return new ResponseEntity(ErrorConstants.ERR_FILE_FORMAT_NOT_SUPPORTED, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            }
        } else {
            //  if call is from the mirth server, then may have no attached file in the request
            // Read from request content
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            importXml = buffer.toString();
        }


        Mapping myMap = new Mapping();
        String ODM_MAPPING_DIRPath = CoreResources.ODM_MAPPING_DIR;
        myMap.loadMapping(ODM_MAPPING_DIRPath + File.separator + "cd_odm_mapping.xml");

        Unmarshaller um1 = new Unmarshaller(myMap);
        boolean fail = false;
        ODMContainer odmContainer = new ODMContainer();
        InputStream inputStream = null;
        try {
            // unmarshal xml to java
            inputStream = new ByteArrayInputStream(importXml.getBytes());
            String defaultEncoding = "UTF-8";

            BOMInputStream bOMInputStream = new BOMInputStream(inputStream);
            ByteOrderMark bom = bOMInputStream.getBOM();
            String charsetName = bom == null ? defaultEncoding : bom.getCharsetName();
            InputStreamReader reader = new InputStreamReader(new BufferedInputStream(bOMInputStream), charsetName);

            odmContainer = (ODMContainer) um1.unmarshal(reader);

        } catch (Exception e) {
            logger.error("found exception with xml transform {}", e);
            return new ResponseEntity(ErrorConstants.ERR_INVALID_XML_FILE + "\n" + e.getMessage(), HttpStatus.UNSUPPORTED_MEDIA_TYPE);

        } finally {
            inputStream.close();
        }


        String studyOID = odmContainer.getCrfDataPostImportContainer().getStudyOID();
        studyOID = studyOID.toUpperCase();
        Study publicStudy = studyDao.findPublicStudy(studyOID);
        if (publicStudy == null) {
            return new ResponseEntity(ErrorConstants.ERR_STUDY_NOT_EXIST, HttpStatus.NOT_FOUND);
        }
        String siteOid = null;
        String studyOid = null;

        if (publicStudy.getStudy() == null) {
            // This is a studyOid
            studyOid = studyOID;
        } else {
            //This is a siteOid
            siteOid = studyOID;
            studyOid = publicStudy.getStudy().getOc_oid();

        }
        if (studyOid != null)
            studyOid = studyOid.toUpperCase();
        if (siteOid != null)
            siteOid = siteOid.toUpperCase();

        utilService.setSchemaFromStudyOid(studyOid);

        ResponseEntity<Object> response = null;
        UserAccountBean userAccountBean = utilService.getUserAccountFromRequest(request);
        ArrayList<StudyUserRoleBean> userRoles = userAccountBean.getRoles();

        if (!validateService.isStudyAvailable(studyOid)) {
            return new ResponseEntity(ErrorConstants.ERR_STUDY_NOT_AVAILABLE, HttpStatus.OK);
        }

        if (siteOid!=null && !validateService.isStudyAvailable(siteOid)) {
            return new ResponseEntity(ErrorConstants.ERR_SITE_NOT_AVAILABLE, HttpStatus.OK);
        }

        if (!validateService.isStudyOidValid(studyOid)) {
            return new ResponseEntity(ErrorConstants.ERR_STUDY_NOT_EXIST, HttpStatus.OK);
        }


        if (siteOid != null) {
            if (!validateService.isUserHasAccessToSite(userRoles, siteOid)) {
                return new ResponseEntity(ErrorConstants.ERR_NO_ROLE_SETUP, HttpStatus.OK);
            } else if (!validateService.isUserHas_CRC_INV_DM_DEP_DS_RoleInSite(userRoles, siteOid)) {
                return new ResponseEntity(ErrorConstants.ERR_NO_SUFFICIENT_PRIVILEGES, HttpStatus.OK);
            }
        } else {
            if (!validateService.isUserHasAccessToStudy(userRoles, studyOid)) {
                return new ResponseEntity(ErrorConstants.ERR_NO_ROLE_SETUP, HttpStatus.OK);
            } else if (!validateService.isUserHas_DM_DEP_DS_RoleInStudy(userRoles, studyOid)) {
                return new ResponseEntity(ErrorConstants.ERR_NO_SUFFICIENT_PRIVILEGES, HttpStatus.OK);
            }
        }
        String schema = CoreResources.getRequestSchema();

        String uuid = startImportJob(odmContainer, schema, studyOid, siteOid, userAccountBean, fileNm);

        logger.info("REST request to Import Job uuid {} ", uuid);
        return new ResponseEntity<Object>("job uuid: " + uuid, HttpStatus.OK);
    }


    private Study getTenantStudy(String studyOid) {
        return studyDao.findByOcOID(studyOid);
    }

    public String startImportJob(ODMContainer odmContainer, String schema, String studyOid, String siteOid, UserAccountBean userAccountBean, String fileNm) {
        utilService.setSchemaFromStudyOid(studyOid);

        Study site = studyDao.findByOcOID(siteOid);
        Study study = studyDao.findByOcOID(studyOid);
        UserAccount userAccount = userAccountDao.findById(userAccountBean.getId());
        JobDetail jobDetail = userService.persistJobCreated(study, site, userAccount, JobType.XML_IMPORT, fileNm);
        CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> {
            try {
                importService.validateAndProcessDataImport(odmContainer, studyOid, siteOid, userAccountBean, schema, jobDetail);
            } catch (Exception e) {
                logger.error("Exception is thrown while processing dataImport: " + e);
            }
            return null;

        });
        return jobDetail.getUuid();
    }

    public File getXSDFile(HttpServletRequest request, String fileNm) {
        HttpSession session = request.getSession();
        ServletContext context = session.getServletContext();

        return new File(SpringServletAccess.getPropertiesDir(context) + fileNm);
    }





}
