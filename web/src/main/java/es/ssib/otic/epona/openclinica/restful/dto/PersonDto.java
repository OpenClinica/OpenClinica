package es.ssib.otic.epona.openclinica.restful.dto;

import java.io.Serializable;
import java.util.Date;

public class PersonDto
	implements Serializable {

	public static final long serialVersionUID = 1L;

	private Date fechaNacimiento;
	private String dni;
	private String cipAut;
	private String nombre;
	private String apellido1;
	private String apellido2;
	private String sexo;
	private String mensajeError;

	public PersonDto() {
	}

	public PersonDto(
		Date fechaNacimiento,
		String dni,
		String cipAut,
		String nombre,
		String apellido1,
		String apellido2,
		String sexo) {

		this.fechaNacimiento =
			fechaNacimiento;
		this.dni =
			dni;
		this.cipAut =
			cipAut;
		this.nombre =
			nombre;
		this.apellido1 =
			apellido1;
		this.apellido2 =
			apellido2;
		this.sexo =
			sexo;
	}

	public PersonDto(
		String mensajeError) {

		this.mensajeError =
			mensajeError;
	}

	public Date getFechaNacimiento() {
		return
			this.fechaNacimiento;
	}

	public void setFechaNacimiento(
		Date fechaNacimiento) {

		this.fechaNacimiento =
			fechaNacimiento;
	}

	public String getDni() {
		return
			this.dni;
	}

	public void setDni(
		String dni) {

		this.dni =
			dni;
	}

	public String getCipAut() {
		return
			this.cipAut;
	}

	public void setCipAut(
		String cipAut) {

		this.cipAut =
			cipAut;
	}

	public String getNombre() {

		return
			this.nombre;
	}

	public void setNombre(
		String nombre) {

		this.nombre =
			nombre;
	}

	public String getApellido1() {

		return
			this.apellido1;
	}

	public void setApellido1(
		String apellido1) {

		this.apellido1 =
			apellido1;
	}

	public String getApellido2() {

		return
			this.apellido2;
	}

	public void setApellido2(
		String apellido2) {

		this.apellido2 =
			apellido2;
	}

	public String getSexo() {
		return
			this.sexo;
	}

	public void setSexo(
		String sexo) {

		this.sexo =
			sexo;
	}

	public String getMensajeError() {

		return
			this.mensajeError;
	}

	public void setMensajeError(
		String mensajeError) {

		this.mensajeError =
			mensajeError;
	}
}

