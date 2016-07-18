/* OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.domain.rule.RulesPostImportContainer;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.service.rule.RuleSetServiceInterface;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.SQLInitServlet;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.XMLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DownloadRuleSetXmlServlet extends SecureController {

    protected final Logger log = LoggerFactory.getLogger(DownloadRuleSetXmlServlet.class);
    private static final long serialVersionUID = 5381321212952389008L;
    RuleSetServiceInterface ruleSetService;

    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }

        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MANAGE_STUDY_SERVLET, resexception.getString("not_study_director"), "1");

    }

    private FileWriter handleLoadCastor(FileWriter writer, RulesPostImportContainer rpic) {

        try {
            // Create Mapping
            Mapping mapping = new Mapping();
            mapping.loadMapping(getCoreResources().getURL("mappingMarshaller.xml"));
            // Create XMLContext
            XMLContext xmlContext = new XMLContext();
            xmlContext.addMapping(mapping);

            Marshaller marshaller = xmlContext.createMarshaller();
            marshaller.setWriter(writer);
            marshaller.marshal(rpic);
            return writer;

        } catch (FileNotFoundException ex) {
            throw new OpenClinicaSystemException(ex.getMessage(), ex.getCause());
        } catch (IOException ex) {
            throw new OpenClinicaSystemException(ex.getMessage(), ex.getCause());
        } catch (MarshalException e) {
            throw new OpenClinicaSystemException(e.getMessage(), e.getCause());
        } catch (ValidationException e) {
            throw new OpenClinicaSystemException(e.getMessage(), e.getCause());
        } catch (MappingException e) {
            throw new OpenClinicaSystemException(e.getMessage(), e.getCause());
        } catch (Exception e) {
            throw new OpenClinicaSystemException(e.getMessage(), e.getCause());
        }
    }

    private RulesPostImportContainer prepareRulesPostImportRuleSetRuleContainer(String ruleSetRuleIds) {
        List<RuleSetRuleBean> ruleSetRules = new ArrayList<RuleSetRuleBean>();
        RulesPostImportContainer rpic = new RulesPostImportContainer();

        if (ruleSetRuleIds !="") {
        String[] splitExpression = ruleSetRuleIds.split(",");

        for (String string : splitExpression) {
            RuleSetRuleBean rsr = getRuleSetService().getRuleSetRuleDao().findById(Integer.valueOf(string));
            ruleSetRules.add(rsr);
        }
        rpic.populate(ruleSetRules);
        
        } 
        return rpic;
    }

    @Override
    public void processRequest() throws Exception {

        // String ruleSetId = request.getParameter("ruleSetId");
        String ruleSetRuleIds = request.getParameter("ruleSetRuleIds");

        String dir = SQLInitServlet.getField("filePath") + "rules" + File.separator;
        Long time = System.currentTimeMillis();
        File f = new File(dir + "rules" + currentStudy.getOid() + "-" + time + ".xml");
        FileWriter writer = new FileWriter(f);
        handleLoadCastor(writer, prepareRulesPostImportRuleSetRuleContainer(ruleSetRuleIds));

        response.setHeader("Content-disposition", "attachment; filename=\"" + "rules" + currentStudy.getOid() + "-" + time + ".xml" + "\";");
        response.setContentType("text/xml");
        response.setHeader("Pragma", "public");

        ServletOutputStream op = response.getOutputStream();

        DataInputStream in = null;
        try {
            response.setContentType("text/xml");
            response.setHeader("Pragma", "public");
            response.setContentLength((int) f.length());

            byte[] bbuf = new byte[(int) f.length()];
            in = new DataInputStream(new FileInputStream(f));
            int length;
            while ((in != null) && ((length = in.read(bbuf)) != -1)) {
                op.write(bbuf, 0, length);
            }

            in.close();
            op.flush();
            op.close();
        } catch (Exception ee) {
            ee.printStackTrace();
        } finally {
            if (in != null) {
                in.close();
            }
            if (op != null) {
                op.close();
            }
        }

    }

    private RuleSetServiceInterface getRuleSetService() {
        ruleSetService =
            this.ruleSetService != null ? ruleSetService : (RuleSetServiceInterface) SpringServletAccess.getApplicationContext(context).getBean(
                    "ruleSetService");
        // TODO: Add getRequestURLMinusServletPath(),getContextPath()
        return ruleSetService;
    }

    private CoreResources getCoreResources() {
        return (CoreResources) SpringServletAccess.getApplicationContext(context).getBean("coreResources");
    }
}
