package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.akaza.openclinica.bean.admin.CrfVersionMappingBean;
import org.akaza.openclinica.bean.submit.crfdata.ImportItemDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ImportItemGroupDataBean;
import org.akaza.openclinica.domain.datamap.AuditLogEvent;
import org.akaza.openclinica.domain.datamap.ItemData;
import org.akaza.openclinica.domain.datamap.ItemGroupMetadata;
import org.hibernate.Filter;
import org.hibernate.impl.FilterImpl;

public class AuditLogEventDao extends AbstractDomainDao<AuditLogEvent> {
	private final static String GROUPOID_ORDINAL_DELIM = ":";

	 @Override
	    public Class<AuditLogEvent> domainClass() {
	        return AuditLogEvent.class;
	    }
	 
	 @SuppressWarnings("unchecked")
	public <T> T findByParam(AuditLogEvent auditLogEvent, String anotherAuditTable){
		   getSessionFactory().getStatistics().logSummary();
		   String query = "from " + getDomainClassName();
		   String buildQuery = "";
	       if(auditLogEvent.getEntityId()!=null && auditLogEvent.getAuditTable()!=null && anotherAuditTable==null)
	       {	   buildQuery+= "do.entityId =:entity_id ";
	         	   buildQuery+= " and  do.auditTable =:audit_table order by do.auditId ";
	       }
	       else if(auditLogEvent.getEntityId()!=null && auditLogEvent.getAuditTable()!=null && anotherAuditTable!=null)
	       {
	    	   buildQuery+= "do.entityId =:entity_id ";
         	   buildQuery+= " and ( do.auditTable =:audit_table or do.auditTable =:anotherAuditTable) order by do.auditId ";
	       }
	       if(!buildQuery.isEmpty())
		    query = "from " + getDomainClassName() +  " do  where "+buildQuery;
	       else
	    	   query = "from " + getDomainClassName()  ;
	       org.hibernate.Query q = getCurrentSession().createQuery(query);
	       if(auditLogEvent.getEntityId()!=null && auditLogEvent.getAuditTable()!=null && anotherAuditTable==null)
	       {  q.setInteger("entity_id", auditLogEvent.getEntityId());
	        	   q.setString("audit_table", auditLogEvent.getAuditTable());
	       }
	       else if(auditLogEvent.getEntityId()!=null && auditLogEvent.getAuditTable()!=null && anotherAuditTable!=null)
	       {
	    	   q.setInteger("entity_id", auditLogEvent.getEntityId());
        	   q.setString("audit_table", auditLogEvent.getAuditTable());
        	   q.setString("anotherAuditTable", anotherAuditTable);
	       }
	        	   return (T) q.list();
	 }

	 @SuppressWarnings("unchecked")
	public <T> T findByParamUpdated( ArrayList<CrfVersionMappingBean> arr, ItemData id ,String itemName, HashMap<String, List<ItemData>> oidDNAuditMap	){
		   getSessionFactory().getStatistics().logSummary();
		   String query = "";
		   String buildQuery = " (   ";
        	   for (CrfVersionMappingBean ar :arr){
		       buildQuery+= " ( do.eventCrfId ="+ ar.getEventCrfId()   +")  OR ";        		   
        	   }
        	   
		      buildQuery= buildQuery.substring(0,buildQuery.length()-4);       
		       buildQuery+= " ) ";
	
		       
	   for (ItemGroupMetadata igm :id.getItem().getItemGroupMetadatas() ){
		     if (igm.getCrfVersion().getCrfVersionId() == arr.get(0).getCrfVersionId() ){
	//   System.out.println(id.getItem().getItemGroupMetadatas().get(0).getItemGroup().getOcOid() + GROUPOID_ORDINAL_DELIM + String.valueOf(0));
		    	 if (igm.isRepeatingGroup()  &&  id.getOrdinal()>1){		    			 
     			      buildQuery+= " and do.entityId = " + id.getItemDataId();

		    	 }else if(igm.isRepeatingGroup() && id.getOrdinal()==1){
		    		 buildQuery+= " and do.entityName =\'"+itemName.toString()+"\'";
                     buildQuery+= " and do.eventCrfId !="+ id.getEventCrf().getEventCrfId();
    				      
	    	 }else{
	    		 buildQuery+= " and do.entityName =\'"+itemName.toString()+"\'";

 
		    	 } 		    		 
		     }
	   }
		   
		      
// 		    			 oidDNAuditMap.containsKey(id.getItem().getItemGroupMetadatas().get(0).getItemGroup().getOcOid() + GROUPOID_ORDINAL_DELIM + String.valueOf(0))){

	   

		       buildQuery+= " and (do.oldValue !='' OR do.newValue != '') ";
	      	   buildQuery+= " order by do.auditId ";
	       
		    query = "from " + getDomainClassName() +  " do  where "+buildQuery;

		    org.hibernate.Query q = getCurrentSession().createQuery(query);

	
	          
	  	        	   return (T) q.list();
	 }
}