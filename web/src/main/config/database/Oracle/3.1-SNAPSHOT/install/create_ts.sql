set echo off
set feedback off
set verify off
set serveroutput on size 20000
Rem
Rem
accept ts_name prompt "Specify Tablespace name for Clinica Data: "
Rem
prompt If the tablespace is new, please specify the following
prompt Tablespace Size ( (S)mall - 32MB, (M)edium - 256MB, (L)arge -512MB
prompt
accept ts_type prompt "Specify Tablespace type for Clinica Data (S/M/L): "
Rem
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
datafile '&ts_file' size &&ts_size;
Rem
Rem
