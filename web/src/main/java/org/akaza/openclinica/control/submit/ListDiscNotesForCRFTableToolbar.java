package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.control.DefaultToolbar;
import org.jmesa.core.CoreContext;
import org.jmesa.view.html.HtmlBuilder;
import org.jmesa.view.html.toolbar.*;

import java.util.ArrayList;
import java.util.ResourceBundle;

public class ListDiscNotesForCRFTableToolbar extends DefaultToolbar {

    private final ArrayList<StudyEventDefinitionBean> studyEventDefinitions;
    private final StudyEventDefinitionBean selectedStudyEventDefinition;
    private String module;
    private int resolutionStatus;
    private int discNoteType;
    private boolean studyHasDiscNotes;
    private ResourceBundle resword;

    public ListDiscNotesForCRFTableToolbar(ArrayList<StudyEventDefinitionBean> studyEventDefinitions, StudyEventDefinitionBean selectedStudyEventDefinition) {
        super();
        this.studyEventDefinitions = studyEventDefinitions;
        this.selectedStudyEventDefinition = selectedStudyEventDefinition;

    }

    @Override
    protected void addToolbarItems() {
        addToolbarItem(ToolbarItemType.SEPARATOR);
        addToolbarItem(createCustomItem(new StudyEventDefinitionDropDownItem()));
        if (this.studyHasDiscNotes) {
            this.addToolbarItem(this.createCustomItem(new DownloadLinkItem()));
        }
    }

    private ToolbarItem createCustomItem(AbstractItem item) {

        ToolbarItemRenderer renderer = new CustomItemRenderer(item, getCoreContext());
        renderer.setOnInvokeAction("onInvokeAction");
        item.setToolbarItemRenderer(renderer);

        return item;
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

    private class StudyEventDefinitionDropDownItem extends AbstractItem {

        @Override
        public String disabled() {
            return null;
        }

        @Override
        public String enabled() {
            String js =
                "var selectedValue = document.getElementById('sedDropDown').options[document.getElementById('sedDropDown').selectedIndex].value;  "
                    + " if (selectedValue != null && selectedValue != 0 ) { "
                    + "window.location='ListDiscNotesForCRFServlet?module=submit&defId='+selectedValue;}"
                    + " if (selectedValue != null && selectedValue == 0 ) { " + "window.location='ListDiscNotesSubjectServlet?module=submit' } ";
            HtmlBuilder html = new HtmlBuilder();
            html.append(resword.getString("events")+": ");
            html.select().id("sedDropDown").onchange(js).close();
            html.option().value("0");
            html.close().append(resword.getString("all_events")).optionEnd();
            for (StudyEventDefinitionBean studyEventDefinition : studyEventDefinitions) {
                html.option().value(String.valueOf(studyEventDefinition.getId()));
                if (studyEventDefinition.getId() == selectedStudyEventDefinition.getId()) {
                    html.selected();
                }
                html.close().append(studyEventDefinition.getName()).optionEnd();
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
