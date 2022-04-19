package com.autodesk.bsm.pelican.util;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Compares a set of named object properties for equality
 */
public class PropertiesComparator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesComparator.class.getSimpleName());

    public static class PropertyComparisonResult {

        private Class propertyClass;
        private String property;
        private Object actualValue;
        private Object expectedValue;
        private boolean equal;

        public Class getPropertyClass() {
            return propertyClass;
        }

        public void setPropertyClass(final Class propertyClass) {
            this.propertyClass = propertyClass;
        }

        public String getProperty() {
            return property;
        }

        public void setProperty(final String property) {
            this.property = property;
        }

        public Object getActualValue() {
            return actualValue;
        }

        public void setActualValue(final Object value) {
            this.actualValue = value;
        }

        public Object getExpectedValue() {
            return expectedValue;
        }

        public void setExpectedValue(final Object value) {
            this.expectedValue = value;
        }

        public boolean isEqual() {
            return equal;
        }

        public void setEqual(final boolean equal) {
            this.equal = equal;
        }

        public String toString() {

            final StringBuilder string = new StringBuilder();
            string.append("Expected value '");
            string.append(getExpectedValue());
            string.append("', Actual value '");
            string.append(getActualValue());
            string.append("' Type '");
            string.append(getPropertyClass().getName());
            string.append("'");

            return string.toString();
        }
    }

    private List<PropertyComparisonResult> results = new ArrayList<>();

    /**
     * Given a list of properties, compare them for equality. Objects do not need to be the same instance.
     */
    public boolean equals(final Collection<String> properties, final Object actualObject, final Object expectedObject) {

        LOGGER.info("Compare properties");

        if (properties == null || properties.isEmpty()) {
            throw new RuntimeException("No comparison properties specified.");
        }

        if (actualObject == null && expectedObject == null) {
            return true;
        }

        if (actualObject == null) {
            throw new RuntimeException("First Object parameter is null.");
        }

        if (expectedObject == null) {
            throw new RuntimeException("First Object parameter is null.");
        }

        boolean equal = true;

        final Set<String> evaluatedProperties = new HashSet<>();

        for (final String property : properties) {

            if (evaluatedProperties.contains(property)) {
                // prevent needless mistakes
                throw new RuntimeException("Property " + property + " is duplicated in input properties");
            }

            final PropertyComparisonResult result = new PropertyComparisonResult();
            results.add(result);
            result.setProperty(property);

            Object actualValue;
            Object expectedValue;

            try {
                // get the values
                actualValue = PropertyUtils.getNestedProperty(actualObject, property);
                expectedValue = PropertyUtils.getNestedProperty(expectedObject, property);

                // save the values
                result.setActualValue(actualValue);
                result.setExpectedValue(expectedValue);

                // get the return type
                final PropertyDescriptor descriptor = PropertyUtils.getPropertyDescriptor(actualObject, property);
                result.setPropertyClass(descriptor.getReadMethod().getReturnType());

            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }

            // compare
            LOGGER.info("Compare " + property);
            result.setEqual(ObjectUtils.equals(actualValue, expectedValue));

            if (!result.isEqual()) {
                equal = false;
            }

            evaluatedProperties.add(property);
        }

        logResults();

        return equal;
    }

    /**
     * Given a list of properties of date time, compare them with tolerance
     */
    public boolean diff(final Collection<String> properties, final String dateFormat, final Object actualObject,
        final Object expectedObject) {

        LOGGER.info("Compare date time properties");

        if (properties == null || properties.isEmpty()) {
            throw new RuntimeException("No comparison properties specified.");
        }

        if (actualObject == null && expectedObject == null) {
            return true;
        }

        if (actualObject == null) {
            throw new RuntimeException("Actual Object parameter is null.");
        }

        if (expectedObject == null) {
            throw new RuntimeException("Expected Object parameter is null.");
        }

        boolean equal = true;

        final Set<String> evaluatedProperties = new HashSet<>();

        for (final String property : properties) {

            if (evaluatedProperties.contains(property)) {
                // prevent needless mistakes
                throw new RuntimeException("Property " + property + " is duplicated in input properties");
            }

            final PropertyComparisonResult result = new PropertyComparisonResult();
            results.add(result);
            result.setProperty(property);

            LOGGER.info("Compare " + property);

            final SimpleDateFormat dateFormate = new SimpleDateFormat(dateFormat);

            Date actualDate;
            Date expectedDate;

            try {
                final Object actualValue = PropertyUtils.getNestedProperty(actualObject, property);
                final Object expectedValue = PropertyUtils.getNestedProperty(expectedObject, property);

                if (actualValue == null || expectedValue == null) {
                    if (actualValue == null && expectedValue == null) {
                        result.setEqual(true);
                        equal = true;
                    } else {
                        result.setEqual(false);
                        equal = false;
                    }
                    continue;
                }
                actualDate = dateFormate.parse(actualValue.toString());
                expectedDate = dateFormate.parse(expectedValue.toString());

                LOGGER.info("Actual Date  : " + actualDate);
                LOGGER.info("Expected Date: " + expectedDate);

                // save the values
                result.setActualValue(actualDate);
                result.setExpectedValue(expectedDate);

                final PropertyDescriptor descriptor = PropertyUtils.getPropertyDescriptor(actualObject, property);
                result.setPropertyClass(descriptor.getReadMethod().getReturnType());

                final long diffSeconds = Math.abs((actualDate.getTime() - expectedDate.getTime()) / 1000);

                // compare with tolerance
                if (diffSeconds < 90L) {
                    result.setEqual(true);
                    equal = true;
                } else {
                    result.setEqual(false);
                    equal = false;
                }
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (final ParseException e) {
                throw new RuntimeException("Unable to parse date value: ." + e);
            }
        }
        logResults();

        return equal;
    }

    /**
     * Get the list of property comparison results for more discrete analysis.
     */
    public List<PropertyComparisonResult> getResults() {
        return results;
    }

    private void logResults() {

        final StringBuilder message = new StringBuilder();

        for (final PropertyComparisonResult result : results) {

            if (!result.isEqual()) {

                message.append("Property '");
                message.append(result.getProperty());
                message.append("' is not equal.  ");
                message.append(result.toString());
                message.append("'.\n");
            }
        }
        if (message.length() > 0) {
            LOGGER.info(message.toString());
        }
    }
}
