// default package
// Generated Jul 31, 2013 2:03:33 PM by Hibernate Tools 3.4.0.CR1
package org.akaza.openclinica.domain.datamap;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

import org.akaza.openclinica.domain.enumsupport.CodedEnum;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;

/**
 * @author jnyayapathi
 *
 */

public enum SubjectEventStatus  implements CodedEnum {

	INVALID(0, "invalid"), SCHEDULED(1, "scheduled"), NOT_SCHEDULED(2, "not_scheduled"), DATA_ENTRY_STARTED(3, "data_entry_started"), COMPLETED(4, "completed"), STOPPED(5, "stopped"), SKIPPED(6, "skipped"),
	LOCKED(7, "locked"), SIGNED(8, "signed");

	
	SubjectEventStatus(){
		
	}
	SubjectEventStatus(int code){
		this(code,null);
	}
	
	SubjectEventStatus(int code,String description)
	{
		this.code = code;
		this.description = description;
		
	}
	private int code;
	private String description;
	
	
	  
	    public static SubjectEventStatus getByCode(Integer code) {
	        HashMap<Integer, SubjectEventStatus> enumObjects = new HashMap<Integer, SubjectEventStatus>();
	        for (SubjectEventStatus theEnum : SubjectEventStatus.values()) {
	            enumObjects.put(theEnum.getCode(), theEnum);
	        }
	        return enumObjects.get(Integer.valueOf(code));
	    }
	    
	    
	    public static SubjectEventStatus getByI18nDescription(String i18nDescription, Locale locale) {
	        for (SubjectEventStatus theEnum : SubjectEventStatus.values()) {
	            if(i18nDescription.equals(theEnum.getI18nDescription(locale))) {
	                return theEnum;
	            }
	        }
	        return null;
	    }
	    
	    public String getI18nDescription(Locale locale) {
	        if (!"".equals(this.description)) {
	            ResourceBundle resterm = ResourceBundleProvider.getTermsBundle(locale);
	            String des = resterm.getString(this.description);
	            if(des != null) {
	                return des.trim();
	            }  else {
	                return "";
	            }
	        } else {
	            return this.description;
	        }
	    }
	@Override
    public String toString() {
        ResourceBundle resterm = ResourceBundleProvider.getTermsBundle();
        return resterm.getString(getDescription());
    }
	 public static SubjectEventStatus getByName(String name) {
	        return SubjectEventStatus.valueOf(SubjectEventStatus.class, name);
	    }
		  public String getName() {
	        return this.name();
	    }

	    public Integer getCode() {
	        return code;
	    }

	    public String getDescription() {
	        return description;
	    }


}
