package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.bean.core.ItemDataType;
import org.akaza.openclinica.bean.core.ResponseType;
import org.akaza.openclinica.bean.submit.*;
import org.akaza.openclinica.core.form.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.*;
import java.util.Map.Entry;

/**
 * BeanFactory contains methods for generating beans representing non-persistent
 * data. the data derives largely from spreadsheets defining Case Report Forms.
 * The code generally uses the beans for previewing a new CRF version.
 * 
 * @see org.akaza.openclinica.control.admin.SpreadsheetPreviewNw
 */
public class BeanFactory {
    public static final String UNGROUPED = "Ungrouped";
    protected static final Logger logger = LoggerFactory.getLogger("org.akaza.openclinica.control.managestudy.BeanFactory");

    // A thread-safe Map that, for each CRF name, contains the response_label
    // name as the index to response_options_text and response_values.
    // This is designed to deal with
    // the Items that have a response_label value, but leave the options text
    // and values
    // blank and use a response set previously defined in the spreadsheet
    // template.
    private static Map<String, Map<String, String[]>> cachedResponseLabels = Collections.synchronizedMap(new HashMap<String, Map<String, String[]>>());

    /**
     * This method creates a List of DisplayItemGroupBeans. The
     * DisplayItemGroupBeans are listed in the order their items are defined in
     * the CRF spreadsheet. These beans include the items that are and are not
     * associated with a group. If an item is defined in a spreadsheet but does
     * not have a group label, that item is included in a DisplayItemGroupBean,
     * but not linked to any groups (i.e., that DisplayItemGroupBean has an
     * empty FormGroupBean object, with an empty String for a group label). For
     * example, if the spreadsheet defines two items first, but does not assign
     * them a group, then those items will make up the first
     * DisplayItemGroupBean. The spreadsheet then might define a <em>group</em>
     * of items followed by more items that are not part of a group. The former
     * items will represent the second DisplayItemGroupBean. The third
     * DisplayItemGroupBean will include the last collection of "orphaned"
     * items, accompanied by an empty FormGroupBean object.
     * 
     * @param itemsMap
     *            A Map containing rows of Item information from the
     *            spreadsheet. The items are in the order they were defined in
     *            the spreadsheet.
     * @param sectionLabel
     *            A String specifying the name of the section we are displaying.
     * @param groupsMap
     *            A Map containing rows of Group information from the
     *            spreadsheet.
     * @param crfName
     *            The name of the CRF, a String.
     * @see org.akaza.openclinica.bean.submit.ItemGroupBean
     * @return A List of DisplayItemGroupBeans
     */
    public List<DisplayItemGroupBean> createGroupBeans(Map<Integer, Map<String, String>> itemsMap, Map<Integer, Map<String, String>> groupsMap,
            String sectionLabel, String crfName) {

        List<DisplayItemGroupBean> groupBeans = new ArrayList<DisplayItemGroupBean>();
        // We have to have data in all three Map parameters to
        // properly build group beans
        if (groupsMap == null || groupsMap.isEmpty() || sectionLabel == null || sectionLabel.length() < 1 || sectionLabel.equalsIgnoreCase(UNGROUPED)
            || itemsMap == null || itemsMap.isEmpty()) {
            return groupBeans;
        }

        // First, separate the items into those only associated with this
        // section
        Map<String, String> innermap;
        List<DisplayItemBean> displayItems;
        Map.Entry<Integer, Map<String, String>> me;
        // This Map will hold the DisplayitemBeans that are associated with this
        // section
        Map<Integer, Map<String, String>> newMap = new HashMap<Integer, Map<String, String>>();
        String lab;
        for (Iterator<Map.Entry<Integer, Map<String, String>>> iter = itemsMap.entrySet().iterator(); iter.hasNext();) {
            me = iter.next();
            innermap = me.getValue();
            lab = innermap.get("section_label");
            if (lab != null && lab.equalsIgnoreCase(sectionLabel)) {
                newMap.put(me.getKey(), innermap);
            }
        }
        displayItems = this.createDisplayItemBeansFromMap(newMap, crfName);

        // Now, separate the DisplayItemBeans into those associated with groups,
        // and any others that do not have groups
        ItemGroupBean fgBean;
        DisplayItemGroupBean disFgBean;
        String latestGroupLabel;
        boolean validGroupFlag = false;
        int ordinal = 0;
        // Examine each Item; if it doesn't have a group label, store the item
        // as a
        // DisplayItemBean in a new DisplayFormGroupBean. Once items defining a
        // group begin, create a new DisplayFormGroupBean for those
        // group-related
        // items and place the first DisplayFormGroupBean the code created in
        // the List.
        for (DisplayItemBean disBean : displayItems) {

            latestGroupLabel = disBean.getMetadata().getGroupLabel();
            // If the group label is a valid String, then the displayItem is
            // related
            // to a group. Store it in a FormGroupBean.
            if (latestGroupLabel != null && latestGroupLabel.length() > 0 && !latestGroupLabel.equalsIgnoreCase(UNGROUPED)) {
                // set this flag to true, indicating that the items being
                // processed now
                // are associated with a valid group
                validGroupFlag = true;
                // If lastGroupLabel is not yet stored in the List of
                // DisplayGroupBeans
                // then the beans associated with that label (DisplayGroupBeans
                // and
                // FormGroupBeans) have to be initialized.
                // Otherwise, just store the new displayitembean in the existing
                // FormGroupBean/DisplayFormGroupBean
                disFgBean = getGroupFromLabel(latestGroupLabel, groupBeans);
                // If the DisplayGroupBean doesn't have a valid group label yet,
                // then a new one has to be initialized for this DisplayItemBean
                if (!(disFgBean.getItemGroupBean().getName().length() > 0)) {
                    // Get FormGroupBean from group label
                    fgBean = initFormGroupFromMap(latestGroupLabel, groupsMap);
                    ordinal++;
                    fgBean.getMeta().setOrdinal(ordinal);
                    disFgBean.setItemGroupBean(fgBean);
                    disFgBean.setGroupMetaBean(fgBean.getMeta());
                    groupBeans.add(disFgBean);

                }
            } else {
                // if there is no group label associated with the
                // DisplayItemBean, then it
                // does not have a group; it is an "orphaned" item. In this
                // case, create a
                // DisplayFormGroup with a group label signified with an empty
                // string. This
                // "group" will hold the orphaned items. What if there are
                // orphaned items
                // in the spreadsheet that are divided by grouped items? Then
                // these orphaned
                // items have to be in separate DisplayFormGroups. To handle
                // this case, the
                // code checks the validGroupFlag boolean variable. if "true,"
                // indicating that
                // non-orphaned beans have just been processed, then create a
                // new
                // DisplayFormGroup for these orphaned items.
                boolean isFirst = isFirstUngroupedItem(groupBeans);
                if (validGroupFlag || isFirst) {
                    disFgBean = new DisplayItemGroupBean();
                    fgBean = new ItemGroupBean();
                    ordinal++;
                    fgBean.getMeta().setOrdinal(ordinal);
                    fgBean.setName(UNGROUPED);
                    disFgBean.setItemGroupBean(fgBean);
                    disFgBean.setGroupMetaBean(fgBean.getMeta());
                    groupBeans.add(disFgBean);

                } else {
                    // The existing DisplayFormGroupBean for orphaned items
                    // is the FormGroupBean containing the highest ordinal, and
                    // with a group label
                    // containing an empty string
                    disFgBean = getLatestDisFormBeanForOrphanedItems(groupBeans);
                }
                validGroupFlag = false;
            }

            disFgBean.getItems().add(disBean);
        }

        return groupBeans;
    }

    private boolean isFirstUngroupedItem(List<DisplayItemGroupBean> groupBeans) {

        for (DisplayItemGroupBean digBean : groupBeans) {
            if (digBean.getItemGroupBean().getName().equalsIgnoreCase(UNGROUPED)) {
                return false;
            }
        }
        return true;
    }

    private DisplayItemGroupBean getLatestDisFormBeanForOrphanedItems(List<DisplayItemGroupBean> beans) {

        if (beans == null || beans.isEmpty()) {
            return new DisplayItemGroupBean();
        }
        // create a LinkedList then grab the tail of the list to get
        // the FormGroupBean with the highest ordinal
        LinkedList<DisplayItemGroupBean> list = new LinkedList<DisplayItemGroupBean>();
        for (DisplayItemGroupBean dBean : beans) {
            if (dBean.getItemGroupBean().getName().length() < 1 || dBean.getItemGroupBean().getName().equalsIgnoreCase(UNGROUPED)) {
                list.add(dBean);
            }
        }
        // sort the list
        Collections.sort(list, new Comparator<DisplayItemGroupBean>() {

            public int compare(DisplayItemGroupBean displayFormGroupBean, DisplayItemGroupBean displayFormGroupBean1) {
                return displayFormGroupBean.getGroupMetaBean().getOrdinal().compareTo(displayFormGroupBean1.getGroupMetaBean().getOrdinal());
            }
        });
        // return the DisplayFormGroupBean with the highest ordinal
        return list.getLast();
    }

    /**
     * This method places a group metadata bean into an ItemGroupBean
     * 
     * @param groupLabel
     *            The String specifying a group label.
     * @param groupMap
     *            The Map containing the data for a group.
     * @return ItemGroupBean
     */
    private ItemGroupBean initFormGroupFromMap(String groupLabel, Map<Integer, Map<String, String>> groupMap) {
        ItemGroupBean formGroupBean = new ItemGroupBean();
        ItemGroupMetadataBean igMetaBean = new ItemGroupMetadataBean();
        Map<String, String> groupValues;
        NumberFormat numFormatter = NumberFormat.getInstance();
        numFormatter.setMaximumFractionDigits(0);
        String tempValue;
        for (Entry<Integer, Map<String, String>> entry : groupMap.entrySet()) {
            groupValues = entry.getValue();
            if (groupValues.get("group_label").equalsIgnoreCase(groupLabel)) {
                formGroupBean.setName(groupLabel);
                igMetaBean.setHeader(groupValues.get("group_header"));
                //Hamid repeating group or not
                if(groupValues.get("repeating_group") != null){
                    igMetaBean.setRepeatingGroup(Boolean.parseBoolean(groupValues.get("repeating_group")));
//                System.out.println("=============" + groupValues.get("repeating_group"));
                }

                // YW 10-04-2007 <<
                // BWP changed to try/catch block
                tempValue = groupValues.get("group_repeat_max");
                // This line is necessary because "0" will not throw an
                // exception, but is still unacceptable
                if(StringUtil.isBlank(tempValue)){
                    tempValue = "0";
                } else if ("0.0".equalsIgnoreCase(tempValue)) {
                    tempValue = "22000";
                }
                try {

                    igMetaBean.setRepeatMax(new Integer(numFormatter.format(Double.parseDouble(tempValue))));

                } catch (NumberFormatException nfe) {
                    // BWP >>an arbitrarily large number allows infinite
                    // repeats; it could also be -1
                    igMetaBean.setRepeatMax(22000);
                }
                // YW >>
                // YW 10-04-2007 <<
                // BWP changed to try/catch block
                tempValue = groupValues.get("group_repeat_number");
                // This line is necessary because "0" will not throw an
                // exception, but is still unacceptable
                if(igMetaBean.isRepeatingGroup() && StringUtil.isBlank(tempValue)){
                    tempValue = "1";
                }else if(StringUtil.isBlank(tempValue)){
                    tempValue = "0";
                }else if ("0.0".equalsIgnoreCase(tempValue)) {
                    tempValue = "1";
                }
                try {
                    igMetaBean.setRepeatNum(new Integer(numFormatter.format(Double.parseDouble(tempValue))));

                } catch (NumberFormatException nfe) {
                    // BWP 10-13-07
                    igMetaBean.setRepeatNum(1);
                }
                // YW >>
            }
        }
        formGroupBean.setMeta(igMetaBean);
        return formGroupBean;
    }

    private DisplayItemGroupBean getGroupFromLabel(String label, List<DisplayItemGroupBean> groupBeans) {

        if (groupBeans == null || groupBeans.isEmpty()) {
            return new DisplayItemGroupBean();
        }

        for (DisplayItemGroupBean dBean : groupBeans) {
            if (dBean.getItemGroupBean().getName().equalsIgnoreCase(label)) {
                return dBean;
            }
        }
        // If we didn't find the group label, return an empty bean
        return new DisplayItemGroupBean();

    }

    public ItemGroupBean createFormGroupBeanFromMap(Map<Integer, Map<String, String>> groupMap) {
        ItemGroupBean fBean = new ItemGroupBean();
        Map<String, String> beanMap;
        Map.Entry<Integer, Map<String, String>> me;
        NumberFormat numFormatter = NumberFormat.getInstance();
        numFormatter.setMaximumFractionDigits(0);
        String tempValue;
        for (Iterator<Map.Entry<Integer, Map<String, String>>> iter = groupMap.entrySet().iterator(); iter.hasNext();) {

            me = iter.next();
            beanMap = me.getValue();
            fBean.setName(beanMap.get("group_label"));
            tempValue = beanMap.get("group_repeat_number");
            // This line is necessary because "0" will not throw an
            // exception, but is still unacceptable
            if ("0.0".equalsIgnoreCase(tempValue)) {
                tempValue = "1";
            }
            try {
                fBean.getMeta().setRepeatNum(new Integer(numFormatter.format(Double.parseDouble(tempValue))));
            } catch (NumberFormatException nfe) {
                // BWP 10-13-07
                fBean.getMeta().setRepeatNum(1);
            }
            tempValue = beanMap.get("group_repeat_max");
            // This line is necessary because "0" will not throw an
            // exception, but is still unacceptable
            if ("0.0".equalsIgnoreCase(tempValue)) {
                tempValue = "22000";
            }
            try {
                fBean.getMeta().setRepeatMax(new Integer(numFormatter.format(Double.parseDouble(tempValue))));
            } catch (NumberFormatException nfe) {
                // BWP >>an arbitrarily large number allows infinite repeats; it
                // could also be -1
                fBean.getMeta().setRepeatMax(22000);
            }
            fBean.getMeta().setHeader(beanMap.get("group_header"));
        }
        return fBean;
    }

    /*
     * Generate the list of children for a DisplayItemBean. Currently, this
     * method must return an ArrayList, because that is the data type for an
     * ItemFormMetaDataBean's children field.
     */
    public ArrayList<DisplayItemBean> createChildren(List<DisplayItemBean> childrenItems, DisplayItemBean parent) {
        ArrayList<DisplayItemBean> children = new ArrayList<DisplayItemBean>();
        // If the ItemBean's parent label is the same as an ItemBean's name,
        // then the former ItemBean is
        // a child of that paent ItemBean.
        for (DisplayItemBean bean : childrenItems) {
            if (bean.getMetadata().getParentLabel().equalsIgnoreCase(parent.getItem().getName())) {
                children.add(bean);
            }
        }

        // sort in ascending order based on the column number of
        // the ItemFormMetadataBean. This is specified by the DisplayItemBean
        // API.
        Collections.sort(children, new Comparator<DisplayItemBean>() {

            public int compare(DisplayItemBean displayItemBean, DisplayItemBean displayItemBean1) {
                return new Integer(displayItemBean.getMetadata().getColumnNumber()).compareTo(displayItemBean1.getMetadata().getColumnNumber());
            }
        });
        return children;
    }

    /*
     * Create an ItemBean from a Map specifying its property values, as in
     * {item_name: Item1, ...) etc.
     */
    public ItemBean createItemBean(Map<String, String> itemValuesMap) {
        ItemBean iBean = new ItemBean();
        String itemDesc = itemValuesMap.get("description_label");
        String itemDataType;
        iBean.setDescription(itemDesc);
        itemDesc = itemValuesMap.get("item_name");
        iBean.setName(itemDesc);
        itemDesc = itemValuesMap.get("units");
        iBean.setUnits(itemDesc);
        itemDataType = itemValuesMap.get("data_type");
        // TODO: solve the problem with the getByName method
        ItemDataType itemDT = ItemDataType.getByName(itemDataType);
        iBean.setItemDataTypeId(itemDT.getId());
        return iBean;
    }

    /* Create a ResponseSetBean from a Map of property/value pairs */
    public ResponseSetBean createResponseSetBean(Map<String, String> itemValuesMap, String crfName) {
        ResponseSetBean rBean = new ResponseSetBean();
        // If an item has a response label that has already been defined (by
        // previous
        // item definitions) then use the response_options_text and
        // response_values associated
        // with that response label
        String[] innerArray = new String[2];
        String responseType = itemValuesMap.get("response_type");
        String responseLab = itemValuesMap.get("response_label");
        String optionsTxt = itemValuesMap.get("response_options_text");
        String optionsVals = itemValuesMap.get("response_values");
        rBean.setResponseType(ResponseType.getByName(responseType));
        rBean.setLabel(itemValuesMap.get("response_label"));
        // Find out if the response text and option values are already cached
        // for
        // this CRF. The String index is the CRF name; the String array refrs to
        // the options text and options values.
        Map<String, String[]> responseLabels = cachedResponseLabels.get(crfName);
        if (responseLabels == null) {
            responseLabels = new HashMap<String, String[]>();
            cachedResponseLabels.put(crfName, responseLabels);
        }
        if (responseLabels.get(responseLab) == null) {
            innerArray[0] = optionsTxt;
            innerArray[1] = optionsVals;
            responseLabels.put(responseLab, innerArray);
        }

        // if the item has a response label but no values for
        // response_options_text
        // or response_values, provide the item with the cached values for these
        // properties
        String[] tmpArr;
        if (responseLab != null && responseLab.length() != 0 && optionsTxt != null && optionsTxt.length() == 0 && optionsVals != null
            && optionsVals.length() == 0) {
            tmpArr = responseLabels.get(responseLab);
            // The responseLabel is stored in the Map
            if (tmpArr != null) {
                rBean.setOptions(tmpArr[0], tmpArr[1]);
                return rBean;
            }
        }

        rBean.setOptions(optionsTxt, optionsVals);
        return rBean;

    }

    /*
     * Create an ItemFormMetadataBean from the values referred to by a Map.Entry
     * object. The Map.Entry contains an integer pointing to a Map of Item
     * values from the Sreadsheet template, as in (item_name: Item1, etc...}
     */
    public ItemFormMetadataBean createItemFormMetadataBean(Map.Entry<Integer, Map<String, String>> me, String crfName) {
        if (me == null)
            return new ItemFormMetadataBean();
        ItemFormMetadataBean metadataBean = new ItemFormMetadataBean();
        // the POI library returns all numbers as doubles (2.0), we round
        // off the numbers to 2, for instance
        NumberFormat numFormatter = NumberFormat.getInstance();
        numFormatter.setMaximumFractionDigits(0);
        String itemPageNum;
        String columnNum;
        String questNum;
        metadataBean.setOrdinal(me.getKey());
        Map<String, String> map = me.getValue();
        // response_layout property for checkboxes and radio buttons
        metadataBean.setResponseLayout(map.get("response_layout"));
        metadataBean.setDefaultValue(map.get("default_value"));
        metadataBean.setParentLabel(map.get("parent_item"));
        metadataBean.setLeftItemText(map.get("left_item_text"));
        metadataBean.setRightItemText(map.get("right_item_text"));
        metadataBean.setHeader(map.get("header"));
        metadataBean.setSubHeader(map.get("subheader"));
        metadataBean.setGroupLabel(map.get("group_label"));
        itemPageNum = map.get("page_number");
        try {
            itemPageNum = numFormatter.format(Double.parseDouble(itemPageNum));
        } catch (NumberFormatException nfe) {
            itemPageNum = "";
        }
        metadataBean.setPageNumberLabel(itemPageNum);
        columnNum = map.get("column_number");
        try {
            columnNum = numFormatter.format(Double.parseDouble(columnNum));
            metadataBean.setColumnNumber(Integer.parseInt(columnNum));
        } catch (NumberFormatException nfe) {
            metadataBean.setColumnNumber(0);
        }
        questNum = map.get("question_number");
        //We support any character in QUESTION_NUMBER field. So there is no need to do this number formatting.
//        try {
//            questNum = numFormatter.format(Double.parseDouble(questNum));
//        } catch (NumberFormatException nfe) {
//            questNum = "";
//        }
        metadataBean.setQuestionNumberLabel(questNum);
        String requStr = map.get("required");
        requStr = requStr.equalsIgnoreCase("") ? "0" : requStr;
        double required;
        try {
            required = Double.parseDouble(requStr);
        } catch (NumberFormatException nfe) {
            required = 0;
        }
        if (required > 0) {
            metadataBean.setRequired(true);
        }
        ResponseSetBean respBean = this.createResponseSetBean(map, crfName);
        metadataBean.setResponseSet(respBean);
        return metadataBean;
    }

    /**
     * Create an ItemFormMetadataBean from the values referred to by a Map
     * object. The Map contains the Item values from the Sreadsheet template, as
     * in (item_name: Item1, etc...}
     * 
     * @param map
     *            A Map involving the column names and values Of items.
     * @param ordinal
     *            The ordinal property for the meta data bean.
     * @param crfName
     *            The CRF name.
     * @return An ItemFormMetadataBean representing the values from the Map.
     */
    public ItemFormMetadataBean createMetadataBean(Map<String, String> map, int ordinal, String crfName) {
        if (map == null)
            return new ItemFormMetadataBean();
        ItemFormMetadataBean metadataBean = new ItemFormMetadataBean();
        // the POI library returns all numbers as doubles (2.0), we round
        // off the numbers to 2, for instance
        NumberFormat numFormatter = NumberFormat.getInstance();
        numFormatter.setMaximumFractionDigits(0);
        String itemPageNum;
        String columnNum;
        String questNum;
        metadataBean.setOrdinal(ordinal);
        metadataBean.setResponseLayout(map.get("response_layout"));
        metadataBean.setParentLabel(map.get("parent_item"));
        metadataBean.setLeftItemText(map.get("left_item_text"));
        metadataBean.setRightItemText(map.get("right_item_text"));
        metadataBean.setDefaultValue(map.get("default_value"));
        metadataBean.setHeader(map.get("header"));
        metadataBean.setSubHeader(map.get("subheader"));
        itemPageNum = map.get("page_number");
        try {
            itemPageNum = numFormatter.format(Double.parseDouble(itemPageNum));
        } catch (NumberFormatException nfe) {
            itemPageNum = "";
        }
        metadataBean.setPageNumberLabel(itemPageNum);
        columnNum = map.get("column_number");
        try {
            columnNum = numFormatter.format(Double.parseDouble(columnNum));
            metadataBean.setColumnNumber(Integer.parseInt(columnNum));
        } catch (NumberFormatException nfe) {
            metadataBean.setColumnNumber(0);
        }
        questNum = map.get("question_number");
        try {
            questNum = numFormatter.format(Double.parseDouble(questNum));
        } catch (NumberFormatException nfe) {
            questNum = "";
        }
        metadataBean.setQuestionNumberLabel(questNum);
        String requStr = map.get("required");
        requStr = requStr.equalsIgnoreCase("") ? "0" : requStr;
        double required;
        try {
            required = Double.parseDouble(requStr);
        } catch (NumberFormatException nfe) {
            required = 0;
        }
        if (required > 0) {
            metadataBean.setRequired(true);
        }
        ResponseSetBean respBean = this.createResponseSetBean(map, crfName);
        metadataBean.setResponseSet(respBean);
        return metadataBean;
    }

    // Create a DisplaySectionBean using an items map, a section's title,
    // the section's page number, and the CRF name/ The CRF name is needed for
    // creating the ResponseSetBean.
    public DisplaySectionBean createDisplaySectionBean(Map<Integer, Map<String, String>> map, String sectionTitle, String sectionLabel, String sectionSubtitle,
            String instructions, String crfName, int sectionBorders) {
        /*
         * each map row or entry looks like this: 36: {subheader=, validation=,
         * page_number=2.0, response_type=text, left_item_text=Other Body
         * Sysytem/Site, response_options_text=, required=, response_values=,
         * question_number=35.0, phi=0.0, validation_error_message=, header=,
         * units=, data_type=ST, response_label=text, item_name=can_base36,
         * column_number=2.0, section_label=Specify Other Body System/Site,
         * description_label=label, parent_item=can_base34, right_item_text=}
         */
        // Play defense!
        if (map == null)
            return new DisplaySectionBean();
        // Display a section title in the JSP
        SectionBean sbean = new SectionBean();
        sbean.setTitle(sectionTitle);
        sbean.setSubtitle(sectionSubtitle);
        sbean.setInstructions(instructions);
        sbean.setBorders(sectionBorders);
        DisplaySectionBean displaySectionBean = new DisplaySectionBean();
        displaySectionBean.setSection(sbean);
        ArrayList<DisplayItemBean> allDisplayItems = new ArrayList<DisplayItemBean>();
        List<DisplayItemBean> childrenItems = new ArrayList<DisplayItemBean>();
        DisplayItemBean displayItem;
        ItemFormMetadataBean itemMeta;
        Map.Entry<Integer, Map<String, String>> me;
        Map<String, String> innerMap;
        for (Iterator<Map.Entry<Integer, Map<String, String>>> iter = map.entrySet().iterator(); iter.hasNext();) {
            displayItem = new DisplayItemBean();
            // me is one row in the list of Items, indexed by the item number
            me = iter.next();
            if (itemSecLabelMatchesSection(me, sectionLabel)) {
                itemMeta = this.createItemFormMetadataBean(me, crfName);
                displayItem.setMetadata(itemMeta);
                innerMap = me.getValue();
                displayItem.setItem(this.createItemBean(innerMap));
                // Compensate for any items that identify themselves as parents
                String parentName = displayItem.getMetadata().getParentLabel();
                if (parentName.length() > 0 && !displayItem.getItem().getName().equalsIgnoreCase(parentName)) {
                    displayItem.getMetadata().setParentId(1);
                    childrenItems.add(displayItem);
                } else {
                    allDisplayItems.add(displayItem);
                }
            }
        }
        // Set the children for each of the display items
        for (DisplayItemBean parentBean : allDisplayItems) {
            parentBean.setChildren(this.createChildren(childrenItems, parentBean));
        }
        // Sort the List of DisplayItemBeans based on their ordinal; see
        // getDisplayBean() in DataEntryServlet
        Collections.sort(allDisplayItems);
        displaySectionBean.setItems(allDisplayItems);
        return displaySectionBean;
    }

    // This has to return an ArrayList (not a List, as it should, if you program
    // to interfaces), because the SectionBean.setitems method takes an
    // ArrayList
    public ArrayList createItemBeanList(Map<Integer, Map<String, String>> itemsMap, String secLabel, String crfName) {
        if (itemsMap == null)
            return new ArrayList();
        ArrayList<ItemBean> itemList = new ArrayList<ItemBean>();
        ItemBean itemBean;
        ItemFormMetadataBean metaBean;
        Map<String, String> innerMap;
        Map.Entry<Integer, Map<String, String>> me;
        // SpreadsheetPreview returns doubles as Strings (such as "1.0")
        // for "1" in a spreadsheet cell, so make sure only "1" is displayed
        // using
        // this NumberFormat object
        NumberFormat numFormatter = NumberFormat.getInstance();
        numFormatter.setMaximumFractionDigits(0);
        for (Iterator<Map.Entry<Integer, Map<String, String>>> iter = itemsMap.entrySet().iterator(); iter.hasNext();) {
            me = iter.next();

            // iterate the internal Map, with all the item headers and values
            innerMap = me.getValue();
            // String itemPagNumber = innerMap.get("page_number");
            String itemSectionLabel = innerMap.get("section_label");
            if (itemSectionLabel.equalsIgnoreCase(secLabel)) {
                itemBean = this.createItemBean(innerMap);
                metaBean = createItemFormMetadataBean(me, crfName);
                itemBean.setItemMeta(metaBean);
                itemList.add(itemBean);
            }
        }
        return itemList;
    }

    // Return a list of SectionBeans using Item and Section information
    public List<SectionBean> createSectionBeanList(Map<Integer, Map<String, String>> sectionsMap, Map<Integer, Map<String, String>> itemsMap, String crfName,
            Map<Integer, Map<String, String>> groupsMap) {
        if (sectionsMap == null || itemsMap == null) {
            return new ArrayList<SectionBean>();
        }
        List<SectionBean> secsList = new ArrayList<SectionBean>();
        SectionBean secBean;
        Map.Entry<Integer, Map<String, String>> me;
        Map<String, String> sectionVals;
        String sectionLabel;
        // SpreadsheetPreview returns doubles as Strings (such as "1.0")
        // for "1" in a spreadsheet cell, so make sure only "1" is displayed
        // using
        // this NumberFormat object
        NumberFormat numFormatter = NumberFormat.getInstance();
        numFormatter.setMaximumFractionDigits(0);
        for (Iterator<Map.Entry<Integer, Map<String, String>>> iter = sectionsMap.entrySet().iterator(); iter.hasNext();) {
            secBean = new SectionBean();

            me = iter.next();
            sectionVals = me.getValue();
            sectionLabel = sectionVals.get("section_label");
            secBean.setName(sectionLabel);
            int borders = 0;
            // set borders property
            String bordersTemp = sectionVals.get("borders");
            if (bordersTemp != null) {
                borders = new Integer(bordersTemp);
            }
            secBean.setBorders(borders);
            secBean.setParent(createSectionBean(sectionVals.get("parent_section")));
            secBean.setTitle(sectionVals.get("section_title"));
            secBean.setSubtitle(sectionVals.get("subtitle"));
            secBean.setInstructions(sectionVals.get("instructions"));
            String pagNumber = sectionVals.get("page_number");
            // ensure pagNumber is an actual number ; it doesn't have to be
            // enterd as a numer in the Excel file
            try {
                pagNumber = numFormatter.format(Double.parseDouble(pagNumber));
            } catch (NumberFormatException nfe) {
                pagNumber = "0";
            }
            secBean.setItems(createItemBeanList(itemsMap, sectionLabel, crfName));
            secBean.setPageNumberLabel(pagNumber);

            secsList.add(secBean);
        }
        // Add groups data
        secsList = addPreviewGroupsInfo(secsList, groupsMap);
        return secsList;
    }

    public List<SectionBean> addPreviewGroupsInfo(List<SectionBean> secsList, Map<Integer, Map<String, String>> groupsMap) {
        if (secsList == null || secsList.isEmpty()) {
            return secsList;
        }
        ItemGroupBean groupBean = new ItemGroupBean();
        Map<String, String> groupValues;
        String groupLabel = "";
        // Find out which groups belong with which section
        for (Entry<Integer, Map<String, String>> entry : groupsMap.entrySet()) {
            groupValues = entry.getValue();
            groupLabel = groupValues.get("group_label");
            groupBean = this.initFormGroupFromMap(groupLabel, groupsMap);
            for (SectionBean secBean : secsList) {
                if (sectionHasGroup(groupLabel, secBean)) {
                    secBean.getGroups().add(groupBean);
                }
            }
        }

        return secsList;

    }

    // Does a group belong in a section, based on the group label?
    private boolean sectionHasGroup(String groupLabel, SectionBean secBean) {
        // if an item in a section matches with the group label, then the group
        // belongs to
        // that section
        for (Object itemObj : secBean.getItems()) {
            if (((ItemBean) itemObj).getItemMeta().getGroupLabel().equalsIgnoreCase(groupLabel)) {
                return true;
            }
        }

        return false;
    }

    // Does an item's section label match a section's section label?
    private boolean itemSecLabelMatchesSection(Map.Entry me, String sectionLabel) {
        Map map = (Map) me.getValue();
        String tmp = (String) map.get("section_label");
        return tmp.equalsIgnoreCase(sectionLabel);
    }

    public List<DisplayItemBean> createDisplayItemBeansFromMap(Map<Integer, Map<String, String>> itemsMap, String crfName) {

        List<DisplayItemBean> allDisplayItems = new ArrayList<DisplayItemBean>();
        DisplayItemBean displayItem;
        ItemFormMetadataBean itemMeta;
        Map.Entry<Integer, Map<String, String>> me;
        Map<String, String> innerMap;
        for (Iterator<Map.Entry<Integer, Map<String, String>>> iter = itemsMap.entrySet().iterator(); iter.hasNext();) {
            displayItem = new DisplayItemBean();
            // me is one row in the list of Items, indexed by the item number
            me = iter.next();
            itemMeta = this.createItemFormMetadataBean(me, crfName);
            innerMap = me.getValue();
            String labeltmp = innerMap.get("group_label");
            if (labeltmp.length() < 1) {
                itemMeta.setGroupLabel(UNGROUPED);
            } else {
                itemMeta.setGroupLabel(labeltmp);
            }
            displayItem.setMetadata(itemMeta);
            displayItem.setItem(this.createItemBean(innerMap));
            allDisplayItems.add(displayItem);
        }

        // Sort the List of DisplayItemBeans based on their ordinal; see
        // getDisplayBean() in DataEntryServlet
        Collections.sort(allDisplayItems);
        return allDisplayItems;
    }

    // TODO: Implement this method
    public static SectionBean createSectionBean(String secLabel) {
        return new SectionBean();
    }

}
