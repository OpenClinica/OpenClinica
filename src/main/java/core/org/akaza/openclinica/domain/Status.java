package core.org.akaza.openclinica.domain;

import core.org.akaza.openclinica.domain.enumsupport.CodedEnum;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;

import java.util.*;

/*
 * @Author Krikor Krumlian
 */
public enum Status implements CodedEnum {

    INVALID(0, "invalid"),
    AVAILABLE(1, "available"),
    UNAVAILABLE(2, "unavailable"),
    PRIVATE(3, "private"),
    PENDING(4, "pending"),
    DELETED(5, "removed"),
    LOCKED(6, "locked"),
    AUTO_DELETED(7, "auto-removed"),
    SIGNED(8, "signed"),
    FROZEN(9, "frozen"),
    SOURCE_DATA_VERIFICATION(10, "source_data_verification"),
    RESET(11, "reset"),
    ARCHIVED(12, "archived");

    private int code;
    private String description;
    private static final Status[] members =
            { INVALID, AVAILABLE, PENDING, PRIVATE, UNAVAILABLE, LOCKED, DELETED, AUTO_DELETED, SIGNED, FROZEN, SOURCE_DATA_VERIFICATION, RESET, ARCHIVED };
    private static List list = Arrays.asList(members);

    private static final Status[] activeMembers = { AVAILABLE, SIGNED, DELETED, AUTO_DELETED };
    private static List activeList = Arrays.asList(activeMembers);

    private static final Status[] studySubjectDropDownMembers = { AVAILABLE, SIGNED, DELETED, AUTO_DELETED };
    private static List studySubjectDropDownList = Arrays.asList(studySubjectDropDownMembers);

    private static final Status[] subjectDropDownMembers = { AVAILABLE, DELETED };
    private static List subjectDropDownList = Arrays.asList(subjectDropDownMembers);

    private static final Status[] studyUpdateMembers = { PENDING, AVAILABLE, FROZEN, LOCKED };
    private static List studyUpdateMembersList = Arrays.asList(studyUpdateMembers);

    Status() {
    }

    Status(int code) {
        this(code, null);
    }

    Status(int code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String toString() {
        ResourceBundle resterm = ResourceBundleProvider.getTermsBundle();
        return resterm.getString(getDescription());
    }

    public static Status getByName(String name) {
        HashMap<String, Status> enumObjects = new HashMap<String, Status>();
        for (Status theEnum : Status.values()) {
            enumObjects.put(theEnum.getDescription(), theEnum);
        }
        return enumObjects.get(name.toLowerCase()   );
    }

    public static Status getByCode(Integer code) {
        HashMap<Integer, Status> enumObjects = new HashMap<Integer, Status>();
        for (Status theEnum : Status.values()) {
            enumObjects.put(theEnum.getCode(), theEnum);
        }
        return enumObjects.get(Integer.valueOf(code));
    }
    
    public static Status getByI18nDescription(String i18nDescription, Locale locale) {
        for (Status theEnum : Status.values()) {
            if(i18nDescription.equals(theEnum.getI18nDescription(locale))) {
                return theEnum;
            }
        }
        return null;
    }
    
    public String getI18nDescription(Locale locale) {
        if (!"".equals(this.description)) {
            ResourceBundle resterm = ResourceBundleProvider.getTermsBundle(locale);
            String des = resterm.getString(this.description);
            if(des != null) {
                return des.trim();
            }  else {
                return "";
            }
        } else {
            return this.description;
        }
    }

    /**
     * A wrapper for name() method to be used in JSPs
     */
    public String getName() {
        return this.name();
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ArrayList toArrayList() {
        return new ArrayList(list);
    }

    public static ArrayList toActiveArrayList() {
        return new ArrayList(activeList);
    }

        public static ArrayList toDropDownArrayList() {
        return new ArrayList(studySubjectDropDownList);
    }

    public static ArrayList toStudyUpdateMembersList() {
        return new ArrayList(studyUpdateMembersList);
    }
    public static ArrayList toSubjectDropDownArrayList() {
        return new ArrayList(subjectDropDownList);
    }

    public boolean isInvalid() {
        return this == Status.INVALID;
    }

    public boolean isAvailable() {
        return this == Status.AVAILABLE;
    }

    public boolean isPending() {
        return this == Status.PENDING;
    }

    public boolean isPrivate() {
        return this == Status.PRIVATE;
    }

    public boolean isUnavailable() {
        return this == Status.UNAVAILABLE;
    }

    public boolean isDeleted() {
        return this == Status.DELETED || this == Status.AUTO_DELETED;
    }

    public boolean isLocked() {
        return this == Status.LOCKED;
    }

    public boolean isSigned() {
        return this == Status.SIGNED;
    }

    public boolean isFrozen() {
        return this == Status.FROZEN;
    }

    public boolean isArchived() {
        return this == Status.ARCHIVED;
    }

}
