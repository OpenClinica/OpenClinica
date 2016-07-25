/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.web;

import org.akaza.openclinica.view.Page;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.admin.DownloadVersionSpreadSheetServlet;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.ConfigurationDao;
import org.akaza.openclinica.domain.technicaladmin.ConfigurationBean;

/**
 * <P>
 * <b>SqlInitServlet.java </b>, servlet designed to run on startup, gathers all
 * the SQL queries and stores them in memory. Runs the static object SqlFactory,
 * which reads the properties file and then processes all the DAO-based XML
 * files.
 *
 * @author thickerson
 *
 *
 */
public class SQLInitServlet extends HttpServlet {

    private ServletContext context;
    private static Properties params = new Properties();
    private static Properties entParams = new Properties();

    @Override
    public void init() throws ServletException {


        context = getServletContext();
        CoreResources cr = (CoreResources) SpringServletAccess.getApplicationContext(context).getBean("coreResources");
        params = cr.getDATAINFO();
        entParams =cr.getDATAINFO();

//        params = (Properties) SpringServletAccess.getApplicationContext(context).getBean("dataInfo");
//        entParams = (Properties) SpringServletAccess.getApplicationContext(context).getBean("enterpriseInfo");

        ConfigurationDao configurationDao = SpringServletAccess
                .getApplicationContext(context)
                .getBean(ConfigurationDao.class);


        Role.COORDINATOR.setDescription(getField("coordinator"));
        Role.STUDYDIRECTOR.setDescription(getField("director"));
        Role.INVESTIGATOR.setDescription(getField("investigator"));
        Role.RESEARCHASSISTANT.setDescription(getField("ra"));
        Role.RESEARCHASSISTANT2.setDescription(getField("ra2"));
        Role.MONITOR.setDescription(getField("monitor"));

        Page.INITIAL_DATA_ENTRY_NW.getFileName();

        //The crf/original/CRF Template  will be created if not exist.
        String theDir = getField("filePath");
        String dir1 = "crf" + File.separator;
        String dir2 = "original" + File.separator;
        String dirRules = "rules";

        // Creating rules directory if not exist mantis issue 6584.
        if (!(new File(theDir)).isDirectory() || !(new File(dirRules)).isDirectory()) {
            (new File(theDir + dirRules)).mkdirs();
        }


        if (!(new File(theDir)).isDirectory() || !(new File(dir1)).isDirectory()
                || !(new File(dir2)).isDirectory()) {
            (new File(theDir + dir1 + dir2)).mkdirs();
            copyTemplate(theDir + dir1 + dir2 + DownloadVersionSpreadSheetServlet.CRF_VERSION_TEMPLATE);
        }
        theDir = theDir + dir1 + dir2;
        File excelFile = new File(theDir + DownloadVersionSpreadSheetServlet.CRF_VERSION_TEMPLATE);
        if(!excelFile.isFile()){
            copyTemplate(theDir);
        }

        // 'passwd_expiration_time' and 'change_passwd_required' are now defined in the database
        // Here the values in the datainfo.properites file (if any) are overridden.
        overridePropertyFromDatabase(configurationDao, "pwd.expiration.days", params, "passwd_expiration_time");
        overridePropertyFromDatabase(configurationDao, "pwd.change.required", params, "change_passwd_required");

    }

    /**
     * Gets a field value from properties by its key name
     *
     * @param key
     * @return String The value of field
     */
    public static String getField(String key) {
        String name = params.getProperty(key);
        if (name != null) {
            name = name.trim();
        }
        return name == null ? "" : name;
    }

    /**
     * Gets the supportURL value from properties by its key name
     *
     * @return String The value of supportURL key
     */
    public static String getSupportURL() {
        String name = params.getProperty("supportURL");
        return name == null ? "" : name.trim();
    }

    /**
     * Gets a field value by its key name from the enterprise.properties file
     *
     * @param key
     * @return String The value of field
     */
    public static String getEnterpriseField(String key) {
        String name = entParams.getProperty(key);
        if (name != null) {
            name = name.trim();
        }
        return name == null ? "" : name;
    }

    /**
     * We return empty String if DBName is not found in params.
     * The only reason why this is done this way is for unit testing
     * to work properly.
     *
     * EntityDAO uses SQLInitServlet.getDBName().equals("oracle") , This works
     * fine in the Servlet environment because of this class but in a unit test
     * it does not
     *
     * @author Krikor Krumlian the return portion
     *
     */
    public static String getDBName() {
        String name = params.getProperty("dataBase");
        return name == null ? "" : name;
    }

    public void copyTemplate(String theDir){
        OutputStream out = null;
        InputStream is = null;
        CoreResources cr = (CoreResources) SpringServletAccess.getApplicationContext(context).getBean("coreResources");
        try {
            is = cr.getInputStream(DownloadVersionSpreadSheetServlet.CRF_VERSION_TEMPLATE);
            File excelOutFile = new File(theDir);
            out = new FileOutputStream(excelOutFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }finally {
            try{
                is.close();
                out.close();
            }catch(Exception e){
            }
        }
    }

    /**
     * Overrides a configuration in a properties file with a value read from the database.
     * @param configurationDao
     * @param propertyNameInDatabase
     * @param properties
     * @param propertyNameInProperties
     */
    private void overridePropertyFromDatabase(ConfigurationDao configurationDao, String propertyNameInDatabase,
                                              Properties properties, String propertyNameInProperties) {
        ConfigurationBean config = configurationDao.findByKey(propertyNameInDatabase);
        if (config != null) {
            properties.setProperty(propertyNameInProperties, config.getValue());
        }
    }

}