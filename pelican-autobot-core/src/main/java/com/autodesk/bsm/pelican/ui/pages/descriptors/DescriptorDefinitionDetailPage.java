package com.autodesk.bsm.pelican.ui.pages.descriptors;

import com.autodesk.bsm.pelican.enums.DescriptorEntityTypes;
import com.autodesk.bsm.pelican.ui.entities.Descriptor;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.ui.pages.admintool.AdminToolPage;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the page object for DescriptorDefinitionDetails page which will be displayed once you finish add
 * descriptor definitions
 *
 * @author kishor
 */
public class DescriptorDefinitionDetailPage extends GenericDetails {

    private static final Logger LOGGER = LoggerFactory.getLogger(DescriptorDefinitionDetailPage.class.getSimpleName());

    private GenericDetails genericDetailsObj;

    public DescriptorDefinitionDetailPage(final AdminToolPage adminToolPage, final WebDriver driver,
        final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);
        genericDetailsObj = adminToolPage.getPage(GenericDetails.class);
    }

    /**
     * This method reads the values of all attributes in a descriptor details Page and creates a descriptor entity
     */
    public Descriptor getDescriptorEntityFromDetails() {

        final Descriptor descriptorObj = new Descriptor();
        descriptorObj.setAppFamily(genericDetailsObj.getValueByField("Application Family"));
        descriptorObj.setApiName(genericDetailsObj.getValueByField("API Name"));
        descriptorObj
            .setEntity(DescriptorEntityTypes.getEntityType((genericDetailsObj.getValueByField("Entity Type"))));
        descriptorObj.setGroupName(genericDetailsObj.getValueByField("Group Name"));
        descriptorObj.setFieldName(genericDetailsObj.getValueByField("Field Name"));
        descriptorObj.setLocalized(genericDetailsObj.getValueByField("Localized"));
        LOGGER.info("Max Length From the page :" + genericDetailsObj.getValueByField("Maximum Length"));
        descriptorObj.setMaxLength(genericDetailsObj.getValueByField("Maximum Length"));
        descriptorObj.setId(genericDetailsObj.getValueByField("ID"));
        return descriptorObj;
    }
}
