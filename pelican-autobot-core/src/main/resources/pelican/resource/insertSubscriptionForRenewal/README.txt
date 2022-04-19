* This script is used to perform load testing for consolidated renewal job.
* This script inserts subscriptions into subscription table as many number of times as subscription_count value
for each record in csv file. All the data for subscription will be same as the subscription in csv except 
subscription id, created date and next billing date.
* Subscription id will be auto populated. Created date and next billing date is provided while calling the script. 
* If subscription_count = 2 then for each entry in csv, 2 new subscriptions will be inserted in subscription table.

Execution Instructions:

1) Create csv file with subscription ids based on your requirement. Attached csv is created with the subscription ids using the below query:
select count(*), id, OWNER_ID from subscription 
where APP_FAMILY_ID = 8888
and status = 0
and EC_STATUS = 5
and LAST_BILLING_CYCLE_DAYS in (30,31) and
PURCHASE_ORDER_ID in 
(select id from purchase_order where origin = 'ESTORE')
group by owner_id
having count(*) = 1
limit 5000;

2) In execute_insert_subscription.sql, change the csv file name, subscription_count, created date and next_billing_date and run it. 