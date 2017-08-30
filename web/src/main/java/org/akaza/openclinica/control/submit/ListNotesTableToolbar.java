package org.akaza.openclinica.control.submit;

import static java.util.Arrays.sort;

import java.util.Comparator;
import java.util.ResourceBundle;

import org.akaza.openclinica.control.DefaultToolbar;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.jmesa.core.CoreContext;
import org.jmesa.limit.Filter;
import org.jmesa.limit.FilterSet;
import org.jmesa.limit.Sort;
import org.jmesa.limit.SortSet;
import org.jmesa.view.html.HtmlBuilder;
import org.jmesa.view.html.toolbar.AbstractItem;
import org.jmesa.view.html.toolbar.AbstractItemRenderer;
import org.jmesa.view.html.toolbar.ClearItemRenderer;
import org.jmesa.view.html.toolbar.ToolbarItem;
import org.jmesa.view.html.toolbar.ToolbarItemRenderer;
import org.jmesa.view.html.toolbar.ToolbarItemType;

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

    private FilterSet filterSet;
    private SortSet sortSet;

    public void setFilterSet(FilterSet filterSet) {
		this.filterSet = filterSet;
	}
    public void setSortSet(SortSet sortSet) {
		this.sortSet = sortSet;
	}
    
    @Override
    protected void addToolbarItems() {
        addToolbarItem(ToolbarItemType.SEPARATOR);
        addToolbarItem(createCustomItem(new ShowMoreItem()));
        if (this.studyHasDiscNotes) {
            addToolbarItem(createDownloadLinkItem());
            addToolbarItem(createNotePopupLinkItem());
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

    public ToolbarItem createNotePopupLinkItem() {
        NotePopupLinkItem item = new NotePopupLinkItem();
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
                           html.a().id("showMore").style("text-decoration: none;").href("javascript:hideCols('listNotes',[" + getIndexes() + "],true);").close();
            html.div().close().nbsp().append(reswords.getString("show_more")).nbsp().divEnd().aEnd();
            html.a().id("hide").style("display: none;text-decoration:none;").href("javascript:hideCols('listNotes',[" + getIndexes() + "],false);").close();
            html.div().close().nbsp().append(reswords.getString("hide")).nbsp().divEnd().aEnd();

                html.script().type("text/javascript").close().append(
                        "$j = jQuery.noConflict(); $j(document).ready(function(){ " + "hideCols('listNotes',[" + getIndexes() + "],false);});").scriptEnd();
            }else{
                html.a().id("showMore").style("display:none;").style("text-decoration: none;").href("javascript:hideCols('listNotes',[" + getIndexes() + "],true);").close();
                html.div().close().nbsp().append(reswords.getString("show_more")).nbsp().divEnd().aEnd();
                html.a().id("hide").style("text-decoration: none;").href("javascript:hideCols('listNotes',[" + getIndexes() + "],false);").close();
                html.div().close().nbsp().append(reswords.getString("hide")).nbsp().divEnd().aEnd();

                html.script().type("text/javascript").close().append(
                        "$j = jQuery.noConflict(); $j(document).ready(function(){ " + "hideCols('listNotes',[" + getIndexes() + "],true);});").scriptEnd();
            }

            return html.toString();
        }

        /**
         * @return These indexes represent the 0-based column indexes on the Notes page.
         *         Columns in this list are hidden by default and can be revealed by clicking 
         *         a link in the table..
         *         
         * @see ListNotesTableFactory#configureColumns(org.jmesa.facade.TableFacade,
         *      java.util.Locale)
         */
        String getIndexes() {
            String result = "1, 4, 8, 10, 12, 16";
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
            String js = "javascript:openDocWindow('ChooseDownloadFormat" +
            		"?resolutionStatus=" + resolutionStatus
            		+ "&discNoteType=" + discNoteType
            		+ "&module=" + module;
            
            for (Filter f: filterSet.getFilters()) {
            	js += "&" + f.getProperty() + "=" + f.getValue();
            }

            Sort sorts[] = sortSet.getSorts().toArray(new Sort[0]);
            sort(sorts, new Comparator<Sort>() {
				@Override
				public int compare(Sort s1, Sort s2) {
					return s1.getPosition() - s2.getPosition();
				}
            });
            for (Sort s: sorts) {
            	js += "&" + "sort." + s.getProperty() + "=" + s.getOrder().name();
            }
            
            js += "')";

            html.a().href(js);
            html.quote();
            html.append(getAction());
            html.quote().close();
            html.append("<span title=\"Download queries for all subjects\" border=\"0\" align=\"left\" class=\"icon icon-download\" hspace=\"6\" width=\"24 \" height=\"15\"/>");
            return html.toString();
        }
    }

    private class NotePopupLinkItem extends AbstractItem {
        @Override
        public String disabled() {
            return null;
        }

        @Override
        public String enabled() {
            HtmlBuilder html = new HtmlBuilder();
            html.a().href("#");
            html.onclick("javascript:openPopup()");
            html.quote();
            html.append(getAction());
            html.quote().close();
            html.append("<span title=\"Print\" border=\"0\" align=\"left\" class=\"icon icon-print\" hspace=\"6\" width=\"24 \" height=\"15\"/>");
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
