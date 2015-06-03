package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.control.DefaultToolbar;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.jmesa.core.CoreContext;
import org.jmesa.view.html.HtmlBuilder;
import org.jmesa.view.html.toolbar.AbstractItem;
import org.jmesa.view.html.toolbar.AbstractItemRenderer;
import org.jmesa.view.html.toolbar.ToolbarItem;
import org.jmesa.view.html.toolbar.ToolbarItemRenderer;
import org.jmesa.view.html.toolbar.ToolbarItemType;

import java.util.List;
import java.util.ResourceBundle;

public class ViewRuleAssignmentTableToolbar extends DefaultToolbar {

    List<Integer> ruleSetRuleIds;
    private final ResourceBundle reswords = ResourceBundleProvider.getWordsBundle();
    private final boolean isDesignerRequest;

    public ViewRuleAssignmentTableToolbar(List<Integer> ruleSetRuleIds, boolean showMoreLink, boolean isDesignerRequest) {
        super();
        this.ruleSetRuleIds = ruleSetRuleIds;
        this.showMoreLink = showMoreLink;
        this.isDesignerRequest = isDesignerRequest;
    }

    @Override
    protected void addToolbarItems() {
        // addToolbarItem(createCustomItem(new XmlExportItem(ruleSetRuleIds)));
        addToolbarItem(ToolbarItemType.SEPARATOR);
        addToolbarItem(createCustomItem(new ShowMoreItem()));
        if (!isDesignerRequest) {
            addToolbarItem(ToolbarItemType.SEPARATOR);
            addToolbarItem(createCustomItem(new TestRuleItem()));
        }
        addToolbarItem(createCustomItem(new NewHiddenItem()));

    }

    private ToolbarItem createCustomItem(AbstractItem item) {

        ToolbarItemRenderer renderer = new CustomItemRenderer(item, getCoreContext());
        renderer.setOnInvokeAction("onInvokeAction");
        item.setToolbarItemRenderer(renderer);

        return item;
    }

    private class TestRuleItem extends AbstractItem {
        @Override
        public String disabled() {
            return null;
        }

        @Override
        public String enabled() {
            HtmlBuilder html = new HtmlBuilder();
            html.a().href("TestRule");
            html.quote();
            html.append(getAction());
            html.quote().close();
            html.nbsp().append(reswords.getString("view_rules_test")).nbsp().aEnd();
            return html.toString();
        }
    }

    private class XmlExportItem extends AbstractItem {

        String theRuleSetRuleIds = "";

        public XmlExportItem(List<Integer> ruleSetRuleIds) {
            if (ruleSetRuleIds.size() > 0) {
                theRuleSetRuleIds += String.valueOf(ruleSetRuleIds.get(0));
                for (int i = 1; i < ruleSetRuleIds.size(); i++) {
                    theRuleSetRuleIds += "," + String.valueOf(ruleSetRuleIds.get(i));
                }

            }
        }

        @Override
        public String disabled() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String enabled() {

            HtmlBuilder actionLink = new HtmlBuilder();
            actionLink.a().href("DownloadRuleSetXml?ruleSetRuleIds=" + theRuleSetRuleIds);
            actionLink.append("onMouseDown=\"javascript:setImage('bt_Download','images/bt_Download_d.gif');\"");
            actionLink.append("onMouseUp=\"javascript:setImage('bt_Download','images/bt_Download.gif');\"").close();
            actionLink.img().name("Download").src("images/bt_Download.gif").border("0").alt("Download").title("Download").end().aEnd();
            actionLink.append("&nbsp;&nbsp;&nbsp;");
            return actionLink.toString();

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
            if (showMoreLink) {
                html.a().id("showMore").href("javascript:hideCols('ruleAssignments',[" + getIndexes() + "],true);").close();
                html.div().close().nbsp().append(reswords.getString("show_more")).nbsp().divEnd().aEnd();
                html.a().id("hide").style("display: none;").href("javascript:hideCols('ruleAssignments',[" + getIndexes() + "],false);").close();
                html.div().close().nbsp().append(reswords.getString("hide")).nbsp().divEnd().aEnd();

                html.script().type("text/javascript").close()
                        .append("$j = jQuery.noConflict(); $j(document).ready(function(){ " + "hideCols('ruleAssignments',[" + getIndexes() + "],false);" +
                            " if($j.browser.msie){" +
                            " $j('tr.header td div:last').each(function(){" +
                            " $j(this).click();" +
                            "var ah = $j(this).height();" +
                            "$j('tr.header td div').each(function(){"+
                            "$j(this).css('height',ah);"+
                            "});" + "});"+ "}"+ "});").scriptEnd();
            } else {
                html.a().id("hide").href("javascript:hideCols('ruleAssignments',[" + getIndexes() + "],false);").close();
                html.div().close().nbsp().append(reswords.getString("hide")).nbsp().divEnd().aEnd();
                html.a().id("showMore").style("display: none;").href("javascript:hideCols('ruleAssignments',[" + getIndexes() + "],true);").close();
                html.div().close().nbsp().append(reswords.getString("show_more")).nbsp().divEnd().aEnd();
            }
            return html.toString();
        }

        /**
         * @return Dynamically generate the indexes of studyGroupClasses. It starts from 4 because there are 4 columns before
         *         study group columns that will require to be hidden.
         * @see ListStudySubjectTableFactory#configureColumns(org.jmesa.facade.TableFacade, java.util.Locale)
         */
        String getIndexes() {
            String result = "0,1,2,3,5,6,10,11,13,15";
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
