/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2005 Akaza
 * Research
 *
 */

package org.akaza.openclinica.bean.odmbeans;

/**
 * 
 * @author ywang (May, 2008)
 * 
 */

public class TranslatedTextBean {
    private String text;
    private String xml_lang;
    

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }
    
    public void setXmlLang(String lang) {
        this.xml_lang = lang;
    }
    
    public String getXmlLang() {
        return this.xml_lang;
    }
}