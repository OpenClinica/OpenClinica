package core.org.akaza.openclinica.service.dto;

public class ODMFilterDTO {

    boolean includeDN = false;
    boolean includeAudit = false;
    boolean crossForm = false;
    boolean archived = false;
    boolean metadata = true;
    boolean clinical = true;
    boolean links = false;

    public ODMFilterDTO() {
        includeDN = false;
        includeAudit = false;
        crossForm = false;
        archived = false;
        metadata = true;
        clinical = true;
        links = false;
    }

    public ODMFilterDTO(String includeDns, String includeAudits, String crossFormLogic, String showArchived, String metadata, String clinicalData, String links) {

        if (showArchived != null && (showArchived.equalsIgnoreCase("yes") || showArchived.equalsIgnoreCase("y")))
            setArchived(true);
        if (crossFormLogic.equalsIgnoreCase("yes") || crossFormLogic.equalsIgnoreCase("y"))
            setCrossForm(true);
        if (metadata.equalsIgnoreCase("no") || metadata.equalsIgnoreCase("n"))
            setMetadata(false);
        if (includeDns.equalsIgnoreCase("yes") || includeDns.equalsIgnoreCase("y"))
            setIncludeDN(true);
        if (includeAudits.equalsIgnoreCase("yes") || includeAudits.equalsIgnoreCase("y"))
            setIncludeAudit(true);
        if (clinicalData.equalsIgnoreCase("no") ||  clinicalData.equalsIgnoreCase("n"))
            setClinical(false);
        if (links.equalsIgnoreCase("yes") ||  links.equalsIgnoreCase("y"))
            setLinks(true);
    }

    public boolean isIncludeDN() {
        return includeDN;
    }
    public void setIncludeDN(boolean includeDN) {
        this.includeDN = includeDN;
    }

    public boolean isIncludeAudit() {
        return includeAudit;
    }
    public void setIncludeAudit(boolean includeAudit) {
        this.includeAudit = includeAudit;
    }

    public boolean isCrossForm() {
        return crossForm;
    }
    public void setCrossForm(boolean crossForm) {
        this.crossForm = crossForm;
    }

    public boolean showArchived() { return archived; }
    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public boolean includeMetadata() {
        return metadata;
    }
    public void setMetadata(boolean metadata) {
        this.metadata = metadata;
    }

    public boolean includeClinical() {
        return clinical;
    }
    public void setClinical(boolean clinical) {
        this.clinical = clinical;
    }

    public boolean includeLinks() {
        return links;
    }
    public void setLinks(boolean links) {
        this.links = links;
    }
}
