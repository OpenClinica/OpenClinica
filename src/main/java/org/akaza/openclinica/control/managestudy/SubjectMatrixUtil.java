package org.akaza.openclinica.control.managestudy;


/**
 * A Utility class for the issues involving the Subject matrix or table
 * @author Bruce W. Perry 3/2009
 */
public class SubjectMatrixUtil {

    /**
     * Create an extended query string for a URL, based on a page number like
     * "2" passed into the method.
     * @param pageNumber  A String
     * @return A String representing the entire query string
     */
    public String createPaginatingQuery(String pageNumber){

        StringBuilder paginatingQuery = new StringBuilder("");
        if(pageNumber != null && (! "".equalsIgnoreCase(pageNumber))){
            int tempNum = 0;
            try {
                tempNum = Integer.parseInt(pageNumber);
            } catch(NumberFormatException nfe) {
                // tempNum is already initialized to 0
            }
            if(tempNum > 0){
                paginatingQuery = new StringBuilder(ListStudySubjectServlet.SUBJECT_PAGE_NUMBER).
                  append("=").append(pageNumber);
                paginatingQuery.append("&ebl_paginated=1");
            }
        }
        return paginatingQuery.toString();
    }
}
