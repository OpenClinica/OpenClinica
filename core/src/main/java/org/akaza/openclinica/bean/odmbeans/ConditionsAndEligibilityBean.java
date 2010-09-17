/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 
 *
 */

package org.akaza.openclinica.bean.odmbeans;



/**
 *
 * @author ywang (Aug., 2010)
 *
 */

public class ConditionsAndEligibilityBean {
    //elements
    private String conditions;
    private String keywords;
    private String eligibilityCriteria;
    private String sex;
    private String healthyVolunteersAccepted;
    private Integer expectedTotalEnrollment;
    private AgeBean age = new AgeBean();
    
    public String getConditions() {
        return conditions;
    }
    public void setConditions(String conditions) {
        this.conditions = conditions;
    }
    public String getKeywords() {
        return keywords;
    }
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }
    public String getEligibilityCriteria() {
        return eligibilityCriteria;
    }
    public void setEligibilityCriteria(String eligibilityCriteria) {
        this.eligibilityCriteria = eligibilityCriteria;
    }
    public String getSex() {
        return sex;
    }
    public void setSex(String sex) {
        this.sex = sex;
    }
    public String getHealthyVolunteersAccepted() {
        return healthyVolunteersAccepted;
    }
    public void setHealthyVolunteersAccepted(String healthyVolunteersAccepted) {
        this.healthyVolunteersAccepted = healthyVolunteersAccepted;
    }
    public Integer getExpectedTotalEnrollment() {
        return expectedTotalEnrollment;
    }
    public void setExpectedTotalEnrollment(Integer expectedTotalEnrollment) {
        this.expectedTotalEnrollment = expectedTotalEnrollment;
    }
    public AgeBean getAge() {
        return age;
    }
    public void setAge(AgeBean age) {
        this.age = age;
    }
}