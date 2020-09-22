package org.akaza.openclinica.config;

import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.service.StudyBuildService;
import core.org.akaza.openclinica.web.rest.client.auth.impl.KeycloakClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Configuration class that retrieves board urls for existing studies on application startup.
 * @author svadla@openclinica.com
 */
@Component
@DependsOn("liquibase")
public class BoardUrlConfig {
  protected static final Logger logger = LoggerFactory.getLogger(BoardUrlConfig.class);

  @Autowired
  private StudyDao studyDao;
  @Autowired
  private KeycloakClientImpl keycloakClient;
  @Autowired
  private StudyBuildService studyBuildService;

  @PostConstruct
  public void retrieveBoardUrls() throws BeansException {
    logger.info("Setting board urls for existing studies");
    String systemToken = keycloakClient.getSystemToken();
    logger.info("System token: {}", systemToken);
    studyDao.findAllParents().forEach(
            study -> {
              String schema = study.getSchemaName();
              CoreResources.setRequestSchema(schema);
              Study tenantSchemaStudy = studyDao.findStudyByOid(study.getOc_oid());
              setBoardUrl(tenantSchemaStudy, systemToken);
            }
    );
    CoreResources.setRequestSchemaToPublic();
  }

  private void setBoardUrl(Study study, String accessToken) {
    String boardUrl = study.getBoardUrl();
    if (boardUrl == null) {
      logger.info("Setting board url for study: {}", study.getOc_oid());
      boardUrl = studyBuildService.getCurrentBoardUrl(accessToken, study);
      study.setBoardUrl(boardUrl);
      studyDao.update(study);
    }
  }
}
