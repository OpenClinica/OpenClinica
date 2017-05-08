/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.dao.managestudy;

/**
 * <P>
 * StudyDAO.java, the data access object that users will access the database for
 * study objects, tbh.
 * 
 * @author thickerson
 * 
 * 
 */

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyType;
import org.akaza.openclinica.dao.core.*;
import org.akaza.openclinica.domain.datamap.StudyEnvEnum;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.*;
public class StudyDAO <K extends String,V extends ArrayList> extends AuditableEntityDAO {
    // private DataSource ds;
    // private DAODigester digester;
    public StudyDAO(DataSource ds) {
        super(ds);
    }

    public StudyDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
    }

    // This constructor sets up the Locale for JUnit tests; see the locale
    // member variable in EntityDAO, and its initializeI18nStrings() method
    public StudyDAO(DataSource ds, DAODigester digester, Locale locale) {

        this(ds, digester);
        this.locale = locale;
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_STUDY;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);// sid
        this.setTypeExpected(2, TypeNames.INT);// parent id
        this.setTypeExpected(3, TypeNames.STRING);// ident
        this.setTypeExpected(4, TypeNames.STRING);// second ident
        this.setTypeExpected(5, TypeNames.STRING);// name
        this.setTypeExpected(6, TypeNames.STRING);// summary
        this.setTypeExpected(7, TypeNames.DATE);// start
        this.setTypeExpected(8, TypeNames.DATE);// end
        this.setTypeExpected(9, TypeNames.DATE);// create
        this.setTypeExpected(10, TypeNames.DATE);// update
        this.setTypeExpected(11, TypeNames.INT);// owner
        this.setTypeExpected(12, TypeNames.INT);// updater
        this.setTypeExpected(13, TypeNames.INT);// type id
        this.setTypeExpected(14, TypeNames.INT);// status
        this.setTypeExpected(15, TypeNames.STRING);// pi
        this.setTypeExpected(16, TypeNames.STRING);// fname
        this.setTypeExpected(17, TypeNames.STRING);// fcity
        this.setTypeExpected(18, TypeNames.STRING);// fstate
        this.setTypeExpected(19, TypeNames.STRING);// fzip
        this.setTypeExpected(20, TypeNames.STRING);// country
        this.setTypeExpected(21, TypeNames.STRING);// frs
        this.setTypeExpected(22, TypeNames.STRING);// fcn
        this.setTypeExpected(23, TypeNames.STRING);// fcdegree
        this.setTypeExpected(24, TypeNames.STRING);// fcphone
        this.setTypeExpected(25, TypeNames.STRING);// fcemail
        this.setTypeExpected(26, TypeNames.STRING);// prottype
        this.setTypeExpected(27, TypeNames.STRING);// protdesc
        this.setTypeExpected(28, TypeNames.DATE);// pdateverif
        this.setTypeExpected(29, TypeNames.STRING);// phase
        this.setTypeExpected(30, TypeNames.INT);// expectotenroll
        this.setTypeExpected(31, TypeNames.STRING);// sponsor
        this.setTypeExpected(32, TypeNames.STRING);// collab
        this.setTypeExpected(33, TypeNames.STRING);// medline
        this.setTypeExpected(34, TypeNames.STRING);// url
        this.setTypeExpected(35, TypeNames.STRING);// url-desc
        this.setTypeExpected(36, TypeNames.STRING);// conds
        this.setTypeExpected(37, TypeNames.STRING);// keyw
        this.setTypeExpected(38, TypeNames.STRING);// eligible
        this.setTypeExpected(39, TypeNames.STRING);// gender, no char avail.
        this.setTypeExpected(40, TypeNames.STRING);// agemax
        this.setTypeExpected(41, TypeNames.STRING);// agemin
        this.setTypeExpected(42, TypeNames.BOOL);// healthy volunteer
        this.setTypeExpected(43, TypeNames.STRING);// purpose
        this.setTypeExpected(44, TypeNames.STRING);// allocation
        this.setTypeExpected(45, TypeNames.STRING);// masking
        this.setTypeExpected(46, TypeNames.STRING);// control
        this.setTypeExpected(47, TypeNames.STRING);// assignment
        this.setTypeExpected(48, TypeNames.STRING);// endpoint
        this.setTypeExpected(49, TypeNames.STRING);// interv
        this.setTypeExpected(50, TypeNames.STRING);// duration
        this.setTypeExpected(51, TypeNames.STRING);// selection
        this.setTypeExpected(52, TypeNames.STRING);// timing
        this.setTypeExpected(53, TypeNames.STRING);// official_title
        this.setTypeExpected(54, TypeNames.BOOL);// results_reference
        this.setTypeExpected(55, TypeNames.STRING);// oc oid
        this.setTypeExpected(56, TypeNames.INT);
        this.setTypeExpected(57, TypeNames.STRING);// schema name
        this.setTypeExpected(58, TypeNames.STRING);// uuid
        this.setTypeExpected(59, TypeNames.STRING);// env type
    }

    /**
     * <b>update </b>, the method that returns an updated study bean after it
     * updates the database. Note that we can use the three stages from our
     * creation use case.
     * 
     * @return sb an updated study bean.
     */
    public EntityBean update(EntityBean eb) {
        StudyBean sb = (StudyBean) eb;
        sb = this.updateStepOne(sb);
        sb = this.createStepTwo(sb);
        sb = this.createStepThree(sb);
        sb = this.createStepFour(sb);
        return sb;
    }

    /**
     * <P>
     * updateStepOne, the update method for the database. This method takes the
     * place of createStepOne, since it runs an update and assumes you already
     * have a primary key in the study bean object.
     * 
     * @param sb
     *            the study bean which will be updated.
     * @return sb the study bean after it is updated with this phase.
     */
    public StudyBean updateStepOne(StudyBean sb) {
        HashMap variables = new HashMap();
        HashMap nullVars = new HashMap();

        if (sb.getParentStudyId() == 0) {
            nullVars.put(new Integer(1), new Integer(Types.INTEGER));
            variables.put(new Integer(1), null);
        } else {
            variables.put(new Integer(1), new Integer(sb.getParentStudyId()));
        }
        variables.put(new Integer(2), sb.getName());
        variables.put(new Integer(3), sb.getOfficialTitle());
        variables.put(new Integer(4), sb.getIdentifier());
        variables.put(new Integer(5), sb.getSecondaryIdentifier());
        variables.put(new Integer(6), sb.getSummary());
        variables.put(new Integer(7), sb.getPrincipalInvestigator());
        if (sb.getDatePlannedStart() == null) {
            nullVars.put(new Integer(8), new Integer(Types.DATE));
            variables.put(new Integer(8), null);
        } else {
            variables.put(new Integer(8), sb.getDatePlannedStart());
        }

        if (sb.getDatePlannedEnd() == null) {
            nullVars.put(new Integer(9), new Integer(Types.DATE));
            variables.put(new Integer(9), null);
        } else {
            variables.put(new Integer(9), sb.getDatePlannedEnd());
        }

        variables.put(new Integer(10), sb.getFacilityName());
        variables.put(new Integer(11), sb.getFacilityCity());
        variables.put(new Integer(12), sb.getFacilityState());
        variables.put(new Integer(13), sb.getFacilityZip());
        variables.put(new Integer(14), sb.getFacilityCountry());
        variables.put(new Integer(15), sb.getFacilityRecruitmentStatus());
        variables.put(new Integer(16), sb.getFacilityContactName());
        variables.put(new Integer(17), sb.getFacilityContactDegree());
        variables.put(new Integer(18), sb.getFacilityContactPhone());
        variables.put(new Integer(19), sb.getFacilityContactEmail());
        variables.put(new Integer(20), new Integer(sb.getStatus().getId()));// status
        // id
        // variables.put(new Integer(19), sb.getStatus())//need to get a
        // function
        // to get the id
        // variables.put(new Integer(19), sb.getCreatedDate());
        variables.put(new Integer(21), new Integer(sb.getUpdaterId()));// owner
        // id
        variables.put(new Integer(22), sb.getUpdatedDate());// date updated
        variables.put(new Integer(23), new Integer(sb.getOldStatus().getId()));// study id
        // variables.put(new Integer(22), new Integer(1));
        // stop gap measure for owner and updater id
        variables.put(new Integer(24), new Integer(sb.getId()));// study id
        this.execute(digester.getQuery("updateStepOne"), variables, nullVars);
        return sb;
    }

    /**
     * <b>create </b>, the method that creates a study in the database.
     * <P>
     * note: create is split up into four custom functions, per the use case; we
     * are creating the standard create function here which calls all four
     * functions at once, but the seperate functions may be required in the
     * control servlets.
     * 
     * @return eb the created entity bean.
     */
    public EntityBean create(EntityBean eb) {
        StudyBean sb = (StudyBean) eb;
        sb = this.createStepOne(sb);
        // in the above step, we will have created a primary key,
        // and in the next steps, we update the study bean
        // in phases
        sb = this.createStepTwo(sb);
        sb = this.createStepThree(sb);
        sb = this.createStepFour(sb);

        return sb;
    }

    /**
     * <P>
     * findNextKey, a method to return a simple int from the database.
     * 
     * @return int, which is the next primary key for creating a study.
     */
    public int findNextKey() {
        this.unsetTypeExpected();
        Integer keyInt = new Integer(0);
        this.setTypeExpected(1, TypeNames.INT);
        ArrayList alist = this.select(digester.getQuery("findNextKey"));
        Iterator it = alist.iterator();
        if (it.hasNext()) {
            HashMap key = (HashMap) it.next();
            keyInt = (Integer) key.get("key");
        }
        return keyInt.intValue();
    }

    /**
     * <P>
     * createStepOne, per the 'Detailed use case for administer system document
     * v1.0rc1' document. We insert the study in this method, and then update
     * the same study in the next three steps.
     * <P>
     * The next three steps, by the way, can then be used to update studies as
     * well.
     * 
     * @param sb
     *            Study bean about to be created.
     * @return same study bean with a primary key in the ID field.
     */
    public StudyBean createStepOne(StudyBean sb) {
        HashMap variables = new HashMap();
        HashMap nullVars = new HashMap();
        sb.setId(this.findNextKey());
        variables.put(new Integer(1), new Integer(sb.getId()));
        if (sb.getParentStudyId() == 0) {
            nullVars.put(new Integer(2), new Integer(Types.INTEGER));
            variables.put(new Integer(2), null);
        } else {
            variables.put(new Integer(2), new Integer(sb.getParentStudyId()));
        }

        variables.put(new Integer(3), sb.getName());
        variables.put(new Integer(4), sb.getOfficialTitle());
        variables.put(new Integer(5), sb.getIdentifier());
        variables.put(new Integer(6), sb.getSecondaryIdentifier());
        variables.put(new Integer(7), sb.getSummary());
        variables.put(new Integer(8), sb.getPrincipalInvestigator());

        if (sb.getDatePlannedStart() == null) {
            nullVars.put(new Integer(9), new Integer(Types.DATE));
            variables.put(new Integer(9), null);
        } else {
            variables.put(new Integer(9), sb.getDatePlannedStart());
        }

        if (sb.getDatePlannedEnd() == null) {
            nullVars.put(new Integer(10), new Integer(Types.DATE));
            variables.put(new Integer(10), null);
        } else {
            variables.put(new Integer(10), sb.getDatePlannedEnd());
        }

        variables.put(new Integer(11), sb.getFacilityName());
        variables.put(new Integer(12), sb.getFacilityCity());
        variables.put(new Integer(13), sb.getFacilityState());
        variables.put(new Integer(14), sb.getFacilityZip());
        variables.put(new Integer(15), sb.getFacilityCountry());
        variables.put(new Integer(16), sb.getFacilityRecruitmentStatusKey());
        variables.put(new Integer(17), sb.getFacilityContactName());
        variables.put(new Integer(18), sb.getFacilityContactDegree());
        variables.put(new Integer(19), sb.getFacilityContactPhone());
        variables.put(new Integer(20), sb.getFacilityContactEmail());
        variables.put(new Integer(21), new Integer(sb.getStatus().getId()));
        // variables.put(new Integer(19), sb.getStatus())//need to get a
        // function
        // to get the id
        variables.put(new Integer(22), new java.util.Date());
        variables.put(new Integer(23), new Integer(sb.getOwnerId()));
        variables.put(new Integer(24), getValidOid(sb));
        // replace this with the owner id
        this.execute(digester.getQuery("createStepOne"), variables, nullVars);
        return sb;
    }

    // we are generating and creating the valid oid at step one, tbh
    private String getOid(StudyBean sb) {

        String oid;
        try {
            oid = sb.getOid() != null ? sb.getOid() : sb.getOidGenerator().generateOid(sb.getIdentifier());
            return oid;
        } catch (Exception e) {
            throw new RuntimeException("CANNOT GENERATE OID");
        }
    }

    private String getValidOid(StudyBean sb) {

        String oid = getOid(sb);
        logger.info("*** " + oid);
        String oidPreRandomization = oid;
        while (findByOid(oid) != null) {
            oid = sb.getOidGenerator().randomizeOid(oidPreRandomization);
        }
        logger.info("returning the following oid: " + oid);
        return oid;

    }

    public StudyBean findByOid(String oid) {
        StudyBean sb = null;
        this.unsetTypeExpected();
        this.setTypesExpected();
        HashMap variables = new HashMap();
        variables.put(new Integer(1), oid);
        ArrayList alist = this.select(digester.getQuery("findByOid"), variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            sb = (StudyBean) this.getEntityFromHashMap((HashMap) it.next());
            return sb;
        } else {
            logger.info("returning null from find by oid...");
            return null;
        }

    }

    public StudyBean findByPublicOid(String oid) {
        StudyBean sb = null;
        this.unsetTypeExpected();
        this.setTypesExpected();
        HashMap variables = new HashMap();
        variables.put(new Integer(1), oid);
        ArrayList alist = this.select(digester.getQuery("findByPublicOid"), variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            sb = (StudyBean) this.getEntityFromHashMap((HashMap) it.next());
            return sb;
        } else {
            logger.info("returning null from find by oid...");
            return null;
        }

    }

    public StudyBean findByUniqueIdentifier(String oid) {
        StudyBean sb = null;
        this.unsetTypeExpected();
        this.setTypesExpected();
        HashMap variables = new HashMap();
        variables.put(new Integer(1), oid);
        ArrayList alist = this.select(digester.getQuery("findByUniqueIdentifier"), variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            sb = (StudyBean) this.getEntityFromHashMap((HashMap) it.next());
            return sb;
        } else {
            logger.info("returning null from find by Unique Identifier...");
            return null;
        }
    }

    public StudyBean findSiteByUniqueIdentifier(String parentUniqueIdentifier, String siteUniqueIdentifier) {
        StudyBean sb = null;
        this.unsetTypeExpected();
        this.setTypesExpected();
        HashMap variables = new HashMap();
        variables.put(new Integer(1), parentUniqueIdentifier);
        variables.put(new Integer(2), siteUniqueIdentifier);
        ArrayList alist = this.select(digester.getQuery("findSiteByUniqueIdentifier"), variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            sb = (StudyBean) this.getEntityFromHashMap((HashMap) it.next());
            return sb;
        } else {
            logger.info("returning null from find by Unique Identifier...");
            return null;
        }
    }

    public StudyBean createStepTwo(StudyBean sb) {
        // UPDATE STUDY SET TYPE_ID=?, PROTOCOL_TYPE=?,PROTOCOL_DESCRIPTION=?,
        // PROTOCOL_DATE_VERIFICATION=?, PHASE=?, EXPECTED_TOTAL_ENROLLMENT=?,
        // SPONSOR=?, COLLABORATORS=?, MEDLINE_IDENTIFIER=?, URL=?,
        // URL_DESCRIPTION=?, CONDITIONS=?, KEYWORDS=?, ELIGIBILITY=?,
        // GENDER=?, AGE_MAX=?, AGE_MIN=?, HEALTHY_VOLUNTEER_ACCEPTED=?
        // WHERE STUDY_ID=?
        HashMap variables = new HashMap();
        HashMap nullVars = new HashMap();
        variables.put(new Integer(1), new Integer(sb.getType().getId()));
        variables.put(new Integer(2), sb.getProtocolTypeKey());
        variables.put(new Integer(3), sb.getProtocolDescription());

        if (sb.getProtocolDateVerification() == null) {
            nullVars.put(new Integer(4), new Integer(Types.DATE));
            variables.put(new Integer(4), null);
        } else {
            variables.put(new Integer(4), sb.getProtocolDateVerification());
        }

        variables.put(new Integer(5), sb.getPhaseKey());
        variables.put(new Integer(6), new Integer(sb.getExpectedTotalEnrollment()));
        variables.put(new Integer(7), sb.getSponsor());
        variables.put(new Integer(8), sb.getCollaborators());
        variables.put(new Integer(9), sb.getMedlineIdentifier());
        variables.put(new Integer(10), new Boolean(sb.isResultsReference()));
        variables.put(new Integer(11), sb.getUrl());
        variables.put(new Integer(12), sb.getUrlDescription());
        variables.put(new Integer(13), sb.getConditions());
        variables.put(new Integer(14), sb.getKeywords());
        variables.put(new Integer(15), sb.getEligibility());
        variables.put(new Integer(16), sb.getGenderKey());

        variables.put(new Integer(17), sb.getAgeMax());
        variables.put(new Integer(18), sb.getAgeMin());
        variables.put(new Integer(19), new Boolean(sb.getHealthyVolunteerAccepted()));
        variables.put(new Integer(20), sb.getSchemaName());
        variables.put(new Integer(21), sb.getUuid());
        variables.put(new Integer(22), sb.getEnvType());
        variables.put(new Integer(23), new Integer(sb.getId()));
        this.execute(digester.getQuery("createStepTwo"), variables, nullVars);
        return sb;
    }

    public StudyBean createStepThree(StudyBean sb) {
        HashMap variables = new HashMap();
        variables.put(new Integer(1), sb.getPurposeKey());
        variables.put(new Integer(2), sb.getAllocationKey());
        variables.put(new Integer(3), sb.getMaskingKey());
        variables.put(new Integer(4), sb.getControlKey());
        variables.put(new Integer(5), sb.getAssignmentKey());
        variables.put(new Integer(6), sb.getEndpointKey());
        variables.put(new Integer(7), sb.getInterventionsKey());
        variables.put(new Integer(8), new Integer(sb.getId()));
        this.execute(digester.getQuery("createStepThree"), variables);
        return sb;
    }

    public StudyBean createStepFour(StudyBean sb) {
        HashMap variables = new HashMap();
        variables.put(new Integer(1), sb.getDurationKey());
        variables.put(new Integer(2), sb.getSelectionKey());
        variables.put(new Integer(3), sb.getTimingKey());
        variables.put(new Integer(4), new Integer(sb.getId()));
        this.execute(digester.getQuery("createStepFour"), variables);
        return sb;
    }

    /*
     * public HashMap fillVariables(StudyBean sb) { HashMap variables = new
     * HashMap(); variables.put(new Integer(1),new
     * Integer(sb.getParentStudyId())); variables.put(new
     * Integer(2),sb.getLabel()); return variables; }
     */

    /**
     * <p>
     * getEntityFromHashMap, the method that gets the object from the database
     * query.
     */
    public Object getEntityFromHashMap(HashMap hm) {
        StudyBean eb = new StudyBean();

        // first set all the strings
        eb.setIdentifier((String) hm.get("unique_identifier"));
        eb.setName((String) hm.get("name"));
        eb.setSummary((String) hm.get("summary"));
        eb.setSecondaryIdentifier((String) hm.get("secondary_identifier"));
        eb.setPrincipalInvestigator((String) hm.get("principal_investigator"));
        eb.setFacilityName((String) hm.get("facility_name"));
        eb.setFacilityCity((String) hm.get("facility_city"));
        eb.setFacilityState((String) hm.get("facility_state"));
        eb.setFacilityZip((String) hm.get("facility_zip"));
        eb.setFacilityCountry((String) hm.get("facility_country"));
        eb.setFacilityRecruitmentStatus((String) hm.get("facility_recruitment_status"));
        eb.setFacilityContactName((String) hm.get("facility_contact_name"));
        eb.setFacilityContactDegree((String) hm.get("facility_contact_degree"));
        eb.setFacilityContactPhone((String) hm.get("facility_contact_phone"));
        eb.setFacilityContactEmail((String) hm.get("facility_contact_email"));
        eb.setProtocolType((String) hm.get("protocol_type"));
        eb.setProtocolDescription((String) hm.get("protocol_description"));
        eb.setPhase((String) hm.get("phase"));
        eb.setSponsor((String) hm.get("sponsor"));
        eb.setCollaborators((String) hm.get("collaborators"));
        eb.setMedlineIdentifier((String) hm.get("medline_identifier"));
        eb.setUrl((String) hm.get("url"));
        eb.setUrlDescription((String) hm.get("url_description"));
        eb.setConditions((String) hm.get("conditions"));
        eb.setKeywords((String) hm.get("keywords"));
        eb.setEligibility((String) hm.get("eligibility"));
        String gender = (String) hm.get("gender");
        // char[] genderarr = gender.toCharArray();
        // Character gender = (Character)hm.get("gender");
        // eb.setGender(gender.charValue());//CHAR?
        eb.setGender(gender);
        // throws null pointer exception?
        eb.setAgeMax((String) hm.get("age_max"));
        eb.setAgeMin((String) hm.get("age_min"));
        eb.setPurpose((String) hm.get("purpose"));
        eb.setAllocation((String) hm.get("allocation"));
        eb.setMasking((String) hm.get("masking"));
        eb.setControl((String) hm.get("control"));
        eb.setAssignment((String) hm.get("assignment"));
        eb.setEndpoint((String) hm.get("endpoint"));
        eb.setInterventions((String) hm.get("interventions"));
        eb.setDuration((String) hm.get("duration"));
        eb.setSelection((String) hm.get("selection"));
        eb.setTiming((String) hm.get("timing"));
        eb.setOfficialTitle((String) hm.get("official_title"));

        eb.setHealthyVolunteerAccepted(((Boolean) hm.get("healthy_volunteer_accepted")).booleanValue());
        eb.setResultsReference(((Boolean) hm.get("results_reference")).booleanValue());
        // eb.setUsingDOB(((Boolean)hm.get("collect_dob")).booleanValue());
        //eb.setDiscrepancyManagement(((Boolean)hm.get("discrepancy_management")
        // ).booleanValue());
        // next set all the ints/dates

        Integer studyId = (Integer) hm.get("study_id");
        eb.setId(studyId.intValue());
        Integer parentStudyId = (Integer) hm.get("parent_study_id");
        if (parentStudyId == null) {
            eb.setParentStudyId(0);
        } else {
            eb.setParentStudyId(parentStudyId.intValue());
        }
        Integer ownerId = (Integer) hm.get("owner_id");
        eb.setOwnerId(ownerId.intValue());
        Integer updateId = (Integer) hm.get("update_id");
        eb.setUpdaterId(updateId.intValue());
        Integer typeId = (Integer) hm.get("type_id");
        eb.setType(StudyType.get(typeId.intValue()));
        Integer statusId = (Integer) hm.get("status_id");
        eb.setStatus(Status.get(statusId.intValue()));
        Integer expecTotalEnrollment = (Integer) hm.get("expected_total_enrollment");
        eb.setExpectedTotalEnrollment(expecTotalEnrollment.intValue());
        Date dateCreated = (Date) hm.get("date_created");
        Date dateUpdated = (Date) hm.get("date_updated");
        Date datePlannedStart = (Date) hm.get("date_planned_start");
        Date datePlannedEnd = (Date) hm.get("date_planned_end");
        Date dateProtocolVerification = (Date) hm.get("protocol_date_verification");

        eb.setCreatedDate(dateCreated);
        eb.setUpdatedDate(dateUpdated);
        eb.setDatePlannedStart(datePlannedStart);
        eb.setDatePlannedEnd(datePlannedEnd);
        eb.setProtocolDateVerification(dateProtocolVerification);// added by
        // jxu
        eb.setStatus(Status.get(statusId.intValue()));
        eb.setOid((String) hm.get("oc_oid"));
        Integer oldStatusId = (Integer) hm.get("old_status_id");
        eb.setOldStatus(Status.get(oldStatusId));
        eb.setSchemaName((String) hm.get("schema_name"));
        eb.setEnvType(StudyEnvEnum.valueOf((String)hm.get("env_type")));
        return eb;
    }

    public Collection findAllByUser(String username) {
        this.unsetTypeExpected();
        this.setTypesExpected();
        HashMap variables = new HashMap();
        variables.put(new Integer(1), username);
        ArrayList alist = this.select(digester.getQuery("findAllByUser"), variables);
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            StudyBean eb = (StudyBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;
    }

    public ArrayList<Integer> getStudyIdsByCRF(int crfId) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        HashMap<Integer, Integer> variables = new HashMap<Integer, Integer>();
        variables.put(1, crfId);
        ArrayList alist = this.select(digester.getQuery("getStudyIdsByCRF"), variables);
        ArrayList<Integer> al = new ArrayList<Integer>();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            HashMap h = (HashMap) it.next();
            al.add((Integer) h.get("study_id"));
        }
        return al;
    }

    // YW 10-18-2007
    public Collection findAllByUserNotRemoved(String username) {
        this.unsetTypeExpected();
        this.setTypesExpected();
        HashMap variables = new HashMap();
        variables.put(new Integer(1), username);
        ArrayList alist = this.select(digester.getQuery("findAllByUserNotRemoved"), variables);
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            StudyBean eb = (StudyBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;
    }

    public ArrayList findAllByStatus(Status status) {
        this.unsetTypeExpected();
        this.setTypesExpected();
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(status.getId()));
        String sql = digester.getQuery("findAllByStatus");
        ArrayList alist = this.select(sql, variables);
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            StudyBean eb = (StudyBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;
    }

    public Collection findAll() {

        return findAllByLimit(false);
    }

    public Collection findAllByLimit(boolean isLimited) {
        this.setTypesExpected();
        String sql = null;
        // Updated for ORACLE and PGSQL compatibility
        if (isLimited) {
            if (CoreResources.getDBName().equals("oracle")) {
                sql = digester.getQuery("findAll") + " where ROWNUM <=5";
            } else {
                sql = digester.getQuery("findAll") + " limit 5";
            }
        } else {
            sql = digester.getQuery("findAll");
        }
        ArrayList alist = this.select(sql);
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            StudyBean eb = (StudyBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;
    }

    public Collection findAllParents() {
        this.setTypesExpected();

        String sql = digester.getQuery("findAllParents");
        ArrayList alist = this.select(sql);
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            StudyBean eb = (StudyBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;
    }

    /**
     * isAParent(), finds out whether or not a study is a parent.
     * 
     * @return a boolean
     */
    public boolean isAParent(int studyId) {
        boolean ret = false;
        Collection col = findAllByParent(studyId);
        if (col != null && col.size() > 0) {
            ret = true;
        }
        return ret;
    }

    public Collection findAllByParent(int parentStudyId) {
        return findAllByParentAndLimit(parentStudyId, false);
    }

    public Collection findAllByParentAndLimit(int parentStudyId, boolean isLimited) {
        this.setTypesExpected();
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(parentStudyId));
        ArrayList alist = null;
        if (isLimited) {
            alist = this.select(digester.getQuery("findAllByParentLimit5"), variables);
        } else {
            alist = this.select(digester.getQuery("findAllByParent"), variables);
        }
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            StudyBean eb = (StudyBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;

    }

    public Collection findAll(int studyId) {
        this.setTypesExpected();
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(studyId));
        variables.put(new Integer(2), new Integer(studyId));
        ArrayList alist = null;
        alist = this.select(digester.getQuery("findAllByStudyId"), variables);
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            StudyBean eb = (StudyBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;

    }

    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();

        return al;
    }

    public EntityBean findByPK(int ID) {
        StudyBean eb = new StudyBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(ID));

        String sql = digester.getQuery("findByPK");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            eb = (StudyBean) this.getEntityFromHashMap((HashMap) it.next());
        }

        return eb;
    }

    /*
     * added 02/2008, tbh
     */
    public EntityBean findByName(String name) {
        StudyBean eb = new StudyBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), name);

        String sql = digester.getQuery("findByName");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            eb = (StudyBean) this.getEntityFromHashMap((HashMap) it.next());
        }

        return eb;
    }

    /**
     * deleteTestOnly, used only to clean up after unit testing
     * 
     * @param name
     */
    public void deleteTestOnly(String name) {
        HashMap variables = new HashMap();
        variables.put(new Integer(1), name);
        this.execute(digester.getQuery("deleteTestOnly"), variables);
    }

    public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();

        return al;
    }

    public Collection findAllByPermission(Object objCurrentUser, int intActionType) {
        ArrayList al = new ArrayList();

        return al;
    }

    /**
     * Only for use by getChildrenByParentIds
     * 
     * @param answer
     * @param parentId
     * @param child
     * @return
     */
    private HashMap addChildToParent(HashMap answer, int parentId, StudyBean child) {
        Integer key = new Integer(parentId);
        ArrayList children = (ArrayList) answer.get(key);

        if (children == null) {
            children = new ArrayList();
        }

        children.add(child);
        answer.put(key, children);

        return answer;
    }

    /**
     * @param allStudies
     *            The result of findAll().
     * @return A HashMap where the keys are Integers whose intValue are studyIds
     *         and the values are ArrayLists; each element of the ArrayList is a
     *         StudyBean representing a child of the study whose id is the key
     *         <p>
     *         e.g., if A has children B and C, then this will return a HashMap
     *         h where h.get(A.getId()) returns an ArrayList whose elements are
     *         B and C
     */
    public HashMap getChildrenByParentIds(ArrayList allStudies) {
        HashMap answer = new HashMap();

        if (allStudies == null) {
            return answer;
        }

        for (int i = 0; i < allStudies.size(); i++) {
            StudyBean study = (StudyBean) allStudies.get(i);

            int parentStudyId = study.getParentStudyId();
            if (parentStudyId > 0) { // study is a child
                answer = addChildToParent(answer, parentStudyId, study);
            }
        }

        return answer;
    }

    public Collection<Integer> findAllSiteIdsByStudy(StudyBean study) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);// sid
        HashMap<Integer, Integer> variables = new HashMap<Integer, Integer>();
        variables.put(new Integer(1), new Integer(study.getId()));
        variables.put(new Integer(2), new Integer(study.getId()));
        ArrayList alist = this.select(digester.getQuery("findAllSiteIdsByStudy"), variables);
        ArrayList<Integer> al = new ArrayList<Integer>();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            HashMap h = (HashMap) it.next();
            al.add((Integer) h.get("study_id"));
        }
        return al;
    }

    public Collection<Integer> findOlnySiteIdsByStudy(StudyBean study) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);// sid
        HashMap<Integer, Integer> variables = new HashMap<Integer, Integer>();
        variables.put(new Integer(1), new Integer(study.getId()));
        ArrayList alist = this.select(digester.getQuery("findOlnySiteIdsByStudy"), variables);
        ArrayList<Integer> al = new ArrayList<Integer>();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            HashMap h = (HashMap) it.next();
            al.add((Integer) h.get("study_id"));
        }
        return al;
    }

    public StudyBean updateSitesStatus(StudyBean sb) {
        HashMap variables = new HashMap();
        HashMap nullVars = new HashMap();

        variables.put(new Integer(1), sb.getStatus().getId());
        variables.put(new Integer(2), sb.getOldStatus().getId());
        variables.put(new Integer(3), sb.getId());

        this.execute(digester.getQuery("updateSitesStatus"), variables, nullVars);
        return sb;
    }

    public StudyBean updateStudyStatus(StudyBean sb) {
        HashMap variables = new HashMap();
        HashMap nullVars = new HashMap();

        variables.put(new Integer(1), sb.getStatus().getId());
        variables.put(new Integer(2), sb.getOldStatus().getId());
        variables.put(new Integer(3), sb.getId());


        this.execute(digester.getQuery("updateStudyStatus"), variables, nullVars);
        return sb;
    }

    public StudyBean findByStudySubjectId(int studySubjectId) {
        StudyBean sb = new StudyBean();
        HashMap variables = new HashMap();
        this.setTypesExpected();

        variables.put(new Integer(1), studySubjectId);

        // >> tbh
        // String sql = digester.getQuery("findByStudySubjectId");
        // ArrayList alist = this.select(sql, variables);
        // Iterator it = alist.iterator();

        // if (it.hasNext()) {
        // sb = (StudyBean) this.getEntityFromHashMap((HashMap) it.next());
        // }
        // was not returning anything?
        ArrayList alist = this.select(digester.getQuery("findByStudySubjectId"), variables);
        Iterator it = alist.iterator();
        if (it.hasNext()) {
            sb = (StudyBean) this.getEntityFromHashMap((HashMap) it.next());
        }
        return sb;
    }

    public Collection findAllByParentStudyIdOrderedByIdAsc(int parentStudyId) {
        this.setTypesExpected();
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(parentStudyId));
        variables.put(new Integer(2), new Integer(parentStudyId));
        ArrayList alist = this.select(digester.getQuery("findAllByParentStudyIdOrderedByIdAsc"), variables);
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            StudyBean eb = (StudyBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;

    }
    public StudyBean getPublicStudy (String ocId) {
        StudyBean study = findByPublicOid(ocId);
        return study;
    }

}
