package org.akaza.openclinica.view.form;

import org.akaza.openclinica.bean.submit.DisplayItemBean;
import org.akaza.openclinica.bean.submit.DisplayItemGroupBean;
import org.akaza.openclinica.bean.submit.DisplaySectionBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.control.managestudy.BeanFactory;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;

/**
 * This is a Decorator design pattern for the ViewBuilderUtil Class. It adds
 * printing related functionality to that class.
 */
public class ViewBuilderPrintDecorator {
    private final ViewBuilderUtil viewBuilderUtil;

    public ViewBuilderPrintDecorator() {
        this.viewBuilderUtil = new ViewBuilderUtil();
    }

    /**
     * Create the div element for showing titles, page number, and instructions
     *
     * @param divRoot
     *            The root div element that will contain this new div element.
     * @param sectionBean
     *            The SectionBean that encompasses the CRF title and
     *            instructions.
     * @param pageNumber
     *            The page number for the printed form.
     * @param changeForInternetExplorer
     *            A boolean value indicating that Internet Explorer is the
     *            browser. In this case, the div elements for titles should not
     *            have float properties set.
     */
    public void showTitles(Element divRoot, SectionBean sectionBean, int pageNumber, boolean changeForInternetExplorer) {
        /*
         * <div id="section_page" style="float:left;border-bottom:thin solid
         * gray"> <div id="section" style="float:left;">SECTION: </div> <div
         * id="page" style="float:right;">Page: 1 </div> <div id="instructions"
         * style="float:left;clear:both">Instructions: </div>
         *
         * </div>
         */
        Element titleDiv = new Element("div");
        titleDiv.setAttribute("id", ("section_page" + pageNumber));
        titleDiv.setAttribute("class", "section_page");
        if (changeForInternetExplorer) {
            titleDiv.setAttribute("style", "float:none");
        }
        divRoot.addContent(titleDiv);
        Element secDiv = new Element("div");
        secDiv.setAttribute("id", ("section" + pageNumber));
        secDiv.setAttribute("class", "section");
        if (changeForInternetExplorer) {
            secDiv.setAttribute("style", "float:none");
        }
        // Section: should be in bold
        Element strong = new Element("strong");
        strong.addContent("Section: ");
        secDiv.addContent(strong);
        secDiv.addContent(sectionBean.getTitle());
        titleDiv.addContent(secDiv);

        Element pageDiv = new Element("div");
        pageDiv.setAttribute("id", ("page" + pageNumber));
//        pageDiv.setAttribute("class", "page");
        if (changeForInternetExplorer) {
            pageDiv.setAttribute("style", "float:none");
        }
        Element strong2 = new Element("strong");
        strong2.addContent("Page: ");
        pageDiv.addContent(strong2);
        pageDiv.addContent("" + pageNumber);
        titleDiv.addContent(pageDiv);

        // Instructions for the section
        Element instructDiv = new Element("div");
        instructDiv.setAttribute("id", ("instructions" + pageNumber));
        instructDiv.setAttribute("class", "instructions");

        if (changeForInternetExplorer) {
            instructDiv.setAttribute("style", "float:none");
        }
        Element strong3 = new Element("strong");
        strong3.addContent("Instructions: ");
        instructDiv.addContent(strong3);
        instructDiv.addContent(sectionBean.getInstructions());
        titleDiv.addContent(instructDiv);

    }

    public boolean hasResponseLayout(List<DisplayItemBean> displayBeans) {
        if (displayBeans == null || displayBeans.isEmpty()) {
            return false;
        }
        synchronized (this.viewBuilderUtil) {
            return viewBuilderUtil.hasResponseLayout(displayBeans);
        }

    }

    public Element setClassNames(Element styledElement) {

        synchronized (this.viewBuilderUtil) {
            return viewBuilderUtil.setClassNames(styledElement);
        }
    }

    public void addRemoveRowControl(Element row, String repeatParentId) {

        synchronized (this.viewBuilderUtil) {
            viewBuilderUtil.addRemoveRowControl(row, repeatParentId, false);
        }
    }

    public void createAddRowControl(Element tbody, String repeatParentId, int columnNumber) {
        synchronized (this.viewBuilderUtil) {
            viewBuilderUtil.createAddRowControl(tbody, repeatParentId, columnNumber, false);
        }
    }

    public List generatePersistentMatrixRows(SortedMap<Integer, List<ItemDataBean>> sortedDataMap, List<DisplayItemBean> rowContentBeans,
            int tabIndex, String repeatParentId, boolean hasDiscrepancyMgt, boolean forPrinting, int maxColRows) {

        synchronized (this.viewBuilderUtil) {
            return viewBuilderUtil.generatePersistentMatrixRowsNew(sortedDataMap, rowContentBeans, tabIndex, repeatParentId, hasDiscrepancyMgt, forPrinting, false, maxColRows);
        }

    }

    public int calcNumberofColumns(DisplayItemGroupBean displayGroup) {
        synchronized (this.viewBuilderUtil) {
            return viewBuilderUtil.calcNumberofColumns(displayGroup);
        }
    }

    /**
     * This method determines whether a CRF section contains a group-type table
     * with three or more columns, for the purpose of printing CRFs.
     *
     * @param displaySectionBean
     *            The DisplaySectionBean representing the section.
     * @return true if the sections contains a group-type table that has three
     *         or more columns.
     */
    public boolean hasThreePlusColumns(DisplaySectionBean displaySectionBean) {

        if (displaySectionBean == null)
            return false;

        List<DisplayItemBean> currentDisplayItems = null;

        for (DisplayItemGroupBean displayItemGroup : displaySectionBean.getDisplayFormGroups()) {

            // Ignore forms that are not group-type tables
            if (displayItemGroup.getItemGroupBean().getName().equalsIgnoreCase(BeanFactory.UNGROUPED)) {
                continue;
            }
            currentDisplayItems = displayItemGroup.getItems();
            // If the group table's items size is greater than 3, then the table
            // has a
            // group table that exceeds three columns
            if (currentDisplayItems.size() > 3) {
                return true;
            }
        }

        return false;
    }

    /**
     *
     * @param groupBeans
     * @return
     */
    public List<DisplayItemGroupBean> reduceColumnsGroupTables(List<DisplayItemGroupBean> groupBeans) {

        if (groupBeans == null || groupBeans.size() == 0)
            return groupBeans;

        List<DisplayItemGroupBean> newGroupBeans = new ArrayList<DisplayItemGroupBean>();

        // the ordinal of the first group table that is being broken up into
        // multiple columns
        // we then have to change the ordinals of all of the group beans that
        // follow this one.
        // See below
        int startOrdinal = 0;
        int i = 0;
        for (DisplayItemGroupBean existingDisplayBean : groupBeans) {
            // If the group table has more than three columns and does not have
            // a name of "ungrouped" then break the bean up into single column
            // beans
            if (existingDisplayBean.getItems().size() > 3 && !BeanFactory.UNGROUPED.equalsIgnoreCase(existingDisplayBean.getItemGroupBean().getName())) {
                
                startOrdinal = existingDisplayBean.getOrdinal();
                // increment the ordinals of the other beans to make up for this
                // one
//                this.incrementDisplayBeanOrdinals(groupBeans, startOrdinal, existingDisplayBean.getItems().size() - 1);

                newGroupBeans.addAll(splitUpGroupBeanIntoSingleColumns(existingDisplayBean));
            } else {
                // otherwise, add the existing bean to the List
                newGroupBeans.add(existingDisplayBean);
            }
        }

        // Now sort the new beans on their ordinal
        Collections.sort(newGroupBeans);
        return newGroupBeans;
    }

    /**
     * This method takes a List of DisplayItemGroupBeans, and increases the
     * ordinals of some of the beans, to compensate for the fact that previous
     * DisplayItemGroupBeans in the List have been split up into single-column
     * beans with new ordinals. Therefore, the DisplayItemGroupBeans following
     * them on the section have to have their ordinals (specifying their
     * position on the CRF section) incremented, so that they keep their proper
     * position on the CRF.
     *
     * @param displayItemGroupBeans
     *            A List of DisplayItemGroupBeans, representing group tables on
     *            a CRF section.
     * @param startOrdinal
     *            The ordinal of the DisplayItemGroupBean that is split up into
     *            multiple DisplayItemGroupBeans.
     * @param incrementByNumber
     *            The number by which the following DisplayItemGroupBeans have
     *            to increment their ordinals. This number is equal to split-up
     *            DisplayItemGroupBean.getItems().size() - 1 .
     */
    public void incrementDisplayBeanOrdinals(List<DisplayItemGroupBean> displayItemGroupBeans, int startOrdinal, int incrementByNumber) {
        int tempOrdinal = 0;
        for (DisplayItemGroupBean displayItemGroupBean : displayItemGroupBeans) {

            tempOrdinal = displayItemGroupBean.getOrdinal();
            if (tempOrdinal <= startOrdinal)
                continue;

            displayItemGroupBean.setOrdinal(displayItemGroupBean.getOrdinal() + incrementByNumber);

        }

    }

    public List<DisplayItemGroupBean> splitUpGroupBeanIntoSingleColumns(DisplayItemGroupBean existingBean) {

        List<DisplayItemGroupBean> newDisplayBeans = new ArrayList<DisplayItemGroupBean>();
        int ordinal = existingBean.getOrdinal();
        DisplayItemGroupBean cloneDisplayBean = cloneDisplayItemGroupBean(existingBean, existingBean.getItems().get(0), ordinal);

        if (existingBean == null) {
            return newDisplayBeans;
        }
        // Create a DisplayItemGroupBean for every display item bean
        for (int i=1; i< existingBean.getItems().size(); i++) {
            DisplayItemBean displayItemBean = existingBean.getItems().get(i);
            if(i%3==0){
                ordinal++;
                newDisplayBeans.add(cloneDisplayBean);
                cloneDisplayBean = cloneDisplayItemGroupBean(existingBean, displayItemBean, ordinal);
            }else{
                cloneDisplayBean.getItems().add(displayItemBean);
            }
        }
        newDisplayBeans.add(cloneDisplayBean);
        return newDisplayBeans;
    }

    public DisplayItemGroupBean cloneDisplayItemGroupBean(DisplayItemGroupBean clonedGroupBean, DisplayItemBean displayItemBean,int ordinal) {

        DisplayItemGroupBean newGroupBean = new DisplayItemGroupBean();
        if (clonedGroupBean == null)
            return newGroupBean;

        newGroupBean.setGroupMetaBean(clonedGroupBean.getGroupMetaBean());
        newGroupBean.setItemGroupBean(clonedGroupBean.getItemGroupBean());
        newGroupBean.getItems().add(displayItemBean);
        newGroupBean.setOrdinal(ordinal);

        return newGroupBean;
    }

}
