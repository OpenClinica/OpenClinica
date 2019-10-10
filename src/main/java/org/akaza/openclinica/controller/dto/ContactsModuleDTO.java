package org.akaza.openclinica.controller.dto;

public class ContactsModuleDTO {
private String Status;

    public ContactsModuleDTO() {
    }

    public ContactsModuleDTO(String status) {
        Status = status;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }
}
