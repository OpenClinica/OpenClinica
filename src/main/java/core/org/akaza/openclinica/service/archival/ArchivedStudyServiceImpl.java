package core.org.akaza.openclinica.service.archival;

import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.dao.jpa.ArchivedStudyEntity;
import core.org.akaza.openclinica.dao.jpa.ArchivedStudyRepository;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Created by yogi on 4/13/17.
 */
@Service("archivedStudyService")
public class ArchivedStudyServiceImpl implements ArchivedStudyService {
    protected final static Logger LOGGER = LoggerFactory.getLogger(ArchivedStudyServiceImpl.class);
    @Autowired ArchivedStudyRepository studyRepository;
    @Autowired StudyDao studyDao;

    @PersistenceContext
    @Qualifier("archiveEM")
    private EntityManager em;
    @Transactional(transactionManager = "archival", propagation= Propagation.REQUIRED,isolation= Isolation.DEFAULT)
    @Override public ArchivedStudyEntity findByUniqueId(String id) {
        return studyRepository.findOne(id);
    }

    @Transactional(transactionManager = "archival", propagation= Propagation.REQUIRED,isolation= Isolation.DEFAULT)
    @Override
    public boolean archiveStudy(String uniqueId) {
        Study study = studyDao.findByUniqueId(uniqueId);
        LOGGER.debug("Study: {}",study.getSchemaName());
        byte[] data = getStudy(study);
        return storeStudy(study, data);
    }

    @Transactional(transactionManager = "archival", propagation= Propagation.REQUIRED,isolation= Isolation.DEFAULT)
    @Override
    public boolean restoreStudy(String uniqueId) {
        // Retrieve image from DB
        ArchivedStudyEntity studyEntity = studyRepository.findOne(uniqueId);
        byte[] data = studyEntity.getStudyData();
        return false;
    }


    private boolean storeStudy(Study study, byte[] data) {
        ArchivedStudyEntity studyEntity = new ArchivedStudyEntity();
        studyEntity.setStudyData(data);
        studyEntity.setDatePlannedEnd(study.getDatePlannedEnd());
        studyEntity.setDatePlannedStart(study.getDatePlannedStart());
        studyEntity.setSchemaName(study.getSchemaName());
        studyEntity.setDateCreated(study.getDateCreated());
        studyEntity.setDateUpdated(study.getDateUpdated());
        studyEntity.setUniqueIdentifier(study.getUniqueIdentifier());
        studyEntity.setName(study.getName());
        studyEntity.setSummary(study.getSummary());
        String identifier = studyRepository.save(studyEntity).getUniqueIdentifier();
        return StringUtils.isNotEmpty(identifier);
    }

    private byte[] getStudy(Study study) {

        // get the input stream
        ProcessBuilder pb =
                new ProcessBuilder("/opt/PostgreSQL/9.5/bin/pg_dump", "--host", "localhost",
                        "--port", CoreResources.getField("dbPort"),
                        "--username", CoreResources.getField("dbUser"),
                        "-n", study.getSchemaName(),
                        "--no-password",
                        "--format", "t",
                        "-b", CoreResources.getField("db"));
        final Map<String, String> env = pb.environment();
        env.put("PGPASSWORD", CoreResources.getField("dbPass"));
        pb.redirectErrorStream(true);
        Process p = null;
        try {
            p = pb.start();
        } catch (IOException e) {
            LOGGER.error("ProcessBuilder is not getting initialized properly: ",e);
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            IOUtils.copy(p.getInputStream(), bos);
            return bos.toByteArray();
        } catch (IOException e) {
            LOGGER.error("Process not able to copy the file: ",e);
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }
        return null;

    }
}
