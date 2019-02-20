package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.*;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Performs the variable substitution in the CRF fields that support it.
 *
 * @author Douglas Rodrigues (drodrigues@openclinica.com)
 */
public class VariableSubstitutionHelper {

    private static final Logger LOG = LoggerFactory.getLogger(VariableSubstitutionHelper.class);

    private static final String ENCODING = "UTF-8";

    private static final String TOKEN_REGEX = "\\$\\{.*?\\}";

    /**
     * Replaces the variables in each {@link DisplayItemBean} of the {@link DisplaySectionBean}.
     *
     * @param section The display section to have its items processed.
     * @param study Study associated with the display section.
     * @param studySubject Subject associated with the display section.
     */
    public static void replaceVariables(DisplaySectionBean section, StudyBean study, StudySubjectBean studySubject,
                                        StudyEventDefinitionBean eventDef, StudyEventBean event, DataSource dataSource) {

        StrSubstitutor subst = new StrSubstitutor(buildTokensMap(section, studySubject, study, eventDef, event, dataSource));

        for (DisplayItemBean displayItem: section.getItems()) {
            ItemFormMetadataBean metadata = displayItem.getMetadata();
            metadata.setRightItemText(replace(subst, metadata.getRightItemText()));
            metadata.setLeftItemText(replace(subst, metadata.getLeftItemText()));
            metadata.setHeader(replace(subst, metadata.getHeader()));
            metadata.setSubHeader(replace(subst, metadata.getSubHeader()));
        }

    }

    private static final String replace(StrSubstitutor subst, String value) {
        return subst.replace(value).replaceAll(TOKEN_REGEX, StringUtils.EMPTY);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> buildTokensMap(DisplaySectionBean section, StudySubjectBean studySubject,
                                                      StudyBean study, StudyEventDefinitionBean eventDef,
                                                      StudyEventBean event, DataSource dataSource) {

        ItemDAO itemDAO = new ItemDAO(dataSource);

        List<ItemBean> items = itemDAO.findAllWithItemDataByCRFVersionId(
                section.getCrfVersion().getId(), section.getEventCRF().getId());

        Map<String, String> tokensMap = new HashMap<String, String>();
        tokensMap.put("studySubject", encode(studySubject.getName()));
        if (studySubject.getOid() != null && !studySubject.getOid().isEmpty()){
            tokensMap.put("studySubjectOID", encode(studySubject.getOid()));
        }
        else tokensMap.put("studySubjectOID", "");
        tokensMap.put("studyName", encode(study.getName()));
        tokensMap.put("eventName", encode(eventDef.getName()));
        if (event == null) tokensMap.put("eventOrdinal", "");
        else tokensMap.put("eventOrdinal", encode(Integer.toString(event.getSampleOrdinal())));
        tokensMap.put("crfName", encode(section.getCrf().getName()));
        tokensMap.put("crfVersion", encode(section.getCrfVersion().getName()));

		// FR:2019-02-20
		// event status as code and status name
		if (event == null) {
			tokensMap.put("subjectEventStatusCode", "");
			tokensMap.put("subjectEventStatus", "");
		} else {
			tokensMap.put("subjectEventStatusCode", encode(Integer.toString(event.getSubjectEventStatus().getId())));
			tokensMap.put("subjectEventStatus", encode(event.getSubjectEventStatus().getName()));
		}

		// FR:2019-02-20
		// crf status as code and status name
		if (section.getCrf().getStatus() == null) {
			tokensMap.put("crfStatusCode", "");
			tokensMap.put("crfStatus", "");
		} else {
			tokensMap.put("crfStatusCode", encode(Integer.toString(section.getCrf().getStatus().getId())));
			tokensMap.put("crfStatus", encode(section.getCrf().getStatus().getName()));
		}

        // Render a set of "item['ITEM_NAME']" tokens for existing item data
        for (ItemBean item : items) {

            // If the item has multiple values, combine them in a comma-separated list
            List<String> values = new ArrayList<String>();
            for (ItemDataBean itemData: item.getItemDataElements()) {
                values.add(itemData.getValue());
            }
            String value = StringUtils.join(values, ',');

            tokensMap.put("item['" + item.getName() + "']", encode(value));
        }

        if (LOG.isDebugEnabled()) {
            for(String key : tokensMap.keySet()) {
                LOG.debug("Substitution context: {} = {}", key, tokensMap.get(key));
            }
        }

        return tokensMap;

    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Invalid encoding " + ENCODING, e);
        }
    }
}
