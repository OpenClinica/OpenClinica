package core.org.akaza.openclinica.web.table.sdv;

import org.jmesa.view.html.editor.DroplistFilterEditor;

import java.util.ArrayList;
import java.util.List;

public class OpenQueriesFilter  extends DroplistFilterEditor {
    @Override
    protected List<Option> getOptions() {
        List<Option> options = new ArrayList<Option>();
        options.add(new Option("No","No"));
        options.add(new Option("Yes","Yes"));
        return options;
    }
}