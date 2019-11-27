package org.akaza.openclinica.control.submit;

import core.org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import core.org.akaza.openclinica.bean.managestudy.StudyGroupClassBean;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.control.DefaultToolbar;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import core.org.akaza.openclinica.service.PermissionService;
import org.akaza.openclinica.service.ViewStudySubjectService;
import org.jmesa.core.CoreContext;
import org.jmesa.view.html.HtmlBuilder;
import org.jmesa.view.html.toolbar.AbstractItem;
import org.jmesa.view.html.toolbar.AbstractItemRenderer;
import org.jmesa.view.html.toolbar.ClearItemRenderer;
import org.jmesa.view.html.toolbar.ToolbarItem;
import org.jmesa.view.html.toolbar.ToolbarItemRenderer;
import org.jmesa.view.html.toolbar.ToolbarItemType;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ListStudySubjectTableToolbar extends DefaultToolbar {

    private final ArrayList<StudyEventDefinitionBean> studyEventDefinitions;
    private final ArrayList<StudyGroupClassBean> studyGroupClasses;
    private final boolean addSubjectLinkShow;
    private ResourceBundle reswords = ResourceBundleProvider.getWordsBundle();
    private String participateModuleStatus;
    private final String ENABLED = "enabled";
    private ViewStudySubjectService viewStudySubjectService;
    private PermissionService permissionService;
    private Study studyBean;
    private HttpServletRequest request;

    public ListStudySubjectTableToolbar(ArrayList<StudyEventDefinitionBean> studyEventDefinitions, ArrayList<StudyGroupClassBean> studyGroupClasses,
                                        boolean addSubjectLinkShow, boolean showMoreLink , String participateModuleStatus, ViewStudySubjectService viewStudySubjectService, PermissionService permissionService, Study studyBean, HttpServletRequest request) {
        super();
        this.studyEventDefinitions = studyEventDefinitions;
        this.studyGroupClasses = studyGroupClasses;
        this.addSubjectLinkShow = addSubjectLinkShow;
        this.showMoreLink = showMoreLink;
        this.participateModuleStatus=participateModuleStatus;
        this.viewStudySubjectService=viewStudySubjectService;
        this.permissionService=permissionService;
        this.studyBean=studyBean;
        this.request=request;
    }

    @Override
    protected void addToolbarItems() {
        addToolbarItem(ToolbarItemType.SEPARATOR);
        addToolbarItem(createCustomItem(new ShowMoreItem()));

        addToolbarItem(ToolbarItemType.SEPARATOR);
        addToolbarItem(createCustomItem(new StudyEventDefinitionDropDownItem()));
        addToolbarItem(createCustomItem(new NewHiddenItem()));
        if (addSubjectLinkShow) {
            addToolbarItem(createAddSubjectItem());
        }
    }

    private ToolbarItem createCustomItem(AbstractItem item) {

        ToolbarItemRenderer renderer = new CustomItemRenderer(item, getCoreContext());
        renderer.setOnInvokeAction("onInvokeAction");
        item.setToolbarItemRenderer(renderer);

        return item;
    }

    public ToolbarItem createAddSubjectItem() {

        AddNewSubjectItem item = new AddNewSubjectItem();
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
                          html.a().id("showMore").style("text-decoration: none").href("javascript:hideCols('findSubjects',[" + getIndexes() + "],true);").close();
            html.div().close().nbsp().append(reswords.getString("show_more")).nbsp().divEnd().aEnd();
            html.a().id("hide").style("display: none;text-decoration: none;").href("javascript:hideCols('findSubjects',[" + getIndexes() + "],false);").close();
            html.div().close().nbsp().append(reswords.getString("hide")).nbsp().divEnd().aEnd();

                html.script().type("text/javascript").close().append(
                        "$j = jQuery.noConflict(); $j(document).ready(function(){ " + "hideCols('findSubjects',[" + getIndexes() + "],false);});").scriptEnd();
            }else{
                html.a().id("hide").style("text-decoration: none").href("javascript:hideCols('findSubjects',[" + getIndexes() + "],false);").close();
                html.div().close().nbsp().append(reswords.getString("hide")).nbsp().divEnd().aEnd();
                html.a().id("showMore").style("display: none;").style("text-decoration: none;").href("javascript:hideCols('findSubjects',[" + getIndexes() + "],true);").close();
                html.div().close().nbsp().append(reswords.getString("show_more")).nbsp().divEnd().aEnd();
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

            int itemsColumnCount=0;

            String[] tableColumns = viewStudySubjectService.getTableColumns(ListStudySubjectTableFactory.PAGE_NAME,ListStudySubjectTableFactory.COMPONENT_NAME);
            if (tableColumns != null) {
                for (String column : tableColumns) {
                    if (permissionService.isUserHasPermission(column, request, studyBean)) {
                        itemsColumnCount++;
                    }
                }
            }

            String result = String.valueOf(1) + "," + String.valueOf(itemsColumnCount+2)+ "," + String.valueOf(itemsColumnCount+3);
            if (participateModuleStatus.equals(ENABLED))
                result = result + "," + String.valueOf(itemsColumnCount+4);

            return result;
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
            String js =
                "var selectedValue = document.getElementById('sedDropDown').options[document.getElementById('sedDropDown').selectedIndex].value; "
                    + " var maxrows = jQuery('select[name=maxRows]').val();"
                    + " if (selectedValue != null  ) { " + "window.location='ListEventsForSubjects?module=submit&defId=' + selectedValue + '&listEventsForSubject_mr_=' + maxrows;" + " } ";
            HtmlBuilder html = new HtmlBuilder();
            html.select().id("sedDropDown").onchange(js).close();
            html.option().close().append(reswords.getString("select_an_event")).optionEnd();
            for (StudyEventDefinitionBean studyEventDefinition : studyEventDefinitions) {
                html.option().value(String.valueOf(studyEventDefinition.getId())).close().append(studyEventDefinition.getName()).optionEnd();
            }
            html.selectEnd();
            return html.toString();
        }

    }

    private class AddNewSubjectItem extends AbstractItem {

        @Override
        public String disabled() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String enabled() {
            HtmlBuilder html = new HtmlBuilder();
            //html.a().href("AddNewSubject");
            html.a().style("text-decoration: none").href("javascript:;").id("addSubject");
            html.quote();
            html.quote().close();
            html.nbsp().append(reswords.getString("add_new_subject")).nbsp().aEnd();
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
