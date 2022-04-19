package com.autodesk.bsm.pelican.ui.pages.descriptors;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.enums.DescriptorEntityTypes;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FindDescriptorDefinitionsPage extends GenericDetails {

    public FindDescriptorDefinitionsPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "entityType")
    private WebElement entityTypeSelector;

    @FindBy(id = "input-groupName")
    private WebElement groupNameInput;

    @FindBy(id = "form-findForm")
    private WebElement findDescriptorsForm;

    @FindBy(id = "appFamilyId")
    private WebElement appFamilySelector;

    @FindBy(id = "submit")
    private WebElement submitButton;

    @FindBy(id = "find-results")
    private WebElement findResultsGrid;

    private static final Logger LOGGER = LoggerFactory.getLogger(FindDescriptorDefinitionsPage.class.getSimpleName());

    public void navigateToPage() {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.DESCRIPTOR_DEFINITION.getForm() + "/"
            + AdminPages.FIND.getForm();
        try {
            getDriver().get(url);
        } catch (final UnhandledAlertException uae) {
            LOGGER.info("Unhandled Alert Exception occupied, skipped the alert and moving on");
            driver.switchTo().alert().dismiss();
        }
        LOGGER.info("Navigated to find description definitions : " + url);
    }

    /**
     * method to do the page action(Find Descriptors defined) for selecting entity type, group and click submit
     *
     * @param entity - If null, Won't do anything in the entity type selector
     * @param groupName - if empty, group text box will be left empty
     */
    public void findDescriptors(final DescriptorEntityTypes entity, final String groupName) {

        navigateToPage();

        if (entity != null) {
            selectEntityType(entity);
        }
        if (!groupName.isEmpty()) {
            setGroupName(groupName);
        }
        // Clicking on submit button will result in the descriptors results according to search criteria
        submit(TimeConstants.ONE_SEC);
    }

    private void selectEntityType(final DescriptorEntityTypes entityType) {
        getActions().select(entityTypeSelector, entityType.getEntity());
        LOGGER.info("Selected Entity Type" + entityType.getEntity());
    }

    private void setGroupName(final String groupName) {
        getActions().setText(groupNameInput, groupName);
        LOGGER.info("Set group name to :  " + groupName);
    }

    private void clickUpdateResults() {
        getActions().click(submitButton);
    }
}
