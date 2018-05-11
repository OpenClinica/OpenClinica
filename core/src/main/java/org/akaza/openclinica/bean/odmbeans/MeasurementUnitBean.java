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
 * @author ywang (May, 2009)
 * 
 */
public class MeasurementUnitBean extends ElementOIDBean {
    private String name;
    private SymbolBean symbol;

    public MeasurementUnitBean() {
        this.symbol = new SymbolBean();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SymbolBean getSymbol() {
        return symbol;
    }

    public void setSymbol(SymbolBean symbol) {
        this.symbol = symbol;
    }
}