USE tempestdb;

DROP PROCEDURE IF EXISTS qa_purge;

DELIMITER $$

CREATE PROCEDURE qa_purge(
	IN creationDateAfter DATETIME
)
BEGIN
	-- appFamilyId hardcoded. Non-negotiable. 
	DECLARE auto_id INT DEFAULT 2001;
	
	-- provide creationDate range if not provided. Also set ms variables for convenience
	DECLARE creationDateAfter_ms BIGINT;
	
	-- rollback on any error, but return the exception
	DECLARE EXIT HANDLER FOR SQLEXCEPTION 
	BEGIN
		ROLLBACK;
		RESIGNAL;
	END;
	
	IF creationDateAfter IS NULL THEN
		SET creationDateAfter = '1970-01-01 00:00:00';
		SET creationDateAfter_ms = 0;
	ELSE
		SET creationDateAfter_ms = 1000 * UNIX_TIMESTAMP(creationDateAfter);
	END IF;
	
	-- everything in a transaction so we can rollback if needed
	START TRANSACTION;
		
		-- DELETE DATA FROM DB --
		
		-- DELETE DATA FROM DB --
		
		delete from bic_release 
		where subplan_product_line_id in (select pl.id from product_line pl  
		where (pl.external_key = 'AUTO_PRODUCT_LINE_MAYA' or pl.external_key = 'AUTO_PRODUCT_LINE_REVIT' and pl.app_family_id = auto_id and pl.created >= creationDateAfter) or (pl.app_family_id = auto_id
		and pl.created >= creationDateAfter)) or download_product_line_id in (select pl.id from product_line pl  
		where (pl.external_key = 'AUTO_PRODUCT_LINE_MAYA' or pl.external_key = 'AUTO_PRODUCT_LINE_REVIT' and pl.app_family_id = auto_id and pl.created >= creationDateAfter) or (pl.app_family_id = auto_id
		and pl.created >= creationDateAfter));
		 
		delete from descriptor 
		where definition_id in (select dd.id from descriptor_definition dd
		where dd.app_family_id = auto_id 
		and dd.created >= creationDateAfter_ms);
		
		delete from descriptor_definition
		where app_family_id = auto_id 
		and created >= creationDateAfter_ms;
		
		delete from file_upload_job 
		where app_family_id = auto_id 
		and created >= creationDateAfter;
		
		delete from finance_report
		where app_family_id = auto_id
		and purchase_order_id in 
		(select po.id from purchase_order po where po.appf_id = auto_id and po.created >= creationDateAfter);
		
		delete from promotion_code 
		where promotion_id in 
		(select pr.id from promotion pr where pr.app_family_id = auto_id and pr.created >= creationDateAfter_ms);
		
		delete from promotion_job
		where promotion_id in 
		(select pr.id from promotion pr where pr.app_family_id = auto_id and pr.created >= creationDateAfter_ms);
				
		delete from promotion_offering
		where promotion_id in  (select pr.id from promotion pr where pr.app_family_id = auto_id and pr.created >= creationDateAfter_ms)
		or offering_id in (select o.id from offering o where o.product_line_id in (select pl.id from product_line pl  
		where (pl.external_key = 'AUTO_PRODUCT_LINE_MAYA' or pl.external_key = 'AUTO_PRODUCT_LINE_REVIT' and pl.app_family_id = auto_id and pl.created >= creationDateAfter) or (pl.app_family_id = auto_id
		and pl.created >= creationDateAfter)) and o.offering_detail_id in (select od.id from offering_detail od where od.app_family_id = auto_id
		AND od.created >= creationDateAfter) and o.id <> 142903 and o.id <> 41673);
		
		delete from promotion_stores 
		where promotion_id in (select pr.id from promotion pr where pr.app_family_id = auto_id and pr.created >= creationDateAfter_ms)
		or store_id in (select s.id from store s where s.type_id in 
		(select st.id from store_type st where st.app_family_id = auto_id and st.created >= creationDateAfter_ms));
		
		delete from promo_price_list_discount
		where promo_id in (select pr.id from promotion pr where pr.app_family_id = auto_id and pr.created >= creationDateAfter_ms)
		or store_id in (select s.id from store s where s.type_id in 
		(select st.id from store_type st where st.app_family_id = auto_id and st.created >= creationDateAfter_ms))
		or price_list_id in (select pl.id from price_list pl where pl.store_id in (select s.id from store s where s.type_id in 
		(select st.id from store_type st where st.app_family_id = auto_id and st.created >= creationDateAfter_ms)));
		
		delete from userapplicablepromotion 
		where promotion_id in (select pr.id from promotion pr where pr.app_family_id = auto_id and pr.created >= creationDateAfter_ms)
		or user_id in (select np.id from named_party np where np.appf_id = auto_id and np.created >= creationDateAfter_ms and np.name NOT IN ('svc_p_pelican','yerragv', '$apiactor', 'US_TAX'));
		
		delete from future_promotions
		where promotion_id in (select pr.id from promotion pr where pr.app_family_id = auto_id and pr.created >= creationDateAfter_ms);
		
		delete from promotion 
		where app_family_id = auto_id and created >= creationDateAfter_ms;
		
		delete from subscription_promotion_use 
		where promotion_id in (select pr.id from promotion pr where pr.app_family_id = auto_id and pr.created >= creationDateAfter_ms)
		or subscription_id in (select s.id from subscription s where s.plan_id in (select o.id from offering o where o.product_line_id in 
		(select pl.id from product_line pl  
		where (pl.external_key = 'AUTO_PRODUCT_LINE_MAYA' or pl.external_key = 'AUTO_PRODUCT_LINE_REVIT' and pl.app_family_id = auto_id and pl.created >= creationDateAfter) or (pl.app_family_id = auto_id
		and pl.created >= creationDateAfter)) or owner_id in (select np.id from named_party np where np.appf_id = auto_id and np.created >= creationDateAfter_ms and np.name NOT IN ('svc_p_pelican','yerragv', '$apiactor', 'US_TAX'))
		or purchase_order_id in (select po.id from purchase_order po where po.appf_id = auto_id and po.created >= creationDateAfter)
		or offer_id in (select so.id from subscription_offer so where so.plan_id in (select o.id from offering o where o.product_line_id in 
		(select pl.id from product_line pl  
		where (pl.external_key = 'AUTO_PRODUCT_LINE_MAYA' or pl.external_key = 'AUTO_PRODUCT_LINE_REVIT' and pl.app_family_id = auto_id and pl.created >= creationDateAfter) or (pl.app_family_id = auto_id
		and pl.created >= creationDateAfter)) and o.id <> 363731 and o.id <> 88792))));
		
		delete from subscription_event 
		where subscription_id in (select s.id from subscription s where s.plan_id in (select o.id from offering o where o.product_line_id in (select pl.id from product_line pl  
		where (pl.external_key = 'AUTO_PRODUCT_LINE_MAYA' or pl.external_key = 'AUTO_PRODUCT_LINE_REVIT' and pl.app_family_id = auto_id and pl.created >= creationDateAfter) or (pl.app_family_id = auto_id
		and pl.created >= creationDateAfter))));
 		
 		delete from subscription 
 		where plan_id in (select o.id from offering o where o.product_line_id in  (select pl.id from product_line pl  
		where (pl.external_key = 'AUTO_PRODUCT_LINE_MAYA' or pl.external_key = 'AUTO_PRODUCT_LINE_REVIT' and pl.app_family_id = auto_id and pl.created >= creationDateAfter) or (pl.app_family_id = auto_id
		and pl.created >= creationDateAfter)));
		
		delete from 	entitlement_core_products 
		where entitlement_id in 
		(select se.id from subscription_entitlement se where se.item_id in 
		(select i.id from item i where i.app_id = auto_id and i.created >= creationDateAfter_ms)
		or se.related_id in (select so.id from subscription_offer so where so.plan_id in (select o.id from offering o where o.product_line_id in (select pl.id from product_line pl  
		where (pl.external_key = 'AUTO_PRODUCT_LINE_MAYA' or pl.external_key = 'AUTO_PRODUCT_LINE_REVIT' and pl.app_family_id = auto_id and pl.created >= creationDateAfter) or (pl.app_family_id = auto_id
		and pl.created >= creationDateAfter)) and o.id <> 363731 and o.id <> 88792))
		or se.licensing_model_id in (select lm.id from licensing_model lm where lm.app_family_id = auto_id and lm.created >= creationDateAfter))
		or core_product_id in (select cp.id from core_product cp where cp.app_family_id = auto_id and cp.created >= creationDateAfter);
		
		delete from subscription_entitlement 
		where item_id in 
		(select i.id from item i where i.app_id = auto_id and i.created >= creationDateAfter_ms)
		or related_id in (select so.id from subscription_offer so where so.plan_id in (select o.id from offering o where o.product_line_id in (select pl.id from product_line pl  
		where (pl.external_key = 'AUTO_PRODUCT_LINE_MAYA' or pl.external_key = 'AUTO_PRODUCT_LINE_REVIT' and pl.app_family_id = auto_id and pl.created >= creationDateAfter) or (pl.app_family_id = auto_id
		and pl.created >= creationDateAfter)) and o.id <> 363731 and o.id <> 88792))
		or licensing_model_id in (select lm.id from licensing_model lm where lm.app_family_id = auto_id and lm.created >= creationDateAfter);
		
		delete from 	subscription_price 
		where offer_id in (select so.id from subscription_offer so where so.plan_id in (select o.id from offering o where o.product_line_id in (select pl.id from product_line pl  
		where (pl.external_key = 'AUTO_PRODUCT_LINE_MAYA' or pl.external_key = 'AUTO_PRODUCT_LINE_REVIT' and pl.app_family_id = auto_id and pl.created >= creationDateAfter) or (pl.app_family_id = auto_id
		and pl.created >= creationDateAfter)) and o.id <> 363731 and o.id <> 88792))
		or price_list_id in (select pl.id from price_list pl where pl.store_id in (select s.id from store s where s.type_id in 
		(select st.id from store_type st where st.app_family_id = auto_id and st.created >= creationDateAfter_ms)))
		and id <> 363731
                and id <> 88792;
        
        	delete from promotion_subscription_offer 
        	where promotion_id in (select pr.id from promotion pr where pr.app_family_id = auto_id and pr.created >= creationDateAfter_ms)
        	or offer_id in (select so.id from subscription_offer so where so.plan_id in (select o.id from offering o where o.product_line_id in (select pl.id from product_line pl  
		where (pl.external_key = 'AUTO_PRODUCT_LINE_MAYA' or pl.external_key = 'AUTO_PRODUCT_LINE_REVIT' and pl.app_family_id = auto_id and pl.created >= creationDateAfter) or (pl.app_family_id = auto_id
		and pl.created >= creationDateAfter)) and o.id <> 363731 and o.id <> 88792));
		
		delete from subscription_offer
		where plan_id in (select o.id from offering o where o.product_line_id in (select pl.id from product_line pl  
		where (pl.external_key = 'AUTO_PRODUCT_LINE_MAYA' or pl.external_key = 'AUTO_PRODUCT_LINE_REVIT' and pl.app_family_id = auto_id and pl.created >= creationDateAfter) or (pl.app_family_id = auto_id
		and pl.created >= creationDateAfter)) and o.id <> 363731 and o.id <> 88792);
		
		delete from subscription_plan_modules
		where plan_id in (select o.id from offering o where o.product_line_id in (select pl.id from product_line pl  
		where (pl.external_key = 'AUTO_PRODUCT_LINE_MAYA' or pl.external_key = 'AUTO_PRODUCT_LINE_REVIT' and pl.app_family_id = auto_id and pl.created >= creationDateAfter) or (pl.app_family_id = auto_id
		and pl.created >= creationDateAfter)));
		
		delete from subscription_plan_core_products 
		where plan_id in (select o.id from offering o where o.product_line_id in (select pl.id from product_line pl  
		where (pl.external_key = 'AUTO_PRODUCT_LINE_MAYA' or pl.external_key = 'AUTO_PRODUCT_LINE_REVIT' and pl.app_family_id = auto_id and pl.created >= creationDateAfter) or (pl.app_family_id = auto_id
		and pl.created >= creationDateAfter))) or core_product_id in (select cp.id from core_product cp where cp.app_family_id = auto_id and cp.created >= creationDateAfter);
			
		delete from core_product 
		where app_family_id = auto_id and created >= creationDateAfter;
		
		delete from offering_detail 
		where app_family_id = auto_id and created >= creationDateAfter;
		
		delete from offering where 
		product_line_id in (select pl.id from product_line pl  
		where (pl.external_key = 'AUTO_PRODUCT_LINE_MAYA' or pl.external_key = 'AUTO_PRODUCT_LINE_REVIT' and pl.app_family_id = auto_id and pl.created >= creationDateAfter) or (pl.app_family_id = auto_id
		and pl.created >= creationDateAfter));
		
		delete from product_line where app_family_id = auto_id and created >= creationDateAfter;
				
		delete from item_inst 
		where owner_id in (select np.id from named_party np where np.appf_id = auto_id and np.created >= creationDateAfter_ms and np.name NOT IN ('svc_p_pelican','yerragv', '$apiactor', 'US_TAX'))
		or item_id in (select i.id from item i where i.app_id = auto_id and i.created >= creationDateAfter_ms)
		or subscription_id in (select s.id from subscription s where s.plan_id in (select o.id from offering o where o.product_line_id in 
		(select pl.id from product_line pl  
		where (pl.external_key = 'AUTO_PRODUCT_LINE_MAYA' or pl.external_key = 'AUTO_PRODUCT_LINE_REVIT' and pl.app_family_id = auto_id and pl.created >= creationDateAfter) or (pl.app_family_id = auto_id
		and pl.created >= creationDateAfter)))) 
		or core_product_id in (select cp.id from core_product cp where cp.app_family_id = auto_id and cp.created >= creationDateAfter)
		or licensing_model_id in (select lm.id from licensing_model lm where lm.app_family_id = auto_id and lm.created >= creationDateAfter);
		
		delete from item_category
		where item_id in (select i.id from item i where i.app_id = auto_id and i.created >= creationDateAfter_ms);
		
		delete from inventory
		where item_id in (select i.id from item i where i.app_id = auto_id and i.created >= creationDateAfter_ms);
		
		delete from item 
		where app_id = auto_id and created >= creationDateAfter_ms;
		
		delete from item_type 
		where id in (select type_id from item i where i.app_id = auto_id and i.created >= creationDateAfter_ms);
		
		delete from store_country 
		where store_id in (select s.id from store s where s.type_id in 
		(select st.id from store_type st where st.app_family_id = auto_id and st.created >= creationDateAfter_ms))
		or price_list_id in (select pl.id from price_list pl where pl.store_id in (select s.id from store s where s.type_id in 
		(select st.id from store_type st where st.app_family_id = auto_id and st.created >= creationDateAfter_ms)));
		
		delete from store_payment_method 
		where store_id in (select s.id from store s where s.type_id in 
		(select st.id from store_type st where st.app_family_id = auto_id and st.created >= creationDateAfter_ms))
		or price_list_id in (select pl.id from price_list pl where pl.store_id in (select s.id from store s where s.type_id in 
		(select st.id from store_type st where st.app_family_id = auto_id and st.created >= creationDateAfter_ms)));
		
		delete from price_list
		where store_id in (select s.id from store s where s.type_id in 
		(select st.id from store_type st where st.app_family_id = auto_id and st.created >= creationDateAfter_ms));
		
		delete from store 
		where type_id in 
		(select st.id from store_type st where st.app_family_id = auto_id and st.created >= creationDateAfter_ms);
		
		delete from store_type
		where app_family_id = auto_id and created >= creationDateAfter_ms;
		
		delete from shipping_method
		where app_family_id = auto_id
		and created >= creationDateAfter; 
		
		delete from fulfillment_response
		where purchase_order_id in (select po.id from purchase_order po where po.appf_id = auto_id and po.created >= creationDateAfter);
		
		delete from fulfillment_group_line_items
		where fulfillment_group_id in (select fg.id from fulfillment_group fg where purchase_order_id in (select po.id from purchase_order po where po.appf_id = auto_id and po.created >= creationDateAfter));
		
		delete from fulfillment_group 
		where purchase_order_id in (select po.id from purchase_order po where po.appf_id = auto_id and po.created >= creationDateAfter);
		
		delete from payment_gateway_response
		where purchase_order_id in (select po.id from purchase_order po where po.appf_id = auto_id and po.created >= creationDateAfter);
		
		delete from invoice_generation_status 
		where purchase_order_id in (select po.id from purchase_order po where po.appf_id = auto_id and po.created >= creationDateAfter);
		
		delete from credit_note_generation_status
		where purchase_order_id in (select po.id from purchase_order po where po.appf_id = auto_id and po.created >= creationDateAfter);
		
		delete from purchase_order where appf_id = auto_id and created >= creationDateAfter;
		
		delete from sec_role_assign
		where np_id in (select np.id from named_party np where np.appf_id = auto_id and np.created >= creationDateAfter_ms and np.name NOT IN ('svc_p_pelican','yerragv', '$apiactor', 'US_TAX'));
		
		delete from 	secure_key_info
		where owner_id in (select np.id from named_party np where np.appf_id = auto_id and np.created >= creationDateAfter_ms and np.name NOT IN ('svc_p_pelican','yerragv', '$apiactor', 'US_TAX')); 
				
		delete from cred 
		where np_id in (select np.id from named_party np where np.appf_id = auto_id and np.created >= creationDateAfter_ms and np.name NOT IN ('svc_p_pelican','yerragv', '$apiactor', 'US_TAX'));
		
		delete from user_tags 
		where user_id in (select np.id from named_party np where np.appf_id = auto_id and np.created >= creationDateAfter_ms and np.name NOT IN ('svc_p_pelican','yerragv', '$apiactor', 'US_TAX')); 
		
		delete from 	user_login
		where user_id in (select np.id from named_party np where np.appf_id = auto_id and np.created >= creationDateAfter_ms and np.name NOT IN ('svc_p_pelican','yerragv', '$apiactor', 'US_TAX'));
		
		delete from email_addr 
		where np_id in (select np.id from named_party np where np.appf_id = auto_id and np.created >= creationDateAfter_ms and np.name NOT IN ('svc_p_pelican','yerragv', '$apiactor', 'US_TAX'));
		
		delete from twofish_currency
		where appf_id = auto_id
		and id <> 4;
		
		delete from offering where app_family_id = auto_id and product_line_id not in 
		(select id from product_line where app_family_id = auto_id);

		delete from subscription where app_family_id = auto_id and plan_id not in 
		(select id from offering where app_family_id = auto_id);

		delete from item_inst where subscription_id not in 
		(select id from subscription where app_family_id in (8888,1,9999));

		delete from subscription_entitlement where related_id not in (
		select id from offering where app_family_id in (8888, 1, 9999));
		
		delete from midas_account where owner_id = 6425 or owner_id = 956793;
		
	COMMIT;
END$$

DELIMITER ;
