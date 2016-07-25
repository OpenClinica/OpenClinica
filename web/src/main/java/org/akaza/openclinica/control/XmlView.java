package org.akaza.openclinica.control;

import java.util.Collection;
import java.util.List;

import org.jmesa.core.CoreContext;
import org.jmesa.util.ItemUtils;
import org.jmesa.view.AbstractExportView;
import org.jmesa.view.component.Column;
import org.jmesa.view.component.Table;

public class XmlView extends AbstractExportView {

    public XmlView(Table table, CoreContext coreContext) {
        super(table, coreContext);
        //setTable(table);
        //setCoreContext(coreContext);
    }

    public byte[] getBytes() {
        String render = (String) render();
        return render.getBytes();
    }

    public Object render() {
        StringBuilder data = new StringBuilder();

        List<Column> columns = getTable().getRow().getColumns();

        int rowcount = 0;
        Collection<?> items = getCoreContext().getPageItems();
        for (Object item : items) {
            rowcount++;

            if (rowcount == 1)
                data.append("" + ItemUtils.getItemValue(item, "ruleSetRuleId"));
            else
                data.append("," + ItemUtils.getItemValue(item, "ruleSetRuleId"));

        }

        return data.toString();
    }
}
