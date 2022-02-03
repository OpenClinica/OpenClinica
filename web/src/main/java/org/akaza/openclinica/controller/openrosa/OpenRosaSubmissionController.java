package org.akaza.openclinica.controller.openrosa;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.rule.FileProperties;
import org.akaza.openclinica.control.submit.UploadFileServlet;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudyParameterValueDao;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyParameterValue;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.akaza.openclinica.web.pform.PFormCache;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(value = "/openrosa")
public class OpenRosaSubmissionController {

    @Autowired
    ServletContext context;

    @Autowired
    private OpenRosaSubmissionService openRosaSubmissionService;

    @Autowired
    private StudyDao studyDao;

    @Autowired
    private StudyParameterValueDao studyParameterValueDao;

    @Autowired
    PformSubmissionNotificationService notifier;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    public static final String FORM_CONTEXT = "ecid";

    /**
     * @api {post} /pages/api/v1/editform/:studyOid/submission Submit form data
     * @apiName doSubmission
     * @apiPermission admin
     * @apiVersion 3.8.0
     * @apiParam {String} studyOid Study Oid.
     * @apiParam {String} ecid Key that will be used to look up subject context information while processing submission.
     * @apiGroup Form
     * @apiDescription Submits the data from a completed form.
     */

    @RequestMapping(value = "/{studyOID}/submission", method = RequestMethod.POST)
    public ResponseEntity<String> doSubmission(HttpServletRequest request, HttpServletResponse response,
            @PathVariable("studyOID") String studyOID, @RequestParam(FORM_CONTEXT) String ecid) {

        logger.info("Processing xform submission.");
        HashMap<String, String> subjectContext = null;
        Locale locale = LocaleResolver.getLocale(request);

        DataBinder dataBinder = new DataBinder(null);
        Errors errors = dataBinder.getBindingResult();
        Study study = studyDao.findByOcOID(studyOID);
        String requestBody=null;

        HashMap<String,String> map = new HashMap();
        ArrayList <HashMap> listOfUploadFilePaths = new ArrayList();

        try {
            // Verify Study is allowed to submit
            if (!mayProceed(studyOID)) {
                logger.info("Submissions to the study not allowed.  Aborting submission.");
                return new ResponseEntity<String>(org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
            }
            if (ServletFileUpload.isMultipartContent(request)) {
                String dir = getAttachedFilePath(studyOID);
                FileProperties fileProperties= new FileProperties();
                DiskFileItemFactory factory = new DiskFileItemFactory();
                ServletFileUpload upload = new ServletFileUpload(factory);
                upload.setFileSizeMax(fileProperties.getFileSizeMax());
                List<FileItem> items = upload.parseRequest(request);              
                for (FileItem item : items) {
                    if (item.getContentType() != null && !item.getFieldName().equals("xml_submission_file") ) {
                        if (!new File(dir).exists()) new File(dir).mkdirs();

                        File file = processUploadedFile(item, dir);
                        map.put(item.getFieldName(), file.getPath());

                    } else if (item.getFieldName().equals("xml_submission_file")) {
                        requestBody = item.getString("UTF-8");
                    }
                }
                listOfUploadFilePaths.add(map);
            } else  {                
                requestBody = IOUtils.toString(request.getInputStream(), "UTF-8");
            }

            // Load user context from ecid
            PFormCache cache = PFormCache.getInstance(context);
            subjectContext = cache.getSubjectContext(ecid);

            // Execute save as Hibernate transaction to avoid partial imports
            openRosaSubmissionService.processRequest(study, subjectContext, requestBody, errors, locale , listOfUploadFilePaths);

        } catch (Exception e) {
            logger.error("Exception while processing xform submission.");
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));

            if (!errors.hasErrors()) {
                // Send a failure response
                logger.info("Submission caused internal error.  Sending error response.");
                return new ResponseEntity<String>(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        if (!errors.hasErrors()) {
            // Log submission with Participate
            notifier.notify(studyOID, subjectContext);
            logger.info("Completed xform submission. Sending successful response");
            String responseMessage = "<OpenRosaResponse xmlns=\"http://openrosa.org/http/response\">" + "<message>success</message>" + "</OpenRosaResponse>";
            return new ResponseEntity<String>(responseMessage, org.springframework.http.HttpStatus.CREATED);
        } else {
            logger.info("Submission contained errors. Sending error response");
            return new ResponseEntity<String>(org.springframework.http.HttpStatus.NOT_ACCEPTABLE);
        }
    }

    private Study getParentStudy(String studyOid) {
        Study study = studyDao.findByOcOID(studyOid);
        Study parentStudy = study.getStudy();
        if (parentStudy != null && parentStudy.getStudyId() > 0)
            return parentStudy;
        else
            return study;
    }


    private boolean mayProceed(String studyOid) throws Exception {
        return mayProceed(studyOid, null);
    }

    private boolean mayProceed(String studyOid, StudySubjectBean ssBean) throws Exception {
        boolean accessPermission = false;
        ParticipantPortalRegistrar participantPortalRegistrar= new ParticipantPortalRegistrar();
        Study study = getParentStudy(studyOid);
        StudyParameterValue pStatus = studyParameterValueDao.findByStudyIdParameter(study.getStudyId(), "participantPortal");

        // ACTIVE, PENDING, or INACTIVE
        String pManageStatus = participantPortalRegistrar.getRegistrationStatus(studyOid).toString();

        // enabled or disabled
        String participateStatus = pStatus.getValue().toString();

        // available, pending, frozen, or locked
        String studyStatus = study.getStatus().getName().toString();

        if (ssBean == null) {
            logger.info("pManageStatus: " + pManageStatus + "  participantStatus: " + participateStatus + "   studyStatus: " + studyStatus);
            if (participateStatus.equalsIgnoreCase("enabled") && studyStatus.equalsIgnoreCase("available") && pManageStatus.equalsIgnoreCase("ACTIVE"))
                accessPermission = true;
        } else {
            logger.info("pManageStatus: " + pManageStatus + "  participantStatus: " + participateStatus + "   studyStatus: " + studyStatus
                    + "  studySubjectStatus: " + ssBean.getStatus().getName());
            if (participateStatus.equalsIgnoreCase("enabled") && studyStatus.equalsIgnoreCase("available") && pManageStatus.equalsIgnoreCase("ACTIVE")
                    && ssBean.getStatus() == Status.AVAILABLE)
                accessPermission = true;
        }
        return accessPermission;
    }

    public static String getAttachedFilePath(String studyOid) throws Exception {    	
        String attachedFilePath = CoreResources.getField("attached_file_location");
        if (attachedFilePath == null || attachedFilePath.length() <= 0) {
            attachedFilePath = CoreResources.getField("filePath") + "attached_files" + File.separator;
        }
        File tempFile =  new File(attachedFilePath,studyOid);
        String canonicalPath= tempFile.getCanonicalPath();
        
        if (canonicalPath.startsWith(attachedFilePath)) {
        	if (attachedFilePath == null || attachedFilePath.length() <= 0) {
                attachedFilePath = CoreResources.getField("filePath") + "attached_files" + File.separator + studyOid + File.separator;
            } else {
                attachedFilePath += studyOid + File.separator;
            }
            return attachedFilePath;
        }else {
        	throw new RuntimeException("Traversal attempt - file path not allowed " + studyOid);
        }
        
    }

    private File processUploadedFile(FileItem item, String dirToSaveUploadedFileIn) {
        dirToSaveUploadedFileIn = dirToSaveUploadedFileIn == null ? System.getProperty("java.io.tmpdir") : dirToSaveUploadedFileIn;
        String fileName = item.getName();
        // Some browsers IE 6,7 getName returns the whole path
        int startIndex = fileName.lastIndexOf('\\');
        if (startIndex != -1) {
            fileName = fileName.substring(startIndex + 1, fileName.length());
        }

        File uploadedFile = new File(dirToSaveUploadedFileIn + File.separator + fileName);
        try {
            uploadedFile = new UploadFileServlet().new OCFileRename().rename(uploadedFile, item.getInputStream());
        } catch (IOException e) {
            throw new OpenClinicaSystemException(e.getMessage());
        }

        try {
            item.write(uploadedFile);
        } catch (Exception e) {
            throw new OpenClinicaSystemException(e.getMessage());
        }
        return uploadedFile;
    }
}
