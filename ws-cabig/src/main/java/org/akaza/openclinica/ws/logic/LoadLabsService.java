package org.akaza.openclinica.ws.logic;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.bean.submit.crfdata.CRFDataPostImportContainer;
import org.akaza.openclinica.bean.submit.crfdata.FormDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ImportItemDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ImportItemGroupDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ODMContainer;
import org.akaza.openclinica.bean.submit.crfdata.StudyEventDataBean;
import org.akaza.openclinica.bean.submit.crfdata.SubjectDataBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemGroupDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.web.crfdata.DataImportService;
import org.akaza.openclinica.ws.cabig.exception.CCBusinessFaultException;
import org.akaza.openclinica.ws.cabig.exception.CCDataValidationFaultException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;

import javax.sql.DataSource;

public class LoadLabsService {

    private final String CONNECTOR_NAMESPACE_V1 = "http://clinicalconnector.nci.nih.gov";
    private HashMap<String, String> itemNames;
    private DomParsingService xmlService;

    public LoadLabsService() {
        xmlService = new DomParsingService();
        // purpose, set up the mapping of tags -> item OIDs here
        // itemNames.put(key, value)
    }

    public ODMContainer generateLoadLabsObject(Element requestElement, StudyDAO studyDao, SubjectDAO subjectDao, ItemDAO itemDao,
            StudySubjectDAO studySubjectDao, ItemGroupDAO itemGroupDao, CRFVersionDAO crfVersionDao, StudyEventDefinitionDAO studyEventDefDao) throws Exception {
        ODMContainer odmContainer = new ODMContainer();
        CRFDataPostImportContainer crfDataPostImportContainer = new CRFDataPostImportContainer();
        ArrayList<SubjectDataBean> subjectData = new ArrayList<SubjectDataBean>();
        ArrayList<StudyEventDataBean> studyEventData = new ArrayList<StudyEventDataBean>();
        ArrayList<FormDataBean> formData = new ArrayList<FormDataBean>();
        ArrayList<ImportItemGroupDataBean> itemGroupData = new ArrayList<ImportItemGroupDataBean>();
        ArrayList<ImportItemDataBean> itemData = new ArrayList<ImportItemDataBean>();

        String formOID, itemGroupOID, studyEventOID, studyOID = "";
        String subjectOID = "";
        NodeList nlist = requestElement.getElementsByTagNameNS(CONNECTOR_NAMESPACE_V1, "performedClinicalResult");
        for (int i = 0; i < nlist.getLength(); i++) {
            Node data = nlist.item(i);
            // HashMap parsedAnswers = new HashMap<String, String>();

            String studyProtocolIdentifier = xmlService.getPerformedObservationValue(data, "studyProtocolIdentifier", "extension");// study unique id, NOT OID
            String studySubjectIdentifier = xmlService.getPerformedObservationValue(data, "studySubjectIdentifier", "extension");// person id, NOT OID or SSID
            System.out.println("found: " + studyProtocolIdentifier + " " + studySubjectIdentifier);
            StudySubjectBean ssBean = new StudySubjectBean();
            odmContainer.setStudyUniqueIdentifier(studyProtocolIdentifier);
            odmContainer.setSubjectUniqueIdentifier(studySubjectIdentifier);
            // assumes that we only have one study and subject per request. need to change?
            ssBean.setOid("");
            try {
                StudyBean studyBean = studyDao.findByUniqueIdentifier(studyProtocolIdentifier);
                studyOID = studyBean.getOid();
                SubjectBean subjectBean = subjectDao.findByUniqueIdentifier(studySubjectIdentifier);
                ssBean = studySubjectDao.findBySubjectIdAndStudy(subjectBean.getId(), studyBean);
            } catch (Exception eee) {
                throw new CCBusinessFaultException("Could not find study or subject in the request.", "CC10310");
            }
            subjectOID = ssBean.getOid();// we actually need to put study subject OID here
            itemData = generateItemDataList(data, itemDao);
            ItemGroupBean itemGroupBean = (ItemGroupBean) itemGroupDao.findByName("RepeatingLabData");
            itemGroupOID = itemGroupBean.getOid();
            // this runs once per loop, so repeat key = i, essentially
            ImportItemGroupDataBean itemGroupDataBean = new ImportItemGroupDataBean();
            itemGroupDataBean.setItemData(itemData);
            itemGroupDataBean.setItemGroupOID(itemGroupOID);
            itemGroupDataBean.setItemGroupRepeatKey(new Integer(i).toString());// ?

            itemGroupData.add(itemGroupDataBean);
        }
        CRFVersionBean crfVersionBean = (CRFVersionBean) crfVersionDao.findByFullName("v1", "Clinical Connector Lab Data");
        formOID = crfVersionBean.getOid();
        // this runs once a request
        FormDataBean formDataBean = new FormDataBean();
        formDataBean.setFormOID(formOID); // crf version dao
        formDataBean.setItemGroupData(itemGroupData);
        formData.add(formDataBean);

        StudyEventDefinitionBean sedBean = new StudyEventDefinitionBean();
        ArrayList<StudyEventDefinitionBean> sedBeans = studyEventDefDao.findAllByStudy(studyDao.findByOid(studyOID));
        for (StudyEventDefinitionBean sed : sedBeans) {
            if ("LoadLabsEvent".equals(sed.getName())) {
                sedBean = sed;
                // studyEventOID = sedBean.getOid();
                break;
            }
            if (sedBean.getId() == 0) {
                throw new CCDataValidationFaultException("No proper Study Event entitled 'LoadLabsEvent' was defined for the Study OID " + studyOID + ".",
                        "CC10310");
            }
        }
        // StudyEventDefinitionBean sedBean = (StudyEventDefinitionBean) studyEventDefDao.findAllB//.findByName("LoadLabsEvent");
        studyEventOID = sedBean.getOid();
        // int studyEventRepeatKey = studyEventDefDao.findNextKey();
        StudyEventDataBean studyEventDataBean = new StudyEventDataBean();
        studyEventDataBean.setFormData(formData);
        studyEventDataBean.setStudyEventOID(studyEventOID); // study event definition dao
        studyEventDataBean.setStudyEventRepeatKey("1");// new Integer(studyEventRepeatKey).toString());
        // need to find the max number of events we've seen on that subject
        studyEventData.add(studyEventDataBean);

        SubjectDataBean subjectDataBean = new SubjectDataBean();
        subjectDataBean.setStudyEventData(studyEventData);
        subjectDataBean.setSubjectOID(subjectOID);
        subjectData.add(subjectDataBean);

        crfDataPostImportContainer.setSubjectData(subjectData);
        crfDataPostImportContainer.setStudyOID(studyOID);
        odmContainer.setCrfDataPostImportContainer(crfDataPostImportContainer);
        return odmContainer;
    }

    public ArrayList<String> importData(DataSource dataSource, CoreResources resources, StudyBean studyBean, UserAccountBean userBean,
            ODMContainer odmContainer, StudyEventBean studyEventBean) throws Exception {
        return new DataImportService().importProcessedData(dataSource, resources, studyBean, userBean, odmContainer, new StringBuffer(), new StringBuffer(),
                false, studyEventBean);
    }

    private ArrayList<ImportItemDataBean> generateItemDataList(Node data, ItemDAO itemDao) throws Exception {
        ArrayList<ImportItemDataBean> itemData = new ArrayList<ImportItemDataBean>();
        itemData.add(generateItemData("asCollectedIndicator", "value", data, (ItemBean) itemDao.findByName("coInd")));
        itemData.add(generateItemData("comment", "value", data, (ItemBean) itemDao.findByName("comment")));
        itemData.add(generateItemData("confidentialityCode", "code", data, (ItemBean) itemDao.findByName("confCode")));
        itemData.add(generateItemData("numericalResult", "value", data, (ItemBean) itemDao.findByName("numRes")));
        itemData.add(generateItemData("numericalResult", "unit", data, (ItemBean) itemDao.findByName("numResMeasUnit")));
        // String asCollectedIndicator = xmlService.getElementValue(data, this.CONNECTOR_NAMESPACE_V1, "asCollectedIndicator", "value");
        // String comment = xmlService.getElementValue(data, CONNECTOR_NAMESPACE_V1, "comment", "value");
        // String confidentialityCode = xmlService.getElementValue(data, CONNECTOR_NAMESPACE_V1, "confidentialityCode", "code");
        // String numericalResult = xmlService.getElementValue(data, CONNECTOR_NAMESPACE_V1, "numericalResult", "value");
        // String numericalResultUnit = xmlService.getElementValue(data, CONNECTOR_NAMESPACE_V1, "numericalResult", "unit");
        // reference range is optional, so we have a try-catch
        String referenceRangeHighValue = "";
        String referenceRangeLowValue = "";
        String referenceRangeComment = "";
        try {
            referenceRangeHighValue = xmlService.getReferenceRangeValue(data, "high");
            referenceRangeLowValue = xmlService.getReferenceRangeValue(data, "low");
            referenceRangeComment = xmlService.getElementValue(data, CONNECTOR_NAMESPACE_V1, "referenceRangeComment", "value");
            itemData.add(generateItemData(referenceRangeHighValue, (ItemBean) itemDao.findByName("refRangeHigh")));
            itemData.add(generateItemData(referenceRangeLowValue, (ItemBean) itemDao.findByName("refRangeLow")));
            itemData.add(generateItemData(referenceRangeComment, (ItemBean) itemDao.findByName("refRangeCom")));
            // reported result status code is also optional
            String reportedResultStatusCode = xmlService.getElementValue(data, CONNECTOR_NAMESPACE_V1, "reportedResultStatusCode", "code");
            itemData.add(generateItemData(reportedResultStatusCode, (ItemBean) itemDao.findByName("repResStatCode")));
        } catch (Exception npe) {
            System.out.println("did not find reference range here");
        }
        String reportedDateTimeStr = xmlService.getElementValue(data, CONNECTOR_NAMESPACE_V1, "reportedDate", "value");
        // parse the date?
        itemData.add(generateItemData(reportedDateTimeStr, (ItemBean) itemDao.findByName("repDate")));

        String textResult = xmlService.getElementValue(data, CONNECTOR_NAMESPACE_V1, "textResult", "value");
        itemData.add(generateItemData(textResult, (ItemBean) itemDao.findByName("textRes")));
        String uncertaintyCode = xmlService.getElementValue(data, CONNECTOR_NAMESPACE_V1, "uncertaintyCode", "code");
        itemData.add(generateItemData(uncertaintyCode, (ItemBean) itemDao.findByName("uncertCode")));
        String activityNameCode = xmlService.getPerformedObservationValue(data, "activityNameCode", "code");
        itemData.add(generateItemData(activityNameCode, (ItemBean) itemDao.findByName("perObsActNameCode")));
        return itemData;
    }

    // possible perf bottleneck: calling itemdao a lot of times to pull that same data again and again, consider calling once and caching in a hashnap

    private ImportItemDataBean generateItemData(String name, String value, Node data, ItemBean item) throws Exception {
        String actualValue = xmlService.getElementValue(data, CONNECTOR_NAMESPACE_V1, name, value);
        ImportItemDataBean importItemDataBean = new ImportItemDataBean();
        importItemDataBean.setItemOID(item.getOid());
        importItemDataBean.setValue(actualValue);
        return importItemDataBean;
    }

    private ImportItemDataBean generateItemData(String actualValue, ItemBean item) {
        ImportItemDataBean importItemDataBean = new ImportItemDataBean();
        importItemDataBean.setItemOID(item.getOid());
        importItemDataBean.setValue(actualValue);
        return importItemDataBean;
    }
}
