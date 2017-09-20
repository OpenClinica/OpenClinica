/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2007 Akaza Research
 */

package org.akaza.openclinica.bean.core;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;

public class Utils {

    private static Utils ref;

    private Utils() {
    }

    public static Utils getInstacne() {
        if (ref == null) {
            ref = new Utils();
        }
        return ref;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * This Method will compare the two Dates and return a String with number of years , weeks and days.
     * 
     * @author Krikor Krumlian 10/20/2006
     * @param eventStartDate
     *            The event start date
     * @param subjectDOB
     *            the Subject's date of birth
     * @return
     */
    public String processAge(Date eventStartDate, Date subjectDOB) {
        int years = 0, months = 0, days = 0;
        String ret = "";

        if (eventStartDate == null || subjectDOB == null) {
            return "N/A";
        }

        // example : 10/20/2006
        Calendar eventsd = Calendar.getInstance();
        eventsd.setTime(eventStartDate);
        long init = eventsd.getTimeInMillis();

        // example : 10/20/1990
        Calendar dob = Calendar.getInstance();
        dob.setTime(subjectDOB);
        long latter = dob.getTimeInMillis();

        // logger.info("<<< event start date: "+eventsd.toString());
        // logger.info("<<< subject birth date: "+dob.toString());
        long difference = Math.abs(init - latter);
        double daysDifference = Math.floor(difference / 1000 / 60 / 60 / 24);
        // logger.info("<<< found age, days difference "+daysDifference);

        if (daysDifference > 200 * 365.24) {
            return "N/A";
            // year is probably set to 0001, in which case DOB was not used but
            // is now
        }

        // Get the number of years
        while (daysDifference - 365.24 > 0) {
            daysDifference = daysDifference - 365.24;
            years++;
        }

        // Get the number of months
        while (daysDifference - 30.43 > 0) {
            daysDifference = daysDifference - 30.43;
            months++;
        }

        // Get the number of days
        while (daysDifference - 1 >= 0) {
            daysDifference = daysDifference - 1;
            days++;
            // was off by one day, hope this fixes it, tbh 102007
        }
        ResourceBundle reswords = ResourceBundleProvider.getWordsBundle();
        if (years > 0)
            ret = ret + years + " " + reswords.getString("Years") + " - ";
        if (months > 0)
            ret = ret + months + " " + reswords.getString("Months") + " - ";
        if (days > 0)
            ret = ret + days + " " + reswords.getString("Days");
        // also changed the above, tbh 10 2007
        if (ret.equals(""))
            ret = reswords.getString("Less_than_a_day");
        return ret;

    }

    /**
     * Convert string with from_pattern to string with to_pattern. Use SimpleDateFormat with no Locale parameter.
     * 
     * @param itemValue
     * @param from_pattern
     * @param to_pattern
     * @return
     */
    // ywang 12-06-2007
    public static String convertedItemDateValue(String itemValue, String from_pattern, String to_pattern) {
        return resolveItemDateValue(itemValue, from_pattern, to_pattern, null);
    }

    public static String convertedItemDateValue(String itemValue, String from_pattern, String to_pattern, Locale locale) {
        return resolveItemDateValue(itemValue, from_pattern, to_pattern, locale);
    }

    private static String resolveItemDateValue(String itemValue, String from_pattern, String to_pattern, Locale locale) {
        SimpleDateFormat sdf_from;
        SimpleDateFormat sdf_to;
        if (locale == null) {
            sdf_from = new SimpleDateFormat(from_pattern);
            sdf_to = new SimpleDateFormat(to_pattern);
        } else {
            sdf_from = new SimpleDateFormat(from_pattern, locale);
            sdf_to = new SimpleDateFormat(to_pattern, locale);
        }
        String temp = itemValue == null ? null : itemValue.trim();
        if (itemValue != null && temp.length() > 4) {
            sdf_from.setLenient(false);
            try {
                java.util.Date date = sdf_from.parse(itemValue);
                return sdf_to.format(date);
            } catch (ParseException fe) {
                return itemValue;
            }
        } else {
            return itemValue;
        }
    }

    /**
     * Zip StringBuffer to a file
     * 
     * @param fileName
     * @param filePath
     * @param content
     * 
     */
    // ywang (07-2008)
    public static boolean createZipFile(String fileName, String filePath, StringBuffer content) {
        try {
            File dir = new File(filePath);
            if (!dir.isDirectory()) {
                dir.mkdirs();
            }
            ZipOutputStream z = new ZipOutputStream(new FileOutputStream(new File(dir, fileName + ".zip")));
            z.putNextEntry(new ZipEntry(fileName));
            byte[] bytes = content.toString().getBytes();
            z.write(bytes, 0, bytes.length);
            z.closeEntry();
            z.finish();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * age = the_year_of_controlDate - the_year_of_birthDate
     * 
     * @param birthDate
     * @param controlDate
     * @return
     */
    public static Integer getAge(Date birthDate, Date controlDate) {
        Integer age = -1;
        if (birthDate.before(controlDate)) {
            Calendar dateOfBirth = Calendar.getInstance();
            dateOfBirth.setTime(birthDate);
            Calendar theDate = Calendar.getInstance();
            theDate.setTime(controlDate);
            age = theDate.get(Calendar.YEAR) - dateOfBirth.get(Calendar.YEAR);
            Calendar today = Calendar.getInstance();
            // add the age to the year to see if it's happened yet
            dateOfBirth.add(Calendar.YEAR, age);
            // subtract one from the age if the birthday hasn't happened yet
            if (today.before(dateOfBirth)) {
                age--;
            }
        }
        return age;
    }

    public static String getAttachedFilePath(StudyBean study) {
        String attachedFilePath = CoreResources.getField("attached_file_location");
        // @pgawade 15-April-2011: issue #8682
        if (attachedFilePath == null || attachedFilePath.length() <= 0) {
            // attachedFilePath = CoreResources.getField("filePath") +
            // "attached_files" + File.separator + study.getIdentifier() +
            // File.separator;
            attachedFilePath = CoreResources.getField("filePath") + "attached_files" + File.separator + study.getOid() + File.separator;
        } else {
            // attachedFilePath += study.getIdentifier() + File.separator;
            attachedFilePath += study.getOid() + File.separator;
        }
        return attachedFilePath;
    }

    public static String getCrfMediaSysPathOrig() {
        String attachedFilePath = CoreResources.getField("attached_file_location");
        if (attachedFilePath == null || attachedFilePath.length() <= 0) {
            attachedFilePath = CoreResources.getField("filePath") + "attached_files" + File.separator;
        }
        return attachedFilePath;
    }

    public static String getFilePath() {
        return CoreResources.getField("filePath");
    }

    public static String getCrfMediaPath(String studyOid, String crfOid, String formLayoutOid) {
        return "crf" + File.separator + studyOid + File.separator + crfOid + File.separator + formLayoutOid + File.separator;
    }

    public static String getCrfMediaFilePathOrig(String crfOid, String formLayoutOid) {
        String attachedFilePath = CoreResources.getField("attached_file_location");
        String filePath = crfOid + File.separator + formLayoutOid + File.separator;
        if (attachedFilePath == null || attachedFilePath.length() <= 0) {
            attachedFilePath = CoreResources.getField("filePath") + "attached_files" + File.separator + crfOid + File.separator + formLayoutOid
                    + File.separator;
        } else {
            attachedFilePath += filePath;
        }
        return attachedFilePath;
    }

    public static String getCrfMediaFilePathWithoutSysPathOrig(String crfOid, String formLayoutOid) {
        String filePath = crfOid + File.separator + formLayoutOid + File.separator;
        return filePath;
    }

    public static String getAttachedFileRootPath() {
        String rootPath = CoreResources.getField("attached_file_location");
        if (rootPath == null || rootPath.length() <= 0) {
            rootPath = CoreResources.getField("filePath") + "attached_files" + File.separator;
        }
        return rootPath;
    }

    /*
     * see if a regular expression fits
     * 
     * @author thickerson August 5th 2010
     */
    public static boolean isMatchingRegexp(String input, String regexp) {
        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(input);
        return m.matches();
    }

    public static boolean isWithinRegexp(String input, String regexp) {
        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(input);
        return m.find();
    }
}
