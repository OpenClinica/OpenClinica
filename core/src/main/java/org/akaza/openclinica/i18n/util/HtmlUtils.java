package org.akaza.openclinica.i18n.util;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.ResourceBundle;

public class HtmlUtils {

    /**
     * Generates the necessary Javascript code to create a localized
     * CalendarPopup object. It adjusts: First day of the week Day headers Month
     * names Month abbreviations "Today" text
     *
     * @param varname
     *            Javascript variable name for the CalendarPopup object
     * @param divname
     *            <div> name to use for the calendar
     * @return a String with the Javacript code
     * @author Nacho M. Castejon and Jose Martinez Garcia, BAP Health
     */
    public static String getCalendarPopupCode(String varname, String divname) {
        StringBuffer out = new StringBuffer();
        out.append("var " + varname + " = new CalendarPopup(\"" + divname + "\");");
        // Following line have been added for year and month navigation combo
        //out.append(varname+".showNavigationDropdowns();");
        int weekStart = Calendar.getInstance(ResourceBundleProvider.getLocale()).getFirstDayOfWeek();

        if (weekStart == Calendar.SUNDAY)
            out.append(varname + ".setWeekStartDay(0);");
        if (weekStart == Calendar.MONDAY)
            out.append(varname + ".setWeekStartDay(1);");
        DateFormatSymbols dfs = new DateFormatSymbols(ResourceBundleProvider.getLocale());
        String[] weekDays = dfs.getShortWeekdays();
        String[] monthNames = dfs.getMonths();
        String[] monthAbbrev = dfs.getShortMonths();

        out.append(varname + ".setDayHeaders(");
        out.append("\"" + weekDays[1].substring(0, 1).toUpperCase(ResourceBundleProvider.getLocale()) + "\"");
        for (int i = 2; i < weekDays.length; i++)
            out.append(",\"" + weekDays[i].substring(0, 1).toUpperCase(ResourceBundleProvider.getLocale()) + "\"");
        out.append(");");

        out.append(varname + ".setMonthNames(");
        out.append("\"" + capitalize(monthNames[0]) + "\"");
        for (int i = 1; i < monthNames.length - 1; i++)
            out.append(",\"" + capitalize(monthNames[i]) + "\"");
        out.append(");");

        out.append(varname + ".setMonthAbbreviations(");
        out.append("\"" + capitalize(monthAbbrev[0]) + "\"");
        for (int i = 1; i < monthAbbrev.length - 1; i++)
            out.append(",\"" + capitalize(monthAbbrev[i]) + "\"");
        out.append(");");
        ResourceBundle reswords = ResourceBundleProvider.getWordsBundle();
        out.append(varname + ".setTodayText(\"" + reswords.getString("today") + "\");");
        return out.toString();
    }

    /**
     * Capitalize the first letter of a String.
     *
     * @param s
     *            String to capitalize
     * @return Capitalized string
     */
    private static String capitalize(String s) {
        char chars[] = s.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    /**
     * This class parses a date String like "12/10/2007" into an int[] array
     * such as {12, 10, 2007} for use, among others, in creating a
     * GregorianClandar object. The method assumes a default database format of
     * "12/10/2007" or "MM/dd/yyyy".
     *
     * @param dateString
     *            A String like "12/10/2007".
     * @return An int array made up of month, day, year (three components, in
     *         that order). If the dateString argument is invalid or empty,
     *         returns an empty array.
     * @see java.util.GregorianCalendar
     */
    private static int[] parseDateToArray(String dateString) {
        int[] intArray = new int[3];
        if (dateString == null || "".equalsIgnoreCase(dateString)) {
            intArray = new int[] {};
            return intArray;
        }
        String[] values = dateString.split("/");
        if (values == null || values.length < 3) {
            intArray = new int[] {};
            return intArray;
        }
        for (int i = 0; i < intArray.length; i++) {
            try {
                intArray[i] = Integer.parseInt(values[i]);
            } catch (NumberFormatException nfe) {
                // the method argument is not a valid date, or in the expected
                // format
                intArray = new int[] {};
                return intArray;

            }
        }
        return intArray;
    }

    /**
     * Parse a date String like "12/10/2007" into an array, then create a
     * GregorianCalendar object, returning a Date object for formatting. This
     * class assumes a format pattern of MM/dd/yyyy.
     *
     * @param _date
     *            A String like "12/10/2007" . If the argument cannot be parsed
     *            into three segments, then the method returns null.
     * @return A java.util.Date object
     */
    public static synchronized Date parseDateValue(String _date) {
        int[] intArr = parseDateToArray(_date);
        Calendar calendar = null;
        // check simple validity of date values
        if (intArr.length == 0 || intArr.length < 3 || intArr[0] > 12 || intArr[1] > 31) {
            return null;
        } else {
            // new GregorianCalendar(year,month,day)
            calendar = new GregorianCalendar(intArr[2], intArr[0] - 1, intArr[1]);
        }
        return calendar.getTime();
    }

}
