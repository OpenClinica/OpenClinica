/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.submit;

import org.akaza.openclinica.bean.core.AuditableEntityBean;

import java.util.Date;

/**
 * @author jxu
 */
public class SubjectBean extends AuditableEntityBean {
    /*
     * since we extend entity bean, we already have the following: subject_id,
     * date_created, date_updated, update_id
     */
    private Date dateOfBirth;
    private char gender = ' ';
    private String uniqueIdentifier = "";
    /*
     * tells that whether the dateOfBirth is a real birthday or only the year
     * part is valid
     */
    private boolean dobCollected;
    
    
    private String study_unique_identifier;  // Not from subject table, used for display purposes
    private String label;                    // Not from subject...

    public void setLabel(String label) {
        this.label = label;       
    }
    
    public String getLabel() {
        return this.label;
    }
    
    public void setStudyIdentifier(String studyId) {
        this.study_unique_identifier = studyId;       
    }
    
    public String getStudyIdentifier() {
        return this.study_unique_identifier;
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
     * @return Returns the uniqueIdentifier.
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

    // disambiguate the meaning of superclass's "name" member
    @Override
    public String getName() {
        return getUniqueIdentifier();
    }

    @Override
    public void setName(String name) {
        setUniqueIdentifier(name);
    }

    /**
     * @return Returns the dobCollected.
     */
    public boolean isDobCollected() {
        return dobCollected;
    }

    /**
     * @param dobCollected
     *            The dobCollected to set.
     */
    public void setDobCollected(boolean dobCollected) {
        this.dobCollected = dobCollected;
    }
}