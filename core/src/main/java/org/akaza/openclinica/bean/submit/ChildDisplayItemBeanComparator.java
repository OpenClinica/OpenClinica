/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.submit;

import java.util.Comparator;

/**
 * @author ssachs
 */
public class ChildDisplayItemBeanComparator implements Comparator {
    private static ChildDisplayItemBeanComparator instance = null;

    private ChildDisplayItemBeanComparator() {

    }

    public static ChildDisplayItemBeanComparator getInstance() {
        if (instance == null) {
            instance = new ChildDisplayItemBeanComparator();
        }
        return instance;
    }

    /**
     * Sorts DisplayItemBean objects first by column number, then by ordinal.
     * Should only be used on DisplayItemBean objects which correspond to child
     * items, that is, items with parentId != 0.
     *
     * @param o1
     *            The first obje
     *
     */
    public int compare(Object o1, Object o2) {
        if (o1 == null || o2 == null) {
            return 0;
        }

        if (!o1.getClass().equals(o2.getClass())) {
            return 0;
        }

        if (!o1.getClass().equals(DisplayItemBean.class)) {
            return 0;
        }

        DisplayItemBean child1 = (DisplayItemBean) o1;
        DisplayItemBean child2 = (DisplayItemBean) o2;

        int column1 = child1.getMetadata().getColumnNumber();
        int column2 = child2.getMetadata().getColumnNumber();

        int ordinal1 = child1.getMetadata().getOrdinal();
        int ordinal2 = child2.getMetadata().getOrdinal();

        return column1 != column2 ? column1 - column2 : ordinal1 - ordinal2;
    }

}
