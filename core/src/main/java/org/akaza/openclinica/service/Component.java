/**
 * 
 */
package org.akaza.openclinica.service;

import java.io.Serializable;

/**
 * @author joekeremian
 *
 */
public class Component implements Serializable {
    private String name;
    private String type;
    private String[] columns;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String[] getColumns() {
        return columns;
    }

    public void setColumns(String[] columns) {
        this.columns = columns;
    }

}
