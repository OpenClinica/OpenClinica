package org.akaza.openclinica.control;

import org.jmesa.view.ViewUtils;
import org.jmesa.view.component.Row;
import org.jmesa.view.html.HtmlBuilder;
import org.jmesa.view.html.toolbar.AbstractItem;
import org.jmesa.view.html.toolbar.AbstractToolbar;
import org.jmesa.view.html.toolbar.MaxRowsItem;
import org.jmesa.view.html.toolbar.ToolbarItemType;

import java.util.List;

public class DefaultToolbar extends AbstractToolbar {

    public DefaultToolbar() {
        super();

    }
    public boolean showMoreLink;
    @SuppressWarnings("unchecked")
    @Override
    public String render() {
        addToolbarItem(ToolbarItemType.FIRST_PAGE_ITEM);
        addToolbarItem(ToolbarItemType.PREV_PAGE_ITEM);
        addToolbarItem(ToolbarItemType.NEXT_PAGE_ITEM);
        addToolbarItem(ToolbarItemType.LAST_PAGE_ITEM);

        addToolbarItem(ToolbarItemType.SEPARATOR);

        MaxRowsItem maxRowsItem = (MaxRowsItem) addToolbarItem(ToolbarItemType.MAX_ROWS_ITEM);
        if (getMaxRowsIncrements() != null) {
            maxRowsItem.setIncrements(getMaxRowsIncrements());
        }

        boolean exportable = ViewUtils.isExportable(getExportTypes());

        if (exportable) {
            addToolbarItem(ToolbarItemType.SEPARATOR);
            addExportToolbarItems(getExportTypes());
        }

        Row row = getTable().getRow();
        List columns = row.getColumns();

        boolean filterable = ViewUtils.isFilterable(columns);

        if (filterable) {
            //addToolbarItem(ToolbarItemType.SEPARATOR);
            //addToolbarItem(ToolbarItemType.FILTER_ITEM);
            //addToolbarItem(ToolbarItemType.CLEAR_ITEM);
        }

        boolean editable = ViewUtils.isEditable(getCoreContext().getWorksheet());

        if (editable) {
            addToolbarItem(ToolbarItemType.SEPARATOR);
            addToolbarItem(ToolbarItemType.SAVE_WORKSHEET_ITEM);
            addToolbarItem(ToolbarItemType.FILTER_WORKSHEET_ITEM);
        }

        addToolbarItems();

        return super.render();
    }
    public class NewHiddenItem extends AbstractItem {

        @Override
        public String disabled() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String enabled() {
            HtmlBuilder html = new HtmlBuilder();
            html.input().id("showMoreLink").type("hidden").name("showMoreLink").value(showMoreLink+"").end();
            return html.toString();
        }

    }
    
    protected void addToolbarItems() {

    }

}
