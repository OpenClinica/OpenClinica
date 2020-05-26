package core.org.akaza.openclinica.web.table.sdv;

import org.akaza.openclinica.domain.enumsupport.EventCrfWorkflowStatusEnum;
import org.akaza.openclinica.domain.enumsupport.StudyEventWorkflowStatusEnum;
import org.jmesa.view.html.editor.DroplistFilterEditor;

import java.util.ArrayList;
import java.util.List;

/**
 * The drop list for the CRF Status filter.
 */
public class CrfStatusFilter extends DroplistFilterEditor {
    @Override
    protected List<Option> getOptions() {
        List<Option> options = new ArrayList<Option>();
        options.add(new Option(EventCrfWorkflowStatusEnum.COMPLETED.getDisplayValue(), EventCrfWorkflowStatusEnum.COMPLETED.getDisplayValue()));
        options.add(new Option(EventCrfWorkflowStatusEnum.LOCKED.getDisplayValue(),EventCrfWorkflowStatusEnum.LOCKED.getDisplayValue()));
        return options;
    }
}
