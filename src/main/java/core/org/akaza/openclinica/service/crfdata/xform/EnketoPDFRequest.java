package core.org.akaza.openclinica.service.crfdata.xform;

import org.springframework.util.LinkedMultiValueMap;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EnketoPDFRequest {

	private String server_url = null;
    private String form_id = null;
    private String instance_id = null;
    private String return_url = null;
    private String instance = null;
    private String ecid = null;
    private String format;
    private String margin;
    private String landscape;
    private String pid = null;
    
    @JsonProperty("instance_attachments")
    private InstanceAttachment instanceAttachments;

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

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;		
	}

	public String getMargin() {
		return margin;
	}

	public void setMargin(String margin) {
		this.margin = margin;		
	}

	public String getLandscape() {
		return landscape;
	}

	public void setLandscape(String landscape) {
		this.landscape = landscape;		
	}

	@JsonProperty("instance_attachments")
	public InstanceAttachment getInstanceAttachments() {
		return instanceAttachments;
	}

	@JsonProperty("instance_attachments")
	public void setInstanceAttachments(InstanceAttachment instanceAttachments) {
		this.instanceAttachments = instanceAttachments;
	}

	public String getEcid() {
		return ecid;
	}

	public void setEcid(String ecid) {
		this.ecid = ecid;		
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}
    
}
