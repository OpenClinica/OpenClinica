package org.akaza.openclinica.controller;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.dao.hibernate.JobDetailDao;
import core.org.akaza.openclinica.dao.hibernate.UserAccountDao;
import core.org.akaza.openclinica.domain.datamap.JobDetail;
import core.org.akaza.openclinica.domain.enumsupport.JobStatus;
import core.org.akaza.openclinica.domain.enumsupport.JobType;
import core.org.akaza.openclinica.domain.user.UserAccount;
import core.org.akaza.openclinica.exception.OpenClinicaSystemException;
import core.org.akaza.openclinica.service.UtilService;
import core.org.akaza.openclinica.web.util.ErrorConstants;
import io.swagger.annotations.Api;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.service.CustomRuntimeException;
import core.org.akaza.openclinica.service.OdmImportService;
import org.akaza.openclinica.service.Page;
import core.org.akaza.openclinica.service.PublishDTO;
import core.org.akaza.openclinica.service.crfdata.ErrorObj;
import org.akaza.openclinica.service.UserService;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.cdisc.ns.odm.v130.ODM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;


@RestController
@RequestMapping(value = "/auth/api")
@Api(value = "Study", tags = {"Study"}, description = "REST API for Study")
public class OdmImportController {
    @Autowired
    private UserService userService;
    @Autowired
    private UtilService utilService;
    @Autowired
    private UserAccountDao userAccountDao;
    @Autowired
    private JobDetailDao jobDetailDao;

    OdmImportService odmImportService;

    private StudyDao studyDao;
    PassiveExpiringMap<String, Future<ResponseEntity<Object>>> expiringMap =
            new PassiveExpiringMap<>(24, TimeUnit.HOURS);

    public OdmImportController(OdmImportService odmImportService, StudyDao studyDao) {
        super();
        this.odmImportService = odmImportService;
        this.studyDao = studyDao;
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/v1/studyversion/boardId/{boardId}", method = RequestMethod.POST)
    public ResponseEntity<Object> importOdmToOC(@RequestBody PublishDTO publishDTO, @PathVariable("boardId") String boardId, HttpServletRequest request)
            throws Exception {

        ODM odm = publishDTO.getOdm();
        List<Page> pages = publishDTO.getPages();
        Instant start = Instant.now();
        String accessToken = (String) request.getSession().getAttribute("accessToken");

        try {
            Map<String, Object> map = odmImportService.importOdm(odm, pages, boardId, accessToken);
            Study study = (Study) map.get("study");
            Study publicStudy = studyDao.findPublicStudy(study.getOc_oid());
            odmImportService.updatePublicStudyPublishedFlag(publicStudy);
            odmImportService.setPublishedVersionsInFM(map, accessToken);
            Instant end = Instant.now();
            logger.info("***** Time execution for {} method : {}   *****", new Object() {
            }.getClass().getEnclosingMethod().getName(), Duration.between(start, end));
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (CustomRuntimeException e) {
            return new ResponseEntity<>(e.getErrList(), HttpStatus.BAD_REQUEST);
        }
    }

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @RequestMapping(value = "/studies/{studyEnvUuid}/publishJobs", method = RequestMethod.POST)
    public ResponseEntity<String> importOdmToOC(@PathVariable("studyEnvUuid") String studyEnvUuid, @RequestBody PublishDTO publishDTO, HttpServletRequest request)
            throws Exception {

        String accessToken = (String) request.getSession().getAttribute("accessToken");
        Study study = studyDao.findByStudyEnvUuid(studyEnvUuid);
        String studyOid = study.getOc_oid();
        utilService.setSchemaFromStudyOid(studyOid);
        study = studyDao.findByStudyEnvUuid(studyEnvUuid);
        Study site = studyDao.findByOcOID(studyOid);
        UserAccountBean userAccountBean = utilService.getUserAccountFromRequest(request);
        UserAccount userAccount = userAccountDao.findById(userAccountBean.getId());
        String fileName = study.getName();

        //check publish status of study so no 2 processes can occur at the same time
        if (!jobDetailDao.findStudyIdWithStatus(study.getStudyId(), JobStatus.IN_PROGRESS).isEmpty()) {
            new ResponseEntity(ErrorConstants.ERROR_OTHER_PROCESS_IN_PROGRESS, HttpStatus.OK);
        }

        JobDetail jobDetail = userService.persistJobCreated(study, site, userAccount, JobType.PUBLISH_STUDY, fileName);

        CompletableFuture<ResponseEntity<Object>> future = CompletableFuture.supplyAsync(() -> {
            Map<String, Object> map;
            ODM odm = publishDTO.getOdm();
            List<Page> pages = publishDTO.getPages();
            try {
                CoreResources.tenantSchema.set("public");
                map = odmImportService.importOdm(odm, pages, publishDTO.getBoardId(), accessToken);
            } catch (Exception e) {
                utilService.setSchemaFromStudyOid(studyOid);
                userService.persistJobFailed(jobDetail, fileName);
                throw new CompletionException(e);
            }
            Study publicStudy = studyDao.findPublicStudy(studyOid);
            odmImportService.updatePublicStudyPublishedFlag(publicStudy);
            odmImportService.setPublishedVersionsInFM(map, accessToken);
            utilService.setSchemaFromStudyOid(studyOid);
            userService.persistJobCompleted(jobDetail, fileName);
            return new ResponseEntity<>(null, HttpStatus.OK);
        });

        String uuid = UUID.randomUUID().toString();
        logger.debug(uuid);
        synchronized (expiringMap) {
            expiringMap.put(uuid, future);
        }
        return new ResponseEntity<>(uuid, HttpStatus.OK);
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/publishJobs/{uuid}", method = RequestMethod.GET)
    public ResponseEntity<Object> checkPublishStatus(@PathVariable("uuid") String publishUuid,
                                                     HttpServletRequest request) {
        Future<ResponseEntity<Object>> future = null;
        synchronized (expiringMap) {
            future = expiringMap.get(publishUuid);
        }
        if (future == null) {
            logger.info("Publish Future :" + publishUuid + " couldn't be found");
            return new ResponseEntity<>("Publish Future :" + publishUuid + " couldn't be found", HttpStatus.BAD_REQUEST);
        } else if (future.isDone()) {
            try {
                ResponseEntity<Object> objectResponseEntity = future.get();
                return new ResponseEntity<>("Completed", HttpStatus.OK);
            } catch (InterruptedException e) {
                logger.info("InterruptedException for :{} ", publishUuid, e);
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            } catch (CustomRuntimeException e) {
                logger.info("CustomRuntimeException for :{} {}", publishUuid, e.getMessage());
                logger.info("CustomRuntimeException for :{} {}", publishUuid, e.getErrList().toString());
                return new ResponseEntity<>(e.getErrList(), HttpStatus.BAD_REQUEST);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause != null && cause instanceof CustomRuntimeException) {
                    logger.info("ExecutionException for : {} {}", publishUuid, e.getMessage());
                    logger.info("ExecutionException for :{} {}", publishUuid, ((CustomRuntimeException) cause).getErrList().toString());
                    return new ResponseEntity<>(((CustomRuntimeException) cause).getErrList(), HttpStatus.BAD_REQUEST);
                } else {
                    List<ErrorObj> err = new ArrayList<>();
                    ErrorObj errorObj = new ErrorObj(e.getMessage(), e.getMessage());
                    err.add(errorObj);
                    logger.info("ExecutionException but not CustomRuntimeException for : {}", publishUuid, e);
                    return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
                }
            }
        } else {
            return new ResponseEntity<>(HttpStatus.PROCESSING.getReasonPhrase(), HttpStatus.OK);
        }
    }
}
