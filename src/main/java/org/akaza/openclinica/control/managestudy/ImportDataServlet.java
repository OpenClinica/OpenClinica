package org.akaza.openclinica.control.managestudy;

import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class ImportDataServlet extends SecureController {
    @Override
    public void mayProceed() throws InsufficientPermissionException {
    }


    @Override
    public void processRequest() throws Exception {
        forwardPage(Page.IMPORT_DATA);
    }
}
