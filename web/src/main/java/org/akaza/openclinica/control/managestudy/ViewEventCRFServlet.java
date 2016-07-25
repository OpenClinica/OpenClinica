/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.DisplayItemBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.dao.submit.SectionDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.util.ArrayList;

/**
 * @author jxu
 *
 * Views the detail of an event CRF
 */
public class ViewEventCRFServlet extends SecureController {
    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {

    }

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        int eventCRFId = fp.getInt("id", true);
        int studySubId = fp.getInt("studySubId", true);

        StudySubjectDAO subdao = new StudySubjectDAO(sm.getDataSource());
        EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());
        ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource());
        ItemDAO idao = new ItemDAO(sm.getDataSource());
        ItemFormMetadataDAO ifmdao = new ItemFormMetadataDAO(sm.getDataSource());
        CRFDAO cdao = new CRFDAO(sm.getDataSource());
        SectionDAO secdao = new SectionDAO(sm.getDataSource());

        if (eventCRFId == 0) {
            addPageMessage(respage.getString("please_choose_an_event_CRF_to_view"));
            forwardPage(Page.LIST_STUDY_SUBJECTS);
        } else {
            StudySubjectBean studySub = (StudySubjectBean) subdao.findByPK(studySubId);
            request.setAttribute("studySub", studySub);

            EventCRFBean eventCRF = (EventCRFBean) ecdao.findByPK(eventCRFId);
            CRFBean crf = cdao.findByVersionId(eventCRF.getCRFVersionId());
            request.setAttribute("crf", crf);

            ArrayList sections = secdao.findAllByCRFVersionId(eventCRF.getCRFVersionId());
            for (int j = 0; j < sections.size(); j++) {
                SectionBean section = (SectionBean) sections.get(j);
                ArrayList itemData = iddao.findAllByEventCRFId(eventCRFId);

                ArrayList displayItemData = new ArrayList();
                for (int i = 0; i < itemData.size(); i++) {
                    ItemDataBean id = (ItemDataBean) itemData.get(i);
                    DisplayItemBean dib = new DisplayItemBean();
                    ItemBean item = (ItemBean) idao.findByPK(id.getItemId());
                    ItemFormMetadataBean ifm = ifmdao.findByItemIdAndCRFVersionId(item.getId(), eventCRF.getCRFVersionId());

                    item.setItemMeta(ifm);
                    dib.setItem(item);
                    dib.setData(id);
                    dib.setMetadata(ifm);
                    displayItemData.add(dib);
                }
                section.setItems(displayItemData);
            }

            request.setAttribute("sections", sections);
            request.setAttribute("studySubId", new Integer(studySubId).toString());
            forwardPage(Page.VIEW_EVENT_CRF);
        }
    }

}
