package org.akaza.openclinica.control.admin;

import java.util.ArrayList;
import java.util.ResourceBundle;

import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.core.util.CrfTemplateColumnName;

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
	private String response_type;//14
	private String response_label;//15
	private String response_options_text;//16
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
	public String getSection_label() {
		return section_label;
	}
	/**
	 * @param section_label the section_label to set
	 */
	public void setSection_label(String section_label) {
		this.section_label = section_label;
	}
	/**
	 * @return the group_label
	 */
	public String getGroup_label() {
		return group_label;
	}
	/**
	 * @param group_label the group_label to set
	 */
	public void setGroup_label(String group_label) {
		this.group_label = group_label;
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
	public String getResponSe_type() {
		return response_type;
	}
	/**
	 * @param responSe_type the responSe_type to set
	 */
	public void setResponSe_type(String responSe_type) {
		this.response_type = responSe_type;
	}
	/**
	 * @return the response_type
	 */
	public String getResponse_type() {
		return response_type;
	}
	/**
	 * @param response_type the response_type to set
	 */
	public void setResponse_type(String response_type) {
		this.response_type = response_type;
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
	public String getDefault_value() {
		return default_value;
	}
	/**
	 * @param default_value the default_value to set
	 */
	public void setDefault_value(String default_value) {
		this.default_value = default_value;
	}
	/**
	 * @return the data_type
	 */
	public String getData_type() {
		return data_type;
	}
	/**
	 * @param data_type the data_type to set
	 */
	public void setData_type(String data_type) {
		this.data_type = data_type;
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
		SpreadSheetItemUtil item =  isItemWithSameParameterExists(CrfTemplateColumnName.ITEM_NAME, column_value, row_items);
		return (item==null);
	}
	public static SpreadSheetItemUtil isItemWithSameParameterExists(CrfTemplateColumnName param_column_index, 
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
	
	////////////////////////////////////// verification rules
	@SuppressWarnings("deprecation")
	public ArrayList<String> verifyParentID(ArrayList< SpreadSheetItemUtil> row_items, ResourceBundle resPageMsg){
		ArrayList<String> ver_errors= new ArrayList<String>();
		
        // BWP>>Prevent parent names that equal the Item names
        if (this.getItemName() != null && this.getItemName().equalsIgnoreCase(this.getParentItem())) {
        	this.setParentItem( "");
        	return ver_errors;
        }
        
        if(!StringUtil.isBlank(this.getParentItem())){
        	SpreadSheetItemUtil cur_item = SpreadSheetItemUtil.isItemWithSameParameterExists(CrfTemplateColumnName.ITEM_NAME, 
        			this.getParentItem(),  row_items);
        	// Checking for a valid parent item name
            if(cur_item == null){
            	ver_errors.add(resPageMsg.getString("parent_id")+row_items.size()+resPageMsg.getString("parent_id_1"));
            }
            //prevent more than one level of hierarchy for parent names 
            if ( cur_item.getParentItem() != null){
            	ver_errors.add(resPageMsg.getString("nested_parent_id")+row_items.size()+resPageMsg.getString("nested_parent_id_1"));
            	   
            }
        }
        
        
		return ver_errors;
	}
}
