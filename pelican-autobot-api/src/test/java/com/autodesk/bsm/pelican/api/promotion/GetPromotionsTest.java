package com.autodesk.bsm.pelican.api.promotion;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.autodesk.bsm.pelican.api.clients.PelicanClient;
import com.autodesk.bsm.pelican.api.clients.PelicanPlatform;
import com.autodesk.bsm.pelican.api.pojos.HttpError;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotion;
import com.autodesk.bsm.pelican.api.pojos.json.JPromotionData;
import com.autodesk.bsm.pelican.api.pojos.json.Offerings;
import com.autodesk.bsm.pelican.api.pojos.json.Promotions;
import com.autodesk.bsm.pelican.api.pojos.json.Promotions.Error;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.enums.EntityType;
import com.autodesk.bsm.pelican.enums.MediaType;
import com.autodesk.bsm.pelican.enums.OfferingType;
import com.autodesk.bsm.pelican.enums.PromotionType;
import com.autodesk.bsm.pelican.enums.Status;
import com.autodesk.bsm.pelican.enums.UsageType;
import com.autodesk.bsm.pelican.ui.generic.BaseTestData;
import com.autodesk.bsm.pelican.util.AssertCollector;
import com.autodesk.bsm.pelican.util.BasicOfferingApiUtils;

import com.google.common.collect.Lists;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Promotions API Test
 *
 * @author t_mohag
 */
public class GetPromotionsTest extends BaseTestData {

    private PelicanPlatform resource;
    private Offerings basicOffering;
    private final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss zzz");
    private JPromotion promo1;
    private JPromotion promo2;
    private JPromotion newPromo;
    private JPromotion cancelledPromo;
    private JPromotion expiredPromo;
    private final List<String> promoIds = new ArrayList<>();
    private final List<String> promoCodes = new ArrayList<>();
    private final List<String> promoNames = new ArrayList<>();
    private static final String INVALID_PROMO_ID = "00000001";
    private static final String INVALID_PROMO_CODE = "$!#";
    private static final String INVALID_PROMOTION_ID_STR = "invalid-promotion-id";
    private static final String INVALID_PROMOTION_CODE_STR = "invalid-promotion-code";
    // The maximum length of a promo code is 16.
    private static final int MAX_PROMO_CODE_LENGTH = 16;
    private static final String CODE = "AUTO";
    private static final String BAD_REQUEST = "Bad Request";
    private static final Logger LOGGER = LoggerFactory.getLogger(GetPromotionsTest.class.getSimpleName());

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {

        resource = new PelicanClient(getEnvironmentVariables()).platform();
        final BasicOfferingApiUtils basicOfferingApiUtils = new BasicOfferingApiUtils(getEnvironmentVariables());

        // Add active basic offering
        basicOffering = basicOfferingApiUtils.addBasicOffering(getPricelistExternalKeyUs(), OfferingType.PERPETUAL,
            MediaType.DVD, Status.ACTIVE, UsageType.COM, null);
        dateFormat.setTimeZone(TimeZone.getTimeZone(PelicanConstants.UTC_TIME_ZONE));
    }

    @BeforeMethod(alwaysRun = true)
    public void startTestMethod(final Method method) {

        promoIds.clear();
        promoCodes.clear();
        promoNames.clear();
    }

    /**
     * Verify get promotions API by IDS with only one valid ID
     */
    @Test
    public void getPromotionsByIdsSingleValue() {
        final String promoCode =
            CODE.concat(RandomStringUtils.randomAlphanumeric(MAX_PROMO_CODE_LENGTH - CODE.length()));
        promo1 = getPromotion(promoCode, Status.ACTIVE, 25.0, getFutureExpirationDate());
        promoIds.add(String.valueOf(promo1.getData().getId()));
        promoNames.add(promo1.getData().getName());

        final Promotions promos = resource.promotions().getPromotionsByIds(promoIds, null);
        AssertCollector.assertFalse("Invalid value for isBundled. isBundled should have been false.",
            promos.getData().get(0).getIsBundledPromo(), assertionErrorList);
        assertValidPromotionsResponse(promos, 1, true);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify get promotions API by CODES with only one valid CODE
     */
    @Test
    public void getPromotionsByCodesSingleValue() {
        final String promoCode =
            CODE.concat(RandomStringUtils.randomAlphanumeric(MAX_PROMO_CODE_LENGTH - CODE.length()));
        promo2 = getPromotion(promoCode, Status.ACTIVE, 25.0, getFutureExpirationDate());
        promoCodes.add(promo2.getData().getCustomPromoCode());
        promoIds.add(String.valueOf(promo2.getData().getId()));
        promoNames.add(promo2.getData().getName());

        final Promotions promos = resource.promotions().getPromotionsByCodes(promoCodes, null);
        AssertCollector.assertThat("Invalid promotion code", promos.getData().get(0).getMeta().getRequestedCode(),
            isOneOf(promoCodes.toArray()), assertionErrorList);
        AssertCollector.assertFalse("Invalid value for isBundled. isBundled should have been false.",
            promos.getData().get(0).getIsBundledPromo(), assertionErrorList);
        assertValidPromotionsResponse(promos, 1, true);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify get promotions API by ID's wherein the promotions with IDS are present and ACTIVE in the admin tool
     */
    @Test
    public void getPromotionsByIdsTest() {
        final String promoCode1 =
            CODE.concat(RandomStringUtils.randomAlphanumeric(MAX_PROMO_CODE_LENGTH - CODE.length()));
        final String promoCode2 =
            CODE.concat(RandomStringUtils.randomAlphanumeric(MAX_PROMO_CODE_LENGTH - CODE.length()));
        promo1 = getPromotion(promoCode1, Status.ACTIVE, 10.0, getFutureExpirationDate());
        promo2 = getPromotion(promoCode2, Status.ACTIVE, 15.0, getFutureExpirationDate());
        // This methods initializes promoIds, promoCodes and promoNames lists.
        // promoIds list will be initialized with Ids of promo1, promo2.
        // promoCodes list will be initialized with codes of promo1, promo2
        // promoNames list will be initialized with names of promo1, promo2
        initValidPromosData(Lists.newArrayList(promo1, promo2));

        final Promotions promos = resource.promotions().getPromotionsByIds(promoIds, null);
        AssertCollector.assertThat("Invalid number of promos with discount percent", promos.getData().size(),
            equalTo(2), assertionErrorList);
        AssertCollector.assertFalse("Invalid value for isBundled. isBundled should have been false.",
            promos.getData().get(0).getIsBundledPromo(), assertionErrorList);
        assertValidPromotionsResponse(promos, 2, false);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify get promotions API by CODES wherein the promotions with CODES are present and ACTIVE in the admin tool
     */
    @Test
    public void getPromotionsByCodesTest() {
        final String promoCode1 =
            CODE.concat(RandomStringUtils.randomAlphanumeric(MAX_PROMO_CODE_LENGTH - CODE.length()));
        final String promoCode2 =
            CODE.concat(RandomStringUtils.randomAlphanumeric(MAX_PROMO_CODE_LENGTH - CODE.length()));
        promo1 = getPromotion(promoCode1, Status.ACTIVE, 10.0, getFutureExpirationDate());
        promo2 = getPromotion(promoCode2, Status.ACTIVE, 15.0, getFutureExpirationDate());
        // This methods initializes promoIds, promoCodes and promoNames lists.
        // promoIds list will be initialized with Ids of promo1, promo2.
        // promoCodes list will be initialized with codes of promo1, promo2
        // promoNames list will be initialized with names of promo1, promo2
        initValidPromosData(Lists.newArrayList(promo1, promo2));
        final List<String> promoStateList = Arrays.asList(Status.ACTIVE.toString());

        final Promotions promos = resource.promotions().getPromotionsByCodes(promoCodes, promoStateList);
        AssertCollector.assertThat("Invalid promotion code", promos.getData().get(0).getMeta().getRequestedCode(),
            isOneOf(promoCodes.toArray()), assertionErrorList);
        AssertCollector.assertThat("Invalid promotion code", promos.getData().get(1).getMeta().getRequestedCode(),
            isOneOf(promoCodes.toArray()), assertionErrorList);
        AssertCollector.assertThat("Invalid number of promos with discount percent", promos.getData().size(),
            equalTo(2), assertionErrorList);
        AssertCollector.assertFalse("Invalid value for isBundled. isBundled should have been false.",
            promos.getData().get(0).getIsBundledPromo(), assertionErrorList);
        assertValidPromotionsResponse(promos, 2, false);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify get promotion API with ID and New state returns Promotion with state New.
     *
     */
    @Test
    public void testGetPromotionByIdsWithNewState() {

        final String promoCode1 =
            CODE.concat(RandomStringUtils.randomAlphanumeric(MAX_PROMO_CODE_LENGTH - CODE.length()));
        final String promoCode2 =
            CODE.concat(RandomStringUtils.randomAlphanumeric(MAX_PROMO_CODE_LENGTH - CODE.length()));

        promo1 = getPromotion(promoCode1, Status.NEW, 25.0, getFutureExpirationDate());
        promo2 = getPromotion(promoCode2, Status.ACTIVE, 25.0, getFutureExpirationDate());
        final String promoId1 = promo1.getData().getId();
        final String promoId2 = promo2.getData().getId();

        final List<String> promoIdList = Arrays.asList(promoId1, promoId2);
        final List<String> promoStateList = Arrays.asList(Status.NEW.toString());

        final Promotions promos = resource.promotions().getPromotionsByIds(promoIdList, promoStateList);
        AssertCollector.assertThat("Promotion" + promoId1 + "Should be Present under Data",
            promos.getData().get(0).getId(), equalTo(promoId1), assertionErrorList);
        AssertCollector.assertThat("Promotion state shoule be New", promos.getData().get(0).getStatus(),
            equalTo(Status.NEW), assertionErrorList);
        AssertCollector.assertThat("Promotion" + promoId2 + "Should be Present under Error",
            promos.getErrors().get(0).getDetail(), equalTo("Invalid promotion ID: " + promoId2), assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify get promotion API with Code and Expired & Active state returns the Promotion with Expired and Active
     * state.
     */
    @Test
    public void testGetPromotionByCodesWithExpiredState() {

        final String promoCode1 =
            CODE.concat(RandomStringUtils.randomAlphanumeric(MAX_PROMO_CODE_LENGTH - CODE.length()));
        final String promoCode2 =
            CODE.concat(RandomStringUtils.randomAlphanumeric(MAX_PROMO_CODE_LENGTH - CODE.length()));
        promo1 = getPromotion(promoCode1, Status.ACTIVE, 10.0, getFutureExpirationDate());
        promo2 = getPromotion(promoCode2, Status.EXPIRED, 15.0, getFutureExpirationDate());

        final List<String> promoCodeList = Arrays.asList(promoCode1, promoCode2);
        final List<String> promoStateList = Arrays.asList(Status.ACTIVE.toString(), Status.EXPIRED.toString());

        final Promotions promotion = resource.promotions().getPromotionsByCodes(promoCodeList, promoStateList);
        AssertCollector.assertThat("Invalid promotion code", promotion.getData().get(0).getMeta().getRequestedCode(),
            isOneOf(promoCode1, promoCode2), assertionErrorList);
        AssertCollector.assertThat("Invalid promotion code", promotion.getData().get(1).getMeta().getRequestedCode(),
            isOneOf(promoCode1, promoCode2), assertionErrorList);
        AssertCollector.assertThat("Promotion should not be under Error ", promotion.getErrors(), nullValue(),
            assertionErrorList);

        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify get promotion API throws an Error Bad Request when Promotion Id or Code is not passed in the argument and
     * passing only status
     */
    @Test
    public void testErrorGetPromotionWithValidStateAndNoIdOrCode() {

        final List<String> promoStateList = Arrays.asList(Status.CANCELLED.toString());

        final HttpError httpError = resource.promotions().getPromotionsByIds(null, promoStateList);
        AssertCollector.assertThat("Invalid http status", httpError.getStatus(),
            equalTo(PelicanConstants.HttpStatusCode_BAD_REQ), assertionErrorList);
        AssertCollector.assertThat("Invalid http reasonPhrase", httpError.getErrorMessage(), equalTo(BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify get promotions API by ID's with an invalid ID
     */
    @Test
    public void getPromotionsByIdsWithErrorsTest() {
        final String promoCode1 =
            CODE.concat(RandomStringUtils.randomAlphanumeric(MAX_PROMO_CODE_LENGTH - CODE.length()));
        promo1 = getPromotion(promoCode1, Status.ACTIVE, 10.0, getFutureExpirationDate());
        promoIds.add(String.valueOf(promo1.getData().getId()));
        promoIds.add(INVALID_PROMO_ID);
        promoCodes.add(promoCode1);
        promoNames.add(promo1.getData().getName());

        final List<String> invalidPromoIds = new ArrayList<>();
        invalidPromoIds.add(INVALID_PROMO_ID);
        final Promotions promos = resource.promotions().getPromotionsByIds(promoIds, null);
        AssertCollector.assertFalse("Invalid value for isBundled. isBundled should have been false.",
            promos.getData().get(0).getIsBundledPromo(), assertionErrorList);
        assertInvalidPromotionsResponse(promos, 1, 1, invalidPromoIds, false);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify get promotions API by CODES with an invalid CODE
     */
    @Test
    public void getPromotionsByCodesWithErrorsTest() {
        final String promoCode1 =
            CODE.concat(RandomStringUtils.randomAlphanumeric(MAX_PROMO_CODE_LENGTH - CODE.length()));
        promo1 = getPromotion(promoCode1, Status.ACTIVE, 10.0, getFutureExpirationDate());
        promoCodes.add(promoCode1);
        promoCodes.add(INVALID_PROMO_CODE);
        promoIds.add(String.valueOf(promo1.getData().getId()));
        promoNames.add(promo1.getData().getName());

        final List<String> invalidPromoCodes = new ArrayList<>();
        invalidPromoCodes.add(INVALID_PROMO_CODE);
        final Promotions promos = resource.promotions().getPromotionsByCodes(promoCodes, null);
        assertInvalidPromotionsResponse(promos, 1, 1, invalidPromoCodes, true);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify get promotions API by IDS with more than 50 IDS as input
     */
    @Test
    public void getPromotionsByIdsInputMaxLimit() {
        for (int i = 0; i <= 50; i++) {
            promoIds.add(UUID.randomUUID().toString());
        }

        final HttpError httpError = resource.promotions().getPromotionsByIds(promoIds, null);
        AssertCollector.assertThat("Invalid http status", httpError.getStatus(),
            equalTo(PelicanConstants.HttpStatusCode_BAD_REQ), assertionErrorList);
        AssertCollector.assertThat("Invalid http reasonPhrase", httpError.getErrorMessage(), equalTo(BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify get promotions API by CODES with more than 50 CODES as input
     */
    @Test
    public void getPromotionsByCodesInputMaxLimit() {
        for (int i = 0; i <= 50; i++) {
            promoCodes.add(UUID.randomUUID().toString());
        }

        final HttpError httpError = resource.promotions().getPromotionsByCodes(promoCodes, null);
        AssertCollector.assertThat("Invalid http status", httpError.getStatus(),
            equalTo(PelicanConstants.HttpStatusCode_BAD_REQ), assertionErrorList);
        AssertCollector.assertThat("Invalid http reasonPhrase", httpError.getErrorMessage(), equalTo(BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify get promotions API by IDS with invalid input
     */
    @Test
    public void getPromotionsByIdsInvalidInput() {
        promoIds.add(",");
        promoIds.add(", ");
        promoIds.add("   ");
        final Promotions httpResponse = resource.promotions().getPromotionsByIds(promoIds, null);
        AssertCollector.assertThat("Invalid http response data", httpResponse.getData(), equalTo(null),
            assertionErrorList);
        AssertCollector.assertThat("Invalid http response error", httpResponse.getErrors(), equalTo(null),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify get promotions API by CODES with invalid input
     */
    @Test
    public void getPromotionsByCodesInvalidInput() {
        promoCodes.add(",");
        promoCodes.add(", ");
        promoCodes.add("   ");

        final HttpError httpError = resource.promotions().getPromotionsByCodes(promoCodes, null);
        AssertCollector.assertThat("Invalid http status", httpError.getStatus(),
            equalTo(PelicanConstants.HttpStatusCode_BAD_REQ), assertionErrorList);
        AssertCollector.assertThat("Invalid http reasonPhrase", httpError.getErrorMessage(), equalTo(BAD_REQUEST),
            assertionErrorList);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify get promotions API by IDS where the input IDS correspond to promotions with invalid status (i.e
     * NEW/CANCELLED/EXPIRED)
     */
    @Test
    public void getPromotionsByIdsWithInvalidStatus() {
        final String newPromoCode =
            CODE.concat(RandomStringUtils.randomAlphanumeric(MAX_PROMO_CODE_LENGTH - CODE.length()));
        newPromo = getPromotion(newPromoCode, Status.NEW, 15.0, getFutureExpirationDate());
        final String cancelledPromoCode =
            CODE.concat(RandomStringUtils.randomAlphanumeric(MAX_PROMO_CODE_LENGTH - CODE.length()));
        cancelledPromo = getPromotion(cancelledPromoCode, Status.CANCELLED, 10.0, getFutureExpirationDate());
        final String expiredPromoCode =
            CODE.concat(RandomStringUtils.randomAlphanumeric(MAX_PROMO_CODE_LENGTH - CODE.length()));
        expiredPromo = getPromotion(expiredPromoCode, Status.EXPIRED, 5.0, getFutureExpirationDate());

        promoIds.add(newPromo.getData().getId());
        promoIds.add(cancelledPromo.getData().getId());
        promoIds.add(expiredPromo.getData().getId());

        final Promotions promos = resource.promotions().getPromotionsByIds(promoIds, null);
        // Assert the GET Promotions API response wherein 3 promoIds
        // corresponding to promotions
        // with invalid status (i.e. NEW, CANCELLED, EXPIRED) are provided as
        // input.
        assertInvalidPromotionsResponse(promos, 0, 3, promoIds, false);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Verify get promotions API by CODES where the input CODES correspond to promotions with invalid status (i.e
     * NEW/CANCELLED/EXPIRED)
     */
    @Test
    public void getPromotionsByCodesWithInvalidStatus() {
        final String newPromoCode =
            CODE.concat(RandomStringUtils.randomAlphanumeric(MAX_PROMO_CODE_LENGTH - CODE.length()));
        newPromo = getPromotion(newPromoCode, Status.NEW, 15.0, getFutureExpirationDate());
        final String cancelledPromoCode =
            CODE.concat(RandomStringUtils.randomAlphanumeric(MAX_PROMO_CODE_LENGTH - CODE.length()));
        cancelledPromo = getPromotion(cancelledPromoCode, Status.CANCELLED, 10.0, getFutureExpirationDate());
        final String expiredPromoCode =
            CODE.concat(RandomStringUtils.randomAlphanumeric(MAX_PROMO_CODE_LENGTH - CODE.length()));
        expiredPromo = getPromotion(expiredPromoCode, Status.EXPIRED, 5.0, getFutureExpirationDate());
        promoCodes.add(newPromoCode);
        promoCodes.add(cancelledPromoCode);
        promoCodes.add(expiredPromoCode);

        final Promotions promos = resource.promotions().getPromotionsByCodes(promoCodes, null);
        // Assert the GET Promotions API response wherein 3 promo codes (i.e.
        // newPromoCode,
        // cancelledPromoCode,
        // expiredPromoCode) corresponding to promotions with invalid status
        // (i.e. NEW, CANCELLED,
        // EXPIRED) are
        // provided as input.
        assertInvalidPromotionsResponse(promos, 0, 3, promoCodes, true);
        AssertCollector.assertAll(assertionErrorList);
    }

    /**
     * Initialize the promotion data for positive test cases
     */
    private void initValidPromosData(final ArrayList<JPromotion> promos) {
        for (final JPromotion promo : promos) {
            promoIds.add(String.valueOf(promo.getData().getId()));
            promoCodes.add(promo.getData().getCustomPromoCode());
            promoNames.add(promo.getData().getName());
        }
    }

    /**
     * This method is used to validate the response corresponding to a Get Promotions API request for positive tests
     */
    private void assertValidPromotionsResponse(final Promotions promos, final int numValidPromos,
        final boolean checkDiscountType) {
        AssertCollector.assertThat("Invalid number of promotions", promos.getData().size(), equalTo(numValidPromos),
            assertionErrorList);
        AssertCollector.assertThat("Errors is not NULL", promos.getErrors(), nullValue(), assertionErrorList);

        for (final JPromotionData responsePromo : promos.getData()) {
            assertValidPromotionResponse(responsePromo, checkDiscountType);
        }
    }

    /**
     * This method is used to validate each promotion's data in the response obtained from a GET Promotions API request.
     */
    private void assertValidPromotionResponse(final JPromotionData responsePromo, final boolean checkDiscountType) {
        AssertCollector.assertThat("Invalid promotion name", responsePromo.getName(), isOneOf(promoNames.toArray()),
            assertionErrorList);
        AssertCollector.assertThat("Invalid promotion id", responsePromo.getId(), isOneOf(promoIds.toArray()),
            assertionErrorList);
        if (checkDiscountType) {
            AssertCollector.assertThat("Invalid promotion promoType", responsePromo.getPromotionType(),
                equalTo(PromotionType.DISCOUNT_PERCENTAGE), assertionErrorList);
        }
        AssertCollector.assertThat("Invalid promotion status", responsePromo.getStatus(), equalTo(Status.ACTIVE),
            assertionErrorList);
    }

    /**
     * This method is used to validate the response corresponding to a Get Promotions API request for negative tests
     */
    private void assertInvalidPromotionsResponse(final Promotions promos, final int numValidPromos, final int numErrors,
        final List<String> invalidPromoIdsOrCodes, final boolean isPromoCode) {
        if (numValidPromos == 0) {
            AssertCollector.assertThat("Invalid number of promotions", promos.getData(), nullValue(),
                assertionErrorList);
        } else {
            AssertCollector.assertThat("Invalid number of promotions", promos.getData().size(), equalTo(numValidPromos),
                assertionErrorList);
            for (final JPromotionData validPromo : promos.getData()) {
                assertValidPromotionResponse(validPromo, true);
            }
        }

        if (numErrors == 0) {
            AssertCollector.assertThat("Invalid number of errors", promos.getErrors(), nullValue(), assertionErrorList);
        } else {
            AssertCollector.assertThat("Invalid number of errors", promos.getErrors().size(), equalTo(numErrors),
                assertionErrorList);
            assertErrorsField(promos.getErrors(), invalidPromoIdsOrCodes, isPromoCode, assertionErrorList);
        }
    }

    /**
     * This method is used to validate the data in the errors array of the response corresponding to a Get Promotion API
     * request.
     *
     * @param assertionErrors TODO
     */
    private void assertErrorsField(final List<Error> errors, final List<String> invalidPromoIdsOrCodes,
        final boolean isPromoCode, final List<AssertionError> assertionErrors) {
        AssertCollector.assertThat("Invalid number of errors", errors.size(), equalTo(invalidPromoIdsOrCodes.size()),
            assertionErrors);
        for (final Error error : errors) {
            if (isPromoCode) {
                AssertCollector.assertThat(error.getCode() + " is missing in errors field", error.getCode(),
                    equalTo(INVALID_PROMOTION_CODE_STR), assertionErrors);
            } else {
                AssertCollector.assertThat(error.getCode() + " is missing in errors field", error.getCode(),
                    equalTo(INVALID_PROMOTION_ID_STR), assertionErrors);
            }
            AssertCollector.assertThat(error.getCode() + " status code in errors field is incorrect", error.getStatus(),
                equalTo(PelicanConstants.HttpStatusCode_OK), assertionErrors);
            AssertCollector.assertThat(error.getCode() + " detail is missing in errors field", error.getDetail(),
                notNullValue(), assertionErrors);
        }
    }

    /**
     * This method is used to dynamically create a promotion
     *
     * @return JPromotion
     */
    private JPromotion getPromotion(final String promoCode, final Status status, final Double discountPercent,
        final Date expirationDate) {
        final String promoName = UUID.randomUUID().toString();
        final String promoDescription = UUID.randomUUID().toString();
        final PromotionType promoType = PromotionType.DISCOUNT_PERCENTAGE;
        final boolean storeWide = false;

        final JPromotion promoRequest = new JPromotion();
        final JPromotionData promoData = new JPromotionData();
        promoData.setType(EntityType.PROMOTION);
        promoData.setName(promoName);
        promoData.setDescription(promoDescription);
        promoData.setCustomPromoCode(promoCode);
        promoData.setStatus(status);
        promoData.setPromotionType(promoType);
        promoData.setStoreWide(storeWide);
        promoData.setDiscountPercent(discountPercent);
        promoData.setStoreIds(Lists.newArrayList(getStoreIdUs()));
        JPromotionData.PromotionOfferings promoOffering = new JPromotionData.PromotionOfferings();
        promoOffering.setId(basicOffering.getOfferings().get(0).getId());
        promoOffering.setQuantity(1);
        promoOffering.setApplyDiscount(true);
        promoData.setBasicOfferings(Lists.newArrayList(promoOffering));

        final Calendar calendar = Calendar.getInstance();
        final Date startDate = calendar.getTime();
        final String promoStartDate = dateFormat.format(startDate);
        final String promoExpirationDate = dateFormat.format(expirationDate);
        promoData.setEffectiveDate(promoStartDate);
        promoData.setExpirationDate(promoExpirationDate);
        promoRequest.setData(promoData);

        final JPromotion createdPromo = resource.promotion().addPromotion(promoRequest);
        LOGGER.info("Id of the created promotion: " + createdPromo.getData().getId());
        LOGGER.info("EntityType of the created Promotion: " + createdPromo.getData().getType());

        // The response from add promotion API currently has only promoId and
        // entityType. So, inorder to
        // validate
        // the data from get promotions API, I am populating the request data
        // (i.e. name, status,
        // discount etc.)
        // in the response pojo of add promotion API call.
        createdPromo.getData().setName(promoName);
        createdPromo.getData().setDescription(promoDescription);
        createdPromo.getData().setCustomPromoCode(promoCode);
        createdPromo.getData().setStatus(status);
        createdPromo.getData().setPromotionType(promoType);
        createdPromo.getData().setDiscountPercent(discountPercent);
        createdPromo.getData().setStoreWide(storeWide);
        promoOffering = new JPromotionData.PromotionOfferings();
        promoOffering.setId(basicOffering.getOfferings().get(0).getId());
        promoOffering.setQuantity(1);
        promoOffering.setApplyDiscount(true);
        createdPromo.getData().setBasicOfferings(Lists.newArrayList(promoOffering));

        return createdPromo;
    }

    /**
     * This method return a future expiration date.
     *
     * @return expirationDate
     */
    private Date getFutureExpirationDate() {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2021);
        return calendar.getTime();
    }
}
