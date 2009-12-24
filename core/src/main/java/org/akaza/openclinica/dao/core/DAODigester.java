/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.dao.core;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

// import org.apache.commons.digester.xmlrules.*;

/**
 * <P>
 * Creates an Apache Commons Digester, which parses SQL queries in XML and then
 * stores them in a hashmap, to be accessed later. Idea is to create one XML
 * file per Data Access Object, so that SQL syntax can be abstracted out of the
 * Java JDBC code.
 * </P>
 *
 * @author thickerson
 *
 * TODO
 */
public class DAODigester {

    private final HashMap queries = new HashMap();
    private InputStream fis;

    public void run() throws IOException, SAXException {
        Digester digester = new Digester();
        digester.push(this);
        // set up a simple format for grabbing queries through XML
        /*
         * <queries> <query> <name>userDaoInsert</name> <sql>INSERT INTO USER
         * (USER_ID, USER_NAME, USER_PASS) VALUES (USER_ID_SEQ.NEXTVAL,?,?);</sql>
         * </query> </queries>
         *
         */
        digester.addCallMethod("queries/query", "setQuery", 2);
        digester.addCallParam("queries/query/name", 0);
        digester.addCallParam("queries/query/sql", 1);
        digester.parse(fis);
    }

    public void setQuery(String name, String query) {
        queries.put(name, query);
    }

    public String getQuery(String name) {
        return (String) queries.get(name);
    }

    public void setInputStream(InputStream fis) {
        this.fis = fis;
    }

}
