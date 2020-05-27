package core.org.akaza.openclinica.web.table.sdv;

import org.jmesa.view.html.editor.DroplistFilterEditor;

import java.util.ArrayList;
import java.util.List;

public class LockStatusFilter extends DroplistFilterEditor {
    @Override
    protected List<Option> getOptions() {
        List<Option> options = new ArrayList<Option>();
        options.add(new Option("Locked", "Locked"));
        options.add(new Option("Not locked","Not locked"));
        return options;
    }
}