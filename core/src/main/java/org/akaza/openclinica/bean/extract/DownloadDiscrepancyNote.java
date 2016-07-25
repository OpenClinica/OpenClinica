package org.akaza.openclinica.bean.extract;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.service.DiscrepancyNoteThread;
import org.akaza.openclinica.service.DiscrepancyNoteUtil;
import org.apache.commons.lang.StringEscapeUtils;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Cell;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.PdfWriter;

/**
 *  This class converts or serializes DiscrepancyNoteBeans to Strings or iText-related
 * classes so that they can be compiled into a file and downloaded to the user. This is a
 * convenience class with a number of different methods for serializing beans to Strings.
 * @see org.akaza.openclinica.control.extract.DiscrepancyNoteOutputServlet
 * @author Bruce W. Perry
 *
 */
public class DownloadDiscrepancyNote implements DownLoadBean{
    public static String CSV ="text/plain; charset=UTF-8";
    public static String PDF = "application/pdf";
    public static String COMMA = ",";
    public static Map<Integer,String> RESOLUTION_STATUS_MAP = new HashMap<Integer,String> ();
    static{
        RESOLUTION_STATUS_MAP.put(1,"New");
        RESOLUTION_STATUS_MAP.put(2,"Updated");
        RESOLUTION_STATUS_MAP.put(3,"Resolution Proposed");
        RESOLUTION_STATUS_MAP.put(4,"Closed");
        RESOLUTION_STATUS_MAP.put(5,"Not Applicable");
    }

    //Does the user want the first line of the CSV to be column headers
    private final boolean firstColumnHeaderLine;
    //A list of DiscrepancyNoteBeans to be downloaded together
    private final List<DiscrepancyNoteBean> discrepancyBeanList =
      new ArrayList<DiscrepancyNoteBean>();

    public DownloadDiscrepancyNote() {
        this.firstColumnHeaderLine = false;
    }

    public DownloadDiscrepancyNote(boolean firstColumnHeaderLine) {
        this.firstColumnHeaderLine = firstColumnHeaderLine;
    }

    public void downLoad(EntityBean bean,
                         String format,
                         OutputStream stream) {
        if(bean == null || stream == null ||
          !( bean instanceof org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean)){
            throw new IllegalStateException(
              "An invalid parameter was passed to the DownloadDiscrepancyNote.downLoad method.");
        }
        DiscrepancyNoteBean discNBean = (DiscrepancyNoteBean) bean;
        //This must be a ServletOutputStream for our purposes
        ServletOutputStream servletStream = (ServletOutputStream) stream;

        try{
            if(CSV.equalsIgnoreCase(format))  {
                servletStream.print(serializeToString(discNBean, false, 0));
            } else {

                //Create PDF version
                serializeToPDF(discNBean,servletStream);

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if(servletStream != null){
                try {
                    servletStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void downLoad(List<EntityBean> listOfBeans, String format,
                         OutputStream stream) {

        //The List must be of DiscrepancyNoteBeans
        if (listOfBeans == null ) {
            return;
        }
        StringBuilder allContent = new StringBuilder();
        String singleBeanContent="";

        for(EntityBean discNoteBean : listOfBeans){
            if(! (discNoteBean instanceof DiscrepancyNoteBean)) return;

            DiscrepancyNoteBean discNBean = (DiscrepancyNoteBean) discNoteBean;
            singleBeanContent = serializeToString(discNBean, false, 0);
            allContent.append(singleBeanContent);
            allContent.append("\n");

        }

        //This must be a ServletOutputStream for our purposes
        ServletOutputStream servletStream = (ServletOutputStream) stream;

        try{
            if(CSV.equalsIgnoreCase(format))  {
                servletStream.print(allContent.toString());
            } else {

                //Create PDF version
                serializeListToPDF(allContent.toString(),servletStream);

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if(servletStream != null){
                try {
                    servletStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public int getContentLength(EntityBean bean, String format) {
        return serializeToString(bean, false, 0).getBytes().length;
    }

    public int getListContentLength(List<DiscrepancyNoteBean> beans, String format) {
        int totalLength = 0;
        int count = 0;
        for(DiscrepancyNoteBean bean : beans) {
            ++count;
            //Only count the byte length of a CSV header row for the first DNote
            totalLength += serializeToString(bean, (count == 1), 0).getBytes().length;
            totalLength += "\n".getBytes().length;

        }
        return totalLength;
    }

    public int getThreadListContentLength(List<DiscrepancyNoteThread> threadBeans) {
        int totalLength = 0;
        int count = 0;
        int threadCount = 1;
        for(DiscrepancyNoteThread discrepancyNoteThread : threadBeans)  {
           for(DiscrepancyNoteBean discNoteBean : discrepancyNoteThread.getLinkedNoteList()) {
                //DiscrepancyNoteBean discNoteBean = discrepancyNoteThread.getLinkedNoteList().getFirst();
                ++count;
                //Only count the byte length of a CSV header row for the first DNote; we're only
                //using response.setContentlength for CSV format, because apparently it is not
                //necessary for PDF
                totalLength += serializeToString(discNoteBean, (count == 1), 0).getBytes().length;
                //each DN bean with have a column indicating the thread number for the
                //note
                totalLength += "\n".getBytes().length;

            }
            totalLength += ("Thread number: "+threadCount).getBytes().length;
            //increment threadCounter
            threadCount++;
        }
        return totalLength;
    }



    public String serializeToString(EntityBean bean, boolean includeHeaderRow,
                                    int threadNumber){
        DiscrepancyNoteBean discNoteBean = (DiscrepancyNoteBean) bean;
        StringBuilder writer  = new StringBuilder("");
        //If includeHeaderRow = true, the first row of the output consists of header names, only
        //for CSV format
        if(includeHeaderRow) {
            writer.append("Study Subject ID");
            writer.append(",");
            writer.append("Subject Status");
            writer.append(",");
            writer.append("Study/Site OID");
            writer.append(",");
            //we're adding a thread number row
            writer.append("Thread ID");
            writer.append(",");

            writer.append("Note ID");
            writer.append(",");

            writer.append("Parent Note ID");
            writer.append(",");

            writer.append("Date Created");
            writer.append(",");
            writer.append("Date Update");
            writer.append(",");
            writer.append("Days Open");
            writer.append(",");
            writer.append("Days Since Updated");
            writer.append(",");


            if(discNoteBean.getDisType() != null)  {
                writer.append("Discrepancy Type");
                writer.append(",");
            }
            writer.append("Resolution Status");
            writer.append(",");
            writer.append("Event Name");
            writer.append(",");
            writer.append("Event Occurrence");
            writer.append(",");
            writer.append("CRF Name");
            writer.append(",");
            writer.append("CRF Status");
            writer.append(",");
            writer.append("Group Label");
            writer.append(",");
            writer.append("Group Ordinal");
            writer.append(",");
            writer.append("Entity name");
            writer.append(",");
            writer.append("Entity value");
            writer.append(",");
            writer.append("Description");
            writer.append(",");
            writer.append("Detailed Notes");
            writer.append(",");
            writer.append("Assigned User");
            writer.append(",");
            writer.append("Study Id");

            writer.append("\n");
        }

        //Fields with embedded commas must be
        // delimited with double-quote characters.
        writer.append(escapeQuotesInCSV(discNoteBean.getStudySub().getLabel()));
        writer.append(",");

        writer.append(escapeQuotesInCSV(discNoteBean.getStudySub().getStatus().getName()));
        writer.append(",");

        writer.append(escapeQuotesInCSV(discNoteBean.getStudy().getOid()));
        writer.append(",");

        writer.append(escapeQuotesInCSV(threadNumber+""));
        writer.append(",");

        writer.append(escapeQuotesInCSV(discNoteBean.getId()+""));
        writer.append(",");

        writer.append(discNoteBean.getParentDnId()>0?discNoteBean.getParentDnId():"");
        writer.append(",");

        writer.append(escapeQuotesInCSV(discNoteBean.getCreatedDateString()+""));
        writer.append(",");

        writer.append(escapeQuotesInCSV(discNoteBean.getUpdatedDateString()+""));
        writer.append(",");

        if (discNoteBean.getParentDnId() == 0){
            writer.append(escapeQuotesInCSV(discNoteBean.getAge()+""));
            writer.append(",");

            String daysSinceUpdated = escapeQuotesInCSV(discNoteBean.getDays()+"");
            writer.append(daysSinceUpdated.equals("0") ? "" : daysSinceUpdated);
            writer.append(",");
        } else {
            writer.append(",");
            writer.append(",");
        }

        if (discNoteBean.getDisType() != null)  {
            writer.append(escapeQuotesInCSV(discNoteBean.getDisType().getName()));
            writer.append(",");
        }

        writer.append(escapeQuotesInCSV(RESOLUTION_STATUS_MAP.get(discNoteBean.getResolutionStatusId())+""));
        writer.append(",");

        writer.append(escapeQuotesInCSV(discNoteBean.getEventName()));
        writer.append(",");

        String eventOccurrence = null != discNoteBean.getStudyEventDefinitionBean()
                && discNoteBean.getStudyEventDefinitionBean().isRepeating()
                ? String.valueOf(discNoteBean.getEvent().getSampleOrdinal()) : "";
        writer.append(escapeQuotesInCSV(eventOccurrence));
        writer.append(",");

        writer.append(escapeQuotesInCSV(discNoteBean.getCrfName()));
        writer.append(",");

        writer.append(escapeQuotesInCSV(discNoteBean.getCrfStatus()));
        writer.append(",");

        String itemGroupName = discNoteBean.getItemGroupName() == null
                ? "" : String.valueOf(discNoteBean.getItemGroupName());
        writer.append(escapeQuotesInCSV(itemGroupName));
        writer.append(",");

        String itemDataOccurence = discNoteBean.getItemDataOrdinal() == null
                ? "" : String.valueOf(discNoteBean.getItemDataOrdinal());
        writer.append(escapeQuotesInCSV(itemDataOccurence));
        writer.append(",");

        writer.append(escapeQuotesInCSV(discNoteBean.getEntityName()));
        writer.append(",");

        writer.append(escapeQuotesInCSV(discNoteBean.getEntityValue()));
        writer.append(",");

        writer.append(escapeQuotesInCSV(discNoteBean.getDescription()+""));
        writer.append(",");

        writer.append(escapeQuotesInCSV(discNoteBean.getDetailedNotes()+""));
        writer.append(",");

        writer.append(escapeQuotesInCSV(discNoteBean.getAssignedUser().getName()));
        writer.append(",");

        writer.append(escapeQuotesInCSV(discNoteBean.getStudyId()+""));


        writer.append("\n");
        return writer.toString();


    }

    private void serializeToPDF(EntityBean bean, OutputStream stream) {

        ServletOutputStream servletStream = (ServletOutputStream) stream;
        DiscrepancyNoteBean discNBean = (DiscrepancyNoteBean) bean;
        StringBuilder writer  = new StringBuilder();
        writer.append(serializeToString(discNBean, false, 0));

        Document pdfDoc = new Document();

        try {
            PdfWriter.getInstance(pdfDoc,
              servletStream);
            pdfDoc.open();
            pdfDoc.add(new Paragraph(writer.toString()));
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        pdfDoc.close();

    }

    public void serializeListToPDF(String content, OutputStream stream) {

        ServletOutputStream servletStream = (ServletOutputStream) stream;

        Document pdfDoc = new Document();

        try {
            PdfWriter.getInstance(pdfDoc,
              servletStream);
            pdfDoc.open();
            pdfDoc.add(new Paragraph(content));
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        pdfDoc.close();

    }

    public void serializeListToPDF(List<DiscrepancyNoteBean> listOfBeans,
                                   OutputStream stream, String studyIdentifier) {

        ServletOutputStream servletStream = (ServletOutputStream) stream;

        Document pdfDoc = new Document();

        try {
            PdfWriter.getInstance(pdfDoc,
              servletStream);
            pdfDoc.open();
            //Create header for the study identifier or name
            if(studyIdentifier != null)  {
                HeaderFooter header = new HeaderFooter(
                  new Phrase("Study Identifier: "+studyIdentifier+" pg."),true);
                header.setAlignment(Element.ALIGN_CENTER);
                Paragraph para = new Paragraph("Study Identifier: "+studyIdentifier,
                  new Font(Font.HELVETICA, 18, Font.BOLD, new Color(0, 0, 0)));
                para.setAlignment(Element.ALIGN_CENTER);
                pdfDoc.setHeader(header);
                pdfDoc.add(para);
            }
            for(DiscrepancyNoteBean discNoteBean : listOfBeans){
                pdfDoc.add(this.createTableFromBean(discNoteBean));
                pdfDoc.add(new Paragraph("\n"));
            }
            //pdfDoc.add(new Paragraph(content));
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        pdfDoc.close();

    }

    public void serializeThreadsToPDF(List<DiscrepancyNoteThread> listOfThreads,
                                      OutputStream stream, String studyIdentifier) {

        ServletOutputStream servletStream = (ServletOutputStream) stream;

        Document pdfDoc = new Document();

        try {
            PdfWriter.getInstance(pdfDoc,
              servletStream);
            pdfDoc.open();
            //Create header for the study identifier or name
            if(studyIdentifier != null)  {
                HeaderFooter header = new HeaderFooter(
                  new Phrase("Study Identifier: "+studyIdentifier+" pg."),true);
                header.setAlignment(Element.ALIGN_CENTER);
                Paragraph para = new Paragraph("Study Identifier: "+studyIdentifier,
                  new Font(Font.HELVETICA, 18, Font.BOLD, new Color(0, 0, 0)));
                para.setAlignment(Element.ALIGN_CENTER);
                pdfDoc.setHeader(header);
                pdfDoc.add(para);
            }
            for(DiscrepancyNoteThread discNoteThread : listOfThreads){
                pdfDoc.add(this.createTableThreadHeader(discNoteThread));
                //Just the parent of the thread?  discNoteThread.getLinkedNoteList()
               for(DiscrepancyNoteBean discNoteBean : discNoteThread.getLinkedNoteList()){
                    //DiscrepancyNoteBean discNoteBean = discNoteThread.getLinkedNoteList().getFirst();
                   if(discNoteBean.getParentDnId()>0) {
                       pdfDoc.add(this.createTableFromBean(discNoteBean));
                       pdfDoc.add(new Paragraph("\n"));
                   }
               }
            }
            //pdfDoc.add(new Paragraph(content));
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        pdfDoc.close();

    }


    public void downLoadDiscBeans(List<DiscrepancyNoteBean> listOfBeans,
                                  String format,
                                  OutputStream stream, String studyIdentifier) {

        if (listOfBeans == null ) {
            return;
        }
        StringBuilder allContent = new StringBuilder();
        String singleBeanContent="";
        int counter=0;

        if(CSV.equalsIgnoreCase(format))  {

            for(DiscrepancyNoteBean discNoteBean : listOfBeans){
                ++counter;

                singleBeanContent = counter == 1 ? serializeToString(discNoteBean, true, 0) : serializeToString(discNoteBean, false, 0);
                allContent.append(singleBeanContent);
                allContent.append("\n");

            }
        }

        //This must be a ServletOutputStream for our purposes
        ServletOutputStream servletStream = (ServletOutputStream) stream;

        try{
            if(CSV.equalsIgnoreCase(format))  {
                servletStream.print(allContent.toString());
            } else {

                //Create PDF version
                this.serializeListToPDF(listOfBeans,servletStream, studyIdentifier);

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if(servletStream != null){
                try {
                    servletStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void downLoadThreadedDiscBeans(List<DiscrepancyNoteThread> listOfThreadedBeans,
                                          String format,
                                          HttpServletResponse response, String studyIdentifier) throws Exception {

        if (listOfThreadedBeans == null ) {
            return;
        }
        StringBuilder allContent = new StringBuilder();
        String singleBeanContent="";
        int counter=0;
        int threadCounter=0;

        if(CSV.equalsIgnoreCase(format))  {
            for(DiscrepancyNoteThread dnThread : listOfThreadedBeans ) {
                threadCounter++;
                for(DiscrepancyNoteBean discNoteBean : dnThread.getLinkedNoteList()){
                   //DiscrepancyNoteBean discNoteBean = dnThread.getLinkedNoteList().getFirst();
                    ++counter;

                    singleBeanContent = counter == 1 ? serializeToString(discNoteBean, true, threadCounter) : serializeToString(discNoteBean, false, threadCounter);
                    allContent.append(singleBeanContent);
               }
            }
        }

        //This must be a ServletOutputStream for our purposes
        ServletOutputStream servletStream = null;

        try{
            if(CSV.equalsIgnoreCase(format))  {
                String result  = StringEscapeUtils.unescapeJava(allContent.toString());
                response.getWriter().print(result);
                //servletStream.print(allContent.toString());
            } else {

                //Create PDF version
                //this.serializeListToPDF(listOfBeans,servletStream, studyIdentifier);
                servletStream = (ServletOutputStream) response.getOutputStream();
                this.serializeThreadsToPDF(listOfThreadedBeans,servletStream, studyIdentifier);


            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if(servletStream != null){
                try {
                    servletStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private String escapeQuotesInCSV(String csvValue){

        if(csvValue == null) return "";

        if(csvValue.contains("\u2018")){
            csvValue = csvValue.replaceAll("\u2018", "'");
        }

        if(csvValue.contains("\u201C")){
            csvValue = csvValue.replaceAll("\u201C", "\"");
        }

        if(csvValue.contains("\r\n")){
            csvValue = csvValue.replaceAll("\r\n", "");
        }

        if(csvValue.contains("\n")){
            csvValue = csvValue.replaceAll("\n", "");
        }

        //Escaping special characters in the String.
        csvValue = StringEscapeUtils.escapeJava(csvValue);


        if(csvValue.contains(",")){
            return new StringBuilder("\"").append(csvValue).append("\"").toString();
        }  else {
            return csvValue;
        }

    }

    private Table createTableThreadHeader(DiscrepancyNoteThread discNoteThread)
      throws BadElementException {
        Table table = new Table(2);
        table.setTableFitsPage(true);
        table.setCellsFitPage(true);
        table.setBorderWidth(1);
        table.setBorderColor(new java.awt.Color(0,0,0));
        table.setPadding(4);
        table.setSpacing(4);
        if(discNoteThread == null || discNoteThread.getLinkedNoteList().isEmpty()){
            return table;
        }

        //Get information for the header; the resolution status, however, has to be the latest
        //resolution status for the DN thread
        DiscrepancyNoteBean dnBean = discNoteThread.getLinkedNoteList().getFirst();
        DiscrepancyNoteUtil discUtil = new DiscrepancyNoteUtil();
        String latestResolutionStatus = discUtil.getResolutionStatusName(
                discNoteThread.getLinkedNoteList().getFirst().getResolutionStatusId());

        StringBuilder content = new StringBuilder("");
        if(dnBean != null){
            if(! "".equalsIgnoreCase(dnBean.getEntityName())) {
                content.append("Item field name/value: ");
                content.append(dnBean.getEntityName());
                if(! "".equalsIgnoreCase(dnBean.getEntityValue())) {
                    content.append(" = ");
                    content.append(dnBean.getEntityValue());
                }

            }
            Paragraph para = new Paragraph(content.toString(),
              new Font(Font.HELVETICA, 14, Font.BOLD, new Color(0, 0, 0)));
            Cell cell = new Cell(para);
            cell.setHeader(true);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setColspan(2);
            table.addCell(cell);
            table.endHeaders();

            //Add at least three more rows of data -- dnBean.getSubjectName()
            //row 1
            cell = createCell("Study Subject",dnBean.getSubjectName());
            table.addCell(cell);

            cell = createCell("Study Event",dnBean.getEventName());
             table.addCell(cell);

            //row 2
            cell = createCell("Study Event Date",dnBean.getEventStart()+"");
            table.addCell(cell);

            StringBuilder tmpStrBuilder = new StringBuilder("CRF: ");
            tmpStrBuilder.append(dnBean.getCrfName());
            tmpStrBuilder.append("\n");
            tmpStrBuilder.append("Status: ");
            tmpStrBuilder.append(dnBean.getCrfStatus());
            content.append(dnBean.getCrfName());

            cell = new Cell(new Paragraph(tmpStrBuilder.toString(), new Font(Font.HELVETICA, 14, Font.BOLD, new Color(0, 0, 0))));

            table.addCell(cell);

            //row 3
            cell = createCell("Type",discUtil.getResolutionStatusTypeName(
              dnBean.getDiscrepancyNoteTypeId()));

            table.addCell(cell);

            cell = createCell("Resolution Status",
              latestResolutionStatus);

            table.addCell(cell);

            cell = createCell("Number of notes",discNoteThread.getLinkedNoteList().size()+"");
            table.addCell(cell);

            cell = createCell("Discrepancy Note ID",dnBean.getId()+"");
            table.addCell(cell);

            cell = createCell("Days Open",dnBean.getAge()+"");
            table.addCell(cell);

            String daysSinceUpdated = escapeQuotesInCSV(dnBean.getDays()+"");
            cell = createCell("Days Since Updated", daysSinceUpdated.equals("0") ? "" : daysSinceUpdated +"");
            table.addCell(cell);
            
        }

        return table;
    }

    private Cell createCell(String propertyName, String propertyValue) throws BadElementException {

        StringBuilder content = new StringBuilder(propertyName+": ");
        content.append(propertyValue);
        Paragraph para = new Paragraph(content.toString(),
          new Font(Font.HELVETICA, 14, Font.BOLD, new Color(0, 0, 0)));
        return new Cell(para);
    }

    private Paragraph createThreadHeader(DiscrepancyNoteThread discNoteThread){
        String content ="";
        int size = discNoteThread.getLinkedNoteList().size();
        int counter=0;
        for(DiscrepancyNoteBean discBean : discNoteThread.getLinkedNoteList()){
            ++counter;
            content += discBean.getEntityName()+"; "+
              RESOLUTION_STATUS_MAP.get(discBean.getResolutionStatusId());
            if(size > 1 && counter != size) {
                content +=" --->";
            }

        }
        Paragraph para = new Paragraph(content,
          new Font(Font.HELVETICA, 16, Font.BOLD, new Color(0, 0, 0)));
        return para;
    }

    private Table createTableFromBean(DiscrepancyNoteBean discBean) throws
      BadElementException {

        Table table = new Table(2);
        table.setTableFitsPage(true);
        table.setCellsFitPage(true);
        table.setBorderWidth(1);
        table.setBorderColor(new java.awt.Color(0, 0, 0));
        table.setPadding(4);
        table.setSpacing(4);
        Cell cell = new Cell("Discrepancy note id: "+discBean.getId());
        cell.setHeader(true);
        cell.setColspan(2);
        table.addCell(cell);
        table.endHeaders();

        cell = new Cell("Subject name: "+discBean.getSubjectName());
        table.addCell(cell);
        cell = new Cell("CRF name: "+discBean.getCrfName());
        table.addCell(cell);
        cell = new Cell("Description: "+discBean.getDescription());
        table.addCell(cell);
        if(discBean.getDisType() != null)  {
            cell = new Cell("Discrepancy note type: "+discBean.getDisType().getName());
            table.addCell(cell);
        }
        cell = new Cell("Event name: "+discBean.getEventName());
        table.addCell(cell);
        cell = new Cell("Parent note ID: "+(discBean.getParentDnId()>0? discBean.getParentDnId():""));
        table.addCell(cell);
        cell = new Cell("Resolution status: "+
          new DiscrepancyNoteUtil().getResolutionStatusName(discBean.getResolutionStatusId()));
        table.addCell(cell);
        cell = new Cell("Detailed notes: "+discBean.getDetailedNotes());
        table.addCell(cell);
        cell = new Cell("Entity name: "+discBean.getEntityName());
        table.addCell(cell);
        cell = new Cell("Entity value: "+discBean.getEntityValue());
        table.addCell(cell);
        cell = new Cell("Date updated: "+discBean.getUpdatedDateString());
        table.addCell(cell);
        cell = new Cell("Study ID: "+discBean.getStudyId());
        table.addCell(cell);



        return table;

    }


}
