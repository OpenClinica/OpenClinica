package org.akaza.openclinica.log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.akaza.openclinica.dao.core.CoreResources;
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

	private boolean started = false;

	@Override
	public void start() {
		if (started)
			return;
		Context context = getContext();
		CoreResources.loadAllProperties();
		context.putProperty("log.dir", CoreResources.getField("log.dir"));
		context.putProperty("logLocation", CoreResources.getField("logLocation"));
		context.putProperty("logLevel", CoreResources.getField("logLevel"));
		context.putProperty("hibernateSQLLogLevel", CoreResources.getField("hibernateSQLLogLevel"));
		context.putProperty("hibernateTypeLogLevel", CoreResources.getField("hibernateTypeLogLevel"));
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
}
