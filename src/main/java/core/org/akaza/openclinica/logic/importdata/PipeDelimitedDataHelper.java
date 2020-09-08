package core.org.akaza.openclinica.logic.importdata;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import core.org.akaza.openclinica.bean.admin.CRFBean;
import core.org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.submit.FormLayoutBean;
import core.org.akaza.openclinica.bean.submit.ItemBean;
import core.org.akaza.openclinica.bean.submit.ItemGroupBean;
import core.org.akaza.openclinica.dao.admin.CRFDAO;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.dao.submit.FormLayoutDAO;
import core.org.akaza.openclinica.dao.submit.ItemDAO;
import core.org.akaza.openclinica.dao.submit.ItemGroupDAO;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.exception.OpenClinicaSystemException;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import core.org.akaza.openclinica.service.StudyBuildService;
import core.org.akaza.openclinica.service.crfdata.ErrorObj;
import core.org.akaza.openclinica.service.rest.errors.ErrorConstants;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PipeDelimitedDataHelper extends ImportDataHelper {

    private final DataSource ds;
    private StudyDao studyDao;

    private static final String PARTICIPANT_ID_HEADER_PROPERTY = "ParticipantIDHeader";
    public static final String DELIMITER_PROPERTY = "Delimiter";
    private static final String DEFAULT_PARTICIPANT_ID_HEADER = "ParticipantID";
    private StudyBuildService studyBuildService;

    public PipeDelimitedDataHelper(DataSource ds, StudyBuildService studyBuildService, StudyDao studyDao) {
        super();
        this.ds = ds;
        this.studyBuildService = studyBuildService;
        this.studyDao = studyDao;
    }

    /**
     * @param mappingFile
     * @param rawItemDataFile
     * @return
     * @throws IOException
     */
    public String transformTextToODMxml(File mappingFile, File rawItemDataFile, HashMap hm) throws OpenClinicaSystemException, IOException {

        String rawMappingStr;
        String rawItemData;
        String odmXml = null;


        rawMappingStr = this.readFileToString(mappingFile);
        rawItemData = this.readFileToString(rawItemDataFile);

        odmXml = transformTextToODMxml(rawMappingStr, rawItemData, hm);

        return odmXml;

    }

    /**
     * @param file
     * @return
     * @throws IOException
     */
    public String readFileToString(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (Scanner sc = new Scanner(file)) {
            String currentLine;

            while (sc.hasNextLine()) {
                currentLine = sc.nextLine();
                sb.append(currentLine);
                sb.append("\r");
            }

        }

        return sb.toString();
    }

    public String transformTextToODMxml(String rawMappingStr, String rawItemData, HashMap hm) throws OpenClinicaSystemException {

        String studyOID;
        String participantId;
        String studyEventOID;
        String formOID;
        String formVersion;
        String itemGroupOID;
        String itemOid;
        String itemName;
        String useRepeatingkey;
        String studyEventRepeatKey;
        String itemDataValue;
        String itemDataXMLValue;

        ArrayList itemDataValues;
        String fileNm;
        boolean foundItemData = false;

        String[] columnNms = getDataColumnNames(rawItemData);
        HashMap mappedValues = getDataMappedValues(rawMappingStr, columnNms);


        /**
         * Hold all ItemOIDs coming from mapping file
         * each ite, like:
         *  ItemGroupOID--Item Name -- Item OID
         */
        ArrayList mappedColumnNameList = null;
        /**
         * Hold all ItemGroupOIDs coming from mapping file
         */
        Object[] mappingItemGroupOIDs;

        String mappingStr;

        try {


            checkPipeNumber(rawItemData);

            columnNms = getDataColumnNames(rawItemData);

            String participantIdHeader = (String) mappedValues.get(PARTICIPANT_ID_HEADER_PROPERTY);
            if (StringUtils.isBlank(participantIdHeader)) {
                participantIdHeader = DEFAULT_PARTICIPANT_ID_HEADER;
            }

            if (this.hasParticipantIDColumn(participantIdHeader, columnNms)) {
                ;
            } else {
                return "errorCode.noParticipantIDinDataFile";
            }

            mappedValues = getDataMappedValues(rawMappingStr, columnNms);

            studyOID = (String) mappedValues.get("StudyOID");
            studyEventOID = (String) mappedValues.get("StudyEventOID");
            formOID = (String) mappedValues.get("FormOID");
            formVersion = (String) mappedValues.get("FormVersion");

            // get default version
            if (formVersion == null || formVersion.trim().length() == 0) {
                formVersion = (String) hm.get("FormVersion");
            }

            mappedColumnNameList = (ArrayList) mappedValues.get("mappedColumnNameList");
            ArrayList itemGroupOIDList = (ArrayList) mappedValues.get("itemGroupOIDList");

            mappingItemGroupOIDs = (Object[]) itemGroupOIDList.toArray();


//////////////////////////////////////////////////////////////////////////////////
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();

            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();

            Document document = documentBuilder.newDocument();


            /**
             * root element
             * <ODM xmlns="http://www.cdisc.org/ns/odm/v1.3"
             *      xmlns:OpenClinica="http://www.cdisc.org/ns/odm/v1.3 ODM1-3.xsd"
             *      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             *      CreationDateTime="2008-04-12T20:24:20"
             *      Description="Demographics Import"
             *      FileOID="1D20080412202420"
             *      FileType="Snapshot"
             *      ODMVersion="1.3">

             */
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            Date date = new Date();

            String creationDateTime = new Timestamp(date.getTime()).toString();
            String fileOID = sdf2.format(timestamp);
            Element odmData = document.createElement("ODM");

            odmData.setAttribute("xmlns", "http://www.cdisc.org/ns/odm/v1.3");
            odmData.setAttribute("xmlns:OpenClinica", "http://www.cdisc.org/ns/odm/v1.3 ODM1-3.xsd");
            odmData.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            odmData.setAttribute("CreationDateTime", creationDateTime);
            odmData.setAttribute("Description", "Import");
            odmData.setAttribute("FileOID", fileOID);
            odmData.setAttribute("FileType", "Snapshot");
            odmData.setAttribute("ODMVersion", "1.3");
            document.appendChild(odmData);


            Element clinicalData = document.createElement("ClinicalData");
            clinicalData.setAttribute("StudyOID", studyOID);
            odmData.appendChild(clinicalData);

            // UpsertOn element
            Element upsertOn = document.createElement("UpsertOn");
            clinicalData.appendChild(upsertOn);

            // set an attribute to UpsertOn element
            Attr attr = document.createAttribute("DataEntryComplete");
            attr.setValue("true");
            upsertOn.setAttributeNode(attr);

            attr = document.createAttribute("DataEntryStarted");
            attr.setValue("true");
            upsertOn.setAttributeNode(attr);

            attr = document.createAttribute("NotStarted");
            attr.setValue("true");
            upsertOn.setAttributeNode(attr);

            String[] dataRows = rawItemData.split(new Character((char) 13).toString());

            int indexofParticipantID = 0;

            for (int i = 0; i < dataRows.length; i++) {
                //logger.info("++DEST++dataRows[i]g++++++" +dataRows[i]);
                // in each data row, first position is participant ID
                // OC-10291
                String tempDataRowStr = dataRows[i].toString().replaceAll("\n", "");
                if (tempDataRowStr.endsWith("|")) {
                    tempDataRowStr = tempDataRowStr + " ";
                }
                String[] dataRow = this.toArrayWithFullItems(tempDataRowStr, "|");
                boolean found = false;

                // find subject OID, It may be at any position
                if (i == 0) {
                    for (int k = 0; i < dataRow.length; k++) {
                        if (dataRow[k].toString().trim().equals(participantIdHeader) || dataRow[k].substring(1).trim().equals(participantIdHeader)) {
                            indexofParticipantID = k;

                            break;
                        }
                    }
                } else {
                    // start process item data
                    // ignore blank line
                    if (dataRows[i].toString().replaceAll("[\\n\\t\\r]", "").trim().length() > 0) {
                        //Empty participant label in csv, gives ""
                        participantId = dataRow[indexofParticipantID].toString().trim().replaceAll("\"", "");
                        //logger.info(i+ "************dataRow************************"+ dataRow);

                        if (participantId != null && participantId.trim().length() > 0) {
                            Element subjectData = document.createElement("SubjectData");
                            subjectData.setAttribute("OpenClinica:StudySubjectID", participantId);

                            Element studyEventData = document.createElement("StudyEventData");
                            studyEventData.setAttribute("StudyEventOID", studyEventOID);

                            Element formData = document.createElement("FormData");
                            formData.setAttribute("FormOID", formOID);
                            formData.setAttribute("FormLayoutOID", formVersion);
                            //OpenClinica:Status="initial data entry"
                            formData.setAttribute("OpenClinica:Status", "initial data entry");

                            studyEventData.appendChild(formData);
                            subjectData.appendChild(studyEventData);
                            clinicalData.appendChild(subjectData);


                            /**
                             * Loop through all item group OIDs list to create all item groups for each study event
                             */
                            for (int j = 0; j < mappingItemGroupOIDs.length; j++) {

                                //create item group, set OID and some default value
                                String currenItemGroupOID = mappingItemGroupOIDs[j].toString().trim();
                                Element itemGroupData = document.createElement("ItemGroupData");
                                itemGroupData.setAttribute("ItemGroupOID", currenItemGroupOID);
                                itemGroupData.setAttribute("TransactionType", "Insert");

                                formData.appendChild(itemGroupData);

                                /**
                                 * check and process data row value -- start from 2nd position
                                 * after each while-loop, it will finish processing one data row
                                 */
                                int columnSize = columnNms.length;

                                for (int k = 0; k < columnSize; k++) {

                                    if (k != indexofParticipantID) {

                                        itemName = columnNms[k].trim();
                                        /**
                                         *  data value must be in both columnNms and  mappingColumnNmsMap
                                         *  check itemOid in mappingColumnNmsMap
                                         *  if found in mapping file, then create the Item
                                         */
                                        Iterator mappedColumnNameListIt = mappedColumnNameList.iterator();
                                        while (mappedColumnNameListIt.hasNext()) {
                                            String[] itemMappingRow = (String[]) mappedColumnNameListIt.next();
                                            String mappingItemGroupOID = itemMappingRow[0];
                                            String mappingItemName = itemMappingRow[1].trim();
                                            String mappingItemOID = itemMappingRow[2].trim();

                                            if (mappingItemGroupOID.equals(currenItemGroupOID) && mappingItemName.equals(itemName)) {
                                                //logger.info("----mappingItemName:"+ mappingItemName + "----itemName:"+ itemName);
                                                itemDataValue = dataRow[k].toString().replaceAll("[\\n\\t\\r]", " ").trim();

                                                //ignore item which has no item value or blank value
                                                if (itemDataValue != null && itemDataValue.trim().length() > 0) {
                                                    Element itemData = document.createElement("ItemData");
                                                    itemData.setAttribute("ItemOID", mappingItemOID);
                                                    itemData.setAttribute("Value", itemDataValue);

                                                    itemGroupData.appendChild(itemData);
                                                    foundItemData = true;
                                                    // if found, then skip the rest
                                                    break;
                                                }

                                            } else {
                                                //logger.info(k+"----mappingItemName:"+ mappingItemName + "----itemName:"+ itemName);

                                                if (mappingItemGroupOID.equals("StudyEventRepeatKey") && mappingItemName.equals(itemName)) {
                                                    studyEventRepeatKey = dataRow[k].toString();
                                                    //logger.info(k+ "************studyEventRepeatKey*************************"+ studyEventRepeatKey);

                                                }
                                            }
                                        } //end of inner while-loop

                                    }


                                }// end of inner for-loop


                            }


                        }
                    }


                }// end of outer for-loop


            }


            if (!foundItemData) {
                throw new OpenClinicaSystemException("errorCode.NoItemDataFound", "Import failed because no matched item data found in data file");
            }


            // create the xml file
            //transform the DOM Object to an XML File
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            DOMSource domSource = new DOMSource(document);
            StringWriter writer = new StringWriter();
            StreamResult streamResult = new StreamResult(writer);


            transformer.transform(domSource, streamResult);

            String output = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + writer.getBuffer().toString();

            //System.out.println("Done creating XML File output:" + output);

            return output;

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        } catch (OpenClinicaSystemException ose) {
            throw ose;
        } catch (Exception e) {
            String msg = e.toString();

            if (msg != null && msg.indexOf("ArrayIndexOutOfBoundsException") > -1) {
                throw new OpenClinicaSystemException("errorCode.dataRowMissingPipe", "Error-data file format missing pipe");
            }


        }

        return "";
    }

    /**
     * @param rawItemData
     * @throws OpenClinicaSystemException
     */
    private void checkPipeNumber(String rawItemData) throws OpenClinicaSystemException {
        String[] dataRows = rawItemData.split(new Character((char) 13).toString());

        String tempDataRowStr = dataRows[0].toString().replaceAll("\n", "");
        if (tempDataRowStr.endsWith("|")) {
            tempDataRowStr = tempDataRowStr + " ";
        }
        String[] headerRow = this.toArrayWithFullItems(tempDataRowStr, "|");

        tempDataRowStr = dataRows[1].toString().replaceAll("\n", "");
        if (tempDataRowStr.endsWith("|")) {
            tempDataRowStr = tempDataRowStr + " ";
        }
        String[] dataRow = this.toArrayWithFullItems(tempDataRowStr, "|");

        if (dataRow.length < headerRow.length || dataRow.length > headerRow.length) {
            throw new OpenClinicaSystemException("errorCode.inconsistentHeaderAndDataColumns", "Data file format error - inconsistent number of header columns and data columns");
        }
    }

    /**
     * @param rawItemData
     */
    private static String[] getDataColumnNames(String rawItemData) {

        //System.out.println("getDataColumnNames==========================" + rawItemData);
        ArrayList columnNmsList = new ArrayList();
        //Iterate to get the item column values
        String[] itemDataRows = rawItemData.split(new Character((char) 13).toString());

        // process  the header row
        //String[] columnNms = itemDataRows[0].toString().split("|");
        String columnNmsStr = itemDataRows[0].toString();

        String[] columnNms = toArray(columnNmsStr, "|");

        return columnNms;
    }

    /**
     * this will ignore/not return empty elements
     * @param columnNmsStr
     * @return
     */
    private static String[] toArray(String columnNmsStr, String delemiterStr) {
        StringTokenizer st = new StringTokenizer(columnNmsStr, delemiterStr);
        int size = st.countTokens();
        String[] columnNms = new String[size];

        int i = 0;
        while (st.hasMoreElements()) {
            String e = st.nextElement().toString();
            columnNms[i] = e;
            i++;
            //System.out.println(e);

        }
        return columnNms;
    }

    private static String[] toArrayWithFullItems(String columnNmsStr, String delemiterStr) {
        String[] columnNms;

        if (delemiterStr.equals("|")) {
            columnNms = columnNmsStr.split("\\|");
        } else {
            columnNms = columnNmsStr.split(delemiterStr);
        }


        return columnNms;
    }

    /**
     * hold mapped and filtered columns information for only items,Height.IG_VITAL_GROUP1.HeightOID
     * any columns if not configured in mapping file will not be used  as items to build ODM
     * <p>
     * return HashMap
     */
    private static HashMap getDataMappedValues(String rawMappingStr, String[] columnNms) {

        HashMap mappedValues = new HashMap<>();
        ArrayList itemGroupOIDList = new ArrayList<>();
        ArrayList mappedColumnNameList = new ArrayList<>();
        String[] keyValueStr;
        String key;
        String val;

        //Iterate to get the item column values
        String[] rawMappingStrRows = rawMappingStr.split(new Character((char) 13).toString());

        //Loop through all the rows
        for (int j = 0; j < rawMappingStrRows.length; j++) {

            String rawMappingStrRowsStr = rawMappingStrRows[j];
            if (rawMappingStrRowsStr.startsWith("#"))
                continue;
            keyValueStr = rawMappingStrRowsStr.split("=");
            //logger.info("++keyValueStr======================+" +keyValueStr);

            if (keyValueStr.length < 2) {
                ;
            } else {
                key = keyValueStr[0].trim();
                val = keyValueStr[1].trim().replaceAll("/n|||/r", "");

                //extract the configuration data
                if (key.equals("StudyOID") || key.substring(1).equals("StudyOID")) {
                    mappedValues.put("StudyOID", val);
                } else if (key.equals("StudyEventOID") || key.substring(1).equals("StudyEventOID")) {
                    mappedValues.put("StudyEventOID", val);
                } else if (key.equals("FormOID") || key.substring(1).equals("FormOID")) {
                    mappedValues.put("FormOID", val);
                } else if (key.equals("FormVersion") || key.substring(1).equals("FormVersion")) {
                    mappedValues.put("FormVersion", val);
                    //SkipMatchCriteria
                } else if (key.equals("SkipMatchCriteria") || key.substring(1).equals("SkipMatchCriteria")) {
                    mappedValues.put("SkipMatchCriteria", val);
                } else if (key != null && (key.trim().startsWith(PARTICIPANT_ID_HEADER_PROPERTY) || key.trim().indexOf(PARTICIPANT_ID_HEADER_PROPERTY) == 1)) {
                    mappedValues.put(PARTICIPANT_ID_HEADER_PROPERTY, val);
                } else if (key != null && (key.trim().startsWith(DELIMITER_PROPERTY) || key.trim().indexOf(DELIMITER_PROPERTY) == 1)) {
                    mappedValues.put(DELIMITER_PROPERTY, val);
                } else {
                    // item OID: Height=IG_VITAL_GROUP1.HeightOID
                    //boolean isCorrectFormat = checkFormItemMappingFormat(rawMappingStrRowsStr);
                    boolean isCorrectFormat = true;
                    if (isCorrectFormat) {
                        String tempKeyValStr = key + val;
                        if (tempKeyValStr != null && tempKeyValStr.trim().length() > 0) {

                            String[] itemMappingvalue = toArray(val, ".");
                            // logger.info("===********************itemMappingvalue:" + itemMappingvalue);
                            /**
                             * save data in each rwo:
                             * itemGrpOid -- key -- itemOid
                             * one special row for repeatingKey
                             * Repeatingkey -- Repeatingkey -- true
                             */
                            //System.out.println(itemMappingvalue);
                            String itemGrpOid = itemMappingvalue[0];
                            String itemOid = itemMappingvalue[1];
                            String[] itemMappingRow = {itemGrpOid, key, itemOid};

                            mappedColumnNameList.add(itemMappingRow);

                            //itemGroupOIDList contain unique groupOID
                            if (!(itemGroupOIDList.contains(itemGrpOid))) {
                                itemGroupOIDList.add(itemGrpOid);
                            }

                        }
                    }


                    if (key.equals("Repeatingkey")) {
                        mappedValues.put("useRepeatingkey", "TRUE");
                    }

                }

            }

        }

        mappedValues.put("itemGroupOIDList", itemGroupOIDList);
        mappedValues.put("mappedColumnNameList", mappedColumnNameList);

        return mappedValues;
    }

    /**
     * Here is the expected format:
     * Height Units=IG_VITAL_GROUP1.HeightUnitsOID
     * @param rawMappingStrRowsStr
     */
    private static boolean checkFormItemMappingFormat(String rawMappingStrRowsStr) {
        // use regular exoression to check item configuration
        String regex = "^[A-Za-z0-9+_-[ ]*[\n]*]+=[A-Za-z0-9+_-]+[.][A-Za-z0-9+_-]+$";
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(rawMappingStrRowsStr);
        return matcher.matches();
    }

    public boolean hasParticipantIDColumn(String participantIdHeader, String[] columnNms) {

        boolean found = false;
        String textStr;

        for (int i = 0; i < columnNms.length; i++) {
            //System.out.println("columnNms==========================" + columnNms[i]);
            if (columnNms[i].trim().equals(participantIdHeader)) {
                found = true;
                break;
            }

            /**
             *  in case data is in UTF-8 or  UTF-8-BOM etc encoding
             */
            textStr = columnNms[i].substring(1);
            if (textStr.trim().equals(participantIdHeader)) {
                found = true;
                break;
            }

        }

        return found;
    }

    /**
     * @param rawItemDataFile
     * @param mappingFile
     * @return
     */
    public String getSkipMatchCriteria(File rawItemDataFile, File mappingFile) throws OpenClinicaSystemException, IOException {

        String rawMappingStr = readFileToString(mappingFile);
        String rawItemData = readFileToString(rawItemDataFile);
        String[] columnNms = getDataColumnNames(rawItemData);
        HashMap mappedValues = getDataMappedValues(rawMappingStr, columnNms);

        return (String) mappedValues.get("SkipMatchCriteria");
    }

    /**
     * @param mappingFile
     * @return
     * @throws IOException
     */
    public String getStudyOidFromMappingFile(File mappingFile) throws IOException {
        String studyOID = null;

        try (Scanner sc = new Scanner(mappingFile)) {
            String currentLine;

            while (sc.hasNextLine()) {
                currentLine = sc.nextLine();

                if (currentLine.indexOf("StudyOID=") >= 0) {
                    String[] tempArray = currentLine.split("=");

                    studyOID = tempArray[1].trim();
                }

            }

        }

        return studyOID;
    }

    /**
     * @param mappingFile
     * @return
     * @throws IOException
     */
    public String getStudyOidFromMappingFile(final MultipartFile mappingFile) throws IOException {
        String studyOID = null;
        String currentLine;

        BufferedReader reader = new BufferedReader(new InputStreamReader(mappingFile.getInputStream()));

        while (reader.ready()) {
            currentLine = reader.readLine();

            if (currentLine.indexOf("StudyOID=") > -1) {
                String[] tempArray = currentLine.split("=");

                studyOID = tempArray[1].trim();
            }

        }

        return studyOID;
    }

    public String getStudySubject(File mappingFile, File rawItemDataFile) throws OpenClinicaSystemException, Exception {
        ResourceBundleProvider.updateLocale(Locale.US);
        ResourceBundle respage = ResourceBundleProvider.getPageMessagesBundle(Locale.US);
        MessageFormat mf = new MessageFormat("");

        String participantLabel = getParticipantID(mappingFile, rawItemDataFile);
        //Empty participant label in csv, gives ""
        participantLabel = participantLabel.replaceAll("\"", "");
        StudySubjectDAO studySubjectDAO = new StudySubjectDAO(ds);
        StudySubjectBean studySubjectBean = studySubjectDAO.findByLabel(participantLabel);

        if (studySubjectBean.getId() == 0) {
            mf.applyPattern(respage.getString("your_subject_label_does_not_reference"));
            Object[] arguments = {participantLabel};
            throw new OpenClinicaSystemException("errorCode.ValidationFailed", mf.format(arguments));
        }
        return participantLabel;
    }

    /**
     * FormOID=F_DEMOGRAPHICS
     * FormVersion=1
     * StudyOID=S_TEST_STU(TEST)
     * StudyEventOID=SE_COMMON
     * @param file
     * @throws IOException
     */
    public HashMap validateMappingFile(File file) throws IOException, OpenClinicaSystemException {

        HashMap hm = new HashMap();

        boolean foundFormOID = false;
        boolean foundFormVersion = false;
        boolean foundStudyOID = false;
        boolean foundStudyEventOID = false;

        String formOIDValue = null;
        String formVersionValue = null;
        String studyOIDValue = null;
        String studyEventOIDValue = null;

        String errorMsg = null;
        ArrayList<String> errorMsgs = new ArrayList<>();
        ArrayList<ImportItemGroupDTO> importItemGroupDTOs = new ArrayList<ImportItemGroupDTO>();

        try (Scanner sc = new Scanner(file)) {

            String currentLine;

            while (sc.hasNextLine()) {
                currentLine = sc.nextLine();

                // skip comment out line or blank line
                if (currentLine != null && currentLine.trim().length() > 0 && !(currentLine.startsWith("#"))) {
                    String[] mappingRow = currentLine.split("=");

                    if (mappingRow.length == 2) {
                        String keyWord = mappingRow[0];
                        String value = mappingRow[1];

                        if (keyWord != null && keyWord.trim().length() > 0) {
                            ;
                        } else {
                            errorMsg = "Invalid format for " + currentLine + ". Format must be 'key=value'";
                            errorMsgs.add(errorMsg);

                        }

                        if (value != null && value.trim().length() > 0) {
                            ;
                        } else {
                            errorMsg = "Invalid format for " + currentLine + ". Format must be 'key=value'";
                            errorMsgs.add(errorMsg);
                        }

                        hm.put(keyWord, value);

                        if (keyWord != null && keyWord.trim().startsWith("FormOID") && value != null && value.trim().length() > 0) {
                            formOIDValue = value.trim();
                            foundFormOID = true;
                        } else if (keyWord != null && keyWord.trim().startsWith("FormVersion") && value != null && value.trim().length() > 0) {
                            formVersionValue = value.trim();
                            if (formVersionValue != null || formVersionValue.trim().length() > 0) {
                                foundFormVersion = true;
                            }

                        } else if (keyWord != null && keyWord.trim().startsWith("StudyOID") && value != null && value.trim().length() > 0) {
                            studyOIDValue = value.trim();
                            foundStudyOID = true;
                        } else if (keyWord != null && keyWord.trim().startsWith("StudyEventOID") && value != null && value.trim().length() > 0) {
                            studyEventOIDValue = value.trim();
                            foundStudyEventOID = true;
                        } else if (keyWord != null && (keyWord.trim().startsWith("SkipMatchCriteria") || keyWord.trim().indexOf("SkipMatchCriteria") == 1)) {
                            //check SkipMatchCriteria format
                            if (value != null && value.trim().length() > 0) {
                                errorMsg = this.validateSkipMatchCriteriaFormat(currentLine);

                                if (errorMsg != null) {
                                    errorMsgs.add(errorMsg);
                                } else {
                                    ArrayList<ImportItemGroupDTO> importItemGroupDTOsFromSkipMatchCriteria = this.convertSkipMatchCriteriaToImportItemGroupDTO(mappingRow);
                                    for (ImportItemGroupDTO itgd : importItemGroupDTOsFromSkipMatchCriteria) {
                                        addToItemGroupDTOList(importItemGroupDTOs, itgd);
                                    }
                                }
                            }

                        } else if (keyWord != null && (keyWord.trim().startsWith(PARTICIPANT_ID_HEADER_PROPERTY) || keyWord.trim().indexOf("SkipMatchCriteria") == 1)) {
                            // do nothing
                        } else if (keyWord != null && (keyWord.trim().startsWith(DELIMITER_PROPERTY) || keyWord.trim().indexOf("SkipMatchCriteria") == 1)) {
                            // do nothing
                        } else {
                            //check item configuration format
                            errorMsg = this.validateItemFormat(mappingRow);
                            if (errorMsg != null) {
                                errorMsgs.add(errorMsg);
                            }

                            ImportItemGroupDTO importItemGroupDTO = this.convertToImportItemGroupDTO(mappingRow);

                            addToItemGroupDTOList(importItemGroupDTOs, importItemGroupDTO);

                        }
                    } else {
                        errorMsg = "Invalid format for " + currentLine + ". Only one '=' can be included in this line.";
                        errorMsgs.add(errorMsg);
                    }
                }
            }// end of while loop

        }

        if (!foundFormOID) {
            throw new OpenClinicaSystemException("errorCode.noFormOID", "Please check mapping file, make sure that it has correct FormOID configuration.  ");
        }

        if (!foundStudyOID) {
            throw new OpenClinicaSystemException("errorCode.noStudyOID", "Please check mapping file, make sure that it has correct StudyOID configuration.  ");
        }

        if (!foundStudyEventOID) {
            throw new OpenClinicaSystemException("errorCode.noStudyEventOID", "Please check mapping file, make sure that it has correct StudyEventOID configuration.  ");
        }

        if (errorMsgs.size() > 0) {
            throw new OpenClinicaSystemException("errorCode.missingItemOrItemGroupOID", errorMsgs.toString());
        }

        // check against current system/DB
        errorMsgs.clear();
        ArrayList<ErrorObj> errors = this.validateStudyMetadata(formOIDValue, formVersionValue, studyOIDValue, studyEventOIDValue, importItemGroupDTOs, hm, null);
        if (errors.size() > 0) {
            throw new OpenClinicaSystemException(errors.get(0).getCode(), errors.get(0).getMessage());
        }

        return hm;
    }

    /**
     * @param importItemGroupDTOs
     * @param importItemGroupDTO
     */
    private void addToItemGroupDTOList(ArrayList<ImportItemGroupDTO> importItemGroupDTOs,
                                       ImportItemGroupDTO importItemGroupDTO) {

        boolean found = false;
        if (importItemGroupDTOs.isEmpty()) {
            importItemGroupDTOs.add(importItemGroupDTO);
        } else {
            for (ImportItemGroupDTO itemGroupDTO : importItemGroupDTOs) {
                String itemGroupOID = itemGroupDTO.getItemGroupOID();
                if (itemGroupOID != null && itemGroupOID.equals(importItemGroupDTO.getItemGroupOID())) {
                    found = true;
                    itemGroupDTO.getItemOIDs().addAll(importItemGroupDTO.getItemOIDs());
                }

            }

            if (!found) {
                importItemGroupDTOs.add(importItemGroupDTO);
            }
        }
    }

    /**
     * Height Units=IG_VITAL_GROUP1.HeightUnitsOID
     * @param keyValueStr
     * @return
     */
    private String validateItemFormat(String[] keyValueStr) {

        String errorMsg = null;
        String key;
        String val;

        if (keyValueStr.length < 2) {
            ;
        } else {
            key = keyValueStr[0].trim();
            val = keyValueStr[1].trim().replaceAll("/n|||/r", "");

            String[] itemMappingvalue = toArray(val, ".");

            if (itemMappingvalue.length < 2) {
                errorMsg = "Invalid mapping format for " + val + ". Item mapping must be in the format ColumnName=ItemGroupOID.ItemOID.\n";
                return errorMsg;
            }

            String itemGrpOid = itemMappingvalue[0];
            String itemOid = itemMappingvalue[1];

            if (itemGrpOid == null || itemGrpOid.trim().length() == 0) {
                errorMsg = "Invalid mapping format for " + keyValueStr[0] + "=" + keyValueStr[1] + ". Item mapping must be in the format ColumnName=ItemGroupOID.ItemOID.\n";
                return errorMsg;
            }

            if (itemOid == null || itemOid.trim().length() == 0) {
                errorMsg = "Invalid mapping format for " + keyValueStr[0] + "=" + keyValueStr[1] + ". Item mapping must be in the format ColumnName=ItemGroupOID.ItemOID.\n";
                return errorMsg;
            }
        }

        return errorMsg;
    }


    /**
     * Height Units=IG_VITAL_GROUP1.HeightUnitsOID
     * @param keyValueStr
     * @return
     */
    private ImportItemGroupDTO convertToImportItemGroupDTO(String[] keyValueStr) {

        ImportItemGroupDTO importItemGroupDTO = null;
        String key;
        String val;

        if (keyValueStr.length < 2) {
            ;
        } else {
            key = keyValueStr[0].trim();
            val = keyValueStr[1].trim().replaceAll("/n|||/r", "");

            String[] itemMappingvalue = toArray(val, ".");

            String itemGroupOID = itemMappingvalue[0];
            String itemOID = itemMappingvalue[1];

            importItemGroupDTO = new ImportItemGroupDTO();
            importItemGroupDTO.setItemGroupOID(itemGroupOID.trim());
            importItemGroupDTO.getItemOIDs().add(itemOID.trim());
        }

        return importItemGroupDTO;
    }

    /**
     * SkipMatchCriteria = IG_LAB_O_OTHERCHEM.I_LAB_O_OR_ODATE, IG_LAB_O_OTHERCHEM.I_LAB_O_LABINFO
     * convert : IG_LAB_O_OTHERCHEM.I_LAB_O_OR_ODATE to ImportItemGroupDTO
     * @param keyValueStr
     * @return
     */
    private ArrayList<ImportItemGroupDTO> convertSkipMatchCriteriaToImportItemGroupDTO(String[] keyValueStr) {

        ArrayList importItemGroupDTOs = new ArrayList<ImportItemGroupDTO>();
        ImportItemGroupDTO importItemGroupDTO = null;

        String val;
        String skipMatchCriteriaStr;
        String[] skipMatchCriteriaVal;

        skipMatchCriteriaStr = keyValueStr[1].trim().replaceAll("/n|||/r", "");
        skipMatchCriteriaVal = this.toArray(skipMatchCriteriaStr, ",");

        for (int i = 0; i < skipMatchCriteriaVal.length; i++) {
            val = skipMatchCriteriaVal[i];
            String[] itemMappingvalue = toArray(val, ".");

            if (itemMappingvalue.length != 2) {

            }

            String itemGroupOID = itemMappingvalue[0];
            String itemOID = itemMappingvalue[1];

            importItemGroupDTO = new ImportItemGroupDTO();
            importItemGroupDTO.setItemGroupOID(itemGroupOID.trim());
            importItemGroupDTO.getItemOIDs().add(itemOID.trim());

            importItemGroupDTOs.add(importItemGroupDTO);
        }

        return importItemGroupDTOs;
    }

    private String validateSkipMatchCriteriaFormat(String currentLine) {
        String[] keyValueStr = currentLine.split("=");
        String errorMsg = null;
        String key;
        String skipMatchCriteriaStr;
        String[] skipMatchCriteriaVal;

        if (keyValueStr.length != 2) {
            errorMsg = "Invalid SkipMatchCriteria parameter " + currentLine + ". Each parameter must be in the format  ItemGroupOID.ItemOID with additional parameters separated by commas.";
            return errorMsg;
        } else {
            key = keyValueStr[0].trim();
            skipMatchCriteriaStr = keyValueStr[1].trim().replaceAll("/n|||/r", "");

            skipMatchCriteriaVal = this.toArray(skipMatchCriteriaStr, ",");
            for (int i = 0; i < skipMatchCriteriaVal.length; i++) {
                String val = skipMatchCriteriaVal[i];
                String[] itemMappingvalue = toArray(val, ".");

                if (itemMappingvalue.length < 2) {
                    errorMsg = "Invalid SkipMatchCriteria parameter " + val + ". Each parameter must be in the format ItemGroupOID.ItemOID with additional parameters separated by commas.\n";
                    return errorMsg;
                } else if (itemMappingvalue.length > 2) {
                    errorMsg = "Invalid SkipMatchCriteria parameter " + val + ". Each parameter must be in the format  ItemGroupOID.ItemOID with additional parameters separated by commas.\n";
                    return errorMsg;
                }

                String itemGrpOid = itemMappingvalue[0];
                String itemOid = itemMappingvalue[1];

                if (itemGrpOid == null || itemGrpOid.trim().length() == 0) {
                    errorMsg = "Invalid SkipMatchCriteria parameter " + val + ". Each parameter must be in the format ItemGroupOID.ItemOID with additional parameters separated by commas. \n";
                    return errorMsg;
                }

                if (itemOid == null || itemOid.trim().length() == 0) {
                    errorMsg = "Invalid SkipMatchCriteria parameter " + val + ". Each parameter must be in the format ItemGroupOID.ItemOID with additional parameters separated by commas. \n";
                    return errorMsg;
                }

                //continue to check item group and item oid


            }

        }

        return errorMsg;

    }


    public ArrayList<ErrorObj> validateStudyMetadata(String formOIDValue,
                                                     String formVersionValue,
                                                     String studyOIDValue,
                                                     String studyEventOIDValue,
                                                     ArrayList<ImportItemGroupDTO> importItemGroupDTOs,
                                                     HashMap hm,
                                                     Locale newLocale) {

        ArrayList<ErrorObj> errors = new ArrayList<ErrorObj>();
        Locale locale;
        ErrorObj eo;
        if (newLocale != null) {
            locale = newLocale;
        } else {
            locale = Locale.US;
        }
        ResourceBundleProvider.updateLocale(locale);
        ResourceBundle respage = ResourceBundleProvider.getPageMessagesBundle(locale);
        MessageFormat mf = new MessageFormat("");

        try {
            // check 1: study
            String studyOid = studyOIDValue;
            Study studyBean = studyDao.findByOcOID(studyOid);
            if (studyBean == null) {
                mf.applyPattern(respage.getString("your_study_oid_does_not_reference_an_existing"));
                Object[] arguments = {studyOid};
                eo = new ErrorObj(ErrorConstants.ERR_STUDY_NOT_EXIST, mf.format(arguments));
                errors.add(eo);

            } else if (!studyBuildService.isPublicStudySameAsTenantStudy(studyBean, studyOid)) {
                mf.applyPattern(respage.getString("your_current_study_is_not_the_same_as"));
                Object[] arguments = {studyBean.getName()};
                eo = new ErrorObj(ErrorConstants.ERR_STUDY_NOT_EXIST, mf.format(arguments));
                errors.add(eo);
            }

            // check 2:study event
            StudyEventDefinitionDAO studyEventDefinitionDAO = new StudyEventDefinitionDAO(ds);
            FormLayoutDAO formLayoutDAO = new FormLayoutDAO(ds);
            ItemGroupDAO itemGroupDAO = new ItemGroupDAO(ds);
            ItemDAO itemDAO = new ItemDAO(ds);
            CRFDAO crfDAO = new CRFDAO(ds);
            EventDefinitionCRFDAO edcDAO = new EventDefinitionCRFDAO(ds);

            String sedOid = studyEventOIDValue;
            StudyEventDefinitionBean studyEventDefintionBean = studyEventDefinitionDAO.findByOidAndStudy(sedOid, studyBean.getStudyId(),
                    studyBean.checkAndGetParentStudyId());

            if (studyEventDefintionBean == null) {
                mf.applyPattern(respage.getString("your_study_event_oid_for_study_oid"));
                Object[] arguments = {sedOid, studyOid};
                eo = new ErrorObj(ErrorConstants.ERR_EVENT_NOT_EXIST, mf.format(arguments));
                errors.add(eo);
            }


            // check Form
            String formOid = formOIDValue;
            String formLayoutName = null;
            boolean needToCheckFormVersion = false;

            if (formVersionValue != null) {
                formLayoutName = formVersionValue;
                needToCheckFormVersion = true;
            }

            CRFBean crfBean = crfDAO.findByOid(formOid);
            if (crfBean != null && studyEventDefintionBean != null) {
                EventDefinitionCRFBean edcBean = edcDAO.findByStudyEventDefinitionIdAndCRFId(studyBean, studyEventDefintionBean.getId(),
                        crfBean.getId());
                if (edcBean == null || edcBean.getId() == 0) {
                    mf.applyPattern(respage.getString("your_form_oid_for_study_event_oid"));
                    Object[] arguments = {formOid, sedOid};
                    eo = new ErrorObj(ErrorConstants.ERR_EVENT_NOT_EXIST, mf.format(arguments));
                    errors.add(eo);
                } else {
                    if (formVersionValue == null) {
                        FormLayoutBean formLayoutBean = null;
                        int formLayoutId = edcBean.getDefaultVersionId();
                        if (formLayoutId == 0) {
                            logger.error("FormLayout is null");
                            return null;
                        } else {
                            formLayoutBean = (FormLayoutBean) formLayoutDAO.findByPK(formLayoutId);
                        }
                        formLayoutName = formLayoutBean.getName();
                        formVersionValue = formLayoutBean.getName();
                        hm.put("FormVersion", formVersionValue);
                    }
                }
            }

            if (crfBean != null) {
                if (needToCheckFormVersion) {
                    FormLayoutBean formLayoutBean = (FormLayoutBean) formLayoutDAO.findByFullName(formLayoutName, crfBean.getName());
                    if (formLayoutBean == null || formLayoutBean.getId() == 0) {
                        mf.applyPattern(respage.getString("your_form_layout_oid_for_form_oid"));
                        Object[] arguments = {formLayoutName, formOid};
                        eo = new ErrorObj(ErrorConstants.ERR_FORM_LAYOUT, mf.format(arguments));
                        errors.add(eo);
                    }
                }

            } else {
                mf.applyPattern(respage.getString("your_form_oid_did_not_generate"));
                Object[] arguments = {formOid};
                eo = new ErrorObj(ErrorConstants.ERR_FORM, mf.format(arguments));
                errors.add(eo);
            }

            // check item group and Item OID
            if (importItemGroupDTOs != null) {
                for (ImportItemGroupDTO importItemGroupDTO : importItemGroupDTOs) {
                    String itemGroupOID = importItemGroupDTO.getItemGroupOID();
                    ItemGroupBean itemGroupBean = itemGroupDAO.findByOid(itemGroupOID);
                    if (itemGroupBean != null && crfBean != null) {
                        itemGroupBean = itemGroupDAO.findByOidAndCrf(itemGroupOID, crfBean.getId());
                        if (itemGroupBean == null) {
                            mf.applyPattern(respage.getString("your_item_group_oid_for_form_oid"));
                            Object[] arguments = {itemGroupOID, formOid};
                            eo = new ErrorObj(ErrorConstants.ERR_ITEM_GROUP_OID, mf.format(arguments));
                            errors.add(eo);
                        }
                    } else if (itemGroupBean == null) {
                        mf.applyPattern(respage.getString("the_item_group_oid_did_not"));
                        Object[] arguments = {itemGroupOID};
                        eo = new ErrorObj(ErrorConstants.ERR_ITEM_GROUP_OID, mf.format(arguments));
                        errors.add(eo);
                    }

                    ArrayList<String> itemOIDs = importItemGroupDTO.getItemOIDs();
                    if (itemOIDs != null) {
                        for (String itemOID : itemOIDs) {

                            List<ItemBean> itemBeans = (List<ItemBean>) itemDAO.findByOid(itemOID);
                            if (itemBeans.size() != 0 && itemGroupBean != null) {
                                ItemBean itemBean = itemDAO.findItemByGroupIdandItemOid(itemGroupBean.getId(), itemOID);
                                if (itemBean == null) {
                                    mf.applyPattern(respage.getString("your_item_oid_for_item_group_oid"));
                                    Object[] arguments = {itemOID, itemGroupOID};
                                    eo = new ErrorObj(ErrorConstants.ERR_ITEM_OID, mf.format(arguments));
                                    errors.add(eo);
                                }
                            } else if (itemBeans.size() == 0) {
                                mf.applyPattern(respage.getString("the_item_oid_did_not"));
                                Object[] arguments = {itemOID};
                                eo = new ErrorObj(ErrorConstants.ERR_ITEM_OID, mf.format(arguments));
                                errors.add(eo);
                            }
                        } // itemOID
                    } // if (itemOIDs != null)
                } // importItemGroupDTOs
            } // if (importItemGroupDTOs != null)


        } catch (NullPointerException npe) {
            logger.debug("found a nullpointer here");
        }
        // if errors == null you pass, if not you fail
        return errors;
    }


    public String getParticipantID(File mappingFile, File rawItemDataFile) throws OpenClinicaSystemException, IOException {

        String rawMappingStr;
        String rawItemData;
        String participantID = null;


        rawMappingStr = this.readFileToString(mappingFile);
        rawItemData = this.readFileToString(rawItemDataFile);

        participantID = getParticipantID(rawMappingStr, rawItemData);

        return participantID;

    }

    public String getParticipantID(String rawMappingStr, String rawItemData) throws OpenClinicaSystemException {

        String subjectKey;

        /**
         * Hold all ItemOIDs coming from mapping file
         * each ite, like:
         *  ItemGroupOID--Item Name -- Item OID
         */
        ArrayList mappedColumnNameList = null;
        /**
         * Hold all ItemGroupOIDs coming from mapping file
         */
        Object[] mappingItemGroupOIDs;

        String[] columnNms = getDataColumnNames(rawItemData);
        HashMap mappedValues = getDataMappedValues(rawMappingStr, columnNms);
        ResourceBundleProvider.updateLocale(Locale.US);
        ResourceBundle respage = ResourceBundleProvider.getPageMessagesBundle(Locale.US);

        try {

            columnNms = getDataColumnNames(rawItemData);

            mappedValues = getDataMappedValues(rawMappingStr, columnNms);
            mappedColumnNameList = (ArrayList) mappedValues.get("mappedColumnNameList");
            ArrayList itemGroupOIDList = (ArrayList) mappedValues.get("itemGroupOIDList");


            String participantIdHeader = (String) mappedValues.get(PARTICIPANT_ID_HEADER_PROPERTY);
            if (StringUtils.isBlank(participantIdHeader)) {
                participantIdHeader = DEFAULT_PARTICIPANT_ID_HEADER;
            }

            mappingItemGroupOIDs = (Object[]) itemGroupOIDList.toArray();
            String[] dataRows = rawItemData.split(new Character((char) 13).toString());

            int indexofParticipantID = 0;

            for (int i = 0; i < dataRows.length; i++) {
                String tempDataRowStr = dataRows[i].toString().replaceAll("\n", "");
                if (tempDataRowStr.endsWith("|")) {
                    tempDataRowStr = tempDataRowStr + " ";
                }
                String[] dataRow = this.toArrayWithFullItems(tempDataRowStr, "|");
                // find subject OID, It may be at any position
                if (i == 0) {
                    for (int k = 0; i < dataRow.length; k++) {
                        if (dataRow[k].toString().trim().equals(participantIdHeader) || dataRow[k].substring(1).trim().equals(participantIdHeader)) {
                            indexofParticipantID = k;

                            break;
                        }
                    }
                } else {
                    if (dataRows[i].toString().replaceAll("/n|||/r", "").trim().length() > 0) {
                        subjectKey = dataRow[indexofParticipantID].toString().trim();

                        return subjectKey;
                    }

                }


            }


        } catch (Exception e) {

            String msg = respage.getString("participant_id_header_not_matching_mapping_file");
            throw new OpenClinicaSystemException("errorCode.participantIdHeaderNotMatchingMappingFile", msg);
        }

        return "";
    }


}
