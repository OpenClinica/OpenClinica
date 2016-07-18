package org.akaza.openclinica.ws;


import org.akaza.openclinica.exception.OpenClinicaSystemException;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateAdapter extends XmlAdapter<String, Date> {

    // the desired format
    private String pattern = "MM/dd/yyyy";
    
    public String marshal(Date date) throws Exception {
        return new SimpleDateFormat(pattern).format(date);
    }
    
    public Date unmarshal(String dateString) throws Exception {
        throw new OpenClinicaSystemException("Please implement me!!");
    } 
    
}

