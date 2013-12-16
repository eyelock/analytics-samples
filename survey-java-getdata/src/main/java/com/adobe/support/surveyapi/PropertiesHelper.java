package com.adobe.support.surveyapi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

public class PropertiesHelper
{
	Logger logger = LoggerFactory.getLogger(this.getClass());

	public static String LOG_LEVEL_PN = "log.level";

	public static String SURVEY_API_URL_PN = "survey.api.url";
	public static String SURVEY_API_USER_PN = "survey.api.user";
	public static String SURVEY_API_PASS_PN = "survey.api.secret";
	public static String SURVEY_API_METHOD_PN = "survey.api.method";
	
	public static String SURVEY_API_RSID_PN = "survey.api.rsid";
	public static String SURVEY_API_SURVEYID_PN = "survey.api.surveyid";
	public static String SURVEY_API_STARTDATE_PN = "survey.api.startdate";
	public static String SURVEY_API_ENDDATE_PN = "survey.api.enddate";

	private Properties properties;

	String logLevel = null;

	String surveyApiUrl = null;
	String surveyApiUser = null;
	String surveyApiSecret = null;
	String surveyApiMethod = null;
	
	String reportSuiteId = null;
	String surveyId = null;
	String startDate = null;
	String endDate = null;
	

	public PropertiesHelper( File file )
	{
		properties = new Properties();
		try
		{
			properties.load(new FileReader(file));
			configureAndValidate();
		}
		catch( FileNotFoundException e )
		{
			logger.error("problem loading properties file: {}", e.getMessage());
		}
		catch( IOException e )
		{
			logger.error("problem loading properties file: {}", e.getMessage());
		}
	}
	

	public static PropertiesHelper createInstance( File propertiesFile )
	{
		PropertiesHelper properties = new PropertiesHelper(propertiesFile);
		return properties;
	}

	private boolean configureAndValidate()
	{
		setLogLevel(properties.getProperty(LOG_LEVEL_PN));	

		surveyApiUrl = properties.getProperty(SURVEY_API_URL_PN);
		surveyApiUser = properties.getProperty(SURVEY_API_USER_PN);
		surveyApiSecret = properties.getProperty(SURVEY_API_PASS_PN);
		surveyApiMethod = properties.getProperty(SURVEY_API_METHOD_PN);
		
		reportSuiteId = properties.getProperty(SURVEY_API_RSID_PN);
		surveyId = properties.getProperty(SURVEY_API_SURVEYID_PN);
		startDate = properties.getProperty(SURVEY_API_STARTDATE_PN);
		endDate = properties.getProperty(SURVEY_API_ENDDATE_PN);


		return true;
	}

	public String getLogLevel()
	{
		return logLevel;
	}

	/**
	 * Bad practice, but I also set the property and _Change_ the log level for
	 * LOGBAK.
	 * 
	 * @param logLevel
	 */
	public void setLogLevel( String logLevel )
	{
		this.logLevel = logLevel;
		Level logbakLevel = null;

		// get from a string
		if( "trace".equals(logLevel) )
		{
			logbakLevel = Level.TRACE;
		}
		else if( "debug".equals(logLevel) )
		{
			logbakLevel = Level.DEBUG;
		}
		else if( "info".equals(logLevel) )
		{
			logbakLevel = Level.INFO;
		}
		else if( "warn".equals(logLevel) )
		{
			logbakLevel = Level.WARN;
		}
		else if( "error".equals(logLevel) )
		{
			logbakLevel = Level.ERROR;
		}

		// default to debug if didn't find
		if( logbakLevel == null )
		{
			logger.warn("invalid string for the log level: {}", logLevel);
			logbakLevel = Level.DEBUG;
		}

		// set the log level
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
				.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		root.setLevel(logbakLevel);
	}


	public String getSurveyApiUrl() {
		return surveyApiUrl;
	}


	public void setSurveyApiUrl(String surveyApiUrl) {
		this.surveyApiUrl = surveyApiUrl;
	}


	public String getSurveyApiUser() {
		return surveyApiUser;
	}


	public void setSurveyApiUser(String surveyApiUser) {
		this.surveyApiUser = surveyApiUser;
	}


	public String getSurveyApiSecret() {
		return surveyApiSecret;
	}


	public void setSurveyApiSecret(String surveyApiSecret) {
		this.surveyApiSecret = surveyApiSecret;
	}


	public String getSurveyApiMethod() {
		return surveyApiMethod;
	}


	public void setSurveyApiMethod(String surveyApiMethod) {
		this.surveyApiMethod = surveyApiMethod;
	}


	public String getReportSuiteId() {
		return reportSuiteId;
	}


	public void setReportSuiteId(String reportSuiteId) {
		this.reportSuiteId = reportSuiteId;
	}


	public String getSurveyId() {
		return surveyId;
	}


	public void setSurveyId(String surveyId) {
		this.surveyId = surveyId;
	}


	public String getStartDate() {
		return startDate;
	}


	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}


	public String getEndDate() {
		return endDate;
	}


	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

}
