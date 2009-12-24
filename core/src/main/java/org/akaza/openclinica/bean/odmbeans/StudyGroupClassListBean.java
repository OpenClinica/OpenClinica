/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2005 Akaza
 * Research
 *
 */
package org.akaza.openclinica.bean.odmbeans;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author ywang (Nov, 2008)
 * 
 */

public class StudyGroupClassListBean {
    private String ID;
    private String name;
    private String type;
    private String status;
    private String subjectAssignment;
    private List<org.akaza.openclinica.bean.odmbeans.StudyGroupItemBean> studyGroupItems;

    public StudyGroupClassListBean() {
        studyGroupItems = new ArrayList<org.akaza.openclinica.bean.odmbeans.StudyGroupItemBean>();
    }

    public void setID(String id) {
        this.ID = id;
    }

    public String getId() {
        return this.ID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }

    public void setSubjectAssignment(String subjectAssignment) {
        this.subjectAssignment = subjectAssignment;
    }

    public String getSubjectAssignment() {
        return this.subjectAssignment;
    }

    public void setStudyGroupItems(List<org.akaza.openclinica.bean.odmbeans.StudyGroupItemBean> studyGroupItems) {
        this.studyGroupItems = studyGroupItems;
    }

    public List<org.akaza.openclinica.bean.odmbeans.StudyGroupItemBean> getStudyGroupItems() {
        return this.studyGroupItems;
    }

}