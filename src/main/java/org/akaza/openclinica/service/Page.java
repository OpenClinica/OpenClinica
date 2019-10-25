/**
 * 
 */
package org.akaza.openclinica.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author joekeremian
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)

public class Page implements Serializable {

	@XmlElement
	private String name;
	@XmlElement
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
