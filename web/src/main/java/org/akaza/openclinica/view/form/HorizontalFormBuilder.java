package org.akaza.openclinica.view.form;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.*;
import org.akaza.openclinica.control.managestudy.BeanFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.ProcessingInstruction;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.xml.transform.Result;

/**
 * This class generates a horizontal HTML table with multiple columns and
 * headers.
 */
public class HorizontalFormBuilder extends DefaultFormBuilder {
    // The object that will provide the content for the table's headers and
    // cells.
    // This is a List of group- or matrix-type tables. The List orders the
    // tables on the section
    // according to the ordinal of each DisplayItemGroupBean's
    // ItemGroupMetadataBean.
    private List<DisplayItemGroupBean> displayItemGroups;
    // Used for displaying the section title, subtitle, and instructions,
    // if necessary
    private SectionBean sectionBean;
    private StudyBean studyBean;
    private EventCRFBean eventCRFbean;
    // is the form a ViewSectionDataEntry form?
    private boolean isDataEntry;
    // Have the form values already been saved during initial or doubledata
    // entry?
    private boolean hasDbFormValues;
    // A value that's needed to seed the tabindex values
    private int tabindexSeed;

    public HorizontalFormBuilder() {
        this.displayItemGroups = new ArrayList<DisplayItemGroupBean>();
        this.tabindexSeed = 1;
        this.hasDbFormValues = false;
    }

    // The list of DisplayFormGroupBeans that provide the content for an XHTML
    // table
    public List<DisplayItemGroupBean> getDisplayItemGroups() {
        // Threadsafe form of accessor method
        return new ArrayList<DisplayItemGroupBean>(displayItemGroups);
    }

    public void setDisplayItemGroups(List<DisplayItemGroupBean> displayItems) {
        this.displayItemGroups = displayItems;
    }

    public SectionBean getSectionBean() {
        return sectionBean;
    }

    public void setSectionBean(SectionBean sectionBean) {
        this.sectionBean = sectionBean;
    }

    @Override
    public String createMarkup() {
        // If the CRF is involved with ViewDataEntry and already has
        // data associated with it, pass on the responsibility to another object
        ViewPersistanceHandler persistanceHandler = new ViewPersistanceHandler();
        if (isDataEntry) {
            List<ItemDataBean> itemDataBeans;
            persistanceHandler = new ViewPersistanceHandler();
            itemDataBeans = persistanceHandler.fetchPersistedData(sectionBean.getId(), eventCRFbean.getId());
            if (!itemDataBeans.isEmpty()) {
                hasDbFormValues = true;
            }
            persistanceHandler.setItemDataBeans(itemDataBeans);
        }
        // Keep track of whether a group has any repeat behavior; true or false
        boolean repeatFlag;
        //Should the table have dark borders?
        boolean hasBorders=false;
        if(sectionBean != null){
            hasBorders= (sectionBean.getBorders() > 0);
        }
      
        // The CellFactory object that generates the content for HTML table TD
        // cells.
        CellFactory cellFactory = new CellFactory();
        RepeatManager repeatManager = new RepeatManager();
        FormBeanUtil formUtil = new FormBeanUtil();
        ViewBuilderUtil builderUtil = new ViewBuilderUtil();
        // The number of repeating table rows that the group will start with.
        int repeatNumber;
        // the div tag that will be the root
        Element divRoot = new Element("div");
        divRoot.setAttribute("id", "tableRoot");
        Document doc = new Document();
        ProcessingInstruction pi = new ProcessingInstruction(Result.PI_DISABLE_OUTPUT_ESCAPING, "");
        doc.addContent(pi);
        doc.setRootElement(divRoot);
        // Show the section's title, subtitle, or instructions
        builderUtil.showTitles(divRoot, this.getSectionBean());
        // One way to generate an id for the repeating tbody or tr element
        int uniqueId = 0;
        // The tabindex attribute for select and input tags
        int tabindex = tabindexSeed;
        boolean hasDiscrepancyMgt = false;
        StudyBean studBean = this.getStudyBean();
        if (studBean != null && studBean.getStudyParameterConfig().getDiscrepancyManagement().equalsIgnoreCase("true")) {

            hasDiscrepancyMgt = true;
        }
        // Create a table for every DisplayItemGroupBean
        // A DisplayItemGroupBean contains an ItemGroupBean and
        // its list of DisplayItemBeans
        for (DisplayItemGroupBean displayItemGroup : this.displayItemGroups) {

            List<DisplayItemBean> currentDisplayItems = displayItemGroup.getItems();
            // A Map that contains persistent (stored in a database), repeated
            // rows
            // in a matrix type table
            // The Map index is the Item id of the first member of the row; the
            // value is a List
            // of item beans that make up the row
            SortedMap<Integer, List<ItemDataBean>> ordinalItemDataMap = new TreeMap<Integer, List<ItemDataBean>>();
            // Is this a persistent matrix table and does it already have
            // repeated rows
            // in the database?
            boolean hasStoredRepeatedRows = false;
            boolean unGroupedTable = displayItemGroup.getItemGroupBean().getName().equalsIgnoreCase(BeanFactory.UNGROUPED) || !displayItemGroup.getGroupMetaBean().isRepeatingGroup();
            // Load any database values into the DisplayItemBeans
            if (hasDbFormValues) {
                currentDisplayItems = persistanceHandler.loadDataIntoDisplayBeans(currentDisplayItems, (!unGroupedTable));
                /*
                 * The highest number ordinal represents how many repeated rows
                 * there are. If the ordinal in ItemDataBeans > 1, then we know
                 * that the group has persistent repeated rows. Get a structure
                 * that maps each ordinal (i.e., >= 2) to its corresponding List
                 * of ItemDataBeans. Then iterate the existing DisplayBeans,
                 * with the number of new rows equaling the highest ordinal
                 * number minus 1. For example, in a List of ItemDataBeans, if
                 * the highest ordinal property among these beans is 5, then the
                 * matrix table has 4 repeated rows from the database. Provide
                 * each new row with its values by using the ItemDataBeans.
                 */
                if (!unGroupedTable && persistanceHandler.hasPersistentRepeatedRows(currentDisplayItems)) {
                    hasStoredRepeatedRows = true;
                    // if the displayitems contain duplicate item ids, then
                    // these duplicates
                    // represent repeated rows. Separate them into a Map of new
                    // rows that
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

            // This group represents "orphaned" items (those without a group) if
            // the FormGroupBean has a group label of UNGROUPED
            Element orphanTable = null;
            if (unGroupedTable) {
                orphanTable = formUtil.createXHTMLTableFromNonGroup(currentDisplayItems, tabindex, hasDiscrepancyMgt, hasDbFormValues, false);
                // We have to track the point the tabindex has reached here
                // The tabindex will increment by the size of the
                // displayItemGroup List
                tabindex += currentDisplayItems.size();
                divRoot.addContent(orphanTable);
                continue;
            }// end if unGroupedTable

            uniqueId++;
            String repeatParentId = "repeatParent" + uniqueId;
            repeatNumber = displayItemGroup.getGroupMetaBean().getRepeatNum();
            // If the form has repeat behavior, this number is > 0
            // Do not allow repeat numbers < 1
//            repeatNumber = repeatNumber < 1 ? 1 : repeatNumber;
            // And a limit of 12
            repeatNumber = repeatNumber > 12 ? 12 : repeatNumber;
            // This is always true during this iteration
            repeatFlag = displayItemGroup.getGroupMetaBean().isRepeatingGroup();

            Element table = createTable();
            // add the thead element
            Element thead = this.createThead();
            table.addContent(thead);
            divRoot.addContent(table);
            // Add the first row for the th tags
            Element thRow = new Element("tr");
            thead.addContent(thRow);
            // Does this group involve a Horizontal checkbox or radio button?
            boolean hasResponseLayout =
              builderUtil.hasResponseLayout(currentDisplayItems);
            // add th elements to the thead element
            // We have to create an extra thead column for the Remove Row
            // button, if
            // the table involves repeating rows
            List<Element> thTags =
              repeatFlag ? createTheadContentsFromDisplayItems(currentDisplayItems,
                true, hasBorders) : createTheadContentsFromDisplayItems(currentDisplayItems, false, hasBorders);

            for (Element el : thTags) {
                thRow.addContent(el);
            }

            if (hasResponseLayout) {
                Element thRowSubhead = new Element("tr");
                thead.addContent(thRowSubhead);
                addResponseLayoutRow(thRowSubhead, currentDisplayItems, hasBorders);
            }

            // Create the tbody tag
            Element tbody;
            Element row;
            Element td;
            tbody = this.createTbody();
            // The table adds the tbody to the XML or markup
            table.addContent(tbody);

            // For each row in the table
            // for (int i = 1; i <= repeatNumber; i++) {
            row = new Element("tr");
            // If the group has repeat behavior and repeats row by row,
            // then the
            // repetition model type attributes have to be added to the tr tag
            int repeatMax = displayItemGroup.getGroupMetaBean().getRepeatMax();
            // Make sure repeatMax >= 1
            repeatMax = repeatMax < 1 ? 40 : repeatMax;
            if (repeatFlag && !(isDataEntry && hasStoredRepeatedRows)) {
                row = repeatManager.addParentRepeatAttributes(row, repeatParentId, repeatNumber, repeatMax);
            }
            // The content for the table cells. For each item...
            for (DisplayItemBean displayBean : currentDisplayItems) {
                // What type of input: text, radio, checkbox, etc.?
                String responseName = displayBean.getMetadata().getResponseSet().getResponseType().getName();
                // We have to create cells in a different way if the input
                // is radio or checkbox, and the response_layout is horizontal
                if (displayBean.getMetadata().getResponseLayout().equalsIgnoreCase("horizontal")
                  && (responseName.equalsIgnoreCase("checkbox") || responseName.equalsIgnoreCase("radio"))) {
                    Element[] elements =
                      cellFactory.createCellContentsForChecks(
                        responseName, displayBean, displayBean.getMetadata().getResponseSet().getOptions().size(),
                        ++tabindex, false, false);
                    for (Element el : elements) {
                        el = builderUtil.setClassNames(el);
                        if(hasBorders) {
                            this.createDarkBorders(el);
                        }
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
                if(hasBorders) {
                    this.createDarkBorders(td);
                }
                // Create cells within each row
                td = cellFactory.createCellContents(td, responseName, displayBean, ++tabindex, hasDiscrepancyMgt, hasDbFormValues, false);
                if (repeatFlag) {
                    td = repeatManager.addChildRepeatAttributes(td, repeatParentId, displayBean.getItem().getId(), null);
                }

                row.addContent(td);
            }// end for displayBean
            // We need an extra cell for holding the "Remove Row" button
            if (repeatFlag) {
                builderUtil.addRemoveRowControl(row, repeatParentId, hasBorders);
            }

            tbody.addContent(row);

            // }//end for every row

            if (hasStoredRepeatedRows) {
                List<Element> storedRepeatedRows =
                  builderUtil.generatePersistentMatrixRows(ordinalItemDataMap,
                    currentDisplayItems, tabindex, repeatParentId, hasDiscrepancyMgt, false, hasBorders);

                // add these new rows to the table
                for (Element newRow : storedRepeatedRows) {
                    tbody.addContent(newRow);
                }

            }
            // Create a row for the Add Row button, if the group includes any
            // repeaters
            if (repeatFlag) {
                builderUtil.createAddRowControl(tbody, repeatParentId, (builderUtil.calcNumberofColumns(displayItemGroup) + 1), hasBorders);

            }
        }// end for displayFormGroup
        XMLOutputter outp = new XMLOutputter();
        Format format = Format.getPrettyFormat();
        format.setOmitDeclaration(true);
        outp.setFormat(format);
        Writer writer = new StringWriter();
        try {
            outp.output(doc, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

    private void addResponseLayoutRow(Element thRow,
                                      List<DisplayItemBean> displayBeans, boolean hasDarkBorder) {

        String responseName;
        String responseLayout;
        ItemFormMetadataBean metaBean;

        // Now create the th row
        Element th2;

        ResponseSetBean respBean;
        ResponseOptionBean optBean;
        for (DisplayItemBean dBean : displayBeans) {
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
            // next to a rad or check with a hor layout.
            if ((responseName.equalsIgnoreCase("radio") || responseName.equalsIgnoreCase("checkbox")) && responseLayout.equalsIgnoreCase("horizontal")) {
                for (int i = 0; i < respBean.getOptions().size(); i++) {
                    optBean = (ResponseOptionBean) respBean.getOptions().get(i);
                    if (optBean != null) {
                        th2 = createThCell(optBean.getText(), 1);
                        if(hasDarkBorder) {
                            this.createDarkBorders(th2);
                            thRow.addContent(th2);
                        }
                    }
                }
            } else {
                // create empty cells for non-radios or checks, or rads and
                // checks
                // without horizontal layout
                th2 = createThCell("", 1);
                if(hasDarkBorder) {
                    this.createDarkBorders(th2);
                    thRow.addContent(th2);
                }
            }
        }
        // now add the final empty th cell for the row
        th2 = createThCell();
        if(hasDarkBorder) {
            this.createDarkBorders(th2);
        }
        thRow.addContent(th2);
    }

    @Override
    public Element createTable() {
        Element tab = super.createTable();
        return setClassNames(tab);
    }

    public Element createThCell(String cellText, int colSpan) {
        Element th = super.createThCell(cellText);
        if (colSpan > 1) {
            th.setAttribute("colspan", colSpan + "");
        }
        return setClassNames(th);
    }

    public Element createTHTagFromItemMeta(ItemFormMetadataBean itemFormBean,
                                           boolean hasDarkBorders) {

        // include quest number in the header
        Element thTag;
        String responseType = itemFormBean.getResponseSet().getResponseType().getName();
        boolean hasQuestNumber = !"".equalsIgnoreCase(itemFormBean.getQuestionNumberLabel());
        Element newSpan = new Element("span");
        if (hasQuestNumber) {
            newSpan = new Element("span");
            newSpan.setAttribute("style", "margin-right:1em");
            newSpan.addContent(itemFormBean.getQuestionNumberLabel());
        }
        String header = itemFormBean.getHeader();
        if (header != null && header.length() == 0) {
            header = itemFormBean.getLeftItemText();
        }
        // Implement colspan required for headers associated with
        // cells containing checkboxes or radio buttons
        if ((responseType.equalsIgnoreCase("radio") || responseType.equalsIgnoreCase("checkbox"))
          && itemFormBean.getResponseLayout().equalsIgnoreCase("horizontal")) {
            thTag = this.createThCell(header,
              itemFormBean.getResponseSet().getOptions().size());
        } else {
            thTag = this.createThCell(header, 1);
        }
        if(hasDarkBorders) {
            createDarkBorders(thTag);
        }
        if (hasQuestNumber) {
            thTag.addContent(0, newSpan);
        }
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

    public List<Element> createTheadContentsFromDisplayItems(
      List<DisplayItemBean> displayBeans, boolean generateExtraColumn,
      boolean hasDarkBorders) {
        List<Element> elements = new ArrayList<Element>();
        ItemFormMetadataBean itemFormBean;
        // Get the names for the table's headers;
        // Use the item header first, then left item text if the
        // header is blank; add question number labels potentially
        for (DisplayItemBean displayBean : displayBeans) {
            itemFormBean = displayBean.getMetadata();
            elements.add(createTHTagFromItemMeta(itemFormBean, hasDarkBorders));
        }
        // Create an extra column for the cells that contain a Remove Row button
        if (generateExtraColumn) {
            if(! hasDarkBorders){
                elements.add(this.createThCell("",0));
            } else {
                Element thElement = new Element("th");
                String cssClasses = CssRules.getClassNamesForTag("th borders_on");
                thElement.setAttribute("class",cssClasses);
                elements.add(thElement);
            }
        }
        return elements;
    }

    @Override
    public Element setClassNames(Element styledElement) {
        String cssClasses = CssRules.getClassNamesForTag(styledElement.getName());
        return cssClasses.length() == 0 ? styledElement : styledElement.setAttribute("class", cssClasses);
    }

    public int getTabindexSeed() {
        return tabindexSeed;
    }

    public void setTabindexSeed(int tabindexSeedint) {
        // tabindexSeed is already initialized by the constructor to 1
        if (tabindexSeedint > 1)
            this.tabindexSeed = tabindexSeedint;
    }

    public boolean isDataEntry() {
        return isDataEntry;
    }

    public void setDataEntry(boolean dataEntry) {
        isDataEntry = dataEntry;
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
}
