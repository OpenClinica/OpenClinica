package org.akaza.openclinica.controller;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

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
        
         
        //from event_crf get 
        CRFDAO cdao = new CRFDAO(dataSource);
        CRFBean crfBean = (CRFBean)cdao.findByPK(crfId);
        CRFVersionDAO crfVersionDao = new CRFVersionDAO(dataSource);
        ArrayList<CRFVersionBean> versions = (ArrayList<CRFVersionBean>) crfVersionDao.findAllActiveByCRF(crfId);
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
      	request.setAttribute("selectedVersionName",selectedVersionName.trim());
      	
        ModelMap gridMap = new ModelMap();
        ArrayList<String> pageMessages = (ArrayList<String>) request.getAttribute("pageMessages");
        if (pageMessages == null) {
            pageMessages = new ArrayList<String>();
        }
        if ( selectedVersionId == -1){
        	String errorMessage = "Please select CRF version number";
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
        try{
        	ItemDAO item_dao = new ItemDAO(dataSource);
        	ItemDataBean d_bean = null;
	        ItemFormMetadataDAO item_form_mdata = new ItemFormMetadataDAO(dataSource);
	        ArrayList<ItemBean> cur_items = item_dao.findAllWithItemDataByCRFVersionId(eventCRFId);
	        ArrayList<ItemBean> new_items = item_dao.findAllItemsByVersionId(selectedVersionId);
	       
	        int cur_counter = 0; int new_counter = 0;
	        ItemBean cur_element=null; ItemBean new_element=null;
	        while ( true){
	        	if (cur_counter >= (cur_items.size()-1) && new_counter >= (new_items.size()-1 )){break;}
	        	
	        	if ( cur_counter< cur_items.size()){
	        		cur_element = cur_items.get(cur_counter);
	        	}
	        	if (new_counter < new_items.size()){
	        		new_element = new_items.get(new_counter);
	        	}
	        	if ( cur_counter >= (cur_items.size()-1) && new_counter < (new_items.size()-1 )){
	        		 new_counter++;
	        		String[]row = {"","","",new_element.getName(),	new_element.getOid(),""};
        			rows.add(row);
	        	}
	        	if ( cur_counter < (cur_items.size()-1) && new_counter >= (new_items.size()-1 )){
	        		cur_counter ++; 
	        		for (ItemDataBean data_item : cur_element.getItemDataElements()){
	        			String[]row = {cur_element.getName()+" ("+data_item.getOrdinal()+")",
	        					cur_element.getOid(),
	        					data_item.getValue(),
        						"","",""};
	        			rows.add(row);
	        		}
	        	}
	        	if ( new_element != null && cur_element != null &&  new_element.getId() == cur_element.getId()){
	        		cur_counter ++;  new_counter++;
	        		for (ItemDataBean data_item : cur_element.getItemDataElements()){
	        			String[]row = {cur_element.getName()+" ("+data_item.getOrdinal()+")",
	        					cur_element.getOid(),
	        					data_item.getValue(),
        						cur_element.getName()+" ("+data_item.getOrdinal()+")",
        						new_element.getOid(),
        						data_item.getValue()};
	        			rows.add(row);
	        		}
	        		
	        	}
	        	else if ( new_element != null && cur_element != null && new_element.getId() < cur_element.getId()){
	        		 new_counter++;
	        		String[]row = {"","","",new_element.getName(),	new_element.getOid(),""};
        			rows.add(row);
	        	}
	        	else if ( new_element != null && cur_element != null && new_element.getId() > cur_element.getId()){
	        		cur_counter ++; 
	        		for (ItemDataBean data_item : cur_element.getItemDataElements()){
	        			String[]row = {cur_element.getName()+" ("+data_item.getOrdinal()+")",
	        					cur_element.getOid(),
	        					data_item.getValue(),
        						"","",""};
	        			rows.add(row);
	        		}
	        	}
	        	
	        }
	        

//	        for ( ItemBean item: cur_items){
//	        	field_id = new Integer(item.getId());
//	        	for ( int count = 0; count < item.getItemDataElements().size(); count++){
//	        		d_bean = item.getItemDataElements().get(count);
//	        		String[]row = {"","","","","",""};
//	        		rows.add(row);
//	        		row[0]= item.getName()+" ("+d_bean.getOrdinal()+")";//item name + ordinal
//	        		row[1]= item.getOid();//item oid
//	        		row[2]= d_bean.getValue(); //value
//	        		
//	        		if ( new_items_hash.get( field_id) != null){
//	        			row[5]=row[2];
//	        			row[3]=new_items_hash.get( field_id).getName();
//	        			row[4]=new_items_hash.get( field_id).getOid();
//	        		}
//	        		else{
//	        			row[3]=row[4]=row[5]="";
//	        		}
//	        	}
//	        }
//	       
//	        for ( ItemBean item: cur_items){
//	        	field_id = new Integer(item.getId());
//	        	if ( new_items_hash.get( field_id) != null){
//		        	for ( String[] cur_row : rows){
//		        		if ( cur_row[1].equals(item.getOid())){
//		        			cur_row[5]=cur_row[2];
//		        			cur_row[3]=new_items_hash.get( field_id).getName();
//		        			cur_row[4]=new_items_hash.get( field_id).getOid();
//		        		}
//		        	}
//	        	}
//	        	else{//new items
//	        		row = new String[6];
//	        		rows.add(row);
//	        		row[0]=row[1]=row[2]=row[5]="";
//	        		row[3]=item.getName();
//	        		row[4] = item.getOid();
//	        		
//	        	}
//	        }		
	        
        }catch(Exception e){
        	 pageMessages.add("Can not extract data for the 'Change CRF version' action.");
            
        }
        request.setAttribute("pageMessages", pageMessages);
        gridMap.addAttribute("rows", rows);
        return gridMap;
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
//update event_crf_id table
        try{
	        EventCRFDAO event_crf_dao = new EventCRFDAO(dataSource);
	        event_crf_dao.updateCRFVersionID(eventCRFId, newCRFVersionId);
	        //create AuditEvent
	        
	        
	        redirect( request, response, "/ViewStudySubject?id="+studySubjectId);
        }
        catch (Exception e){
        	ResourceBundleProvider.updateLocale(new Locale("en_US"));
             
        	String erm = ResourceBundleProvider.getPageMessagesBundle().getString("error_message_cannot_update_crf_version");
        	pageMessages.add("error_message_cannot_update_crf_version");
        	
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
   
}
