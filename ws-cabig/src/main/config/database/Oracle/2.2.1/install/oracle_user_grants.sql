/*--------------------------------------------------------------------------
*
* File       : oracle_user_grants.sql
*
* Subject    : Grants necessary privileges to user Clinia to create objects
*
* Parameters : None
*
* Conditions : Should be executed with DBA privileges
*
* Author/Dt  : Shriram Mani 12/10/2007
*
* Comments   : Privileges : CONNECT, RESOURCES & CREATE MATERIALIZED VIEW
*
--------------------------------------------------------------------------*/
set echo off
set feedback off
set verify off
set serveroutput on size 20000
Rem
Rem
grant connect, resource to clinica;
grant create materialized view to clinica;
Rem
Rem
