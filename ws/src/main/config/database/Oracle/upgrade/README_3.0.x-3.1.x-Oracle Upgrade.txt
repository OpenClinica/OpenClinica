Oracle Duplicate Delete Script
 
There is an issue in 3.0.x and earlier versions of OpenClinica 
where duplicate rows could be saved into the item_data table. The 
issue occurs when a complete section of non-required items are 
left blank.  When the duplicate row is inserted, the system may 
try to update the wrong row, which in turn means the information 
may not be shown in a data set or on the CRF in the user 
interface.

OpenClinica has built a script that will delete this duplicate 
data so only one record will exist in the database. Any audit 
history and discrepancy note data will be moved to the single 
remaining row of information.  This script has been tested 
extensively and runs a series of checks to determine which of the 
duplicate records to keep.

OpenClinica requires you to execute these scripts on your 
database to upgrade to from 3.0.x to 3.1.x.  Once these scripts 
are ran, it will apply a unique constraint on the database that 
will work in conjunction with other fixes in the code to ensure 
duplicate records will never be saved in the item_data table 
again.  

If you do not run this script and remove the duplicate rows, you 
will not be able to upgrade from 3.0.x to 3.1.x versions of 
OpenClinica.

The database and user permissions for Oracle are different in 
OpenClinica 3.0.x then in 3.1.x. These scripts were designed to 
run on a 3.1.x OpenClinica created database. In order to upgrade,
 you will have to perform the following steps:

1. Backup your data. We recommend doing this as the db user 
"clinica" and another as "sysdba". This is crucial and be sure 
you have a full backup before proceeding or you may lose data.

2. Drop the current OpenClinica tablespace

3. Drop the "clinica" user or whatever username you used with 
OpenClinica

4. Create a new tablespace and "clinica" user utilizing the 
Oracle Install scripts provided with OpenClinica 3.1.x (cannot 
use 3.0.4.2 versions). When creating the new tablespace and user 
the names should match exactly the names used in 3.0.x.

5. Import the backup you took in step 1 into the new 3.1.x 
structure you just created.

6. Open a sqlplus command prompt using the "clinica" user account. 
Once opened, execute "Duplicate_Delete_v2.sql" file on 
your database. The script can be found in the Oracle\upgrade 
directory within the OpenClinica 3.1.x package.

7. In the same sqlplus command prompt type "spool FILENAME.log" 
where FILENAME.log is the path to where you want the data to be 
logged too.

8. In the same sqlplus command prompt issue a "select * from 
my_dbms_output_view;" command.

9. In the same sqlplus command prompt issue a "quit" to exit 
sqlplus.

The FILENAME.log that was created is your log file. At the end of 
it you should see something similar to "There are no duplicate 
rows in the item_data table. The constraint has been applied." If 
you see this, you are all set and the scripts worked.

If you see "Duplicate records have been found after the double 
check. View data output for a report." You will have to clear up 
any remaining duplicates on your own manually and enable the 
constraint. You can enable the constraint manually utilizing this 
SQL "'ALTER TABLE item_data ADD CONSTRAINT 
duplicate_item_uniqueness_key UNIQUE (item_id, event_crf_id, 
ordinal)'"

