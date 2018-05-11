set echo off
set feedback off
set verify off
set serveroutput on size 20000
Rem
Rem
Rem accept ts_name prompt "Specify Tablespace name for Clinica Data: "
Rem
prompt If the tablespace is new, please specify the following

prompt

prompt Please make sure there is enough space available on the disk for 512M
accept ts_file prompt "DataFile Name (with exact path): "
Rem
Rem

Rem
Rem
create tablespace openclinica
datafile '&ts_file' size 512M;
Rem
Rem
