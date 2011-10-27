package org.akaza.openclinica.controller;

import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.ItemGroupMetadataBean;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.dao.submit.ItemGroupMetadataDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.HttpSessionRequiredException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.akaza.openclinica.view.StudyInfoPanel;
import org.akaza.openclinica.web.SQLInitServlet;
import org.akaza.openclinica.web.table.sdv.SDVUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;


/**
 * Implement the functionality for displaying a table of Event CRFs for Source Data
 * Verification. This is an autowired, multiaction Controller.
 */
@Controller("changeCRFVersionController")
public class ChangeCRFVersionController {
    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @Autowired
    CoreResources coreResources;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    
    //Autowire the class that handles the sidebar structure with a configured
    //bean named "sidebarInit"
    @Autowired
    @Qualifier("sidebarInit")
    private SidebarInit sidebarInit;
    
    ResourceBundle  resword,resformat;


    public ChangeCRFVersionController() {
    }

   
/*
 * Allows user to select new CRF version
 * 
 */
     //  @RequestMapping(value="/managestudy/chooseCRFVersion", method = RequestMethod.GET)
         @RequestMapping(value="/managestudy/chooseCRFVersion", method = RequestMethod.GET)
    public ModelMap chooseCRFVersion(HttpServletRequest request,HttpServletResponse response,
    		@RequestParam("crfId") int crfId,
    		@RequestParam("crfName") String crfName,
    		@RequestParam("crfversionId") int crfVersionId,
    		@RequestParam("crfVersionName") String crfVersionName,	
    		@RequestParam("studySubjectLabel") String studySubjectLabel, 
    		@RequestParam("studySubjectId") int studySubjectId,
    		@RequestParam("eventCRFId") int eventCRFId,
    		@RequestParam("eventDefinitionCRFId") int eventDefinitionCRFId)
	
	 {

    	//to be removed for aquamarine
    	  if(!mayProceed(request)){
              try{
                  response.sendRedirect(request.getContextPath() + "/MainMenu?message=authentication_failed");
              }catch (Exception e){
                  e.printStackTrace();
              }
              return null;
          }
    	  resetPanel(request); 
        ModelMap gridMap = new ModelMap();
        /*EventCRFDAO eventCRFDAO = new EventCRFDAO(dataSource);
        List<EventCRFBean> eventCRFBeans = eventCRFDAO.findAllByStudySubject(studySubjectId);*/

        request.setAttribute("eventCRFId", eventCRFId);
        request.setAttribute("studySubjectLabel", studySubjectLabel);
        request.setAttribute("eventDefinitionCRFId", eventDefinitionCRFId);
        request.setAttribute("studySubjectId", studySubjectId);
        request.setAttribute("crfId", crfId);
    	request.setAttribute("crfName",crfName);
    	request.setAttribute("crfversionId", crfVersionId);
    	request.setAttribute("crfVersionName",crfVersionName.trim());
       
        ArrayList<String> pageMessages = (ArrayList<String>) request.getAttribute("pageMessages");
        if (pageMessages == null) {
            pageMessages = new ArrayList<String>();
        }

        request.setAttribute("pageMessages", pageMessages);
        Object errorMessage = request.getParameter("errorMessage");
        if ( errorMessage != null){
        	pageMessages.add( (String)errorMessage);
        }
        //get CRF by ID with all versions
        //create List of all versions (label + value)
        //set default CRF version label
        setupResource(request);
         
        //from event_crf get 
        StudyBean study = (StudyBean) request.getSession().getAttribute("study");
        
        CRFDAO cdao = new CRFDAO(dataSource);
        CRFBean crfBean = (CRFBean)cdao.findByPK(crfId);
        CRFVersionDAO crfVersionDao = new CRFVersionDAO(dataSource);
        ArrayList<CRFVersionBean> versions = (ArrayList<CRFVersionBean>) crfVersionDao.findAllActiveByCRF(crfId);
        StudyEventDefinitionDAO sfed = new StudyEventDefinitionDAO(dataSource);
    	StudyEventDefinitionBean sedb =   sfed.findByEventDefinitionCRFId(eventDefinitionCRFId);
    	request.setAttribute("eventName", sedb.getName());
    		
    	 EventCRFDAO ecdao = new EventCRFDAO(dataSource);
    	 EventCRFBean ecb = (EventCRFBean) ecdao.findByPK(eventCRFId);

         StudyEventDAO sedao = new StudyEventDAO(dataSource);
         StudyEventBean seb = (StudyEventBean) sedao.findByPK(ecb.getStudyEventId());
         request.setAttribute("eventCreateDate", formatDate(seb.getCreatedDate()));
       	if ( sedb.isRepeating()){
    		request.setAttribute("eventOrdinal", seb.getSampleOrdinal());
    	}
        if (study.getParentStudyId()>0 ){
        	EventDefinitionCRFDAO edfdao = new EventDefinitionCRFDAO(dataSource);
        	EventDefinitionCRFBean edf = (EventDefinitionCRFBean) edfdao.findByPK(eventDefinitionCRFId);
        	
        	if (!edf.getSelectedVersionIds().equals("")){
	        	String[] version_ids = edf.getSelectedVersionIds().split(",");
	        	HashMap<String,String> tmp = new HashMap<String,String>(version_ids.length);
	        	for ( String vs : version_ids){
	        		tmp.put(vs, vs);
	        	}
	        	ArrayList<CRFVersionBean> site_versions = new ArrayList<CRFVersionBean>(versions.size());
	        	
	        	for (CRFVersionBean vs : versions) {
	                if (tmp.get( String.valueOf(vs.getId())) != null) {
	                	site_versions.add(vs);
	                }
	            }
        	    versions = site_versions;
        	}
        
        }
       // String dir = SQLInitServlet.getField("filePath") + "crf" + File.separator + "new" + File.separator;// for
        
        /*
        // check whether the speadsheet is available on the server
        for (CRFVersionBean curVersion :  versions) {
            File file = new File(dir + crfBean.getId() + curVersion.getOid() + ".xls");
            logger.info("looking in " + dir + crfBean.getId() + curVersion.getOid() + ".xls");
            if (file.exists()) {
            	curVersion.setDownloadable(true);
            } else {
                File file2 = new File(dir + crfBean.getId() + curVersion.getName() + ".xls");
                logger.info("initial failed, looking in " + dir + crfBean.getId() + curVersion.getName() + ".xls");
                if (file2.exists()) {
                	curVersion.setDownloadable(true);
                }
            }

        }
        */
        crfBean.setVersions(versions);
        gridMap.addAttribute("numberOfVersions", crfBean.getVersions().size()+1);
        gridMap.addAttribute("crfBean", crfBean);
        
        return gridMap;
    }

    /*
     * Displays two set of columns for user to confirm his decision to switch to a new version of CRF
     * field name | OID | field value
     */

          

           
    @RequestMapping(value="/managestudy/confirmCRFVersionChange",  method = RequestMethod.POST)
    // @RequestMapping("/managestudy/confirmCRFVersionChange")
    public ModelMap confirmCRFVersionChange(HttpServletRequest request,HttpServletResponse response,
    		@RequestParam(value="crfId", required = false) int crfId,
    		@RequestParam(value="crfName", required = false) String crfName,
    		@RequestParam(value="crfversionId", required = false) int crfVersionId,
    		@RequestParam(value="crfVersionName", required = false) String crfVersionName,	
    		@RequestParam(value="studySubjectLabel", required = false) String studySubjectLabel, 
    		@RequestParam(value="studySubjectId", required = false) int studySubjectId,
    		@RequestParam(value="eventCRFId", required = false) int eventCRFId,
    		@RequestParam(value="eventDefinitionCRFId", required = false) int eventDefinitionCRFId,
    		@RequestParam(value="selectedVersionId" , required = false) int selectedVersionId,
    		@RequestParam(value="selectedVersionName", required=false) String selectedVersionName,
    		@RequestParam(value="eventName",  required=false) String eventName,
		    @RequestParam(value="eventCreateDate",  required=false) String eventCreateDate,        	
		    @RequestParam(value="eventOrdinal", required=false) String eventOrdinal,
		        
    		@RequestParam("confirmCRFVersionSubmit") String as
    ) 
    
    
    {

    	//add here error handling for post with no data and redirect from OC error page
    	//to be removed for aquamarine
    	  if(!mayProceed(request)){
        		  if( redirect( request, response, "/MainMenu?message=authentication_failed") == null)
        			  return null;
          }
    	  resetPanel(request); 
    	  request.setAttribute("eventCRFId", eventCRFId);
          request.setAttribute("studySubjectLabel", studySubjectLabel);
          request.setAttribute("eventDefinitionCRFId", eventDefinitionCRFId);
          request.setAttribute("studySubjectId", studySubjectId);
          request.setAttribute("crfId", crfId);
      	request.setAttribute("crfName",crfName);
      	request.setAttribute("crfversionId", crfVersionId);
      	request.setAttribute("crfVersionName",crfVersionName.trim());
      	request.setAttribute("selectedVersionId",selectedVersionId);
      	if (selectedVersionName != null){
      		selectedVersionName = selectedVersionName.trim();
      	}
      	request.setAttribute("selectedVersionName",selectedVersionName);
      	request.setAttribute("eventName", eventName);
    	request.setAttribute("eventCreateDate", eventCreateDate);
    	request.setAttribute("eventOrdinal", eventOrdinal);
      	
        ModelMap gridMap = new ModelMap();
        ArrayList<String> pageMessages = (ArrayList<String>) request.getAttribute("pageMessages");
        if (pageMessages == null) {
            pageMessages = new ArrayList<String>();
        }
        setupResource(request);
        if ( selectedVersionId == -1){
        	
        	String errorMessage = resword.getString("confirm_crf_version_em_select_version");//"Please select CRF version";
//        	pageMessages.add(errorMessage);
//        	request.setAttribute("pageMessages",pageMessages);
//        	request.setAttribute("errorMessage",errorMessage);
            StringBuffer params = new StringBuffer();
            params.append("/pages/managestudy/chooseCRFVersion?crfId="+crfId);
            params.append("&crfName="+crfName);
            params.append("&crfversionId="+crfVersionId);
            params.append("&crfVersionName="+crfVersionName);
            params.append("&studySubjectLabel="+studySubjectLabel);
            params.append("&studySubjectId="+studySubjectId);
            params.append("&eventCRFId="+eventCRFId);
            params.append("&eventDefinitionCRFId="+eventDefinitionCRFId);
            params.append("&errorMessage="+errorMessage);
            
            if( redirect( request, response, params.toString()) == null){
      			  return null;
            }
        }
       
    	
        request.getSession().removeAttribute("pageMessages"  );
        //get dATa for current crf version display
        //select   name, ordinal, oc_oid,   item_data_id, i.item_id, value        from item_data id, item i 
       // where id.item_id=i.item_id and event_crf_id = 171 order  by i.item_id,ordinal;
        ArrayList<String[]> rows = new ArrayList<String[]>();
        int cur_counter = 0; int new_counter = 0;
	       
        try{
        	ItemDAO item_dao = new ItemDAO(dataSource);
        	ItemDataBean d_bean = null;
        	//get metadata to find repeat group or not
	        ItemGroupMetadataDAO dao_item_form_mdata = new ItemGroupMetadataDAO(dataSource);
	        List<ItemGroupMetadataBean> beans_item_form_mdata = dao_item_form_mdata.findByCrfVersion( crfVersionId);
	        HashMap<Integer, ItemGroupMetadataBean> hash_item_form_mdata = new HashMap<Integer, ItemGroupMetadataBean>(beans_item_form_mdata.size());
	        //put in hash 
	        
	        for (ItemGroupMetadataBean bn : beans_item_form_mdata){
	        	hash_item_form_mdata.put(new Integer(bn.getItemId()), bn);
	        }
	        List<ItemGroupMetadataBean> bn_new_item_form_mdata = dao_item_form_mdata.findByCrfVersion( selectedVersionId);
	        HashMap<Integer, ItemGroupMetadataBean> hash_new_item_form_mdata = new HashMap<Integer, ItemGroupMetadataBean>(bn_new_item_form_mdata.size());
	        //put in hash 
	        
	        for (ItemGroupMetadataBean bn : bn_new_item_form_mdata){
	        	hash_new_item_form_mdata.put(new Integer(bn.getItemId()), bn);
	        }
	        //get items description
	        ArrayList<ItemBean> cur_items = item_dao.findAllWithItemDataByCRFVersionId(crfVersionId,eventCRFId);
	        ArrayList<ItemBean> new_items = item_dao.findAllItemsByVersionId(selectedVersionId);
	       
	         ItemBean cur_element=null; ItemBean new_element=null;
	        ItemGroupMetadataBean bn_mdata= null;ItemGroupMetadataBean bn_new_mdata=null;
	        while ( true){
	        	if (cur_counter >= (cur_items.size()-1) && new_counter >= (new_items.size()-1 )){break;}
	        	cur_element = cur_items.get(cur_counter);
        		bn_mdata = hash_item_form_mdata.get( new Integer(cur_element.getId()));
        		new_element = new_items.get(new_counter);
        		bn_new_mdata = hash_new_item_form_mdata.get( new Integer(new_element.getId()));
        		
	        	if (  new_element.getId() == cur_element.getId()){
	        		buildRecord( cur_element,  new_element,  bn_mdata, bn_new_mdata, rows);
	        	}
	        	else if (  new_element.getId() < cur_element.getId()){
	        		buildRecord( null,  new_element,  null, bn_new_mdata, rows);  		
	        	}
	        	else if (  new_element.getId() > cur_element.getId()){
	        		buildRecord( cur_element,  null,  bn_mdata, null, rows);
	        	}
	        	
	        	
	        	if ( cur_counter >= (cur_items.size()-1) && new_counter < (new_items.size()-1 )){
	        		while(new_counter< new_items.size()-1){
	        			new_counter ++; 
	        			new_element = new_items.get(new_counter);
	            		bn_new_mdata = hash_new_item_form_mdata.get( new Integer(new_element.getId()));
	            		
	            		buildRecord( null,  new_element,  null, bn_new_mdata, rows);  		
	        		}
	        		break;
             	}
	        	if ( cur_counter < (cur_items.size()-1) && new_counter >= (new_items.size()-1 )){
	        		while(cur_counter< cur_items.size()-1){
	        			cur_counter ++; 
		        		cur_element = cur_items.get(cur_counter);
		        		bn_mdata = hash_item_form_mdata.get( new Integer(cur_element.getId()));
		        		buildRecord( cur_element,  null,  bn_mdata, null, rows);
	        		}
	        		break;
	        	}
	        	if ( new_element.getId() == cur_element.getId()){
	        		cur_counter ++;  new_counter++;continue;
	        	}
	        	else if (new_element.getId() < cur_element.getId()){
	        		 new_counter++;continue;
	        	}
	        	else if ( new_element.getId() > cur_element.getId()){
	        		cur_counter ++; continue;
	        	}
	        	
	        	
	        }
	        

	       
        }catch(Exception e){
        	 System.out.println(cur_counter+" "+new_counter);
        	 pageMessages.add(resword.getString("confirm_crf_version_em_dataextraction"));
            
        }
        request.setAttribute("pageMessages", pageMessages);
        gridMap.addAttribute("rows", rows);
        return gridMap;
    }

    private void buildRecord(ItemBean cur_element, ItemBean new_element, ItemGroupMetadataBean cur_bean_mdata, 
    		ItemGroupMetadataBean new_bean_mdata,ArrayList<String[]> rows){
    	
    	
    	String[] row = new String[8];
    	int cycle_count=0;
    	if (cur_element == null && new_element != null){
    		row[0]=row[1]=row[2]=row[3]=row[7]="";
    		row[4]=(new_bean_mdata.isRepeatingGroup())? new_element.getName()+"(1)":new_element.getName();
    		row[5]=new_element.getOid();
    		row[6]=String.valueOf(new_element.getId());
    		rows.add(row);
    		return;
    	}
    	else if (cur_element != null && new_element == null) {
    		
    		for (ItemDataBean data_item : cur_element.getItemDataElements()){
    			row = new String[8];
    			row[0] = (cur_bean_mdata.isRepeatingGroup())? cur_element.getName()+" ("+data_item.getOrdinal()+")": cur_element.getName();
    			row[1]=		cur_element.getOid();
    			row[2]=		String.valueOf(cur_element.getId());
    			row[3]=		data_item.getValue()	;
    			row[4]=row[6]=row[7]=row[5]="";
    			rows.add(row);
    			cycle_count++;
    			if ( cycle_count >0 && !cur_bean_mdata.isRepeatingGroup()){
    				break;
    			}
    		}
    		return;
    	}
    	else if (cur_element != null && new_element != null){
    		//for repeating groups: 3 cases
    		//one cycle: repeating group item -> none-repeating group item
    		//second cycle -> back none-repeating to prev repeating
    		for (ItemDataBean data_item : cur_element.getItemDataElements()){
    			row = new String[8];
    			if (!cur_bean_mdata.isRepeatingGroup() && cycle_count>0 ){
    				row[0]=row[1]=row[2]=row[3]="";
    			}else{
    				row[0] = (cur_bean_mdata.isRepeatingGroup())? cur_element.getName()+" ("+data_item.getOrdinal()+")": cur_element.getName();
        			row[1]=		cur_element.getOid();
	    			row[2]=		String.valueOf(cur_element.getId());
	    			row[3]=	data_item.getValue()	;
    			}
    			if (new_bean_mdata.isRepeatingGroup()){
    				//case when new one is a repeating group and has data from some previous entry while current does not have a repeating group
    				if (!cur_bean_mdata.isRepeatingGroup()){
		    			row[4] =  cur_element.getName()+" ("+data_item.getOrdinal()+")";
	 				}
    				
    				//new one is repeating & cur is repeating
    				if (cur_bean_mdata.isRepeatingGroup()){
		    			row[4]=row[0];
    				}
    				row[5]=new_element.getOid();
	    			row[6]=String.valueOf(new_element.getId());
	    			row[7]=	data_item.getValue()	;
    			}
    			else{
    				if ( cycle_count == 0){
    					
    					row[4]=row[0];
    	    			row[5]=new_element.getOid();
    	    			row[6]=String.valueOf(new_element.getId());
    	    			row[7]=	data_item.getValue()	;
    				}
    				else{
    					row[4]=row[5]=row[6]=row[7]="";
    				}
    			}
    			cycle_count++;
    			//do not add row if all items empty -> from data of repeat group to none-rep
    			if ( !(row[0].equals("") && row[4].equals(""))){
    				rows.add(row);
    			}
    		}
    		return;
    	}
    	
    	
    	
    }
    
    @RequestMapping("/managestudy/changeCRFVersion")
    // @RequestMapping("/managestudy/changeCRFVersionAction")
    public ModelMap changeCRFVersionAction(HttpServletRequest request,HttpServletResponse response,
    		@RequestParam("crfId") int crfId,
    		@RequestParam("crfName") String crfName,
    		@RequestParam("crfversionId") int crfVersionId,
    		@RequestParam("crfVersionName") String crfVersionName,	
    		@RequestParam("studySubjectLabel") String studySubjectLabel, 
    		@RequestParam("studySubjectId") int studySubjectId,
    		@RequestParam("eventCRFId") int eventCRFId,
    		@RequestParam("eventDefinitionCRFId") int eventDefinitionCRFId,
    		@RequestParam(value="newCRFVersionId" , required = true) int newCRFVersionId  		)
	
    
    {

    	//to be removed for aquamarine
    	  if(!mayProceed(request)){
    		  if( redirect( request, response, "/MainMenu?message=authentication_failed") == null)
    			  return null;
          }
    	  
      
        
        ArrayList<String> pageMessages = (ArrayList<String>) request.getAttribute("pageMessages");
        if (pageMessages == null) {
            pageMessages = new ArrayList<String>();
        }
        request.setAttribute("pageMessages", pageMessages);
        setupResource(request);
//update event_crf_id table
        try{
	        EventCRFDAO event_crf_dao = new EventCRFDAO(dataSource);
	        event_crf_dao.updateCRFVersionID(eventCRFId, newCRFVersionId);
	        //create AuditEvent
	        
	        pageMessages.add(resword.getString("confirm_crf_version_ms"));
	        String msg=resword.getString("confirm_crf_version_ms");
	        redirect( request, response, "/ViewStudySubject?isFromCRFVersionChange="+msg+"&id="+studySubjectId);
        }
        catch (Exception e){
          
        	pageMessages.add(resword.getString("error_message_cannot_update_crf_version"));
        	
        }
        
        
       
        return null;
    }
    @ExceptionHandler(HttpSessionRequiredException.class)
    public String handleSessionRequiredException(HttpSessionRequiredException ex, HttpServletRequest request) {
        return "redirect:/MainMenu";
    }

    @ExceptionHandler(NullPointerException.class)
    public String handleNullPointerException(NullPointerException ex, HttpServletRequest request, HttpServletResponse response) {
        StudyBean currentStudy = (StudyBean) request.getSession().getAttribute("study");
        if (currentStudy == null) {
            return "redirect:/MainMenu";
        }
        throw ex;
    }

    private void setUpSidebar(HttpServletRequest request) {
        if (sidebarInit.getAlertsBoxSetup() == SidebarEnumConstants.OPENALERTS) {
            request.setAttribute("alertsBoxSetup", true);
        }

        if (sidebarInit.getInfoBoxSetup() == SidebarEnumConstants.OPENINFO) {
            request.setAttribute("infoBoxSetup", true);
        }
        if (sidebarInit.getInstructionsBoxSetup() == SidebarEnumConstants.OPENINSTRUCTIONS) {
            request.setAttribute("instructionsBoxSetup", true);
        }

        if (sidebarInit.getEnableIconsBoxSetup() == SidebarEnumConstants.DISABLEICONS) {
            request.setAttribute("enableIconsBoxSetup", false);
        }
      
    }
    //to be depricated in aquamarine
    private boolean mayProceed(HttpServletRequest request) {

        StudyUserRoleBean currentRole = (StudyUserRoleBean)request.getSession().getAttribute("userRole");
        Role r = currentRole.getRole();

        if (r.equals(Role.ADMIN) || r.equals(Role.STUDYDIRECTOR) || r.equals(Role.COORDINATOR)) {
            return true;
        }
        return false;
    }
    
    private Object redirect(HttpServletRequest request,HttpServletResponse response, String location){
    	 try{
                response.sendRedirect(request.getContextPath() + location);
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        
    }
    
    private void resetPanel(HttpServletRequest request){
    	 StudyInfoPanel panel = new StudyInfoPanel();
         panel.reset();
         panel.setIconInfoShown(false);
         request.getSession().setAttribute("panel", panel);
    
    }
    
    private void setupResource(HttpServletRequest request){
    	 Locale locale = request.getLocale();
         ResourceBundleProvider.updateLocale(locale);
         resword = ResourceBundleProvider.getWordsBundle(locale);
         resformat = ResourceBundleProvider.getFormatBundle(locale);
    }
    private String formatDate(Date date){
    	String dateFormat = resformat.getString("date_format_string") ;
    	SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
    	String s = formatter.format(date);
    	return s;
    }
   
}
