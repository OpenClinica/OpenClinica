/**
 * 
 */
package org.akaza.openclinica.service;

<<<<<<< HEAD
import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
=======
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
>>>>>>> OC-11624: Removed the links with studyParameterConfig

/**
 * @author joekeremian
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)

public class Component implements Serializable {
	@XmlElement
	private String name;
	@XmlElement
	private String type;
	@XmlElement
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
