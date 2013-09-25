package org.akaza.openclinica.service.extract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.akaza.openclinica.bean.odmbeans.OdmClinicalDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ExportFormDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ExportStudyEventDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ExportSubjectDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ImportItemDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ImportItemGroupDataBean;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDefinitionDao;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
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
	private StudyDao studyDao;

	private StudySubjectDao studySubjectDao;
	private StudyEventDefinitionDao studyEventDefDao;

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

		List<StudyEvent>ses = new ArrayList<StudyEvent>();
		
		return constructClinicalDataStudy(studySubjs, study,ses, null);
	}

	private OdmClinicalDataBean constructClinicalDataStudy(List<StudySubject> studySubjs, Study study,List<StudyEvent>studyEvents,String formVersionOID) {
		OdmClinicalDataBean odmClinicalDataBean = new OdmClinicalDataBean();
		ExportSubjectDataBean expSubjectBean;
		List<ExportSubjectDataBean> exportSubjDataBeanList = new ArrayList<ExportSubjectDataBean>();
		for(StudySubject studySubj:studySubjs)
		{
		if(studyEvents.size()==0)
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
		exportSubjectDataBean.setStudySubjectId(studySubj.getStudySubjectId()
				+ "");
		exportSubjectDataBean.setSecondaryId(studySubj.getSecondaryLabel());
		exportSubjectDataBean.setStatus(studySubj.getStatus().toString());

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
		// For each metadata get the group, and then get list of all items in
		// that group.so we can a data structure of groupOID and list of
		// itemOIDs with corresponding values will be created.
		for (ItemGroupMetadata igGrpMetadata : set) {
			groupOID = igGrpMetadata.getItemGroup().getOcOid();
			
			if (!oidMap.containsKey(groupOID)) {
				String groupOIDOrdnl = groupOID;
				ArrayList<String> itemsValues = new ArrayList<String>();

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
						itemValue = itemOID + DELIMITER + itemDataValue;
						itemsValues.add(itemValue);
						groupOIDOrdnl = groupOID + GROUPOID_ORDINAL_DELIM
								+ itemData.getOrdinal();
						if (itemData.getEventCrf().getEventCrfId() == eventCrfId) {

							if (oidMap.containsKey(groupOIDOrdnl)) {

								ArrayList<String> itemgrps = oidMap
										.get(groupOIDOrdnl);
								if (!itemgrps.contains(itemValue)) {
									itemgrps.add(itemValue);
									oidMap.remove(groupOIDOrdnl);
								}
								oidMap.put(groupOIDOrdnl, itemgrps);
							} else {
								oidMap.put(groupOIDOrdnl, itemsValues);
							}
						}
					}

				}

			}
		}

		return populateImportItemGrpBean(oidMap);
	}

	private String fetchItemDataValue(ItemData itemData, Item item) {
		String idValue = itemData.getValue();
		return idValue;

	}

	private ArrayList<ImportItemGroupDataBean> populateImportItemGrpBean(
			HashMap<String, ArrayList<String>> oidMap) {
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
						iiDList.add(iiDataBean);

					}
				}
				importItemGrpDataBean.setItemData(iiDList);
				iigDataBean.add(importItemGrpDataBean);
			}
		}

		return iigDataBean;
	}

	@Override
	public OdmClinicalDataBean getClinicalData(String studyOID, String studySubjectOID,
			String studyEventOID, String formVersionOID,Boolean collectDNs,Boolean collectAudit) {
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
