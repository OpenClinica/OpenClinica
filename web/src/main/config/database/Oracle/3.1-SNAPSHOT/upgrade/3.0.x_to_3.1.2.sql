-- Please refer to the README_Instructions_3.0.x_to_3.1.2.txt document for more information.

CREATE OR REPLACE Procedure add_row_to_rule_action_run
IS      
cursor c1 is select id from rule_action where rule_action_run_id is null;
BEGIN
	FOR cc in c1
	LOOP
		INSERT INTO RULE_ACTION_RUN (administrative_data_entry,initial_data_entry,double_data_entry,import_data_entry,batch,version) 
				VALUES (1, 1, 1, 1, 1,0);                
		UPDATE rule_action set rule_action_run_id = RULE_ACTION_RUN_ID_SEQ.currval  where id = cc.id;   
	END LOOP;
	RETURN;
END;  
/    
EXECUTE add_row_to_rule_action_run();
EXECUTE updt_metadata_repeating_group();