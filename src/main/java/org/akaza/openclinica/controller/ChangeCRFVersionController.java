package org.akaza.openclinica.controller;

import core.org.akaza.openclinica.bean.admin.CRFBean;
import core.org.akaza.openclinica.bean.core.ResolutionStatus;
import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.login.StudyUserRoleBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.*;
import core.org.akaza.openclinica.bean.submit.*;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import core.org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.control.core.SecureController;
import core.org.akaza.openclinica.core.EventCRFLocker;
import core.org.akaza.openclinica.core.LockInfo;
import core.org.akaza.openclinica.dao.admin.AuditDAO;
import core.org.akaza.openclinica.dao.admin.CRFDAO;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.EventCrfDao;
import core.org.akaza.openclinica.dao.hibernate.UserAccountDao;
import core.org.akaza.openclinica.dao.hibernate.VersioningMapDao;
import core.org.akaza.openclinica.dao.submit.EventCRFDAO;
import core.org.akaza.openclinica.dao.submit.FormLayoutDAO;
import core.org.akaza.openclinica.dao.submit.ItemDAO;
import core.org.akaza.openclinica.dao.submit.ItemGroupMetadataDAO;
import core.org.akaza.openclinica.domain.datamap.EventCrf;
import core.org.akaza.openclinica.domain.datamap.VersioningMap;
import core.org.akaza.openclinica.domain.user.UserAccount;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import core.org.akaza.openclinica.service.PermissionService;
import org.akaza.openclinica.domain.enumsupport.StudyEventWorkflowStatusEnum;
import org.akaza.openclinica.view.StudyInfoPanel;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.HttpSessionRequiredException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Implement the functionality for displaying a table of Event CRFs for Source Data
 * Verification. This is an autowired, multiaction Controller.
 */
@Controller("changeCRFVersionController")
public class ChangeCRFVersionController {
    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @Autowired
    CoreResources coreResources;

    @Autowired
    VersioningMapDao versioningMapDao;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    // Autowire the class that handles the sidebar structure with a configured
    // bean named "sidebarInit"
    @Autowired
    @Qualifier("sidebarInit")
    private SidebarInit sidebarInit;
    @Autowired
    EventCRFLocker eventCRFLocker;
    @Autowired
    UserAccountDao userAccountDao;
    @Autowired
    private EventCrfDao eventCrfDao;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private StudyDao studyDao;

    private DiscrepancyNoteDAO dnDao;

    ResourceBundle resword, resformat, respage;

    public ChangeCRFVersionController() {
    }

    /*
     * Allows user to select new CRF version
     * 
     */
    // @RequestMapping(value="/managestudy/chooseCRFVersion", method = RequestMethod.GET)
    @RequestMapping(value = "/managestudy/chooseCRFVersion", method = RequestMethod.GET)
    public ModelMap chooseCRFVersion(HttpServletRequest request, HttpServletResponse response, @RequestParam("crfId") int crfId,
            @RequestParam("crfName") String crfName, @RequestParam("formLayoutId") int formLayoutId, @RequestParam("formLayoutName") String formLayoutName,
            @RequestParam("studySubjectLabel") String studySubjectLabel, @RequestParam("studySubjectId") int studySubjectId,
            @RequestParam("eventCrfId") int eventCRFId, @RequestParam("eventDefinitionCRFId") int eventDefinitionCRFId,
            @RequestParam("originatingPage") String originatingPage)

    {

        // to be removed for aquamarine
        if (!mayProceed(request)) {
            try {
                response.sendRedirect(request.getContextPath() + "/MainMenu?message=authentication_failed");
            } catch (Exception e) {
                logger.error("Redirecting response failed: ",e);
            }
            return null;
        }
        resetPanel(request);
        ModelMap gridMap = new ModelMap();

        request.setAttribute("eventCrfId", eventCRFId);
        request.setAttribute("studySubjectLabel", studySubjectLabel);
        request.setAttribute("eventDefinitionCRFId", eventDefinitionCRFId);
        request.setAttribute("studySubjectId", studySubjectId);
        request.setAttribute("crfId", crfId);
        request.setAttribute("crfName", crfName);
        request.setAttribute("formLayoutId", formLayoutId);
        request.setAttribute("formLayoutName", formLayoutName.trim());
        request.setAttribute(SecureController.ORIGINATING_PAGE, originatingPage);

        ArrayList<String> pageMessages = initPageMessages(request);
        String errorMessage = request.getParameter("errorMessage");
        errorMessage = StringEscapeUtils.escapeHtml(errorMessage);
        errorMessage = StringEscapeUtils.escapeJavaScript(errorMessage);
        if (errorMessage != null) {
            pageMessages.add(errorMessage);
        }
        // get CRF by ID with all versions
        // create List of all versions (label + value)
        // set default CRF version label
        setupResource(request);

        Study study = (Study) request.getSession().getAttribute("study");

        CRFDAO cdao = new CRFDAO(dataSource);
        CRFBean crfBean = (CRFBean) cdao.findByPK(crfId);
        FormLayoutDAO formLayoutDao = new FormLayoutDAO(dataSource);
        ArrayList<FormLayoutBean> formLayouts = (ArrayList<FormLayoutBean>) formLayoutDao.findAllActiveByCRF(crfId);

        StudyEventDefinitionDAO sfed = new StudyEventDefinitionDAO(dataSource);
        StudyEventDefinitionBean sedb = sfed.findByEventDefinitionCRFId(eventDefinitionCRFId);
        request.setAttribute("eventName", sedb.getName());

        EventCRFDAO ecdao = new EventCRFDAO(dataSource);
        EventCRFBean ecb = (EventCRFBean) ecdao.findByPK(eventCRFId);
        final EventCrf ec = eventCrfDao.findById(eventCRFId);
        if (permissionService.hasFormAccess(ec, formLayoutId, 0, request) != true) {
            redirect(request, response, "/NoAccess?originatingPage="+ originatingPage);
        }

        StudyEventDAO sedao = new StudyEventDAO(dataSource);
        StudyEventBean seb = (StudyEventBean) sedao.findByPK(ecb.getStudyEventId());
        request.setAttribute("eventCreateDate", formatDate(seb.getCreatedDate()));
        if (sedb.isRepeating()) {
            request.setAttribute("eventOrdinal", seb.getSampleOrdinal());
        }
        HttpSession session = request.getSession();
        UserAccountBean ub = (UserAccountBean) session.getAttribute("userBean");
        Study currentPublicStudy = (Study) session.getAttribute("publicStudy");
        if (eventCRFLocker.isLocked(currentPublicStudy.getSchemaName()
                + ecb.getStudyEventId() + ecb.getFormLayoutId(), ub.getId(), request.getSession().getId())) {
            String errorData = getErrorData(request, ecb, currentPublicStudy);
            if (redirect(request, response, "/ViewStudySubject?id=" + seb.getStudySubjectId() + "&errorData=" + errorData) == null)
                return null;
        }
        if (study.isSite()) {
            EventDefinitionCRFDAO edfdao = new EventDefinitionCRFDAO(dataSource);
            EventDefinitionCRFBean edf = (EventDefinitionCRFBean) edfdao.findByPK(eventDefinitionCRFId);

            if (!edf.getSelectedVersionIds().equals("")) {
                String[] version_ids = edf.getSelectedVersionIds().split(",");
                HashMap<String, String> tmp = new HashMap<String, String>(version_ids.length);
                for (String vs : version_ids) {
                    tmp.put(vs, vs);
                }
                ArrayList<FormLayoutBean> site_versions = new ArrayList<FormLayoutBean>(formLayouts.size());

                for (FormLayoutBean vs : formLayouts) {
                    if (tmp.get(String.valueOf(vs.getId())) != null) {
                        site_versions.add(vs);
                    }
                }
                formLayouts = site_versions;
            }

        }

        crfBean.setVersions(formLayouts);
        gridMap.addAttribute("numberOfVersions", crfBean.getVersions().size() + 1);
        gridMap.addAttribute("crfBean", crfBean);

        return gridMap;
    }

    private String getErrorData(HttpServletRequest request, EventCRFBean ecb, Study currentPublicStudy) {
        LockInfo lockInfo = eventCRFLocker.getLockOwner(currentPublicStudy.getSchemaName()
                + ecb.getStudyEventId() + ecb.getFormLayoutId());
        UserAccount userAccount = userAccountDao.findByUserId(lockInfo.getUserId());
        request.setAttribute("errorData", "This form is currently unavailable for this action.\\n " +
                "User " + userAccount.getUserName() + " is currently entering data.\\n " +
                resword.getString("CRF_perform_action") +"\\n");
        String errorData = "";
        try {
            errorData = URLEncoder.encode("This form is currently unavailable for this action.\\n " +
                    "User " + userAccount.getUserName() + " is currently entering data.\\n " +
                    resword.getString("CRF_perform_action") + "\\n", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("Getting error data failed due to unsupported encoding: ",e);
        }
        return errorData;
    }

    /*
     * Displays two set of columns for user to confirm his decision to switch to a new version of CRF
     * field name | OID | field value
     */

    @RequestMapping(value = "/managestudy/confirmCRFVersionChange", method = RequestMethod.POST)
    // @RequestMapping("/managestudy/confirmCRFVersionChange")
    public ModelMap confirmCRFVersionChange(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value = "crfId", required = false) int crfId, @RequestParam(value = "crfName", required = false) String crfName,
            @RequestParam(value = "formLayoutId", required = false) int formLayoutId,
            @RequestParam(value = "formLayoutName", required = false) String formLayoutName,
            @RequestParam(value = "studySubjectLabel", required = false) String studySubjectLabel,
            @RequestParam(value = "studySubjectId", required = false) int studySubjectId,
            @RequestParam(value = "eventCrfId", required = false) int eventCRFId,
            @RequestParam(value = "eventDefinitionCRFId", required = false) int eventDefinitionCRFId,
            @RequestParam(value = "selectedVersionId", required = false) int selectedVersionId,
            @RequestParam(value = "selectedVersionName", required = false) String selectedVersionName,
            @RequestParam(value = "eventName", required = false) String eventName,
            @RequestParam(value = "eventCreateDate", required = false) String eventCreateDate,
            @RequestParam(value = "eventOrdinal", required = false) String eventOrdinal,
            @RequestParam(value = "originatingPage", required = false) String originatingPage,

            @RequestParam("confirmCRFVersionSubmit") String as)

    {

        // add here error handling for post with no data and redirect from OC error page
        // to be removed for aquamarine
        if (!mayProceed(request)) {
            if (redirect(request, response, "/MainMenu?message=authentication_failed") == null)
                return null;
        }
        FormLayoutDAO formLayoutDao = new FormLayoutDAO(dataSource);
        selectedVersionName = (formLayoutDao.findByPK(selectedVersionId)).getName().trim();

        resetPanel(request);
        request.setAttribute("eventCrfId", eventCRFId);
        request.setAttribute("studySubjectLabel", studySubjectLabel);
        request.setAttribute("eventDefinitionCRFId", eventDefinitionCRFId);
        request.setAttribute("studySubjectId", studySubjectId);
        request.setAttribute("crfId", crfId);
        request.setAttribute("crfName", crfName);
        request.setAttribute("formLayoutId", formLayoutId);
        request.setAttribute("formLayoutName", formLayoutName.trim());
        request.setAttribute("selectedVersionId", selectedVersionId);
        if (selectedVersionName != null) {
            selectedVersionName = selectedVersionName.trim();
        }
        request.setAttribute("selectedVersionName", selectedVersionName);
        request.setAttribute("eventName", eventName);
        request.setAttribute("eventCreateDate", eventCreateDate);
        request.setAttribute("eventOrdinal", eventOrdinal);
        request.setAttribute("originatingPage", originatingPage);


        ModelMap gridMap = new ModelMap();
        ArrayList<String> pageMessages = initPageMessages(request);

        setupResource(request);
        if (selectedVersionId == -1) {

            String errorMessage = resword.getString("confirm_crf_version_em_select_version");// "Please select CRF
                                                                                             // version";
            StringBuffer params = new StringBuffer();
            params.append("/pages/managestudy/chooseCRFVersion?crfId=" + crfId);
            params.append("&crfName=" + crfName);
            params.append("&formLayoutId=" + formLayoutId);
            params.append("&formLayoutName=" + formLayoutName);
            params.append("&selectedVersionName=" + selectedVersionName);
            params.append("&studySubjectLabel=" + studySubjectLabel);
            params.append("&studySubjectId=" + studySubjectId);
            params.append("&eventCrfId=" + eventCRFId);
            params.append("&eventDefinitionCRFId=" + eventDefinitionCRFId);
            params.append("&errorMessage=" + errorMessage);
            params.append("&originatingPage=" + originatingPage);


            if (redirect(request, response, params.toString()) == null) {
                return null;
            }
        }

        request.getSession().removeAttribute("pageMessages");
        // get dATa for current crf version display
        // select name, ordinal, oc_oid, item_data_id, i.item_id, value from item_data id, item i
        // where id.item_id=i.item_id and event_crf_id = 171 order by i.item_id,ordinal;
        ArrayList<String[]> rows = new ArrayList<String[]>();
        int cur_counter = 0;
        int new_counter = 0;

        try {
            ItemDAO item_dao = new ItemDAO(dataSource);
            ItemDataBean d_bean = null;
            // get metadata to find repeat group or not
            ItemGroupMetadataDAO dao_item_form_mdata = new ItemGroupMetadataDAO(dataSource);

            HashMap<Integer, ItemGroupMetadataBean> hash_item_form_mdata = new HashMap<Integer, ItemGroupMetadataBean>();
            HashMap<Integer, ItemGroupMetadataBean> hash_new_item_form_mdata = new HashMap<Integer, ItemGroupMetadataBean>();

            List<ItemBean> cur_items = new ArrayList<>();
            List<ItemBean> new_items = new ArrayList<>();

            List<VersioningMap> origFormLayoutItems = versioningMapDao.findByFormLayoutId(formLayoutId);
            for (VersioningMap vm : origFormLayoutItems) {
                ItemGroupMetadataBean beans_item_form_mdata = (ItemGroupMetadataBean) dao_item_form_mdata
                        .findByItemAndCrfVersion(vm.getVersionMapId().getItemId(), vm.getVersionMapId().getCrfVersionId());
                hash_item_form_mdata.put(new Integer(vm.getVersionMapId().getItemId()), beans_item_form_mdata);
                ItemBean itemBean = (ItemBean) item_dao.findByPK(vm.getVersionMapId().getItemId());
                cur_items.add(itemBean);
            }

            List<VersioningMap> newFormLayoutItems = versioningMapDao.findByFormLayoutId(selectedVersionId);
            for (VersioningMap vm : newFormLayoutItems) {
                ItemGroupMetadataBean bn_new_item_form_mdata = (ItemGroupMetadataBean) dao_item_form_mdata
                        .findByItemAndCrfVersion(vm.getVersionMapId().getItemId(), vm.getVersionMapId().getCrfVersionId());
                hash_new_item_form_mdata.put(new Integer(vm.getVersionMapId().getItemId()), bn_new_item_form_mdata);
                ItemBean itemBean = (ItemBean) item_dao.findByPK(vm.getVersionMapId().getItemId());
                new_items.add(itemBean);
            }

            // get items description
            List<ItemBean> cur_items_with_data = item_dao.findAllWithItemDataByFormLayoutId(formLayoutId, eventCRFId);
            HashMap<String, ItemBean> hash_items_with_data = new HashMap<String, ItemBean>(cur_items_with_data.size());
            for (ItemBean item : cur_items_with_data) {
                hash_items_with_data.put(item.getOid(), item);
            }
            ItemBean temp = null;
            for (ItemBean item : cur_items) {
                temp = hash_items_with_data.get(item.getOid());
                if (temp != null) {
                    item.setItemDataElements(temp.getItemDataElements());
                }
            }

            List<ItemBean> new_items_with_data = item_dao.findAllWithItemDataByFormLayoutId(selectedVersionId, eventCRFId);
            hash_items_with_data = new HashMap<String, ItemBean>(new_items_with_data.size());
            for (ItemBean item : new_items_with_data) {
                hash_items_with_data.put(item.getOid(), item);
            }
            for (ItemBean item : new_items) {
                temp = hash_items_with_data.get(item.getOid());
                if (temp != null) {
                    item.setItemDataElements(temp.getItemDataElements());
                }
            }

            ItemBean cur_element = null;
            ItemBean new_element = null;
            ItemGroupMetadataBean bn_mdata = null;
            ItemGroupMetadataBean bn_new_mdata = null;
            while (cur_counter < cur_items.size() || new_counter < new_items.size()) {

                if (cur_counter < cur_items.size()) {
                    cur_element = cur_items.get(cur_counter);
                    bn_mdata = hash_item_form_mdata.get(new Integer(cur_element.getId()));
                }
                if (new_counter < new_items.size()) {
                    new_element = new_items.get(new_counter);
                    bn_new_mdata = hash_new_item_form_mdata.get(new Integer(new_element.getId()));
                }

                if (cur_counter < cur_items.size() && new_counter < new_items.size() && new_element.getId() == cur_element.getId()) {
                    buildRecord(cur_element, new_element, bn_mdata, bn_new_mdata, rows);
                    cur_counter++;
                    new_counter++;
                } else if (cur_counter < cur_items.size() && (new_counter >= new_items.size() || new_element.getId() > cur_element.getId())) {
                    buildRecord(cur_element, null, bn_mdata, null, rows);
                    cur_counter++;
                } else if (new_counter < new_items.size() && (cur_counter >= cur_items.size() || new_element.getId() < cur_element.getId())) {

                    buildRecord(null, new_element, null, bn_new_mdata, rows);
                    new_counter++;
                }

            }
        } catch (Exception e) {

            logger.error(cur_counter + " " + new_counter);
            pageMessages.add(resword.getString("confirm_crf_version_em_dataextraction"));

        }
        request.setAttribute("pageMessages", pageMessages);
        gridMap.addAttribute("rows", rows);
        return gridMap;
    }

    private void buildRecord(ItemBean cur_element, ItemBean new_element, ItemGroupMetadataBean cur_bean_mdata, ItemGroupMetadataBean new_bean_mdata,
            ArrayList<String[]> rows) {

        String[] row = new String[8];
        int cycle_count = 0;
        if (cur_element == null && new_element != null) {
            if (new_element.getItemDataElements() == null || new_element.getItemDataElements().size() < 1) {
                row[0] = row[1] = row[2] = row[3] = row[7] = "";
                row[4] = (new_bean_mdata.isRepeatingGroup()) ? new_element.getName() + "(1)" : new_element.getName();
                row[5] = new_element.getOid();
                row[6] = String.valueOf(new_element.getId());
                rows.add(row);
                return;
            } else {
                for (ItemDataBean data_item : new_element.getItemDataElements()) {
                    row = new String[8];
                    row[0] = row[1] = row[2] = row[3] = row[7] = "";
                    row[4] = (new_bean_mdata.isRepeatingGroup()) ? new_element.getName() + "(" + data_item.getOrdinal() + ")" : new_element.getName();
                    row[5] = new_element.getOid();
                    row[6] = String.valueOf(new_element.getId());
                    row[7] = data_item.getValue();
                    rows.add(row);
                }
                return;
            }
        } else if (cur_element != null && new_element == null) {
            if (cur_element.getItemDataElements() == null || cur_element.getItemDataElements().size() < 1) {
                row[0] = (cur_bean_mdata.isRepeatingGroup()) ? cur_element.getName() + "(1)" : cur_element.getName();
                row[1] = cur_element.getOid();
                row[2] = String.valueOf(cur_element.getId());
                row[4] = row[6] = row[7] = row[5] = row[3] = "";
                rows.add(row);
            } else {
                for (ItemDataBean data_item : cur_element.getItemDataElements()) {
                    row = new String[8];
                    row[0] = (cur_bean_mdata.isRepeatingGroup()) ? cur_element.getName() + " (" + data_item.getOrdinal() + ")" : cur_element.getName();
                    row[1] = cur_element.getOid();
                    row[2] = String.valueOf(cur_element.getId());
                    row[3] = data_item.getValue();
                    row[4] = row[6] = row[7] = row[5] = "";
                    rows.add(row);
                    cycle_count++;
                    if (cycle_count > 0 && !cur_bean_mdata.isRepeatingGroup()) {
                        break;
                    }
                }
            }
            return;
        } else if (cur_element != null && new_element != null) {
            // for repeating groups: 3 cases
            // one cycle: repeating group item -> none-repeating group item
            // second cycle -> back none-repeating to prev repeating
            if (cur_element.getItemDataElements() == null) {
                row[4] = (new_bean_mdata.isRepeatingGroup()) ? new_element.getName() + "(1)" : new_element.getName();
                row[5] = new_element.getOid();
                row[6] = String.valueOf(new_element.getId());
                row[0] = (cur_bean_mdata.isRepeatingGroup()) ? cur_element.getName() + "(1)" : cur_element.getName();
                row[1] = cur_element.getOid();
                row[2] = String.valueOf(cur_element.getId());
                row[3] = row[7] = "";
                rows.add(row);
                return;
            }

            for (ItemDataBean data_item : cur_element.getItemDataElements()) {
                row = new String[8];
                if (!cur_bean_mdata.isRepeatingGroup() && cycle_count > 0) {
                    row[0] = row[1] = row[2] = row[3] = "";
                } else {
                    row[0] = (cur_bean_mdata.isRepeatingGroup()) ? cur_element.getName() + " (" + data_item.getOrdinal() + ")" : cur_element.getName();
                    row[1] = cur_element.getOid();
                    row[2] = String.valueOf(cur_element.getId());
                    row[3] = data_item.getValue();
                }
                if (new_bean_mdata.isRepeatingGroup()) {
                    // case when new one is a repeating group and has data from some previous entry while current does
                    // not have a repeating group
                    if (!cur_bean_mdata.isRepeatingGroup()) {
                        row[4] = cur_element.getName() + " (" + data_item.getOrdinal() + ")";
                    }

                    // new one is repeating & cur is repeating
                    if (cur_bean_mdata.isRepeatingGroup()) {
                        row[4] = row[0];
                    }
                    row[5] = new_element.getOid();
                    row[6] = String.valueOf(new_element.getId());
                    row[7] = data_item.getValue();
                } else {
                    if (cycle_count == 0) {

                        row[4] = row[0];
                        row[5] = new_element.getOid();
                        row[6] = String.valueOf(new_element.getId());
                        row[7] = data_item.getValue();
                    } else {
                        row[4] = row[5] = row[6] = row[7] = "";
                    }
                }
                cycle_count++;
                // do not add row if all items empty -> from data of repeat group to none-rep
                if (!(row[0].equals("") && row[4].equals(""))) {
                    rows.add(row);
                }
            }
            return;
        }

    }

    @RequestMapping("/managestudy/changeCRFVersion")
    // @RequestMapping("/managestudy/changeCRFVersionAction")
    public ModelMap changeCRFVersionAction(HttpServletRequest request, HttpServletResponse response, @RequestParam("crfId") int crfId,
            @RequestParam("crfName") String crfName, @RequestParam("formLayoutId") int formLayoutId, @RequestParam("formLayoutName") String formLayoutName,
            @RequestParam("studySubjectLabel") String studySubjectLabel, @RequestParam("studySubjectId") int studySubjectId,
            @RequestParam("eventCrfId") int eventCRFId, @RequestParam("eventDefinitionCRFId") int eventDefinitionCRFId,
            @RequestParam(value = "newFormLayoutId", required = true) int newFormLayoutId)

    {

        // to be removed for aquamarine
        if (!mayProceed(request)) {
            if (redirect(request, response, "/MainMenu?message=authentication_failed") == null)
                return null;
        }

        ArrayList<String> pageMessages = initPageMessages(request);

        setupResource(request);
        HttpSession session = request.getSession();
        // update event_crf_id table
        try {
            EventCRFDAO eventCRFDAO = new EventCRFDAO(dataSource);
            StudyEventDAO sed = new StudyEventDAO(dataSource);
            UserAccountBean ub = (UserAccountBean) session.getAttribute("userBean");
            Study currentPublicStudy = (Study) session.getAttribute("publicStudy");
            EventCRFBean ecb = (EventCRFBean) eventCRFDAO.findByPK(eventCRFId);
            StudyEventBean seb = (StudyEventBean) sed.findByPK(ecb.getStudyEventId());
            if (eventCRFLocker.isLocked(currentPublicStudy.getSchemaName()
                    + ecb.getStudyEventId() + ecb.getFormLayoutId(), ub.getId(), request.getSession().getId())) {
                String errorData = getErrorData(request, ecb, currentPublicStudy);
                if (redirect(request, response, "/ViewStudySubject?id=" + seb.getStudySubjectId() + "&errorData=" + errorData) == null)
                    return null;
            }
            Connection con = dataSource.getConnection();
            con.setAutoCommit(false);
            eventCRFDAO.updateFormLayoutID(eventCRFId, newFormLayoutId, getCurrentUser(request).getId(), con);

            String status_before_update = null;
            String status_before_update_new = null;

            Status subjectStatus = null;
            AuditDAO auditDao = new AuditDAO(dataSource);

            // event signed, check if subject is signed as well
            StudySubjectDAO studySubDao = new StudySubjectDAO(dataSource);
            StudySubjectBean studySubBean = (StudySubjectBean) studySubDao.findByPK(seb.getStudySubjectId());
            if (studySubBean.getStatus().isSigned()) {
                status_before_update = auditDao.findLastStatus("study_subject", studySubBean.getId(), "8");
                if (status_before_update != null && status_before_update.length() == 1) {
                    int subject_status = Integer.parseInt(status_before_update);
                    subjectStatus = Status.get(subject_status);
                    studySubBean.setStatus(subjectStatus);
                }
                studySubBean.setUpdater(getCurrentUser(request));
                studySubDao.update(studySubBean, con);
            }
            seb.setUpdater(getCurrentUser(request));
            seb.setUpdatedDate(new Date());

            status_before_update = auditDao.findLastStatus("study_event", seb.getId(), "8");
            status_before_update_new = auditDao.findLastStatus("study_event", seb.getId(),StudyEventWorkflowStatusEnum.SIGNED.toString());

            if (status_before_update != null && (status_before_update.length() == 1 || status_before_update_new.length() == 1)) {
                seb.setWorkflowStatus(StudyEventWorkflowStatusEnum.SIGNED);
            }
            sed.update(seb, con);

            con.commit();
            con.setAutoCommit(true);
            con.close();
            pageMessages.add(resword.getString("confirm_crf_version_ms"));
            String msg = resword.getString("confirm_crf_version_ms");

            // OC-11580 Auto-close queries when a form is migrated to a version that does not contain the item that is queried
            ItemDAO item_dao = new ItemDAO(dataSource);
            List<ItemBean> cur_items_with_data = item_dao.findAllWithItemDataByFormLayoutId(formLayoutId, eventCRFId);
            List<ItemBean> new_items_with_data = item_dao.findAllWithItemDataByFormLayoutId(newFormLayoutId, eventCRFId);
            cur_items_with_data.removeAll(new_items_with_data);
            dnDao = new DiscrepancyNoteDAO(dataSource);
            Study study = studyDao.findByPK(studySubBean.getStudyId());
            for (ItemBean item : cur_items_with_data) {
                for (ItemDataBean itemData : item.getItemDataElements()) {
                    ArrayList<DiscrepancyNoteBean> parentDiscrepancyNoteList = dnDao.findParentNotesOnlyByItemData(itemData.getId());
                    for (DiscrepancyNoteBean parentDiscrepancyNote : parentDiscrepancyNoteList) {
                        String description = resword.getString("dn_auto-closed_description");
                        String detailedNotes = resword.getString("dn_auto_closed_detailed_notes_due_to_migration");
                        // create new DN record , new DN Map record , also update the parent record
                        createDiscrepancyNoteBean(description, detailedNotes, itemData.getId(), study, ub, parentDiscrepancyNote);
                    }
                }
            }

            redirect(request, response, "/ViewStudySubject?isFromCRFVersionChange=" + msg + "&id=" + studySubjectId);
        } catch (Exception e) {

            pageMessages.add(resword.getString("error_message_cannot_update_crf_version"));

        }

        return null;
    }

    @ExceptionHandler(HttpSessionRequiredException.class)
    public String handleSessionRequiredException(HttpSessionRequiredException ex, HttpServletRequest request) {
        return "redirect:/MainMenu";
    }

    @ExceptionHandler(NullPointerException.class)
    public String handleNullPointerException(NullPointerException ex, HttpServletRequest request, HttpServletResponse response) {
        Study currentStudy = (Study) request.getSession().getAttribute("study");
        if (currentStudy == null) {
            return "redirect:/MainMenu";
        }
        throw ex;
    }

    public DiscrepancyNoteDAO getDnDao() {
        return dnDao;
    }

    private void setUpSidebar(HttpServletRequest request) {
        if (sidebarInit.getAlertsBoxSetup() == SidebarEnumConstants.OPENALERTS) {
            request.setAttribute("alertsBoxSetup", true);
        }

        if (sidebarInit.getInfoBoxSetup() == SidebarEnumConstants.OPENINFO) {
            request.setAttribute("infoBoxSetup", true);
        }
        if (sidebarInit.getInstructionsBoxSetup() == SidebarEnumConstants.OPENINSTRUCTIONS) {
            request.setAttribute("instructionsBoxSetup", true);
        }

        if (sidebarInit.getEnableIconsBoxSetup() == SidebarEnumConstants.DISABLEICONS) {
            request.setAttribute("enableIconsBoxSetup", false);
        }

    }

    // to be depricated in aquamarine
    private boolean mayProceed(HttpServletRequest request) {

        HttpSession session = request.getSession();
        StudyUserRoleBean currentRole = (StudyUserRoleBean) session.getAttribute("userRole");

        Role r = currentRole.getRole();

        if (r.equals(Role.STUDYDIRECTOR) || r.equals(Role.COORDINATOR)) {
            return true;
        }
        ArrayList<String> pageMessages = initPageMessages(request);

        pageMessages.add((respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin")));

        return false;
    }

    private UserAccountBean getCurrentUser(HttpServletRequest request) {
        UserAccountBean ub = (UserAccountBean) request.getSession().getAttribute("userBean");
        return ub;
    }

    private Object redirect(HttpServletRequest request, HttpServletResponse response, String location) {
        try {
            response.sendRedirect(request.getContextPath() + location);
        } catch (Exception e) {
            logger.error("Error while redirecting the request: ",e);
        }
        return null;

    }

    private void resetPanel(HttpServletRequest request) {
        StudyInfoPanel panel = new StudyInfoPanel();
        panel.reset();
        panel.setIconInfoShown(false);
        request.getSession().setAttribute("panel", panel);

    }

    private void setupResource(HttpServletRequest request) {
        Locale locale = request.getLocale();
        ResourceBundleProvider.updateLocale(locale);
        resword = ResourceBundleProvider.getWordsBundle(locale);
        resformat = ResourceBundleProvider.getFormatBundle(locale);
    }

    private String formatDate(Date date) {
        String dateFormat = resformat.getString("date_format_string");
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        String s = formatter.format(date);
        return s;
    }

    private ArrayList<String> initPageMessages(HttpServletRequest request) {
        ArrayList<String> pageMessages = (ArrayList<String>) request.getAttribute("pageMessages");

        if (pageMessages == null) {
            pageMessages = new ArrayList<String>();
        }

        request.setAttribute("pageMessages", pageMessages);
        return pageMessages;
    }

    private void createDiscrepancyNoteBean(String description, String detailedNotes, int itemDataId, Study studyBean, UserAccountBean ub,
                                           DiscrepancyNoteBean parentDiscrepancyNote) {
        DiscrepancyNoteBean dnb = new DiscrepancyNoteBean();
        dnb.setEntityId(itemDataId); // this is needed for DN Map object
        dnb.setStudyId(studyBean.getStudyId());
        dnb.setEntityType(DiscrepancyNoteBean.ITEM_DATA);
        dnb.setDescription(description);
        dnb.setDetailedNotes(detailedNotes);
        dnb.setDiscrepancyNoteTypeId(parentDiscrepancyNote.getDiscrepancyNoteTypeId()); // set to parent DN Type Id
        dnb.setResolutionStatusId(ResolutionStatus.CLOSED_MODIFIED.getId()); // set to closed Modified
        dnb.setColumn("value"); // this is needed for DN Map object
        dnb.setAssignedUser(null);
        dnb.setOwner(ub);
        dnb.setParentDnId(parentDiscrepancyNote.getId());
        dnb.setActivated(false);
        dnb.setThreadUuid(parentDiscrepancyNote.getThreadUuid());
        dnb = (DiscrepancyNoteBean) getDnDao().create(dnb); // create child DN
        getDnDao().createMapping(dnb); // create DN mapping

        DiscrepancyNoteBean itemParentNote = (DiscrepancyNoteBean) getDnDao().findByPK(dnb.getParentDnId());
        itemParentNote.setResolutionStatusId(ResolutionStatus.CLOSED_MODIFIED.getId()); // set to closed Modified
        itemParentNote.setAssignedUser(null);
        itemParentNote.setOwner(ub);
        itemParentNote.setDetailedNotes(detailedNotes);
        getDnDao().update(itemParentNote); // update parent DN
        getDnDao().updateAssignedUserToNull(itemParentNote); // update parent DN assigned user

    }

}
