/*
 * Copyright (C) 2017 University of Pittsburgh.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package edu.pitt.dbmi.causal.cmd;

import edu.cmu.tetrad.util.ParamDescription;
import edu.cmu.tetrad.util.ParamDescriptions;
import edu.pitt.dbmi.causal.cmd.tetrad.TetradAlgorithms;
import edu.pitt.dbmi.causal.cmd.tetrad.TetradIndependenceTests;
import edu.pitt.dbmi.causal.cmd.tetrad.TetradScores;
import edu.pitt.dbmi.causal.cmd.util.DataTypes;
import edu.pitt.dbmi.causal.cmd.util.Delimiters;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 *
 * Aug 27, 2017 10:42:03 PM
 *
 * @author Kevin V. Bui (kvb2@pitt.edu)
 */
public class CmdOptions {

    private static final CmdOptions INSTANCE = new CmdOptions();

    private final Map<String, Option> options = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    private CmdOptions() {
        addRequiredOptions();
        addOptionalOptions();
    }

    public static CmdOptions getInstance() {
        return INSTANCE;
    }

    public Option getLongOption(String param) {
        return options.get(param);
    }

    public boolean hasLongParam(String param) {
        return options.containsKey(param);
    }

    public Options getOptions() {
        Options opts = new Options();
        options.entrySet().forEach(e -> {
            opts.addOption(e.getValue());
        });

        return opts;
    }

    public Options getMainOptions() {
        List<Option> optList = getBaseOptions();
        optList.add(options.get(CmdParams.HELP));
        optList.add(options.get(CmdParams.HELP_ALL));
        optList.add(options.get(CmdParams.VERSION));

        return toOptions(optList);
    }

    public Options toOptions(List<Option> optionList) {
        Options opts = new Options();

        if (optionList != null) {
            optionList.forEach(e -> opts.addOption(e));
        }

        return opts;
    }

    public List<Option> getBaseOptions() {
        List<Option> opts = new LinkedList<>();

        getRequiredOptions().forEach(e -> opts.add(e));

        // dataset options
        opts.add(options.get(CmdParams.QUOTE_CHAR));
        opts.add(options.get(CmdParams.COMMENT_MARKER));

        // output options
        opts.add(options.get(CmdParams.FILE_PREFIX));
        opts.add(options.get(CmdParams.JSON));
        opts.add(options.get(CmdParams.DIR_OUT));

        // data validation options
        opts.add(options.get(CmdParams.SKIP_VALIDATION));

        opts.add(options.get(CmdParams.SKIP_LATEST));

        return opts;
    }

    private void addOptionalOptions() {
        options.put(CmdParams.QUOTE_CHAR, Option.builder().longOpt(CmdParams.QUOTE_CHAR).desc("Single character denotes quote.").hasArg().argName("character").build());
        options.put(CmdParams.MISSING_MARKER, Option.builder().longOpt(CmdParams.MISSING_MARKER).desc("Denotes missing value.").hasArg().argName("string").build());
        options.put(CmdParams.COMMENT_MARKER, Option.builder().longOpt(CmdParams.COMMENT_MARKER).desc("Comment marker.").hasArg().argName("string").build());
        options.put(CmdParams.NO_HEADER, Option.builder().longOpt(CmdParams.NO_HEADER).desc("Indicates tabular dataset has no header.").build());

        options.put(CmdParams.HELP, new Option(null, CmdParams.HELP, false, "Show help."));
        options.put(CmdParams.HELP_ALL, new Option(null, CmdParams.HELP_ALL, false, "Show all options and descriptions."));
        options.put(CmdParams.VERSION, new Option(null, CmdParams.VERSION, false, "Show version."));
        options.put(CmdParams.FILE_PREFIX, Option.builder().longOpt(CmdParams.FILE_PREFIX).desc("Output file name prefix.").hasArg().argName("string").build());
        options.put(CmdParams.JSON, new Option(null, CmdParams.JSON, false, "Write out graph as json."));
        options.put(CmdParams.DIR_OUT, Option.builder().longOpt(CmdParams.DIR_OUT).desc("Output directory").hasArg().argName("directory").build());

        options.put(CmdParams.KNOWLEDGE, Option.builder().longOpt(CmdParams.KNOWLEDGE).desc("Prior knowledge file.").hasArg().argName("file").build());
        options.put(CmdParams.EXCLUDE_VARIABLE, Option.builder().longOpt(CmdParams.EXCLUDE_VARIABLE).desc("Variables to be excluded from run.").hasArg().argName("file").build());

        options.put(CmdParams.SKIP_VALIDATION, new Option(null, CmdParams.SKIP_VALIDATION, false, "Skip validation."));
        options.put(CmdParams.SKIP_LATEST, new Option(null, CmdParams.SKIP_LATEST, false, "Skip checking for latest software version."));

        options.put(CmdParams.TEST, Option.builder().longOpt(CmdParams.TEST).desc(getIndependenceTestDesc()).hasArg().argName("string").build());
        options.put(CmdParams.SCORE, Option.builder().longOpt(CmdParams.SCORE).desc(getScoreDesc()).hasArg().argName("string").build());

        // tetrad parameters
        ParamDescriptions paramDescs = ParamDescriptions.getInstance();
        Set<String> params = paramDescs.getNames();
        params.forEach(param -> {
            ParamDescription paramDesc = paramDescs.get(param);
            String longOpt = param;
            String desc = paramDesc.getDescription();
            Serializable defaultVal = paramDesc.getDefaultValue();
            String argName = defaultVal.getClass().getSimpleName().toLowerCase();
            boolean hasArg = !(paramDesc.getDefaultValue() instanceof Boolean);
            Class type = paramDesc.getDefaultValue().getClass();
            options.put(param, Option.builder().longOpt(longOpt).desc(desc).hasArg(hasArg).type(type).argName(argName).build());
        });
    }

    private void addRequiredOptions() {
        options.put(CmdParams.ALGORITHM, Option.builder().longOpt(CmdParams.ALGORITHM).desc(getAlgorithmDesc()).hasArg().argName("string").required().build());
        options.put(CmdParams.DATASET, Option.builder().longOpt(CmdParams.DATASET).desc("Dataset. Multiple files are seperated by commas.").hasArg().argName("files").required().build());
        options.put(CmdParams.DELIMITER, Option.builder().longOpt(CmdParams.DELIMITER).desc(getDelimiterDesc()).hasArg().argName("string").required().build());
        options.put(CmdParams.DATA_TYPE, Option.builder().longOpt(CmdParams.DATA_TYPE).desc(getDataTypeDesc()).hasArg().argName("string").required().build());
    }

    public List<Option> getRequiredOptions() {
        return options.entrySet().stream()
                .filter(e -> e.getValue().isRequired())
                .map(e -> e.getValue())
                .collect(Collectors.toList());
    }

    private String getTimeoutDesc() {
        StringBuilder sb = new StringBuilder();
        sb.append("Set the time limit for graph searching. ");
        sb.append("Units: s=second, m=minute, h=hour, d=day. ");
        sb.append("For an example, 12m = 12 minutes");

        return sb.toString();
    }

    private String getDataTypeDesc() {
        return "Data type: " + DataTypes.getInstance().getNames().stream()
                .collect(Collectors.joining(", "));
    }

    private String getDelimiterDesc() {
        return "Delimiter: " + Delimiters.getInstance().getNames().stream()
                .collect(Collectors.joining(", "));
    }

    private String getScoreDesc() {
        return "Score: " + TetradScores.getInstance().getCommands().stream()
                .collect(Collectors.joining(", "));
    }

    private String getIndependenceTestDesc() {
        return "Independence Test: " + TetradIndependenceTests.getInstance().getCommands().stream()
                .collect(Collectors.joining(", "));
    }

    private String getAlgorithmDesc() {
        return "Algorithm: " + TetradAlgorithms.getInstance().getCommands().stream()
                .collect(Collectors.joining(", "));
    }

}