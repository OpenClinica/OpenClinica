/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package core.org.akaza.openclinica.dao.managestudy;

import java.util.List;

import core.org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.service.DiscrepancyNotesSummary;
import core.org.akaza.openclinica.service.managestudy.ViewNotesFilterCriteria;
import core.org.akaza.openclinica.service.managestudy.ViewNotesSortCriteria;

/**
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 *
 */
public interface ViewNotesDao {

    List<DiscrepancyNoteBean> findAllDiscrepancyNotes(Study currentStudy, ViewNotesFilterCriteria filter,
                                                      ViewNotesSortCriteria sort, List<String> userTags);

    DiscrepancyNotesSummary calculateNotesSummary(Study currentStudy, ViewNotesFilterCriteria filter, boolean isQueryOnly, List<String> userTags);


}
