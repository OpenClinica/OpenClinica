package org.akaza.openclinica.web.filter;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.jstl.core.Config;

import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter for applying the OpenClinica's Locale
 *
 * @since Jan. 2012
 */
// @author ywang
public final class LocaleFilter implements Filter {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    /**
     * Save customized Locale into session for Locale attribute and fmt Locale; and set response Locale
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)request;
        HttpServletResponse resp = (HttpServletResponse)response;
        updateLocale(req,resp,LocaleResolver.resolveLocale(req));
        if (chain != null)  {
          chain.doFilter(request, response);
        }
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void destroy() {
    }

    private void updateLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        if(locale != null) {
            HttpSession session = request.getSession(false);
            if(session != null) {
                session.setAttribute(LocaleResolver.getLocaleSessionAttributeName(), locale);
                Config.set(session, Config.FMT_LOCALE, locale);
                if(response != null)    response.setLocale(locale);
            } else {
                logger.debug("Locale can not be saved into session because session is null.");
            }
        } else {
            logger.debug("No Locale updating has been done because passed Locale is null.");
        }
    }
}
