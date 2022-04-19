package com.autodesk.bsm.pelican.util;

import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.enums.DescriptorEntityTypes;
import com.autodesk.bsm.pelican.ui.entities.Descriptor;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.ui.generic.GenericGrid;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;
import com.autodesk.bsm.pelican.ui.pages.descriptors.FindDescriptorDefinitionsPage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class DescriptorUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(DescriptorUtils.class.getSimpleName());

    /**
     * Making the constructor private to avoid instantiation
     */
    private DescriptorUtils() {}

    /**
     * This method reads the values of all attributes in a descriptor details Page and creates a descriptor entity
     *
     * @return descriptors
     */
    public static Descriptor getDescriptorData(final DescriptorEntityTypes entityType, final String groupName,
        final String fieldName, final String apiName, final String localized, final String maxLength) {
        LOGGER.info("Set the descriptor values");
        final Descriptor descriptor = new Descriptor();
        descriptor.setAppFamily(PelicanConstants.AUTO_FAMILY_SELECT_VALUE);
        descriptor.setEntity(entityType);
        descriptor.setGroupName(groupName);
        descriptor.setFieldName(fieldName);
        descriptor.setApiName(apiName);
        descriptor.setLocalized(localized);
        descriptor.setMaxLength(maxLength);
        return descriptor;
    }

    public static String getExistingDescriptorId(final AdminToolPage adminToolPage,
        final FindDescriptorDefinitionsPage findDescriptorDefinitionsPage,
        final DescriptorEntityTypes descriptorEntityTypes, final String groupName, final String fieldName) {
        findDescriptorDefinitionsPage.navigateToPage();
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        findDescriptorDefinitionsPage.findDescriptors(descriptorEntityTypes, groupName);
        Util.waitInSeconds(TimeConstants.MINI_WAIT);
        final GenericGrid resultsGrid = adminToolPage.getPage(GenericGrid.class);
        final List<String> descriptors = resultsGrid.getColumnValues("Field Name");
        if (descriptors != null && !descriptors.isEmpty()) {
            for (int i = 0; i < descriptors.size(); i++) {
                if (descriptors.get(i).equals(fieldName)) {
                    final int selectedRow = i + 1;
                    final GenericDetails descriptorDetails = resultsGrid.selectResultRow(selectedRow);
                    return descriptorDetails.getValueByField("ID");
                }
            }
        }

        return null;
    }
}
