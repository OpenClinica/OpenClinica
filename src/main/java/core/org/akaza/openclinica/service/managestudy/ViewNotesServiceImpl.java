/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package core.org.akaza.openclinica.service.managestudy;

import java.util.List;

import core.org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import core.org.akaza.openclinica.dao.managestudy.ViewNotesDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.service.DiscrepancyNotesSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 *
 */
public class ViewNotesServiceImpl implements ViewNotesService {

    private static final Logger LOG = LoggerFactory.getLogger(ViewNotesServiceImpl.class);

    private ViewNotesDao viewNotesDao;

    @Override
    public List<DiscrepancyNoteBean> listNotes(Study currentStudy,
                                               ViewNotesFilterCriteria filter, ViewNotesSortCriteria sort, List<String> userTags) {
        List<DiscrepancyNoteBean> result = viewNotesDao.findAllDiscrepancyNotes(currentStudy, filter, sort, userTags);
        LOG.debug("Found " + result.size() + " discrepancy notes");
        return result;
    }

    @Override
    public DiscrepancyNotesSummary calculateNotesSummary(Study currentStudy,
            ViewNotesFilterCriteria filter, boolean isQueryOnly, List<String> userTags) {
        DiscrepancyNotesSummary result = viewNotesDao.calculateNotesSummary(currentStudy, filter, isQueryOnly, userTags);
        return result;
    }

    public ViewNotesDao getViewNotesDao() {
        return viewNotesDao;
    }

    public void setViewNotesDao(ViewNotesDao viewNotesDao) {
        this.viewNotesDao = viewNotesDao;
    }


}
