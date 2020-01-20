package core.org.akaza.openclinica.web.table.sdv;

import core.org.akaza.openclinica.domain.SourceDataVerification;
import org.akaza.openclinica.domain.enumsupport.SdvStatus;
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
        String optionA = SdvStatus.NOT_VERIFIED.getDisplayValue() + " & " + SdvStatus.CHANGED_AFTER_VERIFIED.getDisplayValue();
        options.add(new Option(optionA, optionA));
        for (SdvStatus sdv : SdvStatus.values()) {
                options.add(new Option(sdv.getDisplayValue(), sdv.getDisplayValue()));
        }
        return options;
    }
}
