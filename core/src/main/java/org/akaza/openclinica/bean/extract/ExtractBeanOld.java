/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.extract;

/**
 * ExtractBean.java, the object which organizes data from the view after it has
 * been pulled from the database. Basically contains <br/>--an arraylist of
 * subject names, <br/>--an arraylist of column names, <br/>--and the hashmap
 * for row-column values. <br/>May also generate the values, based on export
 * format required.
 *
 * @author thickerson
 *
 * TODO add ability to extract in: SAS, STATA, SPSS formats TODO in the far
 * future possibly create XLS and PDF formats???
 */
public class ExtractBeanOld {
    //
    // public static final int SAS_FORMAT = 1;
    //
    // public static final int SPSS_FORMAT = 2;
    //
    // public static final int CSV_FORMAT = 3;
    //
    // public static final int PDF_FORMAT = 4;
    //
    // public static final int XLS_FORMAT = 5;
    //
    // public static final int TXT_FORMAT = 6;
    //
    // private int format = 1;
    //
    // private String datasetName = "";
    //
    // private Date dateCreated;
    //
    // private String parentStudyName = "";
    //
    // private String parentProtocolId = "";
    //
    // private String siteName = "";
    //
    // private OrderedEntityBeansSet studyEventDefinitions = new
    // OrderedEntityBeansSet(
    // new ExtractStudyEventDefinitionBean());
    //
    // private OrderedEntityBeansSet studySubjects = new OrderedEntityBeansSet(
    // new ExtractStudySubjectBean());
    //
    // private int studySubjectNum = 0;
    //
    // private ArrayList itemNames = new ArrayList();
    //
    // private ArrayList itemDbNames = new ArrayList();// the item name in the
    // DB
    //
    // private ArrayList eventNames = new ArrayList();
    //
    // private ResponseSetBean SPSSNames = new ResponseSetBean();
    // private ResponseSetBean SPSSLabels = new ResponseSetBean();
    // //private ResponseSetBean responseSet = new ResponseSetBean();
    //
    // private ArrayList itemValues = new ArrayList();
    //
    // private HashMap SPSSColLengths = new HashMap();
    //
    // private int getSPSSColLength(String name) {
    // if (SPSSColLengths.containsKey(name)) {
    // Integer i = (Integer) SPSSColLengths.get(name);
    // return i.intValue();
    // } else {
    // return 1;
    // }
    // }
    //
    // /**
    // * Key is item name for the result set you are trying
    // * to turn into SPSS format
    // */
    // private HashMap responseSets = new HashMap();
    //
    // /**
    // * Key is the Value number, and the value is the
    // * id number that will be declared a SYSMIS value
    // * in the SPSS syntax file, tbh
    // */
    // private HashMap missingVals = new HashMap();
    // /**
    // * Key is studyEventDefinitionId. Value is Integer whose intValue() is the
    // max
    // * number of samples over all the subjects in this dataset.
    // */
    // private HashMap numSamplesBySED = new HashMap();
    //
    // /**
    // * @return Returns the itemDbNames.
    // */
    // public ArrayList getItemDbNames() {
    // return itemDbNames;
    // }
    //
    // /**
    // * @param itemDbNames
    // * The itemDbNames to set.
    // */
    // public void setItemDbNames(ArrayList itemDbNames) {
    // this.itemDbNames = itemDbNames;
    // }
    //
    // public void addMissingValue(String key, String value) {
    // this.missingVals.put(key,value);
    // }
    //
    // public ExtractStudyEventDefinitionBean addStudyEventDefinition(Integer
    // studyEventDefinitionId,
    // String studyEventDefinitionName, Boolean repeating) {
    // ExtractStudyEventDefinitionBean sedb = new
    // ExtractStudyEventDefinitionBean();
    //
    // if ((studyEventDefinitionId == null) || (studyEventDefinitionName ==
    // null)
    // || (repeating == null)) {
    // return sedb;
    // }
    //
    // sedb.setId(studyEventDefinitionId.intValue());
    // sedb.setName(studyEventDefinitionName);
    // sedb.setRepeating(repeating.booleanValue());
    //
    // return (ExtractStudyEventDefinitionBean) studyEventDefinitions.add(sedb);
    // }
    //
    // public ExtractStudySubjectBean addStudySubject(Integer studySubjectId,
    // String studySubjectLabel,
    // String studyProtocolId, Date dateOfBirth, String gender) {
    // ExtractStudySubjectBean ssb = new ExtractStudySubjectBean();
    //
    // if ((studySubjectId == null) || (studySubjectLabel == null) ||
    // (studyProtocolId == null)) {
    // return ssb;
    // }
    //
    // ssb.setId(studySubjectId.intValue());
    // ssb.setName(studySubjectLabel);
    // ssb.setStudyProtocolId(studyProtocolId);
    // ssb.setDateOfBirth(dateOfBirth);
    // ssb.setGender(gender);
    // if (dateOfBirth == null) {
    // dateOfBirth = new Date();
    // }
    // Calendar cal = Calendar.getInstance();
    // cal.setTime(dateOfBirth);
    // int year = cal.get(Calendar.YEAR);
    //
    // ssb.setYearOfBirth(year+"");
    // ssb = (ExtractStudySubjectBean) studySubjects.add(ssb);
    //
    // return ssb;
    // }
    //
    // public void updateStudyEventDefinition(ExtractStudyEventDefinitionBean
    // sedb) {
    // studyEventDefinitions.update(sedb);
    // }
    //
    // public void updateStudySubject(ExtractStudySubjectBean ssb) {
    // studySubjects.update(ssb);
    // }
    //
    // public void updateMaxSamples(ExtractStudyEventDefinitionBean sedb, int
    // potentialMaxNumSamples) {
    // Integer key = new Integer(sedb.getId());
    //
    // Integer currMax;
    // if (numSamplesBySED.containsKey(key)) {
    // currMax = (Integer) numSamplesBySED.get(key);
    // } else {
    // currMax = new Integer(0);
    // }
    //
    // if (currMax.intValue() < potentialMaxNumSamples) {
    // numSamplesBySED.put(key, new Integer(potentialMaxNumSamples));
    // }
    // }
    //
    // public int getNumSamples(ExtractStudyEventDefinitionBean sedb) {
    // Integer key = new Integer(sedb.getId());
    //
    // if (numSamplesBySED.containsKey(key)) {
    // Integer numSamples = (Integer) numSamplesBySED.get(key);
    //
    // if (numSamples != null) {
    // return numSamples.intValue();
    // }
    // }
    // return 0;
    // }
    //
    // /**
    // * @param format
    // * The format to set.
    // */
    // public void setFormat(int format) {
    // this.format = format;
    // }
    //
    // public String export() {
    // StringBuffer sb = new StringBuffer();
    //
    // return sb.toString();
    // }
    //
    // public int getNumSamples() {
    // return 0;
    // }
    //
    // //TODO rework entire export so that it is Excel-friendly, tbh
    //
    // //public HSSFWorkbook exportExcel(DatasetBean db) {
    // // HSSFWorkbook wb = new HSSFWorkbook();
    // // HSSFSheet sheet = wb.createSheet("new sheet");
    //
    // // Create a row and put some cells in it. Rows are 0 based.
    // // HSSFRow row = sheet.createRow((short) 0);
    // // Create a cell and put a value in it.
    // //HSSFCell cell = row.createCell((short)0);
    // //cell.setCellValue(1);
    //
    // // Or do it on one line.
    // /*
    // * row.createCell((short)1).setCellValue(1.2);
    // * row.createCell((short)2).setCellValue("This is a string");
    // * row.createCell((short)3).setCellValue(true);
    // */
    // // row.createCell((short) 0).setCellValue("Database Export Header
    // Metadata");
    // // row = sheet.createRow((short) 1);
    // // row.createCell((short) 0).setCellValue("Dataset Name");
    // // row.createCell((short) 1).setCellValue(datasetName);
    // // row = sheet.createRow((short) 2);
    // // row.createCell((short) 0).setCellValue("Date");
    // // java.text.SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
    // // row.createCell((short) 1).setCellValue(sdf.format(new
    // Date(System.currentTimeMillis())));
    // // row = sheet.createRow((short) 3);
    // // row.createCell((short) 0).setCellValue("Protocol ID");
    // // row.createCell((short) 1).setCellValue(parentProtocolId);
    // // row = sheet.createRow((short) 4);
    // // row.createCell((short) 0).setCellValue("Study Name");
    // // row.createCell((short) 1).setCellValue(parentStudyName);
    // // if (!siteName.equals("")) {
    // // row.createCell((short) 2).setCellValue("Site Name");
    // // row.createCell((short) 3).setCellValue(siteName);
    // //}
    // // row = sheet.createRow((short) 5);
    // // row.createCell((short) 0).setCellValue("Subjects");
    // // row.createCell((short)
    // 1).setCellValue(studySubjects.getEntities().size());
    // // row = sheet.createRow((short) 6);
    // //int row_cnt = 6;
    // // row.createCell((short) 0).setCellValue("Study Event Definitions");
    // // row.createCell((short)
    // 1).setCellValue(studyEventDefinitions.getEntities().size());
    // //Iterator it_later = studyEventDefinitions.getEntities().iterator();
    // // Iterator it_header = studyEventDefinitions.getEntities().iterator();
    // // int ordinal_seds = 1;
    // //begin for loop, creating excel ss
    // // for (Iterator it = studyEventDefinitions.getEntities().iterator();
    // it.hasNext();) {
    // // ExtractStudyEventDefinitionBean sed =
    // (ExtractStudyEventDefinitionBean) it.next();
    // // String repeating = "";
    // // if (sed.isRepeating()) {
    // // repeating = " (Repeating) ";
    // // }
    // //if repeating:
    // //change string to (Repeating)
    // //sb.append("Study Event Definition "+ordinal_seds+" "+repeating+sep);
    // //sb.append(sed.getName()+end);
    // // row_cnt++;
    // // row = sheet.createRow((short) row_cnt);//not a 6!
    // // row.createCell((short) 0).setCellValue(
    // // "Study Event Definition " + ordinal_seds + " " + repeating);
    // // row.createCell((short) 1).setCellValue(sed.getName());
    //
    // // int ordinal_crfvs = 1;
    // // for (Iterator itcrf = sed.getCRFVersions().getEntities().iterator();
    // itcrf.hasNext();) {
    // // ExtractCRFVersionBean ecvb = (ExtractCRFVersionBean) itcrf.next();
    // // row_cnt++;
    // // row = sheet.createRow((short) row_cnt);
    // //TODO create an overall int which will track row creation
    // // row.createCell((short) 0).setCellValue("CRF " + ordinal_crfvs);
    // // row.createCell((short) 1).setCellValue(ecvb.getCrfName());
    // //sb.append("CRF "+ordinal_crfvs+sep);
    // //sb.append(ecvb.getCrfName()+sep);
    // //sb.append(ExtractStudyEventDefinitionBean.getCode(ordinal_seds - 1) +
    // // ExtractCRFVersionBean.getCode(ordinal_crfvs));
    //
    // //sb.append(end);
    // // ordinal_crfvs++;
    // //third, iterate through crf versions
    // // }
    // // ordinal_seds++;
    // // }
    //
    // // create the header bar here:
    // //
    // //
    // //header.append("Subject Event Item Values (Item-CRF-Ordinal)"+end);
    // //header.append("Subject Unique ID"+sep+"Protocol-ID-Site ID"+sep);
    // // row = sheet.createRow((short) (row_cnt));
    // // row.createCell((short) 0).setCellValue("Subject Event Item Values
    // (Item-CRF-Ordinal)");
    // // row_cnt++;
    // // row = sheet.createRow((short) (row_cnt));
    // // row.createCell((short) 0).setCellValue("Subject Unique ID");
    // // row.createCell((short) 1).setCellValue("Protocol-ID-Site ID");
    //
    // // ArrayList defs = studyEventDefinitions.getEntities();
    // // ArrayList subjects = studySubjects.getEntities();
    // // row_cnt++;
    // // row = sheet.createRow((short) (row_cnt));
    // // int col_count = 0;
    // // for (int i = 0; i < defs.size(); i++) {
    // // ExtractStudyEventDefinitionBean sed =
    // (ExtractStudyEventDefinitionBean) defs.get(i);
    // // ArrayList crfVersions = sed.getCRFVersions().getEntities();
    //
    // // for (int j = 0; j < crfVersions.size(); j++) {
    // // ExtractCRFVersionBean crfVersion = (ExtractCRFVersionBean)
    // crfVersions.get(j);
    //
    // // for (int k = 1; k <= getNumSamples(sed); k++) {
    // // ArrayList items = crfVersion.getItems().getEntities();
    //
    // // for (int l = 0; l < items.size(); l++) {
    // // ItemBean ib = (ItemBean) items.get(l);
    // // row.createCell((short) col_count).setCellValue(getColumnHeader(sed, i,
    // j + 1, k, ib));
    // // col_count++;
    // //header.append(getColumnHeader(sed, i, j+1, k, ib));
    // //header.append(sep);
    // // }
    // // }
    // // }
    // // }
    //
    // //sb.append(end);
    // //sb.append(header);
    // //sb.append(end);
    //
    // //for (int h = 0; h < subjects.size(); h++) {
    // // ExtractStudySubjectBean studySubj = (ExtractStudySubjectBean)
    // subjects.get(h);
    // // row_cnt++;
    // // row = sheet.createRow((short) (row_cnt));
    // //sb.append(studySubj.getName() + sep);
    // //sb.append(studySubj.getStudyLabel() + sep);
    // // row.createCell((short) 0).setCellValue(studySubj.getName());
    // // row.createCell((short) 1).setCellValue(studySubj.getStudyLabel());
    // // int item_cnt = 1;
    // // for (int i = 0; i < defs.size(); i++) {
    // // ExtractStudyEventDefinitionBean sed =
    // (ExtractStudyEventDefinitionBean) defs.get(i);
    // // ArrayList crfVersions = sed.getCRFVersions().getEntities();
    //
    // // for (int j = 0; j < crfVersions.size(); j++) {
    // // ExtractCRFVersionBean crfVersion = (ExtractCRFVersionBean)
    // crfVersions.get(j);
    //
    // // for (int k = 1; k <= getNumSamples(sed); k++) {
    // // ArrayList items = crfVersion.getItems().getEntities();
    //
    // // for (int l = 0; l < items.size(); l++) {
    // // ItemBean ib = (ItemBean) items.get(l);
    // // item_cnt++;
    // // row.createCell((short) item_cnt).setCellValue(
    // // studySubj.getValue(sed.getId(), k, crfVersion.getId(), ib.getId()));
    // //sb.append(studySubj.getValue(sed.getId(), k,
    // // crfVersion.getId(), ib.getId()));
    // //sb.append(sep);
    // // }
    // // }
    // // }
    // // }
    // //sb.append(end);
    // // }
    //
    // // Write the output to a file
    // /*
    // * FileOutputStream fileOut; try { fileOut = new FileOutputStream(".xls");
    // * wb.write(fileOut); fileOut.close(); } catch (FileNotFoundException e1)
    // {
    // * e1.printStackTrace(); } catch (IOException e) { e.printStackTrace(); }
    // */
    // // return wb;
    // // }
    //
    // public ArrayList exportSPSS(DatasetBean db) {
    // /*
    // * the overall idea here is to create two files, one is the DDL and one is
    // * the data; trick here is to get the max cols for strings
    // */
    // ArrayList files = new ArrayList();
    // //generate data file first, since it's easier
    // //and we need the column numbers
    // String end = "\n";
    // String sep = "\t"; //justify all columns and create tab
    // StringBuffer dataFile = new StringBuffer("");
    // ArrayList defs = studyEventDefinitions.getEntities();
    // ArrayList subjects = studySubjects.getEntities();
    // logger.info("starting first file");
    // /*
    // * major changes to creation of data file and creation
    // * of the second file as well:
    // * SPSS requires numbers to be in every data column, not just
    // * in the places where there are result sets. This requires us to create
    // * custom result sets here in the first stage for people who are creating
    // * SPSS parsable files.
    // * overall goal: create something quick and parse it in at the end of the
    // day,
    // * to make sure it's SPSS-legal. tbh
    // */
    // this.SPSSNames.setResponseTypeId(3);
    // this.SPSSLabels.setResponseTypeId(3);
    // //so that it will get picked up, tbh
    // int rsIterator = 1;
    // int itemIterator = 0;
    // //first pass to create max widths of columns here, tbh
    // for (int h = 0; h < subjects.size(); h++) {
    // ExtractStudySubjectBean studySubj = (ExtractStudySubjectBean)
    // subjects.get(h);
    //
    // //begin by setting a result set item here for names and study labels
    // // ResponseOptionBean nameRo = new ResponseOptionBean();
    // // nameRo.setValue(new Integer(rsIterator).toString());
    // // nameRo.setText(studySubj.getName());
    // // SPSSNames.addOption(nameRo);
    // //
    // // ResponseOptionBean labelRo = new ResponseOptionBean();
    // // labelRo.setValue(new Integer(rsIterator).toString());
    // // labelRo.setText(studySubj.getStudyLabel());
    // // SPSSLabels.addOption(labelRo);
    // this.updateSPSSColLengths("Subject Unique ID", new
    // Integer(rsIterator).toString()
    // .length());
    // this.updateSPSSColLengths("Protocol-ID-Site ID", new
    // Integer(rsIterator).toString()
    // .length());
    // // dataFile.append(rsIterator
    // // + this.generateWhitespace(this.getSPSSWhitespace("Subject Unique ID",
    // new Integer(rsIterator).toString()
    // // .length()))); //+ sep);
    // // logger.info("found whitespace for subject unique id:"+
    // // this.generateWhitespace(this.getSPSSWhitespace("Subject Unique ID",
    // new Integer(rsIterator).toString()
    // // .length()))+";");
    // // // appending max width here
    // // dataFile.append(rsIterator //studySubj.getStudyLabel()
    // // + this.generateWhitespace(this.getSPSSWhitespace("Protocol-ID-Site
    // ID", new Integer(rsIterator).toString()
    // // .length())));// + sep);
    // // logger.info("found whitespace for subject unique id:"+
    // // this.generateWhitespace(this.getSPSSWhitespace("Protocol-ID-Site ID",
    // new Integer(rsIterator).toString()
    // // .length()))+";");
    // // appending max width here
    //
    // rsIterator++;
    //
    // for (int i = 0; i < defs.size(); i++) {
    // ExtractStudyEventDefinitionBean sed = (ExtractStudyEventDefinitionBean)
    // defs.get(i);
    // ArrayList crfVersions = sed.getCRFVersions().getEntities();
    //
    // for (int j = 0; j < crfVersions.size(); j++) {
    // ExtractCRFVersionBean crfVersion = (ExtractCRFVersionBean)
    // crfVersions.get(j);
    //
    // for (int k = 1; k <= getNumSamples(sed); k++) {
    // ArrayList items = crfVersion.getItems().getEntities();
    //
    // for (int l = 0; l < items.size(); l++) {
    // itemIterator++;
    // ItemBean ib = (ItemBean) items.get(l);
    // ResponseSetBean rsb = getResponseSet(ib.getName());
    // String value = studySubj.getValue(sed.getId(),
    // k, crfVersion.getId(), ib.getId());
    // if ((rsb.getResponseTypeId() == 2)||
    // (rsb.getResponseTypeId() == 1)) {
    // //generate the response set for later
    // // ResponseOptionBean itemRo = new ResponseOptionBean();
    // // itemRo.setValue(new Integer(itemIterator).toString());
    // // String answer = studySubj.getValue(sed.getId(),
    // // k, crfVersion.getId(), ib.getId());
    // // itemRo.setText(answer);
    // // rsb.addOption(itemRo);
    // // addResponseSet(ib.getName(),rsb);
    // //set up missing values here
    // if ("".equals(value.trim())) {
    // addMissingValue(ib.getName(),
    // new Integer(itemIterator).toString());
    // }
    // this.updateSPSSColLengths(getColumnHeader(sed, i, j + 1, k, ib),
    // new Integer(itemIterator).toString().length());
    // //dataFile.append(itemIterator);
    // //not sure about the generation of white space here
    // //dataFile.append(this.generateWhitespace(this.getSPSSWhitespace(
    // // getColumnHeader(sed, i, j + 1, k, ib),
    // //new Integer(itemIterator).toString().length())));
    // //dataFile.append(sep);
    // } else {
    // //allow the value to be printed
    // //String val = studySubj.getValue(sed.getId(), k, crfVersion.getId(),
    // ib.getId());
    // this.updateSPSSColLengths(getColumnHeader(sed,
    // i, j + 1, k, ib), value.length());
    // // dataFile.append(studySubj.getValue(sed.getId(), k, crfVersion.getId(),
    // ib.getId()));
    // // dataFile.append(this.generateWhitespace(this.getSPSSWhitespace(
    // // getColumnHeader(sed,
    // // i, j + 1, k, ib),
    // // studySubj.getName().length())));
    //
    // //dataFile.append(sep);
    // }
    //
    // //append max width here
    // }
    // }
    // }
    // }
    // }
    // //generate dump of column lengths
    // logger.info("*** dump of column lengths:");
    // logger.info(this.SPSSColLengths.toString());
    // logger.info("==============================");
    // //reset our iterators
    // rsIterator = 1;
    // itemIterator = 0;
    // //second pass to actually create the data file, tbh
    //
    // for (int h = 0; h < subjects.size(); h++) {
    // ExtractStudySubjectBean studySubj = (ExtractStudySubjectBean)
    // subjects.get(h);
    //
    // //begin by setting a result set item here for names and study labels
    // ResponseOptionBean nameRo = new ResponseOptionBean();
    // nameRo.setValue(new Integer(rsIterator).toString());
    // nameRo.setText(studySubj.getName());
    // SPSSNames.addOption(nameRo);
    //
    // ResponseOptionBean labelRo = new ResponseOptionBean();
    // labelRo.setValue(new Integer(rsIterator).toString());
    // labelRo.setText(studySubj.getStudyLabel());
    // SPSSLabels.addOption(labelRo);
    //
    // dataFile.append(this.generateWhitespace(
    // "Subject Unique ID", new Integer(rsIterator).toString()
    // .length()
    // ) + rsIterator);
    // // dataFile.append(rsIterator
    // // + this.generateWhitespace(this.getSPSSWhitespace("Subject Unique ID",
    // new Integer(rsIterator).toString()
    // // .length()))); //+ sep);
    // logger.info("found whitespace for subject unique id:"+
    // this.generateWhitespace("Subject Unique ID", new
    // Integer(rsIterator).toString()
    // .length())+";");
    // // appending max width here
    // dataFile.append(this.generateWhitespace(
    // "Protocol-ID-Site ID", new Integer(rsIterator).toString()
    // .length()
    // ) + rsIterator);
    // // dataFile.append(rsIterator //studySubj.getStudyLabel()
    // // + this.generateWhitespace(this.getSPSSWhitespace("Protocol-ID-Site
    // ID", new Integer(rsIterator).toString()
    // // .length())));// + sep);
    // logger.info("found whitespace for subject unique id:"+
    // this.generateWhitespace("Protocol-ID-Site ID", new
    // Integer(rsIterator).toString()
    // .length())+";");
    // // appending max width here
    //
    // rsIterator++;
    //
    // for (int i = 0; i < defs.size(); i++) {
    // ExtractStudyEventDefinitionBean sed = (ExtractStudyEventDefinitionBean)
    // defs.get(i);
    // ArrayList crfVersions = sed.getCRFVersions().getEntities();
    //
    // for (int j = 0; j < crfVersions.size(); j++) {
    // ExtractCRFVersionBean crfVersion = (ExtractCRFVersionBean)
    // crfVersions.get(j);
    //
    // for (int k = 1; k <= getNumSamples(sed); k++) {
    // ArrayList items = crfVersion.getItems().getEntities();
    //
    // for (int l = 0; l < items.size(); l++) {
    // itemIterator++;
    // ItemBean ib = (ItemBean) items.get(l);
    // ResponseSetBean rsb = getResponseSet(ib.getName());
    // String value = studySubj.getValue(sed.getId(),
    // k, crfVersion.getId(), ib.getId());
    // if ((rsb.getResponseTypeId() == 2)||
    // (rsb.getResponseTypeId() == 1)) {
    // //generate the response set for later
    // ResponseOptionBean itemRo = new ResponseOptionBean();
    // itemRo.setValue(new Integer(itemIterator).toString());
    // String answer = studySubj.getValue(sed.getId(),
    // k, crfVersion.getId(), ib.getId());
    // itemRo.setText(answer);
    // rsb.addOption(itemRo);
    // addResponseSet(ib.getName(),rsb);
    // //set up missing values here
    // // if ("".equals(answer.trim())) {
    // // addMissingValue(ib.getName(),
    // // new Integer(itemIterator).toString());
    // // }
    //
    // //not sure about the generation of white space here
    // dataFile.append(this.generateWhitespace(
    // getColumnHeader(sed, i, j + 1, k, ib),
    // new Integer(itemIterator).toString().length()));
    // dataFile.append(itemIterator);
    // //dataFile.append(sep);
    // } else {
    // //allow the value to be printed
    //
    // dataFile.append(this.generateWhitespace(
    // getColumnHeader(sed,
    // i, j + 1, k, ib),
    // studySubj.getName().length()));
    //
    // dataFile.append(studySubj.getValue(
    // sed.getId(), k, crfVersion.getId(), ib.getId()));
    // //dataFile.append(sep);
    // }
    //
    // //append max width here
    // }
    // }
    // }
    // }
    // dataFile.append(end);
    // }
    //
    // files.add(dataFile.toString());
    // logger.info("ending first file");
    // //next up -- creating the DDL file
    // //this will be done in several stages:
    // //-create the data list
    // //-create the variable labels
    // //-create the value labels
    // //-create missing values and recode labels (all will equal 99 and SYSMIS
    // // for now)
    // StringBuffer ddlFile = new StringBuffer("");
    // int iterator = 3;
    // int spacer = 0;
    // int endSpacer = 1;
    // ddlFile.append("* NOTE: You will have to change the below line to point "
    // + "to the physical location of your data file.\n");
    // ddlFile.append("DATA LIST FILE=\"PHYSICAL_LOCATION\" /\n");
    // //V1 -- user name
    // spacer++;
    // ddlFile.append("V1 " + spacer + "-");
    // endSpacer = this.getSPSSColLength("Subject Unique ID");//((Integer)
    // //SPSSColLengths.get("Subject Unique ID")).intValue();
    // ddlFile.append(endSpacer + "\t");
    // spacer = endSpacer;
    // spacer++;
    // //V2 -- site id
    // ddlFile.append("V2 " + spacer + "-");
    // int addEnd = 0;
    // try {
    // addEnd = ((Integer) SPSSColLengths.get("Protocol-ID-Site
    // ID")).intValue();
    // } catch (NullPointerException ne) {
    //
    // }
    // endSpacer = spacer + (addEnd-1);
    // //*** work around so that we report only one column if there's
    // //only a single digit...tbh, 6-22
    // //endSpacer = this.getSPSSColLength("Protocol-ID-Site ID");//((Integer)
    // //SPSSColLengths.get("Protocol-ID-Site ID")).intValue();
    // ddlFile.append(endSpacer + "\t");
    // spacer = endSpacer;
    // //TODO extract that column header out, so that we don't have to update
    // both
    // //that and the DsDAO at the same time
    // //logger.info("starting second file");
    // for (int i = 0; i < defs.size(); i++) {
    // ExtractStudyEventDefinitionBean sed = (ExtractStudyEventDefinitionBean)
    // defs.get(i);
    // ArrayList crfVersions = sed.getCRFVersions().getEntities();
    //
    // for (int j = 0; j < crfVersions.size(); j++) {
    // ExtractCRFVersionBean crfVersion = (ExtractCRFVersionBean)
    // crfVersions.get(j);
    //
    // for (int k = 1; k <= getNumSamples(sed); k++) {
    // ArrayList items = crfVersion.getItems().getEntities();
    //
    // for (int l = 0; l < items.size(); l++) {
    // ItemBean ib = (ItemBean) items.get(l);
    // //we don't need study specific information here, but we
    // String columnHeader = getColumnHeader(sed, i, j+1,
    // k, ib); //as opposed to ib.getName(), tbh
    // ddlFile.append("V" + iterator);
    //
    // ddlFile.append(" ");
    // spacer++;
    // int addEndSpacer = 1;
    // try {
    // Integer aSpacer = (Integer)this.SPSSColLengths.get(columnHeader);
    // addEndSpacer = aSpacer.intValue();
    // } catch (NullPointerException ne) {
    // System.out.print("* found npe at "+columnHeader+" "+iterator+" *");
    // // "generating spaces at V"+
    // //iterator + " " + columnHeader);
    // //ne.printStackTrace();
    // }
    // endSpacer = spacer + addEndSpacer - 1;
    // //our little experiment; we need to count the beginning space
    // //as one, so we need to create an effect where 2-4 counts
    // //spaces 2,3,4 as part of the data
    // ddlFile.append(spacer + "-" + endSpacer);
    // spacer = endSpacer;
    //
    // if ((iterator % 3) == 0) {
    // ddlFile.append("\n");
    // } else {
    // //trying to get the end period close to the finish
    // if (l < items.size()-1) {
    // ddlFile.append("\t");
    // }
    // }
    // iterator++;
    // //append max width here
    // }
    // }
    // }
    // }
    // ddlFile.append(".\n\n");
    // //now, add the var labels
    // ddlFile.append("VARIABLE LABELS\n");
    // ddlFile.append("\tV1 \"Subject Unique ID\"\n");
    // ddlFile.append("\tV2 \"Protocol-ID-Site ID\"\n");
    // iterator = 3;
    // for (int i = 0; i < defs.size(); i++) {
    // ExtractStudyEventDefinitionBean sed = (ExtractStudyEventDefinitionBean)
    // defs.get(i);
    // ArrayList crfVersions = sed.getCRFVersions().getEntities();
    //
    // for (int j = 0; j < crfVersions.size(); j++) {
    // ExtractCRFVersionBean crfVersion = (ExtractCRFVersionBean)
    // crfVersions.get(j);
    //
    // for (int k = 1; k <= getNumSamples(sed); k++) {
    // ArrayList items = crfVersion.getItems().getEntities();
    //
    // for (int l = 0; l < items.size(); l++) {
    // ItemBean ib = (ItemBean) items.get(l);
    //
    // String columnHeader = ib.getName();
    // ddlFile.append("\tV" + iterator + " \"" + columnHeader + "\"\n");
    // iterator++;
    //
    // }
    // }
    // }
    // }
    // ddlFile.append(".\n\n");
    // //next step -- add the value labels
    // ddlFile.append("VALUE LABELS\n");
    //
    // //append value labels for name and study label here, tbh
    //
    // //repetitive code, consider placing in its own method, tbh
    // ddlFile.append(this.getSPSSResponseSet("V1",SPSSNames));
    // ddlFile.append(this.getSPSSResponseSet("V2",SPSSLabels));
    // // ddlFile.append("\tV1" + "\n");
    // // Iterator opIt = SPSSNames.getOptions().iterator();
    // // while (opIt.hasNext()) {
    // // ResponseOptionBean ro = new ResponseOptionBean();
    // // ro = (ResponseOptionBean) opIt.next();
    // // ddlFile.append("\t" + ro.getValue() + " " + "\"" + ro.getText() +
    // "\"");
    // // if (opIt.hasNext()) {
    // // ddlFile.append("\n");
    // // } else {
    // // ddlFile.append(" /\n");
    // // }
    // // }
    // //
    // // ddlFile.append("\tV2" + "\n");
    // // Iterator opIt2 = SPSSLabels.getOptions().iterator();
    // // while (opIt2.hasNext()) {
    // // ResponseOptionBean ro = new ResponseOptionBean();
    // // ro = (ResponseOptionBean) opIt2.next();
    // // ddlFile.append("\t" + ro.getValue() + " " + "\"" + ro.getText() +
    // "\"");
    // // if (opIt2.hasNext()) {
    // // ddlFile.append("\n");
    // // } else {
    // // ddlFile.append(" /\n");
    // // }
    // // }
    //
    // iterator = 3;
    // for (int i = 0; i < defs.size(); i++) {
    // ExtractStudyEventDefinitionBean sed = (ExtractStudyEventDefinitionBean)
    // defs.get(i);
    // ArrayList crfVersions = sed.getCRFVersions().getEntities();
    //
    // for (int j = 0; j < crfVersions.size(); j++) {
    // ExtractCRFVersionBean crfVersion = (ExtractCRFVersionBean)
    // crfVersions.get(j);
    //
    // for (int k = 1; k <= getNumSamples(sed); k++) {
    // ArrayList items = crfVersion.getItems().getEntities();
    //
    // for (int l = 0; l < items.size(); l++) {
    // ItemBean ib = (ItemBean) items.get(l);
    // ResponseSetBean rsb = getResponseSet(ib.getName());
    // if (rsb.getOptions() != null) {//((rsb.getOptions() != null) &&
    // (rsb.getResponseTypeId() != 1)
    // //&& (rsb.getResponseTypeId() != 2)) {//not text or text area
    //
    // //change the above so that we know there are numbers
    // //in the value list -- SPSS will not work properly if that
    // //is not the case
    // /*ddlFile.append("\tV" + iterator + "\n");
    // Iterator opIt3 = rsb.getOptions().iterator();
    // while (opIt3.hasNext()) {
    // ResponseOptionBean ro = new ResponseOptionBean();
    // ro = (ResponseOptionBean) opIt3.next();
    // ddlFile.append("\t" + ro.getValue() + " " + "\"" + ro.getText() + "\"");
    // if (opIt3.hasNext()) {
    // ddlFile.append("\n");
    // } else {
    // ddlFile.append(" /\n");
    // }
    // }*/
    // ddlFile.append(this.getSPSSResponseSet("V" + iterator,rsb));
    // //ddlFile.append("/");
    // }
    // iterator++;
    //
    // }
    // }
    // }
    // }
    // ddlFile.append(".\n\n");
    // //next add missing vlaues file and
    // //user-defined missing value codes
    // //are there never any missing values?
    // ddlFile.append("MISSING VALUES\n");
    // iterator = 3;
    // StringBuffer recode = new StringBuffer("");
    // for (int i = 0; i < defs.size(); i++) {
    // ExtractStudyEventDefinitionBean sed = (ExtractStudyEventDefinitionBean)
    // defs.get(i);
    // ArrayList crfVersions = sed.getCRFVersions().getEntities();
    //
    // for (int j = 0; j < crfVersions.size(); j++) {
    // ExtractCRFVersionBean crfVersion = (ExtractCRFVersionBean)
    // crfVersions.get(j);
    //
    // for (int k = 1; k <= getNumSamples(sed); k++) {
    // ArrayList items = crfVersion.getItems().getEntities();
    //
    // for (int l = 0; l < items.size(); l++) {
    // ItemBean ib = (ItemBean) items.get(l);
    // if (this.missingVals.containsKey(ib.getName())) {
    // String missingId = (String)this.missingVals.get(ib.getName());
    // ddlFile.append("V"+iterator+" ("+missingId+")\n");
    //
    // recode.append("V"+iterator+" ("+missingId+"=SYSMIS)/\n");
    // }
    // iterator++;
    // }
    // }
    // }
    // }
    // ddlFile.append(".\n\n");
    // ddlFile.append("RECODE\n");
    // ddlFile.append(recode.toString());
    // ddlFile.append(".\nEXECUTE\n");
    // files.add(ddlFile.toString());
    // logger.info("ending second file");
    // return files;
    // }
    //
    // public String getSPSSResponseSet(String varNumber, ResponseSetBean rsb) {
    // StringBuffer ddlFile = new StringBuffer("");
    // ddlFile.append("\t" + varNumber + "\n");
    // Iterator opIt3 = rsb.getOptions().iterator();
    // while (opIt3.hasNext()) {
    // ResponseOptionBean ro = new ResponseOptionBean();
    // ro = (ResponseOptionBean) opIt3.next();
    // if (ro.getValue().equals("text")) {
    // //?do nothing?
    // } else {
    // ddlFile.append("\t" + ro.getValue() + " " + "\"" + ro.getText() + "\"");
    // if (opIt3.hasNext()) {
    // ddlFile.append("\n");
    // } else {
    // ddlFile.append(" /\n");
    // }
    // }
    // }
    // return ddlFile.toString();
    // }
    //
    // public int getSPSSWhitespace(String checkName, int colLength) {
    // try {
    // int checkInt = ((Integer) SPSSColLengths.get(checkName)).intValue();
    // if (checkInt > colLength) {
    // //if ((checkInt - colLength) > 30) {
    // //return 30;
    // //} else {
    // return checkInt - colLength;
    // //}
    // } else {
    // //return zero, below
    // }
    // } catch (NullPointerException e) {
    // // TODO Auto-generated catch block
    // //e.printStackTrace();
    // //logger.info("found npe, generating whitespace for "+
    // //checkName+", proposed length "+colLength);
    // return 0;
    // }
    // return 0;
    // }
    //
    // // public String generateWhitespace(int howMuch) {
    // // String whitespace = "";
    // // if (howMuch > 0) {
    // // for (int i = 0; i < howMuch; i++) {
    // // whitespace.concat(" ");
    // // }
    // // }
    // // return whitespace;
    // // }
    //
    // public String generateWhitespace(String key, int howMuch) {
    // String whitespace = "";
    // Integer initial = (Integer)this.SPSSColLengths.get(key);
    // if (howMuch < initial.intValue()) {
    // for (int i = 0; i <= (initial.intValue() - howMuch); i++) {
    // whitespace += " ";
    // }
    // }
    // return whitespace;
    // }
    //
    // public String export(DatasetBean db, StudyBean currentStudy) {
    // String end = "";//ending character
    // String sep = "";//seperating character
    // if (format == SAS_FORMAT) {
    // end = "\n";
    // sep = "\t";
    // } else if (format == SPSS_FORMAT) {
    // end = "\n";
    // sep = "\t";
    // } else if (format == CSV_FORMAT) {
    // end = "\n";
    // sep = ",";
    // } else if (format == TXT_FORMAT) {
    // end = "\n";
    // sep = "\t";
    // }
    // //TODO work on this, just a marker
    // //the beginning, replicate this depending on status
    // StringBuffer sb = new StringBuffer("");
    // StringBuffer header = new StringBuffer("");
    // //first, export metadata
    // sb.append("Database Export Header Metadata" + end);
    //
    // sb.append("Dataset Name" + sep + datasetName + end);
    //
    // sb.append("Date" + sep);
    // java.text.SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
    // sb.append(sdf.format(new Date(System.currentTimeMillis())));
    // sb.append(end);
    //
    // sb.append("Protocol ID" + sep + parentProtocolId);
    // sb.append(end);
    //
    // sb.append("Study Name" + sep + parentStudyName);
    // sb.append(end);
    //
    // if (!siteName.equals("")) {
    // sb.append("Site Name" + sep + siteName);
    // }
    //
    // sb.append(end);
    // sb.append("Subjects " + sep);
    // sb.append(studySubjects.getEntities().size());
    // sb.append(end);
    // sb.append("Study Event Definitions " + sep);
    // sb.append(studyEventDefinitions.getEntities().size());
    // sb.append(end);
    //
    // Iterator it_later = studyEventDefinitions.getEntities().iterator();
    // Iterator it_header = studyEventDefinitions.getEntities().iterator();
    // int ordinal_seds = 1;
    // for (Iterator it = studyEventDefinitions.getEntities().iterator();
    // it.hasNext();) {
    // //second, iterate through seds
    // ExtractStudyEventDefinitionBean sed = (ExtractStudyEventDefinitionBean)
    // it.next();
    // String repeating = "";
    // if (sed.isRepeating()) {
    // repeating = " (Repeating) ";
    // }
    // //if repeating:
    // //change string to (Repeating)
    // sb.append("Study Event Definition " + ordinal_seds + " " + repeating +
    // sep);
    // sb.append(sed.getName() + end);
    // int ordinal_crfvs = 1;
    //
    // for (Iterator itcrf = sed.getCRFVersions().getEntities().iterator();
    // itcrf.hasNext();) {
    // ExtractCRFVersionBean ecvb = (ExtractCRFVersionBean) itcrf.next();
    //
    //
    // sb.append("CRF " + ordinal_crfvs + sep);
    // sb.append(ecvb.getCrfName() + sep);
    // sb.append(ExtractStudyEventDefinitionBean.getCode(ordinal_seds - 1)
    // + ExtractCRFVersionBean.getCode(ordinal_crfvs));
    // //TODO add code here
    // sb.append(end);
    // ordinal_crfvs++;
    // //third, iterate through crf versions
    //
    // }
    // ordinal_seds++;
    // }
    //
    // // create the header bar here:
    // //
    // //
    // header.append("Subject Event Item Values (Item-CRF-Ordinal)" + end);
    // header.append("Subject Unique ID" + sep + "Protocol-ID-Site ID" + sep);
    //
    // if (db.isShowSubjectDob()) {
    // if (currentStudy.isUsingDOB()) {
    // header.append("Date Of Birth" + sep);
    // } else {
    // header.append("Year Of Birth" + sep);
    // }
    // }
    // if (db.isShowSubjectGender()) {
    // header.append("Gender" + sep);
    // }
    //
    // ArrayList defs = studyEventDefinitions.getEntities();
    // ArrayList subjects = studySubjects.getEntities();
    // for (int i = 0; i < defs.size(); i++) {
    // ExtractStudyEventDefinitionBean sed = (ExtractStudyEventDefinitionBean)
    // defs.get(i);
    // ArrayList crfVersions = sed.getCRFVersions().getEntities();
    //
    // for (int k = 1; k <= getNumSamples(sed); k++) {
    // if (db.isShowEventLocation()) {
    // header.append(getEventHeader(sed, i, k, "Location"));
    // header.append(sep);
    // }
    // if (db.isShowEventStart()) {
    // header.append(getEventHeader(sed, i, k,"Event_Start_Date"));
    // header.append(sep);
    // }
    // if (db.isShowEventEnd()) {
    // header.append(getEventHeader(sed, i, k,"Event_End_Date"));
    // header.append(sep);
    // }
    //
    // }
    // }
    //
    // for (int i = 0; i < defs.size(); i++) {
    // ExtractStudyEventDefinitionBean sed = (ExtractStudyEventDefinitionBean)
    // defs.get(i);
    // ArrayList crfVersions = sed.getCRFVersions().getEntities();
    //
    // for (int j = 0; j < crfVersions.size(); j++) {
    // ExtractCRFVersionBean crfVersion = (ExtractCRFVersionBean)
    // crfVersions.get(j);
    //
    // for (int k = 1; k <= getNumSamples(sed); k++) {
    // ArrayList items = crfVersion.getItems().getEntities();
    //
    // for (int l = 0; l < items.size(); l++) {
    // ItemBean ib = (ItemBean) items.get(l);
    //
    // header.append(getColumnHeader(sed, i, j + 1, k, ib));
    // header.append(sep);
    // }
    // }
    // }
    // }
    //
    // sb.append(end);
    // sb.append(header);
    // sb.append(end);
    //
    // //header ends, value starts here
    // for (int h = 0; h < subjects.size(); h++) {
    // ExtractStudySubjectBean studySubj = (ExtractStudySubjectBean)
    // subjects.get(h);
    //
    // sb.append(studySubj.getName() + sep);
    // sb.append(studySubj.getStudyLabel() + sep);
    //
    // if (db.isShowSubjectDob()) {
    // if (currentStudy.isUsingDOB()) {
    // sb.append(sdf.format(studySubj.getDateOfBirth()) + sep);
    // } else {
    // sb.append(studySubj.getYearOfBirth() + sep);
    // }
    // }
    // if (db.isShowSubjectGender()) {
    // sb.append(studySubj.getGender() + sep);
    // }
    //
    // for (int i = 0; i < defs.size(); i++) {
    // ExtractStudyEventDefinitionBean sed = (ExtractStudyEventDefinitionBean)
    // defs.get(i);
    // ArrayList crfVersions = sed.getCRFVersions().getEntities();
    //
    // for (int k = 1; k <= getNumSamples(sed); k++) {
    // StudyEventBean seb = studySubj.getStudyEvent(sed.getId(), k);
    //
    // if (db.isShowEventLocation()) {
    // sb.append(seb.getLocation());
    // sb.append(sep);
    // }
    // if (db.isShowEventStart()) {
    // if (seb.getDateStarted() != null){
    // sb.append(sdf.format(seb.getDateStarted()));
    // }
    // sb.append(sep);
    // }
    // if (db.isShowEventEnd()) {
    // if (seb.getDateEnded() != null){
    // sb.append(sdf.format(seb.getDateEnded()));
    // } else {
    // sb.append("");
    // }
    // sb.append(sep);
    // }
    // }
    // }
    //
    // for (int i = 0; i < defs.size(); i++) {
    // ExtractStudyEventDefinitionBean sed = (ExtractStudyEventDefinitionBean)
    // defs.get(i);
    // ArrayList crfVersions = sed.getCRFVersions().getEntities();
    //
    // for (int j = 0; j < crfVersions.size(); j++) {
    // ExtractCRFVersionBean crfVersion = (ExtractCRFVersionBean)
    // crfVersions.get(j);
    //
    // for (int k = 1; k <= getNumSamples(sed); k++) {
    // ArrayList items = crfVersion.getItems().getEntities();
    //
    // for (int l = 0; l < items.size(); l++) {
    // ItemBean ib = (ItemBean) items.get(l);
    //
    // sb.append(studySubj.getValue(sed.getId(), k, crfVersion.getId(),
    // ib.getId()));
    // sb.append(sep);
    // }
    // }
    // }
    // }
    // sb.append(end);
    // }
    // return sb.toString();
    // }
    //
    // private String getColumnHeader(ExtractStudyEventDefinitionBean sed, int
    // sedNum,
    // int crfVersionNum, int sampleNum, ItemBean ib) {
    // String sampleNumLabel = sed.isRepeating() ? "_" + sampleNum : "";
    //
    // return ib.getName() + "_" +
    // ExtractStudyEventDefinitionBean.getCode(sedNum)
    // + ExtractCRFVersionBean.getCode(crfVersionNum) + sampleNumLabel;
    // }
    //
    // private String getEventHeader(ExtractStudyEventDefinitionBean sed, int
    // sedNum, int sampleNum, String eventAttribute) {
    // String sampleNumLabel = sed.isRepeating() ? "_" + sampleNum : "";
    //
    // return eventAttribute + "_" +
    // ExtractStudyEventDefinitionBean.getCode(sedNum)
    // + sampleNumLabel;
    // }
    //
    // /**
    // * @return Returns the datasetName.
    // */
    // public String getDatasetName() {
    // return datasetName;
    // }
    //
    // /**
    // * @param datasetName
    // * The datasetName to set.
    // */
    // public void setDatasetName(String datasetName) {
    // this.datasetName = datasetName;
    // }
    //
    // /**
    // * @return Returns the dateCreated.
    // */
    // public Date getDateCreated() {
    // return dateCreated;
    // }
    //
    // /**
    // * @param dateCreated
    // * The dateCreated to set.
    // */
    // public void setDateCreated(Date dateCreated) {
    // this.dateCreated = dateCreated;
    // }
    //
    // /**
    // * @return Returns the parentStudyName.
    // */
    // public String getParentStudyName() {
    // return parentStudyName;
    // }
    //
    // /**
    // * @param parentStudyName
    // * The parentStudyName to set.
    // */
    // public void setParentStudyName(String parentStudyName) {
    // this.parentStudyName = parentStudyName;
    // }
    //
    // /**
    // * @return Returns the parentProtocolId.
    // */
    // public String getParentProtocolId() {
    // return parentProtocolId;
    // }
    //
    // /**
    // * @param parentProtocolId
    // * The parentProtocolId to set.
    // */
    // public void setParentProtocolId(String parentProtocolId) {
    // this.parentProtocolId = parentProtocolId;
    // }
    //
    // /**
    // * @return Returns the siteName.
    // */
    // public String getSiteName() {
    // return siteName;
    // }
    //
    // /**
    // * @param siteName
    // * The siteName to set.
    // */
    // public void setSiteName(String siteName) {
    // this.siteName = siteName;
    // }
    //
    // /**
    // * @return Returns the studyEventDefinitions.
    // */
    // public OrderedEntityBeansSet getStudyEventDefinitions() {
    // return studyEventDefinitions;
    // }
    //
    // /**
    // * @param studyEventDefinitions
    // * The studyEventDefinitions to set.
    // */
    // public void setStudyEventDefinitions(OrderedEntityBeansSet
    // studyEventDefinitions) {
    // this.studyEventDefinitions = studyEventDefinitions;
    // }
    //
    // /**
    // * @return Returns the studySubjects.
    // */
    // public OrderedEntityBeansSet getStudySubjects() {
    // return studySubjects;
    // }
    //
    // /**
    // * @param studySubjects
    // * The studySubjects to set.
    // */
    // public void setStudySubjects(OrderedEntityBeansSet studySubjects) {
    // this.studySubjects = studySubjects;
    // }
    //
    // /**
    // * @return Returns the studySubjectNum.
    // */
    // public int getStudySubjectNum() {
    // return studySubjectNum;
    // }
    //
    // /**
    // * @param studySubjectNum
    // * The studySubjectNum to set.
    // */
    // public void setStudySubjectNum(int studySubjectNum) {
    // this.studySubjectNum = studySubjectNum;
    // }
    //
    // /**
    // * @return Returns the itemNames.
    // */
    // public ArrayList getItemNames() {
    // return itemNames;
    // }
    //
    // /**
    // * @param itemNames
    // * The itemNames to set.
    // */
    // public void setItemNames(ArrayList itemNames) {
    // this.itemNames = itemNames;
    // }
    //
    // /**
    // * Generates item dataset header names for html view
    // *
    // */
    // public void genItemNames() {
    // ArrayList defs = studyEventDefinitions.getEntities();
    // ArrayList subjects = studySubjects.getEntities();
    //
    // for (int i = 0; i < defs.size(); i++) {
    // ExtractStudyEventDefinitionBean sed = (ExtractStudyEventDefinitionBean)
    // defs.get(i);
    // ArrayList crfVersions = sed.getCRFVersions().getEntities();
    //
    // for (int j = 0; j < crfVersions.size(); j++) {
    // ExtractCRFVersionBean crfVersion = (ExtractCRFVersionBean)
    // crfVersions.get(j);
    //
    // for (int k = 1; k <= getNumSamples(sed); k++) {
    // ArrayList items = crfVersion.getItems().getEntities();
    //
    // for (int l = 0; l < items.size(); l++) {
    // ItemBean ib = (ItemBean) items.get(l);
    //
    // DisplayItemHeaderBean dihb = new DisplayItemHeaderBean();
    // dihb.setItemHeaderName(getColumnHeader(sed, i, j + 1, k, ib));
    // dihb.setItem(ib);
    // itemNames.add(dihb);
    //
    // }
    // }
    // }
    // }
    //
    // }
    //
    // /**
    // * Generates values for each row of item data for html view
    // *
    // */
    // public void genItemValues(StudyBean currentStudy,DatasetBean db) {
    // ArrayList subjects = studySubjects.getEntities();
    // ArrayList defs = studyEventDefinitions.getEntities();
    // for (int h = 0; h < subjects.size(); h++) {
    // DisplayItemDataBean didb = new DisplayItemDataBean();
    // ExtractStudySubjectBean studySubj = (ExtractStudySubjectBean)
    // subjects.get(h);
    //
    // didb.setSubjectName(studySubj.getName());
    // didb.setStudyLabel(studySubj.getStudyLabel());
    // Date dob = studySubj.getDateOfBirth();
    // SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
    // if (currentStudy.isUsingDOB()){
    // didb.setSubjectDob(sdf.format(studySubj.getDateOfBirth()));
    // } else {
    // didb.setSubjectDob(studySubj.getYearOfBirth());
    // }
    // didb.setSubjectGender(studySubj.getGender());
    //
    // for (int i = 0; i < defs.size(); i++) {
    // ExtractStudyEventDefinitionBean sed = (ExtractStudyEventDefinitionBean)
    // defs.get(i);
    //
    // for (int k = 1; k <= getNumSamples(sed); k++) {
    // StudyEventBean seb = studySubj.getStudyEvent(sed.getId(), k);
    //
    // if (db.isShowEventLocation()) {
    // didb.getEventValues().add(seb.getLocation());
    // }
    // if (db.isShowEventStart()) {
    // if (seb.getDateStarted() != null){
    // didb.getEventValues().add(sdf.format(seb.getDateStarted()));
    // } else {
    // didb.getEventValues().add("");
    // }
    //
    // }
    // if (db.isShowEventEnd()) {
    // if (seb.getDateEnded() != null){
    // didb.getEventValues().add(sdf.format(seb.getDateEnded()));
    // } else {
    // didb.getEventValues().add("");
    // }
    //
    // }
    // }
    // }
    // for (int i = 0; i < defs.size(); i++) {
    // ExtractStudyEventDefinitionBean sed = (ExtractStudyEventDefinitionBean)
    // defs.get(i);
    // ArrayList crfVersions = sed.getCRFVersions().getEntities();
    //
    // for (int j = 0; j < crfVersions.size(); j++) {
    // ExtractCRFVersionBean crfVersion = (ExtractCRFVersionBean)
    // crfVersions.get(j);
    //
    // for (int k = 1; k <= getNumSamples(sed); k++) {
    // ArrayList items = crfVersion.getItems().getEntities();
    //
    // for (int l = 0; l < items.size(); l++) {
    // ItemBean ib = (ItemBean) items.get(l);
    //
    // didb.getItemValues().add(
    // studySubj.getValue(sed.getId(), k, crfVersion.getId(), ib.getId()));
    // }
    // }
    // }
    //
    // }
    // itemValues.add(didb);
    // }
    //
    // }
    //
    //
    //
    // /**
    // * Generates event dataset header names
    // *
    // */
    // public void genEventNames(DatasetBean db) {
    // ArrayList defs = studyEventDefinitions.getEntities();
    // ArrayList subjects = studySubjects.getEntities();
    //
    // for (int i = 0; i < defs.size(); i++) {
    // ExtractStudyEventDefinitionBean sed = (ExtractStudyEventDefinitionBean)
    // defs.get(i);
    // for (int k = 1; k <= getNumSamples(sed); k++) {
    // if (db.isShowEventLocation()) {
    // eventNames.add(getEventHeader(sed, i, k, "Location"));
    // }
    // if (db.isShowEventStart()) {
    // eventNames.add(getEventHeader(sed, i, k,"Event_Start_Date"));
    // }
    // if (db.isShowEventEnd()) {
    // eventNames.add(getEventHeader(sed, i, k,"Event_End_Date"));
    // }
    //
    // }
    // }
    //
    //
    // }
    //
    //
    // /**
    // * @return Returns the itemValues.
    // */
    // public ArrayList getItemValues() {
    // return itemValues;
    // }
    //
    // /**
    // * @param itemValues
    // * The itemValues to set.
    // */
    // public void setItemValues(ArrayList itemValues) {
    // this.itemValues = itemValues;
    // }
    //
    // /*
    // *
    // * @author thickerson
    // *
    // *
    // */
    // public void updateSPSSColLengths(String name, int length) {
    // //int keyNum = this.SPSSColLengths.size();
    // //String key = "V"+(keyNum++);//V1, V2, V3, etc.
    // //if (this.SPSSColLengths.containsKey(name)) {
    // int compareMe = 0;
    // if (SPSSColLengths.containsKey(name)) {
    // compareMe = ((Integer) this.SPSSColLengths.get(name)).intValue();
    // }
    // if (length >= compareMe) {
    // Integer putMeIn = new Integer(length);
    // this.SPSSColLengths.put(name, putMeIn);
    // logger.info("just updated "+name+" with a new value: "+
    // putMeIn);
    // } else {
    // this.SPSSColLengths.put(name, new Integer(compareMe));
    // }
    // //logger.info("*** entered a length for "+name);
    // //}
    // }
    //
    // /**
    // * @return Returns the responseSet.
    // */
    // public HashMap getResponseSets() {
    // return responseSets;
    // }
    //
    // /**
    // * @param responseSet
    // * The responseSet to set.
    // */
    // public void setResponseSets(HashMap responseSets) {
    // this.responseSets = responseSets;
    // }
    //
    // public void addResponseSet(String name, ResponseSetBean rsb) {
    // this.responseSets.put(name, rsb);
    // }
    //
    // public ResponseSetBean getResponseSet(String name) {
    // if (this.responseSets.containsKey(name)) {
    // return (ResponseSetBean) this.responseSets.get(name);
    // } else {
    // ResponseSetBean fakeBean = new ResponseSetBean();
    // fakeBean.setResponseTypeId(1);
    // return fakeBean;
    // }
    // }
    // /**
    // * @return Returns the eventNames.
    // */
    // public ArrayList getEventNames() {
    // return eventNames;
    // }
    // /**
    // * @param eventNames The eventNames to set.
    // */
    // public void setEventNames(ArrayList eventNames) {
    // this.eventNames = eventNames;
    // }
}
