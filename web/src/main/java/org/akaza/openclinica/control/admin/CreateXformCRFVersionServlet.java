package org.akaza.openclinica.control.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.rule.FileUploadHelper;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.CrfDao;
import org.akaza.openclinica.domain.datamap.CrfBean;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.crfdata.ErrorObj;
import org.akaza.openclinica.service.crfdata.ExecuteIndividualCrfObject;
import org.akaza.openclinica.service.crfdata.FormArtifactTransferObj;
import org.akaza.openclinica.service.crfdata.XformMetaDataService;
import org.akaza.openclinica.service.dto.FormVersion;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.openclinica.ns.odm_ext_v130.v31.OCodmComplexTypeDefinitionFormLayoutDef;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.web.client.RestTemplate;

public class CreateXformCRFVersionServlet extends SecureController {
    Locale locale;
    FileUploadHelper uploadHelper = new FileUploadHelper();
    // public final String FM_BASEURL = "http://fm2.openclinica.info/api/protocol/";
    public final String FM_BASEURL = "http://fm.openclinica.info/api/protocol/";
    // public final String FM_BASEURL = "http://oc.local:8090/api/protocol/";

    @Override
    protected void processRequest() throws Exception {
        CrfDao crfDao = (CrfDao) SpringServletAccess.getApplicationContext(context).getBean("crfDao");
        XformMetaDataService xformService = (XformMetaDataService) SpringServletAccess.getApplicationContext(context).getBean("xformService");

        Locale locale = LocaleResolver.getLocale(request);
        ResourceBundleProvider.updateLocale(locale);
        resword = ResourceBundleProvider.getWordsBundle(locale);

        // Retrieve submission data from multipart request
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        List<FileItem> items = upload.parseRequest(request);
        String crfName = retrieveFormFieldValue(items, "crfName");
        DataBinder dataBinder = new DataBinder(new FormVersion());
        Errors errors = dataBinder.getBindingResult();
        int crfId = Integer.valueOf(retrieveFormFieldValue(items, "crfId"));

        if (crfId != 0) {
            CrfBean crfBean = crfDao.findByCrfId(crfId);
            crfName = crfBean.getName();
        }

        FormArtifactTransferObj transferObj = getFormArtifactsFromFM(items, currentStudy.getOid(), crfName);
        if (transferObj.getErr().size() != 0) {
            for (ErrorObj er : transferObj.getErr()) {
                errors.rejectValue("name", er.getCode(), er.getMessage());
            }
        } else {
            List<OCodmComplexTypeDefinitionFormLayoutDef> formLayoutDefs = new ArrayList<>();
            OCodmComplexTypeDefinitionFormLayoutDef formLayoutDef;
            for (FormVersion version : transferObj.getForm().getVersions()) {
                formLayoutDef = new OCodmComplexTypeDefinitionFormLayoutDef();
                formLayoutDef.setOID(version.getName());
                formLayoutDef.setURL(version.getArtifactURL());
                formLayoutDefs.add(formLayoutDef);
            }
            ExecuteIndividualCrfObject eicObj = new ExecuteIndividualCrfObject(transferObj.getForm(), formLayoutDefs, errors, currentStudy, ub, false, null);
            xformService.executeIndividualCrf(eicObj);
        }
        if (errors.hasErrors()) {
            request.setAttribute("errorList", errors.getAllErrors());
        }
        forwardPage(Page.CREATE_XFORM_CRF_VERSION_SERVLET);
    }

    private String retrieveFormFieldValue(List<FileItem> items, String fieldName) throws Exception {
        for (FileItem item : items) {
            if (fieldName.equals(item.getFieldName()))
                return item.getString("UTF-8");
        }
        logger.warn("Form field '" + fieldName + "' missing from xform submission.");
        return "";
    }

    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        locale = LocaleResolver.getLocale(request);

        // Make sure xforms are enabled
        String xformEnabled = CoreResources.getField("xform.enabled");
        if (xformEnabled == null || !xformEnabled.equals("true")) {
            addPageMessage(respage.getString("may_not_create_xforms"));
            throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("may_not_create_xforms"), "1");
        }

        if (ub.isSysAdmin()) {
            return;
        }
        Role r = currentRole.getRole();
        if (r.equals(Role.STUDYDIRECTOR) || r.equals(Role.COORDINATOR)) {
            return;
        }
        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("may_not_submit_data"), "1");
    }

    private FormArtifactTransferObj getFormArtifactsFromFM(List<FileItem> files, String studyOid, String crfName) {
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        ArrayList<ByteArrayResource> byteArrayResources = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();

        String uploadFilesUrl = FM_BASEURL + studyOid + "/forms/" + crfName + "/artifacts";
        map.add("file", byteArrayResources);

        for (FileItem file : files) {
            String filename = file.getName();
            if (!file.isFormField()) {
                ByteArrayResource contentsAsResource = new ByteArrayResource(file.get()) {
                    @Override
                    public String getFilename() {
                        return filename;
                    }
                };
                map.get("file").add(contentsAsResource);
            }
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
        // TODO: replace with Crf object instead of String object
        FormArtifactTransferObj response = restTemplate.postForObject(uploadFilesUrl, requestEntity, FormArtifactTransferObj.class);

        return response;
    }

}
