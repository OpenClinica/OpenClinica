package core.org.akaza.openclinica.web.table.sdv;

import org.jmesa.view.html.editor.DroplistFilterEditor;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: bruceperry
 * Date: May 27, 2009
 * Time: 4:49:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class SdvStatusFilter extends DroplistFilterEditor {
    @Override
    protected List<Option> getOptions()  {
        List<Option> options = new ArrayList<Option>();
        options.add(new Option("ready_to_verify_and_change_since_verified", "Ready to verify + Change since verified"));
        options.add(new Option("ready_to_verify","Ready to verify"));
        options.add(new Option("change_since_verified", "Change since verified"));
        options.add(new Option("verified", "Verified"));
        return options;
    }
}
