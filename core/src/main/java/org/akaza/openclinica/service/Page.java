/**
 * 
 */
package org.akaza.openclinica.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author joekeremian
 *
 */

public class Page implements Serializable {
    private String name;
    private List<Component> components;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Component> getComponents() {
        return components;
    }

    public void setComponents(List<Component> components) {
        this.components = components;
    }

    public void addComponent(Component component) {
        if (getComponents() == null)
            components = new ArrayList<>();
        components.add(component);
    }

}
