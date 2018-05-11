package org.akaza.openclinica.control.submit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudyGroupClassBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.control.AbstractTableFactory;
import org.akaza.openclinica.control.DefaultActionsEditor;

import org.akaza.openclinica.control.submit.ListDiscNotesSubjectTableFactory.StatusFilterMatcher;
import org.akaza.openclinica.control.submit.ListDiscNotesSubjectTableFactory.SubjectEventStatusFilterMatcher;

import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.ListDiscNotesSubjectFilter;
import org.akaza.openclinica.dao.managestudy.ListDiscNotesSubjectSort;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupClassDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectGroupMapDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.jmesa.core.filter.MatcherKey;
import org.jmesa.facade.TableFacade;
import org.jmesa.limit.Filter;
import org.jmesa.limit.FilterSet;
import org.jmesa.limit.Limit;
import org.jmesa.limit.Sort;
import org.jmesa.limit.SortSet;
import org.jmesa.view.component.Row;
/**
 * 
 * @author jnyayapathi
 *
 */
public class DiscNotesSubjectStatisticsFactory extends AbstractTableFactory{
	private String[] columnNames = new String[] {};
    private StudyEventDefinitionDAO studyEventDefinitionDao;
    private StudySubjectDAO studySubjectDAO;
    private SubjectDAO subjectDAO;
    private StudyEventDAO studyEventDAO;
    private StudyGroupClassDAO studyGroupClassDAO;
    private SubjectGroupMapDAO subjectGroupMapDAO;
    private StudyGroupDAO studyGroupDAO;
    private StudyDAO studyDAO;
    private EventCRFDAO eventCRFDAO;
    private EventDefinitionCRFDAO eventDefintionCRFDAO;
    private DiscrepancyNoteDAO discrepancyNoteDAO;
    private StudyBean studyBean;

    private ArrayList<StudyEventDefinitionBean> studyEventDefinitions;
    private ArrayList<StudyGroupClassBean> studyGroupClasses;
    private StudyUserRoleBean currentRole;
    private UserAccountBean currentUser;
    private ResourceBundle resword;
    private ResourceBundle resformat;
    private ResourceBundle resterm;
    private String module;
    private Integer resolutionStatus;
    private Integer discNoteType;
    private Boolean studyHasDiscNotes;
    private Set<Integer> resolutionStatusIds;
    private Map<Object,Map> discrepancyMap;

	@Override
	protected String getTableName() {
		return "discNotesSummary";
	}

	@Override
	protected void configureColumns(TableFacade tableFacade, Locale locale) {
		//resword = ResourceBundleProvider.getWordsBundle(locale);
        //resformat = ResourceBundleProvider.getFormatBundle(locale);
        tableFacade.setColumnProperties(columnNames);
        Row row = tableFacade.getTable().getRow();
        HashMap<Object,Map> items = (HashMap<Object,Map>) getDiscrepancyMap();
        Set theKeys  = items.keySet();
        Iterator theKeysItr = theKeys.iterator();
        configureColumn(row.getColumn(columnNames[0]), "_", null, null);
        configureColumn(row.getColumn(columnNames[1]), theKeysItr.next().toString(), null, null);
        configureColumn(row.getColumn(columnNames[2]), theKeysItr.next().toString(), null, null);
        configureColumn(row.getColumn(columnNames[3]), theKeysItr.next().toString(), null, null);
        configureColumn(row.getColumn(columnNames[4]), theKeysItr.next().toString(), null, null);
        configureColumn(row.getColumn(columnNames[5]), "Totals", null, null);
        // study event definition columns
      
        //String actionsHeader = resword.getString("rule_actions") + "&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;";
        //configureColumn(row.getColumn(columnNames[columnNames.length - 1]), "Summary Statistics", null, null, false,
         //       false);					
	}

	 @Override
	    public void configureTableFacade(HttpServletResponse response, TableFacade tableFacade) {
	        super.configureTableFacade(response, tableFacade);
	        
	        getColumnNamesMap();
	       // tableFacade.addFilterMatcher(new MatcherKey(Status.class), new StatusFilterMatcher());
	        // tableFacade.addFilterMatcher(new MatcherKey(Integer.class), new
	        // SubjectEventStatusFilterMatcher());

	       

	    }
	@Override
	public void setDataAndLimitVariables(TableFacade tableFacade) {
        StudyBean study = this.getStudyBean();
        Limit limit = tableFacade.getLimit();

        ListDiscNotesSubjectFilter subjectFilter = getSubjectFilter(limit);
       // subjectFilter.addFilter("dn.discrepancy_note_type_id", this.discNoteType);
        StringBuffer constraints = new StringBuffer();
        /*  if (this.discNoteType > 0 && this.discNoteType < 10) {
            constraints.append(" and dn.discrepancy_note_type_id=" + this.discNoteType);
        }
        if (this.resolutionStatusIds != null && this.resolutionStatusIds.size() > 0) {
            String s = " and (";
            for (Integer resolutionStatusId : this.resolutionStatusIds) {
                s += "dn.resolution_status_id = " + resolutionStatusId + " or ";
            }
            s = s.substring(0, s.length() - 3) + " )";
            subjectFilter.addFilter("dn.resolution_status_id", s);
            constraints.append(s);
        }
*/
        if (!limit.isComplete()) {
//            int totalRows = getStudySubjectDAO().getCountWithFilter(subjectFilter, study);
            tableFacade.setTotalRows(6);
        }


        int rowStart = limit.getRowSelect().getRowStart();
        int rowEnd = 6;

        ListDiscNotesSubjectSort subjectSort = getSubjectSort(limit);
        HashMap<Object,Map> items = (HashMap<Object,Map>) getDiscrepancyMap();

        Collection<HashMap<Object, Object>> theItems = new ArrayList<HashMap<Object, Object>>();
        Collection<HashMap<Object, Object>> theItemsKeys = new ArrayList<HashMap<Object, Object>>();
        Collection<HashMap<Object, Object>> theItemsVals = new ArrayList<HashMap<Object, Object>>();
        Iterator keyIt = null;
        if(items.values().iterator().hasNext())
        keyIt = items.values().iterator().next().keySet().iterator();
        HashMap<Object, Object> theItem = new HashMap();

   	 Set theKeys  = items.keySet();
   
   	 
   	 
   	 List<Object> existingKey = new ArrayList();
   	Iterator theKeysItr = theKeys.iterator();
   	while(keyIt.hasNext())   
   	{
     	 String key = "",val = "";	

	       key = keyIt.next().toString();
	     //  val=keyIt.ne.toString();
//	if(!existingKey.contains(key))
//		{
//		existingKey.add(key);
//		break;
//		}
//	else
//	{
//		key = keyIt.next().toString();
//		// val=firstVals.get(key).toString();
//		existingKey.add( key);
//		break;
//	}
 	
   		
   		for(Map<String,String[]> firstVals:items.values())
        {
        	
   			
   			
   			
   			
   			theItem = new HashMap();
            Iterator it = firstVals.values().iterator();
        	// keyIt = firstVals.keySet().iterator();
            String label = (String)theKeysItr.next();
        	
          	
 
              
          
               while(it.hasNext())
                    {
                	    
                	theItem.put("_", key);
                	theItem.put(label, it.next());
                	
                    }
                	
                
           	 theItems.add(theItem);
  
        }
        

       
        theItemsVals.addAll(theItems);
  
		
		tableFacade.setItems(theItemsVals);
	}
	}
	   private void getColumnNamesMap() {
	        ArrayList<String> columnNamesList = new ArrayList<String>();
	        HashMap<Object,Map> items = (HashMap<Object,Map>) getDiscrepancyMap();
	        Set theKeys  = items.keySet();
	        Iterator theKeysItr = theKeys.iterator();
	        columnNamesList.add("_");
	        columnNamesList.add(theKeysItr.next().toString());
	        columnNamesList.add(theKeysItr.next().toString());
	        
	        columnNamesList.add(theKeysItr.next().toString());
	        columnNamesList.add(theKeysItr.next().toString());
	        
	        columnNamesList.add("Totals");
	        columnNames = columnNamesList.toArray(columnNames);
	    }
	  
	 protected ListDiscNotesSubjectSort getSubjectSort(Limit limit) {
	        ListDiscNotesSubjectSort listDiscNotesSubjectSort = new ListDiscNotesSubjectSort();
	        SortSet sortSet = limit.getSortSet();
	        Collection<Sort> sorts = sortSet.getSorts();
	        for (Sort sort : sorts) {
	            String property = sort.getProperty();
	            String order = sort.getOrder().toParam();
	            listDiscNotesSubjectSort.addSort(property, order);
	        }

	        return listDiscNotesSubjectSort;
	    }

	
	
	
	   protected ListDiscNotesSubjectFilter getSubjectFilter(Limit limit) {
	        ListDiscNotesSubjectFilter listDiscNotesSubjectFilter = new ListDiscNotesSubjectFilter();
	        FilterSet filterSet = limit.getFilterSet();
	        Collection<Filter> filters = filterSet.getFilters();
	        for (Filter filter : filters) {
	            String property = filter.getProperty();
	            String value = filter.getValue();
	            listDiscNotesSubjectFilter.addFilter(property, value);
	        }

	        return listDiscNotesSubjectFilter;
	    }
	
	
	
	
		public Map<Object, Map> getDiscrepancyMap() {
			return discrepancyMap;
		}

		public void setDiscrepancyMap(Map<Object, Map> discrepancyMap) {
			this.discrepancyMap = discrepancyMap;
		}

	   public StudyEventDefinitionDAO getStudyEventDefinitionDao() {
	        return studyEventDefinitionDao;
	    }

	    public void setStudyEventDefinitionDao(StudyEventDefinitionDAO studyEventDefinitionDao) {
	        this.studyEventDefinitionDao = studyEventDefinitionDao;
	    }

	    public StudyBean getStudyBean() {
	        return studyBean;
	    }

	    public void setStudyBean(StudyBean studyBean) {
	        this.studyBean = studyBean;
	    }

	    public StudySubjectDAO getStudySubjectDAO() {
	        return studySubjectDAO;
	    }

	    public void setStudySubjectDAO(StudySubjectDAO studySubjectDAO) {
	        this.studySubjectDAO = studySubjectDAO;
	    }

	    public SubjectDAO getSubjectDAO() {
	        return subjectDAO;
	    }

	    public void setSubjectDAO(SubjectDAO subjectDAO) {
	        this.subjectDAO = subjectDAO;
	    }

	    public StudyEventDAO getStudyEventDAO() {
	        return studyEventDAO;
	    }

	    public void setStudyEventDAO(StudyEventDAO studyEventDAO) {
	        this.studyEventDAO = studyEventDAO;
	    }

	    public StudyGroupClassDAO getStudyGroupClassDAO() {
	        return studyGroupClassDAO;
	    }

	    public void setStudyGroupClassDAO(StudyGroupClassDAO studyGroupClassDAO) {
	        this.studyGroupClassDAO = studyGroupClassDAO;
	    }

	    public SubjectGroupMapDAO getSubjectGroupMapDAO() {
	        return subjectGroupMapDAO;
	    }

	    public void setSubjectGroupMapDAO(SubjectGroupMapDAO subjectGroupMapDAO) {
	        this.subjectGroupMapDAO = subjectGroupMapDAO;
	    }

	    public StudyDAO getStudyDAO() {
	        return studyDAO;
	    }

	    public void setStudyDAO(StudyDAO studyDAO) {
	        this.studyDAO = studyDAO;
	    }

	    public StudyUserRoleBean getCurrentRole() {
	        return currentRole;
	    }

	    public void setCurrentRole(StudyUserRoleBean currentRole) {
	        this.currentRole = currentRole;
	    }

	    public EventCRFDAO getEventCRFDAO() {
	        return eventCRFDAO;
	    }

	    public void setEventCRFDAO(EventCRFDAO eventCRFDAO) {
	        this.eventCRFDAO = eventCRFDAO;
	    }

	    public EventDefinitionCRFDAO getEventDefintionCRFDAO() {
	        return eventDefintionCRFDAO;
	    }

	    public void setEventDefintionCRFDAO(EventDefinitionCRFDAO eventDefintionCRFDAO) {
	        this.eventDefintionCRFDAO = eventDefintionCRFDAO;
	    }

	    public StudyGroupDAO getStudyGroupDAO() {
	        return studyGroupDAO;
	    }

	    public void setStudyGroupDAO(StudyGroupDAO studyGroupDAO) {
	        this.studyGroupDAO = studyGroupDAO;
	    }

	    public DiscrepancyNoteDAO getDiscrepancyNoteDAO() {
	        return discrepancyNoteDAO;
	    }

	    public void setDiscrepancyNoteDAO(DiscrepancyNoteDAO discrepancyNoteDAO) {
	        this.discrepancyNoteDAO = discrepancyNoteDAO;
	    }

	    public ResourceBundle getResword() {
	        return resword;
	    }

	    public void setResword(ResourceBundle resword) {
	        this.resword = resword;
	    }

	    public ResourceBundle getResterm() {
	        return resterm;
	    }

	    public void setResterm(ResourceBundle resterm) {
	        this.resterm = resterm;
	    }

	    public String getModule() {
	        return module;
	    }

	    public void setModule(String module) {
	        this.module = module;
	    }

	    public UserAccountBean getCurrentUser() {
	        return currentUser;
	    }

	    public Integer getResolutionStatus() {
	        return resolutionStatus;
	    }

	    public void setResolutionStatus(Integer resolutionStatus) {
	        this.resolutionStatus = resolutionStatus;
	    }

	    public Integer getDiscNoteType() {
	        return discNoteType;
	    }

	    public void setDiscNoteType(Integer discNoteType) {
	        this.discNoteType = discNoteType;
	    }

	    public Boolean isStudyHasDiscNotes() {
	        return studyHasDiscNotes;
	    }

	    public void setStudyHasDiscNotes(Boolean studyHasDiscNotes) {
	        this.studyHasDiscNotes = studyHasDiscNotes;
	    }

	    public void setCurrentUser(UserAccountBean currentUser) {
	        this.currentUser = currentUser;
	    }

}
