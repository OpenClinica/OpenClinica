/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 *
 * Created on Jul 7, 2005
 */
package org.akaza.openclinica.bean.extract;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.submit.ItemBean;

/**
 * @author ssachs
 */
public class SEDColumn {
    private StudyEventDefinitionBean studyEventDefinition;
    private CRFBean crf;
    private ItemBean item;

    public SEDColumn(StudyEventDefinitionBean sedb, CRFBean cb, ItemBean ib) {
        this.studyEventDefinition = sedb;
        this.crf = cb;
        this.item = ib;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!obj.getClass().equals(this.getClass())) {
            return false;
        }
        if (obj == null) {
            return false;
        }

        SEDColumn other = (SEDColumn) obj;
        return other.studyEventDefinition.getId() == studyEventDefinition.getId() && other.crf.getId() == crf.getId() && other.item.getId() == item.getId();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        // the class contract states
        // o1.equals(o2) <=> o1.hashCode() == o2.hashCode()
        String s = new String(studyEventDefinition.getId() + "-" + crf.getId() + "-" + item.getId());
        return s.hashCode();
    }

    /**
     * @return Returns the crf.
     */
    public CRFBean getCrf() {
        return crf;
    }

    /**
     * @return Returns the item.
     */
    public ItemBean getItem() {
        return item;
    }

    /**
     * @return Returns the studyEventDefinition.
     */
    public StudyEventDefinitionBean getStudyEventDefinition() {
        return studyEventDefinition;
    }
}
