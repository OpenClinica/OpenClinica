package org.akaza.openclinica.web.table.sdv;

import org.jmesa.view.html.editor.DroplistFilterEditor;

import java.util.ArrayList;
import java.util.List;

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
        options.add(new Option("complete", "Complete"));
        options.add(new Option("none", "None"));
        return options;
    }
}
