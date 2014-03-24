package org.akaza.openclinica.web.restful;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Runs a set of post-processing operations on JSON clinical data.
 *
 * @author Douglas Rodrigues (drodrigues@openclinica.com)
 */
public class JSONClinicalDataPostProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(JSONClinicalDataPostProcessor.class);

    private static final DateFormat DATE_INTERNAL_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static final DateFormat DATE_TIME_INTERNAL_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private static final DateFormat DATE_TIME_AUDIT_LOG_INTERNAL_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private static final String DATE_FORMAT_KEY = "date_format_string";

    private static final String DATE_TIME_FORMAT_KEY = "date_time_format_string";

    private static final Pattern DATE_PATTERN = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}");

    private static final Pattern DATE_TIME_PATTERN =
            Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]");

    /**
     * Matches the date & time format used to render audit log entries
     */
    private static final Pattern DATE_TIME_AUDIT_LOG_PATTERN =
            Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}");


    private final Locale locale;

    private final ResourceBundle formatResourceBundle;

    public JSONClinicalDataPostProcessor(Locale locale) {
        this.locale = locale;
        this.formatResourceBundle = ResourceBundleProvider.getFormatBundle(locale);
    }

    /**
     * Iterates over the elements of a JSON object.
     *
     * @param json JSON object to be processed
     */
    public void process(JSON json) {
        processJSONFields(json);
    }

    private void processJSONFields(JSON json) {
        if (json instanceof JSONArray) {
            processJSONArray((JSONArray) json);
        } else if (json instanceof JSONObject) {
            processJSONObject((JSONObject) json);
        }
    }

    private JSON processJSONArray(JSONArray jsonArray) {

        for (int i = 0; i < jsonArray.size(); i++) {
            Object elem = jsonArray.get(i);
            if (elem instanceof JSONArray) {
                processJSONArray((JSONArray) elem);
            } else if (elem instanceof JSONObject) {
                processJSONObject((JSONObject) elem);
            } else if (elem instanceof String) {
                jsonArray.set(i, processString((String) elem));
            }
        }
        return jsonArray;
    }

    private JSON processJSONObject(JSONObject jsonObject) {
        for (Object key : jsonObject.keySet()) {
            Object elem = jsonObject.get(key);
            if (elem instanceof JSONArray) {
                processJSONArray((JSONArray) elem);
            } else if (elem instanceof JSONObject) {
                processJSONObject((JSONObject) elem);
            } else if (elem instanceof String) {
                jsonObject.put(key, processString((String) elem));
            }
        }

        return jsonObject;
    }

    private String processString(String elem) {
        /*
        Tries to match the string with the following formats:
        2011-07-05
        2011-05-17 00:00:00.0
        2013-11-18T18:42:28 (Audit log date format)
        */

        boolean isShort = DATE_PATTERN.matcher(elem).matches();
        boolean isLong = DATE_TIME_PATTERN.matcher(elem).matches();
        boolean isAudit = DATE_TIME_AUDIT_LOG_PATTERN.matcher(elem).matches();

        if (isShort || isLong || isAudit) {
            try {
                Date date;
                DateFormat formatter;

                if (isShort) {
                    date = DATE_INTERNAL_FORMAT.parse(elem);
                    formatter = new SimpleDateFormat(formatResourceBundle.getString(DATE_FORMAT_KEY), locale);
                } else if (isLong) {
                    date = DATE_TIME_INTERNAL_FORMAT.parse(elem);
                    formatter = new SimpleDateFormat(formatResourceBundle.getString(DATE_TIME_FORMAT_KEY), locale);
                } else {
                    date = DATE_TIME_AUDIT_LOG_INTERNAL_FORMAT.parse(elem);
                    formatter = new SimpleDateFormat(formatResourceBundle.getString(DATE_TIME_FORMAT_KEY), locale);
                }

                return formatter.format(date);

            } catch (ParseException e) {
                LOG.warn("Could not parse date from ODM element '" + elem + "'", e);
            }
        }

        return elem;
    }

}
