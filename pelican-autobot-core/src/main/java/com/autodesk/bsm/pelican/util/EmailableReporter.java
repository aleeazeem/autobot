package com.autodesk.bsm.pelican.util;

import org.testng.IReporter;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.log4testng.Logger;
import org.testng.xml.XmlSuite;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class has methods to consume testng out put xml and creates readable report
 *
 * @Sumant testng out put xml
 * @return test case reports in html format
 */

public class EmailableReporter implements IReporter {

    private static final Logger L = Logger.getLogger(EmailableReporter.class);
    private PrintWriter out = null;

    /**
     * Creates summary of the run
     */
    public void generateReport(final List<XmlSuite> xml, final List<ISuite> suites, final String outDir) {

        try {
            L.info("***** Generating Emailable Report ******");

            // Separate reports for each team
            PrintWriter testngPxReportOut;
            PrintWriter testngPyReportOut;
            PrintWriter testngPzReportOut;
            // Report which contains all modules
            PrintWriter testngPelicanReportOut;
            final String pxTeamReportName = "Regression_Test_Px_Report.html";
            final String pyTeamReportName = "Regression_Test_Py_Report.html";
            final String pzTeamReportName = "Regression_Test_Pz_Report.html";
            final String pelicanTeamReportName = "Regression_Test_Pelican_Report.html";

            try {
                testngPxReportOut = createWriter(outDir, pxTeamReportName);
                testngPyReportOut = createWriter(outDir, pyTeamReportName);
                testngPzReportOut = createWriter(outDir, pzTeamReportName);
                testngPelicanReportOut = createWriter(outDir, pelicanTeamReportName);
            } catch (final IOException e) {
                L.error("output file", e);
                return;
            }
            startHtml(testngPxReportOut);
            startHtml(testngPyReportOut);
            startHtml(testngPzReportOut);
            startHtml(testngPelicanReportOut);

            L.info("********** testPlan ********** " + "\nPath for the reports: " + outDir);

            // Note: Please add any new modules in these arrays
            final String[] pxModules =
                { "Purchase Order", "Finance Report", "Payment Profile", "Triggers Api", "Show Hives", "Actor" };
            final String[] pyModules = { "Item", "Subscription Plan", "Offerings", "User Offerings", "Bic Release",
                    "Store", "Promotion", "Price Quotes", "Item Type", "Product Line", "Features",
                    "Descriptor Definition", "Store Type", "Licensing Model", "Currency", "Basic Offerings" };
            final String[] pzModules = { "Item Instance", "Subscription", "User", "Events", "Entitlement" };

            // Combine all 3 arrays to make Pelican module array
            final int pxLength = pxModules.length;
            final int pyLength = pyModules.length;
            final int pzLength = pzModules.length;

            final String[] pelicanModules = new String[pxLength + pyLength + pzLength];
            System.arraycopy(pxModules, 0, pelicanModules, 0, pxLength);
            System.arraycopy(pyModules, 0, pelicanModules, pxLength, pyLength);
            System.arraycopy(pzModules, 0, pelicanModules, pxLength + pyLength, pzLength);

            generateSuiteSummaryReport(suites, testngPxReportOut, pxTeamReportName, Arrays.asList(pxModules));
            generateSuiteSummaryReport(suites, testngPyReportOut, pyTeamReportName, Arrays.asList(pyModules));
            generateSuiteSummaryReport(suites, testngPzReportOut, pzTeamReportName, Arrays.asList(pzModules));
            generateSuiteSummaryReport(suites, testngPelicanReportOut, pelicanTeamReportName,
                Arrays.asList(pelicanModules));

            endHtml(testngPxReportOut);
            endHtml(testngPyReportOut);
            endHtml(testngPzReportOut);
            endHtml(testngPelicanReportOut);
        } catch (final Exception e) {
            L.info("********* Exception occurred in generateReport **********" + e.getMessage());
            e.printStackTrace();

        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Method to create new print writer for each module.
     *
     * @param outdir
     * @param reportName
     * @return
     * @throws IOException
     */
    private PrintWriter createWriter(final String outdir, final String reportName) throws IOException {
        return new PrintWriter(new BufferedWriter(new FileWriter(new File(outdir, reportName))));
    }

    /**
     * Method to create result summary, this adds background color, status indication of testcase and time taken.
     *
     * @param tests
     * @param style
     * @param testngReportOut
     */
    private void resultSummary(final IResultMap tests, final String style, final PrintWriter testngReportOut) {
        try {
            String bgFontColor = "";

            if (style.equalsIgnoreCase("comp_failed")) {
                bgFontColor = " background-color: #FF0000; color: #FFFFFF;";
            } else if (style.equalsIgnoreCase("comp_passed")) {
                bgFontColor = " background-color: #04B404; color: #FFFFFF;";
            } else if (style.equalsIgnoreCase("comp_skipped")) {
                bgFontColor = " background-color: #F2F5A9; color: #000000;";
            }

            for (final ITestNGMethod method : getMethodSet(tests)) {
                String description = "";
                final String methodName = method.getMethodName();
                Throwable exception = null;
                int testStatusCode;
                String testStatus = "";
                long totalTime = 0;
                for (final ITestResult testResult : tests.getResults(method)) {
                    testStatusCode = testResult.getStatus();
                    if (testStatusCode == 1) {
                        testStatus = "Passed";
                    } else if (testStatusCode == 2) {
                        testStatus = "Failed";
                    } else if (testStatusCode == 3) {
                        testStatus = "Skipped";
                    }
                    exception = testResult.getThrowable();

                    final long end = testResult.getEndMillis();
                    final long start = testResult.getStartMillis();
                    totalTime = (end - start);
                }

                if (exception != null) {
                    description = exception.getLocalizedMessage();
                }

                final NumberFormat formatter = new DecimalFormat("#,##0.0");

                if (!(testStatus.equalsIgnoreCase("Passed"))) {
                    testngReportOut.println("<tr style='background-color: #585858;  " + bgFontColor + "\'>"
                        + "<td style='width: 450px;word-break: break-all'>" + methodName + "</td>"
                        + "<td style='width: 70px'>" + formatter.format(totalTime / 1000.) + "</td>"
                        + "<td style='width: 40px'>" + testStatus + "</td> "
                        + "<td style='width: 745px;word-break: break-all'>" + description + "</td>" + "</tr>");
                }
            }
        } catch (final Exception e) {
            L.info("********* Exception occurred in resultSummary--" + e.getCause());
        }
    }

    /**
     * @param tests
     * @return
     */
    private Collection<ITestNGMethod> getMethodSet(final IResultMap tests) {
        final Set<ITestNGMethod> r = new TreeSet<>(new TestSorter<>());
        r.addAll(tests.getAllMethods());
        return r;
    }

    /**
     * Generate the summary for each report and respective modules.
     *
     * @param suites
     * @param reportOut
     * @param reportName
     * @param modules
     */
    private void generateSuiteSummaryReport(final List<ISuite> suites, final PrintWriter reportOut,
        final String reportName, final List<String> modules) {
        try {

            // Calculate and draw tests summary
            calculateAndPrintTestCounts(suites, reportOut, modules, printInfoMessage(reportName, reportOut));

            // Create lists for green and red modules
            final List<String> greenModules = new ArrayList<>();
            final List<String> redModules = new ArrayList<>();
            final Map<String, ISuiteResult> results = suites.get(0).getResults();

            // This goes to each module to make sure, it belongs to the correct team module
            for (final ISuiteResult r : results.values()) {
                final ITestContext overview = r.getTestContext();
                final String moduleName = overview.getName();

                // Logic to add to red modules.
                if (modules.contains(moduleName)
                    && overview.getFailedTests().size() + overview.getSkippedTests().size() > 0) {
                    redModules.add(moduleName);
                } else {
                    // If there are no failures in the module, then add to the green module
                    greenModules.add(moduleName);
                }

                // If there are red modules in given modules, then print the failed & skipped testcases
                if (redModules.contains(moduleName) && doModulesIntersect(modules, redModules)) {
                    reportOut.println("<table style='border-width: 2px'>");
                    reportOut.println("<tr>");
                    reportOut.println("<td>" + moduleName + "</td>");
                    reportOut.println("<tr>");
                    resultSummary(overview.getFailedConfigurations(), "comp_failed", reportOut);
                    resultSummary(overview.getFailedTests(), "comp_failed", reportOut);
                    resultSummary(overview.getSkippedConfigurations(), "comp_skipped", reportOut);
                    resultSummary(overview.getSkippedTests(), "comp_skipped", reportOut);
                }
                if (doModulesIntersect(modules, greenModules)) {
                    resultSummary(overview.getPassedTests(), "comp_passed", reportOut);
                }

                reportOut.println("</table>");
            }

            // If there are no red modules in modules, then print "happy" message
            if (!doModulesIntersect(modules, redModules)) {
                reportOut.println("<td>Hurray!!!!! - No Test case Failures :-)</td>");
            }

            // Print green modules in the report
            if (greenModules.size() > 0) {
                reportOut.println("<table style='border-width: 2px'>");
                reportOut.println("<td> </td>");
                reportOut.println("<tr>");
                reportOut.println(
                    "<table style='border-width: 2px'>" + "<tr style='background-color: #585858; color: #FFFFFF;'>"
                        + "<td style='width: 450px'>Modules with NO Failures: </td>");

                for (final String module : greenModules) {
                    if (modules.contains(module)) {
                        reportOut.println(
                            "<tr style='background-color: #585858;   background-color: #04B404; " + "color: #FFFFFF;\'>"
                                + "<td style='width: 450px;word-break: break-all'>" + module + "</td>");
                    }
                }
            }

            reportOut.println("<tr>");
            reportOut.println("</table>");
        } catch (final Exception e) {
            L.info("********* Exception occurred in generateSuiteSummaryReport--" + e.getCause());
            e.printStackTrace();
        }
    }

    /**
     * Helper method to create a logic of modules intersect with redmodule.
     *
     * @param modules
     * @param redModules
     * @return
     */
    private boolean doModulesIntersect(final List<String> modules, final List<String> redModules) {

        for (final String redModule : redModules) {
            if (modules.contains(redModule)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method to print the info message to teams.
     *
     * @param reportName
     * @param reportOut
     * @return
     */
    private String printInfoMessage(final String reportName, final PrintWriter reportOut) {

        String team;
        switch (reportName) {
            case "Regression_Test_Px_Report.html":
                team = "PX Team";
                break;
            case "Regression_Test_Py_Report.html":
                team = "PY Team";
                break;
            case "Regression_Test_Pz_Report.html":
                team = "PZ Team";
                break;
            default:
                team = "Pelican Team";
                break;
        }

        reportOut.println("<table width='100%' cellspacing='1' cellpadding='5'>"
            + "<tr><td style='background-color: #ffffff;'>Hello " + team + ", </td></tr>"
            + "<tr><td style='background-color: #ffffff;'>This is a Automated test report from Jenkins." + " </td></tr>"
            + "<tr><td style='background-color: #ffffff;'><b>Environment :"
            + (System.getProperty("environmentType") != null ? System.getProperty("environmentType") : "NULL")
            + " </b></td></tr>" + "<tr><td style='background-color: #ffffff;'><b>For more information <u><a href='"
            + System.getProperty("BUILD_URL") + "'> click here</a></u></b></td></tr>" + "</table>");

        return team;
    }

    /**
     * This method is to get over all test count, with pass, fail and skip.
     *
     * @param suites
     * @param reportOut
     * @param modules
     * @param team
     */
    private void calculateAndPrintTestCounts(final List<ISuite> suites, final PrintWriter reportOut,
        final List<String> modules, final String team) {

        int passedTestCount = 0;
        int failedTestCount = 0;
        int skippedTestCount = 0;
        int passedTestCountForTeam = 0;
        int failedTestCountForTeam = 0;
        int skippedTestCountForTeam = 0;

        for (final ISuite chartSuite : suites) {
            final Map<String, ISuiteResult> results = chartSuite.getResults();
            for (final String name : results.keySet()) {
                final ISuiteResult suiteResult = results.get(name);
                final ITestContext testContext = suiteResult.getTestContext();
                if (modules.contains(name)) {
                    passedTestCountForTeam += testContext.getPassedTests().size();
                    failedTestCountForTeam += testContext.getFailedTests().size();
                    skippedTestCountForTeam += testContext.getSkippedTests().size();
                }
                passedTestCount += testContext.getPassedTests().size();
                failedTestCount += testContext.getFailedTests().size();
                skippedTestCount += testContext.getSkippedTests().size();
            }
        }
        reportOut.println("<h1>" + (System.getProperty("TestType") != null ? System.getProperty("TestType") : "")
            + " Test Result Summary for : " + team + "</h1>");
        drawResultTable(reportOut, passedTestCountForTeam, failedTestCountForTeam, skippedTestCountForTeam);

        reportOut.println("<br>");

        reportOut.println("<table width=\"100%\" cellspacing=\"1\" cellpadding=\"5\">");
        drawSuiteSummary(reportOut, passedTestCount, failedTestCount, skippedTestCount);
    }

    /**
     * Starts HTML stream
     */
    private void startHtml(final PrintWriter out) {
        out.println(
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">");
        out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        out.println("<head>");
        out.println("<title>TestNG:  Unit Test</title>");

        out.println("</head>");
        out.println("<body style=\"font-family: Arial,sans-serif;font-size: 12px;\">");
    }

    /**
     * Finishes HTML stream
     */
    private void endHtml(final PrintWriter out) {
        out.println("</body></html>");
        out.flush();
        out.close();
    }

    /**
     * Arranges methods by classname and method name
     */
    private class TestSorter<T extends ITestNGMethod> implements Comparator {

        /**
         * Arranges methods by classname and method name
         */
        public int compare(final Object o1, final Object o2) {
            int r = ((T) o1).getTestClass().getName().compareTo(((T) o2).getTestClass().getName());
            if (r == 0) {
                r = ((T) o1).getMethodName().compareTo(((T) o2).getMethodName());
            }
            return r;
        }
    }

    private void drawResultTable(final PrintWriter out, final int passedCount, final int failedCount,
        final int skippedCount) {
        try {

            final int total = (passedCount + failedCount + skippedCount);
            final DecimalFormat df = new DecimalFormat("#");

            out.println("<table class=\"resultSummary\">");
            out.println("<tbody>");
            out.println("<tr>");
            out.println("<td></td><td></td>");
            out.println("<td>Percentage</td>");
            out.println("<td>Test Cases</td>");
            out.println("</tr>");
            out.println("<tr>");
            out.println("<td style=\"background-color: #04B404; padding: 8px; width: 8px\"><font color=\"#FFFFFF\">"
                + " </font></td>");
            out.println("<td>Passed</td>");
            out.println("<td>:" + df.format((passedCount * 100.0f) / total) + " %" + "</td>");
            out.println("<td>" + passedCount + "</td>");
            out.println("</tr>");
            out.println("<tr>");
            out.println("<td style=\"background-color: red; padding: 8px; width: 8px\"><font color=\"#FFFFFF\">"
                + " </font></td>");
            out.println("    <td>Failed</td>");
            out.println("<td>:" + df.format((failedCount * 100.0f) / total) + " %" + "</td>");
            out.println("<td>" + failedCount + "</td>");

            out.println("</tr>");
            out.println("<tr>");
            out.println("    <td style=\"background-color: yellow; padding: 8px; width: 8px\">"
                + "<font color=\"#FFFFFF\"> </font></td>");
            out.println("    <td>Skipped</td>");
            out.println("<td>:" + df.format((skippedCount * 100.0f) / total) + " %" + "</td>");
            out.println("<td>" + skippedCount + "</td>");

            out.println("</tr>");
            out.println("</tbody>");
            out.println("</table>");
        } catch (final Exception e) {
            L.info("********* Exception occurred in drawResultTable--" + e.getMessage());
            e.printStackTrace();
        }

    }

    private void drawSuiteSummary(final PrintWriter testngReportOut, final int passedTestCount,
        final int failedTestCount, final int skippedTestCount) {
        try {
            final int total = passedTestCount + failedTestCount + skippedTestCount;
            testngReportOut.println("<table style=\"border-top: 1px solid black; padding-top: 20px;padding-bottom: "
                + "5px;clear: left;\">");
            testngReportOut.println("<tbody>");
            testngReportOut.println("<tr>");

            testngReportOut.println("<td><b>Overall Pelican Result</b></td>");
            testngReportOut.println("<td   style=\"text-align: center;background-color: #04B404;width: 50px;\">"
                + "<b><font style=\"color: white;\"> " + passedTestCount + " </font></b></td>");

            testngReportOut.println("<td  style=\"text-align: center;background-color: red;width: 50px;\"><b>"
                + "<font style=\"color: white;\"> " + failedTestCount + " </font></b></td>");

            testngReportOut.println("<td  style=\"text-align: center;background-color: yellow;width: 50px;\"><b> "
                + skippedTestCount + " </b></td>");

            testngReportOut.println("<td class=\"component_header\" style=\"text-align: center;background-color: "
                + "#eaf0f7;width: 50px;;\"><b> # " + total + "</b></td>");
            final long passedPer = (passedTestCount * 100) / total;
            testngReportOut.println("<td  style=\"text-align: center;background-color: #eaf0f7;width: 50px;;\"><b>"
                + passedPer + "%</b></td>");

            testngReportOut.println("</tr>");
            testngReportOut.println("</tbody>");
            testngReportOut.println("</table>");

            if (total != passedTestCount) {
                testngReportOut.println("<table style='border-width: 2px'>");
                testngReportOut.println("<tr style='background-color: #585858; color: #FFFFFF;'>"
                    + "<td style='width: 450px'> Test Name</td>" + "<td style='width: 70px'> Duration(S)</td>"
                    + "<td style='width: 40px'>Status</td>" + "<td style='width: 745px'> Reason for Failure</td>"
                    + "</tr>");
                testngReportOut.println("</table>");
            }
        } catch (final Exception e) {
            L.info("********* Exception occurred in drawSuiteSummary--" + e.getMessage());
            e.printStackTrace();
        }
    }
}
