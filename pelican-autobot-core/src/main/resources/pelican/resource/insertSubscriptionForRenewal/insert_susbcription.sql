DROP PROCEDURE IF EXISTS insert_subscription;

DELIMITER //
-- Passing next billing date so that we can pass today's date and subscription can be renewed. 
-- Passing Created_date so that it will be easy to find all the subscriptions created for load testing. Clean up will be easier.
-- subscription_count is the number of subscriptions that we want to add.
CREATE PROCEDURE insert_subscription(IN subscription_count INT(5), IN created_date DATETIME, IN next_billing_date DATETIME)
BEGIN
	DECLARE subscription_initial_count INT DEFAULT 1;
	DECLARE finished INTEGER DEFAULT 0;
    DECLARE subscription_id BIGINT(20);
	
 -- cursor for temporary subscription table	
DECLARE subscription_cursor CURSOR FOR  SELECT id FROM temp_subscription;	
 
 -- declare NOT FOUND handler
DECLARE CONTINUE HANDLER FOR NOT FOUND SET finished = 1;


 -- open cursor	
OPEN subscription_cursor;
    
insert_subscription_loop: LOOP
	FETCH subscription_cursor INTO subscription_id;
    
	IF finished = 1 THEN 
 	LEAVE insert_subscription_loop;
	END IF;
	
    SET subscription_initial_count = 1;
    
		WHILE(subscription_initial_count <= subscription_count) DO
						INSERT INTO subscription (CREATED,
			APP_FAMILY_ID,
			APP_ID,
			OWNER_ID,
			PLAN_ID,
			NEXT_BILLING_DATE,
			STATUS,
			PURCHASE_ORDER_ID,
			DAYS_CREDITED,
			SUSPENDED_DATE,
			LAST_BILLING_CYCLE_DAYS,
			CUR_ID,
			AMT,
			STORED_PAYMENT_PROFILE_ID,
			LAST_BILLING_CYCLE_CHARGE_DATE,
			PG_CONFIG_ID,
			BILLING_COUNT,
			EXPIRATION_DATE,
			NEXT_GRANT_DATE,
			OFFER_ID,
			PENDING_PAYMENT,
			LAST_RENEWAL_REMINDER_TS,
			BILLING_OPTION_ID,
			EMAIL_REMINDERS_ENABLED,
			RESOLVE_BY_DATE,
			PRICE_ID,
			LAST_MODIFIED,
			QUANTITY,
			EXTERNAL_KEY,
			EC_STATUS,
			EC_LAST_UPDATED,
			ADDED_TO_SUBSCRIPTION_ID,
			QTY_TO_REDUCE)
			SELECT created_date,
			APP_FAMILY_ID,
			APP_ID,
			OWNER_ID,
			PLAN_ID,
			next_billing_date,
			STATUS,
			PURCHASE_ORDER_ID,
			DAYS_CREDITED,
			SUSPENDED_DATE,
			LAST_BILLING_CYCLE_DAYS,
			CUR_ID,
			AMT,
			STORED_PAYMENT_PROFILE_ID,
			LAST_BILLING_CYCLE_CHARGE_DATE,
			PG_CONFIG_ID,
			BILLING_COUNT,
			EXPIRATION_DATE,
			NEXT_GRANT_DATE,
			OFFER_ID,
			PENDING_PAYMENT,
			LAST_RENEWAL_REMINDER_TS,
			BILLING_OPTION_ID,
			EMAIL_REMINDERS_ENABLED,
			RESOLVE_BY_DATE,
			PRICE_ID,
			LAST_MODIFIED,
			QUANTITY,
			EXTERNAL_KEY,
			EC_STATUS,
			EC_LAST_UPDATED,
			ADDED_TO_SUBSCRIPTION_ID,
			QTY_TO_REDUCE
			FROM subscription
			where id = subscription_id ;

			SET subscription_initial_count = subscription_initial_count + 1;

		END WHILE;
COMMIT;
END LOOP insert_subscription_loop;

	CLOSE subscription_cursor;
	
END//
DELIMITER ;
