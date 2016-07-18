package org.akaza.openclinica.web.table.sdv;

import org.jmesa.view.html.editor.DroplistFilterEditor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: bruceperry
 * Date: May 19, 2009
 */
public class SubjectStatusFilter extends DroplistFilterEditor {
    //AVAILABLE, PENDING, PRIVATE, UNAVAILABLE, LOCKED, DELETED,
    // AUTO_DELETED, SIGNED, FROZEN,SOURCE_DATA_VERIFICATION
    @Override
    protected List<Option> getOptions()  {
        List<Option> options = new ArrayList<Option>();
        options.add(new Option("available","Available"));
        options.add(new Option("pending", "Pending"));
        options.add(new Option("private", "Private"));
        options.add(new Option("unavailable", "Unavailable"));
        options.add(new Option("locked", "Locked"));
        options.add(new Option("deleted", "Deleted"));
        options.add(new Option("auto_deleted", "Auto deleted"));
        options.add(new Option("signed", "Signed"));
        options.add(new Option("frozen", "Frozen"));
        options.add(new Option("source_data_verification", "SDV"));
        return options;
    }
}
