package org.akaza.openclinica.dao.core;

import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class CoreResources implements ResourceLoaderAware {

    private ResourceLoader resourceLoader;
    public static String PROPERTIES_DIR;
    private static String DB_NAME;
    private static Properties DATAINFO;

    private Properties dataInfo;

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        try {
            //setPROPERTIES_DIR();
            String dbName = dataInfo.getProperty("dataBase");
            DATAINFO = dataInfo;
            DB_NAME = dbName;
            SQLFactory factory = SQLFactory.getInstance();
            factory.run(dbName, resourceLoader);
        } catch (OpenClinicaSystemException e) {
            //throw new OpenClinicaSystemException(e.getMessage(), e.fillInStackTrace());
        }
    }

    public InputStream getInputStream(String fileName) throws IOException {
        return resourceLoader.getResource("classpath:properties/" + fileName).getInputStream();
    }

    public URL getURL(String fileName) throws IOException {
        return resourceLoader.getResource("classpath:properties/" + fileName).getURL();
    }

    private void setPROPERTIES_DIR() {
        String resource = "classpath:properties/placeholder.properties";
        System.out.println("Resource" + resource);
        Resource scr = resourceLoader.getResource(resource);
        String absolutePath = null;
        try {
            System.out.println("Resource" + resource);
            absolutePath = scr.getFile().getAbsolutePath();
            System.out.println("Resource" + ((ClassPathResource) scr).getPath());
            System.out.println("Resource" + resource);
            PROPERTIES_DIR = absolutePath.replaceAll("placeholder.properties", "");
            System.out.println("Resource" + PROPERTIES_DIR);
        } catch (IOException e) {
            throw new OpenClinicaSystemException(e.getMessage(), e.fillInStackTrace());
        }

    }

    public static String getDBName() {
        return DB_NAME;
    }

    public static String getField(String key) {
        String value = DATAINFO.getProperty(key);
        if (value != null) {
            value = value.trim();
        }
        return value == null ? "" : value;

    }

    public Properties getDataInfo() {
        return dataInfo;
    }

    public void setDataInfo(Properties dataInfo) {
        this.dataInfo = dataInfo;
    }

}
