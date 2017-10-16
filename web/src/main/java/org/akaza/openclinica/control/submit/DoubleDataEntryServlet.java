/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.submit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FilenameUtils;
import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.submit.DisplayEventCRFBean;
import org.akaza.openclinica.bean.submit.DisplayItemBean;
import org.akaza.openclinica.bean.submit.DisplayItemGroupBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.ResponseOptionBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.control.form.DiscrepancyValidator;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.RuleValidator;
import org.akaza.openclinica.control.form.ScoreItemValidator;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.SectionDAO;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ssachs
 */
public class DoubleDataEntryServlet extends DataEntryServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(DoubleDataEntryServlet.class);

    Locale locale;
    // < ResourceBundlerespage,restext,resexception,resword;

    public static final String COUNT_VALIDATE = "countValidate";
    public static final String DDE_ENTERED = "ddeEntered";
    public static final String DDE_PROGESS = "doubleDataProgress";

    private boolean userIsOwnerAndLessThanTwelveHoursHavePassed(HttpServletRequest request) {
        UserAccountBean ub =(UserAccountBean) request.getSession().getAttribute(USER_BEAN_NAME);
        StudyUserRoleBean  currentRole = (StudyUserRoleBean) request.getSession().getAttribute("userRole");
        EventCRFBean ecb = (EventCRFBean)request.getAttribute(INPUT_EVENT_CRF);

        boolean userIsOwner = ub.getId() == ecb.getOwnerId();
        boolean lessThanTwelveHoursHavePassed = !DisplayEventCRFBean.initialDataEntryCompletedMoreThanTwelveHoursAgo(ecb);

        return userIsOwner && lessThanTwelveHoursHavePassed;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed(HttpServletRequest request, HttpServletResponse response) throws InsufficientPermissionException {
        checkStudyLocked(Page.LIST_STUDY_SUBJECTS, respage.getString("current_study_locked"), request, response);
        checkStudyFrozen(Page.LIST_STUDY_SUBJECTS, respage.getString("current_study_frozen"), request, response);
        UserAccountBean ub =(UserAccountBean) request.getSession().getAttribute(USER_BEAN_NAME);
        StudyUserRoleBean  currentRole = (StudyUserRoleBean) request.getSession().getAttribute("userRole");
        HttpSession session = request.getSession();
        locale = LocaleResolver.getLocale(request);


        // < respage =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.page_messages",
        // locale);
        // < restext =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.notes",locale);
        // <
        // resexception=ResourceBundle.getBundle(
        // "org.akaza.openclinica.i18n.exceptions",locale);
        // < resword =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.words",locale);

        getInputBeans(request);
        EventCRFBean ecb = (EventCRFBean)request.getAttribute(INPUT_EVENT_CRF);
        FormProcessor fp = new FormProcessor(request);
        SectionBean sb = (SectionBean)request.getAttribute(SECTION_BEAN);

        // BWP 12/2/07>> The following COUNT_VALIDATE session attribute is not
        // accessible,
        // for unknown reasons (threading problems?), when
        // double-data entry displays error messages; it's value is always 0; so
        // I have to create my
        // own session variable here to keep track of DDE stages

        // We'll go by the SectionBean's ordinal first
        int tabNumber = 1;
        if (sb != null) {
            tabNumber = sb.getOrdinal();
        }
        // if tabNumber still isn't valid, check the "tab" parameter
        if (tabNumber < 1) {
            if (fp == null) {
                fp = new FormProcessor(request);
            }
            String tab = fp.getString("tab");
            if (tab == null || tab.length() < 1) {
                tabNumber = 1;
            } else {
                tabNumber = fp.getInt("tab");
            }
        }
        SectionDAO sectionDao = new SectionDAO(getDataSource());
        int crfVersionId = ecb.getCRFVersionId();
        int eventCRFId = ecb.getId();
        ArrayList sections = sectionDao.findAllByCRFVersionId(crfVersionId);
        int sectionSize = sections.size();

        HttpSession mySession = request.getSession();
        DoubleDataProgress doubleDataProgress = (DoubleDataProgress) mySession.getAttribute(DDE_PROGESS);
        if (doubleDataProgress == null || doubleDataProgress.getEventCRFId() != eventCRFId) {
            doubleDataProgress = new DoubleDataProgress(sectionSize, eventCRFId);
            mySession.setAttribute(DDE_PROGESS, doubleDataProgress);
        }
        boolean hasVisitedSection = doubleDataProgress.getSectionVisited(tabNumber, eventCRFId);

        // setting up one-time validation here
        // admit that it's an odd place to put it, but where else?
        // placing it in dataentryservlet is creating too many counts
        int keyId = ecb.getId();
        Integer count = (Integer) session.getAttribute(COUNT_VALIDATE + keyId);
        if (count != null) {
            count++;
            session.setAttribute(COUNT_VALIDATE + keyId, count);
            LOGGER.info("^^^just set count to session: " + count);
        } else {
            count = 0;
            session.setAttribute(COUNT_VALIDATE + keyId, count);
            LOGGER.info("***count not found, set to session: " + count);
        }

        DataEntryStage stage = ecb.getStage();
        if (stage.equals(DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE) && !hasVisitedSection) {
            // if the user has not entered this section yet in Double Data
            // Entry, then
            // set a flag that default values should be shown in the form
            request.setAttribute(DDE_ENTERED, true);

        }
        // Now update the session attribute
        doubleDataProgress.setSectionVisited(eventCRFId, tabNumber, true);
        mySession.setAttribute("doubleDataProgress", doubleDataProgress);
        // StudyEventStatus status =
        Role r = currentRole.getRole();
        session.setAttribute("mayProcessUploading", "true");

        //        if (!SubmitDataServlet.maySubmitData(ub, currentRole)) {
        //            this.session.setAttribute("mayProcessUploading", "false");
        //            String exceptionName = resexception.getString("no_permission_validation");
        //            String noAccessMessage = resexception.getString("not_perfom_validation_syscontact");
        //
        //            addPageMessage(noAccessMessage);
        //            throw new InsufficientPermissionException(Page.MENU, exceptionName, "1");
        //        }
        //
        //        if (stage.equals(DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE)) {
        //            if (userIsOwnerAndLessThanTwelveHoursHavePassed() && !r.equals(Role.STUDYDIRECTOR) && !r.equals(Role.COORDINATOR)) {
        //                this.session.setAttribute("mayProcessUploading", "false");
        //                addPageMessage(respage.getString("since_perform_data_entry"));
        //                throw new InsufficientPermissionException(Page.LIST_STUDY_SUBJECTS_SERVLET, resexception.getString("owner_attempting_double_data_entry"), "1");
        //            }
        //        } else if (stage.equals(DataEntryStage.DOUBLE_DATA_ENTRY)) {
        //            if (ub.getId() != ecb.getValidatorId() && !r.equals(Role.STUDYDIRECTOR) && !r.equals(Role.COORDINATOR)) {
        //                this.session.setAttribute("mayProcessUploading", "false");
        //                addPageMessage(respage.getString("validation_has_already_begun"));
        //                throw new InsufficientPermissionException(Page.LIST_STUDY_SUBJECTS_SERVLET, resexception
        //                        .getString("non_validator_attempting_double_data_entry"), "1");
        //            }
        //        } else {
        //            this.session.setAttribute("mayProcessUploading", "false");
        //            addPageMessage(respage.getString("not_perform_validation"));
        //            throw new InsufficientPermissionException(Page.LIST_STUDY_SUBJECTS_SERVLET, resexception.getString("using_double_data_entry_CRF_completed"), "1");
        //        }

        return;
    }

    /*
     * (non-Javadoc)
     *
     * @seeorg.akaza.openclinica.control.submit.DataEntryServlet#
     * validateInputOnFirstRound()
     */
    @Override
    protected boolean validateInputOnFirstRound() {
        return true;
    }

    @Override
    protected DisplayItemBean validateDisplayItemBean(DiscrepancyValidator v, DisplayItemBean dib, String inputName, HttpServletRequest request) {

        org.akaza.openclinica.bean.core.ResponseType rt = dib.getMetadata().getResponseSet().getResponseType();
        HttpSession session = request.getSession();
        EventCRFBean ecb = (EventCRFBean)request.getAttribute(INPUT_EVENT_CRF);

        boolean isSingleItem = false;
        if (StringUtil.isBlank(inputName)) {// for single items
            inputName = getInputName(dib);
            isSingleItem = true;
        }


        // we only give warning to user if data entered in DDE is different from
        // IDE when the first
        // time user hits 'save'
        int keyId = ecb.getId();
        Integer validationCount = (Integer) session.getAttribute(COUNT_VALIDATE + keyId);

        // @pgawade 12-Aug-2011 issue 10601:
        // 1. Moved the call to loadFormValue method setting values
        // from form here. It was at the end
        // of this method before that is after validations and so an extra
        // validation for checking value entered
        // against the data type was getting implemented for non-repeating group
        // items.
        // 2. Copying the form data into new object for non repeating items to
        // pass to validation
        // for matching IDE and DDE values. Id same reference is used, the way
        // code here was written, it affetcs
        // the value used in validation of data entered in DDE against the
        // related data type
        ItemDataBean valueToCompareTmp = dib.getData();
        ItemDataBean valueToCompare = copyItemDataBean(valueToCompareTmp);

        if (!isSingleItem) {
            valueToCompare = dib.getDbData();
        }


        // only load form value when an item is not in a group,
        // if in group, the value is already loaded
        // see formGroups = loadFormValueForItemGroup(digb,digbs,formGroups);
        if (isSingleItem) {
            dib = loadFormValue(dib, request);
        }

        boolean showOriginalItem = getItemMetadataService().isShown(dib.getItem().getId(), ecb, valueToCompare);// was dib.getData()
        boolean showItem = dib.getMetadata().isShowItem();
        if(!showItem && dib.getScdData().getScdItemMetadataBean().getScdItemFormMetadataId()>0) {
            showItem = true;
        }
        boolean showDuplicateItem = getItemMetadataService().hasPassedDDE(dib.getMetadata(), ecb, valueToCompare);//.isShown(dib.getItem().getId(), ecb, dib.getDbData());// where is the set db data?
        LOGGER.debug("*** show original item has value " + dib.getData().getValue() + " and show item has value " + valueToCompare.getValue());
        LOGGER.debug("--- show original: " + showOriginalItem + " show duplicate: " + showDuplicateItem + " and just show item: " + showItem);
        LOGGER.debug("VALIDATION COUNT " + validationCount);
        if (showOriginalItem && showDuplicateItem || showItem) {
            if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.TEXT) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.TEXTAREA)) {
                dib = validateDisplayItemBeanText(v, dib, inputName, request);
                if (validationCount == null || validationCount.intValue() == 0) {
                    v.addValidation(inputName, Validator.MATCHES_INITIAL_DATA_ENTRY_VALUE, valueToCompare, false);
                    v.setErrorMessage(respage.getString("value_you_specified") + " " + valueToCompare.getValue() + " "
                        + respage.getString("from_initial_data_entry"));
                }

            } else if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.FILE)) {
            	dib = validateDisplayItemBeanText(v, dib, inputName, request);
            	if (validationCount == null || validationCount.intValue() == 0) {
            		// we want to check if the current uploaded file matched the initial data file, quick way to do it is by using the hash
            		// value of the file content that was added to the filename

            		// cloned for prosperity
            		ItemDataBean pathStrippedValueToCompare;
            		try {
            			pathStrippedValueToCompare = (ItemDataBean) BeanUtils.cloneBean(valueToCompare);
            		} catch (Throwable e) {
            			e.printStackTrace();
            			return dib;
            		}
            		// the value we want to check only has filename with no path, so we strip path from the one stored in database, and compare
            		pathStrippedValueToCompare.setValue(FilenameUtils.getName(valueToCompare.getValue()));

            		v.addValidation(inputName, Validator.MATCHES_INITIAL_DATA_ENTRY_VALUE, pathStrippedValueToCompare, false);
            		v.setErrorMessage(respage.getString("value_you_specified") + " " + pathStrippedValueToCompare.getValue() + " "
            			+ respage.getString("from_initial_data_entry"));
            	}

            } else if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.RADIO) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.SELECT)) {
                dib = validateDisplayItemBeanSingleCV(v, dib, inputName);
                // ItemFormMetadataBean ifmdb = dib.getMetadata();
                // ResponseSetBean rsBean = ifmdb.getResponseSet();
                // logger.info("### found a response set count of "+inputName+"
                // "+rsBean.getOptions().size());
                // TODO sees it at this end tbh 1878

                if (validationCount == null || validationCount.intValue() == 0) {
                    v.addValidation(inputName, Validator.MATCHES_INITIAL_DATA_ENTRY_VALUE, valueToCompare, false);
                    String errorValue = valueToCompare.getValue();

                    java.util.ArrayList options = dib.getMetadata().getResponseSet().getOptions();

                    for (int u = 0; u < options.size(); u++) {
                        ResponseOptionBean rob = (ResponseOptionBean) options.get(u);
                        if (rob.getValue().equals(errorValue)) {
                            errorValue = rob.getText();
                        }
                    }
                    v.setErrorMessage(respage.getString("value_you_specified") + " " + errorValue + " " + respage.getString("from_initial_data_entry"));
                }
            } else if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.CHECKBOX) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.SELECTMULTI)) {
                dib = validateDisplayItemBeanMultipleCV(v, dib, inputName);

                if (validationCount == null || validationCount.intValue() == 0) {
                    v.addValidation(inputName, Validator.MATCHES_INITIAL_DATA_ENTRY_VALUE, valueToCompare, true);
                    // repeated from above, tbh 112007
                    String errorValue = valueToCompare.getValue();
                    String errorTexts = "";

                    java.util.ArrayList options = dib.getMetadata().getResponseSet().getOptions();

                    for (int u = 0; u < options.size(); u++) {
                        ResponseOptionBean rob = (ResponseOptionBean) options.get(u);
                        if (errorValue.contains(rob.getValue())) {
                            errorTexts = errorTexts + rob.getText();
                            if (u < options.size() - 1) {
                                // the values for multi-select are seperated by
                                // comma
                                errorTexts = errorTexts + ", ";
                            }
                        }
                    }
                    v.setErrorMessage(respage.getString("value_you_specified") + " " + errorTexts + " " + respage.getString("from_initial_data_entry"));
                }
            }

        }
        // // only load form value when an item is not in a group,
        // // if in group, the value is already loaded
        // // see formGroups = loadFormValueForItemGroup(digb,digbs,formGroups);
        // if (isSingleItem) {
        // dib = loadFormValue(dib, request);
        // }

        return dib;

    }

    // note that this step sets us up both for
    // displaying the data on the form again, in the event of an error
    // and sending the data to the database, in the event of no error
    //
    // we have to do this after adding the validations, so that we don't
    // overwrite the value the item data bean had from initial data entry
    // before the validator stores it as part of the Matches Initial Data Entry
    // Value validation
    // dib = loadFormValue(dib);
    // return dib;
    // }
    // should be from the db, we check here for a difference
    @Override
    protected List<DisplayItemGroupBean> validateDisplayItemGroupBean(DiscrepancyValidator v, DisplayItemGroupBean digb, List<DisplayItemGroupBean> digbs,
            List<DisplayItemGroupBean> formGroups, HttpServletRequest request, HttpServletResponse response) {
        EventDefinitionCRFBean edcb = (EventDefinitionCRFBean)request.getAttribute(EVENT_DEF_CRF_BEAN);
        HttpSession session = request.getSession();
        LOGGER.info("===got this far");
        EventCRFBean ecb = (EventCRFBean)request.getAttribute(INPUT_EVENT_CRF);

        int keyId = ecb.getId();
        Integer validationCount = (Integer) session.getAttribute(COUNT_VALIDATE + keyId);

        formGroups = loadFormValueForItemGroup(digb, digbs, formGroups, edcb.getId(), request);
        LOGGER
                .info("found formgroups size for " + digb.getGroupMetaBean().getName() + ": " + formGroups.size() + " compare to db groups size: "
                    + digbs.size());

        String inputName = "";
        for (int i = 0; i < formGroups.size(); i++) {
            DisplayItemGroupBean displayGroup = formGroups.get(i);

            List<DisplayItemBean> items = displayGroup.getItems();
            int order = displayGroup.getOrdinal();
            if (displayGroup.isAuto() && displayGroup.getFormInputOrdinal() > 0) {
                order = displayGroup.getFormInputOrdinal();
            }
            for (DisplayItemBean displayItem : items) {
                if (displayGroup.isAuto()) {
                    inputName = getGroupItemInputName(displayGroup, order, displayItem);
                } else {
                    inputName = getGroupItemManualInputName(displayGroup, order, displayItem);
                }
                validateDisplayItemBean(v, displayItem, inputName, request);
            }

            if (validationCount == null || validationCount.intValue() == 0) {
                if (i == 0 && formGroups.size() != digbs.size()) {
                    v.addValidation(inputName + "group", Validator.DIFFERENT_NUMBER_OF_GROUPS_IN_DDE);
                    // TODO internationalize this string, tbh
                    v.setErrorMessage("There are additional values here that were not present in the initial data entry. You have entered a different number of groups"
                                + " for the item groups containing " + inputName);

                }
            }
        }

        return formGroups;

    }

    @Override
    protected DisplayItemBean validateCalcTypeDisplayItemBean(ScoreItemValidator sv, DisplayItemBean dib, String inputName, HttpServletRequest request) {

        org.akaza.openclinica.bean.core.ResponseType rt = dib.getMetadata().getResponseSet().getResponseType();
        ItemDataDAO iddao = new ItemDataDAO(getDataSource());
        boolean isSingleItem = false;
        HttpSession session = request.getSession();
        if (StringUtil.isBlank(inputName)) {// for single items
            inputName = getInputName(dib);
            isSingleItem = true;
        }
        EventCRFBean ecb = (EventCRFBean)request.getAttribute(INPUT_EVENT_CRF);

        // we only give warning to user if data entered in DDE is different from
        // IDE when the first
        // time user hits 'save'
        int keyId = ecb.getId();
        Integer validationCount = (Integer) session.getAttribute(COUNT_VALIDATE + keyId);

        ItemDataBean valueToCompare = new ItemDataBean();
        if (isSingleItem) {
            int idId = dib.getData().getId();
            if (idId > 0) {
                               valueToCompare = (ItemDataBean) iddao.findByPK(idId);
            }
        } else {
            valueToCompare = dib.getDbData();
        }
        if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.CALCULATION) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.GROUP_CALCULATION)) {
            boolean showOriginalItem = getItemMetadataService().isShown(dib.getItem().getId(), ecb, valueToCompare);
            boolean showItem = dib.getMetadata().isShowItem();
            boolean showDuplicateItem = getItemMetadataService().hasPassedDDE(dib.getMetadata(), ecb, valueToCompare);//.isShown(dib.getItem().getId(), ecb, dib.getDbData());
            if (showOriginalItem && showDuplicateItem || showItem) {
                dib = validateDisplayItemBeanText(sv, dib, inputName, request);
            }
            if (showOriginalItem && showDuplicateItem || showItem) {
                sv.addValidation(inputName, Validator.MATCHES_INITIAL_DATA_ENTRY_VALUE, valueToCompare, false);
                sv.setErrorMessage(respage.getString("value_you_specified") + " " + valueToCompare.getValue() + " "
                    + respage.getString("from_initial_data_entry"));
            }
        }

        return dib;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.akaza.openclinica.control.submit.DataEntryServlet#getBlankItemStatus
     * ()
     */
    @Override
    protected Status getBlankItemStatus() {
        return Status.PENDING;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.akaza.openclinica.control.submit.DataEntryServlet#getNonBlankItemStatus
     * ()
     */
    @Override
    protected Status getNonBlankItemStatus(HttpServletRequest request) {
        return Status.UNAVAILABLE;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.akaza.openclinica.control.submit.DataEntryServlet#getEventCRFAnnotations
     * ()
     */
    @Override
    protected String getEventCRFAnnotations(HttpServletRequest request) {
        EventCRFBean ecb = (EventCRFBean)request.getAttribute(INPUT_EVENT_CRF);

        return ecb.getValidatorAnnotations();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.akaza.openclinica.control.submit.DataEntryServlet#setEventCRFAnnotations
     * (java.lang.String)
     */
    @Override
    protected void setEventCRFAnnotations(String annotations, HttpServletRequest request) {
        EventCRFBean ecb = (EventCRFBean)request.getAttribute(INPUT_EVENT_CRF);

        ecb.setValidatorAnnotations(annotations);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.submit.DataEntryServlet#getJSPPage()
     */
    @Override
    protected Page getJSPPage() {
        return Page.DOUBLE_DATA_ENTRY;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.akaza.openclinica.control.submit.DataEntryServlet#getServletPage()
     */
    @Override
    protected String getServletPage(HttpServletRequest request) {
        FormProcessor fp = new FormProcessor(request);
        String tabId = fp.getString("tab", true);
        String sectionId = fp.getString(DataEntryServlet.INPUT_SECTION_ID, true);
        String eventCRFId = fp.getString(INPUT_EVENT_CRF_ID, true);
        if (StringUtil.isBlank(sectionId) || StringUtil.isBlank(tabId)) {
            return Page.DOUBLE_DATA_ENTRY_SERVLET.getFileName();
        } else {
            Page target = Page.DOUBLE_DATA_ENTRY_SERVLET;
           return target.getFileName() + "?eventCRFId=" + eventCRFId + "&sectionId=" + sectionId + "&tab=" + tabId;
           // return target;
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.submit.DataEntryServlet#loadDBValues()
     */
    @Override
    protected boolean shouldLoadDBValues(DisplayItemBean dib) {
        // my understanding-jxu:
        // if the status is pending, should not load the db value
        // if the status is UNAVAILABLE,load DB value
        // interesting bug here: some fields load, some don't
        // remove a session value here:
        // int keyId = ecb.getId();
        // session.removeAttribute(COUNT_VALIDATE+keyId);
        // logger.info("^^^removed count_validate here");
        // wonky place to do it, but no other place at the moment, tbh
        if (dib.getData().getStatus() == null || dib.getData().getStatus().equals(Status.UNAVAILABLE)) {
            return true;
        }
        /*
         * if (!dib.getData().getStatus().equals(Status.UNAVAILABLE)) {
         * logger.info("status don't match.."); return false; //return true; }
         */

        // how about this instead:
        // if it's pending, return false
        // otherwise return true?
        if (dib.getData().getStatus().equals(Status.PENDING)) {
            // logger.info("status was pending...");
            return false;
            // return true;
        }

        return true;
    }

    @Override
    protected DisplayItemBean validateDisplayItemBean(DiscrepancyValidator v, DisplayItemBean dib, String inputName, RuleValidator rv,
            HashMap<String, ArrayList<String>> groupOrdinalPLusItemOid, Boolean fireRuleValidation, ArrayList<String> messages, HttpServletRequest request) {

        org.akaza.openclinica.bean.core.ResponseType rt = dib.getMetadata().getResponseSet().getResponseType();
        EventCRFBean ecb = (EventCRFBean)request.getAttribute(INPUT_EVENT_CRF);

        boolean isSingleItem = false;
        if (StringUtil.isBlank(inputName)) {// for single items
            inputName = getInputName(dib);
            isSingleItem = true;
        }
        // we only give warning to user if data entered in DDE is different from
        // IDE when the first
        // time user hits 'save'
        int keyId = ecb.getId();
        Integer validationCount = (Integer) request.getSession().getAttribute(COUNT_VALIDATE + keyId);

        ItemDataBean valueToCompare = dib.getData();
        if (!isSingleItem) {
            valueToCompare = dib.getDbData();
        }

        if (isSingleItem) {
            dib = loadFormValue(dib, request);
        }
        if (groupOrdinalPLusItemOid.containsKey(dib.getItem().getOid()) || fireRuleValidation) {
            messages = messages == null ? groupOrdinalPLusItemOid.get(dib.getItem().getOid()) : messages;
            dib = validateDisplayItemBeanSingleCV(rv, dib, inputName, messages);
        }

        return dib;

    }

    @Override
    protected List<DisplayItemGroupBean> validateDisplayItemGroupBean(DiscrepancyValidator v, DisplayItemGroupBean digb, List<DisplayItemGroupBean> digbs,
            List<DisplayItemGroupBean> formGroups, RuleValidator rv, HashMap<String, ArrayList<String>> groupOrdinalPLusItemOid, HttpServletRequest request, HttpServletResponse response) {
        EventCRFBean ecb = (EventCRFBean)request.getAttribute(INPUT_EVENT_CRF);
        EventDefinitionCRFBean edcb = (EventDefinitionCRFBean)request.getAttribute(EVENT_DEF_CRF_BEAN);
        // logger.info("===got this far");
        int keyId = ecb.getId();
        Integer validationCount = (Integer) request.getSession().getAttribute(COUNT_VALIDATE + keyId);

        formGroups = loadFormValueForItemGroup(digb, digbs, formGroups, edcb.getId(), request);
        LOGGER
                .info("found formgroups size for " + digb.getGroupMetaBean().getName() + ": " + formGroups.size() + " compare to db groups size: "
                    + digbs.size());

        String inputName = "";
        for (int i = 0; i < formGroups.size(); i++) {
            DisplayItemGroupBean displayGroup = formGroups.get(i);

            List<DisplayItemBean> items = displayGroup.getItems();
            int order = displayGroup.getOrdinal();
            if (displayGroup.isAuto() && displayGroup.getFormInputOrdinal() > 0) {
                order = displayGroup.getFormInputOrdinal();
            }
            for (DisplayItemBean displayItem : items) {
                if (displayGroup.isAuto()) {
                    inputName = getGroupItemInputName(displayGroup, order, displayItem);
                } else {
                    inputName = getGroupItemManualInputName(displayGroup, order, displayItem);
                }
                if (displayItem.getMetadata().isShowItem() || getItemMetadataService().isShown(displayItem.getItem().getId(), ecb, displayItem.getData())) {
                    // add the validation
                    if (groupOrdinalPLusItemOid.containsKey(displayItem.getItem().getOid())
                        || groupOrdinalPLusItemOid.containsKey(String.valueOf(order + 1) + displayItem.getItem().getOid())) {
                    	LOGGER.debug("IN : " + String.valueOf(order + 1) + displayItem.getItem().getOid());
                        validateDisplayItemBean(v, displayItem, inputName, rv, groupOrdinalPLusItemOid, true, groupOrdinalPLusItemOid.get(String
                                .valueOf(order + 1)
                            + displayItem.getItem().getOid()), request);
                    } else {
                        validateDisplayItemBean(v, displayItem, inputName, rv, groupOrdinalPLusItemOid, false, null, request);
                    }
                } else {
                	LOGGER.debug("OUT : " + String.valueOf(order + 1) + displayItem.getItem().getOid());
                }
                // validateDisplayItemBean(v, displayItem, inputName);
            }

        }
        return formGroups;
    }

    @Override
    protected boolean shouldRunRules() {
        return true;
    }

    @Override
    protected boolean isAdministrativeEditing() {
        return false;
    }

    @Override
    protected boolean isAdminForcedReasonForChange(HttpServletRequest request) {
        return false;
    }

    private ItemDataBean copyItemDataBean(ItemDataBean src) {
        ItemDataBean result = new ItemDataBean();
        result.setEventCRFId(src.getEventCRFId());
        result.setItemId(src.getItemId());
        result.setValue(src.getValue());
        result.setOrdinal(src.getOrdinal());
        result.setSelected(src.isSelected());
        result.setAuditLog(src.isAuditLog());
        result.setCreatedDate(src.getCreatedDate());
        result.setUpdatedDate(src.getUpdatedDate());
        result.setOwner(src.getOwner());
        result.setOwnerId(src.getOwnerId());
        result.setUpdater(src.getUpdater());
        result.setUpdaterId(src.getUpdaterId());
        result.setStatus(src.getStatus());

        return result;

    }
}
