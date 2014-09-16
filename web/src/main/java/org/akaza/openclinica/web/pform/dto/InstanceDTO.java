package org.akaza.openclinica.web.pform.dto;

public class InstanceDTO {
	private DataDTO data;

	public DataDTO getData() {
	  return data;
  }

	public void setData(DataDTO data) {
	  this.data = data;
  }
	
	@Override
	public String toString() {
	  return "<instance> "+data.toString()+"</instance> ";
	}
}