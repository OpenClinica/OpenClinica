package org.akaza.openclinica.control.admin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Utils;
import org.akaza.openclinica.bean.rule.FileUploadHelper;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.CrfDao;
import org.akaza.openclinica.domain.datamap.CrfBean;
import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.datamap.FormLayout;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.crfdata.ExecuteIndividualCrfObject;
import org.akaza.openclinica.service.crfdata.XformMetaDataService;
import org.akaza.openclinica.service.dto.Crf;
import org.akaza.openclinica.service.dto.Version;
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
    public final String FM_BASEURL = "http://fm.openclinica.info:8080/api/protocol/";

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
        int crfId = Integer.valueOf(retrieveFormFieldValue(items, "crfId"));
        String crfDescription = "";
        DataBinder dataBinder = new DataBinder(new CrfVersion());
        Errors errors = dataBinder.getBindingResult();

        String crfOid = "";
        if (crfId != 0) {
            CrfBean crfBean = crfDao.findByCrfId(crfId);
            crfName = crfBean.getName();
            if (crfBean != null) {
                crfOid = crfBean.getOcOid();
            }
        } else {
            String url = FM_BASEURL + currentStudy.getOid() + "/forms/" + crfName;
            RestTemplate restTemplate = new RestTemplate();
            Crf resp = restTemplate.getForObject(url, Crf.class);
            crfOid = resp.getOcoid();
        }

        Crf crf = getFormArtifactsFromFM(items, currentStudy.getOid(), crfOid);
        /**
         * ODM odm = new ODM();
         * ODMcomplexTypeDefinitionStudy odmStudy = odm.getStudy().get(0);
         * List<ODMcomplexTypeDefinitionMetaDataVersion> odmMetadataVersions = odmStudy.getMetaDataVersion();
         * for (ODMcomplexTypeDefinitionFormDef odmFormDef : odmMetadataVersions.get(0).getFormDef()) {
         * if (crfOid.equals(odmFormDef.getOID())) {
         * List<OCodmComplexTypeDefinitionFormLayoutDef> formLayoutDefs = odmFormDef.getFormLayoutDef();
         * break;
         * }
         * }
         **/

        List<OCodmComplexTypeDefinitionFormLayoutDef> formLayoutDefs = new ArrayList<>();
        OCodmComplexTypeDefinitionFormLayoutDef formLayoutDef;
        for (Version version : crf.getVersions()) {
            formLayoutDef = new OCodmComplexTypeDefinitionFormLayoutDef();
            formLayoutDef.setOID(version.getName());
            formLayoutDef.setURL(version.getArtifactURL());
            formLayoutDefs.add(formLayoutDef);
        }

        ExecuteIndividualCrfObject eicObj = new ExecuteIndividualCrfObject(crf, formLayoutDefs, errors, items, currentStudy, ub, false, crfName,
                crfDescription);

        xformService.executeIndividualCrf(eicObj);
        forwardPage(Page.CREATE_XFORM_CRF_VERSION_SERVLET);
    }

    private void itemBelongsToRepeatGroup(List<String> repeatGroupList, String itemPath) {
        for (String repeatGroup : repeatGroupList) {
            int index = -1;
            String parentPath = itemPath;
            while (index != 0) {
                index = parentPath.lastIndexOf("/");
                parentPath = parentPath.substring(0, index);
                if (repeatGroupList.contains(parentPath)) {

                }
            }

        }
    }

    private String retrieveFormFieldValue(List<FileItem> items, String fieldName) throws Exception {
        for (FileItem item : items) {
            if (fieldName.equals(item.getFieldName()))
                return item.getString("UTF-8");
        }
        logger.warn("Form field '" + fieldName + "' missing from xform submission.");
        return "";
    }

    private void saveAttachedMedia(List<FileItem> items, CrfBean crf, FormLayout formLayout) {
        boolean hasFiles = false;
        for (FileItem item : items) {
            if (!item.isFormField() && item.getName() != null && !item.getName().isEmpty())
                hasFiles = true;
        }

        if (hasFiles) {
            // Create the directory structure for saving the media
            String dir = Utils.getCrfMediaFilePathWithoutSysPath(crf.getOcOid(), formLayout.getOcOid());
            if (!new File(dir).exists()) {
                new File(dir).mkdirs();
                logger.debug("Made the directory " + dir);
            }
            // Save any media files
            for (FileItem item : items) {
                if (!item.isFormField()) {

                    String fileName = item.getName();
                    // Some browsers IE 6,7 getName returns the whole path
                    int startIndex = fileName.lastIndexOf('\\');
                    if (startIndex != -1) {
                        fileName = fileName.substring(startIndex + 1, fileName.length());
                    }

                    File uploadedFile = new File(dir + File.separator + fileName);
                    try {
                        item.write(uploadedFile);
                    } catch (Exception e) {
                        throw new OpenClinicaSystemException(e.getMessage());
                    }
                }
            }
        }
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

    private Crf getFormArtifactsFromFM(List<FileItem> files, String studyOid, String formOid) {
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        ArrayList<ByteArrayResource> byteArrayResources = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();

        String uploadFilesUrl = FM_BASEURL + studyOid + "/forms/" + formOid + "/artifacts";
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
        Crf response = restTemplate.postForObject(uploadFilesUrl, requestEntity, Crf.class);

        return response;
    }

}
