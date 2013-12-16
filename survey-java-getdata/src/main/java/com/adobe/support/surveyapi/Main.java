package com.adobe.support.surveyapi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main implements Runnable
{
	Logger logger = LoggerFactory.getLogger(this.getClass());
	List<Runnable> tests = new ArrayList<Runnable>();

	public static void main( String[] args ) throws ParseException
	{
		new Main(args).run();
	}

	public static String HELP_OPTION = "h";
	public static String PROPERTIES_FILE_OPTION = "p";

	private CommandLineParser parser;
	private Options options;
	private String[] args;
	private CommandLine cmdLine;
	private PropertiesHelper propertiesHelper;

	public Main( String[] args )
	{
		this.args = args;
		this.options = createOptions();
		this.parser = new BasicParser();

		try
		{
			this.cmdLine = this.parser.parse(this.options, this.args);
		}
		catch( ParseException e )
		{
			e.printStackTrace();
		}
	}

	private Options createOptions()
	{
		Options options = new Options();

		options.addOption(HELP_OPTION, false, "print this message");
		options.addOption(PROPERTIES_FILE_OPTION, true, "the properties file that contains all the options for the tool");

		return options;
	}

	public void run()
	{
		// if help, show it
		if( cmdLine.hasOption(HELP_OPTION) )
		{
			logger.trace("running help");
			showHelp();
			return;
		}

		if( !cmdLine.hasOption(PROPERTIES_FILE_OPTION) )
		{
			logger.error("must use the properties file option and supply a valid file");
			showHelp();
			return;
		}

		File propertiesFile = new File(cmdLine.getOptionValue(PROPERTIES_FILE_OPTION));

		// check that trigger option and properties file option has been given
		if( !(propertiesFile != null && propertiesFile.exists() && !propertiesFile.isDirectory()) )
		{

			logger.error( "properties file does not resolve to a correct file: file={}; exists={}; notIsDirectory={}",
						  new Object[]
					      { cmdLine.getOptionValue(PROPERTIES_FILE_OPTION), Boolean.toString(propertiesFile.exists()),
							Boolean.toString(!propertiesFile.isDirectory()) });

			showHelp();
			return;
		}

		propertiesHelper = PropertiesHelper.createInstance(propertiesFile);


		logger.info("++++++++++++++ STARTING CHECKS ++++++++++++++++");
		runTests();
		logger.info("++++++++++++++ END OF CHECKS; Goodbye ++++++++++++++++");
	}

	private void showHelp()
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "VFTNPS Analysis Tool: all arguments are mandatory (apart from -h)" +
							 " and all files and directories etc must be valid and correct",
							 options);
	}
	
	
	private void runTests() {
		new SurveyApiCall(propertiesHelper).run();
	}

}
