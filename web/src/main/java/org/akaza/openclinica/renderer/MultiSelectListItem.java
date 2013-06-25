package org.akaza.openclinica.renderer;

public class MultiSelectListItem {

  public MultiSelectListItem (String id, String label) {
    this.id = id;
    this.label = label;
  }
  
  public String id;
  public String label;
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getLabel() {
    return label;
  }
  public void setLabel(String label) {
    this.label = label;
  }
  
}