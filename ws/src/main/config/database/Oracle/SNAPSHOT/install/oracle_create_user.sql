/*--------------------------------------------------------------------------
*
* File       : oracle_create_user.sql
*
* Subject    : Creates the user CLINICA
*
* Parameters : Default Tablespace Name
*
* Conditions : None
*
* Author/Dt  : Shriram Mani  12/10/2007
*
* Comments   : If the user exists, script errors out.
*
--------------------------------------------------------------------------*/

set echo off
set feedback off
set verify off
set serveroutput on size 20000
Rem
Rem
prompt This script will create the user Clinca. Parameter: TablespaceName
Rem
prompt The user is created with a default tablespace.
prompt
accept ts_name prompt "Specify Tablespace name for Clinica objects: "
prompt
Rem
create user clinica identified by clinica
default tablespace &&ts_name
quota 0 on system;
Rem
Rem
