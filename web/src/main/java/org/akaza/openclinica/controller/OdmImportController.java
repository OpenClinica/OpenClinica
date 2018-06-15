package org.akaza.openclinica.controller;

import io.swagger.annotations.Api;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.service.CustomRuntimeException;
import org.akaza.openclinica.service.OdmImportService;
import org.akaza.openclinica.service.Page;
import org.akaza.openclinica.service.PublishDTO;
import org.akaza.openclinica.service.crfdata.ErrorObj;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.cdisc.ns.odm.v130.ODM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Api(value = "Study", tags = { "Study" }, description = "REST API for Study")
public class OdmImportController {
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
        Page page = publishDTO.getPage();
        Instant start = Instant.now();
        String accessToken = (String) request.getSession().getAttribute("accessToken");

        try {
            Map<String, Object> map = (Map<String, Object>) odmImportService.importOdm(odm, page, boardId, accessToken);
            Study study = (Study) map.get("study");
            Study publicStudy = studyDao.findPublicStudy(study.getOc_oid());
            odmImportService.updatePublicStudyPublishedFlag(publicStudy);
            odmImportService.setPublishedVersionsInFM(map, accessToken);
            Instant end = Instant.now();
            logger.info("***** Time execustion for {} method : {}   *****", new Object() {
            }.getClass().getEnclosingMethod().getName(), Duration.between(start, end));

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (CustomRuntimeException e) {
            return new ResponseEntity<>(e.getErrList(), HttpStatus.BAD_REQUEST);
        }

    }

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
	@RequestMapping(value = "/studies/{studyEnvUuid}/publishJobs", method = RequestMethod.POST)
	public String importOdmToOC(@RequestBody PublishDTO publishDTO, HttpServletRequest request)
			throws Exception {

        String accessToken = (String) request.getSession().getAttribute("accessToken");
        CompletableFuture<ResponseEntity<Object>> future = CompletableFuture.supplyAsync(() -> {

            ODM odm = publishDTO.getOdm();
            Page page = publishDTO.getPage();
            Map<String, Object> map = null;
            try {
                CoreResources.tenantSchema.set("public");
                map = (Map<String, Object>) odmImportService.importOdm(odm, page, publishDTO.getBoardId(), accessToken);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
            Study study = (Study) map.get("study");
            Study publicStudy = studyDao.findPublicStudy(study.getOc_oid());
            odmImportService.updatePublicStudyPublishedFlag(publicStudy);
            odmImportService.setPublishedVersionsInFM(map, accessToken);
            return new ResponseEntity<Object>(null, HttpStatus.OK);
        });
        String uuid = UUID.randomUUID().toString();
        System.out.println(uuid);
        synchronized (expiringMap) {
            expiringMap.put(uuid, future);
        }
        return uuid;

    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/publishJobs/{uuid}", method = RequestMethod.GET)
    public ResponseEntity<Object>  checkPublishStatus(@PathVariable("uuid") String publishUuid,
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
                e.printStackTrace();
                logger.info("InterruptedException for :" + publishUuid + e.getMessage());
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            } catch (CustomRuntimeException e) {
                logger.info("CustomRuntimeException for :" + publishUuid + e.getMessage());
                logger.info("CustomRuntimeException for :" + publishUuid + e.getErrList().toString());
                return new ResponseEntity<>(e.getErrList(), HttpStatus.BAD_REQUEST);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause != null && cause instanceof CustomRuntimeException) {
                    logger.info("ExecutionException for :" + publishUuid + e.getMessage());
                    logger.info("ExecutionException for :" + publishUuid + ((CustomRuntimeException) cause).getErrList().toString());
                    return new ResponseEntity<>(((CustomRuntimeException) cause).getErrList(), HttpStatus.BAD_REQUEST);
                } else {
                    List<ErrorObj> err = new ArrayList<>();
                    ErrorObj errorObj = new ErrorObj(e.getMessage(), e.getMessage());
                    err.add(errorObj);
                    e.printStackTrace();
                    logger.info("ExecutionException but not CustomRuntimeException for :" + publishUuid, e);
                    return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
                }
            }
        } else {
            return new ResponseEntity<>(HttpStatus.PROCESSING.getReasonPhrase(), HttpStatus.OK);
        }
    }
}
