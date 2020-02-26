# installation instructions for LibreClinica-1.0 on Debian 10 (Buster)

**prerequisite**  
It is expected that you have experience on how to install software and how to edit text files on Debian. 
Furthermore it is expected that you have a running Debian 10 (buster) installation with shell access and 
that the user you use for the installation is a member of the sudo group. For running LibreClinica you need a working 
mail transfer agent (MTA) to allow LibreClinica to send emails. How to install, configure and run a MTA is not explained
in this instruction.  
**This installation instruction is not a complete guide to run a productive LibreClinica instance since it is missing steps
to secure your server like configuring the firewall or enabling https. But it is sufficant for your system administrator
to get you a working copy of LibreClinica.**

_This installation instruction is a blue print for a successfull LibreClinica installation. If you follow these steps
literally you end up with a working LibreClinica instance. Experienced users can modify the instructions to their needs
when necessary but should be aware that there are dependencies between the steps and that changes to one step might require
changes to another step._

1. **install required software components:**
    1. update package list: `sudo apt update`
    1. install software: `sudo apt install tomcat9 postgresql-11`  
        _openjdk-11 is installed as a dependency of tomcat9_
1. **setup directories:**  
    _The folder in this example is named libreclinica since this is the default path used when the
    *.war archive copied is named libreclinica.war. If you like to name the folder differently then name your
    war archive differently or configure the datainfo.properties (step 5) to use a different folder._
    1. configuration directory: `sudo mkdir -p /usr/share/tomcat9/libreclinica/config`
    1. log directory: `sudo mkdir /usr/share/tomcat9/libreclinica/logs`
    1. data directory: `sudo mkdir /usr/share/tomcat9/libreclinica/data`
    1. change owner: `sudo chown -R tomcat:tomcat /usr/share/tomcat9/libreclinica`
    1. create softlink: `sudo ln -s /usr/share/tomcat9/libreclinica/config/ /usr/share/tomcat9/libreclinica.config`
1. **setup database**
    1. create role: `sudo -u postgres createuser -e -I -D -R -S -P clinica`  
        *update datainfo.properties with the password you entered for the new postgres user (step 5)*
    1. create database: `sudo -u postgres createdb -e -O clinica -E UTF8 libreclinica`
1. **copy the \*.war archive** to the webapps folder  
    `cp LibreClinica-1.0.0.war /var/lib/tomcat9/webapps/<context name>.war`  
    *context name is the name that comes usually after the slash  
    e.g. for https://libreclinica.org/libreclinica it is
    /var/lib/tomcat9/webapps/libreclinica.war*
1. **configure datainfo.properties**  
    Edit /usr/share/tomcat9/libreclinica.config/datainfo.properties to match your requirements.  
    _Detailed instructions on how to configure it properly can be found in the different 
    sections of the datainfo.properties._
    
    **relevant keys for your datainfo.properties**  
    For a common installation it should be sufficient to change the following keys:
    1. **database**
        * dbType=postgres
        * dbUser=clinica
        * dbPass=SecretPassword
        * db=libreclinica
        * dbPort=5432
        * dbHost=localhost
    1. **email server**
        * mailHost=smtp.example.com
        * mailPort=25|465|custom port
        * mailProtocol=smtp|smtps
        * mailUsername=SecretUser
        * mailPassword=SecretPassword
        * mailSmtpAuth=true|false
        * mailSmtpStarttls.enable=true|false
        * mailSmtpsAuth=true|false
        * mailSmtpsStarttls.enable=true|false
        * mailSmtpConnectionTimeout=5000
    1. **misc**
        * mailErrorMsg=support@example.com
        * adminEmail=admin@example.com
        * sysURL=https://example.com/libreclinica/MainMenu
1. **setup ReadWritePaths**  
    edit /etc/systemd/system/multi-user.target.wants/tomcat9.service and  
    add `ReadWritePaths=/usr/share/tomcat9/libreclinica`  
    and reload the unit files with `systemctl daemon-reload`
1. **restart tomcat** `systemctl restart tomcat9`


You now should be able to access your LibreClinica installation port 8080. e.g.  
http://<ip of your machine>:8080/libreclinica

In a productive environment your system administrator should configure a web server 
like nginx or apache to act as a reverse proxy for your LibreClinica installation so that
you can access your installation from the URL configured for key _sysURL_.
