/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.view.Page;

/**
 * Processes request to change ordinals of study event definitions in a study
 *
 * @author jxu
 */
public class ChangeDefinitionOrdinalServlet extends ChangeOrdinalServlet {

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        int current = fp.getInt("current");
        int previous = fp.getInt("previous");
        StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());
        increase(current, previous, seddao);
        String url=response.encodeRedirectURL("ListEventDefinition");
        response.sendRedirect(url);
//        forwardPage(Page.LIST_DEFINITION_SERVLET);

    }

    /**
     * increase the ordinal for current object and decrease the ordinal of the
     * previous one
     *
     * @param idCurrent
     * @param idPrevious
     */
    private void increase(int idCurrent, int idPrevious, StudyEventDefinitionDAO dao) {

        if (idCurrent > 0) {
            StudyEventDefinitionBean current = (StudyEventDefinitionBean) dao.findByPK(idCurrent);

            int currentOrdinal = current.getOrdinal();
            current.setOrdinal(currentOrdinal - 1);
            dao.update(current);
        }
        if (idPrevious > 0) {
            StudyEventDefinitionBean previous = (StudyEventDefinitionBean) dao.findByPK(idPrevious);
            int previousOrdinal = previous.getOrdinal();
            previous.setOrdinal(previousOrdinal + 1);

            dao.update(previous);
        }

    }

}