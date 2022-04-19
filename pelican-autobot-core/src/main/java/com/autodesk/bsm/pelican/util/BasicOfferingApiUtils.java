package com.autodesk.bsm.pelican.util;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.basicoffering.Currency;
import com.autodesk.bsm.pelican.api.pojos.json.BasicOfferingPrice;
import com.autodesk.bsm.pelican.api.pojos.json.BasicOfferingPriceData;
import com.autodesk.bsm.pelican.api.pojos.json.Offering;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.CancellationPolicy;
import com.autodesk.bsm.pelican.enums.EntityType;
import com.autodesk.bsm.pelican.enums.MediaType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;

import org.apache.commons.lang.RandomStringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.TimeZone;

/**
 * A class to create Basic Offering.
 *
 * @author Shweta Hegde
 */
public class BasicOfferingApiUtils {

    private static final String TIER = "1";
    private static final String BASICOFFERING_PRICE_TYPE = "price";
    private static final int PRICE_AMOUNT = 500;
    private static final String APPEND_STRING = "SQA_Test_Util_";
    private final PelicanPlatform resource;

    public BasicOfferingApiUtils(final EnvironmentVariables environmentVariables) {
        resource = new PelicanClient(environmentVariables).platform();
    }

    /**
     * A method used to add a basic offering given the application family, price list external key, offering type, media
     * type, status and usage type
     */
    public Offerings addBasicOffering(final String priceListExternalKey, final OfferingType offeringType,
        final MediaType mediaType, final Status status, final UsageType usageType, final String productLine) {
        return addBasicOffering(priceListExternalKey, offeringType, mediaType, status, PRICE_AMOUNT, usageType, null,
            productLine);
    }

    /**
     * A method used to add a basic offering given the application family, price list external key, offering type, media
     * type, status and usage type
     *
     * @param basicOfferingExternalKey TODO
     */
    public Offerings addBasicOffering(final String priceListExternalKey, final OfferingType offeringType,
        final MediaType mediaType, final Status status, final int price, final UsageType usageType,
        final String basicOfferingExternalKey, final String productLine) {

        // Add Basic Offering
        final Offerings newBasicOffering = addBasicOffering(resource, offeringType, mediaType, status, usageType,
            basicOfferingExternalKey, productLine);
        final String basicOfferingId = newBasicOffering.getOfferings().get(0).getId();

        // Add basic offering price
        final BasicOfferingPrice basicOfferingPrice = new BasicOfferingPrice();
        final DateFormat dateFormat = new SimpleDateFormat(PelicanConstants.DATE_FORMAT_WITH_SLASH);
        dateFormat.setTimeZone(TimeZone.getTimeZone(PelicanConstants.UTC_TIME_ZONE));
        final Calendar calendar = Calendar.getInstance();
        final Date startDate = calendar.getTime();
        calendar.add(Calendar.YEAR, 1);
        final Date endDate = calendar.getTime();
        final String priceStartDate = dateFormat.format(startDate);
        final String priceEndDate = dateFormat.format(endDate);
        final BasicOfferingPriceData basicOfferingPriceData = new BasicOfferingPriceData();
        basicOfferingPriceData.setType(BASICOFFERING_PRICE_TYPE);
        basicOfferingPriceData.setAmount(price);
        basicOfferingPriceData.setStartDate(priceStartDate);
        basicOfferingPriceData.setEndDate(priceEndDate);
        basicOfferingPriceData.setPriceList(priceListExternalKey);
        basicOfferingPrice.setData(basicOfferingPriceData);
        addPricesToBasicOffering(resource, basicOfferingPrice, basicOfferingId);

        return resource.offerings().getOfferingById(basicOfferingId, "prices");
    }

    /**
     * This method adds Basic Offering
     *
     * @param basicOfferingExternalKey TODO
     * @return Offerings
     */
    public Offerings addBasicOffering(final PelicanPlatform resource, final OfferingType offeringType,
        final MediaType mediaType, final Status status, final UsageType usageType,
        final String basicOfferingExternalKey, final String productLine) {

        final Offerings basicOffering = new Offerings();
        String offeringExternalKey = basicOfferingExternalKey;
        // Add basic offering
        if (basicOfferingExternalKey == null) {
            offeringExternalKey = APPEND_STRING + RandomStringUtils.randomAlphabetic(12);
        }
        final Offering basicOfferingData = new Offering();
        basicOfferingData.setExternalKey(offeringExternalKey);
        basicOfferingData.setName(offeringExternalKey);
        basicOfferingData.setEntityType(EntityType.OFFERING);
        basicOfferingData.setOfferingType(offeringType);
        basicOfferingData.setTier(TIER);
        basicOfferingData.setStatus(status.toString());
        basicOfferingData.setUsageType(usageType);
        basicOfferingData.setOfferingDetail(PelicanConstants.OFFERING_DETAILS1);
        basicOfferingData.setCancellationPolicy(CancellationPolicy.CANCEL_AT_END_OF_BILLING_PERIOD);
        if (productLine == null) {
            basicOfferingData.setProductLine(BaseTestData.getProductLineExternalKeyRevit());
        } else {
            basicOfferingData.setProductLine(productLine);
        }

        if (offeringType.equals(OfferingType.CURRENCY)) {
            basicOfferingData.setCurrency(BaseTestData.getCloudCurrencyName());
            basicOfferingData.setAmount(PRICE_AMOUNT);
        } else {
            basicOfferingData.setMediaType(mediaType);
        }
        basicOffering.setOffering(basicOfferingData);
        final Offerings newBasicOffering = resource.offerings().addOffering(basicOffering);

        return resource.offerings().getOfferingById(newBasicOffering.getOffering().getId(), "prices");
    }

    /**
     * A method used to add a currency
     *
     * @param - Pelican Platform Resource, Product Line object
     */
    public Currency addCurrency(final PelicanPlatform resource, final LinkedHashMap<String, String> requestBody) {
        return resource.currency().add(requestBody);
    }

    /**
     * A method used to add prices to the basic offerings
     *
     * @param - Pelican Platform Resource, Basic Offering Object
     */
    public BasicOfferingPrice addPricesToBasicOffering(final PelicanPlatform resource,
        final BasicOfferingPrice basicOfferingPrice, final String offeringId) {
        return resource.basicOfferingPrice().addBasicOfferingPrice(basicOfferingPrice, offeringId);
    }

}
