package com.autodesk.bsm.pelican.datacleanup;

import com.autodesk.bsm.pelican.ui.entities.EnvironmentVariables;
import com.autodesk.bsm.pelican.util.DbUtils;
import com.autodesk.bsm.pelican.util.PelicanEnvironment;
import com.autodesk.bsm.pelican.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DataCleanupStoreProcedureScript {

    private EnvironmentVariables environmentVariables;
    private static int iteration = 1;
    private static final Logger LOGGER = LoggerFactory.getLogger(DataCleanupStoreProcedureScript.class.getSimpleName());

    /**
     * Data setup
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() {
        environmentVariables = new PelicanEnvironment().initializeEnvironmentVariables();
    }

    @Test
    public void test_dataCleanupStoreProcedureScript() throws SQLException {
        int status;
        // Create a jdbc connection with tempestdb
        final Connection connection = DbUtils.getDbConnection(environmentVariables);
        // Create the SQL procedure in tempestdb
        status = createProcedure(connection);
        // if the create procedure is success, Call the procedure with parameters
        if (status == 0) {
            LOGGER.info("Status is 0");
            status = callProcedure(connection);
        }
        connection.close();
    }

    private static int createProcedure(final Connection con) {

        String storeprocedurequery = null;
        final String sqlScriptPath =
            System.getProperty("user.dir") + "/src/main/resources/pelican/resource/qa_purge.sql";
        Statement stmt = null;

        Boolean procedureresponse;
        if (Util.fileExists(sqlScriptPath)) {
            storeprocedurequery = Util.readFileReturnString(sqlScriptPath);
        } else {
            Assert.fail("SQL Clean Procedure script is not found at: " + sqlScriptPath);
        }

        LOGGER.info("Store Procedure script: " + storeprocedurequery);
        int status = 0;

        try {
            stmt = con.createStatement();
            procedureresponse = stmt.execute(storeprocedurequery);

        } catch (final SQLException ex) {

            status = 1;

            LOGGER.info("SQLException, Create Statement: " + ex.getMessage());

            if (iteration == 1) {

                iteration++;
                dropProcedure(con);
                createProcedure(con);

            } else {
                Assert.fail("Attempt " + iteration + ", Failed to Create Procedure in DB");
            }
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (final SQLException e) {
                    System.err.println("SQLException while closing SQL statement: " + e.getMessage());
                    Assert.fail("Failed to Close the statement in Create Procedure");
                }
            }
        }
        return status;

    }

    private static int dropProcedure(final Connection con) {
        int status = 0;
        Statement stmt = null;

        try {
            stmt = con.createStatement();

            LOGGER.info("Action: Drop Store Procedure from DB");

            stmt.execute("DROP PROCEDURE IF EXISTS `tempestdb`.`qa_purge`");

        } catch (final SQLException ex) {
            System.err.println("SQLException while drop procedure: " + ex.getMessage());
            Assert.fail("Failed while Drop Stored Procedure from DB");
            status = 1;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (final SQLException e) {
                    System.err.println("SQLException while closing SQL statement: " + e.getMessage());
                    Assert.fail("Failed while Close of Statement in Drop Procedure");
                }

                LOGGER.info("Drop Store Procedure from DB - Successful");

            }
        }

        return status;
    }

    private static int callProcedure(final Connection con) {
        CallableStatement cs = null;
        String startDate = null;
        int status = 0;

        try {
            cs = con.prepareCall("{call qa_purge(?)}");

            try {
                if (System.getProperty("dataCleanStartDate") != null) {
                    startDate = System.getProperty("dataCleanStartDate");
                } else {
                    startDate = "2015-09-01 00:00:00";

                }

            } catch (final Exception e) {

                Assert.fail("Failed to read the Start Date for the Data clean.");
            }

            LOGGER.info("Start Date for this Store Procedure: " + startDate);

            // Setting the start End.
            cs.setString(1, startDate);

            cs.execute();

        } catch (final SQLException e) {
            System.err.println("SQLException while call procedure: " + e.getMessage());
            Assert.fail("Failed while Call Stored Procedure");
            status = 1;
        } finally {
            if (cs != null) {
                try {
                    cs.close();

                    // if the call procedure is success, drop the procedure in db
                    dropProcedure(con);

                } catch (final SQLException e) {
                    System.err.println("SQLException while close callablestatement: " + e.getMessage());
                    Assert.fail("Failed while Close of callablestatement in Call Procedure method");
                }
            }
        }

        return status;
    }

}
