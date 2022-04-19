package com.autodesk.bsm.pelican.util;

import com.autodesk.bsm.pelican.api.pojos.DRSubscription;
import com.autodesk.bsm.pelican.api.pojos.DRWIPData;
import com.autodesk.bsm.pelican.api.pojos.json.ProductLine;
import com.autodesk.bsm.pelican.api.pojos.json.ProductLineData;
import com.autodesk.bsm.pelican.api.pojos.subscription.Subscription;
import com.autodesk.bsm.pelican.constants.PelicanConstants;
import com.autodesk.bsm.pelican.constants.PelicanDbConstants;
import com.autodesk.bsm.pelican.enums.BuyerFieldsInDb;
import com.autodesk.bsm.pelican.enums.EntityType;
import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a class which will mainly query the database with a desired query and return the required entities from the
 * database.
 */
public class DbUtils {

    private static String JDBC_DRIVER;
    private static Connection dbConnection = null;
    private static Connection databaseConnection = null;
    private static Statement dbStatement = null;
    private static String sqlQuery;
    private static ResultSet resultSet;
    private static int recordsCount;
    private static final String MIGRATION_START = "18";
    private static final Logger LOGGER = LoggerFactory.getLogger(DbUtils.class.getSimpleName());

    /**
     * Get the count of the total number of subscription plans in the database whose status is cancelled, active and
     * expired.
     */
    public static int getTotalNumberOfSubscriptionPlan(final EnvironmentVariables environmentVariables) {

        try {
            databaseConnection = DbUtils.getDbConnection(environmentVariables);
            LOGGER.info("Executing the sql query against database");
            dbStatement = databaseConnection.createStatement();

            // SQL Query to return the count of the subscription plans in the
            // data base for the auto family
            sqlQuery = "select count(1) from  offering where app_family_id = " + environmentVariables.getAppFamilyId()
                + " and offering_type in (0,3)";
            LOGGER.info("SQL Query " + sqlQuery);
            resultSet = dbStatement.executeQuery(sqlQuery);
            LOGGER.info("Fetching the data from the result set");
            while (resultSet.next()) {
                recordsCount = resultSet.getInt(1);
            }
        } catch (final SQLException sqle) {
            sqle.printStackTrace();
        }

        return recordsCount;
    }

    /**
     * Get the count of the total number of basic offerings in the database whose status is new, active or canceled
     * depending on parameters.
     */
    public static int getTotalNumberOfBasicOfferings(final String statuses,
        final EnvironmentVariables environmentVariables) {

        try {
            databaseConnection = DbUtils.getDbConnection(environmentVariables);
            LOGGER.info("Executing the sql query against database");
            dbStatement = databaseConnection.createStatement();

            // SQL Query to return the count of the subscription plans in the
            // data base for the auto family
            sqlQuery = "select count(1) from  offering where app_family_id = " + environmentVariables.getAppFamilyId()
                + " and offering_type not in (0,3) and status in (" + statuses + ")";
            LOGGER.info("SQL Query " + sqlQuery);
            resultSet = dbStatement.executeQuery(sqlQuery);
            LOGGER.info("Fetching the data from the result set");
            while (resultSet.next()) {
                recordsCount = resultSet.getInt(1);
            }
        } catch (final SQLException sqle) {
            sqle.printStackTrace();
        }

        return recordsCount;
    }

    /**
     * Get the count of the total number of subscription plans in the database whose status is ACTIVE, NEW or CANCELED
     * depending on the parameter passed.
     *
     * @param environmentVariables TODO
     * @param subscriptionPlanStatus - status
     * @return int recordsCount
     */
    public static int getTotalNumberOfSubscriptionPlans(final int subscriptionPlanStatus,
        final EnvironmentVariables environmentVariables) {

        try {
            databaseConnection = DbUtils.getDbConnection(environmentVariables);
            LOGGER.info("Executing the sql query against database");
            dbStatement = databaseConnection.createStatement();

            // SQL Query to return the count of the subscription plans in the
            // data base for the auto family
            sqlQuery = "select count(1) from  offering where app_family_id = " + environmentVariables.getAppFamilyId()
                + " and offering_type in (0,3) and status = " + subscriptionPlanStatus;
            LOGGER.info("SQL Query " + sqlQuery);
            resultSet = dbStatement.executeQuery(sqlQuery);
            LOGGER.info("Fetching the data from the result set");
            while (resultSet.next()) {
                recordsCount = resultSet.getInt(1);
            }

        } catch (final SQLException sqle) {
            sqle.printStackTrace();
        }
        return recordsCount;
    }

    /**
     * This is a method to get the promotion details.
     *
     * @param promotionId - promotion id
     * @param environmentVariables - environment variables
     * @return number of times a promotion is used
     */
    public static int getPromotionDetails(final String promotionId, final EnvironmentVariables environmentVariables) {
        int timesUsed = 0;

        try {
            databaseConnection = DbUtils.getDbConnection(environmentVariables);
            LOGGER.info("Executing the sql query against database");
            dbStatement = databaseConnection.createStatement();
            // SQl query to return the id of a subscription plan which doesn't
            // have a price and offer.
            sqlQuery = "select count(*) from userpromotionuse where PROMOTION_ID=" + promotionId;
            LOGGER.info("SQL Query " + sqlQuery);
            resultSet = dbStatement.executeQuery(sqlQuery);
            LOGGER.info("Fetching the data from the result set");

            while (resultSet.next()) {
                timesUsed = resultSet.getInt(1);
            }
            LOGGER.info("times used is " + timesUsed);

        } catch (final SQLException sqle) {
            sqle.printStackTrace();
        }
        return timesUsed;
    }

    /**
     * Method to get upload file error message.
     *
     * @param id (job id)
     * @param environmentVariables
     * @return errorMsg (upload file error message)
     */
    public static String getUploadErrorMessage(final String id, final EnvironmentVariables environmentVariables)
        throws SQLException {
        databaseConnection = DbUtils.getDbConnection(environmentVariables);
        LOGGER.info("Executing the sql query against database");
        dbStatement = databaseConnection.createStatement();
        // SQl query to return the uploaded file job id.
        sqlQuery = "select * from file_upload_job where ID=" + id;
        LOGGER.info("SQL Query " + sqlQuery);
        resultSet = dbStatement.executeQuery(sqlQuery);
        LOGGER.info("Fetching the data from the result set");
        String errorMsg = null;
        while (resultSet.next()) {
            errorMsg = resultSet.getString("ERRORS");
        }
        LOGGER.info("Error message is " + errorMsg);
        return errorMsg;
    }

    /**
     * This is a method to update buyer detail in purchase order.
     *
     * @param purchaseOrderId - PO Id
     * @param field - field name
     * @param value - value
     * @param environmentVariables - environment variables
     * @return boolean
     */
    public static boolean updateBuyerDetailInPurchaseOrder(final String purchaseOrderId, final BuyerFieldsInDb field,
        final String value, final EnvironmentVariables environmentVariables) {
        boolean rowUpdated = false;
        try {
            databaseConnection = DbUtils.getDbConnection(environmentVariables);
            LOGGER.info("Executing the sql query against database");
            dbStatement = databaseConnection.createStatement();
            // SQl query to update the buyer user fields in purchase_order table
            // in tempestDb
            // It will update only in Application family 2001 i.e Auto!
            sqlQuery = "UPDATE purchase_order SET " + field + " = '" + value + "' WHERE ID='" + purchaseOrderId
                + "' and APPF_ID =" + environmentVariables.getAppFamilyId();
            LOGGER.info("SQL Query " + sqlQuery);
            final int resultSet = dbStatement.executeUpdate(sqlQuery);
            LOGGER.info("Fetching the data from the result set");
            if (resultSet > 0) {
                rowUpdated = true;
            }
            LOGGER.info("times used is " + resultSet);
        } catch (final SQLException sqle) {
            sqle.printStackTrace();
        }

        return rowUpdated;

    }

    /**
     * This is a method to get po from subscription.
     *
     * @param subscriptionId - subscription id
     * @param environmentVariables - environmentVariables
     * @return List - count
     */
    public static List<Integer> getPurchaseOrderfromSubscription(final String subscriptionId,
        final EnvironmentVariables environmentVariables) {

        final List<Integer> count = new ArrayList<>();
        try {
            databaseConnection = DbUtils.getDbConnection(environmentVariables);

            LOGGER.info("Executing the sql query against database");
            dbStatement = databaseConnection.createStatement();

            // SQl query to get the Purchase Order from Subscription table in
            // tempestDb
            // @param is Subscription id
            sqlQuery = "select purchase_order_id from subscription where app_family_id='"
                + environmentVariables.getAppFamilyId() + "' and id='" + subscriptionId + "'";

            LOGGER.info("SQL Query: " + sqlQuery);

            resultSet = dbStatement.executeQuery(sqlQuery);
            LOGGER.info("Fetching the data from the result set");

            while (resultSet.next()) {
                count.add(resultSet.getInt(1));
            }

        } catch (final SQLException sqle) {
            sqle.printStackTrace();
        }

        LOGGER.info("Total # of Purchase Orders Found: " + count.size());
        return count;
    }

    /**
     * Return all the Roles, listed in sec_role table
     *
     * @param environmentVariables TODO
     * @return HashMap: String, is the Role Name and Integer, role ID.
     */
    public static HashMap<String, String> getAllRoles(final EnvironmentVariables environmentVariables) {

        final HashMap<String, String> roleDetails = new HashMap<>();
        try {
            databaseConnection = DbUtils.getDbConnection(environmentVariables);

            LOGGER.info("Executing the sql query against database");
            dbStatement = databaseConnection.createStatement();

            sqlQuery = "select NAME, id from sec_role";

            LOGGER.info("SQL Query for Get Roles: " + sqlQuery);

            resultSet = dbStatement.executeQuery(sqlQuery);
            LOGGER.info("Fetching the data from the result set");

            while (resultSet.next()) {
                roleDetails.put(resultSet.getString("NAME"), resultSet.getString("ID"));
            }

        } catch (final SQLException sqle) {
            sqle.printStackTrace();
        }

        LOGGER.info("Total # of Roles Found in DB: " + roleDetails.size());
        return roleDetails;
    }

    /**
     * This method updates the table in Database.
     *
     * @param environmentVariables TODO
     */
    public static void updateTableInDb(final String table, final String column, final String updateString,
        final String conditionColumn, final String conditionString, final EnvironmentVariables environmentVariables) {

        try {
            databaseConnection = DbUtils.getDbConnection(environmentVariables);
            LOGGER.info("Executing the sql query against database");
            dbStatement = databaseConnection.createStatement();

            // SQL Query to update table in data base for the auto family
            sqlQuery = "update " + table + " set " + column + " = " + updateString + " where " + conditionColumn + " = "
                + conditionString;
            LOGGER.info("SQL Query : " + sqlQuery);
            final int updatedRows = dbStatement.executeUpdate(sqlQuery);
            if (updatedRows == 0) {
                throw new SQLException("No tables are updated");
            } else {
                LOGGER.info("Updated the database");
            }

        } catch (final SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    /**
     * This is the method to update any table in workers DB.
     *
     * @param query
     * @param environmentVariables
     * @return int - count of records affected
     */
    public static int insertOrUpdateQueryFromWorkerDb(final String query,
        final EnvironmentVariables environmentVariables) {
        try {
            databaseConnection = DbUtils.getTriggersDbConnection(environmentVariables);
            LOGGER.info("Executing the sql query against database");
            dbStatement = databaseConnection.createStatement();
            LOGGER.info("Query: " + query);
            final int rows = dbStatement.executeUpdate(query);
            LOGGER.info("No. of inserted/updated database rows : " + rows);

            return rows;

        } catch (final SQLException sqle) {
            sqle.printStackTrace();
        }

        return 0;
    }

    /**
     * This is the method to update any table in tempest DB.
     *
     * @param updateQuery - update query
     * @param environmentVariables - environmentVariables
     */
    public static void updateQuery(final String updateQuery, final EnvironmentVariables environmentVariables) {

        try {
            databaseConnection = DbUtils.getDbConnection(environmentVariables);
            LOGGER.info("Executing the sql query against database");
            dbStatement = databaseConnection.createStatement();
            LOGGER.info("Update Query: " + updateQuery);
            final int updatedRows = dbStatement.executeUpdate(updateQuery);
            LOGGER.info("Updated the database for rows : " + updatedRows);

        } catch (final SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    /**
     * Return the database connection.
     */
    public static Connection getDbConnection(final EnvironmentVariables environmentVariables) {

        try {

            // Database Driver url
            JDBC_DRIVER = environmentVariables.getJDBCDriver();
            final String DB_URL = environmentVariables.getDbUrl();
            // Database credentials
            final String dbUsername = environmentVariables.getDbUsername();
            final String dbPassword = environmentVariables.getDbPassword();

            // STEP 2: Register JDBC driver
            Class.forName(JDBC_DRIVER);

            // STEP 3: Open a connection
            LOGGER.info("Connecting to database..." + DB_URL);

            dbConnection = DriverManager.getConnection(DB_URL, dbUsername, dbPassword);

        } catch (ClassNotFoundException | SQLException ce) {
            ce.printStackTrace();
        }

        return dbConnection;
    }

    /**
     * Return triggers database connection.
     */
    private static Connection getTriggersDbConnection(final EnvironmentVariables environmentVariables) {

        try {
            // Database Driver url
            JDBC_DRIVER = environmentVariables.getJDBCDriver();
            final String TRIGGERS_DB_URL = environmentVariables.getTriggersDBUrl();
            // Database credentials
            final String triggersDBUsername = environmentVariables.getTriggersDBUsername();
            final String triggersDBPassword = environmentVariables.getTriggersDBPassword();

            // STEP 2: Register JDBC driver
            Class.forName(JDBC_DRIVER);

            // STEP 3: Open a connection
            LOGGER.info("Connecting to triggers database..." + TRIGGERS_DB_URL);

            dbConnection = DriverManager.getConnection(TRIGGERS_DB_URL, triggersDBUsername, triggersDBPassword);

        } catch (ClassNotFoundException | SQLException ce) {
            LOGGER.error(ExceptionUtils.getFullStackTrace(ce));
        }
        return dbConnection;
    }

    /**
     * Get DR Migration upload job status from job_statuses table in triggers database.
     */
    public static int getDRMigrationJobStatusFromJobStatusesTable(final EnvironmentVariables environmentVariables,
        final String category) {
        try {
            databaseConnection = DbUtils.getTriggersDbConnection(environmentVariables);
            LOGGER.info("Executing the sql query against triggers database");
            dbStatement = databaseConnection.createStatement();

            // SQL Query to return the count of the product lines in the
            // triggers database
            sqlQuery =
                "select state from  job_statuses where category = " + category + " order by created desc LIMIT 1";
            LOGGER.info("SQL Query: " + sqlQuery);
            resultSet = dbStatement.executeQuery(sqlQuery);
            LOGGER.info("Fetching the data from the result set");
            while (resultSet.next()) {
                recordsCount = resultSet.getInt(1);
            }
        } catch (final SQLException sqle) {
            LOGGER.error(ExceptionUtils.getFullStackTrace(sqle));
        }
        return recordsCount;
    }

    /**
     * Get the upload job status from job_statuses table in triggers database.
     */
    private static int getLatestJobIdFromJobStatusesTable(final EnvironmentVariables environmentVariables,
        final String category) {
        try {
            databaseConnection = DbUtils.getTriggersDbConnection(environmentVariables);
            LOGGER.info("Executing the sql query against triggers database");
            dbStatement = databaseConnection.createStatement();

            // SQL Query to return the count of the product lines in the
            // triggers database
            sqlQuery = "select id from  job_statuses where category = " + category + " order by created desc LIMIT 1";
            LOGGER.info("SQL Query: " + sqlQuery);
            resultSet = dbStatement.executeQuery(sqlQuery);
            LOGGER.info("Fetching the data from the result set");
            while (resultSet.next()) {
                recordsCount = resultSet.getInt(1);
            }
        } catch (final SQLException sqle) {
            LOGGER.error(ExceptionUtils.getFullStackTrace(sqle));
        }
        return recordsCount;
    }

    /**
     * Get the status of each record (i.e. under same job_status_id) in WIP in the triggers database.
     */
    public static List<Integer> getLatestJobRecordsStatusFromWip(final EnvironmentVariables environmentVariables,
        final String category) {
        final List<Integer> jobStatuses = new ArrayList<>();
        try {
            databaseConnection = DbUtils.getTriggersDbConnection(environmentVariables);
            LOGGER.info("Executing the sql query against triggers database");
            dbStatement = databaseConnection.createStatement();
            final int jobStatusId = getLatestJobIdFromJobStatusesTable(environmentVariables, category);

            // SQL Query to return the count of the product lines in the
            // triggers database
            sqlQuery = "select state from work_in_progress where job_status_id =" + jobStatusId;
            LOGGER.info("SQL Query " + sqlQuery);
            resultSet = dbStatement.executeQuery(sqlQuery);
            LOGGER.info("Fetching the data from the result set");
            while (resultSet.next()) {
                jobStatuses.add(resultSet.getInt(1));
            }
        } catch (final SQLException sqle) {
            LOGGER.error(ExceptionUtils.getFullStackTrace(sqle));
        }
        return jobStatuses;
    }

    /**
     * Get the latest job data from work_in_progress table in the triggers database.
     *
     * @param subscriptionId - subscription id
     */
    public static DRWIPData getLatestJobDataFromWip(final Integer subscriptionId,
        final EnvironmentVariables environmentVariables) {
        DRWIPData wipData = null;
        try {
            databaseConnection = DbUtils.getTriggersDbConnection(environmentVariables);
            LOGGER.info("Executing the sql query against triggers database");
            dbStatement = databaseConnection.createStatement();

            // SQL Query to return the count of the product lines in the
            // triggers database
            sqlQuery = "select object_id, state, job_status_id, notes from work_in_progress" + " where object_id = "
                + subscriptionId + " order by id desc limit 1";
            LOGGER.info("SQL Query " + sqlQuery);
            resultSet = dbStatement.executeQuery(sqlQuery);
            LOGGER.info("Fetching the data from the result set");
            while (resultSet.next()) {
                wipData = new DRWIPData();
                wipData.setObjectId(resultSet.getInt(1));
                wipData.setState(resultSet.getInt(2));
                wipData.setJobStatusId(resultSet.getInt(3));
                wipData.setNotes(resultSet.getString(4));
            }
        } catch (final SQLException sqle) {
            LOGGER.error(ExceptionUtils.getFullStackTrace(sqle));
        }
        return wipData;
    }

    /**
     * Get the data from mig_subscription table.
     *
     * @param inputSubIds - Subscription id list
     */
    public static List<DRSubscription> getDataFromMigSubscriptionTable(final List<String> inputSubIds,
        final EnvironmentVariables environmentVariables) {
        final List<DRSubscription> drSubs = new ArrayList<>();
        try {
            databaseConnection = DbUtils.getTriggersDbConnection(environmentVariables);
            LOGGER.info("Executing the sql query against triggers database");
            dbStatement = databaseConnection.createStatement();

            // SQL Query to return the count of the product lines in the
            // triggers database
            sqlQuery = "select product_name, locale_id, sku_code, subscription_id, shopping_currency_code,"
                + "contract_term, quantity from mig_subscription where subscription_id in " + "("
                + Util.getString(inputSubIds) + ") order by created desc limit " + inputSubIds.size();
            LOGGER.info("SQL Query " + sqlQuery);
            resultSet = dbStatement.executeQuery(sqlQuery);
            LOGGER.info("Fetching the data from the result set");
            while (resultSet.next()) {
                final DRSubscription drSub = new DRSubscription();
                drSub.setProductName(resultSet.getString(1));
                drSub.setLocale(resultSet.getString(2));
                drSub.setSku(resultSet.getString(3));
                drSub.setSubscriptionId(Double.valueOf(resultSet.getString(4)).intValue());
                drSub.setCurrency(com.autodesk.bsm.pelican.enums.Currency.valueOf(resultSet.getString(5)));
                drSub.setContractTerm(resultSet.getString(6));
                drSub.setQuantity(Double.parseDouble(resultSet.getString(7)));
                drSubs.add(drSub);
            }
        } catch (final SQLException sqle) {
            LOGGER.error(ExceptionUtils.getFullStackTrace(sqle));
        }
        return drSubs;
    }

    /**
     * Get the id of the matching descriptor from the descriptor table.
     *
     * @return id of the matching descriptor in descriptor table
     */
    public static String getOfferDescriptorIdFromTable(final String offerId,
        final EnvironmentVariables environmentVariables) {
        return DbUtils.selectQuery("Select ID from descriptor where ENTITY_ID = " + offerId + " order by ID desc", "ID",
            environmentVariables).get(0);
    }

    /**
     * Generic Select query which returns only one value.
     *
     * @param sqlQuery : you need to pass the SQL query
     * @param field : pass the table column for which you are looking for
     * @param environmentVariables TODO
     * @return value of the field from table
     */
    public static List<String> selectQuery(final String sqlQuery, final String field,
        final EnvironmentVariables environmentVariables) {

        final List<String> resultList = new ArrayList<>();
        try {
            LOGGER.info("Executing the sql query:" + sqlQuery + "\nFetching value of the " + field);
            databaseConnection = DbUtils.getDbConnection(environmentVariables);
            dbStatement = databaseConnection.createStatement();
            resultSet = dbStatement.executeQuery(sqlQuery);
            LOGGER.info("Fetching the data from the result set");

            while (resultSet.next()) {
                final String value = resultSet.getString(field);

                resultList.add(value);
            }
        } catch (final SQLException sqle) {
            sqle.printStackTrace();
        }
        return resultList;
    }

    /**
     * Generic Select query which returns all rows and column from tempestDB
     *
     * @param sqlQuery : you need to pass the SQL query
     * @return List. Map is key,value pair for the column and list size is equal to number of rows returned by the
     *         query.
     */
    public static List<Map<String, String>> selectQuery(final String sqlQuery,
        final EnvironmentVariables environmentVariables) {
        final List<Map<String, String>> resultMapList = new ArrayList<>();
        try {
            LOGGER.info("Executing the sql query:" + sqlQuery);
            databaseConnection = DbUtils.getDbConnection(environmentVariables);
            dbStatement = databaseConnection.createStatement();
            resultSet = dbStatement.executeQuery(sqlQuery);
            LOGGER.info("Fetching the data from the result set");
            final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            final int columnCount = resultSetMetaData.getColumnCount();
            Map<String, String> resultHashMap;
            while (resultSet.next()) {
                resultHashMap = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    resultHashMap.put(resultSetMetaData.getColumnName(i), resultSet.getString(i));
                }
                resultMapList.add(resultHashMap);
            }

        } catch (final SQLException sqle) {
            sqle.printStackTrace();
        }
        return resultMapList;
    }

    /**
     * Get the subscription entitlement id (tempest database).
     *
     * @param environmentVariables TODO
     * @return subscription entitlement id
     */
    public static String getEntitlementIdFromItemId(final String subscriptionPlanId, final String featureId,
        final EnvironmentVariables environmentVariables) {
        final List<String> resultList = DbUtils.selectQuery(
            String.format(PelicanDbConstants.SELECT_ID_SUBSCRIPTION_ENTITLEMENT, subscriptionPlanId, featureId),
            PelicanConstants.ID_FIELD, environmentVariables);

        if (resultList == null || resultList.size() == 0) {
            return null;
        } else {
            return resultList.get(0);
        }
    }

    /**
     * Get the subscription entitlement id (tempest database).
     *
     * @param environmentVariables TODO
     * @return subscription entitlement id
     */
    public static String getEntitlementId(final String subscriptionPlanId,
        final EnvironmentVariables environmentVariables) {
        final List<String> resultList = DbUtils.selectQuery(
            String.format(PelicanDbConstants.SELECT_SINGLE_ID_SUBSCRIPTION_ENTITLEMENT, subscriptionPlanId),
            PelicanConstants.ID_FIELD, environmentVariables);

        if (resultList == null || resultList.size() == 0) {
            return null;
        } else {
            return resultList.get(0);
        }
    }

    /**
     * This is a method to return all the ids of subscription entitlements for a plan
     *
     * @param subscriptionPlanId
     * @param featureId
     * @param environmentVariables
     * @return entitlement ids as List<String>
     */
    public static List<String> getAllEntitlementIdsForPlan(final String subscriptionPlanId, final String featureId,
        final EnvironmentVariables environmentVariables) {
        final List<String> resultList = DbUtils.selectQuery(
            String.format(PelicanDbConstants.SELECT_ID_SUBSCRIPTION_ENTITLEMENT, subscriptionPlanId, featureId),
            PelicanConstants.ID_FIELD, environmentVariables);

        if (resultList == null || resultList.size() == 0) {
            return null;
        } else {
            return resultList;
        }
    }

    /**
     * Get the Currency entitlement id
     *
     * @param subscriptionPlanId
     * @param environmentVariables
     * @return
     */
    public static String getCurrencyEntitlementId(final String subscriptionPlanId,
        final EnvironmentVariables environmentVariables) {
        final List<String> resultList =
            DbUtils.selectQuery(String.format(PelicanDbConstants.SELECT_ID_CURRENCY_ENTITLEMENT, subscriptionPlanId),
                PelicanConstants.ID_FIELD, environmentVariables);

        if (resultList == null || resultList.size() == 0) {
            return null;
        } else {
            return resultList.get(0);
        }
    }

    /**
     * This is a method to return offering id with known offering external key
     *
     * @param subscriptionPlanExtKey
     * @param environmentVariables
     * @return offering id as String
     */
    public static String getOfferingId(final String subscriptionPlanExtKey,
        final EnvironmentVariables environmentVariables) {
        final List<Map<String, String>> offeringSQLResult =
            DbUtils.selectQuery(String.format(PelicanDbConstants.SELECT_ID_OFFERING, subscriptionPlanExtKey,
                environmentVariables.getAppFamilyId()), environmentVariables);

        return offeringSQLResult.get(0).get("ID");
    }

    /**
     * Generic Select query which returns all rows and column from Workers DB
     *
     * @param sqlQuery : you need to pass the SQL query
     * @return List. Map is key,value pair for the column and list size is equal to number of rows returned by the
     *         query.
     */
    public static List<Map<String, String>> selectQueryFromWorkerDb(final String sqlQuery,
        final EnvironmentVariables environmentVariables) {
        final List<Map<String, String>> resultMapList = new ArrayList<>();
        try {
            LOGGER.info("Executing the sql query:" + sqlQuery);
            databaseConnection = DbUtils.getTriggersDbConnection(environmentVariables);
            dbStatement = databaseConnection.createStatement();
            resultSet = dbStatement.executeQuery(sqlQuery);
            LOGGER.info("Fetching the data from the result set");
            final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            final int columnCount = resultSetMetaData.getColumnCount();
            Map<String, String> resultHashMap;
            while (resultSet.next()) {
                resultHashMap = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    resultHashMap.put(resultSetMetaData.getColumnName(i), resultSet.getString(i));
                }
                resultMapList.add(resultHashMap);
            }

        } catch (final SQLException sqle) {
            sqle.printStackTrace();
        }
        return resultMapList;
    }

    /**
     * This method is used for select query with 2 conditions separated with AND Ex: select id from table1 where column1
     * = '1' and 'column2' = 2; where column1 and column2 relations are conditions.
     *
     * @param environmentVariables TODO
     * @return String, referene_id
     */
    public static String selectQuery(final String table, final String requiredColumn, final String conditionColumn1,
        final String value1, final String conditionColumn2, final String value2,
        final EnvironmentVariables environmentVariables) {

        String referenceId = "";
        try {
            databaseConnection = DbUtils.getDbConnection(environmentVariables);
            LOGGER.info("Executing the sql query against database");
            dbStatement = databaseConnection.createStatement();

            // SQL Query to update table in data base for the auto family
            sqlQuery = "select " + requiredColumn + " from " + table + " where " + conditionColumn1 + " = " + value1
                + " and " + conditionColumn2 + " = " + value2 + " LIMIT 1";
            LOGGER.info("SQL Query : " + sqlQuery);
            resultSet = dbStatement.executeQuery(sqlQuery);
            LOGGER.info("Fetching the data from the result set");
            while (resultSet.next()) {
                referenceId = resultSet.getString(1);
            }
        } catch (final SQLException sqle) {
            sqle.printStackTrace();
        }
        return referenceId;
    }

    /**
     * Get the product line data from product_line table in tempest database.
     *
     * @param productLineExtKey - Product line external key
     * @return productLine
     */
    public static ProductLine getProductLineData(final String productLineExtKey,
        final EnvironmentVariables environmentVariables) {
        ProductLine productLine = null;
        try {
            databaseConnection = DbUtils.getDbConnection(environmentVariables);
            LOGGER.info("Executing the sql query against tempest database");
            dbStatement = databaseConnection.createStatement();

            // SQL Query to return the product line data in the tempest database
            sqlQuery = "select * from product_line where app_family_id = " + environmentVariables.getAppFamilyId()
                + " AND external_key = '" + productLineExtKey + "'";
            LOGGER.info("SQL Query: " + sqlQuery);
            resultSet = dbStatement.executeQuery(sqlQuery);
            LOGGER.info("Fetching the data from the result set");
            while (resultSet.next()) {
                productLine = new ProductLine();
                final ProductLineData productLineData = new ProductLineData();
                productLineData.setId(resultSet.getString(1));
                productLineData.setType(EntityType.PRODUCT_LINE.name());
                productLineData.setName(resultSet.getString(4));
                productLineData.setExternalKey(resultSet.getString(5));
                productLine.setData(productLineData);
            }
        } catch (final SQLException sqle) {
            LOGGER.error(ExceptionUtils.getFullStackTrace(sqle));
        }
        return productLine;
    }

    /**
     * Get the subscription data from subscription table in tempest database.
     *
     * @param subscriptionExtKey - Subscription external key
     * @return subscription
     */
    public static Subscription getSubscriptionData(final String subscriptionExtKey,
        final EnvironmentVariables environmentVariables) {
        Subscription subscription = null;
        try {
            databaseConnection = DbUtils.getDbConnection(environmentVariables);
            LOGGER.info("Executing the sql query against tempest database");
            dbStatement = databaseConnection.createStatement();

            // SQL Query to return the subscription data in the tempest database
            sqlQuery = "select * from subscription where app_family_id = " + environmentVariables.getAppFamilyId()
                + " AND external_key = '" + subscriptionExtKey + "'";
            LOGGER.info("SQL Query: " + sqlQuery);
            resultSet = dbStatement.executeQuery(sqlQuery);
            LOGGER.info("Fetching the data from the result set");
            while (resultSet.next()) {
                subscription = new Subscription();
                subscription.setId(resultSet.getString(1));
                subscription.setStatus(resultSet.getString(8));
                subscription.setNextBillingPriceId(resultSet.getString(27));
                subscription.setQuantity(Integer.valueOf(resultSet.getString(29)));
                subscription.setExternalKey(resultSet.getString(30));
            }
        } catch (final SQLException sqle) {
            LOGGER.error(ExceptionUtils.getFullStackTrace(sqle));
        }
        return subscription;
    }

    /**
     * Get total number of subscriptions which have a migration started activity (tempest database).
     *
     * @return recordsCount
     */
    public static int totalSubscriptionUploaded(final EnvironmentVariables environmentVariables) throws SQLException {

        databaseConnection = DbUtils.getDbConnection(environmentVariables);
        LOGGER.info("Executing the sql query against tempest database");
        dbStatement = databaseConnection.createStatement();

        sqlQuery = "select count(1) from subscription_event se,subscription su" + " where su.id=se.SUBSCRIPTION_ID and"
            + " se.type = " + MIGRATION_START + " and su.APP_FAMILY_ID = " + environmentVariables.getAppFamilyId();
        LOGGER.info("SQL Query: " + sqlQuery);
        resultSet = dbStatement.executeQuery(sqlQuery);
        LOGGER.info("Fetching the data from the result set");
        while (resultSet.next()) {
            recordsCount = resultSet.getInt(1);
        }
        return recordsCount;
    }

    /**
     * Verify if the Value of the Secure Key's is flagged in midas_hive table.
     *
     * @param secureKeys - List of Keys as Strings type
     * @return true/false based on success/failure
     */
    public static boolean isPasswordFlaggedInMidas(final List<String> secureKeys,
        final EnvironmentVariables environmentVariables) {
        boolean keysAreSecure = false;
        try {
            String keys = "";
            databaseConnection = DbUtils.getDbConnection(environmentVariables);
            LOGGER.info("Executing the sql query against tempest database");
            dbStatement = databaseConnection.createStatement();
            LOGGER.info("Size of List to compare with :" + secureKeys.size());
            for (final String secureKey : secureKeys) {
                keys = keys + "\"" + secureKey + "\",";
            }

            keys = keys.substring(0, keys.length() - 1);

            sqlQuery = "select count(IS_CREDENTIAL) from midas_hive where K in(" + keys + " )  and IS_CREDENTIAL=1;";
            LOGGER.info("SQL Query: " + sqlQuery);
            resultSet = dbStatement.executeQuery(sqlQuery);
            LOGGER.info("Fetching the data from the result set");
            while (resultSet.next()) {
                keysAreSecure = (resultSet.getInt(1) == secureKeys.size());
            }
        } catch (final SQLException sqle) {
            LOGGER.error(ExceptionUtils.getFullStackTrace(sqle));
        }
        return keysAreSecure;
    }

    /**
     * Get the subscription entitlement id (tempest database).
     *
     * @param environmentVariables TODO
     * @return subscription entitlement id
     */
    public static String getSubscriptionEntitlementId(final String subscriptionPlanId,
        final EnvironmentVariables environmentVariables) {
        return DbUtils.selectQuery(
            "Select ID from subscription_entitlement where RELATED_ID=" + subscriptionPlanId + " order by ID desc",
            "ID", environmentVariables).get(0);
    }

    /**
     * Get the licensing model id (tempest database).
     *
     * @return licensing model id
     */
    public static String getLicensingModelId(final String appFamilyId, final String licensingModelExternalKey,
        final EnvironmentVariables environmentVariables) {
        return DbUtils.selectQuery("Select ID from licensing_model where EXTERNAL_KEY='" + licensingModelExternalKey
            + "' and App_Family_Id=" + appFamilyId, "ID", environmentVariables).get(0);
    }

    /**
     * Get Value from Midas_hive.
     *
     * @return value
     */
    public static String getMidasHiveValue(final String key, final EnvironmentVariables environmentVariables) {
        return DbUtils
            .selectQuery(String.format(PelicanDbConstants.SELECT_VALUE_FROM_MIDAS_HIVE, key), environmentVariables)
            .get(0).get(PelicanDbConstants.VALUE);
    }

    /**
     * This is a method to update eos and eol dates in subscription_entitlement table.
     *
     * @param eos
     * @param eolImmediate
     * @param eolRenewal
     * @param entitlementId
     * @param environmentVariables
     * @return boolean
     */
    public static void updateRemoveFeatureDates(final String eos, final String eolImmediate, final String eolRenewal,
        final String entitlementId, final EnvironmentVariables environmentVariables) {
        String sqlQuery = null;
        if (StringUtils.isNotEmpty(eos)) {
            sqlQuery = String.format(PelicanDbConstants.UPDATE_EOS_SUBSCRIPTION_ENTITLEMENT, eos, entitlementId);
            DbUtils.updateQuery(sqlQuery, environmentVariables);
        }
        if (StringUtils.isNotEmpty(eolImmediate)) {
            sqlQuery =
                String.format(PelicanDbConstants.UPDATE_EOL_IMME_SUBSCRIPTION_ENTITLEMENT, eolImmediate, entitlementId);
            DbUtils.updateQuery(sqlQuery, environmentVariables);
        }
        if (StringUtils.isNotEmpty(eolRenewal)) {
            sqlQuery = String.format(PelicanDbConstants.UPDATE_EOL_RENEWAL_SUBSCRIPTION_ENTITLEMENT, eolRenewal,
                entitlementId);
            DbUtils.updateQuery(sqlQuery, environmentVariables);
        }
    }

    /**
     * returns records from work_in_progress table
     *
     * @param environmentVariables
     * @param jobId
     * @return List<Map<String, String>>
     */
    public static List<Map<String, String>> getRecordsFromWip(final EnvironmentVariables environmentVariables,
        final String jobId) {
        final List<Map<String, String>> resultList = DbUtils.selectQueryFromWorkerDb(
            String.format(PelicanDbConstants.SELECT_WORK_IN_PROGRESS_WITH_JOB_STATUS_ID, jobId), environmentVariables);

        return resultList;
    }

    /**
     * get Job id from job_statuses table
     *
     * @param environmentVariables
     * @param guid
     *
     * @return String
     */
    public static String getIdFromGuidInJobStatuses(final EnvironmentVariables environmentVariables,
        final String guid) {

        final List<Map<String, String>> resultList = DbUtils.selectQueryFromWorkerDb(
            String.format(PelicanDbConstants.SELECT_JOB_STATUSES_WITH_GUID, guid), environmentVariables);

        return resultList.get(0).get(PelicanConstants.ID);
    }
}
