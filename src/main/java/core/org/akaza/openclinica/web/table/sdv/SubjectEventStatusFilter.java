package core.org.akaza.openclinica.web.table.sdv;

import core.org.akaza.openclinica.domain.datamap.SubjectEventStatus;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.domain.enumsupport.StudyEventWorkflowStatusEnum;
import org.jmesa.view.html.editor.DroplistFilterEditor;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SubjectEventStatusFilter extends DroplistFilterEditor {
    @Override
    protected List<Option> getOptions() {
        List<Option> options = new ArrayList<Option>();
        for (StudyEventWorkflowStatusEnum workflowStatus : StudyEventWorkflowStatusEnum.values()) {
                String subjectEventStatusDesc = workflowStatus.getDisplayValue();
                options.add(new Option(subjectEventStatusDesc, subjectEventStatusDesc));
        }
        return options;
    }
}
