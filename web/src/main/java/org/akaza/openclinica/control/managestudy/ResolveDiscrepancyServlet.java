/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 *
 * Created on Sep 22, 2005
 */
package org.akaza.openclinica.control.managestudy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.DiscrepancyNoteType;
import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.Utils;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.FormLayoutBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.bean.submit.ItemGroupMetadataBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.submit.CreateDiscrepancyNoteServlet;
import org.akaza.openclinica.control.submit.EnketoFormServlet;
import org.akaza.openclinica.control.submit.EnterDataForStudyEventServlet;
import org.akaza.openclinica.control.submit.TableOfContentsServlet;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.hibernate.VersioningMapDao;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.FormLayoutDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.dao.submit.ItemGroupDAO;
import org.akaza.openclinica.dao.submit.ItemGroupMetadataDAO;
import org.akaza.openclinica.domain.datamap.VersioningMap;
import org.akaza.openclinica.domain.xform.XformParser;
import org.akaza.openclinica.domain.xform.dto.Bind;
import org.akaza.openclinica.domain.xform.dto.Body;
import org.akaza.openclinica.domain.xform.dto.Form;
import org.akaza.openclinica.domain.xform.dto.Group;
import org.akaza.openclinica.domain.xform.dto.Head;
import org.akaza.openclinica.domain.xform.dto.Html;
import org.akaza.openclinica.domain.xform.dto.Instance;
import org.akaza.openclinica.domain.xform.dto.Item;
import org.akaza.openclinica.domain.xform.dto.ItemSet;
import org.akaza.openclinica.domain.xform.dto.Itext;
import org.akaza.openclinica.domain.xform.dto.Label;
import org.akaza.openclinica.domain.xform.dto.Meta;
import org.akaza.openclinica.domain.xform.dto.Model;
import org.akaza.openclinica.domain.xform.dto.Repeat;
import org.akaza.openclinica.domain.xform.dto.RootItem;
import org.akaza.openclinica.domain.xform.dto.Select;
import org.akaza.openclinica.domain.xform.dto.Select1;
import org.akaza.openclinica.domain.xform.dto.Text;
import org.akaza.openclinica.domain.xform.dto.Translation;
import org.akaza.openclinica.domain.xform.dto.UserControl;
import org.akaza.openclinica.service.DiscrepancyNoteUtil;
import org.akaza.openclinica.service.crfdata.EnketoUrlService;
import org.akaza.openclinica.service.crfdata.xform.PFormCacheSubjectContextEntry;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InconsistentStateException;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.pform.PFormCache;

/**
 * @author ssachs
 *
 *         TODO To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Style - Code Templates
 */
public class ResolveDiscrepancyServlet extends SecureController {

    private static final String INPUT_NOTE_ID = "noteId";
    private static final String CAN_ADMIN_EDIT = "canAdminEdit";
    private static final String EVENT_CRF_ID = "ecId";
    public static final String ORIGINATING_PAGE = "originatingPage";
    public static final String STUDYSUBJECTID = "studySubjectId";

    private static final String RESOLVING_NOTE = "resolving_note";
    private static final String RETURN_FROM_PROCESS_REQUEST = "returnFromProcess";
    private static final String FLAVOR = "flavor";
    private static final String QUERY_FLAVOR = "-query";
    public static final String SINGLE_ITEM_FLAVOR = "-single_item";
    private static final String COMMENT = "_comment";
    public static final String QUERY_SUFFIX = "form-queries.xml";
    public static final String FS_QUERY_ATTRIBUTE = "oc:queryParent";
    public static final String VIEW_MODE = "view";
    public static final String EDIT_MODE = "edit";

    public Page getPageForForwarding(DiscrepancyNoteBean note, boolean isCompleted) {
        String entityType = note.getEntityType().toLowerCase();
        request.setAttribute("fromResolvingNotes", "yes");

        if ("subject".equalsIgnoreCase(entityType)) {
            if (ub.isSysAdmin() || ub.isTechAdmin()) {
                return Page.UPDATE_STUDY_SUBJECT_SERVLET;
            } else {
                return Page.VIEW_STUDY_SUBJECT_SERVLET;
            }
        } else if ("studysub".equalsIgnoreCase(entityType)) {
            if (ub.isSysAdmin() || ub.isTechAdmin()) {
                return Page.UPDATE_STUDY_SUBJECT_SERVLET;
            } else {
                return Page.VIEW_STUDY_SUBJECT_SERVLET;
            }
        } else if ("studyevent".equalsIgnoreCase(entityType)) {
            if (ub.isSysAdmin() || ub.isTechAdmin()) {
                return Page.UPDATE_STUDY_EVENT_SERVLET;
            } else {
                return Page.ENTER_DATA_FOR_STUDY_EVENT_SERVLET;
            }
        } else if ("itemdata".equalsIgnoreCase(entityType) || "eventcrf".equalsIgnoreCase(entityType)) {
            if (currentRole.getRole().equals(Role.MONITOR) || !isCompleted) {
                return Page.ENKETO_FORM_SERVLET;
            } else {
                return Page.ENKETO_FORM_SERVLET;
            }
        }
        return null;
    }

    public boolean prepareRequestForResolution(HttpServletRequest request, DataSource ds, StudyBean currentStudy, DiscrepancyNoteBean note, boolean isCompleted,
            String module, String flavor) throws Exception {
        String entityType = note.getEntityType().toLowerCase();
        int id = note.getEntityId();
        if ("subject".equalsIgnoreCase(entityType)) {
            StudySubjectDAO ssdao = new StudySubjectDAO(ds);
            StudySubjectBean ssb = ssdao.findBySubjectIdAndStudy(id, currentStudy);

            request.setAttribute("action", "show");
            request.setAttribute("id", String.valueOf(note.getEntityId()));
            request.setAttribute("studySubId", String.valueOf(ssb.getId()));
        } else if ("studysub".equalsIgnoreCase(entityType)) {
            request.setAttribute("action", "show");
            request.setAttribute("id", String.valueOf(note.getEntityId()));
        } else if ("eventcrf".equalsIgnoreCase(entityType)) {
            request.setAttribute("editInterview", "1");

            EventCRFDAO ecdao = new EventCRFDAO(ds);
            EventCRFBean ecb = (EventCRFBean) ecdao.findByPK(id);
            request.setAttribute(TableOfContentsServlet.INPUT_EVENT_CRF_BEAN, ecb);
            // If the request is passed along to ViewSectionDataEntryServlet,
            // that code needs
            // an event crf id; the (ecb.getId()+"") is necessary because
            // FormProcessor throws
            // a ClassCastException without the casting to a String
            request.setAttribute(ViewSectionDataEntryServlet.EVENT_CRF_ID, ecb.getId() + "");
        } else if ("studyevent".equalsIgnoreCase(entityType)) {
            StudyEventDAO sedao = new StudyEventDAO(ds);
            StudyEventBean seb = (StudyEventBean) sedao.findByPK(id);
            request.setAttribute(EnterDataForStudyEventServlet.INPUT_EVENT_ID, String.valueOf(id));
            request.setAttribute(UpdateStudyEventServlet.EVENT_ID, String.valueOf(id));
            request.setAttribute(UpdateStudyEventServlet.STUDY_SUBJECT_ID, String.valueOf(seb.getStudySubjectId()));
        }

        // this is for item data
        else if ("itemdata".equalsIgnoreCase(entityType)) {
            ItemDataDAO iddao = new ItemDataDAO(ds);
            ItemDAO idao = new ItemDAO(ds);
            ItemDataBean idb = (ItemDataBean) iddao.findByPK(id);
            ItemBean item = (ItemBean) idao.findByPK(idb.getItemId());
            ItemGroupMetadataDAO igmdao = new ItemGroupMetadataDAO<>(ds);

            EventCRFDAO ecdao = new EventCRFDAO(ds);
            EventCRFBean ecb = (EventCRFBean) ecdao.findByPK(idb.getEventCRFId());

            FormLayoutDAO fldao = new FormLayoutDAO(ds);
            FormLayoutBean formLayout = (FormLayoutBean) fldao.findByPK(ecb.getFormLayoutId());
            CRFDAO cdao = new CRFDAO(ds);
            CRFBean crf = cdao.findByLayoutId(formLayout.getId());

            StudyEventDAO sedao = new StudyEventDAO(ds);
            StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(ds);

            StudySubjectDAO ssdao = new StudySubjectDAO(sm.getDataSource());
            StudySubjectBean ssb = (StudySubjectBean) ssdao.findByPK(ecb.getStudySubjectId());

            ItemFormMetadataDAO ifmdao = new ItemFormMetadataDAO(ds);
            ItemFormMetadataBean ifmb = ifmdao.findByItemIdAndFormLayoutId(idb.getItemId(), ecb.getFormLayoutId());

            ItemGroupMetadataBean igmBean = (ItemGroupMetadataBean) igmdao.findByItemAndCrfVersion(idb.getItemId(), ecb.getCRFVersionId());
            ItemGroupDAO igdao = new ItemGroupDAO<>(ds);
            ItemGroupBean igBean = (ItemGroupBean) igdao.findByPK(igmBean.getItemGroupId());
            int repeatOrdinal = idb.getOrdinal();
            ItemDataBean idata = null;
            if (igmBean.isRepeatingGroup() && repeatOrdinal > 1) {
                if (idb.isDeleted()) {
                    repeatOrdinal = 0;
                } else {
                    List<ItemGroupMetadataBean> igms = igmdao.findMetaByGroupAndCrfVersion(igBean.getId(), ecb.getCRFVersionId());

                    for (int i = 0; i < idb.getOrdinal(); i++) {
                        for (ItemGroupMetadataBean igm : igms) {
                            idata = iddao.findByItemIdAndEventCRFIdAndOrdinal(igm.getItemId(), ecb.getId(), i + 1);
                            if (idata != null && idata.isDeleted()) {
                                repeatOrdinal--;
                                break;
                            }
                        }
                    }
                }
            }

            EnketoUrlService enketoUrlService = (EnketoUrlService) SpringServletAccess.getApplicationContext(context).getBean("enketoUrlService");
            XformParser xformParser = (XformParser) SpringServletAccess.getApplicationContext(context).getBean("xformParser");
            VersioningMapDao versioningMapDao = (VersioningMapDao) SpringServletAccess.getApplicationContext(context).getBean("versioningMapDao");
            StudyEventBean seb = (StudyEventBean) sedao.findByPK(ecb.getStudyEventId());
            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seddao.findByPK(seb.getStudyEventDefinitionId());
            // Cache the subject context for use during xform submission
            PFormCache cache = PFormCache.getInstance(context);
            PFormCacheSubjectContextEntry subjectContext = new PFormCacheSubjectContextEntry();
            subjectContext.setStudySubjectOid(ssb.getOid());
            subjectContext.setStudyEventDefinitionId(String.valueOf(seb.getStudyEventDefinitionId()));
            subjectContext.setOrdinal(String.valueOf(seb.getSampleOrdinal()));
            subjectContext.setFormLayoutOid(formLayout.getOid());
            subjectContext.setUserAccountId(String.valueOf(ub.getId()));
            subjectContext.setItemName(item.getName() + COMMENT);
            subjectContext.setItemRepeatOrdinalAdjusted(repeatOrdinal);
            subjectContext.setItemRepeatOrdinalOriginal(idb.getOrdinal());
            subjectContext.setItemInRepeatingGroup(igmBean.isRepeatingGroup());
            subjectContext.setItemRepeatGroupName(igBean.getLayoutGroupPath());
            subjectContext.setStudyEventId(String.valueOf(seb.getId()));
            String contextHash = cache.putSubjectContext(subjectContext);
            StudyBean parentStudyBean = getParentStudy(currentStudy.getOid(), ds);
            context.setAttribute("SS_OID", ssb.getOid());
            context.setAttribute("USER_ID", String.valueOf(ub.getId()));

            if (flavor.equals(SINGLE_ITEM_FLAVOR)) {
                // This section is for version migration ,where item does not exist in the current formLayout
                boolean itemExistInFormLayout = false;
                List<VersioningMap> vms = versioningMapDao.findByVersionIdAndItemId(ecb.getCRFVersionId(), item.getId());
                for (VersioningMap vm : vms) {
                    if (vm.getFormLayout().getFormLayoutId() == formLayout.getId()) {
                        itemExistInFormLayout = true;
                        break;
                    }
                }
                if (!itemExistInFormLayout)
                    formLayout = (FormLayoutBean) fldao.findByPK(vms.get(0).getFormLayout().getFormLayoutId());
                // Get Original formLayout file from data directory

                String xformOutput = "";
                String directoryPath = Utils.getFilePath() + Utils.getCrfMediaPath(parentStudyBean.getOid(), crf.getOid(), formLayout.getOid());
                File dir = new File(directoryPath);
                File[] directoryListing = dir.listFiles();
                if (directoryListing != null) {
                    for (File child : directoryListing) {
                        if ((child.getName().endsWith(QUERY_SUFFIX))) {
                            xformOutput = new String(Files.readAllBytes(Paths.get(child.getPath())));
                            break;
                        }
                    }
                }
                // Unmarshal original form layout form
                Html html = xformParser.unMarshall(xformOutput);
                Body body = html.getBody();
                Head head = html.getHead();
                Model model = head.getModel();

                List<Bind> binds = model.getBind();
                List<Instance> instances = model.getInstance();
                binds = getBindElements(binds, item);
                Itext itext = model.getItext();

                UserControl itemUserControl = null;
                UserControl itemCommentUserControl = null;

                List<UserControl> userControls = body.getUsercontrol();
                List<Group> groups = body.getGroup();
                List<Repeat> repeats = body.getRepeat();

                if (userControls != null) {
                    itemUserControl = lookForUserControlInUserControl(userControls, item.getName());
                    itemCommentUserControl = lookForUserControlInUserControl(userControls, item.getName() + COMMENT);
                }
                if (groups != null && itemUserControl == null) {
                    itemUserControl = lookForUserControlInGroup(groups, item.getName(), null);
                    itemCommentUserControl = lookForUserControlInGroup(groups, item.getName() + COMMENT, null);
                }
                if (repeats != null && itemUserControl == null) {
                    itemUserControl = lookForUserControlInRepeat(repeats, item.getName(), null);
                    itemCommentUserControl = lookForUserControlInRepeat(repeats, item.getName() + COMMENT, null);
                }

                if (itemUserControl != null) {
                    itemUserControl.setRef("/form/group_layout/" + item.getName());
                }
                if (itemCommentUserControl != null) {
                    itemCommentUserControl.setRef("/form/group_layout/" + item.getName() + COMMENT);
                }
                List<UserControl> uControls = new ArrayList<>();
                uControls.add(itemUserControl);
                uControls.add(itemCommentUserControl);

                String xform = xformParser.marshall(buildSingleItemForm(item, uControls, binds, itext, instances, seb, ssb, sed, head.getTitle()));
                xform = xform.substring(0, xform.indexOf("<meta>")) + "<group_layout>" + "<" + item.getName() + "/><" + item.getName() + COMMENT + " "
                        + FS_QUERY_ATTRIBUTE + "=\"" + item.getName() + "\"/>" + "</group_layout>" + xform.substring(xform.indexOf("<meta>"));

                String attribute = SINGLE_ITEM_FLAVOR + "[" + idb.getId() + "]";
                context.setAttribute(attribute, xform);
            }
            StudyUserRoleBean currentRole = (StudyUserRoleBean) request.getSession().getAttribute("userRole");
            Role role = currentRole.getRole();

            String formUrl = null;
            if (ecb.getId() > 0) {
                formUrl = enketoUrlService.getEditUrl(contextHash, subjectContext, currentStudy.getOid(), null, flavor, idb, role, EDIT_MODE);
            } else {
                String hash = formLayout.getXform();
                formUrl = enketoUrlService.getInitialDataEntryUrl(contextHash, subjectContext, currentStudy.getOid(), flavor, role, EDIT_MODE, hash);
            }
            int hashIndex = formUrl.lastIndexOf("#");
            String part1 = formUrl;
            String part2 = "";
            if (hashIndex != -1) {
                part1 = formUrl.substring(0, hashIndex);
                part2 = formUrl.substring(hashIndex);
            }
            request.setAttribute(EnketoFormServlet.FORM_URL1, part1);
            request.setAttribute(EnketoFormServlet.FORM_URL2, part2);
            request.setAttribute(ORIGINATING_PAGE, "ViewNotes?module=" + module);
            if (!flavor.equals(SINGLE_ITEM_FLAVOR)) {
                request.setAttribute(STUDYSUBJECTID, ssb.getLabel());
            } else {
                request.setAttribute(STUDYSUBJECTID, "");
            }
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.core.SecureController#processRequest()
     */
    @Override
    protected void processRequest() throws Exception {

        FormProcessor fp = new FormProcessor(request);
        int noteId = fp.getInt(INPUT_NOTE_ID);
        String flavor = fp.getString(FLAVOR);
        String module = (String) session.getAttribute("module");

        StudySubjectDAO studySubjectDAO = new StudySubjectDAO(sm.getDataSource());

        DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(sm.getDataSource());
        dndao.setFetchMapping(true);

        // check that the note exists
        DiscrepancyNoteBean discrepancyNoteBean = (DiscrepancyNoteBean) dndao.findByPK(noteId);

        if (!discrepancyNoteBean.isActive()) {
            throw new InconsistentStateException(Page.MANAGE_STUDY_SERVLET, resexception.getString("you_are_trying_resolve_discrepancy_not_exist"));
        }

        // check that the note has not already been closed
        ArrayList children = dndao.findAllByParent(discrepancyNoteBean);
        discrepancyNoteBean.setChildren(children);
        // This logic has been reverted, issue-7459
        // if (parentNoteIsClosed(discrepancyNoteBean)) {
        // throw new InconsistentStateException(Page.VIEW_DISCREPANCY_NOTES_IN_STUDY_SERVLET, respage
        // .getString("the_discrepancy_choose_has_been_closed_resolved_create_new"));
        // }

        // all clear, send the user to the resolved screen
        String entityType = discrepancyNoteBean.getEntityType().toLowerCase();
        discrepancyNoteBean.setResStatus(ResolutionStatus.get(discrepancyNoteBean.getResolutionStatusId()));
        discrepancyNoteBean.setDisType(DiscrepancyNoteType.get(discrepancyNoteBean.getDiscrepancyNoteTypeId()));
        // BWP 03/17/2009 3166: if it's not an ItemData type note, redirect
        // Monitors to View Subject or
        // View Study Events <<
        if (currentRole.getRole().equals(Role.MONITOR) && !"itemdata".equalsIgnoreCase(entityType) && !"eventcrf".equalsIgnoreCase(entityType)) {
            redirectMonitor(module, discrepancyNoteBean);
            return;
        }
        // >>
        // If Study is Frozen or Locked
        if (currentStudy.getStatus().isFrozen() && !"itemdata".equalsIgnoreCase(entityType) && !"eventcrf".equalsIgnoreCase(entityType)) {
            redirectMonitor(module, discrepancyNoteBean);
            return;
        }

        boolean toView = false;
        boolean isCompleted = false;
        if ("itemdata".equalsIgnoreCase(entityType)) {
            ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource());
            ItemDataBean idb = (ItemDataBean) iddao.findByPK(discrepancyNoteBean.getEntityId());

            EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());

            EventCRFBean ecb = (EventCRFBean) ecdao.findByPK(idb.getEventCRFId());
            StudySubjectBean studySubjectBean = (StudySubjectBean) studySubjectDAO.findByPK(ecb.getStudySubjectId());

            discrepancyNoteBean.setSubjectId(studySubjectBean.getId());
            discrepancyNoteBean.setItemId(idb.getItemId());

            if (ecb.getStatus().equals(Status.UNAVAILABLE)) {
                isCompleted = true;
            }

            toView = true;// we want to go to view note page if the note is
            // for item data
        }
        // logger.info("set up pop up url: " + createNoteURL);
        // System.out.println("set up pop up url: " + createNoteURL);
        boolean goNext = prepareRequestForResolution(request, sm.getDataSource(), currentStudy, discrepancyNoteBean, isCompleted, module, flavor);

        Page p = getPageForForwarding(discrepancyNoteBean, isCompleted);

        // logger.info("found page for forwarding: " + p.getFileName());
        if (p == null) {
            throw new InconsistentStateException(Page.VIEW_DISCREPANCY_NOTES_IN_STUDY_SERVLET,
                    resexception.getString("the_discrepancy_note_triying_resolve_has_invalid_type"));
        } else {
            if (p.getFileName().contains("?")) {
                if (!p.getFileName().contains("fromViewNotes=1")) {
                    p.setFileName(p.getFileName() + "&fromViewNotes=1");
                }
            } else {
                p.setFileName(p.getFileName() + "?fromViewNotes=1");
            }
            String createNoteURL = CreateDiscrepancyNoteServlet.getAddChildURL(discrepancyNoteBean, ResolutionStatus.CLOSED, true);
            setPopUpURL("");
        }

        if (!goNext) {
            setPopUpURL("");
            addPageMessage(respage.getString("you_may_not_perform_admin_edit_on_CRF_not_completed_by_user"));
            p = Page.VIEW_DISCREPANCY_NOTES_IN_STUDY_SERVLET;

        }

        forwardPage(p);
    }

    /**
     * Determines if a discrepancy note is closed or not. The note is closed if
     * it has status closed, or any of its children have closed status.
     *
     * @param note
     *            The discrepancy note. The children should already be set.
     * @return <code>true</code> if the note is closed, <code>false</code>
     *         otherwise.
     */
    public static boolean noteIsClosed(DiscrepancyNoteBean note) {
        if (note.getResolutionStatusId() == ResolutionStatus.CLOSED.getId()) {
            return true;
        }

        ArrayList children = note.getChildren();
        for (int i = 0; i < children.size(); i++) {
            DiscrepancyNoteBean child = (DiscrepancyNoteBean) children.get(i);
            if (child.getResolutionStatusId() == ResolutionStatus.CLOSED.getId()) {
                return true;
            }
        }

        return false;
    }

    public static boolean parentNoteIsClosed(DiscrepancyNoteBean parentNote) {
        if (parentNote.getResolutionStatusId() == ResolutionStatus.CLOSED.getId()) {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        String module = (String) session.getAttribute("module");
        /*
         * BWP: This caused a problem with page refreshing (the subjectId was
         * lost); so I had to comment it out if(subjectId != null){
         * session.removeAttribute("subjectId"); }
         */

        // BWP 11/03/2008 3029: redirect monitor user to ViewStudySubject if
        // they click "add note to thread" link>>
        // if (currentRole.getRole().equals(Role.MONITOR)) {
        // addPageMessage(respage.getString("no_have_permission_to_resolve_discrepancy"));
        //
        // RequestDispatcher dispatcher =
        // request.getRequestDispatcher("/ViewStudySubject?id=" + subjectId +
        // "&module=" + module);
        // try {
        // dispatcher.forward(request, response);
        // } catch (ServletException e) {
        // e.printStackTrace();
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
        // return;
        // }
        // tbh 02/2009: now removed, to allow for the JsonQuery workflow and allow a
        // Monitor to Resolve a JsonQuery.
        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)
                || currentRole.getRole().equals(Role.INVESTIGATOR) || currentRole.getRole().equals(Role.RESEARCHASSISTANT)
                || currentRole.getRole().equals(Role.RESEARCHASSISTANT2) || currentRole.getRole().equals(Role.MONITOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_permission_to_resolve_discrepancy") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_study_director_or_study_coordinator"), "1");
    }

    /**
     * Redirect the request to another page if the user is a Monitor type and
     * the discrepancy note is a type other than item data or event crf.
     *
     * @param module
     *            A String like "managestudy" or "admin"
     * @param discrepancyNoteBean
     */
    private void redirectMonitor(String module, DiscrepancyNoteBean discrepancyNoteBean) {

        if (discrepancyNoteBean != null) {

            String createNoteURL = "";
            // This String will determine whether the type is other than
            // itemdata.
            String entityType = discrepancyNoteBean.getEntityType().toLowerCase();
            // The id of the subject, study subject, or study event
            int entityId = discrepancyNoteBean.getEntityId();
            RequestDispatcher dispatcher = null;
            DiscrepancyNoteUtil discNoteUtil = new DiscrepancyNoteUtil();

            if (entityType != null && !"".equalsIgnoreCase(entityType) && !"itemdata".equalsIgnoreCase(entityType)
                    && !"eventcrf".equalsIgnoreCase(entityType)) {
                // redirect to View Study Subject
                // addPageMessage(resword.getString("monitors_do_not_have_permission_to_resolve_discrepancy_notes"));
                if ("studySub".equalsIgnoreCase(entityType)) {
                    dispatcher = request.getRequestDispatcher("/ViewStudySubject?id=" + entityId + "&module=" + module);
                    discrepancyNoteBean.setSubjectId(entityId);
                } else if ("subject".equalsIgnoreCase(entityType)) {

                    int studySubId = discNoteUtil.getStudySubjectIdForDiscNote(discrepancyNoteBean, sm.getDataSource(), currentStudy.getId());

                    dispatcher = request.getRequestDispatcher("/ViewStudySubject?id=" + studySubId + "&module=" + module);
                    discrepancyNoteBean.setSubjectId(studySubId);
                } else if ("studyevent".equalsIgnoreCase(entityType)) {
                    dispatcher = request.getRequestDispatcher("/EnterDataForStudyEvent?eventId=" + entityId);

                }

                // This code creates the URL for a popup window, which the
                // processing Servlet will initiate.
                // 'true' parameter means that ViewDiscrepancyNote is the
                // handling Servlet.
                createNoteURL = CreateDiscrepancyNoteServlet.getAddChildURL(discrepancyNoteBean, ResolutionStatus.CLOSED, true);
                request.setAttribute(POP_UP_URL, createNoteURL);

                try {
                    if (dispatcher != null) {
                        dispatcher.forward(request, response);
                    }
                } catch (ServletException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private Html buildSingleItemForm(ItemBean item, List<UserControl> userControls, List<Bind> binds, Itext itext, List<Instance> instances, StudyEventBean seb,
            StudySubjectBean ssb, StudyEventDefinitionBean sed, String title) {

        Html html = new Html();
        Head head = new Head();
        Model model = new Model();
        head.setTitle(resword.getString("view_query"));

        // Set body
        Body body = new Body();
        Group group = new Group();
        group.setRef("/form/group_layout");
        Label metaLabel = new Label();
        metaLabel.setLabel(resword.getString("subject_id") + ": " + ssb.getLabel() + "\n" + resword.getString("event_name") + ": " + sed.getName() + "\n"
                + resword.getString("event_date") + ": " + formatDate(seb.getDateStarted()) + "\n" + resword.getString("form_title") + ": " + title + "\n\n ");
        group.setLabel(metaLabel);
        group.setUsercontrol(userControls);
        List<Group> groups = new ArrayList<>();
        groups.add(group);
        body.setGroup(groups);
        // Build Instance
        Meta meta = new Meta();
        meta.setInstanceID("uuid:" + item.getName());

        Form form = new Form();
        form.setMeta(meta);
        form.setId("single_item");
        form.setVersion("v1");

        Instance instance = new Instance();
        instance.setForm(form);
        model.addInstance(instance);

        // Set Binds
        model.setBind(binds);

        // Set or Build IText
        List<String> refs = new ArrayList<>();
        List<Item> items = null;
        ItemSet itemSet = null;
        String userControlRef = null;
        for (UserControl userControl : userControls) {
            if (userControl instanceof Select || userControl instanceof Select1) {
                items = userControl.getItem();
                itemSet = userControl.getItemSet();
                if (items != null) {
                    refs = addItemRefsToList(items, refs);
                } else if (itemSet != null) {
                    String instanceId = getInstanceId(itemSet);
                    Instance inst = getInstanceByInstanceId(instanceId, instances);
                    if (inst != null) {
                        model.addInstance(inst);
                        for (RootItem rootItem : inst.getRoot().getItem()) {
                            refs.add(rootItem.getItextId());
                        }
                    }
                }

            }
            if (userControl != null) {
                userControlRef = userControl.getLabel().getRef();
            }
            if (userControlRef != null) {
                refs.add(userControlRef.substring(10, userControlRef.length() - 2));
            }
        }
        if (itext != null) {
            List<Translation> translations = itext.getTranslation();
            for (Translation translation : translations) {
                List<Text> texts = translation.getText();

                for (Iterator<Text> textIterator = texts.iterator(); textIterator.hasNext();) {
                    Text text = textIterator.next();
                    if (!refs.contains(text.getId())) {
                        textIterator.remove();
                    }
                }
            }
            model.setItext(itext);
        }

        Instance usersInstance = new Instance();
        usersInstance.setId("_users");
        usersInstance.setSrc("jr://file-csv/users.xml");
        model.addInstance(usersInstance);

        // Assemble all
        head.setModel(model);
        html.setHead(head);
        html.setBody(body);
        return html;

    }

    private String getInstanceId(ItemSet itemSet) {
        String instanceId = "";
        String nodeSet = itemSet.getNodeSet();
        if (nodeSet.startsWith("instance")) {
            int begIndex = nodeSet.indexOf("('");
            int endIndex = nodeSet.indexOf("')");
            instanceId = nodeSet.substring(begIndex + 2, endIndex);
            int index = nodeSet.indexOf("/root/item");
            itemSet.setNodeSet(nodeSet.substring(0, index + 10));
        }
        return instanceId;
    }

    private Instance getInstanceByInstanceId(String instanceId, List<Instance> instances) {
        Instance instance = null;
        for (Instance inst : instances) {
            if (inst.getId() != null && inst.getId().equals(instanceId)) {
                instance = inst;
                break;
            }
        }
        return instance;
    }

    private List<String> addItemRefsToList(List<Item> items, List<String> refs) {
        for (Item itm : items) {
            Label itmLabel = itm.getLabel();
            if (itmLabel.getRef() != null) {
                String str = itmLabel.getRef();
                refs.add(str.substring(10, str.length() - 2));
            }
        }
        return refs;
    }

    private UserControl lookForUserControlInUserControl(List<UserControl> userControls, String itemName) {
        UserControl userControl = null;
        for (UserControl uControl : userControls) {
            if (uControl.getRef().endsWith(itemName)) {
                userControl = uControl;
                break;
            }
        }
        return userControl;
    }

    private UserControl lookForUserControlInGroup(List<Group> groups, String itemName, UserControl userControl) {
        for (Group group : groups) {
            if (group.getUsercontrol() != null && userControl == null) {
                userControl = lookForUserControlInUserControl(group.getUsercontrol(), itemName);
                if (userControl != null)
                    break;
            }
            if (group.getGroup() != null && userControl == null) {
                userControl = lookForUserControlInGroup(group.getGroup(), itemName, userControl);
                if (userControl != null)
                    break;
            }
            if (group.getRepeat() != null && userControl == null) {
                userControl = lookForUserControlInRepeat(group.getRepeat(), itemName, userControl);
                if (userControl != null)
                    break;
            }
        }
        return userControl;

    }

    private UserControl lookForUserControlInRepeat(List<Repeat> repeats, String itemName, UserControl userControl) {
        for (Repeat repeat : repeats) {
            if (repeat.getUsercontrol() != null && userControl == null) {
                userControl = lookForUserControlInUserControl(repeat.getUsercontrol(), itemName);
                if (userControl != null)
                    break;
            }
            if (repeat.getGroup() != null && userControl == null) {
                userControl = lookForUserControlInGroup(repeat.getGroup(), itemName, userControl);
                if (userControl != null)
                    break;
            }
            if (repeat.getRepeat() != null && userControl == null) {
                userControl = lookForUserControlInRepeat(repeat.getRepeat(), itemName, userControl);
                if (userControl != null)
                    break;
            }
        }
        return userControl;

    }

    private List<Bind> getBindElements(List<Bind> binds, ItemBean item) {
        for (Iterator<Bind> bindIterator = binds.iterator(); bindIterator.hasNext();) {
            Bind bind = bindIterator.next();
            if (bind.getNodeSet().endsWith(item.getName())) {
                setBindProperties(bind);
                bind.setNodeSet("/form/group_layout/" + item.getName());
            } else if (bind.getNodeSet().endsWith(item.getName() + COMMENT)) {
                setBindProperties(bind);
                bind.setNodeSet("/form/group_layout/" + item.getName() + COMMENT);
                bind.setEnkFor("/form/group_layout/" + item.getName());
            } else if (bind.getNodeSet().endsWith("meta/instanceID")) {
                bind.setNodeSet("/form/meta/instanceID");
            } else {
                bindIterator.remove();
            }
        }
        return binds;
    }

    private void setBindProperties(Bind bind) {
        bind.setConstraint(null);
        bind.setCalculate(null);
        bind.setRequired(null);
        bind.setRelevant(null);
        bind.setConstraintMsg(null);
        bind.setItemGroup(null);
    }

    private String formatDate(Date date) {
        String format = resformat.getString("date_format_string");
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    private StudyBean getParentStudy(String studyOid, DataSource ds) {
        StudyDAO sdao = new StudyDAO(ds);
        StudyBean study = (StudyBean) sdao.findByOid(studyOid);
        if (study.getParentStudyId() == 0) {
            return study;
        } else {
            StudyBean parentStudy = (StudyBean) sdao.findByPK(study.getParentStudyId());
            return parentStudy;
        }
    }
}
