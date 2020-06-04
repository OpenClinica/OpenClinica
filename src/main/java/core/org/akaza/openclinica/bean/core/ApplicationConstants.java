package core.org.akaza.openclinica.bean.core;

public class ApplicationConstants {

    //TODO Clean out these constants.
    public final static String SYSTEM_USER = "system";
    public final static String SYSTEM_USER_FNAME = "System";
    public final static String SYSTEM_USER_UUID= "systemUserUuid";

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
