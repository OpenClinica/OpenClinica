set echo off
set feedback off
set verify off
set serveroutput on size 20000
Rem
accept ts_name prompt "Specify Tablespace name for Clinica Data: "
Rem
prompt Please make sure there is enough space available on the disk for 512M
accept ts_file prompt "DataFile Name (with exact path): "
Rem
create tablespace &ts_name
datafile '&ts_file' size 512M autoextend on;