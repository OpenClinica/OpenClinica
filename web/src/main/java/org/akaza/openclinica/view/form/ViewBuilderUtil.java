package org.akaza.openclinica.view.form;

import org.akaza.openclinica.bean.submit.DisplayItemBean;
import org.akaza.openclinica.bean.submit.DisplayItemGroupBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

/**
 * A utility class containing various methods that are used for generating Web
 * form views.
 *
 */
public class ViewBuilderUtil {

    /**
     * For the purpose of generating a certain HTML view, this method finds out
     * whether any horizontally displayed checkboxes or radio buttons are
     * involved.
     *
     * @param displayBeans
     *            A List of DisplayItemBeans for a CRF Section
     * @return true if any of the DisplayItemBeans have a horizontal checkbox or
     *         radio button
     */
    public boolean hasResponseLayout(List<DisplayItemBean> displayBeans) {
        String responseName;
        for (DisplayItemBean dBean : displayBeans) {
            responseName = dBean.getMetadata().getResponseSet().getResponseType().getName();
            if (responseName == null) {
                responseName = "";
            }
            if (dBean.getMetadata().getResponseLayout().equalsIgnoreCase("horizontal")
              && (responseName.equalsIgnoreCase("radio") || responseName.equalsIgnoreCase("checkbox"))) {
                return true;
            }
        }
        return false;
    }

    public int calcNumberofColumns(DisplayItemGroupBean displayGroup) {
        if (displayGroup == null || displayGroup.getItems().size() == 0) {
            return 0;
        }
        int columns = 0;
        String responseName;
        String responseLayout;
        for (DisplayItemBean disBean : displayGroup.getItems()) {
            responseName = disBean.getMetadata().getResponseSet().getResponseType().getName();
            responseLayout = disBean.getMetadata().getResponseLayout();
            if ((responseName.equalsIgnoreCase("radio") || responseName.equalsIgnoreCase("checkbox")) && responseLayout.equalsIgnoreCase("horizontal")) {
                columns += disBean.getMetadata().getResponseSet().getOptions().size();
            } else {
                columns++;
            }

        }
        return columns;
    }

    public void addRemoveRowControl(Element row, String repeatParentId,
                                    boolean hasDarkBorders) {
        Element repeatCell = new Element("td");
        if(hasDarkBorders){
            String cssClasses = CssRules.getClassNamesForTag("td borders_on");
            repeatCell.setAttribute("class",cssClasses);
        }  else {
            repeatCell = this.setClassNames(repeatCell);
        }
        repeatCell.addContent(RepeatManager.createrepeatButtonControl(ResourceBundleProvider.getResWord("remove"), repeatParentId));
        row.addContent(repeatCell);

    }

    public void createAddRowControl(Element tbody, String repeatParentId, int columnNumber, boolean hasDarkBorders) {
        Element addButtonRow = new Element("tr");
        Element addButtonCell = new Element("td");
        if(hasDarkBorders){
            String cssClasses = CssRules.getClassNamesForTag("td borders_on");
            addButtonCell.setAttribute("class",cssClasses);
           
        }  else {
            addButtonCell = this.setClassNames(addButtonCell);
        }
        addButtonCell.setAttribute("style","display:block;");
        addButtonCell.setAttribute("colspan", columnNumber + "");
        addButtonCell.addContent(RepeatManager.createrepeatButtonControl(ResourceBundleProvider.getResWord("add"), repeatParentId));
        addButtonRow.addContent(addButtonCell);
        tbody.addContent(addButtonRow);
    }

    public Element setClassNames(Element styledElement) {
        String cssClasses = CssRules.getClassNamesForTag(styledElement.getName());
        return cssClasses.length() == 0 ? styledElement : styledElement.setAttribute("class", cssClasses);
    }

    public void showTitles(Element divRoot, SectionBean sectionBean) {
        // Don't create an Element if the Section does not have
        // a title, subtitle, or instructions
        if (divRoot == null || sectionBean.getInstructions().length() == 0 && sectionBean.getTitle().length() == 0 && sectionBean.getSubtitle().length() == 0) {
            return;
        }

        Element table = new Element("table");
        table.setAttribute("class", CssRules.getClassNamesForTag("table section"));
        table.setAttribute("width", "100%");
        divRoot.addContent(table);

        if (sectionBean.getTitle().length() > 0) {
            addTitles(sectionBean, table, "Title");
        }
        if (sectionBean.getSubtitle().length() > 0) {
            addTitles(sectionBean, table, "Subtitle");
        }
        if (sectionBean.getInstructions().length() > 0) {
            addTitles(sectionBean, table, "Instructions");
        }
    }

    public void addTitles(SectionBean sbean, Element table, String content) {
        Element tr = new Element("tr");
        tr.setAttribute("class", "aka_stripes");
        table.addContent(tr);
        Element title = new Element("td");
        title.setAttribute("nowrap", "nowrap");
        title.setAttribute("class", "aka_table_cell_left");
        Element bold = new Element("b");
        bold.addContent(content + ":  ");
        title.addContent(bold);
        if (content.equalsIgnoreCase("subtitle")) {
            title.addContent(sbean.getSubtitle());
        } else if (content.equalsIgnoreCase("title")) {
            title.addContent(sbean.getTitle());
        } else if (content.equalsIgnoreCase("instructions")) {
            title.addContent(sbean.getInstructions());
        }
        tr.addContent(title);
    }

    /**
     * Generate a List of HTML rows (tr tag) for a matrix type table that
     * already has form-field values from the database. In other words, the user
     * previously created repeating rows and filled them with some data.
     *
     * @param sortedDataMap
     *            A SortedMap where the indiex is the Item Data's ordinal (like
     *            "2"), and the value is a List of ItemDataBeans associated with
     *            that ordinal. These data make up the row's content.
     * @param rowContentBeans
     *            The DisplayItemBeans that provide the HTML fields for the
     *            rows.
     * @param tabIndex
     *            The incremented tab index for the form section.
     * @param repeatParentId
     *            The String id for the input name fields. See the RepeatManager
     *            class.
     * @param hasDiscrepancyMgt
     *            A boolean indicating whether the discrepancy note icon should
     *            be displayed.
     * @param forPrinting
     *            A boolean value indicating whether CRF printing is involved
     *            (if true, then discrepancy note icons are not clickable).
     * @param hasDarkBorders
     * @return A List of row Elements, as in new Element("tr"); containing the
     *         previously generated repeating rows.
     */
    public List<Element> generatePersistentMatrixRows(SortedMap<Integer, List<ItemDataBean>> sortedDataMap, List<DisplayItemBean> rowContentBeans,
                                                      int tabIndex, String repeatParentId,
                                                      boolean hasDiscrepancyMgt, boolean forPrinting,
                                                      boolean hasDarkBorders) {

        List<Element> newRows = new ArrayList<Element>();
        List<ItemDataBean> tempList;
        Element tr;
        Element td;
        CellFactory cellFactory = new CellFactory();
        RepeatManager repeatManager = new RepeatManager();
        String responseName;
        boolean repeatFlag = true; // for now...
        String forcedParentId = "";
        // for each repeated row of the matrix table..
        for (Integer ordinal : sortedDataMap.keySet()) {
            tr = new Element("tr");
            tempList = sortedDataMap.get(ordinal);
            forcedParentId = ordinal - 1 + "";

            for (DisplayItemBean disItemBean : rowContentBeans) {
                for (ItemDataBean itemDBean : tempList) {
                    if (disItemBean.getItem().getId() == itemDBean.getItemId()) {
                        disItemBean.setData(itemDBean);
                        break;
                    }
                }
                responseName = disItemBean.getMetadata().getResponseSet().getResponseType().getName();
                // start horiz
                if (disItemBean.getMetadata().getResponseLayout().equalsIgnoreCase("horizontal")
                  && (responseName.equalsIgnoreCase("checkbox") || responseName.equalsIgnoreCase("radio"))) {
                    // The final true parameter styfles the display of default
                    // values in CRFs (because
                    // default values do not appear in forms that have db values
                    Element[] elements =
                      cellFactory.createCellContentsForChecks(responseName, disItemBean, disItemBean.getMetadata().getResponseSet().getOptions().size(),
                        ++tabIndex, true, forPrinting);
                    for (Element el : elements) {
                        if(hasDarkBorders){
                            String cssClasses = CssRules.getClassNamesForTag("td borders_on");
                            el.setAttribute("class",cssClasses);
                        }   else {
                            el = this.setClassNames(el);
                        }
                        if (repeatFlag) {
                            el = repeatManager.addChildRepeatAttributes(el, repeatParentId, disItemBean.getItem().getId(), forcedParentId);
                        }
                        tr.addContent(el);
                    }
                    // move to the next item
                    continue;
                }
                // end
                td = new Element("td");
                if(hasDarkBorders){
                    String cssClasses = CssRules.getClassNamesForTag("td borders_on");
                    td.setAttribute("class",cssClasses);
                }   else {
                    td = this.setClassNames(td);
                }
                td = cellFactory.createCellContents(td, responseName, disItemBean, ++tabIndex, hasDiscrepancyMgt, true, forPrinting);
                // In this case, the parent id looks like parentId_1, etc.
                td = repeatManager.addChildRepeatAttributes(td, repeatParentId, disItemBean.getItem().getId(), forcedParentId);
                tr.addContent(td);
            }
            this.addRemoveRowControl(tr, repeatParentId, hasDarkBorders);
            newRows.add(tr);
        }

        return newRows;
    }


    public List generatePersistentMatrixRowsNew(SortedMap<Integer, List<ItemDataBean>> sortedDataMap, List<DisplayItemBean> rowContentBeans,
                                                      int tabIndex, String repeatParentId,
                                                      boolean hasDiscrepancyMgt, boolean forPrinting,
                                                      boolean hasDarkBorders, int maxColRows) {

        List newRows = new ArrayList();
        List<ItemDataBean> tempList;
        Element tr;
        Element td;
        CellFactory cellFactory = new CellFactory();
        RepeatManager repeatManager = new RepeatManager();
        String responseName;
        boolean repeatFlag = true; // for now...
        String forcedParentId = "";
        // for each repeated row of the matrix table..
        for (Integer ordinal : sortedDataMap.keySet()) {
            List<Element> rowList = new ArrayList();
            tr = new Element("tr");
            tempList = sortedDataMap.get(ordinal);
            forcedParentId = ordinal - 1 + "";
            int count = 0;
            for (DisplayItemBean disItemBean : rowContentBeans) {
                count++;
                for (ItemDataBean itemDBean : tempList) {
                    if (disItemBean.getItem().getId() == itemDBean.getItemId()) {
                        disItemBean.setData(itemDBean);
                        break;
                    }
                }
                responseName = disItemBean.getMetadata().getResponseSet().getResponseType().getName();
                if ((responseName.equalsIgnoreCase("radio")
                        || responseName.equalsIgnoreCase("multi-select")
                        || responseName.equalsIgnoreCase("single-select")
                        || responseName.equalsIgnoreCase("checkbox")) && forPrinting) {
                    responseName = "checkbox";                    
                }

                // start horiz
                if (disItemBean.getMetadata().getResponseLayout().equalsIgnoreCase("horizontal")
                  && (responseName.equalsIgnoreCase("checkbox") || responseName.equalsIgnoreCase("radio"))) {
                    // The final true parameter styfles the display of default
                    // values in CRFs (because
                    // default values do not appear in forms that have db values
                    Element[] elements =
                      cellFactory.createCellContentsForChecks(responseName, disItemBean, disItemBean.getMetadata().getResponseSet().getOptions().size(),
                        ++tabIndex, true, forPrinting);
                    for (Element el : elements) {
                        if(hasDarkBorders){
                            String cssClasses = CssRules.getClassNamesForTag("td borders_on");
                            el.setAttribute("class",cssClasses);
                        }   else {
                            el = this.setClassNames(el);
                        }
                        if (repeatFlag) {
                            el = repeatManager.addChildRepeatAttributes(el, repeatParentId, disItemBean.getItem().getId(), forcedParentId);
                        }
                        tr.addContent(el);
                        if(count%maxColRows==0){
                            rowList.add(tr);
                            tr = new Element("tr");
                        }
                    }
                    // move to the next item
                    continue;
                }
                // end
                td = new Element("td");
                if(hasDarkBorders){
                    String cssClasses = CssRules.getClassNamesForTag("td borders_on");
                    td.setAttribute("class",cssClasses);
                }   else {
                    td = this.setClassNames(td);
                }
                td = cellFactory.createCellContents(td, responseName, disItemBean, ++tabIndex, hasDiscrepancyMgt, true, forPrinting);
                // In this case, the parent id looks like parentId_1, etc.
                td = repeatManager.addChildRepeatAttributes(td, repeatParentId, disItemBean.getItem().getId(), forcedParentId);
                tr.addContent(td);
                if(count%maxColRows==0){
                    rowList.add(tr);
                    tr = new Element("tr");
                }
            }
            if(count%maxColRows!=0)rowList.add(tr);
            newRows.add(rowList);
        }

        return newRows;
    }
    
}
