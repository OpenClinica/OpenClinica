/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 *
 * Created on Sep 23, 2005
 */
package org.akaza.openclinica.web.bean;

import org.akaza.openclinica.bean.core.DiscrepancyNoteType;
import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;

import java.util.ArrayList;

/**
 * @author ssachs
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class DiscrepancyNoteRow extends EntityBeanRow {
    private DiscrepancyNoteType type;
    private ResolutionStatus status;
    private String studyName = "";
    // true if this note belongs to a site, not a study
    private boolean partOfSite = false;
    private int numChildren = 0;
    private String entityName = "";
    // String[] columns = { "Study Subject ID", "Date Created","Event
    // Date","Event", "CRF",
    // "Entity Name", "Entity Value", "Description", "Detailed Notes",
    // "# of Notes", "Resolution Status","Type", "Entity Type", "Created By",
    // "Actions" };

    public static final int COL_SUBJECT = 0;
    public static final int COL_DATE_CREATED = 1;
    //BWP added 2008, 2632
    public static final int COL_DATE_UPDATED = 2;
    public static final int COL_EVENT_DATE = 3;
    public static final int COL_EVENT = 4;
    public static final int COL_CRF = 5;
    public static final int COL_ENTITY_NAME = 6;
    public static final int COL_ENTITY_VALUE = 7;
    public static final int COL_DESCRIPTION = 8;
    public static final int COL_DETAILS = 9;
    public static final int COL_NUM_CHILDREN = 10;
    public static final int COL_RES_STATUS = 11;
    public static final int COL_TYPE = 12;
    public static final int COL_ENTITY_TYPE = 13;
    public static final int COL_OWNER = 14;
    public static final int COL_ACTIONS = 15;

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.core.EntityBeanRow#compareColumn(java.lang.Object,
     *      int)
     */
    @Override
    protected int compareColumn(Object row, int sortingColumn) {
        if (!row.getClass().equals(DiscrepancyNoteRow.class)) {
            return 0;
        }

        DiscrepancyNoteRow arg = (DiscrepancyNoteRow) row;
        DiscrepancyNoteBean thisNote = (DiscrepancyNoteBean) bean;
        DiscrepancyNoteBean argNote = (DiscrepancyNoteBean) arg.bean;

        int answer = 0;
        switch (sortingColumn) {
        case COL_SUBJECT:
            answer = thisNote.getSubjectName().toLowerCase().compareTo(argNote.getSubjectName().toLowerCase());
            break;
        case COL_EVENT:
            answer = thisNote.getEventName().toLowerCase().compareTo(argNote.getEventName().toLowerCase());
            break;
        case COL_EVENT_DATE:
            answer = compareDate(thisNote.getEventStart(), argNote.getEventStart());
            break;
        case COL_CRF:
            answer = thisNote.getCrfName().toLowerCase().compareTo(argNote.getCrfName().toLowerCase());
            break;
        case COL_DESCRIPTION:
            answer = thisNote.getDescription().toLowerCase().compareTo(argNote.getDescription().toLowerCase());
            break;
        case COL_DATE_CREATED:
            answer = compareDate(thisNote.getCreatedDate(), argNote.getCreatedDate());
            break;
        case COL_DATE_UPDATED:
            answer = compareDate(thisNote.getUpdatedDate(), argNote.getUpdatedDate());
            break;
        case COL_OWNER:
            answer = thisNote.getOwner().getName().toLowerCase().compareTo(argNote.getOwner().getName().toLowerCase());
            break;
        case COL_NUM_CHILDREN:
            answer = numChildren - arg.numChildren;
            break;
        case COL_TYPE:
            answer = thisNote.getDiscrepancyNoteTypeId() - argNote.getDiscrepancyNoteTypeId();
            break;
        case COL_RES_STATUS:
            answer = thisNote.getResolutionStatusId() - argNote.getResolutionStatusId();
            break;
        case COL_DETAILS:
            answer = thisNote.getDetailedNotes().toLowerCase().compareTo(argNote.getDetailedNotes().toLowerCase());
            break;
        case COL_ENTITY_TYPE:
            answer = thisNote.getEntityType().toLowerCase().compareTo(argNote.getEntityType().toLowerCase());
            break;
        case COL_ENTITY_NAME:
            answer = thisNote.getEntityName().toLowerCase().compareTo(argNote.getEntityName().toLowerCase());
            break;
        case COL_ENTITY_VALUE:
            answer = thisNote.getEntityValue().toLowerCase().compareTo(argNote.getEntityValue().toLowerCase());
            break;
        }

        return answer;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.core.EntityBeanRow#generatRowsFromBeans(java.util.ArrayList)
     */
    @Override
    public ArrayList generatRowsFromBeans(ArrayList beans) {
        return DiscrepancyNoteRow.generateRowsFromBeans(beans);
    }

    public static ArrayList generateRowsFromBeans(ArrayList beans) {
        ArrayList answer = new ArrayList();

        for (int i = 0; i < beans.size(); i++) {
            try {
                DiscrepancyNoteRow row = new DiscrepancyNoteRow();
                DiscrepancyNoteBean note = (DiscrepancyNoteBean) beans.get(i);
                row.setBean(note);
                answer.add(row);
            } catch (Exception e) {
            }
        }

        return answer;
    }

    public static ArrayList generateBeansFromRows(ArrayList rows) {
        ArrayList answer = new ArrayList();

        for (int i = 0; i < rows.size(); i++) {
            try {
                DiscrepancyNoteRow row = (DiscrepancyNoteRow) rows.get(i);
                DiscrepancyNoteBean bean = (DiscrepancyNoteBean) row.getBean();
                answer.add(bean);
            } catch (Exception e) {
            }
        }

        return answer;
    }

    @Override
    public void setBean(EntityBean bean) {
        super.setBean(bean);
        DiscrepancyNoteBean note = (DiscrepancyNoteBean) bean;
        type = DiscrepancyNoteType.get(note.getDiscrepancyNoteTypeId());
        status = ResolutionStatus.get(note.getResolutionStatusId());
    }

    /**
     * @return Returns the partOfSite.
     */
    public boolean isPartOfSite() {
        return partOfSite;
    }

    /**
     * @param partOfSite
     *            The partOfSite to set.
     */
    public void setPartOfSite(boolean partOfSite) {
        this.partOfSite = partOfSite;
    }

    /**
     * @return Returns the status.
     */
    public ResolutionStatus getStatus() {
        return status;
    }

    /**
     * @param status
     *            The status to set.
     */
    public void setStatus(ResolutionStatus status) {
        this.status = status;
    }

    /**
     * @return Returns the studyName.
     */
    public String getStudyName() {
        return studyName;
    }

    /**
     * @param studyName
     *            The studyName to set.
     */
    public void setStudyName(String studyName) {
        this.studyName = studyName;
    }

    /**
     * @return Returns the type.
     */
    public DiscrepancyNoteType getType() {
        return type;
    }

    /**
     * @param type
     *            The type to set.
     */
    public void setType(DiscrepancyNoteType type) {
        this.type = type;
    }

    /**
     * @return Returns the numChildren.
     */
    public int getNumChildren() {
        return numChildren;
    }

    /**
     * @param numChildren
     *            The numChildren to set.
     */
    public void setNumChildren(int numChildren) {
        this.numChildren = numChildren;
    }

    /**
     * @return Returns the entityName.
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * @param entityName
     *            The entityName to set.
     */
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    @Override
    public String getSearchString() {
        DiscrepancyNoteBean thisNote = (DiscrepancyNoteBean) bean;
        return thisNote.getSubjectName() + " " + thisNote.getDescription() + " " + thisNote.getEntityType() + " " + thisNote.getResStatus().getName() + " "
            + thisNote.getEntityName() + " " + thisNote.getEntityValue() + " " + thisNote.getCrfName() + " " + thisNote.getEventName() + " " + getType();
    }

}
