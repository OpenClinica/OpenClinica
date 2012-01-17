package org.akaza.openclinica.view.form;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.DisplayItemBean;
import org.akaza.openclinica.bean.submit.DisplayItemGroupBean;
import org.akaza.openclinica.bean.submit.DisplaySectionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ResponseOptionBean;
import org.akaza.openclinica.bean.submit.ResponseSetBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.control.managestudy.BeanFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This class is used to generate the markup for a group-type printable table.
 * An array or List of DisplaySectionBeans is sequentially converted into XML to
 * create a Web page representing all of the sections of a CRF. If needed, the
 * code will reproduce rows that are saved in the database. The markup, a large
 * String generated from a JDOM Document object (HTML, basically) is then output
 * to a Web browser by a custom JSP tag.
 */
public class PrintHorizontalFormBuilder extends DefaultFormBuilder {
    private int maxColRow = 4;
    // The sections that make up the print version of the form
    private List<DisplaySectionBean> displaySectionBeans = new ArrayList<DisplaySectionBean>();

    private StudyBean studyBean;
    private EventCRFBean eventCRFbean;
    // Does the HTTP request originate from a print data entry servlet?
    private boolean involvesDataEntry;
    // Have the form values already been saved during initial or double data
    // entry?
    private boolean hasDbFormValues;
    // Alter print view for Internet Explorer browsers
    private boolean isInternetExplorer=false;
    // Does the print view have to be reconfigured for IE browsers?
    private boolean reconfigureView;

    private SectionBean sectionBean;

    public PrintHorizontalFormBuilder() {
    }

    public List<DisplaySectionBean> getDisplaySectionBeans() {
        return displaySectionBeans;
    }

    public void setDisplaySectionBeans(List<DisplaySectionBean> displaySectionBeans) {
        this.displaySectionBeans = displaySectionBeans;
    }

  
    
    @Override
    public String createMarkup() {
        // If the CRF has
        // data associated with it, pass on the responsibility to another object
        ViewPersistanceHandler persistanceHandler = new ViewPersistanceHandler();

        ViewBuilderPrintDecorator builderUtil = new ViewBuilderPrintDecorator();
        // This object holds the printed output of the JDom Document object,
        // which represents
        // the XML for each section's HTML tables. The object is re-written
        // each time a new section is generated
        Writer writer = new StringWriter();
        // This object contains all of the markup for all of the sections.
        StringBuilder webPageBuilder = new StringBuilder();

        // Keep track of the section number so we can create page numbers
        int pageNumber = 0;

        if (isInternetExplorer) {
            for (DisplaySectionBean displaySecBean : this.displaySectionBeans) {

                this.reconfigureView = builderUtil.hasThreePlusColumns(displaySecBean);
                // Now the application knows that the view for at least one
                // section has to be
                // reformulated for IE
                if (reconfigureView)
                    break;
            }
        }
        int uniqueId = 0;
        // Print all the sections of a group-type table
        for (DisplaySectionBean displaySecBean : this.displaySectionBeans) {

            // The CellFactoryPrintDecorator object that generates the content
            // for HTML table TD cells.
            CellFactoryPrintDecorator cellFactory = new CellFactoryPrintDecorator();

            // The object that handles the repetition model attributes for the
            // HTML table elements
            RepeatManager repeatManager = new RepeatManager();

            // These classes "decorate" the FormBeanUtil and ViewBuilderUtil
            // classes to
            // provide special services required in printing
            FormBeanUtilDecorator formUtilDecorator = new FormBeanUtilDecorator();

            // Does this particular section have to be reconfigured for printing
            // in IE browsers?
            boolean changeHTMLForIE = false;
            if (reconfigureView) {
//                changeHTMLForIE = builderUtil.hasThreePlusColumns(displaySecBean);
            }

            // We have to change the Section's ItemGroupBeans if the Section has
            // group tables exceeding three columns, in terms of printing for IE
            // browsers.
            // Any ItemGroupBean specifically has to be reduced to one column,
            // if it exceeds
            // three columns; changeHTMLForIE is 'true' if this section has any
            // group tables
            // that are 3+ columns
//            if (changeHTMLForIE) {
//                List<DisplayItemGroupBean> newGroupBeans = builderUtil.reduceColumnsGroupTables(displaySecBean.getDisplayFormGroups());
//
//                // Now set the display section beans groups to the reshuffled
//                // list
//                displaySecBean.setDisplayFormGroups(newGroupBeans);
//            }

            // increment the page number
            ++pageNumber;
            // The SectionBean associated with this section
            sectionBean = displaySecBean.getSection();

            if (involvesDataEntry) {
                List<ItemDataBean> itemDataBeans;
                persistanceHandler = new ViewPersistanceHandler();
                itemDataBeans = persistanceHandler.fetchPersistedData(sectionBean.getId(), eventCRFbean.getId());

                if (!itemDataBeans.isEmpty()) {
                    hasDbFormValues = true;
                }
                persistanceHandler.setItemDataBeans(itemDataBeans);
            }
            // Keep track of whether a group has any repeat behavior; true or
            // false
            boolean repeatFlag;

            // The number of repeating table rows that the group will start
            // with.
            int repeatNumber;

            // the div tag that will be the root node for each printable section
            Element divRoot = new Element("div");
            divRoot.setAttribute("id", ("toplevel" + pageNumber));
            divRoot.setAttribute("class", "toplevel");

            // remove float properties for IE browsers
            if (isInternetExplorer) {
                divRoot.setAttribute("style", "float:none");
            }
            Document doc = new Document(divRoot);
            // Show the section's title, subtitle, or instructions
            builderUtil.showTitles(divRoot, sectionBean, pageNumber, isInternetExplorer);
            // One way to generate an id for the repeating tbody or tr element

            // The tabindex attribute for select and input tags
            int tabindex = 1;

            // Should discrepancy note icons be displayed
            boolean hasDiscrepancyMgt = false;
            StudyBean studBean = this.getStudyBean();
            if (studBean != null && studBean.getStudyParameterConfig().getDiscrepancyManagement().equalsIgnoreCase("true")) {

                hasDiscrepancyMgt = true;
            }
            // Create a table for every DisplayItemGroupBean
            // A DisplayItemGroupBean contains an ItemGroupBean and
            // its list of DisplayItemBeans
            for (DisplayItemGroupBean displayItemGroup : displaySecBean.getDisplayFormGroups()) {
                ArrayList headerlist = new ArrayList();
                ArrayList bodylist = new ArrayList();
                ArrayList subHeadList = new ArrayList();

                List<DisplayItemBean> currentDisplayItems = displayItemGroup.getItems();
                // A Map that contains persistent (stored in a database),
                // repeated rows
                // in a matrix type table
                // The Map index is the Item id of the first member of the row;
                // the value is a List
                // of item beans that make up the row
                SortedMap<Integer, List<ItemDataBean>> ordinalItemDataMap = new TreeMap<Integer, List<ItemDataBean>>();
                // Is this a persistent matrix table and does it already have
                // repeated rows
                // in the database?
                boolean hasStoredRepeatedRows = false;

                // Is this a non-group type table that shares the same section
                // as a group table?
                boolean unGroupedTable = displayItemGroup.getItemGroupBean().getName().equalsIgnoreCase(BeanFactory.UNGROUPED);

                // Load any database values into the DisplayItemBeans
                if (hasDbFormValues) {
                    currentDisplayItems = persistanceHandler.loadDataIntoDisplayBeans(currentDisplayItems, (!unGroupedTable));
                    /*
                     * The highest number ordinal represents how many repeated
                     * rows there are. If the ordinal in ItemDataBeans > 1, then
                     * we know that the group has persistent repeated rows. Get
                     * a structure that maps each ordinal (i.e., >= 2) to its
                     * corresponding List of ItemDataBeans. Then iterate the
                     * existing DisplayBeans, with the number of new rows
                     * equaling the highest ordinal number minus 1 (meaning, the
                     * first row represents the row of the group table that
                     * would exist if the user displayed the table, but didn't
                     * generate any new rows). For example, in a List of
                     * ItemDataBeans, if the highest ordinal property among
                     * these beans is 5, then the matrix table has 4 repeated
                     * rows from the database. Provide each new row with its
                     * values by using the ItemDataBeans.
                     */
                    if (involvesDataEntry && !unGroupedTable && persistanceHandler.hasPersistentRepeatedRows(currentDisplayItems)) {
                        hasStoredRepeatedRows = true;
                        // if the displayitems contain duplicate item ids, then
                        // these duplicates
                        // represent repeated rows. Separate them into a Map of
                        // new rows that
                        // will be appended to the HTML table.
                        ordinalItemDataMap = persistanceHandler.handleExtraGroupRows();
                    }
                }// end if hasDbFormValues

                // Does the table have a group header?
                String groupHeader = displayItemGroup.getGroupMetaBean().getHeader();
                boolean hasGroupHeader = groupHeader != null && groupHeader.length() > 0;

                // Add group header, if there is one
                if (hasGroupHeader) {
                    Element divGroupHeader = new Element("div");
                    // necessary?
                    divGroupHeader.setAttribute("class", "aka_group_header");
                    Element strong = new Element("strong");
                    strong.setAttribute("style", "float:none");
                    strong.addContent(groupHeader);
                    divGroupHeader.addContent(strong);
                    divRoot.addContent(divGroupHeader);
                }
                Element tableDiv = new Element("div");
                tableDiv.setAttribute("class", "tableDiv");
                if (isInternetExplorer) {
                    tableDiv.setAttribute("style", "float:none");
                }
                divRoot.addContent(tableDiv);

                // This group represents "orphaned" items (those without a
                // group) if
                // the FormGroupBean has a group label of UNGROUPED
                Element orphanTable = null;
                if (unGroupedTable) {
                    orphanTable = formUtilDecorator.createXHTMLTableFromNonGroup(currentDisplayItems, tabindex, hasDiscrepancyMgt, hasDbFormValues, true);
                    // We have to track the point the tabindex has reached here
                    // The tabindex will increment by the size of the
                    // displayItemGroup List
                    tabindex += currentDisplayItems.size();

                    tableDiv.addContent(orphanTable);

                    continue;
                }// end if unGroupedTable

                uniqueId++;
                String repeatParentId = "repeatParent" + uniqueId;
                repeatNumber = displayItemGroup.getGroupMetaBean().getRepeatNum();
                // If the form has repeat behavior, this number is > 0
                // Do not allow repeat numbers < 1
                repeatNumber = repeatNumber < 1 ? 1 : repeatNumber;
                // And a limit of 12
                repeatNumber = repeatNumber > 12 ? 12 : repeatNumber;
                // This is always true during this iteration
                repeatFlag = true;
                Element table = createTable();

                // add the thead element
                Element thead = new Element("tr");
                tableDiv.addContent(table);
//                table.addContent(thead);
                // Does this group involve a Horizontal checkbox or radio
                // button?
                boolean hasResponseLayout = builderUtil.hasResponseLayout(currentDisplayItems);
                // add th elements to the thead element
                // We have to create an extra thead column for the Remove Row
                // button, if
                // the table involves repeating rows; thus the final boolean
                // parameter
                List<Element> thTags =
                    repeatFlag ? createTheadContentsFromDisplayItems(currentDisplayItems, true) : createTheadContentsFromDisplayItems(currentDisplayItems,
                            false);
                int i = 0;
                for (Element el : thTags) {
                    i++;
                    thead.addContent(el);
                    if(i%maxColRow == 0) {
                        headerlist.add(thead);
                        thead = new Element("tr");
                    } 
                }

                if(i%maxColRow!=0)headerlist.add(thead);
                
                // Make sure the layout for "horizontal" checkboxes or radios is
                // displayed
                // in this manner.
                if (hasResponseLayout) {
                    addResponseLayoutRow(subHeadList, currentDisplayItems);
                }


                Element row;
                Element td;
                // For each row in the table
                row = new Element("tr");
                // If the group has repeat behavior and repeats row by row,
                // then the
                // repetition model type attributes have to be added to the tr
                // tag
                if (repeatFlag && !(involvesDataEntry && hasStoredRepeatedRows)) {
                    table = repeatManager.addParentRepeatAttributes(table, repeatParentId, repeatNumber, displayItemGroup.getGroupMetaBean().getRepeatMax());
                }
                // The content for the table cells. For each item...
                int j = 0;
                for (DisplayItemBean displayBean : currentDisplayItems) {
                    j++;
                    // What type of input: text, radio, checkbox, etc.?
                    String responseName = displayBean.getMetadata().getResponseSet().getResponseType().getName();
                    // We have to create cells in a different way if the input
                    // is radio or checkbox, and the response_layout is
                    // horizontal
                    if (displayBean.getMetadata().getResponseLayout().equalsIgnoreCase("horizontal")
                        && (responseName.equalsIgnoreCase("checkbox") || responseName.equalsIgnoreCase("radio"))) {
                        // The final true parameter is for disabling D Notes
                        Element[] elements =
                            cellFactory.createCellContentsForChecks(responseName, displayBean, displayBean.getMetadata().getResponseSet().getOptions().size(),
                                    ++tabindex, false, true);
                        for (Element el : elements) {
                            el = builderUtil.setClassNames(el);
                            if (repeatFlag) {
//                                el = repeatManager.addChildRepeatAttributes(el, repeatParentId, displayBean.getItem().getId(), null);
                            }
                            row.addContent(el);
                        }
                        // move to the next item
                        continue;
                    }
                    td = new Element("td");
                    td = builderUtil.setClassNames(td);
                    // Create cells within each row
                    td = cellFactory.createCellContents(td, responseName, displayBean, ++tabindex, hasDiscrepancyMgt, hasDbFormValues, true);
                    if (repeatFlag) {
                    }
                    row.addContent(td);
                    if(j%maxColRow==0){
                        bodylist.add(row);
                        row = new Element("tr");
                        if (repeatFlag) {
                            repeatParentId = repeatParentId+uniqueId++;
                        }
                    } 
                }// end for displayBean
                if(j%maxColRow!=0)bodylist.add(row);
                //Creating the first/main table
                if((repeatNumber > 1)||hasStoredRepeatedRows){
                    Element newRow = new Element("tr");
                    Element div = new Element("div");
                    div.setAttribute("id", "repeatCaption");
                    Element newCol = new Element("td");
                    Element strong = new Element("strong");
                    strong.addContent("Repeat: 1");
                    div.addContent(strong);
                    newCol.addContent(div);
                    newRow.addContent(newCol);
                    table.addContent(newRow);
                }
                for(int k=0; k<headerlist.size();k++){
                    Element head = (Element)headerlist.get(k);
                    Element body = (Element)bodylist.get(k);
                    table.addContent(head);
                    if(subHeadList.size()>0){
                        try{
                            Element subHead = (Element)subHeadList.get(k);
                            table.addContent(subHead);
                        }catch (IndexOutOfBoundsException IOB){
                        }
                    }
                    table.addContent(body);
                }
                
                // The final true parameter is for disabling D Note icons from
                // being clicked
                if (hasStoredRepeatedRows) {
                    List storedRepeatedRows =
                        builderUtil.generatePersistentMatrixRows(ordinalItemDataMap, currentDisplayItems, tabindex, repeatParentId, hasDiscrepancyMgt, true, maxColRow);
                    // add these new rows to the table
                    int count = 1;
                    for(int l = 0; l<storedRepeatedRows.size();l++){
                        ++count;
                        List<Element> rowsList = (ArrayList)storedRepeatedRows.get(l);
                        divRoot.addContent(createTableWithData(rowsList, headerlist, subHeadList, count));
                    }
                }
            }// end for displayFormGroup
            XMLOutputter outp = new XMLOutputter();
            Format format = Format.getPrettyFormat();
            format.setOmitDeclaration(true);
            outp.setFormat(format);
            // The writer object contains the markup for one printable section
            writer = new StringWriter();
            try {
                outp.output(doc, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // The webPageBuilder object contains the markup for all of the
            // sections
            // in the print view
            webPageBuilder.append(writer.toString());
        }
        return webPageBuilder.toString();
    }
    
    
    
    
    
    
    
    /**
     * Sequentially create a String of XML representing all of the sections on a
     * Case Report Form, for the purpose of web-page display.
     *
     * @return A string representing the XML or XHTML.
     */

    public String createMarkupNoDE() {
        // data associated with it, pass on the responsibility to another object
        ViewPersistanceHandler persistanceHandler = new ViewPersistanceHandler();

        ViewBuilderPrintDecorator builderUtil = new ViewBuilderPrintDecorator();
        // This object holds the printed output of the JDom Document object,
        // which represents
        // the XML for each section's HTML tables. The object is re-written
        // each time a new section is generated
        Writer writer = new StringWriter();
        // This object contains all of the markup for all of the sections.
        StringBuilder webPageBuilder = new StringBuilder();

        // Keep track of the section number so we can create page numbers
        int pageNumber = 0;

        if (isInternetExplorer) {
            for (DisplaySectionBean displaySecBean : this.displaySectionBeans) {

                this.reconfigureView = builderUtil.hasThreePlusColumns(displaySecBean);
                // Now the application knows that the view for at least one
                // section has to be
                // reformulated for IE
                if (reconfigureView)
                    break;
            }
        }
        int uniqueId = 0;
        // Print all the sections of a group-type table
        for (DisplaySectionBean displaySecBean : this.displaySectionBeans) {

            // The CellFactoryPrintDecorator object that generates the content
            // for HTML table TD cells.
            CellFactoryPrintDecorator cellFactory = new CellFactoryPrintDecorator();

            // The object that handles the repetition model attributes for the
            // HTML table elements
            RepeatManager repeatManager = new RepeatManager();

            // These classes "decorate" the FormBeanUtil and ViewBuilderUtil
            // classes to
            // provide special services required in printing
            FormBeanUtilDecorator formUtilDecorator = new FormBeanUtilDecorator();

            // Does this particular section have to be reconfigured for printing
            // in IE browsers?
            boolean changeHTMLForIE = false;
            if (reconfigureView) {
//                changeHTMLForIE = builderUtil.hasThreePlusColumns(displaySecBean);
            }

  
            ++pageNumber;
        
            sectionBean = displaySecBean.getSection();

            if (involvesDataEntry) {
                List<ItemDataBean> itemDataBeans;
                persistanceHandler = new ViewPersistanceHandler();
                itemDataBeans = persistanceHandler.fetchPersistedData(sectionBean.getId(), eventCRFbean.getId());

                if (!itemDataBeans.isEmpty()) {
                    hasDbFormValues = true;
                }
                persistanceHandler.setItemDataBeans(itemDataBeans);
            }
            // Keep track of whether a group has any repeat behavior; true or
            // false
            boolean repeatFlag;

            // The number of repeating table rows that the group will start
            // with.
            int repeatNumber;

            // the div tag that will be the root node for each printable section
            Element divRoot = new Element("div");
            divRoot.setAttribute("id", ("toplevel" + pageNumber));
            divRoot.setAttribute("class", "toplevel");

            // remove float properties for IE browsers
            if (isInternetExplorer) {
                divRoot.setAttribute("style", "float:none");
            }
            Document doc = new Document(divRoot);
            // Show the section's title, subtitle, or instructions
            builderUtil.showTitles(divRoot, sectionBean, pageNumber, isInternetExplorer);
            // One way to generate an id for the repeating tbody or tr element

            // The tabindex attribute for select and input tags
            int tabindex = 1;

            // Should discrepancy note icons be displayed
            boolean hasDiscrepancyMgt = false;
            StudyBean studBean = this.getStudyBean();
            if (studBean != null && studBean.getStudyParameterConfig().getDiscrepancyManagement().equalsIgnoreCase("true")) {

                hasDiscrepancyMgt = true;
            }
            //Not to show discrepancy flags in the print crfs when there is no data
            hasDiscrepancyMgt=false;
            // Create a table for every DisplayItemGroupBean
            // A DisplayItemGroupBean contains an ItemGroupBean and
            // its list of DisplayItemBeans
            for (DisplayItemGroupBean displayItemGroup : displaySecBean.getDisplayFormGroups()) {
                ArrayList headerlist = new ArrayList();
                ArrayList bodylist = new ArrayList();
                ArrayList subHeadList = new ArrayList();

                List<DisplayItemBean> currentDisplayItems = displayItemGroup.getItems();
                // A Map that contains persistent (stored in a database),
                // repeated rows
                // in a matrix type table
                // The Map index is the Item id of the first member of the row;
                // the value is a List
                // of item beans that make up the row
                SortedMap<Integer, List<ItemDataBean>> ordinalItemDataMap = new TreeMap<Integer, List<ItemDataBean>>();
                // Is this a persistent matrix table and does it already have
                // repeated rows
                // in the database?
                boolean hasStoredRepeatedRows = false;

                // Is this a non-group type table that shares the same section
                // as a group table?
               // boolean unGroupedTable = displayItemGroup.getItemGroupBean().getName().equalsIgnoreCase(BeanFactory.UNGROUPED);
                boolean unGroupedTable = displayItemGroup.getGroupMetaBean().isRepeatingGroup()?false:true;
                // Load any database values into the DisplayItemBeans
                if (hasDbFormValues) {
                    currentDisplayItems = persistanceHandler.loadDataIntoDisplayBeans(currentDisplayItems, (!unGroupedTable));
                    /*
                     * The highest number ordinal represents how many repeated
                     * rows there are. If the ordinal in ItemDataBeans > 1, then
                     * we know that the group has persistent repeated rows. Get
                     * a structure that maps each ordinal (i.e., >= 2) to its
                     * corresponding List of ItemDataBeans. Then iterate the
                     * existing DisplayBeans, with the number of new rows
                     * equaling the highest ordinal number minus 1 (meaning, the
                     * first row represents the row of the group table that
                     * would exist if the user displayed the table, but didn't
                     * generate any new rows). For example, in a List of
                     * ItemDataBeans, if the highest ordinal property among
                     * these beans is 5, then the matrix table has 4 repeated
                     * rows from the database. Provide each new row with its
                     * values by using the ItemDataBeans.
                     */
                    if (involvesDataEntry && !unGroupedTable && persistanceHandler.hasPersistentRepeatedRows(currentDisplayItems)) {
                        hasStoredRepeatedRows = true;
                        // if the displayitems contain duplicate item ids, then
                        // these duplicates
                        // represent repeated rows. Separate them into a Map of
                        // new rows that
                        // will be appended to the HTML table.
                        ordinalItemDataMap = persistanceHandler.handleExtraGroupRows();
                    }
                }// end if hasDbFormValues

                // Does the table have a group header?
                String groupHeader = displayItemGroup.getGroupMetaBean().getHeader();
                boolean hasGroupHeader = groupHeader != null && groupHeader.length() > 0;

                // Add group header, if there is one
                if (hasGroupHeader) {
                    Element divGroupHeader = new Element("div");
                    // necessary?
                    divGroupHeader.setAttribute("class", "aka_group_header");
                    Element strong = new Element("strong");
                    strong.setAttribute("style", "float:none");
                    strong.addContent(groupHeader);
                    divGroupHeader.addContent(strong);
                    divRoot.addContent(divGroupHeader);
                }
                Element tableDiv = new Element("div");
                tableDiv.setAttribute("class", "tableDiv");
                if (isInternetExplorer) {
                    tableDiv.setAttribute("style", "float:none");
                }
                divRoot.addContent(tableDiv);

                // This group represents "orphaned" items (those without a
                // group) if
                // the FormGroupBean has a group label of UNGROUPED
                Element orphanTable = null;
                if (unGroupedTable) {
                    orphanTable = formUtilDecorator.createXHTMLTableFromNonGroup(currentDisplayItems, tabindex, hasDiscrepancyMgt, hasDbFormValues, true);
                    // We have to track the point the tabindex has reached here
                    // The tabindex will increment by the size of the
                    // displayItemGroup List
                    tabindex += currentDisplayItems.size();

                    tableDiv.addContent(orphanTable);

                    continue;
                }// end if unGroupedTable

                uniqueId++;
                String repeatParentId = "repeatParent" + uniqueId;
                repeatNumber = displayItemGroup.getGroupMetaBean().getRepeatNum();
                // If the form has repeat behavior, this number is > 0
                // Do not allow repeat numbers < 1
                repeatNumber = repeatNumber < 1 ? 1 : repeatNumber;
                // And a limit of 12
                repeatNumber = repeatNumber > 12 ? 12 : repeatNumber;
                // This is always true during this iteration
                repeatFlag = true;
                Element table = createTable();

                // add the thead element
                Element thead = new Element("tr");
                tableDiv.addContent(table);
//                table.addContent(thead);
                // Does this group involve a Horizontal checkbox or radio
                // button?
                boolean hasResponseLayout = builderUtil.hasResponseLayout(currentDisplayItems);
                // add th elements to the thead element
                // We have to create an extra thead column for the Remove Row
                // button, if
                // the table involves repeating rows; thus the final boolean
                // parameter
                List<Element> thTags =
                    repeatFlag ? createTheadContentsFromDisplayItems(currentDisplayItems, true) : createTheadContentsFromDisplayItems(currentDisplayItems,
                            false);
                int i = 0;
                for (Element el : thTags) {
                    i++;
                    thead.addContent(el);
                    if(i%maxColRow == 0) {
                        headerlist.add(thead);
                        thead = new Element("tr");
                    } 
                }

                if(i%maxColRow!=0)headerlist.add(thead);
                
                // Make sure the layout for "horizontal" checkboxes or radios is
                // displayed
                // in this manner.
                if (hasResponseLayout) {
                    addResponseLayoutRow(subHeadList, currentDisplayItems);
                }


                Element row;
                Element td;
                // For each row in the table
                row = new Element("tr");
                // If the group has repeat behavior and repeats row by row,
                // then the
                // repetition model type attributes have to be added to the tr
                // tag
                if (repeatFlag && !(involvesDataEntry && hasStoredRepeatedRows)) {
                    table = repeatManager.addParentRepeatAttributes(table, repeatParentId, repeatNumber, displayItemGroup.getGroupMetaBean().getRepeatMax());
                }
                // The content for the table cells. For each item...
                int j = 0;
                for (DisplayItemBean displayBean : currentDisplayItems) {
                    j++;
                    // What type of input: text, radio, checkbox, etc.?
                    String responseName = displayBean.getMetadata().getResponseSet().getResponseType().getName();
                    // We have to create cells in a different way if the input
                    // is radio or checkbox, and the response_layout is
                    // horizontal
                    if (displayBean.getMetadata().getResponseLayout().equalsIgnoreCase("horizontal")
                        && (responseName.equalsIgnoreCase("checkbox") || responseName.equalsIgnoreCase("radio"))) {
                        // The final true parameter is for disabling D Notes
                        Element[] elements =
                            cellFactory.createCellContentsForChecks(responseName, displayBean, displayBean.getMetadata().getResponseSet().getOptions().size(),
                                    ++tabindex, false, true);
                        for (Element el : elements) {
                            el = builderUtil.setClassNames(el);
                            if (repeatFlag) {
                                el = repeatManager.addChildRepeatAttributes(el, repeatParentId, displayBean.getItem().getId(), null);
                            }
                            row.addContent(el);
                        }
                        // move to the next item
                        continue;
                    }
                    td = new Element("td");
                    td = builderUtil.setClassNames(td);
                    // Create cells within each row
                    td = cellFactory.createCellContents(td, responseName, displayBean, ++tabindex, hasDiscrepancyMgt, hasDbFormValues, true);
                    if (repeatFlag) {
                    }
                    row.addContent(td);
                    if(j%maxColRow==0){
                        bodylist.add(row);
                        row = new Element("tr");
                        if (repeatFlag) {
                            repeatParentId = repeatParentId+uniqueId++;
                        }
                    } 
                }// end for displayBean
                if(j%maxColRow!=0)bodylist.add(row);
                //Creating the first/main table
                if(hasStoredRepeatedRows){
                    Element newRow = new Element("tr");
                    Element div = new Element("div");
                    div.setAttribute("id", "repeatCaption");
                    Element newCol = new Element("td");
                    Element strong = new Element("strong");
                    strong.addContent("Repeat: 1");
                    div.addContent(strong);
                    newCol.addContent(div);
                    newRow.addContent(newCol);
                    table.addContent(newRow);
                }
            
               if(!hasStoredRepeatedRows)
                for(int ii=0;ii<repeatNumber;ii++){
                    divRoot.addContent( createTableWithoutData(bodylist,headerlist,subHeadList,ii,unGroupedTable));
                    }
                // The final true parameter is for disabling D Note icons from
                // being clicked
                if (hasStoredRepeatedRows) {
                    List storedRepeatedRows =
                        builderUtil.generatePersistentMatrixRows(ordinalItemDataMap, currentDisplayItems, tabindex, repeatParentId, hasDiscrepancyMgt, true, maxColRow);
                    // add these new rows to the table
                    int count = 1;
                    for(int l = 0; l<storedRepeatedRows.size();l++){
                        ++count;
                        List<Element> rowsList = (ArrayList)storedRepeatedRows.get(l);
                        divRoot.addContent(createTableWithData(rowsList, headerlist, subHeadList, count));
                    }
                }
            }// end for displayFormGroup
            XMLOutputter outp = new XMLOutputter();
            Format format = Format.getPrettyFormat();
            format.setOmitDeclaration(true);
            outp.setFormat(format);
            // The writer object contains the markup for one printable section
            writer = new StringWriter();
            try {
                outp.output(doc, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // The webPageBuilder object contains the markup for all of the
            // sections
            // in the print view
            webPageBuilder.append(writer.toString());
        }
        return webPageBuilder.toString();
    }
    
    
    
    

    
    // JN: So this displayFormGroups is being sent in a weird format, the list with values in currentDisplayItems
    // does not have the repeatnumber since metadata info is wrong, hence this weird way of going through the list.
    //Perhaps the better way is to fix the list itself, however the method is DisplaySectionBeanHandler.getDisplaySectionBeans is being used at several places and seems to be right,
    //there could be a issue with eventcrf which was never handled correctly hence this shortcut
    private int getRepeatFromPrevMeta(List<DisplayItemGroupBean> displayFormGroups) {

        if(displayFormGroups.size()>0)
        return displayFormGroups.get(0).getGroupMetaBean().getRepeatNum();
        else
            return 0;

    }

    private Element createTableWithData(List<Element> rows, ArrayList headerList, ArrayList subHeaderList, int rep) {
        Element table = createTable();
        Element newRow = new Element("tr");
        Element newCol = new Element("td");
        Element strong = new Element("strong");
        strong.addContent("Repeat: "+rep);
        newCol.addContent(strong);
        newRow.addContent(newCol);
        table.addContent(newRow);
        for(int i=0; i<headerList.size();i++){
            Element head = (Element)headerList.get(i);
            Element body = rows.get(i);
            table.addContent((Element)head.clone());
            if(subHeaderList.size()>0){
                try{
                    Element subHead = (Element)subHeaderList.get(i);
                    table.addContent((Element)subHead.clone());
                }catch (IndexOutOfBoundsException IOB){
                }
            }
            table.addContent(body);
        }
        return table;
    }


    private Element createTableWithoutData(List<Element> rows, ArrayList headerList, ArrayList subHeaderList, int rep,boolean ungrouped) {
       
//      {
         Element table = createTable();
      //  if(headerList.size()>0){
              table = createTable();
          if(!ungrouped)
          { 
          table.setAttribute("id","repeat"+rep);
          Element newCol = new Element("td");
          Element strong = new Element("strong");
          strong.setAttribute("style","aka_font_general");
          strong.addContent("Repeat: "+(rep+1));//to avoid showing repeat 0;
          newCol.addContent(strong);
         
          Element newRow = new Element("tr");
          newRow.addContent(newCol.cloneContent());
          
              
          table.addContent(newRow.cloneContent());
          strong.removeContent();
          
        //}
          if(headerList.size()==0){
              newRow.setAttribute("style","display:none;");
              newCol.setAttribute("style","display:none;");
              strong.setAttribute("style","display:none;");
              table.setAttribute("style","display:none;");
              
          }
          }    
          for(int i=0; i<headerList.size();i++){
              
             
            Element head = (Element)headerList.get(i);
            Element body = rows.get(i);
            table.addContent((Element)head.clone());
            if(subHeaderList.size()>0){
                try{
                    Element subHead = (Element)subHeaderList.get(i);
                    table.addContent((Element)subHead.clone());
                }catch (IndexOutOfBoundsException IOB){
                }
            }
            table.addContent(body.cloneContent());
      //  }
      }
        return table;
    }
    private void addResponseLayoutRow(ArrayList subHeadList, List<DisplayItemBean> displayBeans) {
        Element thRow = new Element("tr");
        String responseName;
        String responseLayout;
        ItemFormMetadataBean metaBean;

        // Now create the th row
        Element th2;

        ResponseSetBean respBean;
        ResponseOptionBean optBean;
        int j = 0;
        for (DisplayItemBean dBean : displayBeans) {
            j++;
            metaBean = dBean.getMetadata();
            respBean = metaBean.getResponseSet();
            responseName = respBean.getResponseType().getName();
            if (responseName == null) {
                responseName = "";
            }
            responseLayout = metaBean.getResponseLayout();
            if (responseLayout == null) {
                responseLayout = "";
            }
            // You could have a radio or checkbox whose layout is *not*
            // horizontal,
            // next to a rad or check *with* a horizontal layout.
            if ((responseName.equalsIgnoreCase("radio") || responseName.equalsIgnoreCase("checkbox")) && responseLayout.equalsIgnoreCase("horizontal")) {
                for (int i = 0; i < respBean.getOptions().size(); i++) {
                    optBean = (ResponseOptionBean) respBean.getOptions().get(i);
                    if (optBean != null) {
                        th2 = createThCell(optBean.getText(), 1);
                        // Add font for printing
                        String classNames = th2.getAttribute("class").getValue();
                        classNames = classNames + " general_font";
                        th2.setAttribute("class", classNames);
                        thRow.addContent(th2);
                    }
                }
            } else {
                // create empty cells for non-radios or checks, or rads and
                // checks
                // without horizontal layout
                th2 = createThCell("", 1);
                thRow.addContent(th2);
            }
            if(j%maxColRow==0){
                subHeadList.add(thRow);
                thRow = new Element("tr");
            }
        }
        if(j%maxColRow!=0)subHeadList.add(thRow);
        // now add the final empty th cell for the row
        th2 = createThCell();
        thRow.addContent(th2);
    }

    @Override
    public Element createTable() {
        Element tab = super.createTable();
        return setClassNames(tab);
    }

    public Element createThCell(String cellText, int colSpan) {
        Element th = new Element("td");
        th.setText(cellText);
        if (colSpan > 1) {
            th.setAttribute("colspan", colSpan + "");
        }
        th.setAttribute("class", "aka_headerBackground aka_padding_large aka_cellBorders aka_font_general");
        th.setAttribute("align", "center");

        return th;
    }

    public Element createTHTagFromItemMeta(ItemFormMetadataBean itemFormBean) {

        // include quest number in the header
        Element thTag;
        String responseType = itemFormBean.getResponseSet().getResponseType().getName();
        boolean hasQuestNumber = !"".equalsIgnoreCase(itemFormBean.getQuestionNumberLabel());
        Element newSpan = new Element("span");
        String header = itemFormBean.getHeader();
        if (hasQuestNumber) {
            newSpan = new Element("span");
            newSpan.setAttribute("style", "margin-right:1em");
            newSpan.addContent(itemFormBean.getQuestionNumberLabel());
        }

        if (header != null && header.length() == 0) {
            header = itemFormBean.getLeftItemText();
        }
        // Implement colspan required for headers associated with
        // cells containing checkboxes or radio buttons
        if ((responseType.equalsIgnoreCase("radio") || responseType.equalsIgnoreCase("checkbox"))
            && itemFormBean.getResponseLayout().equalsIgnoreCase("horizontal")) {
            thTag = this.createThCell(header, itemFormBean.getResponseSet().getOptions().size());
        } else {
            thTag = this.createThCell(header, 1);
        }
        if (hasQuestNumber) {
            thTag.addContent(0, newSpan);
        }
        // Add font for printing
//        String classNames = thTag.getAttribute("class").getValue();
//        classNames = classNames + " general_font";
//        thTag.setAttribute("class", classNames);

        return thTag;

    }
    public void createDarkBorders(Element element){
        if(element == null)  return;

        //remove the existing class attribute and replace it with one that specifies
        //darker cell borders
        element.removeAttribute("class");
        //Is it a th or td tag?
        String cssRuleIdentifier = element.getName();
        String cssClasses = CssRules.getClassNamesForTag(cssRuleIdentifier+" borders_on");
        element.setAttribute("class",cssClasses);

    }
    public List<Element> createTheadContentsFromDisplayItems(List<DisplayItemBean> displayBeans, boolean generateExtraColumn) {
        List<Element> elements = new ArrayList<Element>();
        ItemFormMetadataBean itemFormBean;
        // Get the names for the table's headers;
        // Use the item header first, then left item text if the
        // header is blank; add question number labels potentially
        for (DisplayItemBean displayBean : displayBeans) {
            itemFormBean = displayBean.getMetadata();
            elements.add(createTHTagFromItemMeta(itemFormBean));
        }
        // Create an extra column for the cells that contain a Remove Row button
//        if (generateExtraColumn) {
//            elements.add(this.createThCell(""));
//        }
        return elements;
    }

    @Override
    public Element setClassNames(Element styledElement) {
        String cssClasses = CssRules.getClassNamesForTag(styledElement.getName());
        return cssClasses.length() == 0 ? styledElement : styledElement.setAttribute("class", cssClasses);
    }

    public SectionBean getSectionBean() {
        return sectionBean;
    }

    public void setSectionBean(SectionBean sectionBean) {
        this.sectionBean = sectionBean;
    }

    public boolean isInvolvesDataEntry() {
        return involvesDataEntry;
    }

    public void setInvolvesDataEntry(boolean involvesDataEntry) {
        this.involvesDataEntry = involvesDataEntry;
    }

    public EventCRFBean getEventCRFbean() {
        return eventCRFbean;
    }

    public void setEventCRFbean(EventCRFBean eventCRFbean) {
        this.eventCRFbean = eventCRFbean;
    }

    public StudyBean getStudyBean() {
        return studyBean;
    }

    public void setStudyBean(StudyBean studyBean) {
        this.studyBean = studyBean;
    }

    public boolean isInternetExplorer() {
        return isInternetExplorer;
    }

    public void setInternetExplorer(boolean internetExplorer) {
        isInternetExplorer = internetExplorer;
    }
}
