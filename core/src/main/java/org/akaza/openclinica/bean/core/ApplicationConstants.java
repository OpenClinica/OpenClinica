package org.akaza.openclinica.bean.core;

public class ApplicationConstants {

    public final static String RANDOMIZE_CLIENT = "randomize";
    public final static String DICOM_CLIENT = "dicom";

    public static String getDateFormatInItemData() {
        return "yyyy-MM-dd";
    }

    public static String getPDateFormatInSavedData() {
        return "yyyy-MM";
    }

    public static String getDateFormatInExtract() {
        return "yyyy-MM-dd";
    }

}
