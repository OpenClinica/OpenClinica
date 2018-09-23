/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.login;

import org.akaza.openclinica.bean.core.AuditableEntityBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;

import java.util.ResourceBundle;

/**
 * @author thickerson
 */

/**
 * @author ssachs
 * 
 *         The superclass id field is the role id. The superclass name field is
 *         the role name.
 */

public class StudyUserRoleBean extends AuditableEntityBean {
    /*
     * this class will hold the following fields: username, rolename, studyid,
     * updateid, datecreated, dateupdated, ownerid, statusid in the context of
     * entitybean, name->username
     */

    private Role role;

    private int studyId;

    // not in the database, and not guaranteed to correspond to studyId; studyId
    // is authoritative
    // this is only provided as a convenience
    private String studyName = "";

    // not in the database, and not guaranteed to correspond to studyId; studyId
    // is authoritative
    // this is only provided as a convenience
    private int parentStudyId = 0;
    // FR 2018-09-20: added for view ListUserAccounts
    private String parentStudyName = "";
    // FR 2018-09-21: used for CSS 
    private Status studyStatus;



    private String lastName = ""; // not in the DB,not guaranteed to have a
    // value

    private String firstName = "";// not in the DB,not guaranteed to have a
    // value

    private String userName = ""; // name here is role.name, this is different
    // from name,not guaranteed to have a value

    // User role capabilities, use this instead the role name.
    private boolean canSubmitData;
    private boolean canExtractData;
    private boolean canManageStudy;

    private int userAccountId = 0;

    private boolean canMonitor;

    public StudyUserRoleBean() {
        role = Role.INVALID;
        studyId = 0;
        setRole(role);
        status = Status.AVAILABLE;
    }

    /**
     * @return Returns the role.
     */
    public Role getRole() {
        return role;
    }

    /**
     * @param role
     *            The role to set.
     */
    public void setRole(Role role) {
        this.role = role;
        super.setId(role.getId());
        super.setName(role.getName());
        // roleName=='coordinator' || roleName=='director' || roleName=='ra' ||
        // roleName=='investigator'}
        this.canSubmitData =
            this.role == Role.COORDINATOR || this.role == Role.STUDYDIRECTOR || this.role == Role.RESEARCHASSISTANT || this.role == Role.RESEARCHASSISTANT2 || this.role == Role.INVESTIGATOR;
        this.canExtractData = this.role == Role.COORDINATOR || this.role == Role.STUDYDIRECTOR || this.role == Role.INVESTIGATOR;
        this.canManageStudy = this.role == Role.COORDINATOR || this.role == Role.STUDYDIRECTOR;
        this.canMonitor = this.role == Role.MONITOR;
    }

    public int getUserAccountId() {
        return userAccountId;
    }

    public void setUserAccountId(int userAccountId) {
        this.userAccountId = userAccountId;
    }

    /**
     * @return Returns the roleName.
     */
    public String getRoleName() {
        return role.getName();
    }

    /**
     * @param roleName
     *            The roleName to set.
     */
    public void setRoleName(String roleName) {
        Role role = Role.getByName(roleName);
        if(role == null || role.getId()==0) {
            ResourceBundle resterm = ResourceBundleProvider.getTermsBundle();
            if(resterm.getString("site_investigator").equals(roleName)) {
                role = Role.INVESTIGATOR;
            } else if("Data Specialist".equals(roleName)) {
                role = Role.INVESTIGATOR;
            }
        }
        setRole(role);
    }

    /**
     * @return Returns the studyId.
     */
    public int getStudyId() {
        return studyId;
    }

    /**
     * @param studyId
     *            The studyId to set.
     */
    public void setStudyId(int studyId) {
        this.studyId = studyId;
    }

    // this is different from the meaning of "name"
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return Returns the studyName.
     */
    public String getStudyName() {
        return studyName;
    }

    /**
     * @param studyName
     *            The studyName to set.
     */
    public void setStudyName(String studyName) {
        this.studyName = studyName;
    }

    /**
     * @return Returns the parentStudyId.
     */
    public int getParentStudyId() {
        return parentStudyId;
    }

    /**
     * @param parentStudyId
     *            The parentStudyId to set.
     */
    public void setParentStudyId(int parentStudyId) {
        this.parentStudyId = parentStudyId;
    }
    // FR 2018-09-20: added for view ListUserAccounts
    public String getParentStudyName() {
		return parentStudyName;
	}
    // FR 2018-09-20: added for view ListUserAccounts
	public void setParentStudyName(String parentStudyName) {
		this.parentStudyName = parentStudyName;
	}

    // FR 2018-09-21: used for css-formatting
    public Status getStudyStatus() {
		return studyStatus;
	}

    // FR 2018-09-21: used for css-formatting
    public void setStudyStatus(Status studyStatus) {
		this.studyStatus = studyStatus;
	}

    // FR 2018-09-21: used for css-formatting
    public String getStudyStatusCSS(){
    	String ret = "STUDY_StudyStatusMissing";
    	try{
    		ret ="STUDY_"+studyStatus.getName().replaceAll("\\W", "_");
    	}catch (Exception e){
    		// FR: if studyStatus is not set we have to find where it has to be set!!!
    		System.err.println("StudyStatus:"+((studyStatus!=null)?(studyStatus.getId()+" - "+studyStatus.getName()):studyStatus));
    		e.printStackTrace();
    	}
    	return ret;
    }

	
	@Override
    public String getName() {
        if (role != null) {
            return role.getName();
        }
        return "";
    }

    @Override
    public void setName(String name) {
        setRoleName(name);
    }

    @Override
    public int getId() {
        if (role != null) {
            return role.getId();
        }
        return 0;
    }

    @Override
    public void setId(int id) {
        setRole(Role.get(id));
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public boolean isInvalid() {
        return this.role == Role.INVALID;
    }

    public boolean isSubmitData() {
        return canSubmitData;
    }

    public boolean isExtractData() {
        return canExtractData;
    }

    public boolean isManageStudy() {
        return canManageStudy;
    }

    public boolean isMonitor() {
        return canMonitor;
    }

    public boolean isInvestigator() {
        return this.role == Role.INVESTIGATOR;
    }

    public boolean isResearchAssistant() {
        return this.role == Role.RESEARCHASSISTANT;
    }

    public boolean isResearchAssistant2() {
        return this.role == Role.RESEARCHASSISTANT2;
    }

    public boolean isCoordinator() {
        return this.role == Role.COORDINATOR;
    }

    public boolean isDirector() {
        return this.role == Role.STUDYDIRECTOR;
    }
}
