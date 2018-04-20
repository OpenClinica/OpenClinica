package org.akaza.openclinica.view.tags;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang.StringEscapeUtils;
/**
 * Created by IntelliJ IDEA.
 * User: bruceperry
 * Date: Nov 14, 2008
 * Time: 3:18:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class AlertTag extends SimpleTagSupport {
    private String message;
    @Override
    public void doTag() throws JspException, IOException {
        JspContext context = getJspContext();
        JspWriter tagWriter = context.getOut();
        StringBuilder builder = new StringBuilder("");

        List<String> messages = (ArrayList) context.findAttribute("pageMessages");
        if(messages != null){
            for(String message : messages){
                builder.append(StringEscapeUtils.escapeHtml(message));
                builder.append("<br />");
            }

        }
        tagWriter.println(builder.toString());


    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String messages) {
        this.message = messages;
    }
}
