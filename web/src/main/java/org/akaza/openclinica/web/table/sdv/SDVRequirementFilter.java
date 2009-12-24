package org.akaza.openclinica.web.table.sdv;

import org.akaza.openclinica.domain.SourceDataVerification;
import org.jmesa.view.html.editor.DroplistFilterEditor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: bruceperry
 * Date: May 19, 2009
 */
public class SDVRequirementFilter extends DroplistFilterEditor {

    @Override
    protected List<Option> getOptions() {
        List<Option> options = new ArrayList<Option>();
        String optionA = SourceDataVerification.AllREQUIRED.toString() + " & " + SourceDataVerification.PARTIALREQUIRED.toString();
        options.add(new Option(optionA, optionA));
        for (SourceDataVerification sdv : SourceDataVerification.values()) {
            if (sdv != SourceDataVerification.NOTAPPLICABLE) {
                options.add(new Option(sdv.toString(), sdv.toString()));
            }
        }

        return options;
    }
}
