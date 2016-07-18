package org.akaza.openclinica.controller.helper.table;

import org.akaza.openclinica.control.DefaultToolbar;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.jmesa.core.CoreContext;
import org.jmesa.view.html.HtmlBuilder;
import org.jmesa.view.html.toolbar.*;

import java.util.ResourceBundle;

public class SDVToolbarSubject extends DefaultToolbar {

    private ResourceBundle reswords = ResourceBundleProvider.getWordsBundle();

    public SDVToolbarSubject(boolean showMoreLink){
        this.showMoreLink = showMoreLink;
    }


    @Override
    protected void addToolbarItems() {
        addToolbarItem(ToolbarItemType.SEPARATOR);
        addToolbarItem(createCustomItem(new ShowMoreItem()));
        addToolbarItem(createCustomItem(new NewHiddenItem()));
    }

    private ToolbarItem createCustomItem(AbstractItem item) {

        ToolbarItemRenderer renderer = new CustomItemRenderer(item, getCoreContext());
        renderer.setOnInvokeAction("onInvokeAction");
        item.setToolbarItemRenderer(renderer);

        return item;
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

            if(showMoreLink){
            html.a().id("showMore").href("javascript:hideCols('s_sdv',[" + getIndexes() + "],true);").close();
            html.div().close().nbsp().append(reswords.getString("show_more")).nbsp().divEnd().aEnd();
            html.a().id("hide").style("display: none;").href("javascript:hideCols('s_sdv',[" + getIndexes() + "],false);").close();
            html.div().close().nbsp().append(reswords.getString("hide")).nbsp().divEnd().aEnd();


                html.script().type("text/javascript").close().append(
                        "$j = jQuery.noConflict(); $j(document).ready(function(){ " + "hideCols('s_sdv',[" + getIndexes() + "],false);});").scriptEnd();
            }else{
                html.a().id("hide").href("javascript:hideCols('s_sdv',[" + getIndexes() + "],false);").close();
                html.div().close().nbsp().append(reswords.getString("hide")).nbsp().divEnd().aEnd();
                html.a().id("showMore").style("display: none;").href("javascript:hideCols('s_sdv',[" + getIndexes() + "],true);").close();
                html.div().close().nbsp().append(reswords.getString("show_more")).nbsp().divEnd().aEnd();

                html.script().type("text/javascript").close().append(
                        "$j = jQuery.noConflict(); $j(document).ready(function(){ " + "hideCols('s_sdv',[" + getIndexes() + "],true);});").scriptEnd();

            }

            return html.toString();
        }

        String getIndexes() {
            String result = "2,3,4";
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
