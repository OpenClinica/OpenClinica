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
import org.akaza.openclinica.log.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 *
 */
public class ViewNotesServiceImpl implements ViewNotesService {

    private static final Logger LOG = LoggerFactory.getLogger(ViewNotesServiceImpl.class);

    private ViewNotesDao viewNotesDao;

    public List<DiscrepancyNoteBean> listNotes(StudyBean currentStudy,
            ViewNotesFilterCriteria filter, ViewNotesSortCriteria sort) {
        Stopwatch sw = Stopwatch.createAndStart("listNotes");
        List<DiscrepancyNoteBean> result = viewNotesDao.findAllDiscrepancyNotes(currentStudy, filter, sort);
        sw.stop();
        return result;
    }

    public ViewNotesDao getViewNotesDao() {
        return viewNotesDao;
    }

    public void setViewNotesDao(ViewNotesDao viewNotesDao) {
        this.viewNotesDao = viewNotesDao;
    }


}
