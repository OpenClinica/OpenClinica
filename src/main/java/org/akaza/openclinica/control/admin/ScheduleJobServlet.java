package org.akaza.openclinica.control.admin;

import core.org.akaza.openclinica.service.PermissionService;
import core.org.akaza.openclinica.service.SchedulerUtilService;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.view.Page;
import org.quartz.JobKey;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;

public abstract class ScheduleJobServlet extends SecureController {
    protected static final String PERIOD = "periodToRun";
    protected static final String FORMAT_ID = "formatId";
    protected static final String DATASET_ID = "dsId";
    protected static final String DATE_START_JOB = "job";
    protected static final String EMAIL = "contactEmail";
    protected static final String JOB_NAME = "jobName";
    protected static final String JOB_DESC = "jobDesc";
    protected static final String USER_ID = "user_id";
    protected static final String NUMBER_OF_FILES_TO_SAVE = "numberOfFilesToSave";
    protected static String TRIGGER_IMPORT_GROUP = "importTrigger";
    protected static String TRIGGER_EXPORT_GROUP = "XsltTriggersExportJobs";

    protected PermissionService permissionService;
    protected SchedulerUtilService schedulerUtilService;
    public ApplicationContext applicationContext;

    @Override
    protected abstract void processRequest() throws Exception;

    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin() || ub.isTechAdmin()) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_allowed_access_extract_data_servlet"), "1");
    }

    protected PermissionService getPermissionService() {
        return permissionService = (PermissionService) SpringServletAccess.getApplicationContext(context).getBean("permissionService");
    }

    protected SchedulerUtilService getSchedulerUtilService() {
        return schedulerUtilService = (SchedulerUtilService) SpringServletAccess.getApplicationContext(context).getBean("schedulerUtilService");
    }

    protected ApplicationContext getApplicationContext() {
        return applicationContext = SpringServletAccess.getApplicationContext(context);
    }


    public HashMap validateForm(FormProcessor fp, HttpServletRequest request, JobKey[] jobKeys, String properName) {
        Validator v = new Validator(request);
        v.addValidation(JOB_NAME, Validator.NO_BLANKS);
        v.addValidation(JOB_NAME, Validator.NO_LEADING_OR_TRAILING_SPACES);
        v.addValidation(JOB_DESC, Validator.NO_BLANKS);
        v.addValidation(EMAIL, Validator.IS_A_EMAIL);
        v.addValidation(PERIOD, Validator.NO_BLANKS);
        v.addValidation(DATE_START_JOB + "Date", Validator.IS_A_DATE);

        int formatId = fp.getInt(FORMAT_ID);
        Date jobDate = fp.getDateTime(DATE_START_JOB);
        int datasetId = fp.getInt(DATASET_ID);
        HashMap errors = v.validate();
        if (formatId == 0) {
            v.addError(errors, FORMAT_ID, "Please pick a file format.");
        }
        for (JobKey jobKey : jobKeys) {
            if (jobKey.getName().equals(fp.getString(JOB_NAME)) && !jobKey.getName().equals(properName)) {
                v.addError(errors, JOB_NAME, "A job with that name already exists.  Please pick another name.");
            }
        }
        if (jobDate.before(new Date())) {
            v.addError(errors, DATE_START_JOB + "Date", "This date needs to be later than the present time.");
        }
        // limit the job description to 250 characters
        String jobDesc = fp.getString(JOB_DESC);
        if (null != jobDesc && !jobDesc.equals("")) {
            if (jobDesc.length() > 250) {
                v.addError(errors, JOB_DESC, "A job description cannot be more than 250 characters.");
            }
        }
        if (datasetId == 0) {
            v.addError(errors, DATASET_ID, "Please pick a dataset.");
        }
        return errors;
    }
}