/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.dao;

import org.akaza.openclinica.dao.core.CoreResources;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 *
 */
public class QueryStore implements Serializable, ResourceLoaderAware {

    private static final long serialVersionUID = -5730668649244361127L;

    private DataSource dataSource;

    private ResourceLoader resourceLoader;

    private final Map<String, Properties> fileByName = new HashMap<String, Properties>();

    public void init() {
        String dbFolder = resolveDbFolder();
        PathMatchingResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver(resourceLoader);
        try {
            Resource resources[] = resourceResolver.getResources("classpath:queries/" + dbFolder + "/**/*.properties");
            for (Resource r : resources) {
                Properties p = new Properties();
                p.load(r.getInputStream());
                fileByName.put(StringUtils.substringBeforeLast(r.getFilename(), "."), p);
            }
        } catch (IOException e) {
            throw new BeanInitializationException("Unable to read files from directory 'classpath:queries/" + dbFolder
                    + "'", e);
        }
    }

    public String query(String fileName, String queryId) {
        Properties file = fileByName.get(fileName);
        if (file == null) {
            throw new IllegalArgumentException("The queries file '" + fileName + "' could not be found");
        }
        String q = file.getProperty(queryId);
        if (q == null) {
            throw new IllegalArgumentException("The query '" + queryId + "' could not be found in the file '" +
                    fileName + "'");
        }
        return q;
    }

    public boolean hasQuery(String fileName, String queryId) {
        Properties file = fileByName.get(fileName);
        if (file == null) {
            throw new IllegalArgumentException("The queries file '" + fileName + "' could not be found");
        }
        String q = file.getProperty(queryId);
        return q != null;
    }

    protected String resolveDbFolder() {
        try {
            Connection conn = dataSource.getConnection();
            CoreResources.setSchema(conn);

            String url = conn.getMetaData().getURL();
            if (url.startsWith("jdbc:postgresql")) {
                return "postgres";
            }
            if (url.startsWith("jdbc:oracle")) {
                return "oracle";
            }
            throw new BeanInitializationException("Unrecognized JDBC url " + url);
        } catch (SQLException e) {
            throw new BeanInitializationException("Unable to read datasource information", e);
        }
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }



}
