/*--------------------------------------------------------------------------
*
* File       : oracle_setup_db.sql
*
* Subject    : Creates tablespace, users, grants, etc
*
* Parameters :
*
* Conditions : This script should be executed as sys/system
*
* Author/Dt  : Shriram  12/17/2007
*
* Comments   : Individual script can be executed separately in order
*               depending upon installation type (new/patch)
*
--------------------------------------------------------------------------*/
spool spool_oracle_setup_db.txt
Rem
Rem
Rem Create Tablespaces required for Clinica Objects
Rem
@@oracle_create_tablespace.sql
Rem
Rem Create User
Rem
@@oracle_create_user.sql
Rem
Rem Grant roles and privileges
Rem
@@oracle_user_grants.sql
Rem
prompt Please check spool_oracle_setup_db.txt for errors
prompt
prompt If the tablespace and the user is created/exists run the script
prompt oracle_setup_objects.sql as user CLINICA
Rem
spool off
exit

