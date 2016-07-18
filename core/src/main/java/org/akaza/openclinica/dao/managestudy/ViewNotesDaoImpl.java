/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.dao.managestudy;

import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.DiscrepancyNoteType;
import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.dao.QueryStore;
import org.akaza.openclinica.service.DiscrepancyNotesSummary;
import org.akaza.openclinica.service.managestudy.ViewNotesFilterCriteria;
import org.akaza.openclinica.service.managestudy.ViewNotesSortCriteria;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 * @author Leonel Gayard (leonel.gayard@gmail.com)
 */
public class ViewNotesDaoImpl extends NamedParameterJdbcDaoSupport implements ViewNotesDao {

    private static final Logger LOG = LoggerFactory.getLogger(ViewNotesDaoImpl.class);

    private static final String QUERYSTORE_FILE = "viewnotes";

    private QueryStore queryStore;

    private static final RowMapper<DiscrepancyNoteBean> DISCREPANCY_NOTE_ROW_MAPPER = new RowMapper<DiscrepancyNoteBean>() {

        @Override
        public DiscrepancyNoteBean mapRow(ResultSet rs, int rowNum) throws SQLException {
            DiscrepancyNoteBean b = new DiscrepancyNoteBean();
            b.setId(rs.getInt("discrepancy_note_id"));
            b.setEntityId(rs.getInt("entity_id"));
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
            b.setSiteId(rs.getString("site_id"));
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
            b.setEventName(rs.getString("event_name"));
            b.setEventStart(rs.getDate("date_start"));
            b.setCrfName(rs.getString("crf_name"));
            int statusId = rs.getInt("status_id");
            if (statusId != 0) {
                b.setCrfStatus(DataEntryStage.get(statusId).getName());
            }
            b.setEntityName(rs.getString("entity_name"));
            b.setEntityValue(rs.getString("value"));
            b.setEntityType(rs.getString("entity_type"));
            b.setDescription(rs.getString("description"));
            b.setDetailedNotes(rs.getString("detailed_notes"));
            b.setNumChildren(rs.getInt("total_notes"));

            String userName = rs.getString("user_name");
            if (!StringUtils.isEmpty(userName)) {
                UserAccountBean userBean = new UserAccountBean();
                userBean.setName(userName);
                userBean.setFirstName(rs.getString("first_name"));
                userBean.setLastName(rs.getString("last_name"));
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
                b.setItemId(rs.getInt("item_id"));
            }

            return b;
        }
    };

    @Override
    public List<DiscrepancyNoteBean> findAllDiscrepancyNotes(StudyBean currentStudy, ViewNotesFilterCriteria filter, ViewNotesSortCriteria sort) {
        Map<String, Object> arguments = listNotesArguments(currentStudy);
        List<DiscrepancyNoteBean> result =
            getNamedParameterJdbcTemplate().query(listNotesSql(filter, sort, arguments, currentStudy.isSite(currentStudy.getParentStudyId())), arguments,
                    DISCREPANCY_NOTE_ROW_MAPPER);
        return result;
    }

    @Override
    public DiscrepancyNotesSummary calculateNotesSummary(StudyBean currentStudy, ViewNotesFilterCriteria filter) {
        Map<String, Object> arguments = new HashMap<String, Object>(2);
        arguments.put("studyId", currentStudy.getId());

        List<String> terms = new ArrayList<String>();
        terms.add(queryStore.query(QUERYSTORE_FILE, "countDiscrepancyNotes.main"));

      //  terms.add(queryStore.query(QUERYSTORE_FILE, "findAllDiscrepancyNotes.filter.studyHideCrf"));
        if (currentStudy.isSite(currentStudy.getParentStudyId())) {
            terms.add(queryStore.query(QUERYSTORE_FILE, "findAllDiscrepancyNotes.filter.siteHideCrf"));
            
        }

        // Reuse the filter criteria from #findAllDiscrepancyNotes, as both
        // queries load data from the same view
        if (filter != null) {
            for (String filterKey : filter.getFilters().keySet()) {
                String filterQuery = queryStore.query(QUERYSTORE_FILE, "findAllDiscrepancyNotes.filter." + filterKey);
                terms.add(filterQuery);
                arguments.put(filterKey, filter.getFilters().get(filterKey));
            }
        }

        terms.add(queryStore.query(QUERYSTORE_FILE, "countDiscrepancyNotes.group"));
        String query = StringUtils.join(terms, ' ');

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

    protected String listNotesSql(ViewNotesFilterCriteria filter, ViewNotesSortCriteria sort, Map<String, Object> arguments, boolean isSite) {
        List<String> terms = new ArrayList<String>();
        terms.add(queryStore.query(QUERYSTORE_FILE, "findAllDiscrepancyNotes.main"));

        if(!isSite)
        {
        	//terms.add(queryStore.query(QUERYSTORE_FILE, "findAllDiscrepancyNotes.filter.studyHideCrf"));
        }
       
        if (isSite) {
            terms.add(queryStore.query(QUERYSTORE_FILE, "findAllDiscrepancyNotes.filter.siteHideCrf"));
        }

        // Append query filters
        if (filter != null) {
            for (String filterKey : filter.getFilters().keySet()) {
                String filterQuery = queryStore.query(QUERYSTORE_FILE, "findAllDiscrepancyNotes.filter." + filterKey);
                terms.add(filterQuery);
                arguments.put(filterKey, filter.getFilters().get(filterKey));
            }
        }

        // Append sort criteria
        if (sort != null) {
            if (!sort.getSorters().isEmpty()) {
                terms.add(queryStore.query(QUERYSTORE_FILE, "findAllDiscrepancyNotes.orderby"));
            }
            for (String sortKey : sort.getSorters().keySet()) {
                String sortQuery = queryStore.query(QUERYSTORE_FILE, "findAllDiscrepancyNotes.sort." + sortKey);
                terms.add(sortQuery);
                terms.add(sort.getSorters().get(sortKey));
            }
        }

        if (filter.getPageNumber() != null && filter.getPageSize() != null) {
            if (queryStore.hasQuery(QUERYSTORE_FILE, "findAllDiscrepancyNotes.paginationPrefix")) {
                terms.add(0, queryStore.query(QUERYSTORE_FILE, "findAllDiscrepancyNotes.paginationPrefix"));
                terms.add(queryStore.query(QUERYSTORE_FILE, "findAllDiscrepancyNotes.paginationSuffix"));
                arguments.put("first", 1 + ((filter.getPageNumber() - 1) * filter.getPageSize()));
                arguments.put("last", filter.getPageSize() * filter.getPageNumber());
            } else {
                // Limit number of results (pagination)
                terms.add(queryStore.query(QUERYSTORE_FILE, "findAllDiscrepancyNotes.limit"));
                arguments.put("limit", filter.getPageSize());
                arguments.put("offset", (filter.getPageNumber() - 1) * filter.getPageSize());
            }
        }

        String result = StringUtils.join(terms, ' ');
        LOG.debug("SQL: " + result);
        return result;
    }

    protected Map<String, Object> listNotesArguments(StudyBean currentStudy) {
        Map<String, Object> arguments = new HashMap<String, Object>();
        arguments.put("studyId", currentStudy.getId());
        arguments.put("limit", 50);
        return arguments;
    }

    public QueryStore getQueryStore() {
        return queryStore;
    }

    public void setQueryStore(QueryStore queryStore) {
        this.queryStore = queryStore;
    }

}
