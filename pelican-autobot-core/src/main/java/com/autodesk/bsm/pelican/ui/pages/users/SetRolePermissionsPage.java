package com.autodesk.bsm.pelican.ui.pages.users;

import com.autodesk.bsm.pelican.constants.TimeConstants;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.ui.generic.GenericDetails;
import com.autodesk.bsm.pelican.util.Util;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * POM represents Set Role Permission Page through AdminTool under User.
 *
 * @author Vaibhavi Joshi
 */
public class SetRolePermissionsPage extends GenericDetails {

    public SetRolePermissionsPage(final WebDriver driver, final EnvironmentVariables environmentVariables) {
        super(driver, environmentVariables);

    }

    @FindBy(xpath = "//*[@value='Update Permissions']")
    private WebElement updatePermissions;

    private static final Logger LOGGER = LoggerFactory.getLogger(SetRolePermissionsPage.class.getSimpleName());

    /**
     * This is a method which will assign passed permission and update the permission for role.
     *
     * @param List of Permission
     */
    public void selectPermissions(final List<String> permissions) {
        final Map<String, WebElement> availablePermissions = getPermissions();

        for (final String permission : permissions) {
            if (availablePermissions.containsKey(permission)) {
                final WebElement checkBox = availablePermissions.get(permission);
                if (!checkBox.isSelected()) {
                    checkBox.click();
                    Util.waitInSeconds(TimeConstants.ONE_SEC);
                }
            }

        }
    }

    /**
     * This is a method which will removed passed permission and update the permission for role.
     *
     * @param List of Permission
     */
    public void unSelectPermissions(final List<String> permissions) {
        final Map<String, WebElement> availablePermissions = getPermissions();

        for (final String permission : permissions) {
            if (availablePermissions.containsKey(permission)) {
                final WebElement checkBox = availablePermissions.get(permission);
                if (checkBox.isSelected()) {
                    checkBox.click();
                    Util.waitInSeconds(TimeConstants.ONE_SEC);
                }
            }

        }
    }

    /**
     * Click on the Update Permission button to update Permission.
     *
     * @return Role Detail Page
     */
    public RoleDetailPage clickOnUpdatePermission() {

        updatePermissions.click();
        Util.waitInSeconds(TimeConstants.THREE_SEC);
        return super.getPage(RoleDetailPage.class);
    }

    /**
     * Click on the cancel button of the set role permission to cancel. Note: This method is written so that any test
     * class can use this
     */
    public void clickOnCancel() {
        cancel();
    }

    /**
     * Returns all Permission Elements from Page
     *
     * @return Permission rows
     */
    private Map<String, WebElement> getPermissions() {
        final Map<String, WebElement> permission = new HashMap<>();
        final String checkBoxSelector = ".//td[@class='idx']/following-sibling::td[1]/input[@type='checkbox']";
        final String permissionSelector = ".//td[@class='idx']/following-sibling::td[2]";

        final List<WebElement> checkBoxes = getDriver().findElements(By.xpath(checkBoxSelector));
        final List<WebElement> permissionRow = getDriver().findElements(By.xpath(permissionSelector));

        LOGGER.info("Total number of Permissions = " + permissionRow.size());
        LOGGER.info("Total number of Checkboxes = " + checkBoxes.size());

        for (int i = 0; i < permissionRow.size(); i++) {
            permission.put(permissionRow.get(i).getText(), checkBoxes.get(i));
        }

        return permission;
    }

}
