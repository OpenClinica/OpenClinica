package org.akaza.openclinica.domain.rule;

import org.akaza.openclinica.domain.AbstractAuditableMutableDomainObject;

import java.util.ArrayList;

public class AuditableBeanWrapper<T extends AbstractAuditableMutableDomainObject> {
    private T auditableBean;
    private boolean isSavable;
    private ArrayList<String> importErrors;

    public AuditableBeanWrapper(T auditableBean) {
        importErrors = new ArrayList<String>();
        this.auditableBean = auditableBean;
        isSavable = true;
    }

    public void error(String message) {
        importErrors.add(message);
        setSavable(false);
    }

    public void warning(String message) {
        importErrors.add(message);
    }

    public T getAuditableBean() {
        return auditableBean;
    }

    public void setAuditableBean(T auditableBean) {
        this.auditableBean = auditableBean;
    }

    public ArrayList<String> getImportErrors() {
        return importErrors;
    }

    public void setImportErrors(ArrayList<String> importErrors) {
        this.importErrors = importErrors;
    }

    public boolean isSavable() {
        return isSavable;
    }

    public void setSavable(boolean isSavable) {
        this.isSavable = isSavable;
    }

}
