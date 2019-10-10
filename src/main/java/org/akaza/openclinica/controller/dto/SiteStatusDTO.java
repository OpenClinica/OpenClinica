package org.akaza.openclinica.controller.dto;

/**
 * Created by krikorkrumlian on 9/18/17.
 */
public class SiteStatusDTO {

    private String siteUuid;
    private String status;

    public String getSiteUuid() {
        return siteUuid;
    }

    public void setSiteUuid(String siteUuid) {
        this.siteUuid = siteUuid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        status = status.equalsIgnoreCase("design") ? "pending" : status;
        status = status.toUpperCase();
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SiteStatusDTO that = (SiteStatusDTO) o;

        if (siteUuid != null ? !siteUuid.equals(that.siteUuid) : that.siteUuid != null) return false;
        return status != null ? status.equals(that.status) : that.status == null;
    }

    @Override
    public int hashCode() {
        int result = siteUuid != null ? siteUuid.hashCode() : 0;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }
}
