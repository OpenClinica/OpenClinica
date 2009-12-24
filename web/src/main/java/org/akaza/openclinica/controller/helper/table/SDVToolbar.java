package org.akaza.openclinica.controller.helper.table;

import org.akaza.openclinica.control.DefaultToolbar;
import org.jmesa.core.CoreContext;
import org.jmesa.view.html.HtmlBuilder;
import org.jmesa.view.html.toolbar.AbstractItem;
import org.jmesa.view.html.toolbar.AbstractItemRenderer;
import org.jmesa.view.html.toolbar.ToolbarItem;
import org.jmesa.view.html.toolbar.ToolbarItemRenderer;
import org.jmesa.view.html.toolbar.ToolbarItemType;

public class SDVToolbar extends DefaultToolbar {

    @Override
    protected void addToolbarItems() {
        addToolbarItem(ToolbarItemType.SEPARATOR);
        addToolbarItem(createCustomItem(new ShowMoreItem()));
        addToolbarItem(ToolbarItemType.SEPARATOR);
        addToolbarItem(createCustomItem(new InfoItem()));
    }

    private ToolbarItem createCustomItem(AbstractItem item) {

        ToolbarItemRenderer renderer = new CustomItemRenderer(item, getCoreContext());
        renderer.setOnInvokeAction("onInvokeAction");
        item.setToolbarItemRenderer(renderer);

        return item;
    }

    private class InfoItem extends AbstractItem {

        @Override
        public String disabled() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String enabled() {
            HtmlBuilder html = new HtmlBuilder();
            html.nbsp().append(" The table is sorted by Event Date");

            return html.toString();
        }
    }

    private class ShowMoreItem extends AbstractItem {

        @Override
        public String disabled() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String enabled() {
            HtmlBuilder html = new HtmlBuilder();
            html.a().id("showMore").href("javascript:hideCols('sdv',[" + getIndexes() + "],true);").close();
            html.div().close().nbsp().append("Show More").nbsp().divEnd().aEnd();
            html.a().id("hide").style("display: none;").href("javascript:hideCols('sdv',[" + getIndexes() + "],false);").close();
            html.div().close().nbsp().append("Hide").nbsp().divEnd().aEnd();

            html.script().type("text/javascript").close().append(
                    "$j = jQuery.noConflict(); $j(document).ready(function(){ " + "hideCols('sdv',[" + getIndexes() + "],false);});").scriptEnd();

            return html.toString();
        }

        String getIndexes() {
            String result = "3,4,7,8,12,13,14";
            return result;
        }

    }

    private static class CustomItemRenderer extends AbstractItemRenderer {
        public CustomItemRenderer(ToolbarItem item, CoreContext coreContext) {
            setToolbarItem(item);
            setCoreContext(coreContext);
        }

        public String render() {
            ToolbarItem item = getToolbarItem();
            return item.enabled();
        }
    }
}
