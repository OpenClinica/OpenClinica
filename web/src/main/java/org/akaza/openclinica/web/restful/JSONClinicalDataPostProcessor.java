package org.akaza.openclinica.web.restful;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Runs a set of post-processing operations on JSON clinical data.
 *
 * @author Douglas Rodrigues (drodrigues@openclinica.com)
 */
public class JSONClinicalDataPostProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(JSONClinicalDataPostProcessor.class);

    private static final DateFormat SHORT_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static final DateFormat LONG_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");

    private static final Pattern SHORT_DATE = Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2}");

    private static final Pattern LONG_DATE =
            Pattern.compile("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]");

    private final Locale locale;

    public JSONClinicalDataPostProcessor(Locale locale) {
        this.locale = locale;
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
        */

        boolean isShort = SHORT_DATE.matcher(elem).matches();
        boolean isLong = LONG_DATE.matcher(elem).matches();

        if (isShort || isLong) {
            try {
                Date date;
                DateFormat formatter;

                if (isShort) {
                    date = SHORT_FORMAT.parse(elem);
                    formatter = DateFormat.getDateInstance(DateFormat.LONG, locale);
                } else {
                    date = LONG_FORMAT.parse(elem);
                    formatter = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
                }

                String formattedDate = formatter.format(date);

                //LOG.debug("Replacing '{}' by formatted date '{}'", elem, formattedDate);
                return formattedDate;


            } catch (ParseException e) {
                LOG.warn("Could not parse date from ODM element '" + elem + "'", e);
            }
        }

        return elem;
    }

}
