/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package core.org.akaza.openclinica.dao.managestudy;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import core.org.akaza.openclinica.bean.core.DataEntryStage;
import core.org.akaza.openclinica.bean.core.DiscrepancyNoteType;
import core.org.akaza.openclinica.bean.core.ResolutionStatus;
import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.dao.QueryStore;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.service.DiscrepancyNotesSummary;
import core.org.akaza.openclinica.service.managestudy.ViewNotesFilterCriteria;
import core.org.akaza.openclinica.service.managestudy.ViewNotesSortCriteria;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import javax.sql.DataSource;

/**
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 * @author Leonel Gayard (leonel.gayard@gmail.com)
 */
public class ViewNotesDaoImpl extends NamedParameterJdbcDaoSupport implements ViewNotesDao {

    private static final Logger LOG = LoggerFactory.getLogger(ViewNotesDaoImpl.class);

    private static final String QUERYSTORE_FILE = "viewnotes";
    private static DataSource dataSource;

    private static final String EVENT_NAME = "event_name";
    private static final String CRF_NAME = "crf_name";
    private static final String ENTITY_NAME = "entity_name";
    private static final String ENTITY_VALUE = "value";
    private static final String SPACE = " ";

    private QueryStore queryStore;

    private static final RowMapper<DiscrepancyNoteBean> DISCREPANCY_NOTE_ROW_MAPPER = new RowMapper<DiscrepancyNoteBean>() {

        @Override
        public DiscrepancyNoteBean mapRow(ResultSet rs, int rowNum) throws SQLException {
            DiscrepancyNoteBean b = new DiscrepancyNoteBean();
            b.setId(rs.getInt("discrepancy_note_id"));
            b.setColumn(rs.getString("column_name"));
            b.setStudyId(rs.getInt("study_id"));
            b.setSubjectId(rs.getInt("study_subject_id"));
            StudySubjectBean studySubjectBean = new StudySubjectBean();
            studySubjectBean.setId(b.getSubjectId());
            studySubjectBean.setLabel(rs.getString("label"));
            studySubjectBean.setStatus(Status.get(rs.getInt("ss_status_id")));
            b.setStudySub(studySubjectBean);
            b.setDiscrepancyNoteTypeId(rs.getInt("discrepancy_note_type_id"));
            b.setDisType(DiscrepancyNoteType.get(b.getDiscrepancyNoteTypeId()));
            b.setResolutionStatusId(rs.getInt("resolution_status_id"));
            b.setResStatus(ResolutionStatus.get(b.getResolutionStatusId()));
            b.setSiteId(rs.getString("unique_identifier"));
            b.setCreatedDate(rs.getDate("date_created"));
            b.setUpdatedDate(rs.getDate("date_updated"));
            b.setDays(rs.getInt("days"));
            if (rs.wasNull()) {
                b.setDays(null);
            }
            b.setAge(rs.getInt("age"));
            if (rs.wasNull()) {
                b.setAge(null);
            }
            b.setEntityType(rs.getString("entity_type"));
            b.setDescription(rs.getString("description"));
            b.setDetailedNotes(rs.getString("detailed_notes"));
            b.setNumChildren(rs.getInt("total_notes"));

            String userName = rs.getString("assigned_user_name");
            if (!StringUtils.isEmpty(userName)) {
                UserAccountBean userBean = new UserAccountBean();
                userBean.setName(userName);
                userBean.setFirstName(rs.getString("assigned_first_name"));
                userBean.setLastName(rs.getString("assigned_last_name"));
                b.setAssignedUser(userBean);
            }
            String ownerUserName = rs.getString("owner_user_name");
            if (!StringUtils.isEmpty(ownerUserName)) {
                UserAccountBean userBean = new UserAccountBean();
                userBean.setName(ownerUserName);
                userBean.setFirstName(rs.getString("owner_first_name"));
                userBean.setLastName(rs.getString("owner_last_name"));
                b.setOwner(userBean);
            }

            // The discrepancy note's item ID is not null only when type =
            // 'itemData'
            if (b.getEntityType().equals("itemData")) {
                b.setEntityId(rs.getInt("item_data_id"));
                b.setSubjectId(rs.getInt("study_subject_id"));
            }
            if (b.getEntityType().equals("studyEvent")) {
                b.setEntityName(rs.getString("column_name"));
                b.setEntityId(rs.getInt("study_event_id"));
            }


            b.setThreadUuid(rs.getString("thread_uuid"));
            b.setThreadNumber(rs.getInt("thread_number"));

            return b;
        }
    };

    @Override
    public List<DiscrepancyNoteBean> findAllDiscrepancyNotes(Study currentStudy, ViewNotesFilterCriteria filter, ViewNotesSortCriteria sort, List<String> userTags) {
        Map<String, Object> arguments = listNotesArguments(currentStudy, userTags);
        List<DiscrepancyNoteBean> result =
                getNamedParameterJdbcTemplate().query(listNotesSql(filter, sort, arguments, currentStudy.isSite(), userTags), arguments,
                        DISCREPANCY_NOTE_ROW_MAPPER);
        return result;
    }

    private void addUserTagsConstraint(StringBuilder terms, List<String> userTags) {
        if (CollectionUtils.isEmpty(userTags)) {
            terms.append(queryStore.query(QUERYSTORE_FILE, "findAllDiscrepancyNotes.filter.permissionTagsEmptyUserTags"));
        } else {
            terms.append(queryStore.query(QUERYSTORE_FILE, "findAllDiscrepancyNotes.filter.permissionTags"));
        }
    }

    @Override
    public DiscrepancyNotesSummary calculateNotesSummary(Study currentStudy, ViewNotesFilterCriteria filter, boolean isQueryOnly, List<String> userTags) {
        Map<String, Object> arguments = new HashMap<String, Object>(2);
        arguments.put("studyId", currentStudy.getStudyId());
        arguments.put("userTags", userTags);
        boolean isSite=currentStudy.isSite();

        StringBuilder filteredTerms= new StringBuilder();
        filteredTerms.append(queryStore.query(QUERYSTORE_FILE, "discrepancyNotes.count.select"));
        filteredTerms.append(filterSubQuery(filter,arguments,isSite,userTags));
        filteredTerms.append(queryStore.query(QUERYSTORE_FILE, "discrepancyNotes.count.group"));

        String query = filteredTerms.toString();

        final Integer[][] result = new Integer[ResolutionStatus.list.size() + 1][DiscrepancyNoteType.list.size() + 1];

        LOG.debug("SQL: " + query);
        getNamedParameterJdbcTemplate().query(query, arguments, new RowMapper<Void>() {
            // Using 'void' as return type as the extractor uses the
            // pre-populated 'result' object
            @Override
            public Void mapRow(ResultSet rs, int rowNum) throws SQLException {
                result[rs.getInt("resolution_status_id")][rs.getInt("discrepancy_note_type_id")] = rs.getInt("total");
                return null;
            }
        });

        return new DiscrepancyNotesSummary(result);
    }

    protected String listNotesSql(ViewNotesFilterCriteria filter, ViewNotesSortCriteria sort, Map<String, Object> arguments, boolean isSite, List<String> userTags) {
        StringBuilder filteredTerms= filterSubQuery(filter,arguments,isSite,userTags);

        // Append sort criteria
        if (sort != null) {
            String sortQuery ="";
            if (!sort.getSorters().isEmpty()) {
                for (String property : sort.getSorters().keySet()) {
                    if (property.startsWith("SE_") && property.contains(".F_") && property.contains(".I_")) {
                        filteredTerms= sortSubQuery(property,filteredTerms);
                        filteredTerms.append(SPACE);
                        filteredTerms.append(sort.getSorters().get(property));
                        filteredTerms.append(SPACE);

                    }else {
                        filteredTerms.append(queryStore.query(QUERYSTORE_FILE, "findAllDiscrepancyNotes.orderby"));
                        sortQuery = queryStore.query(QUERYSTORE_FILE, "findAllDiscrepancyNotes.sort." + property);
                        filteredTerms.append(sortQuery);
                        filteredTerms.append(SPACE);
                        filteredTerms.append(sort.getSorters().get(property));
                        filteredTerms.append(SPACE);
                    }
                }
            } else {
                // set default sorting OC-9405
                String[] defaultSort = { "days","unique_identifier", "label", "thread_number"};
                filteredTerms.append(queryStore.query(QUERYSTORE_FILE, "findAllDiscrepancyNotes.orderby"));

                int count = 0;
                for (String sortKey : defaultSort) {
                    count++;
                    sortQuery = queryStore.query(QUERYSTORE_FILE, "findAllDiscrepancyNotes.sort." + sortKey);
                    filteredTerms.append(sortQuery);
                    filteredTerms.append(" ASC ");
                    if (count < defaultSort.length) {
                        filteredTerms.append(",");
                    }
                }
            }
        }

        if (filter.getPageNumber() != null && filter.getPageSize() != null) {
            if (queryStore.hasQuery(QUERYSTORE_FILE, "findAllDiscrepancyNotes.paginationPrefix")) {
                filteredTerms.append( queryStore.query(QUERYSTORE_FILE, "findAllDiscrepancyNotes.paginationPrefix"));
                filteredTerms.append(queryStore.query(QUERYSTORE_FILE, "findAllDiscrepancyNotes.paginationSuffix"));
                arguments.put("first", 1 + ((filter.getPageNumber() - 1) * filter.getPageSize()));
                arguments.put("last", filter.getPageSize() * filter.getPageNumber());
            } else {
                // Limit number of results (pagination)
                filteredTerms.append(queryStore.query(QUERYSTORE_FILE, "findAllDiscrepancyNotes.limit"));
                arguments.put("limit", filter.getPageSize());
                arguments.put("offset", (filter.getPageNumber() - 1) * filter.getPageSize());
            }
        }
        String result = filteredTerms.toString();
        LOG.debug("SQL: " + result);
        return result;
    }

    protected Map<String, Object> listNotesArguments(Study currentStudy, List<String> userTags) {
        Map<String, Object> arguments = new HashMap<String, Object>();
        arguments.put("studyId", currentStudy.getStudyId());
        arguments.put("limit", 50);
        arguments.put("userTags", userTags);
        return arguments;
    }

    public QueryStore getQueryStore() {
        return queryStore;
    }

    public void setQueryStore(QueryStore queryStore) {
        this.queryStore = queryStore;
    }

    private void filterSet(ViewNotesFilterCriteria filter, Map<String, Object> arguments, StringBuilder terms, boolean isQueryOnly) {
        if (filter != null) {
            for (String filterKey : filter.getFilters().keySet()) {
                String filterQuery = "";
                if (!(filterKey.startsWith("SE_") && filterKey.contains(".F_") && filterKey.contains(".I_"))) {
                    if (filterKey.equals(EVENT_NAME) || filterKey.equals(CRF_NAME) || filterKey.equals(ENTITY_NAME) || filterKey.equals(ENTITY_VALUE)) {
                        String value = "%" + filter.getFilters().get(filterKey).toString().toUpperCase() + "%";
                        switch (filterKey) {
                            case EVENT_NAME:
                                filterQuery = filterQuery + " and UPPER(sed.name) like \'" + value + "\'";
                                break;
                            case CRF_NAME:
                                filterQuery = filterQuery + " and UPPER(c.name) like \'" + value + "\'";
                                break;
                            case ENTITY_NAME:
                                filterQuery = filterQuery + " and UPPER(i.name) like \'" + value + "\'";
                                break;
                            case ENTITY_VALUE:
                                filterQuery = filterQuery + " and UPPER(id.value) like \'" + value + "\'";
                                break;
                            default:
                        }
                    } else {
                        filterQuery = queryStore.query(QUERYSTORE_FILE, "findAllDiscrepancyNotes.filter." + filterKey);

                        if (filterKey.equalsIgnoreCase("discrepancy_note_type_id")) {
                            // summary notes only count query type
                            if (isQueryOnly) {
                                arguments.put(filterKey, 3);
                            } else {
                                arguments.put(filterKey, filter.getFilters().get(filterKey));
                            }
                        } else {
                            arguments.put(filterKey, filter.getFilters().get(filterKey));
                        }
                    }

                    terms.append(filterQuery);

                }
            }
            for (String filterKey : filter.getFilters().keySet()) {
                String filterQuery = "";

                if (filterKey.startsWith("SE_") && filterKey.contains(".F_") && filterKey.contains(".I_")) {
                    String sedOid = filterKey.split("\\.")[0];
                    String formOid = filterKey.split("\\.")[1];
                    String itemOid = filterKey.split("\\.")[2];
                    String value = "%" + filter.getFilters().get(filterKey).toString().toUpperCase() + "%";

                    filterQuery = filterQuery + " INTERSECT ";
                    filterQuery = filterQuery + queryStore.query(QUERYSTORE_FILE, "discrepancyNotes.main.select");
                    filterQuery = filterQuery + queryStore.query(QUERYSTORE_FILE, "discrepancyNotes.main.custom.join");
                    filterQuery = filterQuery + queryStore.query(QUERYSTORE_FILE, "discrepancyNotes.main.where");
                    filterQuery = filterQuery + " and sed.oc_oid=\'" + sedOid + "\' and c.oc_oid = \'" + formOid + "\' and i.oc_oid= \'" + itemOid + "\' and UPPER(id.value) like \'" + value + "\'";

                    terms.append(filterQuery);
                }
            }
        }
    }

    private StringBuilder filterSubQuery(ViewNotesFilterCriteria filter, Map<String, Object> arguments, boolean isSite, List<String> userTags) {
        StringBuilder terms = new StringBuilder();
        terms.append(queryStore.query(QUERYSTORE_FILE, "discrepancyNotes.main.select"));
        terms.append(queryStore.query(QUERYSTORE_FILE, "discrepancyNotes.main.join"));
        terms.append(queryStore.query(QUERYSTORE_FILE, "discrepancyNotes.main.where"));

        if (!isSite) {
            terms.append(queryStore.query(QUERYSTORE_FILE, "findAllDiscrepancyNotes.filter.studyHideCrf"));
        } else {
            terms.append(queryStore.query(QUERYSTORE_FILE, "findAllDiscrepancyNotes.filter.siteHideCrf"));
        }
        addUserTagsConstraint(terms, userTags);

        filterSet(filter, arguments, terms, false);

        return terms;
    }

    private StringBuilder sortSubQuery(String property,StringBuilder filteredTerms){
        StringBuilder sortedTerms= new StringBuilder();
        String sedOid = property.split("\\.")[0];
        String formOid = property.split("\\.")[1];
        String itemOid = property.split("\\.")[2];
        String itemDataType = property.split("\\.")[3];


        String partialScript = "(" +
                "select jj.* ,'' val from ("+filteredTerms.toString()+")jj \n" +
                "\n" +
                " Except " +
                " \n" +
                " select jj.* ,'' val from  "
                + subScript(sedOid, formOid, itemOid,filteredTerms)+
                " ) " +
                "\n" +
                " UNION " +
                "\n" +
                " select  jj.* ,id.value val from "
                + subScript(sedOid, formOid, itemOid,filteredTerms);


        partialScript = "select * from ( " + partialScript + " )kk " + sortByItemType(itemDataType);

        sortedTerms.append(partialScript);

        return sortedTerms;

    }


    private String subScript(String sedOid, String formOid, String itemOid,StringBuilder filteredTerms) {
        return
                "   item_data id JOIN event_crf ec ON id.event_crf_id=ec.event_crf_id\n" +
                        "                                          JOIN study_event se ON se.study_event_id=ec.study_event_id\n" +
                        "                                          JOIN crf_version cv ON cv.crf_version_id=ec.crf_version_id\n" +
                        "                                          JOIN crf c ON c.crf_id=cv.crf_id\n" +
                        "                                          JOIN item i ON i.item_id=id.item_id\n" +
                        "                                          JOIN study_event_definition sed ON sed.study_event_definition_id=se.study_event_definition_id\n" +
                        "                                          JOIN  ("+filteredTerms.toString()+") jj ON jj.study_subject_id=se.study_subject_id \n" +
                        "                                         \n" +
                        "                                          where\n" +
                        "                                          jj.entity_type='itemData' AND\n"+
                        "                                          sed.oc_oid=\'" + sedOid + "\' AND\n" +
                        "                                          c.oc_oid=\'" + formOid + "\' AND\n" +
                        "                                          i.oc_oid=\'" + itemOid + "\' ";
    }

    private String sortByItemType(String itemDataType) {
        String append = "";
        if (itemDataType.equalsIgnoreCase("Floating")) {
            append = " order by  cast(Nullif(val,'' )as double precision)  ";
        } else if (itemDataType.equalsIgnoreCase("Integer")) {
            append = " order by  cast(Nullif(val,'' )as integer)  ";
        } else if (itemDataType.equalsIgnoreCase("date")) {
            append = " order by  cast(Nullif(val,'' )as date)  ";
        } else{
            append = " order by nullif(val,'') ";
        }
        return append;
    }
}