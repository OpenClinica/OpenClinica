/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package core.org.akaza.openclinica.web.bean;

import core.org.akaza.openclinica.bean.login.StudyDTO;
import core.org.akaza.openclinica.domain.datamap.Study;

import java.util.ArrayList;

/**
 * @author jxu
 *
 */
public class StudyRow extends EntityBeanRow {

    private StudyDTO studyDTO;
    // columns:
    public static final int COL_NAME = 0;
    public static final int COL_UNIQUEIDENTIFIER = 1;
    public static final int COL_PRINCIPAL_INVESTIGATOR = 2;
    public static final int COL_FACILITY_NAME = 3;
    public static final int COL_DATE_CREATED = 4;
    public static final int COL_STATUS = 5;

    /*
     * (non-Javadoc)
     *
     * @see core.org.akaza.openclinica.core.EntityBeanRow#compareColumn(java.lang.Object,
     *      int)
     */
    @Override
    protected int compareColumn(Object row, int sortingColumn) {
        if (!row.getClass().equals(StudyRow.class)) {
            return 0;
        }

        StudyDTO thisStudy = (StudyDTO) studyDTO;
        StudyDTO argStudy = (StudyDTO) ((StudyRow) row).studyDTO;

        int answer = 0;
        switch (sortingColumn) {
        case COL_NAME:
            answer = thisStudy.getBriefTitle().toLowerCase().compareTo(argStudy.getBriefTitle().toLowerCase());
            break;
        case COL_UNIQUEIDENTIFIER:
            answer = thisStudy.getUniqueProtocolID().toLowerCase().compareTo(argStudy.getUniqueProtocolID().toLowerCase());
            break;
        case COL_PRINCIPAL_INVESTIGATOR:
            answer = thisStudy.getPrincipalInvestigator().toLowerCase().compareTo(argStudy.getPrincipalInvestigator().toLowerCase());
            break;
        case COL_FACILITY_NAME:
            answer = thisStudy.getFacilityName().toLowerCase().compareTo(argStudy.getFacilityName().toLowerCase());
            break;
        case COL_DATE_CREATED:
            answer = compareDate(thisStudy.getCreatedDate(), argStudy.getCreatedDate());
            break;
        case COL_STATUS:
            answer = thisStudy.getStatus().compareTo(argStudy.getStatus());
            break;
        }

        return answer;
    }

    @Override
    public String getSearchString() {
        StudyDTO thisStudy =  studyDTO;
        return thisStudy.getBriefTitle() + " " + thisStudy.getUniqueProtocolID() + " " + thisStudy.getPrincipalInvestigator() + " " + thisStudy.getFacilityName();
    }

    /*
     * (non-Javadoc)
     *
     * @see core.org.akaza.openclinica.core.EntityBeanRow#generatRowsFromBeans(java.util.ArrayList)
     */
    @Override
    public ArrayList generatRowsFromBeans(ArrayList beans) {
        return StudyRow.generateRowsFromBeans(beans);
    }

    public static ArrayList generateRowsFromBeans(ArrayList<Study> beans) {
        ArrayList answer = new ArrayList();

        Class[] parameters = null;
        Object[] arguments = null;

        for (int i = 0; i < beans.size(); i++) {
            try {
                StudyRow row = new StudyRow();

                row.setStudyDTO(StudyDTO.studyToStudyDTO(beans.get(i)));

                answer.add(row);
            } catch (Exception e) {
            }
        }

        return answer;
    }

    public StudyDTO getStudyDTO() {
        return studyDTO;
    }

    public void setStudyDTO(StudyDTO studyDTO) {
        this.studyDTO = studyDTO;
    }

}
