-- OpenClinica database role creation script
-- If the role already exists you will get : ERROR: role "clinica" already exists SQL state: 42710
--
CREATE ROLE clinica LOGIN
  ENCRYPTED PASSWORD 'clinica'
  SUPERUSER NOINHERIT NOCREATEDB NOCREATEROLE;