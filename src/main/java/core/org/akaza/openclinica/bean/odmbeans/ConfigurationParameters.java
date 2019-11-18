package core.org.akaza.openclinica.bean.odmbeans;

public class ConfigurationParameters {
    private boolean hiddenCrf;
    private boolean participantForm;
    private boolean allowAnynymousSubmission;
    private boolean offline;
    private String submissionUrl;

    public boolean isHiddenCrf() {
        return hiddenCrf;
    }

    public void setHiddenCrf(boolean hiddenCrf) {
        this.hiddenCrf = hiddenCrf;
    }

    public boolean isParticipantForm() {
        return participantForm;
    }

    public void setParticipantForm(boolean participantForm) {
        this.participantForm = participantForm;
    }

    public boolean isAllowAnynymousSubmission() {
        return allowAnynymousSubmission;
    }

    public void setAllowAnynymousSubmission(boolean allowAnynymousSubmission) {
        this.allowAnynymousSubmission = allowAnynymousSubmission;
    }

    public boolean isOffline() {
        return offline;
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }

    public String getSubmissionUrl() {
        return submissionUrl;
    }

    public void setSubmissionUrl(String submissionUrl) {
        this.submissionUrl = submissionUrl;
    }

}
