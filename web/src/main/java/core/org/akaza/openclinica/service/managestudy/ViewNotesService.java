/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package core.org.akaza.openclinica.service.managestudy;

import java.util.List;

import core.org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import core.org.akaza.openclinica.bean.managestudy.StudyBean;
import core.org.akaza.openclinica.service.DiscrepancyNotesSummary;

/**
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 *
 */
public interface ViewNotesService {

    List<DiscrepancyNoteBean> listNotes(StudyBean currentStudy, ViewNotesFilterCriteria filter,
            ViewNotesSortCriteria sort, List<String> userTags);

    DiscrepancyNotesSummary calculateNotesSummary(StudyBean currentStudy, ViewNotesFilterCriteria filter, boolean isQueryOnly, List<String> userTags);

}
