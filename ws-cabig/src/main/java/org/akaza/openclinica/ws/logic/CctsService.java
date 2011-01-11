package org.akaza.openclinica.ws.logic;

import org.akaza.openclinica.bean.managestudy.SubjectTransferBean;
import org.akaza.openclinica.service.subject.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

public abstract class CctsService {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private SubjectService subjectService;
    private String waitBeforeCommit;

    public void commit(SubjectTransferBean subjectTransferBean) {
        boolean isSubjectInQueue = isSubjectInQueue();
        boolean isSubjectInMain = doesSubjectExit();

        if (isSubjectInMain) {
            // TODO : either return something or throw exception or don't do anything
        } else if (isSubjectInQueue) {
            createSubject(subjectTransferBean);
        } else {
            createSubjectTransfer(subjectTransferBean);
        }
    }

    public void rollback() {

        boolean isSubjectInQueue = true; // subjectAlreadyExistInTemporaryLocation();
        boolean isSubjectInMain = true; // jectExistInDatabase();

        if (isSubjectInMain) {
            // TODO : remove subject
        }
        if (isSubjectInQueue) {
            // TODO : Remove from Queue
        } else {
            // TODO : Do nothing
        }

    }

    public void autoCommit() {
        List<SubjectTransferBean> subjectTransfers = getAllSubjectsInQueue();
        for (SubjectTransferBean subjectTransferBean : subjectTransfers) {
            if (isSubjectInQueueForMoreThanXSeconds(subjectTransferBean)) {
                // TODO: 2. Create Subject , StudySubject then save
                removeSubjectFromQueue(subjectTransferBean);
            }
        }
        logger.debug("The time is : " + new Date());
    }

    public abstract boolean isSubjectInQueue();

    public abstract boolean doesSubjectExit();

    public abstract void createSubjectTransfer(SubjectTransferBean subjectTransfer);

    public abstract void createSubject(SubjectTransferBean subjectTransfer);

    public abstract List<SubjectTransferBean> getAllSubjectsInQueue();

    public abstract void removeSubjectFromQueue(SubjectTransferBean subjectTransfer);

    private boolean isSubjectInQueueForMoreThanXSeconds(SubjectTransferBean subjectTransferBean) {
        // TODO: 1.Implement Method
        return true;
    }

    public SubjectService getSubjectService() {
        return subjectService;
    }

    public void setSubjectService(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    public String getWaitBeforeCommit() {
        return waitBeforeCommit;
    }

    public void setWaitBeforeCommit(String waitBeforeCommit) {
        this.waitBeforeCommit = waitBeforeCommit;
    }

}
