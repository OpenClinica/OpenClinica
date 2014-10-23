package org.akaza.openclinica.web.pform.dto;

import java.util.List;

public class GroupDTO {
	private LabelDTO label;
	private List<InputDTO> input;
	private List<SelectOneDTO> selectOne;
	private List<GroupDTO> group;
	private List<UploadDTO> upload;
	private List<SelectDTO> select;
	private List<RepeatDTO> repeat;
	
	public LabelDTO getLabel() {
	  return label;
  }
	
	public void setLabel(LabelDTO label) {
	  this.label = label;
  }
	
	public List<InputDTO> getInput() {
	  return input;
  }
	
	public void setInput(List<InputDTO> input) {
	  this.input = input;
  }
	
	public List<SelectOneDTO> getSelectOne() {
	  return selectOne;
  }
	
	public void setSelectOne(List<SelectOneDTO> selectOne) {
	  this.selectOne = selectOne;
  }
	
	public List<GroupDTO> getGroup() {
	  return group;
  }
	
	public void setGroup(List<GroupDTO> group) {
	  this.group = group;
  }
	
	public List<UploadDTO> getUpload() {
	  return upload;
  }
	
	public void setUpload(List<UploadDTO> upload) {
	  this.upload = upload;
  }
	
	public List<SelectDTO> getSelect() {
	  return select;
  }

	public void setSelect(List<SelectDTO> select) {
	  this.select = select;
  }
	
	public List<RepeatDTO> getRepeat() {
	  return repeat;
  }

	public void setRepeat(List<RepeatDTO> repeat) {
	  this.repeat = repeat;
  }
	
	@Override
	public String toString() {
		String temp = "<group>";
		if (label != null) {
			temp = temp + " " + label.toString();
		}
		
		if (input != null) {
			for (int i = 0; i < input.size(); i++) {
				temp = temp + " " + input.get(i).toString();
			}
		}
		
		if (selectOne != null) {
			for (int i = 0; i < selectOne.size(); i++) {
				temp = temp + " " + selectOne.get(i).toString();
			}
		}
		
		if (group != null) {
			for (int i = 0; i < group.size(); i++) {
				temp = temp + " " + group.get(i).toString();
			}
		}
		
		if (upload != null) {
			for (int i = 0; i < upload.size(); i++) {
				temp = temp + " " + upload.get(i).toString();
			}
		}
		
		if (select != null) {
			for (int i = 0; i < select.size(); i++) {
				temp = temp + " " + select.get(i).toString();
			}
		}
		
		if (repeat != null) {
			for (int i = 0; i < repeat.size(); i++) {
				temp = temp + " " + repeat.get(i).toString();
			}
		}
		
		temp = temp + "</group>";
	  return temp;
	}
}