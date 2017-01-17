package org.akaza.openclinica.service.crfdata.xform;

public class EnketoEditURLRequest {

    private String server_url = null;
    private String form_id = null;
    private String instance_id = null;
    private String return_url = null;
    private String instance = null;
    private boolean complete_button=true;

    public EnketoEditURLRequest(String server_url, String form_id, String instance_id, String return_url, String instance,boolean complete_button) {
        this.server_url = server_url;
        this.form_id = form_id;
        this.instance_id = instance_id;
        this.return_url = return_url;
        this.instance = instance;
        this.complete_button=complete_button;
    }

    public String getServer_url() {
        return server_url;
    }

    public void setServer_url(String server_url) {
        this.server_url = server_url;
    }

    public String getForm_id() {
        return form_id;
    }

    public void setForm_id(String form_id) {
        this.form_id = form_id;
    }

    public String getInstance_id() {
        return instance_id;
    }

    public void setInstance_id(String instance_id) {
        this.instance_id = instance_id;
    }

    public String getReturn_url() {
        return return_url;
    }

    public void setReturn_url(String return_url) {
        this.return_url = return_url;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

	public boolean isComplete_button() {
		return complete_button;
	}

	public void setComplete_button(boolean complete_button) {
		this.complete_button = complete_button;
	}


}
