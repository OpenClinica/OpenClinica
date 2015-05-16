package org.akaza.openclinica.service;

	 
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class jobTriggering {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

 //   @Scheduled(cron="0 0/1 * * * ?")
    public void reportCurrentTime() {
        System.out.println("The time is now " + dateFormat.format(new Date()));
    
/*        Calendar calendar = new GregorianCalendar();
        TimeZone timeZone = calendar.getTimeZone();
        System.out.println("TimeZone:  " + timeZone.getID());
        System.out.println("TimeZone:  " + timeZone.getDisplayName());
        System.out.println("TimeZone:  " + timeZone.getDSTSavings());
        System.out.println("TimeZone:  " + timeZone.getRawOffset());
        TimeZone tZone = TimeZone.getDefault();
        System.out.println("TimeZone:  " + tZone.getID());
*/        
    
    }
}