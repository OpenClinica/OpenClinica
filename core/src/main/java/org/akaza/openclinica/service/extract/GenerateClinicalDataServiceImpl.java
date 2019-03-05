package org.akaza.openclinica.service.extract;

import org.akaza.openclinica.bean.core.Utils;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.odmbeans.*;
import org.akaza.openclinica.bean.submit.crfdata.*;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.*;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.domain.EventCRFStatus;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.*;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.service.PermissionService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

/**
 * Generate CDISC-ODM clinical data without data set.
 * 
 * @author jnyayapathi
 * 
 */

@Service("generateClinicalDataService")
public class GenerateClinicalDataServiceImpl implements GenerateClinicalDataService {
	protected final static Logger LOGGER = LoggerFactory.getLogger("org.akaza.openclinica.service.extract.GenerateClinicalDataServiceImpl");
	protected final static String DELIMITER = ",";
	private final static String GROUPOID_ORDINAL_DELIM = ":";
	private final static String INDICATE_ALL = "*";
	private final static String OPEN_ORDINAL_DELIMITER = "[";
	private final static String CLOSE_ORDINAL_DELIMITER = "]";
	private static final Object STATUS = "Status";
	private static final Object STUDY_EVENT = "study_event";
	private static final Object SUBJECT_GROUP_MAP = "subject_group_map";
	private static boolean isActiveRoleAtSite = true;

	@Autowired
	PermissionService permissionService;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private StudyEventDao studyEventDao;

	private StudyDao studyDao;

	private StudySubjectDao studySubjectDao;

	private StudyEventDefinitionDao studyEventDefDao;

	private boolean collectDns = true;
	private boolean collectAudits = true;
	private AuditLogEventDao auditEventDAO;
	private Locale locale;

	private UserAccountDao userAccountDao;
	private StudyUserRoleDao studyUserRoleDao;
	private ItemDao itemDao;
	private ItemGroupDao itemGroupDao;
	private EventDefinitionCrfDao eventDefinitionCrfDao;
	private EventDefinitionCrfPermissionTagDao eventDefinitionCrfPermissionTagDao;

	public AuditLogEventDao getAuditEventDAO() {
		return auditEventDAO;
	}

	public void setAuditEventDAO(AuditLogEventDao auditEventDAO) {
		this.auditEventDAO = auditEventDAO;
	}

	public boolean isCollectDns() {
		return collectDns;
	}

	public void setCollectDns(boolean collectDns) {
		this.collectDns = collectDns;
	}

	public boolean isCollectAudits() {
		return collectAudits;
	}

	public void setCollectAudits(boolean collectAudits) {
		this.collectAudits = collectAudits;
	}

	public StudyEventDefinitionDao getStudyEventDefDao() {
		return studyEventDefDao;
	}

	public void setStudyEventDefDao(StudyEventDefinitionDao studyEventDefDao) {
		this.studyEventDefDao = studyEventDefDao;
	}

	public StudySubjectDao getStudySubjectDao() {
		return studySubjectDao;
	}

	public void setStudySubjectDao(StudySubjectDao studySubjectDao) {
		this.studySubjectDao = studySubjectDao;
	}

	public GenerateClinicalDataServiceImpl() {

	}

	public GenerateClinicalDataServiceImpl(String StudyOID) {

	}

	public LinkedHashMap<String, OdmClinicalDataBean> getClinicalData(String studyOID,int userId,boolean crossForm) {
		LinkedHashMap<String, OdmClinicalDataBean> hm = new LinkedHashMap<String, OdmClinicalDataBean>();

		Study study = getStudyDao().findByColumnName(studyOID, "oc_oid");
		List<StudySubject> studySubjs = study.getStudySubjects();
		if (study.getStudies().size() < 1) {
			hm.put(studyOID, constructClinicalData(study, studySubjs,userId,crossForm));
		}
		// return odmClinicalDataBean;
		else {
			hm.put(studyOID, constructClinicalData(study, studySubjs,userId,crossForm));// at study level
			for (Study s : study.getStudies()) {// all the sites
				hm.put(s.getOc_oid(), constructClinicalData(s, s.getStudySubjects(),userId,crossForm));
			}
		}

		return hm;
		// return constructClinicalData(study, studySubjs);
	}

	private List<StudySubject> listStudySubjects(String studySubjectOID) {
		ArrayList<StudySubject> studySubjs = new ArrayList<StudySubject>();
		StudySubject studySubj = getStudySubjectDao().findByColumnName(studySubjectOID, "ocOid");

		studySubjs.add(studySubj);
		return studySubjs;
	}

	public OdmClinicalDataBean getClinicalData(String studyOID, String studySubjectOID,int userId ,boolean crossForm) {
		Study study = getStudyDao().findByColumnName(studyOID, "oc_oid");

		return constructClinicalData(study, listStudySubjects(studySubjectOID),userId,crossForm);
	}

	public StudyDao getStudyDao() {
		return studyDao;
	}

	public void setStudyDao(StudyDao studyDao) {
		this.studyDao = studyDao;
	}

	private OdmClinicalDataBean constructClinicalData(Study study, List<StudySubject> studySubjs,int userId,boolean crossForm  ) {

		return constructClinicalDataStudy(studySubjs, study, null, null,userId,crossForm);
	}

	private OdmClinicalDataBean constructClinicalDataStudy(List<StudySubject> studySubjs, Study study, List<StudyEvent> studyEvents, String formVersionOID,int userId,boolean crossForm) {
		OdmClinicalDataBean odmClinicalDataBean = new OdmClinicalDataBean();
		ExportSubjectDataBean expSubjectBean;
		List<ExportSubjectDataBean> exportSubjDataBeanList = new ArrayList<ExportSubjectDataBean>();
		List<String> tagIds = null;
		if(!crossForm) {
			StudyBean studyBean = new StudyBean();
			studyBean.setId(study.getStudyId());
			studyBean.setStudyEnvUuid(study.getStudyEnvUuid());
			studyBean.setStudyUuid(study.getStudyUuid());
			studyBean.setStudyEnvSiteUuid(study.getStudyEnvSiteUuid());
			studyBean.setParentStudyId(study.getStudy() != null ? study.getStudy().getStudyId() : 0);
			tagIds = permissionService.getPermissionTagsList(studyBean, getRequest());
		}

		for (StudySubject studySubj : studySubjs) {
		if(studyEvents==null)
				studyEvents = (ArrayList<StudyEvent>) studyEventDao.fetchListSEs(studySubj.getOcOid());

			if (studyEvents != null) {
				expSubjectBean = setExportSubjectDataBean(studySubj, study, studyEvents, formVersionOID,userId,crossForm,tagIds);
				exportSubjDataBeanList.add(expSubjectBean);

				odmClinicalDataBean.setExportSubjectData(exportSubjDataBeanList);
				odmClinicalDataBean.setStudyOID(study.getOc_oid());
			}
			studyEvents=null;
		}

		return odmClinicalDataBean;
		// return null;
	}

	@SuppressWarnings("unchecked")
	private ExportSubjectDataBean setExportSubjectDataBean(StudySubject studySubj, Study study, List<StudyEvent> studyEvents, String formVersionOID,int userId,boolean crossForm,List<String> tagIds) {

		ExportSubjectDataBean exportSubjectDataBean = new ExportSubjectDataBean();

		if (subjectBelongsToStudy(study, studySubj)) {

			// exportSubjectDataBean.setAuditLogs(studySubj.getA)
			if (studySubj.getSubject().getDateOfBirth() != null)
				exportSubjectDataBean.setDateOfBirth(studySubj.getSubject().getDateOfBirth() + "");
			exportSubjectDataBean.setSubjectGender(studySubj.getSubject().getGender()!=null?studySubj.getSubject().getGender().toString():"");

			for (SubjectGroupMap subjGrpMap : studySubj.getSubjectGroupMaps()) {
				SubjectGroupDataBean subjGrpDataBean = new SubjectGroupDataBean();
				subjGrpDataBean.setStudyGroupClassId("SGC_" + subjGrpMap.getStudyGroupClass().getStudyGroupClassId());
				subjGrpDataBean.setStudyGroupClassName(subjGrpMap.getStudyGroup().getStudyGroupClass().getName());
				subjGrpDataBean.setStudyGroupName(subjGrpMap.getStudyGroup().getName());
				exportSubjectDataBean.getSubjectGroupData().add(subjGrpDataBean);
			}
			exportSubjectDataBean.setStudySubject(studySubj);
			exportSubjectDataBean.setStudySubjectId(studySubj.getLabel());
			if (studySubj.getSubject().getUniqueIdentifier() != null)
				exportSubjectDataBean.setUniqueIdentifier(studySubj.getSubject().getUniqueIdentifier());
			exportSubjectDataBean.setSecondaryId(studySubj.getSecondaryLabel());
			exportSubjectDataBean.setStatus(studySubj.getStatus().toString());
			if (isCollectAudits())
				exportSubjectDataBean.setAuditLogs(fetchAuditLogs(studySubj.getStudySubjectId(), "study_subject", studySubj.getOcOid(), null));
			AuditLogsBean subjectGroupMapLogs = fetchAuditLogs(studySubj.getStudySubjectId(), "subject_group_map", studySubj.getOcOid(), null);
			AuditLogsBean subjectLogs = fetchAuditLogs(studySubj.getSubject().getSubjectId(), "subject", studySubj.getOcOid(), null);

			exportSubjectDataBean.getAuditLogs().getAuditLogs().addAll(subjectGroupMapLogs.getAuditLogs());
			exportSubjectDataBean.getAuditLogs().getAuditLogs().addAll(subjectLogs.getAuditLogs());
			Collections.sort(exportSubjectDataBean.getAuditLogs().getAuditLogs());
			if (isCollectDns())
				exportSubjectDataBean.setDiscrepancyNotes(fetchDiscrepancyNotes(studySubj));

			exportSubjectDataBean.setExportStudyEventData(setExportStudyEventDataBean(study, studySubj, studyEvents,formVersionOID,userId,crossForm,tagIds));

			exportSubjectDataBean.setSubjectOID(studySubj.getOcOid());

			exportSubjectDataBean.setEnrollmentDate(studySubj.getEnrollmentDate()!=null?studySubj.getEnrollmentDate().toString():"");

		}
		return exportSubjectDataBean;

	}

	private boolean subjectBelongsToStudy(Study study, StudySubject studySubj) {
		boolean subjectBelongs = false;

		if (studySubj.getStudy().getOc_oid().equals(study.getOc_oid())) {
			subjectBelongs = true;
		} else {

			if (studySubj.getStudy().getStudy().getOc_oid().equals(study.getOc_oid()))
				subjectBelongs = true;

		}

		return subjectBelongs;
	}

	private ArrayList<ExportStudyEventDataBean> setExportStudyEventDataBean(Study study, StudySubject ss, List<StudyEvent> sEvents, String formVersionOID, int userId,boolean crossForm,List<String> tagIds) {
		ArrayList<ExportStudyEventDataBean> al = new ArrayList<ExportStudyEventDataBean>();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		for (StudyEvent se : sEvents) {
			if (se != null) {
				ExportStudyEventDataBean expSEBean = new ExportStudyEventDataBean();
				expSEBean.setStudyEvent(se);
				expSEBean.setLocation(se.getLocation());
				if (se.getDateEnd() != null) {
					if (se.getEndTimeFlag()) {
						expSEBean.setEndDate(se.getDateEnd() + "");
					} else {
						String temp = sdf.format(se.getDateEnd());
						expSEBean.setEndDate(temp);
					}
				}
				if (se.getDateStart() != null) {
					if (se.getStartTimeFlag()) {
						expSEBean.setStartDate(se.getDateStart() + "");
					} else {
						String temp = sdf.format(se.getDateStart());
						expSEBean.setStartDate(temp);
					}
				}
				expSEBean.setEventName(se.getStudyEventDefinition().getName());
				expSEBean.setStudyEventOID(se.getStudyEventDefinition().getOc_oid());

				expSEBean.setStudyEventRepeatKey(se.getSampleOrdinal().toString());
				if (se.getStudySubject().getSubject().getDateOfBirth() != null && se.getDateStart() != null)
					expSEBean.setAgeAtEvent(Utils.getAge(se.getStudySubject().getSubject().getDateOfBirth(), se.getDateStart()));

				expSEBean.setStatus(fetchStudyEventStatus(se.getSubjectEventStatusId()));
				if (collectAudits)
					expSEBean.setAuditLogs(fetchAuditLogs(se.getStudyEventId(), "study_event", se.getStudyEventDefinition().getOc_oid(), null));
				if (collectDns)
					expSEBean.setDiscrepancyNotes(fetchDiscrepancyNotes(se));

				expSEBean.setExportFormData(getFormDataForClinicalStudy(study, ss, se, formVersionOID,userId,crossForm,tagIds));
				expSEBean.setStudyEventDefinition(se.getStudyEventDefinition());
				al.add(expSEBean);
			}
		}

		return al;
	}

	private ArrayList<ExportFormDataBean> getFormDataForClinicalStudy(Study study, StudySubject ss, StudyEvent se, String formVersionOID,int userId,boolean crossForm,List<String> tagIds) {
		List<ExportFormDataBean> formDataBean = new ArrayList<ExportFormDataBean>();
		boolean formCheck = true;
		if (formVersionOID != null)
			formCheck = false;
		boolean hiddenCrfCheckPassed = true;
		List<CrfBean> hiddenCrfs = new ArrayList<CrfBean>();
		UserAccountDAO userAccountDAO = new UserAccountDAO(dataSource);
		for (EventCrf ecrf : se.getEventCrfs()) {
			EventDefinitionCrf eventDefinitionCrf = null;
			List<EventDefinitionCrf> edcs = se.getStudyEventDefinition().getEventDefinitionCrfs();
			hiddenCrfCheckPassed = true;
			int siteId = 0;
			int parentStudyId = 0;
			if (study.getStudy() != null) {
				// it is site subject

				if (isActiveRoleAtSite) {
					siteId = study.getStudyId();
					parentStudyId = study.getStudy().getStudyId();
					hiddenCrfs = listOfHiddenCrfs(siteId, parentStudyId, edcs, ecrf);
				} else {
					parentStudyId = study.getStudy().getStudyId();
					hiddenCrfs = listOfHiddenCrfs(parentStudyId, parentStudyId, edcs, ecrf);
				}

				if (hiddenCrfs.contains(ecrf.getCrfVersion().getCrf())) {
					hiddenCrfCheckPassed = false;
				}

				eventDefinitionCrf = getEventDefinitionCrfDao().findByStudyEventDefinitionIdAndCRFIdAndStudyId(
						se.getStudyEventDefinition().getStudyEventDefinitionId(), ecrf.getFormLayout().getCrf().getCrfId(), study.getStudyId());

				if (eventDefinitionCrf == null) {
					eventDefinitionCrf = getEventDefinitionCrfDao().findByStudyEventDefinitionIdAndCRFIdAndStudyId(
							se.getStudyEventDefinition().getStudyEventDefinitionId(), ecrf.getFormLayout().getCrf().getCrfId(), study.getStudy().getStudyId());
				}

			} else {
				eventDefinitionCrf = getEventDefinitionCrfDao().findByStudyEventDefinitionIdAndCRFIdAndStudyId(
						se.getStudyEventDefinition().getStudyEventDefinitionId(), ecrf.getFormLayout().getCrf().getCrfId(), study.getStudyId());

			}


			//UserAccount userAccount = getUserAccountDao().findByUserId(userId);
			if(crossForm) {
				tagIds = loadPermissionTags();
            }

			List <EventDefinitionCrfPermissionTag> edcPTagIds=
					getEventDefinitionCrfPermissionTagDao().findByEdcIdTagId(
							eventDefinitionCrf.getEventDefinitionCrfId(),eventDefinitionCrf.getParentId()!=null? eventDefinitionCrf.getParentId(): 0, tagIds);
			if(edcPTagIds.size()!=0){
              continue;
			}



			// This logic is to use the same method for both S_OID/SS_OID/*/* and full path
			if (hiddenCrfCheckPassed) {
				if (!formCheck) {
					if (ecrf.getCrfVersion().getOcOid().equals(formVersionOID))
						formCheck = true;
					else
						formCheck = false;
				}
				if (formCheck) {
					ExportFormDataBean dataBean = new ExportFormDataBean();

					dataBean.setEventDefinitionCrf(eventDefinitionCrf);
					dataBean.setEventCrf(ecrf);
					dataBean.setFormLayout(ecrf.getFormLayout());
					dataBean.setFormName(ecrf.getCrfVersion().getCrf().getName());
					dataBean.setItemGroupData(
							fetchItemData(ecrf.getCrfVersion().getItemGroupMetadatas(), ecrf.getEventCrfId(), ecrf.getCrfVersion().getVersioningMaps(), ecrf));
					dataBean.setFormOID(ecrf.getCrfVersion().getCrf().getOcOid());
					if (ecrf.getDateInterviewed() != null)
						dataBean.setInterviewDate(ecrf.getDateInterviewed() + "");
					if (ecrf.getInterviewerName() != null)
						dataBean.setInterviewerName(ecrf.getInterviewerName());
					// dataBean.setStatus(EventCRFStatus.getByCode(Integer.valueOf(ecrf.getStatus().getCode())).getI18nDescription(getLocale()));
					dataBean.setStatus(fetchEventCRFStatus(ecrf));
					dataBean.setCreatedDate(ecrf.getDateCreated());
					dataBean.setCreatedBy(ecrf.getUserAccount().getUserName());
					dataBean.setUpdatedDate(ecrf.getDateUpdated());
					//UserAccount updatedUserAccount = userAccountDao.findById(ecrf.getUpdateId());
                    UserAccountBean updatedUserAccount = null;
                    if (ecrf.getUpdateId() != null) {
                        updatedUserAccount = (UserAccountBean) userAccountDAO.findByPK(ecrf.getUpdateId());
                    }
					if(updatedUserAccount != null && updatedUserAccount.getId() != 0) {
						dataBean.setUpdatedBy(updatedUserAccount.getName());
					}else {
						// or not set?
						dataBean.setUpdatedBy(ecrf.getUserAccount().getUserName());
					}
					
					if (ecrf.getFormLayout().getName() != null)
						dataBean.setFormLayoutName(ecrf.getFormLayout().getName());
					if (collectAudits)
						dataBean.setAuditLogs(fetchAuditLogs(ecrf.getEventCrfId(), "event_crf", ecrf.getCrfVersion().getCrf().getOcOid(), null));
					if (collectDns)
						dataBean.setDiscrepancyNotes(fetchDiscrepancyNotes(ecrf));

					formDataBean.add(dataBean);
					if (formVersionOID != null)
						formCheck = false;
				}
			}
		}

		return (ArrayList<ExportFormDataBean>) formDataBean;
	}

	private List<CrfBean> listOfHiddenCrfs(Integer siteId, Integer parentStudyId, List<EventDefinitionCrf> edcs, EventCrf ecrf) {
		boolean found = false;
		int crfId = ecrf.getCrfVersion().getCrf().getCrfId();
		List<CrfBean> hiddenCrfs = new ArrayList<CrfBean>();
		LOGGER.info("The study subject is at the site/study " + siteId);
		for (EventDefinitionCrf eventDefCrf : edcs) {

			if (eventDefCrf.getCrf().getCrfId() == crfId && eventDefCrf.getStudy().getStudyId() == siteId) {
				found = true;
				if (eventDefCrf.getHideCrf()) {
					hiddenCrfs.add(eventDefCrf.getCrf());
				}
			}
		}

		if (!found) {
			for (EventDefinitionCrf eventDefCrf : edcs) {
				if (eventDefCrf.getCrf().getCrfId() == crfId && eventDefCrf.getStudy().getStudyId() == parentStudyId && eventDefCrf.getHideCrf()) {
					hiddenCrfs.add(eventDefCrf.getCrf());
				}
			}
		}

		return hiddenCrfs;
	}

	// This logic is taken from eventCRFBean.
	private String fetchEventCRFStatus(EventCrf ecrf) {
		String stage = null;
		Status status = Status.getByCode(ecrf.getStatusId());

		if (ecrf.getEventCrfId() <= 0 || status.getCode() <= 0) {
			stage = EventCRFStatus.UNCOMPLETED.getI18nDescription(getLocale());
		}

		if (status.equals(Status.AVAILABLE)) {
			stage = EventCRFStatus.INITIAL_DATA_ENTRY.getI18nDescription(getLocale());
		}

		if (status.equals(Status.PENDING)) {
			if (ecrf.getValidatorId() != 0) {
				stage = EventCRFStatus.DOUBLE_DATA_ENTRY.getI18nDescription(getLocale());
			} else {
				stage = EventCRFStatus.INITIAL_DATA_ENTRY_COMPLETE.getI18nDescription(getLocale());
			}
		}

		if (status.equals(Status.UNAVAILABLE)) {
			stage = EventCRFStatus.DOUBLE_DATA_ENTRY_COMPLETE.getI18nDescription(getLocale());
		}

		if (status.equals(Status.LOCKED)) {
			stage = EventCRFStatus.LOCKED.getI18nDescription(getLocale());
		}

		if (status.equals(Status.DELETED)) {
			stage = EventCRFStatus.INVALID.getI18nDescription(getLocale());

		}

		if (status.equals(Status.AUTO_DELETED)) {
			stage = EventCRFStatus.INVALID.getI18nDescription(getLocale());
		}

		if (status.equals(Status.RESET)) {
			stage = EventCRFStatus.INVALID.getI18nDescription(getLocale());
		}

		return stage;

	}

	private ArrayList<ImportItemGroupDataBean> fetchItemData(Set<ItemGroupMetadata> set, int eventCrfId, List<VersioningMap> vms, EventCrf eventCrf) {
		String groupOID, itemOID;
		String itemValue = null;
		String itemDataValue;
		HashMap<String, ArrayList<String>> oidMap = new HashMap<String, ArrayList<String>>();
		HashMap<String, List<ItemData>> oidDNAuditMap = new HashMap<String, List<ItemData>>();
		List<ItemData> itds = eventCrf.getItemDatas();

		// For each metadata get the group, and then get list of all items in
		// that group.so we can a data structure of groupOID and list of
		// itemOIDs with corresponding values will be created.
		for (ItemData itemData : itds) {
			List<ItemGroupMetadata> igmetadatas = itemData.getItem().getItemGroupMetadatas();
			for (ItemGroupMetadata igGrpMetadata : igmetadatas) {
				groupOID = igGrpMetadata.getItemGroup().getOcOid();

				if (!oidMap.containsKey(groupOID)) {
					String groupOIDOrdnl = groupOID;
					ArrayList<String> itemsValues = new ArrayList<String>();
					ArrayList<ItemData> itemDatas = new ArrayList<ItemData>();
					List<ItemGroupMetadata> allItemsInAGroup = igGrpMetadata.getItemGroup().getItemGroupMetadatas();

					for (ItemGroupMetadata itemGrpMetada : allItemsInAGroup) {
						itemOID = itemGrpMetada.getItem().getOcOid();
						itemsValues = new ArrayList<String>();
						/*
						 * List<ItemData> itds = itemGrpMetada.getItem()
						 * .getItemDatas();
						 */

						// look for the key
						// of same group and ordinal and add this item to
						// that hashmap

						itemsValues = new ArrayList<String>();
						itemDataValue = fetchItemDataValue(itemData, itemData.getItem());
						itemDatas = new ArrayList<ItemData>();
						itemValue = itemOID + DELIMITER + itemDataValue;
						itemsValues.add(itemValue);
						groupOIDOrdnl = groupOID + GROUPOID_ORDINAL_DELIM + itemData.getOrdinal() + GROUPOID_ORDINAL_DELIM + itemData.isDeleted();

						if (itemData.getItem().getOcOid() == itemOID) {

							if (oidMap.containsKey(groupOIDOrdnl)) {

								ArrayList<String> itemgrps = oidMap.get(groupOIDOrdnl);
								List<ItemData> itemDataTemps = oidDNAuditMap.get(groupOIDOrdnl);
								if (!itemgrps.contains(itemValue)) {
									itemgrps.add(itemValue);
									oidMap.remove(groupOIDOrdnl);
									itemDataTemps.add(itemData);
									oidDNAuditMap.remove(groupOIDOrdnl);
								}
								oidMap.put(groupOIDOrdnl, itemgrps);
								oidDNAuditMap.put(groupOIDOrdnl, itemDataTemps);

							} else {
								oidMap.put(groupOIDOrdnl, itemsValues);
								itemDatas.add(itemData);
								oidDNAuditMap.put(groupOIDOrdnl, itemDatas);
							}

						}
					}

				}

			}
		}

		return populateImportItemGrpBean(oidMap, oidDNAuditMap);
	}

	private String fetchItemDataValue(ItemData itemData, Item item) {
		String idValue = itemData.getValue();
		return idValue;

	}

	private ArrayList<ImportItemGroupDataBean> populateImportItemGrpBean(HashMap<String, ArrayList<String>> oidMap,
			HashMap<String, List<ItemData>> oidDNAuditMap) {
		Set<String> keysGrpOIDs = oidMap.keySet();
		ArrayList<ImportItemGroupDataBean> iigDataBean = new ArrayList<ImportItemGroupDataBean>();
		ImportItemGroupDataBean importItemGrpDataBean = new ImportItemGroupDataBean();
		for (String grpOID : keysGrpOIDs) {
			LOGGER.debug("*********************Processing grpOID:" + grpOID);
			ArrayList<String> vals = oidMap.get(grpOID);
			importItemGrpDataBean = new ImportItemGroupDataBean();
			int firstIndexOf = StringUtils.ordinalIndexOf(grpOID, GROUPOID_ORDINAL_DELIM, 1);
			int secondIndexOf = StringUtils.ordinalIndexOf(grpOID, GROUPOID_ORDINAL_DELIM, 2);

			// int groupIdx = grpOID.indexOf(GROUPOID_ORDINAL_DELIM);
			if (firstIndexOf != -1) {
				String itemGroupOid = grpOID.substring(0, firstIndexOf);
				importItemGrpDataBean.setItemGroupOID(itemGroupOid);
				importItemGrpDataBean.setItemGroupRepeatKey(grpOID.substring(firstIndexOf + 1, secondIndexOf));
				ItemGroup itemGroup = itemGroupDao.findByOcOID(itemGroupOid);
				importItemGrpDataBean.setItemGroupName(itemGroup.getName());
				boolean isDeleted = Boolean.parseBoolean(grpOID.substring(secondIndexOf + 1));
				ArrayList<ImportItemDataBean> iiDList = new ArrayList<ImportItemDataBean>();

				for (String value : vals) {
					ImportItemDataBean iiDataBean = new ImportItemDataBean();
					int index = value.indexOf(DELIMITER);
					if (!value.trim().equalsIgnoreCase(DELIMITER)) {
						String itemOid = value.substring(0, index);
						LOGGER.debug("*******************itemOid:" + itemOid);
						iiDataBean.setItemOID(itemOid);
						iiDataBean.setValue(value.substring(index + 1, value.length()));
						iiDataBean.setDeleted(isDeleted);
						Item item = itemDao.findByOcOID(itemOid);
						LOGGER.debug("*****************item:" + item);
						LOGGER.debug("****************user:" + getCurrentUser());
						LOGGER.debug("schema:" + CoreResources.getRequestSchema());
						try {
                            iiDataBean.setItemName(item.getName());
                        } catch (NullPointerException npe) {
							CoreResources.getRequestSchema();
							Item item1 = itemDao.findByOcOID(itemOid);
							HttpServletRequest request = getRequest();
                            throw npe;
                        }
						if (isCollectAudits() || isCollectDns()) {
							iiDataBean = fetchItemDataAuditValue(oidDNAuditMap.get(grpOID), iiDataBean);
						}
						// if(isCollectDns())
						// iiDataBean.setDiscrepancyNotes(fetchDiscrepancyNotes(oidDNAuditMap.get(grpOID)));
						iiDList.add(iiDataBean);

					}
				}
				importItemGrpDataBean.setItemData(iiDList);
				iigDataBean.add(importItemGrpDataBean);
			}
		}

		return iigDataBean;
	}
    private HttpServletRequest getRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null && requestAttributes.getRequest() != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            return request;
        }
        return null;
    }
	private String getCurrentUser() {
		ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = requestAttributes.getRequest();
		UserAccountBean userAccountBean = (UserAccountBean) request.getSession().getAttribute("userBean");
 		if (userAccountBean != null) {
				return userAccountBean.getName();
			}else {
				  return null;
			  }
	}



	private ImportItemDataBean fetchItemDataAuditValue(List<ItemData> list, ImportItemDataBean iiDataBean) {
		for (ItemData id : list) {
			if (id.getItem().getOcOid().equals(iiDataBean.getItemOID())) {
				if (isCollectAudits())
					iiDataBean.setAuditLogs(fetchAuditLogs(id.getItemDataId(), "item_data", iiDataBean.getItemOID(), null));
				if (isCollectDns())
					iiDataBean.setDiscrepancyNotes(fetchDiscrepancyNotes(id));
				return iiDataBean;
			}
		}

		return iiDataBean;
	}

	private DiscrepancyNotesBean fetchDiscrepancyNotes(ItemData itemData) {
		List<DnItemDataMap> dnItemDataMaps = itemData.getDnItemDataMaps();
		DiscrepancyNotesBean dnNotesBean = new DiscrepancyNotesBean();
		dnNotesBean.setEntityID(itemData.getItem().getOcOid());
		if (isCollectDns()) {
			DiscrepancyNoteBean dnNoteBean = new DiscrepancyNoteBean();

			ArrayList<DiscrepancyNoteBean> dnNotes = new ArrayList<DiscrepancyNoteBean>();
			boolean addDN = true;
			for (DnItemDataMap dnItemDataMap : dnItemDataMaps) {
				DiscrepancyNote dn = dnItemDataMap.getDiscrepancyNote();
				addDN = true;
				fillDNObject(dnNoteBean, dnNotes, addDN, dn, null);
			}
			dnNotesBean.setDiscrepancyNotes(dnNotes);
		}
		return dnNotesBean;

	}

	private DiscrepancyNotesBean fetchDiscrepancyNotes(EventCrf eventCrf) {
		LOGGER.debug("Fetching the discrepancy notes..");
		List<DnEventCrfMap> dnEventCrfMaps = eventCrf.getDnEventCrfMaps();
		DiscrepancyNotesBean dnNotesBean = new DiscrepancyNotesBean();
		dnNotesBean.setEntityID(eventCrf.getCrfVersion().getCrf().getOcOid());
		DiscrepancyNoteBean dnNoteBean = new DiscrepancyNoteBean();
		ArrayList<DiscrepancyNoteBean> dnNotes = new ArrayList<DiscrepancyNoteBean>();
		boolean addDN = true;
		for (DnEventCrfMap dnItemDataMap : dnEventCrfMaps) {
			DiscrepancyNote dn = dnItemDataMap.getDiscrepancyNote();
			addDN = true;
			fillDNObject(dnNoteBean, dnNotes, addDN, dn, dnItemDataMap.getDnEventCrfMapId().getColumnName());
		}
		dnNotesBean.setDiscrepancyNotes(dnNotes);
		return dnNotesBean;

	}

	private DiscrepancyNotesBean fetchDiscrepancyNotes(StudySubject studySubj) {
		List<DnStudySubjectMap> dnMaps = studySubj.getDnStudySubjectMaps();

		DiscrepancyNotesBean dnNotesBean = new DiscrepancyNotesBean();
		dnNotesBean.setEntityID(studySubj.getOcOid());

		DiscrepancyNoteBean dnNoteBean = new DiscrepancyNoteBean();
		DiscrepancyNoteBean dnSubjBean = new DiscrepancyNoteBean();
		ArrayList<DiscrepancyNoteBean> dnNotes = new ArrayList<DiscrepancyNoteBean>();
		boolean addDN = true;
		for (DnStudySubjectMap dnMap : dnMaps) {
			DiscrepancyNote dn = dnMap.getDiscrepancyNote();
			addDN = true;
			fillDNObject(dnNoteBean, dnNotes, addDN, dn, dnMap.getDnStudySubjectMapId().getColumnName());
		}
		dnNotesBean.setDiscrepancyNotes(dnNotes);
		List<DnSubjectMap> dnSubjMaps = studySubj.getSubject().getDnSubjectMaps();
		ArrayList<DiscrepancyNoteBean> dnSubjs = new ArrayList<DiscrepancyNoteBean>();

		for (DnSubjectMap dnMap : dnSubjMaps) {
			DiscrepancyNote dn = dnMap.getDiscrepancyNote();
			addDN = true;
			fillDNObject(dnSubjBean, dnSubjs, addDN, dn, dnMap.getDnSubjectMapId().getColumnName());
		}

		for (DiscrepancyNoteBean dnSubjMap : dnSubjs)
			dnNotesBean.getDiscrepancyNotes().add(dnSubjMap);
		return dnNotesBean;

	}

	private DiscrepancyNotesBean fetchDiscrepancyNotes(StudyEvent studyEvent) {
		List<DnStudyEventMap> dnMaps = studyEvent.getDnStudyEventMaps();
		DiscrepancyNotesBean dnNotesBean = new DiscrepancyNotesBean();
		dnNotesBean.setEntityID(studyEvent.getStudyEventDefinition().getOc_oid());
		DiscrepancyNoteBean dnNoteBean = new DiscrepancyNoteBean();
		ArrayList<DiscrepancyNoteBean> dnNotes = new ArrayList<DiscrepancyNoteBean>();
		boolean addDN = true;
		for (DnStudyEventMap dnMap : dnMaps) {
			DiscrepancyNote dn = dnMap.getDiscrepancyNote();
			addDN = true;
			fillDNObject(dnNoteBean, dnNotes, addDN, dn, dnMap.getDnStudyEventMapId().getColumnName());
		}
		dnNotesBean.setDiscrepancyNotes(dnNotes);
		return dnNotesBean;

	}

	private void fillDNObject(DiscrepancyNoteBean dnNoteBean, ArrayList<DiscrepancyNoteBean> dnNotes, boolean addDN, DiscrepancyNote dn, String columnName) {

		if (dn.getParentDiscrepancyNote() != null) {

		} else {
			dnNoteBean = new DiscrepancyNoteBean();
			dnNoteBean.setStatus(dn.getResolutionStatus().getName());
			dnNoteBean.setNoteType(dn.getEntityType());
			dnNoteBean.setOid("DN_" + dn.getDiscrepancyNoteId());
			dnNoteBean.setNoteType(dn.getDiscrepancyNoteType().getName());

			dnNoteBean.setDateUpdated(dn.getDateCreated());
			dnNoteBean.setEntityName(columnName);

			for (DiscrepancyNote childDN : dn.getChildDiscrepancyNotes()) {
				ChildNoteBean childNoteBean = new ChildNoteBean();
				childNoteBean.setOid("CDN_" + childDN.getDiscrepancyNoteId());
				ElementRefBean userRef = new ElementRefBean();
				childNoteBean.setDescription(childDN.getDescription());
				childNoteBean.setStatus(childDN.getResolutionStatus().getName());

				childNoteBean.setDetailedNote(childDN.getDetailedNotes());

				childNoteBean.setDateCreated(childDN.getDateCreated());
				if (childDN.getUserAccountByOwnerId() != null) {
					childNoteBean.setOwnerUserName(childDN.getUserAccountByOwnerId().getUserName());
					childNoteBean.setOwnerFirstName(childDN.getUserAccountByOwnerId().getFirstName());
					childNoteBean.setOwnerLastName(childDN.getUserAccountByOwnerId().getLastName());
				}

				if (childDN.getUserAccount() != null) {
					userRef.setElementDefOID("USR_" + childDN.getUserAccount().getUserId());
					userRef.setUserName(childDN.getUserAccount().getUserName());
					userRef.setFullName(childDN.getUserAccount().getFirstName() + " " + childDN.getUserAccount().getLastName());
				} else {
					userRef.setElementDefOID("");
					userRef.setUserName("");
					userRef.setFullName("");
				}
				childNoteBean.setUserRef(userRef);
				dnNoteBean.getChildNotes().add(childNoteBean);
			}
			dnNoteBean.setNumberOfChildNotes(dnNoteBean.getChildNotes().size());

			if (!dnNotes.contains(dnNoteBean)) {
				dnNotes.add(dnNoteBean);
			}
		}

	}

	private AuditLogsBean fetchAuditLogs(int entityID, String itemDataAuditTable, String entityValue, String anotherAuditLog) {

		AuditLogsBean auditLogsBean = new AuditLogsBean();

		if (isCollectAudits()) {
			AuditLogEvent auditLog = new AuditLogEvent();
			auditLog.setEntityId(new Integer(entityID));
			auditLog.setAuditTable(itemDataAuditTable);
			auditLogsBean.setEntityID(entityValue);
			ArrayList<AuditLogEvent> auditLogEvent = (getAuditEventDAO().findByParam(auditLog, anotherAuditLog));

			auditLogsBean = fetchODMAuditBean(auditLogEvent, auditLogsBean);
		}
		return auditLogsBean;
	}

	private AuditLogsBean fetchODMAuditBean(ArrayList<AuditLogEvent> auditLogEvents, AuditLogsBean auditLogsBean) {

		for (AuditLogEvent auditLogEvent : auditLogEvents) {
			AuditLogBean auditBean = new AuditLogBean();
			auditBean.setOid("AL_" + auditLogEvent.getAuditId());
			auditBean.setDatetimeStamp(auditLogEvent.getAuditDate());
			auditBean.setDetails(auditLogEvent.getDetails());
			if (auditLogEvent.getEntityName() != null && auditLogEvent.getEntityName().equals(STATUS)) {
				/*
				 * if(auditLogEvent.getAuditTable().equals(EVENT_CRF)){
				 * auditBean.setNewValue(EventCRFStatus.getByCode(Integer.valueOf(auditLogEvent.getNewValue())).
				 * getDescription());
				 * auditBean.setOldValue(EventCRFStatus.getByCode(Integer.valueOf(auditLogEvent.getOldValue())).
				 * getDescription());
				 * }
				 * else
				 */
				if (auditLogEvent.getAuditTable().equals(STUDY_EVENT)) {
					auditBean.setNewValue(fetchStudyEventStatus(Integer.valueOf(auditLogEvent.getNewValue())));
					auditBean.setOldValue(fetchStudyEventStatus(Integer.valueOf(auditLogEvent.getOldValue())));
				} else if (auditLogEvent.getAuditTable().equals(SUBJECT_GROUP_MAP)) {
					auditBean.setNewValue(auditLogEvent.getNewValue());
					auditBean.setOldValue(auditLogEvent.getOldValue());
				} else {
					auditBean.setNewValue(Status.getByCode(Integer.valueOf(auditLogEvent.getNewValue())).getI18nDescription(getLocale()));
					auditBean.setOldValue(Status.getByCode(Integer.valueOf(auditLogEvent.getOldValue())).getI18nDescription(getLocale()));
				}

			}

			else {
				auditBean.setNewValue(auditLogEvent.getNewValue() == null ? "" : auditLogEvent.getNewValue());
				auditBean.setOldValue(auditLogEvent.getOldValue() == null ? "" : auditLogEvent.getOldValue());
			}

			auditBean.setReasonForChange(auditLogEvent.getReasonForChange() == null ? "" : auditLogEvent.getReasonForChange());

			String auditEventTypeName = auditLogEvent.getAuditLogEventType().getName();
			auditEventTypeName = auditEventTypeName.replace(' ', '_');
			auditEventTypeName = auditEventTypeName.substring(0, 1).toLowerCase() + auditEventTypeName.substring(1);
			auditLogEvent.getAuditLogEventType().setName(auditEventTypeName);

			auditBean.setType(auditLogEvent.getAuditLogEventType().getI18nName(locale));

			auditBean.setValueType(auditLogEvent.getEntityName() == null ? "" : auditLogEvent.getEntityName());
			auditBean.setAuditLogEventTypeId(auditLogEvent.getAuditLogEventType().getAuditLogEventTypeId());

			if (auditLogEvent.getUserAccount() != null && auditLogEvent.getUserAccount().getUserId() != 0) {
				auditBean.setUserId("USR_" + auditLogEvent.getUserAccount().getUserId());
				auditBean.setUserName(auditLogEvent.getUserAccount().getUserName());
				auditBean.setName(auditLogEvent.getUserAccount().getFirstName() + " " + auditLogEvent.getUserAccount().getLastName());
			} else {
				auditBean.setUserId("");
				auditBean.setUserName("");
				auditBean.setName("");
			}
			auditLogsBean.getAuditLogs().add(auditBean);

		}
		return auditLogsBean;
	}

	private String fetchStudyEventStatus(Integer valueOf) {
		return SubjectEventStatus.getByCode(valueOf).getI18nDescription(getLocale());

	}

	/**
	 * This is a generic method where the control enters first. Regardless what URL is being used. Depending upon the
	 * combination of URL parameters, further course is determined.
	 */
	@Override
	public LinkedHashMap<String, OdmClinicalDataBean> getClinicalData(String studyOID, String studySubjectOID, String studyEventOID, String formVersionOID,
			Boolean collectDNs, Boolean collectAudit, Locale locale, int userId , boolean crossForm) {
		setLocale(locale);
		setCollectDns(collectDNs);
		setCollectAudits(collectAudit);
		LinkedHashMap<String, OdmClinicalDataBean> clinicalDataHash = new LinkedHashMap<String, OdmClinicalDataBean>();
		UserAccount userAccount = getUserAccountDao().findByColumnName(userId, "userId");
		LOGGER.debug("*********userAccount:userId:" + userId + " name:" + userAccount.getUserName());
		LOGGER.debug("Entering the URL with " + studyOID + ":" + studySubjectOID + ":" + studyEventOID + ":" + formVersionOID + ":DNS:" + collectDNs
				+ ":Audits:" + collectAudit);
		LOGGER.debug("Determining the generic paramters...");
		int parentStudyId = 0;
		Study publicStudy = getStudyDao().findPublicStudy(studyOID);

		if (publicStudy.getStudy() != null) {
			isActiveRoleAtSite = true;
			parentStudyId = publicStudy.getStudy().getStudyId();
		} else {
			parentStudyId = publicStudy.getStudyId();
			isActiveRoleAtSite = false;
		}

		ArrayList<StudyUserRole> surlist = getStudyUserRoleDao().findAllUserRolesByUserAccountStudySites(userAccount, publicStudy.getStudyId(), parentStudyId);
		if (surlist == null || surlist.size() == 0) {
			// Does not have permission to view study or site info / return null
			return null;
		}

		// This piece of code identifies if the study subject is assigned to study level or site level. If the study
		// subject assigned to site is pulled from study level this will get the site OID correctly displayed.
		if (!studySubjectOID.equals(INDICATE_ALL)) {

			// StudySubjectDao ssdao = getStudySubjectDao();
			// StudySubject ss = (StudySubject) getStudySubjectDao().findByColumnName(studySubjectOID, "ocOid");
			// studyOID = ss.getStudy().getOc_oid();
		}
		if (studyEventOID.equals(INDICATE_ALL) && formVersionOID.equals(INDICATE_ALL) && !studySubjectOID.equals(INDICATE_ALL)
				&& !studyOID.equals(INDICATE_ALL)) {
			LOGGER.debug("Adding all the study events,formevents as it is a *");
			LOGGER.debug("study subject is not all and so is study");
            //  studyEventOid=* ,fromVersionOid=*  (Single Subject , All Events , All FormVersions)
			clinicalDataHash.put(studyOID, getClinicalData(studyOID, studySubjectOID,userId,crossForm));

			return clinicalDataHash;
		} else if (studyEventOID.equals(INDICATE_ALL) && formVersionOID.equals(INDICATE_ALL) && studySubjectOID.equals(INDICATE_ALL)
				&& !studyOID.equals(INDICATE_ALL)) {
			LOGGER.info("At the study level.. study event,study subject and forms are *");
			//  studySubjectOid=* ,studyEventOid=* , fromVersionOid=* (All Subjects , All Events , All FormVersions)
			return getClinicalData(studyOID,userId,crossForm);
		} else if (!studyEventOID.equals(INDICATE_ALL) && !studySubjectOID.equals(INDICATE_ALL) && !studyOID.equals(INDICATE_ALL)
				&& formVersionOID.equals(INDICATE_ALL)) {
			LOGGER.info("Obtaining the form version specific");
			// fromVersionOid=*  (Single Subject , Single Event , All FormVersions)
			clinicalDataHash.put(studyOID, getClinicalDatas(studyOID, studySubjectOID, studyEventOID, null,userId,crossForm));
			return clinicalDataHash;
		}

		else if (!studyEventOID.equals(INDICATE_ALL) && !studySubjectOID.equals(INDICATE_ALL) && !studyOID.equals(INDICATE_ALL)
				&& !formVersionOID.equals(INDICATE_ALL)) {
			// none =* (Single Subject , Single Event , Single FormVersion)
			clinicalDataHash.put(studyOID, getClinicalDatas(studyOID, studySubjectOID, studyEventOID, formVersionOID,userId,crossForm));
			return clinicalDataHash;
		}

		return null;
	}

	private void setLocale(Locale locale) {
		this.locale = locale;
	}

	private Locale getLocale() {
		return locale;
	}

	private OdmClinicalDataBean getClinicalDatas(String studyOID, String studySubjectOID, String studyEventOID, String formVersionOID,int userId,boolean crossForm) {
		int seOrdinal = 0;
		String temp = studyEventOID;
		List<StudyEvent> studyEvents = new ArrayList<StudyEvent>();
		StudyEventDefinition studyEventDefinition = null;
		Study study = getStudyDao().findByColumnName(studyOID, "oc_oid");
		List<StudySubject> ss = listStudySubjects(studySubjectOID);
		int idx = studyEventOID.indexOf(OPEN_ORDINAL_DELIMITER);
		LOGGER.info("study event oridinal is.." + idx);
		if (idx > 0) {
			studyEventOID = studyEventOID.substring(0, idx);
			seOrdinal = new Integer(temp.substring(idx + 1, temp.indexOf(CLOSE_ORDINAL_DELIMITER))).intValue();
		}
		studyEventDefinition = getStudyEventDefDao().findByColumnName(studyEventOID, "oc_oid");
		LOGGER.info("study event ordinal.." + seOrdinal);
		if (seOrdinal > 0) {
			studyEvents = studyEventDao.fetchStudyEvents(seOrdinal, studyEventDefinition.getOc_oid(), studySubjectOID);
		}
		else {
			studyEvents = studyEventDao.fetchStudyEvents(studyEventDefinition.getOc_oid(), studySubjectOID);
		}


		return constructClinicalDataStudy(ss, study, studyEvents, formVersionOID,userId,crossForm);
	}

	public UserAccountDao getUserAccountDao() {
		return userAccountDao;
	}

	public void setUserAccountDao(UserAccountDao userAccountDao) {
		this.userAccountDao = userAccountDao;
	}

	public StudyUserRoleDao getStudyUserRoleDao() {
		return studyUserRoleDao;
	}

	public void setStudyUserRoleDao(StudyUserRoleDao studyUserRoleDao) {
		this.studyUserRoleDao = studyUserRoleDao;
	}

	public ItemDao getItemDao() {
		return itemDao;
	}

	public void setItemDao(ItemDao itemDao) {
		this.itemDao = itemDao;
	}

	public ItemGroupDao getItemGroupDao() {
		return itemGroupDao;
	}

	public void setItemGroupDao(ItemGroupDao itemGroupDao) {
		this.itemGroupDao = itemGroupDao;
	}

	public EventDefinitionCrfDao getEventDefinitionCrfDao() {
		return eventDefinitionCrfDao;
	}

	public void setEventDefinitionCrfDao(EventDefinitionCrfDao eventDefinitionCrfDao) {
		this.eventDefinitionCrfDao = eventDefinitionCrfDao;
	}

	public EventDefinitionCrfPermissionTagDao getEventDefinitionCrfPermissionTagDao() {
		return eventDefinitionCrfPermissionTagDao;
	}

	public void setEventDefinitionCrfPermissionTagDao(EventDefinitionCrfPermissionTagDao eventDefinitionCrfPermissionTagDao) {
		this.eventDefinitionCrfPermissionTagDao = eventDefinitionCrfPermissionTagDao;
	}
	private List <String> loadPermissionTags(){
		List<EventDefinitionCrfPermissionTag> tags = getEventDefinitionCrfPermissionTagDao().findAll();

		List<String> tagsList = new ArrayList<>();
		for (EventDefinitionCrfPermissionTag tag : tags) {
			if(!tagsList.contains(tag.getPermissionTagId())) {
				tagsList.add(tag.getPermissionTagId());
			}
		}
		return tagsList;
	}
}
