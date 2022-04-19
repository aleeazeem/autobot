package com.autodesk.bsm.pelican.util;

import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;

import java.util.ArrayList;
import java.util.List;

/**
 * Catch assertion error so that test method can continue to run to validate multiple fields
 *
 * @author yin
 */
public class AssertCollector {

    /**
     * Hamcrest assertThat
     *
     * @param assertionErrorList
     */
    public static <T> void assertThat(final String message, final T actual, final Matcher<? super T> matcher,
        final List<AssertionError> assertionErrorList) {

        try {

            MatcherAssert.assertThat(message, actual, matcher);

        } catch (final AssertionError e) {
            synchronized (assertionErrorList) {
                assertionErrorList.add(e);
            }
        }
    }

    /**
     * Hamcrest assertTrue
     *
     * @param value
     * @param assertionErrorList
     */
    public static <T> void assertTrue(final String reason, final boolean value,
        final List<AssertionError> assertionErrorList) {

        try {

            MatcherAssert.assertThat(reason, value);
        } catch (final AssertionError e) {
            synchronized (assertionErrorList) {
                assertionErrorList.add(e);
            }
        }
    }

    /**
     * Hamcrest assertFalse
     *
     * @param value
     * @param assertionErrorList
     */
    public static <T> void assertFalse(final String reason, final boolean value,
        final List<AssertionError> assertionErrorList) {

        try {

            if (value) {
                throw new AssertionError(reason);
            }
        } catch (final AssertionError e) {
            synchronized (assertionErrorList) {
                assertionErrorList.add(e);
            }
        }
    }

    /**
     * Pop the assertionErrorList
     *
     * @param assertionErrorList
     */
    public static void assertAll(List<AssertionError> assertionErrorList) {
        if (assertionErrorList.size() > 0) {

            boolean first = true;

            final StringBuilder message = new StringBuilder("The following asserts failed:\n");

            for (final AssertionError error : assertionErrorList) {
                if (first) {
                    first = false;
                } else {
                    message.append("\n");
                }
                message.append(error.getMessage());
            }

            // reset
            synchronized (assertionErrorList) {
                assertionErrorList = new ArrayList<>();
            }
            throw new AssertionError(message.toString());
        }
    }

}
