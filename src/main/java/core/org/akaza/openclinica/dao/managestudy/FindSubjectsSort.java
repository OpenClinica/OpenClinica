package core.org.akaza.openclinica.dao.managestudy;

import core.org.akaza.openclinica.dao.hibernate.ItemDao;
import core.org.akaza.openclinica.domain.datamap.Item;
import core.org.akaza.openclinica.domain.datamap.ItemData;
import core.org.akaza.openclinica.domain.datamap.ItemDataType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FindSubjectsSort implements CriteriaCommand {
    List<Sort> sorts = new ArrayList<Sort>();
    HashMap<String, String> columnMapping = new HashMap<String, String>();

    public FindSubjectsSort() {
        columnMapping.put("studySubject.label", "label");
        columnMapping.put("studySubject.status", "status_id");
        columnMapping.put("enrolledAt", "unique_identifier");
        columnMapping.put("studySubject.oid", "oc_oid");
        columnMapping.put("studySubject.secondaryLabel", "secondary_label");
        columnMapping.put("participate.status", "user_status_id");

    }

    public void addSort(String property, String order) {
        sorts.add(new Sort(property, order));
    }

    public List<Sort> getSorts() {
        return sorts;
    }

    public String execute(String criteria) {
        String theCriteria = "";
        for (Sort sort : sorts) {
            theCriteria += buildCriteria(criteria, sort.getProperty(), sort.getOrder());
        }

        return theCriteria;
    }

    private String buildCriteriaInitial(String criteria, String property, String order) {
        if (order.equals(Sort.ASC)) {
            criteria = criteria + " order by " + columnMapping.get(property) + " asc ";
        } else if (order.equals(Sort.DESC)) {
            criteria = criteria + " order by " + columnMapping.get(property) + " desc ";
        }
        return criteria;
    }

    private String buildCriteria(String criteria, String property, String order) {
        return customcolumnSorting(property,order);

    }

    private static class Sort {
        public final static String ASC = "asc";
        public final static String DESC = "desc";

        private final String property;
        private final String order;

        public Sort(String property, String order) {
            this.property = property;
            this.order = order;
        }

        public String getProperty() {
            return property;
        }

        public String getOrder() {
            return order;
        }
    }

    private String customcolumnSorting(String property, String order) {
        String partialScript = "";
        if (property.startsWith("SE_") && property.contains(".F_") && property.contains(".I_")) {
            String sedOid = property.split("\\.")[0];
            String formOid = property.split("\\.")[1];
            String itemOid = property.split("\\.")[2];
            String itemDataType = property.split("\\.")[3];

            partialScript = "(" +
                    "select jj.* ,'' val from (FILTERED_SQL)jj \n" +
                    "\n" +
                    " Except " +
                    " \n" +
                    " select jj.* ,'' val from  "
                    + subScript(sedOid, formOid, itemOid) +
                    " ) " +
                    "\n" +
                    " UNION " +
                    "\n" +
                    " select  jj.* ,id.value val from "
                    + subScript(sedOid, formOid, itemOid);


            partialScript = "select * from ( " + partialScript + " )kk " + sortByItemType(itemDataType);
            if (order.equals(Sort.ASC)) {
                partialScript = partialScript + "  asc NULLS LAST ";
            } else if (order.equals(Sort.DESC)) {
                partialScript = partialScript + " desc  NULLS LAST ";
            }
        } else {
            partialScript = "select jj.* ,'' val from (FILTERED_SQL)jj  ";

            if (property.equals("participate.status")) {
                partialScript = partialScript + "  order by   case " + columnMapping.get(property) +
                        "          when 1 then 1\n" +
                        "          when 3 then 2\n" +
                        "          when 2 then 3\n" +
                        "          when 4 then 4\n" +
                        "          \n" +
                        "          end \n" +
                        "          \n";
                if (order.equals(Sort.ASC))
                    partialScript = partialScript + "          asc Nulls LAST";
                else
                    partialScript = partialScript + "          desc Nulls LAST";

            } else {
                if (order.equals(Sort.ASC)) {
                    partialScript = partialScript + " order by " + columnMapping.get(property) + " asc ";
                } else if (order.equals(Sort.DESC)) {
                    partialScript = partialScript + " order by " + columnMapping.get(property) + " desc ";
                }
            }
        }
        return partialScript;

    }

    private String sortByItemType(String itemDataType) {
        String append = "";
        if (itemDataType.equalsIgnoreCase("Floating")) {
            append = " order by  cast(Nullif(val,'' )as double precision)  ";
        } else if (itemDataType.equalsIgnoreCase("Integer")) {
            append = " order by  cast(Nullif(val,'' )as integer)  ";
        } else if (itemDataType.equalsIgnoreCase("date")) {
            append = " order by  cast(Nullif(val,'' )as date)  ";
        } else{
            append = " order by nullif(val,'') ";
        }
        return append;
    }

    private String subScript(String sedOid, String formOid, String itemOid) {
        return
                "   item_data id JOIN event_crf ec ON id.event_crf_id=ec.event_crf_id\n" +
                        "                                          JOIN study_event se ON se.study_event_id=ec.study_event_id\n" +
                        "                                          JOIN crf_version cv ON cv.crf_version_id=ec.crf_version_id\n" +
                        "                                          JOIN crf c ON c.crf_id=cv.crf_id\n" +
                        "                                          JOIN item i ON i.item_id=id.item_id\n" +
                        "                                          JOIN study_event_definition sed ON sed.study_event_definition_id=se.study_event_definition_id\n" +
                        "                                          JOIN  (FILTERED_SQL) jj ON jj.study_subject_id=se.study_subject_id \n" +
                        "                                         \n" +
                        "                                          where\n" +
                        "                                          sed.oc_oid=\'" + sedOid + "\' AND\n" +
                        "                                          c.oc_oid=\'" + formOid + "\' AND\n" +
                        "                                          i.oc_oid=\'" + itemOid + "\' ";
    }



}