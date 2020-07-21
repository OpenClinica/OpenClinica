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
        options.add(new Option("Locked", "Locked"));
        options.add(new Option("Signed", "Signed"));
        options.add(new Option("Signed and Locked","Signed and Locked"));
        options.add(new Option("Not Signed", "Not Signed"));
        options.add(new Option("Not Locked","Not Locked"));
        options.add(new Option("Not Signed and Not Locked","Not Signed and Not Locked"));

        return options;
    }
}
