package core.org.akaza.openclinica.domain.datamap;

// default package
// Generated Aug 8, 2013 11:32:37 AM by Hibernate Tools 3.4.0.CR1

import javax.persistence.Column;
import javax.persistence.Embeddable;

import core.org.akaza.openclinica.domain.DataMapDomainObject;

/**
 * DnEventCrfMapId generated by hbm2java
 */
@Embeddable
public class DnEventCrfMapId extends DataMapDomainObject {

	private Integer eventCrfId;
	private Integer discrepancyNoteId;
	private String columnName;

	public DnEventCrfMapId() {
	}

	public DnEventCrfMapId(Integer eventCrfId, Integer discrepancyNoteId,
			String columnName) {
		this.eventCrfId = eventCrfId;
		this.discrepancyNoteId = discrepancyNoteId;
		this.columnName = columnName;
	}

	@Column(name = "event_crf_id")
	public Integer getEventCrfId() {
		return this.eventCrfId;
	}

	public void setEventCrfId(Integer eventCrfId) {
		this.eventCrfId = eventCrfId;
	}

	@Column(name = "discrepancy_note_id")
	public Integer getDiscrepancyNoteId() {
		return this.discrepancyNoteId;
	}

	public void setDiscrepancyNoteId(Integer discrepancyNoteId) {
		this.discrepancyNoteId = discrepancyNoteId;
	}

	@Column(name = "column_name")
	public String getColumnName() {
		return this.columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof DnEventCrfMapId))
			return false;
		DnEventCrfMapId castOther = (DnEventCrfMapId) other;

		return ((this.getEventCrfId() == castOther.getEventCrfId()) || (this
				.getEventCrfId() != null && castOther.getEventCrfId() != null && this
				.getEventCrfId().equals(castOther.getEventCrfId())))
				&& ((this.getDiscrepancyNoteId() == castOther
						.getDiscrepancyNoteId()) || (this
						.getDiscrepancyNoteId() != null
						&& castOther.getDiscrepancyNoteId() != null && this
						.getDiscrepancyNoteId().equals(
								castOther.getDiscrepancyNoteId())))
				&& ((this.getColumnName() == castOther.getColumnName()) || (this
						.getColumnName() != null
						&& castOther.getColumnName() != null && this
						.getColumnName().equals(castOther.getColumnName())));
	}

	public int hashCode() {
		int result = 17;

		result = 37
				* result
				+ (getEventCrfId() == null ? 0 : this.getEventCrfId()
						.hashCode());
		result = 37
				* result
				+ (getDiscrepancyNoteId() == null ? 0 : this
						.getDiscrepancyNoteId().hashCode());
		result = 37
				* result
				+ (getColumnName() == null ? 0 : this.getColumnName()
						.hashCode());
		return result;
	}


}
