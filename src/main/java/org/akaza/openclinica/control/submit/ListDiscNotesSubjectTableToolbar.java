package org.akaza.openclinica.control.submit;

import java.util.ArrayList;
import java.util.ResourceBundle;

import core.org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.control.DefaultToolbar;
import org.jmesa.core.CoreContext;
import org.jmesa.view.html.HtmlBuilder;
import org.jmesa.view.html.toolbar.AbstractItem;
import org.jmesa.view.html.toolbar.AbstractItemRenderer;
import org.jmesa.view.html.toolbar.ClearItemRenderer;
import org.jmesa.view.html.toolbar.ToolbarItem;
import org.jmesa.view.html.toolbar.ToolbarItemRenderer;
import org.jmesa.view.html.toolbar.ToolbarItemType;

public class ListDiscNotesSubjectTableToolbar extends DefaultToolbar {

    private ArrayList<StudyEventDefinitionBean> studyEventDefinitions;
    private String module;
    private int resolutionStatus;
    private int discNoteType;
    private boolean studyHasDiscNotes;
    private ResourceBundle resword;
    private final String COMMON = "common";

    public ListDiscNotesSubjectTableToolbar(ArrayList<StudyEventDefinitionBean> studyEventDefinitions) {
        super();
        this.studyEventDefinitions = studyEventDefinitions;
    }

    @Override
    protected void addToolbarItems() {
        addToolbarItem(ToolbarItemType.SEPARATOR);
        addToolbarItem(createCustomItem(new StudyEventDefinitionDropDownItem()));

        if (this.studyHasDiscNotes) {
            addToolbarItem(createDownloadLinkItem());
        }
        addToolbarItem(createListNotesItem());
    }

    private ToolbarItem createCustomItem(AbstractItem item) {

        ToolbarItemRenderer renderer = new CustomItemRenderer(item, getCoreContext());
        renderer.setOnInvokeAction("onInvokeAction");
        item.setToolbarItemRenderer(renderer);

        return item;
    }

    public ToolbarItem createDownloadLinkItem() {
        DownloadLinkItem item = new DownloadLinkItem();
        item.setCode(ToolbarItemType.CLEAR_ITEM.toCode());
        ToolbarItemRenderer renderer = new ClearItemRenderer(item, getCoreContext());
        renderer.setOnInvokeAction("onInvokeAction");
        item.setToolbarItemRenderer(renderer);
        return item;
    }

    public ToolbarItem createListNotesItem() {
        ListNotesItem item = new ListNotesItem();
        item.setCode(ToolbarItemType.CLEAR_ITEM.toCode());
        ToolbarItemRenderer renderer = new ClearItemRenderer(item, getCoreContext());
        renderer.setOnInvokeAction("onInvokeAction");
        item.setToolbarItemRenderer(renderer);
        return item;
    }

    private class ListNotesItem extends AbstractItem {
        @Override
        public String disabled() {
            return null;
        }

        @Override
        public String enabled() {
            HtmlBuilder html = new HtmlBuilder();
            html.a().href("ViewNotes?module=" + module);
            html.quote();
            html.append(getAction());
            html.quote().close();
            html.nbsp().append(resword.getString("view_as_list")).nbsp().aEnd();
            return html.toString();
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
            html.a().href("javascript:openDocWindow('ChooseDownloadFormat?resolutionStatus=" + resolutionStatus + "&discNoteType=" + discNoteType + "&module="
                    + module + "')");
            html.quote();
            html.append(getAction());
            html.quote().close();
            html.img().name("bt_View1").src("images/bt_Download.gif").border("0").alt(resword.getString("download_all_discrepancy_notes"))
                    .title(resword.getString("download_all_discrepancy_notes")).append("class=\"downloadAllDNotes\" width=\"24 \" height=\"15\"").end().aEnd();
            return html.toString();
        }
    }

    private class StudyEventDefinitionDropDownItem extends AbstractItem {

        @Override
        public String disabled() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String enabled() {
            String js = "var selectedValue = document.getElementById('sedDropDown').options[document.getElementById('sedDropDown').selectedIndex].value;  "
                    + " if (selectedValue != null  ) { " + "window.location='ListDiscNotesForCRFServlet?module=submit&defId=' + selectedValue;" + " } ";
            HtmlBuilder html = new HtmlBuilder();
            html.select().id("sedDropDown").onchange(js).close();
            html.option().close().append(resword.getString("select_an_event")).optionEnd();
            ArrayList<StudyEventDefinitionBean> tempList = new ArrayList<>();
            studyEventDefinitions = new ArrayList(tempList);
            for (StudyEventDefinitionBean studyEventDefinition : studyEventDefinitions) {
                html.option().value(String.valueOf(studyEventDefinition.getId())).close().append(studyEventDefinition.getName()).optionEnd();
            }
            html.selectEnd();
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

    public boolean isStudyHasDiscNotes() {
        return studyHasDiscNotes;
    }

    public void setStudyHasDiscNotes(boolean studyHasDiscNotes) {
        this.studyHasDiscNotes = studyHasDiscNotes;
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

    public ResourceBundle getResword() {
        return resword;
    }

    public void setResword(ResourceBundle resword) {
        this.resword = resword;
    }
}
