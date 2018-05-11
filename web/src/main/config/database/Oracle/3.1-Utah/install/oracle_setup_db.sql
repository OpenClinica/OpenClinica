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
* Author/Dt  : Shaun Martin  03/01/2001
*
* Comments   : Complete install script for OpenClinica tablespace and user.
*             
*
--------------------------------------------------------------------------*/
spool spool_oracle_setup_db.txt
prompt
prompt This script will create the tablespace and the oracle user account that OpenClinica will use.
Rem
Rem Create Tablespaces required for Clinica Objects
Rem
set echo off
set feedback off
set verify off
set serveroutput on size 20000
Rem
prompt
prompt Setting up new tablespace for the OpenClinica data.
prompt
Rem
accept ts_name prompt "Specify Tablespace name for Clinica Data: "
prompt
prompt For the new tablespace &ts_name, please specify the following
prompt Tablespace Size ( (S)mall - 32MB, (M)edium - 256MB, (L)arge -512MB
prompt
accept ts_type prompt "Specify Tablespace type for Clinica Data (S/M/L): "
prompt
prompt Please make sure there is enough space available on the disk.
accept ts_file prompt "DataFile Name (with exact path): "
Rem
Rem
column ts_size new_value ts_size;
set heading off
set termout off
select
  case
    when upper('&ts_type') = 'S' then '32 M'
    when upper('&ts_type') = 'M' then '256 M'
    when upper('&ts_type') = 'L' then '512 M'
    else '32 M'
  end ts_size
  from dual;
set heading on
set termout on
Rem
Rem
create tablespace &ts_name
datafile '&ts_file' size &ts_size;
Rem
Rem
Rem
Rem Create User
Rem
Rem
prompt
Rem
prompt We will now setup the oracle user account that OpenClinica will use.
Rem
prompt
accept user_name prompt "Specify the wanted username for the OpenClinica oracle account: "
prompt
prompt The user &user_name will be created with a default tablespace of &ts_name.
Rem
create user &user_name identified by clinica
default tablespace &ts_name
quota 0 on system;
Rem
Rem
Rem
Rem Grant roles and privileges
Rem
Rem
Rem
grant connect, resource to &user_name;
grant create materialized view to &user_name;
Rem
Rem

Rem
prompt
prompt Please check spool_oracle_setup_db.txt for errors
prompt
Rem
spool off
exit