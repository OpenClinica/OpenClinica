# Define variables (change those to match your environment)
$env:CATALINA_HOME=""
$WEBAPP_HOME=""
$TOMCAT_BASE_PATH=""

$TOMCAT_CATALINA_PATH="$TOMCAT_BASE_PATH\bin\catalina.bat"
$TOMCAT_WEBAPPS_PATH="$TOMCAT_BASE_PATH\webapps"
$DATAINFO_PATH="$WEBAPP_HOME\web\src\main\resources\datainfo.properties"
$EXTRACT_PATH="$WEBAPP_HOME\web\src\main\resources\extract.properties"

# You shouldn't have to change anything under this line !
$pattern="<version>"
$fname="$WEBAPP_HOME\pom.xml"
$result=Select-String -Path $fname -Pattern $pattern | Select-Object -First 1
$ocVersion=(($result -split "<version>")[1] -split "</version>")[0]
$path="$WEBAPP_HOME\web\target\OpenClinica-web-$ocVersion"
Write-Output "$ocVersion"

# Create the config folders if they don't exist
$web_path="$WEBAPP_HOME\web\target\OpenClinica-web-$ocVersion"
$ws_path="$WEBAPP_HOME\ws\target\OpenClinica-ws-$ocVersion"
$web_config_folder_name="openclinica-web-$ocVersion.config"
$ws_config_folder_name="openclinica-ws-$ocVersion.config"
if(!(Test-Path -Path "$TOMCAT_BASE_PATH\$web_config_folder_name")){
    New-Item -ItemType "directory" -Path "$TOMCAT_BASE_PATH\$web_config_folder_name"
}
if(!(Test-Path -Path "$TOMCAT_BASE_PATH\$ws_config_folder_name")){
    New-Item -ItemType "directory" -Path "$TOMCAT_BASE_PATH\$ws_config_folder_name"
}

# Copy the config files if they don't exist
Copy-Item -Path "$DATAINFO_PATH" -Destination "$TOMCAT_BASE_PATH\$web_config_folder_name"
Copy-Item -Path "$EXTRACT_PATH" -Destination "$TOMCAT_BASE_PATH\$web_config_folder_name"
Copy-Item -Path "$DATAINFO_PATH" -Destination "$TOMCAT_BASE_PATH\$ws_config_folder_name"
Copy-Item -Path "$EXTRACT_PATH" -Destination "$TOMCAT_BASE_PATH\$ws_config_folder_name"

# Stop tomcat
Start-Process -FilePath "$TOMCAT_CATALINA_PATH" -ArgumentList "stop" -NoNewWindow -PassThru -Wait
# Build
Set-Location -Path "$WEBAPP_HOME"
Invoke-Expression "mvn clean package -DskipTests"
# Deploy OpenClinica-web-$ocVersion
Remove-Item -LiteralPath "$TOMCAT_WEBAPPS_PATH\OpenClinica-web-$ocVersion" -Force -Recurse
Copy-Item -Path "$path" -Destination "$TOMCAT_WEBAPPS_PATH\OpenClinica-web-$ocVersion" -Recurse

# Deploy OpenClinica-ws-$ocVersion
Remove-Item -LiteralPath "$TOMCAT_WEBAPPS_PATH\OpenClinica-ws-$ocVersion" -Force -Recurse
Copy-Item -Path "$path" -Destination "$TOMCAT_WEBAPPS_PATH\OpenClinica-ws-$ocVersion" -Recurse

# Start tomcat
Start-Process -FilePath "$TOMCAT_CATALINA_PATH" -ArgumentList "start" -NoNewWindow -PassThru -Wait
