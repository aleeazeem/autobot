package com.autodesk.bsm.pelican.ui.pages.bicrelease;

import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.ui.entities.BicRelease;
import com.autodesk.bsm.pelican.ui.entities.BicReleaseAdvSearch;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.util.DateTimeUtils;
import com.autodesk.bsm.pelican.util.Wait;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class BicReleasePage extends GenericDetails {

    public BicReleasePage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "appId")
    private WebElement applicationSelect;

    @FindBy(id = "productLineId")
    private WebElement productLineSelect;

    @FindBy(id = "appFamilyId")
    private WebElement applicationFamilySelect;

    @FindBy(id = "input-downloadRelease")
    private WebElement downloadReleaseInput;

    @FindBy(id = "subPlanProductLineId")
    private WebElement subPlanProductLineSelect;

    @FindBy(id = "downloadProductLineId")
    private WebElement downloadProductLineSelect;

    @FindBy(id = "status")
    private WebElement statusSelect;

    @FindBy(id = "input-clicEnabled")
    private WebElement clicCheckbox;

    @FindBy(id = "input-legacySKU")
    private WebElement legacySkuInput;

    @FindBy(id = "input-fcsDate")
    private WebElement fcsDateInput;

    @FindBy(id = "input-ignoreEmail")
    private WebElement ignoreEmailCheckbox;

    // Find By ID search page
    @FindBy(css = ".form-group-labels > h3:nth-child(1)")
    private WebElement findByIdLink;

    // Advanced search page
    @FindBy(css = ".form-group-labels > h3:nth-child(2)")
    private WebElement advancedSearchLink;

    // Advanced Search only
    @FindBy(id = "input-fcsDateAfter")
    private WebElement searchFcsStartDateInput;

    // There are 2 class with submit - one for find by id tab and another in adv search tab. When adv
    // search is active, the first submit is disabled. So, need to find the submit on adv search tab.
    @FindBy(css = "#form-advancedFindForm .submit")
    private WebElement advSearchSubmitButton;

    @FindBy(id = "input-fcsDateBefore")
    private WebElement searchFcsEndDateInput;

    @FindBy(xpath = ".//*[@type='submit']")
    private WebElement findBicReleaseButton;

    @FindBy(xpath = ".//*[@name='download']")
    private WebElement downloadReleaseReportButton;

    @FindBy(id = "input-includeActive")
    private WebElement includeActiveReleasesCheckbox;

    @FindBy(id = "input-includeInactive")
    private WebElement includeInActiveReleasesCheckbox;

    private static final Logger LOGGER = LoggerFactory.getLogger(BicReleasePage.class.getSimpleName());

    /**
     * Navigate to bic release's add form
     */
    public void add() {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.BIC_RELEASE.getForm() + "/"
            + AdminPages.ADD_FORM.getForm();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }

    /**
     * Add a new bic release base on argument. Once it is created, set id, created by and created on
     *
     * @return newly created bic release
     */
    public BicRelease add(final BicRelease bicRelease) {

        // Add bic release
        add();
        LOGGER.info("Add Bic Release");
        setDownloadRelease(bicRelease.getDownloadRelease());
        selectSubPlanProductLine(bicRelease.getSubsPlanProductLine());
        selectDownlaodProductLine(bicRelease.getDownloadProductLine());
        selectStatus(bicRelease.getStatus());
        setClic(bicRelease.isClicEnabled());
        setLegacySku(bicRelease.getLegacySku());
        setFcsDate(bicRelease.getFcsDate());
        setIgnoreEmail(bicRelease.getIgnoreEmailNotification());
        submit();

        // After creation, id, created by and on
        final GenericDetails details = super.getPage(GenericDetails.class);
        return getBicReleaseDetails(details);
    }

    /**
     * Adding BIC Release fails and error message is thrown
     */
    public void addBicReleaseFail(final BicRelease bicRelease) {

        // Add bic release
        add();
        LOGGER.info("Add Bic Release");
        setDownloadRelease(bicRelease.getDownloadRelease());
        selectSubPlanProductLine(bicRelease.getSubsPlanProductLine());
        selectDownlaodProductLine(bicRelease.getDownloadProductLine());
        selectStatus(bicRelease.getStatus());
        setClic(bicRelease.isClicEnabled());
        setLegacySku(bicRelease.getLegacySku());
        setFcsDate(bicRelease.getFcsDate());
        setIgnoreEmail(bicRelease.getIgnoreEmailNotification());
        submit();
    }

    /**
     * Navigate to bic release edit form per id. Edit the item and return the new values. If id is not found, return
     * null
     */
    public BicRelease edit(final BicRelease bicRelease) {

        final String url =
            getEnvironment().getAdminUrl() + "/" + AdminPages.BIC_RELEASE.getForm() + "/show?id=" + bicRelease.getId();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);

        final GenericDetails detailsPage = super.getPage(GenericDetails.class);
        if (!detailsPage.isPageValid()) {
            return null;
        }

        // Click on the edit button so the details page is editable
        detailsPage.edit();

        LOGGER.info("Edit Bic Release Id#" + bicRelease.getId());
        setDownloadRelease(bicRelease.getDownloadRelease());
        selectSubPlanProductLine(bicRelease.getSubsPlanProductLine());
        selectDownlaodProductLine(bicRelease.getDownloadProductLine());
        selectStatus(bicRelease.getStatus());
        setClic(bicRelease.isClicEnabled());
        setLegacySku(bicRelease.getLegacySku());
        setFcsDate(bicRelease.getFcsDate());
        setIgnoreEmail(bicRelease.getIgnoreEmailNotification());
        submit();

        return getBicReleaseDetails(detailsPage);
    }

    /**
     * Navigate to Advanced Find form
     */
    public void advancedFind() {
        navigateToFindForm();
        advancedSearchLink.click();
    }

    public void selectApplication(final String value) {
        if (value != null) {
            LOGGER.info("Select '" + value + "' from application");
            getActions().select(applicationSelect, value);
        }
    }

    public String getApplication() {
        return new Select(applicationSelect).getFirstSelectedOption().getText();
    }

    public List<String> getApplicationOptions() {
        final List<String> options = new ArrayList<>();
        final List<WebElement> webElements = new Select(applicationSelect).getOptions();
        for (final WebElement webElement : webElements) {
            options.add(webElement.getText());
        }
        return options;
    }

    public void setApplicationFamily(final String applicationFamily) {
        if (applicationFamily != null) {
            LOGGER.info("Set Application Family to '" + applicationFamily + "'");
            getActions().setText(applicationFamilySelect, applicationFamily);
        }
    }

    private void setDownloadRelease(final String value) {
        if (value != null) {
            LOGGER.info("Set download release to '" + value + "'");
            getActions().setText(downloadReleaseInput, value);
        }
    }

    public String getDownloadRelease() {
        return downloadReleaseInput.getText();
    }

    private void selectSubPlanProductLine(final String value) {
        if (value != null && !value.equalsIgnoreCase("null (null)")) {
            Wait.dropdownPopulatedWithValue(driver, subPlanProductLineSelect, value);
            LOGGER.info("Select '" + value + "' from subscription plan product line");
            getActions().select(subPlanProductLineSelect, value);
        }
    }

    public String getSubPlanProductLine() {
        return new Select(subPlanProductLineSelect).getFirstSelectedOption().getText();
    }

    public List<String> getSubPlanProductLineOptions() {
        final List<String> options = new ArrayList<>();
        final List<WebElement> webElements = new Select(subPlanProductLineSelect).getOptions();
        for (final WebElement webElement : webElements) {
            options.add(webElement.getText().trim());
        }
        return options;
    }

    private void selectDownlaodProductLine(final String value) {
        if (value != null && !(value.equalsIgnoreCase("null (null)"))) {
            Wait.dropdownPopulatedWithValue(driver, downloadProductLineSelect, value);
            LOGGER.info("Select '" + value + "' from download product line");
            getActions().select(downloadProductLineSelect, value);
        }
    }

    public String getDownloadProductLine() {
        return new Select(downloadProductLineSelect).getFirstSelectedOption().getText();
    }

    public List<String> getDownloadProductLineOptions() {
        final List<String> options = new ArrayList<>();
        final List<WebElement> webElements = new Select(downloadProductLineSelect).getOptions();
        for (final WebElement webElement : webElements) {
            options.add(webElement.getText().trim());
        }
        return options;
    }

    public void selectStatus(final Status value) {
        if (value != null) {
            LOGGER.info("Select '" + value + "' from status");
            getActions().select(statusSelect, value.toString());
        }
    }

    public Status getStatus() {
        return Status.getByValue(new Select(statusSelect).getFirstSelectedOption().getText());
    }

    private void setClic(final boolean enable) {
        if (enable) {
            LOGGER.info("Enable clic");
            getActions().check(clicCheckbox);
        } else {
            LOGGER.info("Disable clic");
            getActions().uncheck(clicCheckbox);
        }
    }

    public boolean isClicEnabled() {
        return clicCheckbox.isSelected();
    }

    private void setLegacySku(final String value) {
        if (value != null) {
            LOGGER.info("Set legacy sku to '" + value + "'");
            getActions().setText(legacySkuInput, value);
        }
    }

    public String getLegacySku() {
        return legacySkuInput.getText();
    }

    private void setFcsDate(final String value) {
        if (value != null) {
            LOGGER.info("Set fcs date to '" + value + "'");
            getActions().setText(fcsDateInput, value);
        }
    }

    public String getFcsDate() {
        return fcsDateInput.getText();
    }

    private void setIgnoreEmail(final boolean enable) {
        if (enable) {
            LOGGER.info("Enable ignore email notification");
            getActions().check(ignoreEmailCheckbox);
        } else {
            LOGGER.info("Disable ignore email notification");
            getActions().uncheck(ignoreEmailCheckbox);
        }
    }

    public boolean isIgnoreEmailEnabled() {
        return ignoreEmailCheckbox.isSelected();
    }

    /*
     * Only available for Advanced Search
     */
    private void setSearchFcsStartDate(final String value) {
        if (value != null) {
            LOGGER.info("Set fcs start date to '" + value + "'");
            getActions().setText(searchFcsStartDateInput, value);
        }
    }

    public boolean doesFcsStartDateExist() {

        boolean found = false;
        try {
            if (searchFcsEndDateInput.isDisplayed()) {
                found = true;
            }
        } catch (final NoSuchElementException e) {
            // TODO: handle exception
        }
        return found;
    }

    /*
     * Only available for Advanced Search
     */
    private void setSearchFcsEndDate(final String value) {
        if (value != null) {
            LOGGER.info("Set fcs end date to '" + value + "'");
            getActions().setText(searchFcsEndDateInput, value);
        }
    }

    public boolean doesFcsEndDateExist() {

        boolean found = false;
        try {
            if (searchFcsEndDateInput.isDisplayed()) {
                found = true;
            }
        } catch (final NoSuchElementException e) {
            // TODO: handle exception
        }
        return found;
    }

    /**
     * Get the grid for the result set of find all with no id
     *
     * @return grid
     */
    public GenericGrid getGrid() {
        navigateToFindById();
        return super.getPage(GenericGrid.class);
    }

    /**
     * Get the grid's result set with find by id where id = null
     */
    public GenericGrid findAll() {
        return getGrid();
    }

    /**
     * Navigate to find by id page to search for Bic Release by id
     *
     * @param id Bic Release id
     * @return If id exists, return the details page. Otherwise null.
     */
    public GenericDetails findById(final String id) {
        navigateToFindForm();
        findByIdLink.click();
        setId(id);

        super.submit();
        final GenericDetails details = super.getPage(GenericDetails.class);
        if (details.isPageValid()) {
            LOGGER.info("Found id #" + id);
            return details;
        } else {
            LOGGER.info(id + " does not exist");
            return null;
        }
    }

    /**
     * Navigate to advanced search page to search for Bic Releases by criteria.
     *
     * @return Generic Grid with Bic Releases matching the criteria.
     */
    public GenericGrid findByCriteria(final BicReleaseAdvSearch bicRelease) {

        // Navigate to advanced search
        navigateToFindForm();
        advancedSearchLink.click();

        // Reusing the BicReleasePage to enter criteria. On the advanced page, not all fields appear in
        // the add/edit page.
        // For cleaning implementation, create another page object for advanced search?
        selectApplication(bicRelease.getApplication());
        selectSubPlanProductLine(bicRelease.getSubPlanProductLine() + " (" + bicRelease.getSubPlanProductLine() + ")");
        selectDownlaodProductLine(
            bicRelease.getDownloadProductLine() + " (" + bicRelease.getDownloadProductLine() + ")");
        setDownloadRelease(bicRelease.getDownloadRelease());
        selectStatus(bicRelease.getStatus());
        setLegacySku(bicRelease.getLegacySku());
        setSearchFcsStartDate(bicRelease.getFcsStartDate());
        setSearchFcsEndDate(bicRelease.getFcsEndDate());

        LOGGER.info("Click submit");
        advSearchSubmitButton.click();

        return super.getPage(GenericGrid.class);

    }

    /**
     * Navigate and show bic release in readonly by id
     */
    public GenericDetails getDetails(final String id) {

        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.BIC_RELEASE.getForm() + "/show?id=" + id;
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);

        return super.getPage(GenericDetails.class);
    }

    public void navigateToDownloadReport() {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.BIC_RELEASE.getForm() + "/"
            + AdminPages.DOWNLOAD_FORM.getForm();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
        Wait.pageLoads(driver);
    }

    public void clickOnDownloadReleaseReport() {
        getActions().click(downloadReleaseReportButton);
    }

    public boolean isIncludeActiveReleasesCheckboxPresent() {
        return includeActiveReleasesCheckbox.isDisplayed();
    }

    public boolean isIncludeInActiveReleasesCheckboxPresent() {
        return includeInActiveReleasesCheckbox.isDisplayed();
    }

    public boolean isproductLineFilterDropDownPresent() {
        return productLineSelect.isDisplayed();
    }

    public void clickOnIncludeActiveOfferingsFilter() {
        includeActiveReleasesCheckbox.click();
    }

    public void clickOnIncludeInActiveOfferingsFilter() {
        includeInActiveReleasesCheckbox.click();
    }

    public String getErrorMessageInDownloadReportForm() {
        navigateToDownloadReport();
        includeActiveReleasesCheckbox.click();
        clickOnDownloadReleaseReport();
        return errorMessageWebElement.getText();
    }

    public String getErrorMessageInAddForm() {
        Wait.elementVisibile(driver, errorMessageWebElement);
        return errorMessageWebElement.getText();
    }

    public void setProductLine(final String productLine) {
        productLineSelect.sendKeys(productLine);
    }

    private void navigateToFindById() {
        final String url =
            getEnvironment().getAdminUrl() + "/" + AdminPages.BIC_RELEASE.getForm() + "/find?findType=byId&id=";
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }

    private void navigateToFindForm() {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.BIC_RELEASE.getForm() + "/"
            + AdminPages.FIND_FORM.getForm();
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }

    // TODO: Create a class to get details
    private BicRelease getBicReleaseDetails(final GenericDetails details) {
        final BicRelease bicRelease = new BicRelease();

        bicRelease.setId(details.getValueByField("ID"));
        bicRelease.setApplicationFamily(details.getValueByField("Application Family"));
        bicRelease.setApplication(details.getValueByField("Application"));
        bicRelease.setSubsPlanProductLine(details.getValueByField("Subscription Plan Product Line"));
        bicRelease.setDownloadProductLine(details.getValueByField("Download Product Line"));
        bicRelease.setDownloadRelease(details.getValueByField("Download Release"));
        bicRelease.setStatus(Status.valueOf(details.getValueByField("Status")));
        if (details.getValueByField("Clic Enabled").equalsIgnoreCase("true")) {
            bicRelease.setClic(true);
        }
        bicRelease.setLegacySku(details.getValueByField("Legacy SKU"));
        bicRelease.setFcsDate(details.getValueByField("FCS Date"));

        if (details.getValueByField("Ignore E-mail Notifications").equalsIgnoreCase("true")) {
            bicRelease.setIgnoredEmailNotification(true);
        }

        bicRelease.setCreatedBy(details.getValueByField("Created By"));
        bicRelease.setCreatedOn(details.getValueByField("Created On"));
        bicRelease.setUpdatedBy(details.getValueByField("Updated By"));
        bicRelease.setUpdatedOn(details.getValueByField("Updated On"));
        return bicRelease;
    }

    /**
     * This is a method to create a bic release with sub plan prod line and release
     *
     * @param subscrtiptionPlanProdLine
     * @param Release
     *
     * @return BicRelease
     */
    public BicRelease createBicRelease(final String subscrtiptionPlanProdLine, final String release) {

        BicRelease bicRelease1 = new BicRelease();
        bicRelease1.setDownloadRelease(release);
        bicRelease1.setSubsPlanProductLine(subscrtiptionPlanProdLine);
        bicRelease1.setDownloadProductLine(subscrtiptionPlanProdLine);
        bicRelease1.setStatus(Status.ACTIVE);
        bicRelease1.setClic(true);
        bicRelease1.setFcsDate(DateTimeUtils.getCurrentDate());
        bicRelease1.setIgnoredEmailNotification(false);
        bicRelease1 = add(bicRelease1);

        return bicRelease1;
    }
}
