package org.akaza.openclinica.controller;

import org.springframework.stereotype.Component;

/**
*  This class represents the state of a sidebar in decorator.jsp. For example,
 * if the Alerts/Messages should be initially displayed as open, then the alertsBoxSetup
 * property would be set to SidebarEnumConstants.OPENALERTS; if the Icon
 * Keys box is disabled for a certain display, then the iconsBoxSetup should be
 * set to SidebarEnumConstants.DISABLEICONS. These values are typically configured
 * in a Spring bean.
 * Date: Jan 14, 2009
 * @see SidebarEnumConstants
 */

public class SidebarInit {
    private SidebarEnumConstants alertsBoxSetup;
    private SidebarEnumConstants infoBoxSetup;
    private SidebarEnumConstants instructionsBoxSetup;
    private SidebarEnumConstants enableIconsBoxSetup;
    private SidebarEnumConstants iconsBoxSetup;

    public SidebarEnumConstants getInfoBoxSetup() {
        return infoBoxSetup;
    }

    public void setInfoBoxSetup(SidebarEnumConstants infoBoxSetup) {
        this.infoBoxSetup = infoBoxSetup;
    }

    public SidebarEnumConstants getInstructionsBoxSetup() {
        return instructionsBoxSetup;
    }

    public void setInstructionsBoxSetup(SidebarEnumConstants instructionsBoxSetup) {
        this.instructionsBoxSetup = instructionsBoxSetup;
    }

    public SidebarEnumConstants getEnableIconsBoxSetup() {
        return enableIconsBoxSetup;
    }

    public void setEnableIconsBoxSetup(SidebarEnumConstants enableIconsBoxSetup) {
        this.enableIconsBoxSetup = enableIconsBoxSetup;
    }

    public SidebarEnumConstants getIconsBoxSetup() {
        return iconsBoxSetup;
    }

    public void setIconsBoxSetup(SidebarEnumConstants iconsBoxSetup) {
        this.iconsBoxSetup = iconsBoxSetup;
    }

    public SidebarEnumConstants getAlertsBoxSetup() {
        return alertsBoxSetup;
    }

    public void setAlertsBoxSetup(SidebarEnumConstants alertsBoxSetup) {
        this.alertsBoxSetup = alertsBoxSetup;
    }
}
