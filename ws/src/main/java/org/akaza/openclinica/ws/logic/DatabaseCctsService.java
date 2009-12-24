package org.akaza.openclinica.ws.logic;

import org.akaza.openclinica.bean.managestudy.SubjectTransferBean;

import java.util.ArrayList;
import java.util.List;

public class DatabaseCctsService extends CctsService {

    @Override
    public boolean isSubjectInQueue() {
        // TODO: Implement this
        return true;
    }

    @Override
    public boolean doesSubjectExit() {
        // TODO: Implement this
        return true;
    }

    @Override
    public void createSubject(SubjectTransferBean subjectTransfer) {
        // TODO Auto-generated method stub

    }

    @Override
    public void createSubjectTransfer(SubjectTransferBean subjectTransfer) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<SubjectTransferBean> getAllSubjectsInQueue() {
        // TODO Auto-generated method stub
        return new ArrayList<SubjectTransferBean>();
    }

    @Override
    public void removeSubjectFromQueue(SubjectTransferBean subjectTransfer) {
        // TODO Auto-generated method stub

    }

}
