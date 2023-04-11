# Define variables (change those to match your environment)
WEBAPP_HOME=~/projects/OpenClinica
TOMCAT_BASE_PATH=/opt/tomcat/libexec
TOMCAT_CATALINA_PATH=$TOMCAT_BASE_PATH/bin/catalina.sh
TOMCAT_WEBAPPS_PATH=$TOMCAT_BASE_PATH/webapps
DATAINFO_PATH=$WEBAPP_HOME/web/src/main/resources/datainfo.properties
EXTRACT_PATH=$WEBAPP_HOME/web/src/main/resources/extract.properties


# Figure out the version numbers based on pom.xml
pattern="<version>"
fname=$WEBAPP_HOME/pom.xml
result=$(grep -m 1 "$pattern" "$fname")
ocVersion=$(awk -F '<version>|</version>' '{print $2}' <<< "$result")
echo "$ocVersion"

# Create the config folders if they don't exist
web_path=$WEBAPP_HOME/web/target/OpenClinica-web-$ocVersion
ws_path=$WEBAPP_HOME/ws/target/OpenClinica-ws-$ocVersion
web_config_folder_name=openclinica-web-$ocVersion.config
ws_config_folder_name=openclinica-ws-$ocVersion.config
mkdir -p $TOMCAT_BASE_PATH/$web_config_folder_name
mkdir -p $TOMCAT_BASE_PATH/$ws_config_folder_name

# Copy the config files if they don't exist
cp -n $DATAINFO_PATH $TOMCAT_BASE_PATH/$web_config_folder_name
cp -n $EXTRACT_PATH $TOMCAT_BASE_PATH/$web_config_folder_name
cp -n $DATAINFO_PATH $TOMCAT_BASE_PATH/$ws_config_folder_name
cp -n $EXTRACT_PATH $TOMCAT_BASE_PATH/$ws_config_folder_name

# Stop tomcat
$TOMCAT_CATALINA_PATH stop
# Build
cd WEBAPP_HOME
mvn clean package -DskipTests
# Deploy OpenClinica-web-$ocVersion
rm -rf $TOMCAT_WEBAPPS_PATH/OpenClinica-web-$ocVersion
cp -R  $web_path $TOMCAT_WEBAPPS_PATH/OpenClinica-web-$ocVersion

# Deploy OpenClinica-ws-$ocVersion
rm -rf $TOMCAT_WEBAPPS_PATH/OpenClinica-ws-$ocVersion
cp -R  $ws_path $TOMCAT_WEBAPPS_PATH/OpenClinica-ws-$ocVersion

# Start tomcat
$TOMCAT_CATALINA_PATH start



