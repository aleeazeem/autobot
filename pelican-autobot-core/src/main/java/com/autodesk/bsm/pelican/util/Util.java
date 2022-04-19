package com.autodesk.bsm.pelican.util;

import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Generic utility methods
 *
 * @author Jeffrey Blaze
 * @version 1.0.0
 */
public final class Util {

    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class.getSimpleName());

    // This version print warnings instead of failing the test when it catches
    // file I/O exceptions
    // This version fails the test when it catches file I/O exceptions
    public static Properties loadPropertiesFile(final String filename) {
        final Properties properties = new Properties();
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filename);
            properties.load(inputStream);
        } catch (final IOException e) {
            LOGGER.info("Error while loading properties from '" + filename + "': " + e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (final IOException e) {
                    // failTest("Error in finally block while loading properties
                    // from '"
                    // + filename + "': " + e.getMessage());
                    LOGGER.info(
                        "Error in finally block while loading properties from '" + filename + "': " + e.getMessage());
                }
            }
        }

        return properties;
    }

    public static boolean fileExists(final String filePath) {
        final File file = new File(filePath);

        return file.exists();
    }

    public static String readFileReturnString(final String filename) {

        FileInputStream inputStream = null;
        String myString = "";
        try {
            inputStream = new FileInputStream(filename);
            myString = IOUtils.toString(inputStream, "UTF-8");

        } catch (final Exception e) {
            e.printStackTrace();

        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return myString;
    }

    /**
     * Get string representation of a list
     *
     * @param params list
     * @return String representation of the params (Ex: "param1,param2")
     */
    public static String getString(final List<String> params) {
        final StringBuilder paramsStrBuilder = new StringBuilder();
        for (final String param : params) {
            paramsStrBuilder.append(param).append(",");
        }
        final String paramsStr = paramsStrBuilder.toString();
        // Remove the extra comma added at the end.
        return paramsStr.substring(0, paramsStr.length() - 1);
    }

    /**
     * Get Test Root directory i.e., pelican-autobot-core directory path so that test data, properties and driver files
     * are accessible
     *
     * @return rootPath
     */
    public static String getTestRootDir() {

        String rootPath = null;
        try {
            rootPath = Paths.get(System.getProperty("user.dir") + "/../pelican-autobot-core").toRealPath().toString();
            LOGGER.info("User test directory is : " + rootPath);
        } catch (final IOException e) {
            LOGGER.error("ERROR finding the root directory");
        }
        return rootPath;
    }

    /**
     * method to return a string after taking out the bracket part in it e.g: return only abc from the string abc(xyz).
     *
     * @param string - String from which the Baracket part need to be out
     * @return remainingString - reamining String after the bracket part is excluded
     **/
    public static String excludeBracketPart(final String string) {
        final int startIndex = string.indexOf("(");
        final int endIndex = string.indexOf(")");
        return string.replace(string.substring(startIndex, endIndex + 1), "");
    }

    /*
     * Method which takes the directory path and deletes all the file names which starts with a pattern name
     *
     * @param: DirectoryPath - String
     *
     * @param: FileStartName - String
     */
    public void deleteAllFilesWithSpecificFileName(final String directoryPath, final String fileName) {
        final File directory = new File(directoryPath);
        final File[] files = directory.listFiles();
        if (files != null) {
            for (final File f : files) {
                if (f.getName().startsWith(fileName)) {
                    f.delete();
                }
            }
        }
    }

    /**
     * Method to return basic-auth authorization header value
     *
     * @return authorization header value
     */
    public static String getBasicAuthHeaderValue(final String username, final String password) {
        final String usernamePasswordStr = username + ":" + password;
        final String base64EncodedToken = new String(Base64.encodeBase64(usernamePasswordStr.getBytes()));
        return "Basic " + base64EncodedToken;
    }

    /**
     * Based on the Test Method Result, printing the output the Result
     *
     * @param testMethodResult is a ITestResult Object
     */
    public static void processResultAndOutput(final ITestResult testMethodResult) {
        String testcaseResult = null;

        if (testMethodResult.getStatus() == ITestResult.SUCCESS) {
            testcaseResult = Status.PASSED.toString();
        } else if (testMethodResult.getStatus() == ITestResult.FAILURE) {
            testcaseResult = Status.FAILED.toString();
        } else if (testMethodResult.getStatus() == ITestResult.SKIP) {
            testcaseResult = Status.SKIPPED.toString();
        }

        final String message = String.format("====== Test Result for %s : %s ======",
            testMethodResult.getMethod().getMethodName(), testcaseResult);

        LOGGER.info("");
        LOGGER.info(StringUtils.repeat("=", message.length()));
        LOGGER.info(message);
        LOGGER.info(StringUtils.repeat("=", message.length()));
    }

    /**
     * Code to capture the snapshot of the Webdriver current page
     *
     * @param driver , which is an Object of selenium webdriver
     * @param result , ITestResult contains the test method name which is required to name the screenshot (*.jpeg)
     */
    public static void takeScreenshot(final WebDriver driver, final ITestResult result) {

        try {
            File screenshotFile;
            final long timeStamp = (new Date()).getTime();
            final String filenName = result.getMethod().getMethodName() + timeStamp + ".jpeg";
            screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

            FileUtils.copyFile(screenshotFile, new File(filenName));

            LOGGER.info("Screenshot taken: " + filenName);
        } catch (final Exception e) {
            e.printStackTrace();
        }

    }

    public static void waitInSeconds(final long timeOutInSecs) {

        final long timeOutInMilliSecs = timeOutInSecs * 1000;
        threadSleep(timeOutInMilliSecs);
    }

    /**
     * Sleeps for specified ms
     */
    private static void threadSleep(final long timeOutInMilliSecs) {
        try {
            LOGGER.info("Sleeping for " + (timeOutInMilliSecs * .001) + " Seconds. ");
            Thread.sleep(timeOutInMilliSecs);
        } catch (final InterruptedException e) {
            e.printStackTrace();

        }
    }

    /**
     * This method scrolls the page vertically
     *
     * @param driver
     * @param verticalScroll
     * @param horizontalScroll
     */
    public static void scroll(final WebDriver driver, final String verticalScroll, final String horizontalScroll) {
        // Scrolling down the page
        final JavascriptExecutor jse = (JavascriptExecutor) driver;
        jse.executeScript("window.scrollBy(" + horizontalScroll + "," + verticalScroll + ")", "");
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
    }

    /**
     * This is a method to add a particular number of days to a specific date
     *
     * @param date
     * @param numberOfDays
     * @return Date as String
     */
    public static String addDaysToAParticularDate(final String date, final int numberOfDays) {
        final DateTimeFormatter formatter = DateTimeFormat.forPattern(PelicanConstants.DATE_FORMAT_WITH_SLASH);
        final DateTime dateTime = formatter.parseDateTime(date);

        final DateTime addDays = dateTime.plusDays(numberOfDays);

        return addDays.toString(formatter);
    }

    /**
     * This is a method to return the number of billing days for a subscription from tempest DB.
     *
     * @param subscriptionId
     * @param pelicanplatform
     * @param environmentVariables
     * @return Integer - count of billing days for a subscription.
     */
    public static Integer getCountOfBillingDays(final String subscriptionId, final PelicanPlatform resource,
        final EnvironmentVariables environmentVariables) {

        final Subscription subscription = resource.subscription().getById(subscriptionId);
        final String nextBillingDate = subscription.getNextBillingDate();
        final DateTimeFormatter formatter = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss zzz");
        final DateTime nextBillDate = formatter.parseDateTime(nextBillingDate);

        return getDaysBetween(getPreviousBillingDate(subscription), nextBillDate);
    }

    /**
     * This is the method to return number of days between start and end
     *
     * @param start
     * @param end
     * @return Integer
     */
    public static Integer getDaysBetween(final DateTime start, final DateTime end) {
        return Days.daysBetween(start.withTimeAtStartOfDay(), end.withTimeAtStartOfDay()).getDays();
    }

    /**
     * This is the method to return the previous billing date
     *
     * @param subscription
     * @return DateTime
     */
    public static DateTime getPreviousBillingDate(final Subscription subscription) {
        return getPreviousBillingDate(subscription.getNextBillingDate(),
            subscription.getBillingOption().getBillingPeriod().getType(),
            subscription.getBillingOption().getBillingPeriod().getCount());
    }

    /**
     * This is the method which computes the previous billing date based on the next billing date
     *
     * @param originPointInTime
     * @param type
     * @param count
     * @return DateTime
     */
    public static DateTime getPreviousBillingDate(final String originPointInTime, final String type, final int count) {
        return computeDateMinusThisPeriod(originPointInTime, type, count);
    }

    /**
     * This is the method which computes the days based on the next billing date and billing type and count
     *
     * @param startingDate
     * @param type
     * @param count
     * @return DateTime
     */
    public static DateTime computeDateMinusThisPeriod(final String startingDate, final String type, final int count) {
        final DateTimeFormatter formatter = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss zzz");
        final DateTime startDate = formatter.parseDateTime(startingDate);
        return startDate.minus(getJodaTimePeriod(type, count));
    }

    /**
     * This is the method which returns the Period based on billing type and count
     *
     * @param type
     * @param count
     * @return Period
     */
    public static org.joda.time.Period getJodaTimePeriod(final String type, final int count) {
        switch (type) {
            case "DAY":
                return org.joda.time.Period.days(count);
            case "WEEK":
                return org.joda.time.Period.weeks(count);
            case "MONTH":
                return org.joda.time.Period.months(count);
            case "YEAR":
                return org.joda.time.Period.years(count);
            case "LIFETIME":
                return null;
            default:
                return null;
        }
    }

    /**
     * Get XML Document from String.
     *
     * @return XML Document
     */
    public static Document loadXMLFromString(final String xmlString) {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document document = null;

        try {
            builder = factory.newDocumentBuilder();
            document = builder.parse(new InputSource(new StringReader(xmlString)));
        } catch (final Exception e) {
            e.printStackTrace();
        }

        return document;
    }
}
