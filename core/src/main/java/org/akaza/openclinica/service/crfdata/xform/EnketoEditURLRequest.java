package org.akaza.openclinica.service.crfdata.xform;

import java.util.HashMap;
import java.util.List;

public class EnketoEditURLRequest {

    private String server_url = null;
    private String form_id = null;
    private String instance_id = null;
    private String return_url = null;
    private String instance = null;
    private String complete_button = null;

    public EnketoEditURLRequest(String server_url, String form_id, String instance_id, String return_url, String instance, String complete_button) {
        this.server_url = server_url;
        this.form_id = form_id;
        this.instance_id = instance_id;
        this.return_url = return_url;
        this.instance = instance;
        this.complete_button = complete_button;
    }

    public HashMap<String, String> getEnketoEditUrlObject(EnketoEditURLRequest eeur, List<String> attachementList) {
        HashMap<String, String> map = new HashMap();
        map.put("server_url", eeur.getServer_url());
        map.put("form_id", eeur.getForm_id());
        map.put("instance_id", eeur.getInstance_id());
        map.put("return_url", eeur.getReturn_url());
        map.put("instance", eeur.getInstance());
        map.put("complete_button", eeur.getComplete_button());
        for (String attachement : attachementList) {
            int index = attachement.indexOf("-+-+-");
            String fileName = attachement.substring(0, index);
            String downloadUrl = attachement.substring(index + 5);
            map.put("instance_attachments[" + fileName + "]", downloadUrl);
        }
        return map;
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

    public String getComplete_button() {
        return complete_button;
    }

    public void setComplete_button(String complete_button) {
        this.complete_button = complete_button;
    }

}
