package com.autodesk.bsm.pelican.ui.pages.descriptors;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.Descriptor;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page object Pattern represent Add Descriptors Page. Access via Descriptors | Add
 *
 * @author Sunitha
 */
public class AddDescriptorPage extends GenericDetails {

    @FindBy(id = "entityType")
    private WebElement entityTypeSelect;

    @FindBy(id = "group")
    private WebElement groupSelect;

    @FindBy(id = "input-fieldName")
    private WebElement fieldNameInput;

    @FindBy(id = "input-apiName")
    private WebElement apiNameInput;

    @FindBy(id = "localized")
    private WebElement localizedSelect;

    @FindBy(id = "input-maxLength")
    private WebElement maximumLengthInput;

    @FindBy(className = "submit")
    private WebElement submitButton;

    @FindBy(id = "input-groupName")
    private WebElement groupNameInput;

    @FindBy(name = "Delete")
    private WebElement deleteButton;

    @FindBy(id = "confirm-btn")
    private WebElement confirmButton;

    @FindBy(xpath = "(//*[text()='Next'])[1]")
    private WebElement next;

    private static final Logger LOGGER = LoggerFactory.getLogger(AddDescriptorPage.class.getSimpleName());

    public AddDescriptorPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    /**
     * Navigate to add descriptors page.
     */
    private void navigateToAddDescriptors() {
        final String url =
            getEnvironment().getAdminUrl() + "/" + AdminPages.DESCRIPTOR_DEFINITION.getForm() + "/addForm";
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }

    /**
     * Navigate to find descriptors.
     */
    public void navigateToFindDescriptors() {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.DESCRIPTOR_DEFINITION.getForm() + "/find";
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }

    /**
     * Method to delete the descriptors.
     */
    public void deleteDescriptors(final String fieldName, final String groupName, final String entityType) {
        selectEntity(entityType);
        final String xpath = "//td[text()='" + fieldName + "']";
        System.out.println("xpath to find descriptor : " + xpath);
        groupNameInput.sendKeys(groupName);
        submitButton.click();
        Util.waitInSeconds(TimeConstants.TWO_SEC);
        boolean flag = true;
        while (flag) {
            if (doesElementExist(xpath)) {
                flag = false;
                getDriver().findElement(By.xpath(xpath)).click();
                deleteButton.click();
                confirmButton.click();
            } else {
                /*
                 * Xpath for pagination last page.
                 */
                final String paginationLastPage = "(//span[text()='Next'])[1]";
                if (!doesElementExist(paginationLastPage)) {
                    next.click();
                    flag = true;
                }
            }
        }
    }

    /**
     * Method to select Entity while defining Descriptors
     */
    private void selectEntity(final String entity) {
        LOGGER.info("Select entity '" + entity + "'");
        getActions().select(entityTypeSelect, entity);
    }

    /**
     * Method to select Group while defining Descriptors
     */
    private void selectGroup(final String group) {
        LOGGER.info("Select group '" + group + "'");
        getActions().select(groupSelect, group);
    }

    /**
     * Method to set Field Name while defining Descriptors
     */
    private void setFieldName(final String fieldName) {
        LOGGER.info("Set field Name '" + fieldName + "'");
        getActions().setText(fieldNameInput, fieldName);
    }

    /**
     * Method to set Api Name while defining Descriptors
     */
    private void setApiName(final String apiName) {
        LOGGER.info("Set Api Name '" + apiName + "'");
        getActions().setText(this.apiNameInput, apiName);
    }

    /**
     * Method to select Localized while defining Descriptors
     */
    private void selectLocalized(final String localized) {
        LOGGER.info("Select localized '" + localized + "'");
        getActions().select(this.localizedSelect, localized);
    }

    /**
     * method to set Maximum Length while defining Descriptors
     */
    private void setMaxLength(final String maxLength) {
        LOGGER.info("Set maximum length '" + maxLength + "'");
        getActions().setText(maximumLengthInput, maxLength);
    }

    /**
     * Method to Add Descriptors
     */
    public void addDescriptor(final Descriptor descriptors) {
        LOGGER.info("Add descriptors");
        navigateToAddDescriptors();
        selectApplicationFamily(descriptors.getAppFamily());
        selectEntity(descriptors.getEntity().getEntity());
        selectGroup(descriptors.getGroupName());
        setFieldName(descriptors.getFieldName());
        setApiName(descriptors.getApiName());
        selectLocalized(descriptors.getLocalized());
        setMaxLength(descriptors.getMaxLength());
        submit(TimeConstants.THREE_SEC);
    }

    /**
     * Method returns grid details
     */
    public GenericGrid getGrid() {
        return super.getPage(GenericGrid.class);
    }

    /**
     * Method to check if Web Element exists.
     *
     * @return boolean
     */
    private boolean doesElementExist(final String xpathExpression) {
        boolean found;
        try {
            getDriver().findElement(By.xpath(xpathExpression)).isDisplayed();
            found = true;
        } catch (final NoSuchElementException e) {
            // If the Element is not present, Selenium throws NoSuchElementException. Catching the same to
            // return false
            found = false;
        }
        return found;
    }

}
