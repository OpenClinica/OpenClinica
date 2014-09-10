There is an issue in 3.0.x and earlier versions of OpenClinica where duplicate rows could be saved into the item_data table. The issue occurs when a complete section of non-required items are left blank.  When the duplicate row is inserted, the system may try to update the wrong row, which in turn means the information may not be shown in a data set or on CRF in the user interface.

Akaza Research has built a script that will delete this duplicate data so only one record will exist in the database. Any audit history and discrepancy note data will be moved to the single remaining row of information.  This script has been tested extensively, but we want to provide you the option of "Opting Out" of running it on your database.

Even though running the script is optional, Akaza Research strongly advises you to execute the script on your database.  Once this script is run, you can then apply a unique constraint on the database that will work in conjunction with other fixes in the code to ensure duplicate data will never be saved in the item_data table again.  

If you do not run this script and remove the duplicate rows, you may not be able to upgrade to future versions of OpenClinica.

Please see the instructions below on what to do.

1. Execute the Duplicate_Report.sql file on your database

This will run through your database and find how many records are duplicated in the item_data table.  If no results are found, please skip to step 3.  If Results are returned, please move to step 2.

EXAMPLE: /opt/PostgreSQL/8.4/bin/psql -u clinica -d DBNAME < Duplicate_Report.sql
Replace DBNAME in the above example with the name of your database.

2. Execute the Duplicate_Delete.sql file on your database

This script will run through your database and delete duplicate rows that should not exist, and preserve the 'last touched' record for that particular data point.  Logic has been written to find the last touched by looking at the created_date and date_updated in the item_data table, as well as the audit_log_event table for those particular items.  Please move to step 3 after the script has completed.

EXAMPLE: /opt/PostgreSQL/8.4/bin/psql -u clinica -d DBNAME < Duplicate_Delete.sql
Replace DBNAME in the above example with the name of your database.

3. Execute the Unique_Constraint.sql

This script will apply a unique constraint on the item_data table using the event_crf_id, item_id, and the ordinal to create a unique key.  If this key is violated, the data will not be saved into the database.  Other code changes in OpenClinica have been implemented to eliminate duplicates being recorded, but this is the last gate keeper to ensure nothing is ever duplicated in the item_data table.

EXAMPLE: /opt/PostgreSQL/8.4/bin/psql -u clinica -d DBNAME < Unique_Constraint.sql
Replace DBNAME in the above example with the name of your database.