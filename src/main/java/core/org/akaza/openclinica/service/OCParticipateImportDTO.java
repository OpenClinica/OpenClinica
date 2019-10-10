package core.org.akaza.openclinica.service;

import org.akaza.openclinica.service.AbstractAuditingDTO;

import java.io.Serializable;

/**
 * A DTO for the OCUser entity.
 */
public class OCParticipateImportDTO extends AbstractAuditingDTO implements Serializable {

    int row;
    String participantId;
    String participantOid;
    String participateStatus;
    String status;
    String message;

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public String getParticipantId() {
        return participantId;
    }

    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }

    public String getParticipantOid() {
        return participantOid;
    }

    public void setParticipantOid(String participantOid) {
        this.participantOid = participantOid;
    }

    public String getParticipateStatus() {
        return participateStatus;
    }

    public void setParticipateStatus(String participateStatus) {
        this.participateStatus = participateStatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}