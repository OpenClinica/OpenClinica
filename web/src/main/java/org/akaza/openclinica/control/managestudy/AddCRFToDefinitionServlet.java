/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.domain.SourceDataVerification;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.bean.CRFRow;
import org.akaza.openclinica.web.bean.EntityBeanTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Processes request to add new CRFs info study event definition
 * 
 * @author jxu
 */
public class AddCRFToDefinitionServlet extends SecureController {

    /**
     * Checks whether the user has the correct privilege
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }
        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_permission_to_update_study_event_definition") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_study_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {

        CRFDAO cdao = new CRFDAO(sm.getDataSource());
        ArrayList crfs = (ArrayList) cdao.findAllByStatus(Status.AVAILABLE);
        ArrayList edcs = (ArrayList) session.getAttribute("eventDefinitionCRFs");
        if (edcs == null) {
            edcs = new ArrayList();
        }
        HashMap crfIds = new HashMap();
        for (int i = 0; i < edcs.size(); i++) {
            EventDefinitionCRFBean edc = (EventDefinitionCRFBean) edcs.get(i);
            Integer crfId = new Integer(edc.getCrfId());
            crfIds.put(crfId, edc);
        }
        for (int i = 0; i < crfs.size(); i++) {
            CRFBean crf = (CRFBean) crfs.get(i);
            if (crfIds.containsKey(new Integer(crf.getId()))) {
                crf.setSelected(true);
            }
        }

        String action = request.getParameter("action");
        if (StringUtil.isBlank(action)) {
            // request.setAttribute("crfs", crfs);
            FormProcessor fp = new FormProcessor(request);
            EntityBeanTable table = fp.getEntityBeanTable();
            ArrayList allRows = CRFRow.generateRowsFromBeans(crfs);
            String[] columns =
                { resword.getString("CRF_name"), resword.getString("date_created"), resword.getString("owner"), resword.getString("date_updated"),
                    resword.getString("last_updated_by"), resword.getString("selected") };
            table.setColumns(new ArrayList(Arrays.asList(columns)));
            table.hideColumnLink(5);
            table.setQuery("AddCRFToDefinition", new HashMap());
            table.setRows(allRows);
            table.computeDisplay();

            request.setAttribute("table", table);
            forwardPage(Page.UPDATE_EVENT_DEFINITION2);
        } else {
            addCRF(crfs);
        }
    }

    private void addCRF(ArrayList crfs) throws Exception {
        boolean hasCrf = false;
        ArrayList edcs = (ArrayList) session.getAttribute("eventDefinitionCRFs");
        StudyEventDefinitionBean sed = (StudyEventDefinitionBean) session.getAttribute("definition");
        Validator v = new Validator(request);
        FormProcessor fp = new FormProcessor(request);
        CRFVersionDAO vdao = new CRFVersionDAO(sm.getDataSource());
        String crfNames = "";
        boolean isCRFSelected = false;
        int ordinalForNewCRF = edcs.size();
        for (int i = 0; i < crfs.size(); i++) {

            int id = fp.getInt("id" + i);
            String name = fp.getString("name" + i);
            // String label = fp.getString("label" + i);
            String selected = fp.getString("selected" + i);
            logger.info("selected:" + selected);
            if (!StringUtil.isBlank(selected) && "yes".equalsIgnoreCase(selected.trim())) {
                logger.info("one crf selected");
                isCRFSelected = true;
                EventDefinitionCRFBean edcBean = new EventDefinitionCRFBean();

                edcBean.setCrfId(id);
                edcBean.setCrfName(name);

                edcBean.setStudyId(ub.getActiveStudyId());
                edcBean.setStatus(Status.AVAILABLE);
                edcBean.setStudyEventDefinitionId(sed.getId());
                edcBean.setStudyId(ub.getActiveStudyId());
                edcBean.setSourceDataVerification(SourceDataVerification.NOTREQUIRED);

                ArrayList versions = (ArrayList) vdao.findAllActiveByCRF(edcBean.getCrfId());
                edcBean.setVersions(versions);
                CRFVersionBean defaultVersion1 = (CRFVersionBean) vdao.findByPK(edcBean.getDefaultVersionId());
                edcBean.setDefaultVersionName(defaultVersion1.getName());
                for (int j = 0; j < edcs.size(); j++) {
                    EventDefinitionCRFBean edcBean1 = (EventDefinitionCRFBean) edcs.get(j);
                    // below added 092007, tbh
                    ArrayList versions1 = (ArrayList) vdao.findAllActiveByCRF(edcBean1.getCrfId());
                    edcBean1.setVersions(versions1);
                    // CRFBean crf = (CRFBean)
                    // cdao.findByPK(edcBean1.getCrfId());
                    // edcBean1.setCrfName(crf.getName());
                    // TO DO: use a better way on JSP page,eg.function tag
                    // edcBean1.setNullFlags(processNullValues(edcBean1));

                    CRFVersionBean defaultVersion = (CRFVersionBean) vdao.findByPK(edcBean1.getDefaultVersionId());
                    edcBean1.setDefaultVersionName(defaultVersion.getName());
                    // above added 092007, tbh
                    if (edcBean1.getCrfId() == edcBean.getCrfId()) {
                        hasCrf = true;
                        crfNames = crfNames + name + " ";
                        break;
                    }
                }
                if (hasCrf == false) {
                    ordinalForNewCRF = ordinalForNewCRF + 1;
                    edcBean.setOrdinal(ordinalForNewCRF);
                    ordinalForNewCRF++;
                    edcs.add(edcBean);
                    crfNames = crfNames + name + " ";
                }
            }
        }

        session.setAttribute("eventDefinitionCRFs", edcs);
        ArrayList<String> sdvOptions = new ArrayList<String>();
        sdvOptions.add(SourceDataVerification.AllREQUIRED.toString());
        sdvOptions.add(SourceDataVerification.PARTIALREQUIRED.toString());
        sdvOptions.add(SourceDataVerification.NOTREQUIRED.toString());
        sdvOptions.add(SourceDataVerification.NOTAPPLICABLE.toString());
        request.setAttribute("sdvOptions", sdvOptions);
        if (isCRFSelected) {
            if (hasCrf == false) {
                addPageMessage(respage.getString("has_have_been_added_need_confirmation"));
            } else {
                addPageMessage(crfNames + respage.getString("has_have_been_added_already"));
            }
        } else {
            addPageMessage(respage.getString("no_new_CRF_added"));
        }
        forwardPage(Page.UPDATE_EVENT_DEFINITION1);
    }

}
