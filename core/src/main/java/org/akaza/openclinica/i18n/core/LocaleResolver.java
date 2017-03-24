package org.akaza.openclinica.i18n.core;

import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Customize Locale.
 *
 * @since Jan. 2012
 */
// @author ywang
public final class LocaleResolver {
	private final static Locale DEFAULT_LOCALE = new Locale("en");
	private final static String LOCALE_SESSION_ATTRIBUTE_NAME
	    = SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME;

	/**
	 * Find Locale basing on order of accept languages and availability of i18n properties files.
	 *
	 * @param request
	 * @return
	 */
	public final static Locale resolveLocale(HttpServletRequest request) {
		if(request != null) {
			for(@SuppressWarnings("unchecked")
			Enumeration<Locale> locales = request.getLocales(); locales.hasMoreElements();) {
				Locale locale = locales.nextElement();
				ResourceBundleProvider.updateLocale(locale);
				if(isQualifiedLocale(locale)) {
				    locale = ResourceBundleProvider.getFormatBundle(locale).getLocale();
                    return locale;
				} else if(getDefaultLocale().getLanguage().equalsIgnoreCase(locale.getLanguage())) {
				    break;
				}
			}
        }
		return getDefaultLocale();
	}

	/**
	 * Get Locale from session first. If it is null, will call resolveLocale method.
	 * @param request
	 * @return
	 */
	public final static Locale getLocale(HttpServletRequest request) {
		/*Edited by ll 20160811  cn will cause dateformat error  so just use US
	    Locale locale = getLocaleInSession(request.getSession(false));
	    if(locale == null) {
	        return resolveLocale(request);
	    }
	    return locale;
	    */
		return getDefaultLocale();
	}

	/*
     * Null will be returned if HttpSession is null or no Locale attribute exists
     * @param session
     * @return
     */
    private final static Locale getLocaleInSession(HttpSession session) {
        if(session != null) {
            return (Locale)session.getAttribute(getLocaleSessionAttributeName());
        }
        return null;
    }

	/*
	 * A locale is qualified only if all ResourceBundles' Locales are available
	 */
	private static boolean isQualifiedLocale(Locale locale) {
		ResourceBundle rb = ResourceBundleProvider.getAdminBundle(locale);
		if(isAvailable(rb, locale)) {
			rb = ResourceBundleProvider.getAuditEventsBundle(locale);
			if(isAvailable(rb, locale))	{
				rb = ResourceBundleProvider.getExceptionsBundle(locale);
				if(isAvailable(rb, locale))	{
					rb = ResourceBundleProvider.getFormatBundle(locale);
					if(isAvailable(rb, locale))	{
						rb = ResourceBundleProvider.getPageMessagesBundle(locale);
						if(isAvailable(rb, locale)) {
							rb = ResourceBundleProvider.getTermsBundle(locale);
							if(isAvailable(rb, locale)) {
								rb = ResourceBundleProvider.getTextsBundle(locale);
								if(isAvailable(rb, locale)) {
									rb = ResourceBundleProvider.getWordsBundle(locale);
									if(isAvailable(rb, locale)) {
										rb = ResourceBundleProvider.getWorkflowBundle(locale);
										return isAvailable(rb, locale);
									}
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	private static boolean isAvailable(ResourceBundle rb, Locale locale) {
	    if(rb != null ) {
	        Locale loc = rb.getLocale();
	        return loc != null && loc.toString().length()>0
	                && loc.getLanguage().equals(locale.getLanguage());
	    }
	    return false;
	}

	public final static String getLocaleSessionAttributeName() {
	    return LocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME;
	}

	public static Locale getDefaultLocale() {
	    return LocaleResolver.DEFAULT_LOCALE;
	}
}
