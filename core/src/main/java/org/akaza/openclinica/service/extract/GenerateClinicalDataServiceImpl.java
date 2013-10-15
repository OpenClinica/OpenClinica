package org.akaza.openclinica.service.extract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.akaza.openclinica.bean.odmbeans.AuditLogBean;
import org.akaza.openclinica.bean.odmbeans.AuditLogsBean;
import org.akaza.openclinica.bean.odmbeans.ChildNoteBean;
import org.akaza.openclinica.bean.odmbeans.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.odmbeans.DiscrepancyNotesBean;
import org.akaza.openclinica.bean.odmbeans.ElementRefBean;
import org.akaza.openclinica.bean.odmbeans.OdmClinicalDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ExportFormDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ExportStudyEventDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ExportSubjectDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ImportItemDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ImportItemGroupDataBean;
import org.akaza.openclinica.dao.hibernate.AuditLogEventDao;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDefinitionDao;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.AuditLogEvent;
import org.akaza.openclinica.domain.datamap.DiscrepancyNote;
import org.akaza.openclinica.domain.datamap.DnEventCrfMap;
import org.akaza.openclinica.domain.datamap.DnItemDataMap;
import org.akaza.openclinica.domain.datamap.DnStudyEventMap;
import org.akaza.openclinica.domain.datamap.DnStudySubjectMap;
import org.akaza.openclinica.domain.datamap.EventCrf;
import org.akaza.openclinica.domain.datamap.Item;
import org.akaza.openclinica.domain.datamap.ItemData;
import org.akaza.openclinica.domain.datamap.ItemGroupMetadata;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.akaza.openclinica.domain.datamap.VersioningMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate CDISC-ODM clinical data without data set.
 * 
 * @author jnyayapathi
 * 
 */

public class GenerateClinicalDataServiceImpl implements GenerateClinicalDataService {
	protected final static Logger LOGGER = LoggerFactory
			.getLogger("org.akaza.openclinica.service.extract.GenerateClinicalDataServiceImpl");
	protected final static String DELIMITER = ",";
	private final static String GROUPOID_ORDINAL_DELIM = ":";
	private final static String INDICATE_ALL="*";
	private final static String OPEN_ORDINAL_DELIMITER = "[";
	private final static String CLOSE_ORDINAL_DELIMITER = "]";
	private static final String ITEM_DATA_AUDIT_TABLE = null;
	private static final Object STATUS = "Status";
	private StudyDao studyDao;

	private StudySubjectDao studySubjectDao;
	private StudyEventDefinitionDao studyEventDefDao;

	private boolean collectDns=true;
	private boolean collectAudits=true;
	private AuditLogEventDao auditEventDAO;
	
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

	public OdmClinicalDataBean getClinicalData(String studyOID) {
		Study study = new Study();
		study.setOc_oid(studyOID);
		study = getStudyDao().findByColumnName(studyOID, "oc_oid");
		
		List<StudySubject>studySubjs = study.getStudySubjects();
		return  constructClinicalData(study, studySubjs);
	}
	private List<StudySubject> listStudySubjects(String studySubjectOID){
		ArrayList<StudySubject>studySubjs = new ArrayList<StudySubject>();
		StudySubject studySubj = getStudySubjectDao().findByColumnName(
				studySubjectOID, "ocOid");
		
		studySubjs.add(studySubj);
		return studySubjs;
	}

	public OdmClinicalDataBean getClinicalData(String studyOID, String studySubjectOID) {
		Study study = getStudyDao().findByColumnName(studyOID, "oc_oid");
		

		return constructClinicalData(study,listStudySubjects(studySubjectOID));
	}

	public StudyDao getStudyDao() {
		return studyDao;
	}

	public void setStudyDao(StudyDao studyDao) {
		this.studyDao = studyDao;
	}

	private OdmClinicalDataBean constructClinicalData(Study study, List<StudySubject> studySubjs) {

		
		return constructClinicalDataStudy(studySubjs, study,null, null);
	}

	private OdmClinicalDataBean constructClinicalDataStudy(List<StudySubject> studySubjs, Study study,List<StudyEvent>studyEvents,String formVersionOID) {
		OdmClinicalDataBean odmClinicalDataBean = new OdmClinicalDataBean();
		ExportSubjectDataBean expSubjectBean;
		List<ExportSubjectDataBean> exportSubjDataBeanList = new ArrayList<ExportSubjectDataBean>();
		for(StudySubject studySubj:studySubjs)
		{
		if(studyEvents==null)
			expSubjectBean = setExportSubjectDataBean(studySubj, study,studySubj.getStudyEvents(),formVersionOID);
		else
			 expSubjectBean = setExportSubjectDataBean(studySubj, study,studyEvents,formVersionOID);
		exportSubjDataBeanList.add(expSubjectBean);
		
		odmClinicalDataBean.setExportSubjectData(exportSubjDataBeanList);
		odmClinicalDataBean.setStudyOID(study.getOc_oid());
		}
		
		return odmClinicalDataBean;
		// return null;
	}



	private ExportSubjectDataBean setExportSubjectDataBean(
			StudySubject studySubj, Study study,List<StudyEvent> studyEvents,String formVersionOID) {

		ExportSubjectDataBean exportSubjectDataBean = new ExportSubjectDataBean();
		
		if(subjectBelongsToStudy(study,studySubj)){
		
		// exportSubjectDataBean.setAuditLogs(studySubj.getA)
		exportSubjectDataBean.setDateOfBirth(studySubj.getSubject()
				.getDateOfBirth() + "");
		exportSubjectDataBean.setSubjectGender(studySubj.getSubject().getGender()+"");
		exportSubjectDataBean.setStudySubjectId(
				studySubj.getLabel());
		if(studySubj.getSubject().getUniqueIdentifier()!=null)exportSubjectDataBean.setUniqueIdentifier(studySubj.getSubject().getUniqueIdentifier());
		exportSubjectDataBean.setSecondaryId(studySubj.getSecondaryLabel());
		exportSubjectDataBean.setStatus(studySubj.getStatus().toString());
		if(isCollectAudits())
		exportSubjectDataBean.setAuditLogs(fetchAuditLogs(studySubj.getStudySubjectId(),"study_subject", studySubj.getOcOid()));
		if(isCollectDns())
			exportSubjectDataBean.setDiscrepancyNotes(fetchDiscrepancyNotes(studySubj));
		
		exportSubjectDataBean
				.setExportStudyEventData(setExportStudyEventDataBean(studySubj,studyEvents,formVersionOID));

		exportSubjectDataBean.setSubjectOID(studySubj.getOcOid());
		}
		return exportSubjectDataBean;

	}

	private boolean subjectBelongsToStudy(Study study, StudySubject studySubj) {
		boolean subjectBelongs = false;
		
		if(studySubj.getStudy().getOc_oid().equals(study.getOc_oid())){
			subjectBelongs = true;
		}
		else{
			
				if(studySubj.getStudy().getStudy().getOc_oid().equals(study.getOc_oid()))
					subjectBelongs=true;
			
		}
		
		
		return subjectBelongs;
	}

	private ArrayList<ExportStudyEventDataBean> setExportStudyEventDataBean(
			StudySubject ss,List<StudyEvent>studyEvents,String formVersionOID) {
		ArrayList<ExportStudyEventDataBean> al = new ArrayList<ExportStudyEventDataBean>();

		for (StudyEvent se : studyEvents) {
			ExportStudyEventDataBean expSEBean = new ExportStudyEventDataBean();
			expSEBean.setLocation(se.getLocation());
			expSEBean.setEndDate(se.getDateEnd() + "");
			expSEBean.setStartDate(se.getDateStart() + "");
			expSEBean.setStudyEventOID(se.getStudyEventDefinition().getOc_oid());
			expSEBean.setStudyEventRepeatKey(se.getSampleOrdinal().toString());
			if(collectAudits)
			expSEBean.setAuditLogs(fetchAuditLogs(se.getStudyEventId(),"study_event",se.getStudyEventDefinition().getOc_oid()));
			if(collectDns)
				expSEBean.setDiscrepancyNotes(fetchDiscrepancyNotes(se));
			
			expSEBean.setExportFormData(getFormDataForClinicalStudy(se,formVersionOID));

			al.add(expSEBean);
		}

		return al;
	}

	private ArrayList<ExportFormDataBean> getFormDataForClinicalStudy(
			StudyEvent se,String formVersionOID) {
		List<ExportFormDataBean> formDataBean = new ArrayList<ExportFormDataBean>();
		boolean formCheck = true;
		if(formVersionOID!=null)formCheck = false;
		for (EventCrf ecrf : se.getEventCrfs()) {
			
			if(!formCheck)
				{	if(ecrf.getCrfVersion().getOcOid().equals(formVersionOID))
						formCheck=true;
					else
						formCheck=false;
				}
				if(formCheck){
				ExportFormDataBean dataBean = new ExportFormDataBean();
				dataBean.setItemGroupData(fetchItemData(ecrf.getCrfVersion()
						.getItemGroupMetadatas(), ecrf.getEventCrfId(), ecrf
						.getCrfVersion().getVersioningMaps()));
				dataBean.setFormOID(ecrf.getCrfVersion().getOcOid());
				dataBean.setInterviewDate(ecrf.getDateInterviewed() + "");
				dataBean.setInterviewerName(ecrf.getInterviewerName());
				dataBean.setStatus(ecrf.getStatus() + "");
				if(collectAudits)
				dataBean.setAuditLogs(fetchAuditLogs(ecrf.getEventCrfId(),"event_crf", formVersionOID));
				if(collectDns)
					dataBean.setDiscrepancyNotes(fetchDiscrepancyNotes(ecrf));
				
				formDataBean.add(dataBean);
				if(formVersionOID!=null)formCheck=false;
				}
			}

		return (ArrayList<ExportFormDataBean>) formDataBean;
	}

	private ArrayList<ImportItemGroupDataBean> fetchItemData(
			Set<ItemGroupMetadata> set, int eventCrfId, List<VersioningMap> vms) {
		String groupOID, itemOID;
		String itemValue = null;
		String itemDataValue;
		HashMap<String, ArrayList<String>> oidMap = new HashMap<String, ArrayList<String>>();
		HashMap<String, List<ItemData>> oidDNAuditMap = new HashMap<String, List<ItemData>>();
		
		// For each metadata get the group, and then get list of all items in
		// that group.so we can a data structure of groupOID and list of
		// itemOIDs with corresponding values will be created.
		for (ItemGroupMetadata igGrpMetadata : set) {
			groupOID = igGrpMetadata.getItemGroup().getOcOid();
			
			if (!oidMap.containsKey(groupOID)) {
				String groupOIDOrdnl = groupOID;
				ArrayList<String> itemsValues = new ArrayList<String>();
				ArrayList<ItemData> itemDatas = new ArrayList<ItemData>();
				List<ItemGroupMetadata> allItemsInAGroup = igGrpMetadata
						.getItemGroup().getItemGroupMetadatas();

				for (ItemGroupMetadata itemGrpMetada : allItemsInAGroup) {
					itemOID = itemGrpMetada.getItem().getOcOid();
					itemsValues = new ArrayList<String>();
					List<ItemData> itds = itemGrpMetada.getItem()
							.getItemDatas();

		
					// look for the key
					// of same group and ordinal and add this item to
					// that hashmap
					for (ItemData itemData : itds) {
						itemsValues = new ArrayList<String>();
						itemDataValue = fetchItemDataValue(itemData,
								itemGrpMetada.getItem());
						itemDatas =  new ArrayList<ItemData>();
						itemValue = itemOID + DELIMITER + itemDataValue;
						itemsValues.add(itemValue);
						groupOIDOrdnl = groupOID + GROUPOID_ORDINAL_DELIM
								+ itemData.getOrdinal();
						
						if (itemData.getEventCrf().getEventCrfId() == eventCrfId) {

							if (oidMap.containsKey(groupOIDOrdnl)) {

								ArrayList<String> itemgrps = oidMap
										.get(groupOIDOrdnl);
							List<ItemData>itemDataTemps = oidDNAuditMap.get(groupOIDOrdnl);
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

		return populateImportItemGrpBean(oidMap,oidDNAuditMap);
	}

	private String fetchItemDataValue(ItemData itemData, Item item) {
		String idValue = itemData.getValue();
		return idValue;

	}

	private ArrayList<ImportItemGroupDataBean> populateImportItemGrpBean(
			HashMap<String, ArrayList<String>> oidMap, HashMap<String, List<ItemData>> oidDNAuditMap) {
		Set<String> keysGrpOIDs = oidMap.keySet();
		ArrayList<ImportItemGroupDataBean> iigDataBean = new ArrayList<ImportItemGroupDataBean>();
		ImportItemGroupDataBean importItemGrpDataBean = new ImportItemGroupDataBean();
		for (String grpOID : keysGrpOIDs) {
			ArrayList<String> vals = oidMap.get(grpOID);
			importItemGrpDataBean = new ImportItemGroupDataBean();
			int groupIdx = grpOID.indexOf(GROUPOID_ORDINAL_DELIM);
			if (groupIdx != -1) {
				importItemGrpDataBean.setItemGroupOID(grpOID.substring(0,
						groupIdx));
				importItemGrpDataBean.setItemGroupRepeatKey(grpOID.substring(
						groupIdx + 1, grpOID.length()));
				ArrayList<ImportItemDataBean> iiDList = new ArrayList<ImportItemDataBean>();

				for (String value : vals) {
					ImportItemDataBean iiDataBean = new ImportItemDataBean();
					int index = value.indexOf(DELIMITER);
					if (!value.trim().equalsIgnoreCase(DELIMITER)) {
						iiDataBean.setItemOID(value.substring(0, index));
						iiDataBean.setValue(value.substring(index + 1,
								value.length()));
						if(isCollectAudits()||isCollectDns()){
							iiDataBean = fetchItemDataAuditValue(oidDNAuditMap.get(grpOID),iiDataBean);
						}
//						if(isCollectDns())
//							iiDataBean.setDiscrepancyNotes(fetchDiscrepancyNotes(oidDNAuditMap.get(grpOID)));
						iiDList.add(iiDataBean);

					}
				}
				importItemGrpDataBean.setItemData(iiDList);
				iigDataBean.add(importItemGrpDataBean);
			}
		}

		return iigDataBean;
	}

	private ImportItemDataBean fetchItemDataAuditValue(List<ItemData> list,
			ImportItemDataBean iiDataBean) {
		for(ItemData id:list){
			if(id.getItem().getOcOid().equals(iiDataBean.getItemOID())){
				if(isCollectAudits())
				iiDataBean.setAuditLogs(fetchAuditLogs(id.getItemDataId(),"item_data", iiDataBean.getItemOID()));
				if(isCollectDns())
					iiDataBean.setDiscrepancyNotes(fetchDiscrepancyNotes(id));
				return iiDataBean;
			}
		}
		
		
		return iiDataBean;
	}

	private DiscrepancyNotesBean fetchDiscrepancyNotes(ItemData itemData) {
		List<DnItemDataMap> dnItemDataMaps  = itemData.getDnItemDataMaps();
		DiscrepancyNotesBean dnNotesBean = new DiscrepancyNotesBean()	;
		dnNotesBean.setEntityID(itemData.getItem().getOcOid());
		if(isCollectDns())
		{
		DiscrepancyNoteBean dnNoteBean = new DiscrepancyNoteBean();
		
		ArrayList<DiscrepancyNoteBean> dnNotes = new ArrayList<DiscrepancyNoteBean>();
		boolean addDN = true;
		for(DnItemDataMap dnItemDataMap:dnItemDataMaps){
			DiscrepancyNote dn =  dnItemDataMap.getDiscrepancyNote();
			addDN=true;
			fillDNObject(dnNoteBean, dnNotes, addDN, dn);
		}
		dnNotesBean.setDiscrepancyNotes(dnNotes);
		}
		return dnNotesBean;
		
	}
	
	private DiscrepancyNotesBean fetchDiscrepancyNotes(EventCrf eventCrf) {
		List<DnEventCrfMap> dnEventCrfMaps  = eventCrf.getDnEventCrfMaps();
		DiscrepancyNotesBean dnNotesBean = new DiscrepancyNotesBean()	;
		dnNotesBean.setEntityID(eventCrf.getCrfVersion().getOcOid());
		DiscrepancyNoteBean dnNoteBean = new DiscrepancyNoteBean();
		ArrayList<DiscrepancyNoteBean> dnNotes = new ArrayList<DiscrepancyNoteBean>();
		boolean addDN = true;
		for(DnEventCrfMap dnItemDataMap:dnEventCrfMaps){
			DiscrepancyNote dn =  dnItemDataMap.getDiscrepancyNote();
			addDN=true;
			fillDNObject(dnNoteBean, dnNotes, addDN, dn);
		}
		dnNotesBean.setDiscrepancyNotes(dnNotes);
		return dnNotesBean;
		
	} 
	private DiscrepancyNotesBean fetchDiscrepancyNotes(StudySubject studySubj) {
		List<DnStudySubjectMap> dnMaps  = studySubj.getDnStudySubjectMaps();
		DiscrepancyNotesBean dnNotesBean = new DiscrepancyNotesBean()	;
		dnNotesBean.setEntityID(studySubj.getOcOid());
		DiscrepancyNoteBean dnNoteBean = new DiscrepancyNoteBean();
		ArrayList<DiscrepancyNoteBean> dnNotes = new ArrayList<DiscrepancyNoteBean>();
		boolean addDN = true;
		for(DnStudySubjectMap dnMap:dnMaps){
			DiscrepancyNote dn =  dnMap.getDiscrepancyNote();
			addDN=true;
			fillDNObject(dnNoteBean, dnNotes, addDN, dn);
		}
		dnNotesBean.setDiscrepancyNotes(dnNotes);
		return dnNotesBean;
		
	} 
	private DiscrepancyNotesBean fetchDiscrepancyNotes(StudyEvent studyEvent) {
		List<DnStudyEventMap> dnMaps  = studyEvent.getDnStudyEventMaps();
		DiscrepancyNotesBean dnNotesBean = new DiscrepancyNotesBean()	;
		dnNotesBean.setEntityID(studyEvent.getStudyEventDefinition().getOc_oid());
		DiscrepancyNoteBean dnNoteBean = new DiscrepancyNoteBean();
		ArrayList<DiscrepancyNoteBean> dnNotes = new ArrayList<DiscrepancyNoteBean>();
		boolean addDN = true;
		for(DnStudyEventMap dnMap:dnMaps){
			DiscrepancyNote dn =  dnMap.getDiscrepancyNote();
			addDN=true;
			fillDNObject(dnNoteBean, dnNotes, addDN, dn);
		}
		dnNotesBean.setDiscrepancyNotes(dnNotes);
		return dnNotesBean;
		
	} 
	private void fillDNObject(DiscrepancyNoteBean dnNoteBean,
			ArrayList<DiscrepancyNoteBean> dnNotes, boolean addDN,
			DiscrepancyNote dn) {
		{
		if(dn.getParentDnId()!=null && dn.getParentDnId()>0){
			ChildNoteBean childNoteBean = new ChildNoteBean();
			ElementRefBean userRef =  new ElementRefBean();
			childNoteBean.setDescription(dn.getDescription());
			childNoteBean.setStatus(dn.getResolutionStatus().getDescription());
			childNoteBean.setDetailedNote(dn.getDetailedNotes());
			childNoteBean.setOid("DN_"+dn.getDiscrepancyNoteId());
			if(dn.getUserAccount()!=null)
			userRef.setElementDefOID("USR_"+dn.getUserAccount().getUserId());
			else
				userRef.setElementDefOID("");	
			childNoteBean.setUserRef(userRef);
			dnNoteBean.getChildNotes().add(childNoteBean);
		}
		else{
			dnNoteBean = new DiscrepancyNoteBean();
			addDN=false;
			dnNoteBean.setStatus(dn.getResolutionStatus().getDescription());
			dnNoteBean.setNoteType(dn.getEntityType());
		}
		if(addDN)
			dnNotes.add(dnNoteBean);
		}
	}

	private AuditLogsBean fetchAuditLogs(int entityID,
			String itemDataAuditTable, String entityValue) {
	
		AuditLogsBean auditLogsBean = new AuditLogsBean();
	
		if(isCollectAudits()){
		AuditLogEvent auditLog = new AuditLogEvent();
		auditLog.setEntityId(new Integer(entityID));
		auditLog.setAuditTable(itemDataAuditTable);
		
		auditLogsBean.setEntityID(entityValue);
		ArrayList<AuditLogEvent> auditLogEvent = (getAuditEventDAO().findByParam(auditLog));
		
		
		auditLogsBean= fetchODMAuditBean(auditLogEvent,auditLogsBean);
		}
		return auditLogsBean;
	}

	private AuditLogsBean fetchODMAuditBean(ArrayList<AuditLogEvent> auditLogEvents,AuditLogsBean auditLogsBean ) {
	
		for(AuditLogEvent auditLogEvent:auditLogEvents){
		AuditLogBean auditBean = new AuditLogBean();
		auditBean.setOid("AL_"+auditLogEvent.getAuditId());
		auditBean.setDatetimeStamp(auditLogEvent.getAuditDate());
		if(auditLogEvent.getEntityName()!=null && auditLogEvent.getEntityName().equals(STATUS))
		{
			auditBean.setNewValue(Status.getByCode(Integer.valueOf(auditLogEvent.getNewValue())).getName());
			auditBean.setOldValue(Status.getByCode(Integer.valueOf(auditLogEvent.getOldValue())).getName());	
		}
		else{
		auditBean.setNewValue(auditLogEvent.getNewValue()==null?"":auditLogEvent.getNewValue());
		auditBean.setOldValue(auditLogEvent.getOldValue()==null?"":auditLogEvent.getOldValue());
		}
		auditBean.setReasonForChange(auditLogEvent.getReasonForChange()==null?"":auditLogEvent.getReasonForChange());
		auditBean.setType(auditLogEvent.getAuditLogEventType().getName());
		auditBean.setUserId("USR_"+auditLogEvent.getUserId());
		auditLogsBean.getAuditLogs().add(auditBean);
		}
		return auditLogsBean;
	}

	@Override
	public OdmClinicalDataBean getClinicalData(String studyOID, String studySubjectOID,
			String studyEventOID, String formVersionOID,Boolean collectDNs,Boolean collectAudit) {
		setCollectDns(collectDNs);
		setCollectAudits(collectAudit);
		if(studyEventOID.equals(INDICATE_ALL) && formVersionOID.equals(INDICATE_ALL)&&!studySubjectOID.equals(INDICATE_ALL) && !studyOID.equals(INDICATE_ALL))
			return getClinicalData(studyOID, studySubjectOID);
		else 	if(studyEventOID.equals(INDICATE_ALL) && formVersionOID.equals(INDICATE_ALL)&& studySubjectOID.equals(INDICATE_ALL) && !studyOID.equals(INDICATE_ALL))
			return getClinicalData(studyOID);
		else if(!studyEventOID.equals(INDICATE_ALL)&&!studySubjectOID.equals(INDICATE_ALL) && !studyOID.equals(INDICATE_ALL) &&  formVersionOID.equals(INDICATE_ALL))
				return getClinicalDatas(studyOID,studySubjectOID,studyEventOID,null);
		else if(!studyEventOID.equals(INDICATE_ALL)&&!studySubjectOID.equals(INDICATE_ALL) && !studyOID.equals(INDICATE_ALL) &&  !formVersionOID.equals(INDICATE_ALL))
			return getClinicalDatas(studyOID,studySubjectOID,studyEventOID,formVersionOID);

		
		return null;
	}

	private OdmClinicalDataBean getClinicalDatas(String studyOID,
			String studySubjectOID, String studyEventOID,String formVersionOID) {
		int seOrdinal = 0;
		String temp = studyEventOID;
		List<StudyEvent>studyEvents = new ArrayList<StudyEvent>();
		StudyEventDefinition sed = null ;
		Study study = getStudyDao().findByColumnName(studyOID, "oc_oid");
		List<StudySubject> ss = listStudySubjects(studySubjectOID);
		int idx = studyEventOID.indexOf(OPEN_ORDINAL_DELIMITER);
		if(idx>0)
			{
			studyEventOID=  studyEventOID.substring(0,idx);
			seOrdinal = new Integer(temp.substring(idx+1, temp.indexOf(CLOSE_ORDINAL_DELIMITER))).intValue();
			}
		sed = getStudyEventDefDao().findByColumnName(studyEventOID, "oc_oid");
		if(seOrdinal>0)
			{
			studyEvents = fetchSE(seOrdinal,sed.getStudyEvents(),studySubjectOID);
			}
	
		else
		{
			
			studyEvents = fetchSE(sed.getStudyEvents(),studySubjectOID);
			
		}
			
		return constructClinicalDataStudy(ss,study,studyEvents,formVersionOID)		;
	}

	
	
	
	private List<StudyEvent>  fetchSE(int seOrdinal, List<StudyEvent> studyEvents,String ssOID) {
		List<StudyEvent> sEs = new ArrayList<StudyEvent>();
		for(StudyEvent se:studyEvents){
			if(se.getSampleOrdinal()==seOrdinal &&se.getStudySubject().getOcOid().equals(ssOID))
				{
				sEs.add(se);
				
				}
		}
	return sEs;
	}

	private List<StudyEvent>  fetchSE( List<StudyEvent> studyEvents,String ssOID) {
		List<StudyEvent> sEs = new ArrayList<StudyEvent>();
		for(StudyEvent se:studyEvents){
			if(se.getStudySubject().getOcOid().equals(ssOID))
				{
				sEs.add(se);
				
				}
		}
	return sEs;
	}

	
}
