package experiment.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import experiment.utils.report.rules.CombinationComparisonRules;
import experiment.utils.report.rules.IComparisonRule;
import experiment.utils.report.rules.NumberDecreaseComparisonRule;
import experiment.utils.report.rules.NumberIncreaseComparisonRule;
import experiment.utils.report.rules.TextComparisonRule;
import sav.common.core.utils.CollectionUtils;

/**
 * @author LLT
 * 
 */
@SuppressWarnings("static-access")
public class Parameters {
	private static final Options opts;
	static final String WORKING_FOLDER = "workingFolder";
	static final String AGLINMENT_FUNCTION = "a";
	static final String COMPARISON_FUNCTION = "c";
	/* keys:  data#project#method:  */
	static final String COMPARISON_KEYS  = "keys";
	static final String INPUT_EXCEL = "input_excels";
	/* ex:
	 * NumberIncreaseComparisonRule#comparedCol1#comparedCol2#compareCol3  TextComparisonRule#comparedCol4 NumberDecreaseComparisonRule#comparedCol5*/
	static final String COMPARISON_RULES = "cmpRules";
	/* same with comparisonRules, but it combines all comparison rules priority by orders */
	static final String COMPARISION_COMBINATION_RULES = "combCmpRules";
	static final String COMPARISON_STATISTIC = "cmpStats";

	private String workingFolder;
	private Function function;
	private List<String> inputExcels = new ArrayList<String>();
	private Map<String, List<String>> comparisonKeys = new HashMap<String, List<String>>();
	private List<IComparisonRule> comparisonRules = new ArrayList<>();
	private boolean statistic;
	
	static {
		opts = new Options();
		opts.addOption(new Option(WORKING_FOLDER, true, "excel input folder"));
		opts.addOption(new Option(AGLINMENT_FUNCTION, false, "align rows of excels that have the same key"));
		opts.addOption(new Option(COMPARISON_FUNCTION, false, "compare excels"));
		opts.addOption(OptionBuilder.withArgName(COMPARISON_KEYS)
				.isRequired(true)
				.hasArgs()
				.create(COMPARISON_KEYS));
		opts.addOption(OptionBuilder.withArgName(INPUT_EXCEL)
				.withDescription("excel files to compare")
				.hasArgs()
				.create(INPUT_EXCEL));
		opts.addOption(OptionBuilder.withArgName(COMPARISON_RULES)
				.withDescription("defined rules to compare excel records")
				.hasArgs()
				.create(COMPARISON_RULES));
		opts.addOption(OptionBuilder.withArgName(COMPARISION_COMBINATION_RULES)
				.withDescription("defined rules to compare excel records")
				.hasArgs()
				.create(COMPARISION_COMBINATION_RULES));
		opts.addOption(OptionBuilder.withArgName(COMPARISON_STATISTIC)
				.withDescription("defined rules to compare excel records")
				.hasArg(false)
				.create(COMPARISON_STATISTIC));
	}

	public static Parameters parse(String[] args) throws ParseException {
		CommandLineParser parser = new GnuParser();
		CommandLine cmd = parser.parse(opts, args);
		if (cmd.getOptions().length == 0) {
			throw new ParseException("No specified option");
		}
		Parameters params = new Parameters();
		if (cmd.hasOption(WORKING_FOLDER)) {
			params.workingFolder = cmd.getOptionValue(WORKING_FOLDER);
		}
		if (cmd.hasOption(AGLINMENT_FUNCTION)) {
			params.function = Function.ALIGNMENT;
		} else if (cmd.hasOption(COMPARISON_FUNCTION)) {
			params.function = Function.COMPARISON;
		}
		if (params.function == null) {
			throw new IllegalArgumentException(
					"Missing input function to perform (-a for alignment, -c for comparison)!");
		}
		if (cmd.hasOption(COMPARISON_KEYS)) {
			String[] values = cmd.getOptionValues(COMPARISON_KEYS);
			for (String value : values) {
				String[] frags = value.split("#");
				List<String> keys = new ArrayList<>();
				for (int i = 1; i < frags.length; i++) {
					keys.add(frags[i]);
				}
				params.comparisonKeys.put(frags[0], keys);
			}
		}
		if (cmd.hasOption(INPUT_EXCEL)) {
			params.inputExcels = CollectionUtils.toArrayList(cmd.getOptionValues(INPUT_EXCEL));
		}
		
		if (cmd.hasOption(COMPARISON_RULES)) {
			String[] values = cmd.getOptionValues(COMPARISON_RULES);
			params.comparisonRules = buildComparisonRules(values);
		} else if (cmd.hasOption(COMPARISION_COMBINATION_RULES)) {
			String[] values = cmd.getOptionValues(COMPARISION_COMBINATION_RULES);
			params.comparisonRules.add(new CombinationComparisonRules(buildComparisonRules(values)));
		} 
		if (cmd.hasOption(COMPARISON_STATISTIC)) {
			params.statistic = true;
		}
		
		return params;
	}
	
	private static List<IComparisonRule> buildComparisonRules(String[] args) {
		List<IComparisonRule> rules = new ArrayList<>();
		for (String value : args) {
			String[] frags = value.split("#");
			List<String> cols = new ArrayList<>();
			for (int i = 1; i < frags.length; i++) {
				cols.add(frags[i]);
			}
			IComparisonRule rule = null;
			ComparisonRuleEnum type = ComparisonRuleEnum.valueOf(frags[0]);
			switch (type) {
			case textDiff:
				rule = new TextComparisonRule(cols);
				break;
			case numDecr:
				rule = new NumberDecreaseComparisonRule(cols);
				break;
			case numIncr:
				rule = new NumberIncreaseComparisonRule(cols);
				break;
			}
			rules.add(rule);
		}
		return rules;
	}

	public String getWorkingFolder() {
		if (workingFolder == null) {
			workingFolder = System.getProperty("user.dir");
		}
		return workingFolder;
	}

	public Function getFunction() {
		return function;
	}

	public void setFunction(Function function) {
		this.function = function;
	}

	public List<String> getInputExcels() {
		return inputExcels;
	}
	
	public Map<String, List<String>> getComparisonKeys() {
		return comparisonKeys;
	}
	
	public List<IComparisonRule> getComparisonRules() {
		return comparisonRules;
	}
	
	public boolean isStatistic() {
		return statistic;
	}
	
}
