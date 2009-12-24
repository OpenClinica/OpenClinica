/*--------------------------------------------------------------------------
*
* File       : oracle_setup_objects.sql
*
* Subject    : Creates objects once the schema is created
*
* Parameters : None
*
* Conditions : Login user must be the owner of the objects
*
* Author/Dt  : Shriram Mani, 05/16/2008
*
* Comments   : 1. Check for Spool file (spool_oracle_setup_objects.txt)
*                  for errors.
*              2. Each script can be executed seperately as long as the
*                  order is maintained for dependencies.
*
--------------------------------------------------------------------------*/
Rem
spool spool_oracle_setup_objects.txt
Rem
Rem Create Tables
Rem
@@oracle_create_table.sql
Rem
Rem Create Sequences
Rem
@@oracle_sequences.sql
Rem
Rem Create Packages
Rem
@@oracle_package.sql
Rem
Rem Create Triggers
Rem
@@oracle_table_triggers.sql
Rem
Rem Create Base Data
Rem
@@oracle_insert_default_data.sql
Rem
Rem Update Base Data
Rem
@@oracle_update_default_data.sql
Rem
Rem Create Indexes
Rem
@@oracle_table_index.sql
Rem
Rem Create Primary Key Constraints
Rem
@@oracle_table_primary_key.sql
Rem
Rem Create Unique Key Constraints
Rem
@@oracle_table_unique_key.sql
Rem
Rem Create Foreign Key Constraints
Rem
@@oracle_table_foreign_key.sql
Rem
Rem Create table to reset the sequences
Rem
@@oracle_extras.sql
Rem
prompt Clinica objects are created in the schema clinica. Please
prompt check the spool_oracle_create_objects.txt file for errors.
spool off
Rem
exit

