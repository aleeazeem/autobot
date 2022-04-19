DROP TABLE IF EXISTS temp_subscription;

CREATE TABLE temp_subscription (
  	id BIGINT(20)
    COLLATE utf8_bin NOT NULL
);

-- change the csv file name from which you want to read subscription.
LOAD DATA LOCAL INFILE 'subscription_ids_5.csv' INTO TABLE temp_subscription;

-- Change created date and next billing to any date you want. Date should be in date format YYYY-MM-DD 00:00:00.000000. 
-- insert_subscription(subscription_count, created_date, next_billing_date);
call insert_subscription(3, '2017-05-17 00:00:00.000000', '2017-05-17 00:00:00.000000');

DROP TABLE IF EXISTS temp_subscription;