package com.ef;

import com.ef.config.BatchConfiguration;
import com.ef.config.LogDb;
import com.ef.config.LogResult;
import com.ef.config.LogResultDb;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.ParseException; 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Parser {
	
	private static final Logger log = LoggerFactory.getLogger( Parser.class );
    private static final String CMD_LINE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final long SECS_IN_HOUR = 60 * 60;
    private static final long SECS_IN_DAY = SECS_IN_HOUR * 24;
    
    private static final SimpleDateFormat sdfCmdLine = new SimpleDateFormat( CMD_LINE_FORMAT );
    
    private static String startDate = null;
    private static String duration = null;
    private static String threshold = null;
    
	public static void usePosixParser(final String[] commandLineArguments) {
		final CommandLineParser cmdLinePosixParser = new PosixParser();
		final Options posixOptions = constructPosixOptions();
		CommandLine commandLine;
		try {
			commandLine = cmdLinePosixParser.parse(posixOptions, commandLineArguments);
			if (commandLine.hasOption("startDate")) {
				startDate = commandLine.getOptionValue("startDate");
				log.info("You want to define startDate");
			}
			if (commandLine.hasOption("duration")) {
				duration = commandLine.getOptionValue("duration");
				log.info("You want to define duration");
			}
			if (commandLine.hasOption("threshold")) {
				threshold = commandLine.getOptionValue("threshold");
				log.info("You want to define threshold");
			}
		} catch (ParseException parseException) // checked exception
		{
			log.error("Encountered exception while parsing using PosixParser:\n" + parseException.getMessage());
		}
	}  
    
    
	public static Options constructPosixOptions() {
		final Options posixOptions = new Options();
		posixOptions.addOption("startDate", true, "startDate");
		posixOptions.addOption("duration", true, "duration (Hourly/Daily)");
		posixOptions.addOption("threshold", true, "threshold");
		return posixOptions;
	}
    
	public static void displayProvidedCommandLineArguments(final String[] commandLineArguments,
			final OutputStream out) {
		final StringBuffer buffer = new StringBuffer();
		for (final String argument : commandLineArguments) {
			buffer.append(argument).append(" ");
		}
		try {
			out.write((buffer.toString() + "\n").getBytes());
		} catch (IOException ioEx) {
			log.error("WARNING: Exception encountered trying to write to OutputStream:\n" + ioEx.getMessage());
			log.info(buffer.toString());
		}
	}
	
	public static void displayHeader(final OutputStream out) {
		final String header = "[Log Management using Spring Batch by Javier Mercado]\n";
		try {
			out.write(header.getBytes());
		} catch (IOException ioEx) {
			System.out.println(header);
		}
	}
	
	public static void displayBlankLines(final int numberBlankLines, final OutputStream out) {
		try {
			for (int i = 0; i < numberBlankLines; ++i) {
				out.write("\n".getBytes());
			}
		} catch (IOException ioEx) {
			for (int i = 0; i < numberBlankLines; ++i) {
				System.out.println();
			}
		}
	}
	
	public static void printUsage(final String applicationName, final Options options, final OutputStream out) {
		final PrintWriter writer = new PrintWriter(out);
		final HelpFormatter usageFormatter = new HelpFormatter();
		usageFormatter.printUsage(writer, 80, applicationName, options);
		writer.flush();
	}
	
	public static void printHelp(final Options options, final int printedRowWidth, final String header,
			final String footer, final int spacesBeforeOption, final int spacesBeforeOptionDescription,
			final boolean displayUsage, final OutputStream out) {
		final String commandLineSyntax = "java -cp target/parser-0.0.1-SNAPSHOT-jar-with-dependencies.jar com.ef.Parser";
		final PrintWriter writer = new PrintWriter(out);
		final HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.printHelp(writer, printedRowWidth, commandLineSyntax, header, options, spacesBeforeOption,
				spacesBeforeOptionDescription, footer, displayUsage);
		writer.flush();
	}


    public static void main(String[] args) throws Throwable {
    	
		displayBlankLines(1, System.out);
		displayHeader(System.out);
		displayBlankLines(2, System.out);
		if (args.length < 3) {
			System.out.println("-- HELP --");
			printHelp(constructPosixOptions(), 80, "POSIX HELP", "End of POSIX Help", 3, 5, true, System.out);
			displayBlankLines(1, System.out);
			System.exit(0);
		}
		displayProvidedCommandLineArguments(args, System.out);
		usePosixParser(args);
    	String cleanStartDate = startDate.replace(  '.', ' '  );
        Timestamp from = new Timestamp( sdfCmdLine.parse( cleanStartDate ).getTime() );
        Timestamp to = null;
        if(duration.toLowerCase().equalsIgnoreCase("hourly")) {
        	to = Timestamp.from( from.toInstant().plusSeconds( SECS_IN_HOUR ));
        } else if(duration.toLowerCase().equalsIgnoreCase("daily")){
        	to = Timestamp.from( from.toInstant().plusSeconds( SECS_IN_DAY ));
        } else {
			System.out.println("-- HELP --");
			printHelp(constructPosixOptions(), 80, "POSIX HELP", "End of POSIX Help", 3, 5, true, System.out);
			displayBlankLines(1, System.out);
			System.exit(0);
        }

        ApplicationContext context = new AnnotationConfigApplicationContext(BatchConfiguration.class);

        JobLauncher jobLauncher = context.getBean(JobLauncher.class);
        Job job = context.getBean(Job.class);

        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
        jobParametersBuilder.addDate("date", new Date());
        jobParametersBuilder.addString("fileName", "access.log");
        JobParameters jobParameters = jobParametersBuilder.toJobParameters();

        JobExecution jobExecution = jobLauncher.run(job, jobParameters);

        log.info("Exit status: " + jobExecution.getExitStatus().getExitCode());

        JobInstance jobInstance = jobExecution.getJobInstance();
        log.info("job instance Id: " + jobInstance.getId());
        

        LogDb logDb = context.getBean( "logDb", LogDb.class );
        int dbRowCount = logDb.countLogRows();
        log.info( "LOG_REGISTRATION table contains {} rows.", dbRowCount );
        
      		
        
        Map<String, Integer> ipMap = logDb.getLogCountRange( from,
                to, Integer.parseInt(threshold) );

        log.info( ipMap.size() + " IP(s) having more than " + threshold + " hits" );
        
        if( !ipMap.isEmpty() ) {
            ipMap.entrySet().stream().forEach( entry -> {
                log.info( entry.getKey() + ", " + entry.getValue() );
            } );
            
            LogResultDb logResultDb = context.getBean( "logResultDb", LogResultDb.class );
            String reason = String.format( "Exceeded count %s of: %d", duration, Integer.parseInt(threshold) );
            List<LogResult> resultsList = new ArrayList<>( ipMap.size() );
            ipMap.entrySet().stream().forEach( entry -> { resultsList.add( new LogResult( entry.getKey(), entry.getValue(), reason ) ); });

            log.info( "Loading {} results to LOG_RESULTS table", ipMap.size() );
            log.info( "Reason: {}", reason );
            logResultDb.batchInsert( resultsList );
            
        }
    }
}
