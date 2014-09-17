package org.akaza.openclinica.log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;

public class LoggerStartupListener extends ContextAwareBase implements
		LoggerContextListener, LifeCycle {

	ResourceLoader resourceLoader;

	private static Properties DATAINFO;
	private static String webapp;
	private Properties dataInfoProp;

	private boolean started = false;

	@Override
	public void start() {
		if (started)
			return;
		Context context = getContext();

		if (resourceLoader == null)
			resourceLoader = new DefaultResourceLoader();
		try {
			webapp = getWebAppName(resourceLoader.getResource("/").getURI().getPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			DATAINFO = loadProperties("datainfo.properties");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		getPropertiesSource();

		context.putProperty("log.dir", getField("log.dir"));
		context.putProperty("logLocation", getField("logLocation"));
		context.putProperty("logLevel", getField("logLevel"));
		context.putProperty("syslog.host", getField("syslog.host"));
		context.putProperty("syslog.port", getField("syslog.port"));
		context.putProperty("collectStats", getField("collectStats"));
		context.putProperty("usage.stats.host", getField("usage.stats.host"));
		context.putProperty("usage.stats.port", getField("usage.stats.port"));
		context.putProperty("OpenClinica.version",getField("OpenClinica.version"));

		started = true;
	}

	@Override
	public void stop() {
	}

	@Override
	public boolean isStarted() {
		return started;
	}

	@Override
	public boolean isResetResistant() {
		return true;
	}

	@Override
	public void onStart(LoggerContext context) {
	}

	@Override
	public void onReset(LoggerContext context) {
	}

	@Override
	public void onStop(LoggerContext context) {
	}

	@Override
	public void onLevelChange(Logger arg0, Level arg1) {
		// TODO Auto-generated method stub

	}

	private static String replaceWebapp(String value) {

		if (value.contains("${WEBAPP}")) {
			value = value.replace("${WEBAPP}", webapp);
		}

		else if (value.contains("${WEBAPP.lower}")) {
			value = value.replace("${WEBAPP.lower}", webapp.toLowerCase());
		}
		if (value.contains("$WEBAPP.lower")) {
			value = value.replace("$WEBAPP.lower", webapp.toLowerCase());
		} else if (value.contains("$WEBAPP")) {
			value = value.replace("$WEBAPP", webapp);
		}

		return value;
	}

	private static String replaceCatHome(String value) {
		String catalina = null;
		if (catalina == null) {
			catalina = System.getProperty("CATALINA_HOME");
		}

		if (catalina == null) {
			catalina = System.getProperty("catalina.home");
		}

		if (catalina == null) {
			catalina = System.getenv("CATALINA_HOME");
		}

		if (catalina == null) {
			catalina = System.getenv("catalina.home");
		}

		if (value.contains("${catalina.home}") && catalina != null) {
			value = value.replace("${catalina.home}", catalina);
		}

		if (value.contains("$catalina.home") && catalina != null) {
			value = value.replace("$catalina.home", catalina);
		}

		return value;
	}

	public static String getField(String key) {
		String value = DATAINFO.getProperty(key);
		if (value != null) {
			value = value.trim();
		}
		return value == null ? "" : value;
	}

	public void getPropertiesSource() {
		try {
			String filePath = "$catalina.home/$WEBAPP.lower.config";

			filePath = replaceWebapp(filePath);
			filePath = replaceCatHome(filePath);

			String dataInfoPropFileName = filePath + "/datainfo.properties";

			Properties OC_dataDataInfoPropertiesConfig = getPropValues(
					dataInfoProp, dataInfoPropFileName);

			if (OC_dataDataInfoPropertiesConfig != null)
				DATAINFO = OC_dataDataInfoPropertiesConfig;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Properties getPropValues(Properties prop, String propFileName)
			throws IOException {

		prop = new Properties();
		File file = new File(propFileName);
		if (!file.exists())
			return null;

		InputStream inputStream = new FileInputStream(propFileName);
		prop.load(inputStream);

		return prop;
	}

	public String getWebAppName(String servletCtxRealPath) {
		String webAppName = null;
		if (null != servletCtxRealPath) {
			String[] tokens = servletCtxRealPath.split("/");
			webAppName = tokens[(tokens.length - 3)].trim();
		}
		return webAppName;
	}


	
	public Properties loadProperties(String fileProps) throws IOException {
		Properties p = new Properties();
		InputStream inpStream = null;
		 
		try {
		 		inpStream = this.getClass().getClassLoader().getResourceAsStream(fileProps);
		 		p.load(inpStream);
		        return p;
		} finally {
		 
		inpStream.close();
		}
		 
		}
	
}
