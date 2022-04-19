package com.autodesk.bsm.pelican.ui.pages.descriptors;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.enums.DescriptorEntityTypes;
import com.autodesk.bsm.pelican.enums.DescriptorGroups;
import com.autodesk.bsm.pelican.ui.entities.Descriptor;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the page object class for Add Descriptor Definitions Page is available in AdminTool - >Applications >
 * Descriptors > Add Descriptor
 *
 * @author kishor
 */
public class AddDescriptorDefinitionsPage extends GenericDetails {

    public AddDescriptorDefinitionsPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    /**
     * Groups available in Descriptor definitions
     *
     * @author kishor
     */

    @FindBy(id = "entityType")
    private WebElement entityTypeSelector;

    @FindBy(id = "group")
    private WebElement groupNameSelector;

    @FindBy(id = "appFamilyId")
    private WebElement appFamilySelector;

    @FindBy(id = "submit")
    private WebElement submitButton;

    @FindBy(id = "input-fieldName")
    private WebElement fieldNameInput;

    @FindBy(id = "input-apiName")
    private WebElement apiNameInput;

    @FindBy(id = "localized")
    private WebElement localizedSelector;

    @FindBy(id = "input-maxLength")
    private WebElement maxLengthInput;

    @FindBy(id = "delete-action-form")
    private WebElement deleteDescriptorForm;

    @FindBy(name = "Delete")
    private WebElement deleteDescriptorButton;

    @FindBy(id = "groupName")
    private WebElement additionalGroupName;

    @FindBy(id = "confirm-btn")
    private WebElement confirmDeleteButton;

    @FindBy(xpath = ".//*[@id='field-entityType']/span[@class='error-message']")
    private WebElement entityRequiredErrorMessage;

    @FindBy(xpath = ".//*[@id='field-fieldName']/span[@class='error-message']")
    private WebElement fieldNameRequiredErrorMessage;

    @FindBy(xpath = ".//*[@id='field-apiName']/span[@class='error-message']")
    private WebElement apiNameRequiredErrorMessage;

    private static final Logger LOGGER = LoggerFactory.getLogger(AddDescriptorDefinitionsPage.class.getSimpleName());

    /**
     * Navigate to the Add descriptor page by using add URL
     */
    private void navigateToPage() {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.DESCRIPTOR_DEFINITION.getForm() + "/"
            + AdminPages.ADD_FORM.getForm();
        getDriver().get(url);
        LOGGER.info("Navigated to add description definitions : " + url);
    }

    /**
     * This utility method will add desciptor definition through the adminToolPage UI It uses the values from getter
     * methods in Descriptor POJO class to fill the add page form So proper values should be set before in the test
     * class calling this method using the setters
     */
    public void addDescriptors(final Descriptor descriptorObj) {
        final String appFamily = descriptorObj.getAppFamily();
        final DescriptorEntityTypes entity = descriptorObj.getEntity();
        final String groupName = descriptorObj.getGroupName();
        final String fieldName = descriptorObj.getFieldName();
        final String apiName = descriptorObj.getApiName();
        final String localized = descriptorObj.getLocalized();
        final String maxLength = descriptorObj.getMaxLength();
        final String otherGroupName = descriptorObj.getOtherGroupName();
        // navigate to the add page
        navigateToPage();
        // Select the entity only if input is not null
        if (entity != null) {
            selectEntityType(entity);
        }
        // Select the appFamily only if input is not null
        if (appFamily != null && !appFamily.isEmpty()) {
            setApplicationFamily(appFamily);
        }

        // Select the groupName only if input is not null
        if (groupName != null && !groupName.isEmpty()) {
            setGroupName(groupName);
            // In case the group selected by the user is "Other", another text
            // box will be displayed to fill the group name
            if (groupName.equalsIgnoreCase(DescriptorGroups.Other.toString()) && otherGroupName != null) {
                setAdditionalGroupName(otherGroupName);
            }
        }
        // Enter the fieldName only if input is not null
        if (fieldName != null && !fieldName.isEmpty()) {
            setFieldName(fieldName);
        }
        // Enter the apiName only if input is not null
        if (apiName != null && !apiName.isEmpty()) {
            setApiName(apiName);
        }
        // Select the localized only if input is not null
        if (localized != null && !localized.isEmpty()) {
            selectLocalized(localized);
        }
        // Set the entity only if input is not null
        if (maxLength != null && !maxLength.isEmpty()) {
            setMaximumLength(maxLength);
        }

        submit(TimeConstants.ONE_SEC);
    }

    /**
     * This method checks and return the error message displayed in the webpage when fieldName is left empty while
     * adding descriptor
     *
     * @return errorMessage String
     */
    public String getFieldNameRequiredErrorMessage() {
        String errorMessage = "";
        try {
            errorMessage = fieldNameRequiredErrorMessage.getText();
        } catch (final NoSuchElementException ex) {
            LOGGER.error("No Error message displayed for FieldName required scenario" + ex.getMessage());
        }
        return errorMessage;
    }

    /**
     * This method checks and return the error message displayed in the webpage when apiName is left empty while adding
     * descriptor
     *
     * @return errorMessage String
     */
    public String getApiNameRequiredErrorMessage() {
        String errorMessage = "";
        try {
            errorMessage = apiNameRequiredErrorMessage.getText();
        } catch (final NoSuchElementException ex) {
            LOGGER.error("No Error message displayed for ApiName required scenario" + ex.getMessage());
        }
        return errorMessage;
    }

    /**
     * This method checks and return the error message displayed in the webpage when entity type is left empty while
     * adding descriptor
     *
     * @return errorMessage String
     */
    public String getEntityRequiredErrorMessage() {
        String errorMessage = "";
        try {
            errorMessage = entityRequiredErrorMessage.getText();
        } catch (final NoSuchElementException ex) {
            LOGGER.error("No Error message displayed for Entity required scenario" + ex.getMessage());
        }
        return errorMessage;
    }

    /**
     * This is a utility method to delete the descriptors created by the automation script
     */
    public void deleteDescriptor(final String descriptorId) {
        navigateToDescriptorDetail(descriptorId);
        // deleteDescriptorForm.submit(); //another way of deleting is
        // submitting the deleteForm
        getActions().click(deleteDescriptorButton);
        Util.waitInSeconds(TimeConstants.ONE_SEC);
        // Click on the confirmation popup
        confirmDeleteDescriptorDefinition();
    }

    private void selectEntityType(final DescriptorEntityTypes entityType) {
        getActions().select(entityTypeSelector, entityType.getEntity());
        LOGGER.info("Selected Entity Type" + entityType.getEntity());
    }

    private void setGroupName(final String groupName) {
        getActions().selectByValue(groupNameSelector, groupName);
        LOGGER.info("Set group name to :  " + groupName);
    }

    private void setApplicationFamily(final String applicationFamily) {
        getActions().select(appFamilySelector, applicationFamily);
        LOGGER.info("Set application family to :  " + applicationFamily);
    }

    private void setFieldName(final String fieldName) {
        getActions().setText(fieldNameInput, fieldName);
        LOGGER.info("Set field Name value to : " + fieldName);
    }

    private void setApiName(final String apiName) {
        getActions().setText(apiNameInput, apiName);
        LOGGER.info("Set api Name value to : " + apiName);
    }

    private void selectLocalized(final String localized) {
        getActions().select(localizedSelector, localized);
    }

    private void setMaximumLength(final String maxLength) {
        getActions().setText(maxLengthInput, maxLength);
        LOGGER.info("Set maxLength value to : " + maxLength);
    }

    private void navigateToDescriptorDetail(final String id) {
        final String url =
            getEnvironment().getAdminUrl() + "/" + AdminPages.DESCRIPTOR_DEFINITION.getForm() + "/show?id=" + id;
        getDriver().get(url);
        LOGGER.info("Navigated to Description Detail : " + url);
    }

    private void confirmDeleteDescriptorDefinition() {
        getActions().click(confirmDeleteButton);
        LOGGER.info("Clicked on Confirm Delete Button");
    }

    private void setAdditionalGroupName(final String otherGroupName) {
        getActions().setText(additionalGroupName, otherGroupName);
        LOGGER.info("Set Additional Group Name : " + otherGroupName);
    }
}
