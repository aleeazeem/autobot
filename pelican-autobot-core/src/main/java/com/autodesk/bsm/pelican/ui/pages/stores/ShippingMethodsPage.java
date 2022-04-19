package com.autodesk.bsm.pelican.ui.pages.stores;

import com.autodesk.bsm.pelican.api.pojos.json.Price;
import com.autodesk.bsm.pelican.api.pojos.json.ShippingMethod;
import com.autodesk.bsm.pelican.api.pojos.json.ShippingMethod.Descriptor;
import com.autodesk.bsm.pelican.api.pojos.json.ShippingMethod.Destination;
import com.autodesk.bsm.pelican.enums.AdminPages;
import com.autodesk.bsm.pelican.enums.Country;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.enums.StateProvince;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ShippingMethodsPage extends GenericDetails {

    public ShippingMethodsPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(id = "currencyId")
    private WebElement currencySelector;

    @FindBy(id = "input-name")
    private WebElement nameInput;

    @FindBy(className = "cancel-btn")
    private WebElement cancelButton;

    @FindBy(name = "Edit")
    private WebElement editButton;

    @FindBy(css = "#field-externalKey .error-message")
    private WebElement extKeyErrorMessage;

    @FindBy(id = "input-forwardingAgentId")
    private WebElement agentIdInput;

    @FindBy(id = "input-amount")
    private WebElement priceInput;

    @FindBy(id = "country")
    private WebElement countrySelector;

    @FindBy(id = "stateProvince")
    private WebElement stateSelector;

    @FindBy(id = "addDestination")
    private WebElement addDestinationLink;

    @FindBy(id = "submit")
    private WebElement addShippingMethodButton;

    private static final Logger LOGGER = LoggerFactory.getLogger(ShippingMethodsPage.class.getSimpleName());

    protected void setName(final String name) {
        LOGGER.info("Set name to " + name);
        getActions().setText(nameInput, name);
    }

    private void setAgentId(final String agentId) {
        LOGGER.info("Set AgentId to " + agentId);
        getActions().setText(agentIdInput, agentId);
    }

    private void setPrice(final String price) {
        LOGGER.info("Set price to " + price);
        getActions().setText(priceInput, price);
    }

    // This method selects the Currency from dropdown using the value
    private void selectCurrency(final Currency currency) {
        if (currency != null) {
            LOGGER.info("Select " + currency + " from currency drop down");
            new Select(currencySelector).selectByValue("" + currency.getCode());
        }
    }

    // This method selects the Country from dropdown using the value
    private void selectCountry(final Country country) {
        if (country != null) {
            LOGGER.info("Select " + country + " from country drop down");
            new Select(countrySelector).selectByValue(country.getCountryCode());
        }
    }

    // This method selects the state from dropdown using the value
    public void selectState(final StateProvince state) {
        if (state != null) {
            LOGGER.info("Select " + state + " from state drop down");
            new WebDriverWait(getDriver(), 15).until(ExpectedConditions.elementToBeClickable(stateSelector));
            new Select(stateSelector).selectByValue(state.getStateName());
        }
    }

    public ShippingMethod add(final ShippingMethod shippingMethod) {
        LOGGER.info("Add Shipping Method");
        navigateToAddForm();
        setName(shippingMethod.getDescriptor().getName());
        setExternalKey(shippingMethod.getExternalKey());
        setAgentId(shippingMethod.getAgentId());
        setPrice(shippingMethod.getPrice().getAmount());
        selectCurrency(Currency.getByValue(shippingMethod.getPrice().getCurrency()));
        addDestinations(shippingMethod.getDestinations());
        submit();
        return getDetails();
    }

    /**
     * Navigate to the Shipping Methods detail page and click on the edit button
     */
    public ShippingMethodsPage edit(final String id) {
        ShippingMethodsPage page = null;

        final GenericDetails detailPage = getDetailPage(id);
        if (!detailPage.isPageValid()) {
            LOGGER.error("Unable to find Shipping Methods #" + id + " for edit");
        } else {
            LOGGER.info("Edit Shipping Methods #" + id);
            editButton.click();
            page = getPage(ShippingMethodsPage.class);
        }
        return page;
    }

    public boolean delete(final String id) {
        boolean success = false;
        final GenericDetails detailPage = getDetailPage(id);
        if (detailPage.isPageValid()) {
            LOGGER.info("Delete Shipping Methods #" + id);
            deleteAndConfirm();
            // if Shipping Methods is successfully deleted, then we're back to
            // Shipping Method detail page
            if (getHeader().equalsIgnoreCase("Shipping Method Detail")) {
                success = true;
            }
        } else {
            LOGGER.error("Unable to find Shipping Methods #" + id + " for deletion");
        }
        return success;
    }

    private ShippingMethod getDetails() {
        final ShippingMethod shippingMethod = new ShippingMethod();
        final Descriptor shippingDescriptor = new Descriptor();
        final Price shippingPrice = new Price();

        // After detail after creation
        final GenericDetails details = super.getPage(GenericDetails.class);

        shippingMethod.setId(details.getValueByField("ID"));
        shippingMethod.setExternalKey(details.getValueByField("External Key"));
        shippingDescriptor.setName(details.getValueByField("Name"));
        shippingMethod.setDescriptor(shippingDescriptor);
        shippingMethod.setPrice(shippingPrice);

        return shippingMethod;
    }

    public String getExternalKeyErrorMessage() {
        LOGGER.info("External key got error message: " + extKeyErrorMessage.getText());
        return extKeyErrorMessage.getText();
    }

    /**
     * Navigate to find by ext key page to search for ShippingMethods by Ext Key
     *
     * @param extKey shipping method extKey
     * @return if extKey exists, return the shippingMethod page. Otherwise null.
     */
    public GenericDetails findByExtKey(final String extKey) {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.SHIPPING_METHODS.getForm()
            + "/find?findType=advanced&externalKey=" + extKey;
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
        GenericDetails detailsPage;
        try {
            // Click on the first row in the shipping method search result grid
            final GenericGrid searchGrid = super.getPage(GenericGrid.class);
            detailsPage = searchGrid.selectResultRowWithHyperLink("shippingMethod", 0);
        } catch (final NoSuchElementException e) {
            detailsPage = null;
            // TODO: handle exception
        }
        return detailsPage;
    }

    private void addDestinations(final List<Destination> destinations) {
        if (destinations.size() > 0) {
            for (final Destination destination : destinations) {
                getDriver().manage().timeouts().implicitlyWait(30000, TimeUnit.MILLISECONDS);
                selectCountry(Country.getByCode(destination.getCountry()));
                // selectState(StateProvince.getByName(destination.getState()));
                addDestinationLink.click();
            }
        }
    }

    private GenericDetails getDetailPage(final String id) {
        final String url =
            getEnvironment().getAdminUrl() + "/" + AdminPages.SHIPPING_METHODS.getForm() + "/show?id=" + id;
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);

        return getPage(GenericDetails.class);
    }

    private void navigateToAddForm() {
        final String url = getEnvironment().getAdminUrl() + "/" + AdminPages.SHIPPING_METHODS.getForm() + "/addForm";
        LOGGER.info("Navigate to '" + url + "'");
        getDriver().get(url);
    }

}
