package org.akaza.openclinica.web.pform.formlist;

import java.security.NoSuchAlgorithmException;

import org.akaza.openclinica.domain.datamap.CrfBean;
import org.akaza.openclinica.domain.datamap.FormLayout;

public class XForm {
    private String formID = null;
    private String name = null;
    private String majorMinorVersion = null;
    private String version = null;
    private String hash = null;
    private String downloadURL = null;
    private String manifestURL = null;
    private String formLayout = null;

    public XForm() {

    }

    public XForm(CrfBean crf, FormLayout formLayout) throws NoSuchAlgorithmException {
        this.formID = formLayout.getOcOid();
        this.name = crf.getName();
        this.majorMinorVersion = formLayout.getName();
        this.version = formLayout.getName();
    }

    public String getFormID() {
        return formID;
    }

    public void setFormID(String formID) {
        this.formID = formID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMajorMinorVersion() {
        return majorMinorVersion;
    }

    public void setMajorMinorVersion(String majorMinorVersion) {
        this.majorMinorVersion = majorMinorVersion;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getDownloadURL() {
        return downloadURL;
    }

    public void setDownloadURL(String downloadURL) {
        this.downloadURL = downloadURL;
    }

    public String getManifestURL() {
        return manifestURL;
    }

    public void setManifestURL(String manifestURL) {
        this.manifestURL = manifestURL;
    }

    public String getFormLayout() {
        return formLayout;
    }

    public void setFormLayout(String formLayout) {
        this.formLayout = formLayout;
    }

}
