package core.org.akaza.openclinica.dao.managestudy;

import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;

import java.util.ResourceBundle;

public interface CriteriaCommand {
     final ResourceBundle resterm = ResourceBundleProvider.getTermsBundle();
     final String LOCKED ="LOCKED";
     final String NOT_LOCKED ="NOT_LOCKED";
     final String SIGNED ="SIGNED";
     final String NOT_SIGNED ="NOT_SIGNED";

    public String execute(String criteria);

}
