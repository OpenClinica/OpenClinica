/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.dao.managestudy;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.akaza.openclinica.bean.core.DiscrepancyNoteType;
import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.service.managestudy.ViewNotesFilterCriteria;
import org.akaza.openclinica.service.managestudy.ViewNotesSortCriteria;
import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 *
 */
public class ViewNotesDaoImpl extends JdbcDaoSupport implements ViewNotesDao {

    private static final RowMapper<DiscrepancyNoteBean> DISCREPANCY_NOTE_ROW_MAPPER =
            new RowMapper<DiscrepancyNoteBean>() {

        public DiscrepancyNoteBean mapRow(ResultSet rs, int rowNum) throws SQLException {
            DiscrepancyNoteBean b = new DiscrepancyNoteBean();
            b.setId(rs.getInt("discrepancy_note_id"));
            b.setStudyId(rs.getInt("study_id"));
            StudySubjectBean studySubjectBean = new StudySubjectBean();
            studySubjectBean.setId(b.getStudyId());
            studySubjectBean.setLabel(rs.getString("label"));
            b.setStudySub(studySubjectBean);
            b.setDiscrepancyNoteTypeId(rs.getInt("discrepancy_note_type_id"));
            b.setDisType(DiscrepancyNoteType.get(b.getDiscrepancyNoteTypeId()));
            b.setResolutionStatusId(rs.getInt("resolution_status_id"));
            b.setResStatus(ResolutionStatus.get(b.getResolutionStatusId()));
            b.setSiteId(rs.getString("site_id"));
            b.setDays(rs.getInt("days"));
            b.setAge(rs.getInt("age"));
            b.setEventName(rs.getString("event_name"));
            b.setCrfName(rs.getString("crf_name"));
            b.setEntityName(rs.getString("entity_name"));
            b.setEntityValue(rs.getString("value"));
            b.setDescription(rs.getString("description"));
            String userName = rs.getString("user_name");
            if (!StringUtils.isEmpty(userName)) {
                UserAccountBean userBean = new UserAccountBean();
                userBean.setName(userName);
                userBean.setFirstName(rs.getString("first_name"));
                userBean.setLastName(rs.getString("last_name"));
                b.setAssignedUser(userBean);
            }
            return b;
        }
    };

    public List<DiscrepancyNoteBean> listNotes(StudyBean currentStudy, ViewNotesFilterCriteria filter,
            ViewNotesSortCriteria sort) {
        List<DiscrepancyNoteBean> result = getJdbcTemplate().query(listNotesSql(), listNotesArguments(),
                DISCREPANCY_NOTE_ROW_MAPPER);
        return result;
    }

    protected String listNotesSql() {
        // TODO Implement
        //return "select * from view_dn_item_data where user_name is not null limit 50";
        return "select * from view_dn_item_data limit 50";
    }

    protected Object[] listNotesArguments() {
        // TODO Implement
        return new Object[0];
    }

}
