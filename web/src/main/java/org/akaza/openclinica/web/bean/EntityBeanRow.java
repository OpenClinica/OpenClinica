/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.web.bean;

import org.akaza.openclinica.bean.core.EntityBean;

import java.util.ArrayList;
import java.util.Date;

/**
 * <p>
 * This class represents a row in an EntityBeanTable. Its purpose is to assist
 * that class with the sorting and searching functions.
 *
 * <p>
 * When an EntityBeanTable wants to sort by a particular column, it sets the
 * sortingColumn and ascendingSort properties of each of its member
 * EntityBeanRows. It then calls Collections.sort(rows) to do the sorting;
 * because this class implements Comparable, this results in several calls to
 * this class's compareTo method. This class's compareTo method calls the
 * abstract compareColumn(Object, int) method.
 *
 *
 * <p>
 * When an EntityBeanTable wants to see if one of its member EntityBeanRows
 * matches a given keyword, it calls getSearchString().indexOf(keyword); if the
 * result is non-negative, the row matches.
 *
 * <p>
 * Consequently, this means the following:
 * <ul>
 * <li> The subclass's compareColumn method must compare two beans based on the
 * value of one column only. Comparisons on each column displayed by the table
 * must be supported.
 *
 * <li> The subclass's getSearchString() method must aggregate all of the data
 * which is to be searched by the table's search feature.
 * </ul>
 *
 * <p>
 * Furhter, the subclass must implement generateRowsFromBeans as a matter of
 * convenience to the developer. Most DAOs provide results in the form of an
 * ArrayList of EntityBeans; however the EntityBeanTable expects an ArrayList of
 * EntityBeanRow objects. The generatRowsFromBeans method helps bridge this gap.
 * A template for such code is provided in the Javadoc.
 *
 * <p>
 * Finally, the developer may encounter some inconvenience if the rows to be
 * displayed are not EntityBean objects. In this case, it is recommended to
 * create a new "dummy" EntityBean subclass, which contains the objects to be
 * displayed in the rows as properties.
 *
 * @author ssachs
 * @see EntityBeanTable
 * @see UserAccountRow
 */
public abstract class EntityBeanRow implements Comparable {
    /**
     * The object which will be displayed.
     */
    protected EntityBean bean;

    /**
     * The column we are sorting by.
     */
    private int sortingColumn;

    /**
     * <code>true</code> if we are sorting in ascending order,
     * <code>false</code> otherwise
     */
    private boolean ascendingSort;

    public EntityBeanRow() {
        bean = new EntityBean();
        sortingColumn = 0;
        ascendingSort = true;
    }

    /**
     * <p>
     * Compare this row to the argument row in the specified column.
     * <p>
     * <b>NB</b>: This method must take care to implement case-insensitive
     * search if that is desired. Typically this is accomplished in a manner
     * similar to the following:
     *
     * <code>
     * String n1 = bean1.getName();
     * String n2 = bean2.getName();
     * n1 = n1.toLowerCase();
     * n2 = n2.toLowerCase();
     * return n1.compareTo(n2);
     * </code>
     *
     * @param row
     * @param sortingColumn
     * @return -1 if this row has a value in the specified column that is less
     *         than the argument row's 0 if the two rows have an equal value in
     *         the specified column 1 if this row has a value in the specified
     *         column that is greater than the argument row's
     */
    protected abstract int compareColumn(Object row, int sortingColumn);

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object row) {
        if (ascendingSort) {
            return compareColumn(row, sortingColumn);
        } else {
            return -1 * compareColumn(row, sortingColumn);
        }
    }

    /**
     * Compares two date objects, handle null case
     */
    public int compareDate(Date d1, Date d2) {
        if (d1 == null && d2 != null) {
            return -1;
        } else if (d1 != null && d2 == null) {
            return 1;
        } else if (d1 == null && d2 == null) {
            return 0;
        } else {
            return d1.compareTo(d2);
        }
    }

    /**
     * <p>
     * This method is used by EntityBeanList when filtering. In particular, when
     * the user specifies a filter keyword, the EntityBeanList searches through
     * the String returned by this method to determine if this EntityBean should
     * be displayed on the screen.
     *
     * <p>
     * The default behavior is to return the bean's name. Subclasses for which
     * this behavior is inappropriate should override this method.
     *
     * @return The String which represents this bean for purposes of filtering
     *         in EntityBeanList.
     */
    public String getSearchString() {
        return bean.getName();
    }

    /**
     * A convenience member for generating a list of EntityBeanRows from a list
     * of EntityBeans. Code similar to the following is recommended:
     *
     * <code>
     public ArrayList generatRowsFromBeans(ArrayList beans) {
    	ArrayList answer = new ArrayList();

    	Class[] parameters = null;
    	Object[] arguments = null;

    	for (int i = 0; i < beans.size(); i++) {
    		try {
    			EntityBeanRow row = new EntityBeanRow();
    			row.setBean((EntityBean) beans.get(i));
    			answer.add(row);
    		} catch (Exception e) { }
    	}

    	return answer;
     }
     </code>
     *
     * <p>
     * Code that's more specific to the subclass is obviously recommended.
     *
     * @param beans
     *            A set of EntityBeans which are to be displayed.
     * @return A set of EntityBeanRows in the same order as beans, where each
     *         element in the result has its bean property set to the
     *         corresponding value in the beans argument.
     */
    public abstract ArrayList generatRowsFromBeans(ArrayList beans);

    /**
     * @return Returns the bean.
     */
    public EntityBean getBean() {
        return bean;
    }

    /**
     * @param bean
     *            The bean to set.
     */
    public void setBean(EntityBean bean) {
        this.bean = bean;
    }

    /**
     * @return Returns the sortingColumn.
     */
    public int getSortingColumn() {
        return sortingColumn;
    }

    /**
     * @param sortingColumn
     *            The sortingColumn to set.
     */
    public void setSortingColumn(int sortingColumn) {
        this.sortingColumn = sortingColumn;
    }

    /**
     * @return Returns the ascendingSort.
     */
    public boolean isAscendingSort() {
        return ascendingSort;
    }

    /**
     * @param ascendingSort
     *            The ascendingSort to set.
     */
    public void setAscendingSort(boolean ascendingSort) {
        this.ascendingSort = ascendingSort;
    }
}
