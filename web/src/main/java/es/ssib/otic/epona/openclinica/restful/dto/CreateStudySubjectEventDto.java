package es.ssib.otic.epona.openclinica.restful.dto;

import java.io.Serializable;
import java.util.Date;

public class CreateStudySubjectEventDto
	implements Serializable {

	private static final long serialVersionUID = 1L;

	private int studyEventDefinitionId;
	private int studySubjectId;
	private boolean crearNuevo;
	private Date startDate;
	private Date endDate;
	
	public int getStudyEventDefinitionId() {

		return
			this.studyEventDefinitionId;
	}

	public void setStudyEventDefinitionId(
		int studyEventDefinitionId) {

		this.studyEventDefinitionId =
			studyEventDefinitionId;
	}

	public int getStudySubjectId() {

		return
			this.studySubjectId;
	}

	public void setStudySubjectId(
		int studySubjectId) {

		this.studySubjectId =
			studySubjectId;
	}

	public boolean isCrearNuevo() {

		return
			this.crearNuevo;
	}

	public void setCrearNuevo(
		boolean crearNuevo) {

		this.crearNuevo =
			crearNuevo;
	}

	public Date getStartDate() {

		return
			this.startDate;
	}

	public void setStartDate(
		Date startDate) {

		this.startDate =
			startDate;
	}

	public Date getEndDate() {

		return
			this.endDate;
	}

	public void setEndDate(
		Date endDate) {

		this.endDate =
			endDate;
	}
}
