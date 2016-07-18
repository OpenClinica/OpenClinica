package org.akaza.openclinica.view.form;

import org.akaza.openclinica.bean.submit.DisplayItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.core.SessionManager;
import org.akaza.openclinica.dao.submit.ItemDataDAO;

import java.util.*;

/**
 * A class that handles persistent values that must appear in forms when they
 * are generated.
 */
public class ViewPersistanceHandler {
    private List<ItemDataBean> itemDataBeans;
    private ViewBuilderUtil viewBuilderUtil;

    public ViewPersistanceHandler() {
        super();
        itemDataBeans = new ArrayList<ItemDataBean>();
        viewBuilderUtil = new ViewBuilderUtil();
    }

    public List<ItemDataBean> fetchPersistedData(int sectionId, int eventcrfId) {

        //SessionManager sessionManager = new SessionManager();
        ItemDataDAO itemDataDAO = new ItemDataDAO(SessionManager.getStaticDataSource());
        List<ItemDataBean> itemDataBeans = itemDataDAO.findAllActiveBySectionIdAndEventCRFId(sectionId, eventcrfId);
        return itemDataBeans == null ? new ArrayList<ItemDataBean>() : itemDataBeans;
    }

    /**
     *
     * @param disBeans
     * @param hasGroupedItems
     * @return
     */
    public List<DisplayItemBean> loadDataIntoDisplayBeans(List<DisplayItemBean> disBeans, boolean hasGroupedItems) {

        List<ItemDataBean> dataBeans = getItemDataBeans();
        if (dataBeans.isEmpty() || disBeans == null || disBeans.isEmpty()) {
            return new ArrayList<DisplayItemBean>();
        }
        // The process is straightforward if the display beans are not involved
        // with a
        // group-type matrix table that has persistent repeated rows
        dataItem: for (DisplayItemBean itemBean : disBeans) {
            for (ItemDataBean iDataBean : dataBeans) {
                // We're not handling persistent repeated rows at this point;
                // Just the first row of data.
                // see this.handleExtraGroupRows
                if (iDataBean.getOrdinal() == 1 && iDataBean.getItemId() == itemBean.getItem().getId()) {
                    itemBean.setData(iDataBean);
                    continue dataItem;
                }
            }
        }
        return disBeans;

        // Just return the DisplayBeans unchanged if they have persistent
        // repeated rows.
        // Another method handles that condition; see this.handleExtraGroupRows
        // return disBeans;
    }

    public boolean hasPersistentRepeatedRows(List<DisplayItemBean> itemBeans) {

        // if ItemDataBeans have any ordinal values > 1, and
        // at least one display item has data in this collection, then they
        // have repeated rows in the database
        for (DisplayItemBean disBean : itemBeans) {
            for (ItemDataBean dataBean : this.getItemDataBeans()) {
                if (disBean.getItem().getId() == dataBean.getItemId() && dataBean.getOrdinal() > 1) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     *
     * @return
     * @see org.akaza.openclinica.view.form.ViewPersistanceHandler
     *      loadDataIntoDisplayBeans()
     */
    public SortedMap<Integer, List<ItemDataBean>> handleExtraGroupRows() {

        // handle duplicate item ids in itemdatabeans
        // A sorted map with the ordinal as the index, and the related
        // ItemDataBean as value.
        // All of the contained ItemDataBeans have the same Item id
        List<ItemDataBean> dataBeans = this.getItemDataBeans();
        SortedMap<Integer, List<ItemDataBean>> ordinalItemDataMap = new TreeMap<Integer, List<ItemDataBean>>();

        List<ItemDataBean> innerDataBeanList = new ArrayList<ItemDataBean>();
        int tracker = 0;
        List<Integer> listOrdinal = new ArrayList<Integer>();
        int currentOrdinal = 0;
        int tempOrdinal;
        // Populate the SortedMap with ordinal --> List of ItemDataBeans
        for (ItemDataBean itemDataBean : dataBeans) {
            // an ordinal > 1 means the databean represents a repeated row
            tempOrdinal = itemDataBean.getOrdinal();
            if (tempOrdinal > 1) {
                tracker++;
                if (tracker == 1) {
                    // first data bean found
                    innerDataBeanList.add(itemDataBean);
                    listOrdinal.add(tempOrdinal);
                    // currentOrdinal = tempOrdinal;
                    ordinalItemDataMap.put(tempOrdinal, innerDataBeanList);
                } else {
                    // if tempOrdinal is the same as currentOrdinal, then just
                    // add the data bean
                    // to the existing List.
                    if (listOrdinal.contains(tempOrdinal)) {
                        // if(tempOrdinal == currentOrdinal) {
                        ordinalItemDataMap.get(tempOrdinal).add(itemDataBean);
                    } else {
                        listOrdinal.add(tempOrdinal);
                        // currentOrdinal = tempOrdinal;
                        innerDataBeanList = new ArrayList<ItemDataBean>();
                        innerDataBeanList.add(itemDataBean);
                        ordinalItemDataMap.put(tempOrdinal, innerDataBeanList);
                    }
                }

            }// tempOrdinal > 0

        }// for each dataBean

        return ordinalItemDataMap;
    }

    public Map<Integer, List<DisplayItemBean>> sortDuplicatesIntoRows(List<DisplayItemBean> displayBeans) {

        // We know a DisplayItemBean is part of a duplicate row because its:
        // 1. ItemDataBean ordinal is greater than 1 and
        // 2. it shares the same ordinal with other members of its row.

        Map<Integer, List<DisplayItemBean>> rowMap = new HashMap<Integer, List<DisplayItemBean>>();

        List<DisplayItemBean> mapList = new ArrayList<DisplayItemBean>();
        List<DisplayItemBean> dupesList = new ArrayList<DisplayItemBean>();

        for (DisplayItemBean disBean : displayBeans) {
            if (disBean.getData().getOrdinal() > 1) {
                dupesList.add(disBean);
            }
        }

        // Now separate clusters of the same ordinal into different Lists
        int currentOrdinal = 0;
        int currentItemId = 0;
        for (DisplayItemBean disBean : dupesList) {
            if (currentItemId == 0) {
                currentItemId = disBean.getItem().getId();
                currentOrdinal = disBean.getData().getOrdinal();
                mapList.add(disBean);
                rowMap.put(currentItemId, mapList);
                continue;
            }
            if (disBean.getData().getOrdinal() == currentOrdinal) {
                mapList.add(disBean);

            } else {
                // A different ordinal means create a new row
                currentOrdinal = disBean.getData().getOrdinal();
                mapList = new ArrayList<DisplayItemBean>();
                mapList.add(disBean);
                rowMap.put(disBean.getItem().getId(), mapList);
            }
        }

        return rowMap;
    }

    private boolean listContainsDisplayItem(List<DisplayItemBean> beanList, DisplayItemBean singleBean) {

        for (DisplayItemBean disBean : beanList) {
            if (singleBean.getItem().getId() == disBean.getItem().getId()) {
                return true;
            }
        }
        return false;
    }

    public List<ItemDataBean> getItemDataBeans() {
        return itemDataBeans;
    }

    public void setItemDataBeans(List<ItemDataBean> itemDataBeans) {
        this.itemDataBeans = itemDataBeans;
    }

    public ViewBuilderUtil getViewBuilderUtil() {
        return viewBuilderUtil;
    }

    public void setViewBuilderUtil(ViewBuilderUtil viewBuilderUtil) {
        this.viewBuilderUtil = viewBuilderUtil;
    }
}
