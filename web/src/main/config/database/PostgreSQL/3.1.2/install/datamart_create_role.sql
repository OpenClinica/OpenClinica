-- OpenClinica datamart role creation script
-- If the role already exists you will get : ERROR: role "datamart" already exists SQL state: 42710
-- If you do get that error it is nothing to worry about it just means ths user exitsts already
--
CREATE ROLE datamart LOGIN
  ENCRYPTED PASSWORD 'datamart'
  NOINHERIT NOCREATEDB NOCREATEROLE;
