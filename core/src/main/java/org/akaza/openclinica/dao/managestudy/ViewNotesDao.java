/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.dao.managestudy;

import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.service.DiscrepancyNotesSummary;
import org.akaza.openclinica.service.managestudy.ViewNotesFilterCriteria;
import org.akaza.openclinica.service.managestudy.ViewNotesSortCriteria;

import java.util.List;

/**
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 *
 */
public interface ViewNotesDao {

    List<DiscrepancyNoteBean> findAllDiscrepancyNotes(StudyBean currentStudy, ViewNotesFilterCriteria filter,
            ViewNotesSortCriteria sort);

    DiscrepancyNotesSummary calculateNotesSummary(StudyBean currentStudy, ViewNotesFilterCriteria filter);


}
