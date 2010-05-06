package org.akaza.openclinica.bean.submit.crfdata;

import org.akaza.openclinica.bean.odmbeans.ElementRefBean;

public class ImportItemDataBean {
    private String itemOID;
    private String transactionType;
    private String value;
    private String isNull; // boolean, tbh?
    private ElementRefBean measurementUnitRef = new ElementRefBean();
    private String reasonForNull;

    public String getItemOID() {
        return itemOID;
    }

    public void setItemOID(String itemOID) {
        this.itemOID = itemOID;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getIsNull() {
        return isNull;
    }

    public void setIsNull(String isNull) {
        this.isNull = isNull;
    }

    public ElementRefBean getMeasurementUnitRef() {
        return measurementUnitRef;
    }

    public void setMeasurementUnitRef(ElementRefBean measurementUnitRef) {
        this.measurementUnitRef = measurementUnitRef;
    }

    public String getReasonForNull() {
        return reasonForNull;
    }

    public void setReasonForNull(String reasonForNull) {
        this.reasonForNull = reasonForNull;
    }
}
