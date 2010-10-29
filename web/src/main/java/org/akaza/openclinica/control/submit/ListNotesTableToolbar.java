package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudyGroupClassBean;
import org.akaza.openclinica.control.DefaultToolbar;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.jmesa.view.html.toolbar.*;
import org.jmesa.view.html.HtmlBuilder;
import org.jmesa.core.CoreContext;

import java.util.ArrayList;
import java.util.ResourceBundle;

public class ListNotesTableToolbar extends DefaultToolbar {
    private ResourceBundle reswords = ResourceBundleProvider.getWordsBundle();
    private String module;
    private int resolutionStatus;
    private int discNoteType;
    private boolean studyHasDiscNotes;
    private ResourceBundle resword;

    public ListNotesTableToolbar(boolean showMoreLink) {
        super();
        this.showMoreLink = showMoreLink;
    }

    @Override
    protected void addToolbarItems() {
        addToolbarItem(ToolbarItemType.SEPARATOR);
        addToolbarItem(createCustomItem(new ShowMoreItem()));
        if (this.studyHasDiscNotes) {
            addToolbarItem(createDownloadLinkItem());
        }
//        addToolbarItem(ToolbarItemType.SEPARATOR);
//        addToolbarItem(createBackToNotesMatrixListItem());
        addToolbarItem(createCustomItem(new NewHiddenItem()));

    }

    public ToolbarItem createDownloadLinkItem() {
        DownloadLinkItem item = new DownloadLinkItem();
        item.setCode(ToolbarItemType.CLEAR_ITEM.toCode());
        ToolbarItemRenderer renderer = new ClearItemRenderer(item, getCoreContext());
        renderer.setOnInvokeAction("onInvokeAction");
        item.setToolbarItemRenderer(renderer);
        return item;
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
            if(showMoreLink){
                           html.a().id("showMore").href("javascript:hideCols('listNotes',[" + getIndexes() + "],true);").close();
            html.div().close().nbsp().append(reswords.getString("show_more")).nbsp().divEnd().aEnd();
            html.a().id("hide").style("display: none;").href("javascript:hideCols('listNotes',[" + getIndexes() + "],false);").close();
            html.div().close().nbsp().append(reswords.getString("hide")).nbsp().divEnd().aEnd();

                html.script().type("text/javascript").close().append(
                        "$j = jQuery.noConflict(); $j(document).ready(function(){ " + "hideCols('listNotes',[" + getIndexes() + "],false);});").scriptEnd();
            }else{
                html.a().id("showMore").style("display:none;").href("javascript:hideCols('listNotes',[" + getIndexes() + "],true);").close();
                html.div().close().nbsp().append(reswords.getString("show_more")).nbsp().divEnd().aEnd();
                html.a().id("hide").href("javascript:hideCols('listNotes',[" + getIndexes() + "],false);").close();
                html.div().close().nbsp().append(reswords.getString("hide")).nbsp().divEnd().aEnd();

                html.script().type("text/javascript").close().append(
                        "$j = jQuery.noConflict(); $j(document).ready(function(){ " + "hideCols('listNotes',[" + getIndexes() + "],true);});").scriptEnd();
            }

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
            String result = "4, 5, 9, 13, 15, 16, 18";
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
            html.nbsp().append(reswords.getString("view_as_matrix")).nbsp().aEnd();

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

    private class DownloadLinkItem extends AbstractItem {
        @Override
        public String disabled() {
            return null;
        }

        @Override
        public String enabled() {
            HtmlBuilder html = new HtmlBuilder();
            html.a().href(
                    "javascript:openDocWindow('ChooseDownloadFormat?resolutionStatus=" + resolutionStatus + "&discNoteType=" + discNoteType + "&module="
                        + module + "')");
            html.quote();
            html.append(getAction());
            html.quote().close();
            html.img().name("bt_View1").src("images/bt_Download.gif").border("0").alt(resword.getString("download_all_discrepancy_notes")).title(
                    resword.getString("download_all_discrepancy_notes")).append("class=\"downloadAllDNotes\" width=\"24 \" height=\"15\"").end().aEnd();
            return html.toString();
        }
    }

    

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public int getResolutionStatus() {
        return resolutionStatus;
    }

    public void setResolutionStatus(int resolutionStatus) {
        this.resolutionStatus = resolutionStatus;
    }

    public int getDiscNoteType() {
        return discNoteType;
    }

    public void setDiscNoteType(int discNoteType) {
        this.discNoteType = discNoteType;
    }

    public boolean isStudyHasDiscNotes() {
        return studyHasDiscNotes;
    }

    public void setStudyHasDiscNotes(boolean studyHasDiscNotes) {
        this.studyHasDiscNotes = studyHasDiscNotes;
    }

    public ResourceBundle getResword() {
        return resword;
    }

    public void setResword(ResourceBundle resword) {
        this.resword = resword;
    }
}
