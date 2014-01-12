package com.d2s.subgraph.eval;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.xml.sax.SAXException;

import com.d2s.subgraph.eval.analysis.WriteAnalysis;
import com.d2s.subgraph.eval.experiments.DbpediaExperimentSetup;
import com.d2s.subgraph.eval.experiments.DbpoExperimentSetup;
import com.d2s.subgraph.eval.experiments.ExperimentSetup;
import com.d2s.subgraph.eval.experiments.LgdExperimentSetup;
import com.d2s.subgraph.eval.experiments.LmdbExperimentSetup;
import com.d2s.subgraph.eval.experiments.Sp2bExperimentSetup;
import com.d2s.subgraph.eval.experiments.SwdfExperimentSetup;
import com.d2s.subgraph.eval.generation.FetchGraphsResults;

public class RunExperiments {
	private static String CLI_ARG_RUN_QUERIES = "q";
	private static String CLI_ARG_RUN_ANALYSIS = "a";
	private static String CLI_ARG_NO_CACHE = "c";
	private static String CLI_ARG_HELP = "h";

	public static void runExperiments(ExperimentSetup experimentSetup, boolean runQueries, boolean analyzeQueries) throws Exception {
		if (runQueries) {
			FetchGraphsResults.doFetch(experimentSetup);
		}
		if (analyzeQueries)
			WriteAnalysis.doWrite(experimentSetup);
	}

	public static void runExperiments(CommandLine cmd) throws Exception {
		for (String remainingArg : cmd.getArgs()) {
			try {
				runExperiments(getExperimentSetupForArg(remainingArg, !cmd.hasOption(CLI_ARG_NO_CACHE)), cmd.hasOption(CLI_ARG_RUN_QUERIES), cmd.hasOption(CLI_ARG_RUN_ANALYSIS));
			} catch (IllegalStateException e) {
				System.out.println(e.getMessage());
			}

		}

	}

	private static ExperimentSetup getExperimentSetupForArg(String arg, boolean useQueryCacheFile) throws IllegalStateException, IOException, SAXException, ParserConfigurationException {
		if (arg.equals("dbp")) {
			return new DbpediaExperimentSetup(useQueryCacheFile);
		} else if (arg.equals("swdf")) {
			return new SwdfExperimentSetup(useQueryCacheFile);
		} else if (arg.equals("dbpo")) {
			return new DbpoExperimentSetup(DbpoExperimentSetup.QALD_KEEP_OPTIONALS, useQueryCacheFile);
		} else if (arg.equals("lgd")) {
			return new LgdExperimentSetup(useQueryCacheFile);
		} else if (arg.equals("lmdb")) {
			return new LmdbExperimentSetup(useQueryCacheFile);
		} else if (arg.equals("sp2b")) {
			return new Sp2bExperimentSetup(useQueryCacheFile);
		} else {
			throw new IllegalStateException("could not find an experiment setup class for argument " + arg);
		}
	}

	private static Options getCliOptions() {
		// create Options object
		Options options = new Options();

		// add t option
		options.addOption(CLI_ARG_RUN_QUERIES, "query", false, "fetch and run queries on subgraphs");
		options.addOption(CLI_ARG_RUN_ANALYSIS, "analysis", false, "run analysis (using cached query results)");
		options.addOption(CLI_ARG_NO_CACHE, "nocache", false, "dont use the cached set of queries");
		options.addOption(CLI_ARG_HELP, "help", false, "show this dialogue");

		return options;
	}

	@SuppressWarnings("unused")
	private static CommandLine parseOptions(String[] args) throws ParseException {
		CommandLineParser parser = new GnuParser();
		Options options = getCliOptions();
		CommandLine cmd = parser.parse(options, args);
		if (cmd.getOptions().length == 0 || cmd.hasOption(CLI_ARG_HELP)) {
			System.out.println("java -jar ... [options] [graph abbreviations (e.g. dbp, swdf)[...][...]]");
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("ant", options);
			System.exit(0);
		}
		return cmd;

	}

	public static void main(String[] args) throws Exception {
//		 RunExperiments.runExperiments(parseOptions(args));
		
		
		boolean useCachedQueries = false;
		boolean execQueries = true;
		boolean runAnalysis = false;
		
		 RunExperiments.runExperiments(new SwdfExperimentSetup(useCachedQueries), execQueries, runAnalysis);
		// new EvaluateGraphs(new
		// DbpoExperimentSetup(DbpoExperimentSetup.QALD_REMOVE_OPTIONALS)),
		// new EvaluateGraphs(new
		// DbpoExperimentSetup(DbpoExperimentSetup.QALD_KEEP_OPTIONALS)),
		// new EvaluateGraphs(new
		// DbpoExperimentSetup(DbpoExperimentSetup.QUERY_LOGS)),
		// new EvaluateGraphs(new Sp2bExperimentSetup()),
		// new EvaluateGraphs(new LmdbExperimentSetup()),
		// new EvaluateGraphs(new LgdExperimentSetup()),
		// new EvaluateGraphs(new DbpExperimentSetup()),
	}

}
