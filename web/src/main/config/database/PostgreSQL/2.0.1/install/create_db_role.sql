/* Creates the role that will own the entities in the OpenClinica application's database
   -- You will not need to execute this file more than once on the same DBMS
   -- This role may already exist if you have already installed another instance or previous version of OpenClinica
   Authors: jsampson
*/

--Change the password to secure your database
--Remember to update the connection password in your web application's configuration file (the default file is CATALINA_HOME\conf\Catalina\localhost\OpenClinica.xml)
CREATE USER clinica WITH PASSWORD 'clinica' NOCREATEDB NOCREATEUSER;
