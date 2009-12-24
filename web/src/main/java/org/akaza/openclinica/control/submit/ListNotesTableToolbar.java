package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudyGroupClassBean;
import org.akaza.openclinica.control.DefaultToolbar;
import org.jmesa.view.html.toolbar.*;
import org.jmesa.view.html.HtmlBuilder;
import org.jmesa.core.CoreContext;

import java.util.ArrayList;

public class ListNotesTableToolbar extends DefaultToolbar {

    public ListNotesTableToolbar() {
        super();
    }

    @Override
    protected void addToolbarItems() {
        addToolbarItem(ToolbarItemType.SEPARATOR);
        addToolbarItem(createCustomItem(new ShowMoreItem()));
        addToolbarItem(ToolbarItemType.SEPARATOR);
        addToolbarItem(createBackToNotesMatrixListItem());    

    }

    private ToolbarItem createCustomItem(AbstractItem item) {

        ToolbarItemRenderer renderer = new CustomItemRenderer(item, getCoreContext());
        renderer.setOnInvokeAction("onInvokeAction");
        item.setToolbarItemRenderer(renderer);

        return item;
    }

    public ToolbarItem createBackToNotesMatrixListItem() {
        ShowLinkToNotesMatrix item = new ShowLinkToNotesMatrix();
        item.setCode(ToolbarItemType.CLEAR_ITEM.toCode());
        ToolbarItemRenderer renderer = new ClearItemRenderer(item, getCoreContext());
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
            html.a().id("showMore").href("javascript:hideCols('listNotes',[" + getIndexes() + "],true);").close();
            html.div().close().nbsp().append("Show More").nbsp().divEnd().aEnd();
            html.a().id("hide").style("display: none;").href("javascript:hideCols('listNotes',[" + getIndexes() + "],false);").close();
            html.div().close().nbsp().append("Hide").nbsp().divEnd().aEnd();

            html.script().type("text/javascript").close().append(
                    "$j = jQuery.noConflict(); $j(document).ready(function(){ " + "hideCols('listNotes',[" + getIndexes() + "],false);});").scriptEnd();

            return html.toString();
        }

        /**
         * @return Dynamically generate the indexes of studyGroupClasses. It
         *         starts from 4 because there are 4 columns before study group
         *         columns that will require to be hidden.
         * @see ListStudySubjectTableFactory#configureColumns(org.jmesa.facade.TableFacade,
         *      java.util.Locale)
         */
        String getIndexes() {
            String result = "1,9,10,14,15";
            return result;
        }

    }
    private class ShowLinkToNotesMatrix extends AbstractItem {
        @Override
        public String disabled() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String enabled() {
            HtmlBuilder html = new HtmlBuilder();
            html.a().href("ListDiscNotesSubjectServlet?module=submit").id("backToNotesMatrix");
            html.quote();
            html.quote().close();
            html.nbsp().append("View as Matrix").nbsp().aEnd();

            return html.toString();
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
