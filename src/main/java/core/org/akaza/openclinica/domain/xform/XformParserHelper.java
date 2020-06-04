package core.org.akaza.openclinica.domain.xform;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import core.org.akaza.openclinica.bean.core.Utils;
import core.org.akaza.openclinica.dao.hibernate.ItemGroupMetadataDao;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.*;
import core.org.akaza.openclinica.domain.xform.dto.*;
import org.akaza.openclinica.controller.dto.SdvItemDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XformParserHelper {
    public static final String ENKETO_ORDINAL = "enk:ordinal";
    public static final String ENKETO_LAST_USED_ORDINAL = "enk:last-used-ordinal";
    public static final String FS_QUERY_ATTRIBUTE = "oc:queryParent";
    public static final String JR_TEMPLATE = "jr:template";
    public static final String QUERY_SUFFIX = "form-queries.xml";
    public static final String PARTICIPATE_SUFFIX = "form-participate.xml";
    public static final String NO_SUFFIX = "form.xml";
    public static final String QUERY_FLAVOR = "-query";
    public static final String PARTICIPATE_FLAVOR = "-participate";
    public static final String SINGLE_ITEM_FLAVOR = "-single_item";
    public static final String NO_FLAVOR = "";
    public static final String SEPARATOR = "/";

    @Autowired
    StudyDao studyDao;

    @Autowired
    XformParser xformParser;

    @Autowired
    private ItemGroupMetadataDao itemGroupMetadataDao;

    public List<String> instanceItemPaths(Node outerNode, List<String> list, String path, Errors errors) {
        int outerNodeLength = outerNode.getChildNodes().getLength();
        if (outerNodeLength == 0)
            list.add(path);
        for (int b = 0; b < outerNodeLength; b++) {
            Node node = outerNode.getChildNodes().item(b);
            if (node instanceof Element && !node.getNodeName().equals("formhub") && !node.getNodeName().equals("meta")) {
                if (node.hasChildNodes()
                        && (node.getFirstChild() instanceof Element || node.getFirstChild().getNextSibling() instanceof Element)){
                    list = instanceItemPaths(node, list, path + "/" + node.getNodeName(), errors);
                } else {
                    list.add(path + "/" + node.getNodeName());
                }
            }
        }
        return list;
    }

    public Set<Node> instanceItemNodes(Node outerNode, Set<Node> set) {
        int outerNodeLength = outerNode.getChildNodes().getLength();
        if (outerNodeLength == 0)
            set.add(outerNode);
        for (int b = 0; b < outerNodeLength; b++) {
            Node node = outerNode.getChildNodes().item(b);
            if (node instanceof Element && !node.getNodeName().equals("formhub") && !node.getNodeName().equals("meta")) {
                if (node.hasChildNodes()
                       && (node.getFirstChild() instanceof Element || node.getFirstChild().getNextSibling() instanceof Element)) {
                    set = instanceItemNodes(node, set);
                } else {
                    set.add(node);
                }
            }
        }
        return set;
    }

    public Set<Node> instanceEnketoAttr(Node outerNode, Set<Node> set) {
        int outerNodeLength = outerNode.getChildNodes().getLength();
        if (outerNodeLength == 0)
            set.add(outerNode);
        for (int b = 0; b < outerNodeLength; b++) {
            Node node = outerNode.getChildNodes().item(b);
            if (node instanceof Element && !node.getNodeName().equals("formhub") && !node.getNodeName().equals("meta")) {
                if (node.getAttributes() != null && node.getAttributes().getNamedItem(ENKETO_ORDINAL) != null) {
                    set.add(node);
                } else {
                    set = instanceEnketoAttr(node, set);
                }
            }
        }
        return set;
    }


    public String getXformOutput(String studyOID, int studyFilePath, String crfOID, String formLayoutOID, String flavor) throws IOException {
        String xformOutput = "";
        String directoryPath = Utils.getFilePath() + Utils.getCrfMediaPath(studyOID, studyFilePath, crfOID, formLayoutOID);
        File dir = new File(directoryPath);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if ((flavor.equals(QUERY_FLAVOR) && child.getName().endsWith(QUERY_SUFFIX))
                        || (flavor.equals(PARTICIPATE_FLAVOR) && child.getName().endsWith(PARTICIPATE_SUFFIX))
                        || (flavor.equals(NO_FLAVOR) && child.getName().endsWith(NO_SUFFIX))) {
                    xformOutput = new String(Files.readAllBytes(Paths.get(child.getPath())));
                    break;
                }
            }
        }
        return xformOutput;
    }

    public Html getHtml(FormLayout formLayout, String flavor, String studyOid ) throws Exception {
        String xformOutput = "";
        Study study =studyDao.findByOcOID(studyOid);
        Study parentStudy= study.getStudy()!=null?study.getStudy():study;
        int studyFilePath = parentStudy.getFilePath();
        CrfBean crf = formLayout.getCrf();

        do {
            xformOutput = getXformOutput(parentStudy.getOc_oid(), studyFilePath, crf.getOcOid(), formLayout.getOcOid(), flavor);
            studyFilePath--;
        } while (xformOutput.equals("") && studyFilePath > 0);
        Html html = xformParser.unMarshall(xformOutput);

        return html;
    }

    public List<Bind> getBinds(FormLayout formLayout, String flavor, String studyOid) throws Exception {

        Html html = getHtml(formLayout, flavor, studyOid);
        Body body = html.getBody();
        Head head = html.getHead();
        Model model = head.getModel();

        List<Bind> binds = model.getBind();
        return binds;
    }

    public List<ItemData> getItemDatasInXformOrder(List<ItemData> itemDataList, FormLayout formLayout, String studyOID){
        List<ItemData> xformOrderItemDataList = new ArrayList<>();
        try {
            List<Bind> binds = getBinds(formLayout, "", studyOID);
            binds.forEach(bind ->  {if(bind.getItemGroup() == null) bind.setItemGroup(""); });
            Map<String, List<Bind>> bindGroupMap = binds.stream().collect(Collectors.groupingBy(Bind::getItemGroup));
            Map<String, ItemData> itemLabelAndGroupNameMap = new HashMap<>();
            for(ItemData itemData : itemDataList){
                String key = itemLabelAndGroupNameMapKeyGenerator(itemData);
                itemLabelAndGroupNameMap.put(key, itemData);
            }
            Set<String> repeatingItemGroupsSet = itemLabelAndGroupNameMap.keySet().stream().filter(key -> key.split(SEPARATOR).length > 2)
                    .map(key -> key.split(SEPARATOR)[1]).collect(Collectors.toCollection(HashSet::new));
            for(int i = 0; i < binds.size(); i++){
                String key = bindItemKeyGenerator(binds.get(i));
                String itemGroupName = binds.get(i).getItemGroup();
                if(itemLabelAndGroupNameMap.get(key) != null){
                    xformOrderItemDataList.add(itemLabelAndGroupNameMap.get(key));
                    itemLabelAndGroupNameMap.remove(key);
                }
                else if(repeatingItemGroupsSet.contains(itemGroupName)){
                    //repeatingGroup
                    List<Bind> repeatingGroupBindList = bindGroupMap.get(itemGroupName);
                    for(int j =1; j<= itemLabelAndGroupNameMap.size();j++){
                        key = j+SEPARATOR;
                        boolean itemOrdinalFound = false;
                        for(Bind bind : repeatingGroupBindList){
                            String repeatingGroupKey = key + bindItemKeyGenerator(bind);
                            if(itemLabelAndGroupNameMap.get(repeatingGroupKey) != null)
                            {
                                itemOrdinalFound = true;
                                xformOrderItemDataList.add(itemLabelAndGroupNameMap.get(repeatingGroupKey));
                                itemLabelAndGroupNameMap.remove(repeatingGroupKey);
                            }
                        }
                        if(!itemOrdinalFound)
                            break;
                    }
                    repeatingItemGroupsSet.remove(itemGroupName);
                }
            }

        }catch (Exception e){
            xformOrderItemDataList = itemDataList;
        }
        return xformOrderItemDataList;
    }


    private String bindItemKeyGenerator(Bind bind){
        String nodeSet = bind.getNodeSet();
        int sepPos = nodeSet.lastIndexOf(SEPARATOR);
        String itemName = nodeSet.substring(sepPos+1,nodeSet.length());
        String itemGroupName = bind.getItemGroup();
        String key = itemGroupName +SEPARATOR+ itemName;
        return  key;
    }

    private String itemLabelAndGroupNameMapKeyGenerator(ItemData itemData){
        ItemGroupMetadata itemGroupMetadata = itemGroupMetadataDao.findByItemId(itemData.getItem().getItemId());
        String key ="";
        if(itemGroupMetadata.isRepeatingGroup()) {
            key = itemData.getOrdinal()+SEPARATOR;
        }
        String groupName = itemGroupMetadata.getItemGroup().getName() == null ? "" : itemGroupMetadata.getItemGroup().getName();
        key += groupName +SEPARATOR+ itemData.getItem().getName();
        return key;
    }
}
