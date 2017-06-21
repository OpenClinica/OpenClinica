package es.ssib.otic.epona.openclinica.restful.dto;

import java.io.Serializable;

public class IdResultDto
	implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;
	private String errorMessage;
	private boolean error;

	public IdResultDto(
		int id) {

		this.id =
			id;
		this.error =
			false;
	}

	public IdResultDto(
		String errorMessage) {

		this.errorMessage =
			errorMessage;
		this.id =
			-1;
		this.error =
			true;
	}

	public boolean isError() {

		return
			this.error;
	}

	public int getId() {

		return
			this.id;
	}

	public String getErrorMessage() {

		return
			this.errorMessage;
	}
}
