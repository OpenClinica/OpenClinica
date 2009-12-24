/*--------------------------------------------------------------------------
*
* File       : oracle_setup_objects.sql
*
* Subject    : Creates objects (tables/views/sequences/triggers/etc)
*
* Parameters : None
*
* Conditions : Objects are created in the schema logged in. Make sure
*               to login as user clinica
*
* Author/Dt  : Shriram Mani 12/17/2007
*
* Comments   : Individual scripts can be executed in order. Tables are
*               pre-requisite for all objects (except sequences). Primary
*               key constraints are pre-requisite for foreign key
*               constraints.
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
Rem Create Indexes
Rem
@@oracle_table_index.sql
Rem
Rem Create Primary Key Constraints
Rem
@@oracle_table_primary_key.sql
Rem
Rem Create Foreign Key Constraints
Rem
@@oracle_table_foreign_key.sql
Rem
Rem Create Materialized View for Dataware Housing
Rem
@@oracle_create_mv.sql
Rem
prompt Clinica objects are created in the schema clinica. Please
prompt check the spool_oracle_create_objects.txt file for errors.
spool off
Rem
exit