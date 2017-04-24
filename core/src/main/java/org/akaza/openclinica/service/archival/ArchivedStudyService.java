package org.akaza.openclinica.service.archival;

import org.akaza.openclinica.dao.jpa.ArchivedStudyEntity;
import org.springframework.stereotype.Service;

/**
 * Created by yogi on 4/13/17.
 */
public interface ArchivedStudyService {
    ArchivedStudyEntity findByUniqueId(String id);
    boolean archiveStudy(String uniqueId);
    boolean restoreStudy(String uniqueId);
}
