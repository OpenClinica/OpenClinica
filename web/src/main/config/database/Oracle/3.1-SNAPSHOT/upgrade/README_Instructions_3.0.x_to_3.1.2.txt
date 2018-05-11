There is an issue when migrating an Oracle database from 3.0.x to 3.1.2, or if the database has migrated from 
3.0.x to either 3.1 or 3.1.1 and now want to upgrade to 3.1.2.  

Please refer to https://issuetracker.openclinica.com/view.php?id=10813 for information about the issue.

After you have followed all other installation instructions please execute the 3.0.x_to_3.1.2.sql file on your Oracle 
database running OpenClinica.  Restart Tomcat once the script has completed running in order to completely fix the issue.

**THE SCRIPT SHOULD NOT BE EXECUTED IF THIS IS AN UPGRADE OF 3.1 OR 3.1.1 THAT WAS FRESHLY INSTALLED**
