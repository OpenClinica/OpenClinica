package org.akaza.openclinica.web.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

/**
 * Miscellaneous utilities for web applications.
 * 
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 * 
 */
public abstract class WebUtil {

    private static final String HTTP_PREFIX = "http:";
    private static final String HTTPS_PREFIX = "https:";

    /**
     * Retrieves the base path pointing to the web application root directory.
     * 
     * Usage:
     * 
     * <pre>
     *  &lt;head&gt;
     *  &lt;base href="&lt;%= org.akaza.openclinica.web.util.WebUtil.basePath(pageContext) %&gt;" /&gt;
     *  ...
     *  &lt;/head&&gt;
     * </pre>
     * 
     * @param pageContext
     *            <code>JSP</code>'s page context
     * @return Base path
     */
    public static String basePath(PageContext pageContext) {
        HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
        StringBuffer base = req.getRequestURL();

        // Remove 'http:' or 'https:' protocols from the base URL to prevent
        // issues loading the content with different
        // protocols
        if (base.indexOf(HTTP_PREFIX) == 0) {
            base.delete(0, HTTP_PREFIX.length());
        } else if (base.indexOf(HTTPS_PREFIX) == 0) {
            base.delete(0, HTTPS_PREFIX.length());
        }

        base.delete(base.indexOf(req.getRequestURI()), base.length());
        base.append(req.getContextPath()).append("/");
        return base.toString();
    }

}
