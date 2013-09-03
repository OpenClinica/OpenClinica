package org.akaza.openclinica.service.extract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.akaza.openclinica.bean.extract.odm.FullReportBean;
import org.akaza.openclinica.bean.odmbeans.OdmClinicalDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ExportFormDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ExportStudyEventDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ExportSubjectDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ImportItemDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ImportItemGroupDataBean;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.domain.datamap.EventCrf;
import org.akaza.openclinica.domain.datamap.Item;
import org.akaza.openclinica.domain.datamap.ItemData;
import org.akaza.openclinica.domain.datamap.ItemGroupMetadata;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.akaza.openclinica.domain.datamap.VersioningMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * To generate CDISC-ODM clinical data without data set.
 * @author jnyayapathi
 *
 */

public class GenerateClinicalDataService {
	 protected final static Logger logger = LoggerFactory.getLogger("org.akaza.openclinica.service.extract.GenerateClinicalDataService");
	 protected final static String DELIMITER = ",";
	 private final static String GROUPOID_ORDINAL_DELIM = ":";
	 
	 private StudyDao studyDao;
	 
	 private StudySubjectDao studySubjectDao;
	 
	 public StudySubjectDao getStudySubjectDao() {
		return studySubjectDao;
	}
	public void setStudySubjectDao(StudySubjectDao studySubjectDao) {
		this.studySubjectDao = studySubjectDao;
	}
	public GenerateClinicalDataService(){
		 
	 }
	 public GenerateClinicalDataService(String StudyOID){
		 
		 
		 
		 
	 }
	 
	 public void getClinicalData(String studyOID){
		 Study study = new Study();
		 study.setOc_oid(studyOID);
		study =  getStudyDao().findByColumnName(studyOID, "oc_oid");
		
		 //  Study  study=  getStudyDaoHib().findById(studyBean.getId());
	        //Study study = getStudyDaoHib().findByColumnName(studyOID, "oc_oid");
	       //System.out.println("Study name"+study.getStudyId());
	      // System.out.println(study.getStudies().get(0).getStudy().getEventDefinitionCrfs().get(0).getCrfVersion().getName());
		 
	 }
	 
	 public String getClinicalData(String studyOID,String studySubjectOID){
		 Study study = getStudyDao().findByColumnName(studyOID,"oc_oid");
		 StudySubject studySubj = getStudySubjectDao().findByColumnName(studySubjectOID,"ocOid");
		return constructClinicalData(study,studySubj);
		// return null;
	 }
	public StudyDao getStudyDao() {
		return studyDao;
	}
	public void setStudyDao(StudyDao studyDao) {
		this.studyDao = studyDao;
	}
	
	private String constructClinicalData(Study study,StudySubject studySubj){
		
		
		return constructClinicalDataStudy(studySubj);
	}
	
	private String constructClinicalDataStudy(StudySubject studySubj){
		OdmClinicalDataBean odmClinicalDataBean = new OdmClinicalDataBean();
		
		ExportSubjectDataBean expSubjectBean = setExportSubjectDataBean(studySubj);
		
		
		List<ExportSubjectDataBean> exportSubjDataBeanList = new ArrayList<ExportSubjectDataBean>(); 
		exportSubjDataBeanList.add(expSubjectBean);
		odmClinicalDataBean.setExportSubjectData(exportSubjDataBeanList);
		odmClinicalDataBean.setStudyOID(studySubj.getStudy().getOc_oid());
		
		
		 FullReportBean report = new FullReportBean();
         report.setClinicalData(odmClinicalDataBean);
         report.createChunkedOdmXml(Boolean.TRUE, true, true);
        return report.getXmlOutput().toString();
		//return null;
	}
	
	private ExportSubjectDataBean setExportSubjectDataBean(StudySubject studySubj){
		
		
		ExportSubjectDataBean exportSubjectDataBean = new ExportSubjectDataBean();
		//exportSubjectDataBean.setAuditLogs(studySubj.getA)
		exportSubjectDataBean.setDateOfBirth(studySubj.getSubject().getDateOfBirth()+"");
		exportSubjectDataBean.setStudySubjectId(studySubj.getStudySubjectId()+"");
		exportSubjectDataBean.setSecondaryId(studySubj.getSecondaryLabel());
		exportSubjectDataBean.setStatus(studySubj.getStatus().toString());
		
		exportSubjectDataBean.setExportStudyEventData(setExportStudyEventDataBean(studySubj));
		
		exportSubjectDataBean.setSubjectOID(studySubj.getOcOid());
	//	exportSubjectDataBean.setStudyEventData(studyEventData)
		//exportSubjectDataBean.setDiscrepancyNotes(studySubj.getSubject().getDnStudySubjectMaps());
		return exportSubjectDataBean;
		
			
	}
	
	private ArrayList<ExportStudyEventDataBean> setExportStudyEventDataBean(StudySubject ss){
		ArrayList<ExportStudyEventDataBean> al = new ArrayList<ExportStudyEventDataBean>();
		
		for(StudyEvent se : ss.getStudyEvents())
		{
			ExportStudyEventDataBean expSEBean = new ExportStudyEventDataBean();
			expSEBean.setLocation(se.getLocation());
			expSEBean.setEndDate(se.getDateEnd()+"");
			expSEBean.setStartDate(expSEBean.getStartDate()+"");
			expSEBean.setStudyEventOID(se.getStudyEventDefinition().getOcOid());
			expSEBean.setStudyEventRepeatKey(se.getStudyEventDefinition().getOrdinal().toString());
			expSEBean.setExportFormData(getFormDataForClinicalStudy(se));
			
			al.add(expSEBean);
		}
		
		return al;
	}
	
	private ArrayList<ExportFormDataBean> getFormDataForClinicalStudy(StudyEvent se) {
		List<ExportFormDataBean> formDataBean = new ArrayList<ExportFormDataBean>();
		for(EventCrf ecrf:se.getEventCrfs()){
			ExportFormDataBean dataBean = new ExportFormDataBean();
			//dataBean.setDiscrepancyNotes(ecrf)
			//dataBean.setItemGroupData(getItemData(ecrf));
			dataBean.setItemGroupData(fetchItemData(ecrf.getCrfVersion().getItemGroupMetadatas(),ecrf.getEventCrfId(),ecrf.getCrfVersion().getVersioningMaps()));
			dataBean.setFormOID(ecrf.getCrfVersion().getOcOid());
			dataBean.setInterviewDate(ecrf.getDateInterviewed()+"");
			dataBean.setInterviewerName(ecrf.getInterviewerName());
			dataBean.setStatus(ecrf.getStatus()+"");
			formDataBean.add(dataBean);
			
		}
		return (ArrayList<ExportFormDataBean>) formDataBean;
	}
	
	private ArrayList<ImportItemGroupDataBean> fetchItemData(Set<ItemGroupMetadata> set,int eventCrfId,List<VersioningMap>vms){
		String groupOID,itemOID;
		Integer groupID;
		String itemValue;
		
		
		HashMap<String, ArrayList<String>> oidMap = new HashMap<String, ArrayList<String>>();
		
		//For each metadata get the group, and then get list of all items in that group.so we can a data structure of groupOID and list of itemOIDs with corresponding values will be created.
		
		for(ItemGroupMetadata igGrpMetadata:set){
			groupOID = igGrpMetadata.getItemGroup().getOcOid();
			int defaultOrdinal = 1;
		
			//This logic here is default ordinal for ungrouped items and repeating groups is 1; This needs to be handled differently either here or in populateItemGrpBean to not show this information in case of ungrouped items
			
			if(!oidMap.containsKey(groupOID)){		
				String groupOIDOrdnl = groupOID;
			groupID = igGrpMetadata.getItemGroup().getId();
			ArrayList<String> itemsValues = new ArrayList<String>();
			//itemOID = igGrpMetadata.getItem().getOcOid();
			List<ItemGroupMetadata>allItemsInAGroup = igGrpMetadata.getItemGroup().getItemGroupMetadatas();
			
			for(ItemGroupMetadata itemGrpMetada:allItemsInAGroup){
				itemOID = itemGrpMetada.getItem().getOcOid();
				itemsValues = new ArrayList<String>();
				List<ItemData> itds =  itemGrpMetada.getItem().getItemDatas();
				
				//TODO: Only one item data value should be present not several of them, this should not be a list
				// There also needs to be the response option value populated here depending on what the response type is.
				if(!igGrpMetadata.isRepeatingGroup())
				for(ItemData itemData:itds){
					itemValue = itemOID +DELIMITER+itemData.getValue();
					itemsValues.add(itemValue);
					groupOIDOrdnl = groupOID+GROUPOID_ORDINAL_DELIM+itemData.getOrdinal();
				}
				else{//if the group is a repeating group, look for the key of same group and ordinal and add this item to that hashmap
					for(ItemData itemData:itds){
						itemsValues = new ArrayList<String>(); 
						itemValue = itemOID +DELIMITER+itemData.getValue();
						itemsValues.add(itemValue);
						groupOIDOrdnl = groupOID+GROUPOID_ORDINAL_DELIM+itemData.getOrdinal();
						if(oidMap.containsKey(groupOIDOrdnl))
						{
							
							ArrayList<String>itemgrps = oidMap.get(groupOIDOrdnl);
							itemgrps.add(itemValue);
							oidMap.remove(groupOIDOrdnl);
							oidMap.put(groupOIDOrdnl,itemgrps);
						}
						else
						{
							oidMap.put(groupOIDOrdnl, itemsValues);
						}
					}
				}
				
			
			}
			if(!igGrpMetadata.isRepeatingGroup())
			oidMap.put(groupOIDOrdnl,itemsValues);
		}
		}
		
		return populateImportItemGrpBean(oidMap);
	}
	
private ArrayList<ImportItemGroupDataBean> populateImportItemGrpBean(
			HashMap<String, ArrayList<String>> oidMap) {
		Set<String> keysGrpOIDs = oidMap.keySet();
		ArrayList<ImportItemGroupDataBean> iigDataBean =new ArrayList<ImportItemGroupDataBean>();
		ImportItemGroupDataBean importItemGrpDataBean = new ImportItemGroupDataBean();
		for(String grpOID:keysGrpOIDs){
			ArrayList<String> vals = oidMap.get(grpOID);
			importItemGrpDataBean = new ImportItemGroupDataBean();
			int groupIdx = grpOID.indexOf(GROUPOID_ORDINAL_DELIM);
			
			importItemGrpDataBean.setItemGroupOID(grpOID.substring(0,groupIdx));
			importItemGrpDataBean.setItemGroupRepeatKey(grpOID.substring(groupIdx+1,grpOID.length()));
			ArrayList<ImportItemDataBean> iiDList = new ArrayList<ImportItemDataBean>();
			
			for(String value :vals){
				ImportItemDataBean iiDataBean = new ImportItemDataBean();
				int index = value.indexOf(DELIMITER);
				if(!value.trim().equalsIgnoreCase(DELIMITER))
				{
					iiDataBean.setItemOID(value.substring(0,index));
					iiDataBean.setValue(value.substring(index+1, value.length()));
					iiDList.add(iiDataBean);
					
				}
			}
			importItemGrpDataBean.setItemData(iiDList);
			iigDataBean.add(importItemGrpDataBean);
		}
		return iigDataBean;
	}
	/*	private ArrayList<ImportItemGroupDataBean> fetchItemData(
			Set<ItemGroupMetadata> set,int eventCrfId,List<VersioningMap>vms) {
		ArrayList<ImportItemGroupDataBean> iigDataBean =new ArrayList<ImportItemGroupDataBean>();
		ImportItemGroupDataBean importIDBean = new ImportItemGroupDataBean();
		String itemGroupOID = null;
		for(ItemGroupMetadata igMetadata:set){
	    for(VersioningMap vm:vms){
	    	if(vm.getItem().equals(igMetadata.getItem()))
			{
	    	if(!igMetadata.isRepeatingGroup())
			importIDBean = checkIfGroupOIDExists(iigDataBean,igMetadata.getItemGroup().getOcOid());
			//Only if every groupOID is not created go ahead and create, the items get iterated in for non-repeating groups.
		//	if(importIDBean.getItemGroupOID()==null || importIDBean.getItemGroupOID().isEmpty()){
			ImportItemDataBean iiDataBean = new ImportItemDataBean();
			//}
			if(igMetadata.getItemGroup().getOcOid()!=null && !igMetadata.getItemGroup().getOcOid().isEmpty())
				itemGroupOID = igMetadata.getItemGroup().getOcOid();
			//importIDBean.setItemGroupOID(igMetadata.getItemGroup().getOcOid());
			List<ItemData>itemDatas = vm.getItem().getItemDatas();
			for(ItemData itemData :itemDatas){
				{
					iiDataBean.setValue(itemData.getValue());
					iiDataBean.setItemOID(itemData.getItem().getOcOid());
					importIDBean.setItemGroupRepeatKey(itemData.getOrdinal()+"");
					importIDBean.setItemGroupOID(igMetadata.getItemGroup().getOcOid());
					if(!importIDBean.getItemData().contains(iiDataBean))
					importIDBean.getItemData().add(iiDataBean);
				}
			}
			
			}//setItemDataValues(importIDBean,igMetadata.getCrfVersion().getVersioningMaps(),iiDataBean,itemGroupOID,eventCrfId);
	    	
			}
			
		}
		return iigDataBean;
	}*/
	private ImportItemGroupDataBean checkIfGroupOIDExists(
			ArrayList<ImportItemGroupDataBean> iigDataBean, String ocOid) {
		ImportItemGroupDataBean iiGrpDataBean = new ImportItemGroupDataBean();
		
		/*for(ImportItemGroupDataBean databean:iigDataBean ){
			if(databean.getItemGroupOID()!=null && databean.getItemGroupOID().equals(ocOid)){
				iiGrpDataBean =  databean;
				break;
			}
		}*/
		
		return iiGrpDataBean;
		
	}
	//fetches the item data values 
	private void setItemDataValues(ImportItemGroupDataBean importIDBean,
			List<VersioningMap>vm,ImportItemDataBean iiDataBean,String groupOID,int eventCrfId) {
		
	//	ItemData id = new ItemData();
		for(VersioningMap vm1:vm){
			 iiDataBean = new ImportItemDataBean();
			Item item= vm1.getItem();
			List<ItemGroupMetadata> itGrpMetas = item.getItemGroupMetadatas();
			List<ItemData>itemDatas = item.getItemDatas();
			if(itemDatas.size()>0)
			{
				for(ItemGroupMetadata itmGrpMeta:itGrpMetas)
				{
					if(itmGrpMeta.getItemGroup().getOcOid().equals(groupOID)  )
					{
					//id = item.getItemDatas().get(item.getItemDatas().size()-1);
					for(ItemData id :itemDatas)	{
					if(id.getEventCrf().getEventCrfId()==eventCrfId)
					{				
					iiDataBean.setValue(id.getValue());
					iiDataBean.setItemOID(id.getItem().getOcOid());
					importIDBean.setItemGroupRepeatKey(id.getOrdinal()+"");
					importIDBean.setItemGroupOID(itmGrpMeta.getItemGroup().getOcOid());
					if(!importIDBean.getItemData().contains(iiDataBean))
					importIDBean.getItemData().add(iiDataBean);
					}
					}
					}
				}
			}
		}
		
	}
	
}
