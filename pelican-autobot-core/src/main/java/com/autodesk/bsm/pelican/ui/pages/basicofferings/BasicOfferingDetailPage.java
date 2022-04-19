package com.autodesk.bsm.pelican.ui.pages.basicofferings;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: t_mohag
 */
public class BasicOfferingDetailPage extends GenericDetails {

    public BasicOfferingDetailPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(xpath = "//button[text()='Edit Localized Descriptors']")
    private WebElement editLocalizedDescriptors;

    @FindBy(xpath = "//button[text()='Edit Non-Localized Descriptors']")
    private WebElement editNonLocalizedDescriptors;

    @FindBy(xpath = "//select")
    private WebElement localeSelect;

    @FindBy(xpath = "//td[text()='Yes']/preceding-sibling::th")
    private List<WebElement> yesDescriptors;

    @FindBy(xpath = "//td[text()='No']/preceding-sibling::th")
    private List<WebElement> noDescriptors;

    @FindBy(xpath = "//input[contains(@name,'ipp')]")
    private WebElement ippDescriptors;

    @FindBy(id = "input-estore.AUTO_TEST_DESCRIPTOR_API")
    private WebElement eStoreDescriptorsInput;

    @FindBy(xpath = ".//*[@id='prices-table[0]']/tbody/tr/td[5]")
    private WebElement effectiveStartDate;

    @FindBy(xpath = ".//*[@id='prices-table[0]']/tbody/tr/td[6]")
    private WebElement effectiveEndDate;

    @FindBy(xpath = "//*[@id='bd']/div[2]/div[1]/div/div/div/form[1]/span/button")
    private WebElement editBasicOfferingButton;

    @FindBy(xpath = "//td[@class='first']")
    private WebElement firstPriceId;

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicOfferingDetailPage.class.getSimpleName());

    private EditBasicOfferingDescriptorPage editBasicOfferingDescriptorPage;

    /**
     * This method returns id of Basic Offering
     *
     * @return id
     */
    public String getId() {
        final String id = getValueByField("ID");
        LOGGER.info("Basic Offering Id : " + id);
        return id;
    }

    /**
     * This method returns name of Basic Offering
     *
     * @return name
     */
    public String getName() {
        final String name = getValueByField("Name");
        LOGGER.info("Basic Offering Name : " + name);
        return name;
    }

    /**
     * This method returns external key of Basic Offering
     *
     * @return External key
     */
    public String getExternalKey() {
        final String externalKey = getValueByField("External Key");
        LOGGER.info("Basic Offering External key : " + externalKey);
        return externalKey;
    }

    /**
     * This method returns Offering type of Basic Offering
     *
     * @return offering type
     */
    public String getOfferingType() {
        final String offeringType = getValueByField("Offering Type");
        LOGGER.info("Basic Offering Offering Type : " + offeringType);
        return offeringType;
    }

    /**
     * This method returns product line of Basic Offering
     *
     * @return product line
     */
    public String getProductLine() {
        final String productLine = getValueByField("Product Line");
        LOGGER.info("Basic Offering Product Line : " + productLine);
        return productLine;
    }

    /**
     * This method returns media type of Basic Offering
     *
     * @return media type
     */
    public String getMediaType() {
        final String mediaType = getValueByField("Media Type");
        LOGGER.info("Basic Offering Media Type : " + mediaType);
        return mediaType;
    }

    /**
     * This method returns Usage Type of Basic Offering
     *
     * @return Usage Type
     */
    public String getUsageType() {
        final String usageType = getValueByField("Usage Type");
        LOGGER.info("Basic Offering Usage Type : " + usageType);
        return usageType;
    }

    /**
     * This method returns currency of Basic Offering
     *
     * @return Currency
     */
    public String getCurrency() {
        final String currency = getValueByField("Currency");
        LOGGER.info("Basic Offering Currency : " + currency);
        return currency;
    }

    /**
     * This method returns Amount of Basic Offering
     *
     * @return Amount
     */
    public String getAmount() {
        final String amount = getValueByField("Amount");
        LOGGER.info("Basic Offering Amount : " + amount);
        return amount;
    }

    /**
     * This method returns Offering Detail of Basic Offering
     *
     * @return Offering Detail
     */
    public String getOfferingDetail() {
        final String offeringDetail = getValueByField("Offering Detail");
        LOGGER.info("Basic Offering Offering Detail : " + offeringDetail);
        return offeringDetail;
    }

    /**
     * This method returns status of Basic Offering
     *
     * @return status
     */
    public String getStatus() {
        final String status = getValueByField("Status");
        LOGGER.info("Basic Offering Status : " + status);
        return status;
    }

    /**
     * This method returns Basic Offering First Price.
     */
    public String getFirstPriceId() {
        final String priceId = firstPriceId.getText();
        LOGGER.info("Price Id : " + priceId);
        return priceId;
    }

    /**
     * Method to navigate to Basic Offering Page
     */
    public void navigateToBasicOfferingPage(final String id) {
        final String url =
            getEnvironment().getAdminUrl() + "/" + AdminPages.BASIC_OFFERINGS.getForm() + "/show?id=" + id;
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
        Util.waitInSeconds(TimeConstants.ONE_SEC);
    }

    /**
     * Method to edit Localized Descriptor values
     */
    public void editLocalizedDescriptorValue(final String descriptorValue) {
        editLocalizedDescriptors.click();
        editBasicOfferingDescriptorPage = super.getPage(EditBasicOfferingDescriptorPage.class);
        editBasicOfferingDescriptorPage.editLocalizedDescriptor(descriptorValue);
        editBasicOfferingDescriptorPage.updateDescriptor();
    }

    /**
     * Method to edit Non Localized Descriptor values
     */
    public void editNonLocalizedDescriptorValue(final String descriptorValue) {
        editNonLocalizedDescriptors.click();
        editBasicOfferingDescriptorPage = super.getPage(EditBasicOfferingDescriptorPage.class);
        editBasicOfferingDescriptorPage.editNonLocalizedDescriptor(descriptorValue);
        editBasicOfferingDescriptorPage.updateDescriptor();
    }

    /**
     * Method to return list of all Localized Descriptors
     */
    public List<String> getAllLocalizedDescriptorsNames() {
        return getListOfText(yesDescriptors);
    }

    /**
     * Method to return list of all Non Localized Descriptors
     */
    public List<String> getAllNonLocalizedDescriptorsNames() {
        return getListOfText(noDescriptors);
    }

    /**
     * Method to return list of titles for all descriptors
     *
     * @return textList(list of descriptor names list)
     */
    private List<String> getListOfText(final List<WebElement> webElementList) {
        final List<String> textList = new ArrayList<>();
        for (final WebElement element : webElementList) {
            if (element.getText() != null && element.getText().length() > 0) {
                textList.add(element.getText());
            }
        }
        return textList;
    }

    /**
     * Method to get effective start date of a price in an offer
     *
     * @return effectiveStartDate
     */
    public String getEffectiveStartDateOfPriceInAnOffer() {
        return effectiveStartDate.getText();
    }

    /**
     * Method to get effective end date of a price in an offer
     *
     * @return effectiveEndDate
     */
    public String getEffectiveEndDateOfPriceInAnOffer() {
        return effectiveEndDate.getText();
    }

    /**
     * This method deletes a Basic Offering
     *
     * @return true or false depending upon the pop up confirmation
     */
    public boolean deleteBasicOffering() {
        LOGGER.info("Delete Offering Detail");
        deleteAndConfirm();
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        return getHeader().equalsIgnoreCase("Offering Deleted");
    }

    /**
     * Click on the edit basic offering button
     */
    public EditBasicOfferingPage clickOnEditBasicOfferingButton() {
        editBasicOfferingButton.click();
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        return super.getPage(EditBasicOfferingPage.class);
    }

    /**
     * Method to get href value attached to See Audit Trail Link.
     *
     * @return actual href value linked to See Audit Trail link.
     */
    public String getAuditTrailLink() {
        return auditTrailLink.getAttribute("href");
    }

    /**
     * Method to generate Audit Trail Link for provided Entity.
     *
     * @param entityId
     * @return expected href value
     */
    public String generateAuditTrailLink(final String entityId) {
        final String suffixUrl = "?entityType=BasicOffering&entityId=" + entityId;
        return getEnvironment().getAdminUrl() + "/" + AdminPages.REPORTS.getForm() + "/"
            + AdminPages.AUDIT_LOG.getForm() + suffixUrl;
    }
}
