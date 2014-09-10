There is an issue in 3.0.x and earlier versions of OpenClinica where duplicate rows could be saved into the item_data table. The issue occurs when a complete section of non-required items are left blank.  When the duplicate row is inserted, the system may try to update the wrong row, which in turn means the information may not be shown in a data set or on CRF in the user interface.

OpenClinica has built a script that will delete this duplicate data so only one record will exist in the database. Any audit history and discrepancy note data will be moved to the single remaining row of information.  This script has been tested extensively, but we want to provide you the option of "Opting Out" of running it on your database.

Even though running the script is optional, OpenClinica strongly advises you to execute the script on your database.  Once this script is run, you can then apply a unique constraint on the database that will work in conjunction with other fixes in the code to ensure duplicate data will never be saved in the item_data table again.  

If you do not run this script and remove the duplicate rows, you may not be able to upgrade to future versions of OpenClinica.

Please see the instructions below on what to do.

1. Execute the Duplicate_Delete_v5.sql file on your database

This script will run through your database and delete duplicate rows that should not exist, and preserve the 'last touched' record for that particular data point.  Logic has been written to find the last touched by looking at the created_date and date_updated in the item_data table, as well as the audit_log_event table for those particular items.  Please move to step 3 after the script has completed.

EXAMPLE: /opt/PostgreSQL/8.4/bin/psql -u clinica -d DBNAME < Duplicate_Delete_v5.sql
Replace DBNAME in the above example with the name of your database.