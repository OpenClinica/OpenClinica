package core.org.akaza.openclinica.web.table.sdv;

import core.org.akaza.openclinica.domain.datamap.SubjectEventStatus;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.jmesa.view.html.editor.DroplistFilterEditor;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SubjectEventStatusFilter extends DroplistFilterEditor {
    @Override
    protected List<Option> getOptions() {
        ResourceBundle resWords = ResourceBundleProvider.getWordsBundle();
        List<Option> options = new ArrayList<Option>();
        for (SubjectEventStatus subjectEventStatus : SubjectEventStatus.values()) {
            if (subjectEventStatus != SubjectEventStatus.INVALID) {
                String subjectEventStatusDesc = resWords.getString(subjectEventStatus.getDescription());
                options.add(new Option(subjectEventStatusDesc, subjectEventStatusDesc));
            }
        }
        return options;
    }
}
