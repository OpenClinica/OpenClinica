/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.core;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.akaza.openclinica.i18n.util.ResourceBundleProvider;

/**
 * @author ssachs
 *
 * Superclass for controlled vocabulary terms like status, role, etc.
 */
public class Term extends EntityBean {

    Locale locale;
    //ResourceBundle resterm;
    protected String description;

    public Term() {
        super();
    }

    public Term(int id, String name) {
        setId(id);
        setName(name);
        setDescription("");

    }

    public Term(int id, String name, String description) {
        setId(id);
        setName(name);
        setDescription(description);
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        if (!this.description.equals("")) {
            ResourceBundle resterm = ResourceBundleProvider.getTermsBundle();
            return resterm.getString(this.description).trim();
        } else
            return null;
        // return this.description;
    }

    /**
     * @param description
     *            The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    public boolean equals(Term t) {
        return id == t.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public static boolean contains(int id, List list) {
        Term t = new Term(id, "");

        for (int i = 0; i < list.size(); i++) {
            Term temp = (Term) list.get(i);
            if (temp.equals(t)) {
                return true;
            }
        }
        return false;
    }

    public static Term get(int id, List list) {
        Term t = new Term(id, "");

        for (int i = 0; i < list.size(); i++) {
            Term temp = (Term) list.get(i);
            if (temp.equals(t)) {
                return temp;
            }
        }

        return new Term();
    }

    @Override
    public String getName() {
        // *
        ResourceBundle resterm = ResourceBundleProvider.getTermsBundle();
        String name = null;
        if(resterm != null) {
        	 name = resterm.getString(this.name);
        }
       
        if(name != null) {
           return name.trim();
        }  else {
            return "";
        }
        // return this.name;
    }

    // TODO
    /*
     * public String getLocalizedName() { locale = LocaleProvider.getLocale();
     * resterm=
     * ResourceBundle.getBundle("org.akaza.openclinica.i18n.terms",locale);
     * return resterm.getString(this.name); }
     */

}
