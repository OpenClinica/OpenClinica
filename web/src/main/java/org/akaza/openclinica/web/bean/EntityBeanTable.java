/*
 * Created on Dec 31, 2004
 */
package org.akaza.openclinica.web.bean;

import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.view.Link;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A class for displaying a table of EntityBean objects on the screen.
 * 
 * <p>
 * This class facilitates the display of multiple rows with the following UI
 * features:
 * <ul>
 * <li>pagination - ten rows at a time are displayed on the screen
 * <li>sorting - rows are sorted according to any column chosen by the user
 * <li>searching - rows are filtered according to one or more keywords
 * </ul>
 * 
 * <p>
 * The process for deploying these features is as follows:
 * 
 * <ol>
 * <li>Implement an EntityBeanRow class to store the rows of your table.
 * 
 * <li>In the servlet, obtain a new EntityBeanTable from the FormProcessor:
 * <br/><code>EntityBeanTable table = fp.getEntityBeanTable();</code> <br/> Note
 * that this step covers both the case where the table is being generated for
 * the first time, and the case where the table has already been displayed, and
 * the user has requested a modification to the display (e.g., go to the next
 * page or sort by a specific column) - the FormProcessor automatically reads in
 * any requested modifications from the request and applies them to the table.
 * 
 * <li>Populate the table with all of the rows you wish to display: <br/> <code>
 * ArrayList allUsers = uadao.findAll();
 * ArrayList allUsersRows = UserAccountRow.generateRows(allUsers);
 * table.setRows(allUsersRows);
 * </code>
 * 
 * <li>Populate the table with the columns you wish to display: <br/> <code>
 * String columns[] = { "username", "first name", "last name", "status", "actions"};
 * table.setColumns(new ArrayList(Arrays.asList(columns)));
 * </code>
 * 
 * <li>Populate the table with the base query which invokes the screen you're
 * about to display: <code>
 * HashMap args = new HashMap();
 * args.put("userId", userBean.getId());
 * table.setBaseQuery("ViewUserAccount", args);
 * </code>
 * 
 * <li>Force the table to compute its display: <br/>
 * <code>table.computeDisplay();</code>
 * 
 * <li>Put the table in the request: <br/><code>setTable(table);
 * <br/>This method is inherited from SecureController.
 * 
 * <li> Send the user to the JSP with forwardPage as usual.
 * </ul>
 * 
 * <p>In the JSP, the table will be displayed by include/showTable.jsp,
 * which
 * 
 * @author ssachs
 * @see EntityBeanRow
 * @see org.akaza.openclinica.control.admin.ListUserAccountsServlet <p>Method
 *      'computeDisplay()' modified by ywang to remove duplicated items when
 *      search by keywords.
 */
public class EntityBeanTable {
    /**
     * The number of rows to display per page.
     */
    public static final int NUM_ROWS_PER_PAGE = 10;

    /**
     * All of the rows which might be displyaed, before computeDisplay() is
     * called. All of the rows which will be displayed (after applying the
     * tabling parameters), after computeDisplay() is called. Each element is an
     * <a href="{@docRoot}
     * /org/akaza/openclinica/core/EntityBeanRow.html">EntityBeanRow</a>s
     */
    protected ArrayList rows;

    /**
     * An array of EntityBeanColumn objects which represent column headings.
     */
    protected ArrayList columns;

    /**
     * Always equal to columns.size(); maintained by setColumns.
     */
    protected int numColumns;

    /**
     * Which page are we viewing now, ranges from 1 to
     * ceil(rows.size()/NUM_ROWS_PER_PAGE)
     */
    protected int currPageNumber;

    /**
     * How many page numbers are there total,always equal to ceil(rows.size() /
     * NUM_ROWS_PER_PAGE); maintained by setRows.
     */
    protected int totalPageNumbers;

    /**
     * Indicates the column we're currently sorting by. Ranges form 0 to
     * columns.size() - 1; interpreted as an index into columns.
     */
    protected int sortingColumnInd;

    /**
     * Indicates whether the sorting column was explicitly set in the GET or
     * POST variables. This makes the functionality in
     * setSortingIfNotExplicitlySet possible.
     */
    protected boolean sortingColumnExplicitlySet = false;

    /**
     * <code>true</code> if we're sorting in ascending order, <code>false</code>
     * otherwise
     */
    protected boolean ascendingSort;

    /**
     * <code>true</code> if we use the keyword filter <code>false</code>
     * otherwise
     */
    protected boolean filtered;

    /**
     * String the user wants to filter the rows by.
     */
    protected String keywordFilter;

    /**
     * <code>true</code> if we are viewing at most NUM_ROWS_PER_PAGE at a time
     * <code>false</code> if we are viewing all of the pages on one screen
     */
    protected boolean paginated = true;

    /**
     * A set of links to display in the upper-right hand corner. Each element is
     * a Link object.
     */
    protected ArrayList links;

    protected String postAction;
    protected HashMap postArgs;
    protected String baseGetQuery;

    protected String noRowsMessage;
    protected String noColsMessage;

    public EntityBeanTable() {
        rows = new ArrayList();
        columns = new ArrayList();
        numColumns = 0;

        currPageNumber = 1;
        totalPageNumbers = 0;
        sortingColumnInd = 0;
        ascendingSort = true;
        filtered = false;
        keywordFilter = "";

        postAction = "";
        postArgs = new HashMap();
        baseGetQuery = "";

        noRowsMessage = "";
        noColsMessage = "";

        links = new ArrayList();
    }

    /**
     * @return Returns the totalPageNumbers.
     */
    public int getTotalPageNumbers() {
        return totalPageNumbers;
    }

    /**
     * @return Returns the columns.
     */
    public ArrayList getColumns() {
        return columns;
    }

    /**
     * @param columns
     *            The columns to set. Each element is a String with the column
     *            name.
     */
    public void setColumns(ArrayList columns) {
        // this.columns = columns;
        ArrayList newColumns = new ArrayList();

        for (int i = 0; i < columns.size(); i++) {
            String name = (String) columns.get(i);
            EntityBeanColumn c = new EntityBeanColumn();
            c.setName(name);
            newColumns.add(c);
        }

        this.columns = newColumns;
        numColumns = this.columns.size();
    }

    /**
     * @return Returns the ascendingSort.
     */
    public boolean isAscendingSort() {
        return ascendingSort;
    }

    /**
     * @return Returns the currPageNumber.
     */
    public int getCurrPageNumber() {
        return currPageNumber;
    }

    /**
     * @return Returns the keywordFilter.
     */
    public String getKeywordFilter() {
        return keywordFilter;
    }

    /**
     * @return Returns the rows.
     */
    public ArrayList getRows() {
        return rows;
    }

    /**
     * Re-computes the correct value of page numbers as: <code>rows.size() /
     * NUM_ROWS_PER_PAGE</code>
     */
    private void updateTotalPageNumbers() {
        totalPageNumbers = rows.size() / NUM_ROWS_PER_PAGE;

        if (rows.size() > totalPageNumbers * NUM_ROWS_PER_PAGE) {
            totalPageNumbers++;
        }

    }

    /**
     * 
     * @param rows
     */
    public void setRows(ArrayList rows) {
        this.rows = rows;
        updateTotalPageNumbers();
    }

    // /**
    // * Adds an entity to display at the bottom of the table.
    // * @param e The entity to add to the table.
    // */
    // public void addRow(EntityBean e) {
    // rows.add(e);
    // updateTotalPageNumbers();
    // }

    /**
     * @return Returns the filtered.
     */
    public boolean isFiltered() {
        return filtered;
    }

    /**
     * @return Returns the sortingColumnInd.
     */
    public int getSortingColumnInd() {
        return sortingColumnInd;
    }

    /**
     * @return Returns the numColumns.
     */
    public int getNumColumns() {
        return numColumns;
    }

    public void setQuery(String baseURL, HashMap args) {
    	 setQuery(  baseURL,   args, false) ;
    }
    public void setQuery(String baseURL, HashMap args, boolean isUTFEncode) {
        postAction = baseURL;
        postArgs = args;

        baseGetQuery = baseURL + "?";
        baseGetQuery += FormProcessor.FIELD_SUBMITTED + "=" + 1;
        String value = null; 
        for ( Object key : args.keySet()){
        	value = (String) args.get(key);
        	if (isUTFEncode){
            	try{
            		value = URLEncoder.encode((String) args.get(key), "UTF-8");
            	}catch(UnsupportedEncodingException ev){
            		value = (String) args.get(key);
            	}
            }
        	baseGetQuery += "&" + (String)key + "=" + value;
        }
       

    }
    
    

    /**
     * @return Returns the baseGetQuery.
     */
    public String getBaseGetQuery() {
        return baseGetQuery;
    }

    /**
     * @return Returns the postAction.
     */
    public String getPostAction() {
        return postAction;
    }

    /**
     * @return Returns the postArgs.
     */
    public HashMap getPostArgs() {
        return postArgs;
    }

    /**
     * @param ascendingSort
     *            The ascendingSort to set.
     */
    public void setAscendingSort(boolean ascendingSort) {
        this.ascendingSort = ascendingSort;
    }

    /**
     * @param currPageNumber
     *            The currPageNumber to set.
     */
    public void setCurrPageNumber(int currPageNumber) {
        this.currPageNumber = currPageNumber;
    }

    /**
     * @param filtered
     *            The filtered to set.
     */
    public void setFiltered(boolean filtered) {
        this.filtered = filtered;
    }

    /**
     * @param keywordFilter
     *            The keywordFilter to set.
     */
    public void setKeywordFilter(String keywordFilter) {
        this.keywordFilter = keywordFilter;
    }

    /**
     * @param sortingColumnInd
     *            The sortingColumnInd to set.
     */
    public void setSortingColumnInd(int sortingColumnInd) {
        this.sortingColumnInd = sortingColumnInd;
    }

    /**
     * @return Returns the noColsMessage.
     */
    public String getNoColsMessage() {
        return noColsMessage;
    }

    /**
     * @return Returns the noRowsMessage.
     */
    public String getNoRowsMessage() {
        return noRowsMessage;
    }

    /**
     * @return Returns the paginated.
     */
    public boolean isPaginated() {
        return paginated;
    }

    /**
     * @param paginated
     *            The paginated to set.
     */
    public void setPaginated(boolean paginated) {
        this.paginated = paginated;
    }

    /**
     * Compute the subset of rows which should be shown on the screen. Note that
     * the tabling parameters should be set properly before this method is
     * called!
     */
    public void computeDisplay() {
        ArrayList displayRows;
        Set temprows = new HashSet();

        // *****************
        // FILTER BY KEYWORD
        // *****************

        // the filter is considered to have been executed if the keyword filter
        // bit is on,
        // and if there is at least one keyword to search by
        boolean filterExecuted = false;

        displayRows = new ArrayList();
        if (filtered) {

            String[] keywords = null;
            if (keywordFilter != null) {
                if (keywordFilter.startsWith(" ")) {
                    // the search allows the user to implement a search such as
                    // " 20 " thus
                    // searching only for space char-20-space char alone, not
                    // 1920, for example
                    keywords = new String[] { keywordFilter };
                } else {
                    // split the keywords on a space character, or by commas

                    keywords = keywordFilter.split("\\s");
                }
            }

            if (keywords != null) {
                for (int j = 0; j < keywords.length; j++) {
                    String keyword = keywords[j];
                    if (keyword == null || "".equals(keyword)) {
                        continue;
                    }

                    keyword = keyword.toLowerCase();

                    filterExecuted = true;

                    loopRows: for (int i = 0; i < rows.size(); i++) {
                        EntityBeanRow row = (EntityBeanRow) rows.get(i);

                        String searchString = row.getSearchString().toLowerCase();
                        // If the keyword matches the whole search string,
                        // return a match
                        if (searchString.equalsIgnoreCase(keyword)) {
                            temprows.add(row);
                            // continue searching the next row
                            continue loopRows;
                        }
                        // if the searchString contains "-" chars such as
                        // ATS-120-5
                        // then split the searchString on
                        // that character, and determine if any components of
                        // the split result match the
                        // keyword; SEE issue 2640

                        if (searchString.contains("-")) {

                            // The searchString is the combination of the
                            // subject's primary
                            // and secondary identifiers

                            // First split the search string on a space, to
                            // accomodate
                            // substring searches on a search string like
                            // subject id - secondary id
                            String[] newSearchString = searchString.split(" ");

                            String[] subStrings = null;
                            // each component is half of a string like
                            // "subject id  secondary id"
                            for (String component : newSearchString) {
                                // if the entire split searchString matches the
                                // keyword...
                                // if (component.indexOf(keyword) >= 0){
                                if (component.equalsIgnoreCase(keyword)) {
                                    temprows.add(row);
                                    // continue searching the next row
                                    continue loopRows;
                                }

                                // if the component does contain a "-" but the
                                // entire
                                // component does not match the keyword
                                subStrings = component.split("-");
                                for (String innerStr : subStrings) {
                                    // An exact match has been requested here;
                                    // see 2640
                                    // This allows the breaking up of ids like
                                    // ATS-120-5 and
                                    // and exact seaches on the separate parts
                                    // like 120
                                    if (innerStr.equalsIgnoreCase(keyword)) {
                                        temprows.add(row);

                                    }
                                }
                            }
                            // continue to the next row, because the
                            // searchString contained a "-",
                            // and the keyword was searched for in both ways,
                            // with and without
                            // splitting on "-"
                            continue;
                        } // end searchString.contains("-")
                        // the search string doesn't contain "-"
                        if (searchString.indexOf(keyword) >= 0) {
                            temprows.add(row);
                        }
                    } // end of loop iterating over rows
                } // end of loop iterating over keywords
            }
            Iterator it = temprows.iterator();
            while (it.hasNext()) {
                displayRows.add(it.next());
            }
        } // end of filtering by keywords

        if (!filterExecuted) {
            displayRows = rows;
        }

        // this seems redundant, since we set the rows property below before
        // returning from the method
        // the reason for this call is to reset the totalNumPages property,
        // to reflect the number of rows that matched the search terms (if any)
        setRows(displayRows);

        // *************
        // SORT THE ROWS
        // *************

        for (int i = 0; i < displayRows.size(); i++) {
            EntityBeanRow row = (EntityBeanRow) displayRows.get(i);
            row.setSortingColumn(sortingColumnInd);
            row.setAscendingSort(ascendingSort);
            displayRows.set(i, row);
        }
        Collections.sort(displayRows);

        // ****************
        // APPLY PAGINATION
        // ****************
        if (paginated) {
            if (currPageNumber < 1) {
                currPageNumber = 1;
            }
            if (currPageNumber > totalPageNumbers && totalPageNumbers > 0) {
                currPageNumber = totalPageNumbers;
            }

            int firstInd = (currPageNumber - 1) * NUM_ROWS_PER_PAGE;
            int lastInd = currPageNumber * NUM_ROWS_PER_PAGE;
            lastInd = lastInd > displayRows.size() ? displayRows.size() : lastInd;

            // JRWS>> This block added to catch issue 1223, where searching a
            // large list of studies fails when search criteria result in zero
            // studies in the list, but you are on the third page of the list
            // when you perform the search
            if (firstInd > lastInd && lastInd == 0) {
                firstInd = 0;
            }

            ArrayList currPage = new ArrayList(displayRows.subList(firstInd, lastInd));

            // it's important not to use setRows here
            // calling setRows will change totalNumPages to be the number of
            // pages in currPage (always 1)
            // we don't want to change totalNumPages since it'll screw up the
            // display of "Previous" and "Next" page links
            rows = currPage;
        } else {
            rows = displayRows;
        }
    }

    /**
     * @return Returns the links.
     */
    public ArrayList getLinks() {
        return links;
    }

    /**
     * @param links
     *            The links to set.
     */
    public void setLinks(ArrayList links) {
        this.links = links;
    }

    public void addLink(String caption, String url) {
        Link l = new Link(caption, url);
        links.add(l);
    }

    /**
     * @return Returns the sortingColumnExplicitlySet.
     */
    public boolean isSortingColumnExplicitlySet() {
        return sortingColumnExplicitlySet;
    }

    /**
     * @param sortingColumnExplicitlySet
     *            The sortingColumnExplicitlySet to set.
     */
    public void setSortingColumnExplicitlySet(boolean sortingColumnExplicitlySet) {
        this.sortingColumnExplicitlySet = sortingColumnExplicitlySet;
    }

    public void setSortingIfNotExplicitlySet(int sortingColumnInd, boolean ascendingSort) {
        if (!sortingColumnExplicitlySet) {
            this.sortingColumnInd = sortingColumnInd;
            this.ascendingSort = ascendingSort;
        }
    }

    /**
     * Signal that the column at index <code>i</code> should not be displayed
     * with a link in the JSP; this prevents users from sorting on the i-th
     * column.
     * 
     * @param i
     *            The index of the column whose link should not be displayed.
     *            The first column is 0.
     */
    public void hideColumnLink(int i) {
        if (i >= 0 && i < columns.size()) {
            EntityBeanColumn c = (EntityBeanColumn) columns.get(i);
            c.setShowLink(false);
            columns.set(i, c);
        }
    }
}