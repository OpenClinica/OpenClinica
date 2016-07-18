package org.akaza.openclinica.controller;

import org.akaza.openclinica.bean.core.DiscrepancyNoteType;
import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.*;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.*;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

@Controller
@RequestMapping(value = "/auth/api/v1/discrepancynote")
@ResponseStatus(value = org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
public class DiscrepancyNoteController {

	@Autowired
	@Qualifier("dataSource")
	private BasicDataSource dataSource;

	@Autowired
	ServletContext context;

	public static final String FORM_CONTEXT = "ecid";

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
	StudyDAO sdao;
	DiscrepancyNoteDAO dnDao;

	@RequestMapping(value = "/dnote", method = RequestMethod.POST)
	public ResponseEntity buidDiscrepancyNote(@RequestBody HashMap<String, String> map, HttpServletRequest request) throws Exception {
		ResourceBundleProvider.updateLocale(new Locale("en_US"));
		System.out.println("I'm in EnketoForm DN Rest Method");
		org.springframework.http.HttpStatus httpStatus = null;

		String se_oid = map.get("EntityID");
		String ordinal = map.get("Ordinal");
		String entityName = map.get("EntityName"); // start_date , end_date , location
		String studySubjectOid = map.get("SS_OID");
		String noteType = map.get("NoteType");
		String resolutionStatus = map.get("Status");
		String assignedUser = map.get("AssignedUser");
		String owner = map.get("Owner");
		String description = map.get("Description");
		String detailedNotes = map.get("DetailedNote");
		String dn_id = map.get("DN_Id");
		dn_id = dn_id != null ? dn_id.replaceFirst("DN_",""): dn_id;

		UserAccountDAO udao = new UserAccountDAO(dataSource);
		StudySubjectDAO ssdao = new StudySubjectDAO(dataSource);
		StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(dataSource);
		StudyEventDAO sedao = new StudyEventDAO(dataSource);
		DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(dataSource);

		UserAccountBean assignedUserBean = (UserAccountBean) udao.findByUserName(assignedUser);
		UserAccountBean ownerBean = (UserAccountBean)request.getSession().getAttribute("userBean");
		StudySubjectBean ssBean = ssdao.findByOid(studySubjectOid);
		StudyEventDefinitionBean sedBean = seddao.findByOid(se_oid);
		StudyBean studyBean = getStudy(sedBean.getStudyId());
		StudyEventBean seBean = (StudyEventBean) sedao.findByStudySubjectIdAndDefinitionIdAndOrdinal(ssBean.getId(), sedBean.getId(), Integer.valueOf(ordinal));
		String entityType = "studyEvent";

		DiscrepancyNoteBean parent = (DiscrepancyNoteBean) dndao.findByPK(Integer.valueOf(dn_id));
		
		if (!mayProceed(resolutionStatus, noteType, seBean, entityName, parent, ownerBean)) {
			httpStatus = org.springframework.http.HttpStatus.BAD_REQUEST;
			return new ResponseEntity(httpStatus);
		}

		if (!parent.isActive()){
			saveFieldNotes(description, detailedNotes, seBean.getId(), entityType, studyBean, ownerBean, assignedUserBean, resolutionStatus, noteType, entityName);
			httpStatus = org.springframework.http.HttpStatus.OK;
		} else {
			createDiscrepancyNoteBean(description, detailedNotes, seBean.getId(), entityType, studyBean, ownerBean, assignedUserBean, parent.getId(), resolutionStatus, noteType, entityName);
			httpStatus = org.springframework.http.HttpStatus.OK;
		}
		return new ResponseEntity(httpStatus);
	}

	private StudyBean getParentStudy(Integer studyId) {
		StudyBean study = getStudy(studyId);
		if (study.getParentStudyId() == 0) {
			return study;
		} else {
			StudyBean parentStudy = (StudyBean) sdao.findByPK(study.getParentStudyId());
			return parentStudy;
		}

	}

	private StudyBean getParentStudy(String studyOid) {
		StudyBean study = getStudy(studyOid);
		if (study.getParentStudyId() == 0) {
			return study;
		} else {
			StudyBean parentStudy = (StudyBean) sdao.findByPK(study.getParentStudyId());
			return parentStudy;
		}

	}

	private StudyBean getStudy(Integer id) {
		sdao = new StudyDAO(dataSource);
		StudyBean studyBean = (StudyBean) sdao.findByPK(id);
		return studyBean;
	}

	private StudyBean getStudy(String oid) {
		sdao = new StudyDAO(dataSource);
		StudyBean studyBean = (StudyBean) sdao.findByOid(oid);
		return studyBean;
	}

	private Date getDate(String dateInString) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
		Date date = formatter.parse(dateInString);

		System.out.println(date);
		System.out.println(formatter.format(date));

		return date;

	}

	public void saveFieldNotes(String description, String detailedNotes, int entityId, String entityType, StudyBean sb, UserAccountBean ownerBean, UserAccountBean assignedUserBean,
			String resolutionStatus, String noteType, String entityName) {

		// Create a new thread each time
		DiscrepancyNoteBean parent = createDiscrepancyNoteBean(description, detailedNotes, entityId, entityType, sb, ownerBean, assignedUserBean, null, resolutionStatus, noteType, entityName);
		createDiscrepancyNoteBean(description, detailedNotes, entityId, entityType, sb, ownerBean, assignedUserBean, parent.getId(), resolutionStatus, noteType, entityName);

	}

	private DiscrepancyNoteBean createDiscrepancyNoteBean(String description, String detailedNotes, int entityId, String entityType, StudyBean sb, UserAccountBean ownerBean,
			UserAccountBean assignedUserBean, Integer parentId, String resolutionStatus, String noteType, String entityName) {
		DiscrepancyNoteBean dnb = new DiscrepancyNoteBean();

		dnb.setResStatus(ResolutionStatus.getByName(resolutionStatus));

		dnb.setEntityId(entityId);
		dnb.setStudyId(sb.getId());
		dnb.setEntityType(entityType);
		dnb.setDescription(description);
		dnb.setDetailedNotes(detailedNotes);
		dnb.setColumn(entityName);
		dnb.setOwner(ownerBean);
		dnb.setAssignedUser(assignedUserBean);
		dnb.setDisType(DiscrepancyNoteType.getByName(noteType));
		dnb.setDiscrepancyNoteTypeId(dnb.getDisType().getId());
		dnb.setResolutionStatusId(dnb.getResStatus().getId());
		dnb.setAssignedUserId(dnb.getAssignedUser().getId());

		if (parentId != null) {
			dnb.setParentDnId(parentId);
		}
		dnb = (DiscrepancyNoteBean) getDnDao().create(dnb);
		getDnDao().createMapping(dnb);

		DiscrepancyNoteBean eventParentNote = (DiscrepancyNoteBean) getDnDao().findByPK(dnb.getParentDnId());
		eventParentNote.setResolutionStatusId(dnb.getResolutionStatusId());
		eventParentNote.setDiscrepancyNoteTypeId(dnb.getDiscrepancyNoteTypeId());
		eventParentNote.setAssignedUserId(assignedUserBean.getId());
		eventParentNote.setUpdater(ownerBean);
		getDnDao().update(eventParentNote); // update parent DN
		if(eventParentNote.getAssignedUserId() != 0) {
			getDnDao().updateAssignedUser(eventParentNote); // update parent DN assigned user
		}

		return dnb;

	}

	public DiscrepancyNoteDAO getDnDao() {
		dnDao = new DiscrepancyNoteDAO(dataSource);
		return dnDao;
	}

	public void setDnDao(DiscrepancyNoteDAO dnDao) {
		this.dnDao = dnDao;
	}

	public Boolean mayProceed(String resolutionStatus, String noteType, StudyEventBean seBean, String entityName, DiscrepancyNoteBean parent, UserAccountBean ownerBean) {
		Boolean result = true;
		if (!resolutionStatus.equals("Updated") && !resolutionStatus.equals("Resolution Proposed") && !resolutionStatus.equals("Closed") && !resolutionStatus.equals("New")) {
			result = false;
		}
		if (!noteType.equals("Annotation") && !noteType.equals("Query") && !noteType.equals("Reason for Change") && !noteType.equals("Failed Validation Check")) {
			result = false;
		}
		if (!seBean.isActive()) {
			result = false;
		}
		if (!entityName.equals("start_date") && !entityName.equals("end_date") && !entityName.equals("location")) {
			result = false;
		}
		if (!ownerBean.isActive()) {
			result = false;
		}

		return result;
	}

}
