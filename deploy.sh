# Define variables (change those to match your environment)
WEBAPP_HOME=~/projects/OpenClinica
TOMCAT_CATALINA_PATH=/opt/tomcat/libexec/bin/catalina.sh
TOMCAT_WEBAPPS_PATH=/opt/tomcat/libexec/webapps

# You shouldn't have to change anything under this line ! 
pattern="<version>"
fname=$WEBAPP_HOME/pom.xml
result=$(grep -m 1 "$pattern" "$fname")
ocVersion=$(awk -F '<version>|</version>' '{print $2}' <<< "$result")
path=$WEBAPP_HOME/web/target/OpenClinica-web-$ocVersion
echo "$ocVersion , $path"


$TOMCAT_CATALINA_PATH stop
cd WEBAPP_HOME
mvn clean package -DskipTests
rm -rf       $TOMCAT_WEBAPPS_PATH/OpenClinica-web-$ocVersion
cp -R  $path $TOMCAT_WEBAPPS_PATH/OpenClinica-web-$ocVersion

rm -rf       $TOMCAT_WEBAPPS_PATH/OpenClinica-ws-$ocVersion
cp -R  $path $TOMCAT_WEBAPPS_PATH/OpenClinica-ws-$ocVersion

$TOMCAT_CATALINA_PATH start
