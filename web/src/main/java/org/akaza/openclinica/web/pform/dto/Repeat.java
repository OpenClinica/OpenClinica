package org.akaza.openclinica.web.pform.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.akaza.openclinica.bean.submit.SectionBean;

public class Repeat {

private String nodeset;
private List<UserControl> usercontrol;
private Label label;
private String count;
public HashMap<String, String> counter = new HashMap<String, String>();
String key ="jr:count";
String value="";



public Repeat() {
}


/*public Repeat(HashMap<String, String> counter) {
	this.counter = counter;
	counter.put(key, value);
}
*/

public HashMap<String, String> getCounter() {
	return counter;
}
public void setCounter(HashMap<String, String> counter) {
	this.counter = counter;
}
public String getCount() {
	return count;
}
public void setCount(String count) {
	this.count = count;
}
public Label getLabel() {
	return label;
}
public void setLabel(Label label) {
	this.label = label;
}
public List<UserControl> getUsercontrol() {
	return usercontrol;
}
public void setUsercontrol(List<UserControl> usercontrol) {
	this.usercontrol = usercontrol;
}
public String getNodeset() {
	return nodeset;
}
public void setNodeset(String nodeset) {
	this.nodeset = nodeset;
}
}
