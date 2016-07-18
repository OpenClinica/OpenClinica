package org.akaza.openclinica.service.extract;

import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.extract.ExtractPropertyBean;

import java.io.File;
import java.text.SimpleDateFormat;

public class ExtractUtils {

    /**
         * Returns the datetime based on pattern :"yyyy-MM-dd-HHmmssSSS", typically for resolving file name
         * @param endFilePath
         * @param dsBean
         * @param sdfDir
         * @return
         */
        public  String resolveVars(String endFilePath, DatasetBean dsBean, SimpleDateFormat sdfDir, String filePath){
    
            if(endFilePath.contains("$exportFilePath")) {
                endFilePath =   endFilePath.replace("$exportFilePath", filePath+"datasets");// was + File.separator, tbh
            }
         
             if(endFilePath.contains("${exportFilePath}")) {
                endFilePath =   endFilePath.replace("${exportFilePath}", filePath+"datasets");// was + File.separator, tbh
            }
            if(endFilePath.contains("$datasetId")) {
                endFilePath = endFilePath.replace("$datasetId", dsBean.getId()+"");
            }
            if(endFilePath.contains("${datasetId}")) {
                endFilePath = endFilePath.replace("${datasetId}", dsBean.getId()+"");
             }
            if(endFilePath.contains("$datasetName")) {
                endFilePath = endFilePath.replace("$datasetName", dsBean.getName());
            }
            if(endFilePath.contains("${datasetName}"))
                 {
             endFilePath = endFilePath.replace("${datasetName}", dsBean.getName());
                 }
           
           if(endFilePath.contains("$dateTime")) {
             String simpleDatePattern = "yyyy-MM-dd-HHmmssSSS";
            sdfDir = new SimpleDateFormat(simpleDatePattern);
                endFilePath = endFilePath.replace("$dateTime",  sdfDir.format(new java.util.Date()));
            }
            if(endFilePath.contains("${dateTime}")){
             String simpleDatePattern = "yyyy-MM-dd-HHmmssSSS";
                sdfDir = new SimpleDateFormat(simpleDatePattern);
                endFilePath = endFilePath.replace("${dateTime}",  sdfDir.format(new java.util.Date()));
            }
            if(endFilePath.contains("$date")) {
                 String dateFilePattern = "yyyy-MM-dd";
                  sdfDir = new SimpleDateFormat(dateFilePattern);
                endFilePath = endFilePath.replace("$date",sdfDir.format(new java.util.Date()) );
            }
            if(endFilePath.contains("${date}"))
            {
                 String dateFilePattern = "yyyy-MM-dd";
                  sdfDir = new SimpleDateFormat(dateFilePattern);
             endFilePath = endFilePath.replace("${date}",sdfDir.format(new java.util.Date()) );
            }
            //TODO change to dateTime
          
        return endFilePath;
       }

    //TODO: ${linkURL} needs to be added
    /**
     *
     * for dateTimePattern, the directory structure is created. "yyyy" + File.separator + "MM" + File.separator + "dd" + File.separator,
     * to resolve location
     */
    public  String getEndFilePath(String endFilePath, DatasetBean dsBean, SimpleDateFormat sdfDir, String filePath){
        String simpleDatePattern =  "yyyy" + File.separator + "MM" + File.separator + "dd" + File.separator ;
        SimpleDateFormat sdpDir = new SimpleDateFormat(simpleDatePattern);
       
        String datePattern = "yyyy-MM-dd";
        SimpleDateFormat dateDir = new SimpleDateFormat(datePattern);
        
        if(endFilePath.contains("$exportFilePath")) {
            endFilePath =  endFilePath.replace("$exportFilePath", filePath+"datasets");// was + File.separator, tbh
        }
     
         if(endFilePath.contains("${exportFilePath}")) {
            endFilePath =  endFilePath.replace("${exportFilePath}", filePath+"datasets");// was + File.separator, tbh
        }
        if(endFilePath.contains("$datasetId")) {
           endFilePath = endFilePath.replace("$datasetId", dsBean.getId()+"");
        }
        if(endFilePath.contains("${datasetId}")) {
           endFilePath = endFilePath.replace("${datasetId}", dsBean.getId()+"");
         }
        if(endFilePath.contains("$datasetName")) {
           endFilePath = endFilePath.replace("$datasetName", dsBean.getName());
        }
        if(endFilePath.contains("${datasetName}"))
                {
            endFilePath = endFilePath.replace("${datasetName}", dsBean.getName());
                }
        //TODO change to dateTime
       
        if(endFilePath.contains("$dateTime")) {
           endFilePath = endFilePath.replace("$dateTime",  sdfDir.format(new java.util.Date()));
         }
         if(endFilePath.contains("${dateTime}")){
               endFilePath = endFilePath.replace("${dateTime}",  sdfDir.format(new java.util.Date()));
         }
        if(endFilePath.contains("$date")) {
           
           endFilePath = endFilePath.replace("$date",dateDir.format(new java.util.Date()) );
        }
        if(endFilePath.contains("${date}"))
        {
            endFilePath = endFilePath.replace("${date}",dateDir.format(new java.util.Date()) );
        }
       
       return endFilePath;
    }

    public  ExtractPropertyBean setAllProps(ExtractPropertyBean epBean,DatasetBean dsBean,SimpleDateFormat sdfDir, String filePath) {
    	epBean.setFiledescription(resolveVars(epBean.getFiledescription(), dsBean, sdfDir, filePath));
    	epBean.setLinkText(resolveVars(epBean.getLinkText(), dsBean, sdfDir, filePath));
    	epBean.setHelpText(resolveVars(epBean.getHelpText(), dsBean, sdfDir, filePath));
    	epBean.setFileLocation(resolveVars(epBean.getFileLocation(), dsBean, sdfDir, filePath));
    	epBean.setFailureMessage(resolveVars(epBean.getFailureMessage(), dsBean, sdfDir, filePath));
    	epBean.setSuccessMessage(resolveVars(epBean.getSuccessMessage(), dsBean, sdfDir, filePath));
    	epBean.setZipName(resolveVars(epBean.getZipName(), dsBean, sdfDir, filePath));
    	epBean.setDatasetName(dsBean.getName());//JN:Adding this line to reflect the dataset name in the scheduled jobs list.
    	return epBean;
    }

}
