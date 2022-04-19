package com.autodesk.bsm.pelican.ui.generic;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.PelicanEnvironment;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Class which handles Selenium Grid and Remote location, returns Selenium Webdriver for the hub
 *
 * @author mandas.
 */
public class SeleniumWebdriver extends BaseTestData {
    private static final Logger LOGGER = LoggerFactory.getLogger(SeleniumWebdriver.class.getSimpleName());
    private static String version = null;
    private WebDriver driver = null;

    protected WebDriver getDriver() {
        return driver;
    }

    /**
     * This method runs after each class which inherits SeleniumWebdriver It closes the browser.
     */
    @AfterClass(alwaysRun = true)
    protected synchronized void closeDriverAfterClass() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * This Method would run after every test method in this class and output the Result of the test case.
     *
     * @param result would contain the result of the test method
     */
    @Override
    @AfterMethod(alwaysRun = true)
    protected synchronized void printEndTestMethodLog(final ITestResult result) {

        Boolean screenShotFlag = false;
        if (result.getStatus() == ITestResult.FAILURE) {
            if (PelicanEnvironment.getValueFromProperty("isScreenShotRequired") == null) {
                if (getEnvironmentVariables().getScreenshotFlag()) {
                    screenShotFlag = true;
                }
            } else {
                if (PelicanEnvironment.getValueFromProperty("isScreenShotRequired")
                    .equalsIgnoreCase(PelicanConstants.TRUE)) {
                    screenShotFlag = true;
                }
            }
        }

        if (screenShotFlag) {
            Util.takeScreenshot(driver, result);
        }

        Util.processResultAndOutput(result);
    }

    /**
     * This is a method to initialize a web driver object
     *
     * @param environmentVariables .
     */
    protected void initializeDriver(final EnvironmentVariables environmentVariables) {

        DesiredCapabilities capabilities;

        String platform;
        if (System.getProperty("os.name").contains("Windows")) {
            platform = "Windows";
        } else if (System.getProperty("os.name").contains("Mac")) {
            platform = "Mac";
        } else {
            // assuming its linux for now
            platform = System.getProperty("os.name");
        }
        final String browser = environmentVariables.getBrowser();

        LOGGER.info("Platform (OS): " + platform);
        LOGGER.info("Browser : " + browser);
        LOGGER.info("Version : " + version);

        final String remoteWebDriverurl = getRemoteWebDriverurl();

        if (remoteWebDriverurl != null) {

            LOGGER.info("RemoteWebDriverurl: " + remoteWebDriverurl);

            /*
             * Function sets DesiredCapabilities and driver with respect to Platform, Browser
             */
            capabilities = setBrowserSettingsForRemoteDriver(platform, browser, version);

            try {
                driver = new RemoteWebDriver(new URL(remoteWebDriverurl), capabilities);
            } catch (final MalformedURLException ex) {
                LOGGER.error("Remote Web Driver : ", ex.getCause());
            }
        } else {
            LOGGER.info("RemoteWebDriver is Null");
            capabilities = DesiredCapabilities.chrome();
            final ChromeOptions chromeOpt = new ChromeOptions();
            final Map<String, Object> chromePrefs = new HashMap<>();
            String downloadFilepath;

            if (("Windows").equalsIgnoreCase(platform)) {
                downloadFilepath = environmentVariables.getDownloadPathForWindows();
                System.setProperty("webdriver.chrome.driver", PelicanConstants.TOOLS_PATH + "chromedriver.exe");
            } else if (("Mac").equalsIgnoreCase(platform)) {
                downloadFilepath = System.getProperty("user.home") + environmentVariables.getDownloadPathForMac();
                System.setProperty("webdriver.chrome.driver", PelicanConstants.TOOLS_PATH + "chromedrivermac");
            } else {
                LOGGER.info("Environment is " + platform);
                downloadFilepath = System.getProperty("user.home");
                chromeOpt.addArguments("--headless");
            }
            chromePrefs.put("profile.default_content_settings.popups", 0);
            chromePrefs.put("download.default_directory", downloadFilepath);
            chromePrefs.put("Browser.setDownloadBehavior", "allow");
            chromeOpt.setExperimentalOption("prefs", chromePrefs);

            if (browser.equalsIgnoreCase("NA")) {
                LOGGER.info("Running GUI tests in headless mode!!!");
                chromeOpt.addArguments("--headless", "--no-sandbox", "window-size=1024,768", "--disable-gpu");
            }
            capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOpt);
            driver = new ChromeDriver(capabilities);
        }
    }

    /**
     * This is a method which returns the web driver url
     *
     * @return String.
     */
    private static String getRemoteWebDriverurl() {
        String remoteWebDriverurl = null;

        if (PelicanEnvironment.getValueFromProperty(PelicanConstants.REMOTE_WEBDRIVER) != null) {
            remoteWebDriverurl = PelicanEnvironment.getValueFromProperty(PelicanConstants.REMOTE_WEBDRIVER);
        } else {
            LOGGER.info("RemoteWebDriverurl is NULL, so running Test in Local machine !!!");
        }

        return remoteWebDriverurl;
    }

    private static DesiredCapabilities setBrowserSettingsForRemoteDriver(final String platform, final String browser,
        final String version) {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setVersion(version);

        if (("mac").equalsIgnoreCase(platform)) {
            caps.setPlatform(org.openqa.selenium.Platform.MAC);
            LOGGER.info("Using properties of MAC");

            if (browser.equalsIgnoreCase("chrome")) {
                caps = DesiredCapabilities.chrome();
                LOGGER.info("Using properties of Chrome");
            } else if (browser.equalsIgnoreCase("firefox")) {
                caps = DesiredCapabilities.firefox();
                LOGGER.info("Using properties of Firefox");
            } else if (browser.equalsIgnoreCase("safari")) {
                caps = DesiredCapabilities.safari();
                LOGGER.info("Using properties of Safari");
            }
        } else if (("windows").equalsIgnoreCase(platform)) {
            caps.setPlatform(org.openqa.selenium.Platform.WINDOWS);
            LOGGER.info("Using properties of Windows");

            if (browser.equalsIgnoreCase("ie") || browser.equalsIgnoreCase("iexplorer")) {
                caps.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
                caps = DesiredCapabilities.internetExplorer();
                LOGGER.info("Using properties of Internet Explorer");
            } else if (browser.equalsIgnoreCase("chrome")) {
                caps = DesiredCapabilities.chrome();
                LOGGER.info("Using properties of Chrome");
            } else if (browser.equalsIgnoreCase("firefox")) {
                caps = DesiredCapabilities.firefox();
                LOGGER.info("Using properties of Firefox");
            }
        } else if (("unix").equalsIgnoreCase(platform)) {
            caps.setPlatform(org.openqa.selenium.Platform.UNIX);
            LOGGER.info("Using properties of UNIX");

            if (browser.equalsIgnoreCase("ie") || browser.equalsIgnoreCase("iexplorer")) {
                caps.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
                caps = DesiredCapabilities.internetExplorer();
                LOGGER.info("Using properties of Internet Explorer");
            } else if (browser.equalsIgnoreCase("chrome")) {
                caps = DesiredCapabilities.chrome();
                LOGGER.info("Using properties of Chrome");
            } else if (browser.equalsIgnoreCase("firefox")) {
                caps = DesiredCapabilities.firefox();
                LOGGER.info("Using properties of Firefox");
            } else if (browser.equalsIgnoreCase("safari")) {
                caps = DesiredCapabilities.safari();
                LOGGER.info("Using properties of Safari");
            }
        }
        return caps;
    }
}
