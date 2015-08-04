/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.managestudy;

import org.akaza.openclinica.bean.core.AuditableEntityBean;
import org.akaza.openclinica.bean.oid.OidGenerator;
import org.akaza.openclinica.bean.oid.StudySubjectOidGenerator;

import java.util.ArrayList;
import java.util.Date;

/**
 * @author jxu
 *
 */
public class StudySubjectBean extends AuditableEntityBean {
    // STUDY_SUBJECT_ID, LABEL, SUBJECT_ID, STUDY_ID
    // STATUS_ID, DATE_CREATED, OWNER_ID,
    // DATE_UPDATED, UPDATE_ID,secondary_label
    private String label = "";

    private int subjectId;

    private int studyId;
    
    /**
     * @vbc 08/06/2008 NEW EXTRACT DATA IMPLEMENTATION 
     * - add dob_collected
     */
    private boolean isDobCollected;
    
    // private int studyGroupId;

    private Date enrollmentDate;

    private String secondaryLabel = "";

    private String uniqueIdentifier = "";// not in the table, for display
    // purpose

    private String studyName = "";// not in the table, for display purpose

    private char gender = 'm';// not in the table, for display purpose

    private Date dateOfBirth;// not in the db

    /**
     * An array of the groups this subject belongs to. Each element is a
     * StudyGroupMapBean object. Not in the database.
     */
    private ArrayList studyGroupMaps;
    
    private Date eventStartDate;//not in DB, for adding subject from subject matrix
    
    /**
     * The OID, used for export and import of data.
     */
    private String oid;

    private OidGenerator oidGenerator = new StudySubjectOidGenerator();
    private String time_zone;
    	
	public StudySubjectBean() {
        studyGroupMaps = new ArrayList();
    }

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

	public OidGenerator getOidGenerator() {
		return oidGenerator;
	}

	public void setOidGenerator(OidGenerator oidGenerator) {
		this.oidGenerator = oidGenerator;
	}

    /**
     * @return Returns the uniqueIndentifier.
     */
    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    /**
     * @param uniqueIdentifier
     *            The uniqueIdentifier to set.
     */
    public void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
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
     * @return Returns the gender.
     */
    public char getGender() {
        return gender;
    }

    /**
     * @param gender
     *            The gender to set.
     */
    public void setGender(char gender) {
        this.gender = gender;
    }

    /**
     * @return Returns the label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label
     *            The label to set.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return Returns the secondaryLabel.
     */
    public String getSecondaryLabel() {
        return secondaryLabel;
    }

    /**
     * @param secondaryLabel
     *            The secondaryLabel to set.
     */
    public void setSecondaryLabel(String secondaryLabel) {
        this.secondaryLabel = secondaryLabel;
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

    /**
     * @return Returns the subjectId.
     */
    public int getSubjectId() {
        return subjectId;
    }

    /**
     * @param subjectId
     *            The subjectId to set.
     */
    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    /**
     * @return Returns the enrollmentDate.
     */
    public Date getEnrollmentDate() {
        return enrollmentDate;
    }

    /**
     * @param enrollmentDate
     *            The enrollmentDate to set.
     */
    public void setEnrollmentDate(Date enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    // disambiguate the meaning of "name" in this context
    @Override
    public String getName() {
        return getLabel();
    }

    @Override
    public void setName(String name) {
        setLabel(name);
    }

    /**
     * @return Returns the studyGroupMaps.
     */
    public ArrayList getStudyGroupMaps() {
        return studyGroupMaps;
    }

    /**
     * @param studyGroupMaps
     *            The studyGroupMaps to set.
     */
    public void setStudyGroupMaps(ArrayList studyGroupMaps) {
        this.studyGroupMaps = studyGroupMaps;
    }

    /**
     * @return Returns the dateOfBirth.
     */
    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * @param dateOfBirth
     *            The dateOfBirth to set.
     */
    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * @return the eventStartDate
     */
    public Date getEventStartDate() {
        return eventStartDate;
    }

    /**
     * @param eventStartDate the eventStartDate to set
     */
    public void setEventStartDate(Date eventStartDate) {
        this.eventStartDate = eventStartDate;
    }

	/**
	 * @return the isDobCollected
	 */
	public boolean isDobCollected() {
		return isDobCollected;
	}

	/**
	 * @param isDobCollected the isDobCollected to set
	 */
	public void setDobCollected(boolean isDobCollected) {
		this.isDobCollected = isDobCollected;
	}

	public String getTime_zone() {
		return time_zone;
	}

	public void setTime_zone(String time_zone) {
		this.time_zone = time_zone;
	}
    

}