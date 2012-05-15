/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2011 Akaza Research
 */

package org.akaza.openclinica.control.admin;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.ResourceBundle;

import org.akaza.openclinica.bean.core.ApplicationConstants;
import org.akaza.openclinica.bean.core.ResponseType;
import org.akaza.openclinica.bean.core.Utils;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.core.util.CrfTemplateColumnNameEnum;

public class SpreadSheetItemUtil {
	
	
	
	
	
	private String item_name;//1
	private String description_label;//2
	private String left_item_text;//3
	private String units;//4
	private String right_item_text;//5
	private String section_label;//6
	private String group_label;//7
	private String header;//8
	private String subheader;//9
	private String parent_item;//10
	private String column_number;//11
	private String page_number;//12
	private String question_number;//13
	private int response_type_id;//14
	private String response_label;//15
	private String response_options_text;//16
	private String[] response_options;
	private String response_values_or_calculations;//17
	private String response_layout;//18
	private String default_value;//19
	private String data_type;//20
	private String width_decimal;//21
	private String validation;//22
	private String validation_error_message;//23
	private String phi;//24
	private String required;//25
	private String item_display_status;//26
	private String simple_conditional_display;//27
	
	
	public SpreadSheetItemUtil(){}
	
	private String cleanProperty(String property){
		property= property.trim();
		return property.replaceAll("<[^>]*>", "");
        
	}
	
	public String getSimple_conditional_display() {
		return simple_conditional_display;
	}
	public void setSimple_conditional_display(String simple_conditional_display) {
		this.simple_conditional_display = simple_conditional_display;
	}
	/**
	 * @return the item_name
	 */
	public String getItemName() {
		return item_name;
	}
	/**
	 * @param item_name the item_name to set
	 */
	public void setItemName(String item_name) {
		item_name = cleanProperty(item_name);
		this.item_name = item_name;
	}
	/**
	 * @return the description_label
	 */
	public String getDescriptionLabel() {
		return description_label;
	}
	/**
	 * @param description_label the description_label to set
	 */
	public void setDescriptionLabel(String description_label) {
		this.description_label = description_label;
	}
	/**
	 * @return the left_item_text
	 */
	public String getLeft_item_text() {
		return left_item_text;
	}
	/**
	 * @param left_item_text the left_item_text to set
	 */
	public void setLeft_item_text(String left_item_text) {
		this.left_item_text = left_item_text;
	}
	/**
	 * @return the units
	 */
	public String getUnits() {
		return units;
	}
	/**
	 * @param units the units to set
	 */
	public void setUnits(String units) {
		this.units = units;
	}
	/**
	 * @return the right_item_text
	 */
	public String getRight_item_text() {
		return right_item_text;
	}
	/**
	 * @param right_item_text the right_item_text to set
	 */
	public void setRight_item_text(String right_item_text) {
		this.right_item_text = right_item_text;
	}
	/**
	 * @return the section_label
	 */
	public String getSectionLabel() {
		return section_label;
	}
	/**
	 * @param section_label the section_label to set
	 */
	public void setSectionLabel(String section_label) {
		this.section_label = cleanProperty(section_label);
       
       
		
	}
	/**
	 * @return the group_label
	 */
	public String getGroupLabel() {
		return group_label;
	}
	/**
	 * @param group_label the group_label to set
	 */
	public void setGroupLabel(String group_label) {
		this.group_label =  cleanProperty(group_label);
	}
	/**
	 * @return the header
	 */
	public String getHeader() {
		return header;
	}
	/**
	 * @param header the header to set
	 */
	public void setHeader(String header) {
		this.header = header;
	}
	/**
	 * @return the subheader
	 */
	public String getSubheader() {
		return subheader;
	}
	/**
	 * @param subheader the subheader to set
	 */
	public void setSubheader(String subheader) {
		this.subheader = subheader;
	}
	/**
	 * @return the parent_item
	 */
	public String getParentItem() {
		return parent_item;
	}
	/**
	 * @param parent_item the parent_item to set
	 */
	public void setParentItem(String parent_item) {
		this.parent_item = cleanProperty(parent_item);
	
	}
	/**
	 * @return the column_number
	 */
	public String getColumn_number() {
		return column_number;
	}
	/**
	 * @param column_number the column_number to set
	 */
	public void setColumn_number(String column_number) {
		this.column_number = column_number;
	}
	/**
	 * @return the page_number
	 */
	public String getPage_number() {
		return page_number;
	}
	/**
	 * @param page_number the page_number to set
	 */
	public void setPage_number(String page_number) {
		this.page_number = page_number;
	}
	/**
	 * @return the question_number
	 */
	public String getQuestion_number() {
		return question_number;
	}
	/**
	 * @param question_number the question_number to set
	 */
	public void setQuestion_number(String question_number) {
		this.question_number = question_number;
	}
	/**
	 * @return the responSe_type
	 */
	public int getResponseTypeId() {
		return response_type_id;
	}
	/**
	 * @param responSe_type the responSe_type to set
	 */
	public void setResponseTypeId(int response_type_id) {
		this.response_type_id = response_type_id;
	}
	
	
	/**
	 * @return the response_label
	 */
	public String getResponse_label() {
		return response_label;
	}
	/**
	 * @param response_label the response_label to set
	 */
	public void setResponse_label(String response_label) {
		this.response_label = response_label;
	}
	/**
	 * @return the response_options_text
	 */
	public String getResponse_options_text() {
		return response_options_text;
	}
	/**
	 * @param response_options_text the response_options_text to set
	 */
	public void setResponse_options_text(String response_options_text) {
		this.response_options_text = response_options_text;
	}
	/**
	 * @return the response_values_or_calculations
	 */
	public String getResponse_values_or_calculations() {
		return response_values_or_calculations;
	}
	/**
	 * @param response_values_or_calculations the response_values_or_calculations to set
	 */
	public void setResponse_values_or_calculations(
			String response_values_or_calculations) {
		this.response_values_or_calculations = response_values_or_calculations;
	}
	/**
	 * @return the response_layout
	 */
	public String getResponse_layout() {
		return response_layout;
	}
	/**
	 * @param response_layout the response_layout to set
	 */
	public void setResponse_layout(String response_layout) {
		this.response_layout = response_layout;
	}
	/**
	 * @return the default_value
	 */
	public String getDefaultValue() {
		return default_value;
	}
	/**
	 * @param default_value the default_value to set
	 */
	public void setDefaultValue(String default_value) {
		this.default_value = cleanProperty(default_value);
	}
	/**
	 * @return the data_type
	 */
	public String getDataType() {
		return data_type;
	}
	/**
	 * @param data_type the data_type to set
	 */
	public void setDataType(String data_type) {
		this.data_type = cleanProperty(data_type);
	}
	/**
	 * @return the width_decimal
	 */
	public String getWidth_decimal() {
		return width_decimal;
	}
	/**
	 * @param width_decimal the width_decimal to set
	 */
	public void setWidth_decimal(String width_decimal) {
		this.width_decimal = width_decimal;
	}
	/**
	 * @return the validation
	 */
	public String getValidation() {
		return validation;
	}
	/**
	 * @param validation the validation to set
	 */
	public void setValidation(String validation) {
		this.validation = validation;
	}
	/**
	 * @return the validation_error_message
	 */
	public String getValidation_error_message() {
		return validation_error_message;
	}
	/**
	 * @param validation_error_message the validation_error_message to set
	 */
	public void setValidation_error_message(String validation_error_message) {
		this.validation_error_message = validation_error_message;
	}
	/**
	 * @return the phi
	 */
	public String getPhi() {
		return phi;
	}
	/**
	 * @param phi the phi to set
	 */
	public void setPhi(String phi) {
		this.phi = phi;
	}
	/**
	 * @return the required
	 */
	public String getRequired() {
		return required;
	}
	/**
	 * @param required the required to set
	 */
	public void setRequired(String required) {
		this.required = required;
	}
	/**
	 * @return the item_display_status
	 */
	public String getItem_display_status() {
		return item_display_status;
	}
	/**
	 * @param item_display_status the item_display_status to set
	 */
	public void setItem_display_status(String item_display_status) {
		this.item_display_status = item_display_status;
	}

	/*
	 * for itemName (column 0
	 */
	public static boolean isItemWithSameParameterExists(
			String column_value, ArrayList< SpreadSheetItemUtil> row_items){
		SpreadSheetItemUtil item =  isItemWithSameParameterExists(CrfTemplateColumnNameEnum.ITEM_NAME, column_value, row_items);
		return (item==null);
	}
	public static SpreadSheetItemUtil isItemWithSameParameterExists(CrfTemplateColumnNameEnum param_column_index, 
			String column_value, ArrayList< SpreadSheetItemUtil> row_items){
		for (SpreadSheetItemUtil cur_item : row_items ){
			switch (param_column_index){
			case ITEM_NAME:{
				if ( cur_item.getItemName().equals(column_value)){
					return cur_item;
				}
				break;
			}
			
			}
		}
		return null;
		
	}
	//TODO if we ever go to normal OO parsing of spreadsheet this method should be moved to 
	//SpredSheetGroupUtil
	//the problem here that Group now can be ungrouped group
	public static void versifySectionGroupPlacementForItems(ArrayList< SpreadSheetItemUtil> row_items,
			ArrayList<String> ver_errors, HashMap<String,String> htmlErrors,
			int sheetNumber,
			ResourceBundle resPageMsg,
			HashMap<String, ItemGroupBean> itemGroups){
		HashMap <String,String> group_section_map = new HashMap <String,String>();
		String section_label;
		int row_number=1;
		for (SpreadSheetItemUtil cur_item : row_items){
			row_number++;
			if ( cur_item.getGroupLabel() == null || cur_item.getGroupLabel().length()<1 ){
				continue;
			}
			//verify that this is repeating group
			ItemGroupBean item_group = itemGroups.get(cur_item.getGroupLabel());
			boolean isRepeatingGroup=item_group.getMeta().isRepeatingGroup();
			if (!isRepeatingGroup){
				continue;
			}
			section_label = group_section_map.get(cur_item.getGroupLabel());
			if (section_label != null){//not first item in group
				if (! section_label.equals(cur_item.getSectionLabel())){//error: items of one group belong to more than one section
					ver_errors.add(resPageMsg.getString("group_in_several_sections") + cur_item.getGroupLabel()+"'.");
		            htmlErrors.put(sheetNumber + "," + (row_number-1) + ","+CrfTemplateColumnNameEnum.GROUP_LABEL.getCellNumber(), 
		            		resPageMsg.getString("INVALID_VALUE") );
		  
				}
			}else{//first item in group
				group_section_map.put(cur_item.getGroupLabel(), cur_item.getSectionLabel());
			}
			
			
		}
	}
	////////////////////////////////////// verification rules
	public void verifyParentID(ArrayList< SpreadSheetItemUtil> row_items, ArrayList<String> ver_errors, 
			HashMap<String,String> htmlErrors,  int sheetNumber, ResourceBundle resPageMsg,
			HashMap<String, ItemGroupBean> itemGroups){
		
		int row_number = row_items.size();
        // BWP>>Prevent parent names that equal the Item names 
        if (this.getItemName() != null && this.getItemName().equalsIgnoreCase(this.getParentItem())) {
        	this.setParentItem( "");
        	
        }
        
        if(!StringUtil.isBlank(this.getParentItem())){
        	SpreadSheetItemUtil cur_item = SpreadSheetItemUtil.isItemWithSameParameterExists(CrfTemplateColumnNameEnum.ITEM_NAME, 
        			this.getParentItem(),  row_items);
        	// Checking for a valid parent item name
            if(cur_item == null){
            	ver_errors.add(resPageMsg.getString("parent_id")+row_number+resPageMsg.getString("parent_id_1"));
            	 htmlErrors.put(sheetNumber + "," + row_number + ","+CrfTemplateColumnNameEnum.PARENT_ITEM.getCellNumber(), resPageMsg.getString("INVALID_FIELD"));
            	 
            }
            //prevent more than one level of hierarchy for parent names (new ver)
            if ( cur_item != null && cur_item.getParentItem() != null && cur_item.getParentItem().length()>0){
            	ver_errors.add(resPageMsg.getString("nested_parent_id")+row_items.size()+resPageMsg.getString("nested_parent_id_1"));
            	 htmlErrors.put(sheetNumber + "," + row_number + ","+CrfTemplateColumnNameEnum.PARENT_ITEM.getCellNumber(), resPageMsg.getString("INVALID_FIELD"));   
            }
            //prevent item in RGroup to have parent id (new ver)
          //verify that this is repeating group
            if ( itemGroups != null && itemGroups.size() > 0){
				ItemGroupBean item_group = itemGroups.get(this.getGroupLabel());
				if ( item_group != null ){
					boolean isRepeatingGroup=item_group.getMeta().isRepeatingGroup();
					if ( isRepeatingGroup){
			            if ( this.getGroupLabel()!= null && this.getGroupLabel().length()>0){
			            	ver_errors.add(resPageMsg.getString("parentId_group")+row_items.size()+resPageMsg.getString("nested_parent_id_1"));
			            	 htmlErrors.put(sheetNumber + "," + row_number + ","+CrfTemplateColumnNameEnum.PARENT_ITEM.getCellNumber(), resPageMsg.getString("INVALID_FIELD"));
			            }
					}
				}
            }
        }
        
	}
	
	public void verifySectionLabel(ArrayList< SpreadSheetItemUtil> row_items, 
			ArrayList<String> ver_errors, ArrayList<String> secNames,
			HashMap<String,String> htmlErrors, int sheetNumber,  ResourceBundle resPageMsg){
		int row_number=row_items.size();
		StringBuffer str= new StringBuffer();
		if ( this.getSectionLabel() == null || this.getSectionLabel().length()==0){
			str.append(resPageMsg.getString("the") + " ");
			str.append( resPageMsg.getString("SECTION_LABEL_column") + " ");
			str.append( resPageMsg.getString("not_valid_section_at_row") + " ");
			str.append( row_number + ", " + resPageMsg.getString("items_worksheet_with_dot")) ;
			str.append(" "+ resPageMsg.getString("check_to_see_that_there_is_valid_LABEL"));
			ver_errors.add(str.toString());
            htmlErrors.put(sheetNumber + "," + row_number + ","+ CrfTemplateColumnNameEnum.SECTION_LABEL.getCellNumber(), 
            		resPageMsg.getString("NOT_A_VALID_LABEL"));
       }
		
       if (this.getSectionLabel() != null && this.getSectionLabel().length() > 2000) {
    	   ver_errors.add(resPageMsg.getString("section_label_length_error"));
    	   htmlErrors.put(sheetNumber + "," + row_number + ","+ CrfTemplateColumnNameEnum.SECTION_LABEL.getCellNumber(), 
           		resPageMsg.getString("NOT_A_VALID_LABEL"));
       }

		if (!secNames.contains(this.getSectionLabel())) {
			if ( str.length()==0){
				str.append(resPageMsg.getString("the") + " ");
				str.append( resPageMsg.getString("SECTION_LABEL_column") + " ");
				str.append( resPageMsg.getString("not_valid_section_at_row") + " ");
				str.append( row_number + ", " + resPageMsg.getString("items_worksheet_with_dot")) ;
				str.append(" "+ resPageMsg.getString("check_to_see_that_there_is_valid_LABEL"));
			}
			ver_errors.add(str.toString());
	            
			htmlErrors.put(sheetNumber + "," + row_number + ","+ CrfTemplateColumnNameEnum.SECTION_LABEL.getCellNumber(),
					resPageMsg.getString("NOT_A_VALID_LABEL"));
	     }

	}
	public void verifyItemName(ArrayList< SpreadSheetItemUtil> row_items, ArrayList<String> ver_errors, 
			HashMap<String,String> htmlErrors, int sheetNumber,  ResourceBundle resPageMsg){
		
		int k = row_items.size();
		String itemName = this.getItemName();
		  // regexp to make sure it is all word characters, '\w+' in regexp terms
        if (!Utils.isMatchingRegexp(itemName, "\\w+")) {
            // different item error to go here
        	ver_errors.add(resPageMsg.getString("item_name_column") + " " + resPageMsg.getString("was_invalid_at_row") + " " + k + ", "
                    + resPageMsg.getString("items_worksheet_with_dot")  + " "+resPageMsg.getString("you_can_only_use_letters_or_numbers"));
                htmlErrors.put(sheetNumber + "," + k + ",0", resPageMsg.getString("INVALID_FIELD"));
        }
        if (StringUtil.isBlank(itemName)) {
        	ver_errors.add(resPageMsg.getString("the") + " " + resPageMsg.getString("item_name_column") + " "
                + resPageMsg.getString("was_blank_at_row") + " "+k + ", " + resPageMsg.getString("items_worksheet_with_dot") );
            htmlErrors.put(sheetNumber + "," + k + ","+ CrfTemplateColumnNameEnum.ITEM_NAME.getCellNumber()
                , resPageMsg.getString("required_field"));
        }
        if (itemName != null && itemName.length() > 255) {
        	ver_errors.add(resPageMsg.getString("item_name_length_error"));
        }

        if (SpreadSheetItemUtil.isItemWithSameParameterExists(itemName, row_items)) {
            // errors.add("A duplicate ITEM_NAME of " + itemName
            // + " was detected at row " + k
            // + ", Items worksheet.");
        	ver_errors.add(resPageMsg.getString("duplicate") + " " + resPageMsg.getString("item_name_column") + " " + itemName + " "
                + resPageMsg.getString("was_detected_at_row") + " " + k + ", " + resPageMsg.getString("items_worksheet_with_dot") );
            htmlErrors.put(sheetNumber + "," + k + ","+CrfTemplateColumnNameEnum.ITEM_NAME.getCellNumber(),
            		resPageMsg.getString("required_field"));
        }
		
	}

	public void verifyDefaultValue( ArrayList< SpreadSheetItemUtil> row_items,
			ArrayList<String> ver_errors, 
			HashMap<String,String> htmlErrors, int sheetNumber,  ResourceBundle resPageMsg){
		
		int row_number = row_items.size();
		
		 if ("date".equalsIgnoreCase(this.getDataType()) && !"".equals(this.getDefaultValue())) {
             // BWP>> try block needs to be added, because
             // cell.getDateCellValue()
             // can throw an exception.
             // All database values are stored in this format? en
             // Locale MM/dd/yyyy
             try {
            	 this.setDefaultValue( new SimpleDateFormat(ApplicationConstants.getDateFormatInItemData()).format(this.getDefaultValue()));
             } catch (Exception e) {
            	 this.setDefaultValue("");
            	 //TODO raise exception
             }
         }
         if (this.getDefaultValue().length() > 0) {
             if(this.getResponseTypeId() == ResponseType.CALCULATION.getId()
                 || this.getResponseTypeId() == ResponseType.GROUP_CALCULATION.getId()
                 || this.getResponseTypeId() == ResponseType.FILE.getId()
                 || this.getResponseTypeId() == ResponseType.INSTANT_CALCULATION.getId()) {
            	 ver_errors.add(resPageMsg.getString("default_value_not_allowed") + this.getItemName() +" "+resPageMsg.getString("change_radio") + " "+resPageMsg.getString("items_worksheet_with_dot"));
                 htmlErrors.put(sheetNumber + "," + row_number + ","+CrfTemplateColumnNameEnum.DEFAULT_VALUE.getCellNumber()
            	 , resPageMsg.getString("INVALID_FIELD"));
             } 
            	 
             //do not allow more than one value as a default value, value should be from response types
             else if(this.getResponseTypeId() == ResponseType.SELECT.getId()) {
            	 if( this.getDefaultValue().indexOf(',')!=-1){
            		 ver_errors.add(resPageMsg.getString("default_value_wrong_select") + row_number + ", " + resPageMsg.getString("items_worksheet_with_dot"));
                     htmlErrors.put(sheetNumber + "," + row_number + ","+CrfTemplateColumnNameEnum.DEFAULT_VALUE.getCellNumber()
                	 , resPageMsg.getString("INVALID_FIELD"));
            	 }
            	 //more logic should be here: current implementation supports DEFAULT_LABEL, several non - in-response values
//            	 if (response_options!=null && response_options.length>0){
//            		 boolean flagDefaultValueVerified=false;
//            		 for (String cur_option: response_options){
//            			 if (cur_option.trim().equals(this.getDefaultValue())){
//            				 flagDefaultValueVerified=true;
//            				 break;
//            			 }
//            		 }
//            		 if (!flagDefaultValueVerified){
//            			 ver_errors.add(resPageMsg.getString("default_value_wrong_select_1") + row_number + ", " + resPageMsg.getString("items_worksheet_with_dot"));
//                         htmlErrors.put(sheetNumber + "," + row_number + ","+CrfTemplateColumnNameEnum.DEFAULT_VALUE.getCellNumber()
//                    	 , resPageMsg.getString("INVALID_FIELD"));
//            		 }
//            	 }
            	 
             }
             else if(this.getResponseTypeId() == ResponseType.CHECKBOX.getId()||
            		 this.getResponseTypeId() == ResponseType.SELECTMULTI.getId()){
            	 //TODO
             }
           
         }
	}
	

	/**
	 * @return the response_options
	 */
	public String[] getResponseOptions() {
		return response_options;
	}

	/**
	 * @param response_options the response_options to set
	 */
	public void setResponseOptions(String[] response_options) {
		this.response_options = response_options;
	}
	
}
