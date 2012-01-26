package org.akaza.openclinica.i18n.util;

import org.akaza.openclinica.core.form.StringUtil;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Utility class for format*.properties file
 *
 * @since Jan. 2012
 */
// @author ywang
public class I18nFormatUtil {
    private final static Locale DEFAULT_LOCALE = new Locale("en");
    private final static String YEAR_FORMAT_STRING = "yyyy";

    private I18nFormatUtil() {}

    /**
     * If Locale is null, default "en" Locale will be used.
     * @param locale
     * @return
     */
    public final static SimpleDateFormat getDateFormat(Locale locale) {
        Locale l = resolveLocale(locale);
        String dateFormat = dateFormatString(locale);
        return new SimpleDateFormat(dateFormat,l);
    }

    /**
     * If Locale is null, default "en" Locale will be used.
     * @param locale
     * @return
     */
    public final static String dateFormatString(Locale locale) {
        Locale l = resolveLocale(locale);
        return StringUtil.parseDateFormat(
                ResourceBundleProvider.getFormatBundle(l).getString("date_format_string"));
    }

    /**
     * If Locale is null, default "en" Locale will be used.
     * @param locale
     * @return
     */
	public final static SimpleDateFormat getDateTimeFormat(Locale locale) {
        Locale l = resolveLocale(locale);
        return new SimpleDateFormat(
                ResourceBundleProvider.getFormatBundle(l).getString("date_time_format_string"),l);
    }

    /**
     * If Locale is null, default "en" Locale will be used.
     * @param locale
     * @return
     */
	public final static SimpleDateFormat getYearMonthFormat(Locale locale) {
	    Locale l = resolveLocale(locale);
	    String yearMonthFormat = yearMonthFormatString(l);
	    return new SimpleDateFormat(yearMonthFormat,l);
	}

    /**
     * If Locale is null, default "en" Locale will be used.
     * @param locale
     * @return
     */
	public final static String yearMonthFormatString(Locale locale) {
	    Locale l = resolveLocale(locale);
	    return StringUtil.parseDateFormat(
                ResourceBundleProvider.getFormatBundle(l).getString("date_format_year_month"));
	}

	public final static String yearFormatString() {
	    return YEAR_FORMAT_STRING;
	}

    private final static Locale resolveLocale(Locale locale) {
        return locale == null ? DEFAULT_LOCALE : locale;
    }
}
