package com.autodesk.bsm.pelican.ui.pages.stores;

import com.autodesk.bsm.pelican.enums.Country;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * This is the page class for the Store Details Page
 *
 * @author t_joshv
 */
public class StoreDetailPage extends GenericDetails {

    public StoreDetailPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(css = "#form-addPriceListForm .submit")
    private WebElement addPriceListButton;

    @FindBy(css = "#form-assignCountryForm .submit")
    private WebElement assignCountryButton;

    @FindBy(id = "assignCountry-country")
    private WebElement assignCountrySelect;

    @FindBy(id = "assignCountry-priceListId")
    private WebElement assignPriceListSelect;

    @FindBy(id = "confirm-btn")
    private WebElement confirmButton;

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreDetailPage.class.getSimpleName());

    /**
     * click on the edit button on the store detail page.
     *
     * @return Edit store page
     */
    public EditStorePage editStore() {
        LOGGER.info("Click on the edit button on the store detail page");
        editButton.click();

        return super.getPage(EditStorePage.class);
    }

    /**
     * This method returns id of a store
     *
     * @return id of a store
     */
    public String getId() {
        final String id = getValueByField("ID");
        LOGGER.info("ID : " + id);
        return id;
    }

    /**
     * This method returns external key of a store
     *
     * @return external key of a store
     */
    public String getExternalKey() {
        final String externalKey = getValueByField("External Key");
        LOGGER.info("External Key : " + externalKey);
        return externalKey;
    }

    /**
     * This method returns name of a store
     *
     * @return Name of a store
     */
    public String getName() {
        final String name = getValueByField("Name");
        LOGGER.info("Name : " + name);
        return name;
    }

    /**
     * This method returns Type of a store
     *
     * @return Type of a store
     */
    public String getType() {
        final String type = getValueByField("Type");
        LOGGER.info("Type : " + type);
        return type;
    }

    /**
     * This method returns Status of a store
     *
     * @return Status of a store
     */
    public String getStatus() {
        final String status = getValueByField("Status");
        LOGGER.info("Status : " + status);
        return status;
    }

    /**
     * This method returns Send Tax Invoices Email
     *
     * @return Status of Send Tax Invoices Email
     */
    public String getSendTaxInvoicesEmails() {
        final String sendTaxInvoicesEmails = getValueByField("Send Tax Invoices Emails");
        LOGGER.info("Send Tax Invoices Emails : " + sendTaxInvoicesEmails);
        return sendTaxInvoicesEmails;
    }

    /**
     * This method returns Vat Percent
     *
     * @return Vat Percent
     */
    public String getVatPercent() {
        final String vatPercent = getValueByField("Vat Percent");
        LOGGER.info("Vat Percent : " + vatPercent);
        return vatPercent;
    }

    /**
     * This method returns Sold to CSN
     *
     * @return Sold to CSN
     */
    public String getSoldToCsn() {
        final String soldToCsn = getValueByField("Sold To CSN");
        LOGGER.info("Sold To CSN : " + soldToCsn);
        return soldToCsn;
    }

    /**
     * This method returns created date of a store
     *
     * @return created date of a store
     */
    public String getCreated() {
        final String created = getValueByField("Created");
        LOGGER.info("Created : " + created);
        return created;
    }

    /**
     * This method returns last modified date of a store
     *
     * @return last modified date of a store
     */
    public String getLastModified() {
        final String lastModified = getValueByField("Last Modified");
        LOGGER.info("Last Modified : " + lastModified);
        return lastModified;
    }

    /**
     * Navigate to add price list form
     *
     * @return price list page object
     */
    public AddPriceListPage addPriceList() {
        LOGGER.info("Click add price list button");
        addPriceListButton.click();
        return super.getPage(AddPriceListPage.class);
    }

    /**
     * Select price list by row. Start at row #0
     *
     * @return PriceList Detail Page.
     */
    public PriceListDetailPage selectPriceList(final int row) {

        getDriver().findElement(By.xpath(".//*[@id='bd']/div[2]/div[2]/div/table/tbody/tr[" + (row + 1) + "]")).click();
        return super.getPage(PriceListDetailPage.class);
    }

    /**
     * This method will assign country to price list on store detail page.
     *
     * @param country
     * @param priceListName
     */
    public void assignCountryToPriceList(final Country country, final String priceListName) {
        LOGGER.info("Select " + country.getLongDescription() + " from dropdown");
        getActions().select(assignCountrySelect, country.getLongDescription());

        LOGGER.info("Select " + priceListName + " from dropdown");
        getActions().select(assignPriceListSelect, priceListName);

        LOGGER.info("Click assign country");
        assignCountryButton.click();

    }

    /**
     * Delete country by row. Start at row #0
     *
     * @param row number
     */
    public void deleteCountry(final int row) {

        final String css = "#countries .find-results";
        final List<WebElement> totalElements = getDriver().findElements(By.cssSelector(css + ".results > tbody > tr"));
        final int totalRows = totalElements != null ? totalElements.size() : 0;

        if (row > totalRows) {
            throw new RuntimeException("Requested row, " + row + ", is greater than the total rows of " + totalRows);
        }

        if (row < 1) {
            if (totalElements != null) {
                totalElements.get(row).findElement(By.className("delete-button")).click();
            }
        } else {
            if (totalElements != null) {
                totalElements.get(row - 1).findElement(By.className("delete-button")).click();
            }
        }

        final String mainWindow = getDriver().getWindowHandle();
        final Set<String> windowHandles = getDriver().getWindowHandles();
        getDriver().switchTo().window(windowHandles.toArray()[windowHandles.toArray().length - 1].toString());
        clickOnConfirmButton();
        getDriver().switchTo().window(mainWindow);

    }

    /**
     * Get Price List Grid.
     *
     * @return
     */
    public PriceListGrid getPriceListGrid() {
        final String css = ".detail-sections > .find-results";
        final PriceListGrid grid = new PriceListGrid(css, getDriver(), getEnvironment());
        PageFactory.initElements(getDriver(), grid);
        return grid;
    }

    /**
     * Get Country Grid.
     *
     * @return
     */
    public CountryGrid getCountryGrid() {
        final String css = "#countries .find-results";
        final CountryGrid grid = new CountryGrid(css, getDriver(), getEnvironment());
        PageFactory.initElements(getDriver(), grid);
        return grid;
    }

    /**
     * This method deletes a Store.
     *
     * @return true or false depending upon the pop up confirmation.
     */
    public boolean deleteStoreAndConfirm() {
        LOGGER.info("Delete Store");
        deleteAndConfirm();
        return getHeader().equalsIgnoreCase("Store Deleted");
    }

    /**
     * Does country exist in the country assigned list
     *
     * @return true if it exists. Otherwise false
     */
    public boolean doesCountryExist(final Country country) {
        boolean found = false;
        final List<WebElement> optionWebElements = new Select(assignCountrySelect).getOptions();
        for (final WebElement option : optionWebElements) {
            if (option.getText().equals(country.getLongDescription())) {
                found = true;
                break;
            }
        }
        return found;
    }

    /**
     * This method will click on the confirm button in the pop up
     */
    private void clickOnConfirmButton() {
        confirmButton.click();
    }

}
