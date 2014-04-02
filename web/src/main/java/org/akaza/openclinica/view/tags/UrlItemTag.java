package org.akaza.openclinica.view.tags;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.DisplayItemBean;
import org.akaza.openclinica.bean.submit.DisplaySectionBean;
import org.akaza.openclinica.bean.submit.ResponseOptionBean;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Custom tag used to render CRF parameterized links.
 *
 * @author Douglas Rodrigues (drodrigues@openclinica.com)
 */
public class UrlItemTag extends SimpleTagSupport {

    private static final Logger LOG = LoggerFactory.getLogger(UrlItemTag.class);

    private DisplayItemBean displayItem;

    private StudyBean study;

    private StudySubjectBean studySubject;

    private DisplaySectionBean section;

    private LocalizationContext bundle;

    public void setDisplayItem(DisplayItemBean displayItem) {
        this.displayItem = displayItem;
    }

    public void setStudy(StudyBean study) {
        this.study = study;
    }

    public void setStudySubject(StudySubjectBean studySubject) {
        this.studySubject = studySubject;
    }

    public void setSection(DisplaySectionBean section) {
        this.section = section;
    }

    public void setBundle(LocalizationContext bundle) {
        this.bundle = bundle;
    }

    @Override
    public void doTag() throws JspException, IOException {
        super.doTag();

        StringBuffer sb = new StringBuffer();
        sb.append("<a href=\"");
        sb.append(parseUrl(displayItem.getMetadata().getDefaultValue()));
        sb.append("\">");
        sb.append(bundle.getResourceBundle().getString("url_item_link_label"));
        sb.append("</a>");

        getJspContext().getOut().print(sb);
    }

    private String parseUrl(final String url) {
        StrSubstitutor substitutor = new StrSubstitutor(tokensMap());
        try {
            return new URI(substitutor.replace(url)).toASCIIString();
        } catch (URISyntaxException e) {
            LOG.error("Could not create URL", e);
            return StringUtils.EMPTY;
        }
    }

    private Map<String, String> tokensMap() {
        Map<String, String> result = new HashMap<String, String>();

        result.put("studySubject", studySubject.getOid());
        result.put("studyName", study.getName());
        result.put("eventName", displayItem.getEventDefinitionCRF().getName());
        result.put("eventOrdinal", Integer.toString(displayItem.getEventDefinitionCRF().getOrdinal()));
        result.put("crfName", displayItem.getEventDefinitionCRF().getCrf().getName());
        result.put("crfVersion", Integer.toString(displayItem.getEventDefinitionCRF().getCrf().getVersionNumber()));
        result.put("groupName", displayItem.getMetadata().getGroupLabel());
        result.put("groupOrdinal", Integer.toString(displayItem.getMetadata().getOrdinal()));
        result.put("itemName", displayItem.getItem().getName());

        return result;
    }

}
