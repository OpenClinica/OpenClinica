/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.ItemDataType;
import org.akaza.openclinica.bean.core.NullValue;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.DisplayItemBean;
import org.akaza.openclinica.bean.submit.DisplaySectionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.dao.submit.SectionDAO;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InconsistentStateException;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.text.MessageFormat;

/**
 * @author ssachs
 */
public class InitialDataEntryServletOld extends SecureController {
    // these inputs are used when other servlets redirect you here
    // this is most typically the case when the user enters data and clicks the
    // "Previous" or "Next" button
    public static final String INPUT_EVENT_CRF = "event";
    public static final String INPUT_SECTION = "section";

    // these inputs come from the form or from another jsp, such as the
    // tableOfContents.jsp
    // e.g. InitialDataEntry?eventCRFId=123&sectionId=234
    public static final String INPUT_EVENT_CRF_ID = "eventCRFId";
    public static final String INPUT_SECTION_ID = "sectionId";
    public static final String INPUT_IGNORE_PARAMETERS = "ignore";
    public static final String INPUT_CHECK_INPUTS = "checkInputs";

    // this comes from the form
    public static final String RESUME_LATER = "submittedResume";
    public static final String GO_PREVIOUS = "submittedPrev";
    public static final String GO_NEXT = "submittedNext";

    public static final String BEAN_DISPLAY = "section";

    private FormProcessor fp;
    private EventCRFDAO ecdao;
    private EventCRFBean ecb;

    private SectionDAO sdao;
    private SectionBean sb;

    private EventDefinitionCRFDAO edcdao;
    private EventDefinitionCRFBean edcb;

    private ItemDAO idao;
    private ItemFormMetadataDAO ifmdao;
    private ItemDataDAO iddao;

    private void getInputBeans() {
        // if ((fp != null) && (ecdao != null) && (ecb != null) && (sdao !=
        // null) && (sb != null)) {
        // return ;
        // }

        fp = new FormProcessor(request);
        ecdao = new EventCRFDAO(sm.getDataSource());
        sdao = new SectionDAO(sm.getDataSource());

        ecb = (EventCRFBean) request.getAttribute(INPUT_EVENT_CRF);
        if (ecb == null) {
            int eventCRFId = fp.getInt(INPUT_EVENT_CRF_ID, true);
            ecb = (EventCRFBean) ecdao.findByPK(eventCRFId);
        }

        sb = (SectionBean) request.getAttribute(INPUT_SECTION);
        if (sb == null) {
            int sectionId = fp.getInt(INPUT_SECTION_ID, true);
            sb = (SectionBean) sdao.findByPK(sectionId);
        }

        return;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.akaza.openclinica.control.core.SecureController#processRequest()
     */
    @Override
    protected void processRequest() throws Exception {
        getInputBeans();

        if (!ecb.isActive()) {
            throw new InconsistentStateException(Page.SUBMIT_DATA, resexception.getString("event_not_exists"));
        }

        DisplaySectionBean section = getDisplayBean();
        SectionBean previousSec = sdao.findPrevious(ecb, sb);
        SectionBean nextSec = sdao.findNext(ecb, sb);
        section.setFirstSection(!previousSec.isActive());
        section.setLastSection(!nextSec.isActive());

        Boolean b = (Boolean) request.getAttribute(INPUT_IGNORE_PARAMETERS);

        if (!fp.isSubmitted() || b != null) {
            // TODO: prevent data enterer from seeing results of first round of
            // data entry, if this is second round
            request.setAttribute(BEAN_DISPLAY, section);
            forwardPage(Page.INITIAL_DATA_ENTRY);
        } else {
            errors = new HashMap();
            ArrayList items = section.getItems();

            if (fp.getBoolean(INPUT_CHECK_INPUTS)) {
                Validator v = new Validator(request);

                // TODO: always validate null values
                for (int i = 0; i < items.size(); i++) {
                    DisplayItemBean dib = (DisplayItemBean) items.get(i);
                    dib = validateDisplayItemBean(v, dib);

                    ArrayList children = dib.getChildren();
                    for (int j = 0; j < children.size(); j++) {
                        DisplayItemBean child = (DisplayItemBean) children.get(j);
                        child = validateDisplayItemBean(v, child);
                        children.set(j, child);
                    }

                    dib.setChildren(children);
                    items.set(i, dib);
                }

                // we have to do this since we loaded all the form values into
                // the display item beans above
                section.setItems(items);

                errors = v.validate();
            } else {
                for (int i = 0; i < items.size(); i++) {
                    DisplayItemBean dib = (DisplayItemBean) items.get(i);
                    dib = loadFormValue(dib);

                    ArrayList children = dib.getChildren();
                    for (int j = 0; j < children.size(); j++) {
                        DisplayItemBean child = (DisplayItemBean) children.get(j);
                        child = loadFormValue(child);
                        children.set(j, child);
                    }

                    dib.setChildren(children);
                    items.set(i, dib);
                }
                // we have to do this since we loaded all the form values into
                // the display item beans above
                section.setItems(items);

                // section.setCheckInputs(false);
            }

            if (!errors.isEmpty()) {
                // force the servlet to accept whatever data is entered next
                // go-round
                // section.setCheckInputs(false);

                request.setAttribute(BEAN_DISPLAY, section);
                setInputMessages(errors);
                addPageMessage(respage.getString("errors_in_submission_see_below_details"));
                addPageMessage(respage.getString("to_override_these_errors"));
                forwardPage(Page.INITIAL_DATA_ENTRY);
            } else {
                ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource());
                boolean success = true;
                boolean temp = true;

                items = section.getItems();
                for (int i = 0; i < items.size(); i++) {
                    DisplayItemBean dib = (DisplayItemBean) items.get(i);
                    temp = writeToDB(dib, iddao);
                    success = success && temp;

                    ArrayList childItems = dib.getChildren();
                    for (int j = 0; j < childItems.size(); j++) {
                        DisplayItemBean child = (DisplayItemBean) childItems.get(j);
                        temp = writeToDB(child, iddao);
                        success = success && temp;
                    }
                }

                request.setAttribute(INPUT_IGNORE_PARAMETERS, Boolean.TRUE);
                if (!success) {
                    addPageMessage(resexception.getString("database_error"));
                    request.setAttribute(BEAN_DISPLAY, section);
                    forwardPage(Page.TABLE_OF_CONTENTS_SERVLET);
                } else {
                    boolean forwardingSucceeded = false;

                    if (!fp.getString(GO_PREVIOUS).equals("")) {
                        if (previousSec.isActive()) {
                            forwardingSucceeded = true;
                            request.setAttribute(INPUT_EVENT_CRF, ecb);
                            request.setAttribute(INPUT_SECTION, previousSec);
                            forwardPage(Page.INITIAL_DATA_ENTRY_SERVLET);
                        }
                    } else if (!fp.getString(GO_NEXT).equals("")) {
                        if (nextSec.isActive()) {
                            forwardingSucceeded = true;
                            request.setAttribute(INPUT_EVENT_CRF, ecb);
                            request.setAttribute(INPUT_SECTION, nextSec);
                            forwardPage(Page.INITIAL_DATA_ENTRY_SERVLET);
                        }
                    }

                    if (!forwardingSucceeded) {
                        request.setAttribute(TableOfContentsServlet.INPUT_EVENT_CRF_BEAN, ecb);
                        addPageMessage(respage.getString("data_saved_continue_later"));
                        forwardPage(Page.TABLE_OF_CONTENTS_SERVLET);
                    }
                }
            }
        }
    }

    private DisplayItemBean loadFormValue(DisplayItemBean dib) {
        String inputName = "input" + dib.getItem().getId();
        org.akaza.openclinica.bean.core.ResponseType rt = dib.getMetadata().getResponseSet().getResponseType();

        if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.CHECKBOX) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.SELECTMULTI)) {
            dib.loadFormValue(fp.getStringArray(inputName));
        } else {
            dib.loadFormValue(fp.getString(inputName));
        }

        return dib;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.akaza.openclinica.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        getInputBeans();

        DataEntryStage stage = ecb.getStage();
        Role r = currentRole.getRole();

        if (stage.equals(DataEntryStage.UNCOMPLETED)) {
            if (!SubmitDataServlet.maySubmitData(ub, currentRole)) {
                String exceptionName = resexception.getString("no_permission_to_perform_data_entry");
                String noAccessMessage =
                    respage.getString("you_may_not_perform_data_entry_on_a_CRF") + " " + respage.getString("change_study_contact_study_coordinator");

                addPageMessage(noAccessMessage);
                throw new InsufficientPermissionException(Page.MENU, exceptionName, "1");
            }
        } else if (stage.equals(DataEntryStage.INITIAL_DATA_ENTRY)) {
            if (ub.getId() != ecb.getOwnerId() && !r.equals(Role.STUDYDIRECTOR) && !r.equals(Role.COORDINATOR)) {
                UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
                String ownerName  = ((UserAccountBean)udao.findByPK(ecb.getOwnerId())).getName();
                MessageFormat mf = new MessageFormat("");
                mf.applyPattern(respage.getString("you_may_not_perform_data_entry_on_event_CRF_because_not_owner"));
                Object[] arguments = { ownerName };
                addPageMessage(mf.format(arguments));
                
                throw new InsufficientPermissionException(Page.SUBMIT_DATA, resexception.getString("non_owner_attempting_DE_on_event"), "1");
            }
        } else {
            addPageMessage(respage.getString("you_not_enter_data_initial_DE_completed"));
            throw new InsufficientPermissionException(Page.SUBMIT_DATA, resexception.getString("using_IDE_event_CRF_completed"), "1");
        }

        return;
    }

    private DisplaySectionBean getDisplayBean() throws Exception {
        DisplaySectionBean section = new DisplaySectionBean();

        section.setEventCRF(ecb);

        if (sb.getParentId() > 0) {
            SectionBean parent = (SectionBean) sdao.findByPK(sb.getParentId());
            sb.setParent(parent);
        }

        section.setSection(sb);

        CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());
        CRFVersionBean cvb = (CRFVersionBean) cvdao.findByPK(ecb.getCRFVersionId());
        section.setCrfVersion(cvb);

        CRFDAO cdao = new CRFDAO(sm.getDataSource());
        CRFBean cb = (CRFBean) cdao.findByPK(cvb.getCrfId());
        section.setCrf(cb);

        edcdao = new EventDefinitionCRFDAO(sm.getDataSource());
        StudyBean study = (StudyBean) session.getAttribute("study");
        edcb = edcdao.findByStudyEventIdAndCRFVersionId(study, ecb.getStudyEventId(), cvb.getId());
        section.setEventDefinitionCRF(edcb);

        // setup DAO's here to avoid creating too many objects
        idao = new ItemDAO(sm.getDataSource());
        ifmdao = new ItemFormMetadataDAO(sm.getDataSource());
        iddao = new ItemDataDAO(sm.getDataSource());

        // get all the display item beans
        ArrayList displayItems = getParentDisplayItems(sb, edcb, idao, ifmdao, iddao);

        // now sort them by ordinal
        Collections.sort(displayItems);

        // now get the child DisplayItemBeans
        for (int i = 0; i < displayItems.size(); i++) {
            DisplayItemBean dib = (DisplayItemBean) displayItems.get(i);
            dib.setChildren(getChildrenDisplayItems(dib, edcb));
            dib.loadDBValue();
            displayItems.set(i, dib);
        }

        section.setItems(displayItems);

        return section;
    }

    /**
     * For each item in this section which is a parent, get a DisplayItemBean
     * corresponding to that item. Note that an item is a parent iff its
     * parentId == 0.
     * 
     * @param sb
     *            The section whose items we are retrieving.
     * @return An array of DisplayItemBean objects, one per parent item in the
     *         section. Note that there is no guarantee on the ordering of the
     *         objects.
     * @throws Exception
     */
    private ArrayList getParentDisplayItems(SectionBean sb, EventDefinitionCRFBean edcb, ItemDAO idao, ItemFormMetadataDAO ifmdao, ItemDataDAO iddao)
            throws Exception {
        ArrayList answer = new ArrayList();

        // DisplayItemBean objects are composed of an ItemBean, ItemDataBean and
        // ItemFormDataBean.
        // However the DAOs only provide methods to retrieve one type of bean at
        // a time (per section)
        // the displayItems hashmap allows us to compose these beans into
        // DisplayItemBean objects,
        // while hitting the database only three times
        HashMap displayItems = new HashMap();

        ArrayList items = idao.findAllParentsBySectionId(sb.getId());
        for (int i = 0; i < items.size(); i++) {
            DisplayItemBean dib = new DisplayItemBean();
            dib.setEventDefinitionCRF(edcb);
            ItemBean ib = (ItemBean) items.get(i);
            dib.setItem(ib);
            displayItems.put(new Integer(dib.getItem().getId()), dib);
        }

        ArrayList metadata = ifmdao.findAllBySectionId(sb.getId());
        for (int i = 0; i < metadata.size(); i++) {
            ItemFormMetadataBean ifmb = (ItemFormMetadataBean) metadata.get(i);
            DisplayItemBean dib = (DisplayItemBean) displayItems.get(new Integer(ifmb.getItemId()));
            if (dib != null) {
                dib.setMetadata(ifmb);
                displayItems.put(new Integer(ifmb.getItemId()), dib);
            }
        }

        ArrayList data = iddao.findAllBySectionIdAndEventCRFId(sb.getId(), ecb.getId());
        for (int i = 0; i < data.size(); i++) {
            ItemDataBean idb = (ItemDataBean) data.get(i);
            DisplayItemBean dib = (DisplayItemBean) displayItems.get(new Integer(idb.getItemId()));
            if (dib != null) {
                dib.setData(idb);
                displayItems.put(new Integer(idb.getItemId()), dib);
            }
        }

        Iterator hmIt = displayItems.keySet().iterator();
        while (hmIt.hasNext()) {
            Integer key = (Integer) hmIt.next();
            DisplayItemBean dib = (DisplayItemBean) displayItems.get(key);
            answer.add(dib);
        }

        return answer;
    }

    /**
     * Get the DisplayItemBean objects corresponding to the items which are
     * children of the specified parent.
     * 
     * @param parent
     *            The item whose children are to be retrieved.
     * @return An array of DisplayItemBean objects corresponding to the items
     *         which are children of parent, and are sorted by column number
     *         (ascending), then ordinal (ascending).
     */
    private ArrayList getChildrenDisplayItems(DisplayItemBean parent, EventDefinitionCRFBean edcb) {
        ArrayList answer = new ArrayList();

        int parentId = parent.getItem().getId();
        ArrayList childItemBeans = idao.findAllByParentIdAndCRFVersionId(parentId, ecb.getCRFVersionId());

        for (int i = 0; i < childItemBeans.size(); i++) {
            ItemBean child = (ItemBean) childItemBeans.get(i);
            ItemDataBean data = iddao.findByItemIdAndEventCRFId(child.getId(), ecb.getId());
            ItemFormMetadataBean metadata = ifmdao.findByItemIdAndCRFVersionId(child.getId(), ecb.getCRFVersionId());

            // DisplayItemBean dib = new DisplayItemBean(edcb);
            DisplayItemBean dib = new DisplayItemBean();
            dib.setEventDefinitionCRF(edcb);
            dib.setItem(child);
            dib.setData(data);
            dib.setMetadata(metadata);
            dib.loadDBValue();

            answer.add(dib);
        }

        // this is a pretty slow and memory intensive way to sort... see if we
        // can have the db do this instead
        // ChildDisplayItemBeanComparator childSorter =
        // ChildDisplayItemBeanComparator.getInstance();
        // Collections.sort(answer, childSorter);
        Collections.sort(answer);

        return answer;
    }

    private DisplayItemBean validateDisplayItemBean(Validator v, DisplayItemBean dib) {
        ItemBean ib = dib.getItem();
        ItemDataType idt = ib.getDataType();
        org.akaza.openclinica.bean.core.ResponseType rt = dib.getMetadata().getResponseSet().getResponseType();

        String inputName = "input" + ib.getId();

        // note that this step sets us up both for
        // displaying the data on the form again, in the event of an error
        // and sending the data to the database, in the event of no error
        dib = loadFormValue(dib);

        // types TEL and ED are not supported yet
        if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.TEXT) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.TEXTAREA)
            || rt.equals(org.akaza.openclinica.bean.core.ResponseType.CALCULATION) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.GROUP_CALCULATION)) {
            boolean isNull = false;
            ArrayList nullValues = edcb.getNullValuesList();
            for (int i = 0; i < nullValues.size(); i++) {
                NullValue nv = (NullValue) nullValues.get(i);
                if (nv.getName().equals(fp.getString(inputName))) {
                    isNull = true;
                }
            }

            if (!isNull) {
                if (idt.equals(ItemDataType.ST)) {
                    v.addValidation(inputName, Validator.NO_BLANKS);
                } else if (idt.equals(ItemDataType.INTEGER)) {
                    v.addValidation(inputName, Validator.NO_BLANKS);
                    v.addValidation(inputName, Validator.IS_AN_INTEGER);
                } else if (idt.equals(ItemDataType.REAL)) {
                    v.addValidation(inputName, Validator.NO_BLANKS);
                    v.addValidation(inputName, Validator.IS_A_NUMBER);
                } else if (idt.equals(ItemDataType.BL)) {
                    // there is no validation here since this data type is
                    // explicitly allowed to be null
                    // if the string input for this field parses to a non-zero
                    // number, the value will be true; otherwise, 0
                } else if (idt.equals(ItemDataType.BN)) {
                    v.addValidation(inputName, Validator.NO_BLANKS);
                } else if (idt.equals(ItemDataType.SET)) {
                    v.addValidation(inputName, Validator.NO_BLANKS_SET);
                    v.addValidation(inputName, Validator.IN_RESPONSE_SET_SINGLE_VALUE, dib.getMetadata().getResponseSet());
                }
            }
        } else if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.RADIO) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.SELECT)) {
            v.addValidation(inputName, Validator.NO_BLANKS_SET);
            v.addValidation(inputName, Validator.IN_RESPONSE_SET_SINGLE_VALUE, dib.getMetadata().getResponseSet());
        } else if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.CHECKBOX) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.SELECTMULTI)) {
            v.addValidation(inputName, Validator.NO_BLANKS_SET);
            v.addValidation(inputName, Validator.IN_RESPONSE_SET, dib.getMetadata().getResponseSet());
        }

        return dib;
    }

    private boolean writeToDB(DisplayItemBean dib, ItemDataDAO iddao) {
        ItemDataBean idb = dib.getData();

        if (idb.getValue().equals("")) {
            idb.setStatus(Status.AVAILABLE);
        } else {
            Status newStatus;
            newStatus = edcb.isDoubleEntry() ? Status.PENDING : Status.UNAVAILABLE;
            idb.setStatus(newStatus);
        }

        if (!idb.isActive()) {
            idb.setCreatedDate(new Date());
            idb.setOwner(ub);
            idb.setItemId(dib.getItem().getId());
            idb.setEventCRFId(ecb.getId());

            idb = (ItemDataBean) iddao.create(idb);
        } else {
            idb = (ItemDataBean) iddao.update(idb);
        }

        return idb.isActive();
    }

}