package core.org.akaza.openclinica.domain.enumsupport;

/**
 * The ModuleStatus enumeration.
 */
public enum ModuleStatus {
    PENDING,ACTIVE,INACTIVE;

    public static boolean isActive(String moduleValue){
        if (ModuleStatus.valueOf(moduleValue).equals(ACTIVE)) {
            return true;
        }
        return false;
    }
}
