/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.service.managestudy;

import java.util.List;

import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.managestudy.ViewNotesDao;
import org.akaza.openclinica.service.DiscrepancyNotesSummary;
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
    public List<DiscrepancyNoteBean> listNotes(StudyBean currentStudy,
            ViewNotesFilterCriteria filter, ViewNotesSortCriteria sort) {
        List<DiscrepancyNoteBean> result = viewNotesDao.findAllDiscrepancyNotes(currentStudy, filter, sort);
        LOG.debug("Found " + result.size() + " discrepancy notes");
        return result;
    }

    @Override
    public DiscrepancyNotesSummary calculateNotesSummary(StudyBean currentStudy,
            ViewNotesFilterCriteria filter, boolean isQueryOnly) {
        DiscrepancyNotesSummary result = viewNotesDao.calculateNotesSummary(currentStudy, filter, isQueryOnly);
        return result;
    }

    public ViewNotesDao getViewNotesDao() {
        return viewNotesDao;
    }

    public void setViewNotesDao(ViewNotesDao viewNotesDao) {
        this.viewNotesDao = viewNotesDao;
    }


}
