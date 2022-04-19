package com.autodesk.bsm.pelican.ui.store;

import com.autodesk.bsm.pelican.enums.Country;
import com.autodesk.bsm.pelican.enums.Currency;
import com.autodesk.bsm.pelican.ui.pages.stores.AddPriceListPage;
import com.autodesk.bsm.pelican.ui.pages.stores.AddStorePage;
import com.autodesk.bsm.pelican.ui.pages.stores.StoreDetailPage;

import org.apache.commons.lang.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;

public class StoreHelper {

    /**
     * Create store with random name and external key. Add price list. Then, assign country if assginCountry = true.
     *
     * @return newly created store
     */
    public static StoreDetailPage createStore(final AddStorePage addStorePage, final String storeName,
        final String storeExtKey, final String storeTypeName, final boolean assignCountry) {

        addStorePage.addStore(storeName, storeExtKey, null, storeTypeName, false, null, null);
        StoreDetailPage storeDetailPage = addStorePage.clickAddStore();

        // Add price lists to the newly created store
        final List<Currency> currencies = new ArrayList<>();
        currencies.add(Currency.USD);
        currencies.add(Currency.CAD);
        currencies.add(Currency.MXN);

        for (final Currency currency1 : currencies) {
            final String name = "North America " + currency1.toString();
            final String extKey = name.replace(" ", "_") + RandomStringUtils.randomAlphanumeric(8);

            final AddPriceListPage addPriceListPage = storeDetailPage.addPriceList();
            addPriceListPage.addPriceList(name, extKey, currency1.getLongDescription());

            storeDetailPage = addPriceListPage.clickOnAddPriceList();

            // Assign the country to price list
            final Currency currency = currency1;
            Country assignedCountry;
            if (currency == Currency.USD) {
                assignedCountry = Country.US;
            } else if (currency == Currency.CAD) {
                assignedCountry = Country.CA;
            } else if (currency == Currency.MXN) {
                assignedCountry = Country.MX;
            } else {
                throw new RuntimeException("Unknown currency: " + currency.toString());
            }

            if (assignCountry) {
                storeDetailPage.assignCountryToPriceList(assignedCountry, name);
            }
        }

        return storeDetailPage;
    }
}
