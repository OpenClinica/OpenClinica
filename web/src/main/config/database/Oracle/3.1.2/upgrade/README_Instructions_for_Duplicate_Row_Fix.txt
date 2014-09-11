There is an issue in 3.0.x and earlier versions of OpenClinica where duplicate rows could be saved into the item_data table. The issue occurs when a complete section of non-required items are left blank.  When the duplicate row is inserted, the system may try to update the wrong row, which in turn means the information may not be shown in a data set or on CRF in the user interface.

Akaza Research has built a script that will delete this duplicate data so only one record will exist in the database. Any audit history and discrepancy note data will be moved to the single remaining row of information.  This script has been tested extensively, but we want to provide you the option of "Opting Out" of running it on your database.

Even though running the script is optional, Akaza Research strongly advises you to execute the script on your database.  Once this script is run, you can then apply a unique constraint on the database that will work in conjunction with other fixes in the code to ensure duplicate data will never be saved in the item_data table again.  

If you do not run this script and remove the duplicate rows, you may not be able to upgrade to future versions of OpenClinica.

Please see the instructions below on what to do.

1. Execute the Duplicate_Report.sql file on your database

This will run through your database and find how many records are duplicated in the item_data table.  If no results are found, please skip to step 3.  If Results are returned, please move to step 2.

EXAMPLE: /opt/PostgreSQL/8.4/bin/psql -u clinica -d DBNAME < Duplicate_Report.sql
Replace DBNAME in the above example with the name of your database.

2. Execute the Duplicate_Delete_v2.sql file on your database

This script will run through your database and delete duplicate rows that should not exist, and preserve the 'last touched' record for that particular data point.  Logic has been written to find the last touched by looking at the created_date and date_updated in the item_data table, as well as the audit_log_event table for those particular items.  The rows deleted so far are all rows we know should be removed. The script then checks to see if other duplicate records exist, if none do it enables the constraint and you are all set and are ready for your upgrade. If it does report duplicates that still exist you have to move on to step 3.

EXAMPLE: /opt/PostgreSQL/8.4/bin/psql -u clinica -d DBNAME < Duplicate_Delete_v2.sql
Replace DBNAME in the above example with the name of your database.

3. We will be modifying the "Manual_Duplicate_Delete.sql" based on the results of Duplicate_Delete_v2.sql and executing it.

This script will manually delete records that we could not choose for you. The output of the Duplicate_Delete_v2.sql script should look like the following for example.

item_id | event_crf_id | ordinal | min_value | max_value |   name   | item_data_id
---------+--------------+---------+-----------+-----------+----------+--------------
    3333 |         2222 |      30 | 20        | 30        | TESTITEM  |       222222
    3333 |         2222 |      30 | 20        | 30        | TESTITEM   |      333333

Based on this output you see the same item_id record twice in my example "3333" all of the duplicate item_id's have to be removed before upgrading to 3.1.x.

You as the user have to determine which record you want to keep. Using the above example we will keep the item_data_id of 333333 and delete the 222222. To do this edit "Manual_Duplicate_Delete.sql" and look for "ret_count = ret_count + delete(XXXXXX, YYYYYY);" replace XXXXXX with 222222 and YYYYYY with 333333. You can add more rows that are identical to "ret_count = ret_count + delete(XXXXXX, YYYYYY);" below for multiple deletes at once. Once done editing execute the script as follows.

EXAMPLE: /opt/PostgreSQL/8.4/bin/psql -u clinica -d DBNAME < Manual_Duplicate_Delete.sql
Replace DBNAME in the above example with the name of your database.

Once done executing re-run the Duplicate_Delete_v2.sql so that you can be sure you have no more duplicates and it will enable the constraint to prevent further duplicates from occurring.

EXAMPLE: /opt/PostgreSQL/8.4/bin/psql -u clinica -d DBNAME < Duplicate_Delete_v2.sql
Replace DBNAME in the above example with the name of your database.