package org.akaza.openclinica.view.form;

import org.akaza.openclinica.bean.core.NullValue;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.*;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.core.SessionManager;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.submit.*;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.service.crfdata.DynamicsMetadataService;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.sql.DataSource;
import java.util.*;

/**
 * This class builds DisplayFormGroupBeans and DisplayItemBeans in preparation
 * for displaying a form. The DisplayFormGroupBean contains the
 * DisplayItemBeans, and is itself contained by a DisplaySectionBean.
 */
public class FormBeanUtil {

    private static Logger logger = LoggerFactory.getLogger(FormBeanUtil.class);
    public static final String UNGROUPED = "Ungrouped";
    public static final String ADMIN_EDIT = "administrativeEditing";
    private static DynamicsMetadataService itemMetadataService;
    private ServletContext context;

    private static DynamicsMetadataService getItemMetadataService(ServletContext context) {
        itemMetadataService =
            itemMetadataService != null ? itemMetadataService : (DynamicsMetadataService) SpringServletAccess.getApplicationContext(context).getBean(
                    "dynamicsMetadataService");
        return itemMetadataService;
    }

    public static ItemFormMetadataBean runDynamicsCheck(ItemFormMetadataBean metadataBean, EventCRFBean eventCrfBean, ItemDataBean itemDataBean,
            ServletContext context) {
        if (!metadataBean.isShowItem()) {

            // if the base case is not already shown, let's check it
            boolean showItem = getItemMetadataService(context).isShown(new Integer(metadataBean.getItemId()), eventCrfBean, itemDataBean);
            metadataBean.setShowItem(showItem);
           // System.out.println("running is shown to the db..." + showItem + " for " + metadataBean.getItemId());
            // setting true or false here, tbh
        }
        // however, we run into a puzzle here at the last section, apparently we might take a deep-copy again, resetting this to false

        return metadataBean;
    }

    /**
     * Create a List of DisplayItemBeans from a List of Items.
     * 
     * @param itemBeans
     *            A List of ItemBeans that will provide the source of each
     *            DisplayItemBean
     * @param dataSource
     *            A DataSource for the DAO classes.
     * @param crfVersionId
     *            The CRF version Id for fetching associated
     *            ItemFormMetadataBeans.
     * @param sectionId
     *            The section ID associated with the Items.
     * @param nullValuesList
     *            A List of Strings containing "null values" such as "not
     *            applicable" or NA.
     * @return A List of DisplayItemBeans.
     */
    public static List<DisplayItemBean> getDisplayBeansFromItems(List<ItemBean> itemBeans, 
            DataSource dataSource, 
            EventCRFBean eventCrfBean, 
            int sectionId,
            List<String> nullValuesList, 
            ServletContext context) {
        // logger = LoggerFactory.getLogger(getClass().getName());
        List<DisplayItemBean> disBeans = new ArrayList<DisplayItemBean>();
        if (itemBeans == null || itemBeans.isEmpty())
            return disBeans;
        ItemFormMetadataDAO metaDao = new ItemFormMetadataDAO(dataSource);
        ItemDataDAO itemDataDAO = new ItemDataDAO(dataSource);
        DisplayItemBean displayBean;
        ItemFormMetadataBean meta;

        // Add any null values to checks or radios
        String responseName;
        List<ResponseOptionBean> respOptions;
        ResponseOptionBean respBean;

        boolean hasNullValues = nullValuesList != null && !nullValuesList.isEmpty();
        String tmpVal = "";
        // findByItemIdAndCRFVersionId
        for (ItemBean iBean : itemBeans) {
            displayBean = new DisplayItemBean();
            meta = metaDao.findByItemIdAndCRFVersionId(iBean.getId(), eventCrfBean.getCRFVersionId());//TODO: eventcrfBean is not valid??

            // Only include Items that belong to the associated section
            if (meta.getSectionId() == sectionId) {
                displayBean.setItem(iBean);
                ItemDataBean itemDataBean = itemDataDAO.findByItemIdAndEventCRFId(iBean.getId(), eventCrfBean.getId());//findByItemIdAndEventCRFIdAndOrdinal(iBean.getId(), eventCrfBean.getId(), ordinal)
                // null values is set by adding the event def. crf bean, but
                // here we have taken a different approach, tbh
                // displayBean.setEventDefinitionCRF();
                displayBean.setMetadata(runDynamicsCheck(meta, eventCrfBean, itemDataBean, context));
                displayBean.setData(itemDataBean);
                displayBean.setDbData(itemDataBean);
                // System.out.println("just set: " + itemDataBean.getValue() + " from " + itemDataBean.getItemId());

                responseName = displayBean.getMetadata().getResponseSet().getResponseType().getName();
                respOptions = displayBean.getMetadata().getResponseSet().getOptions();
                if (hasNullValues
                    && respOptions != null
                    && ("checkbox".equalsIgnoreCase(responseName) || "radio".equalsIgnoreCase(responseName) || "single-select".equalsIgnoreCase(responseName) || "multi-select"
                            .equalsIgnoreCase(responseName))) {

                    for (String val : nullValuesList) {
                        respBean = new ResponseOptionBean();
                        // BWP>> set text to the extended version, "not
                        // applicable"?
                        tmpVal = DataEntryInputGenerator.NULL_VALUES_LONGVERSION.get(val);
                        if (tmpVal != null && tmpVal.length() > 0) {
                            respBean.setText(tmpVal);
                        } else {
                            respBean.setText(val);
                        }

                        respBean.setValue(val);
                        respOptions.add(respBean);
                    }
                }
                disBeans.add(displayBean);
                // logger.info("### respOptions size
                // "+respOptions.size()+" of item name "+iBean.getName());
            }
            // logger.info("### found name "+iBean.getName()+" and found
            // response size: "+
            // displayBean.getMetadata().getResponseSet().getOptions().size());

        }

        // sort the List of DisplayItemBeans on their ordinal
        Collections.sort(disBeans);
        return disBeans;
    }

    
    
    public static List<DisplayItemBean> getDisplayBeansFromItemsForPrint(List<ItemBean> itemBeans, 
            DataSource dataSource, 
            EventCRFBean eventCrfBean, 
            int sectionId,
            List<String> nullValuesList, 
            ServletContext context,
            int crfVersionId) {
        // logger = LoggerFactory.getLogger(getClass().getName());
        List<DisplayItemBean> disBeans = new ArrayList<DisplayItemBean>();
        if (itemBeans == null || itemBeans.isEmpty())
            return disBeans;
        ItemFormMetadataDAO metaDao = new ItemFormMetadataDAO(dataSource);
        ItemDataDAO itemDataDAO = new ItemDataDAO(dataSource);
        DisplayItemBean displayBean;
        ItemFormMetadataBean meta;

        // Add any null values to checks or radios
        String responseName;
        List<ResponseOptionBean> respOptions;
        ResponseOptionBean respBean;

        boolean hasNullValues = nullValuesList != null && !nullValuesList.isEmpty();
        String tmpVal = "";
        // findByItemIdAndCRFVersionId
        for (ItemBean iBean : itemBeans) {
            displayBean = new DisplayItemBean();
            meta = metaDao.findByItemIdAndCRFVersionId(iBean.getId(), crfVersionId);//TODO: eventcrfBean is not valid??

            // Only include Items that belong to the associated section
            if (meta.getSectionId() == sectionId) {
                displayBean.setItem(iBean);
                ItemDataBean itemDataBean = itemDataDAO.findByItemIdAndEventCRFId(iBean.getId(), eventCrfBean.getId());//findByItemIdAndEventCRFIdAndOrdinal(iBean.getId(), eventCrfBean.getId(), ordinal)
                // null values is set by adding the event def. crf bean, but
                // here we have taken a different approach, tbh
                // displayBean.setEventDefinitionCRF();
                displayBean.setMetadata(runDynamicsCheck(meta, eventCrfBean, itemDataBean, context));
                displayBean.setData(itemDataBean);
                displayBean.setDbData(itemDataBean);
                // System.out.println("just set: " + itemDataBean.getValue() + " from " + itemDataBean.getItemId());

                responseName = displayBean.getMetadata().getResponseSet().getResponseType().getName();
                respOptions = displayBean.getMetadata().getResponseSet().getOptions();
                if (hasNullValues
                    && respOptions != null
                    && ("checkbox".equalsIgnoreCase(responseName) || "radio".equalsIgnoreCase(responseName) || "single-select".equalsIgnoreCase(responseName) || "multi-select"
                            .equalsIgnoreCase(responseName))) {

                    for (String val : nullValuesList) {
                        respBean = new ResponseOptionBean();
                        // BWP>> set text to the extended version, "not
                        // applicable"?
                        tmpVal = DataEntryInputGenerator.NULL_VALUES_LONGVERSION.get(val);
                        if (tmpVal != null && tmpVal.length() > 0) {
                            respBean.setText(tmpVal);
                        } else {
                            respBean.setText(val);
                        }

                        respBean.setValue(val);
                        respOptions.add(respBean);
                    }
                }
                disBeans.add(displayBean);
                // logger.info("### respOptions size
                // "+respOptions.size()+" of item name "+iBean.getName());
            }
            // logger.info("### found name "+iBean.getName()+" and found
            // response size: "+
            // displayBean.getMetadata().getResponseSet().getOptions().size());

        }

        // sort the List of DisplayItemBeans on their ordinal
        Collections.sort(disBeans);
        return disBeans;
    }

    public static List<DisplayItemBean> getDisplayBeansFromItems(List<ItemBean> itemBeans, DataSource dataSource, EventCRFBean eventCrfBean, int sectionId,
            EventDefinitionCRFBean edcb, int test, ServletContext context) {
        //int test is for method overloading. 
        List<DisplayItemBean> disBeans = new ArrayList<DisplayItemBean>();
        if (itemBeans == null || itemBeans.isEmpty())
            return disBeans;
        ItemFormMetadataDAO metaDao = new ItemFormMetadataDAO(dataSource);
        ItemDataDAO itemDataDao = new ItemDataDAO(dataSource);
        DisplayItemBean displayBean;
        ItemFormMetadataBean meta;
        for (ItemBean iBean : itemBeans) {
            displayBean = new DisplayItemBean();
            displayBean.setEventDefinitionCRF(edcb);
            meta = metaDao.findByItemIdAndCRFVersionId(iBean.getId(), eventCrfBean.getCRFVersionId());
            ItemDataBean itemDataBean = itemDataDao.findByItemIdAndEventCRFId(iBean.getId(), eventCrfBean.getId());

            if (meta.getSectionId() == sectionId) {
                displayBean.setItem(iBean);
                displayBean.setMetadata(runDynamicsCheck(meta, eventCrfBean, itemDataBean, context));
                displayBean.setData(itemDataBean);
                // << tbh 05/2010
                disBeans.add(displayBean);
            }
        }
        Collections.sort(disBeans);
        return disBeans;
    }

    public void addBeansToResponseOptions(List<String> values, List<ResponseOptionBean> respOptions) {
        ResponseOptionBean respBean;
        String tmpVal = "";
        // Only add the values if the ResponseOptionBeans do not already contain
        // them
        for (String val : values) {
            respBean = new ResponseOptionBean();
            // BWP>> set text to the extended version, "not applicable"?
            tmpVal = DataEntryInputGenerator.NULL_VALUES_LONGVERSION.get(val);
            if (tmpVal != null && tmpVal.length() > 0) {
                respBean.setText(tmpVal);
            } else {
                respBean.setText(val);
            }
            respBean.setValue(val);
            respOptions.add(respBean);
        }
    }

    public List<ResponseOptionBean> getNullValuesListAsOptionBeans(List<NullValue> nullValues) {

        List<ResponseOptionBean> responseOptionBeans = new ArrayList<ResponseOptionBean>();
        if (nullValues == null || nullValues.isEmpty()) {
            return responseOptionBeans;
        }
        ResponseOptionBean respBean;
        String name;
        for (NullValue nullVal : nullValues) {
            name = nullVal.getName();
            respBean = new ResponseOptionBean();
            respBean.setText(name);
            respBean.setValue(name);
            responseOptionBeans.add(respBean);
        }
        return responseOptionBeans;
    }

    public DisplayItemBean getDisplayBeanFromSingleItem(ItemFormMetadataBean itemFBean, int sectionId, DataSource dataSource, EventCRFBean eventCrfBean,
            List<String> nullValuesList, ServletContext context) {

        DisplayItemBean disBean = new DisplayItemBean();
        ItemBean itemBean = new ItemBean();
        ItemDAO itemDAO = new ItemDAO(dataSource);
        ItemDataDAO itemDataDao = new ItemDataDAO(dataSource);
        if (itemFBean == null)
            return disBean;

        itemBean = (ItemBean) itemDAO.findByPK(itemFBean.getItemId());
        if (itemBean == null) {
            itemBean = new ItemBean();
        }

        // Add any null values to checks or radios
        String responseName;
        List<ResponseOptionBean> respOptions;
        ResponseOptionBean respBean;
        boolean hasNullValues = nullValuesList != null && !nullValuesList.isEmpty();

        // Only include Items that belong to the associated section
        if (itemFBean.getSectionId() == sectionId) {
            ItemDataBean itemDataBean = itemDataDao.findByItemIdAndEventCRFId(itemBean.getId(), eventCrfBean.getId());
            disBean.setItem(itemBean);
            disBean.setMetadata(runDynamicsCheck(itemFBean, eventCrfBean, itemDataBean, context));
            disBean.setData(itemDataBean);
            logger.debug("3. just set: " + itemDataBean.getValue());
            responseName = disBean.getMetadata().getResponseSet().getResponseType().getName();
            respOptions = disBean.getMetadata().getResponseSet().getOptions();
            if (hasNullValues
                && respOptions != null
                && ("checkbox".equalsIgnoreCase(responseName) || "radio".equalsIgnoreCase(responseName) || "single-select".equalsIgnoreCase(responseName) || "multi-select"
                        .equalsIgnoreCase(responseName))) {
                this.addBeansToResponseOptions(nullValuesList, respOptions);
            }
        }
        return disBean;
    }

    /**
     * Create an XHTML table that encompasses the Items that are not part of a
     * group.
     * 
     * @param items
     *            A List of DisplayItemBeans that make up the table content.
     * @param tabindex
     *            The tab index for any form fields created in the table.
     * @param hasDiscrepancyMgt
     *            'true' is the items should be accompanied by discrepancy
     *            notes.
     * @param hasDBValues
     *            'true' if the items within the table are pre-filled with
     *            database values.
     * @param forPrinting
     * @return The org.jdom Element that represents the table.
     */
    public Element createXHTMLTableFromNonGroup(List<DisplayItemBean> items, Integer tabindex, boolean hasDiscrepancyMgt, boolean hasDBValues,
            boolean forPrinting) {
        Element table = new Element("table");
        String cssClasses = CssRules.getClassNamesForTag(table.getName());
        table.setAttribute("class", cssClasses);
        /*
         * Don't add header cells to orphan-type tables, because when the header
         * is empty, Firefox does not show any borders: Element thead = new
         * Element("thead"); table.addContent(thead);
         */
        Element tbody = new Element("tbody");
        table.addContent(tbody);

        // divide up items into columns
        int numberOfColumns = getNumberOfColumnsFromItems(items);
       // numberOfColumns = 5;
        // Fixing table width for orphan table Mantis issue: 9087.
        // ToDo recheck why the main table getting fixed 
       // int tableWidth = numberOfColumns*300;
     //   table.setAttribute("style", "width:" + tableWidth + "px;");


        // A Map designed to hold DisplayItemBeans according to their column
        // number
        // The Map index is the column number; it is sorted so it begins with
        // column 1
        SortedMap<Integer, List<DisplayItemBean>> colMap = new TreeMap<Integer, List<DisplayItemBean>>();
        // A row by row Map for multi-col tables; the index is sorted and begins
        // with the first row of DisplayItemBeans
        SortedMap<Integer, List<DisplayItemBean>> multiRowMap = new TreeMap<Integer, List<DisplayItemBean>>();
        // This isn't necessary unless there is more than one column
       if (numberOfColumns > 1) {
            int column;
            for (DisplayItemBean displayItem : items) {
                column = displayItem.getMetadata().getColumnNumber();
                if (colMap.get(column) == null) {
                    colMap.put(column, new ArrayList<DisplayItemBean>());
                }
                colMap.get(column).add(displayItem);
            }
            int numberOfRows = getNumberOfTableRows(colMap);
           // numberOfRows = 2;
            List<DisplayItemBean> itemsList;
            // the list inside multiRowMap
            List<DisplayItemBean> rowsList;
            for (int i = 1; i <= numberOfRows; i++) {
                rowsList = new ArrayList<DisplayItemBean>();
                for (int j = 1; j <= colMap.size(); j++) {
                    itemsList = colMap.get(j);
                    if (itemsList != null && itemsList.size() >= i) {
                        rowsList.add(itemsList.get(i - 1));
                    }
                    // iterate through the columns map
                }
                multiRowMap.put(i, rowsList);
            }

        }
        // Handle the Items in a single column
        // or several columns
        // The last boolean parameter specifies whether the displayed items
        // are involved with printing, in which discrepancy note icons are not
        // made clickable
        if (numberOfColumns == 1) {
            createSingleColumn(tbody, items, tabindex, hasDiscrepancyMgt, hasDBValues, forPrinting);
        } else {
            createMultipleCols(tbody, multiRowMap, tabindex, hasDiscrepancyMgt, numberOfColumns, hasDBValues, forPrinting);
        }
        return table;
    }

    /**
     * Calculate the number of rows in a structure (table) of DisplayItemBeans.
     * 
     * @param columnsMap
     *            A Map that maps the column number to the List of
     *            DisplayItemBeans in that column.
     * @return The number of rows in the table.
     */
    private int getNumberOfTableRows(Map<Integer, List<DisplayItemBean>> columnsMap) {

        int highestRowNumber = 0;
        int temp;
        Map.Entry<Integer, List<DisplayItemBean>> me;
        List<DisplayItemBean> beanList;
        for (Iterator<Map.Entry<Integer, List<DisplayItemBean>>> iter = columnsMap.entrySet().iterator(); iter.hasNext();) {
            me = iter.next();
            beanList = me.getValue();
            temp = beanList.size();
            highestRowNumber = temp > highestRowNumber ? temp : highestRowNumber;
        }
        return 1;
    }

    /**
     * Get the highest column number for a List of DisplayItemBeans.
     * 
     * @param displayItems
     *            The List of DisplayItemBeans.
     * @return An int representing the highest column number in the List,
     *         obained by getting the ItemFormMetadataBean columnNumber
     *         property.
     */
    public int getNumberOfColumnsFromItems(List<DisplayItemBean> displayItems) {
        if (displayItems == null)
            return 0;
        // one column is the default
        int highestColumnNum = 1;
        int temp;
        for (DisplayItemBean displayItem : displayItems) {
            temp = displayItem.getMetadata().getColumnNumber();
            highestColumnNum = highestColumnNum < temp ? temp : highestColumnNum;
        }
        return highestColumnNum;
    }

    /**
     * Create a table containing a single column of Items.
     * 
     * @param tbody
     *            The JDOM Element representing a tbody tag for the XHTML table.
     * @param items
     *            The DisplayItemBeans representing the Items for the table.
     * @param tabindex
     *            The Integer representing the form field's tab index.
     * @param hasDiscrepancyMgt
     *            A boolean value specifying whether discrepancy icons should be
     *            displayed.
     * @param hasDBValues
     *            A boolean value specifying whether the input elements are
     *            prefiled with database values.
     * @param forPrinting
     *            A boolean value specifying whether the CRF is being displayed
     *            for printing (therefore, the D Note icons should not be
     *            enabled or clickable).
     */
    private void createSingleColumn(Element tbody, List<DisplayItemBean> items, Integer tabindex, boolean hasDiscrepancyMgt, boolean hasDBValues,
            boolean forPrinting) {
        CellFactory cellFactory = new CellFactory();
        // Use this decorator class to generate vertical checkboxes, if any of
        // the
        // items have this type of layout
        CellFactoryPrintDecorator cellFactoryPrintDecorator = new CellFactoryPrintDecorator();
        // Create header/subheader rows, if necessary
        String tmpName;
        for (DisplayItemBean disBean : items) {
            tmpName = disBean.getMetadata().getHeader();
            if (tmpName != null && tmpName.length() > 0) {
                Element tr = createHeaderSubheaderCell(disBean, true);
                // add the row to the tbody element]
                tbody.addContent(tr);
            }
            // Does the row tag need a subheader type background?
            tmpName = disBean.getMetadata().getSubHeader();
            if (tmpName != null && tmpName.length() > 0) {
                Element tr2 = createHeaderSubheaderCell(disBean, false);
                tbody.addContent(tr2);
            }

            // Create the row containing the item + form field
            Element trRow = new Element("tr");
            String leftSideTxt = "";
            String questNumber = "";
            tbody.addContent(trRow);
            String responseName = disBean.getMetadata().getResponseSet().getResponseType().getName();
            Element tdCell = new Element("td");
            String classNames = CssRules.getClassNamesForTag("td");
            tdCell.setAttribute("class", classNames);
            // use vertical-alignment for these TD cells
            tdCell.setAttribute("style", "vertical-align:top");

            boolean horizontalLayout = "horizontal".equalsIgnoreCase(disBean.getMetadata().getResponseLayout());
            // Just use this method for vertical checkboxes and radio buttons
            if (("checkbox".equalsIgnoreCase(responseName) || "radio".equalsIgnoreCase(responseName)) && !horizontalLayout) {

                cellFactoryPrintDecorator.createCellContentsForVerticalLayout(tdCell, responseName, disBean, ++tabindex, hasDiscrepancyMgt, hasDBValues,
                        forPrinting);

            } else {
                cellFactory.createCellContents(tdCell, responseName, disBean, ++tabindex, hasDiscrepancyMgt, hasDBValues, forPrinting);
            }
            questNumber = disBean.getMetadata().getQuestionNumberLabel();
            boolean hasQuestion = questNumber.length() > 0;
            leftSideTxt = disBean.getMetadata().getLeftItemText();
            cellFactory.addTextToCell(tdCell, leftSideTxt, CellFactory.LEFT);
            if (hasQuestion) {
                addQuestionNumbers(tdCell, questNumber);
            }
            trRow.addContent(tdCell);

        }

    }

    /* Add question numbers to the left of existing cell content */
    private Element addQuestionNumbers(Element tdCell, String questNumber) {
        Element existingSpan = tdCell.getChild("span");
        Element newSpan = new Element("span");
        newSpan.setAttribute("style", "margin-right:1em");
        newSpan.addContent(questNumber);
        existingSpan.addContent(0, newSpan);
        return tdCell;
    }

    private Element createHeaderSubheaderCell(DisplayItemBean disBean, boolean isHeader) {
        CellFactory cellFactory = new CellFactory();
        Element tr = new Element("tr");
        Element td = new Element("td");
        String cssClassNames = CssRules.getClassNamesForTag("tr header");
        tr.setAttribute("class", cssClassNames);
        cssClassNames = CssRules.getClassNamesForTag("td header");
        td.setAttribute("class", cssClassNames);
        if (isHeader) {
            td = cellFactory.addTextToCell(td, disBean.getMetadata().getHeader(), CellFactory.LEFT);
        } else {
            td = cellFactory.addTextToCell(td, disBean.getMetadata().getSubHeader(), CellFactory.LEFT);
        }

        tr.addContent(td);
        return tr;
    }

    private Element createHeaderCellMultiColumn(DisplayItemBean disBean, boolean isHeader, Element row, int numberOfColumns) {
        // At this point, the row could already contain td cells with headers or
        // subheaders
        CellFactory cellFactory = new CellFactory();
        if (row == null) {
            row = new Element("tr");
        }
        Element td = new Element("td");
        String cssClassNames = "";
        // Does the row have the class attribute yet?
        if (row.getAttribute("class") == null) {
            cssClassNames = CssRules.getClassNamesForTag("tr header");
            row.setAttribute("class", cssClassNames);
        }

        cssClassNames = CssRules.getClassNamesForTag("td header");
        td.setAttribute("class", cssClassNames);
        if (isHeader) {
            td = cellFactory.addTextToCell(td, disBean.getMetadata().getHeader(), CellFactory.LEFT);
        } else {
            td = cellFactory.addTextToCell(td, disBean.getMetadata().getSubHeader(), CellFactory.LEFT);
        }
        row.addContent(td);
        return row;
    }

    /**
     * Create a multiple column XHTML table from a list of display items.
     * 
     * @param tbody
     *            The Element to add the rows to.
     * @param displayItemRows
     *            The list of DisplayItemBean and the column they should appear
     *            in.
     * @param tabindex
     *            The tab index of the form field.
     * @param hasDiscrepancyMgt
     *            A flag indicating whether or not the Item is involved with
     * @param numberOfColumns
     *            An integer representing the highest number of columns with
     *            cell content in this group of rows (e.g., one row has five td
     *            cells with content, the highest number of any
     * @param hasDBValues
     * @param forPrinting
     */
    private void createMultipleCols(Element tbody, Map<Integer, List<DisplayItemBean>> displayItemRows, Integer tabindex, boolean hasDiscrepancyMgt,
            int numberOfColumns, boolean hasDBValues, boolean forPrinting) {

        CellFactory cellFactory = new CellFactory();
        // Use this decorator class to generate vertical checkboxes or radios,
        // if any of the
        // items have this type of layout
        CellFactoryPrintDecorator cellFactoryPrintDecorator = new CellFactoryPrintDecorator();

        // Create header/subheader rows, if necessary
        Map.Entry<Integer, List<DisplayItemBean>> me;
        List<DisplayItemBean> itemsList;
        int numberOfBeansInRow;

        Element  formFieldRow = new Element("tr");;
        Element headerRow;
        Element subHeaderRow;

        for (Iterator<Map.Entry<Integer, List<DisplayItemBean>>> iter = displayItemRows.entrySet().iterator(); iter.hasNext();) {
            me = iter.next();
            itemsList = me.getValue();
            numberOfBeansInRow = itemsList.size();
            // Each Entry points to a List of DisplayItemBeans that are in the
            // rows
            // Create the row containing the item + form field
            
            headerRow = new Element("tr");
            subHeaderRow = new Element("tr");
            String leftSideTxt = "";
            // keep track of the last bean in the row, so we can fill the row
            // with any necessary empty td cells
            for (DisplayItemBean disBean : me.getValue()) {
                // Each row has its own instance of headerRow and subHeaderRow
                if (disBean.getMetadata().getHeader().length() > 0) {
                    headerRow = createHeaderCellMultiColumn(disBean, true, headerRow, numberOfBeansInRow);
                }
                if (disBean.getMetadata().getSubHeader().length() > 0) {
                    subHeaderRow = createHeaderCellMultiColumn(disBean, false, subHeaderRow, numberOfBeansInRow);
                }
                String responseName = disBean.getMetadata().getResponseSet().getResponseType().getName();
                Element tdCell = new Element("td");
                String classNames = CssRules.getClassNamesForTag("td");
                tdCell.setAttribute("class", classNames);
                String questNumber = "";

                questNumber = disBean.getMetadata().getQuestionNumberLabel();
                boolean hasQuestion = questNumber.length() > 0;
                leftSideTxt = disBean.getMetadata().getLeftItemText();

                // use vertical-alignment for these TD cells
                tdCell.setAttribute("style", "vertical-align:top");

                boolean horizontalLayout = "horizontal".equalsIgnoreCase(disBean.getMetadata().getResponseLayout());
                // Just use this method for vertically arranged checkboxes and
                // radio buttons
                // The final "forPrinting" parameter is a boolean specifying
                // whether the CRF
                // is a print view; therefore, disable the clicking of
                // discrepancy note icons
                if (("checkbox".equalsIgnoreCase(responseName) || "radio".equalsIgnoreCase(responseName)) && !horizontalLayout) {

                    cellFactoryPrintDecorator.createCellContentsForVerticalLayout(tdCell, responseName, disBean, ++tabindex, hasDiscrepancyMgt, hasDBValues,
                            forPrinting);

                } else {
                    cellFactory.createCellContents(tdCell, responseName, disBean, ++tabindex, hasDiscrepancyMgt, hasDBValues, forPrinting);
                }
                cellFactory.addTextToCell(tdCell, leftSideTxt, CellFactory.LEFT);
                if (hasQuestion) {
                    addQuestionNumbers(tdCell, questNumber);
                }
                formFieldRow.addContent(tdCell);

            }
            int cellCountDif;
            int childrenCount;
            // if the header/subheader rows have content,
            // then add them to the tbody element
            if ((childrenCount = headerRow.getChildren("td").size()) > 0) {
                // if the header row has fewer cells then the number of item TD
                // cells,
                // then pad the header row with empty td cells to make up the
                // difference
                cellCountDif = numberOfBeansInRow - childrenCount;
                if (cellCountDif > 0) {
                    headerRow = addEmptyTDcells(headerRow, cellCountDif);
                }
                tbody.addContent(headerRow);
            }
            if ((childrenCount = subHeaderRow.getChildren("td").size()) > 0) {
                cellCountDif = numberOfBeansInRow - childrenCount;
                if (cellCountDif > 0) {
                    subHeaderRow = addEmptyTDcells(subHeaderRow, cellCountDif);
                }
                tbody.addContent(subHeaderRow);
            }
            // pad the actual row of form fields with td cells, to make the
            // columns equivalent
            cellCountDif = numberOfColumns - numberOfBeansInRow;
            if (cellCountDif > 0) {
                formFieldRow = addEmptyTDcells(formFieldRow, cellCountDif);
            }
           
        }
        tbody.addContent(formFieldRow);
    }

    private Element addEmptyTDcells(Element row, int numberOfCellsToAdd) {
        Element td;
        for (int i = 1; i <= numberOfCellsToAdd; i++) {
            td = new Element("td");
            row.addContent(td);
        }
        return row;
    }

    /**
     * Create a DisplayFormGroupBean from a List of DisplayItemBeans and a
     * FormGroupBean.
     * 
     * @param displayItems
     *            The DisplayItemBeans that provide the table content.
     * @param itemGroup
     *            The FormGroupBean that represents the Group information from
     *            the spreadsheet template..
     * @return A DisplayFormGroupBean.
     */
    public DisplayItemGroupBean createDisplayFormGroup(List<DisplayItemBean> displayItems, ItemGroupBean itemGroup) {
        DisplayItemGroupBean fgBean = new DisplayItemGroupBean();
        fgBean.setItems(displayItems);
        fgBean.setItemGroupBean(itemGroup);
        fgBean.setGroupMetaBean(itemGroup.getMeta());
        return fgBean;
    }

    /**
     * Create a DisplaySectionBean with a list of ItemGroupBeans. This List will
     * include any ItemGroupBeans that are associated with ungrouped items; in
     * other words, the DisplaySectionBean will represent a section that has
     * both ungrouped and grouped tables.
     * 
     * @param sectionId
     *            The Section ID associated with the Items, which end up
     *            providing the content of the tables.
     * @param crfVersionId
     *            The CRF version ID associated with the Items.
     * @param dataSource
     * @param eventCRFDefId
     *            The id for the Event CRF Definition.
     * @return A DisplaySectionBean with sorted ItemGroupBeans, meaning the
     *         ItemGroupBeans should be listed in the order that they appear on
     *         the CRF section.
     * @return A DisplaySectionBean representing a CRF section.
     */
    public DisplaySectionBean createDisplaySectionBWithFormGroups(int sectionId, int crfVersionId, DataSource dataSource, int eventCRFDefId,
            EventCRFBean eventCrfBean, ServletContext context) {

        DisplaySectionBean displaySectionBean = new DisplaySectionBean();

        ItemGroupDAO formGroupDAO = new ItemGroupDAO(dataSource);
        ItemGroupMetadataDAO igMetaDAO = new ItemGroupMetadataDAO(dataSource);
        ItemDAO itemDao = new ItemDAO(dataSource);
        ItemFormMetadataDAO metaDao = new ItemFormMetadataDAO(dataSource);
        SectionDAO sectionDao = new SectionDAO(dataSource);

        // Give the DisplaySectionBean a legitimate SectionBean
        SectionBean secBean = (SectionBean) sectionDao.findByPK(sectionId);
        displaySectionBean.setSection(secBean);
        // changed from: findGroupBySectionId
        List<ItemGroupBean> itemGroupBeans = formGroupDAO.findLegitGroupAllBySectionId(sectionId);
        // all items associated with the section, including those not in a group
        List<ItemFormMetadataBean> allMetas = new ArrayList<ItemFormMetadataBean>();
        try {
            allMetas = metaDao.findAllBySectionId(sectionId);
        } catch (OpenClinicaException oce) {
            logger.info("oce.getOpenClinicaMessage() = " + oce.getOpenClinicaMessage());
        }
        // Sort these items according to their position on the CRF; their
        // ordinal
        Collections.sort(allMetas);
        // The DisplayItemGroupBean(s) for "nongrouped" items
        List<DisplayItemGroupBean> nonGroupBeans = null;

        // if(itemGroupBeans.isEmpty()) return displaySectionBean;
        // Find out whether there are any checkboxes/radios/select elements
        // and if so, get any null values
        // associated with them
        List<String> nullValuesList = new ArrayList<String>();
        boolean itemsHaveChecksRadios = itemsIncludeChecksRadiosSelects(allMetas);
        if (itemsHaveChecksRadios && eventCRFDefId > 0) {
            // method returns null values as a List<String>
            nullValuesList = this.getNullValuesByEventCRFDefId(eventCRFDefId, dataSource);
        }

        // Get the items associated with each group
        List<ItemBean> itBeans;
        List<DisplayItemBean> displayItems;
        List<DisplayItemGroupBean> displayFormBeans = new ArrayList<DisplayItemGroupBean>();
        DisplayItemGroupBean displayItemGBean;

        for (ItemGroupBean itemGroup : itemGroupBeans) {
            itBeans = itemDao.findAllItemsByGroupId(itemGroup.getId(), crfVersionId);
            logger.debug("just ran find all by group id " + itemGroup.getId() + " found " + itBeans.size() + " item beans");
            List<ItemGroupMetadataBean> metadata = igMetaDAO.findMetaByGroupAndSection(itemGroup.getId(), crfVersionId, sectionId);
            if (!metadata.isEmpty()) {
                // for a given crf version, all the items in the same group
                // have the same group metadata info
                // so we can get one of the metadata and set the metadata for
                // the group
                ItemGroupMetadataBean meta = metadata.get(0);
                itemGroup.setMeta(meta);
            }
            displayItems = getDisplayBeansFromItems(itBeans, dataSource, eventCrfBean, sectionId, nullValuesList, context);
            displayItemGBean = this.createDisplayFormGroup(displayItems, itemGroup);
            displayFormBeans.add(displayItemGBean);
        }
        // We still have to sort these display item group beans on their
        // ItemGroupMetadataBean?
        // then number their ordinals accordingly
        Collections.sort(displayFormBeans, new Comparator<DisplayItemGroupBean>() {

            public int compare(DisplayItemGroupBean displayItemGroupBean, DisplayItemGroupBean displayItemGroupBean1) {
                return displayItemGroupBean.getGroupMetaBean().compareTo(displayItemGroupBean1.getGroupMetaBean());
            }
        });
        // Now provide the display item group beans with an ordinal
        int digOrdinal = 0;
        for (DisplayItemGroupBean digBean : displayFormBeans) {
            digBean.setOrdinal(++digOrdinal);
        }

        // find out whether there are any ungrouped items by comparing the
        // number of
        // grouped items to allMetas.size()
        // List<DisplayItemGroupBean> nonGroupBeans=null;
        int tempCount = 0;
        for (DisplayItemGroupBean groupBean : displayFormBeans) {
            tempCount += groupBean.getItems().size();
        }
        if (tempCount < allMetas.size()) {
            nonGroupBeans = createGroupBeansForNongroupedItems(allMetas, displayFormBeans, sectionId, dataSource, nullValuesList, eventCrfBean, context);
        }
        if (nonGroupBeans != null) {
            displayFormBeans.addAll(nonGroupBeans);
        }
        // sort the list according to the ordinal of the contained
        // DisplayItemGroupBeans
        Collections.sort(displayFormBeans, new Comparator<DisplayItemGroupBean>() {

            public int compare(DisplayItemGroupBean disFormGroupBean, DisplayItemGroupBean disFormGroupBean1) {
                Integer compInt = disFormGroupBean1.getOrdinal();
                Integer compInt2 = disFormGroupBean.getOrdinal();
                return compInt2.compareTo(compInt);
            }
        });
        displaySectionBean.setDisplayFormGroups(displayFormBeans);

        return displaySectionBean;
    }
    
    
    public DisplaySectionBean createDisplaySectionBWithFormGroupsForPrint(int sectionId, int crfVersionId, DataSource dataSource, int eventCRFDefId,
            EventCRFBean eventCrfBean, ServletContext context) {

        DisplaySectionBean displaySectionBean = new DisplaySectionBean();

        ItemGroupDAO formGroupDAO = new ItemGroupDAO(dataSource);
        ItemGroupMetadataDAO igMetaDAO = new ItemGroupMetadataDAO(dataSource);
        ItemDAO itemDao = new ItemDAO(dataSource);
        ItemFormMetadataDAO metaDao = new ItemFormMetadataDAO(dataSource);
        SectionDAO sectionDao = new SectionDAO(dataSource);

        // Give the DisplaySectionBean a legitimate SectionBean
        SectionBean secBean = (SectionBean) sectionDao.findByPK(sectionId);
        displaySectionBean.setSection(secBean);
        // changed from: findGroupBySectionId
        List<ItemGroupBean> itemGroupBeans = formGroupDAO.findLegitGroupAllBySectionId(sectionId);
        // all items associated with the section, including those not in a group
        List<ItemFormMetadataBean> allMetas = new ArrayList<ItemFormMetadataBean>();
        try {
            allMetas = metaDao.findAllBySectionId(sectionId);
        } catch (OpenClinicaException oce) {
            logger.info("oce.getOpenClinicaMessage() = " + oce.getOpenClinicaMessage());
        }
        // Sort these items according to their position on the CRF; their
        // ordinal
        Collections.sort(allMetas);
        // The DisplayItemGroupBean(s) for "nongrouped" items
        List<DisplayItemGroupBean> nonGroupBeans = null;

        // if(itemGroupBeans.isEmpty()) return displaySectionBean;
        // Find out whether there are any checkboxes/radios/select elements
        // and if so, get any null values
        // associated with them
        List<String> nullValuesList = new ArrayList<String>();
        boolean itemsHaveChecksRadios = itemsIncludeChecksRadiosSelects(allMetas);
        if (itemsHaveChecksRadios && eventCRFDefId > 0) {
            // method returns null values as a List<String>
            nullValuesList = this.getNullValuesByEventCRFDefId(eventCRFDefId, dataSource);
        }

        // Get the items associated with each group
        List<ItemBean> itBeans;
        List<DisplayItemBean> displayItems;
        List<DisplayItemGroupBean> displayFormBeans = new ArrayList<DisplayItemGroupBean>();
        DisplayItemGroupBean displayItemGBean;

        for (ItemGroupBean itemGroup : itemGroupBeans) {
            itBeans = itemDao.findAllItemsByGroupIdForPrint(itemGroup.getId(), crfVersionId,sectionId);//TODO:fix me!
            logger.debug("just ran find all by group id " + itemGroup.getId() + " found " + itBeans.size() + " item beans");
            List<ItemGroupMetadataBean> metadata = igMetaDAO.findMetaByGroupAndSectionForPrint(itemGroup.getId(), crfVersionId, sectionId);//TODO:fix me add item_form_metadata.section_id to the query
            if (!metadata.isEmpty()) {
                // for a given crf version, all the items in the same group
                // have the same group metadata info
                // so we can get one of the metadata and set the metadata for
                // the group
                ItemGroupMetadataBean meta = metadata.get(0);
                itemGroup.setMeta(meta);
            }
            displayItems = getDisplayBeansFromItemsForPrint(itBeans, dataSource, eventCrfBean, sectionId, nullValuesList, context,crfVersionId);
            displayItemGBean = this.createDisplayFormGroup(displayItems, itemGroup);
            displayFormBeans.add(displayItemGBean);
        }
        // We still have to sort these display item group beans on their
        // ItemGroupMetadataBean?
        // then number their ordinals accordingly
        Collections.sort(displayFormBeans, new Comparator<DisplayItemGroupBean>() {

            public int compare(DisplayItemGroupBean displayItemGroupBean, DisplayItemGroupBean displayItemGroupBean1) {
                return displayItemGroupBean.getGroupMetaBean().compareTo(displayItemGroupBean1.getGroupMetaBean());
            }
        });
        // Now provide the display item group beans with an ordinal
        int digOrdinal = 0;
        for (DisplayItemGroupBean digBean : displayFormBeans) {
            digBean.setOrdinal(++digOrdinal);
        }

        // find out whether there are any ungrouped items by comparing the
        // number of
        // grouped items to allMetas.size()
        // List<DisplayItemGroupBean> nonGroupBeans=null;
        int tempCount = 0;
        for (DisplayItemGroupBean groupBean : displayFormBeans) {
            tempCount += groupBean.getItems().size();
        }
        if (tempCount < allMetas.size()) {
            nonGroupBeans = createGroupBeansForNongroupedItems(allMetas, displayFormBeans, sectionId, dataSource, nullValuesList, eventCrfBean, context);
        }
        if (nonGroupBeans != null) {
            displayFormBeans.addAll(nonGroupBeans);
        }
        // sort the list according to the ordinal of the contained
        // DisplayItemGroupBeans
        Collections.sort(displayFormBeans, new Comparator<DisplayItemGroupBean>() {

            public int compare(DisplayItemGroupBean disFormGroupBean, DisplayItemGroupBean disFormGroupBean1) {
                Integer compInt = disFormGroupBean1.getOrdinal();
                Integer compInt2 = disFormGroupBean.getOrdinal();
                return compInt2.compareTo(compInt);
            }
        });
        displaySectionBean.setDisplayFormGroups(displayFormBeans);

        return displaySectionBean;
    }

    /**
     * This method is designed to make it easier to order grouped and nongrouped
     * items on a CRF. This method generates a List of DisplayItemGroupBeans
     * representing the items that are not formally part of a group-type
     * horizontal table (they have a name in the database of 'Ungrouped' ). The
     * DisplayItemGroupBeans are ordered according to the position of the items
     * on the CRF, compared with the CRF items that are in groups. When this
     * List is combined with a List of DisplayItemGroupBeans representing
     * *grouped* items, the combined List can be sorted and displayed in the
     * proper order on a CRF.
     * 
     * @param allItems
     *            A List of the ItemFormMetadataBeans associated with the CRF.
     * @param displayFormBeans
     *            The List of DisplayItemGroupBeans
     * @param sectionId
     *            The section ID associated with the items
     * @param dataSource
     *            The DataSource used to acquire the DAO-related connections
     * @param nullValuesList
     *            The list of any "null values" associated with the items (like
     *            "not applicable")
     * @return An ArrayList of DisplayItemGroupBeans for 'Ungrouped' items.
     */
    private List<DisplayItemGroupBean> createGroupBeansForNongroupedItems(List<ItemFormMetadataBean> allItems, List<DisplayItemGroupBean> displayFormBeans,
            int sectionId, DataSource dataSource, List<String> nullValuesList, EventCRFBean eventCrfBean, ServletContext context) {

        // This will hold the List of placeholder groupBeans for orphaned items
        List<DisplayItemGroupBean> groupBeans = new ArrayList<DisplayItemGroupBean>();

        // Now create a Map that maps the item to the group ordinal
        // (e.g., 1) or 0 (non-group),
        // as a convenient way to position an item on a CRF
        // and associate an item with the ordinal of its group.
        // The inner Map maps the ItemFormMetadataBean to its group ordinal;
        // The Integer index to this Map represents the order of the item on the
        // CRF.
        SortedMap<Integer, Map<ItemFormMetadataBean, Integer>> groupMapping = new TreeMap<Integer, Map<ItemFormMetadataBean, Integer>>();

        Map<ItemFormMetadataBean, Integer> innerMap;
        int counter = 0;
        int tmpOrdinal;

        for (ItemFormMetadataBean imetaBean : allItems) {
            innerMap = new HashMap<ItemFormMetadataBean, Integer>();
            if (isGrouped(imetaBean, displayFormBeans)) {
                tmpOrdinal = getGroupOrdinal(imetaBean, displayFormBeans);
                innerMap.put(imetaBean, tmpOrdinal);
            } else {
                innerMap.put(imetaBean, 0);
            }
            groupMapping.put(++counter, innerMap);
        }

        // The groupMapping Map maps the index position of the item on the CRF
        // form (1,2,3...) to
        // the ItemFormMetadataBean and its associated group ordinal, if it has
        // one
        // If the ordinal is 0, then the associated ItemFormMetadataBean
        // represents an
        // ungrouped or orphaned item
        DisplayItemGroupBean nongroupedBean;
        ItemGroupBean itemGroup = new ItemGroupBean();
        ItemGroupMetadataBean metaBean = new ItemGroupMetadataBean();

        metaBean.setName(UNGROUPED);
        List<DisplayItemBean> items;
        // Now cycle through the groupMapping and create default groups or
        // DisplayItemGroupBeans, in order, for
        // the orphaned items.
        // A DisplayItemGroupBean is only created and stored in the returned
        // List
        // if its contents or associated items are nongrouped.

        // This int tracks the ordinal associated with each grouped item's
        // associated
        // DisplayItemGroupBean
        // as that item is incremented
        int ordinalTracker = 0;
        // This int is set to the latest DisplayItemGroupBean ordinal containing
        // nongrouped beans, so that it can be incremented and used to change
        // the ordinal of any DisplayItemGroupBeans (containing *grouped* items)
        // that follow this bean on the CRF
        // int nonGroupOrdinal=0;
        Map.Entry<Integer, Map<ItemFormMetadataBean, Integer>> entry;
        Map<ItemFormMetadataBean, Integer> beanMap;
        nongroupedBean = new DisplayItemGroupBean();
        ItemFormMetadataBean tempItemMeta;
        int tempOrdinal;
        // a flag indicating that the last item handled was an orphaned one
        // so that if the next item is grouped, that's a signal to store the
        // new itemgroupbean just created for orphaned items in in the
        // itemgroupbean List.
        boolean isOrphaned = false;
        // cycle through each item and create DisplayItemGroupBean(s) for
        // any of the orphaned items
        for (Iterator<Map.Entry<Integer, Map<ItemFormMetadataBean, Integer>>> iter = groupMapping.entrySet().iterator(); iter.hasNext();) {

            entry = iter.next();
            beanMap = entry.getValue(); // the ItemFormMetadataBean and any
            // group ordinal
            tempItemMeta = beanMap.keySet().iterator().next(); // the
            // ItemFormMetadataBean
            // If this value is 0, the item is orphaned; if > 0 the item is
            // grouped
            // and doesn't need a new itemgroupbean
            tempOrdinal = beanMap.get(tempItemMeta);
            // we need to create a new itemgroupbean if tempOrdinal == 0 and
            // ordinalTracker != 0
            if (tempOrdinal == 0) { // an orphaned item

                if (ordinalTracker == 0 || !isOrphaned) {
                    // initialize a new group for the item
                    nongroupedBean = new DisplayItemGroupBean();
                    itemGroup = new ItemGroupBean();
                    itemGroup.setName(UNGROUPED);
                    nongroupedBean.setItemGroupBean(itemGroup);
                    // set this flag, so that if the next item is orphaned, then
                    // the code can place it in the existing itemgroupbean
                    isOrphaned = true;
                    ordinalTracker++;
                    nongroupedBean.setOrdinal(ordinalTracker);
                    // nonGroupOrdinal=nongroupedBean.getOrdinal();
                    // nonGroupOrdinal= ordinalTracker;
                }
                // Add the item as a displayitem to the itemgroupbean
                nongroupedBean.getItems().add(getDisplayBeanFromSingleItem(tempItemMeta, sectionId, dataSource, eventCrfBean, nullValuesList, context));

            } else { // a grouped item
                // if the last item was orphaned then a new itemgroupbean had to
                // have
                // been created; therefore, store it in the List.
                if (isOrphaned) {
                    groupBeans.add(nongroupedBean);
                    // We also know that the ordinal has changed, because a
                    // nongroupedBean
                    // has been created
                    ordinalTracker++;
                    incrementOrdinal(tempItemMeta, displayFormBeans, ordinalTracker);

                } else {
                    ordinalTracker = getGroupOrdinal(tempItemMeta, displayFormBeans);
                }
                isOrphaned = false;

            }

        }// end for
        // If the last item was orphaned, then we know that a new itemgroup
        // is leftover and must be added to the List
        if (isOrphaned) {
            groupBeans.add(nongroupedBean);
        }

        return groupBeans;
    }

    private void incrementOrdinal(ItemFormMetadataBean itemFormBean, List<DisplayItemGroupBean> displayFormBeans, int ordinalTracker) {
        outer: for (DisplayItemGroupBean digBean : displayFormBeans) {
            for (DisplayItemBean diBean : digBean.getItems()) {
                if (itemFormBean.getItemId() == diBean.getItem().getId()) {
                    // int tmp = digBean.getOrdinal();
                    // ++tmp
                    // The DisplayItemGroupBean's ordinal should be one more
                    // than any
                    // preceding DisplayItemGroupBeans involving nongrouped
                    // items
                    digBean.setOrdinal(ordinalTracker);
                    break outer;
                }
            }

        }

    }

    private boolean isGrouped(ItemFormMetadataBean itemFormBean, List<DisplayItemGroupBean> displayFormBeans) {
        boolean grouped = false;
        outer: for (DisplayItemGroupBean digBean : displayFormBeans) {
            for (DisplayItemBean diBean : digBean.getItems()) {
                if (itemFormBean.getItemId() == diBean.getItem().getId()) {
                    grouped = true;
                    break outer;
                }
            }

        }
        return grouped;
    }

    /**
     * This method returns the ordinal of the DisplayItemGroupBean that contains
     * ItemFormMetadataBean passed in as the parameter. It is a utility method
     * used for finding out the group table ordinal associated with a particular
     * item.
     * 
     * @param itemFBean
     *            An ItemFormMetadataBean.
     * @param displayFormBeans
     *            An ArrayList of DisplayItemGroupBeans associated with a
     *            particular CRF.
     * @return The ordinal of the DisplayItemGroupBean that contains
     *         ItemFormMetadataBean passed in as the parameter; an int.
     */
    private int getGroupOrdinal(ItemFormMetadataBean itemFBean, List<DisplayItemGroupBean> displayFormBeans) {
        int ordinal = 0;
        outer: for (DisplayItemGroupBean digBean : displayFormBeans) {
            for (DisplayItemBean diBean : digBean.getItems()) {
                if (itemFBean.getItemId() == diBean.getItem().getId()) {
                    ordinal = digBean.getOrdinal();
                    break outer;
                }
            }

        }
        return ordinal;

    }

    /**
     * Create a DisplaySectionBean with a list of FormGroupBeans.
     * 
     * @param crfVersionId
     *            The CRF version ID associated with the Items.
     * @param sectionBean
     *            The SectionBean with an ID associated with the Items, which
     *            end up providing the content of the tables.
     * @param sm
     *            A SessionManager, from which DataSources are acquired for the
     *            DAO objects.
     * @return A DisplaySectionBean.
     */
    public DisplaySectionBean createDisplaySectionBeanWithItemGroups(int sectionId, EventCRFBean eventCrfBean, SectionBean sectionBean, SessionManager sm,
            ServletContext context) {
        DisplaySectionBean dBean = new DisplaySectionBean();
        ItemGroupDAO formGroupDAO = new ItemGroupDAO(sm.getDataSource());
        ItemGroupMetadataDAO igMetaDAO = new ItemGroupMetadataDAO(sm.getDataSource());
        ItemDAO itemDao = new ItemDAO(sm.getDataSource());
        // Get all items associated with this crfVersion ID; divide them up into
        // items with a group, and those without a group.
        // get all items associated with a section id, then split them up into
        // grouped
        // and non-grouped items
        List<ItemBean> allItems = itemDao.findAllParentsBySectionId(sectionId);
        // Get a List of FormGroupBeans for each group associated with
        // this crfVersionId.
        // List<ItemGroupBean> arrList =
        // formGroupDAO.findGroupByCRFVersionIDMap(crfVersionId);
        List<ItemGroupBean> arrList = formGroupDAO.findLegitGroupBySectionId(sectionId);

        if (arrList.isEmpty())
            return dBean;

        // Get the items associated with each group
        List<ItemBean> itBeans;
        List<DisplayItemBean> displayItems;
        List<DisplayItemGroupBean> displayFormBeans = new ArrayList<DisplayItemGroupBean>();
        DisplayItemGroupBean displayFormGBean;
        for (ItemGroupBean itemGroup : arrList) {
            itBeans = itemDao.findAllItemsByGroupId(itemGroup.getId(), eventCrfBean.getCRFVersionId());

            List<ItemGroupMetadataBean> metadata = igMetaDAO.findMetaByGroupAndSection(itemGroup.getId(), eventCrfBean.getCRFVersionId(), sectionId);
            // Create DisplayItemBeans out of the found items; we have to
            // further
            // delineate the items by section label. The following method
            // includes
            // this requirement
            if (!metadata.isEmpty()) {
                // for a given crf version, all the items in the same group have
                // the same group metadata
                // so we can get one of them and set metadata for the group
                ItemGroupMetadataBean meta = metadata.get(0);
                itemGroup.setMeta(meta);
            }
            // TODO: the last arg is a list of null value strings
            displayItems = getDisplayBeansFromItems(itBeans, sm.getDataSource(), eventCrfBean, sectionBean.getId(), null, context);
            displayFormGBean = this.createDisplayFormGroup(displayItems, itemGroup);
            displayFormBeans.add(displayFormGBean);
        }
        // sort the list according to the ordinal of the contained
        // FormGroupBeans
        Collections.sort(displayFormBeans, new Comparator<DisplayItemGroupBean>() {

            public int compare(DisplayItemGroupBean disFormGroupBean, DisplayItemGroupBean disFormGroupBean1) {
                return disFormGroupBean.getGroupMetaBean().getOrdinal().compareTo(disFormGroupBean1.getGroupMetaBean().getOrdinal());
            }
        });
        dBean.setDisplayFormGroups(displayFormBeans);
        return dBean;
    }

    /**
     * Create a DisplaySectionBean with a list of ItemGroupBeans. NOTE:
     * unGrouped Items are not included
     * 
     * @param study
     *            The StudyBean
     * @param sectionId
     *            The Section ID associated with the Items, which end up
     *            providing the content of the tables.
     * @param crfVersionId
     *            The CRF version ID associated with the Items.
     * @param studyEventId
     *            The Study Event ID associated with the CRF Version ID.
     * @param sm
     *            A SessionManager, from which DataSources are acquired for the
     *            DAO objects.
     * @return A DisplaySectionBean.
     */
    public DisplaySectionBean createDisplaySectionWithItemGroups(StudyBean study, int sectionId, EventCRFBean eventCrfBean, int studyEventId,
            SessionManager sm, int eventDefinitionCRFId, ServletContext context) {

        DisplaySectionBean dBean = new DisplaySectionBean();
        ItemGroupDAO formGroupDAO = new ItemGroupDAO(sm.getDataSource());
        ItemGroupMetadataDAO igMetaDAO = new ItemGroupMetadataDAO(sm.getDataSource());
        ItemDAO itemDao = new ItemDAO(sm.getDataSource());
        ItemFormMetadataDAO metaDao = new ItemFormMetadataDAO(sm.getDataSource());

        List<ItemGroupBean> arrList = formGroupDAO.findLegitGroupBySectionId(sectionId);
        // all items associated with the section, including those not in a group
        List<ItemFormMetadataBean> allMetas = new ArrayList<ItemFormMetadataBean>();
        try {
            allMetas = metaDao.findAllBySectionId(sectionId);
        } catch (OpenClinicaException oce) {
            logger.info("oce.getOpenClinicaMessage() = " + oce.getOpenClinicaMessage());
        }
        // Sort these items according to their position on the CRF
       // Collections.sort(allMetas);
        if (arrList.isEmpty())
            return dBean;

        // Find out whether there are any checkboxes/radios/select elements
        // and if so, get any null values
        // associated with them
        List<String> nullValuesList = new ArrayList<String>();
        boolean itemsHaveChecksRadios = itemsIncludeChecksRadiosSelects(allMetas);
        // tbh>>
        // logger.info("line 923, found event def crf id
        // "+eventDefinitionCRFId);
        if (eventDefinitionCRFId <= 0) {
            EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(sm.getDataSource());
            EventDefinitionCRFBean edcBean = edcdao.findByStudyEventIdAndCRFVersionId(study, studyEventId, eventCrfBean.getCRFVersionId());
            eventDefinitionCRFId = edcBean.getId();
        }
        // tbh, 112007, added above because the id was not passed properly for
        // DDE
        // if the id is zero, horizontal result sets do not display null values
        // logger.info("line 933, found event def crf id
        // "+eventDefinitionCRFId);
        if (itemsHaveChecksRadios && eventDefinitionCRFId > 0) {
            // method returns null values as a List<String>
            nullValuesList = this.getNullValuesByEventCRFDefId(eventDefinitionCRFId, sm.getDataSource());
        }
        // Get the items associated with each group
        List<ItemBean> itBeans;
        List<DisplayItemBean> displayItems;
        List<DisplayItemGroupBean> displayFormBeans = new ArrayList<DisplayItemGroupBean>();
        DisplayItemGroupBean displayItemGBean;
        for (ItemGroupBean itemGroup : arrList) {
            itBeans = itemDao.findAllItemsByGroupId(itemGroup.getId(), eventCrfBean.getCRFVersionId());

            List<ItemGroupMetadataBean> metadata = igMetaDAO.findMetaByGroupAndSection(itemGroup.getId(), eventCrfBean.getCRFVersionId(), sectionId);
            if (!metadata.isEmpty()) {
                // for a given crf version, all the items in the same group
                // have the same group metadata
                // so we can get one of them and set metadata for the group
                ItemGroupMetadataBean meta = metadata.get(0);
                itemGroup.setMeta(meta);
            }
            // include arrayList parameter until I determine difference in
            // classes
            displayItems = getDisplayBeansFromItems(itBeans, sm.getDataSource(), eventCrfBean, sectionId, nullValuesList, context);
            displayItemGBean = this.createDisplayFormGroup(displayItems, itemGroup);
            displayFormBeans.add(displayItemGBean);
        }
        // We still have to sort these display item group beans on their
        // ItemGroupMetadataBean?
        // then number their ordinals accordingly
        Collections.sort(displayFormBeans, new Comparator<DisplayItemGroupBean>() {

            public int compare(DisplayItemGroupBean displayItemGroupBean, DisplayItemGroupBean displayItemGroupBean1) {
                return displayItemGroupBean.getGroupMetaBean().compareTo(displayItemGroupBean1.getGroupMetaBean());
            }
        });
        // Now provide the display item group beans with an ordinal
        int digOrdinal = 0;
        for (DisplayItemGroupBean digBean : displayFormBeans) {
            digBean.setOrdinal(++digOrdinal);
        }

        dBean.setDisplayFormGroups(displayFormBeans);

        return dBean;
    }

    public boolean sectionHasUngroupedItems(DataSource dataSource, int sectionId, List<DisplayItemGroupBean> displayFormBeans) {
        ItemFormMetadataDAO metaDao = new ItemFormMetadataDAO(dataSource);
        List<ItemFormMetadataBean> allMetas = new ArrayList<ItemFormMetadataBean>();
        try {
            allMetas = metaDao.findAllBySectionId(sectionId);
        } catch (OpenClinicaException oce) {
            logger.info("oce.getOpenClinicaMessage() = " + oce.getOpenClinicaMessage());
        }
        int size = allMetas.size();
        int tempCount = 0;
        String grpName = "";
        // Only count grouped items
        for (DisplayItemGroupBean groupBean : displayFormBeans) {
            grpName = groupBean.getItemGroupBean().getName();
            if (!(grpName.equalsIgnoreCase("Ungrouped") || grpName.length() < 1)) {
                tempCount += groupBean.getItems().size();
            }
        }

        return tempCount < size;
    }

    public List<String> getNullValuesByEventCRFDefId(int eventDefinitionCRFId, DataSource dataSource) {
        if (eventDefinitionCRFId < 1 || dataSource == null) {
            return new ArrayList<String>();
        }
        List<String> nullValuesList = new ArrayList<String>();
        // hold the bean's return value
        List<NullValue> nullObjectList = new ArrayList<NullValue>();
        EventDefinitionCRFBean eventCRFDefBean;
        EventDefinitionCRFDAO eventDefinitionCRFDAO = new EventDefinitionCRFDAO(dataSource);
        eventCRFDefBean = (EventDefinitionCRFBean) eventDefinitionCRFDAO.findByPK(eventDefinitionCRFId);
        nullObjectList = eventCRFDefBean.getNullValuesList();
        if (nullObjectList == null) {
            return new ArrayList<String>();
        }

        for (NullValue nullVal : nullObjectList) {
            nullValuesList.add(nullVal.getName());
        }
        return nullValuesList;

    }

    private boolean itemsIncludeChecksRadiosSelects(List<ItemFormMetadataBean> metaBeans) {
        String responseName;
        for (ItemFormMetadataBean fbean : metaBeans) {
            responseName = fbean.getResponseSet().getResponseType().getName();
            if (responseName == null || responseName.length() < 1) {
                return false;
            }
            if (responseName.equalsIgnoreCase("checkbox") || responseName.equalsIgnoreCase("radio") || responseName.equalsIgnoreCase("single-select")
                || responseName.equalsIgnoreCase("multi-select")) {
                return true;
            }
        }
        return false;
    }

    public void addNullValuesToDisplayItemWithGroupBeans(List<DisplayItemWithGroupBean> groupBeans, List<String> nullValuesList) {

        if (nullValuesList == null || nullValuesList.isEmpty() || groupBeans == null || groupBeans.isEmpty()) {
            return;
        }
        DisplayItemGroupBean displayItemGroupBean = null;
        List<DisplayItemBean> disBeans = new ArrayList<DisplayItemBean>();
        String responseName = "";
        List<ResponseOptionBean> respOptions;

        for (DisplayItemWithGroupBean withGroupBean : groupBeans) {
            displayItemGroupBean = withGroupBean.getItemGroup();
            disBeans = displayItemGroupBean.getItems();
            for (DisplayItemBean singleBean : disBeans) {
                responseName = singleBean.getMetadata().getResponseSet().getResponseType().getName();
                respOptions = singleBean.getMetadata().getResponseSet().getOptions();
                if (respOptions != null
                    && ("checkbox".equalsIgnoreCase(responseName) || "radio".equalsIgnoreCase(responseName) || "single-select".equalsIgnoreCase(responseName) || "multi-select"
                            .equalsIgnoreCase(responseName))) {
                    this.addBeansToResponseOptions(nullValuesList, respOptions);
                }
            }
        }
    }
}
