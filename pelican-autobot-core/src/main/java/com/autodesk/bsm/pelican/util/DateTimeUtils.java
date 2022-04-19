package com.autodesk.bsm.pelican.util;

import com.autodesk.bsm.pelican.constants.PelicanConstants;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class DateTimeUtils {

    private static Calendar calendar;
    private static DateFormat dateFormat;
    private static List<String> dates;
    private static Date date;
    private static String startDate;
    private static String endDate;
    private static final Logger LOGGER = LoggerFactory.getLogger(DateTimeUtils.class.getSimpleName());

    /**
     * this method would return simpledateformate for custom format
     *
     * @return SimpleDateFormat
     */
    public static SimpleDateFormat getSimpleDateFormat(final String customFormat) {
        return new SimpleDateFormat(customFormat);
    }

    /**
     * this method would return simpledateformate for custom format in pacific time zone
     *
     * @return SimpleDateFormat
     */
    public static SimpleDateFormat getSimpleDateFormatInPST(final String customFormat) {
        final TimeZone pacificTimeZone = TimeZone.getTimeZone("America/Los_Angeles");
        final SimpleDateFormat sdf = new SimpleDateFormat(customFormat);
        sdf.setTimeZone(pacificTimeZone);

        return sdf;
    }

    public static String getDateStamp(final String date) throws ParseException {
        final SimpleDateFormat formatDate = new SimpleDateFormat(PelicanConstants.DATE_FORMAT_WITH_SLASH);
        return formatDate.format(formatDate.parse(date));
    }

    public static String getUTCDatetimeAsString(final String dateFormat) {
        final SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        sdf.setTimeZone(TimeZone.getTimeZone(PelicanConstants.UTC_TIME_ZONE));

        return sdf.format(new Date());
    }

    public static String getYesterdayUTCDatetimeAsString(final String dateFormat) {
        final SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        sdf.setTimeZone(TimeZone.getTimeZone(PelicanConstants.UTC_TIME_ZONE));
        final Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -1);
        final Date yesterday = c.getTime();

        return sdf.format(yesterday);
    }

    public static String getPreviousUTCDatetimeAsString(final String dateFormat, final int daysBefore) {
        final SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        sdf.setTimeZone(TimeZone.getTimeZone(PelicanConstants.UTC_TIME_ZONE));
        final Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -daysBefore);
        final Date yesterday = c.getTime();

        return sdf.format(yesterday);
    }

    public static String getTomorrowUTCDatetimeAsString(final String dateFormat) {
        final SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        sdf.setTimeZone(TimeZone.getTimeZone(PelicanConstants.UTC_TIME_ZONE));
        final Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 1);
        final Date tomorrow = c.getTime();

        return sdf.format(tomorrow);
    }

    /**
     * This method will return the Date as string by adding the hours to the current date in UTC time zone.
     *
     * @param dateFormat
     * @param hoursToAdd
     * @return String
     */
    public static String getDatetimeWithAddedHoursAsString(final String dateFormat, final int hoursToAdd) {
        final SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        sdf.setTimeZone(TimeZone.getTimeZone(PelicanConstants.UTC_TIME_ZONE));
        final Calendar c = Calendar.getInstance();
        c.add(Calendar.HOUR_OF_DAY, hoursToAdd);
        final Date tomorrow = c.getTime();

        return sdf.format(tomorrow);
    }

    /**
     * This method will return the Date as string by adding the minutes to the current date in UTC time zone.
     *
     * @param dateFormat
     * @param minutesToAdd
     * @return String
     */
    public static String getDatetimeWithAddedMinutesAsString(final String dateFormat, final int minutesToAdd) {
        final SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        sdf.setTimeZone(TimeZone.getTimeZone(PelicanConstants.UTC_TIME_ZONE));
        final Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, minutesToAdd);
        final Date tomorrow = c.getTime();

        return sdf.format(tomorrow);
    }

    public static String getNextMonthDateAsString() throws ParseException {

        String dt = getUTCDatetimeAsString(PelicanConstants.DATE_FORMAT_WITH_SLASH);
        final SimpleDateFormat sdf = new SimpleDateFormat(PelicanConstants.DATE_FORMAT_WITH_SLASH);
        final Calendar c = Calendar.getInstance();
        c.setTime(sdf.parse(dt));
        c.add(Calendar.MONTH, 1); //
        dt = sdf.format(c.getTime());
        return dt;
    }

    /**
     * Method to get previous billing date based on Billing Frequency.
     *
     * @return
     * @throws ParseException
     */
    public static String getPreviousBillingDate(String date, final String billingFrequency) throws ParseException {

        final SimpleDateFormat sdf = new SimpleDateFormat(PelicanConstants.DATE_FORMAT_WITH_SLASH);
        final Calendar c = Calendar.getInstance();
        c.setTime(sdf.parse(date));
        if (billingFrequency.equalsIgnoreCase("MONTH")) {
            c.add(Calendar.MONTH, -1);
        } else if (billingFrequency.equalsIgnoreCase("YEAR")) {
            c.add(Calendar.YEAR, -1);
        }
        date = sdf.format(c.getTime());
        return date;
    }

    /**
     * Get Next Billing Date for Monthly and Yearly subscriptions.
     *
     * @param date
     * @param billingFrequency
     * @return
     * @throws ParseException
     */
    public static String getNextBillingDate(String date, final String billingFrequency) throws ParseException {

        final SimpleDateFormat sdf = new SimpleDateFormat(PelicanConstants.DATE_FORMAT_WITH_SLASH);
        final Calendar c = Calendar.getInstance();
        c.setTime(sdf.parse(date));
        if (billingFrequency.equalsIgnoreCase("MONTH")) {
            c.add(Calendar.MONTH, 1);
        } else if (billingFrequency.equalsIgnoreCase("YEAR")) {
            c.add(Calendar.YEAR, 1);
        }
        date = sdf.format(c.getTime());
        return date;
    }

    /**
     * This is a method to return the current date - specified hours as a string
     *
     * @param dateFormat
     * @param numberOfHours
     * @return Date as String
     */
    public static String getCurrentTimeMinusSpecifiedHours(final String dateFormat, final int numberOfHours) {
        final SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        sdf.setTimeZone(TimeZone.getTimeZone(PelicanConstants.UTC_TIME_ZONE));
        final Calendar c = Calendar.getInstance();
        c.add(Calendar.HOUR_OF_DAY, -numberOfHours);
        final Date pastDate = c.getTime();

        return sdf.format(pastDate);
    }

    /**
     * This method returns a future expiration date.
     *
     * @return expirationDate
     */
    public static Date getFutureExpirationDate() {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2021);
        return calendar.getTime();
    }

    /**
     * This method returns a future expiration date.
     *
     * @return expirationDate
     */
    public static Date getUTCFutureExpirationDate() {
        final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(PelicanConstants.UTC_TIME_ZONE));
        calendar.set(Calendar.YEAR, 2021);
        return calendar.getTime();
    }

    /**
     * Adjust date by specified number from now to the future
     *
     * @param date format e.g. "MM/dd/yyyy"
     * @param number of days to adjust to the future
     */
    public static Date getNowPlusSecs(final int secs) {
        final DateTime date = new DateTime(DateTimeZone.UTC);
        return (date.plusSeconds(secs).toDate());
    }

    /*
     * This method returns previous month's date as String. It returns today's date minus 1 month.
     */
    public static String getPreviousMonthDateAsString() throws ParseException {
        String date = getUTCDatetimeAsString(PelicanConstants.DATE_FORMAT_WITH_SLASH);
        final SimpleDateFormat dateFormat = new SimpleDateFormat(PelicanConstants.DATE_FORMAT_WITH_SLASH);
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateFormat.parse(date));
        calendar.add(Calendar.MONTH, -1);
        date = dateFormat.format(calendar.getTime());
        return date;
    }

    /**
     * This method returns previous days date as String. It returns today's date minus received days
     */
    public static String getNowMinusDays(final int days) {

        String date = getUTCDatetimeAsString(PelicanConstants.DATE_FORMAT_WITH_SLASH);
        final SimpleDateFormat dateFormat = new SimpleDateFormat(PelicanConstants.DATE_FORMAT_WITH_SLASH);
        final Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(dateFormat.parse(date));
        } catch (final ParseException ex) {
            LOGGER.error("There is a parse exception");
        }
        calendar.add(Calendar.DAY_OF_MONTH, -days);
        date = dateFormat.format(calendar.getTime());
        return date;
    }

    /**
     * This method returns previous days date as String. It returns today's date minus received days
     *
     * @param days
     * @return
     */
    public static String getNowPlusDays(final int days) {
        try {
            String date = getUTCDatetimeAsString(PelicanConstants.DATE_FORMAT_WITH_SLASH);
            final SimpleDateFormat dateFormat = new SimpleDateFormat(PelicanConstants.DATE_FORMAT_WITH_SLASH);
            final TimeZone timeZone = TimeZone.getTimeZone(PelicanConstants.UTC_TIME_ZONE);
            final Calendar calendar = Calendar.getInstance(timeZone);
            calendar.setTime(dateFormat.parse(date));
            calendar.add(Calendar.DAY_OF_MONTH, days);
            date = dateFormat.format(calendar.getTime());
            return date;
        } catch (final ParseException e) {
            LOGGER.info("Parsing Exception:" + e.getMessage());
            return null;
        }
    }

    /**
     * This method returns start date and end date in a list
     *
     * @return start and end date
     */
    public static List<String> getStartDateAndEndDate(final String dateTimeFormat) {

        dates = new ArrayList<>();
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat(dateTimeFormat);
        dateFormat.setTimeZone(TimeZone.getTimeZone(PelicanConstants.UTC_TIME_ZONE));
        date = new Date();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR, -1);
        startDate = dateFormat.format(calendar.getTime());
        dates.add(startDate);
        calendar.add(Calendar.HOUR, 2);
        endDate = dateFormat.format(calendar.getTime());
        dates.add(endDate);

        return dates;
    }

    /**
     * This method returns future start date and end date in a list
     *
     * @return start and end date
     */
    public static List<String> getFutureStartDateAndEndDate(final String dateTimeFormat) {

        dates = new ArrayList<>();
        dateFormat = new SimpleDateFormat(dateTimeFormat);
        dateFormat.setTimeZone(TimeZone.getTimeZone(PelicanConstants.UTC_TIME_ZONE));
        date = new Date();
        calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 2);
        startDate = dateFormat.format(calendar.getTime());
        dates.add(startDate);
        calendar.add(Calendar.DATE, 1);
        endDate = dateFormat.format(calendar.getTime());
        dates.add(endDate);

        return dates;
    }

    /**
     * This method returns start date and end date in a list with bigger ranges
     *
     * @return start and end date
     */
    public static List<String> getStartDateAndEndDateGreaterRanges(final String dateTimeFormat) {

        dates = new ArrayList<>();
        dateFormat = new SimpleDateFormat(dateTimeFormat);
        dateFormat.setTimeZone(TimeZone.getTimeZone(PelicanConstants.UTC_TIME_ZONE));
        date = new Date();
        calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, -50);
        startDate = dateFormat.format(calendar.getTime());
        dates.add(startDate);
        calendar.add(Calendar.DATE, 100);
        endDate = dateFormat.format(calendar.getTime());
        dates.add(endDate);

        return dates;
    }

    /**
     * This method returns past start date and end date in a list with bigger ranges
     *
     * @return start and end date
     */
    public static List<String> getPastStartDateAndEndDate(final String dateTimeFormat) {

        dates = new ArrayList<>();
        dateFormat = new SimpleDateFormat(dateTimeFormat);
        dateFormat.setTimeZone(TimeZone.getTimeZone(PelicanConstants.UTC_TIME_ZONE));
        date = new Date();
        calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, -3);
        startDate = dateFormat.format(calendar.getTime());
        dates.add(startDate);
        calendar.add(Calendar.DATE, 1);
        endDate = dateFormat.format(calendar.getTime());
        dates.add(endDate);

        return dates;
    }

    /**
     * method to change format of a date from MM-dd-yyyy to yyyy-MM-dd or vice versa
     *
     * @return date with changed format
     */
    public static String changeDateFormat(final String givenDate, final String currentFormat,
        final String requiredFormat) {
        final DateFormat parser = new SimpleDateFormat(currentFormat);
        Date date = null;
        try {
            date = parser.parse(givenDate);
        } catch (final ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        final DateFormat formatter = new SimpleDateFormat(requiredFormat);
        return formatter.format(date);
    }

    /**
     * This method takes date as string and converts into Date using the passed date format
     *
     * @param dateFormat
     * @param date
     * @return
     * @throws ParseException
     */
    public static Date getDate(final String dateFormat, final String date) throws ParseException {

        final SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.parse(date);
    }

    /**
     * Get today's date
     *
     * @param the date format: "MM/dd/yyyy"
     */
    public static String getNowAsString(final String dtFormat) {
        final DateTimeFormatter format = DateTimeFormat.forPattern(dtFormat);
        final DateTime date = new DateTime();
        return date.toString(format);
    }

    /**
     * Adjust date by specified number from now to the past
     *
     * @param date format e.g. "MM//dd//yyyy"
     * @param number of days to adjust to the past
     */
    public static String getNowMinusDays(final String dtFormat, final int numberOfDays) {
        if (numberOfDays < 0) {
            throw new RuntimeException("Argument 'numberOfDays' less than 0");
        }
        final DateTimeFormatter format = DateTimeFormat.forPattern(dtFormat);
        final DateTime date = new DateTime();
        return date.minusDays(numberOfDays).toString(format);
    }

    /**
     * Adjust date by specified number of minutes from now to the future
     *
     * @param date format e.g. "MM//dd//yyyy"
     * @param number of minutes to adjust to the future
     */
    public static String getNowAsUTCPlusMinutes(final String dtFormat, final int minutes) {
        final DateTimeFormatter format = DateTimeFormat.forPattern(dtFormat);
        final DateTime date = new DateTime(DateTimeZone.UTC);
        return date.plusMinutes(minutes).toString(format);
    }

    /**
     * Adjust date by specified number of days from now to the future
     *
     * @param date format e.g. "MM//dd//yyyy"
     * @param number of days to adjust to the future
     */
    public static String getNowAsUTCPlusDays(final String dtFormat, final int days) {
        final DateTimeFormatter format = DateTimeFormat.forPattern(dtFormat);
        final DateTime date = new DateTime(DateTimeZone.UTC);
        return date.plusDays(days).toString(format);
    }

    /**
     * Adjust date by specified number of days from now to the future in date time format.
     *
     * @param date format e.g. "MM//dd//yyyy"
     * @param number of days to adjust to the future
     */
    public static DateTime getNowPlusDaysAsUTC(final String dtFormat, final int days) {
        final DateTime date = new DateTime(DateTimeZone.UTC);
        return date.plusDays(days);
    }

    /**
     * Adjust date by specified number from now to the future
     *
     * @param date format e.g. "MM/dd/yyyy"
     * @param number of days to adjust to the future
     */
    public static String getNowPlusDays(final String dtFormat, final int numberOfDays) {
        final DateTimeFormatter format = DateTimeFormat.forPattern(dtFormat);
        final DateTime date = new DateTime();
        return date.plusDays(numberOfDays).toString(format);
    }

    public static String getNowAsUTC(final String dtFormat) {
        final DateTimeFormatter format = DateTimeFormat.forPattern(dtFormat);
        final DateTime date = new DateTime(DateTimeZone.UTC);
        return date.toString(format);
    }

    /**
     * Adjust date by specified number of minutes from now to the past
     *
     * @param date format e.g. "MM//dd//yyyy"
     * @param number of minutes to adjust to the past
     */
    public static String getNowMinusMinutes(final String dtFormat, final int numberOfMinutes) {
        if (numberOfMinutes < 0) {
            throw new RuntimeException("Argument 'numberOfHours' less than 0");
        }
        final DateTimeFormatter format = DateTimeFormat.forPattern(dtFormat);
        final DateTime date = new DateTime();
        return date.minusMinutes(numberOfMinutes).toString(format);
    }

    public static String getNowAsUTCPlusMonths(final String dtFormat, final int numberOfMonths) {
        if (numberOfMonths < 0) {
            throw new RuntimeException("Argument 'numberOfMonths' less than 0");
        }
        final DateTimeFormatter format = DateTimeFormat.forPattern(dtFormat);
        final DateTime date = new DateTime(DateTimeZone.UTC);
        return date.plusMonths(numberOfMonths).toString(format);
    }

    public static String getNowAsUTCPlusYears(final String dtFormat, final int years) {
        if (years < 0) {
            throw new RuntimeException("Argument '# of years' less than 0");
        }
        final DateTimeFormatter format = DateTimeFormat.forPattern(dtFormat);
        final DateTime date = new DateTime(DateTimeZone.UTC);
        return date.plusYears(years).toString(format);
    }

    public static double getDaysInBillingCycle(final String billingStartDate, final String billingEndDate) {
        // get the StartDate details
        final int startMonth = Integer.parseInt(billingStartDate.substring(0, 2));
        final int startDate = Integer.parseInt(billingStartDate.substring(3, 5));
        final int startYear = Integer.parseInt(billingStartDate.substring(6, 10));
        // get the next billingdate details
        final int endMonth = Integer.parseInt(billingEndDate.substring(0, 2));
        final int endDate = Integer.parseInt(billingEndDate.substring(3, 5));
        final int endYear = Integer.parseInt(billingEndDate.substring(6, 10));

        @SuppressWarnings("deprecation")
        final Date startDay = new Date(startYear - 1900, startMonth - 1, startDate);
        final Date endDay = new Date(endYear - 1900, endMonth - 1, endDate);
        final double daysInCycle = Days.daysBetween(new DateTime(startDay), new DateTime(endDay)).getDays();
        LOGGER.info("Days in the billing cycle is :" + daysInCycle);
        return daysInCycle;
    }

    /**
     * Method to add days to the given date
     *
     * @return string representation of the date in dateFormat
     */
    public static String addDaysToDate(final String date, final String dateFormat, final int daysToAdd) {
        final DateTimeFormatter format = DateTimeFormat.forPattern(dateFormat);
        final DateTime newDate = format.parseDateTime(date);
        return newDate.plusDays(daysToAdd).toString(dateFormat);
    }

    /**
     * method parse to Date from String
     *
     * @return simple date format
     */
    public static Date convertStringToDate(final String dateString, final String format) {
        Date date = null;
        final SimpleDateFormat df = new SimpleDateFormat(format);
        try {
            date = df.parse(dateString);
        } catch (final Exception ex) {
            LOGGER.info(ex.getMessage());
        }
        return date;
    }

    /**
     * @return Arraylist of start date and end date
     */
    public static ArrayList<String> getStartDateOfTodayAndEndDateInNextYear() {
        final ArrayList<String> dateRangeList = new ArrayList<>();
        final DateFormat dFormat = new SimpleDateFormat(PelicanConstants.DATE_FORMAT_WITH_SLASH);
        dFormat.setTimeZone(TimeZone.getTimeZone(PelicanConstants.UTC_TIME_ZONE));
        final Calendar calendar = Calendar.getInstance();
        final Date startDay = calendar.getTime();
        final String startDate = dFormat.format(startDay);
        dateRangeList.add(startDate);
        calendar.add(Calendar.YEAR, 1);
        final Date endDay = calendar.getTime();
        final String endDate = dFormat.format(endDay);
        dateRangeList.add(endDate);

        return dateRangeList;
    }

    /**
     * @return Arraylist of future start date and end date
     */
    public static ArrayList<String> getFuturestartDateAndEndDate() {
        final ArrayList<String> futureDateRangeList = new ArrayList<>();
        final DateFormat dFormat = new SimpleDateFormat(PelicanConstants.DATE_FORMAT_WITH_SLASH);
        dFormat.setTimeZone(TimeZone.getTimeZone(PelicanConstants.UTC_TIME_ZONE));
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 3);
        final Date futureStartDay = calendar.getTime();
        final String futureStartDate = dFormat.format(futureStartDay);
        futureDateRangeList.add(futureStartDate);
        calendar.add(Calendar.YEAR, 2);
        final Date futureEndDay = calendar.getTime();
        final String futureEndDate = dFormat.format(futureEndDay);
        futureDateRangeList.add(futureEndDate);

        return futureDateRangeList;

    }

    public static String getCurrentDate(final String outputFormat) {
        final DateFormat dformat = new SimpleDateFormat(outputFormat);
        dformat.setTimeZone(TimeZone.getTimeZone(PelicanConstants.UTC_TIME_ZONE));

        return dformat.format(new Date());
    }

    public static String getCurrentDate() {
        return getCurrentDate(PelicanConstants.DEFAULT_DATE_FORMAT);
    }

    public static String getToday() {
        return getCurrentDate(PelicanConstants.DEFAULT_DATE_FORMAT);
    }

    /*
     * public static String formatDate(String stringDate, String outputFormat) { String[] validParseFormats =
     * {"MM/dd/yy", "MM-dd-yy", "MM.dd.yy"}; DateFormat df; Date date = null; boolean success = false; String
     * formattedDate = "";
     *
     * for (String format : validParseFormats) { df = new SimpleDateFormat(format);
     *
     * try { date = df.parse(stringDate); success = true;
     *
     * break; } catch (ParseException pe) {} }
     *
     * if (success) { df = new SimpleDateFormat(outputFormat); formattedDate = df.format(date); }
     *
     * return formattedDate; }
     */

    public static String formatDate(final String stringDate, final String outputFormat) {
        // These represent the valid formats of the passed-in date, stringDate.
        // The output format is defined in outputFormat
        // NOTE See comments at PelicanConstants.DEFAULT_DATE_FORMAT
        final String[] validParseFormats =
            { "MM/dd/yy", "MM-dd-yy", "MM.dd.yy", PelicanConstants.DATE_FORMAT_WITH_SLASH };
        // String[] validParseFormats = {"dd/MM/yy", "dd-MM-yy", "dd.MM.yy"};

        DateFormat df;
        Date date = null;
        boolean success = false;
        String formattedDate = "";

        // If stringDate is empty, we will pass it through. This allows the user
        // to specify
        // empty date fields for error checking
        if (!stringDate.isEmpty()) {
            for (final String format : validParseFormats) {
                df = new SimpleDateFormat(format);

                try {
                    date = df.parse(stringDate);
                    success = true;

                    break;
                } catch (final ParseException pe) {
                }
            }

            if (success) {
                df = new SimpleDateFormat(outputFormat);
                formattedDate = df.format(date);
            } else {
                formattedDate = PelicanConstants.INVALID_INPUT;
            }
        }

        return formattedDate;
    }

    /**
     * method to convert a list of string to a list of Date
     */
    public static List<Date> convertStringListToDateList(final List<String> stringList, final String format) {
        final List<Date> dateList = new ArrayList<>();
        for (final String aStringList : stringList) {
            dateList.add(convertStringToDate(aStringList, format));
        }
        return dateList;
    }

    /**
     * @param stringDateTime
     * @return String [0] Date String [1] Time
     */
    public static String[] getDateAndTimePartFromDateTime(final String dateTime) {
        return dateTime.split(" ");
    }

    /**
     * This method returns true if the given date is on or after start date and on or before end date or else false
     *
     * @param startDate
     * @param endDate
     * @param date
     * @param dateFormat
     * @return boolean
     */
    public static boolean isDateInRange(final String startDate, final String endDate, final String date,
        final String dateFormat) {

        final Date date1 = convertStringToDate(startDate, dateFormat);
        final Date date2 = convertStringToDate(endDate, dateFormat);
        final Date date3 = convertStringToDate(date, dateFormat);

        return (date3.equals(date1) || date3.after(date1)) && (date3.equals(date2) || date3.before(date2));
    }

    /**
     * This method returns true if the both date are same or else false
     *
     * @param startDate
     * @param endDate
     * @param dateFormat
     * @return boolean
     */
    public static boolean isSameDate(final String startDate, final String endDate, final String dateFormat) {
        final Date date1 = convertStringToDate(startDate, dateFormat);
        final Date date2 = convertStringToDate(endDate, dateFormat);

        return date1.equals(date2);
    }

    /**
     * Method to add months to a date.
     *
     * @param date
     * @param dateFormat
     * @param monthsToAdd
     * @return
     */
    public static String addMonthsToDate(final String date, final String dateFormat, final int monthsToAdd) {
        final DateTimeFormatter format = DateTimeFormat.forPattern(dateFormat);
        final DateTime newDate = format.parseDateTime(date);
        return newDate.plusMonths(monthsToAdd).toString(dateFormat);
    }

    /**
     * Method to parse date in Audit log date format.
     *
     * @param date
     * @param isOldDate
     * @return String
     */
    public static String getAuditLogDate(final String date, final boolean isOldDate) {
        if (date == null) {
            return null;
        }
        String dateModify = date;
        if (isOldDate) {
            dateModify = dateModify.replace(PelicanConstants.UTC_TIME_ZONE, "");
        } else {
            dateModify = dateModify + " 00:00:00";
        }

        return DateTimeUtils.changeDateFormat(dateModify, PelicanConstants.DB_DATE_FORMAT,
            PelicanConstants.AUDIT_LOG_DATE_FORMAT);
    }

    public static String getCurrentTimeStamp() {
        final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(PelicanConstants.UTC_TIME_ZONE));
        return String.valueOf(cal.getTimeInMillis() / 1000);
    }

    /**
     * This is a method to return the date as String for eg: June 3, 2018
     *
     * @param inputDate
     * @return
     * @throws ParseException
     */
    public static String getDateAsText(final String inputDate) throws ParseException {

        final SimpleDateFormat formatter = new SimpleDateFormat(PelicanConstants.DATE_TIME_FORMAT_WITH_OUT_TIME_ZONE);
        final Date date = formatter.parse(inputDate);

        final DateFormat dateFormat = new SimpleDateFormat(PelicanConstants.DATE_FORMAT_IN_TEXT);

        return (dateFormat.format(date));

    }
}
