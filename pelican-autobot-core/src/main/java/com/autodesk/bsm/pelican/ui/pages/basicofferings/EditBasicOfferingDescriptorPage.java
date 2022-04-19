package com.autodesk.bsm.pelican.ui.pages.basicofferings;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This page represents edit Localized/Non-Localized descriptors page. This is usually accessed from the subscription
 * plan page.
 *
 * @author Muhammad
 */
public class EditBasicOfferingDescriptorPage extends GenericDetails {

    public EditBasicOfferingDescriptorPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);
        // TODO Auto-generated constructor stub
    }

    @FindBy(id = "input-estore.AUTO_TEST_DESCRIPTOR_API")
    private WebElement nonLocalizedDescriptorsInput;

    @FindBy(id = "input-ipp.AUTO_TEST_LOCAL_DESCRIPTOR_API")
    private WebElement localizedDescriptorsInput;

    private static final Logger LOGGER = LoggerFactory.getLogger(EditBasicOfferingDescriptorPage.class.getSimpleName());

    /**
     * Method to edit Localized Descriptor value
     */
    public void editLocalizedDescriptor(final String descriptorValue) {
        localizedDescriptorsInput.clear();
        localizedDescriptorsInput.sendKeys(descriptorValue);
        LOGGER.info("Auto Test Local Descriptor: " + descriptorValue);
    }

    /**
     * Method to edit Non-Localized Descriptor value
     */
    public void editNonLocalizedDescriptor(final String descriptorValue) {
        nonLocalizedDescriptorsInput.clear();
        nonLocalizedDescriptorsInput.sendKeys(descriptorValue);
        LOGGER.info("Auto Test Descriptor: " + descriptorValue);
    }

    /**
     * Method to click on update button descriptor and return a detail page of basic offering
     *
     * @return basic offering detail page
     */
    public BasicOfferingDetailPage updateDescriptor() {
        submit(TimeConstants.ONE_SEC);
        return super.getPage(BasicOfferingDetailPage.class);
    }

    /**
     * Method to click on cancel button descriptor and return a detail page of basic offering without any change
     *
     * @return basic offering detail page
     */
    public BasicOfferingDetailPage cancelDescriptor() {
        cancel();
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        return super.getPage(BasicOfferingDetailPage.class);
    }
}
